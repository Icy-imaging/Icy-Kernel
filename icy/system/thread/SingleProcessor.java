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

    /**
     * Add a task to the processor.<br>
     */
    @Override
    public boolean addTask(Runnable task, boolean onEventThread, int id)
    {
        if (queue || (!isProcessing()))
        {
            // remove current task if any
            removeAllWaitingTasks();
            // add task
            return super.addTask(task, onEventThread, id);
        }

        return false;
    }

    /**
     * @deprecated use {@link #addTask(Runnable)} instead
     */
    @Deprecated
    public synchronized boolean requestProcess(Runnable task)
    {
        return addTask(task);
    }

    /**
     * @deprecated use {@link #addTask(Runnable, boolean)} instead
     */
    @Deprecated
    public synchronized boolean requestProcess(Runnable task, boolean onAWTEventThread)
    {
        return addTask(task, onAWTEventThread);
    }

}
