/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.system.thread;

import java.util.EventListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class Processor extends ThreadPoolExecutor
{
    public interface ProcessorEventListener extends EventListener
    {
        public void processDone(Processor source);
    }

    class ProcessorThreadFactory implements ThreadFactory
    {
        @Override
        public Thread newThread(Runnable r)
        {
            final Thread result = new Thread(r);

            result.setPriority(priority);

            return result;
        }
    }

    protected class Runner implements Runnable
    {
        private final Runnable task;
        private final boolean onDispatchEvent;
        private final int id;

        public Runner(Runnable task, boolean onDispatchEvent, int id)
        {
            super();

            this.task = task;
            this.onDispatchEvent = onDispatchEvent;
            this.id = id;
        }

        @Override
        public void run()
        {
            if (task != null)
            {
                if (onDispatchEvent)
                    ThreadUtil.invokeNow(task);
                else
                    task.run();
            }
        }

        /**
         * @return the id
         */
        public int getId()
        {
            return id;
        }

        /**
         * @return the task
         */
        public Runnable getTask()
        {
            return task;
        }
    }

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = Thread.MIN_PRIORITY;

    /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = Thread.NORM_PRIORITY;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = Thread.MAX_PRIORITY;

    /**
     * parameters
     */
    int priority;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal
     */
    boolean rejected;

    /**
     * 
     */
    public Processor(int maxWaiting, int maxProcessing, int priority)
    {
        super(maxProcessing, maxProcessing, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxWaiting));

        setThreadFactory(new ProcessorThreadFactory());
        allowCoreThreadTimeOut(true);
        setKeepAliveTime(5, TimeUnit.SECONDS);

        this.priority = priority;
        listeners = new EventListenerList();
    }

    /**
     * 
     */
    public Processor(int maxWaiting, int maxProcessing)
    {
        this(maxWaiting, maxProcessing, NORM_PRIORITY);
    }

    /**
     * Add a task
     */
    public boolean addTask(Runnable task, boolean onAWTEventThread, int id)
    {
        try
        {
            execute(new Runner(task, onAWTEventThread, id));
        }
        catch (RejectedExecutionException E)
        {
            return false;
        }

        return true;
    }

    /**
     * Add a task
     */
    public boolean addTask(Runnable task, boolean onAWTEventThread)
    {
        return addTask(task, onAWTEventThread, -1);
    }

    /**
     * return true if one or more process are executing
     */
    public boolean isProcessing()
    {
        return (getActiveCount() > 0);
    }

    /**
     * Wait for all tasks completion
     */
    public void waitAll()
    {
        while (isProcessing())
            ThreadUtil.sleep(1);
    }

    /**
     * shutdown and wait current tasks completion
     */
    public void shutdownAndWait()
    {
        shutdown();
        while (!isTerminated())
            ThreadUtil.sleep(1);
    }

    /**
     * @return the priority
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * @param priority
     *        the priority to set
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    /**
     * Get the number of free slot in queue
     */
    public int getFreeSlotNumber()
    {
        return getQueue().remainingCapacity();
    }

    /**
     * Return true if queue is full
     */
    public boolean isFull()
    {
        return getFreeSlotNumber() == 0;
    }

    /**
     * return the number of waiting task
     */
    public int getWaitingTasksCount()
    {
        return getQueue().size();
    }

    /**
     * return the number of task with specified id waiting in queue
     */
    public int getWaitingTasksCount(int id)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        int result = 0;
        // scan all tasks
        for (Runner task : tasks)
            if (task.getId() == id)
                result++;

        return result;
    }

    /**
     * return true if we have at least one task with specified id waiting in queue
     */
    public boolean hasWaitingTasks(int id)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        // scan all tasks
        for (Runner task : tasks)
            if (task.getId() == id)
                return true;

        return false;
    }

    /**
     * return the number of task from specified instance waiting in queue
     */
    public int getWaitingTasksCount(Runnable task)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        int result = 0;
        // scan all tasks
        for (Runner runner : tasks)
            if (runner.getTask() == task)
                result++;

        return result;
    }

    /**
     * return true if we have at least one task waiting in queue
     */
    public boolean hasWaitingTasks()
    {
        return (getWaitingTasksCount() > 0);
    }

    /**
     * return true if we have at least one task from specified instance waiting in queue
     */
    public boolean hasWaitingTasks(Runnable task)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        // scan all tasks
        for (Runner runner : tasks)
            if (runner.getTask() == task)
                return true;

        return false;
    }

    /**
     * Remove first waiting task from specified instance
     */
    public boolean removeFirstWaitingTask(Runnable task)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        // remove first task of specified instance
        for (Runner runner : tasks)
            if (runner.getTask() == task)
                return remove(runner);

        return false;
    }

    /**
     * Remove first waiting task with specified id
     */
    public boolean removeFirstWaitingTask(int id)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);

        // remove first task with specified id
        for (Runner runner : tasks)
            if (runner.getId() == id)
                return remove(runner);

        return false;
    }

    /**
     * Remove all waiting tasks from specified instance
     */
    public boolean removeWaitingTasks(Runnable task)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);
        boolean result = false;

        // remove all tasks of specified instance
        for (Runner runner : tasks)
            if (runner.getTask() == task)
                result |= remove(runner);

        return result;
    }

    /**
     * Remove all waiting tasks with specified id
     */
    public boolean removeWaitingTasks(int id)
    {
        final Runner[] tasks = getQueue().toArray(new Runner[0]);
        boolean result = false;

        // remove all tasks with specified id
        for (Runner runner : tasks)
            if (runner.getId() == id)
                result |= remove(runner);

        return result;
    }

    /**
     * Clear all waiting tasks
     */
    public void removeAllWaitingTasks()
    {
        // remove current task if any
        getQueue().clear();
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ProcessorEventListener listener)
    {
        listeners.add(ProcessorEventListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ProcessorEventListener listener)
    {
        listeners.remove(ProcessorEventListener.class, listener);
    }

    /**
     * fire event
     */
    public void fireDoneEvent()
    {
        for (ProcessorEventListener listener : listeners.getListeners(ProcessorEventListener.class))
            listener.processDone(this);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);

        // notify we just achieved a process
        fireDoneEvent();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);

        //
    }

}
