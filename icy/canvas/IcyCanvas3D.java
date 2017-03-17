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
package icy.canvas;

import icy.gui.viewer.Viewer;
import icy.sequence.DimensionId;
import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;

import java.awt.Point;

/**
 * @author Stephane
 */
public abstract class IcyCanvas3D extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = 6001100311244609559L;

    /** mouse position (image coordinate space) */
    protected Point3D.Double mouseImagePos;

    public IcyCanvas3D(Viewer viewer)
    {
        super(viewer);

        // default for 3D canvas
        posX = -1;
        posY = -1;
        posZ = -1;
        posT = 0;

        // initial mouse position
        mouseImagePos = new Point3D.Double();
    }

    @Override
    public void setPositionT(int t)
    {
        // position -1 not supported for T dimension on this canvas
        if (t != -1)
            super.setPositionT(t);
    }

    @Override
    public double getMouseImagePosX()
    {
        // can be called before constructor ended
        if (mouseImagePos == null)
            return 0d;

        return mouseImagePos.x;

    }

    @Override
    public double getMouseImagePosY()
    {
        // can be called before constructor ended
        if (mouseImagePos == null)
            return 0d;

        return mouseImagePos.y;
    }

    @Override
    public double getMouseImagePosZ()
    {
        // can be called before constructor ended
        if (mouseImagePos == null)
            return 0d;

        return mouseImagePos.z;
    }

    /**
     * Return mouse image position
     */
    public Point3D.Double getMouseImagePos()
    {
        return (Point3D.Double) mouseImagePos.clone();
    }

    public void setMouseImagePos(double x, double y, double z)
    {
        if ((mouseImagePos.x != x) || (mouseImagePos.y != y) || (mouseImagePos.z != z))
        {
            mouseImagePos.x = x;
            mouseImagePos.y = y;
            mouseImagePos.z = z;

            // direct update of mouse canvas position
            mousePos = imageToCanvas(mouseImagePos);
            // notify change
            mouseImagePositionChanged(DimensionId.NULL);
        }
    }

    /**
     * Set mouse image position
     */
    public void setMouseImagePos(Point3D.Double point)
    {
        setMouseImagePos(point.x, point.y, point.z);
    }

    @Override
    public boolean setMousePos(int x, int y)
    {
        final boolean result = super.setMousePos(x, y);

        if (result)
        {
            if (mouseImagePos == null)
                mouseImagePos = new Point3D.Double();

            final Point3D newPos = canvasToImage(mousePos);
            final double newX = newPos.getX();
            final double newY = newPos.getY();
            final double newZ = newPos.getZ();
            boolean changed = false;

            // need to check against NaN is conversion is not supported
            if (!Double.isNaN(newX) && (newX != mouseImagePos.x))
            {
                mouseImagePos.x = newX;
                changed = true;
            }
            // need to check against NaN is conversion is not supported
            if (!Double.isNaN(newY) && (newY != mouseImagePos.y))
            {
                mouseImagePos.y = newY;
                changed = true;
            }
            // need to check against NaN is conversion is not supported
            if (!Double.isNaN(newZ) && (newZ != mouseImagePos.z))
            {
                mouseImagePos.z = newZ;
                changed = true;
            }

            // notify change
            if (changed)
                mouseImagePositionChanged(DimensionId.NULL);
        }

        return result;
    }

    /**
     * Convert specified image point to canvas point.
     */
    @SuppressWarnings("static-method")
    public Point imageToCanvas(double x, double y, double z)
    {
        // default implementation, must be override in IcyCanvas3D implementation
        return new Point(0, 0);
    }

    /**
     * Convert specified image point to canvas point
     */
    public Point imageToCanvas(Point3D.Double point)
    {
        return imageToCanvas(point.x, point.y, point.z);
    }

    /**
     * Convert specified canvas point to image point
     */
    @SuppressWarnings("static-method")
    public Point3D.Double canvasToImage(int x, int y)
    {
        // default implementation, must be override in IcyCanvas3D implementation
        return new Point3D.Double(0d, 0d, 0d);
    }

    /**
     * Convert specified canvas point to image point
     */
    public Point3D.Double canvasToImage(Point point)
    {
        return canvasToImage(point.x, point.y);
    }

    /**
     * Adjust view position and possibly scaling factor to ensure the specified region become visible.<br>
     * It's up to the Canvas implementation to decide how to make the region visible.
     * 
     * @param region
     *        the region we want to see
     */
    public void centerOn(Rectangle3D.Integer region)
    {
        // override it in Canvas implementation
    }
}