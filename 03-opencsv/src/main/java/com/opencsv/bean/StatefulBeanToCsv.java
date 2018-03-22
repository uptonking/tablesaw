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

import com.opencsv.CSVWriter;
import com.opencsv.ICSVParser;
import com.opencsv.bean.concurrent.AccumulateCsvResults;
import com.opencsv.bean.concurrent.IntolerantThreadPoolExecutor;
import com.opencsv.bean.concurrent.OrderedObject;
import com.opencsv.bean.concurrent.ProcessCsvBean;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvRuntimeException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.*;

/**
 * This class writes beans out in CSV format to a {@link java.io.Writer},
 * keeping state information and making an intelligent guess at the mapping
 * strategy to be applied.
 * <p>This class implements multi-threading on writing more than one bean, so
 * there should be no need to use it across threads in an application. As such,
 * it is not thread-safe.</p>
 * 
 * @param <T> Type of the bean to be written
 * @author Andrew Rucker Jones
 * @see opencsvUtils#determineMappingStrategy(java.lang.Class, java.util.Locale) 
 * @since 3.9
 */
public class StatefulBeanToCsv<T> {
    /** The beans being written are counted in the order they are written. */
    private int lineNumber = 0;
    
    private final char separator;
    private final char quotechar;
    private final char escapechar;
    private final String lineEnd;
    private boolean headerWritten = false;
    private MappingStrategy<T> mappingStrategy;
    private final Writer writer;
    private CSVWriter csvwriter;
    private boolean throwExceptions;
    private List<CsvException> capturedExceptions = new ArrayList<>();
    private boolean orderedResults = true;
    private IntolerantThreadPoolExecutor executor = null;
    private BlockingQueue<OrderedObject<String[]>> resultantLineQueue;
    private BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue;
    private AccumulateCsvResults accumulateThread = null;
    private ConcurrentNavigableMap<Long, String[]> resultantBeansMap = null;
    private ConcurrentNavigableMap<Long, CsvException> thrownExceptionsMap = null;
    private Locale errorLocale = Locale.getDefault();
    
