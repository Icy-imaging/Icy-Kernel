/**
 * 
 */
package icy.type;

import icy.math.MathUtil;

import java.awt.image.DataBuffer;
import java.util.ArrayList;

import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;

/**
 * DataType class.<br>
 * This class is used to define the internal native data type of a given object.
 * 
 * @author Stephane
 */
public enum DataType
{
    UBYTE, BYTE, USHORT, SHORT, UINT, INT, ULONG, LONG, FLOAT, DOUBLE, UNDEFINED;

    /**
     * cached
     */
    public static final double UBYTE_MAX_VALUE = MathUtil.POW2_8_DOUBLE - 1;
    public static final double USHORT_MAX_VALUE = MathUtil.POW2_16_DOUBLE - 1;
    public static final double UINT_MAX_VALUE = MathUtil.POW2_32_DOUBLE - 1;
    public static final double ULONG_MAX_VALUE = MathUtil.POW2_64_DOUBLE - 1;
    public static final double INT_MIN_VALUE = Integer.MIN_VALUE;
    public static final double LONG_MIN_VALUE = Long.MIN_VALUE;
    public static final double INT_MAX_VALUE = Integer.MAX_VALUE;
    public static final double LONG_MAX_VALUE = Long.MAX_VALUE;

    public static final float UBYTE_MAX_VALUE_F = MathUtil.POW2_8_FLOAT - 1;
    public static final float USHORT_MAX_VALUE_F = MathUtil.POW2_16_FLOAT - 1;
    public static final float UINT_MAX_VALUE_F = MathUtil.POW2_32_FLOAT - 1;
    public static final float ULONG_MAX_VALUE_F = MathUtil.POW2_64_FLOAT - 1;
    public static final float INT_MIN_VALUE_F = Integer.MIN_VALUE;
    public static final float LONG_MIN_VALUE_F = Long.MIN_VALUE;
    public static final float INT_MAX_VALUE_F = Integer.MAX_VALUE;
    public static final float LONG_MAX_VALUE_F = Long.MAX_VALUE;

    /**
     * Return all dataType as String items array (can be used for ComboBox).<br>
     * 
     * @param javaTypeOnly
     *        Define if we want only java compatible data type (no unsigned integer types)
     * @param longString
     *        Define if we want long string format (bpp information)
     * @param wantUndef
     *        Define if we want the UNDEFINED data type in the list
     */
    public static String[] getItems(boolean javaTypeOnly, boolean longString, boolean wantUndef)
    {
        final ArrayList<String> result = new ArrayList<String>();

        for (DataType dataType : DataType.values())
            if (((!javaTypeOnly) || dataType.isJavaType()) && (wantUndef || (dataType != UNDEFINED)))
                result.add(dataType.toString(longString));

        return (String[]) result.toArray();
    }

    /**
     * Return a DataType from the specified string.<br>
     * ex : <code>getDataType("byte")</code> will return <code>DataType.BYTE</code>
     */
    public static DataType getDataType(String value)
    {
        for (DataType dataType : DataType.values())
            if (dataType.toString(false).equals(value) || dataType.toString(true).equals(value))
                return dataType;

        return UNDEFINED;
    }

    /**
     * Return a DataType from old dataType.<br>
     * ex : <code>getDataTypeFromOldDataType(TypeUtil.BYTE, false)</code> will return
     * <code>DataType.UBYTE</code>
     */
    public static DataType getDataType(int oldDataType, boolean signed)
    {
        switch (oldDataType)
        {
            case TypeUtil.TYPE_BYTE:
                if (signed)
                    return BYTE;
                return UBYTE;
            case TypeUtil.TYPE_SHORT:
                if (signed)
                    return SHORT;
                return USHORT;
            case TypeUtil.TYPE_INT:
                if (signed)
                    return INT;
                return UINT;
            case TypeUtil.TYPE_FLOAT:
                return FLOAT;
            case TypeUtil.TYPE_DOUBLE:
                return DOUBLE;
            default:
                return UNDEFINED;
        }
    }

    /**
     * Return a DataType from old dataType.<br>
     * ex : <code>getDataType(TypeUtil.BYTE)</code> will return <code>DataType.BYTE</code>
     */
    public static DataType getDataType(int oldDataType)
    {
        return getDataType(oldDataType, true);
    }

    /**
     * Return a DataType from the specified primitive class type
     */
    public static DataType getDataType(Class<?> classType)
    {
        if (classType.equals(java.lang.Byte.TYPE))
            return DataType.BYTE;
        if (classType.equals(java.lang.Short.TYPE))
            return DataType.SHORT;
        if (classType.equals(java.lang.Integer.TYPE))
            return DataType.INT;
        if (classType.equals(java.lang.Long.TYPE))
            return DataType.LONG;
        if (classType.equals(java.lang.Float.TYPE))
            return DataType.FLOAT;
        if (classType.equals(java.lang.Double.TYPE))
            return DataType.DOUBLE;

        return DataType.UNDEFINED;
    }

