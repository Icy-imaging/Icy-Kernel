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
package icy.sequence;

import icy.image.IcyBufferedImage;
import icy.image.ImageDataIterator;
import icy.roi.ROI;
import icy.type.DataIterator;
import icy.type.DataType;
import icy.type.rectangle.Rectangle5D;
import icy.type.rectangle.Rectangle5D.Integer;

import java.awt.Rectangle;
import java.util.NoSuchElementException;

/**
 * Sequence data iterator.<br>
 * This class permit to use simple iterator to read / write <code>Sequence</code> data<br>
 * as double in XYCZT <i>([T[Z[C[Y[X}}]]])</i> dimension order.<br>
 * Whatever is the internal {@link DataType} data is returned and set as double.<br>
 * <b>If the sequence size or type is modified during iteration the iterator
 * becomes invalid and can causes exception to happen.</b>
 * 
 * @author Stephane
 */
public class SequenceDataIterator implements DataIterator
{
    protected final Sequence sequence;
    protected final ROI roi;

    protected final Rectangle XYBounds;
    protected final int startC, endC;
    protected final int startZ, endZ;
    protected final int startT, endT;
    protected final boolean inclusive;

    /**
     * internals
     */
    protected int c, z, t;
    protected boolean done;
    protected ImageDataIterator imageIterator;

