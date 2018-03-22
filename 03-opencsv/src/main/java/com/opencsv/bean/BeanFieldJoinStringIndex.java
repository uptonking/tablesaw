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

import java.lang.reflect.Field;
import java.util.Locale;
import org.apache.commons.collections4.MultiValuedMap;

/**
 * Implements a {@link BeanFieldJoin} with a {@link java.lang.String} for an
 * index.
 * 
 * @param <T> The type of the bean being populated
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class BeanFieldJoinStringIndex<T> extends BeanFieldJoin<T> {
    
    public BeanFieldJoinStringIndex(
            Field field, boolean required, Locale errorLocale,
            CsvConverter converter, Class<? extends MultiValuedMap> mapType) {
        super(field, required, errorLocale, converter, mapType);
    }

    @Override
    protected Object putNewValue(MultiValuedMap map, String index, Object newValue) {
        return map.put(index, newValue);
    }
}
