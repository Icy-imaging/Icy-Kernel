/**
 * 
 */
package icy.type.collection.array;

import icy.type.TypeUtil;

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
    public static Object[] createArray(int dataType, int len)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return new byte[len][];

            case TypeUtil.TYPE_SHORT:
                return new short[len][];

            case TypeUtil.TYPE_INT:
                return new int[len][];

            case TypeUtil.TYPE_FLOAT:
                return new float[len][];

            case TypeUtil.TYPE_DOUBLE:
                return new double[len][];
        }

        return null;
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return Array2DUtil.byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return Array2DUtil.shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return Array2DUtil.intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return Array2DUtil.floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
                return Array2DUtil.doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
        }

        // not yet implemented
        return out;
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return doubleArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return doubleArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return doubleArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return floatArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return floatArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return floatArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return intArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return intArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return shortArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToDoubleArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToDoubleArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToDoubleArray((int[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToDoubleArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToFloatArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToFloatArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToFloatArray((int[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray((int[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToIntArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray((short[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToShortArray((int[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToShortArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray((byte[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToByteArray((short[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToByteArray((int[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToByteArray((float[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return doubleArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return doubleArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return doubleArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return floatArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return floatArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return floatArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);

            default:
                return out;
        }
    }

    public static Object floatArrayToSafeArray(float[][] in, Object out, boolean signed)
    {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object intArrayToSafeArray(int[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return intArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return intArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);

            default:
                return out;
        }
    }

    public static Object intArrayToSafeArray(int[][] in, Object out, boolean signed)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object shortArrayToSafeArray(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return shortArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);

            default:
                return out;
        }
    }

    public static Object shortArrayToSafeArray(short[][] in, Object out, boolean signed)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static int[][] arrayToSafeIntArray(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray((int[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeIntArray((float[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeIntArray((double[][]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;

        }
    }

    public static int[][] arrayToSafeIntArray(Object in, int[][] out, boolean signed)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, signed);
    }

    public static short[][] arrayToSafeShortArray(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray((short[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToSafeShortArray((int[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeShortArray((float[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeShortArray((double[][]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;

        }
    }

    public static short[][] arrayToSafeShortArray(Object in, short[][] out, boolean signed)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, signed);
    }

    public static byte[][] arrayToSafeByteArray(Object in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray((byte[][]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToSafeByteArray((short[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToSafeByteArray((int[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeByteArray((float[][]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeByteArray((double[][]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;
        }
    }

    public static byte[][] arrayToSafeByteArray(Object in, byte[][] out, boolean signed)
    {
        return arrayToSafeByteArray(in, 0, out, 0, -1, signed);
    }

    //
    //
    //
    //
    //
    //
    //
    //

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

    public static short[][] intArrayToSafeShortArray(int[][] in, int inOffset, short[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static byte[][] intArrayToSafeByteArray(int[][] in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeByteArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, signed);

        return outArray;
    }

    public static byte[][] shortArrayToSafeByteArray(short[][] in, int inOffset, byte[][] out, int outOffset,
            int length, boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.shortArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

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
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] intArrayToSafeByteArray(int[][] array, boolean signed)
    {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] shortArrayToSafeByteArray(short[][] array, boolean signed)
    {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

}
