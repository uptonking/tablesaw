package com.opencsv.bean;

import com.opencsv.CSVReader;
import com.opencsv.bean.mocks.AnnotatedMockBeanForIterator;
import com.opencsv.bean.mocks.MinimalCsvBindByNameBeanForWriting;
import com.opencsv.bean.mocks.MockBean;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.Before;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.junit.After;

import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class IterableCSVToBeanTest {

    private static final String TEST_STRING = "name,orderNumber,num\n" +
            "kyle,abc123456,123\n" +
            "jimmy,def098765,456 ";

    private IterableCSVToBeanBuilder<MockBean> builder;
    private HeaderColumnNameMappingStrategy<MockBean> strategy;
    private IterableCSVToBean<MockBean> bean;
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
        builder = new IterableCSVToBeanBuilder<>();
        strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(MockBean.class);
        bean = builder.withReader(createReader())
                .withMapper(strategy)
                .build();
    }

    private CSVReader createReader() {
        StringReader reader = new StringReader(TEST_STRING);
        return new CSVReader(reader);
    }

    private CsvToBeanFilter createFilter() {
        return new CsvToBeanFilter() {
            @Override
            public boolean allowLine(String[] line) {
                int index = strategy.getColumnIndex("num");
                return Integer.parseInt(line[index].trim()) > 200;
            }
        };
    }

    @Test
    public void nextLine() throws InvocationTargetException, IOException, IntrospectionException, InstantiationException, IllegalAccessException, CsvRequiredFieldEmptyException {
        MockBean mockBean = bean.nextLine();
        assertEquals("kyle", mockBean.getName());
        assertEquals("abc123456", mockBean.getOrderNumber());
        assertEquals(123, mockBean.getNum());

        mockBean = bean.nextLine();
        assertEquals("jimmy", mockBean.getName());
        assertEquals("def098765", mockBean.getOrderNumber());
        assertEquals(456, mockBean.getNum());

        mockBean = bean.nextLine();
        assertNull(mockBean);
    }

    @Test
    public void nextLineWithFilter() throws InvocationTargetException, IOException, IntrospectionException, InstantiationException, IllegalAccessException, CsvRequiredFieldEmptyException {
        bean = builder.withReader(createReader())
                .withMapper(strategy)
                .withFilter(createFilter())
                .build();

        MockBean mockBean = bean.nextLine();
        assertEquals("jimmy", mockBean.getName());
        assertEquals("def098765", mockBean.getOrderNumber());
        assertEquals(456, mockBean.getNum());

        mockBean = bean.nextLine();
        assertNull(mockBean);
    }

    @Test
    public void readWithIterator() {
        Iterator<MockBean> iterator = bean.iterator();

        String englishErrorMessage = null;
        try {
            iterator.remove();
            fail("Removing from an IterableCsvToBean should not be supported.");
        } catch (UnsupportedOperationException e) {
            englishErrorMessage = e.getLocalizedMessage();
            assertNotNull(englishErrorMessage);
            // Good
        }

        assertTrue(iterator.hasNext());
        MockBean mockBean = iterator.next();
        assertEquals("kyle", mockBean.getName());
        assertEquals("abc123456", mockBean.getOrderNumber());
        assertEquals(123, mockBean.getNum());

        assertTrue(iterator.hasNext());
        mockBean = iterator.next();
        assertEquals("jimmy", mockBean.getName());
        assertEquals("def098765", mockBean.getOrderNumber());
        assertEquals(456, mockBean.getNum());

        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("IterableCSVToBean.iterator().next() did not throw NoSuchElementException despite being empty.");
        } catch (NoSuchElementException e) {
            // Good
        }
        assertFalse(iterator.hasNext());
        
        builder = new IterableCSVToBeanBuilder<>();
        strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setErrorLocale(Locale.GERMAN);
        strategy.setType(MockBean.class);
        bean = builder.withReader(createReader())
                .withErrorLocale(Locale.GERMAN)
                .withMapper(strategy)
                .build();
        iterator = bean.iterator();
        try {
            iterator.remove();
            fail("Removing from an IterableCsvToBean should not be supported.");
        } catch (UnsupportedOperationException e) {
            assertNotEquals(englishErrorMessage, e.getLocalizedMessage());
            // Good
        }
    }

    @Test
    public void readWithIteratorAndFilter() {
        bean = builder.withReader(createReader())
                .withMapper(strategy)
                .withFilter(createFilter())
                .build();

        Iterator<MockBean> iterator = bean.iterator();

        assertTrue(iterator.hasNext());
        MockBean mockBean = iterator.next();
        assertEquals("jimmy", mockBean.getName());
        assertEquals("def098765", mockBean.getOrderNumber());
        assertEquals(456, mockBean.getNum());

        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("IterableCSVToBean.iterator().next() did not throw NoSuchElementException despite being empty.");
        } catch (NoSuchElementException e) {
            // Good
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void readWithIteratorOfAnnotatedBean() {
        IterableCSVToBeanBuilder<MinimalCsvBindByNameBeanForWriting> minimalBuilder = new IterableCSVToBeanBuilder<>();
        HeaderColumnNameMappingStrategy<MinimalCsvBindByNameBeanForWriting> minimalStrategy = new HeaderColumnNameMappingStrategy<>();
        minimalStrategy.setType(MinimalCsvBindByNameBeanForWriting.class);
        StringReader reader = new StringReader("finda,findb,c\n1,2,3\n4,5,6");
        CSVReader csvreader = new CSVReader(reader);
        Iterator<MinimalCsvBindByNameBeanForWriting> iterator = minimalBuilder.withReader(csvreader)
                .withMapper(minimalStrategy)
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

    @Test
    public void testRequiredHeaderMissing() {
        IterableCSVToBeanBuilder<AnnotatedMockBeanForIterator> minimalBuilder = new IterableCSVToBeanBuilder<>();
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanForIterator> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanForIterator.class);
        Reader fin = new StringReader("a\n1;2\n3,4");
        CSVReader read = new CSVReader(fin, ';');
        Iterator<AnnotatedMockBeanForIterator> iterator = minimalBuilder.withReader(read)
                .withMapper(strat)
                .build()
                .iterator();
        try {
            iterator.next();
            fail("The bean should not have been returned since the headers are incomplete.");
        }
        catch(NoSuchElementException e) {
            // Good
        }
    }

    @Test
    public void testPrematureEOLUsingHeaderNameMapping() {
        IterableCSVToBeanBuilder<AnnotatedMockBeanForIterator> minimalBuilder = new IterableCSVToBeanBuilder<>();
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanForIterator> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanForIterator.class);
        Reader fin = new StringReader("a;b\n1;2\n3");
        CSVReader read = new CSVReader(fin, ';');
        Iterator<AnnotatedMockBeanForIterator> iterator = minimalBuilder.withReader(read)
                .withMapper(strat)
                .build()
                .iterator();
        iterator.next(); // The first bean is okay.
        try {
            iterator.next();
            fail("The second bean should not have been returned since it is incomplete.");
        }
        catch(NoSuchElementException e) {
            // Good
        }
    }
}
