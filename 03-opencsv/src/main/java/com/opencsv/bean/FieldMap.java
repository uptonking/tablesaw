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

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.util.Collection;
import java.util.Locale;

/**
 * Defines the basic characteristics of a map between field identifiers and
 * their associated {@link BeanField}s.
 * Such a mapping requires a method of matching multiple fields. This method
 * will require data to initialize itself. Such a mapping also requires a key to
 * index the {@link BeanField} that is to be provided for a match.
 * 
 * @param <I> The initializer type used to build the many-to-one mapping
 * @param <K> Type of the field identifier (key)
 * @param <C> Type of the ComplexFieldMapEntry used. This is specified as a
 *   parameter so we can provide type safety and polymorphism through a generic
 *   interface on the one hand, and still extend specific implementations with
 *   methods needed for that type of complex mapping without having to add every
 *   such method to the generic interface.
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public interface FieldMap<I, K, C extends ComplexFieldMapEntry<I, K>> {

    /**
     * This method generates a header that can be used for writing beans of the
     * type provided back to a file.
     * Whether or not this generated header is used is decided by the
     * {@link com.opencsv.bean.MappingStrategy} in use. The ordering of the
     * headers should be carefully considered by the implementing class.
     * 
     * @param bean One perfect, shining example of how the bean to be written
     *   should look. The most crucial thing is, for fields that result from
     *   joining multiple fields on reading and thus need to be split on
     *   writing, the {@link org.apache.commons.collections4.MultiValuedMap} in
     *   question must have the complete structure of the header to be
     *   generated, even if some values are empty.
     * @return An array of header names for the output file
     * @throws CsvRequiredFieldEmptyException If a required header is missing
     *   while attempting to write. Since every other header is hard-wired
     *   through the bean fields and their associated annotations, this can only
     *   happen with multi-valued fields.
     */
    String[] generateHeader(final Object bean) throws CsvRequiredFieldEmptyException;

    /**
     * Gets the {@link BeanField} associated with this key.
     * If a key could possibly match both a regular, simple key (one added with
     * {@link #put(java.lang.Object, com.opencsv.bean.BeanField)}), and a
     * complex key (one added with
     * {@link #putComplex(java.lang.Object, com.opencsv.bean.BeanField)}), the
     * simple key is always matched. If a key could match more than one complex
     * key, the return value is undefined.
     * 
     * @param key The key under which to search for a {@link BeanField}
     * @return The {@link BeanField} found, or null if none is present
     */
    BeanField get(final K key);

    /**
     * Associates the given {@link BeanField} with the given {@code key}.
     * 
     * @param key The key under which to index the provided {@link BeanField}
     * @param value The {@link BeanField} to be indexed
     * @return If there was a value previously associated with this key, it is
     *   returned
     */
    BeanField put(final K key, final BeanField value);
    
    /**
     * Adds a {@link BeanField} to this map indexed by the data in
     * {@code initializer}.
     * This is what makes this map special: It allows one to define
     * characteristics of a method to match multiple keys.
     * 
     * @param initializer Whatever data the implementation needs to match
     *   multiple keys
     * @param value The {@link BeanField} that is to be returned on a later
     *   match
     */
    void putComplex(final I initializer, final BeanField value);
    
    /**
     * Sets the locale to be used for error messages.
     * 
     * @param errorLocale The locale to be used for error messages
     */
    void setErrorLocale(final Locale errorLocale);
    
    /**
     * Provides all values currently in the map.
     * 
     * @return The values in the map
     */
    Collection<BeanField> values();
    
}
