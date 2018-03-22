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

import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.opencsvUtils;
import com.opencsv.exceptions.*;
import java.util.concurrent.BlockingQueue;

/**
 * A class for converting one bean into its string representation for writing to
 * an output.
 * @param <T> The type of the bean to be processed
 * @since 4.0
 * @author Andrew Rucker Jones
 */
public class ProcessCsvBean<T> implements Runnable {
    
    private final long lineNumber;
    private final MappingStrategy<T> mappingStrategy;
    private final T bean;
    private final BlockingQueue<OrderedObject<String[]>> resultantLineQueue;
    private final BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue;
    private final boolean throwExceptions;
    
    /**
     * The only constructor for creating a line of CSV output out of a bean.
     * @param lineNumber Which record in the output file is being processed
     * @param mappingStrategy The mapping strategy to be used
     * @param bean The bean to be transformed into a line of output
     * @param resultantLineQueue A queue in which to place the line created
     * @param thrownExceptionsQueue A queue in which to place a thrown
     *   exception, if one is thrown
     * @param throwExceptions Whether exceptions should be thrown or captured
     *   for later processing
     */
    public ProcessCsvBean(long lineNumber, MappingStrategy<T> mappingStrategy,
            T bean, BlockingQueue<OrderedObject<String[]>> resultantLineQueue,
            BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue,
            boolean throwExceptions) {
        this.lineNumber = lineNumber;
        this.mappingStrategy = mappingStrategy;
        this.bean = bean;
        this.resultantLineQueue = resultantLineQueue;
        this.thrownExceptionsQueue = thrownExceptionsQueue;
        this.throwExceptions = throwExceptions;
    }
    
    @Override
    public void run() {
        try {
            opencsvUtils.queueRefuseToAcceptDefeat(resultantLineQueue,
                    new OrderedObject<>(lineNumber, mappingStrategy.transmuteBean(bean)));
        }
        catch (CsvException e) {
            e.setLineNumber(lineNumber);
            if(throwExceptions) {
                throw new RuntimeException(e);
            }
            opencsvUtils.queueRefuseToAcceptDefeat(thrownExceptionsQueue,
                    new OrderedObject<>(lineNumber, e));
        }
        catch(CsvRuntimeException csvre) {
            // Rethrowing exception here because I do not want the CsvRuntimeException caught and rewrapped in the catch below.
            throw csvre;
        }
        catch(Exception t) {
            throw new RuntimeException(t);
        }
    }
    
}
