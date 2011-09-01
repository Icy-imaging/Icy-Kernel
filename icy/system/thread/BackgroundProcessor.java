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

/**
 * @author Stephane
 */
public class BackgroundProcessor
{
    private static final int DEFAULT_MAX_PROCESSING = SystemUtil.getAvailableProcessors() * 2;
    private static final int DEFAULT_MAX_WAITING = 1024;

    private static final Processor bgProcessor = new Processor(DEFAULT_MAX_WAITING, DEFAULT_MAX_PROCESSING,
            Processor.MIN_PRIORITY);

    /**
     * start background processing of specified Runnable
     * 
     * @param runnable
     * @param onAWTEventThread
     * @return false if queue is full
     */
    public static boolean bgRun(Runnable runnable, boolean onAWTEventThread)
    {
        return bgProcessor.addTask(runnable, onAWTEventThread);
    }

    /**
     * start background processing of specified Runnable
     * 
     * @param runnable
     */
    public static boolean bgRun(Runnable runnable)
    {
        return bgRun(runnable, false);
    }

    /**
     * same as bgRun except it waits until it accepts the new task
     * 
     * @param runnable
     */
    public static void bgRunWait(Runnable runnable)
    {
        if (runnable != null)
        {
            while (!bgRun(runnable, false))
                ThreadUtil.sleep(1);
        }
    }

    /**
     * 
     */
    public static int getActiveCount()
    {
        return bgProcessor.getActiveCount();
    }

    /**
     * 
     */
    public static boolean hasIdleSlots()
    {
        return getActiveCount() < DEFAULT_MAX_PROCESSING;
    }

    /**
     * 
     */
    public static void waitForIdleSlots()
    {
        while (!hasIdleSlots())
            ThreadUtil.sleep(1);
    }

}