    /**
     * Create a new SequenceData iterator to iterate data through the specified 5D region
     * (inclusive).
     * 
     * @param sequence
     *        Sequence we want to iterate data from
     * @param bounds5D
     *        the 5D rectangular region we want to iterate
     */
    public SequenceDataIterator(Sequence sequence, Rectangle5D.Integer bounds5D)
    {
        super();

        this.sequence = sequence;
        roi = null;
        imageIterator = null;
        inclusive = true;

        if (sequence != null)
        {
            final Rectangle5D.Integer bounds = (Integer) bounds5D.createIntersection(sequence.getBounds5D());

            XYBounds = (Rectangle) bounds.toRectangle2D();

            startZ = bounds.z;
            endZ = (bounds.z + bounds.sizeZ) - 1;
            startT = bounds.t;
            endT = (bounds.t + bounds.sizeT) - 1;
            startC = bounds.c;
            endC = (bounds.c + bounds.sizeC) - 1;
        }
        else
        {
            XYBounds = null;
            startZ = 0;
            endZ = 0;
            startT = 0;
            endT = 0;
            startC = 0;
            endC = 0;
        }

        // start iterator
        reset();
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified dimensions
     * (inclusive).
     * 
     * @param sequence
     *        Sequence we want to iterate data from
     * @param XYBounds
     *        XY region to iterate
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     * @param c
     *        C position (channel) we want to iterate data
     */
    public SequenceDataIterator(Sequence sequence, Rectangle XYBounds, int z, int t, int c)
    {
        this(sequence, new Rectangle5D.Integer(XYBounds.x, XYBounds.y, z, t, c, (XYBounds.x + XYBounds.width) - 1,
                (XYBounds.y + XYBounds.height) - 1, 1, 1, 1));
    }

    /**
     * @deprecated Use {@link #SequenceDataIterator(Sequence, Rectangle5D.Integer)} instead
     */
    @Deprecated
    public SequenceDataIterator(Sequence sequence, int startX, int endX, int startY, int endY, int startC, int endC,
            int startZ, int endZ, int startT, int endT)
    {
        this(sequence, new Rectangle5D.Integer(startX, startY, startZ, startT, startC, (endX - startX) + 1,
                (endY - startY) + 1, (endZ - startZ) + 1, (endT - startT) + 1, (endC - startC) + 1));
    }

    /**
     * @deprecated Use {@link #SequenceDataIterator(Sequence, Rectangle, int, int, int)} instead
     */
    @Deprecated
    public SequenceDataIterator(Sequence sequence, int startX, int endX, int startY, int endY, int c, int z, int t)
    {
        this(sequence, new Rectangle5D.Integer(startX, startY, z, t, c, (endX - startX) + 1, (endY - startY) + 1, 1, 1,
                1));
    }

    /**
     * Create a new SequenceData iterator to iterate data of specified channel.
     * 
     * @param sequence
     *        Sequence we want to iterate data from
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     * @param c
     *        C position (channel) we want to iterate data
     */
    public SequenceDataIterator(Sequence sequence, int z, int t, int c)
    {
        this(sequence, new Rectangle5D.Integer(0, 0, z, t, c, sequence.getSizeX(), sequence.getSizeY(), 1, 1, 1));
    }

    /**
     * Create a new SequenceData iterator to iterate all data.
     * 
     * @param sequence
     *        Sequence we want to iterate data from.
     */
    public SequenceDataIterator(Sequence sequence)
    {
        this(sequence, new Rectangle5D.Integer(0, 0, 0, 0, 0, sequence.getSizeX(), sequence.getSizeY(),
                sequence.getSizeZ(), sequence.getSizeT(), sequence.getSizeC()));
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     * 
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     * @param z
     *        Z position overriding (set to -1 to use the ROI Z information).
     * @param t
     *        T position overriding (set to -1 to use the ROI T information).
     * @param c
     *        C position overriding (set to -1 to use the ROI C information).
     */
    public SequenceDataIterator(Sequence sequence, ROI roi, boolean inclusive, int z, int t, int c)
    {
        super();

        this.sequence = sequence;
        this.roi = roi;
        this.inclusive = inclusive;
        XYBounds = null;

        if ((sequence != null) && (roi != null))
        {
            Rectangle5D bounds5D = roi.getBounds5D();

            // force Z position
            if (z != -1)
            {
                bounds5D.setZ(z);
                bounds5D.setSizeZ(1d);
            }
            // force T position
            if (t != -1)
            {
                bounds5D.setT(t);
                bounds5D.setSizeT(1d);
            }
            // force C position
            if (c != -1)
            {
                bounds5D.setC(c);
                bounds5D.setSizeC(1d);
            }

            // get final bounds
            final Rectangle5D.Integer bounds = (Integer) sequence.getBounds5D().createIntersection(bounds5D);

            startZ = bounds.z;
            endZ = (bounds.z + bounds.sizeZ) - 1;
            startT = bounds.t;
            endT = (bounds.t + bounds.sizeT) - 1;
            startC = bounds.c;
            endC = (bounds.c + bounds.sizeC) - 1;
        }
        else
        {
            startZ = 0;
            endZ = 0;
            startT = 0;
            endT = 0;
            startC = 0;
            endC = 0;
        }

        // start iterator
        reset();
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     * 
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     */
    public SequenceDataIterator(Sequence sequence, ROI roi, boolean inclusive)
    {
        this(sequence, roi, inclusive, -1, -1, -1);
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     * 
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     */
    public SequenceDataIterator(Sequence sequence, ROI roi)
    {
        this(sequence, roi, false);
    }

    @Override
    public void reset()
    {
        done = (sequence == null) || (startT > endT) || (startZ > endZ) || (startC > endC);

        if (!done)
        {
            t = startT;
            z = startZ;
            c = startC;

            // prepare XY data
            prepareDataXY();
            nextImageifNeeded();
        }
    }

    /**
     * Prepare data for XY iteration.
     */
    protected void prepareDataXY()
    {
        final IcyBufferedImage img = sequence.getImage(t, z);

        // get the 2D mask for specified C
        if (roi != null)
            imageIterator = new ImageDataIterator(img, roi.getBooleanMask2D(z, t, c, inclusive), c);
        else
            imageIterator = new ImageDataIterator(img, XYBounds, c);
    }

    @Override
    public void next()
    {
        imageIterator.next();
        nextImageifNeeded();
    }

    /**
     * Advance one image position.
     */
    protected void nextImageifNeeded()
    {
        while (imageIterator.done() && !done)
        {
            if (++c > endC)
            {
                c = startC;

                if (++z > endZ)
                {
                    z = startZ;

                    if (++t > endT)
                    {
                        done = true;
                        return;
                    }
                }
            }

            prepareDataXY();
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

        return imageIterator.get();
    }

    @Override
    public void set(double value)
    {
        if (done)
            throw new NoSuchElementException(null);

        imageIterator.set(value);
    }

    /**
     * Return current X position.
     */
    public int getPositionX()
    {
        if (imageIterator != null)
            return imageIterator.getPositionX();

        return 0;
    }

    /**
     * Return current Y position.
     */
    public int getPositionY()
    {
        if (imageIterator != null)
            return imageIterator.getPositionY();

        return 0;
    }

    /**
     * Return current C position.
     */
    public int getPositionC()
    {
        return c;
    }

    /**
     * Return current Z position.
     */
    public int getPositionZ()
    {
        return z;
    }

    /**
     * Return current T position.
     */
    public int getPositionT()
    {
        return t;
    }

}
