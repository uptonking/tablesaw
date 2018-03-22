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
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class converts an input to a date type.
 * I would dearly love to use Apache Commons BeanUtils to make this class smaller
 * and easier, but BeanUtils is abysmal with dates of all types.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2 (previously BeanFieldDate since 3.8)
 * @see com.opencsv.bean.CsvDate
 */
public class ConverterDate extends AbstractCsvConverter {

    private final String formatString;

    /**
     * @param type         The type of the field being populated
     * @param formatString The string to use for formatting the date. See
     *                     {@link com.opencsv.bean.CsvDate#value()}
     * @param locale       If not null or empty, specifies the locale used for
     *                     converting locale-specific data types
     * @param errorLocale The locale to use for error messages.
     */
    public ConverterDate(Class<?> type, String locale, Locale errorLocale, String formatString) {
        super(type, locale, errorLocale);
        this.formatString = formatString;
    }
    
    /**
     * @return A {@link java.text.SimpleDateFormat} primed with the proper
     *   format string and a locale, if one has been set.
     */
    private SimpleDateFormat getFormat() {
        SimpleDateFormat sdf;
        if (StringUtils.isNotEmpty(locale)) {
            Locale l = Locale.forLanguageTag(locale);
            sdf = new SimpleDateFormat(formatString, l);
        } else {
            sdf = new SimpleDateFormat(formatString);
        }
        return sdf;
    }
    
    /**
     * Converts the input to/from a date object.
     * <p>This method should work with any type derived from {@link java.util.Date}
     * as long as it has a constructor taking one long that specifies the number
     * of milliseconds since the epoch. The following types are explicitly
     * supported:
     * <ul><li>java.util.Date</li>
     * <li>java.sql.Date</li>
     * <li>java.sql.Time</li>
     * <li>java.sql.Timestamp</li></ul></p>
     *
     * @param <U> The type to be converted to
     * @param value The string to be converted into a date/time type or vice
     *   versa
     * @param fieldType The class of the destination field
     * @return The object resulting from the conversion
     * @throws CsvDataTypeMismatchException If the conversion fails
     */
    private <U> U convertDate(Object value, Class<U> fieldType)
            throws CsvDataTypeMismatchException {
        U o;

        if(value instanceof String) {
            Date d;
            try {
                d = getFormat().parse((String)value);
                o = fieldType.getConstructor(Long.TYPE).newInstance(d.getTime());
            }
            // I would have prefered a CsvBeanIntrospectionException, but that
            // would have broken backward compatibility. This is not completely
            // illogical: I know all of the data types I expect here, and they
            // should all be instantiated with no problems. Ergo, this must be
            // the wrong data type.
            catch(ParseException | InstantiationException
                    | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                CsvDataTypeMismatchException csve = new CsvDataTypeMismatchException(value, fieldType);
                csve.initCause(e);
                throw csve;
            }
        }
        else if(Date.class.isAssignableFrom(value.getClass())) {
            o = fieldType.cast(getFormat().format((Date)value));
        }
        else {
            throw new CsvDataTypeMismatchException(value, fieldType,
                    ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("csvdate.not.date"));
        }
        
        return o;
    }
    
    /**
     * Converts the input to/from a calendar object.
     * <p>This method should work for any type that implements
     * {@link java.util.Calendar} or is derived from
     * {@link javax.xml.datatype.XMLGregorianCalendar}. The following types are
     * explicitly supported:
     * <ul><li>Calendar (always a GregorianCalendar)</li>
     * <li>GregorianCalendar</li>
     * <li>XMLGregorianCalendar</li></ul>
     * It is also known to work with
     * org.apache.xerces.jaxp.datatype.XMLGregorianCalendarImpl.</p>
     *
     * @param <U> The type to be converted to
     * @param value The string to be converted into a date/time type or vice
     *   versa
     * @param fieldType The class of the destination field
     * @return The object resulting from the conversion
     * @throws CsvDataTypeMismatchException If the conversion fails
     */
    private <U> U convertCalendar(Object value, Class<U> fieldType)
            throws CsvDataTypeMismatchException {
        U o;

        if(value instanceof String) {
            // Parse input
            Date d;
            try {
                d = getFormat().parse((String)value);
            } catch (ParseException e) {
                CsvDataTypeMismatchException csve = new CsvDataTypeMismatchException(value, fieldType);
                csve.initCause(e);
                throw csve;
            }

            // Make a GregorianCalendar out of it, because this works for all
            // supported types, at least as an intermediate step.
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(d);

            // XMLGregorianCalendar requires special processing.
            if (fieldType == XMLGregorianCalendar.class) {
                try {
                    o = fieldType.cast(DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(gc));
                } catch (DatatypeConfigurationException e) {
                    // I've never known how to handle this exception elegantly,
                    // especially since I can't conceive of the circumstances
                    // under which it is thrown.
                    CsvDataTypeMismatchException ex = new CsvDataTypeMismatchException(
                            ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("xmlgregoriancalendar.impossible"));
                    ex.initCause(e);
                    throw ex;
                }
            }
            else {
                o = fieldType.cast(gc);
            }
        }
        else {
            Calendar c;
            if(value instanceof XMLGregorianCalendar) {
                c = ((XMLGregorianCalendar)value).toGregorianCalendar();
            }
            else if (value instanceof Calendar) {
                c = (Calendar)value;
            }
            else {
                throw new CsvDataTypeMismatchException(value, fieldType,
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("csvdate.not.date"));
            }
            o = fieldType.cast(getFormat().format(c.getTime()));
        }

        return o;
    }

    /**
     * Splits the conversion into date-based and calendar-based.
     * 
     * @param <U> The type to be converted to
     * @param value The string to be converted into a date/time type or vice
     *   versa
     * @param fieldType The class of the destination field
     * @return The object resulting from the conversion
     * @throws CsvDataTypeMismatchException If a non-convertable type is
     *                                      passed in, or if the conversion fails
     */
    private <U> U convertCommon(Object value, Class<U> fieldType)
            throws CsvDataTypeMismatchException {
        U o;
        Class conversionClass = (fieldType == String.class)?value.getClass():fieldType;
        
        // Send to the proper submethod
        if (Date.class.isAssignableFrom(conversionClass)) {
            o = convertDate(value, fieldType);
        } else if (Calendar.class.isAssignableFrom(conversionClass)
                || XMLGregorianCalendar.class.isAssignableFrom(conversionClass)) {
            o = convertCalendar(value, fieldType);
        } else {
            throw new CsvDataTypeMismatchException(value, fieldType,
                    ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("csvdate.not.date"));
        }
        
        return o;
    }

    @Override
    public Object convertToRead(String value) throws CsvDataTypeMismatchException {
        return StringUtils.isBlank(value) ? null : convertCommon(value, type);
    }

    /**
     * This method converts the encapsulated date type to a string, respecting
     * any locales and conversion patterns that have been set through opencsv
     * annotations.
     * 
     * @param value The object containing a date of one of the supported types
     * @return A string representation of the date. If a
     *   {@link CsvBindByName#locale() locale} or {@link CsvDate#value() conversion
     *   pattern} has been specified through annotations, these are used when
     *   creating the return value.
     * @throws CsvDataTypeMismatchException If an unsupported type as been
     *   improperly annotated
     */
    @Override
    public String convertToWrite(Object value)
            throws CsvDataTypeMismatchException {
        return value == null ? null : convertCommon(value, String.class);
    }
}
