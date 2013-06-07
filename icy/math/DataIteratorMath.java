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
package icy.math;

import icy.type.DataIterator;
import icy.type.DataIteratorUtil;

/**
 * Math utilities for {@link DataIterator} classes.
 * 
 * @author Stephane
 */
public class DataIteratorMath
{
    /**
     * @deprecated Use {@link DataIteratorUtil#count(DataIterator)} instead.
     */
    @Deprecated
    public static long count(DataIterator it)
    {
        return DataIteratorUtil.count(it);
    }

    /**
     * Returns the sum of all values contained in the specified {@link DataIterator}.
     * Returns <code>0</code> if no value in <code>DataIterator</code>.
     */
    public static double sum(DataIterator it)
    {
        double result = 0;

        it.reset();

        while (!it.done())
        {
            result += it.get();
            it.next();
        }

        return result;
    }

    /**
     * Returns the minimum value found in the specified {@link DataIterator}.
     * Returns <code>Double.MAX_VALUE</code> if no value in <code>DataIterator</code>.
     */
    public static double min(DataIterator it)
    {
        double result = Double.MAX_VALUE;

        it.reset();

        while (!it.done())
        {
            final double value = it.get();
            if (value < result)
                result = value;
            it.next();
        }

        return result;
    }

    /**
     * Returns the maximum value found in the specified {@link DataIterator}.
     * Returns <code>Double.MIN_VALUE</code> if no value in <code>DataIterator</code>.
     */
    public static double max(DataIterator it)
    {
        double result = -Double.MAX_VALUE;

        it.reset();

        while (!it.done())
        {
            final double value = it.get();
            if (value > result)
                result = value;
            it.next();
        }

        return result;
    }

    /**
     * Returns the mean value found in the specified {@link DataIterator}.
     * Returns <code>0</code> if no value in <code>DataIterator</code>.
     */
    public static double mean(DataIterator it)
    {
        double result = 0;
        long numSample = 0;

        it.reset();

        while (!it.done())
        {
            result += it.get();
            numSample++;
            it.next();
        }

        return result / numSample;
    }
}
