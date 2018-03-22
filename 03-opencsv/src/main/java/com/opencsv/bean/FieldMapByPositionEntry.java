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

/**
 * Represents one entry in {@link FieldMapByPosition}.
 * Note: This is not used in the internal structure of
 * {@link FieldMapByPosition}, but rather when representing its contents to the
 * outside world.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
public class FieldMapByPositionEntry {
    
    /** The position of the related field in the CSV input. */
    private final int position;
    
    /** The {@link BeanField} associated with this position. */
    private final BeanField field;
    
    /**
     * Initializes this entry.
     * 
     * @param position The position from the CSV input
     * @param field The field associated with this position
     */
    public FieldMapByPositionEntry(int position, BeanField field) {
        this.position = position;
        this.field = field;
    }
    
    /** @return The column position with which this entry was initialized */
    public int getPosition() {return position;}
    
    /** @return The {@link BeanField} with which this entry was initialized */
    public BeanField getField() {return field;}
}
