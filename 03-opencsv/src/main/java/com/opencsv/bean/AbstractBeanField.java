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
package com.opencsv.bean;

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This base bean takes over the responsibility of converting the supplied
 * string to the proper type for the destination field and setting the
 * destination field.
 * <p>All custom converters must be descended from this class.</p>
 * <p>Internally, opencsv uses another set of classes for the actual conversion,
 * leaving this class mostly to deal with assigment to bean fields.</p>
 *
 * @param <T> Type of the bean being populated
 * @author Andrew Rucker Jones
 * @since 3.8
 */
abstract public class AbstractBeanField<T> implements BeanField<T> {
    
    /** The field this class represents. */
    protected Field field;
    
    /**
     * This is just to avoid instantiating a new PropertyUtilsBean for every
     * time it needs to be used.
     */
    private PropertyUtilsBean propUtils;
    
    /** Whether or not this field is required. */
    protected boolean required;
    
    /** Locale for error messages. */
    protected Locale errorLocale;
    
    /**
     * A class that converts from a string to the destination type on reading
     * and vice versa on writing.
     * This is only used for opencsv-internal conversions, not by custom
     * converters.
     */
    protected CsvConverter converter;
    
    /**
     * Default nullary constructor, so derived classes aren't forced to create
     * a constructor identical to this one.
     */
    public AbstractBeanField() {
        required = false;
        errorLocale = Locale.getDefault();
    }

    /**
     * Constructor for an optional field.
     * @param field A {@link java.lang.reflect.Field} object.
     * @deprecated Please always use the constructor with all arguments
     */
    @Deprecated
    public AbstractBeanField(Field field) {
        this(field, false, Locale.getDefault(), null);
    }

    /**
     * @param field A {@link java.lang.reflect.Field} object.
     * @param required Whether or not this field is required in input
     * @since 3.10
     * @deprecated Please always use the constructor with all arguments
     */
    @Deprecated
    public AbstractBeanField(Field field, boolean required) {
        this(field, required, Locale.getDefault(), null);
    }

    /**
     * @param field A {@link java.lang.reflect.Field} object.
     * @param required Whether or not this field is required in input
     * @param errorLocale The errorLocale to use for error messages.
     * @since 4.0
     * @deprecated Please always use the constructor with all arguments
     */
    @Deprecated
    public AbstractBeanField(Field field, boolean required, Locale errorLocale) {
        this(field, required, errorLocale, null);
    }

    /**
     * @param field A {@link java.lang.reflect.Field} object.
     * @param required Whether or not this field is required in input
     * @param errorLocale The errorLocale to use for error messages.
     * @param converter The converter to be used to perform the actual data
     *   conversion
     * @since 4.2
     */
    public AbstractBeanField(Field field, boolean required, Locale errorLocale, CsvConverter converter) {
        this.field = field;
        this.required = required;
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        this.converter = converter;
    }

    @Override
    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public Field getField() {
        return this.field;
    }
    
