/**
 * 
 */
package icy.type.dimension;

import java.awt.Dimension;

/**
 * The <code>Dimension4D</code> class is to encapsulate a 4D dimension (X,Y,Z,T).
 * 
 * @author Stephane
 */
public abstract class Dimension4D implements Cloneable
{
    /**
     * Returns the size of the X dimension in double precision.
     * 
     * @return the size of the X dimension.
     */
    public abstract double getSizeX();

    /**
     * Returns the size of the Y dimension in double precision.
     * 
     * @return the size of the Y dimension.
     */
    public abstract double getSizeY();

    /**
     * Returns the size of the Z dimension in double precision.
     * 
     * @return the size of the Z dimension.
     */
    public abstract double getSizeZ();

    /**
     * Returns the size of the T dimension in double precision.
     * 
     * @return the size of the T dimension.
     */
    public abstract double getSizeT();

    /**
     * Sets the size of the X dimension of this <code>Dimension4D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeX(double value);

    /**
     * Sets the size of the Y dimension of this <code>Dimension4D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeY(double value);

    /**
     * Sets the size of the Z dimension of this <code>Dimension4D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeZ(double value);

    /**
     * Sets the size of the T dimension of this <code>Dimension4D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeT(double value);

    /**
     * Sets the size of this <code>Dimension4D</code> object.
     * 
     * @param sizeX
     *        the new size for the X dimension
     * @param sizeY
     *        the new size for the Y dimension
     * @param sizeZ
     *        the new size for the Z dimension
     * @param sizeT
     *        the new size for the T dimension
     */
    public abstract void setSize(double sizeX, double sizeY, double sizeZ, double sizeT);

