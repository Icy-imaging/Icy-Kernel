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

import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * Thread utilities class.
 * 
 * @author Stephane
 */
public class ThreadUtil
{
    /**
     * This class is used to catch exception in the EDT.
     */
    public static class CaughtRunnable implements Runnable
    {
        private final Runnable runnable;

        public CaughtRunnable(Runnable runnable)
        {
            super();

            this.runnable = runnable;
        }

        @Override
        public void run()
        {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                IcyExceptionHandler.handleException(t, true);
            }
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

    // low priority background processor
    private static final Processor bgProcessor;
    // single Runnable / Callable instance processor
    private static final InstanceProcessor instanceProcessors[];
    // low priority single Runnable / Callable instance processor
    private static final InstanceProcessor bgInstanceProcessors[];

    static
    {
        if (SystemUtil.is32bits())
        {
            int wantedThread = SystemUtil.getAvailableProcessors() * 2;
            wantedThread = Math.max(wantedThread, 2);

            // 32 bits JVM, limit the number of thread
            bgProcessor = new Processor(Math.min(wantedThread, 8));
            instanceProcessors = new InstanceProcessor[Math.min(wantedThread, 4)];
            bgInstanceProcessors = new InstanceProcessor[Math.min(wantedThread, 4)];
        }
        else
        {
            int wantedThread = SystemUtil.getAvailableProcessors() * 2;
            wantedThread = Math.max(wantedThread, 4);

            // 64 bits JVM, can have higher limit
            bgProcessor = new Processor(Math.min(wantedThread, 16));
            instanceProcessors = new InstanceProcessor[Math.min(wantedThread, 8)];
            bgInstanceProcessors = new InstanceProcessor[Math.min(wantedThread, 8)];
        }

        bgProcessor.setPriority(MIN_PRIORITY);
        bgProcessor.setDefaultThreadName("Background processor");
        bgProcessor.setKeepAliveTime(3, TimeUnit.SECONDS);

        for (int i = 0; i < instanceProcessors.length; i++)
        {
            // keep these thread active
            instanceProcessors[i] = new InstanceProcessor(NORM_PRIORITY);
            instanceProcessors[i].setDefaultThreadName("Instance processor (normal priority)");
            instanceProcessors[i].setKeepAliveTime(3, TimeUnit.SECONDS);
            bgInstanceProcessors[i] = new InstanceProcessor(MIN_PRIORITY);
            bgInstanceProcessors[i].setDefaultThreadName("Instance processor (low priority)");
            bgInstanceProcessors[i].setKeepAliveTime(3, TimeUnit.SECONDS);
        }
    }

