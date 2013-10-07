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
package icy.type.point;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Point5D interface.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point5D implements Cloneable
{
    /**
     * Returns the X coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the X coordinate of this <code>Point5D</code>.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the Y coordinate of this <code>Point5D</code>.
     */
    public abstract double getY();

    /**
     * Returns the Z coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the Z coordinate of this <code>Point5D</code>.
     */
    public abstract double getZ();

    /**
     * Returns the T coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the T coordinate of this <code>Point5D</code>.
     */
    public abstract double getT();

    /**
     * Returns the C coordinate of this <code>Point5D</code> in <code>double</code> precision.
     * 
     * @return the C coordinate of this <code>Point5D</code>.
     */
    public abstract double getC();

    /**
     * Sets the X coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setX(double x);

    /**
     * Sets the Y coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setY(double y);

    /**
     * Sets the Z coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setZ(double z);

    /**
     * Sets the T coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setT(double t);

    /**
     * Sets the C coordinate of this <code>Point5D</code> in <code>double</code> precision.
     */
    public abstract void setC(double c);

    /**
     * Sets the location of this <code>Point5D</code> to the
     * specified <code>double</code> coordinates.
     * 
     * @param x
     *        the new X coordinate of this {@code Point5D}
     * @param y
     *        the new Y coordinate of this {@code Point5D}
     * @param z
     *        the new Z coordinate of this {@code Point5D}
     * @param t
     *        the new T coordinate of this {@code Point5D}
     * @param c
     *        the new C coordinate of this {@code Point5D}
     */
    public void setLocation(double x, double y, double z, double t, double c)
    {
        setX(x);
        setY(y);
        setZ(z);
        setT(t);
        setC(c);
    }

    /**
     * Sets the location of this <code>Point5D</code> to the same
     * coordinates as the specified <code>Point5D</code> object.
     * 
     * @param p
     *        the specified <code>Point5D</code> to which to set
     *        this <code>Point5D</code>
     */
    public void setLocation(Point5D p)
    {
        setLocation(p.getX(), p.getY(), p.getZ(), p.getT(), p.getC());
    }

    /**
     * Convert to 2D point
     */
    public abstract Point2D toPoint2D();

    /**
     * Convert to 3D point
     */
    public abstract Point3D toPoint3D();

    /**
     * Convert to 4D point
     */
    public abstract Point4D toPoint4D();

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Point5D)
        {
            final Point5D pt = (Point5D) obj;
            return (getX() == pt.getX()) && (getY() == pt.getY()) && (getZ() == pt.getZ()) && (getT() == pt.getT())
                    && (getC() == pt.getC());
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
        return getClass().getName() + "[" + getX() + "," + getY() + "," + getZ() + ", " + getT() + "," + getC() + "]";
    }

    public static class Double extends Point5D
    {
        /**
         * Create an array of Point5D.Double from the input double array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 5.<br>
         * <code>input[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static Point5D.Double[] toPoint5D(double[] input)
        {
            final Point5D.Double[] result = new Point5D.Double[input.length / 5];

            int pt = 0;
            for (int i = 0; i < input.length; i += 5)
                result[pt++] = new Point5D.Double(input[i + 0], input[i + 1], input[i + 2], input[i + 3], input[i + 4]);

            return result;
        }

        /**
         * Create an array of double from the input Point5D.Double array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 5.<br>
         * <code>result[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static double[] toDoubleArray(Point5D.Double[] input)
        {
            final double[] result = new double[input.length * 5];

            int off = 0;
            for (Point5D.Double pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
                result[off++] = pt.t;
                result[off++] = pt.c;
            }

            return result;
        }

        public double x;
        public double y;
        public double z;
        public double t;
        public double c;

        public Double(double x, double y, double z, double t, double c)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        public Double(double[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Double()
        {
            this(0d, 0d, 0d, 0d, 0d);
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
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point2D.Double(x, y);
        }

        @Override
        public Point3D toPoint3D()
        {
            return new Point3D.Double(x, y, z);
        }

        @Override
        public Point4D toPoint4D()
        {
            return new Point4D.Double(x, y, z, t);
        }
    }

    public static class Float extends Point5D
    {
        /**
         * Create an array of Point5D.Float from the input float array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 5.<br>
         * <code>input[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static Point5D.Float[] toPoint5D(float[] input)
        {
            final Point5D.Float[] result = new Point5D.Float[input.length / 5];

            int pt = 0;
            for (int i = 0; i < input.length; i += 5)
                result[pt++] = new Point5D.Float(input[i + 0], input[i + 1], input[i + 2], input[i + 3], input[i + 4]);

            return result;
        }

        /**
         * Create an array of float from the input Point5D.Float array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 5.<br>
         * <code>result[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static float[] toFloatArray(Point5D.Float[] input)
        {
            final float[] result = new float[input.length * 5];

            int off = 0;
            for (Point5D.Float pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
                result[off++] = pt.t;
                result[off++] = pt.c;
            }

            return result;
        }

        public float x;
        public float y;
        public float z;
        public float t;
        public float c;

        public Float(float x, float y, float z, float t, float c)
        {
            super();

            this.x = x;
            this.y = y;
            this.c = c;
            this.z = z;
            this.t = t;
        }

        public Float(float[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Float()
        {
            this(0f, 0f, 0f, 0f, 0f);
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
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = (float) t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = (float) c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.t = (float) t;
            this.c = (float) c;
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point2D.Float(x, y);
        }

        @Override
        public Point3D toPoint3D()
        {
            return new Point3D.Float(x, y, z);
        }

        @Override
        public Point4D toPoint4D()
        {
            return new Point4D.Float(x, y, z, t);
        }
    }

    public static class Integer extends Point5D
    {
        /**
         * Create an array of Point5D.Integer from the input integer array.<br>
         * <br>
         * The format of the input array should be as follow:<br>
         * <code>input.lenght</code> = number of point * 5.<br>
         * <code>input[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>input[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static Point5D.Integer[] toPoint5D(int[] input)
        {
            final Point5D.Integer[] result = new Point5D.Integer[input.length / 5];

            int pt = 0;
            for (int i = 0; i < input.length; i += 5)
                result[pt++] = new Point5D.Integer(input[i + 0], input[i + 1], input[i + 2], input[i + 3], input[i + 4]);

            return result;
        }

        /**
         * Create an array of integer from the input Point5D.Integer array.<br>
         * <br>
         * The format of the output array is as follow:<br>
         * <code>result.lenght</code> = number of point * 5.<br>
         * <code>result[(pt * 5) + 0]</code> = X coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 1]</code> = Y coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 2]</code> = Z coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 3]</code> = T coordinate for point <i>pt</i><br>
         * <code>result[(pt * 5) + 4]</code> = C coordinate for point <i>pt</i><br>
         */
        public static int[] toIntegerArray(Point5D.Integer[] input)
        {
            final int[] result = new int[input.length * 5];

            int off = 0;
            for (Point5D.Integer pt : input)
            {
                result[off++] = pt.x;
                result[off++] = pt.y;
                result[off++] = pt.z;
                result[off++] = pt.t;
                result[off++] = pt.c;
            }

            return result;
        }

        public int x;
        public int y;
        public int z;
        public int t;
        public int c;

        public Integer(int x, int y, int z, int t, int c)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.c = c;
        }

        public Integer(int[] xyztc)
        {
            final int len = xyztc.length;

            if (len > 0)
                this.x = xyztc[0];
            if (len > 1)
                this.y = xyztc[1];
            if (len > 2)
                this.z = xyztc[2];
            if (len > 3)
                this.t = xyztc[3];
            if (len > 4)
                this.c = xyztc[4];
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0);
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
        public double getT()
        {
            return t;
        }

        @Override
        public void setT(double t)
        {
            this.t = (int) t;
        }

        @Override
        public double getC()
        {
            return c;
        }

        @Override
        public void setC(double c)
        {
            this.c = (int) c;
        }

        @Override
        public void setLocation(double x, double y, double z, double t, double c)
        {
            this.x = (int) x;
            this.y = (int) y;
            this.z = (int) z;
            this.t = (int) t;
            this.c = (int) c;
        }

        @Override
        public Point2D toPoint2D()
        {
            return new Point(x, y);
        }

        @Override
        public Point3D toPoint3D()
        {
            return new Point3D.Integer(x, y, z);
        }

        @Override
        public Point4D toPoint4D()
        {
            return new Point4D.Integer(x, y, z, t);
        }
    }
}
