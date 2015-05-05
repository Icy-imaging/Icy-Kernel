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
package icy.type;

import icy.math.MathUtil;
import icy.type.collection.array.ArrayDataType;
import icy.type.collection.array.ArrayUtil;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;

import loci.formats.FormatTools;
import ome.units.quantity.Time;
import ome.xml.model.enums.PixelType;

/**
 * @author stephane
 */
public class TypeUtil
{
    /**
     * Tag for byte data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#BYTE} instead
     */
    @Deprecated
    public static final int TYPE_BYTE = DataBuffer.TYPE_BYTE;

    /**
     * Tag for short data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#SHORT} instead
     */
    @Deprecated
    public static final int TYPE_SHORT = DataBuffer.TYPE_SHORT;

    /**
     * Tag for int data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#INT} instead
     */
    @Deprecated
    public static final int TYPE_INT = DataBuffer.TYPE_INT;

    /**
     * Tag for float data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#FLOAT} instead
     */
    @Deprecated
    public static final int TYPE_FLOAT = DataBuffer.TYPE_FLOAT;

    /**
     * Tag for double data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#DOUBLE} instead
     */
    @Deprecated
    public static final int TYPE_DOUBLE = DataBuffer.TYPE_DOUBLE;

    /**
     * Tag for undefined data (use DataBuffer reference)
     * 
     * @deprecated use {@link DataType#UNDEFINED} instead
     */
    @Deprecated
    public static final int TYPE_UNDEFINED = DataBuffer.TYPE_UNDEFINED;

    /**
     * Return the size (in byte) of the specified dataType
     * 
     * @deprecated use {@link DataType} method instead
     */
    @Deprecated
    public static int sizeOf(int dataType)
    {
        switch (dataType)
        {
            case TYPE_BYTE:
                return 1;
            case TYPE_SHORT:
                return 2;
            case TYPE_INT:
                return 4;
            case TYPE_FLOAT:
                return 4;
            case TYPE_DOUBLE:
                return 8;
        }

        return 0;
    }

    /**
     * Return true if specified dataType is a float type
     * 
     * @deprecated use {@link DataType} method instead
     */
    @Deprecated
    public static boolean isFloat(int dataType)
    {
        return (dataType == TYPE_FLOAT) || (dataType == TYPE_DOUBLE);
    }

    /**
     * Convert dataType to String
     * 
     * @deprecated use {@link DataType} method instead
     */
    @Deprecated
    public static String toLongString(int dataType)
    {
        switch (dataType)
        {
            case TYPE_BYTE:
                return "byte (8 bpp)";
            case TYPE_SHORT:
                return "short (16 bpp)";
            case TYPE_INT:
                return "int (32 bpp)";
            case TYPE_FLOAT:
                return "float (32 bbp)";
            case TYPE_DOUBLE:
                return "double (64 bbp)";
        }

        return "undefined";
    }

    /**
     * convert dataType to String
     * 
     * @deprecated use {@link DataType} method instead
     */
    @Deprecated
    public static String toString(int dataType)
    {
        switch (dataType)
        {
            case TYPE_BYTE:
                return "byte";
            case TYPE_SHORT:
                return "short";
            case TYPE_INT:
                return "int";
            case TYPE_FLOAT:
                return "float";
            case TYPE_DOUBLE:
                return "double";
        }

        return "undefined";
    }

    public static String toLongString(int dataType, boolean signed)
    {
        if (isFloat(dataType))
            return toLongString(dataType);

        return toString(signed) + " " + toLongString(dataType);
    }

    public static String toString(int dataType, boolean signed)
    {
        if (isFloat(dataType))
            return toString(dataType);

        return toString(signed) + " " + toString(dataType);
    }

    public static String toString(boolean signed)
    {
        if (signed)
            return "signed";

        return "unsigned";
    }

