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

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * This class is used for combining multiple columns of the input, possibly
 * with multiple identically named columns, into one field.
 * 
 * @param <T> The type of the bean being populated
 * @author Andrew Rucker Jones
 * @since 4.2
 */
abstract public class BeanFieldJoin<T> extends BeanFieldSingleValue<T> {
    
    /**
     * The type of the {@link org.apache.commons.collections4.MultiValuedMap}
     * that should be instantiated for the bean field being populated.
     */
    private final Class<? extends MultiValuedMap> mapType;
    
    /**
     * Creates a new instance.
     * 
     * @param field The bean field this object represents
     * @param required Whether or not a value is always required for this field
     * @param errorLocale The locale to use for error messages
     * @param converter The converter to be used for performing the data
     *   conversion on reading or writing
     * @param mapType The type of the
     *   {@link org.apache.commons.collections4.MultiValuedMap} that should be
     *   instantiated for the bean field being populated
     */
    public BeanFieldJoin(
            Field field, boolean required, Locale errorLocale,
            CsvConverter converter, Class<? extends MultiValuedMap> mapType) {
        
        // Simple assignments
        super(field, required, errorLocale, converter);
        
        // Check that we really have a collection
        if(!MultiValuedMap.class.isAssignableFrom(field.getType())) {
            throw new CsvBadConverterException(
                    BeanFieldJoin.class,
                    String.format(
                            ResourceBundle.getBundle(
                                    ICSVParser.DEFAULT_BUNDLE_NAME,
                                    errorLocale).getString("invalid.multivaluedmap.type"),
                            field.getType().toString()));
        }
        
        // Determine the MultiValuedMap implementation that should be
        // instantiated for every bean.
        Class<?> fieldType = field.getType();
        if(!fieldType.isInterface()) {
            this.mapType = (Class<MultiValuedMap>)field.getType();
        }
        else if(!mapType.isInterface()) {
            this.mapType = mapType;
        }
        else {
            if(MultiValuedMap.class.equals(fieldType) || ListValuedMap.class.equals(fieldType)) {
                this.mapType = ArrayListValuedHashMap.class;
            }
            else if(SetValuedMap.class.equals(fieldType)) {
                this.mapType = HashSetValuedHashMap.class;
            }
            else {
                this.mapType = null;
                throw new CsvBadConverterException(
                        BeanFieldJoin.class,
                        String.format(
                                ResourceBundle.getBundle(
                                        ICSVParser.DEFAULT_BUNDLE_NAME,
                                        errorLocale).getString("invalid.multivaluedmap.type"),
                                mapType.toString()));
            }
        }
        
        // Now that we know what type we want to assign, run one last check
        // that assignment is truly possible
        if(!field.getType().isAssignableFrom(this.mapType)) {
            throw new CsvBadConverterException(
                    BeanFieldJoin.class,
                    String.format(
                            ResourceBundle.getBundle(
                                    ICSVParser.DEFAULT_BUNDLE_NAME,
                                    errorLocale).getString("unassignable.multivaluedmap.type"),
                            mapType.getName(), field.getType().getName()));
        }
    }
    
    /**
     * Puts the value given in {@code newValue} into {@code map} using
     * {@code index}.
     * This allows derived classes to do something special before assigning the
     * value, such as converting the index to a different type.
     * 
     * @param map The map to which to assign the new value. Never null.
     * @param index The index under which the new value should be placed in the
     *   map. Never null.
     * @param newValue The new value to be put in the map
     * @return The previous value under this index, or null if there was no
     *   previous value
     */
    abstract protected Object putNewValue(MultiValuedMap map, String index, Object newValue);

