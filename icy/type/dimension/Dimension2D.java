/**
 * 
 */
package icy.type.dimension;

import java.awt.Dimension;

/**
 * The <code>Dimension2D</code> class is to encapsulate a 2D dimension (X,Y).
 * 
 * @author Stephane
 */
public abstract class Dimension2D extends java.awt.geom.Dimension2D
{
    /**
     * Returns the size of the X dimension in double precision.<br>
     * Same as {@link #getWidth()}
     * 
     * @return the size of the X dimension.
     * @see Dimension2D#getWidth()
     */
    public double getSizeX()
    {
        return getWidth();
    }

    /**
     * Returns the size of the Y dimension in double precision.<br>
     * Same as {@link #getHeight()}
     * 
     * @return the size of the Y dimension.
     * @see Dimension2D#getHeight()
     */
    public double getSizeY()
    {
        return getHeight();
    }

    /**
     * Sets the size of the X dimension of this <code>Dimension2D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeX(double value);

    /**
     * Sets the size of the Y dimension of this <code>Dimension2D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeY(double value);

    /**
     * Sets the size of this <code>Dimension2D</code> object.
     * 
     * @param sizeX
     *        the new size for the X dimension
     * @param sizeY
     *        the new size for the Y dimension
     */
    @Override
    public abstract void setSize(double sizeX, double sizeY);

    /**
     * Returns an integer {@link Dimension} that encloses the double <code>Dimension2D</code>.<br>
     * The returned <code>Dimension</code> might also fail to completely enclose the original double
     * <code>Dimension2D</code> if it overflows the limited range of the integer data type.
     * 
     * @return an integer <code>Dimension</code> that completely encloses
     *         the actual double <code>Dimension2D</code>.
     */

    public Dimension getDimInt()
    {
        return new Dimension((int) Math.ceil(getSizeX()), (int) Math.ceil(getSizeY()));
    }

    /**
     * Returns <code>true</code> if the X dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteX();

    /**
     * Returns <code>true</code> if the Y dimension should be considered as infinite.
     */
    public abstract boolean isInfiniteY();

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Dimension2D)
        {
            final Dimension2D dim = (Dimension2D) obj;
            return (getSizeX() == dim.getSizeX()) && (getSizeY() == dim.getSizeY());
        }

        return super.equals(obj);
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "[" + getSizeX() + "," + getSizeY() + "]";
    }

    public static class Double extends Dimension2D
    {
        public double sizeX;
        public double sizeY;

        public Double(double sizeX, double sizeY)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public Double(double[] sizeXY)
        {
            final int len = sizeXY.length;

            if (len > 0)
                sizeX = sizeXY[0];
            if (len > 1)
                sizeY = sizeXY[1];
        }

        public Double()
        {
            this(0d, 0d);
        }

        @Override
        public double getWidth()
        {
            return sizeX;
        }

        @Override
        public double getHeight()
        {
            return sizeY;
        }

        @Override
        public void setSize(double sizeX, double sizeY)
        {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = value;
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = value;
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getSizeX() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getSizeY() == java.lang.Double.POSITIVE_INFINITY);
        }
    }

    public static class Float extends Dimension2D
    {
        public float sizeX;
        public float sizeY;

        public Float(float sizeX, float sizeY)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public Float(float[] sizeXY)
        {
            final int len = sizeXY.length;

            if (len > 0)
                sizeX = sizeXY[0];
            if (len > 1)
                sizeY = sizeXY[1];
        }

        public Float()
        {
            this(0f, 0f);
        }

        @Override
        public double getWidth()
        {
            return sizeX;
        }

        @Override
        public double getHeight()
        {
            return sizeY;
        }

        @Override
        public void setSize(double sizeX, double sizeY)
        {
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = (float) value;
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = (float) value;
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getSizeX() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getSizeY() == java.lang.Float.POSITIVE_INFINITY);
        }
    }
}
