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

import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class collects as many generally useful parts of the implementation
 * of a mapping strategy as possible.
 * Anyone is welcome to use it as a base class for their own mapping strategies.
 * 
 * @param <T> Type of object that is being processed.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
abstract public class AbstractMappingStrategy<T> implements MappingStrategy<T> {
    
    /**
     * Given a header name, this map allows one to find the corresponding
     * property descriptor.
     */
    protected Map<String, PropertyDescriptor> descriptorMap = null;
    
    /** This is the class of the bean to be manipulated. */
    protected Class<? extends T> type;
    
    /**
     * Maintains a bi-directional mapping between column position(s) and header
     * name.
     */
    protected final HeaderIndex headerIndex = new HeaderIndex();
    
    /**
     * Whether or not annotations were found and should be used for determining
     * the binding between columns in a CSV source or destination and fields in
     * a bean.
     */
    protected boolean annotationDriven;
    
    /** Locale for error messages. */
    protected Locale errorLocale = Locale.getDefault();

    /**
     * Maintains a map from classes for bean fields to their associated property
     * editors.
     */
    private final ConcurrentMap<Class<?>, PropertyEditor> editorMap = new ConcurrentHashMap<>();
    
    /**
     * For {@link BeanField#indexAndSplitMultivaluedField(java.lang.Object, java.lang.Object)}
     * it is necessary to determine which index to pass in.
     * 
     * @param index The current column position while transmuting a bean to CSV
     *   output
     * @return The index to be used for this mapping strategy for
     *   {@link BeanField#indexAndSplitMultivaluedField(java.lang.Object, java.lang.Object) }
     */
    abstract protected Object chooseMultivaluedFieldIndexFromHeaderIndex(int index);
    
    /**
     * Returns the {@link FieldMap} associated with this mapping strategy.
     * 
     * @return The {@link FieldMap} used by this strategy
     */
    abstract protected FieldMap getFieldMap();
    
    /**
     * Builds a map of fields for the bean.
     *
     * @throws CsvBadConverterException If there is a problem instantiating the
     *                                  custom converter for an annotated field
     */
    abstract protected void loadFieldMap() throws CsvBadConverterException;
    
    /**
     * Returns the trimmed value of the string only if the property the string
     * is describing should be trimmed to be converted to that type.
     *
     * @param s    String describing the value.
     * @param prop Property descriptor of the value.
     * @return The string passed in if the property is a string, otherwise it
     * will return the string with the beginning and end whitespace removed.
     */
    protected String checkForTrim(String s, PropertyDescriptor prop) {
        return s != null && trimmableProperty(prop) ? s.trim() : s;
    }

    private boolean trimmableProperty(PropertyDescriptor prop) {
        return !prop.getPropertyType().getName().contains("String");
    }

    /**
     * Convert a string value to its Object value.
     *
     * @param value String value
     * @param prop  PropertyDescriptor
     * @return The object set to {@code value} (i.e. Integer).  Will return
     *   a {@link java.lang.String} if no PropertyEditor is found.
     * @throws InstantiationException Thrown on error getting the property
     * editor from the property descriptor.
     * @throws IllegalAccessException Thrown on error getting the property
     * editor from the property descriptor.
     */
    protected Object convertValue(String value, PropertyDescriptor prop) throws InstantiationException, IllegalAccessException {
        PropertyEditor editor = getPropertyEditor(prop);
        Object obj = value;
        if (null != editor) {
            synchronized (editor) {
                editor.setAsText(value);
                obj = editor.getValue();
            }
        }
        return obj;
    }

