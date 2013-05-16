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
 * Point3D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Point3D
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

    @Override
    public String toString()
    {
        return "Point3D[" + getX() + ", " + getY() + ", " + getZ() + "]";
    }

    public static class Double extends Point3D
    {
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
            return "Point3D.Double[" + x + ", " + y + ", " + z + "]";
        }
    }

    public static class Float extends Point3D
    {
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
            return "Point3D.Float[" + x + ", " + y + ", " + z + "]";
        }

    }

    public static class Integer extends Point3D
    {
        int x;
        int y;
        int z;

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
            return "Point3D.Integer[" + x + ", " + y + ", " + z + "]";
        }
    }
}
