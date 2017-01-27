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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.type.point.Point4D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.XMLUtil;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

/**
 * 4D ROI base class.
 */
public abstract class ROI4D extends ROI
{
    /**
     * @deprecated Use {@link ROI4D#getROI4DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI4D> getROI4DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI4D> result = new ArrayList<ROI4D>();

        for (ROI roi : rois)
            if (roi instanceof ROI4D)
                result.add((ROI4D) roi);

        return result;
    }

    /**
     * Return all 4D ROI from the ROI list
     */
    public static List<ROI4D> getROI4DList(List<ROI> rois)
    {
        final List<ROI4D> result = new ArrayList<ROI4D>();

        for (ROI roi : rois)
            if (roi instanceof ROI4D)
                result.add((ROI4D) roi);

        return result;
    }

    public static final String ID_C = "c";

    /**
     * c coordinate attachment
     */
    protected int c;

    public ROI4D()
    {
        super();

        // by default we consider no specific C attachment
        c = -1;
    }

    @Override
    public String getDefaultName()
    {
        return "ROI4D";
    }

    @Override
    final public int getDimension()
    {
        return 4;
    }

    /**
     * Returns true if specified ROI is on the same [C] position than current ROI.
     * 
     * @param shouldContain
     *        if <code>true</code> then current ROI should "contains" specified ROI position [C]
     */
    protected boolean onSamePos(ROI4D roi, boolean shouldContain)
    {
        final int c = getC();
        final int roiC = roi.getC();

        // same position ?
        if (shouldContain)
        {
            if ((c != -1) && (c != roiC))
                return false;
        }
        else
        {
            if ((c != -1) && (roiC != -1) && (c != roiC))
                return false;
        }

        return true;
    }

    /**
     * Tests if a specified {@link Point4D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point4D</code> to be tested
     * @return <code>true</code> if the specified <code>Point3D</code> is inside the boundary of the <code>ROI</code>;
     *         <code>false</code> otherwise.
     */
    public boolean contains(Point4D p)
    {
        return contains(p.getX(), p.getY(), p.getZ(), p.getT());
    }

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified <code>Rectangle4D</code>. The
     * {@code ROI.contains()} method allows a implementation to
     * conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle3D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the {@code ROI} contains the
     * {@code Rectangle4D}.
     * 
     * @param r
     *        The specified <code>Rectangle4D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the <code>Rectangle4D</code>;
     *         <code>false</code> otherwise or, if the <code>ROI</code> contains the <code>Rectangle4D</code> and the
     *         <code>intersects</code> method returns <code>true</code> and the containment calculations would be too
     *         expensive to perform.
     * @see #contains(double, double, double, double, double, double, double, double)
     */
    public boolean contains(Rectangle4D r)
    {
        return contains(r.getX(), r.getY(), r.getZ(), r.getT(), r.getSizeX(), r.getSizeY(), r.getSizeZ(), r.getSizeT());
    }

    /**
     * Tests if the specified coordinates are inside the <code>ROI</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @param t
     *        the specified T coordinate to be tested
     * @return <code>true</code> if the specified 4D coordinates are inside the <code>ROI</code> boundary;
     *         <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y, double z, double t);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified 4D rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively return {@code false}
     * when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the rectangular area are
     * prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the {@code ROI} contains
     * the rectangular area.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param t
     *        the T coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @param sizeT
     *        size for T dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         specified 4D rectangular area; <code>false</code> otherwise or, if the <code>ROI</code> contains the 4D
     *         rectangular area and the <code>intersects</code> method returns <code>true</code> and the containment
     *         calculations would be too
     *         expensive to perform.
     */
    public abstract boolean contains(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT);

    @Override
    public boolean contains(double x, double y, double z, double t, double c)
    {
        final boolean cok;

        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && (c < (getC() + 1d));

        return contains(x, y, z, t) && cok;
    }