    /**
     * Return a DataType from the specified DataBuffer type.<br>
     * ex : <code>getDataTypeFromDataBufferType(DataBuffer.TYPE_BYTE)</code> will return
     * <code>DataType.UBYTE</code>
     */
    public static DataType getDataTypeFromDataBufferType(int dataBufferType)
    {
        switch (dataBufferType)
        {
            case DataBuffer.TYPE_BYTE:
                // consider as unsigned by default
                return UBYTE;
            case DataBuffer.TYPE_SHORT:
                return SHORT;
            case DataBuffer.TYPE_USHORT:
                return USHORT;
            case DataBuffer.TYPE_INT:
                // consider as unsigned by default
                return UINT;
            case DataBuffer.TYPE_FLOAT:
                return FLOAT;
            case DataBuffer.TYPE_DOUBLE:
                return DOUBLE;
            default:
                return UNDEFINED;
        }
    }

    /**
     * Return a DataType from the specified FormatTools type.<br>
     * ex : <code>getDataTypeFromFormatToolsType(FormatTools.UINT8)</code> will return
     * <code>DataType.UBYTE</code>
     */
    public static DataType getDataTypeFromFormatToolsType(int type)
    {
        switch (type)
        {
            case FormatTools.INT8:
                return BYTE;
            case FormatTools.UINT8:
                return UBYTE;
            case FormatTools.INT16:
                return SHORT;
            case FormatTools.UINT16:
                return USHORT;
            case FormatTools.INT32:
                return INT;
            case FormatTools.UINT32:
                return UINT;
            case FormatTools.FLOAT:
                return FLOAT;
            case FormatTools.DOUBLE:
                return DOUBLE;
            default:
                return UNDEFINED;
        }
    }

    /**
     * Return a DataType from the specified PixelType.<br>
     * ex : <code>getDataTypeFromPixelType(FormatTools.UINT8)</code> will return
     * <code>DataType.UBYTE</code>
     */
    public static DataType getDataTypeFromPixelType(PixelType type)
    {
        switch (type)
        {
            case INT8:
                return BYTE;
            case UINT8:
                return UBYTE;
            case INT16:
                return SHORT;
            case UINT16:
                return USHORT;
            case INT32:
                return INT;
            case UINT32:
                return UINT;
            case FLOAT:
                return FLOAT;
            case DOUBLE:
                return DOUBLE;
            default:
                return UNDEFINED;
        }
    }

    /**
     * Return the java compatible data type (signed integer type only).<br>
     * Can be only one of the following :<br>
     * {@link DataType#BYTE}<br>
     * {@link DataType#SHORT}<br>
     * {@link DataType#INT}<br>
     * {@link DataType#LONG}<br>
     * {@link DataType#FLOAT}<br>
     * {@link DataType#DOUBLE}<br>
     * {@link DataType#UNDEFINED}<br>
     */
    public DataType getJavaType()
    {
        switch (this)
        {
            case UBYTE:
                return BYTE;
            case USHORT:
                return SHORT;
            case UINT:
                return INT;
            case ULONG:
                return LONG;
            default:
                return this;
        }
    }

    /**
     * Return the minimum value for current DataType
     */
    public double getMinValue()
    {
        switch (this)
        {
            case BYTE:
                return Byte.MIN_VALUE;
            case SHORT:
                return Short.MIN_VALUE;
            case INT:
                return Integer.MIN_VALUE;
            case LONG:
                // double type loss information here...
                return Long.MIN_VALUE;
            case FLOAT:
                return Float.MIN_VALUE;
            case DOUBLE:
                return Double.MIN_VALUE;
            default:
                return 0d;
        }
    }

    /**
     * Return the maximum value for current DataType
     */
    public double getMaxValue()
    {
        switch (this)
        {
            case UBYTE:
                return UBYTE_MAX_VALUE;
            case BYTE:
                return Byte.MAX_VALUE;
            case USHORT:
                return USHORT_MAX_VALUE;
            case SHORT:
                return Short.MAX_VALUE;
            case UINT:
                return UINT_MAX_VALUE;
            case INT:
                return Integer.MAX_VALUE;
            case ULONG:
                // double type loss information here...
                return ULONG_MAX_VALUE;
            case LONG:
                // double type loss information here...
                return Long.MAX_VALUE;
            case FLOAT:
                return Float.MAX_VALUE;
            case DOUBLE:
                return Double.MAX_VALUE;
            default:
                return 0d;
        }
    }

    /**
     * Get the default bounds for current DataType.<br>
     * This actually returns <code>[0,1]</code> for Float or Double DataType.
     */
    public double[] getDefaultBounds()
    {
        if (isFloat())
            return new double[] {0d, 1d};

        return new double[] {getMinValue(), getMaxValue()};
    }

    /**
     * Get the bounds for current DataType
     */
    public double[] getDefautBounds()
    {
        return new double[] {getMinValue(), getMaxValue()};
    }

