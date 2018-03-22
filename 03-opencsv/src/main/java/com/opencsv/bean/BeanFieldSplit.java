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

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.lang3.StringUtils;

/**
 * This class concerns itself with handling collection-valued bean fields.
 * 
 * @param <T> The type of the bean being populated
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class BeanFieldSplit<T> extends AbstractBeanField<T> {
    
    private Pattern splitOn;
    private final String writeDelimiter;
    private final Class<? extends Collection> collectionType;
    
    /**
     * The only valid constructor.
     * 
     * @param field A {@link java.lang.reflect.Field} object.
     * @param required Whether or not this field is required in input
     * @param errorLocale The errorLocale to use for error messages.
     * @param converter The converter to be used to perform the actual data
     *   conversion
     * @param splitOn See {@link CsvBindAndSplitByName#splitOn()}
     * @param writeDelimiter See {@link CsvBindAndSplitByName#writeDelimiter()}
     * @param collectionType  See {@link CsvBindAndSplitByName#collectionType()}
     */
    public BeanFieldSplit(
            Field field, boolean required, Locale errorLocale,
            CsvConverter converter, String splitOn, String writeDelimiter,
            Class<? extends Collection> collectionType) {
        
        // Simple assignments
        super(field, required, errorLocale, converter);
        this.writeDelimiter = writeDelimiter;
        
        // Check that we really have a collection
        if(!Collection.class.isAssignableFrom(field.getType())) {
            throw new CsvBadConverterException(
                    BeanFieldSplit.class,
                    String.format(
                            ResourceBundle.getBundle(
                                    ICSVParser.DEFAULT_BUNDLE_NAME,
                                    errorLocale).getString("invalid.collection.type"),
                            field.getType().toString()));
        }
        
        // Check the regular expression for validity and compile once for speed
        try {
            this.splitOn = Pattern.compile(splitOn);
        }
        catch(PatternSyntaxException e) {
            // Just so this.splitOn isn't null. Shouldn't ever be used, but if
            // it is, it shouldn't match anything.
            this.splitOn = Pattern.compile(StringUtils.EMPTY);
            
            CsvBadConverterException csve = new CsvBadConverterException(
                    BeanFieldSplit.class,
                    String.format(ResourceBundle.getBundle(
                            ICSVParser.DEFAULT_BUNDLE_NAME,
                            errorLocale).getString("invalid.regex"), splitOn));
            csve.initCause(e);
            throw csve;
        }
        
        // Determine the Collection implementation that should be instantiated
        // for every bean.
        Class<?> fieldType = field.getType();
        if(!fieldType.isInterface()) {
            this.collectionType = (Class<Collection>)field.getType();
        }
        else if(!collectionType.isInterface()) {
            this.collectionType = collectionType;
        }
        else {
            if(Collection.class.equals(fieldType) || List.class.equals(fieldType)) {
                this.collectionType = ArrayList.class;
            }
            else if(Set.class.equals(fieldType)) {
                this.collectionType = HashSet.class;
            }
            else if(SortedSet.class.equals(fieldType) || NavigableSet.class.equals(fieldType)) {
                this.collectionType = TreeSet.class;
            }
            else if(Queue.class.equals(fieldType) || Deque.class.equals(fieldType)) {
                this.collectionType = ArrayDeque.class;
            }
            else if(Bag.class.equals(fieldType)) {
                this.collectionType = HashBag.class;
            }
            else if(SortedBag.class.equals(fieldType)) {
                this.collectionType = TreeBag.class;
            }
            else {
                this.collectionType = null;
                throw new CsvBadConverterException(
                        BeanFieldSplit.class,
                        String.format(
                                ResourceBundle.getBundle(
                                        ICSVParser.DEFAULT_BUNDLE_NAME,
                                        errorLocale).getString("invalid.collection.type"),
                                collectionType.toString()));
            }
        }
        
        // Now that we know what type we want to assign, run one last check
        // that assignment is truly possible
        if(!field.getType().isAssignableFrom(this.collectionType)) {
            throw new CsvBadConverterException(
                    BeanFieldSplit.class,
                    String.format(
                            ResourceBundle.getBundle(
                                    ICSVParser.DEFAULT_BUNDLE_NAME,
                                    errorLocale).getString("unassignable.collection.type"),
                            collectionType.getName(), field.getType().getName()));
        }
    }

    /**
     * This method manages the collection being created as well as splitting the
     * data.
     * Once the data are split, they are sent to the converter for the actual
     * conversion.
     * 
     * @see ConverterPrimitiveTypes#convertToRead(java.lang.String) 
     * @see ConverterDate#convertToRead(java.lang.String) 
     */
    // The rest of the Javadoc is inherited
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        Collection<Object> collection;
        try {
            collection = collectionType.newInstance();
        }
        catch(InstantiationException | IllegalAccessException e) {
            CsvBeanIntrospectionException csve = new CsvBeanIntrospectionException(
                    String.format(
                            ResourceBundle
                                    .getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                    .getString("collection.cannot.be.instantiated"),
                            collectionType.getCanonicalName()));
            csve.initCause(e);
            throw csve;
        }
        
        String[] splitValues = splitOn.split(value);
        for(String s : splitValues) {
            collection.add(converter.convertToRead(s));
        }
        return collection;
    }

    /**
     * Manages converting a collection of values into a single string.
     * The conversion of each individual value is performed by the converter.
     * @throws CsvRequiredFieldEmptyException This implementation does not throw
     *   this exception
     */
    // The rest of the Javadoc is inherited
    @Override
    protected String convertToWrite(Object value)
            throws CsvDataTypeMismatchException {
        String retval = StringUtils.EMPTY;
        if(value != null) {
            Collection<Object> collection = (Collection<Object>) value;
            String[] convertedValue = new String[collection.size()];
            int i = 0;
            for(Object o : collection) {
                convertedValue[i++] = converter.convertToWrite(o);
            }
            retval = StringUtils.join(convertedValue, writeDelimiter);
        }
        return retval;
    }
    
    /**
     * Checks that {@code value} is not null and not an empty
     * {@link java.util.Collection}.
     */
    // The rest of the Javadoc is inherited
    @Override
    protected boolean isFieldEmptyForWrite(Object value) {
        return super.isFieldEmptyForWrite(value) || ((Collection<Object>)value).isEmpty();
    }
}