    /**
     * @return true if the current thread is an AWT event dispatching thread.
     */
    public static boolean isEventDispatchThread()
    {
        return EventQueue.isDispatchThread();
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * Any exception is automatically caught by Icy exception handler.
     * 
     * @param wait
     *        If set to true, the method wait until completion, in this case you have to take
     *        attention to not cause any dead lock.
     * @see #invokeLater(Runnable)
     * @see #invokeNow(Runnable)
     */
    public static void invoke(Runnable runnable, boolean wait)
    {
        if (wait)
            invokeNow(runnable);
        else
            invokeLater(runnable);
    }

    /**
     * @deprecated Use {@link #invokeNow(Runnable)} instead
     */
    @Deprecated
    public static void invokeAndWait(Runnable runnable)
    {
        invokeNow(runnable);
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread now.<br>
     * Wait until completion. Any exception is automatically caught by Icy exception handler.<br>
     * Be careful, using this method may lead to dead lock !
     */
    public static void invokeNow(Runnable runnable)
    {
        if (isEventDispatchThread())
        {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                // the runnable thrown an exception
                IcyExceptionHandler.handleException(t, true);
            }
        }
        else
        {
            try
            {
                EventQueue.invokeAndWait(runnable);
            }
            catch (InvocationTargetException e)
            {
                // the runnable thrown an exception
                IcyExceptionHandler.handleException(e.getTargetException(), true);
            }
            catch (Exception e)
            {
                // probably an interrupt exception here
                System.err.println("ThreadUtil.invokeNow(...) error :");
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * If we already are on the EDT the <code>Runnable</code> is executed immediately else it will
     * be executed later.
     * 
     * @see #invokeLater(Runnable, boolean)
     */
    public static void invokeLater(Runnable runnable)
    {
        invokeLater(runnable, false);
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * Depending the <code>forceLater</code> parameter the <code>Runnable</code> can be executed
     * immediately if we are on the EDT.
     * 
     * @param forceLater
     *        If <code>true</code> the <code>Runnable</code> is forced to execute later even if we
     *        are on the Swing EDT.
     */
    public static void invokeLater(Runnable runnable, boolean forceLater)
    {
        final Runnable r = new CaughtRunnable(runnable);

        if ((!forceLater) && isEventDispatchThread())
            r.run();
        else
            EventQueue.invokeLater(r);
    }

    /**
     * Shutdown all background runner.
     */
    public static void shutdown()
    {
        bgProcessor.shutdown();
        for (int i = 0; i < instanceProcessors.length; i++)
        {
            instanceProcessors[i].shutdown();
            bgInstanceProcessors[i].shutdown();
        }
    }

    /**
     * Return true if all background runner are shutdown and terminated.
     */
    public static boolean isShutdownAndTerminated()
    {
        for (int i = 0; i < instanceProcessors.length; i++)
        {
            if (!instanceProcessors[i].isTerminated())
                return false;
            if (!bgInstanceProcessors[i].isTerminated())
                return false;
        }
        return bgProcessor.isTerminated();
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread now.<br>
     * Wait until completion. Be careful, using this method may lead to dead lock !
     * 
     * @throws ExecutionException
     *         if the computation threw an exception
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting
     * @throws Exception
     *         if the computation threw an exception and the calling thread is the EDT
     */
    public static <T> T invokeNow(Callable<T> callable) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread())
            return callable.call();

        final FutureTask<T> task = new FutureTask<T>(callable);
        invokeNow(task);
        return task.get();
    }

    /**
     * Invoke "runnable" on the AWT event dispatching thread.<br>
     * Depending the <code>forceLater</code> parameter the <code>Callable</code> can be executed
     * immediately if we are on the EDT.
     * 
     * @param forceLater
     *        If <code>true</code> the <code>Callable</code> is forced to execute later even if we
     *        are on the EDT.
     */
    public static <T> Future<T> invokeLater(Callable<T> callable, boolean forceLater) throws Exception
    {
        final FutureTask<T> task = new FutureTask<T>(callable);
        invokeLater(task, forceLater);
        return task;
    }

    /**
     * Retrieve the instance processor (normal priority) to use for specified runnable.
     */
    private static InstanceProcessor getInstanceProcessor(Runnable runnable)
    {
        // get processor index from the hash code
        return instanceProcessors[runnable.hashCode() % instanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (normal priority) to use for specified callable.
     */
    private static InstanceProcessor getInstanceProcessor(Callable<?> callable)
    {
        // get processor index from the hash code
        return instanceProcessors[callable.hashCode() % instanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (low priority) to use for specified runnable.
     */
    private static InstanceProcessor getBgInstanceProcessor(Runnable runnable)
    {
        // get processor index from the hash code
        return bgInstanceProcessors[runnable.hashCode() % bgInstanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (low priority) to use for specified callable.
     */
    private static InstanceProcessor getBgInstanceProcessor(Callable<?> callable)
    {
        // get processor index from the hash code
        return bgInstanceProcessors[callable.hashCode() % bgInstanceProcessors.length];
    }

    /**
     * @deprecated Use {@link #bgRun(Runnable)} instead and {@link #invokeNow(Runnable)} separately.
     * @see #bgRun(Runnable)
     */
    @Deprecated
    public static boolean bgRun(Runnable runnable, boolean onEDT)
    {
        return (bgProcessor.submit(runnable, onEDT) != null);
    }

    /**
     * @deprecated Use {@link #bgRun(Runnable)} instead and check for acceptance.
     */
    @Deprecated
    public static void bgRunWait(Runnable runnable)
    {
        while (!bgRun(runnable))
            ThreadUtil.sleep(1);
    }

    /**
     * Adds background processing (low priority) of specified Runnable.<br>
     * Returns <code>false</code> if background process queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static boolean bgRun(Runnable runnable)
    {
        return (bgProcessor.submit(runnable) != null);
    }

    /**
     * @deprecated Use {@link #bgRun(Callable)} instead and {@link #invokeNow(Callable)} separately.
     * @see #bgRun(Callable)
     */
    @Deprecated
    public static <T> Future<T> bgRun(Callable<T> callable, boolean onEDT)
    {
        return bgProcessor.submit(callable, onEDT);
    }

    /**
     * Adds background processing (low priority) of specified Callable task.<br>
     * Returns a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static <T> Future<T> bgRun(Callable<T> callable)
    {
        return bgProcessor.submit(callable);
    }

    /**
     * @deprecated Use {@link #runSingle(Runnable)} instead and {@link #invokeNow(Runnable)}
     *             separately.
     * @see #bgRunSingle(Runnable)
     */
    @Deprecated
    public static boolean bgRunSingle(Runnable runnable, boolean onEDT)
    {
        final InstanceProcessor processor = getInstanceProcessor(runnable);

        if (processor.hasWaitingTasks(runnable))
            return false;

        return (processor.submit(runnable, onEDT) != null);
    }

    /**
     * @deprecated Use {@link #runSingle(Callable)} instead and {@link #invokeNow(Callable)}
     *             separately.
     * @see #bgRunSingle(Callable)
     */
    @Deprecated
    public static <T> Future<T> bgRunSingle(Callable<T> callable, boolean onEDT)
    {
        final InstanceProcessor processor = getInstanceProcessor(callable);

        if (processor.hasWaitingTasks(callable))
            return null;

        return processor.submit(callable, onEDT);
    }

    /**
     * Adds single processing (low priority) of specified Runnable.<br>
     * If this <code>Runnable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * Returns <code>false</code> if single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static boolean bgRunSingle(Runnable runnable)
    {
        final InstanceProcessor processor = getBgInstanceProcessor(runnable);

        if (processor.hasWaitingTasks(runnable))
            return false;

        return (processor.submit(runnable) != null);
    }

    /**
     * Adds single processing (low priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * Returns a Future representing the pending result of the task or <code>null</code> if
     * single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static <T> Future<T> bgRunSingle(Callable<T> callable)
    {
        final InstanceProcessor processor = getBgInstanceProcessor(callable);

        if (processor.hasWaitingTasks(callable))
            return null;

        return processor.submit(callable);
    }

    /**
     * Add single processing (normal priority) of specified Runnable.<br>
     * If this <code>Runnable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * Return <code>false</code> if single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static boolean runSingle(Runnable runnable)
    {
        final InstanceProcessor processor = getInstanceProcessor(runnable);

        if (processor.hasWaitingTasks(runnable))
            return false;

        return (processor.submit(runnable) != null);
    }

    /**
     * Add single processing (normal priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * Return a Future representing the pending result of the task or <code>null</code> if
     * single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     */
    public static <T> Future<T> runSingle(Callable<T> callable)
    {
        final InstanceProcessor processor = getInstanceProcessor(callable);

        if (processor.hasWaitingTasks(callable))
            return null;

        return processor.submit(callable);
    }

    /**
     * Return true if the specified runnable is waiting to be processed in background processing.
     */
    public static boolean hasWaitingBgTask(Runnable runnable)
    {
        return bgProcessor.getWaitingTasksCount(runnable) > 0;
    }

    /**
     * Return true if the specified callable is waiting to be processed in background processing.
     */
    public static boolean hasWaitingBgTask(Callable<?> callable)
    {
        return bgProcessor.getWaitingTasksCount(callable) > 0;
    }

    /**
     * Return true if the specified runnable is waiting to be processed<br>
     * in single scheme background processing (low priority).
     */
    public static boolean hasWaitingBgSingleTask(Runnable runnable)
    {
        final InstanceProcessor processor = getBgInstanceProcessor(runnable);
        return processor.hasWaitingTasks(runnable);
    }

    /**
     * Return true if the specified callable is waiting to be processed<br>
     * in single scheme background processing (low priority).
     */
    public static boolean hasWaitingBgSingleTask(Callable<?> callable)
    {
        final InstanceProcessor processor = getBgInstanceProcessor(callable);
        return processor.hasWaitingTasks(callable);
    }

    /**
     * Return true if the specified runnable is waiting to be processed<br>
     * in single scheme background processing (normal priority).
     */
    public static boolean hasWaitingSingleTask(Runnable runnable)
    {
        final InstanceProcessor processor = getInstanceProcessor(runnable);
        return processor.hasWaitingTasks(runnable);
    }

    /**
     * Return true if the specified callable is waiting to be processed<br>
     * in single scheme background processing (normal priority).
     */
    public static boolean hasWaitingSingleTask(Callable<?> callable)
    {
        final InstanceProcessor processor = getInstanceProcessor(callable);
        return processor.hasWaitingTasks(callable);
    }

    /**
     * Return the number of active background tasks.
     */
    public static int getActiveBgTaskCount()
    {
        return bgProcessor.getActiveCount();
    }

    /**
     * Same as {@link Thread#sleep(long)} except Exception is caught and ignored.
     */
    public static void sleep(long milli)
    {
        try
        {
            Thread.sleep(milli);
        }
        catch (InterruptedException e)
        {
            // have to interrupt the thread
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Same as {@link Thread#sleep(long)} except Exception is caught and ignored.
     */
    public static void sleep(int milli)
    {
        try
        {
            Thread.sleep(milli);
        }
        catch (InterruptedException e)
        {
            // have to interrupt the thread
            Thread.currentThread().interrupt();
        }
    }
}
