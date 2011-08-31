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

import icy.system.IcyExceptionHandler;

import javax.swing.SwingUtilities;

/**
 * @author stephane
 */
public class ThreadUtil
{
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
     * Invoke "runnable" on the AWT<br>
     * 
     * @param runnable
     *        the runnable to invoke on AWT
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
     * Invoke "runnable" on the AWT now, wait until all events completion<br>
     * Be careful, this can cause dead lock !
     */
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
     * Invoke "runnable" on the AWT now, wait until completion<br>
     * Be careful, this can cause dead lock !
     */
    public static void invokeNow(Runnable runnable)
    {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else
            invokeAndWait(runnable);
    }

    /**
     * Invoke "runnable" on the AWT later or now if we are in the AWT.<br>
     * Return immediatly
     */
    public static void invokeLater(Runnable runnable)
    {
        invokeLater(runnable, false);
    }

    /**
     * Invoke "runnable" on the AWT later or now if we are in the AWT.<br>
     * If 'forceLater' is true then invocation is delayed even if we are on the AWT.<br>
     * Return immediatly
     */
    public static void invokeLater(Runnable runnable, boolean forceLater)
    {
        if (SwingUtilities.isEventDispatchThread() && !forceLater)
            runnable.run();
        else
            SwingUtilities.invokeLater(runnable);
    }

    /**
     * start background processing of specified Runnable
     * 
     * @param runnable
     * @param onAWTEventThread
     */
    public static boolean bgRun(Runnable runnable, boolean onAWTEventThread)
    {
        return BackgroundProcessor.bgRun(runnable, onAWTEventThread);
    }

    /**
     * start background processing of specified Runnable
     * 
     * @param runnable
     */
    public static boolean bgRun(Runnable runnable)
    {
        return BackgroundProcessor.bgRun(runnable);
    }

    /**
     * same as bgRun except it waits until it accepts the specified task
     * 
     * @param runnable
     */
    public static void bgRunWait(Runnable runnable)
    {
        BackgroundProcessor.bgRunWait(runnable);
    }

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
