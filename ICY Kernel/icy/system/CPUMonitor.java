/**
 * 
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
        return SystemUtil.getAvailableProcessors();
    }

    public long getElapsedTimeMilli()
    {
        return mon.getElapsedTimeMilli();
    }
}
