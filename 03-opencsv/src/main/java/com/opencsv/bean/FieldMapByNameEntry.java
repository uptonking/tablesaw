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

/**
 * Represents one entry in {@link FieldMapByName}.
 * Note: This is not used in the internal structure of {@link FieldMapByName}, but
 * rather when representing its contents to the outside world.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class FieldMapByNameEntry {
    
    /**
     * The name of the header or a regular expression pattern matching possible
     * names for the header.
     */
    private final String name;
    
    /** The {@link BeanField} associated with this header or these headers. */
    private final BeanField field;
    
    /**
     * Whether {@link #name} is a header name or a regular expression pattern
     * that is meant to match header names.
     */
    private final boolean regexPattern;
    
    /**
     * Initializes the entry.
     * 
     * @param name The name or regular expression pattern representing the header(s)
     * @param field The field associated with the header(s)
     * @param regexPattern Whether or not {@code name} is a regular expression pattern
     */
    public FieldMapByNameEntry(String name, BeanField field, boolean regexPattern) {
        this.name = name;
        this.field = field;
        this.regexPattern = regexPattern;
    }

    /**
     * @return The name of the header or a regular expression pattern
     *   matching all possible header names
     */
    public String getName() {
        return name;
    }

    /**
     * @return The {@link BeanField} associated with this header or these headers
     */
    public BeanField getField() {
        return field;
    }

    /**
     * @return Whether the string returned by {@link #getName()} is a header name
     *   or a regular expression pattern to match header names
     */
    public boolean isRegexPattern() {
        return regexPattern;
    }
}
