/**
 * 
 */
package icy.type;

import java.awt.geom.Point2D;
import java.util.NoSuchElementException;

/**
 * Position 2D iterator.
 * 
 * @author Stephane
 */
public interface Position2DIterator
{
    /**
     * Reset iterator to initial position.
     */
    public void reset();

    /**
     * Pass to the next element.
     * 
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public void next() throws NoSuchElementException;

    /**
     * Returns <tt>true</tt> if the iterator has no more elements.
     */
    public boolean done();

    /**
     * @return the current position of the iterator
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public Point2D get() throws NoSuchElementException;

    /**
     * @return the current position X of the iterator
     */
    public int getX() throws NoSuchElementException;

    /**
     * @return the current position Y of the iterator
     */
    public int getY() throws NoSuchElementException;
}