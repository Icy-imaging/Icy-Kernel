/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.system.thread;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author stephane
 */
public class SingleProcessor extends Processor
{
    class SingleProcessorRejectedExecutionHandler implements RejectedExecutionHandler
    {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
        {
            // just ignore
            // System.out.println("Ignore execution :" + r);
        }
    }

    private final boolean queue;

    /**
     * 
     */
    public SingleProcessor(boolean enableQueue, String name)
    {
        super(1, 1);

        queue = enableQueue;
        setRejectedExecutionHandler(new SingleProcessorRejectedExecutionHandler());
        setDefaultThreadName(name);
    }

    /**
     * 
     */
    public SingleProcessor(boolean enableQueue)
    {
        this(enableQueue, "SingleProcessor");
    }

    @Override
    protected synchronized <T> Future<T> submit(FutureTaskAdapter<T> task)
    {
        if (queue || (!isProcessing()))
        {
            // remove current task if any
            removeAllWaitingTasks();
            // then add task
            return super.submit(task);
        }

        // return null mean the task was ignored
        return null;
    }

    /**
     * @deprecated use {@link #submit(Runnable)} instead.
     */
    @Deprecated
    public synchronized boolean requestProcess(Runnable task)
    {
        return submit(task) != null;
    }

    /**
     * @deprecated use {@link #submit(Runnable, boolean)} instead
     */
    @Deprecated
    public synchronized boolean requestProcess(Runnable task, boolean onAWTEventThread)
    {
        return submit(task, onAWTEventThread) != null;
    }

}
