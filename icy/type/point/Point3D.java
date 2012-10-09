/**
 * 
 */
package icy.type.point;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 * @author Stephane
 */
public class Point3D extends Point3d
{
    /**
     * 
     */
    private static final long serialVersionUID = 8749743213894368046L;

    public Point3D(double x, double y, double z)
    {
        super(x, y, z);
    }

    public Point3D(double[] xyz)
    {
        super(xyz);
    }

    public Point3D(Point3d pt)
    {
        super(pt);
    }

    public Point3D(Point3f pt)
    {
        super(pt);
    }

    public Point3D()
    {
        super();
    }
}
