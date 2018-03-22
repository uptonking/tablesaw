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

import com.opencsv.exceptions.CsvException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * The accumulator takes two queues of results of transforming input into beans
 * (beans an exceptions) and orders them for later consumption.
 * This task is delegated to a separate thread so threads can quickly queue
 * their results in a (synchronized, thread-safe) queue and move on with other
 * work, while the relatively expensive operation of ordering the results
 * doesn't block other threads waiting for access to the ordered map.
 * @param <T> Type of the bean being created
 * @author Andrew Rucker Jones
 * @since 4.0
 */
public class AccumulateCsvResults<T> extends Thread {
    private final BlockingQueue<OrderedObject<T>> resultantBeansQueue;
    private final BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue;
    private final ConcurrentMap<Long, T> resultantBeanMap;
    private final ConcurrentMap<Long, CsvException> thrownExceptionsMap;
    private boolean mustStop = false;

    /**
     * The only accepted constructor for the accumulator.
     * @param resultantBeansQueue A queue of beans coming out of the pool of
     *   threads creating them. The accumulator pulls from this queue.
     * @param thrownExceptionsQueue A queue of
     *   {@link com.opencsv.exceptions.CsvException} and its derivatives coming
     *   out of the pool of threads creating beans. The accumulator pulls from
     *   this queue.
     * @param resultantBeanMap The (ordered) map of beans that have been
     *   created. The accumulator inserts into this map.
     * @param thrownExceptionsMap The (ordered) map of suppressed exceptions
     *   thrown during bean creation. The accumulator inserts into this map.
     */
    public AccumulateCsvResults(BlockingQueue<OrderedObject<T>> resultantBeansQueue,
            BlockingQueue<OrderedObject<CsvException>> thrownExceptionsQueue,
            ConcurrentMap<Long, T> resultantBeanMap,
            ConcurrentMap<Long, CsvException> thrownExceptionsMap) {
        super();
        this.resultantBeansQueue = resultantBeansQueue;
        this.thrownExceptionsQueue = thrownExceptionsQueue;
        this.resultantBeanMap = resultantBeanMap;
        this.thrownExceptionsMap = thrownExceptionsMap;
    }

    /**
     * Checks whether the accumulator should shut itself down.
     * This method must always be used to check the value of the signal boolean,
     * because it's synchronized.
     * @return Whether the accumulator should stop
     */
    public synchronized boolean isMustStop() {
        return mustStop;
    }

    /**
     * Tells the accumulator whether it should stop.
     * This method must always be used to set the value of the signal boolean,
     * because it's synchronized.
     * @param mustStop Whether the accumulator should stop
     */
    public synchronized void setMustStop(boolean mustStop) {
        this.mustStop = mustStop;
    }

    @Override
    public void run() {
        while(!isMustStop() || !resultantBeansQueue.isEmpty() || !thrownExceptionsQueue.isEmpty()) {
            while(!resultantBeansQueue.isEmpty()) {
                OrderedObject<T> bean = resultantBeansQueue.poll();
                if(bean != null) {
                    resultantBeanMap.put(bean.getOrdinal(), bean.getElement());
                }
            }
            while(!thrownExceptionsQueue.isEmpty()) {
                OrderedObject<CsvException> capturedException = thrownExceptionsQueue.poll();
                if(capturedException != null) {
                    thrownExceptionsMap.put(capturedException.getOrdinal(), capturedException.getElement());
                }
            }
            Thread.yield();
        }
    }
}
