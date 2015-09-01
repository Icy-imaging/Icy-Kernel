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
 * Utilities class to convert any data array from/to byte array.
 * 
 * @author Stephane
 */
public class ByteArrayConvert
{
    /**
     * Get maximum length in bytes for a copy from in to out with specified offset.<br>
     * If specified length != -1 then the value is directly returned (assumed to be in bytes).
     */
    static int getCopyLengthInBytes(Object in, int inOffset, Object out, int outOffset, int length)
    {
        if (length == -1)
            return getCopyLengthInBytes(in, inOffset, out, outOffset);

        return length;
    }

    /**
     * Get maximum length in bytes for a copy from in to out with specified offset.
     */
    public static int getCopyLengthInBytes(Object in, int inOffset, Object out, int outOffset)
    {
        // 'in' object can't be null !
        final int len = getCopyLengthInBytes(in, inOffset);

        if (out == null)
            return len;

        return Math.min(len, getCopyLengthInBytes(out, outOffset));
    }

    /**
     * Get length in bytes for a copy from or to array with specified offset.
     */
    public static int getCopyLengthInBytes(Object array, int offset)
    {
        return ArrayUtil.getCopyLength(array, offset) * ArrayUtil.getDataType(array).getSize();
    }

    /**
     * Read a byte from the input byte array at specified position.
     */
    public static byte readByte(byte[] array, int offset)
    {
        return array[offset];
    }

    /**
     * Read a short value from the input byte array at specified position.
     */
    public static short readShort(byte[] array, int offset, boolean littleEndian)
    {
        if (littleEndian)
            return (short) (((array[offset + 0] & 0xFF) << 0) + ((array[offset + 1] & 0xFF) << 8));

        return (short) (((array[offset + 0] & 0xFF) << 8) + ((array[offset + 1] & 0xFF) << 0));
    }

    /**
     * Read a int value from the input byte array at specified position.
     */
    public static int readInt(byte[] array, int offset, boolean littleEndian)
    {
        if (littleEndian)
            return ((array[offset + 0] & 0xFF) << 0) + ((array[offset + 1] & 0xFF) << 8)
                    + ((array[offset + 2] & 0xFF) << 16) + ((array[offset + 3] & 0xFF) << 24);

        return ((array[offset + 0] & 0xFF) << 24) + ((array[offset + 1] & 0xFF) << 16)
                + ((array[offset + 2] & 0xFF) << 8) + ((array[offset + 3] & 0xFF) << 0);
    }

    /**
     * Read a long value from the input byte array at specified position.
     */
    public static long readLong(byte[] array, int offset, boolean littleEndian)
    {
        if (littleEndian)
        {
            final int v1 = ((array[offset + 0] & 0xFF) << 0) + ((array[offset + 1] & 0xFF) << 8)
                    + ((array[offset + 2] & 0xFF) << 16) + ((array[offset + 3] & 0xFF) << 24);
            final int v2 = ((array[offset + 4] & 0xFF) << 0) + ((array[offset + 5] & 0xFF) << 8)
                    + ((array[offset + 6] & 0xFF) << 16) + ((array[offset + 7] & 0xFF) << 24);
            return ((v1 & 0xFFFFFFFFL) << 0) + ((v2 & 0xFFFFFFFFL) << 32);
        }

        final int v1 = ((array[offset + 0] & 0xFF) << 24) + ((array[offset + 1] & 0xFF) << 16)
                + ((array[offset + 2] & 0xFF) << 8) + ((array[offset + 3] & 0xFF) << 0);
        final int v2 = ((array[offset + 4] & 0xFF) << 24) + ((array[offset + 5] & 0xFF) << 16)
                + ((array[offset + 6] & 0xFF) << 8) + ((array[offset + 7] & 0xFF) << 0);
        return ((v1 & 0xFFFFFFFFL) << 32) + ((v2 & 0xFFFFFFFFL) << 0);
    }

    /**
     * Read a long value from the input byte array at specified position.
     */
    public static float readFloat(byte[] array, int offset, boolean littleEndian)
    {
        return Float.intBitsToFloat(readInt(array, offset, littleEndian));
    }

