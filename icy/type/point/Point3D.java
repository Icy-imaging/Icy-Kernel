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

/**
 * Point3D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point3D implements Cloneable
{
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
     * Convert to 2D point
     */
    public abstract Point2D toPoint2D();

    @Override
    public boolean equals(Object obj)
    {
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
        }

        @Override
        public void setLocation(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
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
        }

        @Override
        public void setLocation(double x, double y, double z)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
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
        }

        @Override
        public void setLocation(double x, double y, double z)
        {
            this.x = (int) x;
            this.y = (int) y;
            this.z = (int) z;
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
