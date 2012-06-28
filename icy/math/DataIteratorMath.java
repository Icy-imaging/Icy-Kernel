/**
 * 
 */
package icy.math;

import icy.type.DataIterator;

/**
 * Math utilities for DataIterator subclasses.
 * 
 * @author Stephane
 */
public class DataIteratorMath
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
