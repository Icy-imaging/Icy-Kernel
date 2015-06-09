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
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Processor class.<br>
 * Allow you to queue and execute tasks on a defined set of thread.
 * 
 * @author stephane
 */
public class Processor extends ThreadPoolExecutor
{
    public static final int DEFAULT_MAX_WAITING = 1024;
    public static final int DEFAULT_MAX_PROCESSING = SystemUtil.getNumberOfCPUs();

    /**
     * @deprecated Useless interface
     */
    @Deprecated
    public interface ProcessorEventListener extends EventListener
    {
        public void processDone(Processor source, Runnable runnable);
    }

    protected class ProcessorThreadFactory implements ThreadFactory
    {
        String name;
        int threadCount;
        final boolean showNumber;

        public ProcessorThreadFactory(String name, boolean showNumber)
        {
            super();

            setName(name);
            threadCount = 0;
            this.showNumber = showNumber;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String value)
        {
            this.name = value;
        }

        String getThreadName()
        {
            String result = name;

            if (showNumber)
                result += " (thread #" + threadCount + ")";

            return result;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            final Thread result = new Thread(r, getThreadName());

            result.setPriority(priority);

            return result;
        }
    }

    protected class ProcessorRejectedExecutionHandler implements RejectedExecutionHandler
    {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
        {
            // ignore if we try to submit process while Icy is exiting
            if (!Icy.isExiting())
                throw new RejectedExecutionException("Cannot add new task, ignore execution of " + r);
        }
    }

    protected class FutureTaskAdapter<T> extends FutureTask<T>
    {
        public Runnable runnable;
        public Callable<T> callable;
        final boolean handleException;

        public FutureTaskAdapter(Runnable runnable, T result, boolean handleException)
        {
            super(runnable, result);

            this.runnable = runnable;
            this.callable = null;
            this.handleException = handleException;
        }

        public FutureTaskAdapter(Runnable runnable, boolean handleException)
        {
            this(runnable, null, handleException);
        }

        public FutureTaskAdapter(Callable<T> callable, boolean handleException)
        {
            super(callable);

            this.runnable = null;
            this.callable = callable;
            this.handleException = handleException;
        }

