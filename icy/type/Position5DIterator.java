/**
 * 
 */
package icy.type;

import icy.type.point.Point5D;

import java.util.NoSuchElementException;

/**
 * Position 5D iterator.
 * 
 * @author Stephane
 */
public interface Position5DIterator
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
    public Point5D get() throws NoSuchElementException;

    /**
     * @return the current position X of the iterator
     */
    public int getX() throws NoSuchElementException;

    /**
     * @return the current position Y of the iterator
     */
    public int getY() throws NoSuchElementException;;

    /**
     * @return the current position Z of the iterator
     */
    public int getZ() throws NoSuchElementException;;

    /**
     * @return the current position T of the iterator
     */
    public int getT() throws NoSuchElementException;;

    /**
     * @return the current position C of the iterator
     */
    public int getC() throws NoSuchElementException;;
}
