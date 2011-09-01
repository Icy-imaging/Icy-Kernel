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
package icy.type.collection.array;

import icy.type.ArrayTypeInfo;
import icy.type.TypeUtil;

/**
 * Array utilities :<br>
 * Basic array type conversion and misc tools
 * 
 * @author stephane
 */
public class ArrayUtil
{
    /**
     * Return the number of element of the specified array
     */
    public static int getLength(Object array)
    {
        if (array != null)
        {
            final ArrayTypeInfo typeInfo = TypeUtil.getTypeInfo(array);

            switch (typeInfo.type)
            {
                case TypeUtil.TYPE_BYTE:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return ((byte[]) array).length;

                        case 2:
                            return ((byte[][]) array).length;

                        case 3:
                            return ((byte[][][]) array).length;
                    }
                    break;

                case TypeUtil.TYPE_SHORT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return ((short[]) array).length;

                        case 2:
                            return ((short[][]) array).length;

                        case 3:
                            return ((short[][][]) array).length;
                    }
                    break;

                case TypeUtil.TYPE_INT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return ((int[]) array).length;

                        case 2:
                            return ((int[][]) array).length;

                        case 3:
                            return ((int[][][]) array).length;
                    }
                    break;

                case TypeUtil.TYPE_FLOAT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return ((float[]) array).length;

                        case 2:
                            return ((float[][]) array).length;

