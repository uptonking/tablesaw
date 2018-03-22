package com.opencsv.bean;

/*
 Copyright 2007 Kyle Miller.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.bean.concurrent.AccumulateCsvResults;
import com.opencsv.bean.concurrent.IntolerantThreadPoolExecutor;
import com.opencsv.bean.concurrent.OrderedObject;
import com.opencsv.bean.concurrent.ProcessCsvLine;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.*;

/**
 * Converts CSV data to objects.
 * Mixing the {@link #parse()} method with the {@link #iterator() Iterator} is
 * not supported and will lead to unpredictable results. Additionally, reusing
 * an instance of this class after all beans have been read is not supported
 * and will certainly break something.
 *
 * @param <T> Class to convert the objects to.
 */
public class CsvToBean<T> implements Iterable<T> {
    
   /** A list of all exceptions during parsing and mapping of the input. */
    private List<CsvException> capturedExceptions = null;

   /** The mapping strategy to be used by this CsvToBean. */
    private MappingStrategy<T> mappingStrategy;

   /** The reader this class will use to access the data to be read. */
    private CSVReader csvReader;

   /** The filter this class will use on the beans it reads. */
    private CsvToBeanFilter filter = null;

    /**
     * Determines whether or not exceptions should be thrown during parsing or
     * collected for later examination through {@link #getCapturedExceptions()}.
     */
    private boolean throwExceptions = true;
    
    /**
     * Determines whether resulting data sets have to be in the same order as
     * the input.
     */
    private boolean orderedResults = true;
    
    /** Counts how many records have been read from the input. */
    private long lineProcessed;
    
    /** Stores the result of parsing a line of input. */
    private String[] line;
    
    /** The ExecutorService for parallel processing of beans. */
    private IntolerantThreadPoolExecutor executor;
    
    /** A separate thread that accumulates and orders results. */
    private AccumulateCsvResults<T> accumulateThread = null;
    
    /** A queue of the beans created. */
    private BlockingQueue<OrderedObject<T>> resultantBeansQueue;
    
    /** A queue of exceptions thrown by threads during processing. */
    private BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue;
    
    /** A sorted, concurrent map for the beans created. */
    private ConcurrentNavigableMap<Long, T> resultantBeansMap = null;
    
    /** A sorted, concurrent map for any exceptions captured. */
    private ConcurrentNavigableMap<Long, CsvException> thrownExceptionsMap = null;
    
    /** The errorLocale for error messages. */
    private Locale errorLocale = Locale.getDefault();

    /**
     * Default constructor.
     */
    public CsvToBean() {
    }

