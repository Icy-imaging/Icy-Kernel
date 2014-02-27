/**
 * 
 */
package icy.common.exception;

/**
 * UnsupportedFormatException is the exception thrown when try to load a resource and the format in
 * not recognized or incorrect.
 * 
 * @author Stephane
 */
public class UnsupportedFormatException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = -1571266483842584203L;

    /**
     * 
     */
    public UnsupportedFormatException()
    {
        super();
    }

    public UnsupportedFormatException(String message)
    {
        super(message);
    }

    public UnsupportedFormatException(Throwable cause)
    {
        super(cause);
    }

    public UnsupportedFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
