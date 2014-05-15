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

/**
 * @author stephane
 */
public class SingleProcessor extends Processor
{
    private final boolean queueEnabled;

    /**
     * 
     */
    public SingleProcessor(boolean enableQueue, String name)
    {
        super(1, 1);

        queueEnabled = enableQueue;
        setRejectedExecutionHandler(new DiscardPolicy());
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
     * Try to submit the specified task for execution and returns a Future representing that task.<br>
     * The Future's <tt>get</tt> method will return <tt>null</tt> upon <em>successful</em>
     * completion.<br>
     * Returns a <code>null</code> Future object if processor is already processing or queue is not
     * empty (depending the {@link #isQueueEnabled()} parameter) to notify the task has been
     * ignored.
     */
    @Override
    protected synchronized <T> Future<T> submit(FutureTaskAdapter<T> task)
    {
        // add task only if not already processing or queue empty
        if (!hasWaitingTasks() && (!isProcessing() || queueEnabled))
            return super.submit(task);

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

    public boolean isQueueEnabled()
    {
        return queueEnabled;
    }
}
