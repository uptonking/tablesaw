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

import java.util.Locale;

/**
 * Defines the basic functionality necessary for using a many-to-one mapping
 * between columns of a CSV file and bean fields.
 * Such a mapping requires a method of matching multiple fields. This method
 * will require data to initialize itself. Such a mapping also requires a key to
 * index the {@link BeanField} that is to be provided for a match.
 * 
 * @param <I> The initializer type used to build the many-to-one mapping
 * @param <K> The type of the key used for indexing
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public interface ComplexFieldMapEntry<I, K> {
    
    /**
     * Determines whether or not the given key is contained in this entry.
     * 
     * @param key The key to be located
     * @return Whether {@code key} is represented by this entry
     */
    boolean contains(K key);
    
    /**
     * @return The {@link BeanField} to which this entry maps
     */
    BeanField getBeanField();
    
    /**
     * Returns the information used to initialize this entry.
     * This information is not guaranteed to be exactly the same as the original
     * value, but is functionally equivalent.
     * 
     * @return The original information used to initialize this mapping entry
     */
    I getInitializer();
    
    /**
     * Sets the locale to be used for error messages.
     * 
     * @param errorLocale The locale to be used for error messages
     */
    void setErrorLocale(final Locale errorLocale);
}
