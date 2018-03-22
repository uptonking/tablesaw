package player.data.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * excel与csv文件的转换器
 * <p>
 * 注意：输入和输出文件名都是自己指定的，扩展名不一定与实际一致，要根据实际文件是.xls还是.xlsx调用方法
 * <p>
 * Using Apache POI API read Microsoft Excel (.xls) file and convert into CSV file with Java API.
 * Using Java API read Comma Separated Values(.csv) file and convert into XLS file with Apache POI API.
 * <p>
 * Microsoft Excel file is converted to CSV for all type of columns data.
 * </p>
 */
public class ExcelCSVConverter {

    /***
     * Date format used to convert excel cell date value
     */
    private static final String OUTPUT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Comma separated characters
     */
    private static final String CVS_SEPERATOR_CHAR = ",";
    /**
     * New line character for CSV file
     */
    private static final String NEW_LINE_CHARACTER = System.lineSeparator();

    /**
     * Convert CSV file to Excel 2007 file
     */
    public static void csvToExcel(String csvFileName, String excelFileName) throws Exception {

        validateFile(csvFileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFileName)));
//        HSSFWorkbook myWorkBook = new HSSFWorkbook();
        XSSFWorkbook myWorkBook = new XSSFWorkbook();
        FileOutputStream writer = new FileOutputStream(new File(excelFileName));
        XSSFSheet mySheet = myWorkBook.createSheet();
        String line = "";
        int rowNo = 0;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(CVS_SEPERATOR_CHAR);
            XSSFRow myRow = mySheet.createRow(rowNo);
            for (int i = 0; i < columns.length; i++) {
                XSSFCell myCell = myRow.createCell(i);
                myCell.setCellValue(columns[i]);
            }
            rowNo++;
        }
        myWorkBook.write(writer);
        writer.close();
    }

    /**
     * Convert the Excel 2007 file data into CSV file
     */
    public static void excelToCSV(String excelFileName, String csvFileName) throws Exception {

        validateFile(excelFileName);

        XSSFWorkbook myWorkBook = new XSSFWorkbook(new FileInputStream(new File(excelFileName)));
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator rowIt = mySheet.rowIterator();
        String csvData = "";

        while (rowIt.hasNext()) {
            XSSFRow myRow = (XSSFRow) rowIt.next();
            for (int i = 0; i < myRow.getLastCellNum(); i++) {
                csvData += getCellData(myRow.getCell(i));
            }
            csvData = csvData.substring(0, csvData.length() - 1);

            csvData += NEW_LINE_CHARACTER;

        }

        writeCSV(csvFileName, csvData);
    }

    /**
     * Convert the Excel 2003 file data into CSV file
     */
    public static void excel03ToCSV(String excelFileName, String csvFileName) throws Exception {

        validateFile(excelFileName);

        HSSFWorkbook myWorkBook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFileName)));
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator rowIt = mySheet.rowIterator();

        String csvData = "";
        while (rowIt.hasNext()) {
            HSSFRow myRow = (HSSFRow) rowIt.next();
            for (int i = 0; i < myRow.getLastCellNum(); i++) {
                csvData += get03CellData(myRow.getCell(i));
            }

            csvData = csvData.substring(0, csvData.length() - 1);

            csvData += NEW_LINE_CHARACTER;
        }
        writeCSV(csvFileName, csvData);
    }

    /**
     * Write the string into a text file
     */
    private static void writeCSV(String csvFileName, String csvData) throws Exception {
        FileOutputStream writer = new FileOutputStream(csvFileName);
        writer.write(csvData.getBytes());
        writer.close();
    }


    /**
     * Get cell value based on the excel column data type
     */
    private static String getCellData(XSSFCell myCell) throws Exception {
        String cellData = "";
        if (myCell == null) {
            cellData += CVS_SEPERATOR_CHAR;
        } else {
            switch (myCell.getCellType()) {
                case XSSFCell.CELL_TYPE_STRING:
                case XSSFCell.CELL_TYPE_BOOLEAN:
                    cellData += myCell.getRichStringCellValue() + CVS_SEPERATOR_CHAR;
                    break;
                case XSSFCell.CELL_TYPE_NUMERIC:
                    cellData += getNumericValue(myCell);
                    break;
                case XSSFCell.CELL_TYPE_FORMULA:
                    cellData += getFormulaValue(myCell);
                default:
                    cellData += CVS_SEPERATOR_CHAR;
            }
        }
        return cellData;
    }

    /**
     * Get cell value based on the excel column data type
     */
    private static String get03CellData(HSSFCell myCell) throws Exception {
        String cellData = "";
        if (myCell == null) {
            cellData += CVS_SEPERATOR_CHAR;
            ;
        } else {
            switch (myCell.getCellType()) {
                case HSSFCell.CELL_TYPE_STRING:
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cellData += myCell.getRichStringCellValue() + CVS_SEPERATOR_CHAR;
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    cellData += get03NumericValue(myCell);
                    break;
                case HSSFCell.CELL_TYPE_FORMULA:
                    cellData += get03FormulaValue(myCell);
                default:
                    cellData += CVS_SEPERATOR_CHAR;
                    ;
            }
        }
        return cellData;
    }

    /**
     * Get the formula value from a cell
     */
    private static String getFormulaValue(XSSFCell myCell) throws Exception {
        String cellData = "";
        if (myCell.getCachedFormulaResultType() == XSSFCell.CELL_TYPE_STRING || myCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
            cellData += myCell.getRichStringCellValue() + CVS_SEPERATOR_CHAR;
        } else if (myCell.getCachedFormulaResultType() == XSSFCell.CELL_TYPE_NUMERIC) {
            cellData += getNumericValue(myCell) + CVS_SEPERATOR_CHAR;
        }
        return cellData;
    }

    private static String get03FormulaValue(HSSFCell myCell) throws Exception {
        String cellData = "";
        if (myCell.getCachedFormulaResultType() == HSSFCell.CELL_TYPE_STRING || myCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            cellData += myCell.getRichStringCellValue() + CVS_SEPERATOR_CHAR;
        } else if (myCell.getCachedFormulaResultType() == HSSFCell.CELL_TYPE_NUMERIC) {
            cellData += get03NumericValue(myCell) + CVS_SEPERATOR_CHAR;
        }
        return cellData;
    }

    /**
     * Get the date or number value from a cell
     */
    private static String getNumericValue(XSSFCell myCell) throws Exception {
        String cellData = "";
        if (HSSFDateUtil.isCellDateFormatted(myCell)) {
            cellData += new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(myCell.getDateCellValue()) + CVS_SEPERATOR_CHAR;
        } else {
            cellData += new BigDecimal(myCell.getNumericCellValue()).toString() + CVS_SEPERATOR_CHAR;
        }
        return cellData;
    }

    private static String get03NumericValue(HSSFCell myCell) throws Exception {
        String cellData = "";
        if (HSSFDateUtil.isCellDateFormatted(myCell)) {
            cellData += new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(myCell.getDateCellValue()) + CVS_SEPERATOR_CHAR;
        } else {
            cellData += new BigDecimal(myCell.getNumericCellValue()).toString() + CVS_SEPERATOR_CHAR;
        }
        return cellData;
    }

    /**
     * 检查输入文件是否存在
     * 若不存在，则抛出异常，退出jvm
     */
    private static void validateFile(String fileName) {
        boolean valid = true;
        try {
            File f = new File(fileName);
            if (!f.exists() || f.isDirectory()) {
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
        }
        if (!valid) {
            System.out.println("File doesn't exist: " + fileName);
            System.exit(0);
        }
    }

//    public static void main(String[] args) throws Exception {
//        String excelfileName1 = "D:\\stephen\\files\\excel-file1.xls";
//        String csvFileName1 = "D:\\stephen\\files\\csv-file1.xls";
//        String excelfileName2 = "D:\\stephen\\files\\excel-file2.xls";
//        String csvFileName2 = "D:\\stephen\\files\\csv-file2.csv";
//        excelToCSV(excelfileName1, csvFileName1);
//        csvToEXCEL(csvFileName2, excelfileName2);
//    }

}
