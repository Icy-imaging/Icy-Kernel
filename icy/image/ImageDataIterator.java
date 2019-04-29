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
package icy.image;

import java.awt.Rectangle;
import java.util.NoSuchElementException;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.type.DataIterator;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;

/**
 * Image data iterator.<br>
 * This class permit to use simple iterator to read / write <code>IcyBufferedImage</code> data<br>
 * as double in XYC <i>([C[Y[X]]])</i> dimension order .<br>
 * Whatever is the internal {@link DataType} data is returned and set as double.<br>
 * <b>If the image size or type is modified during iteration the iterator
 * becomes invalid and can causes exception to happen.</b>
 * 
 * @author Stephane
 */
public class ImageDataIterator implements DataIterator
{
    protected final IcyBufferedImage image;
    protected final DataType dataType;

    /**
     * internals
     */
    protected final BooleanMask2D mask;
    protected final Rectangle regionBounds;
    protected final Rectangle imageBounds;
    protected final Rectangle finalBounds;
    protected final int c;
    protected final int w, h;
    protected int x, y;
    protected int imgOff;
    protected int maskOff;
    protected boolean changed;
    protected boolean done;
    protected Object data;

    /**
     * Create a new ImageData iterator to iterate data through the specified XY region and channel.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param XYBounds
     *        XY region to iterate (inclusive).
     * @param channel
     *        channel (C position) we want to iterate data
     */
    protected ImageDataIterator(IcyBufferedImage image, Rectangle boundsXY, BooleanMask2D maskXY, int channel)
    {
        super();

        if (maskXY != null)
            regionBounds = maskXY.bounds;
        else
            regionBounds = boundsXY;

        this.image = image;
        this.mask = maskXY;

        if (image != null)
        {
            imageBounds = image.getBounds();
            dataType = image.getDataType_();
            c = channel;
            // retain data while we are iterating over image data
            image.lockRaster();
        }
        else
        {
            imageBounds = new Rectangle();
            dataType = DataType.UBYTE;
            c = 0;
        }

        finalBounds = regionBounds.intersection(imageBounds);

        // cached
        w = finalBounds.width;
        h = finalBounds.height;

        changed = false;

        // start iterator
        reset();
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified XY region and channel.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param boundsXY
     *        XY region to iterate (inclusive).
     * @param channel
     *        channel (C position) we want to iterate data
     */
    public ImageDataIterator(IcyBufferedImage image, Rectangle boundsXY, int channel)
    {
        this(image, boundsXY, null, channel);
    }

    /**
     * @deprecated Use {@link #ImageDataIterator(IcyBufferedImage, Rectangle, int)} instead.
     */
    @Deprecated
    public ImageDataIterator(IcyBufferedImage image, int startX, int endX, int startY, int endY, int startC, int endC)
    {
        this(image, new Rectangle(startX, startY, (endX - startX) + 1, (endY - startY) + 1), startC);
    }

    /**
     * @deprecated Use {@link #ImageDataIterator(IcyBufferedImage, Rectangle, int)} instead.
     */
    @Deprecated
    public ImageDataIterator(IcyBufferedImage image, int startX, int endX, int startY, int endY, int c)
    {
        this(image, new Rectangle(startX, startY, (endX - startX) + 1, (endY - startY) + 1), c);
    }

    /**
     * Create a new ImageData iterator to iterate data of specified channel.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param c
     *        C position (channel) we want to iterate data
     */
    public ImageDataIterator(IcyBufferedImage image, int c)
    {
        this(image, image.getBounds(), c);
    }

    /**
     * @deprecated Use {@link #ImageDataIterator(IcyBufferedImage, int)} instead.<br>
     *             The <code>ImageDataIterator</code> iterate only on single channel data.
     */
    @Deprecated
    public ImageDataIterator(IcyBufferedImage image)
    {
        this(image, image.getBounds(), 0);
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified <code>BooleanMask2D</code> and C dimension.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param maskXY
     *        BooleanMask2D defining the XY region to iterate
     * @param channel
     *        channel (C position) we want to iterate data
     */
    public ImageDataIterator(IcyBufferedImage image, BooleanMask2D maskXY, int channel)
    {
        this(image, null, maskXY, channel);
    }

    /**
     * @deprecated Use {@link #ImageDataIterator(IcyBufferedImage, BooleanMask2D, int)} instead
     */
    @Deprecated
    public ImageDataIterator(IcyBufferedImage image, BooleanMask2D maskXY)
    {
        this(image, maskXY, 0);
    }

    /**
     * @deprecated Use {@link #ImageDataIterator(IcyBufferedImage, BooleanMask2D, int)} instead.
     *             You can use the {@link ROI#getBooleanMask2D(int, int, int, boolean)} method to
     *             retrieve the boolean mask from the ROI.
     */
    @Deprecated
    public ImageDataIterator(IcyBufferedImage image, ROI roi)
    {
        this(image, roi.getBooleanMask2D(0, 0, 0, false));
    }

    @Override
    protected void finalize() throws Throwable
    {
        flush();
        
        super.finalize();
    }

    public int getMinX()
    {
        return finalBounds.x;
    }

    public int getMaxX()
    {
        return (finalBounds.x + w) - 1;
    }

    public int getMinY()
    {
        return finalBounds.y;
    }

    public int getMaxY()
    {
        return (finalBounds.y + h) - 1;
    }

    @Override
    public void reset()
    {
        done = (image == null) || (c < 0) || (c >= image.getSizeC()) || finalBounds.isEmpty();

        if (!done)
        {
            // get data
            data = image.getDataXY(c);

            // reset position
            y = 0;
            x = -1;
            imgOff = (finalBounds.x - imageBounds.x) + ((finalBounds.y - imageBounds.y) * imageBounds.width);
            imgOff--;
            if (mask != null)
            {
                maskOff = (finalBounds.x - regionBounds.x) + ((finalBounds.y - regionBounds.y) * regionBounds.width);
                maskOff--;
            }
            // allow to correctly set initial position in boolean mask
            next();
        }
    }

    @Override
    public void next()
    {
        nextPosition();

        if (mask != null)
        {
            // advance while mask do not contains current point
            while (!done && !mask.mask[maskOff])
                nextPosition();
        }
    }

    /**
     * Advance one position
     */
    protected void nextPosition()
    {
        if (mask != null)
        {
            imgOff++;
            maskOff++;
            if (++x >= w)
            {
                x = 0;
                imgOff += imageBounds.width - finalBounds.width;
                maskOff += regionBounds.width - finalBounds.width;

                if (++y >= h)
                    done = true;
            }
        }
        else
        {
            imgOff++;
            if (++x >= w)
            {
                x = 0;
                imgOff += imageBounds.width - finalBounds.width;

                if (++y >= h)
                    done = true;
            }
        }
    }

    @Override
    public boolean done()
    {
        return done;
    }

    @Override
    public double get()
    {
        if (done)
            throw new NoSuchElementException(null);

        return Array1DUtil.getValue(data, imgOff, dataType);
    }

    @Override
    public void set(double value)
    {
        if (done)
            throw new NoSuchElementException(null);

        Array1DUtil.setValue(data, imgOff, dataType, value);
        changed = true;
    }

    /**
     * Returns current X position.
     */
    public int getX()
    {
        return finalBounds.x + x;
    }

    /**
     * Returns current Y position.
     */
    public int getY()
    {
        return finalBounds.y + y;
    }

    /**
     * Returns C position (fixed)
     */
    public int getC()
    {
        return c;
    }

    /**
     * @deprecated Use {@link #getX()} instead
     */
    @Deprecated
    public int getPositionX()
    {
        return getX();
    }

    /**
     * @deprecated Use {@link #getY()} instead
     */
    @Deprecated
    public int getPositionY()
    {
        return getY();
    }

    /**
     * @deprecated Use {@link #getC()} instead
     */
    @Deprecated
    public int getPositionC()
    {
        return getC();
    }

    public void flush()
    {
        // release image raster and save changes to cache
        if (image != null)
            image.releaseRaster(changed);
        changed = false;
    }
}
