package integrationTest.Bug143;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180ParserBuilder;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by scott on 5/14/17.
 */
public class ReaderPrinter {

    private static final String IMPORT_FILE = "src/test/resources/Bug143.csv";
    private static final String SEPARATOR = "\n===========================================================\n";

    public static void main(String[] args) throws IOException {

        RFC4180ParserBuilder rfc4180ParserBuilder = new RFC4180ParserBuilder();
        ICSVParser rfc4180Parser = rfc4180ParserBuilder.build();
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader(IMPORT_FILE));
        CSVReader reader = builder.withCSVParser(rfc4180Parser).withKeepCarriageReturn(true).build();

        String[] headers = reader.readNext();
        String[] line;

        for (int rows = 1; headers != null && (line = reader.readNext()) != null; rows++) {
            printInformationAboutRow(headers, line, rows);
        }
    }

    private static void printInformationAboutRow(String[] headers, String[] line, int rowNumber) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("Row:  " + rowNumber + "\t\tData values: " + valuesMatch(headers, line) + "\n");

        for (int i = 0, max = Math.max(headers.length, line.length); i < max; i++) {
            System.out.println("\t\t" + arrayValue(headers, i) + " :\t" + arrayValue(line, i));
        }
    }

    private static String arrayValue(String[] items, int i) {
        return i >= items.length ? "OUT OF RANGE" : items[i];
    }

    private static String valuesMatch(String[] headers, String[] line) {
        if (headers.length == line.length) {
            return "match";
        }
        return "Do not match!  Number of Headers: " + headers.length + "\t\tNumber of items: " + line.length;
    }

}