    /**
     * Read a long value from the input byte array at specified position.
     */
    public static double readDouble(byte[] array, int offset, boolean littleEndian)
    {
        return Double.longBitsToDouble(readLong(array, offset, littleEndian));
    }

    /**
     * Write a byte to the output byte array at specified position.
     */
    public static void writeByte(byte[] array, int offset, byte value)
    {
        array[offset] = value;
    }

    /**
     * Write a short to the output byte array at specified position.
     */
    public static void writeShort(byte[] array, int offset, short value, boolean littleEndian)
    {
        if (littleEndian)
        {
            array[offset + 0] = (byte) (value >> 0);
            array[offset + 1] = (byte) (value >> 8);
        }
        else
        {
            array[offset + 0] = (byte) (value >> 8);
            array[offset + 1] = (byte) (value >> 0);
        }
    }

    /**
     * Write a int to the output byte array at specified position.
     */
    public static void writeInt(byte[] array, int offset, int value, boolean littleEndian)
    {
        if (littleEndian)
        {
            array[offset + 0] = (byte) (value >> 0);
            array[offset + 1] = (byte) (value >> 8);
            array[offset + 2] = (byte) (value >> 16);
            array[offset + 3] = (byte) (value >> 24);
        }
        else
        {
            array[offset + 0] = (byte) (value >> 24);
            array[offset + 1] = (byte) (value >> 16);
            array[offset + 2] = (byte) (value >> 8);
            array[offset + 3] = (byte) (value >> 0);
        }
    }

    /**
     * Write a long to the output byte array at specified position.
     */
    public static void writeLong(byte[] array, int offset, long value, boolean littleEndian)
    {
        int v;

        if (littleEndian)
        {
            v = (int) (value >> 0);
            array[offset + 0] = (byte) (v >> 0);
            array[offset + 1] = (byte) (v >> 8);
            array[offset + 2] = (byte) (v >> 16);
            array[offset + 3] = (byte) (v >> 24);
            v = (int) (value >> 32);
            array[offset + 4] = (byte) (v >> 0);
            array[offset + 5] = (byte) (v >> 8);
            array[offset + 6] = (byte) (v >> 16);
            array[offset + 7] = (byte) (v >> 24);
        }
        else
        {
            v = (int) (value >> 32);
            array[offset + 0] = (byte) (v >> 24);
            array[offset + 1] = (byte) (v >> 16);
            array[offset + 2] = (byte) (v >> 8);
            array[offset + 3] = (byte) (v >> 0);
            v = (int) (value >> 0);
            array[offset + 4] = (byte) (v >> 24);
            array[offset + 5] = (byte) (v >> 16);
            array[offset + 6] = (byte) (v >> 8);
            array[offset + 7] = (byte) (v >> 0);
        }
    }

    /**
     * Write a float to the output byte array at specified position.
     */
    public static void writeFloat(byte[] array, int offset, float value, boolean littleEndian)
    {
        writeInt(array, offset, Float.floatToRawIntBits(value), littleEndian);
    }

