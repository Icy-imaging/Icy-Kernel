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
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;

/**
 * Image Cache static util class.<br>
 * The cache store and return 1D array data corresponding to the internal {@link IcyBufferedImage#getDataXY(int)} image data.
 * 
 * @author Stephane
 */
public class ImageCache
{
    public final static AbstractCache cache;

    static
    {
        cache = Icy.isCacheEnabled()
                ? new EHCache2(ApplicationPreferences.getCacheMemoryMB(),
                        ApplicationPreferences.getCachePath() + "/icy_cache")
                : null;
    }

    /**
     * @param image
     *        Image to check.
     * @return {@code true} if the image is present in the image cache. {@code false} otherwise.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static boolean isInCache(IcyBufferedImage image) throws RuntimeException
    {
        checkCacheLoaded();
        return cache.isInCache(getKey(image));
    }

    /**
     * @param image
     *        Image to check.
     * @return {@code true} if the image is present on the RAM memory. {@code false} otherwise.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static boolean isOnMemoryCache(IcyBufferedImage image) throws RuntimeException
    {
        checkCacheLoaded();
        return cache.isOnMemoryCache(getKey(image));
    }

    /**
     * @param image
     *        Image to check.
     * @return {@code true} if the image is present in the cache but NOT on the RAM memory (it is stored on disk cache). {@code false} otherwise.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static boolean isOnDiskCache(IcyBufferedImage image) throws RuntimeException
    {
        checkCacheLoaded();
        return cache.isOnDiskCache(getKey(image));
    }

    /**
     * @return Used memory for cache (in bytes).
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static long usedMemory() throws RuntimeException
    {
        checkCacheLoaded();
        return cache.usedMemory();
    }

    /**
     * @return Used disk space for cache (in bytes).'
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static long usedDisk() throws RuntimeException
    {
        checkCacheLoaded();
        return cache.usedDisk();
    }

    /**
     * Gets all data {@link IcyBufferedImage} in the cache.
     * 
     * @return All images stored in the cache.
     * @throws CacheException
     *         If an image keys cannot be read from the cache.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static Collection<IcyBufferedImage> getAllKeys() throws CacheException, RuntimeException
    {
        checkCacheLoaded();
        return getImages(cache.getAllKeys(), false);
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

    private static IcyBufferedImage getImage(Integer key)
    {
        return IcyBufferedImage.getIcyBufferedImage(key);
    }

    /**
     * Gets all data array from cache from a given Collection of {@link IcyBufferedImage}.
     * 
     * @param keys
     *        Collection of images used to retrieve data from cache.
     * @return All the data stored on the cache associated to the images given as parameter.
     * @throws CacheException
     *         If an error occurs during cache retrieval.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static Map<IcyBufferedImage, Object> get(Collection<IcyBufferedImage> keys)
            throws CacheException, RuntimeException
    {
        checkCacheLoaded();
        final Map<IcyBufferedImage, Object> result = new HashMap<IcyBufferedImage, Object>();

        for (IcyBufferedImage key : keys)
            result.put(key, cache.get(getKey(key)));

        return result;
    }

    /**
     * Gets the corresponding data array (2D native array) from cache from a given {@link IcyBufferedImage}.
     * 
     * @param key
     *        Image used to retrieve data from cache.
     * @return Retrieved data array (2D native array).
     * @throws CacheException
     *         If an error occurs during cache retrieval.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static Object get(IcyBufferedImage key) throws CacheException, RuntimeException
    {
        checkCacheLoaded();
        return cache.get(getKey(key));
    }

    /**
     * Puts the specified data array (2D native array) into cache with its associated key.
     * 
     * @param key
     *        Image used as key for the array.
     * @param object
     *        Data array to store.
     * @param eternal
     *        Whether the key should be kept indefinitely in the record.
     * @throws CacheException
     *         If an error occurs during cache storage.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static void set(IcyBufferedImage key, Object object, boolean eternal) throws CacheException, RuntimeException
    {
        checkCacheLoaded();
        cache.set(getKey(key), object, eternal);
    }

    /**
     * Removes an object from the cache from its key.
     * 
     * @param key
     *        Image identifying the object to remove.
     * @throws CacheException
     *         If an error occurs during cache removal.
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static void remove(IcyBufferedImage key) throws CacheException, RuntimeException
    {
        checkCacheLoaded();
        cache.remove(getKey(key));
    }

    private static Integer getKey(IcyBufferedImage image)
    {
        return Integer.valueOf(System.identityHashCode(image));
    }

    /**
     * Called when the cache is no longer used (Releasing all resources and performing cleanup).
     * 
     * @throws RuntimeException
     *         If the cache module has not been loaded.
     */
    public static void end() throws RuntimeException
    {
        checkCacheLoaded();
        cache.end();
    }

    private static void checkCacheLoaded() throws RuntimeException
    {
        if (isEnabled())
        {
            throw new RuntimeException("Cache module is not been enabled. Check launch parameters!");
        }
    }

    public static boolean isEnabled()
    {
        return cache != null;
    }
}