    /**
     * Assigns the value given to the proper field of the bean given.
     * In the case of this kind of bean field, the new value will be added to
     * an existing map, and a new map will be created if one does not already
     * exist.
     */
    // The rest of the Javadoc is inherited
    @Override
    protected void assignValueToField(T bean, Object obj, String header)
            throws CsvDataTypeMismatchException {

        // Find and use getter and setter methods if available
        // obj == null means that the source field was empty. Then we simply
        // make certain that a(n empty) map exists.
        try {
            Method getterMethod = getReadMethod(bean);
            Method setterMethod = getWriteMethod(bean);
            try {
                MultiValuedMap currentValue = (MultiValuedMap) getterMethod.invoke(bean);
                if(currentValue == null) {
                    currentValue = mapType.newInstance();
                    setterMethod.invoke(bean, currentValue);
                }
                if(obj != null) {
                    putNewValue(currentValue, header, obj);
                }
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                CsvBeanIntrospectionException csve =
                        new CsvBeanIntrospectionException(bean, field,
                                e.getLocalizedMessage());
                csve.initCause(e);
                throw csve;
            } catch(InstantiationException e) {
                CsvBadConverterException csve = new CsvBadConverterException(
                        BeanFieldJoin.class,
                        String.format(
                                ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                        .getString("map.cannot.be.instantiated"),
                                mapType.getName()));
                csve.initCause(e);
                throw csve;
            }
        } catch (NoSuchMethodException | SecurityException e1) {
            // Otherwise set the field directly.
            writeWithoutSetter(bean, obj, header);
        }
    }
    
    /**
     * @return An array of all objects in the
     *   {@link org.apache.commons.collections4.MultiValuedMap} addressed by
     *   this bean field answering to the key given in {@code index}
     */
    // The rest of the Javadoc is inherited
    @Override
    public Object[] indexAndSplitMultivaluedField(Object value, Object index)
            throws CsvDataTypeMismatchException {
        Object[] splitObjects = ArrayUtils.EMPTY_OBJECT_ARRAY;
        if(value != null) {
            if(MultiValuedMap.class.isAssignableFrom(value.getClass())) {
                MultiValuedMap map = (MultiValuedMap) value;
                Collection<Object> splitCollection = map.get(index);
                splitObjects = splitCollection.toArray(new Object[splitCollection.size()]);
            }
            else {
                // Note about code coverage: I sincerely doubt this code is
                // reachable. It is meant as one more safeguard.
                throw new CsvDataTypeMismatchException(value, String.class,
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                .getString("field.not.multivaluedmap"));
            }
        }
        return splitObjects;
    }
    
    /**
     * Checks that {@code value} is not null and not empty.
     */
    // The rest of the Javadoc is inherited
    @Override
    protected boolean isFieldEmptyForWrite(Object value) {
        return super.isFieldEmptyForWrite(value) || ((MultiValuedMap<Object, Object>)value).isEmpty();
    }
    
    /**
     * Adds the given value to the MultiValuedMap contained in {@link #field}.
     * If the map does not yet exist, one is instantiated and assigned to the
     * field represented by {@link #field} in {@code bean}. If one already
     * exists, {@code obj} is added to it under the index {@code header}.
     * 
     * @param bean The bean to be populated
     * @param obj The object to be added to the MultiValuedMap in {@code bean}
     *   in the field {@link #field}
     * @param header The index under which {@code obj} should be added to the
     *   MultiValuedMap in {@code bean} in the field {@link #field}
     * @throws CsvDataTypeMismatchException If the data to be assigned cannot
     *   be assigned
     */
    private void writeWithoutSetter(T bean, Object obj, String header) throws CsvDataTypeMismatchException {
        MultiValuedMap map;
        try {
            Object existingValue = FieldUtils.readField(field, bean, true);
            if(existingValue == null) {
                map = mapType.newInstance();
                putNewValue(map, header, obj);
                super.writeWithoutSetter(bean, map);
            }
            else {
                map = (MultiValuedMap) existingValue;
                putNewValue(map, header, obj);
            }
        } catch (IllegalAccessException e2) {
            // The Apache Commons Lang Javadoc claims this can be thrown
            // if the field is final, but it's not true if we override
            // accessibility. This is never thrown.
        } catch(InstantiationException e) {
            CsvBadConverterException csve = new CsvBadConverterException(
                    BeanFieldJoin.class,
                    String.format(
                            ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                                    .getString("map.cannot.be.instantiated"),
                            mapType.getName()));
            csve.initCause(e);
            throw csve;
        }
    }
}
