package com.opencsv.bean;

/*
 * Copyright 2005 Bytecode Pty Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.opencsv.CSVReader;
import com.opencsv.ICSVParser;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Builder for creating an IterableCSVToBean.
 * This should be the preferred method of creating an IterableCSVToBean to keep
 * the number of constructors to a minimum.
 *
 * <pre>
 * {@code
 * IterableCSVToBean bean =
 * new IterableCSVToBean()
 * .withReader(csvReader)
 * .withMapper(mappingStrategy)
 * .withFilter(csvToBeanFilter)
 * .build();
 * }
 * </pre>
 *
 * @see IterableCSVToBean
 *
 * @param <T> Type of the bean to be iterated over
 * @deprecated Use {@link CsvToBeanBuilder} instead, then call
 *   {@link CsvToBean#iterator() } on the result
 */
@Deprecated
public class IterableCSVToBeanBuilder<T> {

    private MappingStrategy<T> mapper;
    private CSVReader csvReader;
    private CsvToBeanFilter filter;
    private Locale errorLocale = Locale.getDefault();

    /**
     * Default constructor.
     */
    public IterableCSVToBeanBuilder() {
    }

    /**
     * Creates the IterableCSVToBean.
     *
     * Will throw a runtime exception if the MappingStrategy or CSVReader is not set.
     *
     * @return An instance of IterableCSVToBean
     */
    public IterableCSVToBean<T> build() {
        if (mapper == null) {
            throw new RuntimeException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("strategy.undefined"));
        }
        if (csvReader == null) {
            throw new RuntimeException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("csvreader.null"));
        }
        IterableCSVToBean<T> result = new IterableCSVToBean<>(csvReader, mapper, filter);
        result.setErrorLocale(errorLocale);
        return result;
    }

    /**
     * Sets the MappingStrategy to be used by the builder.
     *
     * @param mappingStrategy An object that implements
     *   {@link com.opencsv.bean.MappingStrategy}
     * @return The builder with the MappingStrategy set
     */
    public IterableCSVToBeanBuilder<T> withMapper(final MappingStrategy<T> mappingStrategy) {
        this.mapper = mappingStrategy;
        return this;
    }

    /**
     * Sets the reader to be used by the builder.
     * @param reader CSVReader to be incorporated in the builder.
     * @return The builder with the CSVReader set
     */
    public IterableCSVToBeanBuilder<T> withReader(final CSVReader reader) {
        this.csvReader = reader;
        return this;
    }

    /**
     * Used by unit tests.
     * @return The MappingStrategy to be used by the builder.
     */
    protected MappingStrategy<T> getStrategy() {
        return mapper;
    }

    /**
     * Used by unit tests.
     * @return The csvReader to be used by the builder.
     */
    protected CSVReader getCsvReader() {
        return csvReader;
    }

    /**
     * Used by unit tests.
     *
     * @return Filter to be used by the builder.
     */
    protected Object getFilter() {
        return filter;
    }

    /**
     * Sets the filter used to remove unwanted data from the CSV file.
     *
     * @param filter An object implementing CsvToBeanFilter
     * @return The builder with the filter set
     */
    public IterableCSVToBeanBuilder<T> withFilter(final CsvToBeanFilter filter) {
        this.filter = filter;
        return this;
    }
    
    /**
     * Sets the locale for all error messages.
     * @param errorLocale The locale to be used for all error messages. If null,
     *   the default locale is used.
     * @return this
     * @since 4.0
     */
    public IterableCSVToBeanBuilder<T> withErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        return this;
    }
}
