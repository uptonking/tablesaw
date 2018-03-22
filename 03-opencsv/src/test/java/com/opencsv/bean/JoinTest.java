/*
 * Copyright 2018 Andrew Rucker Jones.
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

import com.opencsv.bean.mocks.join.*;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 *
 * @author Andrew Rucker Jones
 */
public class JoinTest {
    
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

    /**
     * Tests reading a wrapped primitive into a MultiValuedMap.
     * <p>Also tests:</p>
     * <ul><li>Reading with a header name mapping strategy</li>
     * <li>Reading with a regular expression that matches more than one
     * identically named headers</li>
     * <li>Map does not already exist</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadPrimitive() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbynamegood.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<String, Integer> map = beans.get(0).getMap1();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        Collection<Integer> values = map.get("index");
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(1, 2, 3)));
        
        map = beans.get(1).getMap1();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        values = map.get("index");
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(2, 3, 4)));
        
        map = beans.get(2).getMap1();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        values = map.get("index");
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(3, 4, 5)));
    }
    
    /**
     * Tests reading in formatted dates.
     * <p>Also tests:</p>
     * <ul><li>Reading a date with a conversion locale and header mapping</li>
     * <li>Reading with a regular expression that matches more than one not
     * identically named headers</li>
     * <li>Test with comma-separated range values</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadDate() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbynamegood.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<String, Date> map = beans.get(0).getMap2();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        Collection<Date> values = map.get("date1");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 15).getTime()));
        values = map.get("date2");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 1, 27).getTime()));
        values = map.get("date3");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 13).getTime()));
        
        map = beans.get(1).getMap2();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        values = map.get("date1");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 16).getTime()));
        values = map.get("date2");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 1, 28).getTime()));
        values = map.get("date3");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 14).getTime()));
        
        map = beans.get(2).getMap2();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        values = map.get("date1");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 17).getTime()));
        values = map.get("date2");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 2, 1).getTime()));
        values = map.get("date3");
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 15).getTime()));
    }
    
    // Test with CsvBindByName taking the same name as CsvBindAndJoinByName
    /**
     * Tests that an overlap in naming between {@link CsvBindByName} and
     * {@link CsvBindAndJoinByName} is resolved to the benefit of the former.
     * <p>Also incidentally tests:</p>
     * <ul><li>Use of a class derived from an implementation of
     * {@link org.apache.commons.collections4.MultiValuedMap}</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testNamingOverlap() throws IOException {
        List<NamingOverlap> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinnamingoverlap.csv"))
                .withType(NamingOverlap.class).build().parse();
        assertNotNull(beans);
        assertEquals(1, beans.size());
        NamingOverlap bean = beans.get(0);
        assertEquals("nameString", bean.getName());
        DerivedStringMultiValuedMap map = bean.getOtherValues();
        assertEquals("string1", map.get("header1").get(0));
        assertEquals("string2", map.get("header2").get(0));
    }
    
    @Test
    public void testIllegalRegularExpression() {
        try {
        new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY))
                .withType(JoinIllegalRegex.class)
                .build()
                .parse();
            fail("Exception should have been thrown");
        }
        catch(CsvBadConverterException csve) {
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
            assertFalse(StringUtils.isBlank(csve.getLocalizedMessage()));
            Throwable e = csve.getCause();
            assertNotNull(e);
            assertTrue(e instanceof PatternSyntaxException);
        }
    }
    
    /**
     * Test with regular expression that doesn't match any header names.
     * 
     * @throws IOException Never
     */
    @Test
    public void testNonMatchingRegularExpression() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbynamegood.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        MultiValuedMap<String, String> map = beans.get(0).getMap3();
        assertNull(map);
    }
    
    @Test
    public void testFieldNotMultiValuedMap() {
        try {
            new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY))
                    .withType(FieldNotMultiValuedMap.class).build().parse();
            fail("Exception should have been thrown.");
        }
        catch(CsvBadConverterException csve) {
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
            assertFalse(StringUtils.isBlank(csve.getLocalizedMessage()));
        }
    }
    
    /**
     * Tests that the range specification "-" works as a double open range.
     * <p>Also incidentally tests:</p>
     * <ul><li>Use of an interface derived from
     * {@link org.apache.commons.collections4.MultiValuedMap}</li>
     * <li>Overlap of position designations between {@link CsvBindByPosition}
     * and {@link CsvBindAndJoinByPosition}</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testDoubleOpenRange() throws IOException {
        List<DoubleOpenRange> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputopenrange.csv"))
                .withType(DoubleOpenRange.class).build().parse();
        assertNotNull(beans);
        assertEquals(1, beans.size());
        DoubleOpenRange bean = beans.get(0);
        assertEquals(10, bean.getPositionOne());
        MultiValuedMap<Integer, Integer> map = bean.getOtherPositions();
        assertNotNull(map);
        assertEquals(5, map.keySet().size());
        assertEquals(20, map.get(2).toArray(new Integer[1])[0].intValue());
        assertEquals(30, map.get(3).toArray(new Integer[1])[0].intValue());
        assertEquals(40, map.get(4).toArray(new Integer[1])[0].intValue());
        assertEquals(50, map.get(5).toArray(new Integer[1])[0].intValue());
        assertEquals(1, map.get(0).toArray(new Integer[1])[0].intValue());
    }
    
    @Test
    public void testOpenRangeNoLowerBound() throws IOException {
        List<OpenRangeNoLowerBound> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(OpenRangeNoLowerBound.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        OpenRangeNoLowerBound bean = beans.get(0);
        assertNotNull(bean);
        assertNotNull(bean.getMap());
        MultiValuedMap<Integer, String> map = bean.getMap();
        assertEquals(4, map.keySet().size());
        assertEquals("10", map.get(0).toArray(new String[1])[0]);
        assertEquals("15. Dez 1978", map.get(1).toArray(new String[1])[0]);
        assertEquals("20", map.get(2).toArray(new String[1])[0]);
        assertEquals("30", map.get(3).toArray(new String[1])[0]);
        
        bean = beans.get(1);
        assertNotNull(bean);
        assertNotNull(bean.getMap());
        map = bean.getMap();
        assertEquals(4, map.keySet().size());
        assertEquals("11", map.get(0).toArray(new String[1])[0]);
        assertEquals("16. Dez 1978", map.get(1).toArray(new String[1])[0]);
        assertEquals("21", map.get(2).toArray(new String[1])[0]);
        assertEquals("31", map.get(3).toArray(new String[1])[0]);
        
        bean = beans.get(2);
        assertNotNull(bean);
        assertNotNull(bean.getMap());
        map = bean.getMap();
        assertEquals(4, map.keySet().size());
        assertEquals("12", map.get(0).toArray(new String[1])[0]);
        assertEquals("17. Dez 1978", map.get(1).toArray(new String[1])[0]);
        assertEquals("22", map.get(2).toArray(new String[1])[0]);
        assertEquals("32", map.get(3).toArray(new String[1])[0]);
    }
    
    @Test
    public void testNonNumberRangeExpression() {
        try {
            new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY)).withType(NonNumberRange.class).build().parse();
            fail("Exception should have been thrown.");
        }
        catch(CsvBadConverterException csve) {
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
            assertNotNull(csve.getLocalizedMessage());
            assertTrue(csve.getCause() instanceof NumberFormatException);
        }
    }
    
    @Test
    public void testEmptyRangeExpression() {
        try {
            new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY))
                    .withType(EmptyRange.class).build().parse();
            fail("Exception should have been thrown.");
        }
        catch(CsvBadConverterException csve) {
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
            assertFalse(StringUtils.isBlank(csve.getLocalizedMessage()));
            assertNull(csve.getCause());
        }
    }
    
    /**
     * Tests a position definition with exactly one column index.
     * <p>Also incidentally tests:</p>
     * <ul><li>Reading with a column position mapping strategy</li>
     * <li>Map already exists</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testRangeWithOnePosition() throws IOException {
        List<GoodJoinByPositionAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(GoodJoinByPositionAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<Integer, Integer> map = beans.get(0).getMap1();
        assertNotNull(map);
        assertEquals(2, map.keySet().size());
        assertEquals(10, map.get(0).toArray(new Integer[1])[0].intValue());
        
        // Map already exists and is initialized in the constructor with this value
        assertEquals(Integer.MIN_VALUE, map.get(Integer.MAX_VALUE).toArray(new Integer[1])[0].intValue());
        
        map = beans.get(1).getMap1();
        assertNotNull(map);
        assertEquals(2, map.keySet().size());
        assertEquals(11, map.get(0).toArray(new Integer[1])[0].intValue());
        
        // Map already exists and is initialized in the constructor with this value
        assertEquals(Integer.MIN_VALUE, map.get(Integer.MAX_VALUE).toArray(new Integer[1])[0].intValue());
        
        map = beans.get(2).getMap1();
        assertNotNull(map);
        assertEquals(2, map.keySet().size());
        assertEquals(12, map.get(0).toArray(new Integer[1])[0].intValue());
        
        // Map already exists and is initialized in the constructor with this value
        assertEquals(Integer.MIN_VALUE, map.get(Integer.MAX_VALUE).toArray(new Integer[1])[0].intValue());
    }
    
    /**
     * Tests that closed ranges work as expected.
     * <p>Also incidentally tests:</p>
     * <ul><li>Adjacent ranges in one range expression</li>
     * <li>Overlapping ranges in one range expression</li>
     * <li>Multiple individual positions</li>
     * <li>Spaces in range expressions</li>
     * <li>Gaps in range expressions</li>
     * <li>Implementation of
     * {@link org.apache.commons.collections4.MultiValuedMap} as bean member</li>
     * <li>Use of {@link org.apache.commons.collections4.multimap.ArrayListValuedHashMap}</li>
     * <li>First setting the bean type, then the error locale</li>
     * <li>Setting an error locale with complex many-to-one mappings</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testClosedRange() throws IOException {
        MappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(GoodJoinByPositionAnnotations.class);
        CsvToBean<GoodJoinByPositionAnnotations> csvToBean = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(GoodJoinByPositionAnnotations.class).withMappingStrategy(mappingStrategy).build();
        csvToBean.setErrorLocale(Locale.GERMAN);
        List<GoodJoinByPositionAnnotations> beans = csvToBean.parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<Integer, String> map = beans.get(0).getMap4();
        assertNotNull(map);
        assertTrue(map instanceof ArrayListValuedHashMap);
        assertEquals(10, map.keySet().size());
        Collection<String> values = map.get(4);
        assertEquals(1, values.size());
        assertTrue(values.contains("string4"));
        values = map.get(5);
        assertEquals(1, values.size());
        assertTrue(values.contains("string5"));
        values = map.get(6);
        assertEquals(1, values.size());
        assertTrue(values.contains("string6"));
        values = map.get(7);
        assertEquals(1, values.size());
        assertTrue(values.contains("string7"));
        values = map.get(8);
        assertEquals(1, values.size());
        assertTrue(values.contains("string8"));
        values = map.get(9);
        assertEquals(1, values.size());
        assertTrue(values.contains("string9"));
        values = map.get(10);
        assertEquals(1, values.size());
        assertTrue(values.contains("string10"));
        values = map.get(12);
        assertEquals(1, values.size());
        assertTrue(values.contains("string12"));
        values = map.get(13);
        assertEquals(1, values.size());
        assertTrue(values.contains("string13"));
        values = map.get(15);
        assertEquals(1, values.size());
        assertTrue(values.contains("string15"));
        
        map = beans.get(1).getMap4();
        assertNotNull(map);
        assertTrue(map instanceof ArrayListValuedHashMap);
        assertEquals(10, map.keySet().size());
        values = map.get(4);
        assertEquals(1, values.size());
        assertTrue(values.contains("string42"));
        values = map.get(5);
        assertEquals(1, values.size());
        assertTrue(values.contains("string52"));
        values = map.get(6);
        assertEquals(1, values.size());
        assertTrue(values.contains("string62"));
        values = map.get(7);
        assertEquals(1, values.size());
        assertTrue(values.contains("string72"));
        values = map.get(8);
        assertEquals(1, values.size());
        assertTrue(values.contains("string82"));
        values = map.get(9);
        assertEquals(1, values.size());
        assertTrue(values.contains("string92"));
        values = map.get(10);
        assertEquals(1, values.size());
        assertTrue(values.contains("string102"));
        values = map.get(12);
        assertEquals(1, values.size());
        assertTrue(values.contains("string122"));
        values = map.get(13);
        assertEquals(1, values.size());
        assertTrue(values.contains("string132"));
        values = map.get(15);
        assertEquals(1, values.size());
        assertTrue(values.contains("string152"));
        
        map = beans.get(2).getMap4();
        assertNotNull(map);
        assertTrue(map instanceof ArrayListValuedHashMap);
        assertEquals(10, map.keySet().size());
        values = map.get(4);
        assertEquals(1, values.size());
        assertTrue(values.contains("string43"));
        values = map.get(5);
        assertEquals(1, values.size());
        assertTrue(values.contains("string53"));
        values = map.get(6);
        assertEquals(1, values.size());
        assertTrue(values.contains("string63"));
        values = map.get(7);
        assertEquals(1, values.size());
        assertTrue(values.contains("string73"));
        values = map.get(8);
        assertEquals(1, values.size());
        assertTrue(values.contains("string83"));
        values = map.get(9);
        assertEquals(1, values.size());
        assertTrue(values.contains("string93"));
        values = map.get(10);
        assertEquals(1, values.size());
        assertTrue(values.contains("string103"));
        values = map.get(12);
        assertEquals(1, values.size());
        assertTrue(values.contains("string123"));
        values = map.get(13);
        assertEquals(1, values.size());
        assertTrue(values.contains("string133"));
        values = map.get(15);
        assertEquals(1, values.size());
        assertTrue(values.contains("string153"));
    }
    
    /**
     * Tests that an open range without an upper boundry works.
     * <p>Also tests:</p>
     * <ul><li>No accessor method for member variable</li>
     * <li>Locale conversion with date and position mapping</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testOpenRangeWithoutUpperBoundry() throws IOException {
        List<GoodJoinByPositionAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(GoodJoinByPositionAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<Integer, Date> map = beans.get(0).showMeTheSecondMap();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        Collection<Date> values = map.get(1);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 15).getTime()));
        values = map.get(16);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 1, 27).getTime()));
        values = map.get(17);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 13).getTime()));
        
        map = beans.get(1).showMeTheSecondMap();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        values = map.get(1);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 16).getTime()));
        values = map.get(16);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 1, 28).getTime()));
        values = map.get(17);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 14).getTime()));
        
        map = beans.get(2).showMeTheSecondMap();
        assertNotNull(map);
        assertEquals(3, map.keySet().size());
        values = map.get(1);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1978, 11, 17).getTime()));
        values = map.get(16);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(1974, 2, 1).getTime()));
        values = map.get(17);
        assertEquals(1, values.size());
        assertTrue(values.contains(new GregorianCalendar(2013, 3, 15).getTime()));
    }
    
    /**
     * Tests that a range expression of the form "maximum-minimum" functions
     * properly.
     * <p>Also incidentally tests:</p>
     * <ul><li>Bean member variable lacks an assignment method</li>
     * <li>Explicit map type</li></ul>
     * 
     * @throws IOException Never
     */
    @Test
    public void testRangeBackward() throws IOException {
        List<GoodJoinByPositionAnnotations> beans = new CsvToBeanBuilder(
                new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(GoodJoinByPositionAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<Integer, Integer> map = beans.get(0).getMap3();
        assertNotNull(map);
        assertTrue(map instanceof HashSetValuedHashMap);
        assertEquals(2, map.keySet().size());
        Collection<Integer> values = map.get(2);
        assertEquals(1, values.size());
        assertTrue(values.contains(20));
        values = map.get(3);
        assertEquals(1, values.size());
        assertTrue(values.contains(30));
        
        map = beans.get(1).getMap3();
        assertNotNull(map);
        assertTrue(map instanceof HashSetValuedHashMap);
        assertEquals(2, map.keySet().size());
        values = map.get(2);
        assertEquals(1, values.size());
        assertTrue(values.contains(21));
        values = map.get(3);
        assertEquals(1, values.size());
        assertTrue(values.contains(31));
        
        map = beans.get(2).getMap3();
        assertNotNull(map);
        assertTrue(map instanceof HashSetValuedHashMap);
        assertEquals(2, map.keySet().size());
        values = map.get(2);
        assertEquals(1, values.size());
        assertTrue(values.contains(22));
        values = map.get(3);
        assertEquals(1, values.size());
        assertTrue(values.contains(32));
    }
    
    @Test
    public void testReadConversionLocalePrimitiveHeaderMapping() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbynamegood.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<String, Integer> map = beans.get(0).getMap4();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(10000, map.get("conversion").toArray(new Integer[1])[0].intValue());
        
        map = beans.get(1).getMap4();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(11000, map.get("conversion").toArray(new Integer[1])[0].intValue());
        
        map = beans.get(2).getMap4();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(12000, map.get("conversion").toArray(new Integer[1])[0].intValue());
    }

    @Test
    public void testReadConversionLocalePrimitivePositionMapping() throws IOException {
        List<GoodJoinByPositionAnnotations> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbypositiongood.csv"))
                .withType(GoodJoinByPositionAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(3, beans.size());
        
        MultiValuedMap<Integer, Integer> map = beans.get(0).getMap5();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(20000, map.get(11).toArray(new Integer[1])[0].intValue());
        
        map = beans.get(1).getMap5();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(21000, map.get(11).toArray(new Integer[1])[0].intValue());
        
        map = beans.get(2).getMap5();
        assertNotNull(map);
        assertEquals(1, map.keySet().size());
        assertEquals(22000, map.get(11).toArray(new Integer[1])[0].intValue());
    }

    /**
     * Tests what happens when a required field specified as a single header
     * name is missing.
     * In case it's not clear, this test uses a header name mapping strategy.
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadEmptyIndividualRequiredFieldHeaderNameMapping() throws IOException {
        try {
            new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbypositionrequiredindividualmissing.csv"))
                    .withType(GoodJoinByPositionAnnotations.class).build().parse();
            fail("Exception should have been thrown.");
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(GoodJoinByPositionAnnotations.class, csve.getBeanClass());
            assertNotNull(csve.getDestinationFields());
            assertEquals(1, csve.getDestinationFields().size());
            assertEquals("map1", csve.getDestinationFields().get(0).getName());
            assertEquals(1, csve.getLineNumber());
        }
    }
    
    /**
     * Tests what happens if a field marked as required and fed out of multiple
     * headers is missing a value in one matching column.
     * In case it's not clear, this test uses a header name mapping strategy.
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadEmptyRegexSingleRequiredFieldHeaderNameMappingValueOnly() throws IOException {
        try {
            new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbynameonerequiredmissing.csv"))
                    .withType(GoodJoinByNameAnnotations.class).build().parse();
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(GoodJoinByNameAnnotations.class, csve.getBeanClass());
            assertEquals("map2", csve.getDestinationField().getName());
            assertEquals(1, csve.getLineNumber());
        }
    }
    
    /**
     * Tests what happens if a field marked as required and fed out of multiple
     * headers is missing all headers that would match.
     * In case it's not clear, this test uses a header name mapping strategy.
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadEmptyRegexAllRequiredFieldHeaderNameMapping() throws IOException {
        try {
            new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbynamerequiredheadermissing.csv"))
                    .withType(GoodJoinByNameAnnotations.class).build().parse();
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(GoodJoinByNameAnnotations.class, csve.getBeanClass());
            assertEquals("map2", csve.getDestinationField().getName());
            assertEquals(-1, csve.getLineNumber());
        }
    }
    
    /**
     * Tests what happens if at least one position for a field marked as
     * required with {@link CsvBindAndJoinByPosition} contains no value.
     * In case it's not clear, this test uses a column position mapping
     * strategy.
     * 
     * @throws IOException Never
     */
    @Test
    public void testReadEmptyIndividualRequiredFieldColumnPositionMapping() throws IOException {
        try {
            new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbypositionrequiredmissing.csv"))
                    .withType(GoodJoinByPositionAnnotations.class).build().parse();
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvRequiredFieldEmptyException);
            CsvRequiredFieldEmptyException csve = (CsvRequiredFieldEmptyException) e.getCause();
            assertEquals(GoodJoinByPositionAnnotations.class, csve.getBeanClass());
            assertEquals("map2", csve.getDestinationField().getName());
            assertEquals(1, csve.getLineNumber());
        }
    }
    
    @Test
    public void testReadEmptyOptionalFieldValueOnly() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbynameoptionalvaluemissing.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(1, beans.size());
        GoodJoinByNameAnnotations bean = beans.get(0);
        assertNotNull(bean.getMap4());
        assertTrue(bean.getMap4().isEmpty());
    }
    
    @Test
    public void testReadEmptyOptionalFieldHeader() throws IOException {
        List<GoodJoinByNameAnnotations> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbynameoptionalheadermissing.csv"))
                .withType(GoodJoinByNameAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(1, beans.size());
        GoodJoinByNameAnnotations bean = beans.get(0);
        assertNull(bean.getMap4());
    }
    
    @Test
    public void testReadEmptyOptionalFieldPosition() throws IOException {
        List<GoodJoinByPositionAnnotations> beans = new CsvToBeanBuilder(new FileReader("src/test/resources/testinputjoinbypositionoptionalmissing.csv"))
                .withType(GoodJoinByPositionAnnotations.class).build().parse();
        assertNotNull(beans);
        assertEquals(1, beans.size());
        GoodJoinByPositionAnnotations bean = beans.get(0);
        MultiValuedMap<Integer, Integer> map = bean.getMap3();
        assertNotNull(map);
        assertEquals(2, map.keySet().size());
        assertNull(map.get(2).toArray(new Integer[1])[0]);
        assertNull(map.get(3).toArray(new Integer[1])[0]);
    }
    
    /**
     * Tests what happens when a required field is missing in a column position
     * mapping strategy.
     * 
     * @throws CsvException Never
     */
    @Test
    public void testWriteEmptyRequiredFieldColumnPositionMapping() throws CsvException {
        GoodJoinByPositionAnnotations bean = new GoodJoinByPositionAnnotations();
        StringWriter w = new StringWriter();
        StatefulBeanToCsv<GoodJoinByPositionAnnotations> b2csv = new StatefulBeanToCsvBuilder(w).build();
        try {
            b2csv.write(bean);
            fail("Exception should have been thrown.");
        }
        catch(CsvRequiredFieldEmptyException e) {
            assertEquals(GoodJoinByPositionAnnotations.class, e.getBeanClass());
            assertNotNull(e.getDestinationFields());
            assertEquals(2, e.getDestinationFields().size());
            assertEquals("map1", e.getDestinationFields().get(0).getName());
            assertEquals("map2", e.getDestinationFields().get(1).getName());
            assertEquals(-1, e.getLineNumber());
        }
    }
    
    /**
     * Tests that multi-valued fields of (wrapped) primitives are written
     * correctly.
     * <p>Also incidentally tests</p>
     * <ul><li>Writing a date type</li>
     * <li>Conversion of a wrapped primitive with a locale</li>
     * <li>Conversion of a date type with a locale</li>
     * <li>Writing with a header mapping</li>
     * <li>Too few values for a multivalued field</li>
     * <li>Too many values for a multivalued field</li>
     * <li>No value for an optional field (null)</li>
     * <li>No value for an optional field (empty map)</li>
     * <li>Writing multiple multivalued beans</li>
     * <li>Subsequent bean has different headers than first bean</li></ul>
     * 
     * @throws CsvException Never
     */
    @Test
    public void testWritePrimitive() throws CsvException {
        List<GoodJoinByNameAnnotations> beanList = new ArrayList<>();
        
        GoodJoinByNameAnnotations bean = new GoodJoinByNameAnnotations();
        MultiValuedMap<String, Integer> map1 = new ArrayListValuedHashMap<>();
        map1.put("index", 1);
        map1.put("index", 2);
        map1.put("index", 3);
        bean.setMap1(map1);
        MultiValuedMap<String, Date> map2 = new ArrayListValuedHashMap<>();
        map2.put("date1", new GregorianCalendar(1978, 0, 15).getTime());
        map2.put("date2", new GregorianCalendar(2018, 1, 7).getTime());
        bean.setMap2(map2);
        MultiValuedMap<String, String> map3 = new ArrayListValuedHashMap<>();
        map3.put("test", "string1");
        bean.setMap3(map3);
        MultiValuedMap<String, Integer> map4 = new ArrayListValuedHashMap<>();
        map4.put("conversion", 10000);
        map4.put("conversion", 20000);
        bean.setMap4(map4);
        beanList.add(bean);
        
        bean = new GoodJoinByNameAnnotations();
        map1 = new ArrayListValuedHashMap<>();
        map1.put("index", 4);
        map1.put("index", 5);
        // Third value is missing; "required" applies to the bean field, not every column
        bean.setMap1(map1);
        map2 = new ArrayListValuedHashMap<>();
        map2.put("date1", new GregorianCalendar(1978, 0, 16).getTime());
        map2.put("date2", new GregorianCalendar(2018, 1, 8).getTime());
        bean.setMap2(map2);
        map3 = new ArrayListValuedHashMap<>();
        map3.put("test", "string2");
        bean.setMap3(map3);
        map4 = new ArrayListValuedHashMap<>();
        map4.put("conversion", 10001);
        map4.put("conversion", 20002);
        bean.setMap4(map4);
        beanList.add(bean);
        
        bean = new GoodJoinByNameAnnotations();
        map1 = new ArrayListValuedHashMap<>();
        map1.put("index", 6);
        map1.put("index", 7);
        map1.put("index", 8);
        map1.put("index", 9); // Fourth value
        map1.put("unknown header", -1); // Different headers from first bean will be ignored
        bean.setMap1(map1);
        map2 = new ArrayListValuedHashMap<>();
        map2.put("date1", new GregorianCalendar(1978, 0, 17).getTime());
        map2.put("date2", new GregorianCalendar(2018, 1, 9).getTime());
        bean.setMap2(map2);
        map3 = new ArrayListValuedHashMap<>();
        map3.put("test", "string3");
        bean.setMap3(map3);
        map4 = new ArrayListValuedHashMap<>();
        map4.put("conversion", 10003);
        map4.put("conversion", 20004);
        bean.setMap4(map4);
        beanList.add(bean);
        
        bean = new GoodJoinByNameAnnotations();
        map1 = new ArrayListValuedHashMap<>();
        map1.put("index", 10);
        map1.put("index", 11);
        map1.put("index", 12);
        bean.setMap1(map1);
        map2 = new ArrayListValuedHashMap<>();
        map2.put("date1", new GregorianCalendar(1978, 0, 18).getTime());
        map2.put("date2", new GregorianCalendar(2018, 1, 10).getTime());
        bean.setMap2(map2);
        map3 = new ArrayListValuedHashMap<>();
        map3.put("test", "string4");
        bean.setMap3(map3);
        bean.setMap4(null); // map4 missing, but optional
        beanList.add(bean);
        
        bean = new GoodJoinByNameAnnotations();
        map1 = new ArrayListValuedHashMap<>();
        map1.put("index", 13);
        map1.put("index", 14);
        map1.put("index", 15);
        bean.setMap1(map1);
        map2 = new ArrayListValuedHashMap<>();
        map2.put("date1", new GregorianCalendar(1978, 0, 19).getTime());
        map2.put("date2", new GregorianCalendar(2018, 1, 11).getTime());
        bean.setMap2(map2);
        map3 = new ArrayListValuedHashMap<>();
        map3.put("test", "string5");
        bean.setMap3(map3);
        bean.setMap4(new ArrayListValuedHashMap<String, Integer>());
        beanList.add(bean);
        
        StringWriter w = new StringWriter();
        StatefulBeanToCsv<GoodJoinByNameAnnotations> btc = new StatefulBeanToCsvBuilder<GoodJoinByNameAnnotations>(w).build();
        btc.write(beanList);
        assertTrue(Pattern.matches(
                "\"conversion\",\"conversion\",\"date1\",\"date2\",\"index\",\"index\",\"index\"\n"
                + "\"10.000\",\"20.000\",\"15. Jan\\.? 1978\",\"07. Feb\\.? 2018\",\"1\",\"2\",\"3\"\n"
                + "\"10.001\",\"20.002\",\"16. Jan\\.? 1978\",\"08. Feb\\.? 2018\",\"4\",\"5\",\"\"\n"
                + "\"10.003\",\"20.004\",\"17. Jan\\.? 1978\",\"09. Feb\\.? 2018\",\"6\",\"7\",\"8\"\n"
                + "\"\",\"\",\"18. Jan\\.? 1978\",\"10. Feb\\.? 2018\",\"10\",\"11\",\"12\"\n"
                + "\"\",\"\",\"19. Jan\\.? 1978\",\"11. Feb\\.? 2018\",\"13\",\"14\",\"15\"\n",
                w.toString()));
    }
    
    /**
     * Tests that writing with a column position-based mapping strategy works.
     * <p>Also incidentally tests:</p>
     * <ul><li>Writing more than one bean works with the column position
     * strategy.</li>
     * <li>More values for a position than one</li>
     * <li>Writing empty optional positions</li>
     * <li>Using a column position in a bean field that would not be read by
     * that bean field</li></ul>
     * 
     * @throws CsvException Never
     */
    @Test
    public void testWriteColumnMapping() throws CsvException {
        List<GoodJoinByPositionAnnotationsForWriting> beanList = new ArrayList<>();
        
        GoodJoinByPositionAnnotationsForWriting bean = new GoodJoinByPositionAnnotationsForWriting();
        MultiValuedMap<Integer, Integer> map1 = new HashSetValuedHashMap<>();
        map1.put(0, 10);
        map1.put(0, 11); // Two values for one position can never work
        bean.setMap1(map1);
        MultiValuedMap<Integer, Date> map2 = new HashSetValuedHashMap<>();
        map2.put(1, new GregorianCalendar(1978, 0, 15).getTime());
        map2.put(16, new GregorianCalendar(2018, 2, 6).getTime());
        bean.setMap2(map2);
        ArrayListValuedHashMap<Integer, String> map4 = new ArrayListValuedHashMap<>();
        map4.put(4, "string4");
        map4.put(5, "string5");
        map4.put(6, "string6");
        map4.put(7, "string7");
        map4.put(8, "string8");
        map4.put(9, "string9");
        map4.put(10, "string10");
        map4.put(11, "string11"); // Matched by another field
        map4.put(12, "string12");
        map4.put(13, "string13");
        map4.put(14, "string14"); // Matched by no field
        map4.put(15, "string15");
        bean.setMap4(map4);
        MultiValuedMap<Integer, Integer> map5 = new HashSetValuedHashMap<>();
        map5.put(11, 1111);
        bean.setMap5(map5);
        beanList.add(bean);
        
        bean = new GoodJoinByPositionAnnotationsForWriting();
        map1 = new HashSetValuedHashMap<>();
        map1.put(0, 12);
        bean.setMap1(map1);
        map2 = new HashSetValuedHashMap<>();
        map2.put(1, new GregorianCalendar(1978, 0, 16).getTime());
        map2.put(16, new GregorianCalendar(2018, 2, 7).getTime());
        bean.setMap2(map2);
        map4 = new ArrayListValuedHashMap<>();
        map4.put(4, "string42");
        map4.put(5, "string52");
        map4.put(6, "string62");
        map4.put(7, "string72");
        map4.put(8, "string82");
        map4.put(9, "string92");
        map4.put(10, "string102");
        map4.put(12, "string122");
        map4.put(13, "string132");
        map4.put(15, "string152");
        bean.setMap4(map4);
        map5 = new HashSetValuedHashMap<>();
        map5.put(11, 1112);
        bean.setMap5(map5);
        beanList.add(bean);
        
        StringWriter w = new StringWriter();
        StatefulBeanToCsv<GoodJoinByPositionAnnotationsForWriting> btc = new StatefulBeanToCsvBuilder<GoodJoinByPositionAnnotationsForWriting>(w).build();
        btc.write(beanList);
        assertTrue(Pattern.matches(
                "\"10\",\"15\\. Jan\\.? 1978\",\"\",\"\",\"string4\",\"string5\",\"string6\",\"string7\",\"string8\",\"string9\",\"string10\",\"1.111\",\"string12\",\"string13\",\"\",\"string15\",\"06\\. März? 2018\"\n"
                + "\"12\",\"16\\. Jan\\.? 1978\",\"\",\"\",\"string42\",\"string52\",\"string62\",\"string72\",\"string82\",\"string92\",\"string102\",\"1.112\",\"string122\",\"string132\",\"\",\"string152\",\"07\\. März? 2018\"\n",
                w.toString()));
    }
    
    @Test
    public void testWriteEmptyRequiredFieldInFirstBean() throws CsvException {
        for(MultiValuedMap<String, Integer> map1 : Arrays.asList(null, new ArrayListValuedHashMap<String, Integer>())) {
            GoodJoinByNameAnnotations bean = new GoodJoinByNameAnnotations();
            bean.setMap1(map1); // Required
            MultiValuedMap<String, Date> map2 = new ArrayListValuedHashMap<>();
            map2.put("date1", new GregorianCalendar(1978, 0, 15).getTime());
            map2.put("date2", new GregorianCalendar(2018, 1, 7).getTime());
            bean.setMap2(map2);
            MultiValuedMap<String, String> map3 = new ArrayListValuedHashMap<>();
            map3.put("test", "string1");
            bean.setMap3(map3);
            MultiValuedMap<String, Integer> map4 = new ArrayListValuedHashMap<>();
            map4.put("conversion", 10000);
            map4.put("conversion", 20000);
            bean.setMap4(map4);

            StringWriter w = new StringWriter();
            StatefulBeanToCsv<GoodJoinByNameAnnotations> btc = new StatefulBeanToCsvBuilder<GoodJoinByNameAnnotations>(w).build();
            try {
                btc.write(bean);
                fail("Exception should have been thrown.");
            }
            catch(CsvRequiredFieldEmptyException e) {
                assertEquals(-1, e.getLineNumber());
                assertEquals(GoodJoinByNameAnnotations.class, e.getBeanClass());
                assertEquals("map1", e.getDestinationField().getName());
            }
        }
    }
    
    @Test
    public void testWriteEmptyRequiredFieldInSecondBean() throws CsvException {
        for(MultiValuedMap<String, Integer> map1version2 : Arrays.asList(null, new ArrayListValuedHashMap<String, Integer>())) {
            List<GoodJoinByNameAnnotations> beanList = new ArrayList<>();
        
            GoodJoinByNameAnnotations bean = new GoodJoinByNameAnnotations();
            MultiValuedMap<String, Integer> map1 = new ArrayListValuedHashMap<>();
            map1.put("index", 1);
            map1.put("index", 2);
            map1.put("index", 3);
            bean.setMap1(map1);
            MultiValuedMap<String, Date> map2 = new ArrayListValuedHashMap<>();
            map2.put("date1", new GregorianCalendar(1978, 0, 15).getTime());
            map2.put("date2", new GregorianCalendar(2018, 1, 7).getTime());
            bean.setMap2(map2);
            MultiValuedMap<String, Integer> map4 = new ArrayListValuedHashMap<>();
            map4.put("conversion", 10000);
            map4.put("conversion", 20000);
            bean.setMap4(map4);
            beanList.add(bean);

            bean = new GoodJoinByNameAnnotations();
            bean.setMap1(map1version2);
            map2 = new ArrayListValuedHashMap<>();
            map2.put("date1", new GregorianCalendar(1978, 0, 16).getTime());
            map2.put("date2", new GregorianCalendar(2018, 1, 8).getTime());
            bean.setMap2(map2);
            map4 = new ArrayListValuedHashMap<>();
            map4.put("conversion", 10001);
            map4.put("conversion", 20002);
            bean.setMap4(map4);
            beanList.add(bean);

            StringWriter w = new StringWriter();
            StatefulBeanToCsv<GoodJoinByNameAnnotations> btc = new StatefulBeanToCsvBuilder<GoodJoinByNameAnnotations>(w).build();
            try {
                btc.write(beanList);
                fail("Exception should have been thrown.");
            }
            catch(CsvRequiredFieldEmptyException e) {
                assertEquals(2, e.getLineNumber());
                assertEquals(GoodJoinByNameAnnotations.class, e.getBeanClass());
                assertEquals("map1", e.getDestinationField().getName());
            }
        }
    }
    
    @Test
    public void testWriteFieldWithoutGetter() throws CsvException {
        GoodJoinByPositionAnnotations bean = new GoodJoinByPositionAnnotations();
        ArrayListValuedHashMap<Integer, Integer> map1 = new ArrayListValuedHashMap<>();
        map1.put(0, Integer.MIN_VALUE);
        bean.setMap1(map1);
        ArrayListValuedHashMap<Integer, Date> map2 = new ArrayListValuedHashMap<>();
        map2.put(1, new GregorianCalendar(1974, 1, 27).getTime());
        map2.put(16, new GregorianCalendar(1978, 0, 15).getTime());
        map2.put(17, new GregorianCalendar(2003, 3, 13).getTime());
        bean.setMap2(map2);
        
        StringWriter w = new StringWriter();
        StatefulBeanToCsv b2csv = new StatefulBeanToCsvBuilder(w).build();
        try {
            b2csv.write(bean);
            fail("Exception should have been thrown");
        }
        catch(CsvBeanIntrospectionException e) {
            assertEquals(bean, e.getBean());
            assertEquals("map2", e.getField().getName());
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
    
    @Test
    public void testUnknownMultiValuedMap() {
        try {
            new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY))
                    .withType(UnknownMultiValuedMapField.class)
                    .build();
            fail("Exception should have been thrown");
        }
        catch(CsvBadConverterException e) {
            assertEquals(BeanFieldJoin.class, e.getConverterClass());
        }
    }
    
    @Test
    public void testUnassignableMultiValuedMap() {
        try {
            new CsvToBeanBuilder(new StringReader(StringUtils.EMPTY))
                    .withType(MismatchedMultiValuedMap.class)
                    .build();
            fail("Exception should have been thrown.");
        }
        catch(CsvBadConverterException e) {
            assertEquals(BeanFieldJoin.class, e.getConverterClass());
        }
    }
    
    @Test
    public void testBeanInstantiationImpossibleIllegalAccess() {
        try {
            new CsvToBeanBuilder(new StringReader("map\n1"))
                    .withType(InstantiationImpossibleIllegalAccess.class)
                    .build().parse();
            fail("Exception should have been thrown.");
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvBeanIntrospectionException);
            CsvBeanIntrospectionException csve = (CsvBeanIntrospectionException)e.getCause();
            assertEquals("map", csve.getField().getName());
        }
    }
    
    @Test
    public void testNoNullaryConstructor() {
        try {
            new CsvToBeanBuilder(new StringReader("map\n1"))
                    .withType(NoNullaryConstructor.class)
                    .build().parse();
            fail("Exception should have been thrown.");
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvBadConverterException);
            CsvBadConverterException csve = (CsvBadConverterException)e.getCause();
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
        }
    }
    
    @Test
    public void testNoNullaryConstructorNoSetter() {
        try {
            new CsvToBeanBuilder(new StringReader("map\n1"))
                    .withType(NoNullaryConstructorNoSetter.class)
                    .build().parse();
            fail("Exception should have been thrown.");
        }
        catch(RuntimeException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof CsvBadConverterException);
            CsvBadConverterException csve = (CsvBadConverterException)e.getCause();
            assertEquals(BeanFieldJoin.class, csve.getConverterClass());
        }
    }
}