    /**
     * Return all data type as String items (can be used for ComboBox)
     * 
     * @deprecated use {@link DataType#getItems(boolean, boolean, boolean)} instead
     */
    @Deprecated
    public static String[] getItems(boolean longString, boolean wantUndef)
    {
        final String[] result;

        if (wantUndef)
            result = new String[6];
        else
            result = new String[5];

        if (longString)
        {
            result[0] = toLongString(TYPE_BYTE);
            result[1] = toLongString(TYPE_SHORT);
            result[2] = toLongString(TYPE_INT);
            result[3] = toLongString(TYPE_FLOAT);
            result[4] = toLongString(TYPE_DOUBLE);
            if (wantUndef)
                result[5] = toLongString(TYPE_UNDEFINED);
        }
        else
        {
            result[0] = toString(TYPE_BYTE);
            result[1] = toString(TYPE_SHORT);
            result[2] = toString(TYPE_INT);
            result[3] = toString(TYPE_FLOAT);
            result[4] = toString(TYPE_DOUBLE);
            if (wantUndef)
                result[5] = toString(TYPE_UNDEFINED);
        }

        return result;
    }

    /**
     * Return the dataType of specified array (passed as Object)
     * 
     * @deprecated use {@link ArrayDataType#getArrayDataType(Object)} instead
     */
    @Deprecated
    public static ArrayTypeInfo getTypeInfo(Object value)
    {
        final ArrayTypeInfo result = new ArrayTypeInfo(TYPE_UNDEFINED, 0);

        if (value instanceof byte[])
        {
            result.type = TYPE_BYTE;
            result.dim = 1;
        }
        else if (value instanceof short[])
        {
            result.type = TYPE_SHORT;
            result.dim = 1;
        }
        else if (value instanceof int[])
        {
            result.type = TYPE_INT;
            result.dim = 1;
        }
        else if (value instanceof float[])
        {
            result.type = TYPE_FLOAT;
            result.dim = 1;
        }
        else if (value instanceof double[])
        {
            result.type = TYPE_DOUBLE;
            result.dim = 1;
        }
        else if (value instanceof byte[][])
        {
            result.type = TYPE_BYTE;
            result.dim = 2;
        }
        else if (value instanceof short[][])
        {
            result.type = TYPE_SHORT;
            result.dim = 2;
        }
        else if (value instanceof int[][])
        {
            result.type = TYPE_INT;
            result.dim = 2;
        }
        else if (value instanceof float[][])
        {
            result.type = TYPE_FLOAT;
            result.dim = 2;
        }
        else if (value instanceof double[][])
        {
            result.type = TYPE_DOUBLE;
            result.dim = 2;
        }
        else if (value instanceof byte[][][])
        {
            result.type = TYPE_BYTE;
            result.dim = 3;
        }
        else if (value instanceof short[][][])
        {
            result.type = TYPE_SHORT;
            result.dim = 3;
        }
        else if (value instanceof int[][][])
        {
            result.type = TYPE_INT;
            result.dim = 3;
        }
        else if (value instanceof float[][][])
        {
            result.type = TYPE_FLOAT;
            result.dim = 3;
        }
        else if (value instanceof double[][][])
        {
            result.type = TYPE_DOUBLE;
            result.dim = 3;
        }
        else if (value instanceof byte[][][][])
        {
            result.type = TYPE_BYTE;
            result.dim = 4;
        }
        else if (value instanceof short[][][][])
        {
            result.type = TYPE_SHORT;
            result.dim = 4;
        }
        else if (value instanceof int[][][][])
        {
            result.type = TYPE_INT;
            result.dim = 4;
        }
        else if (value instanceof float[][][][])
        {
            result.type = TYPE_FLOAT;
            result.dim = 4;
        }
        else if (value instanceof double[][][][])
        {
            result.type = TYPE_DOUBLE;
            result.dim = 4;
        }
        else if (value instanceof byte[][][][][])
        {
            result.type = TYPE_BYTE;
            result.dim = 5;
        }
        else if (value instanceof short[][][][][])
        {
            result.type = TYPE_SHORT;
            result.dim = 5;
        }
        else if (value instanceof int[][][][][])
        {
            result.type = TYPE_INT;
            result.dim = 5;
        }
        else if (value instanceof float[][][][][])
        {
            result.type = TYPE_FLOAT;
            result.dim = 5;
        }
        else if (value instanceof double[][][][][])
        {
            result.type = TYPE_DOUBLE;
            result.dim = 5;
        }

        return result;
    }

