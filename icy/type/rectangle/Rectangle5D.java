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

import icy.type.point.Point5D;

/**
 * Rectangle5D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle5D
{
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

    public static class Double extends Rectangle5D
    {
        private double x;
        private double y;
        private double z;
        private double t;
        private double c;

        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private double sizeT;
        private double sizeC;

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
    }

    public static class Float extends Rectangle5D
    {
        private float x;
        private float y;
        private float z;
        private float t;
        private float c;

        private float sizeX;
        private float sizeY;
        private float sizeZ;
        private float sizeT;
        private float sizeC;

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
    }

    public static class Integer extends Rectangle5D
    {
        private int x;
        private int y;
        private int z;
        private int t;
        private int c;

        private int sizeX;
        private int sizeY;
        private int sizeZ;
        private int sizeT;
        private int sizeC;

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
    }
}
