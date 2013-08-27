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
import icy.type.point.Point4D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.XMLUtil;

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
    final public int getDimension()
    {
        return 4;
    }

    /**
     * Tests if a specified {@link Point4D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point4D</code> to be tested
     * @return <code>true</code> if the specified <code>Point3D</code> is inside the boundary of the
     *         <code>ROI</code>; <code>false</code> otherwise.
     */
    public boolean contains(Point4D p)
    {
        return contains(p.getX(), p.getY(), p.getZ(), p.getT());
    }

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified
     * <code>Rectangle4D</code>. The {@code ROI.contains()} method allows a implementation to
     * conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle3D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the
     * {@code ROI} contains the {@code Rectangle4D}.
     * 
     * @param r
     *        The specified <code>Rectangle4D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         <code>Rectangle4D</code>; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the <code>Rectangle4D</code> and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
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
     * @return <code>true</code> if the specified 4D coordinates are inside the <code>ROI</code>
     *         boundary; <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y, double z, double t);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified 4D rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively
     * return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * rectangular area are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the
     * {@code ROI} contains the rectangular area.
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
     *         specified 4D rectangular area; <code>false</code> otherwise or, if the
     *         <code>ROI</code> contains the 4D rectangular area and the <code>intersects</code>
     *         method returns <code>true</code> and the containment calculations would be too
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

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * <code>Rectangle4D</code>. The {@code ROI.intersects()} method allows a {@code ROI}
     * implementation to conservatively return {@code true} when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle4D</code> and the <code>ROI</code>
     * intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * {@code Rectangle4D} does not intersect the {@code ROI}.
     * 
     * @param r
     *        the specified <code>Rectangle4D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         specified <code>Rectangle4D</code> intersect, or are both highly likely to intersect
     *         and intersection calculations would be too expensive to perform; <code>false</code>
     *         otherwise.
     * @see #intersects(double, double, double,double, double, double, double, double)
     */
    public boolean intersects(Rectangle4D r)
    {
        return intersects(r.getX(), r.getY(), r.getZ(), r.getT(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * 4D rectangular area. The 4D rectangular area is considered to intersect the <code>ROI</code>
     * if any point is contained in both the interior of the <code>ROI</code> and the specified
     * rectangular area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively
     * return {@code true} when:
     * <ul>
     * <li>there is a high probability that the 4D rectangular area and the <code>ROI</code>
     * intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * 4D rectangular area does not intersect the {@code ROI}.
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

        return intersects(x, y, z, t, sizeX, sizeY, sizeZ, sizeT) && cok;
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
        return getBounds4D().getBoundsInt();
    }

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle4D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle4D</code>.
     * 
     * @return an instance of <code>Rectangle4D</code> that is a bounding box of the
     *         <code>ROI</code>.
     */
    public Rectangle4D getBounds4D()
    {
        final Rectangle5D bounds = getBounds5D();
        return new Rectangle4D.Double(bounds.getX(), bounds.getY(), bounds.getZ(), bounds.getT(), bounds.getSizeX(),
                bounds.getSizeY(), bounds.getSizeZ(), bounds.getSizeT());
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

    /**
     * @return the c
     */
    public int getC()
    {
        return c;
    }

    /**
     * @param value
     *        the c to set
     */
    public void setC(int value)
    {
        if (c != value)
        {
            c = value;
            roiChanged();
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
