/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.math;

/**
 * Class defining basic arithmetic and statistic operations on 1D double arrays.
 * 
 * @author Alexandre Dufour & Stephane
 */
public class ArrayMath
{
    /**
     * Element-wise addition of two arrays
     * 
     * @param a1
     * @param a2
     * @return
     */
    public static double[] add(double[] a1, double[] a2)
    {
        double[] result = new double[a1.length];
        add(a1, a2, result);
        return result;
    }

    /**
     * Element-wise addition of two arrays
     * 
     * @param a1
     * @param a2
     * @param output
     *        the array receiving the result
     */
    public static void add(double[] a1, double[] a2, double[] output)
    {
        for (int i = 0; i < a1.length; i++)
            output[i] = a1[i] + a2[i];
    }

    /**
     * Adds a value to all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void add(double[] array, double value, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = array[i] + value;
    }

    /**
     * Adds a value to all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void add(double value, double[] array, double[] output)
    {
        add(array, value, output); // for commutativity purposes
    }

    /**
     * Element-wise subtraction of two arrays
     * 
     * @param a1
     * @param a2
     * @return
     */
    public static double[] subtract(double[] a1, double[] a2)
    {
        double[] result = new double[a1.length];
        subtract(a1, a2, result);
        return result;
    }

    /**
     * Element-wise subtraction of two arrays
     * 
     * @param a1
     * @param a2
     * @param output
     *        the array receiving the result
     */
    public static void subtract(double[] a1, double[] a2, double[] output)
    {
        for (int i = 0; i < a1.length; i++)
            output[i] = a1[i] - a2[i];
    }

    /**
     * Subtracts a value to all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void subtract(double[] array, double value, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = array[i] - value;
    }

    /**
     * Subtracts a value by all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void subtract(double value, double[] array, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = value - array[i];
    }

    /**
     * Element-wise multiplication of two arrays
     * 
     * @param a1
     * @param a2
     * @return
     */
    public static double[] multiply(double[] a1, double[] a2)
    {
        double[] result = new double[a1.length];
        multiply(a1, a2, result);
        return result;
    }

    /**
     * Element-wise multiplication of two arrays
     * 
     * @param a1
     * @param a2
     * @param output
     *        the array receiving the result
     */
    public static void multiply(double[] a1, double[] a2, double[] output)
    {
        for (int i = 0; i < a1.length; i++)
            output[i] = a1[i] * a2[i];
    }

    /**
     * Multiplies a value by all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void multiply(double[] array, double value, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = array[i] * value;
    }

    /**
     * Multiplies a value by all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void multiply(double value, double[] array, double[] output)
    {
        multiply(array, value, output); // for commutativity purposes
    }

    /**
     * Element-wise division of two arrays
     * 
     * @param a1
     * @param a2
     * @return
     */
    public static double[] divide(double[] a1, double[] a2)
    {
        double[] result = new double[a1.length];
        divide(a1, a2, result);
        return result;
    }

    /**
     * Element-wise division of two arrays
     * 
     * @param a1
     * @param a2
     * @param output
     *        the array receiving the result
     */
    public static void divide(double[] a1, double[] a2, double[] output)
    {
        for (int i = 0; i < a1.length; i++)
            output[i] = a1[i] / a2[i];
    }

    /**
     * Divides all elements of the given array by the specified value
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void divide(double[] array, double value, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = array[i] / value;
    }

    /**
     * Divides a value by all elements of the given array
     * 
     * @param array
     * @param value
     * @param output
     *        the array receiving the result
     */
    public static void divide(double value, double[] array, double[] output)
    {
        for (int i = 0; i < array.length; i++)
            output[i] = value / array[i];
    }

    /**
     * Computes the absolute value of each value of the given array
     * 
     * @param input
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static double[] abs(double[] input, boolean overwrite)
    {
        double[] result = overwrite ? input : new double[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the minimum value of an array
     * 
     * @param input
     *        a double array
     * @return the min value of the array
     */
    public static double min(double[] input)
    {
        double min = Double.POSITIVE_INFINITY;
        for (double d : input)
            if (d < min)
                min = d;
        return min;
    }

