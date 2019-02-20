package icy.image.cache;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import icy.file.FileUtil;
import icy.system.IcyExceptionHandler;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class EHCache2 extends AbstractCache
{
    private final Set<Integer> eternalStoredKeys;
    private CacheManager cacheManager;
    private Cache cache;
    private boolean enabled;

    public EHCache2(int cacheSizeMB, String path)
    {
        super();

        eternalStoredKeys = new HashSet<>();

        // get old ehcache agent JAR files
        final String[] oldFiles = FileUtil.getFiles(FileUtil.getTempDirectory(), new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                // old ehcache temp agent JAR files
                return FileUtil.getFileName(pathname.getAbsolutePath(), false).startsWith("ehcache");
            }
        }, false, false, false);
        // delete these files as ehcache don't do it itself
        for (String file : oldFiles)
            FileUtil.delete(file, false);

        // delete previous cache file
        FileUtil.delete(path, true);

        try
        {
            final DiskStoreConfiguration diskConfig = new DiskStoreConfiguration().path(path);
            final Configuration cacheManagerConfig = new Configuration().diskStore(diskConfig);

            final PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                    .strategy(Strategy.LOCALTEMPSWAP);
            // CacheWriterFactoryConfiguration c = new CacheWriterFactoryConfiguration();
            // c.setClass(path);

            final long freeBytes = new File(FileUtil.getDrive(path)).getUsableSpace();
            // subtract 200 MB to available space for safety
            final long freeMB = (freeBytes <= 0) ? Long.MAX_VALUE : Math.max(0, (freeBytes / (1024 * 1024)) - 200);

            final CacheConfiguration cacheConfig = new CacheConfiguration().name("ehCache2")
                    .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                    .maxBytesLocalDisk(Math.min(freeMB, 500000L), MemoryUnit.MEGABYTES)
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).timeToIdleSeconds(20)
                    .timeToLiveSeconds(60).diskExpiryThreadIntervalSeconds(120).persistence(persistenceConfig);
            // .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).eternal(true)
            // .persistence(persistenceConfig);

            cacheManagerConfig.addCache(cacheConfig);

            // singleton cache manager creation
            cacheManager = CacheManager.create(cacheManagerConfig);

            // get the cache
            cache = cacheManager.getCache("ehCache2");
            // // add the custom Tile cache loader to it
            // cache.registerCacheLoader(new ImageCacheLoader());

            enabled = true;
        }
        catch (Exception e)
        {
            System.err.println("Error while initialize image cache:");
            IcyExceptionHandler.showErrorMessage(e, false, true);
            enabled = false;
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @SuppressWarnings("deprecation")
    @Override
    public long usedMemory()
    {
        return cache.calculateInMemorySize() + cache.calculateOffHeapSize();
    }

    @SuppressWarnings("deprecation")
    @Override
    public long usedDisk()
    {
        return cache.calculateOnDiskSize();
    }

    @Override
    public boolean isOnMemoryCache(Integer key)
    {
        if (profiling)
            startProf();

        try
        {
            return cache.isElementInMemory(key) | cache.isElementOffHeap(key);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public boolean isOnDiskCache(Integer key)
    {
        if (profiling)
            startProf();

        try
        {
            return cache.isElementOnDisk(key);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public boolean isInCache(Integer key)
    {
        if (profiling)
            startProf();

        try
        {
            return cache.isKeyInCache(key);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Integer> getAllKeys()
    {
        if (profiling)
            startProf();

        try
        {
            return cache.getKeys();
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public Object get(Integer key)
    {
        if (profiling)
            startProf();

        final boolean checkNull;
        Object result = null;

        // test if we need to check for null result
        synchronized (eternalStoredKeys)
        {
            checkNull = eternalStoredKeys.contains(key);
        }

        try
        {
            final Element e = cache.get(key);

            if (e != null)
                result = e.getObjectValue();

            // check if eternal data was lost (it seems that sometime EhCache loss data put in eternal state !!)
            if (checkNull && (result == null))
                System.err.println("ImageCache error: data '" + key + "' couldn't be retrieved (data loss).");

            return result;
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void set(Integer key, Object object, boolean eternal)
    {
        if (profiling)
            startProf();

        if (eternal)
        {
            synchronized (eternalStoredKeys)
            {
                // save in keyset
                if (object != null)
                    eternalStoredKeys.add(key);
                else
                    eternalStoredKeys.remove(key);
            }
        }

        try
        {
            cache.put(new Element(key, object, eternal));
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void clear()
    {
        if (profiling)
            startProf();

        eternalStoredKeys.clear();

        try
        {
            cache.removeAll();
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void remove(Integer key)
    {
        if (profiling)
            startProf();

        synchronized (eternalStoredKeys)
        {
            // remove from keyset
            eternalStoredKeys.remove(key);
        }

        try
        {
            cache.remove(key);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    public StatisticsGateway getStats()
    {
        return cache.getStatistics();
    }

    @Override
    public void end()
    {
        eternalStoredKeys.clear();
        cacheManager.shutdown();
    }

    @Override
    public String getName()
    {
        return "EHCache 2";
    }

}