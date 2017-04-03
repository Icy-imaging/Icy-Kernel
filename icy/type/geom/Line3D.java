/**
 * 
 */
package icy.type.geom;

import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;

/**
 * This <code>Line3D</code> (3D equivalent to java Line2D class) represents a 3D line segment in {@code (x,y,z)}
 * coordinate space.
 * 
 * @author Stephane
 */
public class Line3D implements Shape3D, Cloneable
{
    /**
     * The X coordinate of the start point of the line segment.
     */
    protected double x1;
    /**
     * The Y coordinate of the start point of the line segment.
     */
    protected double y1;
    /**
     * The Z coordinate of the start point of the line segment.
     */
    protected double z1;
    /**
     * The X coordinate of the end point of the line segment.
     */
    protected double x2;
    /**
     * The Y coordinate of the end point of the line segment.
     */
    protected double y2;
    /**
     * The Z coordinate of the end point of the line segment.
     */
    protected double z2;

    /**
     * Constructs and initializes a Line with coordinates (0, 0, 0) -> (0, 0, 0).
     */
    public Line3D()
    {
        super();

        setLine(0d, 0d, 0d, 0d, 0d, 0d);
    }

    /**
     * Constructs and initializes a Line from the specified coordinates.
     * 
     * @param x1
     *        the X coordinate of the start point
     * @param y1
     *        the Y coordinate of the start point
     * @param z1
     *        the Z coordinate of the start point
     * @param x2
     *        the X coordinate of the end point
     * @param y2
     *        the Y coordinate of the end point
     * @param z2
     *        the Z coordinate of the end point
     */
    public Line3D(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        setLine(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Constructs and initializes a <code>Line3D</code> from the specified <code>Point3D</code> objects.
     * 
     * @param p1
     *        the start <code>Point3D</code> of this line segment
     * @param p2
     *        the end <code>Point3D</code> of this line segment
     */
    public Line3D(Point3D p1, Point3D p2)
    {
        setLine(p1, p2);
    }

    /**
     * Constructs and initializes a <code>Line3D</code> from the specified <code>Line3D</code> object.
     * 
     * @param l
     *        the <code>Line3D</code> to copy
     */
    public Line3D(Line3D l)
    {
        setLine(l);
    }

    /**
     * Returns the X coordinate of the start point in double precision.
     * 
     * @return the X coordinate of the start point of this {@code Line3D} object.
     */
    public double getX1()
    {
        return x1;
    }

    /**
     * Returns the Y coordinate of the start point in double precision.
     * 
     * @return the Y coordinate of the start point of this {@code Line3D} object.
     */
    public double getY1()
    {
        return y1;
    }

    /**
     * Returns the Z coordinate of the start point in double precision.
     * 
     * @return the Z coordinate of the start point of this {@code Line3D} object.
     */
    public double getZ1()
    {
        return z1;
    }

    /**
     * Returns the start <code>Point3D</code> of this <code>Line3D</code>.
     * 
     * @return the start <code>Point3D</code> of this <code>Line3D</code>.
     */
    public Point3D getP1()
    {
        return new Point3D.Double(getX1(), getY1(), getZ1());
    }

    /**
     * Returns the X coordinate of the end point in double precision.
     * 
     * @return the X coordinate of the end point of this {@code Line3D} object.
     */
    public double getX2()
    {
        return x2;
    }

    /**
     * Returns the Y coordinate of the end point in double precision.
     * 
     * @return the Y coordinate of the end point of this {@code Line3D} object.
     */
    public double getY2()
    {
        return y2;
    }

    /**
     * Returns the Z coordinate of the end point in double precision.
     * 
     * @return the Z coordinate of the end point of this {@code Line3D} object.
     */
    public double getZ2()
    {
        return z2;
    }

    /**
     * Returns the end <code>Point3D</code> of this <code>Line3D</code>.
     * 
     * @return the end <code>Point3D</code> of this <code>Line3D</code>.
     */
    public Point3D getP2()
    {
        return new Point3D.Double(getX2(), getY2(), getZ2());
    }

    /**
     * Returns the vector representing this <code>Line3D</code>.
     * 
     * @return the vector representing this <code>Line3D</code>.
     */
    public Point3D getVector()
    {
        return new Point3D.Double(getX2() - getX1(), getY2() - getY1(), getZ2() - getZ1());
    }

    /**
     * Sets the location of the end points of this <code>Line3D</code> to the specified double coordinates.
     * 
     * @param x1
     *        the X coordinate of the start point
     * @param y1
     *        the Y coordinate of the start point
     * @param z1
     *        the Z coordinate of the start point
     * @param x2
     *        the X coordinate of the end point
     * @param y2
     *        the Y coordinate of the end point
     * @param z2
     *        the Z coordinate of the start point
     */
    public void setLine(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    @Override
    public Rectangle3D getBounds()
    {
        double x, y, z;
        double sizeX, sizeY, sizeZ;

        if (x1 < x2)
        {
            x = x1;
            sizeX = x2 - x1;
        }
        else
        {
            x = x2;
            sizeX = x1 - x2;
        }
        if (y1 < y2)
        {
            y = y1;
            sizeY = y2 - y1;
        }
        else
        {
            y = y2;
            sizeY = y1 - y2;
        }
        if (z1 < z2)
        {
            z = z1;
            sizeZ = z2 - z1;
        }
        else
        {
            z = z2;
            sizeZ = z1 - z2;
        }

        return new Rectangle3D.Double(x, y, z, sizeX, sizeY, sizeZ);
    }

    /**
     * Sets the location of the end points of this <code>Line3D</code> to
     * the specified <code>Point3D</code> coordinates.
     * 
     * @param p1
     *        the start <code>Point3D</code> of the line segment
     * @param p2
     *        the end <code>Point3D</code> of the line segment
     */
    public void setLine(Point3D p1, Point3D p2)
    {
        setLine(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
    }

    /**
     * Sets the location of the end points of this <code>Line3D</code> to
     * the same as those end points of the specified <code>Line3D</code>.
     * 
     * @param l
     *        the specified <code>Line3D</code>
     */
    public void setLine(Line3D l)
    {
        setLine(l.getX1(), l.getY1(), l.getZ1(), l.getX2(), l.getY2(), l.getZ2());
    }

    /**
     * Tests if the line segment from {@code (x1,y1,z1)} to {@code (x2,y2,z2)} intersects the line segment from
     * {@code (x3,y3,z3)} to {@code (x4,y4,z4)}.
     *
     * @param x1
     *        the X coordinate of the start point of the first line segment
     * @param y1
     *        the Y coordinate of the start point of the first line segment
     * @param z1
     *        the Z coordinate of the start point of the first line segment
     * @param x2
     *        the X coordinate of the end point of the first line segment
     * @param y2
     *        the Y coordinate of the end point of the first line segment
     * @param z2
     *        the Z coordinate of the end point of the first line segment
     * @param x3
     *        the X coordinate of the start point of the end line segment
     * @param y3
     *        the Y coordinate of the start point of the end line segment
     * @param z3
     *        the Z coordinate of the start point of the end line segment
     * @param x4
     *        the X coordinate of the end point of the end line segment
     * @param y4
     *        the Y coordinate of the end point of the end line segment
     * @param z4
     *        the Z coordinate of the end point of the end line segment
     * @return <code>true</code> if the first specified line segment and the second specified line segment intersect
     *         each other; <code>false</code> otherwise.
     */
    public static boolean linesIntersect(double x1, double y1, double z1, double x2, double y2, double z2, double x3,
            double y3, double z3, double x4, double y4, double z4)
    {
        // line 1 vector
        final Point3D vA = new Point3D.Double(x2 - x1, y2 - y1, z2 - z1);
        // line 2 vector
        final Point3D vB = new Point3D.Double(x4 - x3, y4 - y3, z4 - z3);
        // vector of the 2 starting point
        final Point3D vC = new Point3D.Double(x3 - x1, y3 - y1, z3 - z1);

        final Point3D crossAB = vA.crossProduct(vB);

        if (vC.dotProduct(crossAB) != 0d) // lines are not coplanar
            return false;

        final double norm2 = crossAB.norm2();
        if (norm2 == 0d)
            return false;

        final Point3D crossCB = vC.crossProduct(vB);
        final double dot = crossCB.dotProduct(crossAB);
        final double s = dot / norm2;

        return (s >= 0d) && (s <= 1d);
    }

    /**
     * Tests if the line segment from {@code (x1,y1,z1)} to {@code (x2,y2,z2)} intersects this line segment.
     *
     * @param x1
     *        the X coordinate of the start point of the specified line segment
     * @param y1
     *        the Y coordinate of the start point of the specified line segment
     * @param z1
     *        the Z coordinate of the start point of the specified line segment
     * @param x2
     *        the X coordinate of the end point of the specified line segment
     * @param y2
     *        the Y coordinate of the end point of the specified line segment
     * @param z2
     *        the Z coordinate of the end point of the specified line segment
     * @return <true> if this line segment and the specified line segment intersect each other; <code>false</code>
     *         otherwise.
     */
    public boolean intersectsLine(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return linesIntersect(x1, y1, z1, x2, y2, z2, getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2());
    }

    /**
     * Tests if the specified line segment intersects this line segment.
     * 
     * @param l
     *        the specified <code>Line3D</code>
     * @return <code>true</code> if this line segment and the specified line
     *         segment intersect each other; <code>false</code> otherwise.
     */
    public boolean intersectsLine(Line3D l)
    {
        return linesIntersect(l.getX1(), l.getY1(), l.getZ1(), l.getX2(), l.getY2(), l.getZ2(), getX1(), getY1(),
                getZ1(), getX2(), getY2(), getZ2());
    }

    /**
     * Returns the square of the distance from a point to a line segment.
     * The distance measured is the distance between the specified point and the closest point between the specified end
     * points. If the specified point intersects the line segment in between the end points, this method returns 0.0.
     *
     * @param x1
     *        the X coordinate of the start point of the specified line segment
     * @param y1
     *        the Y coordinate of the start point of the specified line segment
     * @param z1
     *        the Z coordinate of the start point of the specified line segment
     * @param x2
     *        the X coordinate of the end point of the specified line segment
     * @param y2
     *        the Y coordinate of the end point of the specified line segment
     * @param z2
     *        the Z coordinate of the end point of the specified line segment
     * @param px
     *        the X coordinate of the specified point being measured against the specified line segment
     * @param py
     *        the Y coordinate of the specified point being measured against the specified line segment
     * @param pz
     *        the Z coordinate of the specified point being measured against the specified line segment
     * @return a double value that is the square of the distance from the specified point to the specified line segment.
     * @see #ptLineDistSq(double,double,double,double, double, double, double, double, double)
     */
    public static double ptSegDistSq(double x1, double y1, double z1, double x2, double y2, double z2, double px,
            double py, double pz)
    {
        // Adjust vectors relative to x1,y1,z1
        // x2,y2,z2 becomes relative vector from x1,y1,z1 to end of segment
        x2 -= x1;
        y2 -= y1;
        z2 -= z1;
        // px,py,pz becomes relative vector from x1,y1,z1 to test point
        px -= x1;
        py -= y1;
        pz -= z1;

        double dotprod = px * x2 + py * y2 + pz * z2;
        double projlenSq;

        if (dotprod <= 0.0)
        {
            // px,py,pz is on the side of x1,y1,z1 away from x2,y2,z2
            // distance to segment is length of px,py vector
            // "length of its (clipped) projection" is now 0.0
            projlenSq = 0.0;
        }
        else
        {
            // switch to backwards vectors relative to x2,y2,z2
            // x2,y2,z2 are already the negative of x1,y1,z1=>x2,y2,z2
            // to get px,py,pz to be the negative of px,py,pz=>x2,y2,z2
            // the dot product of two negated vectors is the same
            // as the dot product of the two normal vectors
            px = x2 - px;
            py = y2 - py;
            pz = z2 - pz;
            dotprod = px * x2 + py * y2 + pz * z2;

            if (dotprod <= 0.0)
            {
                // px,py,pz is on the side of x2,y2,z2 away from x1,y1,z1
                // distance to segment is length of (backwards) px,py,pz vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0;
            }
            else
            {
                // px,py is between x1,y1,z1 and x2,y2,z2
                // dotprod is the length of the px,py,pz vector
                // projected on the x2,y2,z2=>x1,y1,z1 vector times the
                // length of the x2,y2,z2=>x1,y1,z1 vector
                projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2 + z2 * z2);
            }
        }

        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        // (which is zero if the projection falls outside the range
        // of the line segment).
        double lenSq = px * px + py * py + pz * pz - projlenSq;
        if (lenSq < 0)
            lenSq = 0;

        return lenSq;
    }

    /**
     * Returns the distance from a point to a line segment.
     * The distance measured is the distance between the specified point and the closest point between the specified end
     * points. If the specified point intersects the line segment in between the end points, this method returns 0.0.
     *
     * @param x1
     *        the X coordinate of the start point of the specified line segment
     * @param y1
     *        the Y coordinate of the start point of the specified line segment
     * @param z1
     *        the Z coordinate of the start point of the specified line segment
     * @param x2
     *        the X coordinate of the end point of the specified line segment
     * @param y2
     *        the Y coordinate of the end point of the specified line segment
     * @param z2
     *        the Z coordinate of the end point of the specified line segment
     * @param px
     *        the X coordinate of the specified point being measured against the specified line segment
     * @param py
     *        the Y coordinate of the specified point being measured against the specified line segment
     * @param pz
     *        the Z coordinate of the specified point being measured against the specified line segment
     * @return a double value that is the distance from the specified point to the specified line segment.
     * @see #ptLineDist(double, double, double, double,double, double, double, double, double)
     */
    public static double ptSegDist(double x1, double y1, double z1, double x2, double y2, double z2, double px,
            double py, double pz)
    {
        return Math.sqrt(ptSegDistSq(x1, y1, z1, x2, y2, z2, px, py, pz));
    }

    /**
     * Returns the square of the distance from a point to this line segment.
     * The distance measured is the distance between the specified point and the closest point between the current
     * line's end points. If the specified point intersects the line segment in between the end points, this method
     * returns 0.0.
     *
     * @param px
     *        the X coordinate of the specified point being measured against this line segment
     * @param py
     *        the Y coordinate of the specified point being measured against this line segment
     * @param pz
     *        the Z coordinate of the specified point being measured against this line segment
     * @return a double value that is the square of the distance from the specified point to the current line segment.
     * @see #ptLineDistSq(double, double,double)
     */
    public double ptSegDistSq(double px, double py, double pz)
    {
        return ptSegDistSq(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), px, py, pz);
    }

    /**
     * Returns the square of the distance from a <code>Point3D</code> to this line segment.
     * The distance measured is the distance between the specified point and the closest point between the current
     * line's end points. If the specified point intersects the line segment in between the end points, this method
     * returns 0.0.
     * 
     * @param pt
     *        the specified <code>Point3D</code> being measured against this line segment.
     * @return a double value that is the square of the distance from the specified <code>Point3D</code> to the current
     *         line segment.
     * @see #ptLineDistSq(Point3D)
     */
    public double ptSegDistSq(Point3D pt)
    {
        return ptSegDistSq(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Returns the distance from a point to this line segment.
     * The distance measured is the distance between the specified point and the closest point between the current
     * line's end points. If the specified point intersects the line segment in between the end points, this method
     * returns 0.0.
     *
     * @param px
     *        the X coordinate of the specified point being measured against this line segment
     * @param py
     *        the Y coordinate of the specified point being measured against this line segment
     * @param pz
     *        the Z coordinate of the specified point being measured against this line segment
     * @return a double value that is the distance from the specified point to the current line segment.
     * @see #ptLineDist(double, double, double)
     */
    public double ptSegDist(double px, double py, double pz)
    {
        return ptSegDist(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), px, py, pz);
    }

    /**
     * Returns the distance from a <code>Point3D</code> to this line segment.
     * The distance measured is the distance between the specified point and the closest point between the current
     * line's end points. If the specified point intersects the line segment in between the end points, this method
     * returns 0.0.
     * 
     * @param pt
     *        the specified <code>Point3D</code> being measured against this line segment
     * @return a double value that is the distance from the specified <code>Point3D</code> to the current line segment.
     * @see #ptLineDist(Point3D)
     */
    public double ptSegDist(Point3D pt)
    {
        return ptSegDist(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Returns the square of the distance from a point to a line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by the specified coordinates. If the specified point intersects the line, this
     * method returns 0.0.
     *
     * @param x1
     *        the X coordinate of the start point of the specified line
     * @param y1
     *        the Y coordinate of the start point of the specified line
     * @param z1
     *        the Z coordinate of the start point of the specified line
     * @param x2
     *        the X coordinate of the end point of the specified line
     * @param y2
     *        the Y coordinate of the end point of the specified line
     * @param z2
     *        the Z coordinate of the end point of the specified line
     * @param px
     *        the X coordinate of the specified point being measured against the specified line
     * @param py
     *        the Y coordinate of the specified point being measured against the specified line
     * @param pz
     *        the Z coordinate of the specified point being measured against the specified line
     * @return a double value that is the square of the distance from the specified point to the specified line.
     * @see #ptSegDistSq(double, double, double, double,double, double, double, double, double)
     */
    public static double ptLineDistSq(double x1, double y1, double z1, double x2, double y2, double z2, double px,
            double py, double pz)
    {
        // Adjust vectors relative to x1,y1,z1
        // x2,y2 becomes relative vector from x1,y1,z1 to end of segment
        x2 -= x1;
        y2 -= y1;
        z2 -= z1;
        // px,py,pz becomes relative vector from x1,y1,z1 to test point
        px -= x1;
        py -= y1;
        pz -= z1;

        double dotprod = px * x2 + py * y2 + pz * z2;
        // dotprod is the length of the px,py vector
        // projected on the x1,y1,z1=>x2,y2,z2 vector times the
        // length of the x1,y1,z1=>x2,y2,z2 vector
        double projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2 + z2 * z2);
        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        double lenSq = px * px + py * py + pz * pz - projlenSq;

        if (lenSq < 0)
            lenSq = 0;

        return lenSq;
    }

    /**
     * Returns the distance from a point to a line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by the specified coordinates. If the specified point intersects the line, this
     * method returns 0.0.
     *
     * @param x1
     *        the X coordinate of the start point of the specified line
     * @param y1
     *        the Y coordinate of the start point of the specified line
     * @param z1
     *        the Z coordinate of the start point of the specified line
     * @param x2
     *        the X coordinate of the end point of the specified line
     * @param y2
     *        the Y coordinate of the end point of the specified line
     * @param z2
     *        the Z coordinate of the end point of the specified line
     * @param px
     *        the X coordinate of the specified point being measured against the specified line
     * @param py
     *        the Y coordinate of the specified point being measured against the specified line
     * @param pz
     *        the Z coordinate of the specified point being measured against the specified line
     * @return a double value that is the distance from the specified point to the specified line.
     * @see #ptSegDist(double, double, double, double, double, double, double, double, double)
     */
    public static double ptLineDist(double x1, double y1, double z1, double x2, double y2, double z2, double px,
            double py, double pz)
    {
        return Math.sqrt(ptLineDistSq(x1, y1, z1, x2, y2, z2, px, py, pz));
    }

    /**
     * Returns the square of the distance from a point to this line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by this <code>Line3D</code>. If the specified point intersects the line, this
     * method returns 0.0.
     *
     * @param px
     *        the X coordinate of the specified point being measured against the specified line
     * @param py
     *        the Y coordinate of the specified point being measured against the specified line
     * @param pz
     *        the Z coordinate of the specified point being measured against the specified line
     * @return a double value that is the square of the distance from a specified point to the current line.
     * @see #ptSegDistSq(double, double, double)
     */
    public double ptLineDistSq(double px, double py, double pz)
    {
        return ptLineDistSq(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), px, py, pz);
    }

    /**
     * Returns the square of the distance from a specified <code>Point3D</code> to this line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by this <code>Line3D</code>. If the specified point intersects the line, this
     * method returns 0.0.
     * 
     * @param pt
     *        the specified <code>Point3D</code> being measured against this line
     * @return a double value that is the square of the distance from a specified <code>Point3D</code> to the current
     *         line.
     * @see #ptSegDistSq(Point3D)
     */
    public double ptLineDistSq(Point3D pt)
    {
        return ptLineDistSq(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Returns the distance from a point to this line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by this <code>Line3D</code>. If the specified point intersects the line, this
     * method returns 0.0.
     *
     * @param px
     *        the X coordinate of the specified point being measured against this line
     * @param py
     *        the Y coordinate of the specified point being measured against this line
     * @param pz
     *        the Z coordinate of the specified point being measured against this line
     * @return a double value that is the distance from a specified point to the current line.
     * @see #ptSegDist(double, double, double)
     */
    public double ptLineDist(double px, double py, double pz)
    {
        return ptLineDist(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), px, py, pz);
    }

    /**
     * Returns the distance from a <code>Point3D</code> to this line.
     * The distance measured is the distance between the specified point and the closest point on the
     * infinitely-extended line defined by this <code>Line3D</code>. If the specified point intersects the line, this
     * method returns 0.0.
     * 
     * @param pt
     *        the specified <code>Point3D</code> being measured
     * @return a double value that is the distance from a specified <code>Point3D</code> to the current line.
     * @see #ptSegDist(Point3D)
     */
    public double ptLineDist(Point3D pt)
    {
        return ptLineDist(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2(), pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this <code>Line3D</code>. This method is required to
     * implement the {@link Shape3D} interface, but in the case of <code>Line3D</code> objects it always returns
     * <code>false</code> since a line contains no area.
     * 
     * @param x
     *        the X coordinate of the specified point to be tested
     * @param y
     *        the Y coordinate of the specified point to be tested
     * @param z
     *        the Z coordinate of the specified point to be tested
     * @return <code>false</code> because a <code>Line3D</code> contains no area.
     */
    @Override
    public boolean contains(double x, double y, double z)
    {
        return false;
    }

    /**
     * Tests if a given <code>Point3D</code> is inside the boundary of this <code>Line3D</code>.
     * This method is required to implement the {@link Shape3D} interface, but in the case of <code>Line3D</code>
     * objects it always returns <code>false</code> since a line contains no area.
     * 
     * @param p
     *        the specified <code>Point3D</code> to be tested
     * @return <code>false</code> because a <code>Line3D</code> contains no area.
     */
    @Override
    public boolean contains(Point3D p)
    {
        return false;
    }

    /**
     * Tests if the interior of this <code>Line3D</code> entirely contains the specified set of rectangular coordinates.
     * This method is required to implement the <code>Shape3D</code> interface, but in the case of <code>Line3D</code>
     * objects it always returns false since a line contains no area.
     * 
     * @param x
     *        the X coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param y
     *        the Y coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param z
     *        the Z coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param sizeX
     *        the width of the specified 3D rectangular area
     * @param sizeY
     *        the height of the specified 3D rectangular area
     * @param sizeZ
     *        the depth of the specified 3D rectangular area
     * @return <code>false</code> because a <code>Line3D</code> contains
     *         no area.
     */
    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return false;
    }

    /**
     * Tests if the interior of this <code>Line3D</code> entirely contains the specified <code>Rectangle3D</code>.
     * This method is required to implement the <code>Shape3D</code> interface, but in the case of <code>Line3D</code>
     * objects it always returns <code>false</code> since a line contains no area.
     * 
     * @param r
     *        the specified <code>Rectangle3D</code> to be tested
     * @return <code>false</code> because a <code>Line3D</code> contains no area.
     */
    @Override
    public boolean contains(Rectangle3D r)
    {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return intersects(new Rectangle3D.Double(x, y, z, sizeX, sizeY, sizeZ));
    }

    @Override
    public boolean intersects(Rectangle3D r)
    {
        return r.intersectsLine(getX1(), getY1(), getZ1(), getX2(), getY2(), getZ2());
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
}
