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

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBadConverterException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Maps any header name matching a regular expression to a {@link BeanField}.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class RegexToBeanField extends AbstractFieldMapEntry<String, String> {

    /** The compiled regular expression used to match header names. */
    private final Pattern regex;

    /**
     * Initializes this mapping with the regular expression used to map header
     * names and the {@link BeanField} they should be mapped to.
     * 
     * @param pattern A valid regular expression against which potential header
     *   names are matched
     * @param field The {@link BeanField} this mapping maps to
     * @param errorLocale The locale for error messages
     */
    public RegexToBeanField(final String pattern, final BeanField field, final Locale errorLocale) {
        super(field, errorLocale);
        try {
            regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
        catch(PatternSyntaxException e) {
            CsvBadConverterException csve = new CsvBadConverterException(BeanFieldJoin.class, 
                    ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale)
                            .getString("invalid.regex"));
            csve.initCause(e);
            throw csve;
        }
    }
    
    @Override
    public boolean contains(String key) {
        final Matcher m = regex.matcher(key);
        return m.matches();
    }
    
    @Override
    public String getInitializer() {
        return regex.pattern();
    }
}
