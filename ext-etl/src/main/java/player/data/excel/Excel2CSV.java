package player.data.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Excel文件转换成CSV文件
 */
public class Excel2CSV {

    private final static String QUOTE = "\"";
    private final static String ESCAPED_QUOTE = "\\\"\\\"";
    private final static String COMMA = ",";
    private final static String NEW_LINE = "\n";

    private Workbook workbook = null;
    private ArrayList<ArrayList> data = null;
    private int maxWidth = 0;
    private DataFormatter formatter = null;
    private FormulaEvaluator evl = null;

    private File s;
    private File d;

    /**
     * @param src  原文件本身或所在目录
     * @param dest 目标csv目录 若不存在，则自动建立目录
     */
    public Excel2CSV(File src, File dest) {
        validateArgs(src, dest);
        this.s = src;
        this.d = dest;
    }

    /**
     * @param srcPath  原文件本身或所在目录
     * @param destPath 目标csv目录
     */
    public Excel2CSV(String srcPath, String destPath) {
        this(new File(srcPath), new File(destPath));
    }

    /**
     * 默认在原文件目录的 excel2csv子目录 生成csv文件
     *
     * @param srcPath 原文件目录
     */
    public Excel2CSV(String srcPath) {
        this(new File(srcPath), new File(srcPath.endsWith("/") ? (srcPath + "excel2csv") : (srcPath + "/excel2csv")));
    }

    public void convertExcel2CSV() throws IOException, InvalidFormatException {

        File[] files = s.isDirectory() ? s.listFiles(new ExcelFilenameFilter()) : new File[]{s};

        for (File xls : files) {
            File csv = new File(d, getCsvFileName(xls));

            openWorkbook(xls);

            createCsvFromSheet1();

            //TODO - add logic to go through file and remove leading/trailing commas, perhaps empty columns
            saveCsvFile(csv);
        }
    }

    private void validateArgs(File src, File dest) {
        if (!src.exists())
            throw new IllegalArgumentException(src.getName() + " does not exist.");

        if (!dest.exists()) {
            //throw new IllegalArgumentException(dest.getName() + " does not exist.");
            dest.mkdirs();
        }
        if (!dest.isDirectory())
            throw new IllegalArgumentException(dest.getName() + " is not a directory.");
    }

    private String getCsvFileName(File xls) {
        return xls.getName().substring(0, xls.getName().lastIndexOf(".")) + ".csv";
    }

    private void openWorkbook(File f) throws IOException, InvalidFormatException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            workbook = WorkbookFactory.create(fis);
            evl = workbook.getCreationHelper().createFormulaEvaluator();
            formatter = new DataFormatter(true);
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    private void createCsvFromSheet1() {

        data = new ArrayList<ArrayList>();

        // data in multiple sheets is appended to the single csv
        Sheet s = workbook.getSheetAt(0);
        if (s.getPhysicalNumberOfRows() > 0) {
            for (int j = 0; j <= s.getLastRowNum(); j++)
                addRowToCsv(s.getRow(j));
        }
    }

    private void createCsvFromSheetAll() {

        data = new ArrayList<ArrayList>();

        // data in multiple sheets is appended to the single csv
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet s = workbook.getSheetAt(i);
            if (s.getPhysicalNumberOfRows() > 0) {
                for (int j = 0; j <= s.getLastRowNum(); j++)
                    addRowToCsv(s.getRow(j));
            }
        }
    }

    private void saveCsvFile(File f) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < data.size(); i++) {
                StringBuilder b = new StringBuilder();
                ArrayList<String> line = data.get(i);
                for (int j = 0; j < maxWidth; j++) {
                    if (line.size() > j) {
                        String field = line.get(j);
                        if (field != null)
                            b.append(escape(field));
                    }
                    if (j < (maxWidth - 1))
                        b.append(COMMA);
                }
                bw.write(b.toString().trim());
                if (i < (data.size() - 1))
                    bw.newLine();
            }
        } finally {
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }
    }

    private String getData(Cell c) {
        if (c != null) {
            try {
                return (c.getCellType() != Cell.CELL_TYPE_FORMULA) ? formatter.formatCellValue(c) : formatter.formatCellValue(c, evl);
            } catch (Exception e) {
                System.out.println("Warning:  " + e.getMessage());
            }
        }
        return "";
    }

    private void addRowToCsv(Row r) {
        ArrayList<String> line = new ArrayList<String>();

        if (r != null) {
            int idx = r.getLastCellNum();
            for (int i = 0; i <= idx; i++)
                line.add(getData(r.getCell(i)));

            if (idx > maxWidth)
                maxWidth = idx;
        }
        data.add(line);
    }

    private StringBuffer appendQuote(StringBuffer b) {
        return b.insert(0, QUOTE).append(QUOTE);
    }

    // Unix:  use backslash escape: return field.replaceAll(COMMA, ("\\\\" + COMMA)).replaceAll(NEW_LINE, "\\\\" + NEW_LINE);
    private String escape(String f) {
        if (f.contains(QUOTE))
            f = f.replaceAll(QUOTE, ESCAPED_QUOTE);

        StringBuffer b = new StringBuffer(f);
        if ((b.indexOf(COMMA)) > -1 || (b.indexOf(NEW_LINE) > -1 || b.indexOf(QUOTE) > -1))
            b = appendQuote(b);

        return b.toString().trim();
    }

//    public static void main(String[] args) throws Exception {
//        if (args.length != 2) {
//            System.out.println("Usage: java ToCSV [Source File/Folder] ");
//            System.exit(0);
//        }
//        new Excel2CSV(new File(args[0]), new File(args[1]));
//    }

    /**
     * excel文件名过滤器
     */
    class ExcelFilenameFilter implements FilenameFilter {
        public boolean accept(File file, String name) {
            return (name.endsWith(".xls") || name.endsWith(".xlsx"));
        }
    }

}