    @Override
    public boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeT,
            double sizeZ, double sizeC)
    {
        final boolean cok;

        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && ((c + sizeC) <= (getC() + 1d));

        return contains(x, y, z, t, sizeX, sizeY, sizeZ, sizeT) && cok;
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean contains(ROI roi)
    {
        if (roi instanceof ROI4D)
        {
            final ROI4D roi4d = (ROI4D) roi;

            if (onSamePos(roi4d, true))
            {
                // special case of ROI Point
                if (roi4d.isEmpty())
                    return contains(roi4d.getPosition4D());

                BooleanMask4D mask;
                BooleanMask4D roiMask;

                // take content first
                mask = getBooleanMask(false);
                roiMask = roi4d.getBooleanMask(false);

                // test first only on content
                if (!mask.contains(roiMask))
                    return false;

                // take content and edge
                mask = getBooleanMask(true);
                roiMask = roi4d.getBooleanMask(true);

                // then test on content and edge
                if (!mask.contains(roiMask))
                    return false;

                // contained
                return true;
            }

            return false;
        }

        // use default implementation
        return super.contains(roi);
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified <code>Rectangle4D</code>.
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle4D</code> and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the {@code Rectangle4D}
     * does not intersect the {@code ROI}.
     * 
     * @param r
     *        the specified <code>Rectangle4D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         specified <code>Rectangle4D</code> intersect, or are both highly likely to intersect
     *         and intersection calculations would be too expensive to perform; <code>false</code> otherwise.
     * @see #intersects(double, double, double,double, double, double, double, double)
     */
    public boolean intersects(Rectangle4D r)
    {
        return intersects(r.getX(), r.getY(), r.getZ(), r.getT(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * 4D rectangular area. The 4D rectangular area is considered to intersect the <code>ROI</code> if any point is
     * contained in both the interior of the <code>ROI</code> and the specified
     * rectangular area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the 4D rectangular area and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the 4D rectangular area
     * does not intersect the {@code ROI}.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param t
     *        the T coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @param sizeT
     *        size for T dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public abstract boolean intersects(double x, double y, double z, double t, double sizeX, double sizeY,
            double sizeZ, double sizeT);

    @Override
    public boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC)
    {
        // easy discard
        if ((sizeX == 0d) || (sizeY == 0d) || (sizeZ == 0d) || (sizeT == 0d) || (sizeC == 0d))
            return false;

        final boolean cok;

        if (getC() == -1)
            cok = true;
        else
            cok = ((c + sizeC) > getC()) && (c < (getC() + 1d));

        return cok && intersects(x, y, z, t, sizeX, sizeY, sizeZ, sizeT);
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean intersects(ROI roi)
    {
        if (roi instanceof ROI4D)
        {
            final ROI4D roi4d = (ROI4D) roi;

            if (onSamePos(roi4d, false))
                return getBooleanMask(true).intersects(roi4d.getBooleanMask(true));
        }

        // use default implementation
        return super.intersects(roi);
    }

    /**
     * Calculate and returns the 4D bounding box of the <code>ROI</code>.<br>
     * This method is used by {@link #getBounds4D()} which should try to cache the result as the
     * bounding box calculation can take some computation time for complex ROI.
     */
    public abstract Rectangle4D computeBounds4D();

    @Override
    public Rectangle5D computeBounds5D()
    {
        final Rectangle4D bounds4D = computeBounds4D();
        if (bounds4D == null)
            return new Rectangle5D.Double();

        final Rectangle5D.Double result = new Rectangle5D.Double(bounds4D.getX(), bounds4D.getY(), bounds4D.getZ(),
                bounds4D.getT(), 0d, bounds4D.getSizeX(), bounds4D.getSizeY(), bounds4D.getSizeZ(),
                bounds4D.getSizeT(), 0d);

        if (getC() == -1)
        {
            result.c = Double.NEGATIVE_INFINITY;
            result.sizeC = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.c = getC();
            result.sizeC = 1d;
        }

        return result;
    }

    /**
     * Returns an integer {@link Rectangle4D} that completely encloses the <code>ROI</code>. Note
     * that there is no guarantee that the returned <code>Rectangle4D</code> is the smallest
     * bounding box that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely
     * within the indicated <code>Rectangle4D</code>. The returned <code>Rectangle4D</code> might
     * also fail to completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the
     * limited range of the integer data type. The <code>getBounds4D</code> method generally returns
     * a tighter bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle4D</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle4D.Integer getBounds()
    {
        return getBounds4D().toInteger();
    }

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle4D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle4D</code>.
     * 
     * @return an instance of <code>Rectangle4D</code> that is a bounding box of the <code>ROI</code>.
     */
    public Rectangle4D getBounds4D()
    {
        return getBounds5D().toRectangle4D();
    }

    /**
     * Returns the integer ROI position which normally correspond to the <i>minimum</i> point of the
     * ROI bounds.
     * 
     * @see #getBounds()
     */
    public Point4D.Integer getPosition()
    {
        final Rectangle4D.Integer bounds = getBounds();
        return new Point4D.Integer(bounds.x, bounds.y, bounds.z, bounds.t);
    }

    /**
     * Returns the ROI position which normally correspond to the <i>minimum</i> point of the ROI
     * bounds.
     * 
     * @see #getBounds4D()
     */
    public Point4D getPosition4D()
    {
        return getBounds4D().getPosition();
    }

    @Override
    public boolean canSetBounds()
    {
        // default
        return false;
    }

    /**
     * Set the <code>ROI</code> 4D bounds.<br>
     * Note that not all ROI supports bounds modification and you should call {@link #canSetBounds()} first to test if
     * the operation is supported.<br>
     * 
     * @param bounds
     *        new ROI 4D bounds
     */
    public void setBounds4D(Rectangle4D bounds)
    {
        // do nothing by default (not supported)
    }

    @Override
    public void setBounds5D(Rectangle5D bounds)
    {
        beginUpdate();
        try
        {
            // infinite C dim ?
            if (bounds.getSizeC() == Double.POSITIVE_INFINITY)
                setC(-1);
            else
                setC((int) bounds.getC());

            setBounds4D(bounds.toRectangle4D());
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean canSetPosition()
    {
        // default implementation use translation if available
        return canTranslate();
    }

    /**
     * Set the <code>ROI</code> 4D position.<br>
     * Note that not all ROI supports position modification and you should call {@link #canSetPosition()} first to test
     * if the operation is supported.<br>
     * 
     * @param position
     *        new ROI 4D position
     */
    public void setPosition4D(Point4D position)
    {
        // use translation operation by default if supported
        if (canTranslate())
        {
            final Point4D oldPos = getPosition4D();
            translate(position.getX() - oldPos.getX(), position.getY() - oldPos.getY(),
                    position.getZ() - oldPos.getZ(), position.getT() - oldPos.getT());
        }
    }

    @Override
    public void setPosition5D(Point5D position)
    {
        beginUpdate();
        try
        {
            setC((int) position.getC());
            setPosition4D(position.toPoint4D());
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Returns <code>true</code> if the ROI support translate operation.
     * 
     * @see #translate(double, double, double, double)
     */
    public boolean canTranslate()
    {
        // by default
        return false;
    }

    /**
     * Translate the ROI position by the specified delta X/Y/Z/T.<br>
     * Note that not all ROI support this operation so you should test it by calling {@link #canTranslate()} first.
     * 
     * @param dx
     *        translation value to apply on X dimension
     * @param dy
     *        translation value to apply on Y dimension
     * @param dz
     *        translation value to apply on Z dimension
     * @param dt
     *        translation value to apply on T dimension
     * @see #canTranslate()
     * @see #setPosition4D(Point4D)
     */
    public void translate(double dx, double dy, double dz, double dt)
    {

    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, int c, boolean inclusive)
    {
        // not on the correct C position --> return empty mask
        if (!isActiveFor(c))
            return new boolean[Math.max(0, width) * Math.max(0, height)];

        return getBooleanMask2D(x, y, width, height, z, t, inclusive);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi and for the
     * specified Z,T position.<br>
     * if the pixel (x,y) is contained in the roi Z,T position then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z,T position then result[(y * width) + x] =
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param width
     *        the width of the specified rectangular area
     * @param height
     *        the height of the specified rectangular area
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     * @return the boolean bitmap mask
     */
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, boolean inclusive)
    {
        final boolean[] result = new boolean[Math.max(0, width) * Math.max(0, height)];

        // simple and basic implementation, override it to have better performance
        int offset = 0;
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                if (inclusive)
                    result[offset] = intersects(x + i, y + j, z, t, 1d, 1d, 1d, 1d);
                else
                    result[offset] = contains(x + i, y + j, z, t, 1d, 1d, 1d, 1d);
                offset++;
            }
        }

        return result;
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi and for the
     * specified Z,T position.<br>
     * if the pixel (x,y) is contained in the roi Z,T position then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z,T position then result[(y * width) + x] =
     * false
     * 
     * @param rect
     *        2D rectangular area we want to retrieve the boolean mask
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public boolean[] getBooleanMask2D(Rectangle rect, int z, int t, boolean inclusive)
    {
        return getBooleanMask2D(rect.x, rect.y, rect.width, rect.height, z, t, inclusive);
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        // not on the correct C position --> return empty mask
        if (!isActiveFor(c))
            return new BooleanMask2D(new Rectangle(), new boolean[0]);

        return getBooleanMask2D(z, t, inclusive);
    }

    /**
     * Get the {@link BooleanMask2D} object representing the roi for the specified Z,T position.<br>
     * It contains the rectangle mask bounds and the associated boolean array mask.<br>
     * if the pixel (x,y) is contained in the roi Z,T position then result.mask[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z,T position then result.mask[(y * w) + x] =
     * false
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask2D getBooleanMask2D(int z, int t, boolean inclusive)
    {
        final Rectangle bounds = getBounds4D().toRectangle2D().getBounds();

        // empty ROI --> return empty mask
        if (bounds.isEmpty())
            return new BooleanMask2D(new Rectangle(), new boolean[0]);

        final BooleanMask2D result = new BooleanMask2D(bounds, getBooleanMask2D(bounds, z, t, inclusive));

        // optimized bounds to optimize memory usage for this specific Z, T slice mask
        result.optimizeBounds();

        return result;
    }

    /**
     * Returns the {@link BooleanMask3D} object representing the XYZ volume content at specified Z,
     * T, C position.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask or -1 to retrieve the whole Z
     *        dimension
     * @param t
     *        T position we want to retrieve the boolean mask.
     * @param c
     *        C position we want to retrieve the boolean mask.<br>
     *        Set it to -1 to retrieve the mask whatever is the C position of this ROI4D.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask3D(int z, int t, int c, boolean inclusive)
    {
        // not on the correct C position --> return empty mask
        if (!isActiveFor(c))
            return new BooleanMask3D();

        // whole Z dimension
        if (z == -1)
            return getBooleanMask3D(t, inclusive);

        // define bounds
        final Rectangle3D.Integer bounds = getBounds4D().toRectangle3D().toInteger();
        bounds.setZ(z);
        bounds.setSizeZ(1);

        return new BooleanMask3D(bounds, new BooleanMask2D[] {getBooleanMask2D(z, t, inclusive)});
    }

    /**
     * Get the {@link BooleanMask3D} object representing the roi for specified T position.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask3D(int t, boolean inclusive)
    {
        final Rectangle3D.Integer bounds = getBounds4D().toRectangle3D().toInteger();
        final BooleanMask2D masks[] = new BooleanMask2D[bounds.sizeZ];

        for (int z = 0; z < masks.length; z++)
            masks[z] = getBooleanMask2D(bounds.z + z, t, inclusive);

        return new BooleanMask3D(bounds, masks);
    }

    /**
     * Returns the {@link BooleanMask4D} object representing the XYZT space content at specified Z,
     * T, C position.
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask or -1 to retrieve the whole Z
     *        dimension
     * @param t
     *        T position we want to retrieve the boolean mask or -1 to retrieve the whole T
     *        dimension
     * @param c
     *        C position we want to retrieve the boolean mask.<br>
     *        Set it to -1 to retrieve the mask whatever is the C position of this ROI4D.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask4D getBooleanMask4D(int z, int t, int c, boolean inclusive)
    {
        // not on the correct C position --> return empty mask
        if (!isActiveFor(c))
            return new BooleanMask4D();

        // whole Z dimension
        if (z == -1)
        {
            // whole Z and T dimension
            if (t == -1)
                return getBooleanMask(inclusive);

            // define bounds
            final Rectangle4D.Integer bounds = getBounds4D().toInteger();
            bounds.setT(t);
            bounds.setSizeT(1);

            // whole Z dimension but specific T
            return new BooleanMask4D(bounds, new BooleanMask3D[] {getBooleanMask3D(t, inclusive)});
        }

        final Rectangle4D.Integer bounds4d = getBounds4D().toInteger();

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
                    z, bounds4d.t + i, inclusive)});

        return new BooleanMask4D(bounds4d, masks);
    }

    /**
     * Get the {@link BooleanMask4D} object representing the roi.<br>
     * It contains the 4D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask4D getBooleanMask(boolean inclusive)
    {
        final Rectangle4D.Integer bounds = getBounds();
        final BooleanMask3D masks[] = new BooleanMask3D[bounds.sizeT];

        for (int t = 0; t < masks.length; t++)
            masks[t] = getBooleanMask3D(bounds.t + t, inclusive);

        return new BooleanMask4D(bounds, masks);
    }

    /*
     * Generic implementation for ROI4D using the BooleanMask object so
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
     * Generic implementation for ROI4D using the BooleanMask object so
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

        return numPoints;
    }

    /**
     * Returns the C position.<br>
     * <code>-1</code> is a special value meaning the ROI is set on all C channels (infinite C
     * dimension).
     */
    public int getC()
    {
        return c;
    }

    /**
     * Sets C position of this 4D ROI.<br>
     * You cannot set the ROI on a negative C position as <code>-1</code> is a special value meaning
     * the ROI is set on all C channels (infinite C dimension).
     */
    public void setC(int value)
    {
        final int v;

        // special value for infinite dimension --> change to -1
        if (value == Integer.MIN_VALUE)
            v = -1;
        else
            v = value;

        if (c != v)
        {
            c = v;
            roiChanged(false);
        }
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return isActiveFor(canvas.getPositionC());
    }

    /**
     * Return true if the ROI is active for the specified C coordinate
     */
    public boolean isActiveFor(int c)
    {
        return (getC() == -1) || (c == -1) || (getC() == c);
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            setC(XMLUtil.getElementIntValue(node, ID_C, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }
}
