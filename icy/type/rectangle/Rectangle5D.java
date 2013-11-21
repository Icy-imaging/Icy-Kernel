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

import icy.type.dimension.Dimension5D;
import icy.type.point.Point5D;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Rectangle5D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle5D implements Cloneable
{
    /**
     * Intersects the pair of specified source <code>Rectangle5D</code> objects and puts the result
     * into the specified destination <code>Rectangle5D</code> object. One of the source rectangles
     * can also be the destination to avoid creating a third Rectangle5D object, but in this case
     * the original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle5D</code> objects to be intersected with each
     *        other
     * @param src2
     *        the second of a pair of <code>Rectangle5D</code> objects to be intersected with each
     *        other
     * @param dest
     *        the <code>Rectangle5D</code> that holds the
     *        results of the intersection of <code>src1</code> and <code>src2</code>.<br>
     *        If set to <code>null</code> then a new Rectangle5D is created.
     * @return resulting <code>Rectangle5D</code> from the intersect process.
     */
    public static Rectangle5D intersect(Rectangle5D src1, Rectangle5D src2, Rectangle5D dest)
    {
        final Rectangle5D result;

        if (dest == null)
            result = new Rectangle5D.Double();
        else
            result = dest;

        final double x1 = Math.max(src1.getMinX(), src2.getMinX());
        final double y1 = Math.max(src1.getMinY(), src2.getMinY());
        final double z1 = Math.max(src1.getMinZ(), src2.getMinZ());
        final double t1 = Math.max(src1.getMinT(), src2.getMinT());
        final double c1 = Math.max(src1.getMinC(), src2.getMinC());
        final double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        final double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        final double z2 = Math.min(src1.getMaxZ(), src2.getMaxZ());
        final double t2 = Math.min(src1.getMaxT(), src2.getMaxT());
        final double c2 = Math.min(src1.getMaxC(), src2.getMaxC());

        double dx;
        double dy;
        double dz;
        double dt;
        double dc;

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
        // special infinite case
        if (c2 == java.lang.Double.POSITIVE_INFINITY)
            dc = java.lang.Double.POSITIVE_INFINITY;
        else
            dc = c2 - c1;

        result.setRect(x1, y1, z1, t1, c1, dx, dy, dz, dt, dc);

        return result;
    }

    /**
     * Returns a new <code>Rectangle5D</code> object representing the intersection of this
     * <code>Rectangle5D</code> with the specified <code>Rectangle5D</code>.
     * 
     * @param r
     *        the <code>Rectangle5D</code> to be intersected with this <code>Rectangle5D</code>
     * @return the largest <code>Rectangle5D</code> contained in both the specified
     *         <code>Rectangle5D</code> and in this <code>Rectangle5D</code>.
     */
    public abstract Rectangle5D createIntersection(Rectangle5D r);

    /**
     * Unions the pair of source <code>Rectangle5D</code> objects and puts the result into the
     * specified destination <code>Rectangle5D</code> object. One of the source rectangles can also
     * be the destination to avoid creating a third Rectangle5D object, but in this case the
     * original points of this source rectangle will be overwritten by this method.
     * 
     * @param src1
     *        the first of a pair of <code>Rectangle5D</code> objects to be combined with each other
     * @param src2
     *        the second of a pair of <code>Rectangle5D</code> objects to be combined with each
     *        other
     * @param dest
     *        the <code>Rectangle5D</code> that holds the
     *        results of the union of <code>src1</code> and <code>src2</code>.<br>
     *        If set to <code>null</code> then a new Rectangle5D is created.
     * @return resulting <code>Rectangle5D</code> from the intersect process.
     */
    public static Rectangle5D union(Rectangle5D src1, Rectangle5D src2, Rectangle5D dest)
    {
        final Rectangle5D result;

        if (dest == null)
            result = new Rectangle5D.Double();
        else
            result = dest;

        double x1 = Math.min(src1.getMinX(), src2.getMinX());
        double y1 = Math.min(src1.getMinY(), src2.getMinY());
        double z1 = Math.min(src1.getMinZ(), src2.getMinZ());
        double t1 = Math.min(src1.getMinT(), src2.getMinT());
        double c1 = Math.min(src1.getMinC(), src2.getMinC());
        double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
        double z2 = Math.max(src1.getMaxZ(), src2.getMaxZ());
        double t2 = Math.max(src1.getMaxT(), src2.getMaxT());
        double c2 = Math.max(src1.getMaxC(), src2.getMaxC());

        double dx;
        double dy;
        double dz;
        double dt;
        double dc;

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
        // special infinite case
        if (c2 == java.lang.Double.POSITIVE_INFINITY)
            dc = java.lang.Double.POSITIVE_INFINITY;
        else
            dc = c2 - c1;

        result.setRect(x1, y1, z1, t1, c1, dx, dy, dz, dt, dc);

        return result;
    }

    /**
     * Returns a new <code>Rectangle5D</code> object representing the union of this
     * <code>Rectangle5D</code> with the specified <code>Rectangle5D</code>.
     * 
     * @param r
     *        the <code>Rectangle5D</code> to be combined with this <code>Rectangle5D</code>
     * @return the smallest <code>Rectangle5D</code> containing both the specified
     *         <code>Rectangle5D</code> and this <code>Rectangle5D</code>.
     */
    public abstract Rectangle5D createUnion(Rectangle5D r);

    /**
     * Sets the position and size of this <code>Rectangle5D</code> to the specified
     * <code>double</code> values.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of this <code>Rectangle5D</code>
     * @param y
     *        the Y coordinate of the minimum corner position of this <code>Rectangle5D</code>
     * @param z
     *        the Z coordinate of the minimum corner position of this <code>Rectangle5D</code>
     * @param t
     *        the T coordinate of the minimum corner position of this <code>Rectangle5D</code>
     * @param c
     *        the C coordinate of the minimum corner position of this <code>Rectangle5D</code>
     * @param sizeX
     *        size for X dimension of this <code>Rectangle5D</code>
     * @param sizeY
     *        size for Y dimension of this <code>Rectangle5D</code>
     * @param sizeZ
     *        size for Z dimension of this <code>Rectangle5D</code>
     * @param sizeT
     *        size for T dimension of this <code>Rectangle5D</code>
     * @param sizeC
     *        size for C dimension of this <code>Rectangle5D</code>
     */
    public abstract void setRect(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC);

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
     * Returns the minimum C coordinate.
     */
    public abstract double getC();

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
     * Returns the size of C dimension.
     */
    public abstract double getSizeC();

    /**
     * Returns the point coordinate.
     */
    public Point5D getPosition()
    {
        return new Point5D.Double(getX(), getY(), getZ(), getT(), getC());
    }

    /**
     * Returns the dimension.
     */
    public Dimension5D getDimension()
    {
        return new Dimension5D.Double(getSizeX(), getSizeY(), getSizeZ(), getSizeT(), getSizeC());
    }

    /**
     * Returns an integer {@link Rectangle5D} that completely encloses the
     * double <code>Rectangle</code>. The returned <code>Rectangle</code> might also fail to
     * completely enclose the original double <code>Rectangle</code> if it overflows
     * the limited range of the integer data type.
     * 
     * @return an integer <code>Rectangle</code> that completely encloses
     *         the actual double <code>Rectangle</code>.
     */
    public Rectangle5D.Integer toInteger()
    {
        double sx = getSizeX();
        double sy = getSizeY();
        double sz = getSizeZ();
        double st = getSizeT();
        double sc = getSizeC();
        double x = getX();
        double y = getY();
        double z = getZ();
        double t = getT();
        double c = getC();
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        int it = (int) Math.floor(t);
        int ic = (int) Math.floor(c);
        int isx;
        int isy;
        int isz;
        int ist;
        int isc;

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
        if (sc < 0d)
            isc = 0;
        else if (sc >= java.lang.Integer.MAX_VALUE)
            isc = java.lang.Integer.MAX_VALUE;
        else
            isc = ((int) Math.ceil(c + sc)) - ic;

        return new Rectangle5D.Integer(ix, iy, iz, it, ic, isx, isy, isz, ist, isc);
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
     * Sets the minimum C coordinate.
     */
    public abstract void setC(double c);

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
     * Sets the size of C dimension.
     */
    public abstract void setSizeC(double value);

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
     * Returns the smallest C coordinate of the rectangle.
     */
    public double getMinC()
    {
        return getC();
    }

    /**
     * Returns the largest X coordinate of the rectangle.
     */
    public double getMaxX()
    {
        final double s = getSizeX();

        // handle this special case
        if (s == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getX() + s;
    }

    /**
     * Returns the largest Y coordinate of the rectangle.
     */
    public double getMaxY()
    {
        final double s = getSizeY();

        // handle this special case
        if (s == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getY() + s;
    }

    /**
     * Returns the largest Z coordinate of the rectangle.
     */
    public double getMaxZ()
    {
        final double s = getSizeZ();

        // handle this special case
        if (s == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getZ() + s;
    }

    /**
     * Returns the largest T coordinate of the rectangle.
     */
    public double getMaxT()
    {
        final double s = getSizeT();

        // handle this special case
        if (s == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getT() + s;
    }

    /**
     * Returns the largest C coordinate of the rectangle.
     */
    public double getMaxC()
    {
        final double s = getSizeC();

        // handle this special case
        if (s == java.lang.Double.POSITIVE_INFINITY)
            return java.lang.Double.POSITIVE_INFINITY;

        return getC() + s;
    }

    /**
     * Returns the X coordinate of the center of the rectangle.
     */
    public double getCenterX()
    {
        final double x = getX();
        final double s = getSizeX();

        // handle this special case
        if ((x == java.lang.Double.NEGATIVE_INFINITY) && (s == java.lang.Double.POSITIVE_INFINITY))
            return 0d;

        return x + s / 2d;
    }

    /**
     * Returns the Y coordinate of the center of the rectangle.
     */
    public double getCenterY()
    {
        final double y = getY();
        final double s = getSizeY();

        // handle this special case
        if ((y == java.lang.Double.NEGATIVE_INFINITY) && (s == java.lang.Double.POSITIVE_INFINITY))
            return 0d;

        return y + s / 2d;
    }

    /**
     * Returns the Z coordinate of the center of the rectangle.
     */
    public double getCenterZ()
    {
        final double z = getZ();
        final double s = getSizeZ();

        // handle this special case
        if ((z == java.lang.Double.NEGATIVE_INFINITY) && (s == java.lang.Double.POSITIVE_INFINITY))
            return 0d;

        return z + s / 2d;
    }

    /**
     * Returns the T coordinate of the center of the rectangle.
     */
    public double getCenterT()
    {
        final double t = getT();
        final double s = getSizeT();

        // handle this special case
        if ((t == java.lang.Double.NEGATIVE_INFINITY) && (s == java.lang.Double.POSITIVE_INFINITY))
            return 0d;

        return t + s / 2d;
    }

    /**
     * Returns the C coordinate of the center of the rectangle.
     */
    public double getCenterC()
    {
        final double c = getC();
        final double s = getSizeC();

        // handle this special case
        if ((c == java.lang.Double.NEGATIVE_INFINITY) && (s == java.lang.Double.POSITIVE_INFINITY))
            return 0d;

        return c + s / 2d;
    }

    /**
     * Determines whether the <code>Rectangle5D</code> is empty.
     * 
     * @return <code>true</code> if the <code>Rectangle5D</code> is empty; <code>false</code>
     *         otherwise.
     */
    public boolean isEmpty()
    {
        return (getSizeX() <= 0d) || (getSizeY() <= 0d) || (getSizeZ() <= 0d) || (getSizeT() <= 0d)
                || (getSizeC() <= 0d);
    }

    /**
     * Returns <code>true</code> if the X dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteX();

    /**
     * Returns <code>true</code> if the Y dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteY();

    /**
     * Returns <code>true</code> if the Z dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteZ();

    /**
     * Returns <code>true</code> if the T dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteT();

    /**
     * Returns <code>true</code> if the C dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteC();

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
     * Sets the C dimension to infinite.
     */
    public void setInfiniteC()
    {
        setC(java.lang.Double.NEGATIVE_INFINITY);
        setSizeC(java.lang.Double.POSITIVE_INFINITY);
    }

    /**
     * Tests if the specified coordinates are inside the boundary of the <code>Rectangle5D</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @param t
     *        the specified T coordinate to be tested
     * @param c
     *        the specified C coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside
     *         the <code>Rectangle5D</code> boundary; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double z, double t, double c)
    {
        return (x >= getMinX()) && (y >= getMaxY()) && (z >= getMinZ()) && (t >= getMinT()) && (c >= getMinC())
                && (x < getMaxX()) && (y < getMaxY()) && (z < getMaxZ()) && (t < getMaxT()) && (c < getMaxC());
    }

    /**
     * Tests if the <code>Rectangle5D</code> entirely contains the specified 5D rectangular area.<br>
     * All coordinates that lie inside the 5D rectangular area must lie within the
     * <code>Rectangle5D</code>.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param t
     *        the T coordinate of the minimum corner position of the specified rectangular area
     * @param c
     *        the C coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @param sizeT
     *        size for T dimension of the specified rectangular area
     * @param sizeC
     *        size for C dimension of the specified rectangular area
     * @return <code>true</code> if the <code>Rectangle5D</code> entirely contains the
     *         specified 5D rectangular area; <code>false</code> otherwise
     * @see #intersects
     */
    public boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
            double sizeT, double sizeC)
    {
        if (isEmpty())
            return false;

        return (x >= getMinX()) && (y >= getMaxY()) && (z >= getMinZ()) && (t >= getMinT()) && (c >= getMinC())
                && (x + sizeX <= getMaxX()) && (y + sizeY <= getMaxY()) && (z + sizeZ <= getMaxZ())
                && (t + sizeT <= getMaxT()) && (c + sizeC <= getMaxC());
    }

    /**
     * Tests if the <code>Rectangle5D</code> entirely contains the specified
     * <code>Rectangle5D</code>.
     * 
     * @see #contains(double, double, double, double, double, double, double, double, double,
     *      double)
     */
    public boolean contains(Rectangle5D rect)
    {
        return contains(rect.getX(), rect.getY(), rect.getZ(), rect.getT(), rect.getC(), rect.getSizeX(),
                rect.getSizeY(), rect.getSizeZ(), rect.getSizeT(), rect.getSizeC());
    }

    /**
     * Tests if the interior of the <code>Rectangle5D</code> intersects the interior of a specified
     * 5D rectangular area.<br>
     * The 5D rectangular area is considered to intersect the <code>Rectangle5D</code> if any point
     * is contained in both the interior of the <code>Rectangle5D</code> and the specified
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
     * @param c
     *        the C coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @param sizeT
     *        size for T dimension of the specified rectangular area
     * @param sizeC
     *        size for C dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Rectangle5D</code> and
     *         the interior of the 5D rectangular area intersect.
     */
    public boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC)
    {
        return (x + sizeX > getMinX()) && (y + sizeY > getMaxY()) && (z + sizeZ > getMinZ()) && (t + sizeT > getMinT())
                && (c + sizeC > getMinC()) && (x < getMaxX()) && (y < getMaxY()) && (z < getMaxZ()) && (t < getMaxT())
                && (c < getMaxC());
    }

    /**
     * Tests if the interior of the <code>Rectangle5D</code> intersects the interior of a specified
     * <code>Rectangle5D</code>.<br>
     * 
     * @see #intersects(double, double, double, double, double, double, double, double, double,
     *      double)
     */
    public boolean intersects(Rectangle5D rect)
    {
        return intersects(rect.getX(), rect.getY(), rect.getZ(), rect.getT(), rect.getC(), rect.getSizeX(),
                rect.getSizeY(), rect.getSizeZ(), rect.getSizeT(), rect.getSizeC());
    }

    /**
     * Adds a 5D point, specified by the double precision coordinates arguments, to this
     * <code>Rectangle5D</code>. The resulting <code>Rectangle5D</code> is the smallest
     * <code>Rectangle5D</code> that contains both the original <code>Rectangle5D</code> and the
     * specified 5D point.
     * <p>
     * After adding a 5D point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added 5D
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
     * @param newc
     *        the C coordinate of the new point
     */
    public void add(double newx, double newy, double newz, double newt, double newc)
    {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        double z1 = Math.min(getMinZ(), newz);
        double z2 = Math.max(getMaxZ(), newz);
        double t1 = Math.min(getMinT(), newt);
        double t2 = Math.max(getMaxT(), newt);
        double c1 = Math.min(getMinC(), newc);
        double c2 = Math.max(getMaxC(), newc);

        double dx;
        double dy;
        double dz;
        double dt;
        double dc;

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
        // special infinite case
        if (c2 == java.lang.Double.POSITIVE_INFINITY)
            dc = java.lang.Double.POSITIVE_INFINITY;
        else
            dc = c2 - c1;

        setRect(x1, y1, z1, t1, c1, dx, dy, dz, dt, dc);
    }

    /**
     * Adds the <code>Point5D</code> object <code>pt</code> to this <code>Rectangle5D</code>.
     * The resulting <code>Rectangle5D</code> is the smallest <code>Rectangle5D</code> that contains
     * both the original <code>Rectangle5D</code> and the specified <code>Point5D</code>.
     * <p>
     * After adding a point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added
     * point falls on edge of the enlarged rectangle, <code>contains</code> returns
     * <code>false</code> for that point.
     * 
     * @param pt
     *        the new <code>Point5D</code> to add to this <code>Rectangle5D</code>.
     */
    public void add(Point5D pt)
    {
        add(pt.getX(), pt.getY(), pt.getZ(), pt.getT(), pt.getC());
    }

    /**
     * Adds a <code>Rectangle5D</code> object to this <code>Rectangle5D</code>. The resulting
     * <code>Rectangle5D</code> is the union of the two <code>Rectangle5D</code> objects.
     * 
     * @param r
     *        the <code>Rectangle5D</code> to add to this <code>Rectangle5D</code>.
     */
    public void add(Rectangle5D r)
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

    /**
     * Convert to 4D rectangle
     */
    public abstract Rectangle4D toRectangle4D();

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Rectangle5D)
        {
            final Rectangle5D rect = (Rectangle5D) obj;
            return (getX() == rect.getX()) && (getY() == rect.getY()) && (getC() == rect.getC())
                    && (getZ() == rect.getZ()) && (getT() == rect.getT()) && (getSizeX() == rect.getSizeX())
                    && (getSizeY() == rect.getSizeY()) && (getSizeC() == rect.getSizeC())
                    && (getSizeZ() == rect.getSizeZ()) && (getSizeT() == rect.getSizeT());
        }

        return super.equals(obj);
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
        return getClass().getName() + "[" + getX() + "," + getY() + "," + getZ() + "," + getT() + "," + getC() + " - "
                + getSizeX() + "," + getSizeY() + "," + getSizeZ() + "," + getSizeT() + "," + getSizeC() + "]";
    }

    public static class Double extends Rectangle5D
    {
        public double x;
        public double y;
        public double z;
        public double t;
        public double c;

        public double sizeX;
        public double sizeY;
        public double sizeZ;
        public double sizeT;
        public double sizeC;

        public Double(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
                double sizeT, double sizeC)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Double(Rectangle5D r)
        {
            this(r.getX(), r.getY(), r.getZ(), r.getT(), r.getC(), r.getSizeX(), r.getSizeY(), r.getSizeZ(), r
                    .getSizeT(), r.getSizeC());
        }

        public Double()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
                double sizeT, double sizeC)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
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
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double value)
        {
            c = value;
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
        public double getSizeC()
        {
            return sizeC;
        }

        @Override
        public void setSizeC(double value)
        {
            sizeC = value;
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getX() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeX() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getY() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeY() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteZ()
        {
            return (getZ() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeZ() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getT() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeT() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteC()
        {
            return (getC() == java.lang.Double.NEGATIVE_INFINITY) && (getSizeC() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public Rectangle5D createIntersection(Rectangle5D r)
        {
            final Rectangle5D.Double result = new Rectangle5D.Double();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle5D createUnion(Rectangle5D r)
        {
            final Rectangle5D.Double result = new Rectangle5D.Double();

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

        @Override
        public Rectangle4D toRectangle4D()
        {
            return new Rectangle4D.Double(x, y, z, t, sizeX, sizeY, sizeZ, sizeT);
        }
    }

    public static class Float extends Rectangle5D
    {
        public float x;
        public float y;
        public float z;
        public float t;
        public float c;

        public float sizeX;
        public float sizeY;
        public float sizeZ;
        public float sizeT;
        public float sizeC;

        public Float(float x, float y, float z, float t, float c, float sizeX, float sizeY, float sizeZ, float sizeT,
                float sizeC)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Float(Rectangle5D r)
        {
            this((float) r.getX(), (float) r.getY(), (float) r.getZ(), (float) r.getT(), (float) r.getC(), (float) r
                    .getSizeX(), (float) r.getSizeY(), (float) r.getSizeZ(), (float) r.getSizeT(), (float) r.getSizeC());
        }

        public Float()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        @Override
        public void setRect(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
                double sizeT, double sizeC)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.t = (float) t;
            this.c = (float) c;
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
            this.sizeT = (float) sizeT;
            this.sizeC = (float) sizeC;
        }

        @Override
        public double getX()
        {
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
            return t;
        }

        @Override
        public void setT(double value)
        {
            t = (float) value;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double value)
        {
            c = (float) value;
        }

        @Override
        public double getSizeX()
        {
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
            return sizeT;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (float) value;
        }

        @Override
        public double getSizeC()
        {
            return sizeC;
        }

        @Override
        public void setSizeC(double value)
        {
            sizeC = (float) value;
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getX() == java.lang.Float.NEGATIVE_INFINITY) && (getSizeX() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getY() == java.lang.Float.NEGATIVE_INFINITY) && (getSizeY() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteZ()
        {
            return (getZ() == java.lang.Float.NEGATIVE_INFINITY) && (getSizeZ() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getT() == java.lang.Float.NEGATIVE_INFINITY) && (getSizeT() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteC()
        {
            return (getC() == java.lang.Float.NEGATIVE_INFINITY) && (getSizeC() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public Rectangle5D createIntersection(Rectangle5D r)
        {
            final Rectangle5D.Float result = new Rectangle5D.Float();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle5D createUnion(Rectangle5D r)
        {
            final Rectangle5D.Float result = new Rectangle5D.Float();

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

        @Override
        public Rectangle4D toRectangle4D()
        {
            return new Rectangle4D.Float(x, y, z, t, sizeX, sizeY, sizeZ, sizeT);
        }
    }

    public static class Integer extends Rectangle5D
    {
        public int x;
        public int y;
        public int z;
        public int t;
        public int c;
        public int sizeX;
        public int sizeY;
        public int sizeZ;
        public int sizeT;
        public int sizeC;

        public Integer(int x, int y, int z, int t, int c, int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Integer(Rectangle5D.Integer r)
        {
            this(r.x, r.y, r.z, r.t, r.c, r.sizeX, r.sizeY, r.sizeZ, r.sizeT, r.sizeC);
        }

        public Integer(Rectangle5D r)
        {
            this(r.toInteger());
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        /**
         * Sets the bounds of this {@code Rectangle5D} to the integer bounds
         * which encompass the specified double bounds.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param t
         *        the T coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param c
         *        the C coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle5D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle5D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle5D</code>
         * @param sizeT
         *        size for T dimension of this <code>Rectangle5D</code>
         * @param sizeC
         *        size for C dimension of this <code>Rectangle5D</code>
         */
        @Override
        public void setRect(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
                double sizeT, double sizeC)
        {
            final Rectangle5D.Integer r = new Rectangle5D.Double(x, y, z, t, c, sizeX, sizeY, sizeZ, sizeT, sizeC)
                    .toInteger();
            setRect(r.x, r.y, r.z, r.t, r.c, r.sizeX, r.sizeY, r.sizeZ, r.sizeT, r.sizeC);
        }

        /**
         * Sets the position and size of this <code>Rectangle5D</code> to the specified
         * <code>integer</code> values.
         * 
         * @param x
         *        the X coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param y
         *        the Y coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param z
         *        the Z coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param t
         *        the T coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param c
         *        the C coordinate of the minimum corner position of this <code>Rectangle5D</code>
         * @param sizeX
         *        size for X dimension of this <code>Rectangle5D</code>
         * @param sizeY
         *        size for Y dimension of this <code>Rectangle5D</code>
         * @param sizeZ
         *        size for Z dimension of this <code>Rectangle5D</code>
         * @param sizeT
         *        size for T dimension of this <code>Rectangle5D</code>
         * @param sizeC
         *        size for C dimension of this <code>Rectangle5D</code>
         */
        public void setRect(int x, int y, int z, int t, int c, int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        @Override
        public double getX()
        {
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
            return t;
        }

        @Override
        public void setT(double value)
        {
            t = (int) value;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double value)
        {
            c = (int) value;
        }

        @Override
        public double getSizeX()
        {
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
            return sizeT;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (int) value;
        }

        @Override
        public double getSizeC()
        {
            return sizeC;
        }

        @Override
        public void setSizeC(double value)
        {
            sizeC = (int) value;
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getX() == java.lang.Integer.MIN_VALUE) && (getSizeX() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getY() == java.lang.Integer.MIN_VALUE) && (getSizeY() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteZ()
        {
            return (getZ() == java.lang.Integer.MIN_VALUE) && (getSizeZ() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getT() == java.lang.Integer.MIN_VALUE) && (getSizeT() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteC()
        {
            return (getC() == java.lang.Integer.MIN_VALUE) && (getSizeC() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public Rectangle5D.Integer toInteger()
        {
            return (Integer) clone();
        }

        @Override
        public Rectangle5D createIntersection(Rectangle5D r)
        {
            final Rectangle5D.Integer result = new Rectangle5D.Integer();

            intersect(this, r, result);

            return result;
        }

        @Override
        public Rectangle5D createUnion(Rectangle5D r)
        {
            final Rectangle5D.Integer result = new Rectangle5D.Integer();

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

        @Override
        public Rectangle4D toRectangle4D()
        {
            return new Rectangle4D.Integer(x, y, z, t, sizeX, sizeY, sizeZ, sizeT);
        }
    }
}
