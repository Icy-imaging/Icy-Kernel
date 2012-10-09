package icy.type.point;

public class Point5D
{
    double x;
    double y;
    double z;
    double t;
    double c;

    public Point5D(double x, double y, double z, double t, double c)
    {
        super();

        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
        this.c = c;
    }

    public Point5D(double[] xyztc)
    {
        final int len = xyztc.length;

        if (len > 0)
            this.x = xyztc[0];
        if (len > 1)
            this.y = xyztc[1];
        if (len > 2)
            this.z = xyztc[2];
        if (len > 3)
            this.t = xyztc[3];
        if (len > 4)
            this.c = xyztc[4];
    }

    public Point5D()
    {
        this(0, 0, 0, 0, 0);
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public double getT()
    {
        return t;
    }

    public void setT(double t)
    {
        this.t = t;
    }

    public double getC()
    {
        return c;
    }

    public void setC(double c)
    {
        this.c = c;
    }
}
