/**
 * 
 */
package icy.image.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icy.image.IcyBufferedImage;
import icy.preferences.ApplicationPreferences;

/**
 * Image Cache static util class.<br>
 * The cache store and return 1D array data corresponding to the internal {@link IcyBufferedImage#getDataXY(int)} image data.
 * 
 * @author Stephane
 */
public class ImageCache
{
    public final static AbstractCache cache = new EHCache2(ApplicationPreferences.getCacheMemoryMB(),
            ApplicationPreferences.getCachePath() + "/icy_cache");

    private static Integer getKey(IcyBufferedImage image)
    {
        return Integer.valueOf(System.identityHashCode(image));
    }

    private static IcyBufferedImage getImage(Integer key)
    {
        return IcyBufferedImage.getIcyBufferedImage(key);
    }

    private static Collection<IcyBufferedImage> getImages(Collection<Integer> keys, boolean getNull)
    {
        final List<IcyBufferedImage> result = new ArrayList<IcyBufferedImage>(keys.size());

        for (Integer key : keys)
        {
            final IcyBufferedImage image = getImage(key);

            if (getNull || (image != null))
                result.add(image);
        }

        return result;
    }
    
    public static boolean isEnabled()
    {
        return cache.isEnabled();
    }

    public static boolean isInCache(IcyBufferedImage image)
    {
        return cache.isInCache(getKey(image));
    }

    public static boolean isOnMemoryCache(IcyBufferedImage image)
    {
        return cache.isOnMemoryCache(getKey(image));
    }

    public static boolean isOnDiskCache(IcyBufferedImage image)
    {
        return cache.isOnDiskCache(getKey(image));
    }

    /**
     * Return used memory for cache (in bytes)
     */
    public static long usedMemory()
    {
        return cache.usedMemory();
    }

    /**
     * Return used disk space for cache (in bytes)
     */
    public static long usedDisk()
    {
        return cache.usedDisk();
    }

    /**
     * Get all data {@link IcyBufferedImage} in the cache
     */
    public static Collection<IcyBufferedImage> getAllKeys() throws CacheException
    {
        return getImages(cache.getAllKeys(), false);
    }

    /**
     * Get the corresponding data array (2D native array) from cache from a given {@link IcyBufferedImage}
     */
    public static Object get(IcyBufferedImage key) throws CacheException
    {
        return cache.get(getKey(key));
    }

    /**
     * Get all data array from cache from a given Collection of {@link IcyBufferedImage}
     */
    public static Map<IcyBufferedImage, Object> get(Collection<IcyBufferedImage> keys) throws CacheException
    {
        final Map<IcyBufferedImage, Object> result = new HashMap<IcyBufferedImage, Object>();

        for (IcyBufferedImage key : keys)
            result.put(key, get(key));

        return result;
    }

    /**
     * Put the specified data array (2D native array) into cache with its associated key
     */
    public static void set(IcyBufferedImage key, Object object, boolean eternal) throws CacheException
    {
        cache.set(getKey(key), object, eternal);
    }

    /**
     * Remove an object from the cache from its key
     */
    public static void remove(IcyBufferedImage key) throws CacheException
    {
        cache.remove(getKey(key));
    }

    /**
     * Call it when you're done with the cache (release all resources and cleanup)
     */
    public static void end()
    {
        cache.end();
    }
}
