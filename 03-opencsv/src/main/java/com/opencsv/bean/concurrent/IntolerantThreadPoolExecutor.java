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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This ThreadPoolExecutor automatically shuts down on any failed thread.
 * This is the historically established precedent for dealing with input errors
 * in opencsv. This implementation expects all uncaught exceptions from its
 * threads to be wrapped in a {@link java.lang.RuntimeException}. The number of
 * threads in the pool is fixed.
 * @author Andrew Rucker Jones
 * @since 4.0
 */
public class IntolerantThreadPoolExecutor extends ThreadPoolExecutor {
    
    private Throwable terminalException;

    /**
     * Constructor for a thread pool executor that stops by itself as soon as
     * any thread throws an exception.
     * Threads never time out and the queue for inbound work is unbounded.
     */
    public IntolerantThreadPoolExecutor() {
        super(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(), Long.MAX_VALUE,
                TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    /**
     * Shuts the Executor down if the thread ended in an exception.
     * @param r {@inheritDoc}
     * @param t {@inheritDoc} 
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if(t != null) {
            if(t.getCause() != null) {
                // Normally, everything that gets to this point should be
                // wrapped in a RuntimeException to get past the lack of checked
                // exceptions in Runnable.run().
                terminalException = t.getCause();
            }
            else {
                terminalException = t;
            }
            shutdownNow();
        }
    }
    
    /**
     * If an unrecoverable exception was thrown during processing, it can be
     * retrieved here.
     * @return The exception that halted one of the threads, which caused the
     *   executor to shut itself down
     */
    public Throwable getTerminalException() {
        return terminalException;
    }
}
