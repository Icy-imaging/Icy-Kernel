package icy.image.cache;

import java.util.Collection;

public abstract class AbstractCache
{
    boolean profiling;
    long profilingTime;
    long startTime;

    public AbstractCache()
    {
        super();

        profiling = false;
    }

    public abstract String getName();

    public void setProfiling(boolean value)
    {
        profiling = value;
    }

    public void resetProfiling()
    {
        profilingTime = 0L;
    }

    public long getProfilingTime()
    {
        return profilingTime;
    }

    protected void startProf()
    {
        startTime = System.nanoTime();
    }

    protected void endProf()
    {
        profilingTime += (System.nanoTime() - startTime) / 1000000L;
    }

    /**
     * Returns true if the cache is enabled
     */
    public abstract boolean isEnabled();

    /**
     * Test presence of a key in the cache
     */
    public abstract boolean isInCache(Integer key);

    /**
     * Test presence of a key in the cache
     */
    public abstract boolean isOnMemoryCache(Integer key);

    /**
     * Test presence of a key in the cache
     */
    public abstract boolean isOnDiskCache(Integer key);

    /**
     * Return used memory for cache (in MB)
     */
    public abstract long usedMemory();

    /**
     * Return used disk space for cache (in MB)
     */
    public abstract long usedDisk();

    /**
     * Get all element keys in the cache
     */
    public abstract Collection<Integer> getAllKeys();

    /**
     * Get an object from cache from its key
     */
    public abstract Object get(Integer key);

    /**
     * Put an object in cache with its associated key
     */
    public abstract void set(Integer key, Object object, boolean eternal);

    /**
     * Clear the cache
     */
    public abstract void clear();

    /**
     * Remove an object from the cache from its key
     */
    public abstract void remove(Integer key);

    /**
     * Call it when you're done with the cache (release resources and cleanup)
     */
    public abstract void end();
}
