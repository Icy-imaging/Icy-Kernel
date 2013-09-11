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

import icy.type.point.Point3D;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Rectangle3D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle3D
{
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

        result.setRect(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);

        return result;
    }

    /**
     * Returns a new <code>Rectangle3D</code> object representing the intersection of this
     * <code>Rectangle3D</code> with the specified <code>Rectangle3D</code>.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to be intersected with this <code>Rectangle3D</code>
     * @return the largest <code>Rectangle3D</code> contained in both the specified
     *         <code>Rectangle3D</code> and in this <code>Rectangle3D</code>.
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

        result.setRect(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);

        return result;
    }

    /**
     * Returns a new <code>Rectangle3D</code> object representing the union of this
     * <code>Rectangle3D</code> with the specified <code>Rectangle3D</code>.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to be combined with this <code>Rectangle3D</code>
     * @return the smallest <code>Rectangle3D</code> containing both the specified
     *         <code>Rectangle3D</code> and this <code>Rectangle3D</code>.
     */
    public abstract Rectangle3D createUnion(Rectangle3D r);

    /**
     * Sets the position and size of this <code>Rectangle3D</code> to the specified
     * <code>double</code> values.
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
     * Returns the point coordinate.
     */
    public Point3D getPosition()
    {
        return new Point3D.Double(getX(), getY(), getZ());
    }

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
    public Rectangle3D.Integer getBoundsInt()
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
        return getX() + getSizeX();
    }

    /**
     * Returns the largest Y coordinate of the rectangle.
     */
    public double getMaxY()
    {
        return getY() + getSizeY();
    }

    /**
     * Returns the largest Z coordinate of the rectangle.
     */
    public double getMaxZ()
    {
        return getZ() + getSizeZ();
    }

    /**
     * Returns the X coordinate of the center of the rectangle.
     */
    public double getCenterX()
    {
        return getX() + getSizeX() / 2d;
    }

    /**
     * Returns the Y coordinate of the center of the rectangle.
     */
    public double getCenterY()
    {
        return getY() + getSizeY() / 2d;
    }

    /**
     * Returns the Z coordinate of the center of the rectangle.
     */
    public double getCenterZ()
    {
        return getY() + getSizeY() / 2d;
    }

    /**
     * Determines whether the <code>Rectangle3D</code> is empty.
     * 
     * @return <code>true</code> if the <code>Rectangle3D</code> is empty; <code>false</code>
     *         otherwise.
     */
    public boolean isEmpty()
    {
        return (getSizeX() <= 0d) || (getSizeY() <= 0d) || (getSizeZ() <= 0d);
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
    public boolean contains(double x, double y, double z)
    {
        double x0 = getX();
        double y0 = getY();
        double z0 = getY();
        return (x >= x0) && (y >= y0) && (z >= z0) && (x < x0 + getSizeX()) && (y < y0 + getSizeY())
                && (z < z0 + getSizeZ());
    }

    /**
     * Tests if the <code>Rectangle3D</code> entirely contains the specified 3D rectangular area.
     * All coordinates that lie inside the 3D rectangular area must lie within the
     * <code>Rectangle3D</code>.
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
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        if (isEmpty())
            return false;

        double x0 = getX();
        double y0 = getY();
        double z0 = getZ();
        return (x >= x0) && (y >= y0) && (z >= z0) && (x + sizeX <= x0 + getSizeX()) && (y + sizeY <= y0 + getSizeY())
                && (z + sizeZ <= z0 + getSizeZ());
    }

    /**
     * Tests if the <code>Rectangle3D</code> entirely contains the specified
     * <code>Rectangle3D</code>.
     * 
     * @see #contains(double, double, double, double, double, double)
     */
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
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        double x0 = getX();
        double y0 = getY();
        double z0 = getZ();
        return (x + sizeX > x0) && (y + sizeY > y0) && (z + sizeZ > z0) && (x < x0 + getSizeX())
                && (y < y0 + getSizeY()) && (z < z0 + getSizeZ());
    }

    /**
     * Tests if the interior of the <code>Rectangle3D</code> intersects the interior of a specified
     * <code>Rectangle3D</code>.<br>
     * 
     * @see #intersects(double, double, double, double, double, double)
     */
    public boolean intersects(Rectangle3D rect)
    {
        return intersects(rect.getX(), rect.getY(), rect.getZ(), rect.getSizeX(), rect.getSizeY(), rect.getSizeZ());
    }

    /**
     * Adds a 3D point, specified by the double precision coordinates arguments, to this
     * <code>Rectangle3D</code>. The resulting <code>Rectangle3D</code> is the smallest
     * <code>Rectangle3D</code> that contains both the original <code>Rectangle3D</code> and the
     * specified 3D point.
     * <p>
     * After adding a 3D point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added 3D
     * point falls on edge of the enlarged rectangle, <code>contains</code> returns
     * <code>false</code> for that point.
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
        setRect(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);
    }

    /**
     * Adds the <code>Point3D</code> object <code>pt</code> to this <code>Rectangle3D</code>.
     * The resulting <code>Rectangle3D</code> is the smallest <code>Rectangle3D</code> that contains
     * both the original <code>Rectangle3D</code> and the specified <code>Point3D</code>.
     * <p>
     * After adding a point, a call to <code>contains</code> with the added point as an argument
     * does not necessarily return <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the edges of a rectangle. Therefore, if the added
     * point falls on edge of the enlarged rectangle, <code>contains</code> returns
     * <code>false</code> for that point.
     * 
     * @param pt
     *        the new <code>Point3D</code> to add to this <code>Rectangle3D</code>.
     */
    public void add(Point3D pt)
    {
        add(pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Adds a <code>Rectangle3D</code> object to this <code>Rectangle3D</code>. The resulting
     * <code>Rectangle3D</code> is the union of the two <code>Rectangle3D</code> objects.
     * 
     * @param r
     *        the <code>Rectangle3D</code> to add to this <code>Rectangle3D</code>.
     */
    public void add(Rectangle3D r)
    {
        double x1 = Math.min(getMinX(), r.getMinX());
        double x2 = Math.max(getMaxX(), r.getMaxX());
        double y1 = Math.min(getMinY(), r.getMinY());
        double y2 = Math.max(getMaxY(), r.getMaxY());
        double z1 = Math.min(getMinZ(), r.getMinZ());
        double z2 = Math.max(getMaxZ(), r.getMaxZ());
        setRect(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);
    }

    /**
     * Convert to 2D rectangle
     */
    public abstract Rectangle2D toRectangle2D();

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Rectangle3D)
        {
            final Rectangle3D rect = (Rectangle3D) obj;
            return (getX() == rect.getX()) && (getY() == rect.getY()) && (getZ() == rect.getZ())
                    && (getSizeX() == rect.getSizeX()) && (getSizeY() == rect.getSizeY())
                    && (getSizeZ() == rect.getSizeZ());
        }

        return super.equals(obj);
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
            this(r.getBoundsInt());
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
            final Rectangle3D.Integer r = new Rectangle3D.Double(x, y, z, sizeX, sizeY, sizeZ).getBoundsInt();
            setRect(r.x, r.y, r.z, r.sizeX, r.sizeY, r.sizeZ);
        }

        /**
         * Sets the position and size of this <code>Rectangle3D</code> to the specified
         * <code>integer</code> values.
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
        public Rectangle3D.Integer getBoundsInt()
        {
            return new Rectangle3D.Integer(x, y, z, sizeX, sizeY, sizeZ);
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
