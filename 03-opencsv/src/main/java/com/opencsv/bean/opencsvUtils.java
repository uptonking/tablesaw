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

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * This class is meant to be a collection of general purpose static methods
 * useful in processing mapping strategies.
 * 
 * @author Andrew Rucker Jones
 * @since 3.9
 */
public final class opencsvUtils {
    
    /** This class can't be instantiated. */
    private opencsvUtils() {}
    
    /**
     * Determines which mapping strategy is appropriate for this bean.
     * The algorithm is:<ol>
     * <li>If annotations {@link CsvBindByPosition},
     * {@link CsvCustomBindByPosition}, {@link CsvBindAndSplitByPosition} or
     * {@link CsvBindAndJoinByPosition} are present,
     * {@link ColumnPositionMappingStrategy} is chosen.</li>
     * <li>Otherwise, {@link HeaderColumnNameMappingStrategy} is chosen. If
     * annotations are present, they will be used, otherwise the field names
     * will be used as the column names.</li></ol>
     * 
     * @param <T> The type of the bean for which the mapping strategy is sought
     * @param type The class of the bean for which the mapping strategy is sought
     * @param errorLocale The locale to use for all error messages. If null, the
     *   default locale is used.
     * @return A functional mapping strategy for the bean in question
     */
    public static <T> MappingStrategy<T> determineMappingStrategy(Class type, Locale errorLocale) {
        // Check for annotations
        Field[] fields = FieldUtils.getAllFields(type);
        boolean positionAnnotationsPresent = false;
        for(Field field : fields) {
            if(field.isAnnotationPresent(CsvBindByPosition.class)
                    || field.isAnnotationPresent(CsvBindAndSplitByPosition.class)
                    || field.isAnnotationPresent(CsvBindAndJoinByPosition.class)
                    || field.isAnnotationPresent(CsvCustomBindByPosition.class)) {
                positionAnnotationsPresent = true;
                break;
            }
        }

        // Set the mapping strategy according to what we've found.
        MappingStrategy<T> mappingStrategy;
        if(positionAnnotationsPresent) {
            ColumnPositionMappingStrategy<T> ms = new ColumnPositionMappingStrategy<>();
            ms.setErrorLocale(errorLocale);
            ms.setType(type);
            mappingStrategy = ms;
        }
        else {
            HeaderColumnNameMappingStrategy<T> ms = new HeaderColumnNameMappingStrategy<>();
            ms.setErrorLocale(errorLocale);
            ms.setType(type);
            
            // Ugly hack, but I have to get the field names into the stupid
            // strategy somehow.
            if(!ms.isAnnotationDriven()) {
                SortedSet<String> sortedFields = new TreeSet<>();
                for(Field f : fields) {
                    if(!f.isSynthetic()) { // Otherwise JaCoCo breaks tests
                        sortedFields.add(f.getName());
                    }
                }
                String header = StringUtils.join(sortedFields, ',').concat("\n");
                try {
                    CSVReader csvr = new CSVReaderBuilder(new StringReader(header))
                            .withErrorLocale(errorLocale).build();
                    ms.captureHeader(csvr);
                    ms.findDescriptor(0);
                }
                catch(IOException e) {
                    // Can't happen. It's a StringReader with a defined string.
                }
                catch(CsvRequiredFieldEmptyException e) {
                    // Can't happen. By definition we have all fields
                    // represented in the header.
                }
            }
            
            mappingStrategy = ms;
        }
        return mappingStrategy;
    }
    
    /**
     * I find it annoying that when I want to queue something in a blocking
     * queue, the thread might be interrupted and I have to try again; this
     * method fixes that.
     * @param <E> The type of the object to be queued
     * @param queue The queue the object should be added to
     * @param object The object to be queued
     * @since 4.0
     */
    public static <E> void queueRefuseToAcceptDefeat(BlockingQueue<E> queue, E object) {
        boolean interrupted = true;
        while(interrupted) {
            try {
                queue.put(object);
                interrupted = false;
            }
            catch(InterruptedException ie) {/* Do nothing. */}
        }
    }

}
