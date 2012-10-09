/**
 * 
 */
package icy.type.point;

import javax.vecmath.Point4d;
import javax.vecmath.Point4f;

/**
 * @author Stephane
 */
public class Point4D extends Point4d
{
    /**
     * 
     */
    private static final long serialVersionUID = 3741616052565727065L;

    public Point4D(double x, double y, double z, double t)
    {
        super(x, y, z, t);
    }

    public Point4D(double[] xyzt)
    {
        super(xyzt);
    }

    public Point4D(Point4d pt)
    {
        super(pt);
    }

    public Point4D(Point4f pt)
    {
        super(pt);
    }

    public Point4D()
    {
        super();
    }

    public double getT()
    {
        return getW();
    }

    public void setT(double value)
    {
        setW(value);
    }
}
