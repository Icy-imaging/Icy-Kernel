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

import icy.main.Icy;
import icy.system.SystemUtil;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
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

    protected class RunnableAdapter implements Runnable
    {
        private final Runnable task;
        private final boolean onEDT;

        public RunnableAdapter(Runnable runnable, boolean onEDT)
        {
            super();

            task = runnable;
            this.onEDT = onEDT;
        }

        @Override
        public void run()
        {
            if (task != null)
            {
                if (onEDT)
                    ThreadUtil.invokeNow(task);
                else
                    task.run();
            }
        }

        /**
         * @return the task
         */
        public Runnable getTask()
        {
            return task;
        }
    }

    protected class CallableAdapter<T> implements Callable<T>
    {
        private final Callable<T> task;
        private final boolean onEDT;

        public CallableAdapter(Callable<T> task, boolean onEDT)
        {
            super();

            this.task = task;
            this.onEDT = onEDT;
        }

        /**
         * @return the task
         */
        public Callable<T> getTask()
        {
            return task;
        }

        @Override
        public T call() throws Exception
        {
            if (task != null)
            {
                if (onEDT)
                    return ThreadUtil.invokeNow(task);

                return task.call();
            }

            return null;
        }
    }

    protected class FutureTaskAdapter<T> extends FutureTask<T>
    {
        private final Runnable runnable;
        private final Callable<T> callable;

        public FutureTaskAdapter(Runnable runnable, T result, boolean onEDT)
        {
            super(new RunnableAdapter(runnable, onEDT), result);

            this.runnable = runnable;
            this.callable = null;
        }

        public FutureTaskAdapter(Runnable runnable, boolean onEDT)
        {
            this(runnable, null, onEDT);
        }

        public FutureTaskAdapter(Callable<T> callable, boolean onEDT)
        {
            super(new CallableAdapter<T>(callable, onEDT));

            this.runnable = null;
            this.callable = callable;
        }

        public Runnable getRunnable()
        {
            return runnable;
        }

        public Callable<T> getCallable()
        {
            return callable;
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
    protected Runnable waitingExecution;
    protected long lastAdd;

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
     * @deprecated Use {@link #removeFirstWaitingTask(Runnable)} instead.
     */
    @Deprecated
    public boolean removeTask(Runnable task)
    {
        return removeFirstWaitingTask(task);
    }

    /**
     * @deprecated Use {@link #submit(Runnable, boolean)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public boolean addTask(Runnable task, boolean onEDT, int id)
    {
        return addTask(task, onEDT);
    }

    /**
     * @deprecated Use {@link #submit(Runnable, boolean)} instead.
     */
    @Deprecated
    public boolean addTask(Runnable task, boolean onEDT)
    {
        try
        {
            submit(task, onEDT);
        }
        catch (RejectedExecutionException E)
        {
            return false;
        }

        return true;
    }

    /**
     * @deprecated Use {@link #submit(Runnable)} instead.
     */
    @Deprecated
    public boolean addTask(Runnable task)
    {
        return addTask(task, false);
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
     * Returns a <tt>RunnableFuture</tt> for the given runnable and default
     * value.
     * 
     * @param runnable
     *        the runnable task being wrapped
     * @param value
     *        the default value for the returned future
     * @param onEDT
     *        if set to <code>true</code> then the <tt>RunnableFuture</tt> will be executed on the
     *        Swing EDT.
     * @return a <tt>RunnableFuture</tt> which when run will run the underlying runnable and which,
     *         as a <tt>Future</tt>, will yield the given value as its result and provide for
     *         cancellation of the underlying task.
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(Runnable runnable, T value, boolean onEDT)
    {
        return new FutureTaskAdapter<T>(runnable, value, onEDT);
    };

    /**
     * Returns a <tt>RunnableFuture</tt> for the given callable task.
     * 
     * @param callable
     *        the callable task being wrapped
     * @param onEDT
     *        if set to <code>true</code> then the <tt>RunnableFuture</tt> will be executed on the
     *        Swing EDT.
     * @return a <tt>RunnableFuture</tt> which when run will call the
     *         underlying callable and which, as a <tt>Future</tt>, will yield
     *         the callable's result as its result and provide for
     *         cancellation of the underlying task.
     * @since 1.6
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(Callable<T> callable, boolean onEDT)
    {
        return new FutureTaskAdapter<T>(callable, onEDT);
    }

    /**
     * Submits a task for execution and returns a Future representing that task. The
     * Future's <tt>get</tt> method will return the given result upon successful completion.
     * 
     * @param task
     *        the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    protected synchronized <T> Future<T> submit(FutureTaskAdapter<T> task)
    {
        if (task == null)
            throw new NullPointerException();

        try
        {
            execute(task);
        }
        catch (RejectedExecutionException e)
        {
            if (!Icy.isExiting())
            {
                // error while adding task
                System.err.println("Cannot add new task, ignore execution : " + task);
                // TODO: may be better to throw the RejectedExecutionException exception
                return null;
            }
        }

        waitingExecution = task;
        return task;
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing that task. The
     * Future's <tt>get</tt> method will return <tt>null</tt> upon <em>successful</em> completion.
     * 
     * @param task
     *        the task to submit
     * @param onEDT
     *        if set to <code>true</code> then the <tt>RunnableFuture</tt> will be executed on the
     *        Swing EDT.
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public Future<?> submit(Runnable task, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, null, onEDT));
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing that task. The
     * Future's <tt>get</tt> method will return the given result upon successful completion.
     * 
     * @param task
     *        the task to submit
     * @param result
     *        the result to return
     * @param onEDT
     *        if set to <code>true</code> then the <tt>RunnableFuture</tt> will be executed on the
     *        Swing EDT.
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(Runnable task, T result, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, result, onEDT));
    }

    /**
     * Submits a value-returning task for execution and returns a Future representing the pending
     * results of the task. The Future's <tt>get</tt> method will return the task's result upon
     * successful completion.
     * <p>
     * If you would like to immediately block waiting for a task, you can use constructions of the
     * form <tt>result = exec.submit(aCallable).get();</tt>
     * <p>
     * Note: The {@link Executors} class includes a set of methods that can convert some other
     * common closure-like objects, for example, {@link java.security.PrivilegedAction} to
     * {@link Callable} form so they can be submitted.
     * 
     * @param task
     *        the task to submit
     * @param onEDT
     *        if set to <code>true</code> then the <tt>RunnableFuture</tt> will be executed on the
     *        Swing EDT.
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(Callable<T> task, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, onEDT));
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        return submit(task, false);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        return submit(task, result, false);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        return submit(task, false);
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
    protected List<FutureTaskAdapter<?>> getWaitingTasks()
    {
        final BlockingQueue<Runnable> q = getQueue();
        final List<FutureTaskAdapter<?>> result = new ArrayList<Processor.FutureTaskAdapter<?>>();

        synchronized (q)
        {
            for (Runnable r : q)
                if (r instanceof FutureTaskAdapter<?>)
                    result.add((FutureTaskAdapter<?>) r);
        }

        return result;
    }

    /**
     * Return waiting tasks for the specified Runnable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(Runnable task)
    {
        final List<FutureTaskAdapter<?>> result = new ArrayList<Processor.FutureTaskAdapter<?>>();

        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getRunnable() == task)
                result.add(f);

        return result;
    }

    /**
     * Return waiting tasks for the specified Callable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(Callable<?> task)
    {
        final List<FutureTaskAdapter<?>> result = new ArrayList<Processor.FutureTaskAdapter<?>>();

        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getCallable() == task)
                result.add(f);

        return result;
    }

    /**
     * Return the number of waiting task
     */
    public int getWaitingTasksCount()
    {
        final int result = getQueue().size();

        // TODO : be sure that waitingExecution pass to null when task has been taken in account.
        // Queue can be empty right after a task submission.
        // For this particular case we return 1 if a task has been submitted
        // and not taken in account with a timeout of 1 second.
        if ((result == 0) && ((waitingExecution != null) && ((System.currentTimeMillis() - lastAdd) < 1000)))
            return 1;

        return result;
    }

    /**
     * @deprecated Not anymore supported.<br>
     *             Use {@link #getWaitingTasksCount(Callable)} or
     *             {@link #getWaitingTasksCount(Runnable)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public int getWaitingTasksCount(int id)
    {
        return 0;
    }

    /**
     * Return the number of task waiting in queue for the specified <tt>Runnable</tt> instance.
     */
    public int getWaitingTasksCount(Runnable task)
    {
        int result = 0;

        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getRunnable() == task)
                result++;

        return result;
    }

    /**
     * Return the number of task waiting in queue for the specified <tt>Callable</tt> instance.
     */
    public int getWaitingTasksCount(Callable<?> task)
    {
        int result = 0;

        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getCallable() == task)
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
     * @deprecated Not anymore supported.<br>
     *             Use {@link #hasWaitingTasks(Callable)} or {@link #hasWaitingTasks(Runnable)}
     *             instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public boolean hasWaitingTasks(int id)
    {
        return false;
    }

    /**
     * Return true if we have at least one task in queue for the specified <tt>Runnable</tt>
     * instance.
     */
    public boolean hasWaitingTasks(Runnable task)
    {
        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getRunnable() == task)
                return true;

        return false;
    }

    /**
     * Return true if we have at least one task in queue for the specified <tt>Callable</tt>
     * instance.
     */
    public boolean hasWaitingTasks(Callable<?> task)
    {
        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.getCallable() == task)
                return true;

        return false;
    }

    /**
     * @deprecated Not anymore supported.<br>
     *             USe {@link #removeFirstWaitingTask(Runnable)} or
     *             {@link #removeFirstWaitingTask(Callable)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public boolean removeFirstWaitingTask(int id)
    {
        return false;
    }

    /**
     * Remove first waiting task for the specified <tt>Runnable</tt> instance.
     */
    public boolean removeFirstWaitingTask(FutureTaskAdapter<?> task)
    {
        if (task == null)
            return false;

        if (task.getCallable() != null)
            return removeFirstWaitingTask(task.getCallable());
        if (task.getRunnable() != null)
            return removeFirstWaitingTask(task.getRunnable());

        return false;
    }

    /**
     * Remove first waiting task for the specified <tt>Runnable</tt> instance.
     */
    public boolean removeFirstWaitingTask(Runnable task)
    {
        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.getRunnable() == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * Remove first waiting task for the specified <tt>Callable</tt> instance.
     */
    public boolean removeFirstWaitingTask(Callable<?> task)
    {
        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.getCallable() == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * @deprecated Not anymore supported.<br>
     *             USe {@link #removeWaitingTasks(Runnable)} or
     *             {@link #removeWaitingTasks(Callable)} instead.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public boolean removeWaitingTasks(int id)
    {
        return false;
    }

    /**
     * Remove all waiting tasks for the specified <tt>Runnable</tt> instance.
     */
    public boolean removeWaitingTasks(Runnable task)
    {
        boolean result = false;

        synchronized (getQueue())
        {
            // remove all tasks of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks(task))
                result |= remove(f);
        }

        return result;
    }

    /**
     * Remove all waiting tasks for the specified <tt>Callable</tt> instance.
     */
    public boolean removeWaitingTasks(Callable<?> task)
    {
        boolean result = false;

        synchronized (getQueue())
        {
            // remove all tasks of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks(task))
                result |= remove(f);
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
     * @deprecated This method is useless.
     */
    @Deprecated
    public void limitWaitingTask(Runnable task, int value)
    {
        synchronized (getQueue())
        {
            final List<FutureTaskAdapter<?>> tasks = getWaitingTasks(task);
            final int numToRemove = tasks.size() - value;

            for (int i = 0; i < numToRemove; i++)
                remove(tasks.get(i));
        }
    }

    /**
     * @deprecated Not anymore supported !
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void limitWaitingTask(int id, int value)
    {
        // not anymore supported
    }

    /**
     * @deprecated This method is useless.
     */
    @Deprecated
    public boolean limitWaitingTask(int value)
    {
        synchronized (getQueue())
        {
            final List<FutureTaskAdapter<?>> tasks = getWaitingTasks();
            final int numToRemove = tasks.size() - value;

            for (int i = 0; i < numToRemove; i++)
                remove(tasks.get(i));
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
    public void fireDoneEvent(FutureTaskAdapter<?> task)
    {
        for (ProcessorEventListener listener : listeners.getListeners(ProcessorEventListener.class))
            listener.processDone(this, task);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);

        // notify we just achieved a process
        fireDoneEvent((FutureTaskAdapter<?>) r);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);

        // ok we can remove reference...
        waitingExecution = null;
    }

}
