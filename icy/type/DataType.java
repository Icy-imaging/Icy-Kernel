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
    // UBYTE (unsigned 8 bits integer)
    UBYTE(Byte.SIZE, true, false, 0d, MathUtil.POW2_8_DOUBLE - 1d, Byte.TYPE, DataBuffer.TYPE_BYTE, PixelType.UINT8,
            "unsigned byte (8 bits)", "unsigned byte"),
    // BYTE (signed 8 bits integer)
    BYTE(Byte.SIZE, true, true, Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.TYPE, DataBuffer.TYPE_BYTE, PixelType.INT8,
            "signed byte (8 bits)", "byte"),
    // USHORT (unsigned 16 bits integer)
    USHORT(Short.SIZE, true, false, 0d, MathUtil.POW2_16_DOUBLE - 1d, Short.TYPE, DataBuffer.TYPE_USHORT,
            PixelType.UINT16, "unsigned short (16 bits)", "unsigned short"),
    // SHORT (signed 16 bits integer)
    SHORT(Short.SIZE, true, true, Short.MIN_VALUE, Short.MAX_VALUE, Short.TYPE, DataBuffer.TYPE_SHORT, PixelType.INT16,
            "signed short (16 bits)", "short"),
    // UINT (unsigned 32bits integer)
    UINT(Integer.SIZE, true, false, 0d, MathUtil.POW2_32_DOUBLE - 1d, Integer.TYPE, DataBuffer.TYPE_INT,
            PixelType.UINT32, "unsigned int (32 bits)", "unsigned int"),
    // INT (signed 32 bits integer)
    INT(Integer.SIZE, true, true, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.TYPE, DataBuffer.TYPE_INT,
            PixelType.INT32, "signed int (32 bits)", "int"),
    // ULONG (unsigned 64 bits integer)
    // WARNING : double data type loss information here for min/max
    ULONG(Long.SIZE, true, false, 0d, MathUtil.POW2_64_DOUBLE - 1d, Long.TYPE, DataBuffer.TYPE_UNDEFINED, null,
            "unsigned long (64 bits)", "unsigned long"),
    // LONG (signed 64 bits integer)
    // WARNING : double data type loss information here for min/max
    LONG(Long.SIZE, true, true, Long.MIN_VALUE, Long.MAX_VALUE, Long.TYPE, DataBuffer.TYPE_UNDEFINED, null,
            "signed long (64 bits)", "long"),
    // FLOAT (signed 32 bits float)
    FLOAT(Float.SIZE, false, true, Float.MIN_VALUE, Float.MAX_VALUE, Float.TYPE, DataBuffer.TYPE_FLOAT,
            PixelType.FLOAT, "float (32 bits)", "float"),
    // DOUBLE (signed 64 bits float)
    DOUBLE(Double.SIZE, false, true, Double.MIN_VALUE, Double.MAX_VALUE, Double.TYPE, DataBuffer.TYPE_DOUBLE,
            PixelType.DOUBLE, "double (64 bits)", "double"),
    // UNDEFINED (undefined data type)
    UNDEFINED(0, true, false, 0d, 0d, null, DataBuffer.TYPE_UNDEFINED, null, "undefined", "undefined");

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
     * internals properties
     */
    protected String longString;
    protected String string;
    protected int bitSize;
    protected boolean integer;
    protected boolean signed;
    protected double min;
    protected double max;
    protected Class<?> primitiveClass;
    protected int dataBufferType;
    protected PixelType pixelType;

    private DataType(int bitSize, boolean integer, boolean signed, double min, double max, Class<?> primitiveClass,
            int dataBufferType, PixelType pixelType, String longString, String string)
    {
        this.bitSize = bitSize;
        this.integer = integer;
        this.signed = signed;
        this.min = min;
        this.max = max;
        this.primitiveClass = primitiveClass;
        this.dataBufferType = dataBufferType;
        this.pixelType = pixelType;
        this.longString = longString;
        this.string = string;
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
        return min;
    }

    /**
     * Return the maximum value for current DataType
     */
    public double getMaxValue()
    {
        return max;
    }

    /**
     * Get the default bounds for current DataType.<br>
     * This actually returns <code>[0,1]</code> for Float or Double DataType.
     */
    public double[] getDefaultBounds()
    {
        if (!integer)
            return new double[] {0d, 1d};

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
    public boolean isJavaType()
    {
        return this == getJavaType();
    }

    /**
     * Return true if this is a signed data type
     */
    public boolean isSigned()
    {
        return signed;
    }

    /**
     * Return true if this is a float data type
     */
    public boolean isFloat()
    {
        return !isInteger();
    }

    /**
     * Return true if this is an integer data type
     */
    public boolean isInteger()
    {
        return integer;
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
    public int getBitSize()
    {
        return bitSize;
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
        return primitiveClass;
    }

    /**
     * Return the DataBuffer type corresponding to current DataType
     */
    public int toDataBufferType()
    {
        return dataBufferType;
    }

    /**
     * Return the PixelType corresponding to current DataType
     */
    public PixelType toPixelType()
    {
        return pixelType;
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
        return longString;
    }

    @Override
    public String toString()
    {
        return string;
    }
}
