package player.data.format;

import com.opencsv.CSVReader;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Excel读取器
 * reads both XLSX, XLS and CSV files and converts Excel rows to Java Objects.
 */
public class ExcelReader<T> {

    private static final int MAX_SHEET = 1;
    private static final char CSV_DELIMITER = ',';

    private Builder<T> info;

    /**
     * 行转换器
     * 一行的数据数组转换成bean
     */
    public interface RowConverter<T> {
        T convert(Object[] row);
    }

    /**
     * 建造者
     */
    public static class Builder<T> {
        private boolean hasHeader;
        private RowConverter<T> converter;
        private int sheets;
        private char delimiter = CSV_DELIMITER;

        public Builder() {
        }

        public Builder<T> converter(RowConverter<T> converter) {
            this.converter = converter;
            return this;
        }

        public Builder<T> withHeader() {
            this.hasHeader = true;
            return this;
        }

        public Builder<T> sheets(int sheetCount) {
            this.sheets = sheetCount;
            return this;
        }

        public Builder<T> csvDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * 创建一个ExcelReader
         */
        public ExcelReader<T> build() {
            return new ExcelReader<T>(this);
        }

    }

    public static <T> Builder<T> builder(Class<T> cls) {
        return new Builder<T>();
    }

    private ExcelReader(Builder<T> info) {
        this.info = info;
    }

    public List<T> read(String fileName) throws Exception {
        try (FileInputStream is = new FileInputStream(fileName)) {
            return read(is);
        }
    }

    public List<T> read(InputStream is) throws Exception {
        List<T> objList = null;
        try (BufferedInputStream buf = new BufferedInputStream(is)) {

            if (isExcel(buf)) {
                // XLSX, XLS
                objList = readExcel(buf);

            } else {
                // CSV
                objList = readCsv(buf);
            }
        }
        return objList;
    }

    private List<T> readExcel(InputStream is) throws Exception {
        Workbook workbook = WorkbookFactory.create(is);
        int sheetCount = Math.min(workbook.getNumberOfSheets(), MAX_SHEET);
        List<T> objList = new ArrayList<>();
        sheetCount = (info.sheets == 0) ? sheetCount : info.sheets;
        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            extractSheet(sheet, objList);
        }
        return objList;
    }

    private List<T> readCsv(InputStream in) throws Exception {
        List<T> objList = new ArrayList<>();
        InputStreamReader isr = new InputStreamReader(in);
        try (CSVReader cvsr = new CSVReader(isr, info.delimiter)) {
            List<String[]> allRows = cvsr.readAll();
            int start = info.hasHeader ? 1 : 0;
            for (int i = start; i < allRows.size(); i++) {
                T obj = info.converter.convert(allRows.get(i));
                objList.add(obj);
            }
        }
        return objList;
    }

    private void extractSheet(Sheet sheet, List<T> objList) {
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext() && info.hasHeader) {
            rowIterator.next();
        }
        while (rowIterator.hasNext()) {
            T obj = extractObject(rowIterator);
            objList.add(obj);
        }
    }

    private T extractObject(Iterator<Row> rowIterator) {
        Row row = rowIterator.next();
        Iterator<Cell> cellIterator = row.cellIterator();
        Object[] rowVals = new Object[row.getLastCellNum()];
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            rowVals[cell.getColumnIndex()] = getValue(cell);
        }
        return info.converter.convert(rowVals);
    }

    private boolean isExcel(InputStream is) throws Exception {

        return POIXMLDocument.hasOOXMLHeader(is) /* .xlsx */
                || POIFSFileSystem.hasPOIFSHeader(is); /* .xls */
    }

    private Object getValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            case Cell.CELL_TYPE_BLANK:
                return null;
        }
        return null;
    }

}
