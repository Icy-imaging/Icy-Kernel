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
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROI4D;
import icy.roi.ROI5D;
import icy.type.DataIterator;
import icy.type.DataType;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;

import java.awt.geom.Rectangle2D;
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

    protected int startX, endX;
    protected int startY, endY;
    protected int startC, endC;
    protected int startZ, endZ;
    protected int startT, endT;

    /**
     * internals
     */
    protected int z, t;
    protected boolean done;
    protected BooleanMask2D maskXY;
    protected ImageDataIterator imageIterator;

    /**
     * Create a new SequenceData iterator to iterate data through the specified dimensions
     * (inclusive).
     * 
     * @param sequence
     *        Sequence we want to iterate data from
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
     * @param startZ
     *        start Z position
     * @param endZ
     *        end Z position
     * @param startT
     *        start T position
     * @param endT
     *        end T position
     */
    public SequenceDataIterator(Sequence sequence, int startX, int endX, int startY, int endY, int startC, int endC,
            int startZ, int endZ, int startT, int endT)
    {
        super();

        this.sequence = sequence;
        this.roi = null;
        maskXY = null;
        imageIterator = null;

        if (sequence != null)
        {
            this.startX = Math.max(startX, 0);
            this.endX = Math.min(endX, sequence.getSizeX() - 1);
            this.startY = Math.max(startY, 0);
            this.endY = Math.min(endY, sequence.getSizeY() - 1);
            this.startC = Math.max(startC, 0);
            this.endC = Math.min(endC, sequence.getSizeC() - 1);
            this.startZ = Math.max(startZ, 0);
            this.endZ = Math.min(endZ, sequence.getSizeZ() - 1);
            this.startT = Math.max(startT, 0);
            this.endT = Math.min(endT, sequence.getSizeT() - 1);
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
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     */
    public SequenceDataIterator(Sequence sequence, int startX, int endX, int startY, int endY, int c, int z, int t)
    {
        this(sequence, startX, endX, startY, endY, c, c, z, z, t, t);
    }

    /**
     * Create a new SequenceData iterator to iterate data of specified channel.
     * 
     * @param sequence
     *        Sequence we want to iterate data from
     * @param c
     *        C position (channel) we want to iterate data
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     */
    public SequenceDataIterator(Sequence sequence, int c, int z, int t)
    {
        this(sequence, 0, sequence.getSizeX() - 1, 0, sequence.getSizeY() - 1, c, c, z, z, t, t);
    }

    /**
     * Create a new SequenceData iterator to iterate all data.
     * 
     * @param sequence
     *        Sequence we want to iterate data from.
     */
    public SequenceDataIterator(Sequence sequence)
    {
        this(sequence, 0, sequence.getSizeX() - 1, 0, sequence.getSizeY() - 1, 0, sequence.getSizeC() - 1, 0, sequence
                .getSizeZ() - 1, 0, sequence.getSizeT() - 1);
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
     * @param x
     *        X position overriding (set to -1 to use the ROI X information).
     * @param y
     *        Y position overriding (set to -1 to use the ROI Y information).
     * @param c
     *        C position overriding (set to -1 to use the ROI C information).
     * @param z
     *        Z position overriding (set to -1 to use the ROI Z information).
     * @param t
     *        T position overriding (set to -1 to use the ROI T information).
     */
    public SequenceDataIterator(Sequence sequence, ROI roi, boolean inclusive, int x, int y, int c, int z, int t)
    {
        super();

        this.sequence = sequence;
        this.roi = roi;
        maskXY = null;

        if (roi instanceof ROI5D)
        {
            final Rectangle5D b = ((ROI5D) roi).getBounds5D();

            startX = (int) Math.floor(b.getX());
            startY = (int) Math.floor(b.getY());
            startC = (int) Math.floor(b.getC());
            startZ = (int) Math.floor(b.getZ());
            startT = (int) Math.floor(b.getT());
            endX = startX + ((int) Math.ceil(b.getSizeX()) - 1);
            endY = startY + ((int) Math.ceil(b.getSizeY()) - 1);
            endC = startC + ((int) Math.ceil(b.getSizeC()) - 1);
            endZ = startZ + ((int) Math.ceil(b.getSizeZ()) - 1);
            endT = startT + ((int) Math.ceil(b.getSizeT()) - 1);
        }
        else if (roi instanceof ROI4D)
        {
            final ROI4D roi4d = (ROI4D) roi;
            final Rectangle4D b = roi4d.getBounds4D();
            final int roiC = (c == -1) ? roi4d.getC() : c;

            startX = (int) Math.floor(b.getX());
            startY = (int) Math.floor(b.getY());
            startZ = (int) Math.floor(b.getZ());
            startT = (int) Math.floor(b.getT());
            endX = startX + ((int) Math.ceil(b.getSizeX()) - 1);
            endY = startY + ((int) Math.ceil(b.getSizeY()) - 1);
            endZ = startZ + ((int) Math.ceil(b.getSizeZ()) - 1);
            endT = startT + ((int) Math.ceil(b.getSizeT()) - 1);

            if (roiC < 0)
            {
                startC = 0;
                endC = startC + (sequence.getSizeC() - 1);
            }
            else
            {
                startC = roiC;
                endC = roiC;
            }
        }
        else if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;
            final Rectangle3D b = roi3d.getBounds3D();
            final int roiC = (c == -1) ? roi3d.getC() : c;
            final int roiT = (t == -1) ? roi3d.getT() : t;

            startX = (int) Math.floor(b.getX());
            startY = (int) Math.floor(b.getY());
            startZ = (int) Math.floor(b.getZ());
            endX = startX + ((int) Math.ceil(b.getSizeX()) - 1);
            endY = startY + ((int) Math.ceil(b.getSizeY()) - 1);
            endZ = startZ + ((int) Math.ceil(b.getSizeZ()) - 1);

            if (roiC < 0)
            {
                startC = 0;
                endC = startC + (sequence.getSizeC() - 1);
            }
            else
            {
                startC = roiC;
                endC = roiC;
            }
            if (roiT < 0)
            {
                startT = 0;
                endT = startT + (sequence.getSizeT() - 1);
            }
            else
            {
                startT = roiT;
                endT = roiT;
            }
        }
        else if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;
            final Rectangle2D b = roi2d.getBounds2D();
            final int roiC = (c == -1) ? roi2d.getC() : c;
            final int roiZ = (z == -1) ? roi2d.getZ() : z;
            final int roiT = (t == -1) ? roi2d.getT() : t;

            startX = (int) Math.floor(b.getX());
            startY = (int) Math.floor(b.getY());
            endX = startX + ((int) Math.ceil(b.getWidth()) - 1);
            endY = startY + ((int) Math.ceil(b.getHeight()) - 1);

            if (roiC < 0)
            {
                startC = 0;
                endC = startC + (sequence.getSizeC() - 1);
            }
            else
            {
                startC = roiC;
                endC = roiC;
            }
            if (roiZ < 0)
            {
                startZ = 0;
                endZ = startZ + (sequence.getSizeZ() - 1);
            }
            else
            {
                startZ = roiZ;
                endZ = roiZ;
            }
            if (roiT < 0)
            {
                startT = 0;
                endT = startT + (sequence.getSizeT() - 1);
            }
            else
            {
                startT = roiT;
                endT = roiT;
            }
        }
        else
        {
            if (x < 0)
            {
                startX = 0;
                endX = startX + (sequence.getSizeX() - 1);
            }
            else
            {
                startX = x;
                endX = x;
            }
            if (y < 0)
            {
                startY = 0;
                endY = startY + (sequence.getSizeY() - 1);
            }
            else
            {
                startY = y;
                endY = y;
            }
            if (c < 0)
            {
                startC = 0;
                endC = startC + (sequence.getSizeC() - 1);
            }
            else
            {
                startC = c;
                endC = c;
            }
            if (z < 0)
            {
                startZ = 0;
                endZ = startZ + (sequence.getSizeZ() - 1);
            }
            else
            {
                startZ = z;
                endZ = z;
            }
            if (t < 0)
            {
                startT = 0;
                endT = startT + (sequence.getSizeT() - 1);
            }
            else
            {
                startT = t;
                endT = t;
            }
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
        this(sequence, roi, inclusive, -1, -1, -1, -1, -1);
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
        done = (sequence == null) || (startT > endT) || (startZ > endZ);

        if (!done)
        {
            t = startT;
            z = startZ;

            // prepare XYC data
            prepareDataXYC();
            nextImageifNeeded();
        }
    }

    /**
     * Prepare data for Z iteration.
     */
    protected boolean prepareDataXYC()
    {
        final IcyBufferedImage img = sequence.getImage(t, z);

        if (maskXY != null)
            imageIterator = new ImageDataIterator(img, maskXY, startC, endC);
        else if (roi != null)
            imageIterator = new ImageDataIterator(img, roi);
        else
            imageIterator = new ImageDataIterator(img, startX, endX, startY, endY, startC, endC);

        return imageIterator.done();
    }

    @Override
    public void next()
    {
        if (roi != null)
        {
            // advance while ROI do not contains current point
            internalNext();
            // while (!roiMask.contains(z, t) && !done)
            // internalNext();
        }
        else
            internalNext();
    }

    /**
     * Advance one position.
     */
    protected void internalNext()
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
            if (++z > endZ)
            {
                z = startZ;

                if (++t > endT)
                    done = true;
            }

            if (!done)
                prepareDataXYC();
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
        if (imageIterator != null)
            return imageIterator.getPositionC();

        return 0;
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
