package com.opencsv.bean;

/*
 Copyright 2007 Kyle Miller.

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

import com.opencsv.CSVReader;
import com.opencsv.bean.mocks.MockBean;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.Before;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import org.junit.After;

import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class HeaderColumnNameMappingStrategyTest {

   private static final String TEST_STRING = "name,orderNumber,num\n" +
         "kyle,abc123456,123\n" +
         "jimmy,def098765,456";

   private static final String TEST_QUOTED_STRING = "\"name\",\"orderNumber\",\"num\"\n" +
         "\"kyle\",\"abc123456\",\"123\"\n" +
         "\"jimmy\",\"def098765\",\"456\"";

   private HeaderColumnNameMappingStrategy<MockBean> strat;
   private static Locale systemLocale;

    @BeforeClass
    public static void storeSystemLocale() {
        systemLocale = Locale.getDefault();
    }

    @After
    public void setSystemLocaleBackToDefault() {
        Locale.setDefault(systemLocale);
    }

   @Before
   public void setUp() {
      Locale.setDefault(Locale.US);
      strat = new HeaderColumnNameMappingStrategy<>();
   }

   private List<MockBean> createTestParseResult(String parseString) {
      strat.setType(MockBean.class);
      CsvToBean<MockBean> csv = new CsvToBean<>();
      return csv.parse(strat, new StringReader(parseString));
   }

   @Test
   public void getColumnIndexWithoutHeaderThrowsException() {
       String englishErrorMessage = null;
       try {
           strat.getColumnIndex("some index name");
           fail("An IllegalStateException should have been thrown since the header has not yet been read.");
       }
       catch(IllegalStateException e) {
           englishErrorMessage = e.getLocalizedMessage();
       }
       
       // Now with another locale
       strat.setErrorLocale(Locale.GERMAN);
       try {
           strat.getColumnIndex("some index name");
           fail("An IllegalStateException should have been thrown since the header has not yet been read.");
       }
       catch(IllegalStateException e) {
           assertNotEquals(englishErrorMessage, e.getLocalizedMessage());
       }
   }

   @Test
   public void getColumnIndexAfterParse() {
      createTestParseResult(TEST_STRING);
      assertEquals(0, strat.getColumnIndex("name").intValue());
      assertEquals(1, strat.getColumnIndex("orderNumber").intValue());
      assertEquals(2, strat.getColumnIndex("num").intValue());
      assertNull(strat.getColumnIndex("unknown column"));
   }

   @Test
   public void testParse() {
      List<MockBean> list = createTestParseResult(TEST_STRING);
      assertNotNull(list);
      assertTrue(list.size() == 2);
      MockBean bean = list.get(0);
      assertEquals("kyle", bean.getName());
      assertEquals("abc123456", bean.getOrderNumber());
      assertEquals(123, bean.getNum());
   }

   @Test
   public void testQuotedString() {
      List<MockBean> list = createTestParseResult(TEST_QUOTED_STRING);
      assertNotNull(list);
      assertTrue(list.size() == 2);
      MockBean bean = list.get(0);
      assertEquals("kyle", bean.getName());
      assertEquals("abc123456", bean.getOrderNumber());
      assertEquals(123, bean.getNum());
   }

   @Test
   public void testParseWithSpacesInHeader() {
      List<MockBean> list = createTestParseResult(TEST_STRING);
      assertNotNull(list);
      assertTrue(list.size() == 2);
      MockBean bean = list.get(0);
      assertEquals("kyle", bean.getName());
      assertEquals("abc123456", bean.getOrderNumber());
      assertEquals(123, bean.getNum());
   }

   @Test
   public void verifyColumnNames() throws IOException, CsvRequiredFieldEmptyException {
      strat = new HeaderColumnNameMappingStrategy<>();
      strat.setType(MockBean.class);
      assertNull(strat.getColumnName(0));
      assertNull(strat.findDescriptor(0));

      StringReader reader = new StringReader(TEST_STRING);

      CSVReader csvReader = new CSVReader(reader);
      strat.captureHeader(csvReader);

      assertEquals("name", strat.getColumnName(0));
      assertEquals(strat.findDescriptor(0), strat.findDescriptor("name"));
      assertEquals("name", strat.findDescriptor("name").getName());
   }
   
   @Test
   public void throwsIllegalStateExceptionIfTypeNotSetBeforeParse() {
      strat = new HeaderColumnNameMappingStrategy<>();
      StringReader reader = new StringReader(TEST_STRING);
      CSVReader csvReader = new CSVReader(reader);
      CsvToBean csvtb = new CsvToBean();
      try {
          csvtb.parse(strat, csvReader);
      }
      catch(RuntimeException e) {
          assertEquals(IllegalStateException.class, e.getCause().getClass());
      }
   }
   
   @Test(expected = IllegalStateException.class)
   public void throwsIllegalStateExceptionIfTypeNotSetBeforeGenerateHeaders() throws CsvRequiredFieldEmptyException {
      strat = new HeaderColumnNameMappingStrategy<>();
      strat.generateHeader(new MockBean());
   }
}
