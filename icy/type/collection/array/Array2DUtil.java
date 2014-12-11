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

import icy.type.DataType;

/**
 * @author Stephane
 */
public class Array2DUtil
{
    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(byte[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(short[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(int[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(long[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(float[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);

        }

        return result;
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(double[][] array)
    {
        int result = 0;

        if (array != null)
        {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                result += Array1DUtil.getTotalLength(array[i]);
        }

        return result;
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(byte[][])} instead
     */
    @Deprecated
    public static int getTotalLenght(byte[][] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(short[][])} instead
     */
    @Deprecated
    public static int getTotalLenght(short[][] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(int[][])} instead
     */
    @Deprecated
    public static int getTotalLenght(int[][] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(float[][])} instead
     */
    @Deprecated
    public static int getTotalLenght(float[][] array)
    {
        return getTotalLength(array);
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(double[][])} instead
     */
    @Deprecated
    public static int getTotalLenght(double[][] array)
    {
        return getTotalLength(array);
    }

    /**
     * Create a new 2D array with specified data type and length
     */
    public static Object[] createArray(DataType dataType, int len)
    {
        switch (dataType.getJavaType())
        {
            case BYTE:
                return new byte[len][];
            case SHORT:
                return new short[len][];
            case INT:
                return new int[len][];
            case LONG:
                return new long[len][];
            case FLOAT:
                return new float[len][];
            case DOUBLE:
                return new double[len][];
            default:
                return null;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static Object[] createArray(int dataType, int len)
    {
        return createArray(DataType.getDataType(dataType), len);
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static byte[][] allocIfNull(byte[][] out, int len)
    {
        if (out == null)
            return new byte[len][];

        return out;
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static short[][] allocIfNull(short[][] out, int len)
    {
        if (out == null)
            return new short[len][];

        return out;
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static int[][] allocIfNull(int[][] out, int len)
    {
        if (out == null)
            return new int[len][];

        return out;
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static long[][] allocIfNull(long[][] out, int len)
    {
        if (out == null)
            return new long[len][];

        return out;
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static float[][] allocIfNull(float[][] out, int len)
    {
        if (out == null)
            return new float[len][];

        return out;
    }

    /**
     * Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static double[][] allocIfNull(double[][] out, int len)
    {
        if (out == null)
            return new double[len][];

        return out;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayByteCompare(byte[][] array1, byte[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayByteCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayShortCompare(short[][] array1, short[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayShortCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayIntCompare(int[][] array1, int[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayIntCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayLongCompare(long[][] array1, long[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayLongCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayFloatCompare(float[][] array1, float[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayFloatCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return true is the specified arrays are equals
     */
    public static boolean arrayDoubleCompare(double[][] array1, double[][] array2)
    {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayDoubleCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Return the multi dimension 'in' array as a single dimension byte array.
     */
    public static byte[] toByteArray1D(byte[][] in)
    {
        return toByteArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension short array.
     */
    public static short[] toShortArray1D(short[][] in)
    {
        return toShortArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension int array.
     */
    public static int[] toIntArray(int[][] in)
    {
        return toIntArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension float array.
     */
    public static float[] toFloatArray1D(float[][] in)
    {
        return toFloatArray1D(in, null, 0);
    }

    /**
     * Return the multi dimension 'in' array as a single dimension double array.
     */
    public static double[] toDoubleArray1D(double[][] in)
    {
        return toDoubleArray1D(in, null, 0);
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static byte[] toByteArray1D(byte[][] in, byte[] out, int offset)
    {
        final byte[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final byte[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toByteArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static short[] toShortArray1D(short[][] in, short[] out, int offset)
    {
        final short[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final short[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toShortArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static int[] toIntArray1D(int[][] in, int[] out, int offset)
    {
        final int[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final int[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toIntArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static long[] toLongArray1D(long[][] in, long[] out, int offset)
    {
        final long[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final long[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toLongArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static float[] toFloatArray1D(float[][] in, float[] out, int offset)
    {
        final float[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final float[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toFloatArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static double[] toDoubleArray1D(double[][] in, double[] out, int offset)
    {
        final double[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null)
        {
            final int len = in.length;

            int off = offset;
            for (int i = 0; i < len; i++)
            {
                final double[] s_in = in[i];

                if (s_in != null)
                {
                    Array1DUtil.toDoubleArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * Convert and return the 'in' 2D array in 'out' 2D array type.
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
                return byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
            default:
                return out;
        }
    }

    /**
     * Convert and return the 'in' 2D array in 'out' 2D array type.
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
    public static Object doubleArrayToArray(double[][] in, int inOffset, Object out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return doubleArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return doubleArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT:
                return doubleArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG:
                return doubleArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE:
                return doubleArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
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
    public static Object doubleArrayToArray(double[][] in, Object out)
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
    public static Object floatArrayToArray(float[][] in, int inOffset, Object out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return floatArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return floatArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT:
                return floatArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG:
                return floatArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
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
    public static Object floatArrayToArray(float[][] in, Object out)
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
    public static Object longArrayToArray(long[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return longArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return longArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT:
                return longArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG:
                return longArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT:
                return longArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE:
                return longArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
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
    public static Object longArrayToArray(long[][] in, Object out, boolean signed)
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
    public static Object intArrayToArray(int[][] in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return intArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return intArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT:
                return intArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG:
                return intArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
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
    public static Object intArrayToArray(int[][] in, Object out, boolean signed)
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
    public static Object shortArrayToArray(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return shortArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return shortArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT:
                return shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG:
                return shortArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
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
    public static Object shortArrayToArray(short[][] in, Object out, boolean signed)
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
    public static Object byteArrayToArray(byte[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return byteArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT:
                return byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG:
                return byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
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
    public static Object byteArrayToArray(byte[][] in, Object out, boolean signed)
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
    public static double[][] arrayToDoubleArray(Object in, int inOffset, double[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToDoubleArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToDoubleArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToDoubleArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToDoubleArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToDoubleArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToDoubleArray((double[][]) in, inOffset, out, outOffset, length);
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
    public static double[][] arrayToDoubleArray(Object in, double[][] out, boolean signed)
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
    public static double[][] arrayToDoubleArray(Object in, boolean signed)
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
    public static float[][] arrayToFloatArray(Object in, int inOffset, float[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToFloatArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToFloatArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToFloatArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG:
                return longArrayToFloatArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToFloatArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToFloatArray((double[][]) in, inOffset, out, outOffset, length);
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
    public static float[][] arrayToFloatArray(Object in, float[][] out, boolean signed)
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
    public static float[][] arrayToFloatArray(Object in, boolean signed)
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
    public static int[][] arrayToIntArray(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT:
                return intArrayToIntArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToIntArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToIntArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToIntArray((double[][]) in, inOffset, out, outOffset, length);
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
    public static int[][] arrayToIntArray(Object in, int[][] out, boolean signed)
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
    public static int[][] arrayToIntArray(Object in, boolean signed)
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
    public static short[][] arrayToShortArray(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT:
                return shortArrayToShortArray((short[][]) in, inOffset, out, outOffset, length);
            case INT:
                return intArrayToShortArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToShortArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToShortArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToShortArray((double[][]) in, inOffset, out, outOffset, length);
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
    public static short[][] arrayToShortArray(Object in, short[][] out, boolean signed)
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
    public static short[][] arrayToShortArray(Object in, boolean signed)
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
    public static byte[][] arrayToByteArray(Object in, int inOffset, byte[][] out, int outOffset, int length)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToByteArray((byte[][]) in, inOffset, out, outOffset, length);
            case SHORT:
                return shortArrayToByteArray((short[][]) in, inOffset, out, outOffset, length);
            case INT:
                return intArrayToByteArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG:
                return longArrayToByteArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT:
                return floatArrayToByteArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE:
                return doubleArrayToByteArray((double[][]) in, inOffset, out, outOffset, length);
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
    public static byte[][] arrayToByteArray(Object in, byte[][] out)
    {
        return arrayToByteArray(in, 0, out, 0, -1);
    }

    /**
     * Convert and return the 'in' array as a byte array.<br>
     * 
     * @param in
     *        input array
     */
    public static byte[][] arrayToByteArray(Object in)
    {
        return arrayToByteArray(in, 0, null, 0, -1);
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

    public static double[][] doubleArrayToDoubleArray(double[][] in, int inOffset, double[][] out, int outOffset,
            int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static float[][] doubleArrayToFloatArray(double[][] in, int inOffset, float[][] out, int outOffset,
            int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static long[][] doubleArrayToLongArray(double[][] in, int inOffset, long[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static int[][] doubleArrayToIntArray(double[][] in, int inOffset, int[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil
                    .doubleArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] doubleArrayToShortArray(double[][] in, int inOffset, short[][] out, int outOffset,
            int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] doubleArrayToByteArray(double[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static double[][] floatArrayToDoubleArray(float[][] in, int inOffset, double[][] out, int outOffset,
            int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static float[][] floatArrayToFloatArray(float[][] in, int inOffset, float[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static long[][] floatArrayToLongArray(float[][] in, int inOffset, long[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil
                    .floatArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static int[][] floatArrayToIntArray(float[][] in, int inOffset, int[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] floatArrayToShortArray(float[][] in, int inOffset, short[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] floatArrayToByteArray(float[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil
                    .floatArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] longArrayToDoubleArray(long[][] in, int inOffset, double[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] longArrayToFloatArray(long[][] in, int inOffset, float[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static long[][] longArrayToLongArray(long[][] in, int inOffset, long[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static int[][] longArrayToIntArray(long[][] in, int inOffset, int[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] longArrayToShortArray(long[][] in, int inOffset, short[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil
                    .longArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static byte[][] longArrayToByteArray(long[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] intArrayToDoubleArray(int[][] in, int inOffset, double[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] intArrayToFloatArray(int[][] in, int inOffset, float[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static long[][] intArrayToLongArray(int[][] in, int inOffset, long[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static int[][] intArrayToIntArray(int[][] in, int inOffset, int[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] intArrayToShortArray(int[][] in, int inOffset, short[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static byte[][] intArrayToByteArray(int[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] shortArrayToDoubleArray(short[][] in, int inOffset, double[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] shortArrayToFloatArray(short[][] in, int inOffset, float[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static long[][] shortArrayToLongArray(short[][] in, int inOffset, long[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static int[][] shortArrayToIntArray(short[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static short[][] shortArrayToShortArray(short[][] in, int inOffset, short[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] shortArrayToByteArray(short[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil
                    .shortArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] byteArrayToDoubleArray(byte[][] in, int inOffset, double[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] byteArrayToFloatArray(byte[][] in, int inOffset, float[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static long[][] byteArrayToLongArray(byte[][] in, int inOffset, long[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static int[][] byteArrayToIntArray(byte[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static short[][] byteArrayToShortArray(byte[][] in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static byte[][] byteArrayToByteArray(byte[][] in, int inOffset, byte[][] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static float[][] doubleArrayToFloatArray(double[][] array)
    {
        return doubleArrayToFloatArray(array, 0, null, 0, array.length);
    }

    public static int[][] doubleArrayToIntArray(double[][] array)
    {
        return doubleArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[][] doubleArrayToShortArray(double[][] array)
    {
        return doubleArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] doubleArrayToByteArray(double[][] array)
    {
        return doubleArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] floatArrayToDoubleArray(float[][] array)
    {
        return floatArrayToDoubleArray(array, 0, null, 0, array.length);
    }

    public static int[][] floatArrayToIntArray(float[][] array)
    {
        return floatArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[][] floatArrayToShortArray(float[][] array)
    {
        return floatArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] floatArrayToByteArray(float[][] array)
    {
        return floatArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] intArrayToDoubleArray(int[][] array, boolean signed)
    {
        return intArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] intArrayToFloatArray(int[][] array, boolean signed)
    {
        return intArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] intArrayToShortArray(int[][] array)
    {
        return intArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] intArrayToByteArray(int[][] array)
    {
        return intArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] shortArrayToDoubleArray(short[][] array, boolean signed)
    {
        return shortArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] shortArrayToFloatArray(short[][] array, boolean signed)
    {
        return shortArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] shortArrayToIntArray(short[][] array, boolean signed)
    {
        return shortArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] shortArrayToByteArray(short[][] array)
    {
        return shortArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] byteArrayToDoubleArray(byte[][] array, boolean signed)
    {
        return byteArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] byteArrayToFloatArray(byte[][] array, boolean signed)
    {
        return byteArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] byteArrayToIntArray(byte[][] array, boolean signed)
    {
        return byteArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] byteArrayToShortArray(byte[][] array, boolean signed)
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

    public static Object doubleArrayToSafeArray(double[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return doubleArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);

            case SHORT:
                return doubleArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);

            case INT:
                return doubleArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case LONG:
                return doubleArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);

            case FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);

            case DOUBLE:
                return doubleArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);

            default:
                return out;
        }
    }

    public static Object doubleArrayToSafeArray(double[][] in, Object out, boolean signed)
    {
        return doubleArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object floatArrayToSafeArray(float[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return floatArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);
            case SHORT:
                return floatArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT:
                return floatArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG:
                return floatArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            default:
                return out;
        }
    }

    public static Object floatArrayToSafeArray(float[][] in, Object out, boolean signed)
    {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object longArrayToSafeArray(long[][] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return longArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return longArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return longArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return longArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return longArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object longArrayToSafeArray(long[][] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return longArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #longArrayToSafeArray(long[][], int, Object, int, int, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static Object longArrayToSafeArray(long[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #longArrayToSafeArray(long[][], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object longArrayToSafeArray(long[][] in, Object out, boolean signed)
    {
        return longArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object intArrayToSafeArray(int[][] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return intArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return intArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return intArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return intArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object intArrayToSafeArray(int[][] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #intArrayToSafeArray(int[][], int, Object, int, int, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static Object intArrayToSafeArray(int[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return intArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #intArrayToSafeArray(int[][], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object intArrayToSafeArray(int[][] in, Object out, boolean signed)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object shortArrayToSafeArray(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return shortArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return shortArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned,
                        dstSigned);
            case INT:
                return shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned);
            case LONG:
                return shortArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object shortArrayToSafeArray(short[][] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #shortArrayToSafeArray(short[][], int, Object, int, int, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static Object shortArrayToSafeArray(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return shortArrayToSafeArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #shortArrayToSafeArray(short[][], Object, boolean, boolean)} instead.
     */
    @Deprecated
    public static Object shortArrayToSafeArray(short[][] in, Object out, boolean signed)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object byteArrayToSafeArray(byte[][] in, int inOffset, Object out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(out))
        {
            case BYTE:
                return byteArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned);
            case INT:
                return byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned);
            case LONG:
                return byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default:
                return out;
        }
    }

    public static Object byteArrayToSafeArray(byte[][] in, Object out, boolean srcSigned, boolean dstSigned)
    {
        return byteArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long[][] arrayToSafeLongArray(Object in, int inOffset, long[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToLongArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToLongArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT:
                return intArrayToLongArray((int[][]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG:
                return longArrayToSafeLongArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeLongArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeLongArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;

        }
    }

    public static long[][] arrayToSafeLongArray(Object in, long[][] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeLongArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #arrayToSafeLongArray(Object, int, long[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static long[][] arrayToSafeLongArray(Object in, int inOffset, long[][] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeLongArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeLongArray(Object, long[][], boolean, boolean)} instead
     */
    @Deprecated
    public static long[][] arrayToSafeLongArray(Object in, long[][] out, boolean signed)
    {
        return arrayToSafeLongArray(in, 0, out, 0, -1, signed, signed);
    }

    public static int[][] arrayToSafeIntArray(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT:
                return intArrayToSafeIntArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeIntArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeIntArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeIntArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static int[][] arrayToSafeIntArray(Object in, int[][] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #arrayToSafeIntArray(Object, int, int[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static int[][] arrayToSafeIntArray(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeIntArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeIntArray(Object, int[][], boolean, boolean)} instead
     */
    @Deprecated
    public static int[][] arrayToSafeIntArray(Object in, int[][] out, boolean signed)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, signed, signed);
    }

    public static short[][] arrayToSafeShortArray(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT:
                return shortArrayToSafeShortArray((short[][]) in, inOffset, out, outOffset, length, srcSigned,
                        dstSigned);
            case INT:
                return intArrayToSafeShortArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeShortArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeShortArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeShortArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static short[][] arrayToSafeShortArray(Object in, short[][] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #arrayToSafeShortArray(Object, int, short[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static short[][] arrayToSafeShortArray(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeShortArray(Object, short[][], boolean, boolean)} instead
     */
    @Deprecated
    public static short[][] arrayToSafeShortArray(Object in, short[][] out, boolean signed)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, signed, signed);
    }

    public static byte[][] arrayToSafeByteArray(Object in, int inOffset, byte[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToSafeByteArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT:
                return shortArrayToSafeByteArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT:
                return intArrayToSafeByteArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG:
                return longArrayToSafeByteArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT:
                return floatArrayToSafeByteArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE:
                return doubleArrayToSafeByteArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default:
                return out;
        }
    }

    public static byte[][] arrayToSafeByteArray(Object in, byte[][] out, boolean srcSigned, boolean dstSigned)
    {
        return arrayToSafeByteArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    /**
     * @deprecated Use
     *             {@link #arrayToSafeByteArray(Object, int, byte[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static byte[][] arrayToSafeByteArray(Object in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        return arrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use {@link #arrayToSafeByteArray(Object, byte[][], boolean, boolean)} instead
     */
    @Deprecated
    public static byte[][] arrayToSafeByteArray(Object in, byte[][] out, boolean signed)
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

    public static long[][] doubleArrayToSafeLongArray(double[][] in, int inOffset, long[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeLongArray(in[i + inOffset], 0, outArray[i
                    + outOffset], 0, -1, signed);

        return outArray;
    }

    public static int[][] doubleArrayToSafeIntArray(double[][] in, int inOffset, int[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeIntArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static short[][] doubleArrayToSafeShortArray(double[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeShortArray(in[i + inOffset], 0, outArray[i
                    + outOffset], 0, -1, signed);

        return outArray;
    }

    public static byte[][] doubleArrayToSafeByteArray(double[][] in, int inOffset, byte[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeByteArray(in[i + inOffset], 0, outArray[i
                    + outOffset], 0, -1, signed);

        return outArray;
    }

    public static long[][] floatArrayToSafeLongArray(float[][] in, int inOffset, long[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeLongArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static int[][] floatArrayToSafeIntArray(float[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeIntArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static short[][] floatArrayToSafeShortArray(float[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeShortArray(in[i + inOffset], 0, outArray[i
                    + outOffset], 0, -1, signed);

        return outArray;
    }

    public static byte[][] floatArrayToSafeByteArray(float[][] in, int inOffset, byte[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static long[][] longArrayToSafeLongArray(long[][] in, int inOffset, long[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeLongArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static int[][] longArrayToSafeIntArray(long[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeIntArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static short[][] longArrayToSafeShortArray(long[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] longArrayToSafeByteArray(long[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    /**
     * @deprecated Use
     *             {@link #longArrayToSafeIntArray(long[][], int, int[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static int[][] longArrayToSafeIntArray(long[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeIntArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use
     *             {@link #longArrayToSafeShortArray(long[][], int, short[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static short[][] longArrayToSafeShortArray(long[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean signed)
    {
        return longArrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);

    }

    /**
     * @deprecated Use
     *             {@link #longArrayToSafeByteArray(long[][], int, byte[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static byte[][] longArrayToSafeByteArray(long[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        return longArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static int[][] intArrayToSafeIntArray(int[][] in, int inOffset, int[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeIntArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static short[][] intArrayToSafeShortArray(int[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] intArrayToSafeByteArray(int[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeByteArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    /**
     * @deprecated Use
     *             {@link #intArrayToSafeShortArray(int[][], int, short[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static short[][] intArrayToSafeShortArray(int[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean signed)
    {
        return intArrayToSafeShortArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    /**
     * @deprecated Use
     *             {@link #intArrayToSafeByteArray(int[][], int, byte[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static byte[][] intArrayToSafeByteArray(int[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        return intArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static short[][] shortArrayToSafeShortArray(short[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.shortArrayToSafeShortArray(in[i + inOffset], 0, outArray[i
                    + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] shortArrayToSafeByteArray(short[][] in, int inOffset, byte[][] out, int outOffset,
            int length, boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.shortArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    /**
     * @deprecated Use
     *             {@link #shortArrayToSafeByteArray(short[][], int, byte[][], int, int, boolean, boolean)}
     *             instead
     */
    @Deprecated
    public static byte[][] shortArrayToSafeByteArray(short[][] in, int inOffset, byte[][] out, int outOffset,
            int length, boolean signed)
    {
        return shortArrayToSafeByteArray(in, inOffset, out, outOffset, length, signed, signed);
    }

    public static byte[][] byteArrayToSafeByteArray(byte[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean srcSigned, boolean dstSigned)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.byteArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    //
    //
    //
    //
    //
    //
    //

    public static int[][] doubleArrayToSafeIntArray(double[][] array, boolean signed)
    {
        return doubleArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] doubleArrayToSafeShortArray(double[][] array, boolean signed)
    {
        return doubleArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] doubleArrayToSafeByteArray(double[][] array, boolean signed)
    {
        return doubleArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] floatArrayToSafeIntArray(float[][] array, boolean signed)
    {
        return floatArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] floatArrayToSafeShortArray(float[][] array, boolean signed)
    {
        return floatArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] floatArrayToSafeByteArray(float[][] array, boolean signed)
    {
        return floatArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] intArrayToSafeShortArray(int[][] array, boolean signed)
    {
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[][] intArrayToSafeByteArray(int[][] array, boolean signed)
    {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[][] shortArrayToSafeByteArray(short[][] array, boolean signed)
    {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

}
