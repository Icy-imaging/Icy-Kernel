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

import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * Thread utilities class.
 * 
 * @author Stephane
 */
public class ThreadUtil
{
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
            instanceProcessors[i] = new InstanceProcessor();
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
     * Returns true if the current thread is an AWT event dispatching thread.
     * As of 1.3 this method is just a cover for <code>java.awt.EventQueue.isDispatchThread()</code>
     * .
     * 
     * @return true if the current thread is an AWT event dispatching thread
     */
    public static boolean isEventDispatchThread()
    {
        return SwingUtilities.isEventDispatchThread();
    }

    /**
     * Invoke "runnable" on the AWT event dispatching thread<br>
     * 
     * @param runnable
     *        the runnable to invoke on AWT event dispatching thread
     * @param wait
     *        wait for invocation before returning, be careful, this can cause dead lock !
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
        try
        {
            SwingUtilities.invokeAndWait(runnable);
        }
        catch (Exception e)
        {
            System.err.println("ThreadUtil.invokeAndWait(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
            e.printStackTrace();
        }
    }

    /**
     * Invoke "runnable" on the AWT event dispatching thread now<br>
     * Wait until all events completion. Be careful, this can cause dead lock !
     */
    public static void invokeNow(Runnable runnable)
    {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else
        {
            try
            {
                SwingUtilities.invokeAndWait(runnable);
            }
            catch (Exception e)
            {
                System.err.println("ThreadUtil.invokeAndWait(...) error :");
                IcyExceptionHandler.showErrorMessage(e, true);
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoke "runnable" on the AWT event dispatching thread.<br>
     * If you are in the AWT event dispatching thread the runnable is executed immediately.
     */
    public static void invokeLater(Runnable runnable)
    {
        invokeLater(runnable, false);
    }

    /**
     * Invoke "runnable" on the AWT event dispatching thread.<br>
     * If you are in the AWT event dispatching thread the runnable can be executed immediately<br>
     * depending the value of <code>forceLater</code> parameter.
     * 
     * @param forceLater
     *        Force execution of runnable to be delayed<br>
     *        even if we are on the AWT event dispatching thread
     */
    public static void invokeLater(Runnable runnable, boolean forceLater)
    {
        if (SwingUtilities.isEventDispatchThread() && !forceLater)
            runnable.run();
        else
            SwingUtilities.invokeLater(runnable);
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
     * Return false if queue is full.
     */
    public static boolean bgRun(Runnable runnable, boolean onEventThread)
    {
        return bgProcessor.addTask(runnable, onEventThread);
    }

    /**
     * Add background processing (low priority) of specified Runnable.<br>
     * Return false if queue is full.
     */
    public static boolean bgRun(Runnable runnable)
    {
        return bgRun(runnable, false);
    }

    /**
     * Same as bgRun except it waits until it accepts the specified task.
     */
    public static void bgRunWait(Runnable runnable)
    {
        while (!bgRun(runnable, false))
            ThreadUtil.sleep(1);
    }

    /**
     * Add single background processing (normal priority) of specified Runnable.<br>
     * If this <code>runnable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return false if queue is full.
     */
    public static boolean bgRunSingle(Runnable runnable, boolean onEventThread)
    {
        final InstanceProcessor p = getInstanceProcessor(runnable);

        if (p.hasWaitingTasks(runnable))
            return false;

        return p.addTask(runnable, onEventThread);
    }

    /**
     * Add single background processing (normal priority) of specified Runnable.<br>
     * If this <code>runnable</code> instance is already pending in waiting background process<br>
     * then nothing is done.<br>
     * Return false if queue is full.
     */
    public static boolean bgRunSingle(Runnable runnable)
    {
        return bgRunSingle(runnable, false);
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
     * Return true if the specified runnable is waiting to be processed in background processing.
     */
    public static boolean hasWaitingBgTask(Runnable runnable)
    {
        return bgProcessor.getWaitingTasksCount(runnable) > 0;
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
            // ignore
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
            // ignore
        }
    }
}
