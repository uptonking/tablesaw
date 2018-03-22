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

import com.opencsv.ICSVParser;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This exception is to be thrown when anything goes bad during introspection of
 * beans given to opencsv.
 * It encapsulates exceptions such as {@link java.lang.NoSuchMethodException},
 * {@link java.lang.IllegalAccessException} and
 * {@link java.lang.reflect.InvocationTargetException}. Some might notice that
 * this effectively converts checked exceptions into unchecked exceptions.
 * Introspection exceptions are coding errors that should be fixed during
 * development, and should not have to be handled in production code.
 * 
 * @author Andrew Rucker Jones
 * @since 3.9
 */
public class CsvBeanIntrospectionException extends CsvRuntimeException {
    private static final long serialVersionUID = 1L;
    
    /** The bean that was acted upon. */
    private transient final Object bean;
    
    /** The field that was supposed to be manipulated in the bean. */
    private transient final Field field;
    
    /**
     * Nullary constructor.
     */
    public CsvBeanIntrospectionException() {
        super();
        bean = null;
        field = null;
    }
    
    /**
     * Constructor with a human-readable error message.
     * @param message Error message
     */
    public CsvBeanIntrospectionException(String message) {
        super(message);
        bean = null;
        field = null;
    }
    
    /**
     * Constructor to specify the bean and field whose manipulation caused this
     * exception.
     * @param bean The bean that was to be manipulated
     * @param field The field in the bean
     */
    public CsvBeanIntrospectionException(Object bean, Field field) {
        super();
        this.bean = bean;
        this.field = field;
    }
    
    /**
     * Constructor to provide all information connected to the error raised.
     * @param bean The bean that was to be manipulated
     * @param field The field in the bean
     * @param message Error message
     */
    public CsvBeanIntrospectionException(Object bean, Field field, String message) {
        super(message);
        this.bean = bean;
        this.field = field;
    }
    
    /**
     * Gets a human-readable error message.
     * @return The error message, or if none is found, but {@link #bean} and
     * {@link #field} have been set, returns a default error message
     * incorporating the names of {@link #bean} and {@link #field}
     */
    @Override
    public String getMessage() {
        return getMessageFromLocale(Locale.US);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getMessageFromLocale(Locale.getDefault());
    }
    
    private String getMessageFromLocale(Locale locale) {
        String supermessage = super.getMessage();
        if(supermessage == null && getBean() != null && getField() != null) {
            return String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, locale).getString("error.introspecting.field"),
                    getField().getName(),
                    getBean().getClass().getCanonicalName());
        }
        return supermessage;
    }

    /**
     * Gets the bean that was to be introspected.
     * {@code bean} is marked {@code transient}, because
     * {@link java.lang.Object} is not {@link java.io.Serializable}. If
     * for any reason this exception is serialized and deserialized, this method
     * will subsequently return {@code null}.
     * 
     * @return The bean that caused this exception
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Gets the field from the Reflection API that was involved in the error.
     * {@code field} is marked {@code transient}, because
     * {@link java.lang.reflect.Field} is not {@link java.io.Serializable}. If
     * for any reason this exception is serialized and deserialized, this method
     * will subsequently return {@code null}.
     * 
     * @return The field in the bean that caused this exception
     */
    public Field getField() {
        return field;
    }
}
