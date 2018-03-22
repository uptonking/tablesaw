/*
 * Copyright 2017 Andrew Rucker Jones.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opencsv.bean;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.mocks.*;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.Assert.*;
import org.junit.Test;

public class CsvToBeanAsIteratorTest {

    private static final String TEST_STRING = "name,orderNumber,num\n"
            + "kyle,abc123456,123\n"
            + "jimmy,def098765,456 ";

    private CSVReader createReader() {
        return createReader(TEST_STRING);
    }

    private CSVReader createReader(String testString) {
        StringReader reader = new StringReader(testString);
        return new CSVReader(reader);
    }
    
    private class FilterSmallNumbers implements CsvToBeanFilter {
        
        private final MappingStrategy strategy;
        
        public FilterSmallNumbers(MappingStrategy strategy) {
            this.strategy = strategy;
        }
        
        @Override
        public boolean allowLine(String[] line) {
            int index = strategy.getColumnIndex("num");
            return Integer.parseInt(line[index].trim()) > 200;
        }
    }

    @Test(expected = RuntimeException.class)
    public void throwRuntimeExceptionWhenExceptionIsThrown() {
        CsvToBean bean = new CsvToBean();
        bean.setMappingStrategy(new ErrorHeaderMappingStrategy());
        bean.setCsvReader(createReader());
        for (Object o : bean) {
        }
    }

    @Test(expected = RuntimeException.class)
    public void throwRuntimeExceptionLineWhenExceptionIsThrown() {
        CsvToBean bean = new CsvToBean();
        bean.setMappingStrategy(new ErrorLineMappingStrategy());
        bean.setCsvReader(createReader());
        for (Object o : bean) {
        }
    }

    @Test
    public void parseBeanWithNoAnnotations() {
        HeaderColumnNameMappingStrategy<MockBean> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(MockBean.class);
        CsvToBean<MockBean> bean = new CsvToBean<>();
        bean.setMappingStrategy(strategy);
        bean.setCsvReader(createReader());

        Iterator<MockBean> it = bean.iterator();
        assertTrue(it.hasNext());
        assertEquals(createMockBean("kyle", "abc123456", 123), it.next());
        assertTrue(it.hasNext());
        assertEquals(createMockBean("jimmy", "def098765", 456), it.next());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Iterator should have thrown an exception trying to access the element after the end of the list");
        } catch (NoSuchElementException e) {/* Good. */
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorShouldNotSupportRemove() {
        HeaderColumnNameMappingStrategy<MockBean> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(MockBean.class);
        CsvToBean<MockBean> bean = new CsvToBean<>();
        bean.setMappingStrategy(strategy);
        bean.setCsvReader(createReader());

        Iterator<MockBean> it = bean.iterator();
        it.remove();
    }

    private MockBean createMockBean(String name, String orderNumber, int num) {
        MockBean mockBean = new MockBean();
        mockBean.setName(name);
        mockBean.setOrderNumber(orderNumber);
        mockBean.setNum(num);
        return mockBean;
    }

    @Test
    public void bug133ShouldNotThrowNullPointerExceptionWhenProcessingEmptyWithNoAnnotations() {
        HeaderColumnNameMappingStrategy<Bug133Bean> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(Bug133Bean.class);

        StringReader reader = new StringReader("one;two;three\n"
                + "kyle;;123\n"
                + "jimmy;;456 ");

        CSVParserBuilder parserBuilder = new CSVParserBuilder();
        CSVReaderBuilder readerBuilder = new CSVReaderBuilder(reader);

        CSVParser parser = parserBuilder.withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).withSeparator(';').build();
        CSVReader csvReader = readerBuilder.withCSVParser(parser).build();

        CsvToBean<Bug133Bean> bean = new CsvToBean<>();
        bean.setMappingStrategy(strategy);
        bean.setCsvReader(csvReader);

        Iterator<Bug133Bean> it = bean.iterator();
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalStateException.class)
    public void throwIllegalStateWhenParseWithoutArgumentsIsCalled() {
        CsvToBean csvtb = new CsvToBean();
        csvtb.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void throwIllegalStateWhenOnlyReaderIsSpecifiedToParseWithoutArguments() {
        CsvToBean csvtb = new CsvToBean();
        csvtb.setCsvReader(new CSVReader(new StringReader(TEST_STRING)));
        csvtb.iterator();
    }

    @Test(expected = IllegalStateException.class)
    public void throwIllegalStateWhenOnlyMapperIsSpecifiedToParseWithoutArguments() {
        CsvToBean csvtb = new CsvToBean();
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat = new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        csvtb.setMappingStrategy(strat);
        csvtb.iterator();
    }

    @Test
    public void readWithIteratorAndFilter() {
        HeaderColumnNameMappingStrategy<MockBean> strategy;
        strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(MockBean.class);
        CsvToBean bean = new CsvToBeanBuilder(new StringReader(TEST_STRING))
                .withMappingStrategy(strategy)
                .withFilter(new FilterSmallNumbers(strategy))
                .build();

        Iterator<MockBean> iterator = bean.iterator();

        assertTrue(iterator.hasNext());
        MockBean mockBean = iterator.next();
        assertEquals("jimmy", mockBean.getName());
        assertEquals("def098765", mockBean.getOrderNumber());
        assertEquals(456, mockBean.getNum());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void readWithIteratorOfAnnotatedBean() {
        HeaderColumnNameMappingStrategy<MinimalCsvBindByNameBeanForWriting> minimalStrategy = new HeaderColumnNameMappingStrategy<>();
        minimalStrategy.setType(MinimalCsvBindByNameBeanForWriting.class);
        StringReader reader = new StringReader("finda,findb,c\n1,2,3\n4,5,6");
        Iterator<MinimalCsvBindByNameBeanForWriting> iterator = new CsvToBeanBuilder(reader)
                .withMappingStrategy(minimalStrategy)
                .build()
                .iterator();

        assertTrue(iterator.hasNext());
        MinimalCsvBindByNameBeanForWriting mockBean = iterator.next();
        assertEquals(1, mockBean.getA());
        assertEquals(2, mockBean.getB());
        assertEquals(3, mockBean.getC());

        assertTrue(iterator.hasNext());
        mockBean = iterator.next();
        assertEquals(4, mockBean.getA());
        assertEquals(5, mockBean.getB());
        assertEquals(6, mockBean.getC());

        assertFalse(iterator.hasNext());
    }

    @Test(expected = RuntimeException.class)
    public void testRequiredHeaderMissing() {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanForIterator> strat
                = new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanForIterator.class);
        Reader fin = new StringReader("a\n1;2\n3,4");
        Iterator<AnnotatedMockBeanForIterator> iterator = new CsvToBeanBuilder(fin)
                .withMappingStrategy(strat)
                .withSeparator(';')
                .build()
                .iterator();
        try {
            iterator.next();
            fail("The bean should not have been returned since the headers are incomplete.");
        } catch (NoSuchElementException e) {
            // Good
        }
    }

    @Test
    public void testPrematureEOLUsingHeaderNameMappingWithoutExceptionCapturing() {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanForIterator> strat
                = new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanForIterator.class);
        Reader fin = new StringReader("a;b\n1;2\n3");
        Iterator<AnnotatedMockBeanForIterator> iterator = new CsvToBeanBuilder(fin)
                .withMappingStrategy(strat)
                .withSeparator(';')
                .build()
                .iterator();
        try {
            iterator.next();
            fail("The first bean should not have been returned since the second bean is incomplete.");
        }
        catch (RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
        }
    }

    @Test
    public void testPrematureEOLUsingHeaderNameMappingWithExceptionCapturing() {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanForIterator> strat
                = new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanForIterator.class);
        Reader fin = new StringReader("a;b\n1;2\n3");
        CsvToBean<AnnotatedMockBeanForIterator> csvtb = new CsvToBeanBuilder(fin)
                .withMappingStrategy(strat)
                .withSeparator(';')
                .withThrowExceptions(false)
                .build();
        Iterator<AnnotatedMockBeanForIterator> iterator = csvtb.iterator();
        
        iterator.next(); // The first bean is okay.
        assertFalse(iterator.hasNext());
        List<CsvException> savedExceptions = csvtb.getCapturedExceptions();
        assertEquals(1, savedExceptions.size());
        CsvException csve = savedExceptions.get(0);
        assertTrue(csve instanceof CsvRequiredFieldEmptyException);
    }
    
    @Test
    public void iteratorConvertsIOExceptionToRuntimeException() {
        HeaderColumnNameMappingStrategy<MockBean> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(MockBean.class);
        CsvToBean<MockBean> bean = new CsvToBean<>();
        bean.setMappingStrategy(strategy);
        bean.setCsvReader(new CSVReader(new ReaderThrowsIOException()));

        try {
            bean.iterator();
            fail("Exception should have been thrown.");
        }
        catch(RuntimeException re) {
            assertNotNull(re.getCause());
            assertTrue(re.getCause() instanceof IOException);
        }
    }
}