    @Override
    public boolean isRequired() {
        return required;
    }
    
    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    @Override
    public void setErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        if(converter != null) {
            converter.setErrorLocale(this.errorLocale);
        }
    }
    
    @Override
    public final void setFieldValue(T bean, String value, String header)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException,
            CsvConstraintViolationException {
        if (required && StringUtils.isBlank(value)) {
            throw new CsvRequiredFieldEmptyException(
                    bean.getClass(), field,
                    String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("required.field.empty"),
                            field.getName()));
        }
        
        assignValueToField(bean, convert(value), header);
    }
    
    @Override
    public Object getFieldValue(T bean) {
        Object o = null;
        // Find and use a getter method if one is available.
        try {
            Method getterMethod = getReadMethod(bean);
            try {
                o = getterMethod.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // Can't happen, because we've already established that the
                // method is public through the use of getMethod().
            }
        } catch (NoSuchMethodException | SecurityException e1) {
            // Otherwise set the field directly.
            o = readWithoutGetter(bean);
        }
        
        return o;
    }
    
    /**
     * Gets the method for accessing this field in the given bean (type).
     * This does <em>not</em> account for the special case of "is" for boolean
     * fields. It also does not follow the Java Bean specification for getting
     * the accessor method. It just follows the convention of "get" + "field
     * name with initial capital".
     * 
     * @param bean A representative bean of the type this field belongs to
     * @return The accessor method
     * @throws NoSuchMethodException If the accessor method does not exist
     * @since 4.2
     */
    protected Method getReadMethod(T bean) throws NoSuchMethodException {
        String getterName = "get" + Character.toUpperCase(field.getName().charAt(0))
                + field.getName().substring(1);
        return bean.getClass().getMethod(getterName);
    }
    
    /**
     * Gets the method for assigning this field in the given bean (type).
     * This does <em>not</em> follow the Java Bean specification for getting
     * the assignment method. It just follows the convention of "set" + "field
     * name with initial capital".
     * 
     * @param bean A representative bean of the type this field belongs to
     * @return The assignment method
     * @throws NoSuchMethodException If the assignment method does not exist
     * @since 4.2
     */
    protected Method getWriteMethod(T bean) throws NoSuchMethodException {
        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0))
                + field.getName().substring(1);
        return bean.getClass().getMethod(setterName, field.getType());
    }
    
    /**
     * @return {@code value} wrapped in an array, since we assume most values
     *   will not be multi-valued
     * @since 4.2
     */
    // The rest of the Javadoc is inherited
    @Override
    public Object[] indexAndSplitMultivaluedField(Object value, Object index)
            throws CsvDataTypeMismatchException {
        return new Object[]{value};
    }
    
    /**
     * Whether or not this implementation of {@link BeanField} considers the
     * value passed in as empty for the purposes of determining whether or not
     * a required field is empty.
     * <p>This allows any overriding class to define "empty" while writing
     * values to a CSV file in a way that is meaningful for its own data. A
     * simple example is a {@link java.util.Collection} that is not null, but
     * empty.</p>
     * <p>The default implementation simply checks for {@code null}.</p>
     * 
     * @param value The value of a field out of a bean that is being written to
     *   a CSV file. Can be {@code null}.
     * @return Whether or not this implementation considers {@code value} to be
     *   empty for the purposes of its conversion
     * @since 4.2
     */
    protected boolean isFieldEmptyForWrite(Object value) {
        return value == null;
    }

    /**
     * Assigns the given object to this field of the destination bean.
     * <p>Uses the setter method if available.</p>
     * <p>Derived classes can override this method if they have special needs
     * for setting the value of a field, such as adding to an existing
     * collection.</p>
     *
     * @param bean The bean in which the field is located
     * @param obj  The data to be assigned to this field of the destination bean
     * @param header The header from the CSV file under which this value was found.
     * @throws CsvDataTypeMismatchException If the data to be assigned cannot
     *                                      be converted to the type of the destination field
     */
    protected void assignValueToField(T bean, Object obj, String header)
            throws CsvDataTypeMismatchException {

        // obj == null means that the source field was empty. Then we simply
        // leave the field as it was initialized by the VM. For primitives,
        // that will be values like 0, and for objects it will be null.
        if (obj != null) {
            // Find and use a setter method if one is available.
            try {
                Method setterMethod = getWriteMethod(bean);
                try {
                    setterMethod.invoke(bean, obj);
                } catch (IllegalAccessException e) {
                    // Can't happen, because we've already established that the
                    // method is public through the use of getMethod().
                } catch (InvocationTargetException e) {
                    CsvBeanIntrospectionException csve =
                            new CsvBeanIntrospectionException(bean, field,
                                    e.getLocalizedMessage());
                    csve.initCause(e);
                    throw csve;
                }
            } catch (NoSuchMethodException | SecurityException e1) {
                // Otherwise set the field directly.
                writeWithoutSetter(bean, obj);
            }
        }
    }

    /**
     * Sets a field in a bean if there is no setter available.
     * Turns off all accessibility checking to accomplish the goal, and handles
     * errors as best it can.
     * 
     * @param bean The bean in which the field is located
     * @param obj  The data to be assigned to this field of the destination bean
     * @throws CsvDataTypeMismatchException If the data to be assigned cannot
     *                                      be assigned
     * @since 4.2 (non-private)
     */
    protected void writeWithoutSetter(T bean, Object obj) throws CsvDataTypeMismatchException {
        try {
            FieldUtils.writeField(field, bean, obj, true);
        } catch (IllegalAccessException e2) {
            // The Apache Commons Lang Javadoc claims this can be thrown
            // if the field is final, but it's not true if we override
            // accessibility. This is never thrown.
        } catch (IllegalArgumentException e2) {
            CsvDataTypeMismatchException csve =
                    new CsvDataTypeMismatchException(obj, field.getType());
            csve.initCause(e2);
            throw csve;
        }
    }
    
    /**
     * Gets a field in a bean if there is no getter available.
     * Turns off all accessibility checking to accomplish the goal, and handles
     * errors as best it can.
     * 
     * @param bean The bean in which the field is located
     * @return The contents of the field being accessed in the bean provided
     * @since 4.2
     */
    protected Object readWithoutGetter(T bean) {
        Object o = null;
        
        try {
            o = FieldUtils.readField(field, bean, true);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // Neither exception can ever be thrown.
        }
        
        return o;
    }

    /**
     * Method for converting from a string to the proper datatype of the
     * destination field.
     * This method must be specified in all non-abstract derived classes.
     *
     * @param value The string from the selected field of the CSV file. If the
     *   field is marked as required in the annotation, this value is guaranteed
     *   not to be null, empty or blank according to
     *   {@link org.apache.commons.lang3.StringUtils#isBlank(java.lang.CharSequence)}
     * @return An {@link java.lang.Object} representing the input data converted
     *   into the proper type
     * @throws CsvDataTypeMismatchException    If the input string cannot be converted into
     *                                         the proper type
     * @throws CsvConstraintViolationException When the internal structure of
     *                                         data would be violated by the data in the CSV file
     */
    protected abstract Object convert(String value)
            throws CsvDataTypeMismatchException, CsvConstraintViolationException;
    
    /**
     * This method takes the current value of the field in question in the bean
     * passed in and converts it to a string.
     * It is actually a stub that calls {@link #convertToWrite(java.lang.Object)}
     * for the actual conversion, and itself performs validation and handles
     * exceptions thrown by {@link #convertToWrite(java.lang.Object)}. The
     * validation consists of verifying that both {@code bean} and {@link #field}
     * are not null before calling {@link #convertToWrite(java.lang.Object)}.
     */
    // The rest of the Javadoc is automatically inherited
    @Override
    public final String[] write(T bean, Object index) throws CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        String[] result = ArrayUtils.EMPTY_STRING_ARRAY;
        if(bean != null && field != null) {
            if(propUtils == null) {
                propUtils = new PropertyUtilsBean();
            }
            Object value;
            try {
                // TODO for 5.0: This should probably become getFieldValue()
                value = propUtils.getSimpleProperty(bean, field.getName());
            }
            catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                CsvBeanIntrospectionException csve = new CsvBeanIntrospectionException(bean, field);
                csve.initCause(e);
                throw csve;
            }
            
            if(isFieldEmptyForWrite(value) && required) {
                throw new CsvRequiredFieldEmptyException(
                        bean.getClass(), field,
                        String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("required.field.empty"),
                                field.getName()));
            }
            
            Object[] multivalues = indexAndSplitMultivaluedField(value, index);
            String[] intermediateResult = new String[multivalues.length];
            try {
                for(int i = 0; i < multivalues.length; i++) {
                    intermediateResult[i] = convertToWrite(multivalues[i]);
                }
                result = intermediateResult;
            }
            catch(CsvDataTypeMismatchException e) {
                CsvDataTypeMismatchException csve = new CsvDataTypeMismatchException(
                        bean, field.getType(), e.getMessage());
                csve.initCause(e.getCause());
                throw csve;
            }
            catch(CsvRequiredFieldEmptyException e) {
                // Our code no longer throws this exception from here, but
                // rather from write() using isFieldEmptyForWrite() to determine
                // when to throw the exception. But user code is still allowed
                // to override convertToWrite() and throw this exception
                CsvRequiredFieldEmptyException csve = new CsvRequiredFieldEmptyException(
                        bean.getClass(), field, e.getMessage());
                csve.initCause(e.getCause());
                throw csve;
            }
        }
        return result;
    }
    
    /**
     * This is the method that actually performs the conversion from field to
     * string for {@link #write(java.lang.Object, java.lang.Object) } and should
     * be overridden in derived classes.
     * <p>The default implementation simply calls {@code toString()} on the
     * object in question. Derived classes will, in most cases, want to override
     * this method. Alternatively, for complex types, overriding the
     * {@code toString()} method in the type of the field in question would also
     * work fine.</p>
     * 
     * @param value The contents of the field currently being processed from the
     *   bean to be written. Can be null if the field is not marked as required.
     * @return A string representation of the value of the field in question in
     *   the bean passed in, or an empty string if {@code value} is null
     * @throws CsvDataTypeMismatchException This implementation does not throw
     *   this exception
     * @throws CsvRequiredFieldEmptyException If the input is empty but the
     *   field is required. The case of the field being null is checked before
     *   this method is called, but other implementations may have other cases
     *   that are semantically equivalent to being empty, such as an empty
     *   collection. The preferred way to perform this check is in
     *   {@link #isFieldEmptyForWrite(java.lang.Object) }. This exception may
     *   be removed from this method signature sometime in the future.
     * @since 3.9
     * @see #write(java.lang.Object, java.lang.Object) 
     */
    protected String convertToWrite(Object value)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        // Since we have no concept of which field is required at this level,
        // we can't check for null and throw an exception.
        return Objects.toString(value, StringUtils.EMPTY);
    }
}