                        case 3:
                            return ((float[][][]) array).length;
                    }
                    break;

                case TypeUtil.TYPE_DOUBLE:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return ((double[]) array).length;

                        case 2:
                            return ((double[][]) array).length;

                        case 3:
                            return ((double[][][]) array).length;
                    }
                    break;
            }
        }

        // not yet supported or null array
        return 0;
    }

    /**
     * @deprecated
     *             use {@link #getLength(Object)} instead
     */
    @Deprecated
    public static int getLenght(Object array)
    {
        return getLength(array);
    }

    /**
     * Return the total number of element of the specified array
     */
    public static int getTotalLength(Object array)
    {
        if (array != null)
        {
            final ArrayTypeInfo typeInfo = TypeUtil.getTypeInfo(array);

            switch (typeInfo.type)
            {
                case TypeUtil.TYPE_BYTE:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return Array1DUtil.getTotalLength((byte[]) array);

                        case 2:
                            return Array2DUtil.getTotalLength((byte[][]) array);

                        case 3:
                            return Array3DUtil.getTotalLength((byte[][][]) array);
                    }
                    break;

                case TypeUtil.TYPE_SHORT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return Array1DUtil.getTotalLength((short[]) array);

                        case 2:
                            return Array2DUtil.getTotalLength((short[][]) array);

                        case 3:
                            return Array3DUtil.getTotalLength((short[][][]) array);
                    }
                    break;

                case TypeUtil.TYPE_INT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return Array1DUtil.getTotalLength((int[]) array);

                        case 2:
                            return Array2DUtil.getTotalLength((int[][]) array);

                        case 3:
                            return Array3DUtil.getTotalLength((int[][][]) array);
                    }
                    break;

                case TypeUtil.TYPE_FLOAT:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return Array1DUtil.getTotalLength((float[]) array);

                        case 2:
                            return Array2DUtil.getTotalLength((float[][]) array);

                        case 3:
                            return Array3DUtil.getTotalLength((float[][][]) array);
                    }
                    break;

                case TypeUtil.TYPE_DOUBLE:
                    switch (typeInfo.dim)
                    {
                        case 1:
                            return Array1DUtil.getTotalLength((double[]) array);

                        case 2:
                            return Array2DUtil.getTotalLength((double[][]) array);

                        case 3:
                            return Array3DUtil.getTotalLength((double[][][]) array);
                    }
                    break;
            }
        }

        // not yet supported
        return 0;
    }

    /**
     * @deprecated
     *             use {@link #getTotalLength(Object)} instead
     */
    @Deprecated
    public static int getTotalLenght(Object array)
    {
        return getTotalLength(array);
    }

    /**
     * Create a new 1D array with specified data type and length
     * 
     * @deprecated
     *             use Array1DUtil.createArray instead
     */
    @Deprecated
    public static Object createArray1D(int dataType, int len)
    {
        return Array1DUtil.createArray(dataType, len);
    }

    /**
     * Create a new 2D array with specified data type and length
     * 
     * @deprecated
     *             use Array2DUtil.createArray instead
     */
    @Deprecated
    public static Object createArray2D(int dataType, int len)
    {
        return Array2DUtil.createArray(dataType, len);
    }

    /**
     * Create a new 3D array with specified data type and length
     * 
     * @deprecated
     *             use Array2DUtil.createArray instead
     */
    @Deprecated
    public static Object createArray3D(int dataType, int len)
    {
        return Array3DUtil.createArray(dataType, len);
    }

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValue(Object, int, boolean) instead
     */
    @Deprecated
    public static double getValue(Object array, int offset, boolean signed)
    {
        return getValue(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValue(Object, int, int, boolean) instead
     */
    @Deprecated
    public static double getValue(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return Array1DUtil.getValue((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return Array1DUtil.getValue((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return Array1DUtil.getValue((int[]) array, offset, signed);

            case TypeUtil.TYPE_FLOAT:
                return Array1DUtil.getValue((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return Array1DUtil.getValue((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValueAsFloat(Object, int, boolean) instead
     */
    @Deprecated
    public static float getValueAsFloat(Object array, int offset, boolean signed)
    {
        return getValueAsFloat(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValueAsFloat(Object, int,int, boolean) instead
     */
    @Deprecated
    public static float getValueAsFloat(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return Array1DUtil.getValueAsFloat((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return Array1DUtil.getValueAsFloat((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return Array1DUtil.getValueAsFloat((int[]) array, offset, signed);

            case TypeUtil.TYPE_FLOAT:
                return Array1DUtil.getValueAsFloat((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return Array1DUtil.getValueAsFloat((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValueAsInt(Object, int, boolean) instead
     */
    @Deprecated
    public static int getValueAsInt(Object array, int offset, boolean signed)
    {
        return getValueAsInt(array, offset, TypeUtil.getDataType(array), signed);
    }

    /**
     * Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     * 
     * @deprecated use Array1DUtil.getValueAsInt(Object, int, int, boolean) instead
     */
    @Deprecated
    public static int getValueAsInt(Object array, int offset, int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                return Array1DUtil.getValueAsInt((byte[]) array, offset, signed);

            case TypeUtil.TYPE_SHORT:
                return Array1DUtil.getValueAsInt((short[]) array, offset, signed);

            case TypeUtil.TYPE_INT:
                return Array1DUtil.getValueAsInt((int[]) array, offset);

            case TypeUtil.TYPE_FLOAT:
                return Array1DUtil.getValueAsInt((float[]) array, offset);

            case TypeUtil.TYPE_DOUBLE:
                return Array1DUtil.getValueAsInt((double[]) array, offset);
        }

        return 0;
    }

    /**
     * Set value at specified offset as double value.
     * 
     * @deprecated use Array1DUtil.setValue(Object, int, double) instead
     */
    @Deprecated
    public static void setValue(Object array, int offset, double value)
    {
        setValue(array, offset, TypeUtil.getDataType(array), value);
    }

    /**
     * Set value at specified offset as double value.
     * 
     * @deprecated use Array1DUtil.setValue(Object, int, int,double) instead
     */
    @Deprecated
    public static void setValue(Object array, int offset, int dataType, double value)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                Array1DUtil.setValue((byte[]) array, offset, value);
                break;

            case TypeUtil.TYPE_SHORT:
                Array1DUtil.setValue((short[]) array, offset, value);
                break;

            case TypeUtil.TYPE_INT:
                Array1DUtil.setValue((int[]) array, offset, value);
                break;

            case TypeUtil.TYPE_FLOAT:
                Array1DUtil.setValue((float[]) array, offset, value);
                break;

            case TypeUtil.TYPE_DOUBLE:
                Array1DUtil.setValue((double[]) array, offset, value);
                break;
        }
    }

    /**
     * Return true if the specified array has the same type<br>
     * and the same number of dimension
     */
    public static boolean arrayTypeCompare(Object array1, Object array2)
    {
        return TypeUtil.getTypeInfo(array1).equals(TypeUtil.getTypeInfo(array2));
    }

    /**
     * Return true if the specified array are equals (same type, dimension and data).<br>
     * Only 1D & 2D arrays supported.
     */
    public static boolean arrayCompare(Object array1, Object array2)
    {
        final ArrayTypeInfo info1 = TypeUtil.getTypeInfo(array1);
        final ArrayTypeInfo info2 = TypeUtil.getTypeInfo(array2);

        if (!info1.equals(info2))
            return false;

        switch (info1.type)
        {
            case TypeUtil.TYPE_BYTE:
                switch (info1.dim)
                {
                    case 1:
                        return Array1DUtil.arrayByteCompare((byte[]) array1, (byte[]) array2);

                    case 2:
                        return Array2DUtil.arrayByteCompare((byte[][]) array1, (byte[][]) array2);
                }
                break;

            case TypeUtil.TYPE_SHORT:
                switch (info1.dim)
                {
                    case 1:
                        return Array1DUtil.arrayShortCompare((short[]) array1, (short[]) array2);

                    case 2:
                        return Array2DUtil.arrayShortCompare((short[][]) array1, (short[][]) array2);
                }
                break;

            case TypeUtil.TYPE_INT:
                switch (info1.dim)
                {
                    case 1:
                        return Array1DUtil.arrayIntCompare((int[]) array1, (int[]) array2);

                    case 2:
                        return Array2DUtil.arrayIntCompare((int[][]) array1, (int[][]) array2);
                }
                break;

            case TypeUtil.TYPE_FLOAT:
                switch (info1.dim)
                {
                    case 1:
                        return Array1DUtil.arrayFloatCompare((float[]) array1, (float[]) array2);

                    case 2:
                        return Array2DUtil.arrayFloatCompare((float[][]) array1, (float[][]) array2);
                }
                break;

            case TypeUtil.TYPE_DOUBLE:
                switch (info1.dim)
                {
                    case 1:
                        return Array1DUtil.arrayDoubleCompare((double[]) array1, (double[]) array2);

                    case 2:
                        return Array2DUtil.arrayDoubleCompare((double[][]) array1, (double[][]) array2);
                }
                break;
        }

        return false;
    }

    /**
     * Same as Arrays.fill() but applied to Object array (1D only)
     */
    public static void fill(Object array, int from, int to, double value)
    {
        switch (TypeUtil.getDataType(array))
        {
            case TypeUtil.TYPE_BYTE:
                Array1DUtil.fill((byte[]) array, from, to, (byte) value);
                break;

            case TypeUtil.TYPE_SHORT:
                Array1DUtil.fill((short[]) array, from, to, (short) value);
                break;

            case TypeUtil.TYPE_INT:
                Array1DUtil.fill((int[]) array, from, to, (int) value);
                break;

            case TypeUtil.TYPE_FLOAT:
                Array1DUtil.fill((float[]) array, from, to, (float) value);
                break;

            case TypeUtil.TYPE_DOUBLE:
                Array1DUtil.fill((double[]) array, from, to, value);
                break;
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner
     */
    public static void innerCopy(Object array, int from, int to, int cnt)
    {
        switch (TypeUtil.getDataType(array))
        {
            case TypeUtil.TYPE_BYTE:
                Array1DUtil.innerCopy((byte[]) array, from, to, cnt);
                break;

            case TypeUtil.TYPE_SHORT:
                Array1DUtil.innerCopy((short[]) array, from, to, cnt);
                break;

            case TypeUtil.TYPE_INT:
                Array1DUtil.innerCopy((int[]) array, from, to, cnt);
                break;

            case TypeUtil.TYPE_FLOAT:
                Array1DUtil.innerCopy((float[]) array, from, to, cnt);
                break;

            case TypeUtil.TYPE_DOUBLE:
                Array1DUtil.innerCopy((double[]) array, from, to, cnt);
                break;
        }
    }

    /**
     * Transform the multi dimension 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static Object toArray1D(Object in, Object out, int offset)
    {
        final ArrayTypeInfo info = TypeUtil.getTypeInfo(in);

        switch (info.type)
        {
            case TypeUtil.TYPE_BYTE:
                switch (info.dim)
                {
                    case 1:
                        return Array1DUtil.toByteArray1D((byte[]) in, (byte[]) out, offset);

                    case 2:
                        return Array2DUtil.toByteArray1D((byte[][]) in, (byte[]) out, offset);

                    case 3:
                        return Array3DUtil.toByteArray1D((byte[][][]) in, (byte[]) out, offset);
                }
                break;

            case TypeUtil.TYPE_SHORT:
                switch (info.dim)
                {
                    case 1:
                        return Array1DUtil.toShortArray1D((short[]) in, (short[]) out, offset);

                    case 2:
                        return Array2DUtil.toShortArray1D((short[][]) in, (short[]) out, offset);

                    case 3:
                        return Array3DUtil.toShortArray1D((short[][][]) in, (short[]) out, offset);
                }
                break;

            case TypeUtil.TYPE_INT:
                switch (info.dim)
                {
                    case 1:
                        return Array1DUtil.toIntArray1D((int[]) in, (int[]) out, offset);

                    case 2:
                        return Array2DUtil.toIntArray1D((int[][]) in, (int[]) out, offset);

                    case 3:
                        return Array3DUtil.toIntArray1D((int[][][]) in, (int[]) out, offset);
                }
                break;

            case TypeUtil.TYPE_FLOAT:
                switch (info.dim)
                {
                    case 1:
                        return Array1DUtil.toFloatArray1D((float[]) in, (float[]) out, offset);

                    case 2:
                        return Array2DUtil.toFloatArray1D((float[][]) in, (float[]) out, offset);

                    case 3:
                        return Array3DUtil.toFloatArray1D((float[][][]) in, (float[]) out, offset);
                }
                break;

            case TypeUtil.TYPE_DOUBLE:
                switch (info.dim)
                {
                    case 1:
                        return Array1DUtil.toDoubleArray1D((double[]) in, (double[]) out, offset);

                    case 2:
                        return Array2DUtil.toDoubleArray1D((double[][]) in, (double[]) out, offset);

                    case 3:
                        return Array3DUtil.toDoubleArray1D((double[][][]) in, (double[]) out, offset);
                }
                break;
        }

        return out;
    }

    /**
     * Get maximum length for a copy from in to out with specified offset.<br>
     * If specified length != -1 then the value is directly returned.
     */
    static int getCopyLength(Object in, int inOffset, Object out, int outOffset, int length)
    {
        if (length == -1)
            return getCopyLength(in, inOffset, out, outOffset);

        return length;
    }

    /**
     * Get maximum length for a copy from in to out with specified offset.
     */
    public static int getCopyLength(Object in, int inOffset, Object out, int outOffset)
    {
        // 'in' object can't be null !
        final int len = getCopyLength(in, inOffset);

        if (out == null)
            return len;

        return Math.min(len, getCopyLength(out, outOffset));
    }

    /**
     * Get length for a copy from or to the specified array with specified offset
     */
    public static int getCopyLength(Object array, int offset)
    {
        return getLength(array) - offset;
    }

    /**
     * Convert and return the 'in' array in 'out' array type.<br>
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
        final ArrayTypeInfo typeInfo = TypeUtil.getTypeInfo(in);

        switch (typeInfo.type)
        {
            case TypeUtil.TYPE_BYTE:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case TypeUtil.TYPE_SHORT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.shortArrayToArray((short[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case TypeUtil.TYPE_INT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.intArrayToArray((int[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case TypeUtil.TYPE_FLOAT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.floatArrayToArray((float[]) in, inOffset, out, outOffset, length);

                    case 2:
                        return Array2DUtil.floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);
                }
                break;

            case TypeUtil.TYPE_DOUBLE:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.doubleArrayToArray((double[]) in, inOffset, out, outOffset, length);

                    case 2:
                        return Array2DUtil.doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
                }
                break;
        }

        // not yet implemented
        return out;
    }

    /**
     * Convert and return the 'in' array in 'out' array type.<br>
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
     * @deprecated
     *             use Array1DUtil.doubleArrayToArray instead
     */
    @Deprecated
    public static Object doubleArrayToArray1D(double[] in, int inOffset, Object out, int outOffset, int length)
    {
        return Array1DUtil.doubleArrayToArray(in, inOffset, out, outOffset, length);
    }

    /**
     * Convert and return the 'in' double array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array1DUtil.doubleArrayToArray instead
     */
    @Deprecated
    public static Object doubleArrayToArray1D(double[] in, Object out)
    {
        return Array1DUtil.doubleArrayToArray(in, out);
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
     * @deprecated
     *             use Array1DUtil.floatArrayToArray instead
     */
    @Deprecated
    public static Object floatArrayToArray1D(float[] in, int inOffset, Object out, int outOffset, int length)
    {
        return Array1DUtil.floatArrayToArray(in, inOffset, out, outOffset, length);

    }

    /**
     * Convert and return the 'in' float array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array1DUtil.floatArrayToArray instead
     */
    @Deprecated
    public static Object floatArrayToArray1D(float[] in, Object out)
    {
        return Array1DUtil.floatArrayToArray(in, out);
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
     * @deprecated
     *             use Array1DUtil.intArrayToArray instead
     */
    @Deprecated
    public static Object intArrayToArray1D(int[] in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        return Array1DUtil.intArrayToArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array1DUtil.intArrayToArray instead
     */
    @Deprecated
    public static Object intArrayToArray1D(int[] in, Object out, boolean signed)
    {
        return Array1DUtil.intArrayToArray(in, out, signed);
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
     * @deprecated
     *             use Array1DUtil.shortArrayToArray instead
     */
    @Deprecated
    public static Object shortArrayToArray1D(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.shortArrayToArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array1DUtil.shortArrayToArray instead
     */
    @Deprecated
    public static Object shortArrayToArray1D(short[] in, Object out, boolean signed)
    {
        return Array1DUtil.shortArrayToArray(in, out, signed);
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
     * @deprecated
     *             use Array1DUtil.byteArrayToArray instead
     */
    @Deprecated
    public static Object byteArrayToArray1D(byte[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.byteArrayToArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Convert and return the 2D 'in' double array in 'out' array type.<br>
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
     * @deprecated
     *             use Array2DUtil.doubleArrayToArray instead
     */
    @Deprecated
    public static Object doubleArrayToArray2D(double[][] in, int inOffset, Object out, int outOffset, int length)
    {
        return Array2DUtil.doubleArrayToArray(in, inOffset, out, outOffset, length);
    }

    /**
     * Convert and return the 2D 'in' double array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array2DUtil.doubleArrayToArray instead
     */
    @Deprecated
    public static Object doubleArrayToArray2D(double[][] in, Object out)
    {
        return Array2DUtil.doubleArrayToArray(in, out);
    }

    /**
     * Convert and return the 2D 'in' float array in 'out' array type.<br>
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
     * @deprecated
     *             use Array2DUtil.floatArrayToArray instead
     */
    @Deprecated
    public static Object floatArrayToArray2D(float[][] in, int inOffset, Object out, int outOffset, int length)
    {
        return Array2DUtil.floatArrayToArray(in, inOffset, out, outOffset, length);

    }

    /**
     * Convert and return the 2D 'in' float array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array2DUtil.floatArrayToArray instead
     */
    @Deprecated
    public static Object floatArrayToArray2D(float[][] in, Object out)
    {
        return Array2DUtil.floatArrayToArray(in, out);
    }

    /**
     * Convert and return the 2D 'in' integer array in 'out' array type.<br>
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
     * @deprecated
     *             use Array2DUtil.intArrayToArray instead
     */
    @Deprecated
    public static Object intArrayToArray2D(int[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.intArrayToArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Convert and return the 2D 'in' integer array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.intArrayToArray instead
     */
    @Deprecated
    public static Object intArrayToArray2D(int[][] in, Object out, boolean signed)
    {
        return Array2DUtil.intArrayToArray(in, out, signed);
    }

    /**
     * Convert and return the 2D 'in' short array in 'out' array type.<br>
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
     * @deprecated
     *             use Array2DUtil.shortArrayToArray instead
     */
    @Deprecated
    public static Object shortArrayToArray2D(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.shortArrayToArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Convert and return the 2D 'in' short array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.shortArrayToArray instead
     */
    @Deprecated
    public static Object shortArrayToArray2D(short[][] in, Object out, boolean signed)
    {
        return Array2DUtil.shortArrayToArray(in, out, signed);
    }

    /**
     * Convert and return the 2D 'in' byte array in 'out' array type.<br>
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
     * @deprecated
     *             use Array2DUtil.byteArrayToArray instead
     */
    @Deprecated
    public static Object byteArrayToArray2D(byte[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.byteArrayToArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Convert and return the 2D 'in' byte array in 'out' array type.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.byteArrayToArray instead
     */
    @Deprecated
    public static Object byteArrayToArray2D(byte[][] in, Object out, boolean signed)
    {
        return Array2DUtil.byteArrayToArray(in, out, signed);
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
     * @deprecated
     *             use Array1DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[] arrayToDoubleArray1D(Object in, int inOffset, double[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToDoubleArray(in, inOffset, out, outOffset, length, signed);
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
     * @deprecated
     *             use Array1DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[] arrayToDoubleArray1D(Object in, double[] out, boolean signed)
    {
        return Array1DUtil.arrayToDoubleArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a double array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array1DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[] arrayToDoubleArray1D(Object in, boolean signed)
    {
        return Array1DUtil.arrayToDoubleArray(in, signed);
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
     * @deprecated
     *             use Array2DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[][] arrayToDoubleArray2D(Object in, int inOffset, double[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToDoubleArray(in, inOffset, out, outOffset, length, signed);
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
     * @deprecated
     *             use Array2DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[][] arrayToDoubleArray2D(Object in, double[][] out, boolean signed)
    {
        return Array2DUtil.arrayToDoubleArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a double array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.arrayToDoubleArray instead
     */
    @Deprecated
    public static double[][] arrayToDoubleArray2D(Object in, boolean signed)
    {
        return Array2DUtil.arrayToDoubleArray(in, signed);
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
     * @deprecated
     *             use Array1DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[] arrayToFloatArray1D(Object in, int inOffset, float[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToFloatArray(in, inOffset, out, outOffset, length, signed);
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
     * @deprecated
     *             use Array1DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[] arrayToFloatArray1D(Object in, float[] out, boolean signed)
    {
        return Array1DUtil.arrayToFloatArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a float array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array1DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[] arrayToFloatArray1D(Object in, boolean signed)
    {
        return Array1DUtil.arrayToFloatArray(in, signed);
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
     * @deprecated
     *             use Array2DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[][] arrayToFloatArray2D(Object in, int inOffset, float[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToFloatArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array2DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[][] arrayToFloatArray2D(Object in, float[][] out, boolean signed)
    {
        return Array2DUtil.arrayToFloatArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a float array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.arrayToFloatArray instead
     */
    @Deprecated
    public static float[][] arrayToFloatArray2D(Object in, boolean signed)
    {
        return Array2DUtil.arrayToFloatArray(in, signed);

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
     * @deprecated
     *             use Array1DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[] arrayToIntArray1D(Object in, int inOffset, int[] out, int outOffset, int length, boolean signed)
    {
        return Array1DUtil.arrayToIntArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array1DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[] arrayToIntArray1D(Object in, int[] out, boolean signed)
    {
        return Array1DUtil.arrayToIntArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a int array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array1DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[] arrayToIntArray1D(Object in, boolean signed)
    {
        return Array1DUtil.arrayToIntArray(in, signed);

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
     * @deprecated
     *             use Array2DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[][] arrayToIntArray2D(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToIntArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array2DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[][] arrayToIntArray2D(Object in, int[][] out, boolean signed)
    {
        return Array2DUtil.arrayToIntArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a int array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.arrayToIntArray instead
     */
    @Deprecated
    public static int[][] arrayToIntArray2D(Object in, boolean signed)
    {
        return Array2DUtil.arrayToIntArray(in, signed);

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
     * @deprecated
     *             use Array1DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[] arrayToShortArray1D(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToShortArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array1DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[] arrayToShortArray1D(Object in, short[] out, boolean signed)
    {
        return Array1DUtil.arrayToShortArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a short array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array1DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[] arrayToShortArray1D(Object in, boolean signed)
    {
        return Array1DUtil.arrayToShortArray(in, signed);

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
     * @deprecated
     *             use Array2DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[][] arrayToShortArray2D(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToShortArray(in, inOffset, out, outOffset, length, signed);

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
     * @deprecated
     *             use Array2DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[][] arrayToShortArray2D(Object in, short[][] out, boolean signed)
    {
        return Array2DUtil.arrayToShortArray(in, out, signed);
    }

    /**
     * Convert and return the 'in' array as a short array.<br>
     * 
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     * @deprecated
     *             use Array2DUtil.arrayToShortArray instead
     */
    @Deprecated
    public static short[][] arrayToShortArray2D(Object in, boolean signed)
    {
        return Array2DUtil.arrayToShortArray(in, signed);

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
     * @deprecated
     *             use Array1DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[] arrayToByteArray1D(Object in, int inOffset, byte[] out, int outOffset, int length)
    {
        return Array1DUtil.arrayToByteArray(in, inOffset, out, outOffset, length);
    }

    /**
     * Convert and return the 'in' array in 'out' byte array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array1DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[] arrayToByteArray1D(Object in, byte[] out)
    {
        return Array1DUtil.arrayToByteArray(in, out);
    }

    /**
     * Convert and return the 'in' array as a byte array.<br>
     * 
     * @param in
     *        input array
     * @deprecated
     *             use Array1DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[] arrayToByteArray1D(Object in)
    {
        return Array1DUtil.arrayToByteArray(in);

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
     * @deprecated
     *             use Array2DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[][] arrayToByteArray2D(Object in, int inOffset, byte[][] out, int outOffset, int length)
    {
        return Array2DUtil.arrayToByteArray(in, inOffset, out, outOffset, length);

    }

    /**
     * Convert and return the 'in' array in 'out' byte array.<br>
     * 
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @deprecated
     *             use Array2DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[][] arrayToByteArray2D(Object in, byte[][] out)
    {
        return Array2DUtil.arrayToByteArray(in, out);
    }

    /**
     * Convert and return the 'in' array as a byte array.<br>
     * 
     * @param in
     *        input array
     * @deprecated
     *             use Array2DUtil.arrayToByteArray instead
     */
    @Deprecated
    public static byte[][] arrayToByteArray2D(Object in)
    {
        return Array2DUtil.arrayToByteArray(in);

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

    public static Object arrayToDoubleArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToDoubleArray(array, signed);

            case 2:
                return Array2DUtil.arrayToDoubleArray(array, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToFloatArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToFloatArray(array, signed);

            case 2:
                return Array2DUtil.arrayToFloatArray(array, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToIntArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToIntArray(array, signed);

            case 2:
                return Array2DUtil.arrayToIntArray(array, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToShortArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToShortArray(array, signed);

            case 2:
                return Array2DUtil.arrayToShortArray(array, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToByteArray(Object array)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToByteArray(array);

            case 2:
                return Array2DUtil.arrayToByteArray(array);

            default:
                // not yet implemented
                return null;
        }
    }

    //
    //

    /**
     * Safe convert and return the 'in' array in 'out' array type.<br>
     * Output value is limited to output type limit :<br>
     * unsigned int input = 1000 --> unsigned byte output = 255<br>
     * int input = -1000 --> byte output = -128<br>
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
    public static Object arrayToSafeArray(Object in, int inOffset, Object out, int outOffset, int length, boolean signed)
    {
        final ArrayTypeInfo typeInfo = TypeUtil.getTypeInfo(in);

        switch (typeInfo.type)
        {
            case TypeUtil.TYPE_BYTE:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case TypeUtil.TYPE_SHORT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil
                                .shortArrayToSafeArray((short[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.shortArrayToSafeArray((short[][]) in, inOffset, out, outOffset, length,
                                signed);
                }
                break;

            case TypeUtil.TYPE_INT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.intArrayToSafeArray((int[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.intArrayToSafeArray((int[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case TypeUtil.TYPE_FLOAT:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil
                                .floatArrayToSafeArray((float[]) in, inOffset, out, outOffset, length, signed);

                    case 2:
                        return Array2DUtil.floatArrayToSafeArray((float[][]) in, inOffset, out, outOffset, length,
                                signed);
                }
                break;

            case TypeUtil.TYPE_DOUBLE:
                switch (typeInfo.dim)
                {
                    case 1:
                        return Array1DUtil.doubleArrayToSafeArray((double[]) in, inOffset, out, outOffset, length,
                                signed);

                    case 2:
                        return Array2DUtil.doubleArrayToSafeArray((double[][]) in, inOffset, out, outOffset, length,
                                signed);
                }
                break;
        }

        // not yet implemented
        return out;
    }

    public static Object arrayToSafeArray(Object in, Object out, boolean signed)
    {
        return arrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    /**
     * Same as doubleArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.doubleArrayToSafeArray instead
     */
    @Deprecated
    public static Object doubleArrayToSafeArray1D(double[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.doubleArrayToSafeArray(in, inOffset, out, outOffset, length, signed);
    }

    /**
     * Same as doubleArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.doubleArrayToSafeArray instead
     */
    @Deprecated
    public static Object doubleArrayToSafeArray1D(double[] in, Object out, boolean signed)
    {
        return Array1DUtil.doubleArrayToSafeArray(in, out, signed);
    }

    /**
     * Same as doubleArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.doubleArrayToSafeArray instead
     */
    @Deprecated
    public static Object doubleArrayToSafeArray2D(double[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.doubleArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as doubleArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.doubleArrayToSafeArray instead
     */
    @Deprecated
    public static Object doubleArrayToSafeArray2D(double[][] in, Object out, boolean signed)
    {
        return Array2DUtil.doubleArrayToSafeArray(in, out, signed);
    }

    /**
     * Same as floatArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.floatArrayToSafeArray instead
     */
    @Deprecated
    public static Object floatArrayToSafeArray1D(float[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.floatArrayToSafeArray(in, inOffset, out, outOffset, length, signed);
    }

    /**
     * Same as floatArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.floatArrayToSafeArray instead
     */
    @Deprecated
    public static Object floatArrayToSafeArray1D(float[] in, Object out, boolean signed)
    {
        return Array1DUtil.floatArrayToSafeArray(in, out, signed);
    }

    /**
     * Same as floatArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.floatArrayToSafeArray instead
     */
    @Deprecated
    public static Object floatArrayToSafeArray2D(float[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.floatArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as floatArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.floatArrayToSafeArray instead
     */
    @Deprecated
    public static Object floatArrayToSafeArray2D(float[][] in, Object out, boolean signed)
    {
        return Array2DUtil.floatArrayToSafeArray(in, out, signed);

    }

    /**
     * Same as intArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.intArrayToSafeArray instead
     */
    @Deprecated
    public static Object intArrayToSafeArray1D(int[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.intArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as intArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.intArrayToSafeArray instead
     */
    @Deprecated
    public static Object intArrayToSafeArray1D(int[] in, Object out, boolean signed)
    {
        return Array1DUtil.intArrayToSafeArray(in, out, signed);
    }

    /**
     * Same as intArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.intArrayToSafeArray instead
     */
    @Deprecated
    public static Object intArrayToSafeArray2D(int[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.intArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as intArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.intArrayToSafeArray instead
     */
    @Deprecated
    public static Object intArrayToSafeArray2D(int[][] in, Object out, boolean signed)
    {
        return Array2DUtil.intArrayToSafeArray(in, out, signed);

    }

    /**
     * Same as shortArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.shortArrayToSafeArray instead
     */
    @Deprecated
    public static Object shortArrayToSafeArray1D(short[] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.shortArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as shortArrayToArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.shortArrayToSafeArray instead
     */
    @Deprecated
    public static Object shortArrayToSafeArray1D(short[] in, Object out, boolean signed)
    {
        return Array1DUtil.shortArrayToSafeArray(in, out, signed);
    }

    /**
     * Same as shortArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.shortArrayToSafeArray instead
     */
    @Deprecated
    public static Object shortArrayToSafeArray2D(short[][] in, int inOffset, Object out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.shortArrayToSafeArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as shortArrayToArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.shortArrayToSafeArray instead
     */
    @Deprecated
    public static Object shortArrayToSafeArray2D(short[][] in, Object out, boolean signed)
    {
        return Array2DUtil.shortArrayToSafeArray(in, out, signed);

    }

    /**
     * Same as arrayToIntArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeIntArray instead
     */
    @Deprecated
    public static int[] arrayToSafeIntArray1D(Object in, int inOffset, int[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToSafeIntArray(in, inOffset, out, outOffset, length, signed);
    }

    /**
     * Same as arrayToIntArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeIntArray instead
     */
    @Deprecated
    public static int[] arrayToSafeIntArray1D(Object in, int[] out, boolean signed)
    {
        return Array1DUtil.arrayToSafeIntArray(in, out, signed);
    }

    /**
     * Same as arrayToIntArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeIntArray instead
     */
    @Deprecated
    public static int[][] arrayToSafeIntArray2D(Object in, int inOffset, int[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToSafeIntArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as arrayToIntArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeIntArray instead
     */
    @Deprecated
    public static int[][] arrayToSafeIntArray2D(Object in, int[][] out, boolean signed)
    {
        return Array2DUtil.arrayToSafeIntArray(in, out, signed);

    }

    /**
     * Same as arrayToShortArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeShortArray instead
     */
    @Deprecated
    public static short[] arrayToSafeShortArray1D(Object in, int inOffset, short[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToSafeShortArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as arrayToShortArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeShortArray instead
     */
    @Deprecated
    public static short[] arrayToSafeShortArray1D(Object in, short[] out, boolean signed)
    {
        return Array1DUtil.arrayToSafeShortArray(in, out, signed);

    }

    /**
     * Same as arrayToShortArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeShortArray instead
     */
    @Deprecated
    public static short[][] arrayToSafeShortArray2D(Object in, int inOffset, short[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToSafeShortArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as arrayToShortArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeShortArray instead
     */
    @Deprecated
    public static short[][] arrayToSafeShortArray2D(Object in, short[][] out, boolean signed)
    {
        return Array2DUtil.arrayToSafeShortArray(in, out, signed);

    }

    /**
     * Same as arrayToByteArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeByteArray instead
     */
    @Deprecated
    public static byte[] arrayToSafeByteArray1D(Object in, int inOffset, byte[] out, int outOffset, int length,
            boolean signed)
    {
        return Array1DUtil.arrayToSafeByteArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as arrayToByteArray1D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array1DUtil.arrayToSafeByteArray instead
     */
    @Deprecated
    public static byte[] arrayToSafeByteArray1D(Object in, byte[] out, boolean signed)
    {
        return Array1DUtil.arrayToSafeByteArray(in, out, signed);

    }

    /**
     * Same as arrayToByteArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeByteArray instead
     */
    @Deprecated
    public static byte[][] arrayToSafeByteArray2D(Object in, int inOffset, byte[][] out, int outOffset, int length,
            boolean signed)
    {
        return Array2DUtil.arrayToSafeByteArray(in, inOffset, out, outOffset, length, signed);

    }

    /**
     * Same as arrayToByteArray2D except that we clamp value to output type bounds.
     * 
     * @deprecated
     *             use Array2DUtil.arrayToSafeByteArray instead
     */
    @Deprecated
    public static byte[][] arrayToSafeByteArray2D(Object in, byte[][] out, boolean signed)
    {
        return Array2DUtil.arrayToSafeByteArray(in, out, signed);

    }

    //
    //
    //
    //
    //
    //
    //
    //

    public static Object arrayToSafeIntArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToSafeIntArray(array, null, signed);

            case 2:
                return Array2DUtil.arrayToSafeIntArray(array, null, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToSafeShortArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToSafeShortArray(array, null, signed);

            case 2:
                return Array2DUtil.arrayToSafeShortArray(array, null, signed);

            default:
                // not yet implemented
                return null;
        }
    }

    public static Object arrayToSafeByteArray(Object array, boolean signed)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToSafeByteArray(array, null, signed);

            case 2:
                return Array2DUtil.arrayToSafeByteArray(array, null, signed);

            default:
                // not yet implemented
                return null;
        }
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

    /**
     * Return the specified array as string<br>
     * Default representation use ':' as separator character<br>
     * <br>
     * ex : [0,1,2,3,4] --> "0:1:2:3:4"<br>
     * 
     * @param array
     *        1D array containing values to return as string
     */
    public static String arrayToString(Object array)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToString(array);

            default:
                // not yet implemented
                return null;
        }
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
    public static String array1DToString(Object array, boolean signed, boolean hexa, String separator, int size)
    {
        switch (TypeUtil.getNumDimension(array))
        {
            case 1:
                return Array1DUtil.arrayToString(array, signed, hexa, separator, size);

            default:
                // not yet implemented
                return null;
        }

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
    public static Object stringToArray1D(String value, int dataType)
    {
        return Array1DUtil.stringToArray(value, dataType);
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
    public static Object stringToArray1D(String value, int dataType, boolean hexa, String separator)
    {
        return Array1DUtil.stringToArray(value, dataType, hexa, separator);
    }

    //
    //
    //
    //

    /**
     * Creates a linear array with specified size, initial value and step. <br>
     * Example: to create the array [1,3,5,7] call createLinearArray(4,1,2)
     * 
     * @param size
     *        the size of the array to create
     * @param initialValue
     *        the initial value (i.e. the first element of the array, boolean signed)
     * @param step
     *        the step between consecutive array values
     */
    public static int[] createLinearIntArray(int size, int initialValue, int step)
    {
        final int[] array = new int[size];

        int value = initialValue;
        for (int i = 0; i < size; i++)
        {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a linear array with specified size, initial value and step. <br>
     * Example: to create the array [1,3,5,7] call createLinearArray(4,1,2)
     * 
     * @param size
     *        the size of the array to create
     * @param initialValue
     *        the initial value (i.e. the first element of the array)
     * @param step
     *        the step between consecutive array values
     */
    public static double[] createLinearDoubleArray(int size, double initialValue, double step)
    {
        final double[] array = new double[size];

        double value = initialValue;
        for (int i = 0; i < size; i++)
        {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     */
    public static byte[] toByteArray1D(boolean[] array)
    {
        return Array1DUtil.toByteArray(array, null, 0);
    }

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static byte[] toByteArray1D(boolean[] in, byte[] out, int offset)
    {
        return Array1DUtil.toByteArray(in, out, offset);
    }

    /**
     * Convert a byte array (unpacked form : 1 byte --> 1 boolean) to a boolean array
     */
    public static boolean[] toBooleanArray1D(byte[] array)
    {
        return Array1DUtil.toBooleanArray(array);
    }

    /**
     * Convert a boolean array to a byte array (unpacked form : 1 boolean --> 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * If (out == null) a new array is allocated.
     */
    public static boolean[] toBooleanArray1D(byte[] in, boolean[] out, int offset)
    {
        return Array1DUtil.toBooleanArray(in, out, offset);
    }
}
