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
package icy.type;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;

import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;

/**
 * @author stephane
 */
public class TypeUtil
{
    /** Tag for byte data (use DataBuffer reference) */
    public static final int TYPE_BYTE = DataBuffer.TYPE_BYTE;

    /** Tag for short data (use DataBuffer reference) */
    public static final int TYPE_SHORT = DataBuffer.TYPE_SHORT;

    /** Tag for int data (use DataBuffer reference) */
    public static final int TYPE_INT = DataBuffer.TYPE_INT;

    /** Tag for float data (use DataBuffer reference) */
    public static final int TYPE_FLOAT = DataBuffer.TYPE_FLOAT;

    /** Tag for double data (use DataBuffer reference) */
    public static final int TYPE_DOUBLE = DataBuffer.TYPE_DOUBLE;

    /** Tag for undefined data (use DataBuffer reference) */
    public static final int TYPE_UNDEFINED = DataBuffer.TYPE_UNDEFINED;

    /**
     * Return the size (in byte) of the specified dataType
     */
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
     */
    public static boolean isFloat(int dataType)
    {
        return (dataType == TYPE_FLOAT) || (dataType == TYPE_DOUBLE);
    }

    /**
     * convert dataType to String
     */
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
     */
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
     * return all data type as String items (can be used for ComboBox)
     */
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
     */
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
     * Return the dataType from string
     */
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
     * Return the dataType of specified array (passed as Object)
     */
    public static int getDataType(Object value)
    {
        return getTypeInfo(value).type;
    }

    /**
     * Return the dataType corresponding to the specified DataBuffer type
     */
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
     */
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
     */
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
     */
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
     * Return the number of dimension specified array (passed as Object)
     */
    public static int getNumDimension(Object value)
    {
        return getTypeInfo(value).dim;
    }

    public static int toShort(byte value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFF;
    }

    public static int toInt(byte value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFF;
    }

    public static int toInt(short value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFFFF;
    }

    public static float toFloat(byte value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFF;
    }

    public static float toFloat(short value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFFFF;
    }

    public static float toFloat(int value, boolean signed)
    {
        if (signed)
            return value;

        return (long) value & 0xFFFFFFFF;
    }

    public static float toFloat(double value)
    {
        return (float) value;
    }

    public static double toDouble(byte value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFF;
    }

    public static double toDouble(short value, boolean signed)
    {
        if (signed)
            return value;

        return value & 0xFFFF;
    }

    public static double toDouble(int value, boolean signed)
    {
        if (signed)
            return value;

        return (long) value & 0xFFFFFFFF;
    }

    public static int unsign(byte value)
    {
        return value & 0xFF;
    }

    public static int unsign(short value)
    {
        return value & 0xFFFF;
    }

    public static long unsign(int value)
    {
        return ((long) value) & 0xFFFFFFFF;
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
     * Return the minimum value for the specified dataType
     */
    public static double getMinValue(int dataType, boolean signed)
    {
        return getDefaultBounds(dataType, signed)[0];
    }

    /**
     * Return the maximum value for the specified dataType
     */
    public static double getMaxValue(int dataType, boolean signed)
    {
        return getDefaultBounds(dataType, signed)[1];
    }

    /**
     * Get the default bounds for the specified dataType
     */
    public static double[] getDefaultBounds(int dataType, boolean signed)
    {
        final double min;
        final double max;

        // get default min and max for datatype
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                if (signed)
                {
                    min = Byte.MIN_VALUE;
                    max = Byte.MAX_VALUE;
                }
                else
                {
                    min = 0x00;
                    max = 0xFF;
                }
                break;

            case TypeUtil.TYPE_SHORT:
                if (signed)
                {
                    min = Short.MIN_VALUE;
                    max = Short.MAX_VALUE;
                }
                else
                {
                    min = 0x0000;
                    max = 0xFFFF;
                }
                break;

            case DataBuffer.TYPE_INT:
                if (signed)
                {
                    min = Integer.MIN_VALUE;
                    max = Integer.MAX_VALUE;
                }
                else
                {
                    min = 0x00000000L;
                    max = 0xFFFFFFFFL;
                }
                break;

            default:
                min = 0d;
                max = 1d;
                break;
        }

        return new double[] {min, max};
    }

}