    /** The nullary constructor should never be used. */
    private StatefulBeanToCsv() {
        throw new IllegalStateException(String.format(
                ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME).getString("nullary.constructor.not.allowed"),
                getClass().getName()));
    }
    
    /**
     * The only constructor that should be used.
     * 
     * @param escapechar The escape character to use when writing a CSV file
     * @param lineEnd The line ending to use when writing a CSV file
     * @param mappingStrategy The mapping strategy to use when writing a CSV file
     * @param quotechar The quote character to use when writing a CSV file
     * @param separator The field separator to use when writing a CSV file
     * @param throwExceptions Whether or not exceptions should be thrown while
     *   writing the CSV file. If not, they are collected and can be retrieved
     *   via {@link #getCapturedExceptions() }.
     * @param writer A {@link java.io.Writer} for writing the beans as a CSV to
     */
    public StatefulBeanToCsv(char escapechar, String lineEnd,
            MappingStrategy<T> mappingStrategy, char quotechar, char separator,
            boolean throwExceptions, Writer writer) {
        this.escapechar = escapechar;
        this.lineEnd = lineEnd;
        this.mappingStrategy = mappingStrategy;
        this.quotechar = quotechar;
        this.separator = separator;
        this.throwExceptions = throwExceptions;
        this.writer = writer;
    }
    
    /**
     * Custodial tasks that must be performed before beans are written to a CSV
     * destination for the first time.
     * @param bean Any bean to be written. Used to determine the mapping
     *   strategy automatically. The bean itself is not written to the output by
     *   this method.
     * @throws CsvRequiredFieldEmptyException If a required header is missing
     *   while attempting to write. Since every other header is hard-wired
     *   through the bean fields and their associated annotations, this can only
     *   happen with multi-valued fields.
     */
    private void beforeFirstWrite(T bean) throws CsvRequiredFieldEmptyException {
        
        // Determine mapping strategy
        if(mappingStrategy == null) {
            mappingStrategy = opencsvUtils.determineMappingStrategy(bean.getClass(), errorLocale);
        }
        
        // Build CSVWriter
        csvwriter = new CSVWriter(writer, separator, quotechar, escapechar, lineEnd);
        
        // Write the header
        String[] header = mappingStrategy.generateHeader(bean);
        if(header.length > 0) {
            csvwriter.writeNext(header);
        }
        headerWritten = true;
    }
    
    /**
     * Writes a bean out to the {@link java.io.Writer} provided to the
     * constructor.
     * 
     * @param bean A bean to be written to a CSV destination
     * @throws CsvDataTypeMismatchException If a field of the bean is
     *   annotated improperly or an unsupported data type is supposed to be
     *   written
     * @throws CsvRequiredFieldEmptyException If a field is marked as required,
     *   but the source is null
     */
    public void write(T bean) throws CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        
        // Write header
        if(bean != null) {
            if(!headerWritten) {
                beforeFirstWrite(bean);
            }
            
            // Process the bean
            resultantLineQueue = new ArrayBlockingQueue<>(1);
            thrownExceptionsQueue = new ArrayBlockingQueue<>(1);
            ProcessCsvBean<T> proc = new ProcessCsvBean<>(++lineNumber,
                    mappingStrategy, bean, resultantLineQueue,
                    thrownExceptionsQueue, throwExceptions);
            try {
                proc.run();
            }
            catch(RuntimeException re) {
                if(re.getCause() != null) {
                    if(re.getCause() instanceof CsvRuntimeException) {
                        // Can't currently happen, but who knows what might be
                        // in the future? I'm certain we wouldn't want to wrap
                        // these in another RuntimeException.
                        throw (CsvRuntimeException) re.getCause();
                    }
                    if(re.getCause() instanceof CsvDataTypeMismatchException) {
                        throw (CsvDataTypeMismatchException) re.getCause();
                    }
                    if(re.getCause() instanceof CsvRequiredFieldEmptyException) {
                        throw (CsvRequiredFieldEmptyException) re.getCause();
                    }
                }
                throw re;
            }
            
            // Write out the result
            if(!thrownExceptionsQueue.isEmpty()) {
                OrderedObject<CsvException> o = thrownExceptionsQueue.poll();
                if(o != null && o.getElement() != null) {
                    capturedExceptions.add(o.getElement());
                }
            }
            else {
                // No exception, so there really must always be a string
                OrderedObject<String[]> result = resultantLineQueue.poll();
                if(result != null && result.getElement() != null) {
                    csvwriter.writeNext(result.getElement());
                }
            }
        }
    }
    
    /**
     * Prepare for parallel processing.
     * <p>The structure is:
     * <ol><li>The main thread parses input and passes it on to</li>
     * <li>The executor, which creates a number of beans in parallel and passes
     * these and any resultant errors to</li>
     * <li>The accumulator, which creates an ordered list of the results.</li></ol></p>
     * <p>The threads in the executor queue their results in a thread-safe
     * queue, which should be O(1), minimizing wait time due to synchronization.
     * The accumulator then removes items from the queue and inserts them into a
     * sorted data structure, which is O(log n) on average and O(n) in the worst
     * case. If the user has told us she doesn't need sorted data, the
     * accumulator is not necessary, and thus is not started.</p>
     */
    private void prepareForParallelProcessing() {
        executor = new IntolerantThreadPoolExecutor();
        executor.prestartAllCoreThreads();
        resultantLineQueue = new LinkedBlockingQueue<>();
        thrownExceptionsQueue = new LinkedBlockingQueue<>();

        // The ordered maps and accumulator are only necessary if ordering is
        // stipulated. After this, the presence or absence of the accumulator is
        // used to indicate ordering or not so as to guard against the unlikely
        // problem that someone sets orderedResults right in the middle of
        // processing.
        if(orderedResults) {
            resultantBeansMap = new ConcurrentSkipListMap<>();
            thrownExceptionsMap = new ConcurrentSkipListMap<>();

            // Start the process for accumulating results and cleaning up
            accumulateThread = new AccumulateCsvResults<>(
                    resultantLineQueue, thrownExceptionsQueue, resultantBeansMap,
                    thrownExceptionsMap);
            accumulateThread.start();
        }
    }
    
    private void submitAllLines(List<T> beans) throws InterruptedException {
        for(T bean : beans) {
            if(bean != null) {
                executor.execute(new ProcessCsvBean<T>(
                        ++lineNumber, mappingStrategy, bean,
                        resultantLineQueue, thrownExceptionsQueue,
                        throwExceptions));
            }
        }

        // Normal termination
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // Wait indefinitely
        if(accumulateThread != null) {
            accumulateThread.setMustStop(true);
            accumulateThread.join();
        }

        // There's one more possibility: The very last bean caused a problem.
        if(executor.getTerminalException() != null) {
            // Trigger first catch clause
            throw new RejectedExecutionException();
        }
    }
    
    private void writeResultsOfParallelProcessingToFile() {
        // Prepare results. Checking for these maps to be != null makes the
        // compiler feel better than checking that the accumulator is not null.
        if(thrownExceptionsMap != null && resultantBeansMap != null) {
            capturedExceptions = new ArrayList<>(thrownExceptionsMap.values());
            for(String[] oneLine : resultantBeansMap.values()) {
                csvwriter.writeNext(oneLine);
            }
        }
        else {
            capturedExceptions = new ArrayList<>(thrownExceptionsQueue.size());
            OrderedObject<CsvException> oocsve;
            while(!thrownExceptionsQueue.isEmpty()) {
                oocsve = thrownExceptionsQueue.poll();
                if(oocsve != null && oocsve.getElement() != null) {
                    capturedExceptions.add(oocsve.getElement());
                }
            }
            OrderedObject<String[]> ooresult;
            while(!resultantLineQueue.isEmpty()) {
                try {
                    ooresult = resultantLineQueue.take();
                    csvwriter.writeNext(ooresult.getElement());
                }
                catch(InterruptedException e) {/* We'll get it during the next loop through. */}
            }
        }
    }
    
    /**
     * Writes a list of beans out to the {@link java.io.Writer} provided to the
     * constructor.
     * 
     * @param beans A list of beans to be written to a CSV destination
     * @throws CsvDataTypeMismatchException If a field of the beans is
     *   annotated improperly or an unsupported data type is supposed to be
     *   written
     * @throws CsvRequiredFieldEmptyException If a field is marked as required,
     *   but the source is null
     */
    public void write(List<T> beans) throws CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        if(CollectionUtils.isNotEmpty(beans)) {
            // Write header
            if(!headerWritten) {
                beforeFirstWrite(beans.get(0));
            }
            
            prepareForParallelProcessing();

            // Process the beans
            try {
                submitAllLines(beans);
            }
            catch(RejectedExecutionException e) {
                // An exception in one of the bean writing threads prompted the
                // executor service to shutdown before we were done.
                if(accumulateThread != null) {
                    accumulateThread.setMustStop(true);
                }
                if(executor.getTerminalException() instanceof RuntimeException) {
                    throw (RuntimeException) executor.getTerminalException();
                }
                if(executor.getTerminalException() instanceof CsvDataTypeMismatchException) {
                    throw (CsvDataTypeMismatchException) executor.getTerminalException();
                }
                if(executor.getTerminalException() instanceof CsvRequiredFieldEmptyException) {
                    throw (CsvRequiredFieldEmptyException) executor.getTerminalException();
                }
                throw new RuntimeException(
                        ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("error.writing.beans"), executor.getTerminalException());
            } catch (Exception e) {
                // Exception during parsing. Always unrecoverable.
                // I can't find a way to create this condition in the current
                // code, but we must have a catch-all clause.
                executor.shutdownNow();
                if(accumulateThread != null) {
                    accumulateThread.setMustStop(true);
                }
                if(executor.getTerminalException() instanceof RuntimeException) {
                    throw (RuntimeException) executor.getTerminalException();
                }
                throw new RuntimeException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("error.writing.beans"), e);
            }

            writeResultsOfParallelProcessingToFile();
        }
    }
    
    /**
     * Sets whether or not results must be written in the same order in which
     * they appear in the list of beans provided as input.
     * The default is that order is preserved. If your data do not need to be
     * ordered, you can get a slight performance boost by setting
     * {@code orderedResults} to {@code false}. The lack of ordering then also
     * applies to any captured exceptions, if you have chosen not to have
     * exceptions thrown.
     * @param orderedResults Whether or not the lines written are in the same
     *   order they appeared in the input
     * @since 4.0
     */
    public void setOrderedResults(boolean orderedResults) {
        this.orderedResults = orderedResults;
    }

    /**
     * @return Whether or not exceptions are thrown. If they are not thrown,
     *   they are captured and returned later via {@link #getCapturedExceptions()}.
     */
    public boolean isThrowExceptions() {
        return throwExceptions;
    }

    /**
     * Any exceptions captured during writing of beans to a CSV destination can
     * be retrieved through this method.
     * <p><em>Reads from the list are destructive!</em> Calling this method will
     * clear the list of captured exceptions. However, calling
     * {@link #write(java.util.List)} or {@link #write(java.lang.Object)}
     * multiple times with no intervening call to this method will not clear the
     * list of captured exceptions, but rather add to it if further exceptions
     * are thrown.</p>
     * @return A list of exceptions that would have been thrown during any and
     *   all read operations since the last call to this method
     */
    public List<CsvException> getCapturedExceptions() {
        List<CsvException> intermediate = capturedExceptions;
        capturedExceptions = new ArrayList<>();
        return intermediate;
    }
    
    /**
     * Sets the locale for all error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     * @since 4.0
     */
    public void setErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
    }
}
