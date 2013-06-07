/**
 * 
 */
package icy.type;

/**
 * Utilities for {@link DataIterator} classes.
 * 
 * @author Stephane
 */
public class DataIteratorUtil
{
    /**
     * Returns the number of element contained in the specified {@link DataIterator}.
     */
    public static long count(DataIterator it)
    {
        long result = 0;

        it.reset();

        while (!it.done())
        {
            it.next();
            result++;
        }

        return result;
    }

    /**
     * Sets the specified value to the specified {@link DataIterator}.
     */
    public static void set(DataIterator it, double value)
    {
        it.reset();

        while (!it.done())
        {
            it.set(value);
            it.next();
        }
    }

}
