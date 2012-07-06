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

import icy.system.SystemUtil;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    public static final int DEFAULT_MAX_WAITING = 1024;
    public static final int DEFAULT_MAX_PROCESSING = SystemUtil.getAvailableProcessors() * 2;

    public interface ProcessorEventListener extends EventListener
    {
        public void processDone(Processor source, Runnable runnable);
    }

    class ProcessorThreadFactory implements ThreadFactory
    {
        @Override
        public Thread newThread(Runnable r)
        {
            final Thread result = new Thread(r, defaultThreadName);

            result.setPriority(priority);

            return result;
        }
    }

    protected class Runner implements Runnable
    {
        private final Runnable task;
        private final boolean onEventThread;
        private final int id;

        public Runner(Runnable task, boolean onEventThread, int id)
        {
            super();

            this.task = task;
            this.onEventThread = onEventThread;
            this.id = id;
        }

        @Override
        public void run()
        {
            if (task != null)
            {
                if (onEventThread)
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
    String defaultThreadName;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal
     */
    private Runner waitingExecution;

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.<br>
     * 
     * @param priority
     *        Processor priority<br>
     *        <code>Processor.MIN_PRIORITY</code><br>
     *        <code>Processor.NORM_PRIORITY</code><br>
     *        <code>Processor.MAX_PRIORITY</code>
     */
    public Processor(int maxWaiting, int maxProcessing, int priority)
    {
        super(maxProcessing, maxProcessing, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxWaiting));

        setThreadFactory(new ProcessorThreadFactory());
        allowCoreThreadTimeOut(true);
        setKeepAliveTime(2, TimeUnit.SECONDS);

        this.priority = priority;
        defaultThreadName = "Processor";
        listeners = new EventListenerList();

        waitingExecution = null;
    }

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.
     */
    public Processor(int maxWaiting, int maxProcessing)
    {
        this(maxWaiting, maxProcessing, NORM_PRIORITY);
    }

    /**
     * Create a new Processor with default number of maximum waiting and processing tasks.
     */
    public Processor()
    {
        this(DEFAULT_MAX_WAITING, DEFAULT_MAX_PROCESSING);
    }

    /**
     * Retrieve Runner from Runnable
     */
    private Runner getRunner(Runnable runnable)
    {
        for (Runner runner : getWaitingTasks())
            if (runner.getTask() == runnable)
                return runner;

        return null;
    }

    /**
     * Remove the current task from the waiting queue if possible.<br>
     * Return true if the operation succeed
     */
    public boolean removeTask(Runnable task)
    {
        return remove(getRunner(task));
    }

    /**
     * Add a task to the processor.
     */
    public boolean addTask(Runnable task, boolean onEventThread, int id)
    {
        try
        {
            final Runner runner = new Runner(task, onEventThread, id);
            execute(runner);
            waitingExecution = runner;
        }
        catch (RejectedExecutionException E)
        {
            return false;
        }

        return true;
    }

    /**
     * Add a task to the processor.
     */
    public boolean addTask(Runnable task, boolean onEventThread)
    {
        return addTask(task, onEventThread, -1);
    }

    /**
     * Add a task to the processor.
     */
    public boolean addTask(Runnable task)
    {
        return addTask(task, false, -1);
    }

    @Override
    public boolean remove(Runnable task)
    {
        // don't forget to remove the reference here
        if (waitingExecution == task)
            waitingExecution = null;

        return super.remove(task);
    }

    /**
     * Return true if one or more process are executing or we still have waiting tasks.
     */
    public boolean isProcessing()
    {
        return (getActiveCount() > 0) || hasWaitingTasks();
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

    public String getDefaultThreadName()
    {
        return defaultThreadName;
    }

    public void setDefaultThreadName(String defaultThreadName)
    {
        this.defaultThreadName = defaultThreadName;
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
     * Return waiting tasks
     */
    public Runner[] getWaitingTasks()
    {
        final BlockingQueue<Runnable> q = getQueue();

        synchronized (q)
        {
            return q.toArray(new Runner[0]);
        }
    }

    /**
     * Return waiting tasks with specified id
     */
    public List<Runner> getWaitingTasks(int id)
    {
        final ArrayList<Runner> result = new ArrayList<Runner>();

        // scan all tasks
        for (Runner task : getWaitingTasks())
            if (task.getId() == id)
                result.add(task);

        return result;
    }

    /**
     * Return waiting tasks from specified instance
     */
    public List<Runner> getWaitingTasks(Runnable task)
    {
        final ArrayList<Runner> result = new ArrayList<Runner>();

        // scan all tasks
        for (Runner runner : getWaitingTasks())
            if (runner.getTask() == task)
                result.add(runner);

        return result;
    }

    /**
     * Return the number of waiting task
     */
    public int getWaitingTasksCount()
    {
        final int result = getQueue().size();

        // queue can be empty even if we have a waiting execution
        // in this particular case we return 1
        if ((result == 0) && (waitingExecution != null))
            return 1;

        return result;
    }

    /**
     * Return the number of task with specified id waiting in queue
     */
    public int getWaitingTasksCount(int id)
    {
        int result = 0;
        // scan all tasks
        for (Runner task : getWaitingTasks())
            if (task.getId() == id)
                result++;

        return result;
    }

    /**
     * Return the number of task from specified instance waiting in queue
     */
    public int getWaitingTasksCount(Runnable task)
    {
        int result = 0;
        // scan all tasks
        for (Runner runner : getWaitingTasks())
            if (runner.getTask() == task)
                result++;

        return result;
    }

    /**
     * Return true if we have at least one task waiting in queue
     */
    public boolean hasWaitingTasks()
    {
        return (getWaitingTasksCount() > 0);
    }

    /**
     * Return true if we have at least one task with specified id waiting in queue
     */
    public boolean hasWaitingTasks(int id)
    {
        // scan all tasks
        for (Runner task : getWaitingTasks())
            if (task.getId() == id)
                return true;

        return false;
    }

    /**
     * Return true if we have at least one task from specified instance waiting in queue
     */
    public boolean hasWaitingTasks(Runnable task)
    {
        // scan all tasks
        for (Runner runner : getWaitingTasks())
            if (runner.getTask() == task)
                return true;

        return false;
    }

    /**
     * Remove first waiting task with specified id
     */
    public boolean removeFirstWaitingTask(int id)
    {
        synchronized (getQueue())
        {
            // remove first task with specified id
            for (Runner runner : getWaitingTasks())
                if (runner.getId() == id)
                    return remove(runner);
        }

        return false;
    }

    /**
     * Remove first waiting task from specified instance
     */
    public boolean removeFirstWaitingTask(Runnable task)
    {
        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (Runner runner : getWaitingTasks())
                if (runner.getTask() == task)
                    return remove(runner);
        }

        return false;
    }

    /**
     * Remove all waiting tasks with specified id
     */
    public boolean removeWaitingTasks(int id)
    {
        boolean result = false;

        synchronized (getQueue())
        {
            // remove all tasks with specified id
            for (Runner task : getWaitingTasks(id))
                result |= remove(task);
        }

        return result;
    }

    /**
     * Remove all waiting tasks from specified instance
     */
    public boolean removeWaitingTasks(Runnable task)
    {
        boolean result = false;

        synchronized (getQueue())
        {
            // remove all tasks of specified instance
            for (Runner runner : getWaitingTasks(task))
                result |= remove(runner);
        }

        return result;
    }

    /**
     * Clear all waiting tasks
     */
    public void removeAllWaitingTasks()
    {
        waitingExecution = null;

        synchronized (getQueue())
        {
            // remove all tasks
            getQueue().clear();
        }
    }

    /**
     * Limit number of waiting task of specified instance to specified value
     */
    public void limitWaitingTask(Runnable task, int value)
    {
        synchronized (getQueue())
        {
            final List<Runner> tasks = getWaitingTasks(task);
            final int numToRemove = tasks.size() - value;

            for (int i = 0; i < numToRemove; i++)
                remove(tasks.get(i));
        }
    }

    /**
     * Limit number of waiting task of specified id to specified value
     */
    public void limitWaitingTask(int id, int value)
    {
        synchronized (getQueue())
        {
            final List<Runner> tasks = getWaitingTasks(id);
            final int numToRemove = tasks.size() - value;

            for (int i = 0; i < numToRemove; i++)
                remove(tasks.get(i));
        }
    }

    /**
     * Limit number of waiting task to specified value
     */
    public boolean limitWaitingTask(int value)
    {
        synchronized (getQueue())
        {
            final Runner[] tasks = getWaitingTasks();
            final int numToRemove = tasks.length - value;

            for (int i = 0; i < numToRemove; i++)
                remove(tasks[i]);
        }

        return false;
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
     * 
     * @param task
     */
    public void fireDoneEvent(Runnable task)
    {
        for (ProcessorEventListener listener : listeners.getListeners(ProcessorEventListener.class))
            listener.processDone(this, task);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);

        // notify we just achieved a process
        fireDoneEvent(((Runner) r).getTask());
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);

        // ok we can remove reference...
        waitingExecution = null;
    }

}
