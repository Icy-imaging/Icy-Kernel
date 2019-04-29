package icy.image.cache;

public class CacheException extends Exception
{
    public CacheException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CacheException(String message)
    {
        super(message);
    }
}
