/**
 * 
 */
package icy.type.rectangle;

/**
 * Rectangle3D class.
 * 
 * @author Stephane
 */
public class Rectangle3D
{
    private double x;
    private double y;
    private double z;

    private double sizeX;
    private double sizeY;
    private double sizeZ;

    public Rectangle3D(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        super();

        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
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
}
