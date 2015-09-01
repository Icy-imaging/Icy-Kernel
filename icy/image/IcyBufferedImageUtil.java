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

import icy.image.lut.LUT;
import icy.math.Scaler;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.ArrayUtil;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.RotateDescriptor;
import javax.media.jai.operator.ScaleDescriptor;
import javax.swing.SwingConstants;

/**
 * {@link IcyBufferedImage} utilities class.<br>
 * You can find here tools to clone, manipulate the image data type, its size...
 * 
 * @author Stephane
 */
public class IcyBufferedImageUtil
{
    public static enum FilterType
    {
        NEAREST, BILINEAR, BICUBIC
    };

    /**
     * used for getARGBImage method (fast conversion)
     */
    private static ARGBImageBuilder argbImageBuilder = new ARGBImageBuilder();

    /**
     * @deprecated Use {@link IcyBufferedImage#createFrom(BufferedImage)} instead.
     */
    @Deprecated
    public static IcyBufferedImage toIcyBufferedImage(BufferedImage image)
    {
        return IcyBufferedImage.createFrom(image);
    }

    /**
     * @deprecated Use {@link IcyBufferedImage#createFrom(List)} instead.
     */
    @Deprecated
    public static IcyBufferedImage toIcyBufferedImage(List<BufferedImage> images)
    {
        return IcyBufferedImage.createFrom(images);
    }

    /**
     * Draw the source {@link IcyBufferedImage} into the destination {@link BufferedImage}<br>
     * If <code>dest</code> is null then a new TYPE_INT_ARGB {@link BufferedImage} is returned.<br>
     * 
     * @param source
     *        source image
     * @param dest
     *        destination image
     * @param lut
     *        {@link LUT} is used for color calculation (internal lut is used if null).
     */
    public static BufferedImage toBufferedImage(IcyBufferedImage source, BufferedImage dest, LUT lut)
    {
        if (dest == null)
            return getARGBImage(source, lut, null);

        // dest is ARGB, use it directly as destination for getARGBImage(..)
        if (dest.getType() == BufferedImage.TYPE_INT_ARGB)
            getARGBImage(source, lut, dest);

        // else we need to convert to wanted type...
        dest.getGraphics().drawImage(getARGBImage(source, lut, null), 0, 0, null);

        return dest;
    }

    /**
     * Draw the source {@link IcyBufferedImage} into the destination {@link BufferedImage}<br>
     * If <code>dest</code> is null then a new TYPE_INT_ARGB {@link BufferedImage} is returned.
     * 
     * @param source
     *        source image
     * @param dest
     *        destination image
     */
    public static BufferedImage toBufferedImage(IcyBufferedImage source, BufferedImage dest)
    {
        return toBufferedImage(source, dest, null);
    }

    /**
     * Convert the current {@link IcyBufferedImage} into a {@link BufferedImage} of the specified
     * type.
     * 
     * @param source
     *        source image
     * @param imageType
     *        wanted image type, only the following is accepted :<br>
     *        BufferedImage.TYPE_INT_ARGB<br>
     *        BufferedImage.TYPE_INT_RGB<br>
     *        BufferedImage.TYPE_BYTE_GRAY<br>
     * @param lut
     *        lut used for color calculation (source image lut is used if null)
     * @return BufferedImage
     */
    public static BufferedImage toBufferedImage(IcyBufferedImage source, int imageType, LUT lut)
    {
        if (source == null)
            return null;

        if (imageType == BufferedImage.TYPE_INT_ARGB)
            return getARGBImage(source, lut, null);

        final BufferedImage outImg = new BufferedImage(source.getWidth(), source.getHeight(), imageType);

        toBufferedImage(source, outImg, lut);

        return outImg;
    }

    /**
     * Draw the source {@link IcyBufferedImage} into the destination ARGB {@link BufferedImage}<br>
     * If <code>dest</code> is null then a new ARGB {@link BufferedImage} is returned.<br>
     * This function is faster than {@link #toBufferedImage(IcyBufferedImage, BufferedImage, LUT)}
     * but the output {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     * 
     * @param source
     *        source image
     * @param dest
     *        destination image
     * @param lut
     *        {@link LUT} is used for color calculation (internal lut is used if null).
     */
    public static BufferedImage getARGBImage(IcyBufferedImage source, LUT lut, BufferedImage dest)
    {
        if (source == null)
            return null;

        // use image lut when no specific lut
        if (lut == null)
            return argbImageBuilder.buildARGBImage(source, source.getLUT(), dest);

        return argbImageBuilder.buildARGBImage(source, lut, dest);
    }

