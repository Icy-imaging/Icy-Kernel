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
 * @author stephane
 */
public class MathUtil
{
    public static final double POW2_8_DOUBLE = Math.pow(2, 8);
    public static final float POW2_8_FLOAT = (float) POW2_8_DOUBLE;
    public static final double POW2_16_DOUBLE = Math.pow(2, 16);
    public static final float POW2_16_FLOAT = (float) POW2_16_DOUBLE;
    public static final double POW2_32_DOUBLE = Math.pow(2, 32);
    public static final float POW2_32_FLOAT = (float) POW2_32_DOUBLE;
    public static final double POW2_64_DOUBLE = Math.pow(2, 64);
    public static final float POW2_64_FLOAT = (float) POW2_64_DOUBLE;

    /**
     * Return the specified value as "bytes" string :<br>
     * 1024 --> "1 KB"<br>
     * 1048576 --> "1 MB"<br>
     * ...<br>
     */
    public static String getBytesString(double value)
    {
        final double absValue = Math.abs(value);

        // GB
        if (absValue > 10737418240f)
            return Double.toString(round(value / 1073741824d, 1)) + " GB";
        // MB
        else if (absValue > 10485760f)
            return Double.toString(round(value / 1048576d, 1)) + " MB";
        // KB
        else if (absValue > 10240f)
            return Double.toString(round(value / 1024d, 1)) + " KB";

        // B
        return Double.toString(round(value, 1)) + " B";
    }

    public static double frac(double value)
    {
        return value - Math.floor(value);
    }

    /**
     * Normalize an array
     * 
     * @param array
     *        elements to normalize
     */
    public static void normalize(float[] array)
    {
        final float max = ArrayMath.max(array);
        if (max != 0)
            divide(array, max);
        else
        {
            final float min = ArrayMath.min(array);
            if (min != 0)
                divide(array, min);
        }
    }

    /**
     * Normalize an array
     * 
     * @param array
     *        elements to normalize
     */
    public static void normalize(double[] array)
    {
        final double max = ArrayMath.max(array);
        if (max != 0)
            divide(array, max);
        else
        {
            final double min = ArrayMath.min(array);
            if (min != 0)
                divide(array, min);
        }
    }

