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
package com.opencsv.exceptions;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.UnmodifiableList;

/**
 * This exception should be thrown when a field marked as required is empty in
 * the CSV file.
 *
 * @author Andrew Rucker Jones
 * @since 3.8
 */
public class CsvRequiredFieldEmptyException extends CsvException {
    private static final long serialVersionUID = 1L;

    private final Class<?> beanClass;
    private transient final List<Field> destinationFields;

    /**
     * Default constructor, in case no further information is necessary.
     */
    public CsvRequiredFieldEmptyException() {
        beanClass = null;
        destinationFields = Collections.emptyList();
    }

    /**
     * Constructor with a simple text.
     *
     * @param message Human-readable error text
     */
    public CsvRequiredFieldEmptyException(String message) {
        super(message);
        beanClass = null;
        destinationFields = Collections.emptyList();
    }

    /**
     * Constructor for setting the intended class and field of the target bean.
     * <p>These may not be known in every context.</p>
     * <p>This constructor is provided for backward compatibility and for
     * convenience if you only have one missing destination field.</p>
     *
     * @param beanClass        Class of the destination bean
     * @param destinationField Field of the destination field in the destination bean
     */
    public CsvRequiredFieldEmptyException(Class<?> beanClass, Field destinationField) {
        this.beanClass = beanClass;
        this.destinationFields = Collections.singletonList(destinationField);
    }
    
    /**
     * Constructor for setting the intended class and fields of the target bean.
     * These may not be known in every context.
     *
     * @param beanClass        Class of the destination bean
     * @param destinationFields Fields of the destination fields in the destination bean
     * @since 4.2
     */
    public CsvRequiredFieldEmptyException(Class<?> beanClass, List<Field> destinationFields) {
        this.beanClass = beanClass;
        this.destinationFields = new UnmodifiableList<>(destinationFields);
    }
    
    /**
     * Constructor for setting the intended class of the target bean and a
     * human-readable error message.
     * These may not be known in every context.
     *
     * @param beanClass Class of the destination bean
     * @param message Human-readable error text
     * @since 3.10
     */
    public CsvRequiredFieldEmptyException(Class<?> beanClass, String message) {
        super(message);
        this.beanClass = beanClass;
        this.destinationFields = Collections.emptyList();
    }

    /**
     * Constructor for setting the intended class and field of the target bean
     * along with an error message.
     * <p>The class and field may not be known in every context.</p>
     * <p>This constructor is provided for backward compatibility and for
     * convenience if you only have one missing destination field.</p>
     *
     * @param beanClass        Class of the destination bean
     * @param destinationField Field of the destination field in the destination bean
     * @param message          Human-readable error text
     */
    public CsvRequiredFieldEmptyException(Class<?> beanClass, Field destinationField, String message) {
        super(message);
        this.beanClass = beanClass;
        this.destinationFields = Collections.singletonList(destinationField);
    }

    /**
     * Constructor for setting the intended class and fields of the target bean
     * along with an error message.
     * The class and field may not be known in every context.
     *
     * @param beanClass        Class of the destination bean
     * @param destinationFields Field of the destination field in the destination bean
     * @param message          Human-readable error text
     * @since 4.2
     */
    public CsvRequiredFieldEmptyException(Class<?> beanClass, List<Field> destinationFields, String message) {
        super(message);
        this.beanClass = beanClass;
        this.destinationFields = new UnmodifiableList<>(destinationFields);
    }

    /**
     * Gets the class of the bean to which the value was to be assigned.
     *
     * @return The class of the bean to which the destination field belongs
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Gets the field from the Reflection API that was to be assigned.
     * <p>This method is provided as a convenience for when you know there can
     * be only one field, or you really only care about the first field.</p>
     * <p>{@code destinationFields} is marked {@code transient}, because
     * {@link java.lang.reflect.Field} is not {@link java.io.Serializable}. If
     * for any reason this exception is serialized and deserialized, this method
     * will subsequently return {@code null}.</p>
     *
     * @return The first destination field that was to receive the empty value
     */
    public Field getDestinationField() {
        return CollectionUtils.isEmpty(destinationFields)?null:destinationFields.get(0);
    }
    
    /**
     * Returns the complete list of all fields that were to be empty.
     * {@code destinationFields} is marked {@code transient}, because
     * {@link java.lang.reflect.Field} is not {@link java.io.Serializable}. If
     * for any reason this exception is serialized and deserialized, this method
     * will subsequently return {@code null}.
     *
     * @return All destination fields that were to receive the empty value
     * @since 4.2
     */
    public List<Field> getDestinationFields() {
        return destinationFields;
    }
}
