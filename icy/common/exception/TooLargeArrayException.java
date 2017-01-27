/**
 * 
 */
package icy.common.exception;

/**
 * Exception when trying to allocate a too large array (length > 2^31)
 * 
 * @author Stephane
 */
public class TooLargeArrayException extends RuntimeException
{
    public TooLargeArrayException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TooLargeArrayException(Throwable cause)
    {
        super(cause);
    }

    public TooLargeArrayException(String message)
    {
        super(message);
    }

    public TooLargeArrayException()
    {
        this("Can't allocate array of size >= 2^31");
    }
}