    /**
     * Return the dataType of specified array (passed as Object)
     * 
     * @deprecated use {@link ArrayUtil#getDataType(Object)} method instead
     */
    @Deprecated
    public static int getDataType(Object value)
    {
        return getTypeInfo(value).type;
    }

    /**
     * Return the number of dimension specified array (passed as Object)
     * 
     * @deprecated use {@link ArrayUtil#getDim(Object)} method instead
     */
    @Deprecated
    public static int getNumDimension(Object value)
    {
        return getTypeInfo(value).dim;
    }

    /**
     * Return the dataType from string
     * 
     * @deprecated use {@link DataType#getDataType(String)} method instead
     */
    @Deprecated
    public static int getDataType(String value)
    {
        final String s = value.toLowerCase();

        if (toString(TYPE_BYTE).equals(s) || toLongString(TYPE_BYTE).equals(s))
            return TYPE_BYTE;
        if (toString(TYPE_SHORT).equals(s) || toLongString(TYPE_SHORT).equals(s))
            return TYPE_SHORT;
        if (toString(TYPE_INT).equals(s) || toLongString(TYPE_INT).equals(s))
            return TYPE_INT;
        if (toString(TYPE_FLOAT).equals(s) || toLongString(TYPE_FLOAT).equals(s))
            return TYPE_FLOAT;
        if (toString(TYPE_DOUBLE).equals(s) || toLongString(TYPE_DOUBLE).equals(s))
            return TYPE_DOUBLE;

        return TYPE_UNDEFINED;
    }

    /**
     * Return the dataType corresponding to the specified DataBuffer type
     * 
     * @deprecated use {@link DataType#getDataTypeFromDataBufferType(int)} instead
     */
    @Deprecated
    public static int dataBufferTypeToDataType(int dataBufferType)
    {
        switch (dataBufferType)
        {
            case DataBuffer.TYPE_BYTE:
                return TypeUtil.TYPE_BYTE;

            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                return TypeUtil.TYPE_SHORT;

            case DataBuffer.TYPE_INT:
                return TypeUtil.TYPE_INT;

            case DataBuffer.TYPE_FLOAT:
                return TypeUtil.TYPE_FLOAT;

            case DataBuffer.TYPE_DOUBLE:
                return TypeUtil.TYPE_DOUBLE;

            default:
                return TypeUtil.TYPE_UNDEFINED;
        }
    }

    /**
     * Return true if specified DataBuffer type is considered as signed type
     */
    public static boolean isSignedDataBufferType(int type)
    {
        switch (type)
        {
            case DataBuffer.TYPE_BYTE:
                // assume byte is unsigned
                return false;

            case DataBuffer.TYPE_SHORT:
                return true;

            case DataBuffer.TYPE_USHORT:
                return false;

            case DataBuffer.TYPE_INT:
                // assume int is unsigned
                return false;

            case DataBuffer.TYPE_FLOAT:
                return true;

            case DataBuffer.TYPE_DOUBLE:
                return true;

            default:
                return false;
        }
    }

    /**
     * Return the data type corresponding to the specified FormatTools type
     * 
     * @deprecated use {@link DataType#getDataTypeFromFormatToolsType(int)} instead
     */
    @Deprecated
    public static int formatToolsTypeToDataType(int type)
    {
        switch (type)
        {
            case FormatTools.INT8:
            case FormatTools.UINT8:
                return TypeUtil.TYPE_BYTE;

            case FormatTools.INT16:
            case FormatTools.UINT16:
                return TypeUtil.TYPE_SHORT;

            case FormatTools.INT32:
            case FormatTools.UINT32:
                return TypeUtil.TYPE_INT;

            case FormatTools.FLOAT:
                return TypeUtil.TYPE_FLOAT;

            case FormatTools.DOUBLE:
                return TypeUtil.TYPE_DOUBLE;

            default:
                return TypeUtil.TYPE_UNDEFINED;
        }
    }

