/**
 * 
 */
package icy.type.rectangle;

/**
 * Rectangle4D class.
 * 
 * @author Stephane
 */
public class Rectangle4D
{
    private double x;
    private double y;
    private double z;
    private double t;

    private double sizeX;
    private double sizeY;
    private double sizeZ;
    private double sizeT;

    public Rectangle4D(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ, double sizeT)
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

    public double getX()
    {
        return x;
    }

    public void setX(double value)
    {
        x = value;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double value)
    {
        y = value;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double value)
    {
        z = value;
    }

    public double getT()
    {
        return t;
    }

    public void setT(double value)
    {
        t = value;
    }

    public double getSizeX()
    {
        return sizeX;
    }

    public void setSizeX(double value)
    {
        sizeX = value;
    }

    public double getSizeY()
    {
        return sizeY;
    }

    public void setSizeY(double value)
    {
        sizeY = value;
    }

    public double getSizeZ()
    {
        return sizeZ;
    }

    public void setSizeZ(double value)
    {
        sizeZ = value;
    }

    public double getSizeT()
    {
        return sizeT;
    }

    public void setSizeT(double value)
    {
        sizeT = value;
    }
}
