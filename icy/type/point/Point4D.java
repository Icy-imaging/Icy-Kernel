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

/**
 * Point4D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point4D
{
    /**
     * Returns the X coordinate of this <code>Point4D</code> in <code>double</code> precision.
     * 
     * @return the X coordinate of this <code>Point4D</code>.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of this <code>Point4D</code> in <code>double</code> precision.
     * 
     * @return the Y coordinate of this <code>Point4D</code>.
     */
    public abstract double getY();

    /**
     * Returns the Z coordinate of this <code>Point4D</code> in <code>double</code> precision.
     * 
     * @return the Z coordinate of this <code>Point4D</code>.
     */
    public abstract double getZ();

    /**
     * Returns the T coordinate of this <code>Point4D</code> in <code>double</code> precision.
     * 
     * @return the T coordinate of this <code>Point4D</code>.
     */
    public abstract double getT();

    /**
     * Sets the X coordinate of this <code>Point4D</code> in <code>double</code> precision.
     */
    public abstract void setX(double x);

    /**
     * Sets the Y coordinate of this <code>Point4D</code> in <code>double</code> precision.
     */
    public abstract void setY(double y);

    /**
     * Sets the Z coordinate of this <code>Point4D</code> in <code>double</code> precision.
     */
    public abstract void setZ(double z);

    /**
     * Sets the T coordinate of this <code>Point4D</code> in <code>double</code> precision.
     */
    public abstract void setT(double t);

    /**
     * Sets the location of this <code>Point4D</code> to the
     * specified <code>double</code> coordinates.
     * 
     * @param x
     *        the new X coordinate of this {@code Point4D}
     * @param y
     *        the new Y coordinate of this {@code Point4D}
     * @param z
     *        the new Z coordinate of this {@code Point4D}
     * @param t
     *        the new T coordinate of this {@code Point4D}
     */
    public void setLocation(double x, double y, double z, double t)
    {
        setX(x);
        setY(y);
        setZ(z);
        setT(t);
    }

    /**
     * Sets the location of this <code>Point4D</code> to the same
     * coordinates as the specified <code>Point4D</code> object.
     * 
     * @param p
     *        the specified <code>Point4D</code> to which to set
     *        this <code>Point4D</code>
     */
    public void setLocation(Point4D p)
    {
        setLocation(p.getX(), p.getY(), p.getZ(), p.getT());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Point4D)
        {
            final Point4D pt = (Point4D) obj;
            return (getX() == pt.getX()) && (getY() == pt.getY()) && (getZ() == pt.getZ()) && (getT() == pt.getT());
        }

        return super.equals(obj);
    }

    public static class Double extends Point4D
    {
        public double x;
        public double y;
        public double z;
        public double t;

        public Double(double x, double y, double z, double t)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }

        public Double(double[] xyzt)
        {
            final int len = xyzt.length;

            if (len > 0)
                this.x = xyzt[0];
            if (len > 1)
                this.y = xyzt[1];
            if (len > 2)
                this.z = xyzt[2];
            if (len > 3)
                this.t = xyzt[3];
        }

        public Double()
        {
            this(0, 0, 0, 0);
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
        public void setLocation(double x, double y, double z, double t)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }
    }

    public static class Float extends Point4D
    {
        public float x;
        public float y;
        public float z;
        public float t;

        public Float(float x, float y, float z, float t)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }

        public Float(float[] xyzt)
        {
            final int len = xyzt.length;

            if (len > 0)
                this.x = xyzt[0];
            if (len > 1)
                this.y = xyzt[1];
            if (len > 2)
                this.z = xyzt[2];
            if (len > 3)
                this.t = xyzt[3];
        }

        public Float()
        {
            this(0, 0, 0, 0);
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
        public void setLocation(double x, double y, double z, double t)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.t = (float) t;
        }
    }

    public static class Integer extends Point4D
    {
        public int x;
        public int y;
        public int z;
        public int t;

        public Integer(int x, int y, int z, int t)
        {
            super();

            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }

        public Integer(int[] xyzt)
        {
            final int len = xyzt.length;

            if (len > 0)
                this.x = xyzt[0];
            if (len > 1)
                this.y = xyzt[1];
            if (len > 2)
                this.z = xyzt[2];
            if (len > 3)
                this.t = xyzt[3];
        }

        public Integer()
        {
            this(0, 0, 0, 0);
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
        public void setLocation(double x, double y, double z, double t)
        {
            this.x = (int) x;
            this.y = (int) y;
            this.z = (int) z;
            this.t = (int) t;
        }
    }
}
