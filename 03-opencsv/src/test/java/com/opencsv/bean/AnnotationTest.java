/*
 * Copyright 2016 Andrew Rucker Jones.
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

import com.opencsv.CSVReader;
import com.opencsv.bean.customconverter.BadIntConverter;
import com.opencsv.bean.mocks.*;
import com.opencsv.exceptions.*;
import org.apache.commons.beanutils.ConversionException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * This class tests all annotation-based mapping.
 *
 * @author Andrew Rucker Jones
 */
public class AnnotationTest {

    private static final String UNPARSABLE = "unparsable";

    private static Locale systemLocale;

    @BeforeClass
    public static void storeSystemLocale() {
        systemLocale = Locale.getDefault();
    }

    @Before
    public void setSystemLocaleToValueNotGerman() {
        Locale.setDefault(Locale.US);
    }

    @After
    public void setSystemLocaleBackToDefault() {
        Locale.setDefault(systemLocale);
    }

    private static GregorianCalendar createDefaultTime() {
        return new GregorianCalendar(1978, Calendar.JANUARY, 15, 6, 32, 9);
    }

    @Test
    public void testGoodDataByName() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputfullgood.csv");
        testGoodData(strat, fin, true);
        
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFullDerived> stratd =
                new HeaderColumnNameMappingStrategy<>();
        stratd.setType(AnnotatedMockBeanFullDerived.class);
        fin = new FileReader("src/test/resources/testinputderivedgood.csv");
        List<AnnotatedMockBeanFull> beanList = testGoodData(stratd, fin, true);
        AnnotatedMockBeanFullDerived bean = (AnnotatedMockBeanFullDerived) beanList.get(0);
        assertEquals(7, bean.getIntInSubclass());
        bean = (AnnotatedMockBeanFullDerived) beanList.get(1);
        assertEquals(8, bean.getIntInSubclass());
    }

    @Test
    public void testGoodDataByPosition() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanFull> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputposfullgood.csv");
        testGoodData(strat, fin, true);
    }

    @Test
    public void testGoodDataByNameUnordered() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputfullgood.csv");
        testGoodData(strat, fin, false);
        
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFullDerived> stratd =
                new HeaderColumnNameMappingStrategy<>();
        stratd.setType(AnnotatedMockBeanFullDerived.class);
        fin = new FileReader("src/test/resources/testinputderivedgood.csv");
        testGoodData(stratd, fin, false);
    }

    @Test
    public void testGoodDataByPositionUnordered() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanFull> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputposfullgood.csv");
        testGoodData(strat, fin, false);
    }

    private static List<AnnotatedMockBeanFull> testGoodData(MappingStrategy strat, Reader fin, boolean ordered) {
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean<AnnotatedMockBeanFull> ctb = new CsvToBean<>();
        ctb.setOrderedResults(ordered);
        List<AnnotatedMockBeanFull> beanList = ctb.parse(strat, read);
        assertEquals(2, beanList.size());
        if(ordered) {
            AnnotatedMockBeanFull bean = beanList.get(0);
            assertTrue(bean.getBoolWrapped());
            assertFalse(bean.isBoolPrimitive());
            assertEquals(1L, (long) bean.getByteWrappedDefaultLocale());
            assertEquals(2L, (long) bean.getByteWrappedSetLocale());
            assertEquals(3L, (long) bean.getBytePrimitiveDefaultLocale());
            assertEquals(4L, (long) bean.getBytePrimitiveSetLocale());
            assertEquals(123101.101, bean.getDoubleWrappedDefaultLocale(), 0);
            assertEquals(123202.202, bean.getDoubleWrappedSetLocale(), 0);
            assertEquals(123303.303, bean.getDoublePrimitiveDefaultLocale(), 0);
            assertEquals(123404.404, bean.getDoublePrimitiveSetLocale(), 0);
            assertEquals((float) 123101.101, bean.getFloatWrappedDefaultLocale(), 0);
            assertEquals((float) 123202.202, bean.getFloatWrappedSetLocale(), 0);

            // There appear to be rounding errors when converting from Float to float.
            assertEquals(123303.303, bean.getFloatPrimitiveDefaultLocale(), 0.002);
            assertEquals(123404.404, bean.getFloatPrimitiveSetLocale(), 0.003);

            assertEquals(5000, (int) bean.getIntegerWrappedDefaultLocale());
            assertEquals(6000, (int) bean.getIntegerWrappedSetLocale());
            assertEquals(Integer.MAX_VALUE - 7000, bean.getIntegerPrimitiveDefaultLocale());
            assertEquals(8000, bean.getIntegerPrimitiveSetLocale());
            assertEquals(9000L, (long) bean.getLongWrappedDefaultLocale());
            assertEquals(10000L, (long) bean.getLongWrappedSetLocale());
            assertEquals(11000L, bean.getLongPrimitiveDefaultLocale());
            assertEquals(12000L, bean.getLongPrimitiveSetLocale());
            assertEquals((short) 13000, (short) bean.getShortWrappedDefaultLocale());
            assertEquals((short) 14000, (short) bean.getShortWrappedSetLocale());
            assertEquals(15000, bean.getShortPrimitiveDefaultLocale());
            assertEquals(16000, bean.getShortPrimitiveSetLocale());
            assertEquals('a', (char) bean.getCharacterWrapped());
            assertEquals('b', bean.getCharacterPrimitive());
            assertEquals(BigDecimal.valueOf(123101.101), bean.getBigdecimalDefaultLocale());
            assertEquals(BigDecimal.valueOf(123102.102), bean.getBigdecimalSetLocale());
            assertEquals(BigInteger.valueOf(101), bean.getBigintegerDefaultLocale());
            assertEquals(BigInteger.valueOf(102), bean.getBigintegerSetLocale());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getDateDefaultLocale().getTime());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getGcalDefaultLocale().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getCalDefaultLocale().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getXmlcalDefaultLocale().toGregorianCalendar().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getSqltimeDefaultLocale().getTime());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getSqltimestampDefaultLocale().getTime());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getDateSetLocale().getTime());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getGcalSetLocale().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getCalSetLocale().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getXmlcalSetLocale().toGregorianCalendar().getTimeInMillis());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getSqltimeSetLocale().getTime());
            assertEquals(createDefaultTime().getTimeInMillis(), bean.getSqltimestampSetLocale().getTime());
            assertEquals("1978-01-15", bean.getSqldateDefaultLocale().toString());
            assertEquals("1978-01-15", bean.getSqldateSetLocale().toString());
            assertEquals("test string", bean.getStringClass());
            assertEquals(new GregorianCalendar(1978, 0, 15).getTimeInMillis(), bean.getGcalFormatDefaultLocale().getTimeInMillis());
            assertEquals(new GregorianCalendar(2018, 11, 13).getTimeInMillis(), bean.getGcalFormatSetLocale().getTimeInMillis());
            assertEquals(1.01, bean.getFloatBadLocale(), 0.001);
            assertNull(bean.getColumnDoesntExist());
            assertNull(bean.getUnmapped());

            bean = beanList.get(1);
            assertNull(bean.getBoolWrapped());
            assertFalse(bean.isBoolPrimitive());
            GregorianCalendar gc = createDefaultTime();
            gc.set(Calendar.HOUR_OF_DAY, 16);
            assertEquals(gc.getTimeInMillis(), bean.getGcalDefaultLocale().getTimeInMillis());
            assertNull(bean.getCalDefaultLocale());
        }
        
        return beanList;
    }

    @Test
    public void testGoodDataCustomByName() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanCustom> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);
        FileReader fin = new FileReader("src/test/resources/testinputcustomgood.csv");
        testGoodDataCustom(strat, fin);
    }

    @Test
    public void testGoodDataCustomByPosition() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanCustom> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);
        FileReader fin = new FileReader("src/test/resources/testinputposcustomgood.csv");
        testGoodDataCustom(strat, fin);
    }

    private void testGoodDataCustom(MappingStrategy strat, Reader fin) {
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        List<AnnotatedMockBeanCustom> beanList = ctb.parse(strat, read);

        AnnotatedMockBeanCustom bean = beanList.get(0);
        assertTrue(bean.getBoolWrapped());
        assertFalse(bean.isBoolPrimitive());
        assertEquals(Byte.MAX_VALUE, (long) bean.getByteWrappedDefaultLocale());
        assertEquals(Byte.MAX_VALUE, (long) bean.getByteWrappedSetLocale());
        assertEquals(Byte.MAX_VALUE, (long) bean.getBytePrimitiveDefaultLocale());
        assertEquals(Double.MAX_VALUE, bean.getDoubleWrappedDefaultLocale(), 0);
        assertEquals(Double.MAX_VALUE, bean.getDoubleWrappedSetLocale(), 0);
        assertEquals(Double.MAX_VALUE, bean.getDoublePrimitiveDefaultLocale(), 0);
        assertEquals(Double.MAX_VALUE, bean.getDoublePrimitiveSetLocale(), 0);
        assertEquals(Float.MAX_VALUE, bean.getFloatWrappedDefaultLocale(), 0);
        assertEquals(Float.MAX_VALUE, bean.getFloatWrappedSetLocale(), 0);
        assertEquals(Float.MAX_VALUE, bean.getFloatPrimitiveDefaultLocale(), 0);
        assertEquals(Float.MAX_VALUE, bean.getFloatPrimitiveSetLocale(), 0);
        assertEquals(Integer.MAX_VALUE, (int) bean.getIntegerWrappedDefaultLocale());
        assertEquals(Integer.MAX_VALUE, (int) bean.getIntegerWrappedSetLocale());
        assertEquals(Integer.MAX_VALUE, bean.getIntegerPrimitiveDefaultLocale());
        assertEquals(Integer.MAX_VALUE, bean.getIntegerPrimitiveSetLocale());
        assertEquals(Long.MAX_VALUE, (long) bean.getLongWrappedDefaultLocale());
        assertEquals(Long.MAX_VALUE, (long) bean.getLongWrappedSetLocale());
        assertEquals(Long.MAX_VALUE, bean.getLongPrimitiveDefaultLocale());
        assertEquals(Long.MAX_VALUE, bean.getLongPrimitiveSetLocale());
        assertEquals(Short.MAX_VALUE, (short) bean.getShortWrappedDefaultLocale());
        assertEquals(Short.MAX_VALUE, (short) bean.getShortWrappedSetLocale());
        assertEquals(Short.MAX_VALUE, bean.getShortPrimitiveDefaultLocale());
        assertEquals(Short.MAX_VALUE, bean.getShortPrimitiveSetLocale());
        assertEquals(Character.MAX_VALUE, (char) bean.getCharacterWrapped());
        assertEquals(Character.MAX_VALUE, bean.getCharacterPrimitive());
        assertEquals(BigDecimal.TEN, bean.getBigdecimalDefaultLocale());
        assertEquals(BigDecimal.TEN, bean.getBigdecimalSetLocale());
        assertEquals(BigInteger.TEN, bean.getBigintegerDefaultLocale());
        assertEquals(BigInteger.TEN, bean.getBigintegerSetLocale());
        assertEquals("inside custom converter", bean.getStringClass());
        assertEquals(Arrays.asList("really", "long", "test", "string,", "yeah!"), bean.getComplexString());
        assertTrue(bean.getComplexClass1() instanceof ComplexClassForCustomAnnotation);
        assertFalse(bean.getComplexClass1() instanceof ComplexDerivedClassForCustomAnnotation);
        assertEquals(1, bean.getComplexClass1().i);
        assertEquals('a', bean.getComplexClass1().c);
        assertEquals("long,long.string1", bean.getComplexClass1().s);
        assertTrue(bean.getComplexClass2() instanceof ComplexClassForCustomAnnotation);
        assertFalse(bean.getComplexClass2() instanceof ComplexDerivedClassForCustomAnnotation);
        assertEquals(Integer.MAX_VALUE - 2, bean.getComplexClass2().i);
        assertEquals('z', bean.getComplexClass2().c);
        assertEquals("Inserted in setter methodlong,long.string2", bean.getComplexClass2().s);
        assertTrue(bean.getComplexClass3() instanceof ComplexClassForCustomAnnotation);
        assertTrue(bean.getComplexClass3() instanceof ComplexDerivedClassForCustomAnnotation);
        assertEquals(3, bean.getComplexClass3().i);
        assertEquals('c', bean.getComplexClass3().c);
        assertEquals("long,long.derived.string3", bean.getComplexClass3().s);
        assertEquals((float) 1.0, ((ComplexDerivedClassForCustomAnnotation) bean.getComplexClass3()).f, 0);
        assertEquals("inside custom converter", bean.getRequiredWithCustom());

        bean = beanList.get(1);
        assertEquals(Arrays.asList("really"), bean.getComplexString());
        assertTrue(bean.getComplexClass2() instanceof ComplexClassForCustomAnnotation);
        assertTrue(bean.getComplexClass2() instanceof ComplexDerivedClassForCustomAnnotation);
        assertEquals(Integer.MAX_VALUE - 5, bean.getComplexClass2().i);
        assertEquals('z', bean.getComplexClass2().c);
        assertEquals("Inserted in setter methodlong,long.derived.string5", bean.getComplexClass2().s);
        assertEquals((float) 1.0, ((ComplexDerivedClassForCustomAnnotation) bean.getComplexClass2()).f, 0);

        bean = beanList.get(2);
        assertEquals(new ArrayList<>(), bean.getComplexString());
        assertTrue(bean.getComplexClass1() instanceof ComplexClassForCustomAnnotation);
        assertTrue(bean.getComplexClass1() instanceof ComplexDerivedClassForCustomAnnotation);
        assertEquals(7, bean.getComplexClass1().i);
        assertEquals('g', bean.getComplexClass1().c);
        assertEquals("long,long.derived.string7", bean.getComplexClass1().s);
        assertEquals((float) 1.0, ((ComplexDerivedClassForCustomAnnotation) bean.getComplexClass1()).f, 0);

        for (AnnotatedMockBeanCustom cb : beanList.subList(1, 4)) {
            assertTrue(cb.getBoolWrapped());
            assertFalse(cb.isBoolPrimitive());
            assertFalse(cb.getBoolWrappedOptional());
            assertTrue(cb.isBoolPrimitiveOptional());
        }

        bean = beanList.get(5);
        assertNull(bean.getBoolWrappedOptional());
        assertFalse(bean.isBoolPrimitiveOptional());
        assertNull(bean.getComplexString());
    }

    @Test
    public void testCase7() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase7.csv");
        testCases7And51(strat, fin);
    }

    @Test
    public void testCase51() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanFull> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase51.csv");
        testCases7And51(strat, fin);
    }

    private void testCases7And51(MappingStrategy strat, Reader fin) {
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        try {
            ctb.parse(strat, read);
            fail("The parse should have thrown an Exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(AnnotatedMockBeanFull.class.getName(), csve.getBeanClass().getName());
            assertEquals("byteWrappedSetLocale", csve.getDestinationField().getName());
        }

    }

    @Test
    public void testCase11() throws IOException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        Reader fin = new FileReader("src/test/resources/testinputcase11.csv");
        String englishErrorMessage = testCases11And55(strat, fin);
        
        // Now with another locale
        strat = new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        strat.setErrorLocale(Locale.GERMAN); // In this test, setType(), then setErrorLocale()
        fin = new FileReader("src/test/resources/testinputcase11.csv");
        assertNotEquals(englishErrorMessage, testCases11And55(strat, fin));
    }

    @Test
    public void testCase55() throws IOException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanFull> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        Reader fin = new FileReader("src/test/resources/testinputcase55.csv");
        String englishErrorMessage = testCases11And55(strat, fin);
        
        // Now with a different locale
        strat = new ColumnPositionMappingStrategy<>();
        strat.setErrorLocale(Locale.GERMAN); // In this test, setErrorLocale(), then setType()
        strat.setType(AnnotatedMockBeanFull.class);
        fin = new FileReader("src/test/resources/testinputcase55.csv");
        assertNotEquals(englishErrorMessage, testCases11And55(strat, fin));
    }

    private String testCases11And55(MappingStrategy strat, Reader fin) {
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        String errorMessage = null;
        try {
            ctb.parse(strat, read);
            fail("The parse should have thrown an Exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertTrue(csve.getSourceObject() instanceof String);
            assertEquals("mismatchedtype", csve.getSourceObject());
            assertEquals(Byte.class, csve.getDestinationClass());
            errorMessage = csve.getLocalizedMessage();
            assertTrue(csve.getCause() instanceof ConversionException);
        }
        
        return errorMessage;
    }

    @Test
    public void testCase21() {
        HeaderColumnNameMappingStrategy<TestCases21And63> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(TestCases21And63.class);
        CSVReader read = new CSVReader(new StringReader("list\ntrue false true"));
        testCases21And63(strat, read);
    }

    @Test
    public void testCase63() {
        ColumnPositionMappingStrategy<TestCases21And63> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(TestCases21And63.class);
        CSVReader read = new CSVReader(new StringReader("true false true"));
        testCases21And63(strat, read);
    }

    private void testCases21And63(MappingStrategy strat, CSVReader read) {
        CsvToBean ctb = new CsvToBean();
        try {
            ctb.parse(strat, read);
            fail("The parse should have thrown an Exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertTrue(csve.getSourceObject() instanceof String);
            assertEquals("true false true", csve.getSourceObject());
            assertEquals(List.class, csve.getDestinationClass());
        }
    }
    
    @Test
    public void testBadDataExceptionsCapturedUnordered() {
        CsvToBean ctb = new CsvToBeanBuilder(new StringReader("isnotdate\n19780115T063209"))
                .withType(TestCase34.class)
                .withThrowExceptions(false)
                .withOrderedResults(false)
                .build();
        List<TestCase34> beanList = ctb.parse();
        assertNotNull(beanList);
        assertEquals(0, beanList.size());
        List<CsvException> exceptionList = ctb.getCapturedExceptions();
        assertNotNull(exceptionList);
        assertEquals(1, exceptionList.size());
        assertTrue(exceptionList.get(0) instanceof CsvDataTypeMismatchException);
        CsvDataTypeMismatchException innere = (CsvDataTypeMismatchException) exceptionList.get(0);
        assertEquals(1, innere.getLineNumber());
        assertTrue(innere.getSourceObject() instanceof String);
        assertEquals("19780115T063209", innere.getSourceObject());
        assertEquals(String.class, innere.getDestinationClass());
    }

    @Test
    public void testBadDataByName() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        Reader fin = new FileReader("src/test/resources/testinputcase78null.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(AnnotatedMockBeanFull.class, csve.getBeanClass());
            assertEquals("dateDefaultLocale", csve.getDestinationField().getName());
        }

        fin = new FileReader("src/test/resources/testinputcase78blank.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(AnnotatedMockBeanFull.class, csve.getBeanClass());
            assertEquals("dateDefaultLocale", csve.getDestinationField().getName());
        }

        fin = new FileReader("src/test/resources/testinputcase81.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(GregorianCalendar.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }


        fin = new FileReader("src/test/resources/testinputcase82.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(Date.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }

        fin = new FileReader("src/test/resources/testinputcase83.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(Date.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }

        fin = new FileReader("src/test/resources/testinputcase81.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(GregorianCalendar.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }


        fin = new FileReader("src/test/resources/testinputcase82.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(Date.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }

        fin = new FileReader("src/test/resources/testinputcase83.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(Date.class, csve.getDestinationClass());
            assertEquals(UNPARSABLE, csve.getSourceObject());
            assertTrue(csve.getCause() instanceof ParseException);
        }

        HeaderColumnNameMappingStrategy<TestCase34> strat34 =
                new HeaderColumnNameMappingStrategy<>();
        strat34.setType(TestCase34.class);
        read = new CSVReader(new StringReader("isnotdate\n19780115T063209"));
        try {
            ctb.parse(strat34, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertTrue(csve.getSourceObject() instanceof String);
            assertEquals("19780115T063209", csve.getSourceObject());
            assertEquals(String.class, csve.getDestinationClass());
        }

        // For test case 73
        read = new CSVReader(new StringReader("isnotdate\n19780115T063209"));
        ctb.parse(strat34, read, false);
        List<CsvException> exlist = ctb.getCapturedExceptions();
        assertEquals(1, exlist.size());
        assertTrue(exlist.get(0) instanceof CsvDataTypeMismatchException);
        CsvDataTypeMismatchException innere = (CsvDataTypeMismatchException) exlist.get(0);
        assertEquals(1, innere.getLineNumber());
        assertTrue(innere.getSourceObject() instanceof String);
        assertEquals("19780115T063209", innere.getSourceObject());
        assertEquals(String.class, innere.getDestinationClass());

    }

    @Test
    public void testCase16() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanCustom> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);
        Reader fin = new FileReader("src/test/resources/testinputcase16.csv");
        testCases16And60(strat, fin);
    }

    @Test
    public void testCase60() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanCustom> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);
        Reader fin = new FileReader("src/test/resources/testinputcase60.csv");
        testCases16And60(strat, fin);
    }

    private void testCases16And60(MappingStrategy strat, Reader fin) {
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertTrue(csve.getSourceObject() instanceof String);
            assertEquals("Mismatched data type", csve.getSourceObject());
            assertEquals(ComplexClassForCustomAnnotation.class, csve.getDestinationClass());
            assertTrue(csve.getCause() instanceof IllegalArgumentException);
        }

    }

    @Test
    public void testBadDataCustomByName() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanCustom> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);

        FileReader fin = new FileReader("src/test/resources/testinputcase38.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean ctb = new CsvToBean();
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertTrue(csve.getSourceObject() instanceof String);
            assertEquals("invalidstring", csve.getSourceObject());
            assertEquals(Boolean.class, csve.getDestinationClass());
            assertTrue(csve.getCause() instanceof ConversionException);
        }

        fin = new FileReader("src/test/resources/testinputcase40.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals(AnnotatedMockBeanCustom.class, csve.getBeanClass());
            assertEquals("boolWrapped", csve.getDestinationField().getName());
        }

        fin = new FileReader("src/test/resources/testinputcase41.csv");
        read = new CSVReader(fin, ';');
        try {
            ctb.parse(strat, read);
            fail("Expected parse to throw exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvDataTypeMismatchException);
            CsvDataTypeMismatchException csve = (CsvDataTypeMismatchException) e.getCause();
            assertEquals(1, csve.getLineNumber());
            assertEquals("invaliddatum", csve.getSourceObject());
            assertEquals(Boolean.class, csve.getDestinationClass());
        }

    }

    @Test
    public void testBadConverter() {
        HeaderColumnNameMappingStrategy<TestCase80> strath =
                new HeaderColumnNameMappingStrategy<>();
        try {
            strath.setType(TestCase80.class);
            fail("HeaderColumnNameMappingStrategy.setType() should have thrown an Exception.");
        } catch (CsvBadConverterException e) {
            assertEquals(BadIntConverter.class, e.getConverterClass());
        }

        ColumnPositionMappingStrategy<TestCase80> stratc =
                new ColumnPositionMappingStrategy<>();
        try {
            stratc.setType(TestCase80.class);
            fail("The parse should have thrown an Exception.");
        } catch (CsvBadConverterException e) {
            assertEquals(BadIntConverter.class, e.getConverterClass());
        }
    }

    @Test
    public void testRequiredColumnNonexistantHeaderNameMapping() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase84.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean<AnnotatedMockBeanFull> ctb = new CsvToBean<>();
        try {
            ctb.parse(strat, read);
            fail("RuntimeException with inner exception CsvRequiredFieldEmpty should have been thrown because a required column is completely missing.");
        }
        catch(RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException)e.getCause();
            assertEquals(AnnotatedMockBeanFull.class, csve.getBeanClass());
            assertEquals(-1, csve.getLineNumber());
            assertEquals("byteWrappedSetLocale", csve.getDestinationField().getName());
        }
    }

    @Test
    public void testRequiredColumnNonexistantColumnPositionMapping() throws FileNotFoundException {
        ColumnPositionMappingStrategy<AnnotatedMockBeanFull> strat =
                new ColumnPositionMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase85.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean<AnnotatedMockBeanFull> ctb = new CsvToBean<>();
        try {
            ctb.parse(strat, read);
            fail("RuntimeException with inner exception CsvRequiredFieldEmpty should have been thrown because a required column is completely missing.");
        }
        catch(RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException)e.getCause();
            assertEquals(AnnotatedMockBeanFull.class, csve.getBeanClass());
            assertEquals(2, csve.getLineNumber());
            assertNull(csve.getDestinationField());
        }
    }

    @Test
    public void testPrematureEOLUsingHeaderNameMapping() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanFull> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanFull.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase86.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean<AnnotatedMockBeanFull> ctb = new CsvToBean<>();
        try {
            ctb.parse(strat, read);
            fail("RuntimeException with inner exception CsvRequiredFieldEmpty should have been thrown because a required column is completely missing.");
        }
        catch(RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException)e.getCause();
            assertEquals(AnnotatedMockBeanFull.class, csve.getBeanClass());
            assertEquals(1, csve.getLineNumber());
            assertNull(csve.getDestinationField());
        }
    }
    
    @Test
    public void testCase88() throws FileNotFoundException {
        HeaderColumnNameMappingStrategy<AnnotatedMockBeanCustom> strat =
                new HeaderColumnNameMappingStrategy<>();
        strat.setType(AnnotatedMockBeanCustom.class);
        FileReader fin = new FileReader("src/test/resources/testinputcase88.csv");
        CSVReader read = new CSVReader(fin, ';');
        CsvToBean<AnnotatedMockBeanFull> ctb = new CsvToBean<>();
        try {
            ctb.parse((MappingStrategy)strat, read);
            fail("Exception should have been thrown for missing required value.");
        }
        catch(RuntimeException e) {
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException)e.getCause();
            assertEquals(AnnotatedMockBeanCustom.class, csve.getBeanClass());
            assertEquals(1, csve.getLineNumber());
            assertEquals("requiredWithCustom", csve.getDestinationField().getName());
        }
    }
    
    @Test
    public void testSetterThrowsException() {
        try {
            new CsvToBeanBuilder(new StringReader("map\nstring"))
                    .withType(SetterThrowsException.class).build().parse();
            fail("Exception should have been thrown");
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvBeanIntrospectionException);
            CsvBeanIntrospectionException csve = (CsvBeanIntrospectionException)e.getCause();
            assertEquals("map", csve.getField().getName());
        }
    }
}
