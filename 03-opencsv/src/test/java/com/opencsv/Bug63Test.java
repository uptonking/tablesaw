package com.opencsv;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by scott on 3/4/17.
 */
public class Bug63Test {

    private static String[] fields = new String[5];
    private StringWriter sw;
    private CSVWriter csvWriter;
    private StringReader sr;


    @Test
    public void mappingStrategyRead() throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        String expectedString = "\"contentsID\";\"key\";\"position\";\"text\"\n" +
                "\"\";\"\";\"1\";\"this is a test for row one\"\n" +
                "\"\";\"\";\"2\";\"this is a test for row, two\"\n";

        Contents c2 = new Contents();
        List<Contents> cl2 = new ArrayList<>();

        c2.setKey("");
        c2.setPosition(1);
        c2.setText("this is a test for row one");
        cl2.add(c2);

        c2 = new Contents();
        c2.setKey("");
        c2.setPosition(2);
        c2.setText("this is a test for row, two");
        cl2.add(c2);

        StringWriter writer = new StringWriter();

        StatefulBeanToCsv<Contents> bToC = new StatefulBeanToCsvBuilder(writer)
                .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .withSeparator(';')
                .build();


        bToC.write(cl2);
        assertEquals(expectedString, writer.toString());

    }

    public static class Contents {

        private Integer contentsID;
        private String key;
        private Integer position;
        private String text;

        public Integer getContentsID() {
            return contentsID;
        }

        public void setContentsID(Integer contentsID) {
            this.contentsID = contentsID;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "Contents [contentsID=" + contentsID + ", key=" + key + ", position=" + position + ", text=" + text
                    + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((contentsID == null) ? 0 : contentsID.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((position == null) ? 0 : position.hashCode());
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Contents other = (Contents) obj;
            if (contentsID == null) {
                if (other.contentsID != null)
                    return false;
            } else if (!contentsID.equals(other.contentsID))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (position == null) {
                if (other.position != null)
                    return false;
            } else if (!position.equals(other.position))
                return false;
            if (text == null) {
                return other.text == null;
            } else return text.equals(other.text);
        }
    }

}