    /**
     * Sets the size of this <code>Dimension4D</code> object to match the specified size.
     * 
     * @param d
     *        the new size for the <code>Dimension4D</code> object
     */
    public void setSize(Dimension4D d)
    {
        setSize(d.getSizeX(), d.getSizeY(), d.getSizeZ(), d.getSizeT());
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
     * Convert to 2D dimension.
     */
    public abstract java.awt.geom.Dimension2D toDimension2D();

    /**
     * Convert to 3D dimension.
     */
    public abstract Dimension3D toDimension3D();

    /**
     * Returns an integer {@link Dimension4D} that encloses the double <code>Dimension4D</code>.<br>
     * The returned <code>Dimension4D.Integer</code> might also fail to completely enclose the
     * original double <code>Dimension4D</code> if it overflows the limited range of the integer
     * data type.
     * 
     * @return an integer <code>Dimension4D</code> that completely encloses
     *         the actual double <code>Dimension4D</code>.
     */

    public Dimension4D.Integer toInteger()
    {
        return new Dimension4D.Integer((int) Math.ceil(getSizeX()), (int) Math.ceil(getSizeY()),
                (int) Math.ceil(getSizeZ()), (int) Math.ceil(getSizeT()));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Dimension4D)
        {
            final Dimension4D dim = (Dimension4D) obj;
            return (getSizeX() == dim.getSizeX()) && (getSizeY() == dim.getSizeY()) && (getSizeZ() == dim.getSizeZ())
                    && (getSizeT() == dim.getSizeT());
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
        return getClass().getName() + "[" + getSizeX() + "," + getSizeY() + "," + getSizeZ() + "," + getSizeT() + "]";
    }

    public static class Double extends Dimension4D
    {
        public double sizeX;
        public double sizeY;
        public double sizeZ;
        public double sizeT;

        public Double(double sizeX, double sizeY, double sizeZ, double sizeT)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Double(double[] sizeXYZT)
        {
            final int len = sizeXYZT.length;

            if (len > 0)
                this.sizeX = sizeXYZT[0];
            if (len > 1)
                this.sizeY = sizeXYZT[1];
            if (len > 2)
                this.sizeZ = sizeXYZT[2];
            if (len > 3)
                this.sizeT = sizeXYZT[3];
        }

        public Double()
        {
            this(0d, 0d, 0d, 0d);
        }

        @Override
        public double getSizeX()
        {
            return sizeX;
        }

        @Override
        public double getSizeY()
        {
            return sizeY;
        }

        @Override
        public double getSizeZ()
        {
            return sizeZ;
        }

        @Override
        public double getSizeT()
        {
            return sizeT;
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
        public void setSizeZ(double value)
        {
            sizeZ = value;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = value;
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT)
        {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
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

        @Override
        public boolean isInfiniteZ()
        {
            return (getSizeZ() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getSizeT() == java.lang.Double.POSITIVE_INFINITY);
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension2D.Double(sizeX, sizeY);
        }

        @Override
        public Dimension3D toDimension3D()
        {
            return new Dimension3D.Double(sizeX, sizeY, sizeZ);
        }
    }

    public static class Float extends Dimension4D
    {
        public float sizeX;
        public float sizeY;
        public float sizeZ;
        public float sizeT;

        public Float(float sizeX, float sizeY, float sizeZ, float sizeT)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Float(float[] sizeXYZT)
        {
            final int len = sizeXYZT.length;

            if (len > 0)
                this.sizeX = sizeXYZT[0];
            if (len > 1)
                this.sizeY = sizeXYZT[1];
            if (len > 2)
                this.sizeZ = sizeXYZT[2];
            if (len > 3)
                this.sizeT = sizeXYZT[3];
        }

        public Float()
        {
            this(0f, 0f, 0f, 0f);
        }

        @Override
        public double getSizeX()
        {
            return sizeX;
        }

        @Override
        public double getSizeY()
        {
            return sizeY;
        }

        @Override
        public double getSizeZ()
        {
            return sizeZ;
        }

        @Override
        public double getSizeT()
        {
            return sizeT;
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
        public void setSizeZ(double value)
        {
            sizeZ = (float) value;
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (float) value;
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT)
        {
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
            this.sizeT = (float) sizeT;
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

        @Override
        public boolean isInfiniteZ()
        {
            return (getSizeZ() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getSizeT() == java.lang.Float.POSITIVE_INFINITY);
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension2D.Float(sizeX, sizeY);
        }

        @Override
        public Dimension3D toDimension3D()
        {
            return new Dimension3D.Float(sizeX, sizeY, sizeZ);
        }
    }

    public static class Integer extends Dimension4D
    {
        public int sizeX;
        public int sizeY;
        public int sizeZ;
        public int sizeT;

        public Integer(int sizeX, int sizeY, int sizeZ, int sizeT)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
        }

        public Integer(int[] sizeXYZT)
        {
            final int len = sizeXYZT.length;

            if (len > 0)
                this.sizeX = sizeXYZT[0];
            if (len > 1)
                this.sizeY = sizeXYZT[1];
            if (len > 2)
                this.sizeZ = sizeXYZT[2];
            if (len > 3)
                this.sizeT = sizeXYZT[3];
        }

        public Integer()
        {
            this(0, 0, 0, 0);
        }

        @Override
        public double getSizeX()
        {
            return sizeX;
        }

        @Override
        public double getSizeY()
        {
            return sizeY;
        }

        @Override
        public double getSizeZ()
        {
            return sizeZ;
        }

        @Override
        public double getSizeT()
        {
            return sizeT;
        }

        @Override
        public void setSizeX(double value)
        {
            sizeX = (int) Math.ceil(value);
        }

        @Override
        public void setSizeY(double value)
        {
            sizeY = (int) Math.ceil(value);
        }

        @Override
        public void setSizeZ(double value)
        {
            sizeZ = (int) Math.ceil(value);
        }

        @Override
        public void setSizeT(double value)
        {
            sizeT = (int) Math.ceil(value);
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT)
        {
            this.sizeX = (int) Math.ceil(sizeX);
            this.sizeY = (int) Math.ceil(sizeY);
            this.sizeZ = (int) Math.ceil(sizeZ);
            this.sizeT = (int) Math.ceil(sizeT);
        }

        @Override
        public boolean isInfiniteX()
        {
            return (getSizeX() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteY()
        {
            return (getSizeY() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteZ()
        {
            return (getSizeZ() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public boolean isInfiniteT()
        {
            return (getSizeT() == java.lang.Integer.MAX_VALUE);
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension(sizeX, sizeY);
        }

        @Override
        public Dimension3D toDimension3D()
        {
            return new Dimension3D.Integer(sizeX, sizeY, sizeZ);
        }

        @Override
        public Integer toInteger()
        {
            return (Integer) clone();
        }
    }
}