    /**
     * Parse the values from a CSVReader constructed from the Reader passed in.
     * @param mapper Mapping strategy for the bean.
     * @param reader Reader used to construct a CSVReader
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, Reader reader) {
        setMappingStrategy(mapper);
        setCsvReader(new CSVReaderBuilder(reader).withErrorLocale(errorLocale).build());
        return parse();
    }

    /**
     * Parse the values from a CSVReader constructed from the Reader passed in.
     * @param mapper Mapping strategy for the bean.
     * @param reader Reader used to construct a CSVReader
     * @param throwExceptions If false, exceptions internal to opencsv will not
     *   be thrown, but can be accessed after processing is finished through
     *   {@link #getCapturedExceptions()}.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, Reader reader, boolean throwExceptions) {
        setMappingStrategy(mapper);
        setCsvReader(new CSVReaderBuilder(reader).withErrorLocale(errorLocale).build());
        this.setThrowExceptions(throwExceptions);
        return parse();
    }

    /**
     * Parse the values from a CSVReader constructed from the Reader passed in.
     *
     * @param mapper Mapping strategy for the bean.
     * @param reader Reader used to construct a CSVReader
     * @param filter CsvToBeanFilter to apply - null if no filter.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, Reader reader, CsvToBeanFilter filter) {
        setMappingStrategy(mapper);
        setCsvReader(new CSVReaderBuilder(reader).withErrorLocale(errorLocale).build());
        this.setFilter(filter);
        return parse();
    }

    /**
     * Parse the values from a CSVReader constructed from the Reader passed in.
     * @param mapper Mapping strategy for the bean.
     * @param reader Reader used to construct a CSVReader
     * @param filter CsvToBeanFilter to apply - null if no filter.
     * @param throwExceptions If false, exceptions internal to opencsv will not
     *   be thrown, but can be accessed after processing is finished through
     *   {@link #getCapturedExceptions()}.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, Reader reader,
            CsvToBeanFilter filter, boolean throwExceptions) {
        setMappingStrategy(mapper);
        setCsvReader(new CSVReaderBuilder(reader).withErrorLocale(errorLocale).build());
        this.setFilter(filter);
        this.setThrowExceptions(throwExceptions);
        return parse();
    }

    /**
     * Parse the values from the CSVReader.
     * @param mapper Mapping strategy for the bean.
     * @param csv CSVReader
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, CSVReader csv) {
        setMappingStrategy(mapper);
        setCsvReader(csv);
        return parse();
    }

    /**
     * Parse the values from the CSVReader.
     * @param mapper Mapping strategy for the bean.
     * @param csv CSVReader
     * @param throwExceptions If false, exceptions internal to opencsv will not
     *   be thrown, but can be accessed after processing is finished through
     *   {@link #getCapturedExceptions()}.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, CSVReader csv, boolean throwExceptions) {
        setMappingStrategy(mapper);
        setCsvReader(csv);
        this.setThrowExceptions(throwExceptions);
        return parse();
    }

    /**
     * Parse the values from the CSVReader.
     * Throws exceptions for bad data and other sorts of problems relating
     * directly to opencsv, as well as general exceptions from external code
     * used.
     *
     * @param mapper Mapping strategy for the bean.
     * @param csv CSVReader
     * @param filter CsvToBeanFilter to apply - null if no filter.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, CSVReader csv,
            CsvToBeanFilter filter) {
        setMappingStrategy(mapper);
        setCsvReader(csv);
        this.setFilter(filter);
        return parse();
    }

    /**
     * Parse the values from the CSVReader.
     * Only throws general exceptions from external code used. Problems related
     * to opencsv and the data provided to it are captured for later processing
     * by user code and can be accessed through {@link #getCapturedExceptions()}.
     *
     * @param mapper Mapping strategy for the bean.
     * @param csv CSVReader
     * @param filter CsvToBeanFilter to apply - null if no filter.
     * @param throwExceptions If false, exceptions internal to opencsv will not
     *   be thrown, but can be accessed after processing is finished through
     *   {@link #getCapturedExceptions()}.
     * @return List of Objects.
     * @deprecated Please use {@link CsvToBeanBuilder} instead.
     */
    @Deprecated
    public List<T> parse(MappingStrategy<T> mapper, CSVReader csv,
            CsvToBeanFilter filter, boolean throwExceptions) {
        setMappingStrategy(mapper);
        setCsvReader(csv);
        this.setFilter(filter);
        this.setThrowExceptions(throwExceptions);
        return parse();
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
        // 
        executor = new IntolerantThreadPoolExecutor();
        executor.prestartAllCoreThreads();
        resultantBeansQueue = new LinkedBlockingQueue<>();
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
                    resultantBeansQueue, thrownExceptionsQueue, resultantBeansMap,
                    thrownExceptionsMap);
            accumulateThread.start();
        }
    }
    
    private void submitAllBeans() throws IOException, InterruptedException {
        while (null != (line = csvReader.readNext())) {
            lineProcessed++;
            executor.execute(new ProcessCsvLine<>(
                    lineProcessed, mappingStrategy, filter, line,
                    resultantBeansQueue, thrownExceptionsQueue,
                    throwExceptions));
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
            // Trigger a catch in the calling method
            throw new RejectedExecutionException();
        }
    }
    
    private List<T> prepareResults() {
        // Prepare results. Checking for these maps to be != null makes the
        // compiler feel better than checking that the accumulator is not null.
        // This is to differentiate between the ordered and unordered cases.
        List<T> resultList;
        if(thrownExceptionsMap != null && resultantBeansMap != null) {
            capturedExceptions = new ArrayList<>(thrownExceptionsMap.values());
            resultList = new ArrayList<>(resultantBeansMap.values());
        }
        else {
            capturedExceptions = new ArrayList<>(thrownExceptionsQueue.size());
            OrderedObject<CsvException> oocsve;
            while(!thrownExceptionsQueue.isEmpty()) {
                oocsve = thrownExceptionsQueue.poll();
                if(oocsve != null) {capturedExceptions.add(oocsve.getElement());}
            }
            resultList = new ArrayList<>(resultantBeansQueue.size());
            OrderedObject<T> ooresult;
            while(!resultantBeansQueue.isEmpty()) {
                    ooresult = resultantBeansQueue.poll();
                    if(ooresult != null) {resultList.add(ooresult.getElement());}
            }
        }
        return resultList;
    }

    /**
     * Parses the input based on parameters already set through other methods.
     * @return A list of populated beans based on the input
     * @throws IllegalStateException If either MappingStrategy or CSVReader is
     *   not specified
     */
    public List<T> parse() throws IllegalStateException {
        prepareToReadInput();
        prepareForParallelProcessing();

        // Parse through each line of the file
        try {
            submitAllBeans();
        } catch(RejectedExecutionException e) {
            // An exception in one of the bean creation threads prompted the
            // executor service to shutdown before we were done.
            if(accumulateThread != null) {
                accumulateThread.setMustStop(true);
            }
            throw new RuntimeException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("parsing.error"),
                    lineProcessed, Arrays.toString(line)), executor.getTerminalException());
        } catch (Exception e) {
            // Exception during parsing. Always unrecoverable.
            executor.shutdownNow();
            if(accumulateThread != null) {
                accumulateThread.setMustStop(true);
            }
            throw new RuntimeException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("parsing.error"),
                    lineProcessed, Arrays.toString(line)), e);
        }
        
        return prepareResults();
    }

    /**
     * Returns the list of all exceptions that would have been thrown during the
     * import, but were suppressed by setting {@link #throwExceptions} to
     * {@code false}.
     *
     * @return The list of exceptions captured while processing the input file
     */
    public List<CsvException> getCapturedExceptions() {
        if (capturedExceptions == null) {
            capturedExceptions = new ArrayList<>();
        }
        return capturedExceptions;
    }

    /**
     * Sets the mapping strategy to be used by this bean.
     * @param mappingStrategy Mapping strategy to convert CSV input to a bean
     */
    public void setMappingStrategy(MappingStrategy<T> mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }

    /**
     * Sets the reader to be used to read in the information from the CSV input.
     * @param csvReader Reader for input
     */
    public void setCsvReader(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

    /**
     * Sets a filter to selectively remove some lines of input before they
     * become beans.
     * @param filter A class that filters the input lines
     */
    public void setFilter(CsvToBeanFilter filter) {
        this.filter = filter;
    }

    /**
     * Determines whether errors during import should be thrown or kept in a
     * list for later retrieval via {@link #getCapturedExceptions()}.
     *
     * @param throwExceptions Whether or not to throw exceptions during
     *   processing
     */
    public void setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }
    
    /**
     * Sets whether or not results must be returned in the same order in which
     * they appear in the input.
     * The default is that order is preserved. If your data do not need to be
     * ordered, you can get a slight performance boost by setting
     * {@code orderedResults} to {@code false}. The lack of ordering then also
     * applies to any captured exceptions, if you have chosen not to have
     * exceptions thrown.
     * @param orderedResults Whether or not the beans returned are in the same
     *   order they appeared in the input
     * @since 4.0
     */
    public void setOrderedResults(boolean orderedResults) {
        this.orderedResults = orderedResults;
    }
    
    /**
     * Sets the locale for error messages.
     * @param errorLocale Locale for error messages. If null, the default locale
     *   is used.
     * @since 4.0
     */
    public void setErrorLocale(Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        if(csvReader != null) {
            csvReader.setErrorLocale(this.errorLocale);
        }
        if(mappingStrategy != null) {
            mappingStrategy.setErrorLocale(this.errorLocale);
        }
    }
    
    private void prepareToReadInput() throws IllegalStateException {
        // First verify that the user hasn't failed to give us the information
        // we need to do his or her work for him or her.
        if(mappingStrategy == null || csvReader == null) {
            throw new IllegalStateException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("specify.strategy.reader"));
        }

        // Get the header information
        try {
            mappingStrategy.captureHeader(csvReader);
        } catch (Exception e) {
            throw new RuntimeException(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("header.error"), e);
        }
        
        // Reset to beginning values
        lineProcessed = 0;
        line = null;
    }
    
    /**
     * The iterator returned by this method takes one line of input at a time
     * and returns one bean at a time.
     * <p>The advantage to this method is saving memory. The cost is the loss of
     * parallel processing, reducing throughput.</p>
     * <p>The iterator respects all aspects of {@link CsvToBean}, including
     * filters and capturing exceptions.</p>
     * @return An iterator over the beans created from the input
     */
    @Override
    public Iterator<T> iterator() {
        prepareToReadInput();
        return new CsvToBeanIterator();
    }
    
    /**
     * A private inner class for implementing an iterator for the input data.
     */
    private class CsvToBeanIterator implements Iterator<T> {
        private T bean;
        
        public CsvToBeanIterator() {
            resultantBeansQueue = new ArrayBlockingQueue<>(1);
            thrownExceptionsQueue = new ArrayBlockingQueue<>(1);
            readSingleLine();
        }
        
        private void processException() {
            // An exception was thrown
            OrderedObject<CsvException> o = thrownExceptionsQueue.poll();
            if(o != null && o.getElement() != null) {
                if(capturedExceptions == null) {
                    capturedExceptions = new ArrayList<>();
                }
                capturedExceptions.add(o.getElement());
            }
        }

        private void readLineWithPossibleError() throws IOException {
            // Read a line
            bean = null;
            while(bean == null && null != (line = csvReader.readNext())) {
                lineProcessed++;
                // Create a bean
                ProcessCsvLine<T> proc = new ProcessCsvLine<>(
                        lineProcessed, mappingStrategy, filter, line,
                        resultantBeansQueue, thrownExceptionsQueue,
                        throwExceptions);
                proc.run();

                if(!thrownExceptionsQueue.isEmpty()) {
                    processException();
                }
                else {
                    // No exception, so there really must always be a bean
                    // . . . unless it was filtered
                    OrderedObject<T> o = resultantBeansQueue.poll();
                    bean = o==null?null:o.getElement();
                }
            }
            if(line == null) {
                // There isn't any more
                bean = null;
            }
        }

        private void readSingleLine() {
            try {
                readLineWithPossibleError();
            }
            catch(IOException e) {
                line = null;
                throw new RuntimeException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale).getString("parsing.error"),
                        lineProcessed, Arrays.toString(line)), e);
            }
        }

        @Override
        public boolean hasNext() {
            return bean != null;
        }

        @Override
        public T next() {
            if(bean == null) {
                throw new NoSuchElementException();
            }
            T intermediateBean = bean;
            readSingleLine();
            return intermediateBean;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException(ResourceBundle
                    .getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                    .getString("read.only.iterator"));
        }
    }
}