    /**
     * Return true if this is a compatible java data type (signed integer type only)
     */
    public boolean isJavaType()
    {
        return (this == BYTE) || (this == SHORT) || (this == INT) || (this == LONG) || (this == FLOAT)
                || (this == LONG);
    }

    /**
     * Return true if this is a signed data type
     */
    public boolean isSigned()
    {
        return (this == BYTE) || (this == SHORT) || (this == INT) || (this == LONG) || (this == FLOAT)
                || (this == LONG);
    }

    /**
     * Return true if this is a float data type
     */
    public boolean isFloat()
    {
        return (this == FLOAT) || (this == DOUBLE);
    }

    /**
     * Return true if this is an integer data type
     */
    public boolean isInteger()
    {
        return !isFloat();
    }

    /**
     * @deprecated uses {@link #getSize()} instead
     */
    @Deprecated
    public int sizeOf()
    {
        return getSize();
    }

    /**
     * Return the size (in byte) of the specified dataType
     */
    public int getSize()
    {
        switch (this)
        {
            case UBYTE:
            case BYTE:
                return 1;
            case USHORT:
            case SHORT:
                return 2;
            case UINT:
            case INT:
            case FLOAT:
                return 4;
            case ULONG:
            case LONG:
            case DOUBLE:
                return 8;
            default:
                return 0;
        }
    }

    /**
     * Return the size (in bit) of the specified dataType
     */
    public int getBitSize()
    {
        return getSize() * 8;
    }

    /**
     * Return true if specified data type has same "basic" type (no sign information) data type
     */
    public boolean isSameJavaType(DataType dataType)
    {
        return dataType.getJavaType() == getJavaType();
    }

    /**
     * Return the corresponding primitive class type corresponding to this DataType.
     */
    public Class<?> toPrimitiveClass()
    {
        switch (this)
        {
            case UBYTE:
            case BYTE:
                return java.lang.Byte.TYPE;
            case USHORT:
            case SHORT:
                return java.lang.Short.TYPE;
            case UINT:
            case INT:
                return java.lang.Integer.TYPE;
            case ULONG:
            case LONG:
                return java.lang.Long.TYPE;
            case FLOAT:
                return java.lang.Float.TYPE;
            case DOUBLE:
                return java.lang.Double.TYPE;
            default:
                return java.lang.Void.TYPE;
        }
    }

    /**
     * Return the DataBuffer type corresponding to current DataType
     */
    public int toDataBufferType()
    {
        switch (this)
        {
            case UBYTE:
            case BYTE:
                return DataBuffer.TYPE_BYTE;
            case SHORT:
                return DataBuffer.TYPE_SHORT;
            case USHORT:
                return DataBuffer.TYPE_USHORT;
            case UINT:
            case INT:
                return DataBuffer.TYPE_INT;
            case FLOAT:
                return DataBuffer.TYPE_FLOAT;
            case DOUBLE:
                return DataBuffer.TYPE_DOUBLE;
            default:
                return DataBuffer.TYPE_UNDEFINED;
        }
    }

    /**
     * Return the PixelType corresponding to current DataType
     */
    public PixelType toPixelType()
    {
        switch (this)
        {
            case BYTE:
                return PixelType.INT8;
            case UBYTE:
                return PixelType.UINT8;
            case SHORT:
                return PixelType.INT16;
            case USHORT:
                return PixelType.UINT16;
            case INT:
                return PixelType.INT32;
            case UINT:
                return PixelType.UINT32;
            case FLOAT:
                return PixelType.FLOAT;
            case DOUBLE:
                return PixelType.DOUBLE;
            default:
                return PixelType.UINT8;
        }
    }

    /**
     * Convert DataType to String.<br>
     * 
     * @param longString
     *        Define if we want long description (bpp information)
     */
    public String toString(boolean longString)
    {
        if (longString)
            return toLongString();

        return toString();
    }

    /**
     * Convert DataType to long String (long description with bpp information)
     */
    public String toLongString()
    {
        String result = toString();

        if (isInteger() && isSigned())
            result = "signed " + result;

        switch (this)
        {
            case UBYTE:
            case BYTE:
                return result + " (8 bpp)";
            case USHORT:
            case SHORT:
                return result + " (16 bpp)";
            case UINT:
            case INT:
            case FLOAT:
                return result + " (32 bpp)";
            case ULONG:
            case LONG:
            case DOUBLE:
                return result + " (64 bpp)";
            default:
                return result;
        }
    }

    @Override
    public String toString()
    {
        switch (this)
        {
            case UBYTE:
                return "unsigned byte";
            case BYTE:
                return "byte";
            case USHORT:
                return "unsigned short";
            case SHORT:
                return "short";
            case UINT:
                return "unsigned int";
            case INT:
                return "int";
            case ULONG:
                return "unsigned long";
            case LONG:
                return "long";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            default:
                return "undefined";
        }
    }
}
