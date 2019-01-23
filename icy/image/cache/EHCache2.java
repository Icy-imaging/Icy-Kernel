package icy.image.cache;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

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
    private CacheManager cacheManager;
    private Cache cache;
    private boolean enabled;

    public EHCache2(int cacheSizeMB, String path)
    {
        super();

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
            final CacheConfiguration cacheConfig = new CacheConfiguration().name("ehCache2")
                    .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES).maxBytesLocalDisk(50, MemoryUnit.GIGABYTES)
                    .eternal(true).memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                    .persistence(persistenceConfig);

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
        return (cache.calculateInMemorySize() + cache.calculateOffHeapSize()) / (1024L * 1024L);
    }

    @SuppressWarnings("deprecation")
    @Override
    public long usedDisk()
    {
        return cache.calculateOnDiskSize() / (1024L * 1024L);
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
        try
        {
            final Element e = cache.get(key);

            if (e != null)
                return e.getObjectValue();

            return null;
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
        cacheManager.shutdown();
    }

    @Override
    public String getName()
    {
        return "EHCache 2";
    }

}