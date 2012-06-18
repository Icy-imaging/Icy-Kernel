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
    UBYTE
    {
        @Override
        public DataType getJavaType()
        {
            return BYTE;
        }

        @Override
        public double getMinValue()
        {
            return 0d;
        }

        @Override
        public double getMaxValue()
        {
            return MathUtil.POW2_8_DOUBLE - 1d;
        }

        @Override
        public boolean isJavaType()
        {
            return false;
        }

        @Override
        public boolean isSigned()
        {
            return false;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Byte.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return java.lang.Byte.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_BYTE;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.UINT8;
        }

        @Override
        public String toLongString()
        {
            return "unsigned byte (8 bits)";
        }

        @Override
        public String toString()
        {
            return "unsigned byte";
        }
    },
    BYTE
    {
        @Override
        public DataType getJavaType()
        {
            return BYTE;
        }

        @Override
        public double getMinValue()
        {
            return Byte.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            return Byte.MAX_VALUE;
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Byte.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Byte.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_BYTE;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.INT8;
        }

        @Override
        public String toLongString()
        {
            return "signed byte (8 bits)";
        }

        @Override
        public String toString()
        {
            return "byte";
        }
    },
    USHORT
    {
        @Override
        public DataType getJavaType()
        {
            return SHORT;
        }

        @Override
        public double getMinValue()
        {
            return 0d;
        }

        @Override
        public double getMaxValue()
        {
            return MathUtil.POW2_16_DOUBLE - 1d;
        }

        @Override
        public boolean isJavaType()
        {
            return false;
        }

        @Override
        public boolean isSigned()
        {
            return false;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Short.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Short.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_USHORT;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.UINT16;
        }

        @Override
        public String toLongString()
        {
            return "unsigned short (16 bits)";
        }

        @Override
        public String toString()
        {
            return "unsigned short";
        }
    },
    SHORT
    {
        @Override
        public DataType getJavaType()
        {
            return SHORT;
        }

        @Override
        public double getMinValue()
        {
            return Short.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            return Short.MAX_VALUE;
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Short.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Short.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_SHORT;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.INT16;
        }

        @Override
        public String toLongString()
        {
            return "signed short (16 bits)";
        }

        @Override
        public String toString()
        {
            return "short";
        }
    },
    UINT
    {
        @Override
        public DataType getJavaType()
        {
            return INT;
        }

        @Override
        public double getMinValue()
        {
            return 0d;
        }

        @Override
        public double getMaxValue()
        {
            return MathUtil.POW2_32_DOUBLE - 1d;
        }

        @Override
        public boolean isJavaType()
        {
            return false;
        }

        @Override
        public boolean isSigned()
        {
            return false;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Integer.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Integer.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_INT;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.UINT32;
        }

        @Override
        public String toLongString()
        {
            return "unsigned int (32 bits)";
        }

        @Override
        public String toString()
        {
            return "unsigned int";
        }
    },
    INT
    {
        @Override
        public DataType getJavaType()
        {
            return INT;
        }

        @Override
        public double getMinValue()
        {
            return Integer.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Integer.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Integer.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_INT;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.INT32;
        }

        @Override
        public String toLongString()
        {
            return "signed int (32 bits)";
        }

        @Override
        public String toString()
        {
            return "int";
        }
    },
    ULONG
    {
        @Override
        public DataType getJavaType()
        {
            return LONG;
        }

        @Override
        public double getMinValue()
        {
            return 0d;
        }

        @Override
        public double getMaxValue()
        {
            // WARNING : double data type loss information here compared to long
            return MathUtil.POW2_64_DOUBLE - 1d;
        }

        @Override
        public boolean isJavaType()
        {
            return false;
        }

        @Override
        public boolean isSigned()
        {
            return false;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Long.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Long.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            // not supported
            return DataBuffer.TYPE_UNDEFINED;
        }

        @Override
        public PixelType toPixelType()
        {
            // not supported
            return null;
        }

        @Override
        public String toLongString()
        {
            return "unsigned long (64 bits)";
        }

        @Override
        public String toString()
        {
            return "unsigned long";
        }
    },
    LONG
    {
        @Override
        public DataType getJavaType()
        {
            return LONG;
        }

        @Override
        public double getMinValue()
        {
            // WARNING : double data type loss information here compared to long
            return Long.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            // WARNING : double data type loss information here compared to long
            return Long.MAX_VALUE;
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return Long.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Long.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            // not supported
            return DataBuffer.TYPE_UNDEFINED;
        }

        @Override
        public PixelType toPixelType()
        {
            // not supported
            return null;
        }

        @Override
        public String toLongString()
        {
            return "signed long (64 bits)";
        }

        @Override
        public String toString()
        {
            return "long";
        }
    },
    FLOAT
    {
        @Override
        public DataType getJavaType()
        {
            return FLOAT;
        }

        @Override
        public double getMinValue()
        {
            return Float.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            return Float.MAX_VALUE;
        }

        @Override
        public double[] getDefaultBounds()
        {
            // default to [0..1] for float data type
            return new double[] {0d, 1d};
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return true;
        }

        @Override
        public int getBitSize()
        {
            return Float.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Float.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_FLOAT;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.FLOAT;
        }

        @Override
        public String toLongString()
        {
            return "float (32 bits)";
        }

        @Override
        public String toString()
        {
            return "float";
        }
    },
    DOUBLE
    {
        @Override
        public DataType getJavaType()
        {
            return DOUBLE;
        }

        @Override
        public double getMinValue()
        {
            return Double.MIN_VALUE;
        }

        @Override
        public double getMaxValue()
        {
            return Double.MAX_VALUE;
        }

        @Override
        public double[] getDefaultBounds()
        {
            // default to [0..1] for float data type
            return new double[] {0d, 1d};
        }

        @Override
        public boolean isJavaType()
        {
            return true;
        }

        @Override
        public boolean isSigned()
        {
            return true;
        }

        @Override
        public boolean isFloat()
        {
            return true;
        }

        @Override
        public int getBitSize()
        {
            return Double.SIZE;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            return Double.TYPE;
        }

        @Override
        public int toDataBufferType()
        {
            return DataBuffer.TYPE_DOUBLE;
        }

        @Override
        public PixelType toPixelType()
        {
            return PixelType.DOUBLE;
        }

        @Override
        public String toLongString()
        {
            return "double (8 bits)";
        }

        @Override
        public String toString()
        {
            return "double";
        }
    },
    UNDEFINED
    {
        @Override
        public DataType getJavaType()
        {
            return UNDEFINED;
        }

        @Override
        public double getMinValue()
        {
            return 0d;
        }

        @Override
        public double getMaxValue()
        {
            return 0d;
        }

        @Override
        public boolean isJavaType()
        {
            return false;
        }

        @Override
        public boolean isSigned()
        {
            return false;
        }

        @Override
        public boolean isFloat()
        {
            return false;
        }

        @Override
        public int getBitSize()
        {
            return 0;
        }

        @Override
        public Class<?> toPrimitiveClass()
        {
            // not supported
            return null;
        }

        @Override
        public int toDataBufferType()
        {
            // not supported
            return DataBuffer.TYPE_UNDEFINED;
        }

        @Override
        public PixelType toPixelType()
        {
            // not supported
            return null;
        }

        @Override
        public String toLongString()
        {
            return "undefined";
        }

        @Override
        public String toString()
        {
            return "undefined";
        }
    };

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
    public abstract DataType getJavaType();

    /**
     * Return the minimum value for current DataType
     */
    public abstract double getMinValue();

    /**
     * Return the maximum value for current DataType
     */
    public abstract double getMaxValue();

    /**
     * Get the default bounds for current DataType.<br>
     * This actually returns <code>[0,1]</code> for Float or Double DataType.
     */
    public double[] getDefaultBounds()
    {
        return new double[] {getMinValue(), getMaxValue()};
    }

    /**
     * Get the bounds <code>[min,max]</code> for current DataType.
     */
    public double[] getBounds()
    {
        return new double[] {getMinValue(), getMaxValue()};
    }

    /**
     * Return true if this is a compatible java data type (signed integer type only)
     */
    public abstract boolean isJavaType();

    /**
     * Return true if this is a signed data type
     */
    public abstract boolean isSigned();

    /**
     * Return true if this is a float data type
     */
    public abstract boolean isFloat();

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
        return getBitSize() / 8;
    }

    /**
     * Return the size (in bit) of the specified dataType
     */
    public abstract int getBitSize();

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
    public abstract Class<?> toPrimitiveClass();

    /**
     * Return the DataBuffer type corresponding to current DataType
     */
    public abstract int toDataBufferType();

    /**
     * Return the PixelType corresponding to current DataType
     */
    public abstract PixelType toPixelType();

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
    public abstract String toLongString();

    @Override
    public abstract String toString();
}
