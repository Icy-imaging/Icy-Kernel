/**
 * 
 */
package icy.image;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROI4D;
import icy.roi.ROI5D;
import icy.type.DataIterator;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;

import java.awt.Rectangle;
import java.util.NoSuchElementException;

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
    protected final ROI roi;

    protected int startX, endX;
    protected int startY, endY;
    protected int startC, endC;

    /**
     * internals
     */
    protected BooleanMask2D maskXY;
    protected int x, y, c;
    protected boolean done;
    protected Object data;

    /**
     * Create a new ImageData iterator to iterate data through the specified dimensions (inclusive).
     * 
     * @param image
     *        Image we want to iterate data from
     * @param startX
     *        start X position
     * @param endX
     *        end X position
     * @param startY
     *        start Y position
     * @param endY
     *        end Y position
     * @param startC
     *        start C position
     * @param endC
     *        end C position
     */
    public ImageDataIterator(IcyBufferedImage image, int startX, int endX, int startY, int endY, int startC, int endC)
    {
        super();

        this.image = image;
        this.roi = null;
        this.maskXY = null;

        if (image != null)
        {
            dataType = image.getDataType_();

            this.startX = Math.max(startX, 0);
            this.endX = Math.min(endX, image.getSizeX() - 1);
            this.startY = Math.max(startY, 0);
            this.endY = Math.min(endY, image.getSizeY() - 1);
            this.startC = Math.max(startC, 0);
            this.endC = Math.min(endC, image.getSizeC() - 1);
        }
        else
            dataType = DataType.UNDEFINED;

        // start iterator
        reset();
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified dimensions (inclusive).
     * 
     * @param image
     *        Image we want to iterate data from
     * @param startX
     *        start X position
     * @param endX
     *        end X position
     * @param startY
     *        start Y position
     * @param endY
     *        end Y position
     * @param c
     *        C position (channel) we want to iterate data
     */
    public ImageDataIterator(IcyBufferedImage image, int startX, int endX, int startY, int endY, int c)
    {
        this(image, startX, endX, startY, endY, c, c);
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
        this(image, 0, image.getSizeX() - 1, 0, image.getSizeY() - 1, c, c);
    }

    /**
     * Create a new ImageData iterator to iterate all data.
     * 
     * @param image
     *        Image we want to iterate data from
     */
    public ImageDataIterator(IcyBufferedImage image)
    {
        this(image, 0, image.getSizeX() - 1, 0, image.getSizeY() - 1, 0, image.getSizeC() - 1);
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified
     * <code>BooleanMask2D</code> and C dimension.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param maskXY
     *        BooleanMask2D defining the XY region to iterate
     * @param startC
     *        start C position
     * @param endC
     *        end C position
     */
    public ImageDataIterator(IcyBufferedImage image, BooleanMask2D maskXY, int startC, int endC)
    {
        super();

        this.image = image;
        this.roi = null;
        this.maskXY = maskXY;

        if (image != null)
        {
            dataType = image.getDataType_();

            final Rectangle bounds = maskXY.bounds.intersection(image.getBounds());

            startX = bounds.x;
            endX = bounds.x + (bounds.width - 1);
            startY = bounds.y;
            endY = bounds.y + (bounds.height - 1);

            this.startC = Math.max(startC, 0);
            this.endC = Math.min(endC, image.getSizeC() - 1);
        }
        else
            dataType = DataType.UNDEFINED;

        // start iterator
        reset();
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified
     * <code>BooleanMask2D</code> and channel.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param maskXY
     *        BooleanMask2D defining the XY region to iterate
     * @param c
     *        C position (channel) we want to iterate data
     */
    public ImageDataIterator(IcyBufferedImage image, BooleanMask2D maskXY, int c)
    {
        this(image, maskXY, c, c);
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified
     * <code>BooleanMask2D</code> and channel.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param maskXY
     *        BooleanMask2D defining the XY region to iterate
     */
    public ImageDataIterator(IcyBufferedImage image, BooleanMask2D maskXY)
    {
        this(image, maskXY, 0, image.getSizeC() - 1);
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified ROI.
     * 
     * @param image
     *        Image we want to iterate data from
     * @param roi
     *        ROI defining the region to iterate
     */
    public ImageDataIterator(IcyBufferedImage image, ROI roi)
    {
        super();

        this.image = image;
        this.roi = roi;
        maskXY = null;

        if (image != null)
        {
            dataType = image.getDataType_();

            if (roi instanceof ROI2D)
            {
                final ROI2D roi2d = (ROI2D) roi;

                maskXY = roi2d.getBooleanMask();

                final Rectangle bounds = maskXY.bounds.intersection(image.getBounds());

                startX = bounds.x;
                endX = bounds.x + (bounds.width - 1);
                startY = bounds.y;
                endY = bounds.y + (bounds.height - 1);

                final int roiC = roi2d.getC();

                if (roiC == -1)
                {
                    startC = 0;
                    endC = image.getSizeC() - 1;
                }
                else
                {
                    if ((roiC < 0) || (roiC >= image.getSizeC()))
                    {
                        startC = 0;
                        endC = -1;
                    }
                    else
                    {
                        startC = roiC;
                        endC = roiC;
                    }
                }
            }
            else if (roi instanceof ROI3D)
            {
                // final ROI3D roi3d = (ROI3D) roi;
                // final int roiC = roi3d.getC();

                // not yet supported
                startX = startY = startC = 0;
                endX = endY = endC = -1;
            }
            else if (roi instanceof ROI4D)
            {
                // final ROI4D roi4d = (ROI4D) roi;
                // final int roiC = roi4d.getC();

                // not yet supported
                startX = startY = startC = 0;
                endX = endY = endC = -1;
            }
            else if (roi instanceof ROI5D)
            {
                // final ROI5D roi5d = (ROI5D) roi;

                // not yet supported
                startX = startY = startC = 0;
                endX = endY = endC = -1;
            }
            else
            {
                startX = startY = startC = 0;
                endX = endY = endC = -1;
            }
        }
        else
            dataType = DataType.UNDEFINED;

        // start iterator
        reset();
    }

    @Override
    public void reset()
    {
        done = (image == null) || (startC > endC) || (startY > endY) || (startX > endX);

        if (!done)
        {
            c = startC;
            y = startY;
            x = startX - 1;

            // prepare XY data
            prepareDataXY();
            // set start position
            next();
        }
    }

    /**
     * Prepare data for XY iteration.
     */
    protected void prepareDataXY()
    {
        // if (roi instanceof ROI3D) {
        //
        // }

        data = image.getDataXY(c);
    }

    private boolean maskContains()
    {
        return maskXY.mask[(x - maskXY.bounds.x) + ((y - maskXY.bounds.y) * maskXY.bounds.width)];
    }

    @Override
    public void next()
    {
        if (maskXY != null)
        {
            // advance while ROI do not contains current point
            internalNext();
            while (!maskContains() && !done)
                internalNext();
        }
        else
            internalNext();
    }

    /**
     * Advance one position.
     */
    protected void internalNext()
    {
        if (++x > endX)
        {
            x = startX;

            if (++y > endY)
            {
                y = startY;

                if (++c > endC)
                    done = true;
                else
                    prepareDataXY();
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

        return Array1DUtil.getValue(data, image.getOffset(x, y), dataType);
    }

    @Override
    public void set(double value)
    {
        if (done)
            throw new NoSuchElementException(null);

        Array1DUtil.setValue(data, image.getOffset(x, y), dataType, value);
    }

    /**
     * Return current X position.
     */
    public int getPositionX()
    {
        return x;
    }

    /**
     * Return current Y position.
     */
    public int getPositionY()
    {
        return y;
    }

    /**
     * Return current C position.
     */
    public int getPositionC()
    {
        return c;
    }
}
