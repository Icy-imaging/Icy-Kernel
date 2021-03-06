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
package icy.type.rectangle;

import icy.type.dimension.Dimension3D;
import icy.type.geom.Line3D;
import icy.type.geom.Shape3D;
import icy.type.point.Point3D;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Rectangle3D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle3D implements Shape3D, Cloneable
{
    /**
     * Returns <code>true</code> if the specified Line3D intersects the given Rectangle3D.
     * 
     * @param line
     *        the Line3D we want to test intersection for
     * @param rect
     *        the Rectangle3D we want to test intersection for
     */
    public static boolean intersects(Line3D line, Rectangle3D rect)
    {
        final double rMinX = rect.getMinX();
        final double rMaxX = rect.getMaxX();
        final double rMinY = rect.getMinY();
        final double rMaxY = rect.getMaxY();
        final double rMinZ = rect.getMinZ();
        final double rMaxZ = rect.getMaxZ();

        if ((line.getX2() < rMinX) && (line.getX1() < rMinX))
            return false;
        if ((line.getX2() > rMaxX) && (line.getX1() > rMaxX))
            return false;
        if ((line.getY2() < rMinY) && (line.getY1() < rMinY))
            return false;
        if ((line.getY2() > rMaxY) && (line.getY1() > rMaxY))
            return false;
        if ((line.getZ2() < rMinZ) && (line.getZ1() < rMinZ))
            return false;
        if ((line.getZ2() > rMaxZ) && (line.getZ1() > rMaxZ))
            return false;

        if ((line.getX1() > rMinX) && (line.getX1() < rMaxX) && (line.getY1() > rMinY) && (line.getY1() < rMaxY)
                && (line.getZ1() > rMinZ) && (line.getZ1() < rMaxZ))
            return true;

        return inBox(getIntersection(line.getX1() - rMinX, line.getX2() - rMinX, line), rect, 1)
                || inBox(getIntersection(line.getY1() - rMinY, line.getY2() - rMinY, line), rect, 2)
                || inBox(getIntersection(line.getZ1() - rMinZ, line.getZ2() - rMinZ, line), rect, 3)
                || inBox(getIntersection(line.getX1() - rMaxX, line.getX2() - rMaxX, line), rect, 1)
                || inBox(getIntersection(line.getY1() - rMaxY, line.getY2() - rMaxY, line), rect, 2)
                || inBox(getIntersection(line.getZ1() - rMaxZ, line.getZ2() - rMaxZ, line), rect, 3);
    }

    static Point3D getIntersection(double dst1, double dst2, Line3D line)
    {
        if ((dst1 * dst2) >= 0d)
            return null;
        if (dst1 == dst2)
            return null;

        final double f = -dst1 / (dst2 - dst1);

        // get line vector
        Point3D result = line.getVector();
        // multiply it by factor
        result = new Point3D.Double(result.getX() * f, result.getY() * f, result.getZ() * f);
        // return the hit position
        return new Point3D.Double(line.getX1() + result.getX(), line.getY1() + result.getY(), line.getZ1()
                + result.getZ());
    }

    static boolean inBox(Point3D hit, Rectangle3D rect, int axis)
    {
        if ((hit == null) || (rect == null))
            return false;

        switch (axis)
        {
            default:
                return false;
            case 1:
                return (hit.getZ() > rect.getMinZ()) && (hit.getZ() < rect.getMaxZ()) && (hit.getY() > rect.getMinY())
                        && (hit.getY() < rect.getMaxY());
            case 2:
                return (hit.getZ() > rect.getMinZ()) && (hit.getZ() < rect.getMaxZ()) && (hit.getX() > rect.getMinX())
                        && (hit.getX() < rect.getMaxX());
            case 3:
                return (hit.getX() > rect.getMinX()) && (hit.getX() < rect.getMaxX()) && (hit.getY() > rect.getMinY())
                        && (hit.getY() < rect.getMaxY());
        }
    }

    /**
     * Intersects the pair of specified source <code>Rectangle3D</code> objects and puts the result
     * into the specified destination <code>Rectangle3D</code> object. One of the source rectangles
     * can also be the destination to avoid creating a third Rectangle3D object, but in this case
     * the original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle3D</code> objects to be intersected with each
     *        other
     * @param src2
     *        the second of a pair of <code>Rectangle3D</code> objects to be intersected with each
     *        other
     * @param dest
     *        the <code>Rectangle3D</code> that holds the
     *        results of the intersection of <code>src1</code> and <code>src2</code>
     */
    public static Rectangle3D intersect(Rectangle3D src1, Rectangle3D src2, Rectangle3D dest)
    {
        final Rectangle3D result;

        if (dest == null)
            result = new Rectangle3D.Double();
        else
            result = dest;

        final double x1 = Math.max(src1.getMinX(), src2.getMinX());
        final double y1 = Math.max(src1.getMinY(), src2.getMinY());
        final double z1 = Math.max(src1.getMinZ(), src2.getMinZ());
        final double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        final double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        final double z2 = Math.min(src1.getMaxZ(), src2.getMaxZ());

        double dx;
        double dy;
        double dz;

        // special infinite case
        if (x2 == java.lang.Double.POSITIVE_INFINITY)
            dx = java.lang.Double.POSITIVE_INFINITY;
        else
            dx = x2 - x1;
        // special infinite case
        if (y2 == java.lang.Double.POSITIVE_INFINITY)
            dy = java.lang.Double.POSITIVE_INFINITY;
        else
            dy = y2 - y1;
        // special infinite case
        if (z2 == java.lang.Double.POSITIVE_INFINITY)
            dz = java.lang.Double.POSITIVE_INFINITY;
        else
            dz = z2 - z1;

        result.setRect(x1, y1, z1, dx, dy, dz);

        return result;
    }

    /**
     * Returns a new <code>Rectangle3D</code> object representing the intersection of this <code>Rectangle3D</code> with
     * the specified <code>Rectangle3D</code>.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to be intersected with this <code>Rectangle3D</code>
     * @return the largest <code>Rectangle3D</code> contained in both the specified <code>Rectangle3D</code> and in this
     *         <code>Rectangle3D</code>.
     */
    public abstract Rectangle3D createIntersection(Rectangle3D r);

    /**
     * Unions the pair of source <code>Rectangle3D</code> objects and puts the result into the
     * specified destination <code>Rectangle3D</code> object. One of the source rectangles can also
     * be the destination to avoid creating a third Rectangle3D object, but in this case the
     * original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle3D</code> objects to be combined with each other
     * @param src2
     *        the second of a pair of <code>Rectangle3D</code> objects to be combined with each
     *        other
     * @param dest
     *        the <code>Rectangle3D</code> that holds the
     *        results of the union of <code>src1</code> and <code>src2</code>
     */
    public static Rectangle3D union(Rectangle3D src1, Rectangle3D src2, Rectangle3D dest)
    {
        final Rectangle3D result;

        if (dest == null)
            result = new Rectangle3D.Double();
        else
            result = dest;

        double x1 = Math.min(src1.getMinX(), src2.getMinX());
        double y1 = Math.min(src1.getMinY(), src2.getMinY());
        double z1 = Math.min(src1.getMinZ(), src2.getMinZ());
        double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
        double z2 = Math.max(src1.getMaxZ(), src2.getMaxZ());

        double dx;
        double dy;
        double dz;

        // special infinite case
        if (x2 == java.lang.Double.POSITIVE_INFINITY)
            dx = java.lang.Double.POSITIVE_INFINITY;
        else
            dx = x2 - x1;
        // special infinite case
        if (y2 == java.lang.Double.POSITIVE_INFINITY)
            dy = java.lang.Double.POSITIVE_INFINITY;
        else
            dy = y2 - y1;
        // special infinite case
        if (z2 == java.lang.Double.POSITIVE_INFINITY)
            dz = java.lang.Double.POSITIVE_INFINITY;
        else
            dz = z2 - z1;

        result.setRect(x1, y1, z1, dx, dy, dz);

        return result;
    }

    /**
     * Returns a new <code>Rectangle3D</code> object representing the union of this <code>Rectangle3D</code> with the
     * specified <code>Rectangle3D</code>.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to be combined with this <code>Rectangle3D</code>
     * @return the smallest <code>Rectangle3D</code> containing both the specified <code>Rectangle3D</code> and this
     *         <code>Rectangle3D</code>.
     */
    public abstract Rectangle3D createUnion(Rectangle3D r);

    /**
     * Sets the position and size of this <code>Rectangle3D</code> to the specified <code>double</code> values.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of this <code>Rectangle3D</code>
     * @param y
     *        the Y coordinate of the minimum corner position of this <code>Rectangle3D</code>
     * @param z
     *        the Z coordinate of the minimum corner position of this <code>Rectangle3D</code>
     * @param sizeX
     *        size for X dimension of this <code>Rectangle3D</code>
     * @param sizeY
     *        size for Y dimension of this <code>Rectangle3D</code>
     * @param sizeZ
     *        size for Z dimension of this <code>Rectangle3D</code>
     */
    public abstract void setRect(double x, double y, double z, double sizeX, double sizeY, double sizeZ);

    /**
     * Returns the minimum X coordinate.
     */
    public abstract double getX();

    /**
     * Returns the minimum Y coordinate.
     */
    public abstract double getY();

    /**
     * Returns the minimum Z coordinate.
     */
    public abstract double getZ();

    /**
     * Returns the point coordinates.
     */
    public abstract Point3D getPosition();

    /**
     * Returns the dimension.
     */
    public abstract Dimension3D getDimension();

    /**
     * Returns the size of X dimension.
     */
    public abstract double getSizeX();

    /**
     * Returns the size of Y dimension.
     */
    public abstract double getSizeY();

    /**
     * Returns the size of Z dimension.
     */
    public abstract double getSizeZ();

    /**
     * Returns an integer {@link Rectangle3D} that completely encloses the
     * double <code>Rectangle</code>. The returned <code>Rectangle</code> might also fail to
     * completely enclose the original double <code>Rectangle</code> if it overflows
     * the limited range of the integer data type.
     * 
     * @return an integer <code>Rectangle</code> that completely encloses
     *         the actual double <code>Rectangle</code>.
     */
    public Rectangle3D.Integer toInteger()
    {
        double sx = getSizeX();
        double sy = getSizeY();
        double sz = getSizeZ();
        double x = getX();
        double y = getY();
        double z = getZ();
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        int isx;
        int isy;
        int isz;

        if (sx < 0d)
            isx = 0;
        else if (sx >= java.lang.Integer.MAX_VALUE)
            isx = java.lang.Integer.MAX_VALUE;
        else
            isx = ((int) Math.ceil(x + sx)) - ix;
        if (sy < 0d)
            isy = 0;
        else if (sy >= java.lang.Integer.MAX_VALUE)
            isy = java.lang.Integer.MAX_VALUE;
        else
            isy = ((int) Math.ceil(y + sy)) - iy;
        if (sz < 0d)
            isz = 0;
        else if (sz >= java.lang.Integer.MAX_VALUE)
            isz = java.lang.Integer.MAX_VALUE;
        else
            isz = ((int) Math.ceil(z + sz)) - iz;

        return new Rectangle3D.Integer(ix, iy, iz, isx, isy, isz);
    }

    /**
     * Sets the minimum X coordinate.
     */
    public abstract void setX(double x);

    /**
     * Sets the minimum Y coordinate.
     */
    public abstract void setY(double y);

    /**
     * Sets the minimum Z coordinate.
     */
    public abstract void setZ(double z);

    /**
     * Sets the size of X dimension.
     */
    public abstract void setSizeX(double value);

    /**
     * Sets the size of Y dimension.
     */
    public abstract void setSizeY(double value);

    /**
     * Sets the size of Z dimension.
     */
    public abstract void setSizeZ(double value);

    /**
     * Returns the smallest X coordinate of the rectangle.
     */
    public double getMinX()
    {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the rectangle.
     */
    public double getMinY()
    {
        return getY();
    }

    /**
     * Returns the smallest Z coordinate of the rectangle.
     */
    public double getMinZ()
    {
        return getZ();
    }

    /**
     * Returns the largest X coordinate of the rectangle.
     */
    public double getMaxX()
    {
        // handle this special case
        if (getSizeX() == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getX() + getSizeX();
    }

    /**
     * Returns the largest Y coordinate of the rectangle.
     */
    public double getMaxY()
    {
        // handle this special case
        if (getSizeY() == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getY() + getSizeY();
    }

    /**
     * Returns the largest Z coordinate of the rectangle.
     */
    public double getMaxZ()
    {
        // handle this special case
        if (getSizeZ() == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getZ() + getSizeZ();
    }

    /**
     * Returns the X coordinate of the center of the rectangle.
     */
    public double getCenterX()
    {
        // handle this special case
        if (isInfiniteX())
            return 0d;

        return getX() + (getSizeX() / 2d);
    }

    /**
     * Returns the Y coordinate of the center of the rectangle.
     */
    public double getCenterY()
    {
        // handle this special case
        if (isInfiniteY())
            return 0d;

        return getY() + (getSizeY() / 2d);
    }

    /**
     * Returns the Z coordinate of the center of the rectangle.
     */
    public double getCenterZ()
    {
        // handle this special case
        if (isInfiniteZ())
            return 0d;

        return getZ() + (getSizeZ() / 2d);
    }

    /**
     * Determines whether the <code>Rectangle5D</code> is empty.
     * 
     * @return <code>true</code> if the <code>Rectangle5D</code> is empty; <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return (getSizeX() <= 0d) || (getSizeY() <= 0d) || (getSizeZ() <= 0d);
    }

    /**
     * Returns <code>true</code> if the X dimension should be considered as infinite.
     */
    public boolean isInfiniteX()
    {
        return (getX() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeX() == java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Returns <code>true</code> if the Y dimension should be considered as infinite.
     */
    public boolean isInfiniteY()
    {
        return (getY() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeY() == java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Returns <code>true</code> if the Z dimension should be considered as infinite.
     */
    public boolean isInfiniteZ()
    {
        return (getZ() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeZ() == java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Sets the X dimension to infinite.
     */
    public void setInfiniteX()
    {
        setX(java.lang.Double.NEGATIVE_INFINITY);
        setSizeX(java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Sets the Y dimension to infinite.
     */
    public void setInfiniteY()
    {
        setY(java.lang.Double.NEGATIVE_INFINITY);
        setSizeY(java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Sets the Z dimension to infinite.
     */
    public void setInfiniteZ()
    {
        setZ(java.lang.Double.NEGATIVE_INFINITY);
        setSizeZ(java.lang.Double.POSITIVE_INFINITY);
    }

    @Override
    public Rectangle3D getBounds()
    {
        return new Rectangle3D.Double(this);
    }

    /**
     * Tests if the specified coordinates are inside the boundary of the <code>Rectangle3D</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside
     *         the <code>Rectangle3D</code> boundary; <code>false</code> otherwise.
     */
    @Override
    public boolean contains(double x, double y, double z)
    {
        return (x >= getMinX()) && (y >= getMinY()) && (z >= getMinZ()) && (x < getMaxX()) && (y < getMaxY())
                && (z < getMaxZ());
    }

    @Override
    public boolean contains(Point3D p)
    {
        return contains(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Tests if the <code>Rectangle3D</code> entirely contains the specified 3D rectangular area.
     * All coordinates that lie inside the 3D rectangular area must lie within the <code>Rectangle3D</code>.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @return <code>true</code> if the <code>Rectangle3D</code> entirely contains the
     *         specified 3D rectangular area; <code>false</code> otherwise
     * @see #intersects
     */
    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        final double maxX;
        final double maxY;
        final double maxZ;

        // special infinite case
        if (sizeX == java.lang.Double.POSITIVE_INFINITY)
            maxX = java.lang.Double.POSITIVE_INFINITY;
        else
            maxX = x + sizeX;
        // special infinite case
        if (sizeY == java.lang.Double.POSITIVE_INFINITY)
            maxY = java.lang.Double.POSITIVE_INFINITY;
        else
            maxY = y + sizeY;
        // special infinite case
        if (sizeZ == java.lang.Double.POSITIVE_INFINITY)
            maxZ = java.lang.Double.POSITIVE_INFINITY;
        else
            maxZ = z + sizeZ;

        return (x >= getMinX()) && (y >= getMinY()) && (z >= getMinZ()) && (maxX <= getMaxX()) && (maxY <= getMaxY())
                && (maxZ <= getMaxZ());
    }

    /**
     * Tests if the <code>Rectangle3D</code> entirely contains the specified <code>Rectangle3D</code>.
     * 
     * @see #contains(double, double, double, double, double, double)
     */
    @Override
    public boolean contains(Rectangle3D rect)
    {
        return contains(rect.getX(), rect.getY(), rect.getZ(), rect.getSizeX(), rect.getSizeY(), rect.getSizeZ());
    }

    /**
     * Tests if the interior of the <code>Rectangle3D</code> intersects the interior of a specified
     * 3D rectangular area.<br>
     * The 3D rectangular area is considered to intersect the <code>Rectangle3D</code> if any point
     * is contained in both the interior of the <code>Rectangle3D</code> and the specified
     * rectangular area.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Rectangle3D</code> and
     *         the interior of the 3D rectangular area intersect.
     */
    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        final double maxX;
        final double maxY;
        final double maxZ;

        // special infinite case
        if (sizeX == java.lang.Double.POSITIVE_INFINITY)
            maxX = java.lang.Double.POSITIVE_INFINITY;
        else
            maxX = x + sizeX;
        // special infinite case
        if (sizeY == java.lang.Double.POSITIVE_INFINITY)
            maxY = java.lang.Double.POSITIVE_INFINITY;
        else
            maxY = y + sizeY;
        // special infinite case
        if (sizeZ == java.lang.Double.POSITIVE_INFINITY)
            maxZ = java.lang.Double.POSITIVE_INFINITY;
        else
            maxZ = z + sizeZ;

        return (maxX > getMinX()) && (maxY > getMinY()) && (maxZ > getMinZ()) && (x < getMaxX()) && (y < getMaxY())
                && (z < getMaxZ());
    }

    /**
     * Tests if the interior of the <code>Rectangle3D</code> intersects the interior of a specified
     * <code>Rectangle3D</code>.<br>
     * 
     * @see #intersects(double, double, double, double, double, double)
     */
    @Override
    public boolean intersects(Rectangle3D rect)
    {
        return intersects(rect.getX(), rect.getY(), rect.getZ(), rect.getSizeX(), rect.getSizeY(), rect.getSizeZ());
    }

    /**
     * Tests if the specified 3D line intersects this <code>Rectangle3D</code>.
     */

    public boolean intersectsLine(Line3D line)
    {
        return intersects(line, this);
    }

    /**
     * Tests if the line specified by the given starting and ending Point intersects the plan defined by this
     * <code>Rectangle3D</code>.
     */

    public boolean intersectsLine(Point3D pt1, Point3D pt2)
    {
        return intersects(new Line3D(pt1, pt2), this);
    }

    /**
     * Tests if the specified 3D line intersects this <code>Rectangle3D</code>.
     */

    public boolean intersectsLine(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return intersects(new Line3D(x1, y1, z1, x2, y2, z2), this);
    }

    /**
     * Adds a 3D point, specified by the double precision coordinates arguments, to this <code>Rectangle3D</code>. The
     * resulting <code>Rectangle3D</code> is the smallest <code>Rectangle3D</code> that contains both the original
     * <code>Rectangle3D</code> and the
     * specified 3D point.
     * <p>
     * After adding a 3D point, a call to <code>contains</code> with the added point as an argument does not necessarily
     * return <code>true</code>. The <code>contains</code> method does not return <code>true</code> for points on the
     * edges of a rectangle. Therefore, if the added 3D point falls on edge of the enlarged rectangle,
     * <code>contains</code> returns <code>false</code> for that point.
     * 
     * @param newx
     *        the X coordinate of the new point
     * @param newy
     *        the Y coordinate of the new point
     * @param newz
     *        the Z coordinate of the new point
     */
    public void add(double newx, double newy, double newz)
    {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        double z1 = Math.min(getMinZ(), newz);
        double z2 = Math.max(getMaxZ(), newz);

        double dx;
        double dy;
        double dz;

        // special infinite case
        if (x2 == java.lang.Double.POSITIVE_INFINITY)
            dx = java.lang.Double.POSITIVE_INFINITY;
        else
            dx = x2 - x1;
        // special infinite case
        if (y2 == java.lang.Double.POSITIVE_INFINITY)
            dy = java.lang.Double.POSITIVE_INFINITY;
        else
            dy = y2 - y1;
        // special infinite case
        if (z2 == java.lang.Double.POSITIVE_INFINITY)
            dz = java.lang.Double.POSITIVE_INFINITY;
        else
            dz = z2 - z1;

        setRect(x1, y1, z1, dx, dy, dz);
    }

    /**
     * Adds the <code>Point3D</code> object <code>pt</code> to this <code>Rectangle3D</code>.
     * The resulting <code>Rectangle3D</code> is the smallest <code>Rectangle3D</code> that contains
     * both the original <code>Rectangle3D</code> and the specified <code>Point3D</code>.
     * <p>
     * After adding a point, a call to <code>contains</code> with the added point as an argument does not necessarily
     * return <code>true</code>. The <code>contains</code> method does not return <code>true</code> for points on the
     * edges of a rectangle. Therefore, if the added point falls on edge of the enlarged rectangle,
     * <code>contains</code> returns <code>false</code> for that point.
     * 
     * @param pt
     *        the new <code>Point3D</code> to add to this <code>Rectangle3D</code>.
     */
    public void add(Point3D pt)
    {
        add(pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Adds a <code>Rectangle3D</code> object to this <code>Rectangle3D</code>. The resulting <code>Rectangle3D</code>
     * is the union of the two <code>Rectangle3D</code> objects.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to add to this <code>Rectangle3D</code>.
     */
    public void add(Rectangle3D r)
    {
        union(this, r, this);
    }

    /**
     * Convert to 2D rectangle
     */
    public abstract Rectangle2D toRectangle2D();

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof Rectangle3D)
        {
            final Rectangle3D rect = (Rectangle3D) obj;
            return (getX() == rect.getX()) && (getY() == rect.getY()) && (getZ() == rect.getZ())
                    && (getSizeX() == rect.getSizeX()) && (getSizeY() == rect.getSizeY())
                    && (getSizeZ() == rect.getSizeZ());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY());
        bits ^= java.lang.Double.doubleToLongBits(getZ());
        bits ^= java.lang.Double.doubleToLongBits(getSizeX());
        bits ^= java.lang.Double.doubleToLongBits(getSizeY());
        bits ^= java.lang.Double.doubleToLongBits(getSizeZ());
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Creates a new object of the same class as this object.
     * 
     * @return a clone of this instance.
     * @exception OutOfMemoryError
     *            if there is not enough memory.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "[" + getX() + "," + getY() + "," + getZ() + " - " + getSizeX() + ","
                + getSizeY() + "," + getSizeZ() + "]";
    }

    public static class Double extends Rectangle3D
    {
        public double x;
        public double y;
        public double z;

        public double sizeX;
        public double sizeY;
        public double sizeZ;

        public Double(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Double(Rectangle3D r)
        {
            this(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
        }

        public Double()
        {
            this(0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double value)
        {
            x = value;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double value)
        {
            y = value;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double value)
        {
            z = value;
        }

        @Override
        public double getSizeX()
        {
            return sizeX;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = value;
        }

        @Override
        public double getSizeY()
        {
            return sizeY;
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = value;
        }

        @Override
        public double getSizeZ()
        {
            return sizeZ;
        }

        @Override
        public void setSizeZ(double value)
        {
            sizeZ = value;
        }

        @Override
        public Point3D.Double getPosition()
        {
            return new Point3D.Double(x, y, z);
        }

        @Override
        public Dimension3D.Double getDimension()
        {
            return new Dimension3D.Double(sizeX, sizeY, sizeZ);
        }

        @Override
        public Rectangle3D createIntersection(Rectangle3D r)
        {
            final Rectangle3D.Double result = new Rectangle3D.Double();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle3D createUnion(Rectangle3D r)
        {
            final Rectangle3D.Double result = new Rectangle3D.Double();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle2D.Double(x, y, sizeX, sizeY);
        }
    }

    public static class Float extends Rectangle3D
    {
        public float x;
        public float y;
        public float z;

        public float sizeX;
        public float sizeY;
        public float sizeZ;

        public Float(float x, float y, float z, float sizeX, float sizeY, float sizeZ)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Float(Rectangle3D r)
        {
            this((float) r.getX(), (float) r.getY(), (float) r.getZ(), (float) r.getSizeX(), (float) r.getSizeY(),
                    (float) r.getSizeZ());
        }

        public Float()
        {
            this(0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
        }

        @Override
        public double getX()
        {
            // special infinite case
            if (x == java.lang.Float.NEGATIVE_INFINITY)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (x == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return x;
        }

        @Override
        public void setX(double value)
        {
            x = (float) value;
        }

        @Override
        public double getY()
        {
            // special infinite case
            if (y == java.lang.Float.NEGATIVE_INFINITY)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (y == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return y;
        }

        @Override
        public void setY(double value)
        {
            y = (float) value;
        }

        @Override
        public double getZ()
        {
            // special infinite case
            if (z == java.lang.Float.NEGATIVE_INFINITY)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (z == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return z;
        }

        @Override
        public void setZ(double value)
        {
            z = (float) value;
        }

        @Override
        public double getSizeX()
        {
            // special infinite case
            if (sizeX == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeX;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = (float) value;
        }

        @Override
        public double getSizeY()
        {
            // special infinite case
            if (sizeY == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeY;
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = (float) value;
        }

        @Override
        public double getSizeZ()
        {
            // special infinite case
            if (sizeZ == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeZ;
        }

        @Override
        public void setSizeZ(double value)
        {
            sizeZ = (float) value;
        }

        @Override
        public Point3D.Float getPosition()
        {
            return new Point3D.Float(x, y, z);
        }

        @Override
        public Dimension3D.Float getDimension()
        {
            return new Dimension3D.Float(sizeX, sizeY, sizeZ);
        }

        @Override
        public Rectangle3D createIntersection(Rectangle3D r)
        {
            final Rectangle3D.Float result = new Rectangle3D.Float();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle3D createUnion(Rectangle3D r)
        {
            final Rectangle3D.Float result = new Rectangle3D.Float();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle2D.Float(x, y, sizeX, sizeY);
        }
    }

    public static class Integer extends Rectangle3D
    {
        public int x;
        public int y;
        public int z;

        public int sizeX;
        public int sizeY;
        public int sizeZ;

        public Integer(int x, int y, int z, int sizeX, int sizeY, int sizeZ)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Integer(Rectangle3D.Integer r)
        {
            this(r.x, r.y, r.z, r.sizeX, r.sizeY, r.sizeZ);
        }

        public Integer(Rectangle3D r)
        {
            this(r.toInteger());
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0, 0);
        }

        /**
         * Sets the bounds of this {@code Rectangle3D} to the integer bounds
         * which encompass the specified double bounds.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle3D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle3D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle3D</code>
         */
        @Override
        public void setRect(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
        {
            final Rectangle3D.Integer r = new Rectangle3D.Double(x, y, z, sizeX, sizeY, sizeZ).toInteger();
            setRect(r.x, r.y, r.z, r.sizeX, r.sizeY, r.sizeZ);
        }

        /**
         * Sets the position and size of this <code>Rectangle3D</code> to the specified <code>integer</code> values.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle3D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle3D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle3D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle3D</code>
         */
        public void setRect(int x, int y, int z, int sizeX, int sizeY, int sizeZ)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        @Override
        public double getX()
        {
            // special infinite case
            if (x == java.lang.Integer.MIN_VALUE)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (x == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return x;
        }

        @Override
        public void setX(double value)
        {
            x = (int) value;
        }

        @Override
        public double getY()
        {
            // special infinite case
            if (y == java.lang.Integer.MIN_VALUE)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (y == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return y;
        }

        @Override
        public void setY(double value)
        {
            y = (int) value;
        }

        @Override
        public double getZ()
        {
            // special infinite case
            if (z == java.lang.Integer.MIN_VALUE)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (z == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return z;
        }

        @Override
        public void setZ(double value)
        {
            z = (int) value;
        }

        @Override
        public double getSizeX()
        {
            // special infinite case
            if (sizeX == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeX;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = (int) value;
        }

        @Override
        public double getSizeY()
        {
            // special infinite case
            if (sizeY == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeY;
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = (int) value;
        }

        @Override
        public double getSizeZ()
        {
            // special infinite case
            if (sizeZ == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeZ;
        }

        @Override
        public void setSizeZ(double value)
        {
            sizeZ = (int) value;
        }

        @Override
        public Point3D.Integer getPosition()
        {
            return new Point3D.Integer(x, y, z);
        }

        @Override
        public Dimension3D.Integer getDimension()
        {
            return new Dimension3D.Integer(sizeX, sizeY, sizeZ);
        }

        @Override
        public Rectangle3D.Integer toInteger()
        {
            return (Integer) clone();
        }

        @Override
        public Rectangle3D createIntersection(Rectangle3D r)
        {
            final Rectangle3D.Integer result = new Rectangle3D.Integer();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle3D createUnion(Rectangle3D r)
        {
            final Rectangle3D.Integer result = new Rectangle3D.Integer();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle(x, y, sizeX, sizeY);
        }
    }

}
