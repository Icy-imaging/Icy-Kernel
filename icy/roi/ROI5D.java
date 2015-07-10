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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;
import java.util.List;

/**
 * 5D ROI base class.
 */
public abstract class ROI5D extends ROI
{
    /**
     * @deprecated Use {@link ROI5D#getROI5DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI5D> getROI5DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    /**
     * Return all 5D ROI from the ROI list
     */
    public static List<ROI5D> getROI5DList(List<ROI> rois)
    {
        final List<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    public ROI5D()
    {
        super();
    }

    @Override
    final public int getDimension()
    {
        return 5;
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return true;
    }

    /**
     * Returns an integer {@link Rectangle5D} that completely encloses the <code>ROI</code>. Note
     * that there is no guarantee that the returned <code>Rectangle5D</code> is the smallest
     * bounding box that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely
     * within the indicated <code>Rectangle5D</code>. The returned <code>Rectangle5D</code> might
     * also fail to completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the
     * limited range of the integer data type. The <code>getBounds5D</code> method generally returns
     * a tighter bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle5D</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle5D.Integer getBounds()
    {
        return getBounds5D().toInteger();
    }

    /**
     * Returns the integer ROI position which normally correspond to the <i>minimum</i> point of the
     * ROI bounds.
     * 
     * @see #getBounds()
     */
    public Point5D.Integer getPosition()
    {
        final Rectangle5D.Integer bounds = getBounds();
        return new Point5D.Integer(bounds.x, bounds.y, bounds.z, bounds.t, bounds.c);
    }

    @Override
    public boolean canSetBounds()
    {
        // default
        return false;
    }

    @Override
    public void setBounds5D(Rectangle5D bounds)
    {
        // do nothing by default (not supported)
    }

    @Override
    public boolean canSetPosition()
    {
        // default implementation use translation if available
        return canTranslate();
    }

    @Override
    public void setPosition5D(Point5D position)
    {
        // use translation operation by default if supported
        if (canTranslate())
        {
            final Point5D oldPos = getPosition5D();
            translate(position.getX() - oldPos.getX(), position.getY() - oldPos.getY(),
                    position.getZ() - oldPos.getZ(), position.getT() - oldPos.getT(), position.getC() - oldPos.getC());
        }
    }

    /**
     * Returns <code>true</code> if the ROI support translate operation.
     * 
     * @see #translate(double, double, double, double, double)
     */
    public boolean canTranslate()
    {
        // by default
        return false;
    }

    /**
     * Translate the ROI position by the specified delta X/Y/Z/T.<br>
     * Note that not all ROI support this operation so you should test it by calling
     * {@link #canTranslate()} first.
     * 
     * @param dx
     *        translation value to apply on X dimension
     * @param dy
     *        translation value to apply on Y dimension
     * @param dz
     *        translation value to apply on Z dimension
     * @param dt
     *        translation value to apply on T dimension
     * @param dc
     *        translation value to apply on C dimension
     * @see #canTranslate()
     * @see #setPosition5D(Point5D)
     */
    public void translate(double dx, double dy, double dz, double dt, double dc)
    {

    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean contains(ROI roi)
    {
        if (roi instanceof ROI5D)
            return getBooleanMask(false).contains(((ROI5D) roi).getBooleanMask(false));

        // do it the other way
        return roi.intersects(this);
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean intersects(ROI roi)
    {
        if (roi instanceof ROI5D)
            return getBooleanMask(true).intersects(((ROI5D) roi).getBooleanMask(true));

        // do it the other way
        return roi.intersects(this);
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        final BooleanMask2D result = super.getBooleanMask2D(z, t, c, inclusive);

        // optimized bounds to optimize memory usage for this specific Z, T, C slice mask
        result.optimizeBounds();

        return result;
    }

    /**
     * Returns the {@link BooleanMask3D} object representing the XYZ volume content at specified Z,
     * T, C position.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask or -1 to retrieve the whole Z
     *        dimension
     * @param t
     *        T position we want to retrieve the boolean mask.
     * @param c
     *        C position we want to retrieve the boolean mask.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask3D(int z, int t, int c, boolean inclusive)
    {
        // whole Z dimension
        if (z == -1)
            return getBooleanMask3D(t, c, inclusive);

        // define bounds
        final Rectangle3D.Integer bounds = getBounds5D().toRectangle3D().toInteger();
        bounds.setZ(z);
        bounds.setSizeZ(1);

        return new BooleanMask3D(bounds, new BooleanMask2D[] {getBooleanMask2D(z, t, c, inclusive)});
    }

    /**
     * Returns the {@link BooleanMask3D} object representing the XYZ volume content at specified T,
     * C position.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask3D(int t, int c, boolean inclusive)
    {
        final Rectangle3D.Integer bounds = getBounds5D().toRectangle3D().toInteger();
        final BooleanMask2D masks[] = new BooleanMask2D[bounds.sizeZ];

        for (int z = 0; z < masks.length; z++)
            masks[z] = getBooleanMask2D(bounds.z + z, t, c, inclusive);

        return new BooleanMask3D(bounds, masks);
    }

    /**
     * Returns the {@link BooleanMask4D} object representing the XYZT space content at specified Z
     * and C position.
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask or -1 to retrieve the whole Z
     *        dimension
     * @param t
     *        T position we want to retrieve the boolean mask or -1 to retrieve the whole T
     *        dimension
     * @param c
     *        C position we want to retrieve the boolean mask.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask4D getBooleanMask4D(int z, int t, int c, boolean inclusive)
    {
        // whole Z dimension
        if (z == -1)
        {
            // whole Z and T dimension
            if (t == -1)
                return getBooleanMask4D(c, inclusive);

            // define bounds
            final Rectangle4D.Integer bounds = getBounds5D().toRectangle4D().toInteger();
            bounds.setT(t);
            bounds.setSizeT(1);

            // whole Z dimension but specific T
            return new BooleanMask4D(bounds, new BooleanMask3D[] {getBooleanMask3D(t, c, inclusive)});
        }

        final Rectangle4D.Integer bounds4d = getBounds5D().toRectangle4D().toInteger();

        // specific Z
        bounds4d.setZ(z);
        bounds4d.setSizeZ(1);

        // specific T dimension ?
        if (t != -1)
        {
            bounds4d.setT(t);
            bounds4d.setSizeT(1);
        }

        final Rectangle3D.Integer bounds3d = (Rectangle3D.Integer) bounds4d.toRectangle3D();
        final BooleanMask3D masks[] = new BooleanMask3D[bounds4d.sizeT];

        for (int i = 0; i < bounds4d.sizeT; i++)
            masks[i] = new BooleanMask3D((Rectangle3D.Integer) bounds3d.clone(), new BooleanMask2D[] {getBooleanMask2D(
                    z, bounds4d.t + i, c, inclusive)});

        return new BooleanMask4D(bounds4d, masks);
    }

    /**
     * Get the {@link BooleanMask4D} object representing the roi for specified C position.<br>
     * It contains the 4D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask4D getBooleanMask4D(int c, boolean inclusive)
    {
        final Rectangle4D.Integer bounds = getBounds5D().toRectangle4D().toInteger();
        final BooleanMask3D masks[] = new BooleanMask3D[bounds.sizeT];

        for (int t = 0; t < masks.length; t++)
            masks[t] = getBooleanMask3D(bounds.t + t, c, inclusive);

        return new BooleanMask4D(bounds, masks);
    }

    /**
     * Returns the {@link BooleanMask5D} object representing the XYZTC space content at specified Z,
     * T, C position.
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask or -1 to retrieve the whole Z
     *        dimension
     * @param t
     *        T position we want to retrieve the boolean mask or -1 to retrieve the whole T
     *        dimension
     * @param c
     *        C position we want to retrieve the boolean mask or -1 to retrieve the whole C
     *        dimension
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask5D getBooleanMask5D(int z, int t, int c, boolean inclusive)
    {
        // whole Z dimension
        if (z == -1)
        {
            // whole Z and T dimension
            if (t == -1)
            {
                // whole Z, T and C dimension
                if (c == -1)
                    return getBooleanMask(inclusive);

                // define bounds
                final Rectangle5D.Integer bounds = getBounds5D().toInteger();
                bounds.setC(c);
                bounds.setSizeC(1);

                // whole Z and T dimension but specific C
                return new BooleanMask5D(bounds, new BooleanMask4D[] {getBooleanMask4D(c, inclusive)});
            }

            final Rectangle5D.Integer bounds5d = getBounds();

            // specific T
            bounds5d.setT(t);
            bounds5d.setSizeT(1);
            // specific C dimension ?
            if (c != -1)
            {
                bounds5d.setC(c);
                bounds5d.setSizeC(1);
            }

            final Rectangle4D.Integer bounds4d = (Rectangle4D.Integer) bounds5d.toRectangle4D();
            final BooleanMask4D masks[] = new BooleanMask4D[bounds5d.sizeC];

            for (int i = 0; i < bounds5d.sizeC; i++)
                masks[i] = new BooleanMask4D((Rectangle4D.Integer) bounds4d.clone(),
                        new BooleanMask3D[] {getBooleanMask3D(t, bounds5d.c + i, inclusive)});

            return new BooleanMask5D(bounds5d, masks);
        }

        final Rectangle5D.Integer bounds5d = getBounds();

        // specific Z
        bounds5d.setZ(z);
        bounds5d.setSizeZ(1);
        // specific T dimension ?
        if (t != -1)
        {
            bounds5d.setT(t);
            bounds5d.setSizeT(1);
        }
        // specific C dimension ?
        if (c != -1)
        {
            bounds5d.setC(c);
            bounds5d.setSizeC(1);
        }

        final Rectangle4D.Integer bounds4d = (Rectangle4D.Integer) bounds5d.toRectangle4D();
        final Rectangle3D.Integer bounds3d = (Rectangle3D.Integer) bounds4d.toRectangle3D();
        final BooleanMask4D masks[] = new BooleanMask4D[bounds5d.sizeC];

        for (int i = 0; i < bounds5d.sizeC; i++)
        {
            final BooleanMask3D masks3d[] = new BooleanMask3D[bounds4d.sizeT];

            for (int j = 0; j < bounds5d.sizeT; j++)
                masks3d[i] = new BooleanMask3D((Rectangle3D.Integer) bounds3d.clone(),
                        new BooleanMask2D[] {getBooleanMask2D(z, bounds5d.t + j, bounds5d.c + i, inclusive)});

            masks[i] = new BooleanMask4D((Rectangle4D.Integer) bounds4d.clone(), masks3d);
        }

        return new BooleanMask5D(bounds5d, masks);
    }

    /**
     * Get the {@link BooleanMask5D} object representing the roi.<br>
     * It contains the 5D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask5D getBooleanMask(boolean inclusive)
    {
        final Rectangle5D.Integer bounds = getBounds();
        final BooleanMask4D masks[] = new BooleanMask4D[bounds.sizeC];

        for (int c = 0; c < masks.length; c++)
            masks[c] = getBooleanMask4D(bounds.c + c, inclusive);

        return new BooleanMask5D(bounds, masks);
    }

    /*
     * Generic implementation for ROI5D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double computeNumberOfContourPoints()
    {
        // approximation by using number of point of the edge of boolean mask
        return getBooleanMask(true).getContourPointsAsIntArray().length / getDimension();
    }

    /*
     * Generic implementation for ROI5D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double computeNumberOfPoints()
    {
        double numPoints = 0;

        // approximation by using number of point of boolean mask with and without border
        numPoints += getBooleanMask(true).getNumberOfPoints();
        numPoints += getBooleanMask(false).getNumberOfPoints();
        numPoints /= 2d;

        return numPoints / getDimension();
    }

}
