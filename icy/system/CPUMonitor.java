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
package icy.system;

/**
 * Uses system.profile.CPUMonitor instead
 * 
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class CPUMonitor
{
    private final icy.system.profile.CPUMonitor mon;

    public CPUMonitor()
    {
        mon = new icy.system.profile.CPUMonitor();
    }

    public CPUMonitor(int type)
    {
        mon = new icy.system.profile.CPUMonitor(type);
    }

    public void start() throws IllegalAccessError
    {
        mon.start();
    }

    public void stop()
    {
        mon.stop();
    }

    public long getCPUElapsedTimeMilli()
    {
        return mon.getCPUElapsedTimeMilli();
    }

    public long getUserElapsedTimeMilli()
    {
        return mon.getUserElapsedTimeMilli();
    }

    public double getCPUElapsedTimeSec()
    {
        return mon.getCPUElapsedTimeSec();
    }

    public double getUserElapsedTimeSec()
    {
        return mon.getUserElapsedTimeSec();
    }

    public double getElapsedTimeSec()
    {
        return mon.getElapsedTimeSec();
    }

    public int getThreadCount()
    {
        return mon.getThreadCount();
    }

    /**
     * Uses SystemUtil.getAvailableProcessors() instead.
     * 
     * @deprecated
     */
    @Deprecated
    public static int getAvailableProcessors()
    {
        return SystemUtil.getNumberOfCPUs();
    }

    public long getElapsedTimeMilli()
    {
        return mon.getElapsedTimeMilli();
    }
}
