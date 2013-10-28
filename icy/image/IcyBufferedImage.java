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
package icy.image;

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.image.IcyBufferedImageEvent.IcyBufferedImageEventType;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.LinearColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.image.colormodel.IcyColorModelEvent;
import icy.image.colormodel.IcyColorModelListener;
import icy.image.lut.LUT;
import icy.math.ArrayMath;
import icy.math.MathUtil;
import icy.math.Scaler;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import icy.type.collection.array.ByteArrayConvert;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

import jxl.biff.drawing.PNGReader;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.SignedByteBuffer;
import loci.formats.gui.SignedShortBuffer;
import loci.formats.gui.UnsignedIntBuffer;
import loci.formats.in.APNGReader;
import loci.formats.in.JPEG2000Reader;
import loci.formats.in.TiffDelegateReader;
import loci.formats.in.TiffJAIReader;

/**
 * @author stephane
 */
public class IcyBufferedImage extends BufferedImage implements IcyColorModelListener, ChangeListener
{
    /**
     * @deprecated Use {@link IcyBufferedImageUtil.FilterType} instead.
     */
    @Deprecated
    public static enum FilterType
    {
        NEAREST, BILINEAR, BICUBIC
    };

    private static IcyBufferedImageUtil.FilterType getNewFilterType(FilterType ft)
    {
        switch (ft)
        {
            default:
            case NEAREST:
                return IcyBufferedImageUtil.FilterType.NEAREST;
            case BILINEAR:
                return IcyBufferedImageUtil.FilterType.BILINEAR;
            case BICUBIC:
                return IcyBufferedImageUtil.FilterType.BICUBIC;
        }
    }

    /**
     * Convert a list of BufferedImage to an IcyBufferedImage (multi component).<br>
     * IMPORTANT : source images can be used as part or as the whole result<br>
     * so consider them as "lost"
     * 
     * @param imageList
     *        list of {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @deprecated
     *             use {@link #createFrom} instead
     */
    @Deprecated
    public static IcyBufferedImage convert(List<BufferedImage> imageList)
    {
        return createFrom(imageList);
    }

    /**
     * Create an IcyBufferedImage (multi component) from a list of BufferedImage.<br>
     * IMPORTANT : source images can be used as part or as the whole result<br>
     * so consider them as "lost"
     * 
     * @param imageList
     *        list of {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(List<? extends BufferedImage> imageList)
    {
        if (imageList.size() == 0)
            throw new IllegalArgumentException("imageList should contains at least 1 image");

        final List<IcyBufferedImage> icyImageList = new ArrayList<IcyBufferedImage>();

        // transform images to icy images
        for (BufferedImage image : imageList)
            icyImageList.add(IcyBufferedImage.createFrom(image));

        final IcyBufferedImage firstImage = icyImageList.get(0);

        if (icyImageList.size() == 1)
            return firstImage;

        final DataType dataType = firstImage.getDataType_();
        final int width = firstImage.getWidth();
        final int height = firstImage.getHeight();

        // calculate channel number
        int numChannel = 0;
        for (IcyBufferedImage image : icyImageList)
            numChannel += image.getSizeC();

        final Object[] data = Array2DUtil.createArray(dataType, numChannel);

        // get data from all images
        int destC = 0;
        for (IcyBufferedImage image : icyImageList)
        {
            if (dataType != image.getDataType_())
                throw new IllegalArgumentException("All images contained in imageList should have the same dataType");
            if ((width != image.getWidth()) || (height != image.getHeight()))
                throw new IllegalArgumentException("All images contained in imageList should have the same dimension");

            for (int c = 0; c < image.getSizeC(); c++)
                data[destC++] = image.getDataXY(c);
        }

        // create and return the image
        return new IcyBufferedImage(width, height, data, dataType.isSigned());
    }

    /**
     * Convert a BufferedImage to an IcyBufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as "lost"
     * 
     * @param image
     *        {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @deprecated
     *             use {@link #createFrom} instead
     */
    @Deprecated
    public static IcyBufferedImage convert(BufferedImage image)
    {
        return createFrom(image);
    }

