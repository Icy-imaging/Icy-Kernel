/**
 * 
 */
package icy.math;

import icy.type.TypeUtil;
import icy.type.collection.array.ArrayUtil;

import java.util.Arrays;

/**
 * @author Stephane
 */
public class Histogram
{
    protected final int[] bins;

    protected final double minValue;
    protected final double maxValue;
    protected final boolean integer;

    protected final double dataToBin;
    protected final double binWidth;

    /**
     * Create a histogram for the specified value range and the desired number of bins.
     * 
     * @param nbBin
     *        number of desired bins (should be > 0).
     * @param integer
     *        If true the input value are considered as integer values.<br>
     *        Bins number is then clamped to values range to not bias the histogram.<br>
     *        In this case the effective number of bins can differ from the desired one.
     */
    public Histogram(double minValue, double maxValue, int nbBin, boolean integer)
    {
        super();

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.integer = integer;

        final double range = (maxValue - minValue);
        double bw;

        if (integer)
        {
            if (nbBin > range)
                bw = 1d;
            else
            {
                bw = (range + 1d) / nbBin;
                bw = Math.max(1d, Math.floor(bw));
            }
            bins = new int[(int) Math.ceil((range + 1d) / bw)];
        }
        else
        {
            bw = range / nbBin;
            bins = new int[nbBin];
        }

        binWidth = bw;

        // data to bin index conversion ratio
        if (range > 0)
            dataToBin = (bins.length - 1) / range;
        else
            dataToBin = 0d;
    }

    /**
     * Reset histogram
     */
    public void reset()
    {
        Arrays.fill(bins, 0);
    }

    /**
     * Add the value to the histogram
     */
    public void addValue(double value)
    {
        bins[(int) ((value - minValue) * dataToBin)]++;
    }

    /**
     * Add the specified array of values to the histogram
     * 
     * @param signed
     *        false if the input array should be interpreted as unsigned values<br>
     *        (integer type only)
     */
    public void addValues(Object array, boolean signed)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                addValues((byte[]) array, signed);
                break;

            case SHORT:
                addValues((short[]) array, signed);
                break;

            case INT:
                addValues((int[]) array, signed);
                break;

            case LONG:
                addValues((long[]) array, signed);
                break;

            case FLOAT:
                addValues((float[]) array);
                break;

            case DOUBLE:
                addValues((double[]) array);
                break;
        }
    }

    /**
     * Add the specified byte array to the histogram
     */
    public void addValues(byte[] array, boolean signed)
    {
        if (signed)
        {
            for (byte value : array)
                bins[(int) ((value - minValue) * dataToBin)]++;
        }
        else
        {
            for (byte value : array)
                bins[(int) ((TypeUtil.unsign(value) - minValue) * dataToBin)]++;
        }
    }

    /**
     * Add the specified short array to the histogram
     */
    public void addValues(short[] array, boolean signed)
    {
        if (signed)
        {
            for (short value : array)
                bins[(int) ((value - minValue) * dataToBin)]++;
        }
        else
        {
            for (short value : array)
                bins[(int) ((TypeUtil.unsign(value) - minValue) * dataToBin)]++;
        }
    }

    /**
     * Add the specified int array to the histogram
     */
    public void addValues(int[] array, boolean signed)
    {
        if (signed)
        {
            for (int value : array)
                bins[(int) ((value - minValue) * dataToBin)]++;
        }
        else
        {
            for (int value : array)
                bins[(int) ((TypeUtil.unsign(value) - minValue) * dataToBin)]++;
        }
    }

    /**
     * Add the specified long array to the histogram
     */
    public void addValues(long[] array, boolean signed)
    {
        if (signed)
        {
            for (long value : array)
                bins[(int) ((value - minValue) * dataToBin)]++;
        }
        else
        {
            for (long value : array)
                bins[(int) ((TypeUtil.unsign(value) - minValue) * dataToBin)]++;
        }
    }

    /**
     * Add the specified float array to the histogram
     */
    public void addValues(float[] array)
    {
        for (float value : array)
            bins[(int) ((value - minValue) * dataToBin)]++;
    }

    /**
     * Add the specified double array to the histogram
     */
    public void addValues(double[] array)
    {
        for (double value : array)
            bins[(int) ((value - minValue) * dataToBin)]++;
    }

    /**
     * Get bin index from data value
     */
    protected int toBinIndex(double value)
    {
        return (int) Math.round((value - minValue) * dataToBin);
    }

    /**
     * Returns the minimum allowed value of the histogram.
     */
    public double getMinValue()
    {
        return minValue;
    }

    /**
     * Returns the maximum allowed value of the histogram.
     */
    public double getMaxValue()
    {
        return maxValue;
    }

    /**
     * Returns true if the input value are integer values only.<br>
     * This is used to adapt the bin number.
     */
    public boolean isIntegerType()
    {
        return integer;
    }

    /**
     * Returns the number of bins of the histogram.
     */
    public int getBinNumber()
    {
        return bins.length;
    }

    /**
     * Return the width of a bin
     */
    public double getBinWidth()
    {
        return binWidth;
    }

    /**
     * Returns the size of the specified bin (number of element in the bin)
     */
    public int getBinSize(int index)
    {
        return bins[index];
    }

    /**
     * Returns bins of histogram
     */
    public int[] getBins()
    {
        return bins;
    }

}
