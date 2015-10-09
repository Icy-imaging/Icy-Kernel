/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.itk;


/**
 * ITK library support.<br>
 * Will be externalized in future as a plugin. 
 * 
 * @author Stephane
 */
public class ItkUtil
{
//    public static DataType getDataTypeFromPixelID(int value)
//    {
//        return getDataTypeFromPixelID(PixelIDValueEnum.swigToEnum(value));
//    }
//
//    public static DataType getDataTypeFromPixelID(PixelIDValueEnum value)
//    {
//        if (value == PixelIDValueEnum.sitkInt8)
//            return DataType.BYTE;
//        if (value == PixelIDValueEnum.sitkUInt8)
//            return DataType.UBYTE;
//        if (value == PixelIDValueEnum.sitkInt16)
//            return DataType.SHORT;
//        if (value == PixelIDValueEnum.sitkUInt16)
//            return DataType.USHORT;
//        if (value == PixelIDValueEnum.sitkInt32)
//            return DataType.INT;
//        if (value == PixelIDValueEnum.sitkUInt32)
//            return DataType.UINT;
//        if (value == PixelIDValueEnum.sitkFloat32)
//            return DataType.FLOAT;
//        if (value == PixelIDValueEnum.sitkFloat64)
//            return DataType.DOUBLE;
//
//        return DataType.UNDEFINED;
//    }
//
//    public static boolean isSignedPixelID(int value)
//    {
//        return isSignedPixelID(PixelIDValueEnum.swigToEnum(value));
//    }
//
//    public static boolean isSignedPixelID(PixelIDValueEnum value)
//    {
//        return (value == PixelIDValueEnum.sitkInt8) || (value == PixelIDValueEnum.sitkInt16)
//                || (value == PixelIDValueEnum.sitkInt32) || (value == PixelIDValueEnum.sitkFloat32)
//                || (value == PixelIDValueEnum.sitkFloat64);
//    }
//
//    public static PixelIDValueEnum getPixelIDFromDataType(DataType dataType)
//    {
//        switch (dataType)
//        {
//            case BYTE:
//                return PixelIDValueEnum.sitkInt8;
//            case UBYTE:
//                return PixelIDValueEnum.sitkUInt8;
//            case SHORT:
//                return PixelIDValueEnum.sitkInt16;
//            case USHORT:
//                return PixelIDValueEnum.sitkUInt16;
//            case INT:
//                return PixelIDValueEnum.sitkInt32;
//            case UINT:
//                return PixelIDValueEnum.sitkUInt32;
//            case FLOAT:
//                return PixelIDValueEnum.sitkFloat32;
//            case DOUBLE:
//                return PixelIDValueEnum.sitkFloat64;
//        }
//
//        return PixelIDValueEnum.sitkUnknown;
//    }
//
//    public static int getPixelIDAsIntFromDataType(DataType dataType)
//    {
//        return getPixelIDFromDataType(dataType).swigValue();
//    }
//
//    /**
//     * Convert the specified ICY sequence to ITK image.<br>
//     * ITK images are only 3D [XYZ] so multiple channels images
//     * or multiple frames are not supported.
//     */
//    public static Image getItkIImage(Sequence sequence)
//    {
//        final int sizeX = sequence.getSizeX();
//        final int sizeY = sequence.getSizeY();
//        final int sizeZ = sequence.getSizeZ();
//        final DataType dataType = sequence.getDataType_();
//
//        final Image result = new Image(sizeX, sizeY, sizeZ, getPixelIDFromDataType(dataType));
//        final VectorUInt32 idx = new VectorUInt32(3);
//
//        switch (dataType)
//        {
//            case BYTE:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final byte[] data = sequence.getDataXYAsByte(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsInt8(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case UBYTE:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final byte[] data = sequence.getDataXYAsByte(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsUInt8(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case SHORT:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final short[] data = sequence.getDataXYAsShort(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsInt16(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case USHORT:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final short[] data = sequence.getDataXYAsShort(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsUInt16(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case INT:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final int[] data = sequence.getDataXYAsInt(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsInt32(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case UINT:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final int[] data = sequence.getDataXYAsInt(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsUInt32(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case FLOAT:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final float[] data = sequence.getDataXYAsFloat(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsFloat(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//
//            case DOUBLE:
//                for (int z = 0; z < sizeZ; z++)
//                {
//                    final double[] data = sequence.getDataXYAsDouble(0, z, 0);
//                    int offset = 0;
//                    // set index
//                    idx.set(2, z);
//
//                    for (int y = 0; y < sizeY; y++)
//                    {
//                        // set index
//                        idx.set(1, y);
//
//                        for (int x = 0; x < sizeX; x++, offset++)
//                        {
//                            // set index
//                            idx.set(0, x);
//                            // set pixel
//                            result.setPixelAsDouble(idx, data[offset]);
//                        }
//                    }
//                }
//                break;
//        }
//
//        return result;
//    }
//
//    /**
//     * Convert the specified ITK image to ICY sequence.<br>
//     * ITK images are only 3D [XYZ] so multiple channels images
//     * or multiple frames are not supported.
//     */
//    public static Sequence getSequence(Image itkImg)
//    {
//        final int sizeX = (int) itkImg.getWidth();
//        final int sizeY = (int) itkImg.getHeight();
//        final int sizeZ = (int) itkImg.getDepth();
//        final DataType dataType = getDataTypeFromPixelID(itkImg.getPixelIDValue());
//
//        final Sequence result = new Sequence();
//        final VectorUInt32 idx = new VectorUInt32(3);
//
//        result.beginUpdate();
//        try
//        {
//            switch (dataType)
//            {
//                case BYTE:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final byte[] data = img.getDataXYAsByte(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = itkImg.getPixelAsInt8(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case UBYTE:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final byte[] data = img.getDataXYAsByte(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = (byte) itkImg.getPixelAsUInt8(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case SHORT:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final short[] data = img.getDataXYAsShort(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = itkImg.getPixelAsInt16(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case USHORT:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final short[] data = img.getDataXYAsShort(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = (short) itkImg.getPixelAsUInt16(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case INT:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final int[] data = img.getDataXYAsInt(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = itkImg.getPixelAsInt32(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case UINT:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final int[] data = img.getDataXYAsInt(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = (int) itkImg.getPixelAsUInt32(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case FLOAT:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final float[] data = img.getDataXYAsFloat(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = itkImg.getPixelAsFloat(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//
//                case DOUBLE:
//                    for (int z = 0; z < sizeZ; z++)
//                    {
//                        final IcyBufferedImage img = new IcyBufferedImage(sizeX, sizeY, 1, dataType);
//                        final double[] data = img.getDataXYAsDouble(0);
//                        int offset = 0;
//                        // set index
//                        idx.set(2, z);
//
//                        for (int y = 0; y < sizeY; y++)
//                        {
//                            // set index
//                            idx.set(1, y);
//
//                            for (int x = 0; x < sizeX; x++, offset++)
//                            {
//                                // set index
//                                idx.set(0, x);
//                                // get pixel
//                                data[offset] = itkImg.getPixelAsDouble(idx);
//                            }
//                        }
//
//                        img.dataChanged();
//                        result.setImage(0, z, img);
//                    }
//                    break;
//            }
//        }
//        finally
//        {
//            result.endUpdate();
//        }
//
//        return result;
//    }
}
