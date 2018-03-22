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
import org.apache.commons.lang3.ObjectUtils;

/**
 * Collects common aspects of a {@link ComplexFieldMapEntry}.
 * 
 * @param <I> The initializer type used to build the many-to-one mapping
 * @param <K> The type of the key used for indexing
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
abstract public class AbstractFieldMapEntry<I, K> implements ComplexFieldMapEntry<I, K> {
    
    /** The {@link BeanField} that is the target of this mapping. */
    protected final BeanField field;
    
    /** The locale to be used for error messages. */
    protected Locale errorLocale;
    
    /**
     * The only constructor, and it must be called by all derived classes.
     * 
     * @param field The BeanField being mapped to
     * @param errorLocale The locale to be used for error messages
     */
    protected AbstractFieldMapEntry(final BeanField field, final Locale errorLocale) {
        this.field = field;
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
    }
    
    @Override
    public BeanField getBeanField() {
        return field;
    }
    
    @Override
    public void setErrorLocale(final Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
    }
}