    /**
     * Write a double to the output byte array at specified position.
     */
    public static void writeDouble(byte[] array, int offset, double value, boolean littleEndian)
    {
        writeLong(array, offset, Double.doubleToRawLongBits(value), littleEndian);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     */
    public static byte[] byteArrayToByteArray(byte[] in, int inOffset, byte[] out, int outOffset, int length)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        // simple copy
        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static byte[] byteArrayToByteArray(byte[] in, byte[] out)
    {
        return byteArrayToByteArray(in, 0, out, 0, -1);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     */
    public static byte[] byteArrayToByteArray(byte[] in)
    {
        return byteArrayToByteArray(in, 0, null, 0, -1);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static short[] byteArrayToShortArray(byte[] in, int inOffset, short[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 2;
        final short[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++)
        {
            result[outOff++] = readShort(in, inOff, little);
            inOff += 2;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static short[] byteArrayToShortArray(byte[] in, short[] out, boolean little)
    {
        return byteArrayToShortArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param little
     *        little endian order
     */
    public static short[] byteArrayToShortArray(byte[] in, boolean little)
    {
        return byteArrayToShortArray(in, 0, null, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static int[] byteArrayToIntArray(byte[] in, int inOffset, int[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 4;
        final int[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++)
        {
            result[outOff++] = readInt(in, inOff, little);
            inOff += 4;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static int[] byteArrayToIntArray(byte[] in, int[] out, boolean little)
    {
        return byteArrayToIntArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param little
     *        little endian order
     */
    public static int[] byteArrayToIntArray(byte[] in, boolean little)
    {
        return byteArrayToIntArray(in, 0, null, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static long[] byteArrayToLongArray(byte[] in, int inOffset, long[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 8;
        final long[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++)
        {
            result[outOff++] = readLong(in, inOff, little);
            inOff += 8;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static long[] byteArrayToLongArray(byte[] in, long[] out, boolean little)
    {
        return byteArrayToLongArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param little
     *        little endian order
     */
    public static long[] byteArrayToLongArray(byte[] in, boolean little)
    {
        return byteArrayToLongArray(in, 0, null, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in 'out' float array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static float[] byteArrayToFloatArray(byte[] in, int inOffset, float[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 4;
        final float[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++)
        {
            result[outOff++] = readFloat(in, inOff, little);
            inOff += 4;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static float[] byteArrayToFloatArray(byte[] in, float[] out, boolean little)
    {
        return byteArrayToFloatArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param little
     *        little endian order
     */
    public static float[] byteArrayToFloatArray(byte[] in, boolean little)
    {
        return byteArrayToFloatArray(in, 0, null, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static double[] byteArrayToDoubleArray(byte[] in, int inOffset, double[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 8;
        final double[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++)
        {
            result[outOff++] = readDouble(in, inOff, little);
            inOff += 8;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static double[] byteArrayToDoubleArray(byte[] in, double[] out, boolean little)
    {
        return byteArrayToDoubleArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param little
     *        little endian order
     */
    public static double[] byteArrayToDoubleArray(byte[] in, boolean little)
    {
        return byteArrayToDoubleArray(in, 0, null, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' short array as byte array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of <b>bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] shortArrayToByteArray(short[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 2; i++)
        {
            writeShort(result, outOff, in[inOff++], little);
            outOff += 2;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' int array as byte array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of <b>bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] intArrayToByteArray(int[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 4; i++)
        {
            writeInt(result, outOff, in[inOff++], little);
            outOff += 4;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' long array as byte array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of <b>bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] longArrayToByteArray(long[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 8; i++)
        {
            writeLong(result, outOff, in[inOff++], little);
            outOff += 8;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' float array as byte array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of <b>bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] floatArrayToByteArray(float[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 4; i++)
        {
            writeFloat(result, outOff, in[inOff++], little);
            outOff += 4;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' double array as byte array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of <b>bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] doubleArrayToByteArray(double[] in, int inOffset, byte[] out, int outOffset, int length,
            boolean little)
    {
        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 8; i++)
        {
            writeDouble(result, outOff, in[inOff++], little);
            outOff += 8;
        }

        return result;
    }

    /**
     * Bit transform and return the 'in' byte array in the specified 'out' data type array
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
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static Object byteArrayTo(byte[] in, int inOffset, Object out, int outOffset, int length, boolean little)
    {
        if (out == null)
            return null;

        switch (ArrayUtil.getDataType(out).getJavaType())
        {
            case BYTE:
                return byteArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, little);
            case INT:
                return byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, little);
            case LONG:
                return byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, little);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, little);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, little);
            default:
                return null;
        }
    }

    /**
     * Bit transform and return the 'in' byte array in the specified 'out' data type array
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param little
     *        little endian order
     */
    public static Object byteArrayTo(byte[] in, Object out, boolean little)
    {
        return byteArrayTo(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param outDataType
     *        wanted output array data type
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static Object byteArrayTo(byte[] in, int inOffset, DataType outDataType, int outOffset, int length,
            boolean little)
    {
        switch (outDataType.getJavaType())
        {
            case BYTE:
                return byteArrayToByteArray(in, inOffset, null, outOffset, length);
            case SHORT:
                return byteArrayToShortArray(in, inOffset, null, outOffset, length, little);
            case INT:
                return byteArrayToIntArray(in, inOffset, null, outOffset, length, little);
            case LONG:
                return byteArrayToLongArray(in, inOffset, null, outOffset, length, little);
            case FLOAT:
                return byteArrayToFloatArray(in, inOffset, null, outOffset, length, little);
            case DOUBLE:
                return byteArrayToDoubleArray(in, inOffset, null, outOffset, length, little);
            default:
                return null;
        }
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param outDataType
     *        wanted output array data type
     * @param length
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static Object byteArrayTo(byte[] in, int inOffset, DataType outDataType, int length, boolean little)
    {
        return byteArrayTo(in, inOffset, outDataType, 0, length, little);
    }

    /**
     * Bit transform and return the 'in' byte array in the specified data type array
     * 
     * @param in
     *        input array
     * @param outDataType
     *        wanted output array data type
     * @param little
     *        little endian order
     */
    public static Object byteArrayTo(byte[] in, DataType outDataType, boolean little)
    {
        return byteArrayTo(in, 0, outDataType, 0, -1, little);
    }

    /**
     * @deprecated use {@link #byteArrayTo(byte[], int , DataType , int , boolean )} instead
     */
    @Deprecated
    public static Object byteArrayTo(byte[] in, int inOffset, int outDataType, int length, boolean little)
    {
        return byteArrayTo(in, inOffset, DataType.getDataType(outDataType), 0, length, little);
    }

    /**
     * @deprecated use {@link #byteArrayTo(byte[], DataType , boolean )} instead
     */
    @Deprecated
    public static Object byteArrayTo(byte[] in, int outDataType, boolean little)
    {
        return byteArrayTo(in, 0, DataType.getDataType(outDataType), 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' array as byte array
     * 
     * @param in
     *        input array (define input type)
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of bytes to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] toByteArray(Object in, int inOffset, byte[] out, int outOffset, int length, boolean little)
    {
        switch (ArrayUtil.getDataType(in))
        {
            case BYTE:
                return byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);
            case SHORT:
                return shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length, little);
            case INT:
                return intArrayToByteArray((int[]) in, inOffset, out, outOffset, length, little);
            case LONG:
                return longArrayToByteArray((long[]) in, inOffset, out, outOffset, length, little);
            case FLOAT:
                return floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length, little);
            case DOUBLE:
                return doubleArrayToByteArray((double[]) in, inOffset, out, outOffset, length, little);
            default:
                return out;
        }
    }

    /**
     * Bit transform and return the 'in' array as byte array
     * 
     * @param in
     *        input array (define input type)
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output byte array which is used to receive result
     * @param outOffset
     *        position where we start to write data to
     * @param little
     *        little endian order
     */
    public static byte[] toByteArray(Object in, int inOffset, byte[] out, int outOffset, boolean little)
    {
        return toByteArray(in, inOffset, out, outOffset, -1, little);
    }

    /**
     * Bit transform and return the 'in' array as byte array
     * 
     * @param in
     *        input array (define input type)
     * @param inOffset
     *        position where we start read data from
     * @param length
     *        number of <br>
     *        bytes</b> to compute (-1 means we will use the maximum possible)
     * @param little
     *        little endian order
     */
    public static byte[] toByteArray(Object in, int inOffset, int length, boolean little)
    {
        return toByteArray(in, inOffset, null, 0, length, little);
    }

    /**
     * Bit transform and return the 'in' array as byte array
     * 
     * @param in
     *        input array (define input type)
     * @param out
     *        output byte array which is used to receive result
     * @param little
     *        little endian order
     */
    public static byte[] toByteArray(Object in, byte[] out, boolean little)
    {
        return toByteArray(in, 0, out, 0, -1, little);
    }

    /**
     * Bit transform and return the 'in' array as byte array
     * 
     * @param in
     *        input array (define input type)
     * @param little
     *        little endian order
     */
    public static byte[] toByteArray(Object in, boolean little)
    {
        return toByteArray(in, 0, null, 0, -1, little);
    }
}
