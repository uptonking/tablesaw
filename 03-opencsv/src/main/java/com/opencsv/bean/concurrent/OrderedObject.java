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
package com.opencsv.bean.concurrent;

/**
 * A simple class for ordering objects.
 * Isn't there already something for this purpose in the JDK or Apache Lang? I
 * couldn't find anything.
 * @param <E> Type of the element to be ordered
 * @author Andrew Rucker Jones
 * @since 4.0
 */
public class OrderedObject<E> {
    private final long ordinal;
    private final E element;
    
    /**
     * Creates an object with an order.
     * @param ordinal The position in a sequence of objects
     * @param element The object being sequenced
     */
    public OrderedObject(long ordinal, E element) {
        this.ordinal = ordinal;
        this.element = element;
    }
    
    /**
     * @return The position in a sequence of objects
     */
    public long getOrdinal() {
        return ordinal;
    }
    
    /**
     * @return The object being sequenced
     */
    public E getElement() {
        return element;
    }
}
