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
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class concerns itself with handling single-valued bean fields.
 * 
 * @param <T> The type of the bean being populated
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class BeanFieldSingleValue<T> extends AbstractBeanField<T> {
    
    /**
     * Simply calls the same constructor in the base class.
     * @param field A {@link java.lang.reflect.Field} object.
     * @param required Whether or not this field is required in input
     * @param errorLocale The errorLocale to use for error messages.
     * @param converter The converter to be used to perform the actual data
     *   conversion
     * @see AbstractBeanField#AbstractBeanField(java.lang.reflect.Field, boolean, java.util.Locale, com.opencsv.bean.CsvConverter) 
     */
    public BeanFieldSingleValue(Field field, boolean required, Locale errorLocale, CsvConverter converter) {
        super(field, required, errorLocale, converter);
    }

    /**
     * Passes the string to be converted to the converter.
     * @throws CsvBadConverterException If the converter is null
     */
    // The rest of the Javadoc is inherited
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if(converter != null) {
            return converter.convertToRead(value);
        }
        throw new CsvBadConverterException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("no.converter.specified"));
    }
    
    /**
     * Passes the object to be converted to the converter.
     * @throws CsvBadConverterException If the converter is null
     */
    // The rest of the Javadoc is inherited
    @Override
    protected String convertToWrite(Object value) throws CsvDataTypeMismatchException {
        if(converter != null) {
            return converter.convertToWrite(value);
        }
        throw new CsvBadConverterException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("no.converter.specified"));
    }
}
