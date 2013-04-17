/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
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
    public boolean done();

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
