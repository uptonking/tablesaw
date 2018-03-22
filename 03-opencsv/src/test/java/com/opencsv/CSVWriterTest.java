package com.opencsv;
/*
 Copyright 2015 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CSVWriterTest {

   private static final String SIMPLE_STRING = "XXX";
   private static final String[] SIMPLE_STRING_ARRAY = new String[]{SIMPLE_STRING};

   /**
    * Test routine for converting output to a string.
    *
    * @param args the elements of a line of the cvs file
    * @return a String version
    * @throws IOException if there are problems writing
    */
   private String invokeWriter(String ... args) {
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, ',', '\'');
      csvw.writeNext(args);
      return sw.toString();
   }

   private String invokeNoEscapeWriter(String ... args) {
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, '\'', CSVWriter.NO_ESCAPE_CHARACTER);
      csvw.writeNext(args);
      return sw.toString();
   }

   @Test
   public void correctlyParseNullString() {
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, ',', '\'');
      csvw.writeNext(null);
      assertEquals(0, sw.toString().length());
   }

   @Test
   public void correctlyParserNullObject() {
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, ',', '\'');
      csvw.writeNext(null, false);
      assertEquals(0, sw.toString().length());
   }

   /**
    * Tests parsing individual lines.
    *
    * @throws IOException if the reader fails.
    */
   @Test
   public void testParseLine() throws IOException {

      // test normal case
      String[] normal = {"a", "b", "c"};
      String output = invokeWriter(normal);
      assertEquals("'a','b','c'\n", output);

      // test quoted commas
      String[] quoted = {"a", "b,b,b", "c"};
      output = invokeWriter(quoted);
      assertEquals("'a','b,b,b','c'\n", output);

      // test empty elements
      String[] empty = {,};
      output = invokeWriter(empty);
      assertEquals("\n", output);

      // test multiline quoted
      String[] multiline = {"This is a \n multiline entry", "so is \n this"};
      output = invokeWriter(multiline);
      assertEquals("'This is a \n multiline entry','so is \n this'\n", output);


      // test quoted line
      String[] quoteLine = {"This is a \" multiline entry", "so is \n this"};
      output = invokeWriter(quoteLine);
      assertEquals("'This is a \"\" multiline entry','so is \n this'\n", output);

   }

   @Test
   public void testSpecialCharacters() throws IOException {
      // test quoted line
      String output = invokeWriter("This is a \r multiline entry", "so is \n this");
      assertEquals("'This is a \r multiline entry','so is \n this'\n", output);
   }

   @Test
   public void parseLineWithBothEscapeAndQuoteChar() throws IOException {
      // test quoted line
      String output = invokeWriter("This is a 'multiline' entry", "so is \n this");
      assertEquals("'This is a \"'multiline\"' entry','so is \n this'\n", output);
   }

   /**
    * Tests parsing individual lines.
    *
    * @throws IOException if the reader fails.
    */
   @Test
   public void testParseLineWithNoEscapeChar() throws IOException {

      // test normal case
      String[] normal = {"a", "b", "c"};
      String output = invokeNoEscapeWriter(normal);
      assertEquals("'a','b','c'\n", output);

      // test quoted commas
      String[] quoted = {"a", "b,b,b", "c"};
      output = invokeNoEscapeWriter(quoted);
      assertEquals("'a','b,b,b','c'\n", output);

      // test empty elements
      String[] empty = {,};
      output = invokeNoEscapeWriter(empty);
      assertEquals("\n", output);

      // test multiline quoted
      String[] multiline = {"This is a \n multiline entry", "so is \n this"};
      output = invokeNoEscapeWriter(multiline);
      assertEquals("'This is a \n multiline entry','so is \n this'\n", output);
   }

   @Test
   public void parseLineWithNoEscapeCharAndQuotes() throws IOException {
      String output = invokeNoEscapeWriter("This is a \" 'multiline' entry", "so is \n this");
      assertEquals("'This is a \" 'multiline' entry','so is \n this'\n", output);
   }


   /**
    * Test writing to a list.
    *
    * @throws IOException if the reader fails.
    */
   @Test
   public void testWriteAllAsList() throws IOException {

      List<String[]> allElements = new ArrayList<>();
      String[] line1 = "Name#Phone#Email".split("#");
      String[] line2 = "Glen#1234#glen@abcd.com".split("#");
      String[] line3 = "John#5678#john@efgh.com".split("#");
      allElements.add(line1);
      allElements.add(line2);
      allElements.add(line3);

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.writeAll(allElements);

      csvw.close();
      assertFalse(csvw.checkError());
      String result = sw.toString();
      String[] lines = result.split("\n");

      assertEquals(3, lines.length);
      assertEquals("\"Name\",\"Phone\",\"Email\"", lines[0]);
      assertEquals("\"Glen\",\"1234\",\"glen@abcd.com\"", lines[1]);
      assertEquals("\"John\",\"5678\",\"john@efgh.com\"", lines[2]);
   }

   /**
    * Test writing to an iterator.
    *
    * @throws IOException if the reader fails.
    */
   @Test
   public void testWriteAllAsIterable() throws IOException {
      final String[] line1 = "Name#Phone#Email".split("#");
      final String[] line2 = "Glen#1234#glen@abcd.com".split("#");
      final String[] line3 = "John#5678#john@efgh.com".split("#");

      Iterable iterable = mock(Iterable.class);

      Answer<Iterator> iteratorAnswer = new Answer<Iterator>() {
         @Override
         public Iterator answer(InvocationOnMock invocationOnMock) {
            Iterator<String[]> iterator = mock(Iterator.class);
            when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true)
                    .thenReturn(false);
            when(iterator.next()).thenReturn(line1).thenReturn(line2).thenReturn(line3)
                    .thenThrow(NoSuchElementException.class);
            return iterator;
         }
      };
      when(iterable.iterator()).then(iteratorAnswer);

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.writeAll(iterable);

      csvw.close();
      assertFalse(csvw.checkError());

      String result = sw.toString();
      String[] lines = result.split("\n");

      assertEquals(3, lines.length);
      assertEquals("\"Name\",\"Phone\",\"Email\"", lines[0]);
      assertEquals("\"Glen\",\"1234\",\"glen@abcd.com\"", lines[1]);
      assertEquals("\"John\",\"5678\",\"john@efgh.com\"", lines[2]);
   }

   /**
    * Test writing from a list.
    *
    * @throws IOException if the reader fails.
    */
   @Test
   public void testWriteAllObjects() {

      List<String[]> allElements = new ArrayList<>(3);
      String[] line1 = "Name#Phone#Email".split("#");
      String[] line2 = "Glen#1234#glen@abcd.com".split("#");
      String[] line3 = "John#5678#john@efgh.com".split("#");
      allElements.add(line1);
      allElements.add(line2);
      allElements.add(line3);

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.writeAll(allElements, false);

      String result = sw.toString();
      String[] lines = result.split("\n");

      assertEquals(3, lines.length);

      String[] values = lines[1].split(",");
      assertEquals("1234", values[1]);
   }

   /**
    * Tests the option of having omitting quotes in the output stream.
    *
    * @throws IOException if bad things happen
    */
   @Test
   public void testNoQuoteChars() {

      String[] line = {"Foo", "Bar", "Baz"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
      csvw.writeNext(line);
      String result = sw.toString();

      assertEquals("Foo,Bar,Baz\n", result);
   }

   /**
    * Tests the option of having omitting quotes in the output stream.
    *
    * @throws IOException if bad things happen
    */
   @Test
   public void testNoQuoteCharsAndNoEscapeChars() {

      String[] line = {"Foo", "Bar", "Baz"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
      csvw.writeNext(line);
      String result = sw.toString();

      assertEquals("Foo,Bar,Baz\n", result);
   }

   /**
    * Tests the ability for the writer to apply quotes only where strings contain the separator, escape, quote or new line characters.
    */
   @Test
   public void testIntelligentQuotes() {
      String[] line = {"1", "Foo", "With,Separator", "Line\nBreak", "Hello \"Foo Bar\" World", "Bar"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
      csvw.writeNext(line, false);
      String result = sw.toString();

      assertEquals("1,Foo,\"With,Separator\",\"Line\nBreak\",\"Hello \"\"Foo Bar\"\" World\",Bar\n", result);
   }


   /**
    * Test null values.
    *
    * @throws IOException if bad things happen
    */
   @Test
   public void testNullValues() {

      String[] line = {"Foo", null, "Bar", "baz"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.writeNext(line);
      String result = sw.toString();

      assertEquals("\"Foo\",,\"Bar\",\"baz\"\n", result);
   }

   @Test
   public void testStreamFlushing() throws IOException {

      String WRITE_FILE = "myfile.csv";
      File tester = new File(WRITE_FILE);

      assertFalse(tester.exists());

      String[] nextLine = new String[]{"aaaa", "bbbb", "cccc", "dddd"};

      FileWriter fileWriter = new FileWriter(WRITE_FILE);

      CSVWriter writer = new CSVWriter(fileWriter);

      writer.writeNext(nextLine);

      // If this line is not executed, it is not written in the file.
      writer.close();

      assertTrue(tester.exists());
      // cleanup
      tester.delete();

   }

   @Test(expected = IOException.class)
   public void flushWillThrowIOException() throws IOException {
      String[] line = {"Foo", "bar's"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriterExceptionThrower(sw);
      csvw.writeNext(line);
      csvw.flush();
   }

   @Test
   public void flushQuietlyWillNotThrowException() {
      String[] line = {"Foo", "bar's"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriterExceptionThrower(sw);
      csvw.writeNext(line);
      csvw.flushQuietly();
   }

   @Test
   public void testAlternateEscapeChar() {
      String[] line = {"Foo", "bar's"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, '\'');
      csvw.writeNext(line);
      assertEquals("\"Foo\",\"bar''s\"\n", sw.toString());
   }

   @Test
   public void embeddedQuoteInString() {
      String[] line = {"Foo", "I choose a \\\"hero\\\" for this adventure"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
      csvw.writeNext(line);
      assertEquals("\"Foo\",\"I choose a \\\"hero\\\" for this adventure\"\n", sw.toString());
   }

   @Test
   public void testNoQuotingNoEscaping() {
      String[] line = {"\"Foo\",\"Bar\""};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
      csvw.writeNext(line);
      assertEquals("\"Foo\",\"Bar\"\n", sw.toString());
   }

   @Test
   public void testNestedQuotes() {
      String[] data = new String[]{"\"\"", "test"};
      String oracle = "\"\"\"\"\"\",\"test\"\n";

      CSVWriter writer = null;
      File tempFile = null;
      FileWriter fwriter = null;

      try {
         tempFile = File.createTempFile("csvWriterTest", ".csv");
         tempFile.deleteOnExit();
         fwriter = new FileWriter(tempFile);
         writer = new CSVWriter(fwriter);
      } catch (IOException e) {
         fail();
      }

      // write the test data:
      writer.writeNext(data);

      try {
         writer.close();
      } catch (IOException e) {
         fail();
      }

      try {
         // assert that the writer was also closed.
         fwriter.flush();
         fail();
      } catch (IOException e) {
         // we should go through here..
      }

      // read the data and compare.
      FileReader in = null;
      try {
         in = new FileReader(tempFile);
      } catch (FileNotFoundException e) {
         fail();
      }

      StringBuilder fileContents = new StringBuilder(CSVWriter.INITIAL_STRING_SIZE);
      try {
         int ch;
         while ((ch = in.read()) != -1) {
            fileContents.append((char) ch);
         }
         in.close();
      } catch (IOException e) {
         fail();
      }

      assertTrue(oracle.equals(fileContents.toString()));
   }

   @Test
   public void testAlternateLineFeeds() {
      String[] line = {"Foo", "Bar", "baz"};
      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, "\r");
      csvw.writeNext(line);
      String result = sw.toString();

      assertTrue(result.endsWith("\r"));
   }

   @Test
   public void testResultSetWithHeaders() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
      assertEquals(2, linesWritten);
   }

   @Test
   public void testResultSetWithHeadersWithoutQuotes() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, true, false, false); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("Foo,Bar,baz\nv1,v2,v3\n", result);
      assertEquals(2, linesWritten);
   }

   @Test
   public void testMultiLineResultSetWithHeaders() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);

      int linesWritten = csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
      assertEquals(4, linesWritten);
   }

   @Test
   public void testResultSetWithoutHeaders() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("\"v1\",\"v2\",\"v3\"\n", result);
      assertEquals(1, linesWritten);
   }

   @Test
   public void testResultSetWithoutHeadersAndQuotes() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, false, false, false); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("v1,v2,v3\n", result);
      assertEquals(1, linesWritten);
   }
   
   @Test
   public void testMultiLineResultSetWithoutHeaders() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);

      int linesWritten = csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.

      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
      assertEquals(3, linesWritten);
   }

   @Test
   public void testResultSetTrim() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1         ", "v2 ", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw);
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, true, true); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
      assertEquals(2, linesWritten);
   }

   @Test
   public void needToSetBothQuoteAndEscapeCharIfYouWantThemToBeTheSame() throws SQLException, IOException {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2'v2a", "v3"};

      StringWriter sw = new StringWriter();
      CSVWriter csvw = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, '\'', '\'');
      csvw.setResultService(new ResultSetHelperService());

      ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

      int linesWritten = csvw.writeAll(rs, true, true); // don't need a result set since I am mocking the result.
      assertFalse(csvw.checkError());
      String result = sw.toString();

      assertNotNull(result);
      assertEquals("'Foo','Bar','baz'\n'v1','v2''v2a','v3'\n", result);
      assertEquals(2, linesWritten);
   }

   @Test
   public void issue123SeparatorEscapedWhenQuoteIsNoQuoteChar() {
      String[] header = {"Foo", "Bar", "baz"};
      String[] value = {"v1", "v2" + CSVWriter.DEFAULT_ESCAPE_CHARACTER + "v2a", "v3"};

      List<String[]> lines = new ArrayList<>();
      lines.add(header);
      lines.add(value);
      StringWriter sw = new StringWriter();
      CSVWriter writer = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
      writer.writeAll(lines);

      String result = sw.toString();
      assertNotNull(result);
      assertEquals("Foo,Bar,baz\nv1,v2" + CSVWriter.DEFAULT_ESCAPE_CHARACTER + CSVWriter.DEFAULT_ESCAPE_CHARACTER + "v2a,v3\n", result);
   }

   @Test
   public void issue123SeparatorEscapedWhenQuoteIsNoQuoteCharSpecifingNoneDefaultEscapeChar() {
      String[] header = {"Foo", "Bar", "baz"};
      char escapeCharacter = '\\';
      String[] value = {"v1", "v2" + escapeCharacter + "v2a" + CSVWriter.DEFAULT_SEPARATOR + "v2b", "v3"};
      List<String[]> lines = new ArrayList<>();
      lines.add(header);
      lines.add(value);
      StringWriter sw = new StringWriter();
      CSVWriter writer = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, escapeCharacter);
      writer.writeAll(lines);

      String result = sw.toString();
      assertNotNull(result);
      assertEquals("Foo,Bar,baz\nv1,v2" + escapeCharacter + escapeCharacter + "v2a" + escapeCharacter + CSVWriter.DEFAULT_SEPARATOR + "v2b,v3\n", result);
   }

   @Test
   public void issue136escapeNewLineCharactersWhenNoQuoteCharIsSet() {
      String[] header = {"Foo", "Bar", "baz"};
      char escapeCharacter = '\\';
      String[] value = {"v1", "v2", "v3\n"};
      List<String[]> lines = new ArrayList<>();
      lines.add(header);
      lines.add(value);
      StringWriter sw = new StringWriter();
      CSVWriter writer = new CSVWriter(sw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, escapeCharacter);
      writer.writeAll(lines);

      String result = sw.toString();
      assertNotNull(result);
      assertEquals("Foo,Bar,baz\nv1,v2,v3" + escapeCharacter + "\n\n", result);
   }
   @Test
   public void testIOException() throws IOException {
      Writer writer = mock(Writer.class);
      doThrow(IOException.class).when(writer).write(anyString());
      
      // Using writeNext()
      CSVWriter csvWriter = new CSVWriter(writer);
      csvWriter.writeNext(SIMPLE_STRING_ARRAY);
      csvWriter.close();
      assertTrue(csvWriter.checkError());
      
      // Using writeAll(Iterable<String[]>, boolean)
      csvWriter = new CSVWriter(writer);
      csvWriter.writeAll(Collections.singletonList(SIMPLE_STRING_ARRAY), false);
      csvWriter.close();
      assertTrue(csvWriter.checkError());
      
      // Using writeAll(Iterable<String[]>)
      csvWriter = new CSVWriter(writer);
      csvWriter.writeAll(Collections.singletonList(SIMPLE_STRING_ARRAY));
      csvWriter.close();
      assertTrue(csvWriter.checkError());
   }

   @Test
   public void checkErrorReturnsTrueWhenPassedInPrintWriter() throws IOException {
      Writer writer = mock(Writer.class);
      doThrow(IOException.class).when(writer).write(anyString(), anyInt(), anyInt());

      PrintWriter printWriter = new PrintWriter(writer);

      CSVWriter csvWriter = new CSVWriter(printWriter);

      csvWriter.writeNext(SIMPLE_STRING_ARRAY);

      csvWriter.close();

      assertTrue(csvWriter.checkError());
   }
}
