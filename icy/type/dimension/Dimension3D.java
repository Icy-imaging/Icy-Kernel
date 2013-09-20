/**
 * 
 */
package icy.type.dimension;

import java.awt.Dimension;

/**
 * The <code>Dimension3D</code> class is to encapsulate a 3D dimension (X,Y,Z).
 * 
 * @author Stephane
 */
public abstract class Dimension3D implements Cloneable
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
     * Sets the size of the X dimension of this <code>Dimension3D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeX(double value);

    /**
     * Sets the size of the Y dimension of this <code>Dimension3D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeY(double value);

    /**
     * Sets the size of the Z dimension of this <code>Dimension3D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeZ(double value);

    /**
     * Sets the size of this <code>Dimension3D</code> object.
     * 
     * @param sizeX
     *        the new size for the X dimension
     * @param sizeY
     *        the new size for the Y dimension
     * @param sizeZ
     *        the new size for the Z dimension
     */
    public abstract void setSize(double sizeX, double sizeY, double sizeZ);

    /**
     * Sets the size of this <code>Dimension3D</code> object to match the specified size.
     * 
     * @param d
     *        the new size for the <code>Dimension3D</code> object
     */
    public void setSize(Dimension3D d)
    {
        setSize(d.getSizeX(), d.getSizeY(), d.getSizeZ());
    }

    /**
     * Convert to 2D dimension.
     */
    public abstract java.awt.geom.Dimension2D toDimension2D();

    /**
     * Returns an integer {@link Dimension3D} that encloses the double <code>Dimension3D</code>.<br>
     * The returned <code>Dimension3D.Integer</code> might also fail to completely enclose the
     * original double <code>Dimension3D</code> if it overflows the limited range of the integer
     * data type.
     * 
     * @return an integer <code>Dimension3D</code> that completely encloses
     *         the actual double <code>Dimension3D</code>.
     */

    public Dimension3D.Integer toInteger()
    {
        return new Dimension3D.Integer((int) Math.ceil(getSizeX()), (int) Math.ceil(getSizeY()),
                (int) Math.ceil(getSizeZ()));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Dimension3D)
        {
            final Dimension3D dim = (Dimension3D) obj;
            return (getSizeX() == dim.getSizeX()) && (getSizeY() == dim.getSizeY()) && (getSizeZ() == dim.getSizeZ());
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
        return getClass().getName() + "[" + getSizeX() + "," + getSizeY() + "," + getSizeZ() + "]";
    }

    public static class Double extends Dimension3D
    {
        public double sizeX;
        public double sizeY;
        public double sizeZ;

        public Double(double sizeX, double sizeY, double sizeZ)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Double(double[] sizeXYZ)
        {
            final int len = sizeXYZ.length;

            if (len > 0)
                this.sizeX = sizeXYZ[0];
            if (len > 1)
                this.sizeY = sizeXYZ[1];
            if (len > 2)
                this.sizeZ = sizeXYZ[2];
        }

        public Double()
        {
            this(0d, 0d, 0d);
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
        public void setSize(double sizeX, double sizeY, double sizeZ)
        {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension2D.Double(sizeX, sizeY);
        }
    }

    public static class Float extends Dimension3D
    {
        public float sizeX;
        public float sizeY;
        public float sizeZ;

        public Float(float sizeX, float sizeY, float sizeZ)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Float(float[] sizeXYZ)
        {
            final int len = sizeXYZ.length;

            if (len > 0)
                this.sizeX = sizeXYZ[0];
            if (len > 1)
                this.sizeY = sizeXYZ[1];
            if (len > 2)
                this.sizeZ = sizeXYZ[2];
        }

        public Float()
        {
            this(0f, 0f, 0f);
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
        public void setSize(double sizeX, double sizeY, double sizeZ)
        {
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension2D.Float(sizeX, sizeY);
        }
    }

    public static class Integer extends Dimension3D
    {
        public int sizeX;
        public int sizeY;
        public int sizeZ;

        public Integer(int sizeX, int sizeY, int sizeZ)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Integer(int[] sizeXYZ)
        {
            final int len = sizeXYZ.length;

            if (len > 0)
                this.sizeX = sizeXYZ[0];
            if (len > 1)
                this.sizeY = sizeXYZ[1];
            if (len > 2)
                this.sizeZ = sizeXYZ[2];
        }

        public Integer()
        {
            this(0, 0, 0);
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
        public void setSize(double sizeX, double sizeY, double sizeZ)
        {
            this.sizeX = (int) Math.ceil(sizeX);
            this.sizeY = (int) Math.ceil(sizeY);
            this.sizeZ = (int) Math.ceil(sizeZ);
        }

        @Override
        public java.awt.geom.Dimension2D toDimension2D()
        {
            return new Dimension(sizeX, sizeY);
        }

        @Override
        public Integer toInteger()
        {
            return (Integer) clone();
        }
    }
}