    /**
     * Create an IcyBufferedImage from a BufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     * 
     * @param image
     *        {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(BufferedImage image)
    {
        // IcyBufferedImage --> no conversion needed
        if (image instanceof IcyBufferedImage)
            return (IcyBufferedImage) image;

        // sort of IcyBufferedImage (JAI can return that type) --> no conversion needed
        if (image.getColorModel() instanceof IcyColorModel)
            return new IcyBufferedImage((IcyColorModel) image.getColorModel(), image.getRaster());

        final int w = image.getWidth();
        final int h = image.getHeight();
        final int type = image.getType();
        final BufferedImage temp;

        // we first want a component based image
        switch (type)
        {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                temp.getGraphics().drawImage(image, 0, 0, null);
                break;

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                temp.getGraphics().drawImage(image, 0, 0, null);
                break;

            default:
                // if we have severals components with an unknown / incompatible sampleModel
                if ((image.getColorModel().getNumComponents() > 1)
                        && (!(image.getSampleModel() instanceof ComponentSampleModel)))
                {
                    // change it to a basic ABGR components image
                    temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                    temp.getGraphics().drawImage(image, 0, 0, null);
                }
                else
                    temp = image;
                break;
        }

        // convert initial data type in our data type
        final DataType dataType = DataType.getDataTypeFromDataBufferType(temp.getColorModel().getTransferType());
        // get number of components
        final int numComponents = temp.getRaster().getNumBands();

        // create a compatible image in our format
        final IcyBufferedImage result = new IcyBufferedImage(w, h, numComponents, dataType);

        // copy data from the source image
        result.copyData(temp);

        // in some case we want to restore colormaps from source image
        if ((type == BufferedImage.TYPE_BYTE_BINARY) || (type == BufferedImage.TYPE_BYTE_INDEXED)
                || (numComponents == 2))
            result.setColorMaps(image);

        return result;
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage (old method).
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom_old(IFormatReader reader, int z, int t) throws FormatException,
            IOException
    {
        final int sizeX = reader.getSizeX();
        final int sizeY = reader.getSizeY();
        final List<BufferedImage> imageList = new ArrayList<BufferedImage>();
        final int sizeC = reader.getEffectiveSizeC();

        for (int c = 0; c < sizeC; c++)
            imageList.add(AWTImageTools.openImage(reader.openBytes(reader.getIndex(z, c, t)), reader, sizeX, sizeY));

        // combine channels
        return createFrom(imageList);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.<br>
     * Compatible version (use plain image loading and resize).
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createCompatibleThumbnailFrom(IFormatReader reader, int z, int t)
            throws FormatException, IOException
    {
        return IcyBufferedImageUtil.scale(createFrom(reader, z, t), reader.getThumbSizeX(), reader.getThumbSizeY());
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        {@link IFormatReader}
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createThumbnailFrom(IFormatReader reader, int z, int t) throws FormatException,
            IOException
    {
        final int sizeX = reader.getThumbSizeX();
        final int sizeY = reader.getThumbSizeY();
        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int sizeXY = sizeX * sizeY;
        final int effSizeC = reader.getEffectiveSizeC();
        final int rgbChanCount = reader.getRGBChannelCount();
        final int sizeC = effSizeC * rgbChanCount;
        final boolean indexed = reader.isIndexed();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // System.out.println("Opening image " + dataType);
        // System.out.println("Size X*Y*C : " + sizeX + "*" + sizeY + "*" + sizeC);
        // System.out.println("Effective C : " + effSizeC + "     RGB Channel : " + rgbChanCount);
        // System.out.println("Indexed : " + Boolean.toString(indexed) + "    Interleaved : "
        // + Boolean.toString(interleaved) + "     Little endian : " + Boolean.toString(little));

        try
        {

            // prepare internal image data array
            final Object[] data = Array2DUtil.createArray(dataType, sizeC);
            final IcyColorMap[] colormaps = new IcyColorMap[effSizeC];

            // allocate array
            for (int i = 0; i < sizeC; i++)
                data[i] = Array1DUtil.createArray(dataType, sizeXY);

            for (int effC = 0; effC < effSizeC; effC++)
            {
                // load thumbnail byte data
                final byte[] byteData = reader.openThumbBytes(reader.getIndex(z, effC, t));
                // current final component
                final int c = effC * rgbChanCount;
                final int componentByteLen = byteData.length / rgbChanCount;

                // build data array
                int inOffset = 0;
                if (interleaved)
                {
                    for (int sc = 0; sc < rgbChanCount; sc++)
                    {
                        ByteArrayConvert.byteArrayTo(byteData, inOffset, rgbChanCount, data[c + sc], 0, 1,
                                componentByteLen, little);
                        inOffset++;
                    }
                }
                else
                {
                    for (int sc = 0; sc < rgbChanCount; sc++)
                    {
                        ByteArrayConvert.byteArrayTo(byteData, inOffset, 1, data[c + sc], 0, 1, componentByteLen,
                                little);
                        inOffset += componentByteLen;
                    }
                }

                // indexed color ?
                if (indexed)
                {
                    // TODO : GIF transparent color support
                    //
                    // <<That would let you find the transparency color using the "Transparency
                    // index" metadata value as an index into the array returned by
                    // get8BitLookupTable().>>

                    // only 8 bits and 16 bits lookup table supported
                    switch (dataType.getJavaType())
                    {
                        case BYTE:
                            colormaps[effC] = new IcyColorMap("component " + effC, reader.get8BitLookupTable());
                            break;

                        case SHORT:
                            colormaps[effC] = new IcyColorMap("component " + effC, reader.get16BitLookupTable());
                            break;

                        default:
                            colormaps[effC] = null;
                            break;
                    }
                }
            }

            final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, data, dataType.isSigned());

            result.beginUpdate();
            try
            {
                if (indexed)
                {
                    // error ! we should have same number of colormap than component
                    if (colormaps.length != sizeC)
                    {
                        System.err.println("Warning : " + colormaps.length + " colormap for " + sizeC + " components");
                        System.err.println("Colormap can not be restored");
                    }
                    else
                    {
                        // set colormaps
                        for (int comp = 0; comp < sizeC; comp++)
                        {
                            // sometime loci return black colormap map and we want to avoid them...
                            if ((colormaps[comp] != null) && !colormaps[comp].isBlack())
                                result.setColorMap(comp, colormaps[comp], true);
                        }
                    }
                }
                // special case of 4 channels image, try to set 4th channel colormap
                else if (sizeC == 4)
                {
                    // assume real alpha channel depending from the reader we use
                    final boolean alpha = (reader instanceof PNGReader) || (reader instanceof APNGReader)
                            || (reader instanceof TiffDelegateReader) || (reader instanceof TiffJAIReader)
                            || (reader instanceof JPEG2000Reader);

                    // replace alpha with Cyan color
                    if (!alpha)
                        result.setColorMap(3, LinearColorMap.cyan_, true);
                }
            }
            finally
            {
                result.endUpdate();
            }

            return result;
        }
        catch (Exception E)
        {
            // LOCI do not support thumbnail for all image, try compatible version
            return createCompatibleThumbnailFrom(reader, z, t);
        }
    }

    /**
     * Load a single channel sub image at (Z, T) position from the specified {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        Reader used to load the image
     * @param c
     *        Channel index to load
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(IFormatReader reader, int x, int y, int w, int h, int c, int z, int t)
            throws FormatException, IOException
    {
        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int rgbChanCount = reader.getRGBChannelCount();
        final boolean indexed = reader.isIndexed();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // allocate internal image data array
        final Object data = Array1DUtil.createArray(dataType, w * h);

        final int baseC = c / rgbChanCount;
        final int subC = c % rgbChanCount;

        // get image data
        final byte[] byteData = reader.openBytes(reader.getIndex(z, baseC, t), x, y, w, h);
        // current final component
        final int componentByteLen = byteData.length / rgbChanCount;

        // build data array
        if (interleaved)
            ByteArrayConvert.byteArrayTo(byteData, subC, rgbChanCount, data, 0, 1, componentByteLen, little);
        else
            ByteArrayConvert.byteArrayTo(byteData, subC * componentByteLen, 1, data, 0, 1, componentByteLen, little);

        final IcyBufferedImage result = new IcyBufferedImage(w, h, data, dataType.isSigned());

        // indexed color ?
        if (indexed)
        {
            IcyColorMap map;

            // only 8 bits and 16 bits lookup table supported
            switch (dataType.getJavaType())
            {
                case BYTE:
                    map = new IcyColorMap("component " + c, reader.get8BitLookupTable());
                    break;

                case SHORT:
                    map = new IcyColorMap("component " + c, reader.get16BitLookupTable());
                    break;

                default:
                    map = null;
            }

            // sometime loci return black colormap map and we want to avoid them...
            if ((map != null) && !map.isBlack())
                result.setColorMap(0, map, true);
        }

        return result;
    }

    /**
     * Load the image located at (Z, T) position from the specified {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     * 
     * @param reader
     *        Reader used to load the image
     * @param z
     *        Z position of the image to load
     * @param t
     *        T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        final int sizeX = reader.getSizeX();
        final int sizeY = reader.getSizeY();
        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int sizeXY = sizeX * sizeY;
        final int effSizeC = reader.getEffectiveSizeC();
        final int rgbChanCount = reader.getRGBChannelCount();
        final int sizeC = effSizeC * rgbChanCount;
        final boolean indexed = reader.isIndexed();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // System.out.println("Opening image " + dataType);
        // System.out.println("Size X*Y*C : " + sizeX + "*" + sizeY + "*" + sizeC);
        // System.out.println("Effective C : " + effSizeC + "     RGB Channel : " + rgbChanCount);
        // System.out.println("Indexed : " + Boolean.toString(indexed) + "    Interleaved : "
        // + Boolean.toString(interleaved) + "     Little endian : " + Boolean.toString(little));

        // prepare internal image data array
        final Object[] data = Array2DUtil.createArray(dataType, sizeC);
        final IcyColorMap[] colormaps = new IcyColorMap[effSizeC];

        // allocate array
        for (int i = 0; i < sizeC; i++)
            data[i] = Array1DUtil.createArray(dataType, sizeXY);

        for (int effC = 0; effC < effSizeC; effC++)
        {
            final byte[] byteData = reader.openBytes(reader.getIndex(z, effC, t));

            // current final component
            final int c = effC * rgbChanCount;
            final int componentByteLen = byteData.length / rgbChanCount;

            // build data array
            int inOffset = 0;
            if (interleaved)
            {
                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    ByteArrayConvert.byteArrayTo(byteData, inOffset, rgbChanCount, data[c + sc], 0, 1,
                            componentByteLen, little);
                    inOffset++;
                }
            }
            else
            {
                for (int sc = 0; sc < rgbChanCount; sc++)
                {
                    ByteArrayConvert.byteArrayTo(byteData, inOffset, 1, data[c + sc], 0, 1, componentByteLen, little);
                    inOffset += componentByteLen;
                }
            }

            // indexed color ?
            if (indexed)
            {
                // only 8 bits and 16 bits lookup table supported
                switch (dataType.getJavaType())
                {
                    case BYTE:
                        colormaps[effC] = new IcyColorMap("component " + effC, reader.get8BitLookupTable());
                        break;

                    case SHORT:
                        colormaps[effC] = new IcyColorMap("component " + effC, reader.get16BitLookupTable());
                        break;

                    default:
                        colormaps[effC] = null;
                        break;
                }
            }
        }

        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, data, dataType.isSigned());

        result.beginUpdate();
        try
        {
            if (indexed)
            {
                // error ! we should have same number of colormap than component
                if (colormaps.length != sizeC)
                {
                    System.err.println("Warning : " + colormaps.length + " colormap for " + sizeC + " components");
                    System.err.println("Colormap can not be restored");
                }
                else
                {
                    // set colormaps
                    for (int comp = 0; comp < sizeC; comp++)
                    {
                        // sometime loci return black colormap map and we want to avoid them...
                        if ((colormaps[comp] != null) && !colormaps[comp].isBlack())
                            result.setColorMap(comp, colormaps[comp], true);
                    }
                }
            }
            // special case of 4 channels image, try to set 4th channel colormap
            else if (sizeC == 4)
            {
                // assume real alpha channel depending from the reader we use
                final boolean alpha = (reader instanceof PNGReader) || (reader instanceof APNGReader)
                        || (reader instanceof TiffDelegateReader) || (reader instanceof TiffJAIReader)
                        || (reader instanceof JPEG2000Reader);

                // replace alpha with Cyan color
                if (!alpha)
                    result.setColorMap(3, LinearColorMap.cyan_, true);
            }
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * @deprecated Use {@link #IcyBufferedImage(int, int, IcyColorModel)} instead.
     */
    @Deprecated
    public static IcyBufferedImage createEmptyImage(int width, int height, IcyColorModel cm)
    {
        return new IcyBufferedImage(width, height, cm);
    }

    @SuppressWarnings("unused")
    private static final int TYPE_CUSTOM = 0;
    @SuppressWarnings("unused")
    private static final int TYPE_INT_RGB = 1;
    @SuppressWarnings("unused")
    private static final int TYPE_INT_ARGB = 2;
    @SuppressWarnings("unused")
    private static final int TYPE_INT_ARGB_PRE = 3;
    @SuppressWarnings("unused")
    private static final int TYPE_INT_BGR = 4;
    @SuppressWarnings("unused")
    private static final int TYPE_3BYTE_BGR = 5;
    @SuppressWarnings("unused")
    private static final int TYPE_4BYTE_ABGR = 6;
    @SuppressWarnings("unused")
    private static final int TYPE_4BYTE_ABGR_PRE = 7;
    @SuppressWarnings("unused")
    private static final int TYPE_USHORT_565_RGB = 8;
    @SuppressWarnings("unused")
    private static final int TYPE_USHORT_555_RGB = 9;
    @SuppressWarnings("unused")
    private static final int TYPE_BYTE_GRAY = 10;
    @SuppressWarnings("unused")
    private static final int TYPE_USHORT_GRAY = 11;
    @SuppressWarnings("unused")
    private static final int TYPE_BYTE_BINARY = 12;
    @SuppressWarnings("unused")
    private static final int TYPE_BYTE_INDEXED = 13;

    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_BYTE = TypeUtil.TYPE_BYTE;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_DOUBLE = TypeUtil.TYPE_DOUBLE;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_FLOAT = TypeUtil.TYPE_FLOAT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_INT = TypeUtil.TYPE_INT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_SHORT = TypeUtil.TYPE_SHORT;
    /**
     * @deprecated
     */
    @Deprecated
    public static int TYPE_UNDEFINED = TypeUtil.TYPE_UNDEFINED;