    /**
     * Return true if specified FormatTools type is a signed type
     */
    public static boolean isSignedFormatToolsType(int type)
    {
        switch (type)
        {
            case FormatTools.INT8:
            case FormatTools.INT16:
            case FormatTools.INT32:
            case FormatTools.FLOAT:
            case FormatTools.DOUBLE:
                return true;

            case FormatTools.UINT8:
            case FormatTools.UINT16:
            case FormatTools.UINT32:
                return false;

            default:
                return false;
        }
    }

    /**
     * Return the dataType corresponding to the specified PixelType
     * 
     * @deprecated use {@link DataType#getDataTypeFromPixelType(PixelType)} instead
     */
    @Deprecated
    public static int pixelTypeToDataType(PixelType type)
    {
        switch (type)
        {
            case INT8:
            case UINT8:
                return TypeUtil.TYPE_BYTE;

            case INT16:
            case UINT16:
                return TypeUtil.TYPE_SHORT;

            case INT32:
            case UINT32:
                return TypeUtil.TYPE_INT;

            case FLOAT:
                return TypeUtil.TYPE_FLOAT;

            case DOUBLE:
                return TypeUtil.TYPE_DOUBLE;

            default:
                return TypeUtil.TYPE_UNDEFINED;
        }
    }

    /**
     * Return true if specified PixelType is signed
     */
    public static boolean isSignedPixelType(PixelType type)
    {
        switch (type)
        {
            case INT8:
            case INT16:
            case INT32:
            case FLOAT:
            case DOUBLE:
                return true;

            case UINT8:
            case UINT16:
            case UINT32:
                return false;

            default:
                return false;
        }
    }