    /**
     * Draw the source {@link IcyBufferedImage} into the destination ARGB {@link BufferedImage}<br>
     * If <code>dest</code> is null then a new ARGB {@link BufferedImage} is returned.<br>
     * <br>
     * This function is faster than {@link #toBufferedImage(IcyBufferedImage, BufferedImage)} but
     * the output {@link BufferedImage} is fixed to ARGB type (TYPE_INT_ARGB)
     * 
     * @param source
     *        source image
     * @param dest
     *        destination image
     */
    public static BufferedImage getARGBImage(IcyBufferedImage source, BufferedImage dest)
    {
        return getARGBImage(source, null, dest);
    }

    /**
     * Convert the current {@link IcyBufferedImage} into a ARGB {@link BufferedImage}.
     * 
     * @param source
     *        source image
     * @param lut
     *        {@link LUT} is used for color calculation (internal lut is used if null).
     */
    public static BufferedImage getARGBImage(IcyBufferedImage source, LUT lut)
    {
        return getARGBImage(source, lut, null);
    }

    /**
     * Convert the current {@link IcyBufferedImage} into a ARGB {@link BufferedImage}.
     * 
     * @param source
     *        source image
     */
    public static BufferedImage getARGBImage(IcyBufferedImage source)
    {
        return getARGBImage(source, null, null);
    }

    /**
     * Convert the source image to the specified data type.<br>
     * This method returns a new image (the source image is not modified).
     * 
     * @param source
     *        source image
     * @param dataType
     *        data type wanted
     * @param scaler
     *        scaler for scaling internal data during conversion
     * @return converted image
     */
    public static IcyBufferedImage convertToType(IcyBufferedImage source, DataType dataType, Scaler scaler)
    {
        if (source == null)
            return null;

        final DataType srcDataType = source.getDataType_();

        // can't convert
        if ((srcDataType == DataType.UNDEFINED) || (dataType == DataType.UNDEFINED))
            return null;

        final boolean srcSigned = srcDataType.isSigned();
        final boolean dstSigned = dataType.isSigned();
        final int sizeC = source.getSizeC();
        final IcyBufferedImage result = new IcyBufferedImage(source.getSizeX(), source.getSizeY(), sizeC, dataType);

        for (int c = 0; c < sizeC; c++)
        {
            // no rescale ?
            if ((scaler == null) || scaler.isNull())
                // simple type change
                ArrayUtil.arrayToSafeArray(source.getDataXY(c), result.getDataXY(c), srcSigned, dstSigned);
            else
            {
                // first we convert in double
                final double[] darray = Array1DUtil.arrayToDoubleArray(source.getDataXY(c), srcSigned);
                // then we scale data
                scaler.scale(darray);
                // and finally we convert in wanted datatype
                Array1DUtil.doubleArrayToSafeArray(darray, result.getDataXY(c), dstSigned);
            }
        }

        // copy colormap from source image
        result.setColorMaps(source);
        // notify we modified data
        result.dataChanged();

        return result;
    }

    /**
     * Convert the source image to the specified data type.<br>
     * This method returns a new image (the source image is not modified).
     * 
     * @param dataType
     *        Data type wanted
     * @param rescale
     *        Indicate if we want to scale data value according to data (or data type) range
     * @param useDataBounds
     *        Only used when <code>rescale</code> parameter is true.<br>
     *        Specify if we use the data bounds for rescaling instead of data type bounds.
     * @return converted image
     */
    public static IcyBufferedImage convertToType(IcyBufferedImage source, DataType dataType, boolean rescale,
            boolean useDataBounds)
    {
        if (!rescale)
            return convertToType(source, dataType, null);

        // convert with rescale
        final double boundsSrc[];
        final double boundsDst[] = dataType.getDefaultBounds();

        if (useDataBounds)
            boundsSrc = source.getChannelsGlobalBounds();
        else
            boundsSrc = source.getChannelsGlobalTypeBounds();

        // use scaler to scale data
        return convertToType(source, dataType,
                new Scaler(boundsSrc[0], boundsSrc[1], boundsDst[0], boundsDst[1], false));
    }

    /**
     * Convert the source image to the specified data type.<br>
     * This method returns a new image (the source image is not modified).
     * 
     * @param dataType
     *        data type wanted
     * @param rescale
     *        indicate if we want to scale data value according to data type range
     * @return converted image
     */
    public static IcyBufferedImage convertToType(IcyBufferedImage source, DataType dataType, boolean rescale)
    {
        return convertToType(source, dataType, rescale, false);
    }