    /**
     * Element-wise minimum of two arrays
     * 
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @param output
     *        - the array of min values
     */
    public static void min(double[] a1, double[] a2, double[] output)
    {

        for (int i = 0; i < a1.length; i++)
            if (a1[i] <= a2[i])
                output[i] = a1[i];
            else
                output[i] = a2[i];
    }

    /**
     * Element-wise minimum of two arrays
     * 
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @return the array of min values
     */
    public static double[] min(double[] a1, double[] a2)
    {

        double[] result = new double[a1.length];
        min(a1, a2, result);
        return result;

    }

    /**
     * Computes the maximum value of an array
     * 
     * @param input
     *        a double array
     * @return the max value of the array
     */
    public static double max(double[] input)
    {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : input)
            if (d > max)
                max = d;
        return max;
    }

    /**
     * Element-wise maximum of two arrays
     * 
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @param output
     *        - the array of max values
     */
    public static void max(double[] a1, double[] a2, double[] output)
    {

        for (int i = 0; i < a1.length; i++)
            if (a1[i] >= a2[i])
                output[i] = a1[i];
            else
                output[i] = a2[i];
    }

    /**
     * Element-wise maximum of two arrays
     * 
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @return the array of max values
     */
    public static double[] max(double[] a1, double[] a2)
    {

        double[] result = new double[a1.length];
        max(a1, a2, result);
        return result;

    }

    /**
     * Reorders the given array to compute its median value
     * 
     * @param input
     * @param preserveData
     *        set to true if the given array should not be changed (a copy will be made)
     * @return
     */
    public static double median(double[] input, boolean preserveData)
    {
        return select(input.length / 2, preserveData ? input.clone() : input);
    }

    /**
     * Computes the Maximum Absolute Deviation aka MAD of the given array
     * 
     * @param input
     * @param normalPopulation
     *        normalizes the population by 1.4826
     * @return
     */
    public static double mad(double[] input, boolean normalPopulation)
    {
        double[] temp = new double[input.length];
        double median = median(input, true);

        if (normalPopulation)
            for (int i = 0; i < input.length; i++)
                temp[i] = 1.4826f * (input[i] - median);
        else
            for (int i = 0; i < input.length; i++)
                temp[i] = (input[i] - median);

        abs(temp, true);

        return median(temp, false);
    }

    /**
     * (routine ported from 'Numerical Recipes in C 2nd ed.')<br>
     * Computes the k-th smallest value in the input array and rearranges the array such that the
     * wanted value is located at data[k-1], Lower values
     * are stored in arbitrary order in data[0 .. k-2] Higher values will be stored in arbitrary
     * order in data[k .. end]
     * 
     * @param k
     * @param data
     * @return the k-th smallest value in the array
     */
    public static double select(int k, double[] data)
    {
        int i, ir, j, l, mid;
        double a, temp;
        l = 1;
        ir = data.length;
        while (true)
        {
            if (ir <= l + 1)
            {
                if (ir == l + 1 && data[ir - 1] < data[l - 1])
                {
                    temp = data[l - 1];
                    data[l - 1] = data[ir - 1];
                    data[ir - 1] = temp;
                }
                return data[k - 1];
            }

            mid = (l + ir) >> 1;
            temp = data[mid - 1];
            data[mid - 1] = data[l];
            data[l] = temp;

            if (data[l] > data[ir - 1])
            {
                temp = data[l + 1 - 1];
                data[l] = data[ir - 1];
                data[ir - 1] = temp;
            }

            if (data[l - 1] > data[ir - 1])
            {
                temp = data[l - 1];
                data[l - 1] = data[ir - 1];
                data[ir - 1] = temp;
            }

            if (data[l] > data[l - 1])
            {
                temp = data[l];
                data[l] = data[l - 1];
                data[l - 1] = temp;
            }

            i = l + 1;
            j = ir;
            a = data[l - 1];

            while (true)
            {

                do
                    i++;
                while (data[i - 1] < a);
                do
                    j--;
                while (data[j - 1] > a);
                if (j < i)
                    break;
                temp = data[i - 1];
                data[i - 1] = data[j - 1];
                data[j - 1] = temp;
            }

            data[l - 1] = data[j - 1];
            data[j - 1] = a;

            if (j >= k)
                ir = j - 1;
            if (j <= k)
                l = i;
        }
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     * @return
     */
    public static double sum(double[] input)
    {
        double sum = 0;
        for (double d : input)
            sum += d;
        return sum;
    }

    /**
     * Computes the mean value of the given array
     * 
     * @param input
     * @return
     */
    public static double mean(double[] input)
    {
        return sum(input) / input.length;
    }

    /**
     * Computes the unbiased variance of the given array
     * 
     * @param input
     * @param unbiased
     *        set to true if the result should be normalized by the population size minus 1
     */
    public static double var(double[] input, boolean unbiased)
    {
        double var = 0, mean = mean(input);
        for (double f : input)
            var += (f - mean) * (f - mean);
        return var / (unbiased ? input.length - 1 : input.length);
    }

    /**
     * Computes the standard deviation of the given array (the variance square root)
     * 
     * @param input
     * @param unbiased
     *        set to true if the variance should be unbiased
     * @return the square root of the variance
     */
    public static double std(double[] input, boolean unbiased)
    {
        return Math.sqrt(var(input, unbiased));
    }

    /**
     * Rescales the given array to [newMin,newMax]. Nothing is done if the input is constant or if
     * the new bounds equal the old ones.
     * 
     * @param input
     *        the input array
     * @param output
     *        the output (rescaled) array
     * @param newMin
     *        the new min bound
     * @param newMax
     *        the new max bound
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static double[] rescale(double[] input, double newMin, double newMax, boolean overwrite)
    {
        double min = min(input), max = max(input);

        if (min == max || (min == newMin && max == newMax))
            return input;

        double[] result = overwrite ? input : new double[input.length];

        double ratio = (newMax - newMin) / (max - min);
        double base = newMin - (min * ratio);

        for (int i = 0; i < input.length; i++)
            result[i] = base + input[i] * ratio;

        return result;
    }

    /**
     * Standardize the input data by subtracting the mean value and dividing by the standard
     * deviation
     * 
     * @param input
     *        the data to standardize
     * @param overwrite
     *        true if the output should overwrite the input, false if a new array should be returned
     * @return the standardized data
     */
    public static double[] standardize(double[] input, boolean overwrite)
    {
        double[] output = overwrite ? input : new double[input.length];

        subtract(input, mean(input), output);
        divide(output, std(output, true), output);

        return output;
    }

    /**
     * Computes the classical correlation coefficient between 2 populations.<br>
     * This coefficient is given by:<br>
     * 
     * <pre>
     *                       sum(a[i] * b[i])
     * r(a,b) = -------------------------------------------
     *          sqrt( sum(a[i] * a[i]) * sum(b[i] * b[i]) )
     * </pre>
     * 
     * @param a
     *        a population
     * @param b
     *        a population
     * @return the correlation coefficient between a and b.
     * @throws IllegalArgumentException
     *         if the two input population have different sizes
     */
    public static double correlation(double[] a, double[] b) throws IllegalArgumentException
    {
        if (a.length != b.length)
            throw new IllegalArgumentException("Populations must have same size");

        double sum = 0, sqsum_a = 0, sqsum_b = 0;

        double ai, bi;
        for (int i = 0; i < a.length; i++)
        {
            ai = a[i];
            bi = b[i];
            sum += ai * bi;
            sqsum_a += ai * ai;
            sqsum_b += bi * bi;
        }

        return sum / Math.sqrt(sqsum_a * sqsum_b);
    }

    /**
     * Computes the Pearson correlation coefficient between two populations of same size N. <br>
     * This coefficient is computed by: <br>
     * 
     * <pre>
     *          sum(a[i] * b[i]) - N * mean(a) * mean(b)
     * r(a,b) = ----------------------------------------
     *                  (N-1) * std(a) * std(b)
     * </pre>
     * 
     * @param a
     *        a population
     * @param b
     *        a population
     * @return the Pearson correlation measure between a and b.
     * @throws IllegalArgumentException
     *         if the two input population have different sizes
     */
    public static double correlationPearson(double[] a, double[] b) throws IllegalArgumentException
    {
        if (a.length != b.length)
            throw new IllegalArgumentException("Populations must have same size");

        double sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i] * b[i];

        return (sum - a.length * mean(a) * mean(b)) / ((a.length - 1) * std(a, true) * std(b, true));
    }
}