    /**
     * Return the PixelType corresponding to the specified data type
     * 
     * @deprecated use {@link DataType#toPixelType()} instead
     */
    @Deprecated
    public static PixelType dataTypeToPixelType(int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                if (signed)
                    return PixelType.INT8;
                return PixelType.UINT8;

            case TypeUtil.TYPE_SHORT:
                if (signed)
                    return PixelType.INT16;
                return PixelType.UINT16;

            case TypeUtil.TYPE_FLOAT:
                return PixelType.FLOAT;

            case TypeUtil.TYPE_DOUBLE:
                return PixelType.DOUBLE;

            default:
                if (signed)
                    return PixelType.INT32;
                return PixelType.UINT32;
        }
    }

    /**
     * Unsign the specified byte value and return it as int
     */
    public static int unsign(byte value)
    {
        return value & 0xFF;
    }

    /**
     * Unsign the specified short value and return it as int
     */
    public static int unsign(short value)
    {
        return value & 0xFFFF;
    }

    /**
     * Unsign the specified byte value and return it as long
     */
    public static long unsignL(byte value)
    {
        return value & 0xFFL;
    }

    /**
     * Unsign the specified short value and return it as long
     */
    public static long unsignL(short value)
    {
        return value & 0xFFFFL;
    }

    /**
     * Unsign the specified int value and return it as long
     */
    public static long unsign(int value)
    {
        return value & 0xFFFFFFFFL;
    }

    /**
     * Unsign the specified long value and return it as double (possible information loss)
     */
    public static double unsign(long value)
    {
        final double result = value;
        if (result < 0d)
            return MathUtil.POW2_64_DOUBLE + result;

        return result;
    }

    /**
     * Unsign the specified long value and return it as float (possible information loss)
     */
    public static float unsignF(long value)
    {
        final float result = value;
        if (result < 0f)
            return MathUtil.POW2_64_FLOAT + result;

        return result;
    }

    public static int toShort(byte value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static int toInt(byte value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static int toInt(short value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static int toInt(float value)
    {
        // we have to cast to long before else value is limited to
        // [Integer.MIN_VALUE..Integer.MAX_VALUE] range
        return (int) (long) value;
    }

    public static int toInt(double value)
    {
        // we have to cast to long before else value is limited to
        // [Integer.MIN_VALUE..Integer.MAX_VALUE] range
        return (int) (long) value;
    }

    public static long toLong(byte value, boolean signed)
    {
        if (signed)
            return value;

        return unsignL(value);
    }

    public static long toLong(short value, boolean signed)
    {
        if (signed)
            return value;

        return unsignL(value);
    }

    public static long toLong(int value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static long toLong(float value)
    {
        // handle unsigned long type (else value is clamped to Long.MAX_VALUE)
        if (value > DataType.LONG_MAX_VALUE_F)
            return ((long) (value - DataType.LONG_MAX_VALUE_F)) + 0x8000000000000000L;

        return (long) value;
    }

    public static long toLong(double value)
    {
        // handle unsigned long type (else value is clamped to Long.MAX_VALUE)
        if (value > DataType.LONG_MAX_VALUE)
            return ((long) (value - DataType.LONG_MAX_VALUE)) + 0x8000000000000000L;

        return (long) value;
    }

    public static float toFloat(byte value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static float toFloat(short value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static float toFloat(int value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static float toFloat(long value, boolean signed)
    {
        if (signed)
            return value;

        return unsignF(value);
    }

    public static double toDouble(byte value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static double toDouble(short value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static double toDouble(int value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    public static double toDouble(long value, boolean signed)
    {
        if (signed)
            return value;

        return unsign(value);
    }

    /**
     * Safe integer evaluation from Integer object.<br>
     * Return <code>defaultValue</code> if specified object is null.
     */
    public static int getInt(Integer obj, int defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return obj.intValue();
    }

    /**
     * Safe float evaluation from Float object.<br>
     * Return <code>defaultValue</code> if specified object is null.
     */
    public static float getFloat(Float obj, float defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return obj.floatValue();
    }

    /**
     * Safe double evaluation from Double object.<br>
     * Return <code>defaultValue</code> if <code>obj</code> is null or equal to infinite with
     * <code>allowInfinite</code> set to false.
     */
    public static double getDouble(Double obj, double defaultValue, boolean allowInfinite)
    {
        if (obj == null)
            return defaultValue;

        final double result = obj.doubleValue();

        if ((!allowInfinite) && Double.isInfinite(result))
            return defaultValue;

        return result;
    }

    /**
     * Safe double evaluation from Double object.<br>
     * Return <code>defaultValue</code> if specified object is null.
     */
    public static double getDouble(Double obj, double defaultValue)
    {
        return getDouble(obj, defaultValue, true);
    }

    public static Point toPoint(Point2D p)
    {
        return new Point((int) p.getX(), (int) p.getY());
    }

    public static Point2D.Double toPoint2D(Point p)
    {
        return new Point2D.Double(p.x, p.y);
    }

    public static Point toPoint(Dimension d)
    {
        return new Point(d.width, d.height);
    }

    public static Point2D.Double toPoint2D(Dimension d)
    {
        return new Point2D.Double(d.width, d.height);
    }

    public static Dimension toDimension(Point p)
    {
        return new Dimension(p.x, p.y);
    }

    /**
     * Create an array of Point from the input integer array.<br>
     * <br>
     * The format of the input array should be as follow:<br>
     * <code>input.lenght</code> = number of point * 2.<br>
     * <code>input[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i><br>
     * <code>input[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i><br>
     */
    public static Point[] toPoint(int[] input)
    {
        final Point[] result = new Point[input.length / 2];

        int pt = 0;
        for (int i = 0; i < input.length; i += 2)
            result[pt++] = new Point(input[i + 0], input[i + 1]);

        return result;
    }

    /**
     * Return the minimum value for the specified dataType
     * 
     * @deprecated use {@link DataType#getMinValue()} instead
     */
    @Deprecated
    public static double getMinValue(int dataType, boolean signed)
    {
        return getDefaultBounds(dataType, signed)[0];
    }

    /**
     * Return the maximum value for the specified dataType
     * 
     * @deprecated use {@link DataType#getMaxValue()} instead
     */
    @Deprecated
    public static double getMaxValue(int dataType, boolean signed)
    {
        return getDefaultBounds(dataType, signed)[1];
    }

    /**
     * Get the default bounds for the specified dataType
     * 
     * @deprecated use {@link DataType#getDefaultBounds()} instead
     */
    @Deprecated
    public static double[] getDefaultBounds(int dataType, boolean signed)
    {
        return DataType.getDataType(dataType, signed).getDefaultBounds();
    }
}