    /**
     * Create a copy of the specified image.
     */
    public static IcyBufferedImage getCopy(IcyBufferedImage source)
    {
        if (source == null)
            return null;

        // create a compatible image
        final IcyBufferedImage result = new IcyBufferedImage(source.getSizeX(), source.getSizeY(), source.getSizeC(),
                source.getDataType_());
        // copy data from this image
        result.copyData(source);

        return result;
    }

    /**
     * Creates a new image from the specified region of the source image.
     */
    public static IcyBufferedImage getSubImage(IcyBufferedImage source, Rectangle region, int c, int sizeC)
    {
        if (source == null)
            return null;

        final int startX;
        final int endX;
        final int startY;
        final int endY;
        final int startC;
        final int endC;

        // infinite X dimension ?
        if ((region.x == Integer.MIN_VALUE) && (region.width == Integer.MAX_VALUE))
        {
            startX = 0;
            endX = source.getSizeX();
        }
        else
        {
            startX = Math.max(0, region.x);
            endX = Math.min(source.getSizeX(), region.x + region.width);
        }
        // infinite Y dimension ?
        if ((region.y == Integer.MIN_VALUE) && (region.height == Integer.MAX_VALUE))
        {
            startY = 0;
            endY = source.getSizeY();
        }
        else
        {
            startY = Math.max(0, region.y);
            endY = Math.min(source.getSizeY(), region.y + region.height);
        }
        // infinite C dimension ?
        if ((c == Integer.MIN_VALUE) && (sizeC == Integer.MAX_VALUE))
        {
            startC = 0;
            endC = source.getSizeC();
        }
        else
        {
            startC = Math.max(0, c);
            endC = Math.min(source.getSizeC(), c + sizeC);
        }

        final int sizeX = endX - startX;
        final int sizeY = endY - startY;
        final int adjSizeC = endC - startC;

        if ((sizeX <= 0) || (sizeY <= 0) || (adjSizeC <= 0))
            return null;

        // adjust rectangle
        final DataType dataType = source.getDataType_();
        final boolean signed = dataType.isSigned();

        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, adjSizeC, dataType);

        final int srcSizeX = source.getSizeX();

        for (int ch = startC; ch < endC; ch++)
        {
            final Object src = source.getDataXY(ch);
            final Object dst = result.getDataXY(ch - startC);

            int srcOffset = source.getOffset(startX, startY);
            int dstOffset = 0;

            for (int curY = 0; curY < sizeY; curY++)
            {
                Array1DUtil.arrayToArray(src, srcOffset, dst, dstOffset, sizeX, signed);
                srcOffset += srcSizeX;
                dstOffset += sizeX;
            }
        }

        result.dataChanged();

