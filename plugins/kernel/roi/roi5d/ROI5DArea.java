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
package plugins.kernel.roi.roi5d;

import icy.roi.BooleanMask4D;
import icy.roi.BooleanMask5D;
import icy.roi.ROI;
import icy.roi.ROI4D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;

import java.awt.geom.Point2D;
import java.util.Map.Entry;

import plugins.kernel.roi.roi4d.ROI4DArea;

/**
 * 5D Area ROI.
 * 
 * @author Stephane
 */
public class ROI5DArea extends ROI5DStack<ROI4DArea>
{
    public ROI5DArea()
    {
        super(ROI4DArea.class);

        setName("4D area");
    }

    public ROI5DArea(Point5D pt)
    {
        this();

        addBrush(pt.toPoint2D(), (int) pt.getZ(), (int) pt.getT(), (int) pt.getC());
    }

    /**
     * Create a 3D Area ROI type from the specified {@link BooleanMask5D}.
     */
    public ROI5DArea(BooleanMask5D mask)
    {
        this();

        setAsBooleanMask(mask);
    }

    /**
     * Create a copy of the specified 5D Area ROI.
     */
    public ROI5DArea(ROI5DArea area)
    {
        this();

        // copy the source 4D area ROI
        for (Entry<Integer, ROI4DArea> entry : area.slices.entrySet())
            slices.put(entry.getKey(), new ROI4DArea(entry.getValue()));

        roiChanged(true);
    }

    @Override
    public String getDefaultName()
    {
        return "Area5D";
    }

    /**
     * Adds the specified point to this ROI
     */
    public void addPoint(int x, int y, int z, int t, int c)
    {
        setPoint(x, y, z, t, c, true);
    }

    /**
     * Remove a point from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removePoint(int x, int y, int z, int t, int c)
    {
        setPoint(x, y, z, t, c, false);
    }

    /**
     * Set the value for the specified point in the mask.
     * Don't forget to call optimizeBounds() after consecutive remove point operation
     * to refresh the mask bounds.
     */
    public void setPoint(int x, int y, int z, int t, int c, boolean value)
    {
        final ROI4DArea slice = getSlice(c, value);

        if (slice != null)
            slice.setPoint(x, y, z, t, value);
    }

    /**
     * Add brush point at specified position and for specified Z,T,C slice.
     */
    public void addBrush(Point2D pos, int z, int t, int c)
    {
        getSlice(c, true).addBrush(pos, z, t);
    }

    /**
     * Remove brush point from the mask at specified position and for specified Z,T,C slice.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removeBrush(Point2D pos, int z, int t, int c)
    {
        final ROI4DArea slice = getSlice(c, false);

        if (slice != null)
            slice.removeBrush(pos, z, t);
    }

    /**
     * Sets the ROI slice at given C position to this 5D ROI
     * 
     * @param c
     *        the position where the slice must be set
     * @param roiSlice
     *        the 4D ROI to set
     * @param merge
     *        <code>true</code> if the given slice should be merged with the existing slice, or
     *        <code>false</code> to
     *        replace the existing slice.
     */
    public void setSlice(int c, ROI4D roiSlice, boolean merge)
    {
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot add an empty slice in a 5D ROI");

        final ROI4DArea currentSlice = getSlice(c);
        final ROI newSlice;

        // merge both slice
        if ((currentSlice != null) && merge)
        {
            // we need to modify the C position so we do the merge correctly
            roiSlice.setC(c);
            // do ROI union
            newSlice = currentSlice.getUnion(roiSlice);
        }
        else
            newSlice = roiSlice;

        if (newSlice instanceof ROI4DArea)
            setSlice(c, (ROI4DArea) newSlice);
        else if (newSlice instanceof ROI4D)
            setSlice(c, new ROI4DArea(((ROI4D) newSlice).getBooleanMask(true)));
        else
            throw new IllegalArgumentException(
                    "Can't add the result of the merge operation on 4D slice " + c + ": " + newSlice.getClassName());
    }

    /**
     * Returns true if the ROI is empty (the mask does not contains any point).
     */
    @Override
    public boolean isEmpty()
    {
        for (ROI4DArea area : slices.values())
            if (!area.isEmpty())
                return false;

        return true;
    }

    /**
     * Set the mask from a BooleanMask5D object<br>
     * If specified mask is <i>null</i> then ROI is cleared.
     */
    public void setAsBooleanMask(BooleanMask5D mask)
    {
        // mask empty ? --> just clear the ROI
        if ((mask == null) || mask.isEmpty())
            clear();
        else
        {
            final Rectangle5D.Integer bounds5d = mask.bounds;
            final int startC = bounds5d.c;
            final int sizeC = bounds5d.sizeC;
            final BooleanMask4D masks4d[] = new BooleanMask4D[sizeC];

            for (int c = 0; c < sizeC; c++)
                masks4d[c] = mask.getMask4D(startC + c);

            setAsBooleanMask(bounds5d, masks4d);
        }
    }

    /**
     * Set the 5D mask from a 4D boolean mask array
     * 
     * @param rect
     *        the 5D region defined by 4D boolean mask array
     * @param mask
     *        the 5D mask data (array length should be equals to rect.sizeC)
     */
    public void setAsBooleanMask(Rectangle5D.Integer rect, BooleanMask4D[] mask)
    {
        if (rect.isInfiniteC())
            throw new IllegalArgumentException("Cannot set infinite C dimension on the 5D Area ROI.");

        beginUpdate();
        try
        {
            clear();

            for (int c = 0; c < rect.sizeC; c++)
                setSlice(c + rect.c, new ROI4DArea(mask[c]));
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask.<br>
     * You should call it after consecutive remove operations.
     */
    public void optimizeBounds()
    {
        final Rectangle5D.Integer bounds = getBounds();

        beginUpdate();
        try
        {
            for (int c = bounds.c; c < bounds.c + bounds.sizeC; c++)
            {
                final ROI4DArea roi = getSlice(c);

                if (roi != null)
                {
                    if (roi.isEmpty())
                        removeSlice(c);
                    else
                        roi.optimizeBounds();
                }
            }
        }
        finally
        {
            endUpdate();
        }
    }
}
