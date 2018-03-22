package com.opencsv;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andre Rosot
 */
public class CsvReaderHeaderAwareTest {

    private CSVReaderHeaderAware csvr;

    @Before
    public void setUpWithHeader() throws Exception {
        StringReader reader = createReader();
        csvr = new CSVReaderHeaderAware(reader);
    }

    @Test
    public void shouldKeepBasicParsing() throws IOException {
        String[] nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test quoted commas
        nextLine = csvr.readNext();
        assertEquals("a", nextLine[0]);
        assertEquals("b,b,b", nextLine[1]);
        assertEquals("c", nextLine[2]);

        // test empty elements
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test multiline quoted
        nextLine = csvr.readNext();
        assertEquals(3, nextLine.length);

        // test quoted quote chars
        nextLine = csvr.readNext();
        assertEquals("Glen \"The Man\" Smith", nextLine[0]);

        nextLine = csvr.readNext();
        assertEquals("\"\"", nextLine[0]); // check the tricky situation
        assertEquals("test", nextLine[1]); // make sure we didn't ruin the next field..

        nextLine = csvr.readNext();
        assertEquals(4, nextLine.length);

        assertEquals("a", csvr.readNext()[0]);

        //test end of stream
        assertNull(csvr.readNext());
    }

    @Test
    public void shouldRetrieveColumnsByHeaderName() throws IOException {
        assertEquals("a", csvr.readNext("first")[0]);
        assertEquals("a", csvr.readNext("first")[0]);
        assertEquals("", csvr.readNext("first")[0]);
        assertEquals("PO Box 123,\nKippax,ACT. 2615.\nAustralia", csvr.readNext("second")[0]);
    }

    @Test
    public void shouldRetrieveMultipleColumnsByHeaderName() throws IOException {
        String[] nextLine = csvr.readNext("first", "third");
        assertEquals("a", nextLine[0]);
        assertEquals("c", nextLine[1]);

        assertEquals("b,b,b", csvr.readNext("second")[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForInvalidColumn() throws IOException {
        csvr.readNext("fourth");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForInvalidColumnEvenAmongstValidOnes() throws IOException {
        csvr.readNext("first", "third", "fourth");
    }

    @Test(expected = IOException.class)
    public void shouldFailWhenNumberOfDataItemsIsLessThanHeader() throws IOException {
        csvr.skip(7);
        csvr.readNext("second");
    }

    @Test(expected = IOException.class)
    public void shouldFailWhenNumberOfDataItemsIsGreaterThanHeader() throws IOException {
        csvr.skip(6);
        csvr.readNext("second");
    }

    @Test
    public void shouldRetrieveMap() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(2);

        mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("PO Box 123,\nKippax,ACT. 2615.\nAustralia", mappedLine.get("second"));
        assertEquals("d.", mappedLine.get("third"));
    }

    @Test(expected = IOException.class)
    public void readMapThrowsExceptionIfNumberOfDataItemsIsGreaterThanHeader() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(5);

        mappedLine = csvr.readMap();
    }

    @Test(expected = IOException.class)
    public void readMapThrowsExceptionIfNumberOfDataItemsIsLessThanHeader() throws IOException {
        Map<String, String> mappedLine = csvr.readMap();
        assertEquals("a", mappedLine.get("first"));
        assertEquals("b", mappedLine.get("second"));
        assertEquals("c", mappedLine.get("third"));

        csvr.skip(6);

        mappedLine = csvr.readMap();
    }

    @Test
    public void shouldReturnNullWhenFileIsOver() throws IOException {
        csvr.skip(8);
        assertNull(csvr.readMap());
    }

    @Test
    public void readNextWhenPastEOF() throws IOException {
        csvr.skip(8);
        assertNull(csvr.readNext("first"));
    }

    @Test
    public void shouldInitialiseHeaderWithCompleteConstrucotr() throws IOException {
        ICSVParser parser = mock(ICSVParser.class);
        when(parser.parseLineMulti(anyString())).thenReturn(new String[]{"myHeader"});
        CSVReaderHeaderAware reader = new CSVReaderHeaderAware(createReader(), 0, parser, false, false, 1, Locale.getDefault());
        assertThat(reader.readMap().keySet().iterator().next(), is("myHeader"));
    }

    private StringReader createReader() {
        StringBuilder sb = new StringBuilder(ICSVParser.INITIAL_READ_SIZE);
        sb.append("first,second,third\n");
        sb.append("a,b,c").append("\n");   // standard case
        sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
        sb.append(",,").append("\n"); // empty elements
        sb.append("a,\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\",d.\n");
        sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
        sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
        sb.append("\"a\nb\",b,\"\nd\",e\n");
        sb.append("a");
        return new StringReader(sb.toString());
    }
}
