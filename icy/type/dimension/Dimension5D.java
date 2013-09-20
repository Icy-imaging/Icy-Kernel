/**
 * 
 */
package icy.type.dimension;

import java.awt.Dimension;

/**
 * The <code>Dimension5D</code> class is to encapsulate a 4D dimension (X,Y,Z,T).
 * 
 * @author Stephane
 */
public abstract class Dimension5D implements Cloneable
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
     * Returns the size of the C dimension in double precision.
     * 
     * @return the size of the C dimension.
     */
    public abstract double getSizeC();

    /**
     * Sets the size of the X dimension of this <code>Dimension5D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeX(double value);

    /**
     * Sets the size of the Y dimension of this <code>Dimension5D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeY(double value);

    /**
     * Sets the size of the Z dimension of this <code>Dimension5D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeZ(double value);

    /**
     * Sets the size of the T dimension of this <code>Dimension5D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeT(double value);

    /**
     * Sets the size of the C dimension of this <code>Dimension5D</code> in <code>double</code>
     * precision.
     */
    public abstract void setSizeC(double value);

    /**
     * Sets the size of this <code>Dimension5D</code> object.
     * 
     * @param sizeX
     *        the new size for the X dimension
     * @param sizeY
     *        the new size for the Y dimension
     * @param sizeZ
     *        the new size for the Z dimension
     * @param sizeT
     *        the new size for the T dimension
     * @param sizeC
     *        the new size for the C dimension
     */
    public abstract void setSize(double sizeX, double sizeY, double sizeZ, double sizeT, double sizeC);

    /**
     * Sets the size of this <code>Dimension5D</code> object to match the specified size.
     * 
     * @param d
     *        the new size for the <code>Dimension5D</code> object
     */
    public void setSize(Dimension5D d)
    {
        setSize(d.getSizeX(), d.getSizeY(), d.getSizeZ(), d.getSizeT(), d.getSizeC());
    }

    /**
     * Convert to 2D dimension.
     */
    public abstract java.awt.geom.Dimension2D toDimension2D();

    /**
     * Convert to 3D dimension.
     */
    public abstract Dimension3D toDimension3D();

    /**
     * Convert to 4D dimension.
     */
    public abstract Dimension4D toDimension4D();

    /**
     * Returns an integer {@link Dimension5D} that encloses the double <code>Dimension5D</code>.<br>
     * The returned <code>Dimension5D.Integer</code> might also fail to completely enclose the
     * original double <code>Dimension5D</code> if it overflows the limited range of the integer
     * data type.
     * 
     * @return an integer <code>Dimension5D</code> that completely encloses
     *         the actual double <code>Dimension5D</code>.
     */

    public Dimension5D.Integer toInteger()
    {
        return new Dimension5D.Integer((int) Math.ceil(getSizeX()), (int) Math.ceil(getSizeY()),
                (int) Math.ceil(getSizeZ()), (int) Math.ceil(getSizeT()), (int) Math.ceil(getSizeC()));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Dimension5D)
        {
            final Dimension5D dim = (Dimension5D) obj;
            return (getSizeX() == dim.getSizeX()) && (getSizeY() == dim.getSizeY()) && (getSizeZ() == dim.getSizeZ())
                    && (getSizeT() == dim.getSizeT()) && (getSizeC() == dim.getSizeC());
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
        return getClass().getName() + "[" + getSizeX() + "," + getSizeY() + "," + getSizeZ() + "," + getSizeT() + ","
                + getSizeC() + "]";
    }

    public static class Double extends Dimension5D
    {
        public double sizeX;
        public double sizeY;
        public double sizeZ;
        public double sizeT;
        public double sizeC;

        public Double(double sizeX, double sizeY, double sizeZ, double sizeT, double sizeC)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Double(double[] sizeXYZTC)
        {
            final int len = sizeXYZTC.length;

            if (len > 0)
                this.sizeX = sizeXYZTC[0];
            if (len > 1)
                this.sizeY = sizeXYZTC[1];
            if (len > 2)
                this.sizeZ = sizeXYZTC[2];
            if (len > 3)
                this.sizeT = sizeXYZTC[3];
            if (len > 3)
                this.sizeC = sizeXYZTC[4];
        }

        public Double()
        {
            this(0d, 0d, 0d, 0d, 0d);
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
        public double getSizeC()
        {
            return sizeC;
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
        public void setSizeC(double value)
        {
            sizeC = value;
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT, double sizeC)
        {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
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

        @Override
        public Dimension4D toDimension4D()
        {
            return new Dimension4D.Double(sizeX, sizeY, sizeZ, sizeT);
        }
    }

    public static class Float extends Dimension5D
    {
        public float sizeX;
        public float sizeY;
        public float sizeZ;
        public float sizeT;
        public float sizeC;

        public Float(float sizeX, float sizeY, float sizeZ, float sizeT, float sizeC)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Float(float[] sizeXYZTC)
        {
            final int len = sizeXYZTC.length;

            if (len > 0)
                this.sizeX = sizeXYZTC[0];
            if (len > 1)
                this.sizeY = sizeXYZTC[1];
            if (len > 2)
                this.sizeZ = sizeXYZTC[2];
            if (len > 3)
                this.sizeT = sizeXYZTC[3];
            if (len > 3)
                this.sizeC = sizeXYZTC[4];
        }

        public Float()
        {
            this(0f, 0f, 0f, 0f, 0f);
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
        public double getSizeC()
        {
            return sizeC;
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
        public void setSizeC(double value)
        {
            sizeC = (float) value;
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT, double sizeC)
        {
            this.sizeX = (float) sizeX;
            this.sizeY = (float) sizeY;
            this.sizeZ = (float) sizeZ;
            this.sizeT = (float) sizeT;
            this.sizeC = (float) sizeC;
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

        @Override
        public Dimension4D toDimension4D()
        {
            return new Dimension4D.Float(sizeX, sizeY, sizeZ, sizeT);
        }

    }

    public static class Integer extends Dimension5D
    {
        public int sizeX;
        public int sizeY;
        public int sizeZ;
        public int sizeT;
        public int sizeC;

        public Integer(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC)
        {
            super();

            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.sizeT = sizeT;
            this.sizeC = sizeC;
        }

        public Integer(int[] sizeXYZTC)
        {
            final int len = sizeXYZTC.length;

            if (len > 0)
                this.sizeX = sizeXYZTC[0];
            if (len > 1)
                this.sizeY = sizeXYZTC[1];
            if (len > 2)
                this.sizeZ = sizeXYZTC[2];
            if (len > 3)
                this.sizeT = sizeXYZTC[3];
            if (len > 4)
                this.sizeC = sizeXYZTC[4];
        }

        public Integer()
        {
            this(0, 0, 0, 0, 0);
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
        public double getSizeC()
        {
            return sizeC;
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
        public void setSizeC(double value)
        {
            sizeC = (int) Math.ceil(value);
        }

        @Override
        public void setSize(double sizeX, double sizeY, double sizeZ, double sizeT, double sizeC)
        {
            this.sizeX = (int) Math.ceil(sizeX);
            this.sizeY = (int) Math.ceil(sizeY);
            this.sizeZ = (int) Math.ceil(sizeZ);
            this.sizeT = (int) Math.ceil(sizeT);
            this.sizeC = (int) Math.ceil(sizeC);
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
        public Dimension4D toDimension4D()
        {
            return new Dimension4D.Integer(sizeX, sizeY, sizeZ, sizeT);
        }

        @Override
        public Integer toInteger()
        {
            return (Integer) clone();
        }
    }
}