        @Override
        protected void done()
        {
            super.done();

            if (handleException)
            {
                try
                {
                    get();
                }
                catch (Exception e)
                {
                    IcyExceptionHandler.handleException(e.getCause(), true);
                }
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
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

    /**
     * @deprecated
     */
    @Deprecated
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

    /**
     * @deprecated
     */
    @Deprecated
    protected class FutureTaskAdapterEDT<T> extends FutureTaskAdapter<T>
    {
        public FutureTaskAdapterEDT(Runnable runnable, T result, boolean onEDT)
        {
            super(new RunnableAdapter(runnable, onEDT), result, true);

            // assign the original runnable
            this.runnable = runnable;
            this.callable = null;
        }

        public FutureTaskAdapterEDT(Runnable runnable, boolean onEDT)
        {
            this(runnable, null, onEDT);
        }

        public FutureTaskAdapterEDT(Callable<T> callable, boolean onEDT)
        {
            super(new CallableAdapter<T>(callable, onEDT), true);

            // assign the original callable
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

    /**
     * internal
     */
    protected Runnable waitingExecution;
    protected long lastAdd;

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.<br>
     * 
     * @param maxWaiting
     *        The length of waiting queue.
     * @param numThread
     *        The maximum number of processing thread.
     * @param priority
     *        Processor priority<br>
     *        <code>Processor.MIN_PRIORITY</code><br>
     *        <code>Processor.NORM_PRIORITY</code><br>
     *        <code>Processor.MAX_PRIORITY</code>
     */
    public Processor(int maxWaiting, int numThread, int priority)
    {
        super(numThread, numThread, 2L, TimeUnit.SECONDS, (maxWaiting == -1) ? new LinkedBlockingQueue<Runnable>()
                : new LinkedBlockingQueue<Runnable>(maxWaiting));

        setThreadFactory(new ProcessorThreadFactory("Processor", numThread > 1));
        setRejectedExecutionHandler(new ProcessorRejectedExecutionHandler());
        allowCoreThreadTimeOut(true);

        this.priority = priority;

        waitingExecution = null;
    }

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.
     * 
     * @param maxWaiting
     *        The length of waiting queue.
     * @param numThread
     *        The maximum number of processing thread.
     */
    public Processor(int maxWaiting, int numThread)
    {
        this(maxWaiting, numThread, NORM_PRIORITY);
    }

    /**
     * Create a new Processor with specified number of processing thread.
     * 
     * @param numThread
     *        The maximum number of processing thread.
     */
    public Processor(int numThread)
    {
        this(-1, numThread, NORM_PRIORITY);
    }

    /**
     * Create a new Processor with specified number of processing thread.
     * 
     * @param threadName
     *        The name of the threads (useful for debugging purposes)
     * @param numThread
     *        The maximum number of processing thread.
     */
    public Processor(String threadName, int numThread)
    {
        this(-1, numThread, NORM_PRIORITY);
        setThreadName(threadName);
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
     * @deprecated Use {@link #submit(Runnable)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public boolean addTask(Runnable task, boolean onEDT, int id)
    {
        return addTask(task, onEDT);
    }

    /**
     * @deprecated Use {@link #submit(Runnable)} instead.
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
     * @deprecated Use {@link #newTaskFor(Runnable, Object)} instead.
     */
    @Deprecated
    protected <T> FutureTaskAdapter<T> newTaskFor(Runnable runnable, T value, boolean onEDT)
    {
        return new FutureTaskAdapterEDT<T>(runnable, value, onEDT);
    };

    /**
     * @deprecated Use {@link #newTaskFor(Callable)} instead.
     */
    @Deprecated
    protected <T> FutureTaskAdapter<T> newTaskFor(Callable<T> callable, boolean onEDT)
    {
        return new FutureTaskAdapterEDT<T>(callable, onEDT);
    }

    /**
     * @param handledException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param runnable
     *        the runnable task being wrapped
     * @param value
     *        the default value for the returned future
     * @return a <tt>RunnableFuture</tt> which when run will run the underlying runnable and which,
     *         as a <tt>Future</tt>, will yield the given value as its result and provide for
     *         cancellation of the underlying task.
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(boolean handledException, Runnable runnable, T value)
    {
        return new FutureTaskAdapter<T>(runnable, value, handledException);
    };

    /**
     * @param handledException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param callable
     *        the callable task being wrapped
     * @return a <tt>RunnableFuture</tt> which when run will call the
     *         underlying callable and which, as a <tt>Future</tt>, will yield
     *         the callable's result as its result and provide for
     *         cancellation of the underlying task.
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(boolean handledException, Callable<T> callable)
    {
        return new FutureTaskAdapter<T>(callable, handledException);
    }

    @Override
    public void execute(Runnable task)
    {
        super.execute(task);
        // save the last executed task
        waitingExecution = task;
    }

    /**
     * Submit the given task (internal use only).
     */
    protected synchronized <T> FutureTask<T> submit(FutureTaskAdapter<T> task)
    {
        execute(task);
        return task;
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(false, task, null));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(false, task, result));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(false, task));
    }

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's <tt>get</tt> method will
     * return <tt>null</tt> upon <em>successful</em> completion.
     * 
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public Future<?> submit(boolean handleException, Runnable task)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task, null));
    }

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's <tt>get</tt> method will
     * return the given result upon successful completion.
     * 
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @param result
     *        the result to return
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(boolean handleException, Runnable task, T result)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task, result));
    }

    /**
     * Submits a value-returning task for execution and returns a
     * Future representing the pending results of the task. The
     * Future's <tt>get</tt> method will return the task's result upon
     * successful completion.
     * <p>
     * If you would like to immediately block waiting for a task, you can use constructions of the
     * form <tt>result = exec.submit(aCallable).get();</tt>
     * <p>
     * Note: The {@link Executors} class includes a set of methods that can convert some other
     * common closure-like objects, for example, {@link java.security.PrivilegedAction} to
     * {@link Callable} form so they can be submitted.
     * 
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(boolean handleException, Callable<T> task)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task));
    }

    /**
     * @deprecated Use {@link #submit(Runnable)} instead and ThreadUtil.invokeNow(..) where you need
     *             it.
     */
    @Deprecated
    public Future<?> submit(Runnable task, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, null, onEDT));
    }

    /**
     * @deprecated Use {@link #submit(Runnable, Object)} instead and ThreadUtil.invokeNow(..) where
     *             you
     *             need it.
     */
    @Deprecated
    public <T> Future<T> submit(Runnable task, T result, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, result, onEDT));
    }

    /**
     * @deprecated Use {@link #submit(Callable)} instead and ThreadUtil.invokeNow(..) where you need
     *             it.
     */
    @Deprecated
    public <T> Future<T> submit(Callable<T> task, boolean onEDT)
    {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(task, onEDT));
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

    /**
     * @deprecated Use {@link #getThreadName()} instead
     */
    @Deprecated
    public String getDefaultThreadName()
    {
        return ((ProcessorThreadFactory) getThreadFactory()).getName();
    }

    /**
     * @deprecated Use {@link #setThreadName(String)} instead
     */
    @Deprecated
    public void setDefaultThreadName(String defaultThreadName)
    {
        ((ProcessorThreadFactory) getThreadFactory()).setName(defaultThreadName);
    }

    /**
     * Return the thread name.
     */
    public String getThreadName()
    {
        return ((ProcessorThreadFactory) getThreadFactory()).getName();
    }

    /**
     * Set the wanted thread name.
     */
    public void setThreadName(String defaultThreadName)
    {
        ((ProcessorThreadFactory) getThreadFactory()).setName(defaultThreadName);
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
        final List<FutureTaskAdapter<?>> result = new ArrayList<FutureTaskAdapter<?>>();

        synchronized (q)
        {
            for (Runnable r : q)
                result.add((FutureTaskAdapter<?>) r);
        }

        return result;
    }

    /**
     * Return waiting tasks for the specified Runnable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(Runnable task)
    {
        final List<FutureTaskAdapter<?>> result = new ArrayList<FutureTaskAdapter<?>>();

        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.runnable == task)
                result.add(f);

        return result;
    }

    /**
     * Return waiting tasks for the specified Callable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(Callable<?> task)
    {
        final List<FutureTaskAdapter<?>> result = new ArrayList<FutureTaskAdapter<?>>();

        // scan all tasks
        for (FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.callable == task)
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
            if (f.runnable == task)
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
            if (f.callable == task)
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
            if (f.runnable == task)
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
            if (f.callable == task)
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
     * Remove first waiting task for the specified <tt>FutureTaskAdapter</tt> instance.
     */
    protected boolean removeFirstWaitingTask(FutureTaskAdapter<?> task)
    {
        if (task == null)
            return false;

        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks())
                if (f == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * Remove first waiting task for the specified <tt>Runnable</tt> instance.
     */
    public boolean removeFirstWaitingTask(Runnable task)
    {
        if (task == null)
            return false;

        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.runnable == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * Remove first waiting task for the specified <tt>Callable</tt> instance.
     */
    public boolean removeFirstWaitingTask(Callable<?> task)
    {
        if (task == null)
            return false;

        synchronized (getQueue())
        {
            // remove first task of specified instance
            for (FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.callable == task)
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
     * @deprecated Useless...
     */
    @Deprecated
    public void addListener(ProcessorEventListener listener)
    {

    }

    /**
     * @deprecated Useless...
     */
    @Deprecated
    public void removeListener(ProcessorEventListener listener)
    {

    }

    /**
     * @deprecated useless
     */
    @Deprecated
    public void fireDoneEvent(FutureTaskAdapter<?> task)
    {

    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);

        // ok we can remove reference...
        waitingExecution = null;
    }
}
