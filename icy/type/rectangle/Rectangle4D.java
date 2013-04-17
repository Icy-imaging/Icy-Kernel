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

import icy.type.point.Point4D;

/**
 * Rectangle4D class.<br>
 * Incomplete implementation (work in progress...)
 * 
 * @author Stephane
 */
public abstract class Rectangle4D
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
     * Returns the point coordinate.
     */
    public Point4D getPosition()
    {
        return new Point4D.Double(getX(), getY(), getZ(), getT());
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

    public static class Double extends Rectangle4D
    {
        private double x;
        private double y;
        private double z;
        private double t;

        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private double sizeT;

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

    }

    public static class Float extends Rectangle4D
    {
        private float x;
        private float y;
        private float z;
        private float t;

        private float sizeX;
        private float sizeY;
        private float sizeZ;
        private float sizeT;

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

    }

    public static class Integer extends Rectangle4D
    {
        private int x;
        private int y;
        private int z;
        private int t;

        private int sizeX;
        private int sizeY;
        private int sizeZ;
        private int sizeT;

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
    }
}