        return result;
    }

    /**
     * Creates a new image which is a sub part of the source image from the specified region.
     * 
     * @see #getSubImage(IcyBufferedImage, Rectangle, int, int)
     */
    public static IcyBufferedImage getSubImage(IcyBufferedImage source, Rectangle region)
    {
        return getSubImage(source, region, 0, source.getSizeC());
    }

    /**
     * @deprecated Use {@link #getSubImage(IcyBufferedImage, Rectangle, int, int)} instead.
     */
    @Deprecated
    public static IcyBufferedImage getSubImage(IcyBufferedImage source, int x, int y, int c, int sizeX, int sizeY,
            int sizeC)
    {
        return getSubImage(source, new Rectangle(x, y, sizeX, sizeY), c, sizeC);
    }

    /**
     * Creates a new image which is a sub part of the source image from the specified
     * coordinates and dimensions.
     */
    public static IcyBufferedImage getSubImage(IcyBufferedImage source, int x, int y, int w, int h)
    {
        return getSubImage(source, new Rectangle(x, y, w, h), 0, source.getSizeC());
    }

    /**
     * Build a new single channel image (greyscale) from the specified source image channel.
     */
    public static IcyBufferedImage extractChannel(IcyBufferedImage source, int channel)
    {
        return extractChannels(source, channel);
    }

    /**
     * @deprecated Use {@link #extractChannels(IcyBufferedImage, List)} instead.
     */
    @Deprecated
    public static IcyBufferedImage extractChannels(IcyBufferedImage source, List<Integer> channelNumbers)
    {
        if (source == null)
            return null;

        // create output
        final IcyBufferedImage result = new IcyBufferedImage(source.getSizeX(), source.getSizeY(),
                channelNumbers.size(), source.getDataType_());
        final int sizeC = source.getSizeC();

        // set data from specified band
        for (int i = 0; i < channelNumbers.size(); i++)
        {
            final int channel = channelNumbers.get(i).intValue();

            if (channel < sizeC)
                result.setDataXY(i, source.getDataXY(channel));
        }

        return result;
    }

    /**
     * Build a new image from the specified source image channels.
     */
    public static IcyBufferedImage extractChannels(IcyBufferedImage source, int... channels)
    {
        if ((source == null) || (channels == null) || (channels.length == 0))
            return null;

        // create output
        final IcyBufferedImage result = new IcyBufferedImage(source.getSizeX(), source.getSizeY(), channels.length,
                source.getDataType_());
        final int sizeC = source.getSizeC();

        // set data from specified band
        for (int i = 0; i < channels.length; i++)
        {
            final int channel = channels[i];

            if (channel < sizeC)
                result.setDataXY(i, source.getDataXY(channel));
        }

        return result;
    }

    /**
     * Add empty channel(s) to the specified image and return result as a new image.
     * 
     * @param source
     *        source image.
     * @param index
     *        position where we want to add channel(s).
     * @param num
     *        number of channel(s) to add.
     */
    public static IcyBufferedImage addChannels(IcyBufferedImage source, int index, int num)
    {
        if (source == null)
            return null;

        final IcyBufferedImage result = new IcyBufferedImage(source.getSizeX(), source.getSizeY(), source.getSizeC()
                + num, source.getDataType_());

        for (int c = 0; c < index; c++)
            result.copyData(source, c, c);
        for (int c = index; c < source.getSizeC(); c++)
            result.copyData(source, c, c + num);

        return result;
    }

    /**
     * Add an empty channel to the specified image and return result as a new image.
     * 
     * @param source
     *        source image.
     * @param index
     *        position where we want to add channel.
     */
    public static IcyBufferedImage addChannel(IcyBufferedImage source, int index)
    {
        return addChannels(source, index, 1);
    }

    /**
     * Add an empty channel to the specified image and return result as a new image.
     * 
     * @param source
     *        source image.
     */
    public static IcyBufferedImage addChannel(IcyBufferedImage source)
    {
        return addChannels(source, source.getSizeC(), 1);
    }

    /**
     * Return a rotated version of the source image with specified parameters.
     * 
     * @param source
     *        source image
     * @param xOrigin
     *        X origin for the rotation
     * @param yOrigin
     *        Y origin for the rotation
     * @param angle
     *        rotation angle in radian
     * @param filterType
     *        filter resampling method used
     */
    public static IcyBufferedImage rotate(IcyBufferedImage source, double xOrigin, double yOrigin, double angle,
            FilterType filterType)
    {
        if (source == null)
            return null;

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
        final RenderedOp renderedOp = RotateDescriptor.create(getCopy(source), Float.valueOf((float) xOrigin), Float
                .valueOf((float) yOrigin), Float.valueOf((float) angle), interpolation, null, new RenderingHints(
                JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_ZERO)));

        return IcyBufferedImage.createFrom(renderedOp, source.isSignedDataType());
    }

    /**
     * Return a rotated version of the source image with specified parameters.
     * 
     * @param source
     *        source image
     * @param angle
     *        rotation angle in radian
     * @param filterType
     *        filter resampling method used
     */
    public static IcyBufferedImage rotate(IcyBufferedImage source, double angle, FilterType filterType)
    {
        if (source == null)
            return null;

        return rotate(source, source.getSizeX() / 2d, source.getSizeY() / 2d, angle, filterType);
    }

    /**
     * Return a rotated version of the source image with specified parameters.
     * 
     * @param source
     *        source image
     * @param angle
     *        rotation angle in radian
     */
    public static IcyBufferedImage rotate(IcyBufferedImage source, double angle)
    {
        if (source == null)
            return null;

        return rotate(source, source.getSizeX() / 2d, source.getSizeY() / 2d, angle, FilterType.BILINEAR);
    }

    /**
     * Return a copy of the source image with specified size, alignment rules and filter type.
     * 
     * @param source
     *        source image
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
    public static IcyBufferedImage scale(IcyBufferedImage source, int width, int height, boolean resizeContent,
            int xAlign, int yAlign, FilterType filterType)
    {
        if (source == null)
            return null;

        final IcyBufferedImage result;

        // no content resize ?
        if (!resizeContent)
        {
            final int xt;
            final int yt;

            // calculate translation values
            final int dx = width - source.getWidth();
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

            final int dy = height - source.getHeight();
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
            result = new IcyBufferedImage(width, height, source.getIcyColorModel());
            // copy data from current image to specified destination
            result.copyData(source, null, new Point(xt, yt));
        }
        else
        {
            final Float xScale = Float.valueOf((float) width / source.getWidth());
            final Float yScale = Float.valueOf((float) height / source.getHeight());
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
            final RenderedOp renderedOp = ScaleDescriptor.create(
                    getCopy(source),
                    xScale,
                    yScale,
                    Float.valueOf(0f),
                    Float.valueOf(0f),
                    interpolation,
                    new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender
                            .createInstance(BorderExtender.BORDER_COPY)));

            result = IcyBufferedImage.createFrom(renderedOp, source.isSignedDataType());
        }

        return result;
    }

    /**
     * Return a copy of the image with specified size.<br>
     * By default the FilterType.BILINEAR is used as filter method if resizeContent is true
     * 
     * @param source
     *        source image
     * @param resizeContent
     *        indicate if content should be resized or not (empty area are 0 filled)
     * @param xAlign
     *        horizontal image alignment (SwingConstants.LEFT / CENTER / RIGHT)<br>
     *        (used only if resizeContent is false)
     * @param yAlign
     *        vertical image alignment (SwingConstants.TOP / CENTER / BOTTOM)<br>
     *        (used only if resizeContent is false)
     */
    public static IcyBufferedImage scale(IcyBufferedImage source, int width, int height, boolean resizeContent,
            int xAlign, int yAlign)
    {
        return scale(source, width, height, resizeContent, xAlign, yAlign, FilterType.BILINEAR);
    }

    /**
     * Return a copy of the image with specified size<br>
     * 
     * @param source
     *        source image
     * @param filterType
     *        filter method used for scale (used only if resizeContent is true)
     */
    public static IcyBufferedImage scale(IcyBufferedImage source, int width, int height, FilterType filterType)
    {
        return scale(source, width, height, true, 0, 0, filterType);
    }

    /**
     * Return a copy of the image with specified size.<br>
     * By default the FilterType.BILINEAR is used as filter method.
     */
    public static IcyBufferedImage scale(IcyBufferedImage source, int width, int height)
    {
        return scale(source, width, height, FilterType.BILINEAR);
    }

    /**
     * Translate image internal data of specified channel by the specified (x,y) vector.<br>
     * Data going "outside" image bounds is lost.
     */
    public static void translate(IcyBufferedImage source, int dx, int dy, int channel)
    {
        if (source == null)
            return;

        // nothing to do
        if ((dx == 0) && (dy == 0))
            return;

        final int sizeX = source.getSizeX();
        final int sizeY = source.getSizeY();

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

        final Object data = source.getDataXY(channel);

        if (ady < 0)
        {
            final int hCopy = sizeY + ady;
            int toOffset = 0;
            int fromOffset = -ady * sizeX;

            // copy
            for (int y = 0; y < hCopy; y++)
            {
                // copy first
                Array1DUtil.innerCopy(data, fromOffset + fromCopy, toOffset + toCopy, wCopy);
                // then fill
                Array1DUtil.fill(data, toOffset + fromFill, toOffset + toFill, 0d);
                // adjust offset
                fromOffset += sizeX;
                toOffset += sizeX;
            }
            // fill
            Array1DUtil.fill(data, toOffset, fromOffset, 0d);
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
                Array1DUtil.innerCopy(data, fromOffset + fromCopy, toOffset + toCopy, wCopy);
                // then fill
                Array1DUtil.fill(data, toOffset + fromFill, toOffset + toFill, 0d);
                // adjust offset
                fromOffset -= sizeX;
                toOffset -= sizeX;
            }
            // fill
            Array1DUtil.fill(data, 0, 0 + (ady * sizeX), 0d);
        }

        // notify data changed
        source.dataChanged();
    }

    /**
     * Translate image internal data (all channels) by the specified (x,y) vector.<br>
     * Data going "outside" image bounds is lost.
     */
    public static void translate(IcyBufferedImage source, int dx, int dy)
    {
        if (source != null)
        {
            final int sizeC = source.getSizeC();

            source.beginUpdate();
            try
            {
                for (int c = 0; c < sizeC; c++)
                    translate(source, dx, dy, c);
            }
            finally
            {
                source.endUpdate();
            }
        }
    }
}
