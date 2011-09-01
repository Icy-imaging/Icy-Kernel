/**
 * 
 */
package icy.itk;

import icy.type.TypeUtil;

import org.itk.simple.PixelIDValueEnum;

/**
 * @author Stephane
 */
public class ItkUtil
{
    public static int getDataTypeFromPixelID(int value)
    {
        return getDataTypeFromPixelID(PixelIDValueEnum.swigToEnum(value));
    }

    public static int getDataTypeFromPixelID(PixelIDValueEnum value)
    {
        if ((value == PixelIDValueEnum.sitkInt8) || (value == PixelIDValueEnum.sitkUInt8))
            return TypeUtil.TYPE_BYTE;
        if ((value == PixelIDValueEnum.sitkInt16) || (value == PixelIDValueEnum.sitkUInt16))
            return TypeUtil.TYPE_SHORT;
        if ((value == PixelIDValueEnum.sitkInt32) || (value == PixelIDValueEnum.sitkUInt32))
            return TypeUtil.TYPE_INT;
        if (value == PixelIDValueEnum.sitkFloat32)
            return TypeUtil.TYPE_FLOAT;
        if (value == PixelIDValueEnum.sitkFloat64)
            return TypeUtil.TYPE_DOUBLE;

        return TypeUtil.TYPE_UNDEFINED;
    }

    public static boolean isSignedPixelID(int value)
    {
        return isSignedPixelID(PixelIDValueEnum.swigToEnum(value));

    }

    public static boolean isSignedPixelID(PixelIDValueEnum value)
    {
        return (value == PixelIDValueEnum.sitkInt8) || (value == PixelIDValueEnum.sitkInt16)
                || (value == PixelIDValueEnum.sitkInt32) || (value == PixelIDValueEnum.sitkFloat32)
                || (value == PixelIDValueEnum.sitkFloat64);
    }

    public static PixelIDValueEnum getPixelIDFromDataType(int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                if (signed)
                    return PixelIDValueEnum.sitkInt8;
                return PixelIDValueEnum.sitkUInt8;

            case TypeUtil.TYPE_SHORT:
                if (signed)
                    return PixelIDValueEnum.sitkInt16;
                return PixelIDValueEnum.sitkUInt16;

            case TypeUtil.TYPE_INT:
                if (signed)
                    return PixelIDValueEnum.sitkInt32;
                return PixelIDValueEnum.sitkUInt32;

            case TypeUtil.TYPE_FLOAT:
                return PixelIDValueEnum.sitkFloat32;

            case TypeUtil.TYPE_DOUBLE:
                return PixelIDValueEnum.sitkFloat64;
        }

        return PixelIDValueEnum.sitkUnknown;
    }

    public static int getPixelIDAsIntFromDataType(int dataType, boolean signed)
    {
        switch (dataType)
        {
            case TypeUtil.TYPE_BYTE:
                if (signed)
                    return PixelIDValueEnum.sitkInt8.swigValue();
                return PixelIDValueEnum.sitkUInt8.swigValue();

            case TypeUtil.TYPE_SHORT:
                if (signed)
                    return PixelIDValueEnum.sitkInt16.swigValue();
                return PixelIDValueEnum.sitkUInt16.swigValue();

            case TypeUtil.TYPE_INT:
                if (signed)
                    return PixelIDValueEnum.sitkInt32.swigValue();
                return PixelIDValueEnum.sitkUInt32.swigValue();

            case TypeUtil.TYPE_FLOAT:
                return PixelIDValueEnum.sitkFloat32.swigValue();

            case TypeUtil.TYPE_DOUBLE:
                return PixelIDValueEnum.sitkFloat64.swigValue();
        }

        return PixelIDValueEnum.sitkUnknown.swigValue();
    }

}
