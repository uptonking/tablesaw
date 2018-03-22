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
package com.opencsv.bean.concurrent;

import com.opencsv.bean.CsvToBeanFilter;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.opencsvUtils;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;

/**
 * A class that encapsulates the job of creating a bean from a line of CSV input
 * and making it possible to run those jobs in parallel.
 * @param <T> The type of the bean being created
 * @author Andrew Rucker Jones
 * @since 4.0
 */
public class ProcessCsvLine<T> implements Runnable {
    private final long lineNumber;
    private final MappingStrategy<T> mapper;
    private final CsvToBeanFilter filter;
    private final String[] line;
    private final BlockingQueue<OrderedObject<T>> resultantBeanQueue;
    private final BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue;
    private final boolean throwExceptions;

    /**
     * The only constructor for creating a bean out of a line of input.
     * @param lineNumber Which record in the input file is being processed
     * @param mapper The mapping strategy to be used
     * @param filter A filter to remove beans from the running, if necessary.
     *   May be null.
     * @param line The line of input to be transformed into a bean
     * @param resultantBeanQueue A queue in which to place the bean created
     * @param thrownExceptionsQueue A queue in which to place a thrown
     *   exception, if one is thrown
     * @param throwExceptions Whether exceptions should be thrown, ending
     *   processing, or suppressed and saved for later processing
     */
    public ProcessCsvLine(
            long lineNumber, MappingStrategy<T> mapper, CsvToBeanFilter filter,
            String[] line, BlockingQueue<OrderedObject<T>> resultantBeanQueue,
            BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue,
            boolean throwExceptions) {
        this.lineNumber = lineNumber;
        this.mapper = mapper;
        this.filter = filter;
        this.line = line;
        this.resultantBeanQueue = resultantBeanQueue;
        this.thrownExceptionsQueue = thrownExceptionsQueue;
        this.throwExceptions = throwExceptions;
    }

    @Override
    public void run() {
        try {
            if (filter == null || filter.allowLine(line)) {
                T obj = processLine();
                opencsvUtils.queueRefuseToAcceptDefeat(
                        resultantBeanQueue,
                        new OrderedObject<>(lineNumber, obj));
            }
        } catch (CsvException e) {
            e.setLineNumber(lineNumber);
            if (throwExceptions) {
                throw new RuntimeException(e);
            }
            opencsvUtils.queueRefuseToAcceptDefeat(thrownExceptionsQueue,
                    new OrderedObject<>(lineNumber, e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a single object from a line from the CSV file.
     * @return Object containing the values.
     * @throws IllegalAccessException Thrown on error creating bean.
     * @throws InvocationTargetException Thrown on error calling the setters.
     * @throws InstantiationException Thrown on error creating bean.
     * @throws IntrospectionException Thrown on error getting the
     *   PropertyDescriptor.
     * @throws CsvBadConverterException If a custom converter cannot be
     *   initialized properly
     * @throws CsvDataTypeMismatchException If the source data cannot be
     *   converted to the type of the destination field
     * @throws CsvRequiredFieldEmptyException If a mandatory field is empty in
     *   the input file
     * @throws CsvConstraintViolationException When the internal structure of
     *   data would be violated by the data in the CSV file
     */
    private T processLine()
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException, IntrospectionException,
            CsvBadConverterException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException, CsvConstraintViolationException {
        return mapper.populateNewBean(line);
    }
}
