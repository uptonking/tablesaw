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

import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.util.Locale;

/**
 * Classes implementing this interface perform a conversion from String to
 * some type on reading and some type to String on writing.
 * This interface is used by BeanField to perform the actual data conversion.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public interface CsvConverter {
    /**
     * Method for converting from a string to the proper data type of the
     * destination field.
     *
     * @param value The string from the selected field of the CSV file. If the
     *   field is marked as required in the annotation, this value is guaranteed
     *   not to be null, empty or blank according to
     *   {@link org.apache.commons.lang3.StringUtils#isBlank(java.lang.CharSequence)}
     * @return An {@link java.lang.Object} representing the input data converted
     *   into the proper type
     * @throws CsvDataTypeMismatchException    If the input string cannot be converted into
     *                                         the proper type
     * @throws CsvConstraintViolationException When the internal structure of
     *                                         data would be violated by the data in the CSV file
     */
    Object convertToRead(String value)
            throws CsvDataTypeMismatchException, CsvConstraintViolationException;
    
    /**
     * Method for converting from the data type of the destination field to a
     * string.
     * 
     * @param value The contents of the field currently being processed from the
     *   bean to be written. Can be null if the field is not marked as required.
     * @return A string representation of the value of the field in question in
     *   the bean passed in, or an empty string if {@code value} is null
     * @throws CsvDataTypeMismatchException If the input cannot be converted to
     *   a string by this converter
     */
    String convertToWrite(Object value)
            throws CsvDataTypeMismatchException;
    
    /**
     * Sets the locale for all error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     */
    void setErrorLocale(Locale errorLocale);
}
