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
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class EHCache2 extends AbstractCache
{
    private class CustomCacheEventListener implements CacheEventListener
    {
        public CustomCacheEventListener()
        {
            super();
        }

        @Override
        public void dispose()
        {
            System.out.println("EHCache disposed");
        }

        @Override
        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }

        @Override
        public void notifyElementEvicted(Ehcache ehCache, Element element)
        {
            // Note (Stephane): sometime ehcache evict element from cache even if we have unlimited disk storage tier enabled.
            // I guess this happen when it try to cache into the heap cache and there is not enough free space to do so.
            // Ideally it would move some elements from heap to disk or directly save element in disk storage
            // but it looks like it doesn't work that way (maybe for performance reason), even worse: ehcache can actually evict eternal element
            // while it keep non eternal elements in cache !!

            // eternal element eviction ?
            if (element.isEternal())
            {
                System.out.println("Warning: eternal element " + element.getObjectKey() + " evicted from cache");
                System.out.println("Trying to put it back...");
                // try to force GC and put it back in cache
                System.gc();
                cache.put(new Element(element.getObjectKey(), element.getObjectValue(), true));
            }
            // else
            // System.out.println("EHCache.eviction: "
            // + IcyBufferedImage.getIcyBufferedImage((Integer) element.getObjectKey()).getImageSourceInfo());
        }

        @Override
        public void notifyElementExpired(Ehcache ehCache, Element element)
        {
            // eternal element expiration ?
            if (element.isEternal())
            {
                System.out.println("Warning: eternal element " + element.getObjectKey() + " marked as expired..");
                System.out.println("Trying to put it back...");
                cache.put(new Element(element.getObjectKey(), element.getObjectValue(), true));
            }
            // else
            // System.out.println("EHCache.expiration: "
            // + IcyBufferedImage.getIcyBufferedImage((Integer) element.getObjectKey()).getImageSourceInfo());
        }

        @Override
        public void notifyElementPut(Ehcache ehCache, Element element) throws net.sf.ehcache.CacheException
        {
            //
        }

        @Override
        public void notifyElementRemoved(Ehcache ehCache, Element element) throws net.sf.ehcache.CacheException
        {
            //
        }

        @Override
        public void notifyElementUpdated(Ehcache ehCache, Element element) throws net.sf.ehcache.CacheException
        {
            //
        }

        @Override
        public void notifyRemoveAll(Ehcache ehCache)
        {
            //
        }
    }

    final Set<Integer> eternalStoredKeys;
    CacheManager cacheManager;
    Cache cache;
    boolean enabled;

    public EHCache2(int cacheSizeMB, String path)
    {
        super();

        eternalStoredKeys = new HashSet<Integer>();

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

            // Stephane: we need to put a long idle / live time otherwise not eternal data
            // will be removed from cache as soon it expired on get(key) call even if the cache is not full...
            final CacheConfiguration cacheConfig = new CacheConfiguration().name("ehCache2")
                    .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                    // .maxBytesLocalOffHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                    .maxBytesLocalDisk(Math.min(freeMB, 500000L), MemoryUnit.MEGABYTES)
                    // we want the disk write buffer to be at least 32 MB and 256 MB max
                    .diskSpoolBufferSizeMB(Math.max(32, Math.min(256, cacheSizeMB / 16)))
                    // .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).timeToIdleSeconds(2).timeToLiveSeconds(5)
                    // .diskExpiryThreadIntervalSeconds(10).persistence(persistenceConfig);
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).timeToIdleSeconds(60 * 5)
                    .timeToLiveSeconds(60 * 60).diskExpiryThreadIntervalSeconds(120).persistence(persistenceConfig);
            // .pinning(new PinningConfiguration().store(Store.INCACHE));
            // .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).timeToIdleSeconds(2).timeToLiveSeconds(2)

            cacheManagerConfig.addCache(cacheConfig);

            // singleton cache manager creation
            cacheManager = CacheManager.create(cacheManagerConfig);

            // get the cache
            cache = cacheManager.getCache("ehCache2");
            // // add the custom Tile cache loader to it
            // cache.registerCacheLoader(new ImageCacheLoader());

            cache.getCacheEventNotificationService().registerListener(new CustomCacheEventListener());
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
    public Collection<Integer> getAllKeys() throws CacheException
    {
        if (profiling)
            startProf();

        try
        {
            return cache.getKeys();
        }
        catch (Exception e)
        {
            throw new CacheException("ImageCache: an error occured while retrieving all keys from cache", e);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public Object get(Integer key) throws CacheException
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
                throw new CacheException("ImageCache error: data '" + key + "' couldn't be retrieved (data lost)");

            return result;
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void set(Integer key, Object object, boolean eternal) throws CacheException
    {
        if (profiling)
            startProf();

        synchronized (eternalStoredKeys)
        {
            // save in keyset (only for non null eternal data)
            if ((object != null) && eternal)
                eternalStoredKeys.add(key);
            else
                eternalStoredKeys.remove(key);
        }

        try
        {
            // System.out.println("EHCache.set(" + IcyBufferedImage.getIcyBufferedImage(key).getImageSourceInfo() + ", "
            // + eternal + ")");
            //
            cache.put(new Element(key, object, eternal));
        }
        catch (Exception e)
        {
            throw new CacheException("ImageCache error: data '" + key + "' couldn't be saved in cache", e);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void clear() throws CacheException
    {
        if (profiling)
            startProf();

        eternalStoredKeys.clear();

        try
        {
            cache.removeAll();
        }
        catch (Exception e)
        {
            throw new CacheException("ImageCache: an error occured while clearing cache", e);
        }
        finally
        {
            if (profiling)
                endProf();
        }
    }

    @Override
    public void remove(Integer key) throws CacheException
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
            // System.out
            // .println("EHCache.remove(" + IcyBufferedImage.getIcyBufferedImage(key).getImageSourceInfo() + ")");
            //
            cache.remove(key);
        }
        catch (Exception e)
        {
            throw new CacheException("ImageCache: an error occured while removing data '" + key + "' from cache", e);
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