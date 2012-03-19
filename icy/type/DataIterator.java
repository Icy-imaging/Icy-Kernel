/**
 * 
 */
package icy.type;

import java.util.NoSuchElementException;

/**
 * Data iterator interface.<br>
 * This interface provides iteration for both read and write operation on double data.<br>
 * 
 * @author Stephane
 */
public interface DataIterator
{
    /**
     * Reset iterator to initial position.
     */
    public void reset();

    /**
     * Pass to the next element.
     */
    public void next();

    /**
     * Returns <tt>true</tt> if the iterator has no more elements.
     */
    public boolean isDone();

    /**
     * Returns the current element in the iteration and pass to the next.
     * 
     * @return the current element in the iteration.
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public double getAndNext();

    /**
     * Sets the current element in the iteration and pass to the next.
     * 
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public void setAndNext(double value);

    /**
     * Returns the current element in the iteration.
     * 
     * @return the current element in the iteration.
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public double get();

    /**
     * Sets the current element in the iteration and pass to the next.
     * 
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public void set(double value);
}
