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
package com.opencsv.bean.customconverter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;

/**
 * This class takes a string and splits it on whitespace into a list of strings.
 *
 * @param <T> Type of the bean to be manipulated
 * 
 * @author Andrew Rucker Jones
 * @since 3.8
 * @deprecated Use {@link com.opencsv.bean.CsvBindAndSplitByName} or
 *   {@link com.opencsv.bean.CsvBindAndSplitByPosition} instead
 */
@Deprecated
public class ConvertSplitOnWhitespace<T> extends AbstractBeanField<T> {

    /**
     * Silence code style checker by adding a useless constructor.
     */
    public ConvertSplitOnWhitespace() {
    }

    /**
     * Takes a string that is a list of substrings separated by whitespace and
     * returns a list of the substrings.
     * For example, the string "Jones Smith Cartwright Cooper" would be
     * converted by this method to ("Jones", "Smith", "Cartwright", "Cooper").
     * When might this be useful? A CSV has a set number of columns, but
     * sometimes some fields might need to contain a variable number of entries.
     * In this case, a list within one field of a CSV might make sense.
     *
     * @param value The string to be converted
     * @return List&lt;String&gt; consisting of the substrings from the input
     * that were separated in the input by whitespace
     * 
     */
    @Override
    protected Object convert(String value) {
        List<String> l = null;
        if (!StringUtils.isEmpty(value)) {
            l = new ArrayList<>(Arrays.asList(value.split("\\s+")));
        }
        return l;
    }
    
    /**
     * This method takes the current value of the field in question in the bean
     * passed in and converts it to a string.
     * 
     * @return The concatenation of a list of strings, with every entry
     *   separated by a space
     * @throws CsvDataTypeMismatchException If the field is not a list of strings
     */
    @Override
    protected String convertToWrite(Object value) throws CsvDataTypeMismatchException {
        String result = "";
        try {
            if(value != null) {
                @SuppressWarnings("unchecked") List<String> values = (List<String>) value;
                result = StringUtils.join(values, ' ');
            }
        }
        catch(ClassCastException e) {
            CsvDataTypeMismatchException csve =
                    new CsvDataTypeMismatchException(ResourceBundle
                            .getBundle("convertSplitOnWhitespace", errorLocale)
                            .getString("wrong.type"));
            csve.initCause(e);
            throw csve;
        }
        return result;
    }

}