    /**
     * automatic update of channel bounds
     */
    private boolean autoUpdateChannelBounds;
    /**
     * internal image LUT
     */
    private final LUT internalLut;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;
    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * Build an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     * 
     * @param cm
     *        {@link IcyColorModel}
     * @param wr
     *        {@link WritableRaster}
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     */
    private IcyBufferedImage(IcyColorModel cm, WritableRaster wr, boolean autoUpdateChannelBounds)
    {
        super(cm, wr, false, null);

        // internal lut
        internalLut = new LUT(cm);

        updater = new UpdateEventHandler(this, false);
        listeners = new EventListenerList();

        // automatic update of channel bounds
        this.autoUpdateChannelBounds = autoUpdateChannelBounds;

        // add listener to colorModel
        cm.addListener(this);
    }

    /**
     * Create an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     * 
     * @param cm
     *        {@link IcyColorModel}
     * @param wr
     *        {@link WritableRaster}
     */
    private IcyBufferedImage(IcyColorModel cm, WritableRaster wr)
    {
        this(cm, wr, true);
    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, width and height.<br>
     * Private version, {@link IcyColorModel} is directly used internally.
     */
    private IcyBufferedImage(IcyColorModel cm, int width, int height)
    {
        this(cm, cm.createCompatibleWritableRaster(width, height), true);
    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, data, width and height.
     */
    private IcyBufferedImage(IcyColorModel cm, Object[] data, int width, int height, boolean autoUpdateChannelBounds)
    {
        this(cm, cm.createWritableRaster(data, width, height), autoUpdateChannelBounds);

        // data has been modified
        dataChanged();
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as
     * it allow you to directly send the data array and disable the channel bounds calculation.
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     *        When set to false, you have to set bounds manually by calling
     *        {@link #updateChannelsBounds()} or #setC
     */
    public IcyBufferedImage(int width, int height, Object[] data, boolean signed, boolean autoUpdateChannelBounds)
    {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height,
                autoUpdateChannelBounds);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     */
    public IcyBufferedImage(int width, int height, Object[] data, boolean signed)
    {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height,
                true);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        Should be a 2D array with first dimension giving the number of component<br>
     *        and second dimension equals to <code>width * height</code><br>
     *        The array data type specify the internal data type.
     */
    public IcyBufferedImage(int width, int height, Object[] data)
    {
        this(width, height, data, false);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as
     * it allow you to directly send the data array and disable the channel bounds calculation.
     * 
     * @param width
     * @param height
     * @param data
     *        image data array.<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     * @param autoUpdateChannelBounds
     *        If true then channel bounds are automatically calculated.<br>
     *        When set to false, you have to set bounds manually by calling
     *        {@link #updateChannelsBounds()} or #setC
     * @see #IcyBufferedImage(int, int, Object[], boolean, boolean)
     */
    public IcyBufferedImage(int width, int height, Object data, boolean signed, boolean autoUpdateChannelBounds)
    {
        this(width, height, ArrayUtil.encapsulate(data), signed, autoUpdateChannelBounds);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     * @param signed
     *        use signed data for data type
     */
    public IcyBufferedImage(int width, int height, Object data, boolean signed)
    {
        this(width, height, ArrayUtil.encapsulate(data), signed);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     * 
     * @param width
     * @param height
     * @param data
     *        image data<br>
     *        The length of the array should be equals to <code>width * height</code>.<br>
     *        The array data type specify the internal data type.
     */
    public IcyBufferedImage(int width, int height, Object data)
    {
        this(width, height, ArrayUtil.encapsulate(data));
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType.
     * 
     * @param width
     * @param height
     * @param numComponents
     * @param dataType
     *        image data type {@link DataType}
     */
    public IcyBufferedImage(int width, int height, int numComponents, DataType dataType)
    {
        this(IcyColorModel.createInstance(numComponents, dataType), width, height);
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height and IcyColorModel type.<br>
     */
    public IcyBufferedImage(int width, int height, IcyColorModel cm)
    {
        this(width, height, cm.getNumComponents(), cm.getDataType_());
    }

    /**
     * @deprecated use {@link #IcyBufferedImage(int, int, int, DataType)} instead
     */
    @Deprecated
    public IcyBufferedImage(int width, int height, int numComponents, int dataType, boolean signed)
    {
        this(IcyColorModel.createInstance(numComponents, dataType, signed), width, height);
    }

    /**
     * @deprecated use {@link #IcyBufferedImage(int, int, int, DataType)} instead
     */
    @Deprecated
    public IcyBufferedImage(int width, int height, int numComponents, int dataType)
    {
        this(IcyColorModel.createInstance(numComponents, dataType, false), width, height);
    }

    /**
     * @return true is channel bounds are automatically updated when image data is modified.
     * @see #setAutoUpdateChannelBounds(boolean)
     */
    public boolean getAutoUpdateChannelBounds()
    {
        return autoUpdateChannelBounds;
    }

    /**
     * If set to <code>true</code> (default) then channel bounds will be automatically recalculated
     * when image data is modified.<br>
     * This can consume some time if you make many updates on a large image.<br>
     * In this case you should do your updates in a {@link #beginUpdate()} ... {@link #endUpdate()}
     * block to avoid severals recalculation.
     */
    public void setAutoUpdateChannelBounds(boolean value)
    {
        if (autoUpdateChannelBounds != value)
        {
            if (value)
                updateChannelsBounds();

            autoUpdateChannelBounds = value;
        }
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage, LUT)}
     *             instead.
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(BufferedImage out, LUT lut)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, out, lut);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, BufferedImage)}
     *             instead.
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(BufferedImage out)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, out);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage, LUT, BufferedImage)}
     *             instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(LUT lut, BufferedImage out)
    {
        return IcyBufferedImageUtil.getARGBImage(this, lut, out);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage, BufferedImage)}
     *             instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(BufferedImage out)
    {
        return IcyBufferedImageUtil.getARGBImage(this, out);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage, LUT)} instead.
     */
    @Deprecated
    public BufferedImage getARGBImage(LUT lut)
    {
        return IcyBufferedImageUtil.getARGBImage(this, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getARGBImage(IcyBufferedImage)} instead.
     */
    @Deprecated
    public BufferedImage getARGBImage()
    {
        return IcyBufferedImageUtil.getARGBImage(this);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage, DataType, Scaler)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage convertToType(DataType dataType, Scaler scaler)
    {
        return IcyBufferedImageUtil.convertToType(this, dataType, scaler);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage,DataType, Scaler)}
     *             instead
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, Scaler scaler)
    {
        return convertToType(DataType.getDataType(dataType, signed), scaler);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage convertToType(DataType dataType, boolean rescale)
    {
        return IcyBufferedImageUtil.convertToType(this, dataType, rescale);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#convertToType(IcyBufferedImage,DataType, boolean)}
     *             instead
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, boolean rescale)
    {
        return convertToType(DataType.getDataType(dataType, signed), rescale);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, int, LUT)}
     *             instead
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(LUT lut, int imageType)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, imageType, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#toBufferedImage(IcyBufferedImage, int, LUT)}
     *             instead
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(int imageType, LUT lut)
    {
        return IcyBufferedImageUtil.toBufferedImage(this, imageType, lut);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#getCopy(IcyBufferedImage)} instead
     */
    @Deprecated
    public IcyBufferedImage getCopy()
    {
        return IcyBufferedImageUtil.getCopy(this);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#getSubImage(IcyBufferedImage, int, int, int, int)}
     *             instead
     */
    @Deprecated
    public IcyBufferedImage getSubImageCopy(int x, int y, int w, int h)
    {
        return IcyBufferedImageUtil.getSubImage(this, x, y, w, h);
    }

    /**
     * Not supported on IcyBufferedImage, use getSubImageCopy instead.
     */
    @Deprecated
    @Override
    public IcyBufferedImage getSubimage(int x, int y, int w, int h)
    {
        // IcyBufferedImage doesn't support subImaging (incorrect draw and copy operation)
        throw new UnsupportedOperationException(
                "IcyBufferedImage doesn't support getSubimage method, use getSubImageCopy instead.");

        // return new IcyBufferedImage(getIcyColorModel(), getRaster().createWritableChild(x, y, w,
        // h, 0, 0, null));
    }

    /**
     * Return a single component image corresponding to the component c of current image.<br>
     * This actually create a new image which share its data with internal image
     * so any modifications to one affect the other.<br>
     * if <code>(c == -1)</code> then current image is directly returned<br>
     * if <code>((c == 0) || (sizeC == 1))</code> then current image is directly returned<br>
     * if <code>((c < 0) || (c >= sizeC))</code> then it returns <code>null</code>
     * 
     * @see IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)
     * @since version 1.0.3.3b
     */
    public IcyBufferedImage getImage(int c)
    {
        if (c == -1)
            return this;

        final int sizeC = getSizeC();

        if ((c < 0) || (c >= sizeC))
            return null;
        if (sizeC == 1)
            return this;

        return new IcyBufferedImage(getWidth(), getHeight(), getDataXY(c), isSignedDataType());
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)} instead.
     */
    @Deprecated
    public IcyBufferedImage extractChannel(int channelNumber)
    {
        return IcyBufferedImageUtil.extractChannel(this, channelNumber);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannels(IcyBufferedImage, List)} instead.
     */
    @Deprecated
    public IcyBufferedImage extractChannels(List<Integer> channelNumbers)
    {
        return IcyBufferedImageUtil.extractChannels(this, channelNumbers);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)} instead
     */
    @Deprecated
    public IcyBufferedImage extractBand(int bandNumber)
    {
        return IcyBufferedImageUtil.extractChannel(this, bandNumber);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#extractChannels(IcyBufferedImage, List)} instead
     */
    @Deprecated
    public IcyBufferedImage extractBands(List<Integer> bandNumbers)
    {
        return IcyBufferedImageUtil.extractChannels(this, bandNumbers);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, boolean, int, int, IcyBufferedImageUtil.FilterType)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign,
            FilterType filterType)
    {
        return IcyBufferedImageUtil.scale(this, width, height, resizeContent, xAlign, yAlign,
                getNewFilterType(filterType));
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, boolean, int, int)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign)
    {
        return IcyBufferedImageUtil.scale(this, width, height, resizeContent, xAlign, yAlign);
    }

    /**
     * @deprecated Uses
     *             {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int, IcyBufferedImageUtil.FilterType)}
     *             instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height, FilterType filterType)
    {
        return IcyBufferedImageUtil.scale(this, width, height, getNewFilterType(filterType));
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#scale(IcyBufferedImage, int, int)} instead.
     */
    @Deprecated
    public IcyBufferedImage getScaledCopy(int width, int height)
    {
        return IcyBufferedImageUtil.scale(this, width, height);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#translate(IcyBufferedImage, int, int, int)}
     *             instead.
     */
    @Deprecated
    public void translate(int dx, int dy, int channel)
    {
        IcyBufferedImageUtil.translate(this, dx, dy, channel);
    }

    /**
     * @deprecated Use {@link IcyBufferedImageUtil#translate(IcyBufferedImage, int, int)} instead.
     */
    @Deprecated
    public void translate(int dx, int dy)
    {
        IcyBufferedImageUtil.translate(this, dx, dy);
    }

    /**
     * Get calculated image channel bounds (min and max values)
     */
    private double[] getCalculatedChannelBounds(int channel)
    {
        final DataType dataType = getDataType_();

        final boolean signed = dataType.isSigned();
        final Object data = getDataXY(channel);

        final double min = ArrayMath.min(data, signed);
        final double max = ArrayMath.max(data, signed);

        return new double[] {min, max};
    }

    /**
     * Adjust specified bounds depending internal data type
     */
    private double[] adjustBoundsForDataType(double[] bounds)
    {
        double min, max;

        min = bounds[0];
        max = bounds[1];

        // only for integer data type
        if (!isFloatDataType())
        {
            // we force min to 0 if > 0
            if (min > 0d)
                min = 0d;
            // we force max to 0 if < 0
            if (max < 0d)
                max = 0d;
        }

        final DataType dataType = getDataType_();

        switch (dataType.getJavaType())
        {
            case BYTE:
                // return default bounds ([0..255] / [-128..127])
                return dataType.getDefaultBounds();

            case SHORT:
            case INT:
            case LONG:
                min = MathUtil.prevPow2((long) min + 1);
                max = MathUtil.nextPow2Mask((long) max);
                break;

            case FLOAT:
            case DOUBLE:
                // if [min..max] is included in [-1..1]
                if ((min >= -1d) && (max <= 1d))
                {
                    min = MathUtil.prevPow10(min);
                    max = MathUtil.nextPow10(max);
                }
                break;
        }

        return new double[] {min, max};
    }

    /**
     * Get the data type minimum value.
     */
    public double getDataTypeMin()
    {
        return getDataType_().getMinValue();
    }

    /**
     * Get the data type maximum value.
     */
    public double getDataTypeMax()
    {
        return getDataType_().getMaxValue();
    }

    /**
     * Get data type bounds (min and max values)
     */
    public double[] getDataTypeBounds()
    {
        return new double[] {getDataTypeMin(), getDataTypeMax()};
    }

    /**
     * Get the minimum type value for the specified channel.
     */
    public double getChannelTypeMin(int channel)
    {
        return getIcyColorModel().getComponentAbsMinValue(channel);
    }

    /**
     * Get the maximum type value for the specified channel.
     */
    public double getChannelTypeMax(int channel)
    {
        return getIcyColorModel().getComponentAbsMaxValue(channel);
    }

    /**
     * Get type bounds (min and max values) for the specified channel.
     */
    public double[] getChannelTypeBounds(int channel)
    {
        return getIcyColorModel().getComponentAbsBounds(channel);
    }

    /**
     * Get type bounds (min and max values) for all channels.
     */
    public double[][] getChannelsTypeBounds()
    {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelTypeBounds(c);

        return result;
    }

    /**
     * Get global type bounds (min and max values) for all channels.
     */
    public double[] getChannelsGlobalTypeBounds()
    {
        final int sizeC = getSizeC();
        final double[] result = getChannelTypeBounds(0);

        for (int c = 1; c < sizeC; c++)
        {
            final double[] bounds = getChannelTypeBounds(c);
            result[0] = Math.min(bounds[0], result[0]);
            result[1] = Math.max(bounds[1], result[1]);
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getChannelsGlobalTypeBounds()} instead.
     */
    @Deprecated
    public double[] getChannelTypeGlobalBounds()
    {
        return getChannelsGlobalTypeBounds();
    }

    /**
     * @deprecated Use {@link #getChannelTypeGlobalBounds()} instead.
     */
    @Deprecated
    public double[] getGlobalChannelTypeBounds()
    {
        return getChannelTypeGlobalBounds();
    }

    /**
     * @deprecated Use {@link #getChannelTypeMin(int)} instead.
     */
    @Deprecated
    public double getComponentAbsMinValue(int component)
    {
        return getChannelTypeMin(component);
    }

    /**
     * @deprecated Use {@link #getChannelTypeMax(int)} instead.
     */
    @Deprecated
    public double getComponentAbsMaxValue(int component)
    {
        return getChannelTypeMax(component);
    }

    /**
     * @deprecated Use {@link #getChannelTypeBounds(int)} instead.
     */
    @Deprecated
    public double[] getComponentAbsBounds(int component)
    {
        return getChannelTypeBounds(component);
    }

    /**
     * @deprecated Use {@link #getChannelsTypeBounds()} instead.
     */
    @Deprecated
    public double[][] getComponentsAbsBounds()
    {
        return getChannelsTypeBounds();
    }

    /**
     * @deprecated Use {@link #getGlobalChannelTypeBounds()} instead.
     */
    @Deprecated
    public double[] getGlobalComponentAbsBounds()
    {
        return getChannelTypeGlobalBounds();
    }

    /**
     * Get the minimum value for the specified channel.
     */
    public double getChannelMin(int channel)
    {
        return getIcyColorModel().getComponentUserMinValue(channel);
    }

    /**
     * Get maximum value for the specified channel.
     */
    public double getChannelMax(int channel)
    {
        return getIcyColorModel().getComponentUserMaxValue(channel);
    }

    /**
     * Get bounds (min and max values) for the specified channel.
     */
    public double[] getChannelBounds(int channel)
    {
        return getIcyColorModel().getComponentUserBounds(channel);
    }

    /**
     * Get bounds (min and max values) for all channels.
     */
    public double[][] getChannelsBounds()
    {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelBounds(c);

        return result;
    }

    /**
     * Get global bounds (min and max values) for all channels.
     */
    public double[] getChannelsGlobalBounds()
    {
        final int sizeC = getSizeC();
        final double[] result = new double[2];

        result[0] = Double.MAX_VALUE;
        result[1] = -Double.MAX_VALUE;

        for (int c = 0; c < sizeC; c++)
        {
            final double[] bounds = getChannelBounds(c);

            if (bounds[0] < result[0])
                result[0] = bounds[0];
            if (bounds[1] > result[1])
                result[1] = bounds[1];
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getChannelMin(int)} instead.
     */
    @Deprecated
    public double getComponentUserMinValue(int component)
    {
        return getChannelMin(component);
    }

    /**
     * @deprecated Use {@link #getChannelMax(int)} instead.
     */
    @Deprecated
    public double getComponentUserMaxValue(int component)
    {
        return getChannelMax(component);
    }

    /**
     * @deprecated Use {@link #getChannelBounds(int)} instead.
     */
    @Deprecated
    public double[] getComponentUserBounds(int component)
    {
        return getChannelBounds(component);
    }

    /**
     * @deprecated Use {@link #getChannelsBounds()} instead.
     */
    @Deprecated
    public double[][] getComponentsUserBounds()
    {
        return getChannelsBounds();
    }

    /**
     * Set the preferred data type minimum value for the specified channel.
     */
    public void setChannelTypeMin(int channel, double min)
    {
        getIcyColorModel().setComponentAbsMinValue(channel, min);
    }

    /**
     * Set the preferred data type maximum value for the specified channel.
     */
    public void setChannelTypeMax(int channel, double max)
    {
        getIcyColorModel().setComponentAbsMaxValue(channel, max);
    }

    /**
     * /**
     * Set the preferred data type min and max values for the specified channel.
     */
    public void setChannelTypeBounds(int channel, double min, double max)
    {
        getIcyColorModel().setComponentAbsBounds(channel, min, max);
    }

    /**
     * Set the preferred data type bounds (min and max values) for all channels.
     */
    public void setChannelsTypeBounds(double[][] bounds)
    {
        getIcyColorModel().setComponentsAbsBounds(bounds);
    }

    /**
     * @deprecated Use {@link #setChannelTypeMin(int, double)} instead.
     */
    @Deprecated
    public void setComponentAbsMinValue(int component, double min)
    {
        setChannelTypeMin(component, min);
    }

    /**
     * @deprecated Use {@link #setChannelTypeMax(int, double)} instead.
     */
    @Deprecated
    public void setComponentAbsMaxValue(int component, double max)
    {
        setChannelTypeMax(component, max);
    }

    /**
     * @deprecated Use {@link #setChannelTypeBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentAbsBounds(int component, double[] bounds)
    {
        setChannelTypeBounds(component, bounds[0], bounds[1]);
    }

    /**
     * @deprecated Use {@link #setChannelTypeBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentAbsBounds(int component, double min, double max)
    {
        setChannelTypeBounds(component, min, max);
    }

    /**
     * @deprecated Use {@link #setChannelsTypeBounds(double[][])} instead.
     */
    @Deprecated
    public void setComponentsAbsBounds(double[][] bounds)
    {
        setChannelsTypeBounds(bounds);
    }

    /**
     * Set channel minimum value.
     */
    public void setChannelMin(int channel, double min)
    {
        final IcyColorModel cm = getIcyColorModel();

        if ((min < cm.getComponentAbsMinValue(channel)))
            cm.setComponentAbsMinValue(channel, min);
        cm.setComponentUserMinValue(channel, min);
    }

    /**
     * Set channel maximum value.
     */
    public void setChannelMax(int channel, double max)
    {
        final IcyColorModel cm = getIcyColorModel();

        if ((max > cm.getComponentAbsMaxValue(channel)))
            cm.setComponentAbsMinValue(channel, max);
        cm.setComponentUserMaxValue(channel, max);
    }

    /**
     * Set channel bounds (min and max values)
     */
    public void setChannelBounds(int channel, double min, double max)
    {
        final IcyColorModel cm = getIcyColorModel();
        final double[] typeBounds = cm.getComponentAbsBounds(channel);

        if ((min < typeBounds[0]) || (max > typeBounds[1]))
            cm.setComponentAbsBounds(channel, min, max);
        cm.setComponentUserBounds(channel, min, max);
    }

    /**
     * Set all channel bounds (min and max values)
     */
    public void setChannelsBounds(double[][] bounds)
    {
        // we use the setChannelBounds(..) method so we do range check
        for (int c = 0; c < bounds.length; c++)
        {
            final double[] b = bounds[c];
            setChannelBounds(c, b[0], b[1]);
        }
    }

    /**
     * @deprecated Use {@link #setChannelMin(int, double)} instead.
     */
    @Deprecated
    public void setComponentUserMinValue(int component, double min)
    {
        setChannelMin(component, min);
    }

    /**
     * @deprecated Use {@link #setChannelMax(int, double)} instead.
     */
    @Deprecated
    public void setComponentUserMaxValue(int component, double max)
    {
        setChannelMax(component, max);
    }

    /**
     * @deprecated Use {@link #setChannelBounds(int, double, double)} instead.
     */
    @Deprecated
    public void setComponentUserBounds(int component, double[] bounds)
    {
        setChannelBounds(component, bounds[0], bounds[1]);
    }

    /**
     * @deprecated Use {@link #setChannelBounds(int, double, double)} instead
     */
    @Deprecated
    public void setComponentUserBounds(int component, double min, double max)
    {
        setChannelBounds(component, min, max);
    }

    /**
     * @deprecated Use {@link #setChannelsBounds(double[][])} instead.
     */
    @Deprecated
    public void setComponentsUserBounds(double[][] bounds)
    {
        setChannelsBounds(bounds);
    }

    /**
     * Update channels bounds (min and max values).
     */
    public void updateChannelsBounds()
    {
        final int sizeC = getSizeC();
        final IcyColorModel cm = getIcyColorModel();

        for (int c = 0; c < sizeC; c++)
        {
            // get data type bounds
            final double[] bounds = getCalculatedChannelBounds(c);

            cm.setComponentAbsBounds(c, adjustBoundsForDataType(bounds));

            final IcyColorModel colorModel = getIcyColorModel();

            if (colorModel != null)
            {
                final IcyColorMap colorMap = colorModel.getColorMap(c);

                // we do user bounds adjustment on "non ALPHA" component only
                if (colorMap.getType() != IcyColorMapType.ALPHA)
                    cm.setComponentUserBounds(c, bounds);
            }
        }
    }

    /**
     * @deprecated Use {@link #updateChannelsBounds()} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void updateComponentsBounds(boolean updateChannelBounds, boolean adjustByteToo)
    {
        updateChannelsBounds();
    }

    /**
     * @deprecated Use {@link #updateChannelsBounds()} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void updateComponentsBounds(boolean updateUserBounds)
    {
        updateChannelsBounds();
    }

    /**
     * Return true if point is inside the image
     */
    public boolean isInside(Point p)
    {
        return isInside(p.x, p.y);
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(int x, int y)
    {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(double x, double y)
    {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return the IcyColorModel
     * 
     * @return IcyColorModel
     */
    public IcyColorModel getIcyColorModel()
    {
        return (IcyColorModel) getColorModel();
    }

    /**
     * Return the data type of this image
     * 
     * @return dataType
     * @see DataType
     */
    public DataType getDataType_()
    {
        return getIcyColorModel().getDataType_();
    }

    /**
     * @deprecated use {@link #getDataType_()} instead
     */
    @Deprecated
    public int getDataType()
    {
        return getIcyColorModel().getDataType();
    }

    /**
     * Return true if this is a float data type image
     */
    public boolean isFloatDataType()
    {
        return getDataType_().isFloat();
    }

    /**
     * Return true if this is a signed data type image
     */
    public boolean isSignedDataType()
    {
        return getDataType_().isSigned();
    }

    /**
     * @deprecated Use {@link #getSizeC()} instead.
     */
    @Deprecated
    public int getNumComponents()
    {
        return getSizeC();
    }

    /**
     * @return the number of components of this image
     */
    public int getSizeC()
    {
        return getColorModel().getNumComponents();
    }

    /**
     * @return the width of the image
     */
    public int getSizeX()
    {
        return getWidth();
    }

    /**
     * @return the height of the image
     */
    public int getSizeY()
    {
        return getHeight();
    }

    /**
     * Return 2D dimension of image {sizeX, sizeY}
     */
    public Dimension getDimension()
    {
        return new Dimension(getSizeX(), getSizeY());
    }

    /**
     * Return 2D bounds of image {0, 0, sizeX, sizeY}
     */
    public Rectangle getBounds()
    {
        return new Rectangle(getSizeX(), getSizeY());
    }

    /**
     * Return the number of sample.<br>
     * This is equivalent to<br>
     * <code>getSizeX() * getSizeY() * getSizeC()</code>
     */
    public int getNumSample()
    {
        return getSizeX() * getSizeY() * getSizeC();
    }

    /**
     * Return the offset for specified (x, y) location
     */
    public int getOffset(int x, int y)
    {
        return (y * getWidth()) + x;
    }

    /**
     * return LUT of this image
     */
    public LUT getLUT()
    {
        return internalLut;
    }

    /**
     * create a compatible LUT for this image
     */
    public LUT createCompatibleLUT()
    {
        return new LUT(IcyColorModel.createInstance(getIcyColorModel(), false, false));
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public Object getDataXYC()
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataXYCAsByte();
            case SHORT:
                return getDataXYCAsShort();
            case INT:
                return getDataXYCAsInt();
            case FLOAT:
                return getDataXYCAsFloat();
            case DOUBLE:
                return getDataXYCAsDouble();
            default:
                return null;
        }
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public Object getDataXY(int c)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataXYAsByte(c);
            case SHORT:
                return getDataXYAsShort(c);
            case INT:
                return getDataXYAsInt(c);
            case FLOAT:
                return getDataXYAsFloat(c);
            case DOUBLE:
                return getDataXYAsDouble(c);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public Object getDataCopyXYC()
    {
        return getDataCopyXYC(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYC(Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyXYCAsByte((byte[]) out, offset);
            case SHORT:
                return getDataCopyXYCAsShort((short[]) out, offset);
            case INT:
                return getDataCopyXYCAsInt((int[]) out, offset);
            case FLOAT:
                return getDataCopyXYCAsFloat((float[]) out, offset);
            case DOUBLE:
                return getDataCopyXYCAsDouble((double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c
     */
    public Object getDataCopyXY(int c)
    {
        return getDataCopyXY(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXY(int c, Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyXYAsByte(c, (byte[]) out, offset);
            case SHORT:
                return getDataCopyXYAsShort(c, (short[]) out, offset);
            case INT:
                return getDataCopyXYAsInt(c, (int[]) out, offset);
            case FLOAT:
                return getDataCopyXYAsFloat(c, (float[]) out, offset);
            case DOUBLE:
                return getDataCopyXYAsDouble(c, (double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]
     */
    public Object getDataCopyCXY()
    {
        return getDataCopyCXY(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXY(Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyCXYAsByte((byte[]) out, offset);
            case SHORT:
                return getDataCopyCXYAsShort((short[]) out, offset);
            case INT:
                return getDataCopyCXYAsInt((int[]) out, offset);
            case FLOAT:
                return getDataCopyCXYAsFloat((float[]) out, offset);
            case DOUBLE:
                return getDataCopyCXYAsDouble((double[]) out, offset);
            default:
                return null;
        }

    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public Object getDataCopyC(int x, int y)
    {
        return getDataCopyC(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyC(int x, int y, Object out, int offset)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                return getDataCopyCAsByte(x, y, (byte[]) out, offset);
            case SHORT:
                return getDataCopyCAsShort(x, y, (short[]) out, offset);
            case INT:
                return getDataCopyCAsInt(x, y, (int[]) out, offset);
            case FLOAT:
                return getDataCopyCAsFloat(x, y, (float[]) out, offset);
            case DOUBLE:
                return getDataCopyCAsDouble(x, y, (double[]) out, offset);
            default:
                return null;
        }
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXY(int c, Object values)
    {
        ArrayUtil.arrayToArray(values, getDataXY(c), getDataType_().isSigned());

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataC(int x, int y, Object values)
    {
        switch (getDataType_().getJavaType())
        {
            case BYTE:
                setDataCAsByte(x, y, (byte[]) values);
                break;

            case SHORT:
                setDataCAsShort(x, y, (short[]) values);
                break;

            case INT:
                setDataCAsInt(x, y, (int[]) values);
                break;

            case FLOAT:
                setDataCAsFloat(x, y, (float[]) values);
                break;

            case DOUBLE:
                setDataCAsDouble(x, y, (double[]) values);
                break;
        }
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public byte[][] getDataXYCAsByte()
    {
        return ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public short[][] getDataXYCAsShort()
    {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getBankData();
        return ((DataBufferShort) db).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public int[][] getDataXYCAsInt()
    {
        return ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public float[][] getDataXYCAsFloat()
    {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public double[][] getDataXYCAsDouble()
    {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public byte[] getDataXYAsByte(int c)
    {
        return ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public short[] getDataXYAsShort(int c)
    {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getData(c);
        return ((DataBufferShort) db).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public int[] getDataXYAsInt(int c)
    {
        return ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public float[] getDataXYAsFloat(int c)
    {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public double[] getDataXYAsDouble(int c)
    {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public byte[] getDataCopyXYCAsByte()
    {
        return getDataCopyXYCAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYCAsByte(byte[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, len * sizeC);
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final byte[] src = banks[c];
            System.arraycopy(src, 0, result, offset, len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public short[] getDataCopyXYCAsShort()
    {
        return getDataCopyXYCAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public short[] getDataCopyXYCAsShort(short[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, len * sizeC);
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final short[] src = banks[c];
            System.arraycopy(src, 0, result, offset, len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public int[] getDataCopyXYCAsInt()
    {
        return getDataCopyXYCAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public int[] getDataCopyXYCAsInt(int[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, len * sizeC);
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final int[] src = banks[c];
            System.arraycopy(src, 0, result, offset, len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public float[] getDataCopyXYCAsFloat()
    {
        return getDataCopyXYCAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public float[] getDataCopyXYCAsFloat(float[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, len * sizeC);
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final float[] src = banks[c];
            System.arraycopy(src, 0, result, offset, len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public double[] getDataCopyXYCAsDouble()
    {
        return getDataCopyXYCAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public double[] getDataCopyXYCAsDouble(double[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, len * sizeC);
        int offset = off;

        for (int c = 0; c < sizeC; c++)
        {
            final double[] src = banks[c];
            System.arraycopy(src, 0, result, offset, len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public byte[] getDataCopyXYAsByte(int c)
    {
        return getDataCopyXYAsByte(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYAsByte(int c, byte[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final byte[] src = ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
        final byte[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public short[] getDataCopyXYAsShort(int c)
    {
        return getDataCopyXYAsShort(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyXYAsShort(int c, short[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final DataBuffer db = getRaster().getDataBuffer();
        final short[] src;
        if (db instanceof DataBufferUShort)
            src = ((DataBufferUShort) db).getData(c);
        else
            src = ((DataBufferShort) db).getData(c);
        final short[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public int[] getDataCopyXYAsInt(int c)
    {
        return getDataCopyXYAsInt(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyXYAsInt(int c, int[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int[] src = ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
        final int[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public float[] getDataCopyXYAsFloat(int c)
    {
        return getDataCopyXYAsFloat(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyXYAsFloat(int c, float[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final float[] src = ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
        final float[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public double[] getDataCopyXYAsDouble(int c)
    {
        return getDataCopyXYAsDouble(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyXYAsDouble(int c, double[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final double[] src = ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
        final double[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public byte[] getDataCopyCXYAsByte()
    {
        return getDataCopyCXYAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCXYAsByte(byte[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, len * sizeC);

        for (int c = 0; c < sizeC; c++)
        {
            final byte[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public short[] getDataCopyCXYAsShort()
    {
        return getDataCopyCXYAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCXYAsShort(short[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, len * sizeC);

        for (int c = 0; c < sizeC; c++)
        {
            final short[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public int[] getDataCopyCXYAsInt()
    {
        return getDataCopyCXYAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCXYAsInt(int[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, len * sizeC);

        for (int c = 0; c < sizeC; c++)
        {
            final int[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public float[] getDataCopyCXYAsFloat()
    {
        return getDataCopyCXYAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCXYAsFloat(float[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, len * sizeC);

        for (int c = 0; c < sizeC; c++)
        {
            final float[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public double[] getDataCopyCXYAsDouble()
    {
        return getDataCopyCXYAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCXYAsDouble(double[] out, int off)
    {
        final int len = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, len * sizeC);

        for (int c = 0; c < sizeC; c++)
        {
            final double[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public byte[] getDataCopyCAsByte(int x, int y)
    {
        return getDataCopyCAsByte(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCAsByte(int x, int y, byte[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final byte[][] data = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public short[] getDataCopyCAsShort(int x, int y)
    {
        return getDataCopyCAsShort(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCAsShort(int x, int y, short[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public int[] getDataCopyCAsInt(int x, int y)
    {
        return getDataCopyCAsInt(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCAsInt(int x, int y, int[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final int[][] data = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public float[] getDataCopyCAsFloat(int x, int y)
    {
        return getDataCopyCAsFloat(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCAsFloat(int x, int y, float[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final float[][] data = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public double[] getDataCopyCAsDouble(int x, int y)
    {
        return getDataCopyCAsDouble(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCAsDouble(int x, int y, double[] out, int off)
    {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final double[][] data = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsByte(int c, byte[] values)
    {
        System.arraycopy(values, 0, getDataXYAsByte(c), 0, getSizeX() * getSizeY());

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsShort(int c, short[] values)
    {
        System.arraycopy(values, 0, getDataXYAsShort(c), 0, getSizeX() * getSizeY());

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsInt(int c, int[] values)
    {
        System.arraycopy(values, 0, getDataXYAsInt(c), 0, getSizeX() * getSizeY());

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsFloat(int c, float[] values)
    {
        System.arraycopy(values, 0, getDataXYAsFloat(c), 0, getSizeX() * getSizeY());

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsDouble(int c, double[] values)
    {
        System.arraycopy(values, 0, getDataXYAsDouble(c), 0, getSizeX() * getSizeY());

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsByte(int x, int y, byte[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final byte[][] data = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsShort(int x, int y, short[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsInt(int x, int y, int[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final int[][] data = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsFloat(int x, int y, float[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final float[][] data = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsDouble(int x, int y, double[] values)
    {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final double[][] data = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public double getData(int x, int y, int c)
    {
        return Array1DUtil.getValue(getDataXY(c), getOffset(x, y), getDataType_());
    }

    /**
     * Set the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public void setData(int x, int y, int c, double value)
    {
        Array1DUtil.setValue(getDataXY(c), getOffset(x, y), getDataType_(), value);

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public byte getDataAsByte(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferByte) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsByte(int x, int y, int c, byte value)
    {
        // ignore band offset as it's always 0 here
        (((DataBufferByte) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())] = value;

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public short getDataAsShort(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        final DataBuffer db = getRaster().getDataBuffer();

        if (db instanceof DataBufferUShort)
            return (((DataBufferUShort) db).getData(c))[x + (y * getWidth())];

        return (((DataBufferShort) db).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsShort(int x, int y, int c, short value)
    {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            // ignore band offset as it's always 0 here
            (((DataBufferUShort) db).getData(c))[x + (y * getWidth())] = value;
        else
            (((DataBufferShort) db).getData(c))[x + (y * getWidth())] = value;

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public int getDataAsInt(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferInt) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsInt(int x, int y, int c, int value)
    {
        // ignore band offset as it's always 0 here
        (((DataBufferInt) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())] = value;

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public float getDataAsFloat(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferFloat) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsFloat(int x, int y, int c, float value)
    {
        // ignore band offset as it's always 0 here
        (((DataBufferFloat) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())] = value;

        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public double getDataAsDouble(int x, int y, int c)
    {
        // ignore band offset as it's always 0 here
        return (((DataBufferDouble) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsDouble(int x, int y, int c, double value)
    {
        // ignore band offset as it's always 0 here
        (((DataBufferDouble) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())] = value;

        // notify data changed
        dataChanged();
    }

    /**
     * Same as getRGB but by using the specified LUT instead of internal one
     * 
     * @see java.awt.image.BufferedImage#getRGB(int, int)
     */
    public int getRGB(int x, int y, LUT lut)
    {
        return getIcyColorModel().getRGB(getRaster().getDataElements(x, y, null), lut);
    }

    /**
     * Internal copy data from an icy image (notify data changed)
     * 
     * @param srcImage
     *        source icy image
     * @param srcRect
     *        source region
     * @param dstPt
     *        destination X,Y position
     * @param srcChannel
     *        source channel
     * @param dstChannel
     *        destination channel
     */
    private void fastCopyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcChannel, int dstChannel)
    {
        final int srcSizeX = srcImage.getSizeX();
        final int dstSizeX = getSizeX();

        // limit to source image size
        Rectangle adjSrcRect = srcRect.intersection(new Rectangle(srcSizeX, srcImage.getSizeY()));
        // negative destination x position
        if (dstPt.x < 0)
            // adjust source rect
            adjSrcRect.x += -dstPt.x;
        // negative destination y position
        if (dstPt.y < 0)
            // adjust source rect
            adjSrcRect.y += -dstPt.y;

        final Rectangle dstRect = new Rectangle(dstPt.x, dstPt.y, adjSrcRect.width, adjSrcRect.height);
        // limit to destination image size
        final Rectangle adjDstRect = dstRect.intersection(new Rectangle(dstSizeX, getSizeY()));

        final int w = Math.min(adjSrcRect.width, adjDstRect.width);
        final int h = Math.min(adjSrcRect.height, adjDstRect.height);

        // nothing to copy
        if ((w == 0) || (h == 0))
            return;

        final boolean signed = srcImage.getDataType_().isSigned();

        final Object src = srcImage.getDataXY(srcChannel);
        final Object dst = getDataXY(dstChannel);

        int srcOffset = adjSrcRect.x + (adjSrcRect.y * srcSizeX);
        int dstOffset = adjDstRect.x + (adjDstRect.y * dstSizeX);

        for (int y = 0; y < h; y++)
        {
            ArrayUtil.arrayToArray(src, srcOffset, dst, dstOffset, w, signed);
            srcOffset += srcSizeX;
            dstOffset += dstSizeX;
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Internal copy data from a compatible image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    private void internalCompatibleCopyData(BufferedImage srcImage, int srcChannel, int dstChannel, DataBuffer src_db,
            DataBuffer dst_db, int[] indices, int[] band_offsets, int[] bank_offsets, int scanlineStride_src,
            int pixelStride_src, int maxX, int maxY, int decOffsetSrc)
    {
        final int scanlineStride_dst = getSizeX();

        final int bank = indices[srcChannel];
        final int offset = band_offsets[srcChannel] + bank_offsets[bank] - decOffsetSrc;

        switch (getDataType_().getJavaType())
        {
            case BYTE:
            {
                final byte[] src;
                final byte[] dst = ((DataBufferByte) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof SignedByteBuffer)
                    src = ((SignedByteBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferByte) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case SHORT:
            {
                final short[] src;
                final short[] dst;

                // LOCI use its own buffer classes
                if (src_db instanceof SignedShortBuffer)
                    src = ((SignedShortBuffer) src_db).getData(bank);
                else if (src_db instanceof DataBufferShort)
                    src = ((DataBufferShort) src_db).getData(bank);
                else
                    src = ((DataBufferUShort) src_db).getData(bank);

                if (dst_db instanceof DataBufferShort)
                    dst = ((DataBufferShort) dst_db).getData(dstChannel);
                else
                    dst = ((DataBufferUShort) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case INT:
            {
                final int[] src;
                final int[] dst = ((DataBufferInt) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof UnsignedIntBuffer)
                    src = ((UnsignedIntBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferInt) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case FLOAT:
            {
                final float[] src = ((DataBufferFloat) src_db).getData(bank);
                final float[] dst = ((DataBufferFloat) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case DOUBLE:
            {
                final double[] src = ((DataBufferDouble) src_db).getData(bank);
                final double[] dst = ((DataBufferDouble) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++)
                {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++)
                    {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }
        }
    }

    /**
     * Internal copy data from a compatible image (notify data changed).
     * 
     * @param srcImage
     *        source image
     * @param srcChannel
     *        source channel (-1 for all channels)
     * @param dstChannel
     *        destination channel (only significant if source channel != -1)
     */
    private boolean compatibleCopyData(BufferedImage srcImage, int srcChannel, int dstChannel)
    {
        // not compatible sample model
        if (!(srcImage.getSampleModel() instanceof ComponentSampleModel))
            return false;

        final ComponentSampleModel sm = (ComponentSampleModel) srcImage.getSampleModel();

        // not compatible sample model
        if (DataType.getDataTypeFromDataBufferType(sm.getDataType()) != getDataType_())
            return false;

        final WritableRaster src_wr = srcImage.getRaster();
        final DataBuffer src_db = src_wr.getDataBuffer();
        final DataBuffer dst_db = getRaster().getDataBuffer();
        final int[] indices = sm.getBankIndices();
        final int[] band_offsets = sm.getBandOffsets();
        final int[] bank_offsets = src_db.getOffsets();
        final int scanlineStride_src = sm.getScanlineStride();
        final int pixelStride_src = sm.getPixelStride();
        final int maxX = Math.min(getSizeX(), sm.getWidth());
        final int maxY = Math.min(getSizeY(), sm.getHeight());
        final int decOffsetSrc = src_wr.getSampleModelTranslateX()
                + (src_wr.getSampleModelTranslateY() * scanlineStride_src);

        // all channels
        if (srcChannel == -1)
        {
            final int numBands = sm.getNumBands();

            for (int band = 0; band < numBands; band++)
                internalCompatibleCopyData(srcImage, band, band, src_db, dst_db, indices, band_offsets, bank_offsets,
                        scanlineStride_src, pixelStride_src, maxX, maxY, decOffsetSrc);
        }
        else
        {
            internalCompatibleCopyData(srcImage, srcChannel, dstChannel, src_db, dst_db, indices, band_offsets,
                    bank_offsets, scanlineStride_src, pixelStride_src, maxX, maxY, decOffsetSrc);
        }

        // notify data changed
        dataChanged();

        return true;
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcRect
     *        source region to copy (assume whole image if null)
     * @param dstPt
     *        destination X,Y position (assume [0,0] if null)
     * @param srcChannel
     *        source channel (-1 for all channels)
     * @param dstChannel
     *        destination channel (only significant if source channel != -1)
     */
    public void copyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcChannel, int dstChannel)
    {
        if (srcImage == null)
            return;

        final Rectangle adjSrcRect;
        final Point adjDstPt;

        if (srcRect == null)
            adjSrcRect = new Rectangle(srcImage.getSizeX(), srcImage.getSizeY());
        else
            adjSrcRect = srcRect;
        if (dstPt == null)
            adjDstPt = new Point(0, 0);
        else
            adjDstPt = dstPt;

        // copy all possible components
        if (srcChannel == -1)
        {
            final int sizeC = Math.min(srcImage.getSizeC(), getSizeC());

            beginUpdate();
            try
            {
                for (int c = 0; c < sizeC; c++)
                    fastCopyData(srcImage, adjSrcRect, adjDstPt, c, c);
            }
            finally
            {
                endUpdate();
            }
        }
        else
            fastCopyData(srcImage, adjSrcRect, adjDstPt, srcChannel, dstChannel);
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcRect
     *        source region to copy (assume whole image if null)
     * @param dstPt
     *        destination (assume [0,0] if null)
     */
    public void copyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt)
    {
        if (srcImage == null)
            return;

        copyData(srcImage, srcRect, dstPt, -1, 0);
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     * @param srcChannel
     *        source channel to copy (-1 for all channels)
     * @param dstChannel
     *        destination channel to receive data (only significant if source channel != -1)
     */
    public void copyData(BufferedImage srcImage, int srcChannel, int dstChannel)
    {
        if (srcImage == null)
            return;

        if (srcImage instanceof IcyBufferedImage)
            copyData(((IcyBufferedImage) srcImage), null, null, srcChannel, dstChannel);
        else
        {
            // use intermediate data copy for compatible image
            if (!compatibleCopyData(srcImage, srcChannel, dstChannel))
            {
                // image not compatible, use generic (and slow) data copy
                srcImage.copyData(getRaster());
                // notify data changed
                dataChanged();
            }
        }
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    public void copyData(BufferedImage srcImage)
    {
        copyData(srcImage, -1, -1);
    }

    /**
     * Return raw data component as an array of byte
     * 
     * @param c
     *        component index
     * @param out
     *        output array (can be null)
     * @param offset
     *        output offset
     * @param step
     *        output step
     * @param little
     *        little endian order
     */
    public byte[] getRawData(int c, byte[] out, int offset, int step, boolean little)
    {
        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out, offset
                + (getSizeX() * getSizeY() * getDataType_().getSize()));

        return ByteArrayConvert.toByteArray(getDataXY(c), result, offset, step, little);
    }

    /**
     * Return raw data component as an array of byte
     * 
     * @param c
     *        component index
     * @param out
     *        output array (can be null)
     * @param offset
     *        output offset
     * @param little
     *        little endian order
     */
    public byte[] getRawData(int c, byte[] out, int offset, boolean little)
    {
        return getRawData(c, out, offset, 1, little);
    }

    /**
     * Return raw data component as an array of byte
     * 
     * @param c
     *        component index
     * @param little
     *        little endian order
     */
    public byte[] getRawData(int c, boolean little)
    {
        return getRawData(c, null, 0, little);
    }

    /**
     * Return raw data for all components as an array of byte
     * 
     * @param out
     *        output array (can be null)
     * @param offset
     *        output offset
     * @param little
     *        little endian order
     * @param interleaved
     *        interleave component data
     */
    public byte[] getRawData(byte[] out, int offset, boolean little, boolean interleaved)
    {
        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType_().getSize();

        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out, offset + (sizeC * sizeXY * sizeType));

        int outOff = offset;
        if (interleaved)
        {
            // interleave channel data
            for (int c = 0; c < sizeC; c++)
            {
                getRawData(c, result, outOff, sizeC, little);
                outOff += sizeType;
            }
        }
        else
        {
            for (int c = 0; c < sizeC; c++)
            {
                getRawData(c, result, outOff, 1, little);
                outOff += sizeXY * sizeType;
            }
        }

        return result;
    }

    /**
     * Return raw data for all components as an array of byte
     * 
     * @param little
     *        little endian order
     * @param interleaved
     *        interleave component data
     */
    public byte[] getRawData(boolean little, boolean interleaved)
    {
        return getRawData(null, 0, little, interleaved);
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     * 
     * @param c
     *        component index
     * @param data
     *        data as byte array
     * @param offset
     *        input offset
     * @param step
     *        input step
     * @param little
     *        little endian order
     */
    public void setRawData(int c, byte[] data, int offset, int step, boolean little)
    {
        if (data == null)
            return;

        ByteArrayConvert.byteArrayTo(data, offset, step, getDataXY(c), little);

        // notify data changed
        dataChanged();
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     * 
     * @param c
     *        component index
     * @param data
     *        data as byte array
     * @param offset
     *        input offset
     * @param little
     *        little endian order
     */
    public void setRawData(int c, byte[] data, int offset, boolean little)
    {
        setRawData(c, data, offset, 1, little);
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     * 
     * @param c
     *        component index
     * @param data
     *        data as byte array
     * @param little
     *        little endian order
     */
    public void setRawData(int c, byte[] data, boolean little)
    {
        setRawData(c, data, 0, 1, little);
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed)
     * 
     * @param data
     *        data as byte array
     * @param offset
     *        input offset
     * @param little
     *        little endian order
     * @param interleaved
     *        interleave component data
     */
    public void setRawData(byte[] data, int offset, boolean little, boolean interleaved)
    {
        if (data == null)
            return;

        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType_().getSize();

        beginUpdate();
        try
        {
            int inOff = offset;
            if (interleaved)
            {
                // interleave channel data
                for (int c = 0; c < sizeC; c++)
                {
                    setRawData(c, data, inOff, sizeC, little);
                    inOff += sizeType;
                }
            }
            else
            {
                for (int c = 0; c < sizeC; c++)
                {
                    setRawData(c, data, inOff, 1, little);
                    inOff += sizeXY * sizeType;
                }
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed)
     * 
     * @param data
     *        data as byte array
     * @param little
     *        little endian order
     * @param interleaved
     *        interleave component data
     */
    public void setRawData(byte[] data, boolean little, boolean interleaved)
    {
        setRawData(data, 0, little, interleaved);
    }

    /**
     * Return the colormap of the specified channel.
     */
    public IcyColorMap getColorMap(int channel)
    {
        return getIcyColorModel().getColorMap(channel);
    }

    /**
     * @deprecated Use {@link #getColorMap(int)} instead (different case).
     */
    @Deprecated
    public IcyColorMap getColormap(int channel)
    {
        return getColorMap(channel);
    }

    /**
     * @deprecated Use {@link #setColorMaps(BufferedImage)} instead.
     */
    @Deprecated
    public void copyColormap(BufferedImage srcImage)
    {
        setColorMaps(srcImage);
    }

    /**
     * Set colormaps from specified image.
     */
    public void setColorMaps(BufferedImage srcImage)
    {
        getIcyColorModel().setColorMaps(srcImage.getColorModel());
    }

    /**
     * @deprecated Use {@link #setColorMaps(BufferedImage)} instead (different case).
     */
    @Deprecated
    public void setColormaps(BufferedImage srcImage)
    {
        setColorMaps(srcImage);
    }

    /**
     * Set the colormap for the specified channel.
     * 
     * @param channel
     *        channel we want to set the colormap
     * @param map
     *        source colorspace to copy
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMap(int channel, IcyColorMap map, boolean setAlpha)
    {
        getIcyColorModel().setColorMap(channel, map, setAlpha);
    }

    /**
     * @deprecated Use {@link #setColorMap(int, IcyColorMap, boolean)} instead.
     */
    @Deprecated
    public void setColormap(int channel, IcyColorMap map)
    {
        setColorMap(channel, map, true);
    }

    /**
     * notify image data has changed
     */
    public void dataChanged()
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.DATA_CHANGED));
    }

    /**
     * notify image colorMap has changed
     */
    private void colormapChanged(int component)
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.COLORMAP_CHANGED, component));
    }

    /**
     * notify image channels bounds has changed
     */
    public void channelBoundsChanged(int channel)
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.BOUNDS_CHANGED, channel));
    }

    /**
     * @deprecated Use {@link #channelBoundsChanged(int)} instead.
     */
    @Deprecated
    public void componentBoundsChanged(int component)
    {
        channelBoundsChanged(component);
    }

    /**
     * fire change event
     */
    private void fireChangeEvent(IcyBufferedImageEvent e)
    {
        for (IcyBufferedImageListener listener : listeners.getListeners(IcyBufferedImageListener.class))
            listener.imageChanged(e);
    }

    public void addListener(IcyBufferedImageListener listener)
    {
        listeners.add(IcyBufferedImageListener.class, listener);
    }

    public void removeListener(IcyBufferedImageListener listener)
    {
        listeners.remove(IcyBufferedImageListener.class, listener);
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        IcyBufferedImageEvent event = (IcyBufferedImageEvent) object;

        switch (event.getType())
        {
        // do here global process on image data change
            case DATA_CHANGED:
                // update image components bounds
                if (autoUpdateChannelBounds)
                    updateChannelsBounds();
                break;

            // do here global process on image bounds change
            case BOUNDS_CHANGED:
                break;

            // do here global process on image colormap change
            case COLORMAP_CHANGED:
                break;
        }

        // notify listener we have changed
        fireChangeEvent(event);
    }

    @Override
    public void colorModelChanged(IcyColorModelEvent e)
    {
        switch (e.getType())
        {
            case COLORMAP_CHANGED:
                colormapChanged(e.getComponent());
                break;

            case SCALER_CHANGED:
                channelBoundsChanged(e.getComponent());
                break;
        }
    }
}