    /**
     * @throws IllegalStateException If the type of the bean has not been
     *   initialized through {@link #setType(java.lang.Class)}
     */
    // The rest of the Javadoc is inherited
    @Override
    public T createBean() throws InstantiationException, IllegalAccessException, IllegalStateException {
        if(type == null) {
            throw new IllegalStateException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("type.unset"));
        }
        return type.newInstance();
    }

    @Override
    @Deprecated
    public PropertyDescriptor findDescriptor(int col) {
        
        BeanField beanField = findField(col);
        if(beanField != null) {
            return findDescriptor(beanField.getField().getName());
        }
        
        String columnName = getColumnName(col);
        if(StringUtils.isNotBlank(columnName)) {
            return findDescriptor(columnName);
        }
        return null;
    }

    /**
     * Find the property descriptor for a given column.
     *
     * @param name Column name to look up.
     * @return The property descriptor for the column.
     * @deprecated Introspection will be replaced with reflection in version 5.0
     */
    @Deprecated
    protected PropertyDescriptor findDescriptor(String name) {
        return descriptorMap.get(name.toUpperCase().trim());
    }
    
    /**
     * Gets the name (or position number) of the header for the given column
     * number.
     * The column numbers are zero-based.
     * 
     * @param col The column number for which the header is sought
     * @return The name of the header
     */
    abstract public String findHeader(int col);

    @Override
    public int findMaxFieldIndex() {
        return headerIndex.findMaxIndex();
    }

    /**
     * This method generates a header that can be used for writing beans of the
     * type provided back to a file.
     * <p>The ordering of the headers is determined by the
     * {@link com.opencsv.bean.FieldMap} in use.</p>
     * <p>This method should be called first by all overriding classes to make
     * certain {@link #headerIndex} is properly initialized.</p>
     */
    // The rest of the Javadoc is inherited
    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        if(type == null) {
            throw new IllegalStateException(ResourceBundle
                    .getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                    .getString("type.before.header"));
        }
        
        // Always take what's been given or previously determined first.
        if(headerIndex.isEmpty()) {
            String[] header = getFieldMap().generateHeader(bean);
            headerIndex.initializeHeaderIndex(header);
            return header;
        }
        
        // Otherwise, put headers in the right places.
        return headerIndex.getHeaderIndex();
    }

    /**
     * Returns the PropertyEditor for the given class.
     * Should be more efficient if used often, because it caches PropertyEditors.
     *
     * @param cls The class for which the property editor is desired
     * @return The PropertyEditor for the given class
     */
    protected PropertyEditor getPropertyEditorValue(Class<?> cls) {
        PropertyEditor editor = editorMap.get(cls);

        if (editor == null) {
            editor = PropertyEditorManager.findEditor(cls);
            editorMap.put(cls, editor);
        }

        return editor;
    }

    /**
     * Attempt to find custom property editor on descriptor first, else try the
     * propery editor manager.
     *
     * @param desc PropertyDescriptor.
     * @return The PropertyEditor for the given PropertyDescriptor.
     * @throws InstantiationException Thrown when getting the PropertyEditor for the class.
     * @throws IllegalAccessException Thrown when getting the PropertyEditor for the class.
     */
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc)
            throws InstantiationException, IllegalAccessException {
        Class<?> cls = desc.getPropertyEditorClass();
        if (null != cls) {
            return (PropertyEditor) cls.newInstance();
        }
        return getPropertyEditorValue(desc.getPropertyType());
    }
    
    /**
     * Builds a map of property descriptors for the bean.
     *
     * @return Map of property descriptors
     * @throws IntrospectionException Thrown on error getting information
     *                                about the bean.
     * @deprecated Introspection will be replaced with reflection in version 5.0
     */
    @Deprecated
    protected Map<String, PropertyDescriptor> loadDescriptorMap() throws IntrospectionException {
        Map<String, PropertyDescriptor> map = new HashMap<>();

        PropertyDescriptor[] descriptors = loadDescriptors(getType());
        for (PropertyDescriptor descriptor : descriptors) {
            map.put(descriptor.getName().toUpperCase(), descriptor);
        }

        return map;
    }

    private PropertyDescriptor[] loadDescriptors(Class<? extends T> cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        return beanInfo.getPropertyDescriptors();
    }

    /**
     * Get the column name for a given column position.
     *
     * @param col Column position.
     * @return The column name or null if the position is larger than the
     * header array or there are no headers defined.
     */
    public String getColumnName(int col) {
        // headerIndex is never null because it's final
        return headerIndex.getByPosition(col);
    }

    /**
     * Get the class type that the Strategy is mapping.
     *
     * @return Class of the object that this {@link MappingStrategy} will create.
     */
    public Class<? extends T> getType() {
        return type;
    }

    @Override
    public T populateNewBean(String[] line)
            throws InstantiationException, IllegalAccessException,
            IntrospectionException, InvocationTargetException,
            CsvRequiredFieldEmptyException, CsvDataTypeMismatchException,
            CsvConstraintViolationException {
        verifyLineLength(line.length);
        T bean = createBean();
        for (int col = 0; col < line.length; col++) {
            if (isAnnotationDriven()) {
                setFieldValue(bean, line[col], col);
            } else {
                processProperty(bean, line, col);
            }
        }
        return bean;
    }
    
    @Override
    @Deprecated
    public T populateNewBeanWithIntrospection(String[] line)
            throws InstantiationException, IllegalAccessException,
            IntrospectionException, InvocationTargetException,
            CsvRequiredFieldEmptyException {
        verifyLineLength(line.length);
        T bean = createBean();
        for (int col = 0; col < line.length; col++) {
            processProperty(bean, line, col);
        }
        return bean;
    }
    
    private void processProperty(T bean, String[] line, int col)
            throws InstantiationException,
            IllegalAccessException, InvocationTargetException, CsvBadConverterException {
        PropertyDescriptor prop = findDescriptor(col);
        if (null != prop) {
            String value = checkForTrim(line[col], prop);
            Object obj = convertValue(value, prop);
            prop.getWriteMethod().invoke(bean, obj);
        }
    }

    /**
     * Sets the class type that is being mapped.
     * Also initializes the mapping between column names and bean fields.
     */
    // The rest of the Javadoc is inherited.
    @Override
    public void setType(Class<? extends T> type) throws CsvBadConverterException {
        this.type = type;
        loadFieldMap();
        try {
            descriptorMap = loadDescriptorMap();
        }
        catch(IntrospectionException e) {
            // For the record, especially with respect to code coverage, I have
            // tried to trigger this exception, and I can't. I have read the
            // source code for Java 8, and I can find no possible way for
            // IntrospectionException to be thrown by our code.
            // -Andrew Jones 31.07.2017
            CsvBeanIntrospectionException csve = new CsvBeanIntrospectionException(
                    ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("bean.descriptors.uninitialized"));
            csve.initCause(e);
            throw csve;
        }
    }
    
    /**
     * Attempts to instantiate the class of the custom converter specified.
     *
     * @param converter The class for a custom converter
     * @return The custom converter
     * @throws CsvBadConverterException If the class cannot be instantiated
     */
    protected BeanField instantiateCustomConverter(Class<? extends AbstractBeanField> converter)
            throws CsvBadConverterException {
        try {
            BeanField c = converter.newInstance();
            c.setErrorLocale(errorLocale);
            return c;
        } catch (IllegalAccessException | InstantiationException oldEx) {
            CsvBadConverterException newEx =
                    new CsvBadConverterException(converter,
                            String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("custom.converter.invalid"), converter.getCanonicalName()));
            newEx.initCause(oldEx);
            throw newEx;
        }
    }

    @Override
    public boolean isAnnotationDriven() {
        return annotationDriven;
    }
    
    @Override
    public void setErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        
        // It's very possible that setType() was called first, which creates all
        // of the BeanFields, so we need to go back through the list and correct
        // them all.
        if(getFieldMap() != null) {
            getFieldMap().setErrorLocale(this.errorLocale);
            for(BeanField f : (Collection<BeanField>) getFieldMap().values()) {
                f.setErrorLocale(this.errorLocale);
            }
        }
    }
    
    /**
     * Populates the field corresponding to the column position indicated of the
     * bean passed in according to the rules of the mapping strategy.
     * This method performs conversion on the input string and assigns the
     * result to the proper field in the provided bean.
     *
     * @param bean  Object containing the field to be set.
     * @param value String containing the value to set the field to.
     * @param column The column position from the CSV file under which this
     *   value was found.
     * @throws CsvDataTypeMismatchException    When the result of data conversion returns
     *                                         an object that cannot be assigned to the selected field
     * @throws CsvRequiredFieldEmptyException  When a field is mandatory, but there is no
     *                                         input datum in the CSV file
     * @throws CsvConstraintViolationException When the internal structure of
     *                                         data would be violated by the data in the CSV file
     * @since 4.2
     */
    protected void setFieldValue(T bean, String value, int column)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException,
            CsvConstraintViolationException {
        BeanField beanField = findField(column);
        if (beanField != null) {
            beanField.setFieldValue(bean, value, findHeader(column));
        }
    }
    
    @Override
    public String[] transmuteBean(T bean) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        List<String> transmutedBean;
        int numColumns = findMaxFieldIndex()+1;
        if(isAnnotationDriven()) {
            transmutedBean = writeWithReflection(bean, numColumns);
        }
        else {
            transmutedBean = writeWithIntrospection(bean, numColumns);
        }
        return transmutedBean.toArray(new String[transmutedBean.size()]);
    }

    private List<String> writeWithReflection(T bean, int numColumns)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        BeanField firstBeanField, subsequentBeanField;
        Object firstIndex, subsequentIndex;
        List<String> contents = new ArrayList<>(numColumns > 0 ? numColumns : 0);
        
        for(int i = 0; i < numColumns;) {
            
            // Determine the first value
            firstBeanField = findField(i);
            firstIndex = chooseMultivaluedFieldIndexFromHeaderIndex(i);
            String[] fields = firstBeanField != null
                    ? firstBeanField.write(bean, firstIndex)
                    : ArrayUtils.EMPTY_STRING_ARRAY;
            
            if(fields.length == 0) {
                
                // Write the only value
                contents.add(StringUtils.EMPTY);
                i++; // Advance the index
            }
            else {
                
                // Multiple values. Write the first.
                contents.add(StringUtils.defaultString(fields[0]));
                
                // Now write the rest
                // We must make certain that we don't write more fields
                // than we have columns of the correct type to cover them
                int j = 1;
                int displacedIndex = i+j;
                subsequentBeanField = findField(displacedIndex);
                subsequentIndex = chooseMultivaluedFieldIndexFromHeaderIndex(displacedIndex);
                while(j < fields.length
                        && displacedIndex < numColumns
                        && Objects.equals(firstBeanField, subsequentBeanField)
                        && Objects.equals(firstIndex, subsequentIndex)) {
                    // This field still has a header, so add it
                    contents.add(StringUtils.defaultString(fields[j]));
                    
                    // Prepare for the next loop through
                    displacedIndex = i + (++j);
                    subsequentBeanField = findField(displacedIndex);
                    subsequentIndex = chooseMultivaluedFieldIndexFromHeaderIndex(displacedIndex);
                }
                
                i = displacedIndex; // Advance the index
                
                // And here's where we fill in any fields that are missing to
                // cover the number of columns of the same type
                if(i < numColumns) {
                    subsequentBeanField = findField(i);
                    subsequentIndex = chooseMultivaluedFieldIndexFromHeaderIndex(i);
                    while(Objects.equals(firstBeanField, subsequentBeanField)
                            && Objects.equals(firstIndex, subsequentIndex)
                            && i < numColumns) {
                        contents.add(StringUtils.EMPTY);
                        subsequentBeanField = findField(++i);
                        subsequentIndex = chooseMultivaluedFieldIndexFromHeaderIndex(i);
                    }
                }
            }
        }
        return contents;
    }
    
    private List<String> writeWithIntrospection(T bean, int numColumns) {
        PropertyDescriptor desc;
        List<String> contents = new ArrayList<>(numColumns > 0 ? numColumns : 0);
        for(int i = 0; i < numColumns; i++) {
            try {
                desc = findDescriptor(i);
                Object o = desc != null ? desc.getReadMethod().invoke(bean, (Object[]) null) : null;
                contents.add(Objects.toString(o, ""));
            }
            catch(IllegalAccessException | InvocationTargetException e) {
                CsvBeanIntrospectionException csve = new CsvBeanIntrospectionException(
                        bean, null, ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("error.introspecting.beans"));
                csve.initCause(e);
                throw csve;
            }
        }
        return contents;
    }

    @Override
    @Deprecated
    public Integer getColumnIndex(String name) {
        int[] i = headerIndex.getByName(name);
        return i.length == 0 ? null : i[0];
    }
}
