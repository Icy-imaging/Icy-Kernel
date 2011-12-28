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
package icy.image;

import icy.common.EventHierarchicalChecker;
import icy.common.IcyChangedListener;
import icy.common.UpdateEventHandler;
import icy.image.IcyBufferedImageEvent.IcyBufferedImageEventType;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormodel.IcyColorModel;
import icy.image.colormodel.IcyColorModelEvent;
import icy.image.colormodel.IcyColorModelListener;
import icy.image.colorspace.IcyColorSpace;
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

import javax.media.jai.Interpolation;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ScaleDescriptor;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.SignedByteBuffer;
import loci.formats.gui.SignedShortBuffer;
import loci.formats.gui.UnsignedIntBuffer;

/**
 * @author stephane
 */
public class IcyBufferedImage extends BufferedImage implements IcyColorModelListener, IcyChangedListener
{
    public enum FilterType
    {
        NEAREST, BILINEAR, BICUBIC
    };

    /**
     * used for getARGBImage method (fast conversion)
     */
    private static ARGBImageBuilder argbImageBuilder = new ARGBImageBuilder();

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
    public static IcyBufferedImage createFrom(List<BufferedImage> imageList)
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

        final int dataType = firstImage.getDataType();
        final boolean signed = firstImage.isSignedDataType();
        final int width = firstImage.getWidth();
        final int height = firstImage.getHeight();

        int numComponents = 0;
        // count total number of component
        for (IcyBufferedImage image : icyImageList)
        {
            if ((dataType != image.getDataType()) || (signed != image.isSignedDataType()))
                throw new IllegalArgumentException("All images contained in imageList should have the same dataType");
            if ((width != image.getWidth()) || (height != image.getHeight()))
                throw new IllegalArgumentException("All images contained in imageList should have the same dimension");

            numComponents += image.getNumComponents();
        }

        final IcyBufferedImage result = new IcyBufferedImage(width, height, numComponents, dataType, signed);
        // final IcyColorModel dstColorModel = result.getIcyColorModel();

        // copy data to result image
        int destComp = 0;
        for (IcyBufferedImage image : icyImageList)
        {
            // final IcyColorModel srcColorModel = image.getIcyColorModel();
            final int srcNumComponents = image.getNumComponents();

            for (int srcComp = 0; srcComp < srcNumComponents; srcComp++, destComp++)
            {
                // copy data
                ArrayUtil.arrayToArray(image.getDataXY(srcComp), result.getDataXY(destComp), signed);
                // copy colormap (not always wanted...)
                // dstColorModel.setColormap(destComp, srcColorModel.getColormap(srcComp));
            }
        }

        // notify data changed as we internally modified data
        result.dataChanged();

        return result;
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
    static public IcyBufferedImage convert(BufferedImage image)
    {
        return createFrom(image);
    }

    /**
     * Create an IcyBufferedImage from a BufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost
     * 
     * @param image
     *        {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     */
    static public IcyBufferedImage createFrom(BufferedImage image)
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

        // in some case we want to restore colorMap from source colorModel
        if ((type == BufferedImage.TYPE_BYTE_BINARY) || (type == BufferedImage.TYPE_BYTE_INDEXED)
                || (numComponents == 2))
            ((IcyColorSpace) result.getColorModel().getColorSpace()).copyColormaps(image.getColorModel());

