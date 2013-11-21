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
package plugins.kernel.roi.roi4d;

import icy.roi.BooleanMask3D;
import icy.roi.BooleanMask4D;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.type.point.Point4D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle4D;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map.Entry;

import plugins.kernel.roi.roi3d.ROI3DArea;

/**
 * 4D Area ROI.
 * 
 * @author Stephane
 */
public class ROI4DArea extends ROI4DStack<ROI3DArea>
{
    public ROI4DArea()
    {
        super(ROI3DArea.class);

        setName("4D area");
    }

    public ROI4DArea(Point4D pt)
    {
        this();

        addBrush(pt.toPoint2D(), (int) pt.getZ(), (int) pt.getT());
    }

    public ROI4DArea(Point5D pt)
    {
        this(pt.toPoint4D());
    }

    /**
     * Create a 3D Area ROI type from the specified {@link BooleanMask4D}.
     */
    public ROI4DArea(BooleanMask4D mask)
    {
        this();

        setAsBooleanMask(mask);
    }

    /**
     * Create a copy of the specified 4D Area ROI.
     */
    public ROI4DArea(ROI4DArea area)
    {
        this();

        // copy the source 4D area ROI
        for (Entry<Integer, ROI3DArea> entry : area.slices.entrySet())
            slices.put(entry.getKey(), new ROI3DArea(entry.getValue()));
    }

    /**
     * Adds the specified point to this ROI
     */
    public void addPoint(int x, int y, int z, int t)
    {
        setPoint(x, y, z, t, true);
    }

    /**
     * Remove a point from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removePoint(int x, int y, int z, int t)
    {
        setPoint(x, y, z, t, false);
    }

    /**
     * Set the value for the specified point in the mask.
     * Don't forget to call optimizeBounds() after consecutive remove point operation
     * to refresh the mask bounds.
     */
    public void setPoint(int x, int y, int z, int t, boolean value)
    {
        getSlice(t, true).setPoint(x, y, z, value);
    }

    /**
     * Add brush point at specified position and for specified Z,T slice.
     */
    public void addBrush(Point2D pos, int z, int t)
    {
        getSlice(t, true).addBrush(pos, z);
    }

    /**
     * Remove brush point from the mask at specified position and for specified Z,T slice.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     */
    public void removeBrush(Point2D pos, int z, int t)
    {
        getSlice(t, true).removeBrush(pos, z);
    }

    /**
     * Sets the ROI slice at given T position to this 4D ROI
     * 
     * @param t
     *        the position where the slice must be set
     * @param roiSlice
     *        the 3D ROI to set
     * @param merge
     *        <code>true</code> if the given slice should be merged with the existing slice, or
     *        <code>false</code> to replace the existing slice.
     */
    public void setSlice(int t, ROI3D roiSlice, boolean merge)
    {
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot add an empty slice in a 4D ROI");

        final ROI3DArea currentSlice = getSlice(t);
        final ROI newSlice;

        // merge both slice
        if ((currentSlice != null) && merge)
        {
            // we need to modify the T and C position so we do the merge correctly
            roiSlice.setT(t);
            roiSlice.setC(getC());
            // do ROI union
            newSlice = currentSlice.getUnion(roiSlice);
        }
        else
            newSlice = roiSlice;

        if (newSlice instanceof ROI3DArea)
            setSlice(t, (ROI3DArea) newSlice);
        else if (newSlice instanceof ROI3D)
            setSlice(t, new ROI3DArea(((ROI3D) newSlice).getBooleanMask(true)));
        else
            throw new IllegalArgumentException("Can't add the result of the merge operation on 3D slice " + t + ": "
                    + newSlice.getClassName());
    }

    /**
     * Returns true if the ROI is empty (the mask does not contains any point).
     */
    public boolean isEmpty()
    {
        if (getBounds().isEmpty())
            return true;

        for (ROI3DArea area : slices.values())
            if (!area.isEmpty())
                return false;

        return true;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return false;
    }

    /**
     * Set the mask from a BooleanMask4D object
     */
    public void setAsBooleanMask(BooleanMask4D mask)
    {
        if (mask != null)
        {
            final Collection<BooleanMask3D> values = mask.mask.values();
            setAsBooleanMask(mask.bounds, values.toArray(new BooleanMask3D[values.size()]));
        }
    }

    /**
     * Set the 4D mask from a 3D boolean mask array
     * 
     * @param rect
     *        the 4D region defined by 3D boolean mask array
     * @param mask
     *        the 4D mask data (array length should be equals to rect.sizeZ)
     */
    public void setAsBooleanMask(Rectangle4D.Integer rect, BooleanMask3D[] mask)
    {
        if (rect.isInfiniteT())
            throw new IllegalArgumentException("Cannot set infinite T dimension on the 4D Area ROI.");

        beginUpdate();
        try
        {
            clear();

            for (int t = 0; t < rect.sizeT; t++)
                setSlice(t + rect.t, new ROI3DArea(mask[t]));

            optimizeBounds();
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask<br>
     * You should call it after consecutive remove operations.
     */
    public void optimizeBounds()
    {
        final Rectangle4D.Integer bounds = getBounds();

        beginUpdate();
        try
        {
            for (int t = bounds.t; t < bounds.t + bounds.sizeT; t++)
            {
                final ROI3DArea roi = slices.get(Integer.valueOf(t));

                if (roi.isEmpty())
                    removeSlice(t);
                else
                    roi.optimizeBounds();
            }
        }
        finally
        {
            endUpdate();
        }
    }
}
