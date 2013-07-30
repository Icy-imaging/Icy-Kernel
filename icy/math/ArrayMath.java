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

import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.ArrayUtil;

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
     * @param out
     *        the array receiving the result
     */
    public static Object add(Object a1, Object a2, Object out)
    {
        switch (ArrayUtil.getDataType(a1))
        {
            case BYTE:
                return add((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT:
                return add((short[]) a1, (short[]) a2, (short[]) out);
            case INT:
                return add((int[]) a1, (int[]) a2, (int[]) out);
            case LONG:
                return add((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT:
                return add((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE:
                return add((double[]) a1, (double[]) a2, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Element-wise addition of two arrays
     */
    public static Object add(Object a1, Object a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two double arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] add(double[] a1, double[] a2, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two double arrays
     */
    public static double[] add(double[] a1, double[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two float arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] add(float[] a1, float[] a2, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two float arrays
     */
    public static float[] add(float[] a1, float[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two long arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] add(long[] a1, long[] a2, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two long arrays
     */
    public static long[] add(long[] a1, long[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two int arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] add(int[] a1, int[] a2, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two int arrays
     */
    public static int[] add(int[] a1, int[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two short arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] add(short[] a1, short[] a2, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] + a2[i]);

        return result;
    }

    /**
     * Element-wise addition of two short arrays
     */
    public static short[] add(short[] a1, short[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two byte arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] add(byte[] a1, byte[] a2, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] + a2[i]);

        return result;
    }

    /**
     * Element-wise addition of two byte arrays
     */
    public static byte[] add(byte[] a1, byte[] a2)
    {
        return add(a1, a2, null);
    }

    /**
     * Adds a value to all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object add(Object array, Number value, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return add((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT:
                return add((short[]) array, value.shortValue(), (short[]) out);
            case INT:
                return add((int[]) array, value.intValue(), (int[]) out);
            case LONG:
                return add((long[]) array, value.longValue(), (long[]) out);
            case FLOAT:
                return add((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE:
                return add((double[]) array, value.doubleValue(), (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Adds a value to all elements of the given array
     */
    public static Object add(Object array, Number value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] add(double[] array, double value, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * @deprecated use {@link #add(double[] , double , double[])} instead
     */
    @Deprecated
    public static double[] add(double value, double[] array, double[] out)
    {
        return add(array, value, out);
    }

    /**
     * Adds a value to all elements of the given double array
     */
    public static double[] add(double[] array, double value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] add(float[] array, float value, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the float given array
     */
    public static float[] add(float[] array, float value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] add(long[] array, long value, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the given long array
     */
    public static long[] add(long[] array, long value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] add(int[] array, int value, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the given int array
     */
    public static int[] add(int[] array, int value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] add(short[] array, short value, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] + value);

        return result;
    }

    /**
     * Adds a value to all elements of the given short array
     */
    public static short[] add(short[] array, short value)
    {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] add(byte[] array, byte value, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] + value);

        return result;
    }

    /**
     * Adds a value to all elements of the given byte array
     */
    public static byte[] add(byte[] array, byte value)
    {
        return add(array, value, null);
    }

    /**
     * Element-wise subtraction of two arrays
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(Object a1, Object a2, Object out)
    {
        switch (ArrayUtil.getDataType(a1))
        {
            case BYTE:
                return subtract((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT:
                return subtract((short[]) a1, (short[]) a2, (short[]) out);
            case INT:
                return subtract((int[]) a1, (int[]) a2, (int[]) out);
            case LONG:
                return subtract((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT:
                return subtract((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE:
                return subtract((double[]) a1, (double[]) a2, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Element-wise subtraction of two arrays
     */
    public static Object subtract(Object a1, Object a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two double arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(double[] a1, double[] a2, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two double arrays
     */
    public static double[] subtract(double[] a1, double[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two float arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(float[] a1, float[] a2, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two float arrays
     */
    public static float[] subtract(float[] a1, float[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two long arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(long[] a1, long[] a2, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two long arrays
     */
    public static long[] subtract(long[] a1, long[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two int arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(int[] a1, int[] a2, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two int arrays
     */
    public static int[] subtract(int[] a1, int[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two short arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(short[] a1, short[] a2, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] - a2[i]);

        return result;
    }

    /**
     * Element-wise subtraction of two short arrays
     */
    public static short[] subtract(short[] a1, short[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two byte arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(byte[] a1, byte[] a2, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] - a2[i]);

        return result;
    }

    /**
     * Element-wise subtraction of two byte arrays
     */
    public static byte[] subtract(byte[] a1, byte[] a2)
    {
        return subtract(a1, a2, null);
    }

    /**
     * Subtracts a value to all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(Object array, Number value, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return subtract((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT:
                return subtract((short[]) array, value.shortValue(), (short[]) out);
            case INT:
                return subtract((int[]) array, value.intValue(), (int[]) out);
            case LONG:
                return subtract((long[]) array, value.longValue(), (long[]) out);
            case FLOAT:
                return subtract((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE:
                return subtract((double[]) array, value.doubleValue(), (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Subtracts a value to all elements of the given array
     */
    public static Object subtract(Object array, Number value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(double[] array, double value, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given double array
     */
    public static double[] subtract(double[] array, double value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(float[] array, float value, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the float given array
     */
    public static float[] subtract(float[] array, float value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(long[] array, long value, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given long array
     */
    public static long[] subtract(long[] array, long value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(int[] array, int value, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given int array
     */
    public static int[] subtract(int[] array, int value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(short[] array, short value, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] - value);

        return result;
    }

    /**
     * Subtracts a value to all elements of the given short array
     */
    public static short[] subtract(short[] array, short value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(byte[] array, byte value, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] - value);

        return result;
    }

    /**
     * Subtracts a value to all elements of the given byte array
     */
    public static byte[] subtract(byte[] array, byte value)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(Number value, Object array, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return subtract(value.byteValue(), (byte[]) array, (byte[]) out);
            case SHORT:
                return subtract(value.shortValue(), (short[]) array, (short[]) out);
            case INT:
                return subtract(value.intValue(), (int[]) array, (int[]) out);
            case LONG:
                return subtract(value.longValue(), (long[]) array, (long[]) out);
            case FLOAT:
                return subtract(value.floatValue(), (float[]) array, (float[]) out);
            case DOUBLE:
                return subtract(value.doubleValue(), (double[]) array, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Subtracts a value by all elements of the given array
     */
    public static Object subtract(Number value, Object array)
    {
        return subtract(value, array, null);
    }

    /**
     * Subtracts a value by all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(double value, double[] array, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given double array
     */
    public static double[] subtract(double value, double[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(float value, float[] array, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the float given array
     */
    public static float[] subtract(float value, float[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(long value, long[] array, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given long array
     */
    public static long[] subtract(long value, long[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(int value, int[] array, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given int array
     */
    public static int[] subtract(int value, int[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(short value, short[] array, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (value - array[i]);

        return result;
    }

    /**
     * Subtracts a value by all elements of the given short array
     */
    public static short[] subtract(short value, short[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(byte value, byte[] array, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (value - array[i]);

        return result;
    }

    /**
     * Subtracts a value by all elements of the given byte array
     */
    public static byte[] subtract(byte value, byte[] array)
    {
        return subtract(array, value, null);
    }

    /**
     * Element-wise multiplication of two arrays
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object multiply(Object a1, Object a2, Object out)
    {
        switch (ArrayUtil.getDataType(a1))
        {
            case BYTE:
                return multiply((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT:
                return multiply((short[]) a1, (short[]) a2, (short[]) out);
            case INT:
                return multiply((int[]) a1, (int[]) a2, (int[]) out);
            case LONG:
                return multiply((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT:
                return multiply((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE:
                return multiply((double[]) a1, (double[]) a2, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Element-wise multiplication of two arrays
     */
    public static Object multiply(Object a1, Object a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two double arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] multiply(double[] a1, double[] a2, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two double arrays
     */
    public static double[] multiply(double[] a1, double[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two float arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] multiply(float[] a1, float[] a2, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two float arrays
     */
    public static float[] multiply(float[] a1, float[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two long arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] multiply(long[] a1, long[] a2, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two long arrays
     */
    public static long[] multiply(long[] a1, long[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two int arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] multiply(int[] a1, int[] a2, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two int arrays
     */
    public static int[] multiply(int[] a1, int[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two short arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] multiply(short[] a1, short[] a2, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] * a2[i]);

        return result;
    }

    /**
     * Element-wise multiplication of two short arrays
     */
    public static short[] multiply(short[] a1, short[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two byte arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] multiply(byte[] a1, byte[] a2, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] * a2[i]);

        return result;
    }

    /**
     * Element-wise multiplication of two byte arrays
     */
    public static byte[] multiply(byte[] a1, byte[] a2)
    {
        return multiply(a1, a2, null);
    }

    /**
     * Multiplies a value to all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object multiply(Object array, Number value, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return multiply((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT:
                return multiply((short[]) array, value.shortValue(), (short[]) out);
            case INT:
                return multiply((int[]) array, value.intValue(), (int[]) out);
            case LONG:
                return multiply((long[]) array, value.longValue(), (long[]) out);
            case FLOAT:
                return multiply((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE:
                return multiply((double[]) array, value.doubleValue(), (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Multiplies a value to all elements of the given array
     */
    public static Object multiply(Object array, Number value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] multiply(double[] array, double value, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * @deprecated use {@link #multiply(double[] , double , double[])} instead
     */
    @Deprecated
    public static double[] multiply(double value, double[] array, double[] out)
    {
        return multiply(array, value, out);
    }

    /**
     * Multiplies a value to all elements of the given double array
     */
    public static double[] multiply(double[] array, double value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] multiply(float[] array, float value, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the float given array
     */
    public static float[] multiply(float[] array, float value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] multiply(long[] array, long value, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the given long array
     */
    public static long[] multiply(long[] array, long value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] multiply(int[] array, int value, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the given int array
     */
    public static int[] multiply(int[] array, int value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] multiply(short[] array, short value, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] * value);

        return result;
    }

    /**
     * Multiplies a value to all elements of the given short array
     */
    public static short[] multiply(short[] array, short value)
    {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] multiply(byte[] array, byte value, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] * value);

        return result;
    }

    /**
     * Multiplies a value to all elements of the given byte array
     */
    public static byte[] multiply(byte[] array, byte value)
    {
        return multiply(array, value, null);
    }

    /**
     * Element-wise division of two arrays
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object divide(Object a1, Object a2, Object out)
    {
        switch (ArrayUtil.getDataType(a1))
        {
            case BYTE:
                return divide((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT:
                return divide((short[]) a1, (short[]) a2, (short[]) out);
            case INT:
                return divide((int[]) a1, (int[]) a2, (int[]) out);
            case LONG:
                return divide((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT:
                return divide((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE:
                return divide((double[]) a1, (double[]) a2, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Element-wise division of two arrays
     */
    public static Object divide(Object a1, Object a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two double arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(double[] a1, double[] a2, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two double arrays
     */
    public static double[] divide(double[] a1, double[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two float arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(float[] a1, float[] a2, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two float arrays
     */
    public static float[] divide(float[] a1, float[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two long arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(long[] a1, long[] a2, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two long arrays
     */
    public static long[] divide(long[] a1, long[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two int arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(int[] a1, int[] a2, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two int arrays
     */
    public static int[] divide(int[] a1, int[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two short arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(short[] a1, short[] a2, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] / a2[i]);

        return result;
    }

    /**
     * Element-wise division of two short arrays
     */
    public static short[] divide(short[] a1, short[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two byte arrays (result in output if defined)
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(byte[] a1, byte[] a2, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] / a2[i]);

        return result;
    }

    /**
     * Element-wise division of two byte arrays
     */
    public static byte[] divide(byte[] a1, byte[] a2)
    {
        return divide(a1, a2, null);
    }

    /**
     * Divides a value to all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object divide(Object array, Number value, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return divide((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT:
                return divide((short[]) array, value.shortValue(), (short[]) out);
            case INT:
                return divide((int[]) array, value.intValue(), (int[]) out);
            case LONG:
                return divide((long[]) array, value.longValue(), (long[]) out);
            case FLOAT:
                return divide((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE:
                return divide((double[]) array, value.doubleValue(), (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Divides a value to all elements of the given array
     */
    public static Object divide(Object array, Number value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(double[] array, double value, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given double array
     */
    public static double[] divide(double[] array, double value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(float[] array, float value, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the float given array
     */
    public static float[] divide(float[] array, float value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(long[] array, long value, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given long array
     */
    public static long[] divide(long[] array, long value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(int[] array, int value, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given int array
     */
    public static int[] divide(int[] array, int value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(short[] array, short value, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] / value);

        return result;
    }

    /**
     * Divides a value to all elements of the given short array
     */
    public static short[] divide(short[] array, short value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(byte[] array, byte value, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] / value);

        return result;
    }

    /**
     * Divides a value to all elements of the given byte array
     */
    public static byte[] divide(byte[] array, byte value)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given array
     * 
     * @param out
     *        the array receiving the result
     */
    public static Object divide(Number value, Object array, Object out)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return divide(value.byteValue(), (byte[]) array, (byte[]) out);
            case SHORT:
                return divide(value.shortValue(), (short[]) array, (short[]) out);
            case INT:
                return divide(value.intValue(), (int[]) array, (int[]) out);
            case LONG:
                return divide(value.longValue(), (long[]) array, (long[]) out);
            case FLOAT:
                return divide(value.floatValue(), (float[]) array, (float[]) out);
            case DOUBLE:
                return divide(value.doubleValue(), (double[]) array, (double[]) out);
            default:
                return null;
        }
    }

    /**
     * Divides a value by all elements of the given array
     */
    public static Object divide(Number value, Object array)
    {
        return divide(value, array, null);
    }

    /**
     * Subtracts a value by all elements of the given double array
     * 
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(double value, double[] array, double[] out)
    {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given double array
     */
    public static double[] divide(double value, double[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given float array
     * 
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(float value, float[] array, float[] out)
    {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the float given array
     */
    public static float[] divide(float value, float[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given long array
     * 
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(long value, long[] array, long[] out)
    {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given long array
     */
    public static long[] divide(long value, long[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given int array
     * 
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(int value, int[] array, int[] out)
    {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given int array
     */
    public static int[] divide(int value, int[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given short array
     * 
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(short value, short[] array, short[] out)
    {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (value / array[i]);

        return result;
    }

    /**
     * Divides a value by all elements of the given short array
     */
    public static short[] divide(short value, short[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given byte array
     * 
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(byte value, byte[] array, byte[] out)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (value / array[i]);

        return result;
    }

    /**
     * Divides a value by all elements of the given byte array
     */
    public static byte[] divide(byte value, byte[] array)
    {
        return divide(array, value, null);
    }

    /**
     * Computes the absolute value of each value of the given array
     * 
     * @param overwrite
     *        true : overwrites the input data<br>
     *        false: returns the result in a new array
     */
    public static Object abs(Object array, boolean overwrite)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return abs((byte[]) array, overwrite);
            case SHORT:
                return abs((short[]) array, overwrite);
            case INT:
                return abs((int[]) array, overwrite);
            case LONG:
                return abs((long[]) array, overwrite);
            case FLOAT:
                return abs((float[]) array, overwrite);
            case DOUBLE:
                return abs((double[]) array, overwrite);
            default:
                return null;
        }
    }

    /**
     * Computes the absolute value of each value of the given double array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static double[] abs(double[] input, boolean overwrite)
    {
        final double[] result = overwrite ? input : new double[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given float array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static float[] abs(float[] input, boolean overwrite)
    {
        final float[] result = overwrite ? input : new float[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given long array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static long[] abs(long[] input, boolean overwrite)
    {
        final long[] result = overwrite ? input : new long[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given int array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static int[] abs(int[] input, boolean overwrite)
    {
        final int[] result = overwrite ? input : new int[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given short array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static short[] abs(short[] input, boolean overwrite)
    {
        final short[] result = overwrite ? input : new short[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = (short) Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given byte array
     * 
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static byte[] abs(byte[] input, boolean overwrite)
    {
        final byte[] result = overwrite ? input : new byte[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = (byte) Math.abs(input[i]);

        return result;
    }

    /**
     * Find the minimum value of a generic array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static double min(Object array, boolean signed)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return min((byte[]) array, signed);
            case SHORT:
                return min((short[]) array, signed);
            case INT:
                return min((int[]) array, signed);
            case LONG:
                return min((long[]) array, signed);
            case FLOAT:
                return min((float[]) array);
            case DOUBLE:
                return min((double[]) array);
            default:
                return 0;
        }
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static int min(byte[] array, boolean signed)
    {
        if (signed)
        {
            byte min = Byte.MAX_VALUE;

            for (byte v : array)
                if (v < min)
                    min = v;

            return min;
        }

        int min = Integer.MAX_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final int v = TypeUtil.unsign(array[i]);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static int min(short[] array, boolean signed)
    {
        if (signed)
        {
            short min = Short.MAX_VALUE;

            for (short v : array)
                if (v < min)
                    min = v;

            return min;
        }

        int min = Integer.MAX_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final int v = TypeUtil.unsign(array[i]);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static long min(int[] array, boolean signed)
    {
        if (signed)
        {
            int min = Integer.MAX_VALUE;

            for (int v : array)
                if (v < min)
                    min = v;

            return min;
        }

        long min = Long.MAX_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final long v = TypeUtil.unsign(array[i]);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static long min(long[] array, boolean signed)
    {
        if (signed)
        {
            long min = Integer.MAX_VALUE;

            for (long v : array)
                if (v < min)
                    min = v;

            return min;
        }

        double min = Long.MAX_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final double v = TypeUtil.unsign(array[i]);
            // need to compare in double
            if (v < min)
                min = v;
        }

        // convert back to long (need to be interpreted as unsigned)
        return TypeUtil.toLong(min);
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @return the min value of the array
     */
    public static float min(float[] array)
    {
        float min = Float.MAX_VALUE;

        for (float v : array)
            if (v < min)
                min = v;

        return min;
    }

    /**
     * Find the minimum value of an array
     * 
     * @param array
     *        an array
     * @return the min value of the array
     */
    public static double min(double[] array)
    {
        double min = Double.MAX_VALUE;

        for (double v : array)
            if (v < min)
                min = v;

        return min;
    }

    /**
     * Find the maximum value of a generic array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static double max(Object array, boolean signed)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return max((byte[]) array, signed);
            case SHORT:
                return max((short[]) array, signed);
            case INT:
                return max((int[]) array, signed);
            case LONG:
                return max((long[]) array, signed);
            case FLOAT:
                return max((float[]) array);
            case DOUBLE:
                return max((double[]) array);
            default:
                return 0;
        }
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static int max(byte[] array, boolean signed)
    {
        if (signed)
        {
            byte max = Byte.MIN_VALUE;

            for (byte v : array)
                if (v > max)
                    max = v;

            return max;
        }

        int max = Integer.MIN_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final int v = TypeUtil.unsign(array[i]);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static int max(short[] array, boolean signed)
    {
        if (signed)
        {
            short max = Short.MIN_VALUE;

            for (short v : array)
                if (v > max)
                    max = v;

            return max;
        }

        int max = Integer.MIN_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final int v = TypeUtil.unsign(array[i]);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static long max(int[] array, boolean signed)
    {
        if (signed)
        {
            int max = Integer.MIN_VALUE;

            for (int v : array)
                if (v > max)
                    max = v;

            return max;
        }

        long max = Long.MIN_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final long v = TypeUtil.unsign(array[i]);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static long max(long[] array, boolean signed)
    {
        if (signed)
        {
            long max = Integer.MIN_VALUE;

            for (long v : array)
                if (v > max)
                    max = v;

            return max;
        }

        double max = Long.MIN_VALUE;

        for (int i = 0; i < array.length; i++)
        {
            final double v = TypeUtil.unsign(array[i]);
            // need to compare in double
            if (v > max)
                max = v;
        }

        // convert back to long (need to be interpreted as unsigned)
        return TypeUtil.toLong(max);
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @return the max value of the array
     */
    public static float max(float[] array)
    {
        float max = -Float.MAX_VALUE;

        for (float v : array)
            if (v > max)
                max = v;

        return max;
    }

    /**
     * Find the maximum value of an array
     * 
     * @param array
     *        an array
     * @return the max value of the array
     */
    public static double max(double[] array)
    {
        double max = -Double.MAX_VALUE;

        for (double v : array)
            if (v > max)
                max = v;

        return max;
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
     * Computes the sum of all values from the specified input array.
     * 
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the sum of all values from the array
     */
    public static double sum(Object array, boolean signed)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return sum((byte[]) array, signed);
            case SHORT:
                return sum((short[]) array, signed);
            case INT:
                return sum((int[]) array, signed);
            case LONG:
                return sum((long[]) array, signed);
            case FLOAT:
                return sum((float[]) array);
            case DOUBLE:
                return sum((double[]) array);
            default:
                return 0d;
        }
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(byte[] input, boolean signed)
    {
        double sum = 0;

        if (signed)
        {
            for (byte b : input)
                sum += b;

        }
        else
        {
            for (byte b : input)
                sum += TypeUtil.unsign(b);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(short[] input, boolean signed)
    {
        double sum = 0;

        if (signed)
        {
            for (short s : input)
                sum += s;

        }
        else
        {
            for (short s : input)
                sum += TypeUtil.unsign(s);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(int[] input, boolean signed)
    {
        double sum = 0;

        if (signed)
        {
            for (int i : input)
                sum += i;

        }
        else
        {
            for (int i : input)
                sum += TypeUtil.unsign(i);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(long[] input, boolean signed)
    {
        double sum = 0;

        if (signed)
        {
            for (long l : input)
                sum += l;

        }
        else
        {
            for (long l : input)
                sum += TypeUtil.unsign(l);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
     */
    public static double sum(float[] input)
    {
        double sum = 0;

        for (float f : input)
            sum += f;

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     * 
     * @param input
     *        the array to sum up
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
