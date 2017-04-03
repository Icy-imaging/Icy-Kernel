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
package icy.type.point;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Point3D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point3D implements Cloneable
{
    /**
     * Returns distance between 2 Point2D using specified scale factor for x/y/z dimension.
     */
    public static double getDistance(Point3D pt1, Point3D pt2, double factorX, double factorY, double factorZ)
    {
        double px = (pt2.getX() - pt1.getX()) * factorX;
        double py = (pt2.getY() - pt1.getY()) * factorY;
        double pz = (pt2.getZ() - pt1.getZ()) * factorZ;
        return Math.sqrt(px * px + py * py + pz * pz);
    }

    /**
     * Returns total distance of the specified list of points.
     */
    public static double getTotalDistance(List<Point3D> points, double factorX, double factorY, double factorZ,
            boolean connectLastPoint)
    {
        final int size = points.size();
        double result = 0d;

        if (size > 1)
        {
            for (int i = 0; i < size - 1; i++)
                result += getDistance(points.get(i), points.get(i + 1), factorX, factorY, factorZ);

            // add last to first point distance
            if (connectLastPoint)
                result += getDistance(points.get(size - 1), points.get(0), factorX, factorY, factorZ);
        }

        return result;
    }

    /**
     * Cache the hash code to make computing hashes faster.
     */
    int hash = 0;

    /**
     * Returns the X coordinate of this <code>Point3D</code> in <code>double</code> precision.
     * 
     * @return the X coordinate of this <code>Point3D</code>.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of this <code>Point3D</code> in <code>double</code> precision.
     * 
     * @return the Y coordinate of this <code>Point3D</code>.
     */
    public abstract double getY();

    /**
     * Returns the Z coordinate of this <code>Point3D</code> in <code>double</code> precision.
     * 
     * @return the Z coordinate of this <code>Point3D</code>.
     */
    public abstract double getZ();

    /**
     * Sets the X coordinate of this <code>Point3D</code> in <code>double</code> precision.
     */
    public abstract void setX(double x);

    /**
     * Sets the Y coordinate of this <code>Point3D</code> in <code>double</code> precision.
     */
    public abstract void setY(double y);

    /**
     * Sets the Z coordinate of this <code>Point3D</code> in <code>double</code> precision.
     */
    public abstract void setZ(double z);

    /**
     * Sets the location of this <code>Point3D</code> to the
     * specified <code>double</code> coordinates.
     * 
     * @param x
     *        the new X coordinate of this {@code Point3D}
     * @param y
     *        the new Y coordinate of this {@code Point3D}
     * @param z
     *        the new Z coordinate of this {@code Point3D}
     */
    public void setLocation(double x, double y, double z)
    {
        setX(x);
        setY(y);
        setZ(z);
    }

    /**
     * Sets the location of this <code>Point3D</code> to the same
     * coordinates as the specified <code>Point3D</code> object.
     * 
     * @param p
     *        the specified <code>Point3D</code> to which to set
     *        this <code>Point3D</code>
     */
    public void setLocation(Point3D p)
    {
        setLocation(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Translate this <code>Point3D</code> by the specified <code>double</code> coordinates.
     * 
     * @param x
     *        the X translation value
     * @param y
     *        the Y translation value
     * @param z
     *        the Z translation value
     */
    public void translate(double x, double y, double z)
    {
        setX(getX() + x);
        setY(getY() + y);
        setZ(getZ() + z);
    }

    /**
     * Translate this <code>Point3D</code> by the specified <code>Point3D</code> coordinates.
     * 
     * @param p
     *        the specified <code>Point3D</code> used to translate this <code>Point3D</code>
     */
    public void translate(Point3D p)
    {
        translate(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Convert to 2D point
     */
    public abstract Point2D toPoint2D();

    /**
     * Computes the distance between this point and point {@code (x1, y1, z1)}.
     *
     * @param x1
     *        the x coordinate of other point
     * @param y1
     *        the y coordinate of other point
     * @param z1
     *        the z coordinate of other point
     * @return the distance between this point and point {@code (x1, y1, z1)}.
     */
    public double distance(double x1, double y1, double z1)
    {
        double a = getX() - x1;
        double b = getY() - y1;
        double c = getZ() - z1;
        return Math.sqrt(a * a + b * b + c * c);
    }

    /**
     * Computes the distance between this point and the specified {@code point}.
     *
     * @param point
     *        the other point
     * @return the distance between this point and the specified {@code point}.
     * @throws NullPointerException
     *         if the specified {@code point} is null
     */
    public double distance(Point3D point)
    {
        return distance(point.getX(), point.getY(), point.getZ());
    }

    /**
     * Normalizes the relative magnitude vector represented by this instance.
     * Returns a vector with the same direction and magnitude equal to 1.
     * If this is a zero vector, a zero vector is returned.
     * 
     * @return the normalized vector represented by a {@code Point3D} instance
     */
    public Point3D normalize()
    {
        final double mag = magnitude();

        if (mag == 0d)
            return new Point3D.Double(0d, 0d, 0d);

        return new Point3D.Double(getX() / mag, getY() / mag, getZ() / mag);
    }

    /**
     * Returns a point which lies in the middle between this point and the
     * specified coordinates.
     * 
     * @param x
     *        the X coordinate of the second end point
     * @param y
     *        the Y coordinate of the second end point
     * @param z
     *        the Z coordinate of the second end point
     * @return the point in the middle
     */
    public Point3D midpoint(double x, double y, double z)
    {
        return new Point3D.Double(x + (getX() - x) / 2d, y + (getY() - y) / 2d, z + (getZ() - z) / 2d);
    }

    /**
     * Returns a point which lies in the middle between this point and the
     * specified point.
     * 
     * @param point
     *        the other end point
     * @return the point in the middle
     * @throws NullPointerException
     *         if the specified {@code point} is null
     */
    public Point3D midpoint(Point3D point)
    {
        return midpoint(point.getX(), point.getY(), point.getZ());
    }

    /**
     * Computes the angle (in degrees) between the vector represented
     * by this point and the specified vector.
     * 
     * @param x
     *        the X magnitude of the other vector
     * @param y
     *        the Y magnitude of the other vector
     * @param z
     *        the Z magnitude of the other vector
     * @return the angle between the two vectors measured in degrees
     */
    public double angle(double x, double y, double z)
    {
        final double ax = getX();
        final double ay = getY();
        final double az = getZ();

        final double delta = (ax * x + ay * y + az * z)
                / Math.sqrt((ax * ax + ay * ay + az * az) * (x * x + y * y + z * z));

        if (delta > 1d)
            return 0d;
        if (delta < -1d)
            return 180d;

        return Math.toDegrees(Math.acos(delta));
    }

    /**
     * Computes the angle (in degrees) between the vector represented
     * by this point and the vector represented by the specified point.
     * 
     * @param vector
     *        the other vector
     * @return the angle between the two vectors measured in degrees, {@code NaN} if any of the two vectors is a zero
     *         vector
     * @throws NullPointerException
     *         if the specified {@code vector} is null
     */
    public double angle(Point3D vector)
    {
        return angle(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Computes the angle (in degrees) between the three points with this point
     * as a vertex.
     * 
     * @param p1
     *        one point
     * @param p2
     *        other point
     * @return angle between the vectors (this, p1) and (this, p2) measured
     *         in degrees, {@code NaN} if the three points are not different
     *         from one another
     * @throws NullPointerException
     *         if the {@code p1} or {@code p2} is null
     */
    public double angle(Point3D p1, Point3D p2)
    {
        final double x = getX();
        final double y = getY();
        final double z = getZ();

        final double ax = p1.getX() - x;
        final double ay = p1.getY() - y;
        final double az = p1.getZ() - z;
        final double bx = p2.getX() - x;
        final double by = p2.getY() - y;
        final double bz = p2.getZ() - z;

        final double delta = (ax * bx + ay * by + az * bz)
                / Math.sqrt((ax * ax + ay * ay + az * az) * (bx * bx + by * by + bz * bz));

        if (delta > 1.0)
            return 0.0;

        if (delta < -1.0)
            return 180.0;

        return Math.toDegrees(Math.acos(delta));
    }

    /**
     * Computes norm2 (square length) of the vector represented by this Point3D.
     * 
     * @return norm2 length of the vector
     */
    public double norm2()
    {
        final double x = getX();
        final double y = getY();
        final double z = getZ();

        return x * x + y * y + z * z;
    }

    /**
     * Computes length of the vector represented by this Point3D.
     * 
     * @return length of the vector
     */
    public double length()
    {
        return Math.sqrt(norm2());
    }

    /**
     * Same as {@link #length()}
     */
    public double magnitude()
    {
        return length();
    }

    /**
     * Computes dot (scalar) product of the vector represented by this instance
     * and the specified vector.
     * 
     * @param x
     *        the X magnitude of the other vector
     * @param y
     *        the Y magnitude of the other vector
     * @param z
     *        the Z magnitude of the other vector
     * @return the dot product of the two vectors
     */
    public double dotProduct(double x, double y, double z)
    {
        return getX() * x + getY() * y + getZ() * z;
    }

    /**
     * Computes dot (scalar) product of the vector represented by this instance
     * and the specified vector.
     * 
     * @param vector
     *        the other vector
     * @return the dot product of the two vectors
     * @throws NullPointerException
     *         if the specified {@code vector} is null
     */
    public double dotProduct(Point3D vector)
    {
        return dotProduct(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Computes cross product of the vector represented by this instance
     * and the specified vector.
     * 
     * @param x
     *        the X magnitude of the other vector
     * @param y
     *        the Y magnitude of the other vector
     * @param z
     *        the Z magnitude of the other vector
     * @return the cross product of the two vectors
     */
    public Point3D crossProduct(double x, double y, double z)
    {
        final double ax = getX();
        final double ay = getY();
        final double az = getZ();

        return new Point3D.Double(ay * z - az * y, az * x - ax * z, ax * y - ay * x);
    }

    /**
     * Computes cross product of the vector represented by this instance
     * and the specified vector.
     * 
     * @param vector
     *        the other vector
     * @return the cross product of the two vectors
     * @throws NullPointerException
     *         if the specified {@code vector} is null
     */
    public Point3D crossProduct(Point3D vector)
    {
        return crossProduct(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof Point3D)
        {
            final Point3D pt = (Point3D) obj;
            return (getX() == pt.getX()) && (getY() == pt.getY()) && (getZ() == pt.getZ());
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

    /**
     * @return a hash code for this {@code Point3D} object.
     */
    @Override
    public int hashCode()
    {
        if (hash == 0)
        {
            long bits = 7L;
            bits = 31L * bits + java.lang.Double.doubleToLongBits(getX());
            bits = 31L * bits + java.lang.Double.doubleToLongBits(getY());
            bits = 31L * bits + java.lang.Double.doubleToLongBits(getZ());
            hash = (int) (bits ^ (bits >> 32));
            // so hash could never be 0 when computed
            hash |= 1;
        }

        return hash;
    }

    /**
     * Returns a string representation of this {@code Point3D}.
     * This method is intended to be used only for informational purposes.
     */
    @Override
    public String toString()
    {
        return getClass().getName() + "[" + getX() + "," + getY() + "," + getZ() + "]";
    }

    public static class Double extends Point3D
    {
        /**
         * Create an array of Point3D.Double from the input double array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 3.<br>
         * <code>input[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static Point3D.Double[] toPoint3D(double[] input)
        {
            final Point3D.Double[] result = new Point3D.Double[input.length / 3];

            int pt = 0;
            for (int i = 0; i < input.length; i += 3)
                result[pt++] = new Point3D.Double(input[i + 0], input[i + 1], input[i + 2]);

            return result;
        }

        /**
         * Create an array of double from the input Point3D.Double array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 3.<br>
         * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static double[] toDoubleArray(Point3D.Double[] input)
        {
            final double[] result = new double[input.length * 3];

            int off = 0;
            for (Point3D.Double pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
            }

            return result;
        }

        public double x;
        public double y;
        public double z;

        public Double(double x, double y, double z)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Double(double[] xyz)
        {
            final int len = xyz.length;

            if (len > 0)
                this.x = xyz[0];
            if (len > 1)
                this.y = xyz[1];
            if (len > 2)
                this.z = xyz[2];
        }

        public Double()
        {
            this(0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = x;
            hash = 0;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = y;
            hash = 0;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = z;
            hash = 0;
        }

        @Override
        public String toString()
        {
            return "Point3D.Double[" + x + "," + y + "," + z + "]";
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point2D.Double(x, y);
        }
    }

    public static class Float extends Point3D
    {
        /**
         * Create an array of Point3D.Float from the input float array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 3.<br>
         * <code>input[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static Point3D.Float[] toPoint3D(float[] input)
        {
            final Point3D.Float[] result = new Point3D.Float[input.length / 3];

            int pt = 0;
            for (int i = 0; i < input.length; i += 3)
                result[pt++] = new Point3D.Float(input[i + 0], input[i + 1], input[i + 2]);

            return result;
        }

        /**
         * Create an array of float from the input Point3D.Float array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 3.<br>
         * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static float[] toFloatArray(Point3D.Float[] input)
        {
            final float[] result = new float[input.length * 3];

            int off = 0;
            for (Point3D.Float pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
            }

            return result;
        }

        public float x;
        public float y;
        public float z;

        public Float(float x, float y, float z)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Float(float[] xyz)
        {
            final int len = xyz.length;

            if (len > 0)
                this.x = xyz[0];
            if (len > 1)
                this.y = xyz[1];
            if (len > 2)
                this.z = xyz[2];
        }

        public Float()
        {
            this(0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = (float) x;
            hash = 0;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = (float) y;
            hash = 0;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = (float) z;
            hash = 0;
        }

        @Override
        public String toString()
        {
            return "Point3D.Float[" + x + "," + y + "," + z + "]";
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point2D.Float(x, y);
        }
    }

    public static class Integer extends Point3D
    {
        /**
         * Create an array of Point3D.Integer from the input integer array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 3.<br>
         * <code>input[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static Point3D.Integer[] toPoint3D(int[] input)
        {
            final Point3D.Integer[] result = new Point3D.Integer[input.length / 3];

            int pt = 0;
            for (int i = 0; i < input.length; i += 3)
                result[pt++] = new Point3D.Integer(input[i + 0], input[i + 1], input[i + 2]);

            return result;
        }

        /**
         * Create an array of integer from the input Point3D.Integer array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 3.<br>
         * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i><br>
         */
        public static int[] toIntegerArray(Point3D.Integer[] input)
        {
            final int[] result = new int[input.length * 3];

            int off = 0;
            for (Point3D.Integer pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
            }

            return result;
        }

        public int x;
        public int y;
        public int z;

        public Integer(int x, int y, int z)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Integer(int[] xyz)
        {
            final int len = xyz.length;

            if (len > 0)
                this.x = xyz[0];
            if (len > 1)
                this.y = xyz[1];
            if (len > 2)
                this.z = xyz[2];
        }

        public Integer()
        {
            this(0, 0, 0);
        }

        @Override
        public double getX()
        {
            return x;
        }

        @Override
        public void setX(double x)
        {
            this.x = (int) x;
            hash = 0;
        }

        @Override
        public double getY()
        {
            return y;
        }

        @Override
        public void setY(double y)
        {
            this.y = (int) y;
            hash = 0;
        }

        @Override
        public double getZ()
        {
            return z;
        }

        @Override
        public void setZ(double z)
        {
            this.z = (int) z;
            hash = 0;
        }

        @Override
        public String toString()
        {
            return "Point3D.Integer[" + x + "," + y + "," + z + "]";
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point(x, y);
        }
    }
}
