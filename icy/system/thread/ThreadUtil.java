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

    private static final Processor bgProcessor = new Processor(Processor.DEFAULT_MAX_WAITING,
            Processor.DEFAULT_MAX_PROCESSING, Processor.MIN_PRIORITY);
    private static final InstanceProcessor[] instanceProcessors = new InstanceProcessor[Processor.DEFAULT_MAX_PROCESSING];

    static
    {
        bgProcessor.setDefaultThreadName("Background processor");
        bgProcessor.setKeepAliveTime(3, TimeUnit.SECONDS);

        // instance processors initialization
        for (int i = 0; i < instanceProcessors.length; i++)
        {
            instanceProcessors[i] = new InstanceProcessor(Processor.DEFAULT_MAX_WAITING, Processor.NORM_PRIORITY);
            instanceProcessors[i].setDefaultThreadName("Background single processor " + (i + 1));
            // we want the processor to stay alive
            instanceProcessors[i].setKeepAliveTime(1, TimeUnit.DAYS);
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
            // headless mode ?
            if (Icy.getMainInterface().isHeadLess())
            {
                // just run the code now and hope that graphic part is headless safe ^^
                runnable.run();
                
                // IcyExceptionHandler.showErrorMessage(new HeadlessException(
                // "Cannot use invokeNow(..) in headless mode (EDT do not exist) !"), true);
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
                    IcyExceptionHandler.handleException(e, true);
                }
                catch (Exception e)
                {
                    // probably an interrupt exception here
                    System.err.println("ThreadUtil.invokeNow(...) error :");
                    IcyExceptionHandler.showErrorMessage(e, true);
                }
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
            SwingUtilities.invokeLater(r);
    }

    /**
     * Shutdown all background runner.
     */
    public static void shutdown()
    {
        bgProcessor.shutdown();
        for (InstanceProcessor ip : instanceProcessors)
            ip.shutdown();
    }

    /**
     * Return true if all background runner are shutdown and terminated.
     */
    public static boolean isShutdownAndTerminated()
    {
        for (InstanceProcessor ip : instanceProcessors)
            if (!ip.isTerminated())
                return false;

        return bgProcessor.isTerminated();
    }

    /**
     * Add background processing (low priority) of specified Runnable.<br>
     * Return <code>false</code> if background process queue is full.
     */
    public static boolean bgRun(Runnable runnable, boolean onEDT)
    {
        return (bgProcessor.submit(runnable, onEDT) != null);
    }

    /**
     * Add background processing (low priority) of specified Runnable.<br>
     * Return <code>false</code> if background process queue is full.
     */
    public static boolean bgRun(Runnable runnable)
    {
        return bgRun(runnable, false);
    }

    /**
     * @deprecated Use {@link #bgRun(Runnable)} instead and check for acceptance.
     */
    @Deprecated
    public static void bgRunWait(Runnable runnable)
    {
        while (!bgRun(runnable, false))
            ThreadUtil.sleep(1);
    }

    /**
     * Add single background processing (normal priority) of specified Runnable.<br>
     * If this <code>runnable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return <code>false</code> if background process queue is full.
     */
    public static boolean bgRunSingle(Runnable runnable, boolean onEDT)
    {
        final InstanceProcessor p = getInstanceProcessor(runnable);

        if (p.hasWaitingTasks(runnable))
            return false;

        return (p.submit(runnable, onEDT) != null);
    }

    /**
     * Add single background processing (normal priority) of specified Runnable.<br>
     * If this <code>runnable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return <code>false</code> if background process queue is full.
     */
    public static boolean bgRunSingle(Runnable runnable)
    {
        return bgRunSingle(runnable, false);
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
     *        are on the Swing EDT.
     */
    public static <T> Future<T> invokeLater(Callable<T> callable, boolean forceLater) throws Exception
    {
        final FutureTask<T> task = new FutureTask<T>(callable);
        invokeLater(task, forceLater);
        return task;
    }

    /**
     * Add background processing (low priority) of specified Callable task.<br>
     * Return a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.
     */
    public static <T> Future<T> bgRun(Callable<T> callable, boolean onEDT)
    {
        return bgProcessor.submit(callable, onEDT);
    }

    /**
     * Add background processing (low priority) of specified Callable task.<br>
     * Return a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.
     */
    public static <T> Future<T> bgRun(Callable<T> callable)
    {
        return bgRun(callable, false);
    }

    /**
     * Add single background processing (normal priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.
     */
    public static <T> Future<T> bgRunSingle(Callable<T> callable, boolean onEDT)
    {
        final InstanceProcessor p = getInstanceProcessor(callable);

        if (p.hasWaitingTasks(callable))
            return null;

        return p.submit(callable, onEDT);
    }

    /**
     * Add single background processing (normal priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.
     */
    public static <T> Future<T> bgRunSingle(Callable<T> callable)
    {
        return bgRunSingle(callable, false);
    }

    /**
     * Retrieve the instance processor to use for specified runnable.
     */
    private static InstanceProcessor getInstanceProcessor(Runnable runnable)
    {
        // get processor index from the hash code
        return instanceProcessors[runnable.hashCode() % instanceProcessors.length];
    }

    /**
     * Retrieve the instance processor to use for specified callable.
     */
    private static InstanceProcessor getInstanceProcessor(Callable<?> callable)
    {
        // get processor index from the hash code
        return instanceProcessors[callable.hashCode() % instanceProcessors.length];
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
     * in single scheme background processing.
     */
    public static boolean hasWaitingBgSingleTask(Runnable runnable)
    {
        return getInstanceProcessor(runnable).hasWaitingTasks(runnable);
    }

    /**
     * Return true if the specified callable is waiting to be processed<br>
     * in single scheme background processing.
     */
    public static boolean hasWaitingBgSingleTask(Callable<?> callable)
    {
        return getInstanceProcessor(callable).hasWaitingTasks(callable);
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
