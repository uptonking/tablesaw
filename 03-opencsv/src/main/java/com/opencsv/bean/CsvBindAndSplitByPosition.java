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

import java.lang.annotation.*;
import java.util.Collection;

/**
 * This annotation interprets one field of the input as a collection that will
 * be split up into its components and assigned to a collection-based bean field.
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvBindAndSplitByPosition {

    /**
     * Whether or not the annotated field is required to be present in every
     * data set of the input.
     * This means that the input cannot be empty. The output after conversion is
     * not guaranteed to be non-empty. "Input" means the string from the field
     * in the CSV file on reading and the bean member variable on writing.
     *
     * @return If the field is required to contain information.
     */
    boolean required() default false;

    /**
     * The column position in the input that is used to fill the annotated
     * field.
     *
     * @return The position of the column in the CSV file from which this field
     * should be taken. This column number is zero-based.
     */
    int position();

    /**
     * Defines the locale to be used for decoding the argument.
     * <p>If not specified, the current default locale is used. The locale must be
     * one recognized by {@link java.util.Locale}. Locale conversion is supported
     * for the following data types:<ul>
     * <li>byte and {@link java.lang.Byte}</li>
     * <li>float and {@link java.lang.Float}</li>
     * <li>double and {@link java.lang.Double}</li>
     * <li>int and {@link java.lang.Integer}</li>
     * <li>long and {@link java.lang.Long}</li>
     * <li>short and {@link java.lang.Short}</li>
     * <li>{@link java.math.BigDecimal}</li>
     * <li>{@link java.math.BigInteger}</li>
     * <li>All time data types supported by {@link com.opencsv.bean.CsvDate}</li></ul>
     * <p>The locale must be in a format accepted by
     * {@link java.util.Locale#forLanguageTag(java.lang.String)}</p>
     * <p>Caution must be exercized with the default locale, for the default
     * locale for numerical types does not mean the locale of the running
     * program, such as en-US or de-DE, but rather <em>no</em> locale. Numbers
     * will be parsed more or less the way the Java compiler would parse them.
     * That means, for instance, that thousands separators in long numbers are
     * not permitted, even if the locale of the running program would accept
     * them. When dealing with locale-sensitive data, it is always best to
     * specify the locale explicitly.</p>
     *
     * @return The locale selected. The default is indicated by an empty string.
     */
    String locale() default "";
    
    /**
     * Defines a regular expression for splitting the input.
     * The input string is split using the value of this attribute and the
     * result is put into a collection. The default splits on whitespace.
     * 
     * @return The regular expression used for splitting the input
     */
    String splitOn() default "\\s+";
    
    /**
     * When writing a collection from a bean, this string will be used to
     * separate elements of the collection.
     * Defaults to one space.
     * 
     * @return Delimiter between elements for writing a collection
     */
    String writeDelimiter() default " ";
    
    /**
     * Defines the class used for the collection.
     * <p>This must be a specific implementation of a collection, and not an
     * interface! The default is set to {@code Collection.class} as a signal to
     * use the default for the interface supplied in the bean to be populated.</p>
     * <p>The logic for determining which class to instantiate for the
     * collection is as follows. In all cases, the implementation must have a
     * nullary constructor.</p>
     * <ol><li>If the bean declares a specific implementation instead of the
     * associated interface (e.g. {@link java.util.ArrayList} vs.
     * {@link java.util.List}), that specific implementation will always be
     * used.</li>
     * <li>Otherwise, the implementation named in this field will be used, if it
     * is not an interface.</li>
     * <li>If no implementation is specified in this field (i.e. if
     * an interface is specified, as is the default), a default is used
     * based on the interface of the bean field annotated. These are:
     * <ul><li>{@link java.util.ArrayList} for {@link java.util.Collection}</li>
     * <li>{@link java.util.ArrayList} for {@link java.util.List}</li>
     * <li>{@link java.util.HashSet} for {@link java.util.Set}</li>
     * <li>{@link java.util.TreeSet} for {@link java.util.SortedSet}</li>
     * <li>{@link java.util.TreeSet} for {@link java.util.NavigableSet}</li>
     * <li>{@link java.util.ArrayDeque} for {@link java.util.Queue}</li>
     * <li>{@link java.util.ArrayDeque} for {@link java.util.Deque}</li>
     * <li>{@link org.apache.commons.collections4.bag.HashBag} for {@link org.apache.commons.collections4.Bag}</li>
     * <li>{@link org.apache.commons.collections4.bag.TreeBag} for {@link org.apache.commons.collections4.SortedBag}</ul></li></ol>
     * 
     * @return A class implementing {@link java.util.Collection}
     */
    Class<? extends Collection> collectionType() default Collection.class;
    
    /**
     * Defines what type the elements of the collection should have.
     * It is necessary to instantiate elements of the collection, and it is not
     * always possible to determine the type of the given collection at runtime.
     * A perfect example of this is {@code List<? extends Number>}.
     * 
     * @return The type of the collection elements
     */
    Class<?> elementType();
}
