package com.opencsv.bean;

import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Used to extend the {@link java.lang.reflect.Field} class to include
 * functionality that opencsv requires.
 * This includes a required flag and a
 * {@link #write(java.lang.Object, java.lang.Object) } method for writing beans
 * back out to a CSV file. The required flag determines if the field has to be
 * non-empty.
 *
 * @param <T> Type of the bean being populated
 */
// TODO for 5.0: The correct way to do this is to add another type
// parameter for the type of the index into multivalued fields, I. This would
// then be used for write() and indexAndSplitMultivaluedField(), and may even
// obviate the need for BeanFieldJoinIntegerIndex and BeanFieldJoinStringIndex,
// though I'm not certain about that. I cannot implement this change before
// version 5.0 because it would break every custom converter in existance,
// assuming the author used the type parameter instead of a raw class.
public interface BeanField<T> {

    /**
     * Sets the field to be processed.
     *
     * @param field Which field is being populated
     */
    void setField(Field field);

    /**
     * Gets the field to be processed.
     *
     * @return A field object
     * @see java.lang.reflect.Field
     */
    Field getField();
    
    /**
     * Answers the query, whether this field is required or not.
     * 
     * @return True if the field is required to be set (cannot be null or an
     * empty string), false otherwise
     * @since 3.10
     */
    boolean isRequired();
    
    /**
     * Determines whether or not a field is required.
     * Implementation note: This method is necessary for custom converters. If
     * we did not have it, every custom converter would be required to implement
     * a constructor with this one boolean parameter, and the instantiation code
     * for the custom converter would look much uglier.
     * 
     * @param required Whether or not the field is required
     * @since 3.10
     */
    void setRequired(boolean required);

    /**
     * Populates the selected field of the bean.
     * This method performs conversion on the input string and assigns the
     * result to the proper field in the provided bean.
     *
     * @param bean  Object containing the field to be set.
     * @param value String containing the value to set the field to.
     * @param header The header from the CSV file under which this value was found.
     * @throws CsvDataTypeMismatchException    When the result of data conversion returns
     *                                         an object that cannot be assigned to the selected field
     * @throws CsvRequiredFieldEmptyException  When a field is mandatory, but there is no
     *                                         input datum in the CSV file
     * @throws CsvConstraintViolationException When the internal structure of
     *                                         data would be violated by the data in the CSV file
     */
    void setFieldValue(T bean, String value, String header)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException,
            CsvConstraintViolationException;
    
    /**
     * Gets the contents of the selected field of the given bean.
     * This method performs no conversions of any kind, but simply gets the
     * value of the desired field using introspection if an accessor method is
     * available and reflection if one is not.
     * 
     * @param bean Object containing the field to be read
     * @return The value of the field in the given bean
     * @since 4.2
     */
    Object getFieldValue(T bean);
    
    /**
     * Given the value of a bean field and an index into that value, determine
     * what values need to be written.
     * When writing a bean to a CSV file, some single fields from the bean could
     * have values that need to be split into multiple fields when writing them
     * to the CSV file. Given the value of the bean field and an index into the
     * data, this method returns the objects to be converted and written.
     * 
     * @param value The value of the bean field that should be written
     * @param index An index into {@code value} that determines which of the
     *   many possible values are currently being written. For header-based
     *   mapping strategies, this will be the header name, and for column
     *   position mapping strategies, it will be the zero-based column position.
     * @return An array of {@link Object}s that should be converted for the
     *   output and written
     * @throws CsvDataTypeMismatchException If {@code value} is not of the type
     *   expected by the implementing class
     * @since 4.2
     */
    Object[] indexAndSplitMultivaluedField(Object value, Object index)
            throws CsvDataTypeMismatchException;
    
    /**
     * This method takes the current value of the field in question in the bean
     * passed in and converts it to one or more strings.
     * This method is used to write beans back out to a CSV file, and should
     * ideally provide an accurate representation of the field such that it is
     * roundtrip equivalent. That is to say, this method should write data out
     * just as it would expect to read the data in.
     * 
     * @param bean The bean holding the field to be written
     * @param index The header name or column number of the field currently
     *   being processed. This can be used to find a certain position in a
     *   multivalued field when not all of the values should be written.
     * @return An array of string representations for the values of this field
     *   out of the bean passed in. Typically, there will be only one value, but
     *   {@link com.opencsv.bean.BeanFieldJoin} may return multiple values.
     *   If either the bean or the field are {@code null}, this method returns
     *   an empty array to allow the writer to treat {@code null} specially. It
     *   is also possible that individual values in the array are {@code null}.
     *   The writer may wish to write "(null)" or "\0" or "NULL" or some other
     *   key instead of a blank string.
     * 
     * @throws CsvDataTypeMismatchException If expected to convert an
     *   unsupported data type
     * @throws CsvRequiredFieldEmptyException If the field is marked as required,
     *   but is currently empty
     * @since 3.9
     */
    String[] write(T bean, Object index) throws CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException;
    
    /**
     * Sets the locale for all error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     * @since 4.0
     */
    void setErrorLocale(Locale errorLocale);
}
