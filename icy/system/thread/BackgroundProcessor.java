/*
 * Copyright 2010-2015 Institut Pasteur.
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

/**
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class BackgroundProcessor
{
    /**
     * @deprecated Use {@link ThreadUtil#bgRun(Runnable, boolean)} instead
     */
    @Deprecated
    public static boolean bgRun(Runnable runnable, boolean onEventThread)
    {
        return ThreadUtil.bgRun(runnable, onEventThread);
    }

    /**
     * @deprecated Use {@link ThreadUtil#bgRun(Runnable)} instead
     */
    @Deprecated
    public static boolean bgRun(Runnable runnable)
    {
        return ThreadUtil.bgRun(runnable);
    }

    /**
     * @deprecated Use {@link ThreadUtil#bgRunWait(Runnable)} instead
     */
    @Deprecated
    public static void bgRunWait(Runnable runnable)
    {
        ThreadUtil.bgRunWait(runnable);
    }

    /**
     * @deprecated Use {@link ThreadUtil#getActiveBgTaskCount()} instead
     */
    @Deprecated
    public static int getActiveCount()
    {
        return ThreadUtil.getActiveBgTaskCount();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean hasIdleSlots()
    {
        return true;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void waitForIdleSlots()
    {
    }
}
