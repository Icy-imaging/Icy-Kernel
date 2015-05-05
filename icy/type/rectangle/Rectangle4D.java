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
package icy.type.rectangle;

import icy.type.dimension.Dimension4D;
import icy.type.point.Point4D;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Rectangle4D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle4D implements Cloneable
{
    /**
     * Intersects the pair of specified source <code>Rectangle4D</code> objects and puts the result
     * into the specified destination <code>Rectangle4D</code> object. One of the source rectangles
     * can also be the destination to avoid creating a third Rectangle4D object, but in this case
     * the original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle4D</code> objects to be intersected with each
     *        other
     * @param src2
     *        the second of a pair of <code>Rectangle4D</code> objects to be intersected with each
     *        other
     * @param dest
     *        the <code>Rectangle4D</code> that holds the
     *        results of the intersection of <code>src1</code> and <code>src2</code>
     */
    public static Rectangle4D intersect(Rectangle4D src1, Rectangle4D src2, Rectangle4D dest)
    {
        final Rectangle4D result;

        if (dest == null)
            result = new Rectangle4D.Double();
        else
            result = dest;

        final double x1 = Math.max(src1.getMinX(), src2.getMinX());
        final double y1 = Math.max(src1.getMinY(), src2.getMinY());
        final double z1 = Math.max(src1.getMinZ(), src2.getMinZ());
        final double t1 = Math.max(src1.getMinT(), src2.getMinT());
        final double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        final double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        final double z2 = Math.min(src1.getMaxZ(), src2.getMaxZ());
        final double t2 = Math.min(src1.getMaxT(), src2.getMaxT());

        double dx;
        double dy;
        double dz;
        double dt;

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
        // special infinite case
        if (t2 == java.lang.Double.POSITIVE_INFINITY)
            dt = java.lang.Double.POSITIVE_INFINITY;
        else
            dt = t2 - t1;

        result.setRect(x1, y1, z1, t1, dx, dy, dz, dt);

        return result;
    }

    /**
     * Returns a new <code>Rectangle4D</code> object representing the intersection of this
     * <code>Rectangle4D</code> with the specified <code>Rectangle4D</code>.
     * 
     * @param r
     *        the <code>Rectangle4D</code> to be intersected with this <code>Rectangle4D</code>
     * @return the largest <code>Rectangle4D</code> contained in both the specified
     *         <code>Rectangle4D</code> and in this <code>Rectangle4D</code>.
     */
    public abstract Rectangle4D createIntersection(Rectangle4D r);

    /**
     * Unions the pair of source <code>Rectangle4D</code> objects and puts the result into the
     * specified destination <code>Rectangle4D</code> object. One of the source rectangles can also
     * be the destination to avoid creating a third Rectangle4D object, but in this case the
     * original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle4D</code> objects to be combined with each other
     * @param src2
     *        the second of a pair of <code>Rectangle4D</code> objects to be combined with each
     *        other
     * @param dest
     *        the <code>Rectangle4D</code> that holds the
     *        results of the union of <code>src1</code> and <code>src2</code>
     */
    public static Rectangle4D union(Rectangle4D src1, Rectangle4D src2, Rectangle4D dest)
    {
        final Rectangle4D result;

        if (dest == null)
            result = new Rectangle4D.Double();
        else
            result = dest;

        double x1 = Math.min(src1.getMinX(), src2.getMinX());
        double y1 = Math.min(src1.getMinY(), src2.getMinY());
        double z1 = Math.min(src1.getMinZ(), src2.getMinZ());
        double t1 = Math.min(src1.getMinT(), src2.getMinT());
        double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
        double z2 = Math.max(src1.getMaxZ(), src2.getMaxZ());
        double t2 = Math.max(src1.getMaxT(), src2.getMaxT());

        double dx;
        double dy;
        double dz;
        double dt;

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
        // special infinite case
        if (t2 == java.lang.Double.POSITIVE_INFINITY)
            dt = java.lang.Double.POSITIVE_INFINITY;
        else
            dt = t2 - t1;

        result.setRect(x1, y1, z1, t1, dx, dy, dz, dt);

        return result;
    }

    /**
     * Returns a new <code>Rectangle4D</code> object representing the union of this
     * <code>Rectangle4D</code> with the specified <code>Rectangle4D</code>.
     * 
     * @param r
     *        the <code>Rectangle4D</code> to be combined with this <code>Rectangle4D</code>
     * @return the smallest <code>Rectangle4D</code> containing both the specified
     *         <code>Rectangle4D</code> and this <code>Rectangle4D</code>.
     */
    public abstract Rectangle4D createUnion(Rectangle4D r);

    /**
     * Sets the position and size of this <code>Rectangle4D</code> to the specified
     * <code>double</code> values.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of this <code>Rectangle4D</code>
     * @param y
     *        the Y coordinate of the minimum corner position of this <code>Rectangle4D</code>
     * @param z
     *        the Z coordinate of the minimum corner position of this <code>Rectangle4D</code>
     * @param t
     *        the T coordinate of the minimum corner position of this <code>Rectangle4D</code>
     * @param sizeX
     *        size for X dimension of this <code>Rectangle4D</code>
     * @param sizeY
     *        size for Y dimension of this <code>Rectangle4D</code>
     * @param sizeZ
     *        size for Z dimension of this <code>Rectangle4D</code>
     * @param sizeT
     *        size for T dimension of this <code>Rectangle4D</code>
     */
    public abstract void setRect(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT);

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
     * Returns the minimum T coordinate.
     */
    public abstract double getT();

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
     * Returns the size of T dimension.
     */
    public abstract double getSizeT();

    /**
     * Returns the point coordinates.
     */
    public abstract Point4D getPosition();

    /**
     * Returns the dimension.
     */
    public abstract Dimension4D getDimension();

    /**
     * Returns an integer {@link Rectangle4D} that completely encloses the
     * double <code>Rectangle</code>. The returned <code>Rectangle</code> might also fail to
     * completely enclose the original double <code>Rectangle</code> if it overflows
     * the limited range of the integer data type.
     * 
     * @return an integer <code>Rectangle</code> that completely encloses
     *         the actual double <code>Rectangle</code>.
     */
    public Rectangle4D.Integer toInteger()
    {
        double sx = getSizeX();
        double sy = getSizeY();
        double sz = getSizeZ();
        double st = getSizeT();
        double x = getX();
        double y = getY();
        double z = getZ();
        double t = getT();
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        int it = (int) Math.floor(t);
        int isx;
        int isy;
        int isz;
        int ist;

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
        if (st < 0d)
            ist = 0;
        else if (st >= java.lang.Integer.MAX_VALUE)
            ist = java.lang.Integer.MAX_VALUE;
        else
            ist = ((int) Math.ceil(t + st)) - it;

        return new Rectangle4D.Integer(ix, iy, iz, it, isx, isy, isz, ist);
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
     * Sets the minimum T coordinate.
     */
    public abstract void setT(double t);

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
     * Sets the size of T dimension.
     */
    public abstract void setSizeT(double value);

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
     * Returns the smallest T coordinate of the rectangle.
     */
    public double getMinT()
    {
        return getT();
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
     * Returns the largest T coordinate of the rectangle.
     */
    public double getMaxT()
    {
        // handle this special case
        if (getSizeT() == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getT() + getSizeT();
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
     * Returns the T coordinate of the center of the rectangle.
     */
    public double getCenterT()
    {
        // handle this special case
        if (isInfiniteT())
            return 0d;

        return getT() + (getSizeT() / 2d);
    }

    /**
     * Determines whether the <code>Rectangle5D</code> is empty.
     * 
     * @return <code>true</code> if the <code>Rectangle5D</code> is empty; <code>false</code>
     *         otherwise.
     */
    public boolean isEmpty()
    {
        return (getSizeX() <= 0d) || (getSizeY() <= 0d) || (getSizeZ() <= 0d) || (getSizeT() <= 0d);
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
     * Returns <code>true</code> if the T dimension should be considered as infinite.
     */
    public boolean isInfiniteT()
    {
        return (getT() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeT() == java.lang.Double.POSITIVE_INFINITY);
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

    /**
     * Sets the T dimension to infinite.
     */
    public void setInfiniteT()
    {
        setT(java.lang.Double.NEGATIVE_INFINITY);
        setSizeT(java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Tests if the specified coordinates are inside the boundary of the <code>Rectangle4D</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @param t
     *        the specified T coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside
     *         the <code>Rectangle4D</code> boundary; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double z, double t)
    {
        return (x >= getMinX()) && (y >= getMaxY()) && (z >= getMinZ()) && (t >= getMinT()) && (x < getMaxX())
                && (y < getMaxY()) && (z < getMaxZ()) && (t < getMaxT());
    }

    /**
     * Tests if the <code>Rectangle4D</code> entirely contains the specified 4D rectangular area.<br>
     * All coordinates that lie inside the 4D rectangular area must lie within the
     * <code>Rectangle4D</code>.
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
     * @return <code>true</code> if the <code>Rectangle4D</code> entirely contains the
     *         specified 4D rectangular area; <code>false</code> otherwise
     * @see #intersects
     */
    public boolean contains(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT)
    {
        final double maxX;
        final double maxY;
        final double maxZ;
        final double maxT;

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
        // special infinite case
        if (sizeT == java.lang.Double.POSITIVE_INFINITY)
            maxT = java.lang.Double.POSITIVE_INFINITY;
        else
            maxT = t + sizeT;

        return (x >= getMinX()) && (y >= getMinY()) && (z >= getMinZ()) && (t >= getMinT()) && (maxX <= getMaxX())
                && (maxY <= getMaxY()) && (maxZ <= getMaxZ()) && (maxT <= getMaxT());
    }

    /**
     * Tests if the <code>Rectangle4D</code> entirely contains the specified
     * <code>Rectangle4D</code>.
     * 
     * @see #contains(double, double, double, double, double, double, double, double)
     */
    public boolean contains(Rectangle4D rect)
    {
        return contains(rect.getX(), rect.getY(), rect.getZ(), rect.getT(), rect.getSizeX(), rect.getSizeY(),
                rect.getSizeZ(), rect.getSizeT());
    }

    /**
     * Tests if the interior of the <code>Rectangle4D</code> intersects the interior of a specified
     * 4D rectangular area.<br>
     * The 4D rectangular area is considered to intersect the <code>Rectangle4D</code> if any point
     * is contained in both the interior of the <code>Rectangle4D</code> and the specified
     * rectangular area.
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
     * @return <code>true</code> if the interior of the <code>Rectangle4D</code> and
     *         the interior of the 4D rectangular area intersect.
     */
    public boolean intersects(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT)
    {
        final double maxX;
        final double maxY;
        final double maxZ;
        final double maxT;

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
        // special infinite case
        if (sizeT == java.lang.Double.POSITIVE_INFINITY)
            maxT = java.lang.Double.POSITIVE_INFINITY;
        else
            maxT = t + sizeT;

        return (maxX > getMinX()) && (maxY > getMinY()) && (maxZ > getMinZ()) && (maxT > getMinT()) && (x < getMaxX())
                && (y < getMaxY()) && (z < getMaxZ()) && (t < getMaxT());
    }

    /**
     * Tests if the interior of the <code>Rectangle4D</code> intersects the interior of a specified
     * <code>Rectangle4D</code>.<br>
     * 
     * @see #intersects(double, double, double, double, double, double, double, double)
     */
    public boolean intersects(Rectangle4D rect)
    {
        return intersects(rect.getX(), rect.getY(), rect.getZ(), rect.getT(), rect.getSizeX(), rect.getSizeY(),
                rect.getSizeZ(), rect.getSizeT());
    }

    /**
     * Adds a 4D point, specified by the double precision coordinates arguments, to this
     * <code>Rectangle4D</code>. The resulting <code>Rectangle4D</code> is the smallest
     * <code>Rectangle4D</code> that contains both the original <code>Rectangle4D</code> and the
     * specified 4D point.
     * <p>
     * After adding a 4D point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added 4D
     * point falls on edge of the enlarged rectangle, <code>contains</code> returns
     * <code>false</code> for that point.
     * 
     * @param newx
     *        the X coordinate of the new point
     * @param newy
     *        the Y coordinate of the new point
     * @param newz
     *        the Z coordinate of the new point
     * @param newt
     *        the T coordinate of the new point
     */
    public void add(double newx, double newy, double newz, double newt)
    {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        double z1 = Math.min(getMinZ(), newz);
        double z2 = Math.max(getMaxZ(), newz);
        double t1 = Math.min(getMinT(), newt);
        double t2 = Math.max(getMaxT(), newt);

        double dx;
        double dy;
        double dz;
        double dt;

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
        // special infinite case
        if (t2 == java.lang.Double.POSITIVE_INFINITY)
            dt = java.lang.Double.POSITIVE_INFINITY;
        else
            dt = t2 - t1;

        setRect(x1, y1, z1, t1, dx, dy, dz, dt);
    }

    /**
     * Adds the <code>Point4D</code> object <code>pt</code> to this <code>Rectangle4D</code>.
     * The resulting <code>Rectangle4D</code> is the smallest <code>Rectangle4D</code> that contains
     * both the original <code>Rectangle4D</code> and the specified <code>Point4D</code>.
     * <p>
     * After adding a point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added
     * point falls on edge of the enlarged rectangle, <code>contains</code> returns
     * <code>false</code> for that point.
     * 
     * @param pt
     *        the new <code>Point4D</code> to add to this <code>Rectangle4D</code>.
     */
    public void add(Point4D pt)
    {
        add(pt.getX(), pt.getY(), pt.getZ(), pt.getT());
    }

    /**
     * Adds a <code>Rectangle4D</code> object to this <code>Rectangle4D</code>. The resulting
     * <code>Rectangle4D</code> is the union of the two <code>Rectangle4D</code> objects.
     * 
     * @param r
     *        the <code>Rectangle4D</code> to add to this <code>Rectangle4D</code>.
     */
    public void add(Rectangle4D r)
    {
        union(this, r, this);
    }

    /**
     * Convert to 2D rectangle
     */
    public abstract Rectangle2D toRectangle2D();

    /**
     * Convert to 3D rectangle
     */
    public abstract Rectangle3D toRectangle3D();

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof Rectangle4D)
        {
            final Rectangle4D rect = (Rectangle4D) obj;
            return (getX() == rect.getX()) && (getY() == rect.getY()) && (getZ() == rect.getZ())
                    && (getT() == rect.getT()) && (getSizeX() == rect.getSizeX()) && (getSizeY() == rect.getSizeY())
                    && (getSizeZ() == rect.getSizeZ()) && (getSizeT() == rect.getSizeT());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY());
        bits ^= java.lang.Double.doubleToLongBits(getZ());
        bits ^= java.lang.Double.doubleToLongBits(getT());
        bits ^= java.lang.Double.doubleToLongBits(getSizeX());
        bits ^= java.lang.Double.doubleToLongBits(getSizeY());
        bits ^= java.lang.Double.doubleToLongBits(getSizeZ());
        bits ^= java.lang.Double.doubleToLongBits(getSizeT());
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
        return getClass().getName() + "[" + getX() + "," + getY() + "," + getZ() + "," + getT() + " - " + getSizeX()
                + "," + getSizeY() + "," + getSizeZ() + "," + getSizeT() + "]";
    }

    public static class Double extends Rectangle4D
    {
        public double x;
        public double y;
        public double z;
        public double t;

        public double sizeX;
        public double sizeY;
        public double sizeZ;
        public double sizeT;

        public Double(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ, double sizeT)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Double(Rectangle4D r)
        {
            this(r.getX(), r.getY(), r.getZ(), r.getT(), r.getSizeX(), r.getSizeY(), r.getSizeZ(), r.getSizeT());
        }

        public Double()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
                double sizeT)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
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
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double value)
        {
            t = value;
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
        public double getSizeT()
        {
            return sizeT;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = value;
        }

        @Override
        public Point4D.Double getPosition()
        {
            return new Point4D.Double(x, y, z, t);
        }

        @Override
        public Dimension4D.Double getDimension()
        {
            return new Dimension4D.Double(sizeX, sizeY, sizeZ, sizeT);
        }

        @Override
        public Rectangle4D createIntersection(Rectangle4D r)
        {
            final Rectangle4D.Double result = new Rectangle4D.Double();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle4D createUnion(Rectangle4D r)
        {
            final Rectangle4D.Double result = new Rectangle4D.Double();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle2D.Double(x, y, sizeX, sizeY);
        }

        @Override
        public Rectangle3D toRectangle3D()
        {
            return new Rectangle3D.Double(x, y, z, sizeX, sizeY, sizeZ);
        }
    }

    public static class Float extends Rectangle4D
    {
        public float x;
        public float y;
        public float z;
        public float t;

        public float sizeX;
        public float sizeY;
        public float sizeZ;
        public float sizeT;

        public Float(float x, float y, float z, float t, float sizeX, float sizeY, float sizeZ, float sizeT)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Float(Rectangle4D r)
        {
            this((float) r.getX(), (float) r.getY(), (float) r.getZ(), (float) r.getT(), (float) r.getSizeX(),
                    (float) r.getSizeY(), (float) r.getSizeZ(), (float) r.getSizeT());
        }

        public Float()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
                double sizeT)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.t = (float) t;
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
            this.sizeT = (float) sizeT;
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
        public double getT()
        {
            // special infinite case
            if (t == java.lang.Float.NEGATIVE_INFINITY)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (t == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return t;
        }

        @Override
        public void setT(double value)
        {
            t = (float) value;
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
        public double getSizeT()
        {
            // special infinite case
            if (sizeT == java.lang.Float.POSITIVE_INFINITY)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeT;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (float) value;
        }

        @Override
        public Point4D.Float getPosition()
        {
            return new Point4D.Float(x, y, z, t);
        }

        @Override
        public Dimension4D.Float getDimension()
        {
            return new Dimension4D.Float(sizeX, sizeY, sizeZ, sizeT);
        }

        @Override
        public Rectangle4D createIntersection(Rectangle4D r)
        {
            final Rectangle4D.Float result = new Rectangle4D.Float();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle4D createUnion(Rectangle4D r)
        {
            final Rectangle4D.Float result = new Rectangle4D.Float();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle2D.Float(x, y, sizeX, sizeY);
        }

        @Override
        public Rectangle3D toRectangle3D()
        {
            return new Rectangle3D.Float(x, y, z, sizeX, sizeY, sizeZ);
        }
    }

    public static class Integer extends Rectangle4D
    {
        public int x;
        public int y;
        public int z;
        public int t;

        public int sizeX;
        public int sizeY;
        public int sizeZ;
        public int sizeT;

        public Integer(int x, int y, int z, int t, int sizeX, int sizeY, int sizeZ, int sizeT)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Integer(Rectangle4D.Integer r)
        {
            this(r.x, r.y, r.z, r.t, r.sizeX, r.sizeY, r.sizeZ, r.sizeT);
        }

        public Integer(Rectangle4D r)
        {
            this(r.toInteger());
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }

        /**
         * Sets the bounds of this {@code Rectangle4D} to the integer bounds
         * which encompass the specified double bounds.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param t
         *        the T coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle4D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle4D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle4D</code>
         * @param sizeT
         *        size for T dimension of this <code>Rectangle4D</code>
         */
        @Override
        public void setRect(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
                double sizeT)
        {
            final Rectangle4D.Integer r = new Rectangle4D.Double(x, y, z, t, sizeX, sizeY, sizeZ, sizeT).toInteger();
            setRect(r.x, r.y, r.z, r.t, r.sizeX, r.sizeY, r.sizeZ, r.sizeT);
        }

        /**
         * Sets the position and size of this <code>Rectangle4D</code> to the specified
         * <code>integer</code> values.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param t
         *        the T coordinate of the minimum corner position of this <code>Rectangle4D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle4D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle4D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle4D</code>
         * @param sizeT
         *        size for T dimension of this <code>Rectangle4D</code>
         */
        public void setRect(int x, int y, int z, int t, int sizeX, int sizeY, int sizeZ, int sizeT)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
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
        public double getT()
        {
            // special infinite case
            if (t == java.lang.Integer.MIN_VALUE)
                return java.lang.Double.NEGATIVE_INFINITY;
            if (t == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return t;
        }

        @Override
        public void setT(double value)
        {
            t = (int) value;
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
        public double getSizeT()
        {
            // special infinite case
            if (sizeT == java.lang.Integer.MAX_VALUE)
                return java.lang.Double.POSITIVE_INFINITY;

            return sizeT;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (int) value;
        }

        @Override
        public Point4D.Integer getPosition()
        {
            return new Point4D.Integer(x, y, z, t);
        }

        @Override
        public Dimension4D.Integer getDimension()
        {
            return new Dimension4D.Integer(sizeX, sizeY, sizeZ, sizeT);
        }

        @Override
        public Rectangle4D.Integer toInteger()
        {
            return (Integer) clone();
        }

        @Override
        public Rectangle4D createIntersection(Rectangle4D r)
        {
            final Rectangle4D.Integer result = new Rectangle4D.Integer();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle4D createUnion(Rectangle4D r)
        {
            final Rectangle4D.Integer result = new Rectangle4D.Integer();

            union(this, r, result);

            return result;
        }

        @Override
        public Rectangle2D toRectangle2D()
        {
            return new Rectangle(x, y, sizeX, sizeY);
        }

        @Override
        public Rectangle3D toRectangle3D()
        {
            return new Rectangle3D.Integer(x, y, z, sizeX, sizeY, sizeZ);
        }
    }
}
