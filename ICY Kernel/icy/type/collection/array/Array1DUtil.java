/**
 * 
 */
package icy.type.collection.array;

import icy.math.MathUtil;
import icy.type.TypeUtil;

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
    public static Object createArray(int dataType, int len)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return new byte[len];

            case TypeUtil.TYPE_SHORT:
                return new short[len];

            case TypeUtil.TYPE_INT:
                return new int[len];

            case TypeUtil.TYPE_FLOAT:
                return new float[len];

            case TypeUtil.TYPE_DOUBLE:
                return new double[len];
        }

        return null;
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
    public static float[] allocIfNull(float[] out, int len)
    {
        if (out == null)
            return new float[len];

        return out;
    }

    /**
     * Allocate the specified array if it's defined to null with the specified len
     */
    public static double[] allocIfNull(double[] out, int len)
    {
        if (out == null)
            return new double[len];

        return out;
    }

    //
    //
    //
    //

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static double getValue(Object array, int offset, boolean signed)
    {
        return getValue(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static double getValue(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return getValue((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return getValue((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return getValue((int[]) array, offset, signed);

            case TypeUtil.TYPE_FLOAT:
                return getValue((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return getValue((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static float getValueAsFloat(Object array, int offset, boolean signed)
    {
        return getValueAsFloat(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static float getValueAsFloat(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return getValueAsFloat((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return getValueAsFloat((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return getValueAsFloat((int[]) array, offset, signed);

            case TypeUtil.TYPE_FLOAT:
                return getValueAsFloat((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return getValueAsFloat((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static int getValueAsInt(Object array, int offset, boolean signed)
    {
        return getValueAsInt(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static int getValueAsInt(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return getValueAsInt((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return getValueAsInt((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return getValueAsInt((int[]) array, offset);

            case TypeUtil.TYPE_FLOAT:
                return getValueAsInt((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return getValueAsInt((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(Object array, int offset, double value)
    {
        setValue(array, offset, TypeUtil.getDataType(array), value);
    }

    /**
     * Set value at specified offset as double value.
     */
    public static void setValue(Object array, int offset, int dataType, double value)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                setValue((byte[]) array, offset, value);
                break;

            case TypeUtil.TYPE_SHORT:
                setValue((short[]) array, offset, value);
                break;

            case TypeUtil.TYPE_INT:
                setValue((int[]) array, offset, value);
                break;

            case TypeUtil.TYPE_FLOAT:
                setValue((float[]) array, offset, value);
                break;

            case TypeUtil.TYPE_DOUBLE:
                setValue((double[]) array, offset, value);
                break;
        }
    }

    /**
     * Get value as double from specified byte array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(byte[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFF;
    }

    /**
     * Get value as double from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(short[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFFFF;
    }

    /**
     * Get value as double from specified int array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static double getValue(int[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFFFFFFFFL;
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
        if (signed)
            return array[offset];

        return array[offset] & 0xFF;
    }

    /**
     * Get value as float from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(short[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFFFF;
    }

    /**
     * Get value as float from specified int array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static float getValueAsFloat(int[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFFFFFFFFL;
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
        if (signed)
            return array[offset];

        return array[offset] & 0xFF;
    }

    /**
     * Get value as int from specified short array and offset.<br>
     * If signed is true then we consider data as signed
     */
    public static int getValueAsInt(short[] array, int offset, boolean signed)
    {
        if (signed)
            return array[offset];

        return array[offset] & 0xFFFF;
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
     * Same as Arrays.fill()
     */
    public static void fill(byte[] array, int from, int to, byte value)
    {
        Arrays.fill(array, from, to, value);
    }

    /**
     * Same as Arrays.fill()
     */
    public static void fill(short[] array, int from, int to, short value)
    {
        Arrays.fill(array, from, to, value);
    }

    /**
     * Same as Arrays.fill()
     */
    public static void fill(int[] array, int from, int to, int value)
    {
        Arrays.fill(array, from, to, value);
    }

    /**
     * Same as Arrays.fill()
     */
    public static void fill(float[] array, int from, int to, float value)
    {
        Arrays.fill(array, from, to, value);
    }

    /**
     * Same as Arrays.fill()
     */
    public static void fill(double[] array, int from, int to, double value)
    {
        Arrays.fill(array, from, to, value);
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
     * Return the 'in' array as a single dimension byte array.
     */
    public static byte[] toByteArray1D(byte[] in)
    {
        return toByteArray1D(in, null, 0);
    }

    /**
     * Return the 'in' array as a single dimension short array.
     */
    public static short[] toShortArray1D(short[] in)
    {
        return toShortArray1D(in, null, 0);
    }

    /**
     * Return the 'in' array as a single dimension int array.
     */
    public static int[] toIntArray1D(int[] in)
    {
        return toIntArray1D(in, null, 0);
    }

    /**
     * Return the 'in' array as a single dimension float array.
     */
    public static float[] toFloatArray1D(float[] in)
    {
        return toFloatArray1D(in, null, 0);
    }

    /**
     * Return the 'in' array as a single dimension double array.
     */
    public static double[] toDoubleArray1D(double[] in)
    {
        return toDoubleArray1D(in, null, 0);
    }

    /**
     * Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
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
     * The resulting array is returned in 'out' and from the specified if any.<br>
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
     * The resulting array is returned in 'out' and from the specified if any.<br>
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
     * The resulting array is returned in 'out' and from the specified if any.<br>
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
     * The resulting array is returned in 'out' and from the specified if any.<br>
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return Array1DUtil.shortArrayToArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return Array1DUtil.intArrayToArray((int[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return Array1DUtil.floatArrayToArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
                return Array1DUtil.doubleArrayToArray((double[]) in, inOffset, out, outOffset, length);
        }

        // not yet implemented
        return out;
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return doubleArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return doubleArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return doubleArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return floatArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return floatArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return floatArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return intArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return intArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return shortArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToDoubleArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToDoubleArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToDoubleArray((int[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToDoubleArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToFloatArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToFloatArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToFloatArray((int[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray((int[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToIntArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray((short[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToShortArray((int[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToShortArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToByteArray((int[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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

    public static int[] doubleArrayToIntArray(double[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (int) in[i + inOffset];

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

    public static int[] floatArrayToIntArray(float[] in, int inOffset, int[] out, int outOffset, int length)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (int) in[i + inOffset];

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
                result[i + outOffset] = in[i + inOffset] & 0xFFFFFFFFL;
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
                result[i + outOffset] = in[i + inOffset] & 0xFFFFFFFFL;
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
                result[i + outOffset] = in[i + inOffset] & 0xFFFF;
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
                result[i + outOffset] = in[i + inOffset] & 0xFFFF;
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
                result[i + outOffset] = in[i + inOffset] & 0xFFFF;
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
                result[i + outOffset] = in[i + inOffset] & 0xFF;
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
                result[i + outOffset] = in[i + inOffset] & 0xFF;
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
                result[i + outOffset] = in[i + inOffset] & 0xFF;
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
                result[i + outOffset] = (short) (in[i + inOffset] & 0xFF);
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

    public static double[] intArrayToDoubleArray(int[] array, boolean signed)
    {
        return intArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[] intArrayToFloatArray(int[] array, boolean signed)
    {
        return intArrayToFloatArray(array, 0, null, 0, array.length, signed);
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return doubleArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return doubleArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return doubleArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
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
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return floatArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return floatArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return floatArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);

            case TypeUtil.TYPE_DOUBLE:
                return floatArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);

            default:
                return out;
        }
    }

    public static Object floatArrayToSafeArray(float[] in, Object out, boolean signed)
    {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object intArrayToSafeArray(int[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return intArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return intArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return intArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);

            default:
                return out;
        }
    }

    public static Object intArrayToSafeArray(int[] in, Object out, boolean signed)
    {
        return intArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object shortArrayToSafeArray(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(out))
        {
            case TypeUtil.TYPE_BYTE:
                return shortArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return shortArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);

            default:
                return out;
        }
    }

    public static Object shortArrayToSafeArray(short[] in, Object out, boolean signed)
    {
        return shortArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static int[] arrayToSafeIntArray(Object in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToIntArray((int[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeIntArray((float[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeIntArray((double[]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;

        }
    }

    public static int[] arrayToSafeIntArray(Object in, int[] out, boolean signed)
    {
        return arrayToSafeIntArray(in, 0, out, 0, -1, signed);
    }

    public static short[] arrayToSafeShortArray(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToShortArray((short[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_INT:
                return intArrayToSafeShortArray((int[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeShortArray((float[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeShortArray((double[]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;

        }
    }

    public static short[] arrayToSafeShortArray(Object in, short[] out, boolean signed)
    {
        return arrayToSafeShortArray(in, 0, out, 0, -1, signed);
    }

    public static byte[] arrayToSafeByteArray(Object in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        switch (TypeUtil.getDataType(in))
        {
            case TypeUtil.TYPE_BYTE:
                return byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);

            case TypeUtil.TYPE_SHORT:
                return shortArrayToSafeByteArray((short[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_INT:
                return intArrayToSafeByteArray((int[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_FLOAT:
                return floatArrayToSafeByteArray((float[]) in, inOffset, out, outOffset, length, signed);

            case TypeUtil.TYPE_DOUBLE:
                return doubleArrayToSafeByteArray((double[]) in, inOffset, out, outOffset, length, signed);

            default:
                return out;

        }
    }

    public static byte[] arrayToSafeByteArray(Object in, byte[] out, boolean signed)
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

    public static int[] doubleArrayToSafeIntArray(double[] in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final int result;

                if (value >= Integer.MAX_VALUE)
                    result = Integer.MAX_VALUE;
                else if (value <= Integer.MIN_VALUE)
                    result = Integer.MIN_VALUE;
                else
                    result = (int) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final int result;

                if (value >= 0xFFFFFFFFL)
                    result = 0xFFFFFFFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (int) value;

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

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final short result;

                if (value >= Short.MAX_VALUE)
                    result = Short.MAX_VALUE;
                else if (value <= Short.MIN_VALUE)
                    result = Short.MIN_VALUE;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final short result;

                if (value >= 0xFFFF)
                    result = (short) 0xFFFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static byte[] doubleArrayToSafeByteArray(double[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final byte result;

                if (value >= Byte.MAX_VALUE)
                    result = Byte.MAX_VALUE;
                else if (value <= Short.MIN_VALUE)
                    result = Byte.MIN_VALUE;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final double value = in[i + inOffset];
                final byte result;

                if (value >= 0xFF)
                    result = (byte) 0xFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (byte) value;

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
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final int result;

                if (value >= Integer.MAX_VALUE)
                    result = Integer.MAX_VALUE;
                else if (value <= Integer.MIN_VALUE)
                    result = Integer.MIN_VALUE;
                else
                    result = (int) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final int result;

                if (value >= 0xFFFFFFFFL)
                    result = 0xFFFFFFFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (int) value;

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

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final short result;

                if (value >= Short.MAX_VALUE)
                    result = Short.MAX_VALUE;
                else if (value <= Short.MIN_VALUE)
                    result = Short.MIN_VALUE;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final short result;

                if (value >= 0xFFFF)
                    result = (short) 0xFFFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static byte[] floatArrayToSafeByteArray(float[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final byte result;

                if (value >= Byte.MAX_VALUE)
                    result = Byte.MAX_VALUE;
                else if (value <= Byte.MIN_VALUE)
                    result = Byte.MIN_VALUE;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final float value = in[i + inOffset];
                final byte result;

                if (value >= 0xFF)
                    result = (byte) 0xFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static short[] intArrayToSafeShortArray(int[] in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final int value = in[i + inOffset];
                final short result;

                if (value >= Short.MAX_VALUE)
                    result = Short.MAX_VALUE;
                else if (value <= Short.MIN_VALUE)
                    result = Short.MIN_VALUE;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final int value = in[i + inOffset];
                final short result;

                if (value >= 0xFFFF)
                    result = (short) 0xFFFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (short) value;

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static byte[] intArrayToSafeByteArray(int[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final int value = in[i + inOffset];
                final byte result;

                if (value >= Byte.MAX_VALUE)
                    result = Byte.MAX_VALUE;
                else if (value <= Byte.MIN_VALUE)
                    result = Byte.MIN_VALUE;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final int value = in[i + inOffset];
                final byte result;

                if (value >= 0xFF)
                    result = (byte) 0xFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static byte[] shortArrayToSafeByteArray(short[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        if (signed)
        {
            for (int i = 0; i < len; i++)
            {
                final short value = in[i + inOffset];
                final byte result;

                if (value >= Byte.MAX_VALUE)
                    result = Byte.MAX_VALUE;
                else if (value <= Byte.MIN_VALUE)
                    result = Byte.MIN_VALUE;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
        }
        else
        {
            for (int i = 0; i < len; i++)
            {
                final short value = in[i + inOffset];
                final byte result;

                if (value >= 0xFF)
                    result = (byte) 0xFF;
                else if (value <= 0)
                    result = 0;
                else
                    result = (byte) value;

                outArray[i + outOffset] = result;
            }
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

    public static short[] intArrayToSafeShortArray(int[] array, boolean signed)
    {
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[] intArrayToSafeByteArray(int[] array, boolean signed)
    {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[] shortArrayToSafeByteArray(short[] array, boolean signed)
    {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
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
     * ex : [0,1,2,3,4] --> "0:1:2:3:4"
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
        final int dataType = TypeUtil.getDataType(array);
        final StringBuilder result = new StringBuilder();
        final int base = hexa ? 16 : 10;

        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
            {
                final byte[] data = (byte[]) array;

                if (signed)
                {
                    if (len > 0)
                        result.append(Integer.toString(data[0], base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Integer.toString(data[i], base));
                    }
                }
                else
                {
                    if (len > 0)
                        result.append(Integer.toString(data[0] & 0xFF, base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Integer.toString(data[i] & 0xFF, base));
                    }
                }
                break;
            }

            case TypeUtil.TYPE_SHORT:
            {
                final short[] data = (short[]) array;

                if (signed)
                {
                    if (len > 0)
                        result.append(Integer.toString(data[0], base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Integer.toString(data[i], base));
                    }
                }
                else
                {
                    if (len > 0)
                        result.append(Integer.toString(data[0] & 0xFFFF, base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Integer.toString(data[i] & 0xFFFF, base));
                    }
                }
                break;
            }

            case TypeUtil.TYPE_INT:
            {
                final int[] data = (int[]) array;

                if (signed)
                {
                    if (len > 0)
                        result.append(Integer.toString(data[0], base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Integer.toString(data[i], base));
                    }
                }
                else
                {
                    if (len > 0)
                        result.append(Long.toString(data[0] & 0xFFFFFFFFL, base));
                    for (int i = 1; i < len; i++)
                    {
                        result.append(separator);
                        result.append(Long.toString(data[i] & 0xFFFFFFFFL, base));
                    }
                }
                break;
            }

            case TypeUtil.TYPE_FLOAT:
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

            case TypeUtil.TYPE_DOUBLE:
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
    public static Object stringToArray(String value, int dataType)
    {
        return stringToArray(value, dataType, false, ":");
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
    public static Object stringToArray(String value, int dataType, boolean hexa, String separator)
    {
        if (value == null)
            return createArray(dataType, 0);

        final String[] values = value.split(separator);
        final int len = values.length;
        final int base = hexa ? 16 : 10;

        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
            {
                final byte[] result = new byte[len];

                for (int i = 0; i < len; i++)
                    result[i] = (byte) Integer.parseInt(values[i], base);

                return result;
            }

            case TypeUtil.TYPE_SHORT:
            {
                final short[] result = new short[len];

                for (int i = 0; i < len; i++)
                    result[i] = (short) Integer.parseInt(values[i], base);

                return result;
            }

            case TypeUtil.TYPE_INT:
            {
                final int[] result = new int[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }

            case TypeUtil.TYPE_FLOAT:
            {
                final float[] result = new float[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }

            case TypeUtil.TYPE_DOUBLE:
            {
                final double[] result = new double[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }
        }

        return null;
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

}