        return result;
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage
     * 
     * @param reader
     *        {@link IFormatReader}
     * @return {@link IcyBufferedImage}
     */
    static public IcyBufferedImage createFrom(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        final boolean oldMethod = false;

        // use LOCI tools to handle indexed image
        if (oldMethod)
        {
            final List<BufferedImage> imageList = new ArrayList<BufferedImage>();
            final int sizeC = reader.getEffectiveSizeC();

            for (int c = 0; c < sizeC; c++)
                imageList.add(AWTImageTools.openImage(reader.openBytes(reader.getIndex(z, c, t)), reader,
                        reader.getSizeX(), reader.getSizeY()));

            // combine channels
            return createFrom(imageList);
        }

        // convert in our data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // prepare informations
        final int sizeX = reader.getSizeX();
        final int sizeY = reader.getSizeY();
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
                        colormaps[effC] = new IcyColorMap("component " + effC);
                        break;
                }
            }
        }

        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, data, dataType.isSigned());

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
                final IcyColorSpace colorSpace = result.getIcyColorModel().getIcyColorSpace();

                colorSpace.beginUpdate();
                try
                {
                    // copy colormap
                    for (int comp = 0; comp < sizeC; comp++)
                        if (colormaps[comp] != null)
                            colorSpace.copyColormap(comp, colormaps[comp]);
                }
                finally
                {
                    colorSpace.endUpdate();
                }
            }
        }

        return result;
    }

    /**
     * Create an empty IcyBufferedImage from a IcyColorModel type with specified size
     */
    static public IcyBufferedImage createEmptyImage(int width, int height, IcyColorModel cm)
    {
        return new IcyBufferedImage(width, height, cm.getNumComponents(), cm.getDataType_());
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
     */
    private IcyBufferedImage(IcyColorModel cm, WritableRaster wr)
    {
        super(cm, wr, false, null);

        // internal lut
        internalLut = new LUT(cm);

        updater = new UpdateEventHandler(this, false);
        listeners = new EventListenerList();

        // add listener to colorModel
        cm.addListener(this);
    }

    /**
     * Build an Icy formatted BufferedImage with specified IcyColorModel, width and height.
     */
    private IcyBufferedImage(IcyColorModel cm, int width, int height)
    {
        this(cm, cm.createCompatibleWritableRaster(width, height));
    }

    /**
     * Build an Icy formatted BufferedImage with specified IcyColorModel, data, width and height.
     */
    private IcyBufferedImage(IcyColorModel cm, Object[] data, int width, int height)
    {
        this(cm, cm.createWritableRaster(data, width, height));

        // data has been modified
        dataChanged();
    }

    /**
     * Build an Icy formatted BufferedImage with specified width, height and input data.<br>
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
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height);
    }

    /**
     * Build an Icy formatted BufferedImage with specified width, height and input data.<br>
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
     * Build an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType<br>
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
     * Draw the current {@link IcyBufferedImage} into the specified {@link BufferedImage} out<br>
     * If out is null then a new TYPE_INT_ARGB {@link BufferedImage} is returned.<br>
     * lut {@link LUT} is used for color calculation (internal lut is used if null).
     */
    public BufferedImage convertToBufferedImage(BufferedImage out, LUT lut)
    {
        if (out == null)
            return getARGBImage(lut, null);

        out.getGraphics().drawImage(getARGBImage(lut, null), 0, 0, null);

        return out;
    }

    /**
     * Draw the current {@link IcyBufferedImage} into the specified {@link BufferedImage} out<br>
     * If out is null then a new TYPE_INT_ARGB {@link BufferedImage} is returned
     */
    public BufferedImage convertToBufferedImage(BufferedImage out)
    {
        return convertToBufferedImage(out, null);
    }

    /**
     * Draw the current {@link IcyBufferedImage} into the specified ARGB {@link BufferedImage} out<br>
     * If out is null then a new ARGB {@link BufferedImage} is returned<br>
     * lut {@link LUT} is used for color calculation (internal lut is used if null).<br>
     * <br>
     * This function is faster than {@link #convertToBufferedImage(BufferedImage, LUT)} but the
     * output<br>
     * {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     */
    public BufferedImage getARGBImage(LUT lut, BufferedImage out)
    {
        // use internal lut when no specific lut
        if (lut == null)
            return argbImageBuilder.buildARGBImage(this, internalLut, out);

        return argbImageBuilder.buildARGBImage(this, lut, out);
    }

    /**
     * Draw the current {@link IcyBufferedImage} into the specified ARGB {@link BufferedImage} out<br>
     * If out is null then a new ARGB {@link BufferedImage} is returned<br>
     * <br>
     * This function is faster than {@link #convertToBufferedImage(BufferedImage)} but the output
     * {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     */
    public BufferedImage getARGBImage(BufferedImage out)
    {
        return getARGBImage(null, out);
    }

    /**
     * Convert the current {@link IcyBufferedImage} into a ARGB {@link BufferedImage}<br>
     * lut {@link LUT} is used for color calculation (internal lut is used if null).<br>
     * <br>
     * This function is faster than {@link #convertToBufferedImage(BufferedImage, LUT)} but the
     * output {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     */
    public BufferedImage getARGBImage(LUT lut)
    {
        return getARGBImage(lut, null);
    }

    /**
     * Convert the current {@link IcyBufferedImage} into a ARGB {@link BufferedImage}<br>
     * using the internal lut {@link LUT} for color calculation.<br>
     * <br>
     * This function is faster than {@link #convertToBufferedImage(BufferedImage)} but the
     * output {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     */
    public BufferedImage getARGBImage()
    {
        return getARGBImage(null, null);
    }

    /**
     * Return an image of specified type from current image<br>
     * Create a copy if conversion needed else return current image
     * 
     * @param dataType
     *        data type wanted
     * @param scaler
     *        scaler for scaling internal data during conversion
     * @return converted image
     */
    public IcyBufferedImage convertToType(DataType dataType, Scaler scaler)
    {
        final DataType srcDataType = getDataType_();

        // no conversion needed
        if ((srcDataType == dataType) && scaler.isNull())
            return this;
        // can't convert
        if ((srcDataType == DataType.UNDEFINED) || (dataType == DataType.UNDEFINED))
            return null;

        final boolean srcSigned = srcDataType.isSigned();
        final int numComponents = getNumComponents();
        final IcyBufferedImage result = new IcyBufferedImage(getWidth(), getHeight(), numComponents, dataType);

        for (int c = 0; c < numComponents; c++)
        {
            // no rescale ?
            if (scaler.isNull())
                // simple type change
                ArrayUtil.arrayToArray(getDataXY(c), result.getDataXY(c), srcSigned);
            else
            {
                // first we convert in double
                final double[] darray = Array1DUtil.arrayToDoubleArray(getDataXY(c), srcSigned);
                // then we scale data
                scaler.scale(darray);
                // and finally we convert in wanted datatype
                Array1DUtil.doubleArrayToArray(darray, result.getDataXY(c));
            }
        }

        // copy colormap from source image
        result.copyColormap(this);
        // notify we modified data
        result.dataChanged();

        return result;
    }

    /**
     * @deprecated use {@link #convertToType(DataType, Scaler)} instead
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, Scaler scaler)
    {
        return convertToType(DataType.getDataType(dataType, signed), scaler);
    }

    /**
     * Return an image of specified type from current image<br>
     * Create a copy if conversion needed else return current image
     * 
     * @param dataType
     *        data type wanted
     * @param rescale
     *        indicate if we want to scale data value according to data type range
     * @return converted image
     */
    public IcyBufferedImage convertToType(DataType dataType, boolean rescale)
    {
        final double boundsSrc[] = getGlobalComponentAbsBounds();
        final double boundsDst[];

        if (rescale)
            boundsDst = dataType.getDefaultBounds();
        else
            boundsDst = boundsSrc;

        // use scaler to scale data
        final Scaler scaler = new Scaler(boundsSrc[0], boundsSrc[1], boundsDst[0], boundsDst[1], false);

        return convertToType(dataType, scaler);
    }

    /**
     * @deprecated use {@link #convertToType(DataType, boolean)} instead
     */
    @Deprecated
    public IcyBufferedImage convertToType(int dataType, boolean signed, boolean rescale)
    {
        return convertToType(DataType.getDataType(dataType, signed), rescale);
    }

    /**
     * Return a {@link BufferedImage} containing color data of current image
     * 
     * @param lut
     *        lut used for color calculation (internal lut is used if null)
     * @param imageType
     *        wanted image type, only the following is accepted :<br>
     *        BufferedImage.TYPE_INT_ARGB<br>
     *        BufferedImage.TYPE_INT_RGB<br>
     *        BufferedImage.TYPE_BYTE_GRAY<br>
     * @return BufferedImage
     * @deprecated uses {@link #convertToBufferedImage(int, LUT)} instead
     */
    @Deprecated
    public BufferedImage convertToBufferedImage(LUT lut, int imageType)
    {
        if (imageType == BufferedImage.TYPE_INT_ARGB)
            return getARGBImage(lut);

        final BufferedImage outImg = new BufferedImage(getWidth(), getHeight(), imageType);

        convertToBufferedImage(outImg, lut);

        return outImg;
    }

    /**
     * Return a {@link BufferedImage} containing color data of current image
     * 
     * @param imageType
     *        wanted image type, only the following is accepted :<br>
     *        BufferedImage.TYPE_INT_ARGB<br>
     *        BufferedImage.TYPE_INT_RGB<br>
     *        BufferedImage.TYPE_BYTE_GRAY<br>
     * @param lut
     *        lut used for color calculation (internal lut is used if null)
     * @return BufferedImage
     */
    public BufferedImage convertToBufferedImage(int imageType, LUT lut)
    {
        if (imageType == BufferedImage.TYPE_INT_ARGB)
            return getARGBImage(lut);

        final BufferedImage outImg = new BufferedImage(getWidth(), getHeight(), imageType);

        convertToBufferedImage(outImg, lut);

        return outImg;
    }

    /**
     * Create and return a copy
     */
    public IcyBufferedImage getCopy()
    {
        // create a compatible image
        final IcyBufferedImage result = new IcyBufferedImage(getWidth(), getHeight(), getNumComponents(),
                getDataType_());
        // copy data from this image
        result.copyData(this);

        return result;
    }

    /**
     * Create a sub image from specified coordinates and with specified dimension
     * 
     * @see #IcyBufferedImage.getSubimage(int, int, int, int)
     */
    public IcyBufferedImage getSubImageCopy(int x, int y, int w, int h)
    {
        // adjust rectangle
        final Rectangle r = new Rectangle(x, y, w, h).intersection(getBounds());

        final DataType dataType = getDataType_();
        final boolean signed = dataType.isSigned();
        final int sizeC = getSizeC();
        final int dstSizeX = r.width;
        final int dstSizeY = r.height;

        final IcyBufferedImage result = new IcyBufferedImage(dstSizeX, dstSizeY, sizeC, dataType);

        final int srcSizeX = getSizeX();

        for (int c = 0; c < sizeC; c++)
        {
            final Object src = getDataXY(c);
            final Object dst = result.getDataXY(c);

            int srcOffset = getOffset(r.x, r.y);
            int dstOffset = 0;

            for (int curY = 0; curY < dstSizeY; curY++)
            {
                Array1DUtil.arrayToArray(src, srcOffset, dst, dstOffset, dstSizeX, signed);
                srcOffset += srcSizeX;
                dstOffset += dstSizeX;
            }
        }

        result.dataChanged();

        return result;

        // return convert(super.getSubimage(x, y, w, h));
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
     * @see #extractBand(int)
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

        return new IcyBufferedImage(getWidth(), getHeight(), new Object[] {getDataXY(c)}, isSignedDataType());
    }

    /**
     * Build a new 1 channel image (grey) from the specified image channel
     * 
     * @param channelNumber
     * @return IcyBufferedImage
     */
    public IcyBufferedImage extractChannel(int channelNumber)
    {
        final ArrayList<Integer> list = new ArrayList<Integer>();

        list.add(Integer.valueOf(channelNumber));

        return extractChannels(list);
    }

    /**
     * Build a new image from the specified image channels
     * 
     * @param channelNumbers
     * @return IcyBufferedImage
     */
    public IcyBufferedImage extractChannels(List<Integer> channelNumbers)
    {
        // create output
        final IcyBufferedImage output = new IcyBufferedImage(getWidth(), getHeight(), channelNumbers.size(),
                getDataType(), isSignedDataType());
        final int sizeC = getSizeC();

        // set data from specified band
        for (int i = 0; i < channelNumbers.size(); i++)
        {
            final int channel = channelNumbers.get(i).intValue();

            if (channel < sizeC)
                output.setDataXY(i, getDataXY(channel));
        }

        return output;
    }

    /**
     * Use {@link #extractChannel(int)} instead
     * 
     * @param bandNumber
     * @return IcyBufferedImage
     * @deprecated
     */
    @Deprecated
    public IcyBufferedImage extractBand(int bandNumber)
    {
        return extractChannel(bandNumber);
    }

    /**
     * Use {@link #extractChannels(List)} instead
     * 
     * @param bandNumbers
     * @return IcyBufferedImage
     * @deprecated
     */
    @Deprecated
    public IcyBufferedImage extractBands(List<Integer> bandNumbers)
    {
        return extractChannels(bandNumbers);
    }

    /**
     * Return a copy of the image with specified size<br>
     * 
     * @param resizeContent
     *        indicate if content should be resized or not (empty area are 0 filled)
     * @param xAlign
     *        horizontal image alignment (SwingConstants.LEFT / CENTER / RIGHT)<br>
     *        (used only if resizeContent is false)
     * @param yAlign
     *        vertical image alignment (SwingConstants.TOP / CENTER / BOTTOM)<br>
     *        (used only if resizeContent is false)
     * @param filterType
     *        filter method used for scale (used only if resizeContent is true)
     */
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign,
            FilterType filterType)
    {
        final IcyBufferedImage result;

        // no content resize ?
        if (!resizeContent)
        {
            final int xt;
            final int yt;

            // calculate translation values
            final int dx = width - getWidth();
            switch (xAlign)
            {
                default:
                case SwingConstants.LEFT:
                    xt = 0;
                    break;

                case SwingConstants.CENTER:
                    xt = dx / 2;
                    break;

                case SwingConstants.RIGHT:
                    xt = dx;
                    break;
            }

            final int dy = height - getHeight();
            switch (yAlign)
            {
                default:
                case SwingConstants.TOP:
                    yt = 0;
                    break;

                case SwingConstants.CENTER:
                    yt = dy / 2;
                    break;

                case SwingConstants.BOTTOM:
                    yt = dy;
                    break;
            }

            // create an empty image with specified size and current colormodel description
            result = IcyBufferedImage.createEmptyImage(width, height, getIcyColorModel());
            // copy data from current image to specified destination
            result.copyData(this, null, new Point(xt, yt));
        }
        else
        {

            final Float xScale = Float.valueOf((float) width / getWidth());
            final Float yScale = Float.valueOf((float) height / getHeight());
            final Interpolation interpolation;

            switch (filterType)
            {
                default:
                case NEAREST:
                    interpolation = Interpolation.getInstance(Interpolation.INTERP_NEAREST);
                    break;

                case BILINEAR:
                    interpolation = Interpolation.getInstance(Interpolation.INTERP_BILINEAR);
                    break;

                case BICUBIC:
                    interpolation = Interpolation.getInstance(Interpolation.INTERP_BICUBIC);
                    break;
            }

            // use JAI scaler (use a copy to avoid source alteration)
            final RenderedOp renderedOp = ScaleDescriptor.create(getCopy(), xScale, yScale, Float.valueOf(0f),
                    Float.valueOf(0f), interpolation, null);

            // JAI keep dataType and others stuff in their BufferedImage
            result = IcyBufferedImage.createFrom(renderedOp.getAsBufferedImage());
        }

        return result;
    }

    /**
     * Return a copy of the image with specified size<br>
     * By default the FilterType.BILINEAR is used as filter method if resizeContent is true
     * 
     * @param resizeContent
     *        indicate if content should be resized or not (empty area are 0 filled)
     * @param xAlign
     *        horizontal image alignment (SwingConstants.LEFT / CENTER / RIGHT)<br>
     *        (used only if resizeContent is false)
     * @param yAlign
     *        vertical image alignment (SwingConstants.TOP / CENTER / BOTTOM)<br>
     *        (used only if resizeContent is false)
     */
    public IcyBufferedImage getScaledCopy(int width, int height, boolean resizeContent, int xAlign, int yAlign)
    {
        return getScaledCopy(width, height, resizeContent, xAlign, yAlign, FilterType.BILINEAR);
    }

    /**
     * Return a copy of the image with specified size<br>
     * 
     * @param filterType
     *        filter method used for scale (used only if resizeContent is true)
     */
    public IcyBufferedImage getScaledCopy(int width, int height, FilterType filterType)
    {
        return getScaledCopy(width, height, true, 0, 0, filterType);
    }

    /**
     * Return a copy of the image with specified size<br>
     * By default the FilterType.BILINEAR is used as filter method
     */
    public IcyBufferedImage getScaledCopy(int width, int height)
    {
        return getScaledCopy(width, height, FilterType.BILINEAR);
    }

    /**
     * Translate internal data of specified component by the specified (x,y) vector<br>
     * data going "outside" the image is lost
     */
    public void translate(int dx, int dy, int comp)
    {
        // nothing to do
        if ((dx == 0) && (dy == 0))
            return;

        final int sizeX = getSizeX();
        final int sizeY = getSizeY();

        // limit to sizeX / sizeY
        final int adx = Math.min(Math.max(-sizeX, dx), sizeX);
        final int ady = Math.min(Math.max(-sizeY, dy), sizeY);

        final int fromFill;
        final int toFill;
        final int fromCopy;
        final int toCopy;
        final int wCopy;

        if (adx < 0)
        {
            fromCopy = -adx;
            toCopy = 0;
            wCopy = sizeX + adx;
            fromFill = wCopy;
            toFill = sizeX;
        }
        else
        {
            fromFill = 0;
            toFill = adx;
            fromCopy = fromFill;
            toCopy = toFill;
            wCopy = sizeX - adx;
        }

        final Object data = getDataXY(comp);

        if (ady < 0)
        {
            final int hCopy = sizeY + ady;
            int toOffset = 0;
            int fromOffset = -ady * sizeX;

            // copy
            for (int y = 0; y < hCopy; y++)
            {
                // copy first
                ArrayUtil.innerCopy(data, fromOffset + fromCopy, toOffset + toCopy, wCopy);
                // then fill
                ArrayUtil.fill(data, toOffset + fromFill, toOffset + toFill, 0d);
                // adjust offset
                fromOffset += sizeX;
                toOffset += sizeX;
            }
            // fill
            ArrayUtil.fill(data, toOffset, fromOffset, 0d);
        }
        else
        {
            final int hCopy = sizeY - ady;
            int toOffset = (sizeY - 1) * sizeX;
            int fromOffset = toOffset - (ady * sizeX);

            // copy
            for (int y = 0; y < hCopy; y++)
            {
                // copy first
                ArrayUtil.innerCopy(data, fromOffset + fromCopy, toOffset + toCopy, wCopy);
                // then fill
                ArrayUtil.fill(data, toOffset + fromFill, toOffset + toFill, 0d);
                // adjust offset
                fromOffset -= sizeX;
                toOffset -= sizeX;
            }
            // fill
            ArrayUtil.fill(data, 0, 0 + (ady * sizeX), 0d);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Translate internals data (all components) by the specified (x,y) vector<br>
     * data going "outside" the image is lost
     */
    public void translate(int dx, int dy)
    {
        final int sizeC = getSizeC();

        beginUpdate();
        try
        {
            for (int c = 0; c < sizeC; c++)
                translate(dx, dy, c);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Get calculated image component bounds (min and max values)
     */
    private double[] getCalculatedComponentBounds(int component, boolean adjustByteToo)
    {
        final DataType dataType = getDataType_();

        // return default bounds ([0..255] / [-128..127]) for BYTE data type
        if ((!adjustByteToo) && (dataType.getJavaType() == DataType.BYTE))
            return dataType.getDefaultBounds();

        final boolean signed = dataType.isSigned();
        final Object data = getDataXY(component);

        final double min = ArrayMath.min(data, signed);
        final double max = ArrayMath.max(data, signed);

        return new double[] {min, max};
    }

    /**
     * Adjust specified bounds depending internal data type
     */
    private double[] adjustComponentBounds(double[] bounds)
    {
        double min, max;

        min = bounds[0];
        max = bounds[1];

        // we force min to 0 if > 0
        if (min > 0d)
            min = 0d;
        // we force max to 0 if < 0
        if (max < 0d)
            max = 0d;

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
     * Get global components absolute bounds (min and max values for all components)
     */
    public double[] getGlobalComponentAbsBounds()
    {
        final int numComponents = getNumComponents();
        final double[] result = getComponentAbsBounds(0);

        for (int component = 1; component < numComponents; component++)
        {
            final double[] bounds = getComponentAbsBounds(component);
            result[0] = Math.min(bounds[0], result[0]);
            result[1] = Math.max(bounds[1], result[1]);
        }

        return result;
    }

    /**
     * Get components absolute bounds (min and max values)
     */
    public double[][] getComponentsAbsBounds()
    {
        final int numComponents = getNumComponents();
        final double[][] result = new double[numComponents][];

        for (int component = 0; component < numComponents; component++)
            result[component] = getComponentAbsBounds(component);

        return result;
    }

    /**
     * Get component absolute minimum value
     */
    public double getComponentAbsMinValue(int component)
    {
        return getIcyColorModel().getComponentAbsMinValue(component);
    }

    /**
     * Get component absolute maximum value
     */
    public double getComponentAbsMaxValue(int component)
    {
        return getIcyColorModel().getComponentAbsMaxValue(component);
    }

    /**
     * Get component absolute bounds (min and max values)
     */
    public double[] getComponentAbsBounds(int component)
    {
        return getIcyColorModel().getComponentAbsBounds(component);
    }

    /**
     * Get components user bounds (min and max values)
     */
    public double[][] getComponentsUserBounds()
    {
        final int numComponents = getNumComponents();
        final double[][] result = new double[numComponents][];

        for (int component = 0; component < numComponents; component++)
            result[component] = getComponentUserBounds(component);

        return result;
    }

    /**
     * Get component user minimum value
     */
    public double getComponentUserMinValue(int component)
    {
        return getIcyColorModel().getComponentUserMinValue(component);
    }

    /**
     * Get component user maximum value
     */
    public double getComponentUserMaxValue(int component)
    {
        return getIcyColorModel().getComponentUserMaxValue(component);
    }

    /**
     * Get component user bounds (min and max values)
     */
    public double[] getComponentUserBounds(int component)
    {
        return getIcyColorModel().getComponentUserBounds(component);
    }

    /**
     * Set component absolute minimum value
     */
    public void setComponentAbsMinValue(int component, double min)
    {
        getIcyColorModel().setComponentAbsMinValue(component, min);
    }

    /**
     * Set component absolute maximum value
     */
    public void setComponentAbsMaxValue(int component, double max)
    {
        getIcyColorModel().setComponentAbsMaxValue(component, max);
    }

    /**
     * Set component absolute bounds (min and max values)
     */
    public void setComponentAbsBounds(int component, double[] bounds)
    {
        getIcyColorModel().setComponentAbsBounds(component, bounds);
    }

    /**
     * Set component absolute bounds (min and max values)
     */
    public void setComponentAbsBounds(int component, double min, double max)
    {
        getIcyColorModel().setComponentAbsBounds(component, min, max);
    }

    /**
     * Set components absolute bounds (min and max values)
     */
    public void setComponentsAbsBounds(double[][] bounds)
    {
        getIcyColorModel().setComponentsAbsBounds(bounds);
    }

    /**
     * Set component user minimum value
     */
    public void setComponentUserMinValue(int component, double min)
    {
        getIcyColorModel().setComponentUserMinValue(component, min);
    }

    /**
     * Set component user maximum value
     */
    public void setComponentUserMaxValue(int component, double max)
    {
        getIcyColorModel().setComponentUserMaxValue(component, max);
    }

    /**
     * Set component user bounds (min and max values)
     */
    public void setComponentUserBounds(int component, double[] bounds)
    {
        getIcyColorModel().setComponentUserBounds(component, bounds);
    }

    /**
     * Set component user bounds (min and max values)
     */
    public void setComponentUserBounds(int component, double min, double max)
    {
        getIcyColorModel().setComponentUserBounds(component, min, max);
    }

    /**
     * Set components user bounds (min and max values)
     */
    public void setComponentsUserBounds(double[][] bounds)
    {
        getIcyColorModel().setComponentsUserBounds(bounds);
    }

    /**
     * Update components bounds (min and max values)
     */
    public void updateComponentsBounds(boolean updateUserBounds, boolean adjustByteToo)
    {
        final int numComponents = getNumComponents();
        final IcyColorModel cm = getIcyColorModel();

        for (int component = 0; component < numComponents; component++)
        {
            final double[] bounds = getCalculatedComponentBounds(component, adjustByteToo);

            cm.setComponentAbsBounds(component, adjustComponentBounds(bounds));

            if (updateUserBounds)
            {
                final IcyColorModel colorModel = getIcyColorModel();

                if (colorModel != null)
                {
                    final IcyColorMap colorMap = colorModel.getColormap(component);

                    // we do user bounds adjustment on "non ALPHA" component only
                    if (colorMap.getType() != IcyColorMapType.ALPHA)
                        cm.setComponentUserBounds(component, bounds);
                }
            }
        }
    }

    /**
     * Update components bounds (min and max values)
     */
    public void updateComponentsBounds(boolean updateUserBounds)
    {
        updateComponentsBounds(updateUserBounds, true);
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
     * Return the number of components
     * 
     * @return number of components
     */
    public int getNumComponents()
    {
        return getColorModel().getNumComponents();
    }

    /**
     * @return the number of components of this image
     */
    public int getSizeC()
    {
        return getNumComponents();
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
     * internal copy data from an icy image (notify data changed)
     * 
     * @param srcImage
     *        source icy image
     * @param srcRect
     *        source region
     * @param dstPt
     *        destination point
     * @param srcComp
     *        source component
     * @param dstComp
     *        destination component
     */
    private void fastCopyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcComp, int dstComp)
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

        final Object src = srcImage.getDataXY(srcComp);
        final Object dst = getDataXY(dstComp);

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
     * internal copy data from an compatible image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    private boolean compatibleCopyData(BufferedImage srcImage)
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
        final int numBands = sm.getNumBands();
        final int[] indices = sm.getBankIndices();
        final int[] band_offsets = sm.getBandOffsets();
        final int[] bank_offsets = src_db.getOffsets();
        final int scanlineStride_src = sm.getScanlineStride();
        final int scanlineStride_dst = getSizeX();
        final int pixelStride_src = sm.getPixelStride();
        final int maxX = Math.min(getSizeX(), sm.getWidth());
        final int maxY = Math.min(getSizeY(), sm.getHeight());
        final int decOffsetSrc = src_wr.getSampleModelTranslateX()
                + (src_wr.getSampleModelTranslateY() * scanlineStride_src);

        for (int band = 0; band < numBands; band++)
        {
            final int bank = indices[band];
            final int offset = band_offsets[band] + bank_offsets[bank] - decOffsetSrc;

            switch (getDataType_().getJavaType())
            {
                case BYTE:
                {
                    final byte[] src;
                    final byte[] dst = ((DataBufferByte) dst_db).getData(band);

                    // LOCI use its own structure
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

                    // LOCI use its own structure
                    if (src_db instanceof SignedShortBuffer)
                        src = ((SignedShortBuffer) src_db).getData(bank);
                    else if (src_db instanceof DataBufferShort)
                        src = ((DataBufferShort) src_db).getData(bank);
                    else
                        src = ((DataBufferUShort) src_db).getData(bank);

                    if (dst_db instanceof DataBufferShort)
                        dst = ((DataBufferShort) dst_db).getData(band);
                    else
                        dst = ((DataBufferUShort) dst_db).getData(band);

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
                    final int[] dst = ((DataBufferInt) dst_db).getData(band);

                    // LOCI use its own structure
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
                    final float[] dst = ((DataBufferFloat) dst_db).getData(band);

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
                    final double[] dst = ((DataBufferDouble) dst_db).getData(band);

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
     *        destination (assume [0,0] if null)
     * @param srcComp
     *        source component (-1 for all component)
     * @param dstComp
     *        destination component (only significant if source component != -1)
     */
    public void copyData(IcyBufferedImage srcImage, Rectangle srcRect, Point dstPt, int srcComp, int dstComp)
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
        if (srcComp == -1)
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
            fastCopyData(srcImage, adjSrcRect, adjDstPt, srcComp, dstComp);
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
        copyData(srcImage, srcRect, dstPt, -1, 0);
    }

    /**
     * Copy data from an image (notify data changed)
     * 
     * @param srcImage
     *        source image
     */
    public void copyData(BufferedImage srcImage)
    {
        if (srcImage == null)
            return;

        if (srcImage instanceof IcyBufferedImage)
            // use specific IcyBufferedImage data copy
            copyData((IcyBufferedImage) srcImage, null, null);
        else
        {
            // use intermediate data copy for compatible image
            if (!compatibleCopyData(srcImage))
            {
                // image not compatible, use generic (and slow) data copy
                srcImage.copyData(getRaster());
                // notify data changed
                dataChanged();
            }
        }
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
     * Copy colormap from an image
     * 
     * @param srcImage
     *        source image
     */
    public void copyColormap(BufferedImage srcImage)
    {
        getIcyColorModel().copyColormap(srcImage.getColorModel());
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
     * notify image component bounds has changed
     */
    public void componentBoundsChanged(int component)
    {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.BOUNDS_CHANGED, component));
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
                updateComponentsBounds(true, false);
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
                componentBoundsChanged(e.getComponent());
                break;
        }
    }
}
