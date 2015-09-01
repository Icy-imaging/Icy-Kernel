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
package icy.type.collection.array;

import icy.math.MathUtil;
import icy.type.DataType;
import icy.type.TypeUtil;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author Stephane
 */
public class Array1DUtil
{
    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(byte[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(short[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(int[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(long[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(float[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(double[] array)
    {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(byte[])} instead
     */
    @Deprecated
    public static int getTotalLenght(byte[] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(short[])} instead
     */
    @Deprecated
    public static int getTotalLenght(short[] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(int[])} instead
     */
    @Deprecated
    public static int getTotalLenght(int[] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(float[])} instead
     */
    @Deprecated
    public static int getTotalLenght(float[] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(double[])} instead
     */
    @Deprecated
    public static int getTotalLenght(double[] array)
    {
        return getTotalLength(array);
    }

    /**
     * Create a new 1D array with specified data type and length
     */
    public static Object createArray(DataType dataType, int len)
    {
        switch (dataType.getJavaType())
        {
            case BYTE:
                return new byte[len];
            case SHORT:
                return new short[len];
            case INT:
                return new int[len];
            case LONG:
                return new long[len];
            case FLOAT:
                return new float[len];
            case DOUBLE:
                return new double[len];
            default:
                return null;
        }
    }

    /**
     * Create a new 1D array with specified data type and length
     * 
     * @deprecated use {@link #createArray(DataType, int)} instead
     */
    @Deprecated
    public static Object createArray(int dataType, int len)
    {
        return createArray(DataType.getDataType(dataType), len);
    }

    /**
     * Allocate the specified 1D array if it's defined to null with the specified len
     */
    public static Object allocIfNull(Object out, DataType dataType, int len)
    {
        if (out == null)
        {
            switch (dataType.getJavaType())
            {
                case BYTE:
                    return new byte[len];
                case SHORT:
                    return new short[len];
                case INT:
                    return new int[len];
                case LONG:
                    return new long[len];
                case FLOAT:
                    return new float[len];
                case DOUBLE:
                    return new double[len];
            }
        }

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static boolean[] allocIfNull(boolean[] out, int len)
    {
        if (out == null)
            return new boolean[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static byte[] allocIfNull(byte[] out, int len)
    {
        if (out == null)
            return new byte[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static short[] allocIfNull(short[] out, int len)
    {
        if (out == null)
            return new short[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static int[] allocIfNull(int[] out, int len)
    {
        if (out == null)
            return new int[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static long[] allocIfNull(long[] out, int len)
    {
        if (out == null)
            return new long[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static float[] allocIfNull(float[] out, int len)
    {
        if (out == null)
            return new float[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified lenght
     */
    public static double[] allocIfNull(double[] out, int len)
    {
        if (out == null)
            return new double[len];

        return out;
    }

    /**
     * Do a copy of the specified array
     */
    public static Object copyOf(Object array)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                return Arrays.copyOf((byte[]) array, ((byte[]) array).length);
            case SHORT:
                return Arrays.copyOf((short[]) array, ((short[]) array).length);
            case INT:
                return Arrays.copyOf((int[]) array, ((int[]) array).length);
            case LONG:
                return Arrays.copyOf((long[]) array, ((long[]) array).length);
            case FLOAT:
                return Arrays.copyOf((float[]) array, ((float[]) array).length);
            case DOUBLE:
                return Arrays.copyOf((double[]) array, ((double[]) array).length);
            default:
                return null;
        }
    }

    //
    //
    //
    //

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data.
     * Use {@link #getValue(Object, int, DataType)} we you know the DataType as it is faster.
     */
    public static double getValue(Object array, int offset, boolean signed)
    {
        return getValue(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Get value as double from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static double getValue(Object array, int offset, DataType dataType)
    {
        switch (dataType)
        {
            case BYTE:
                return getValue((byte[]) array, offset, true);
            case UBYTE:
                return getValue((byte[]) array, offset, false);
            case SHORT:
                return getValue((short[]) array, offset, true);
            case USHORT:
                return getValue((short[]) array, offset, false);
            case INT:
                return getValue((int[]) array, offset, true);
            case UINT:
                return getValue((int[]) array, offset, false);
            case LONG:
                return getValue((long[]) array, offset, true);
            case ULONG:
                return getValue((long[]) array, offset, false);
            case FLOAT:
                return getValue((float[]) array, offset);
            case DOUBLE:
                return getValue((double[]) array, offset);
            default:
                return 0d;
        }
    }

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use {@link #getValue(Object, int, DataType)} instead
     */
    @Deprecated
    public static double getValue(Object array, int offset, int dataType, boolean signed)
    {
        return getValue(array, offset, DataType.getDataType(dataType, signed));
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static float getValueAsFloat(Object array, int offset, boolean signed)
    {
        return getValueAsFloat(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static float getValueAsFloat(Object array, int offset, DataType dataType)
    {
        switch (dataType)
        {
            case BYTE:
                return getValueAsFloat((byte[]) array, offset, true);
            case UBYTE:
                return getValueAsFloat((byte[]) array, offset, false);
            case SHORT:
                return getValueAsFloat((short[]) array, offset, true);
            case USHORT:
                return getValueAsFloat((short[]) array, offset, false);
            case INT:
                return getValueAsFloat((int[]) array, offset, true);
            case UINT:
                return getValueAsFloat((int[]) array, offset, false);
            case LONG:
                return getValueAsFloat((long[]) array, offset, true);
            case ULONG:
                return getValueAsFloat((long[]) array, offset, false);
            case FLOAT:
                return getValueAsFloat((float[]) array, offset);
            case DOUBLE:
                return getValueAsFloat((double[]) array, offset);
            default:
                return 0;
        }
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use {@link #getValueAsFloat(Object, int, DataType)} instead
     */
    @Deprecated
    public static float getValueAsFloat(Object array, int offset, int dataType, boolean signed)
    {
        return getValueAsFloat(array, offset, DataType.getDataType(dataType, signed));
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static int getValueAsInt(Object array, int offset, boolean signed)
    {
        return getValueAsInt(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static int getValueAsInt(Object array, int offset, DataType dataType)
    {
        switch (dataType)
        {
            case BYTE:
                return getValueAsInt((byte[]) array, offset, true);
            case UBYTE:
                return getValueAsInt((byte[]) array, offset, false);
            case SHORT:
                return getValueAsInt((short[]) array, offset, true);
            case USHORT:
                return getValueAsInt((short[]) array, offset, false);
            case INT:
            case UINT:
                return getValueAsInt((int[]) array, offset);
            case LONG:
            case ULONG:
                return getValueAsInt((long[]) array, offset);
            case FLOAT:
                return getValueAsInt((float[]) array, offset);
            case DOUBLE:
                return getValueAsInt((double[]) array, offset);
            default:
                return 0;
        }
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use {@link #getValueAsInt(Object, int, DataType)} instead
     */
    @Deprecated
    public static int getValueAsInt(Object array, int offset, int dataType, boolean signed)
    {
        return getValueAsInt(array, offset, DataType.getDataType(dataType, signed));
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static long getValueAsLong(Object array, int offset, boolean signed)
    {
        return getValueAsLong(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static long getValueAsLong(Object array, int offset, DataType dataType)
    {
        switch (dataType)
        {
            case BYTE:
                return getValueAsLong((byte[]) array, offset, true);
            case UBYTE:
                return getValueAsLong((byte[]) array, offset, false);
            case SHORT:
                return getValueAsLong((short[]) array, offset, true);
            case USHORT:
                return getValueAsLong((short[]) array, offset, false);
            case INT:
                return getValueAsLong((int[]) array, offset, true);
            case UINT:
                return getValueAsLong((int[]) array, offset, false);
            case LONG:
            case ULONG:
                return getValueAsLong((long[]) array, offset);
            case FLOAT:
                return getValueAsLong((float[]) array, offset);
            case DOUBLE:
                return getValueAsLong((double[]) array, offset);
            default:
                return 0;
        }
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use {@link #getValueAsLong(Object, int, DataType)} instead
     */
    @Deprecated
    public static long getValueAsLong(Object array, int offset, int dataType, boolean signed)
    {
        return getValueAsLong(array, offset, DataType.getDataType(dataType, signed));
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(Object array, int offset, double value)
    {
        setValue(array, offset, ArrayUtil.getDataType(array), value);
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(Object array, int offset, DataType dataType, double value)
    {
        switch (dataType.getJavaType())
        {
            case BYTE:
                setValue((byte[]) array, offset, value);
                break;

            case SHORT:
                setValue((short[]) array, offset, value);
                break;

            case INT:
                setValue((int[]) array, offset, value);
                break;

            case LONG:
                setValue((long[]) array, offset, value);
                break;

            case FLOAT:
                setValue((float[]) array, offset, value);
                break;

            case DOUBLE:
                setValue((double[]) array, offset, value);
                break;
        }
    }

    /**
     * Set value at specified offset as double value.
     * 
     * @deprecated use {@link #setValue(Object, int, DataType, double)} instead
     */
    @Deprecated
    public static void setValue(Object array, int offset, int dataType, double value)
    {
        setValue(array, offset, DataType.getDataType(dataType), value);
    }

    /**
     * Get value as double from specified byte array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(byte[] array, int offset, boolean signed)
    {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * Get value as double from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(short[] array, int offset, boolean signed)
    {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * Get value as double from specified int array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(int[] array, int offset, boolean signed)
    {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * Get value as double from specified long array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(long[] array, int offset, boolean signed)
    {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * Get value as double from specified float array and offset.
     */
    public static double getValue(float[] array, int offset)
    {
        return array[offset];
    }

    /**
     * Get value as double from specified double array and offset.
     */
    public static double getValue(double[] array, int offset)
    {
        return array[offset];
    }

    //

    /**
     * Get value as float from specified byte array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(byte[] array, int offset, boolean signed)
    {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * Get value as float from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(short[] array, int offset, boolean signed)
    {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * Get value as float from specified int array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(int[] array, int offset, boolean signed)
    {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * Get value as float from specified long array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(long[] array, int offset, boolean signed)
    {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * Get value as float from specified float array and offset.
     */
    public static float getValueAsFloat(float[] array, int offset)
    {
        return array[offset];
    }

    /**
     * Get value as float from specified double array and offset.
     */
    public static float getValueAsFloat(double[] array, int offset)
    {
        return (float) array[offset];
    }

    //

    /**
     * Get value as int from specified byte array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static int getValueAsInt(byte[] array, int offset, boolean signed)
    {
        return TypeUtil.toInt(array[offset], signed);
    }

    /**
     * Get value as int from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static int getValueAsInt(short[] array, int offset, boolean signed)
    {
        return TypeUtil.toInt(array[offset], signed);
    }

    /**
     * Get value as int from specified int array and offset.<br>
     */
    public static int getValueAsInt(int[] array, int offset)
    {
        // can't unsign here
        return array[offset];
    }

    /**
     * Get value as int from specified long array and offset.<br>
     */
    public static int getValueAsInt(long[] array, int offset)
    {
        return (int) array[offset];
    }

    /**
     * Get value as int from specified float array and offset.
     */
    public static int getValueAsInt(float[] array, int offset)
    {
        return (int) array[offset];
    }

    /**
     * Get value as int from specified double array and offset.
     */
    public static int getValueAsInt(double[] array, int offset)
    {
        return (int) array[offset];
    }

    //

    /**
     * Get value as int from specified byte array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static long getValueAsLong(byte[] array, int offset, boolean signed)
    {
        return TypeUtil.toLong(array[offset], signed);
    }

    /**
     * Get value as int from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static long getValueAsLong(short[] array, int offset, boolean signed)
    {
        return TypeUtil.toLong(array[offset], signed);

    }

    /**
     * Get value as int from specified int array and offset.<br>
     */
    public static long getValueAsLong(int[] array, int offset, boolean signed)
    {
        return TypeUtil.toLong(array[offset], signed);

    }

    /**
     * Get value as int from specified long array and offset.<br>
     */
    public static long getValueAsLong(long[] array, int offset)
    {
        // can't unsign here
        return array[offset];
    }

    /**
     * Get value as int from specified float array and offset.
     */
    public static long getValueAsLong(float[] array, int offset)
    {
        return (long) array[offset];
    }

    /**
     * Get value as int from specified double array and offset.
     */
    public static long getValueAsLong(double[] array, int offset)
    {
        return (long) array[offset];
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(byte[] array, int offset, double value)
    {
        array[offset] = (byte) value;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(short[] array, int offset, double value)
    {
        array[offset] = (short) value;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(int[] array, int offset, double value)
    {
        array[offset] = (int) value;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(long[] array, int offset, double value)
    {
        array[offset] = (long) value;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(float[] array, int offset, double value)
    {
        array[offset] = (float) value;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(double[] array, int offset, double value)
    {
        array[offset] = value;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayByteCompare(byte[] array1, byte[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayShortCompare(short[] array1, short[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayIntCompare(int[] array1, int[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayLongCompare(long[] array1, long[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayFloatCompare(float[] array1, float[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayDoubleCompare(double[] array1, double[] array2)
    {
        return Arrays.equals(array1, array2);
    }

    /**
     * Same as Arrays.fill() but applied to Object array from a double value
     */
    public static void fill(Object array, double value)
    {
        fill(array, 0, ArrayUtil.getLength(array), value);
    }

    /**
     * Same as Arrays.fill() but applied to Object array from a double value
     */
    public static void fill(Object array, int from, int to, double value)
    {
        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                fill((byte[]) array, from, to, (byte) value);
                break;

            case SHORT:
                fill((short[]) array, from, to, (short) value);
                break;

            case INT:
                fill((int[]) array, from, to, (int) value);
                break;

            case LONG:
                fill((long[]) array, from, to, (long) value);
                break;

            case FLOAT:
                fill((float[]) array, from, to, (float) value);
                break;

            case DOUBLE:
                fill((double[]) array, from, to, value);
                break;
        }
    }

    /**
     * Same as {@link Arrays#fill(byte[], int, int, byte)}
     */
    public static void fill(byte[] array, int from, int to, byte value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(short[], int, int, short)}
     */
    public static void fill(short[] array, int from, int to, short value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(int[], int, int, int)}
     */
    public static void fill(int[] array, int from, int to, int value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(long[], int, int, long)}
     */
    public static void fill(long[] array, int from, int to, long value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(float[], int, int, float)}
     */
    public static void fill(float[] array, int from, int to, float value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(double[], int, int, double)}
     */
    public static void fill(double[] array, int from, int to, double value)
    {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner.<br>
     * i.e: without overriding any data
     */
    public static void innerCopy(Object array, int from, int to, int cnt)
    {
        if (array == null)
            return;

        switch (ArrayUtil.getDataType(array))
        {
            case BYTE:
                Array1DUtil.innerCopy((byte[]) array, from, to, cnt);
                return;

            case SHORT:
                Array1DUtil.innerCopy((short[]) array, from, to, cnt);
                return;

            case INT:
                Array1DUtil.innerCopy((int[]) array, from, to, cnt);
                return;

            case LONG:
                Array1DUtil.innerCopy((long[]) array, from, to, cnt);
                return;

            case FLOAT:
                Array1DUtil.innerCopy((float[]) array, from, to, cnt);
                return;

            case DOUBLE:
                Array1DUtil.innerCopy((double[]) array, from, to, cnt);
                return;
        }

        // use generic code
        final int delta = to - from;

        if (delta == 0)
            return;

        final int length = Array.getLength(array);

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                Array.set(array, to_++, Array.get(array, from_++));
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                Array.set(array, --to_, Array.get(array, --from_));
        }

    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(byte[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(short[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(int[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(long[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(float[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     */
    public static void innerCopy(double[] array, int from, int to, int cnt)
    {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0)
        {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static byte[] toByteArray1D(byte[] in, byte[] out, int offset)
    {
        final int len = getTotalLength(in);
        final byte[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static short[] toShortArray1D(short[] in, short[] out, int offset)
    {
        final int len = getTotalLength(in);
        final short[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static int[] toIntArray1D(int[] in, int[] out, int offset)
    {
        final int len = getTotalLength(in);
        final int[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static long[] toLongArray1D(long[] in, long[] out, int offset)
    {
        final int len = getTotalLength(in);
        final long[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static float[] toFloatArray1D(float[] in, float[] out, int offset)
    {
        final int len = getTotalLength(in);
        final float[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * If (out == null) a new array is allocated.
     */
    public static double[] toDoubleArray1D(double[] in, double[] out, int offset)
    {
        final int len = getTotalLength(in);
        final double[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * Convert and return the 'in' 1D array in 'out' 1D array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        if input data are integer type then we assume them as signed data
     */
    public static Object arrayToArray(Object in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' 1D array in 'out' 1D array type.
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        if input data are integer type then we assume them as signed data
     */
    public static Object arrayToArray(Object in, Object out, boolean signed)
    {
        return arrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' double array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static Object doubleArrayToArray(double[] in, int inOffset, Object out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return doubleArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return doubleArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT:
                return doubleArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG:
                return doubleArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE:
                return doubleArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' double array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static Object doubleArrayToArray(double[] in, Object out)
    {
        return doubleArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * Convert and return the 'in' float array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static Object floatArrayToArray(float[] in, int inOffset, Object out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return floatArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return floatArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT:
                return floatArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG:
                return floatArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' float array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static Object floatArrayToArray(float[] in, Object out)
    {
        return floatArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * Convert and return the 'in' long array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object longArrayToArray(long[] in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return longArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return longArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT:
                return longArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG:
                return longArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT:
                return longArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE:
                return longArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' long array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object longArrayToArray(long[] in, Object out, boolean signed)
    {
        return longArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' integer array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object intArrayToArray(int[] in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return intArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return intArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT:
                return intArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG:
                return intArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' integer array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object intArrayToArray(int[] in, Object out, boolean signed)
    {
        return intArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' short array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object shortArrayToArray(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return shortArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return shortArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT:
                return shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG:
                return shortArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' short array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object shortArrayToArray(short[] in, Object out, boolean signed)
    {
        return shortArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' byte array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object byteArrayToArray(byte[] in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return byteArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT:
                return byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG:
                return byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' byte array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object byteArrayToArray(byte[] in, Object out, boolean signed)
    {
        return byteArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array in 'out' double array.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static double[] arrayToDoubleArray(Object in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToDoubleArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToDoubleArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToDoubleArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToDoubleArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToDoubleArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToDoubleArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' array in 'out' double array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static double[] arrayToDoubleArray(Object in, double[] out, boolean signed)
    {
        return arrayToDoubleArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array as a double array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static double[] arrayToDoubleArray(Object in, boolean signed)
    {
        return arrayToDoubleArray(in, 0, null, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array in 'out' float array.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static float[] arrayToFloatArray(Object in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToFloatArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToFloatArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToFloatArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToFloatArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToFloatArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToFloatArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' array in 'out' float array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static float[] arrayToFloatArray(Object in, float[] out, boolean signed)
    {
        return arrayToFloatArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array as a float array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static float[] arrayToFloatArray(Object in, boolean signed)
    {
        return arrayToFloatArray(in, 0, null, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array in 'out' int array.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static int[] arrayToIntArray(Object in, int inOffset, int[] out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToIntArray((int[]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToIntArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToIntArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToIntArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' array in 'out' int array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static int[] arrayToIntArray(Object in, int[] out, boolean signed)
    {
        return arrayToIntArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array as a int array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static int[] arrayToIntArray(Object in, boolean signed)
    {
        return arrayToIntArray(in, 0, null, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array in 'out' short array.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static short[] arrayToShortArray(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToShortArray((short[]) in, inOffset, out, outOffset, length);
            case INT:
                return intArrayToShortArray((int[]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToShortArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToShortArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToShortArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' array in 'out' short array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static short[] arrayToShortArray(Object in, short[] out, boolean signed)
    {
        return arrayToShortArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array as a short array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static short[] arrayToShortArray(Object in, boolean signed)
    {
        return arrayToShortArray(in, 0, null, 0, -1, signed);
    }

    /**
     * Convert and return the 'in' array in 'out' byte array.<br>
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static byte[] arrayToByteArray(Object in, int inOffset, byte[] out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);
            case SHORT:
                return shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length);
            case INT:
                return intArrayToByteArray((int[]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToByteArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToByteArray((double[]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' array in 'out' byte array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static byte[] arrayToByteArray(Object in, byte[] out)
    {
        return arrayToByteArray(in, 0, out, 0, -1);
    }

    /**
     * Convert and return the 'in' array as a byte array.<br>
     * 
     * @param in
     *        input array
     */
    public static byte[] arrayToByteArray(Object in)
    {
        return arrayToByteArray(in, 0, null, 0, -1);
    }

    public static double[] doubleArrayToDoubleArray(double[] in, int inOffset, double[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static float[] doubleArrayToFloatArray(double[] in, int inOffset, float[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (float) in[i + inOffset];

        return result;
    }

    public static long[] doubleArrayToLongArray(double[] in, int inOffset, long[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return result;
    }

    public static int[] doubleArrayToIntArray(double[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return result;
    }

    public static short[] doubleArrayToShortArray(double[] in, int inOffset, short[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte[] doubleArrayToByteArray(double[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double[] floatArrayToDoubleArray(float[] in, int inOffset, double[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = in[i + inOffset];

        return result;
    }

    public static float[] floatArrayToFloatArray(float[] in, int inOffset, float[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static long[] floatArrayToLongArray(float[] in, int inOffset, long[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return result;
    }

    public static int[] floatArrayToIntArray(float[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return result;
    }

    public static short[] floatArrayToShortArray(float[] in, int inOffset, short[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte[] floatArrayToByteArray(float[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double[] longArrayToDoubleArray(long[] in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float[] longArrayToFloatArray(long[] in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignF(in[i + inOffset]);
        }

        return result;
    }

    public static long[] longArrayToLongArray(long[] in, int inOffset, long[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static int[] longArrayToIntArray(long[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (int) in[i + inOffset];

        return result;
    }

    public static short[] longArrayToShortArray(long[] in, int inOffset, short[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte[] longArrayToByteArray(long[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double[] intArrayToDoubleArray(int[] in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float[] intArrayToFloatArray(int[] in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long[] intArrayToLongArray(int[] in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static int[] intArrayToIntArray(int[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static short[] intArrayToShortArray(int[] in, int inOffset, short[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte[] intArrayToByteArray(int[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double[] shortArrayToDoubleArray(short[] in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float[] shortArrayToFloatArray(short[] in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long[] shortArrayToLongArray(short[] in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return result;
    }

    public static int[] shortArrayToIntArray(short[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static short[] shortArrayToShortArray(short[] in, int inOffset, short[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static byte[] shortArrayToByteArray(short[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double[] byteArrayToDoubleArray(byte[] in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float[] byteArrayToFloatArray(byte[] in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long[] byteArrayToLongArray(byte[] in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return result;
    }

    public static int[] byteArrayToIntArray(byte[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static short[] byteArrayToShortArray(byte[] in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else
        {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = (short) TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static byte[] byteArrayToByteArray(byte[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static float[] doubleArrayToFloatArray(double[] array)
    {
        return doubleArrayToFloatArray(array, 0, null, 0, array.length);
    }

    public static long[] doubleArrayToLongArray(double[] array)
    {
        return doubleArrayToLongArray(array, 0, null, 0, array.length);
    }

    public static int[] doubleArrayToIntArray(double[] array)
    {
        return doubleArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[] doubleArrayToShortArray(double[] array)
    {
        return doubleArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[] doubleArrayToByteArray(double[] array)
    {
        return doubleArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[] floatArrayToDoubleArray(float[] array)
    {
        return floatArrayToDoubleArray(array, 0, null, 0, array.length);
    }

    public static long[] floatArrayToLongArray(float[] array)
    {
        return floatArrayToLongArray(array, 0, null, 0, array.length);
    }

    public static int[] floatArrayToIntArray(float[] array)
    {
        return floatArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[] floatArrayToShortArray(float[] array)
    {
        return floatArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[] floatArrayToByteArray(float[] array)
    {
        return floatArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[] longArrayToDoubleArray(long[] array, boolean signed)
    {
        return longArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[] longArrayToFloatArray(long[] array, boolean signed)
    {
        return longArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static short[] longArrayToShortArray(long[] array)
    {
        return longArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[] longArrayToByteArray(long[] array)
    {
        return longArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[] intArrayToDoubleArray(int[] array, boolean signed)
    {
        return intArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[] intArrayToFloatArray(int[] array, boolean signed)
    {
        return intArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long[] intArrayToLongArray(int[] array, boolean signed)
    {
        return intArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static short[] intArrayToShortArray(int[] array)
    {
        return intArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[] intArrayToByteArray(int[] array)
    {
        return intArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[] shortArrayToDoubleArray(short[] array, boolean signed)
    {
        return shortArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[] shortArrayToFloatArray(short[] array, boolean signed)
    {
        return shortArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long[] shortArrayToLongArray(short[] array, boolean signed)
    {
        return shortArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int[] shortArrayToIntArray(short[] array, boolean signed)
    {
        return shortArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[] shortArrayToByteArray(short[] array)
    {
        return shortArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[] byteArrayToDoubleArray(byte[] array, boolean signed)
    {
        return byteArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[] byteArrayToFloatArray(byte[] array, boolean signed)
    {
        return byteArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long[] byteArrayToLongArray(byte[] array, boolean signed)
    {
        return byteArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int[] byteArrayToIntArray(byte[] array, boolean signed)
    {
        return byteArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[] byteArrayToShortArray(byte[] array, boolean signed)
    {
        return byteArrayToShortArray(array, 0, null, 0, array.length, signed);
    }

    //
    //
    //
    //
    //
    //
    //
    //

    public static Object doubleArrayToSafeArray(double[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return doubleArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);
            case SHORT:
                return doubleArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT:
                return doubleArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG:
                return doubleArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE:
                return doubleArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default:
                return out;
        }
    }

    public static Object doubleArrayToSafeArray(double[] in, Object out, boolean signed)
    {
        return doubleArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object floatArrayToSafeArray(float[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return floatArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);
            case SHORT:
                return floatArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT:
                return floatArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG:
                return floatArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default:
                return out;
        }
    }

    public static Object floatArrayToSafeArray(float[] in, Object out, boolean signed)
    {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object longArrayToSafeArray(long[] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return longArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return longArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return longArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return longArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return longArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object longArrayToSafeArray(long[] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return longArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #longArrayToSafeArray(long[], int, Object, int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object longArrayToSafeArray(long[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);

    }

    /**
     * @deprecated Use {@link #longArrayToSafeArray(long[], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object longArrayToSafeArray(long[] in, Object out, boolean signed)
    {
        return longArrayToSafeArray(in, 0, out, 0, -1, signed, signed);
    }

    public static Object intArrayToSafeArray(int[] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return intArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return intArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return intArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return intArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object intArrayToSafeArray(int[] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #intArrayToSafeArray(int[], int, Object, int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object intArrayToSafeArray(int[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return intArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #intArrayToSafeArray(int[], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object intArrayToSafeArray(int[] in, Object out, boolean signed)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, signed, signed);
    }

    public static Object shortArrayToSafeArray(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return shortArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return shortArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned);
            case LONG:
                return shortArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object shortArrayToSafeArray(short[] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #shortArrayToSafeArray(short[], int, Object, int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object shortArrayToSafeArray(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return shortArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #shortArrayToSafeArray(short[], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object shortArrayToSafeArray(short[] in, Object out, boolean signed)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, signed, signed);

    }

    public static Object byteArrayToSafeArray(byte[] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return byteArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned);
            case INT:
                return byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned);
            case LONG:
                return byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object byteArrayToSafeArray(byte[] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return byteArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long[] arrayToSafeLongArray(Object in, int inOffset, long[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToLongArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToLongArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT:
                return intArrayToLongArray((int[]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG:
                return longArrayToSafeLongArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeLongArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeLongArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static long[] arrayToSafeLongArray(Object in, long[] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeLongArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #arrayToSafeLongArray(Object, int, long[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static long[] arrayToSafeLongArray(Object in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeLongArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeLongArray(Object, long[], boolean, boolean)} instead.
     */
    @Deprecated
    public static long[] arrayToSafeLongArray(Object in, long[] out, boolean signed)
    {
        return arrayToSafeLongArray(in, 0, out, 0, -1, signed, signed);
    }

    public static int[] arrayToSafeIntArray(Object in, int inOffset, int[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT:
                return intArrayToSafeIntArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeIntArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeIntArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeIntArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static int[] arrayToSafeIntArray(Object in, int[] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #arrayToSafeIntArray(Object, int, int[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static int[] arrayToSafeIntArray(Object in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeIntArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeIntArray(Object, int[], boolean, boolean)} instead.
     */
    @Deprecated
    public static int[] arrayToSafeIntArray(Object in, int[] out, boolean signed)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, signed, signed);
    }

    public static short[] arrayToSafeShortArray(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToSafeShortArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return intArrayToSafeShortArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeShortArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeShortArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeShortArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static short[] arrayToSafeShortArray(Object in, short[] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #arrayToSafeShortArray(Object, int, short[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static short[] arrayToSafeShortArray(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeShortArray(Object, short[], boolean, boolean)} instead.
     */
    @Deprecated
    public static short[] arrayToSafeShortArray(Object in, short[] out, boolean signed)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, signed, signed);
    }

    public static byte[] arrayToSafeByteArray(Object in, int inOffset, byte[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToSafeByteArray((byte[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return shortArrayToSafeByteArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return intArrayToSafeByteArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeByteArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeByteArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeByteArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static byte[] arrayToSafeByteArray(Object in, byte[] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeByteArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use {@link #arrayToSafeByteArray(Object, int, byte[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static byte[] arrayToSafeByteArray(Object in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeByteArray(Object, byte[], boolean, boolean)} instead.
     */
    @Deprecated
    public static byte[] arrayToSafeByteArray(Object in, byte[] out, boolean signed)
    {
        return arrayToSafeByteArray(in, 0, out, 0, -1, signed, signed);
    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    public static long[] doubleArrayToSafeLongArray(double[] in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            // by default value is clamped to [Long.MIN_VALUE..Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (long) in[i + inOffset];
        }
        else
        {
            final double minValue = 0d;
            final double maxValue = DataType.ULONG_MAX_VALUE;
            final long minValueT = 0L;
            final long maxValueT = 0xFFFFFFFFFFFFFFFFL;

            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final long result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toLong(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static int[] doubleArrayToSafeIntArray(double[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            // by default value is clamped to [Integer.MIN_VALUE..Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (int) in[i + inOffset];
        }
        else
        {
            final double minValue = 0d;
            final double maxValue = DataType.UINT_MAX_VALUE;
            final int minValueT = 0;
            final int maxValueT = 0xFFFFFFFF;

            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final int result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toInt(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static short[] doubleArrayToSafeShortArray(double[] in, int inOffset, short[] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final double minValue;
        final double maxValue;

        if (signed)
        {
            minValue = DataType.SHORT.getMinValue();
            maxValue = DataType.SHORT.getMaxValue();
        }
        else
        {
            minValue = DataType.USHORT.getMinValue();
            maxValue = DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++)
        {
            final double value = in[i + inOffset];
            final short result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte[] doubleArrayToSafeByteArray(double[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final double minValue;
        final double maxValue;

        if (signed)
        {
            minValue = DataType.BYTE.getMinValue();
            maxValue = DataType.BYTE.getMaxValue();
        }
        else
        {
            minValue = DataType.UBYTE.getMinValue();
            maxValue = DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++)
        {
            final double value = in[i + inOffset];
            final byte result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static long[] floatArrayToSafeLongArray(float[] in, int inOffset, long[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            // by default value is clamped to [Long.MIN_VALUE..Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (long) in[i + inOffset];
        }
        else
        {
            final float minValue = 0f;
            final float maxValue = DataType.ULONG_MAX_VALUE_F;
            final long minValueT = 0L;
            final long maxValueT = 0xFFFFFFFFFFFFFFFFL;

            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final long result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toLong(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static int[] floatArrayToSafeIntArray(float[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            // by default value is clamped to [Integer.MIN_VALUE..Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (int) in[i + inOffset];
        }
        else
        {
            final float minValue = 0f;
            final float maxValue = DataType.UINT_MAX_VALUE_F;
            final int minValueT = 0;
            final int maxValueT = 0xFFFFFFFF;

            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final int result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toInt(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static short[] floatArrayToSafeShortArray(float[] in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final float minValue;
        final float maxValue;

        if (signed)
        {
            minValue = (float) DataType.SHORT.getMinValue();
            maxValue = (float) DataType.SHORT.getMaxValue();
        }
        else
        {
            minValue = (float) DataType.USHORT.getMinValue();
            maxValue = (float) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++)
        {
            final float value = in[i + inOffset];
            final short result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte[] floatArrayToSafeByteArray(float[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final float minValue;
        final float maxValue;

        if (signed)
        {
            minValue = (float) DataType.BYTE.getMinValue();
            maxValue = (float) DataType.BYTE.getMaxValue();
        }
        else
        {
            minValue = (float) DataType.UBYTE.getMinValue();
            maxValue = (float) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++)
        {
            final float value = in[i + inOffset];
            final byte result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static long[] longArrayToSafeLongArray(long[] in, int inOffset, long[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        // same sign ?
        if (srcSigned == dstSigned)
            return longArrayToLongArray(in, inOffset, in, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);
        final long maxValue = Long.MAX_VALUE;

        for (int i = 0; i < len; i++)
        {
            long value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0)
            {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static int[] longArrayToSafeIntArray(long[] in, int inOffset, int[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned)
        {
            minValue = (long) DataType.INT.getMinValue();
            maxValue = (long) DataType.INT.getMaxValue();
        }
        else
        {
            minValue = (long) DataType.UINT.getMinValue();
            maxValue = (long) DataType.UINT.getMaxValue();
        }

        final int minValueT = (int) minValue;
        final int maxValueT = (int) maxValue;

        for (int i = 0; i < len; i++)
        {
            final long value = in[i + inOffset];
            final int result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (int) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static short[] longArrayToSafeShortArray(long[] in, int inOffset, short[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned)
        {
            minValue = (long) DataType.SHORT.getMinValue();
            maxValue = (long) DataType.SHORT.getMaxValue();
        }
        else
        {
            minValue = (long) DataType.USHORT.getMinValue();
            maxValue = (long) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++)
        {
            final long value = in[i + inOffset];
            final short result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte[] longArrayToSafeByteArray(long[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned)
        {
            minValue = (long) DataType.BYTE.getMinValue();
            maxValue = (long) DataType.BYTE.getMaxValue();
        }
        else
        {
            minValue = (long) DataType.UBYTE.getMinValue();
            maxValue = (long) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++)
        {
            final long value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    /**
     * @deprecated Use {@link #longArrayToSafeIntArray(long[], int, int[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static int[] longArrayToSafeIntArray(long[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeIntArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #longArrayToSafeShortArray(long[], int, short[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static short[] longArrayToSafeShortArray(long[] in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #longArrayToSafeByteArray(long[], int, byte[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static byte[] longArrayToSafeByteArray(long[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static int[] intArrayToSafeIntArray(int[] in, int inOffset, int[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        // same sign ?
        if (srcSigned == dstSigned)
            return intArrayToIntArray(in, inOffset, in, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);
        final int maxValue = Integer.MAX_VALUE;

        for (int i = 0; i < len; i++)
        {
            int value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0)
            {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static short[] intArrayToSafeShortArray(int[] in, int inOffset, short[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final int minValue;
        final int maxValue;

        if (dstSigned)
        {
            minValue = (int) DataType.SHORT.getMinValue();
            maxValue = (int) DataType.SHORT.getMaxValue();
        }
        else
        {
            minValue = (int) DataType.USHORT.getMinValue();
            maxValue = (int) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++)
        {
            final int value = in[i + inOffset];
            final short result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte[] intArrayToSafeByteArray(int[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final int minValue;
        final int maxValue;

        if (dstSigned)
        {
            minValue = (int) DataType.BYTE.getMinValue();
            maxValue = (int) DataType.BYTE.getMaxValue();
        }
        else
        {
            minValue = (int) DataType.UBYTE.getMinValue();
            maxValue = (int) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++)
        {
            final int value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    /**
     * @deprecated Use {@link #intArrayToSafeShortArray(int[], int, short[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static short[] intArrayToSafeShortArray(int[] in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        return intArrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);

    }

    /**
     * @deprecated Use {@link #intArrayToSafeByteArray(int[], int, byte[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static byte[] intArrayToSafeByteArray(int[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        return intArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static short[] shortArrayToSafeShortArray(short[] in, int inOffset, short[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        // same sign ?
        if (srcSigned == dstSigned)
            return shortArrayToShortArray(in, inOffset, in, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);
        final short maxValue = Short.MAX_VALUE;

        for (int i = 0; i < len; i++)
        {
            short value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0)
            {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static byte[] shortArrayToSafeByteArray(short[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final short minValue;
        final short maxValue;

        if (dstSigned)
        {
            minValue = (short) DataType.BYTE.getMinValue();
            maxValue = (short) DataType.BYTE.getMaxValue();
        }
        else
        {
            minValue = (short) DataType.UBYTE.getMinValue();
            maxValue = (short) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++)
        {
            final short value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    /**
     * @deprecated Use {@link #shortArrayToSafeByteArray(short[], int, byte[], int, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static byte[] shortArrayToSafeByteArray(short[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        return shortArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static byte[] byteArrayToSafeByteArray(byte[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        // same sign ?
        if (srcSigned == dstSigned)
            return byteArrayToByteArray(in, inOffset, in, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);
        final byte maxValue = Byte.MAX_VALUE;

        for (int i = 0; i < len; i++)
        {
            byte value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0)
            {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    //
    //
    //
    //
    //
    //
    //

    public static long[] doubleArrayToSafeLongArray(double[] array, boolean signed)
    {
        return doubleArrayToSafeLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int[] doubleArrayToSafeIntArray(double[] array, boolean signed)
    {
        return doubleArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[] doubleArrayToSafeShortArray(double[] array, boolean signed)
    {
        return doubleArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[] doubleArrayToSafeByteArray(double[] array, boolean signed)
    {
        return doubleArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static long[] floatArrayToSafeLongArray(float[] array, boolean signed)
    {
        return floatArrayToSafeLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int[] floatArrayToSafeIntArray(float[] array, boolean signed)
    {
        return floatArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[] floatArrayToSafeShortArray(float[] array, boolean signed)
    {
        return floatArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[] floatArrayToSafeByteArray(float[] array, boolean signed)
    {
        return floatArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static int[] longArrayToSafeIntArray(long[] array, boolean signed)
    {
        return longArrayToSafeIntArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static short[] longArrayToSafeShortArray(long[] array, boolean signed)
    {
        return longArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[] longArrayToSafeByteArray(long[] array, boolean signed)
    {
        return longArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static short[] intArrayToSafeShortArray(int[] array, boolean signed)
    {
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[] intArrayToSafeByteArray(int[] array, boolean signed)
    {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[] shortArrayToSafeByteArray(short[] array, boolean signed)
    {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    //
    //
    //
    //

    /**
     * Return the specified 1D array as string<br>
     * Default representation use ':' as separator character<br>
     * <br>
     * ex : [0,1,2,3,4] --> "0:1:2:3:4"<br>
     * 
     * @param array
     *        1D array containing values to return as string
     */
    public static String arrayToString(Object array)
    {
        return arrayToString(array, false, false, ":", -1);
    }

    /**
     * Return the specified 1D array as string<br>
     * ex : [0,1,2,3,4] --> "0:1:2:3:4"<br>
     * ex : [Obj0,Obj1,Obj2,Obj3,Obj4] --> "Obj0:Obj1:Obj2:Obj3:Obj4"<br>
     * 
     * @param array
     *        1D array containing values to return as string
     * @param signed
     *        input value are signed (only for integer data type)
     * @param hexa
     *        set value in resulting string in hexa decimal format (only for integer data type)
     * @param separator
     *        specify the separator to use between each array value in resulting string
     * @param size
     *        specify the number of significant number to display for float value (-1 means all)
     */
    public static String arrayToString(Object array, boolean signed, boolean hexa, String separator, int size)
    {
        final int len = ArrayUtil.getLength(array);
        final DataType dataType = ArrayUtil.getDataType(array, signed);
        final StringBuilder result = new StringBuilder();
        final int base = hexa ? 16 : 10;

        switch (dataType)
        {
            case UBYTE:
            {
                final byte[] data = (byte[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0] & 0xFF, base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Integer.toString(data[i] & 0xFF, base));
                }
                break;
            }

            case BYTE:
            {
                final byte[] data = (byte[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case USHORT:
            {
                final short[] data = (short[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0] & 0xFFFF, base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Integer.toString(data[i] & 0xFFFF, base));
                }
                break;
            }
            case SHORT:
            {
                final short[] data = (short[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case UINT:
            {
                final int[] data = (int[]) array;

                if (len > 0)
                    result.append(Long.toString(data[0] & 0xFFFFFFFFL, base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Long.toString(data[i] & 0xFFFFFFFFL, base));
                }
                break;
            }

            case INT:
            {
                final int[] data = (int[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case ULONG:
            {
                final long[] data = (long[]) array;

                // we lost highest bit as java doesn't have bigger than long type
                if (len > 0)
                    result.append(Long.toString(data[0] & 0x7FFFFFFFFFFFFFFFL, base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Long.toString(data[i] & 0x7FFFFFFFFFFFFFFFL, base));
                }
                break;
            }

            case LONG:
            {
                final long[] data = (long[]) array;

                if (len > 0)
                    result.append(Long.toString(data[0], base));
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Long.toString(data[i], base));
                }
                break;
            }

            case FLOAT:
            {
                final float[] data = (float[]) array;

                if (size == -1)
                {
                    if (len > 0)
                        result.append(data[0]);
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(data[i]);
                    }
                }
                else
                {
                    if (len > 0)
                        result.append(Double.toString(MathUtil.roundSignificant(data[0], size, true)));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Double.toString(MathUtil.roundSignificant(data[i], size, true)));
                    }
                }
                break;
            }

            case DOUBLE:
            {
                final double[] data = (double[]) array;

                if (size == -1)
                {
                    if (len > 0)
                        result.append(data[0]);
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(data[i]);
                    }
                }
                else
                {
                    if (len > 0)
                        result.append(Double.toString(MathUtil.roundSignificant(data[0], size, true)));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Double.toString(MathUtil.roundSignificant(data[i], size, true)));
                    }
                }
                break;
            }

            // generic method
            default:
            {
                if (len > 0)
                    result.append(Array.get(array, 0).toString());
                for (int i = 1; i < len; i++)
                {
                    result.append(separator);
                    result.append(Array.get(array, i).toString());
                }
            }
        }

        return result.toString();
    }

    /**
     * Return the specified string containing separated values as a 1D array<br>
     * By default separator is assumed to be ':' character<br>
     * ex : "0:1:2:3:4" --> [0,1,2,3,4]<br>
     * 
     * @param value
     *        string containing value to return as 1D array
     * @param dataType
     *        specify the values data type and also the output array data type string
     */
    public static Object stringToArray(String value, DataType dataType)
    {
        return stringToArray(value, dataType, false, ":");
    }

    /**
     * @deprecated use {@link #stringToArray(String, DataType)} instead
     */
    @Deprecated
    public static Object stringToArray(String value, int dataType)
    {
        return stringToArray(value, DataType.getDataType(dataType), false, ":");
    }

    /**
     * Return the specified string containing separated values as a 1D array<br>
     * ex : "0:1:2:3:4" --> [0,1,2,3,4]<br>
     * 
     * @param value
     *        string containing value to return as 1D array
     * @param dataType
     *        specify the values data type and also the output array data type string
     * @param hexa
     *        values in string as stored as hexa values (only for integer data type)
     * @param separator
     *        specify the separator used between each value in the input string
     */
    public static Object stringToArray(String value, DataType dataType, boolean hexa, String separator)
    {
        if (value == null)
            return createArray(dataType, 0);

        final String[] values = value.split(separator);
        final int len = values.length;
        final int base = hexa ? 16 : 10;

        switch (dataType.getJavaType())
        {
            case BYTE:
            {
                final byte[] result = new byte[len];

                for (int i = 0; i < len; i++)
                    result[i] = (byte) Integer.parseInt(values[i], base);

                return result;
            }

            case SHORT:
            {
                final short[] result = new short[len];

                for (int i = 0; i < len; i++)
                    result[i] = (short) Integer.parseInt(values[i], base);

                return result;
            }

            case INT:
            {
                final int[] result = new int[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }

            case LONG:
            {
                final long[] result = new long[len];

                for (int i = 0; i < len; i++)
                    result[i] = Long.parseLong(values[i], base);

                return result;
            }

            case FLOAT:
            {
                final float[] result = new float[len];

                for (int i = 0; i < len; i++)
                    result[i] = Float.parseFloat(values[i]);

                return result;
            }

            case DOUBLE:
            {
                final double[] result = new double[len];

                for (int i = 0; i < len; i++)
                    result[i] = Double.parseDouble(values[i]);

                return result;
            }
        }

        return null;
    }

    /**
     * @deprecated use {@link #stringToArray(String, DataType, boolean, String)} instead
     */
    @Deprecated
    public static Object stringToArray(String value, int dataType, boolean hexa, String separator)
    {
        return stringToArray(value, DataType.getDataType(dataType, false), hexa, separator);
    }

    //
    //
    //
    //

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     */
    public static byte[] toByteArray(boolean[] array)
    {
        return toByteArray(array, null, 0);
    }

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static byte[] toByteArray(boolean[] in, byte[] out, int offset)
    {
        if (in == null)
            return new byte[0];

        final int len = in.length;
        final byte[] result = allocIfNull(out, offset + len);

        for (int i = 0; i < len; i++)
            result[i] = (byte) ((in[i]) ? 1 : 0);

        return result;
    }

    /**
     * Convert a byte array (unpacked form : 1 byte --> 1 boolean) to a boolean array
     */
    public static boolean[] toBooleanArray(byte[] array)
    {
        return toBooleanArray(array, null, 0);
    }

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static boolean[] toBooleanArray(byte[] in, boolean[] out, int offset)
    {
        if (in == null)
            return new boolean[0];

        final int len = in.length;
        final boolean[] result = allocIfNull(out, offset + len);

        for (int i = 0; i < len; i++)
            result[i] = (in[i] != 0) ? true : false;

        return result;
    }

    /**
     * Retrieve interleaved byte data from 'in' array and return the result in the 'out' array.
     * 
     * @param in
     *        input byte array containing interleaved data
     * @param inOffset
     *        input array offset
     * @param step
     *        interleave step
     * @param out
     *        output result array. If set to <code>null</code> then a new array is allocated.
     * @param outOffset
     *        output array offset
     * @param size
     *        number of byte to retrieve
     * @return byte array containing de-interleaved data.
     */
    public static byte[] getInterleavedData(byte[] in, int inOffset, int step, byte[] out, int outOffset, int size)
    {
        final byte[] result = allocIfNull(out, outOffset + size);

        int inOff = inOffset;
        int outOff = outOffset;
        
        for (int i = 0; i < size; i++)
        {
            out[outOff] = in[inOff];
            inOff += step;
            outOff++;
        }

        return result;
    }

    /**
     * De interleave data from 'in' array and return the result in the 'out' array.
     * 
     * @param in
     *        input byte array containing interleaved data
     * @param inOffset
     *        input array offset
     * @param step
     *        interleave step
     * @param out
     *        output result array. If set to <code>null</code> then a new array is allocated
     * @param outOffset
     *        output array offset
     * @param size
     *        number of element to de-interleave
     * @return byte array containing de-interleaved data.
     */
    public static byte[] deInterleave(byte[] in, int inOffset, int step, byte[] out, int outOffset, int size)
    {
        final byte[] result = allocIfNull(out, outOffset + (size * step));

        int inOff1 = inOffset;
        int outOff1 = outOffset;
        
        for (int j = 0; j < step; j++)
        {
            int inOff2 = inOff1;
            int outOff2 = outOff1;

            for (int i = 0; i < size; i++)
            {
                out[outOff2] = in[inOff2];
                inOff2 += step;
                outOff2++;
            }

            inOff1++;
            outOff1 += size;
        }

        return result;
    }
}
