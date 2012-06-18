/**
 * 
 */
package icy.system;

/**
 * Runtime handled exception.<br>
 * The Icy exception handler display a simple error dialog instead of the complete report dialog<br>
 * when it catches this exception.<br>
 * Also no log is saved in the console.
 * 
 * @author Stephane
 */
public class IcyHandledException extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = -1167116427799704383L;

    /**
     * 
     */
    public IcyHandledException()
    {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public IcyHandledException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     */
    public IcyHandledException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public IcyHandledException(Throwable cause)
    {
        super(cause);
    }
}