    /**
     * Replace all values in the array by their logarithm<br>
     * Be careful, all values should be >0 values
     * 
     * @param array
     *        elements to logarithm
     */
    public static void log(double[] array)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = Math.log(array[i]);
    }

    /**
     * Replace all values in the array by their logarithm<br>
     * Be careful, all values should be >0 values
     * 
     * @param array
     *        elements to logarithm
     */
    public static void log(float[] array)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (float) Math.log(array[i]);
    }

    /**
     * Add the the specified value to all elements in an array
     * 
     * @param array
     *        elements to modify
     * @param value
     */
    public static void add(double[] array, double value)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] + value;
    }

    /**
     * Add the the specified value to all elements in an array
     * 
     * @param array
     *        elements to modify
     * @param value
     */
    public static void add(float[] array, float value)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] + value;
    }

    /**
     * Multiply and add all elements in an array by the specified values
     * 
     * @param array
     *        elements to modify
     * @param mulValue
     * @param addValue
     */
    public static void madd(double[] array, double mulValue, double addValue)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (array[i] * mulValue) + addValue;
    }

    /**
     * Multiply and add all elements in an array by the specified values
     * 
     * @param array
     *        elements to modify
     * @param mulValue
     * @param addValue
     */
    public static void madd(float[] array, float mulValue, float addValue)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (array[i] * mulValue) + addValue;
    }

    /**
     * Multiply all elements in an array by the specified value
     * 
     * @param array
     *        elements to modify
     * @param value
     *        value to multiply by
     */
    public static void mul(double[] array, double value)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] * value;
    }

    /**
     * Multiply all elements in an array by the specified value
     * 
     * @param array
     *        elements to modify
     * @param value
     *        value to multiply by
     */
    public static void mul(float[] array, float value)
    {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] * value;
    }

    /**
     * Divides all elements in an array by the specified value
     * 
     * @param array
     *        elements to modify
     * @param value
     *        value used as divisor
     */
    public static void divide(double[] array, double value)
    {
        if (value != 0d)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                array[i] = array[i] / value;
        }
    }

    /**
     * Divides all elements in an array by the specified value
     * 
     * @param array
     *        elements to modify
     * @param value
     *        value used as divisor
     */
    public static void divide(float[] array, float value)
    {
        if (value != 0d)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                array[i] = array[i] / value;
        }
    }

    /**
     * @deprecated use {@link ArrayMath#min(Object, boolean)} instead
     */
    @Deprecated
    public static double min(Object array, boolean signed)
    {
        return ArrayMath.min(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#min(byte[], boolean)} instead
     */
    @Deprecated
    public static int min(byte[] array, boolean signed)
    {
        return ArrayMath.min(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#min(short[], boolean)} instead
     */
    @Deprecated
    public static int min(short[] array, boolean signed)
    {
        return ArrayMath.min(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#min(int[], boolean)} instead
     */
    @Deprecated
    public static long min(int[] array, boolean signed)
    {
        return ArrayMath.min(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#min(float[])} instead
     */
    @Deprecated
    public static float min(float[] array)
    {
        return ArrayMath.min(array);
    }

    /**
     * @deprecated use {@link ArrayMath#min(double[])} instead
     */
    @Deprecated
    public static double min(double[] array)
    {
        return ArrayMath.min(array);

    }

    /**
     * @deprecated use {@link ArrayMath#max(Object, boolean)} instead
     */
    @Deprecated
    public static double max(Object array, boolean signed)
    {
        return ArrayMath.max(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#max(byte[], boolean)} instead
     */
    @Deprecated
    public static int max(byte[] array, boolean signed)
    {
        return ArrayMath.max(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#max(short[], boolean)} instead
     */
    @Deprecated
    public static int max(short[] array, boolean signed)
    {
        return ArrayMath.max(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#max(int[], boolean)} instead
     */
    @Deprecated
    public static long max(int[] array, boolean signed)
    {
        return ArrayMath.max(array, signed);
    }

    /**
     * @deprecated use {@link ArrayMath#max(float[])} instead
     */
    @Deprecated
    public static float max(float[] array)
    {
        return ArrayMath.max(array);
    }

    /**
     * @deprecated use {@link ArrayMath#max(double[])} instead
     */
    @Deprecated
    public static double max(double[] array)
    {
        return ArrayMath.max(array);
    }

    /**
     * Round specified value to specified number of significant digit.<br>
     * If keepInteger is true then integer part of number is entirely conserved.
     */
    public static double roundSignificant(double d, int numDigit, boolean keepInteger)
    {
        final double digit = Math.ceil(Math.log10(Math.abs(d)));
        if ((digit >= numDigit) && keepInteger)
            return Math.round(d);

        final double pow = Math.pow(10, numDigit - digit);
        return Math.round(d * pow) / pow;
    }

    /**
     * Round specified value to specified number of significant digit.
     */
    public static double roundSignificant(double d, int numDigit)
    {
        return roundSignificant(d, numDigit, false);
    }

    /**
     * Round specified value to specified number of decimal.
     */
    public static double round(double d, int numDecimal)
    {
        final double pow = Math.pow(10, numDecimal);
        return Math.round(d * pow) / pow;
    }

    /**
     * Return the previous multiple of "mul" for the specified value
     * <ul>
     * <li>prevMultiple(200, 64) = 192</li>
     * </ul>
     * 
     * @param value
     * @param mul
     */
    public static double prevMultiple(double value, double mul)
    {
        if (mul == 0)
            return 0d;

        return Math.floor(value / mul) * mul;
    }

    /**
     * Return the next multiple of "mul" for the specified value
     * <ul>
     * <li>nextMultiple(200, 64) = 256</li>
     * </ul>
     * 
     * @param value
     * @param mul
     */
    public static double nextMultiple(double value, double mul)
    {
        if (mul == 0)
            return 0d;

        return Math.ceil(value / mul) * mul;
    }

    /**
     * Return the next power of 2 for the specified value
     * <ul>
     * <li>nextPow2(17) = 32</li>
     * <li>nextPow2(16) = 32</li>
     * <li>nextPow2(-12) = -8</li>
     * <li>nextPow2(-8) = -4</li>
     * </ul>
     * 
     * @param value
     * @return next power of 2
     */
    public static long nextPow2(long value)
    {
        long result;

        if (value < 0)
        {
            result = -1;
            while (result > value)
                result <<= 1;
            result >>= 1;
        }
        else
        {
            result = 1;
            while (result <= value)
                result <<= 1;
        }

        return result;
    }

    /**
     * Return the next power of 2 mask for the specified value
     * <ul>
     * <li>nextPow2Mask(17) = 31</li>
     * <li>nextPow2Mask(16) = 31</li>
     * <li>nextPow2Mask(-12) = -8</li>
     * <li>nextPow2Mask(-8) = -4</li>
     * </ul>
     * 
     * @param value
     * @return next power of 2 mask
     */
    public static long nextPow2Mask(long value)
    {
        final long result = nextPow2(value);
        if (value > 0)
            return result - 1;

        return result;
    }

    /**
     * Return the previous power of 2 for the specified value
     * <ul>
     * <li>prevPow2(17) = 16</li>
     * <li>prevPow2(16) = 8</li>
     * <li>prevPow2(-12) = -16</li>
     * <li>prevPow2(-8) = -16</li>
     * </ul>
     * 
     * @param value
     * @return previous power of 2
     */
    public static long prevPow2(long value)
    {
        long result;

        if (value < 0)
        {
            result = -1;
            while (result >= value)
                result <<= 1;
        }
        else
        {
            result = 1;
            while (result < value)
                result <<= 1;
            result >>= 1;
        }

        return result;
    }

    /**
     * Return the next power of 10 for the specified value
     * <ul>
     * <li>nextPow10(0.0067) = 0.01</li>
     * <li>nextPow10(-28.7) = -10</li>
     * </ul>
     * 
     * @param value
     */
    public static double nextPow10(double value)
    {
        if (value == 0)
            return 0;
        else if (value < 0)
            return -Math.pow(10, Math.floor(Math.log10(-value)));
        else
            return Math.pow(10, Math.ceil(Math.log10(value)));
    }

    /**
     * Return the previous power of 10 for the specified value
     * <ul>
     * <li>prevPow10(0.0067) = 0.001</li>
     * <li>prevPow10(-28.7) = -100</li>
     * </ul>
     * 
     * @param value
     */
    public static double prevPow10(double value)
    {
        if (value == 0)
            return 0;
        else if (value < 0)
            return -Math.pow(10, Math.ceil(Math.log10(-value)));
        else
            return Math.pow(10, Math.floor(Math.log10(value)));
    }

    /**
     * Format the specified degree angle to stay in [0..360[ range
     */
    public static double formatDegreeAngle(double angle)
    {
        final double res = angle % 360d;

        if (res < 0)
            return 360d + res;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [-180..180] range
     */
    public static double formatDegreeAngle2(double angle)
    {
        final double res = angle % 360d;

        if (res < -180d)
            return 360d + res;
        if (res > 180d)
            return res - 360d;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [0..2PI[ range
     */
    public static double formatRadianAngle(double angle)
    {
        final double res = angle % (2 * Math.PI);

        if (res < 0)
            return (2 * Math.PI) + res;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [-PI..PI] range
     */
    public static double formatRadianAngle2(double angle)
    {
        final double res = angle % (2 * Math.PI);

        if (res < -Math.PI)
            return (2 * Math.PI) + res;
        if (res > Math.PI)
            return res - (2 * Math.PI);

        return res;
    }

}
