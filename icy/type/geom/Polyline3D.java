package icy.type.geom;

import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;

import java.util.ArrayList;
import java.util.List;

public class Polyline3D implements Shape3D, Cloneable
{
    /**
     * The total number of points. The value of <code>npoints</code> represents the number of points in this
     * <code>Polyline3D</code>.
     */
    public int npoints;

    /**
     * The array of <i>x</i> coordinates. The value of {@link #npoints} is equal to the
     * number of points in this <code>Polyline3D</code>.
     */
    public double[] xpoints;

    /**
     * The array of <i>y</i> coordinates. The value of {@link #npoints} is equal to the
     * number of points in this <code>Polyline3D</code>.
     */
    public double[] ypoints;
    /**
     * The array of <i>z</i> coordinates. The value of {@link #npoints} is equal to the
     * number of points in this <code>Polyline3D</code>.
     */
    public double[] zpoints;

    /**
     * Bounds of the Polyline3D.
     * 
     * @see #getBounds()
     */
    protected Rectangle3D bounds;

    protected List<Line3D> lines;

    /**
     * Creates an empty Polyline3D.
     */
    public Polyline3D()
    {
        super();

        reset();
    }

    /**
     * Constructs and initializes a <code>Polyline3D</code> from the specified parameters.
     * 
     * @param xpoints
     *        an array of <i>x</i> coordinates
     * @param ypoints
     *        an array of <i>y</i> coordinates
     * @param zpoints
     *        an array of <i>z</i> coordinates
     * @param npoints
     *        the total number of points in the <code>Polyline3D</code>
     * @exception NegativeArraySizeException
     *            if the value of <code>npoints</code> is negative.
     * @exception IndexOutOfBoundsException
     *            if <code>npoints</code> is greater than the length of points array.
     * @exception NullPointerException
     *            if one of the points array is <code>null</code>.
     */
    public Polyline3D(double[] xpoints, double[] ypoints, double[] zpoints, int npoints)
    {
        super();

        if (npoints > xpoints.length || npoints > ypoints.length || npoints > zpoints.length)
            throw new IndexOutOfBoundsException("npoints > points.length");

        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.zpoints = new double[npoints];

        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
        System.arraycopy(zpoints, 0, this.zpoints, 0, npoints);

        calculateLines();
    }

    /**
     * Constructs and initializes a <code>Polyline3D</code> from the specified parameters.
     * 
     * @param xpoints
     *        an array of <i>x</i> coordinates
     * @param ypoints
     *        an array of <i>y</i> coordinates
     * @param zpoints
     *        an array of <i>z</i> coordinates
     * @param npoints
     *        the total number of points in the <code>Polyline3D</code>
     * @exception NegativeArraySizeException
     *            if the value of <code>npoints</code> is negative.
     * @exception IndexOutOfBoundsException
     *            if <code>npoints</code> is greater than the length of points array.
     * @exception NullPointerException
     *            if one of the points array is <code>null</code>.
     */
    public Polyline3D(int[] xpoints, int[] ypoints, int[] zpoints, int npoints)
    {
        super();

        if (npoints > xpoints.length || npoints > ypoints.length || npoints > zpoints.length)
            throw new IndexOutOfBoundsException("npoints > points.length");

        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.zpoints = new double[npoints];

        for (int i = 0; i < npoints; i++)
        {
            this.xpoints[i] = xpoints[i];
            this.ypoints[i] = ypoints[i];
            this.zpoints[i] = zpoints[i];
        }

        calculateLines();
    }

    public Polyline3D(Line3D line)
    {
        super();

        npoints = 2;
        xpoints = new double[2];
        ypoints = new double[2];
        zpoints = new double[2];

        xpoints[0] = line.getX1();
        xpoints[1] = line.getX2();
        ypoints[0] = line.getY1();
        ypoints[1] = line.getY2();
        zpoints[0] = line.getZ1();
        zpoints[1] = line.getZ2();

        calculateLines();
    }

    /**
     * Resets this <code>Polyline3D</code> object to an empty polygon.
     * The coordinate arrays and the data in them are left untouched
     * but the number of points is reset to zero to mark the old
     * vertex data as invalid and to start accumulating new vertex
     * data at the beginning.
     * All internally-cached data relating to the old vertices
     * are discarded.
     * Note that since the coordinate arrays from before the reset
     * are reused, creating a new empty <code>Polyline3D</code> might
     * be more memory efficient than resetting the current one if
     * the number of vertices in the new polyline data is significantly
     * smaller than the number of vertices in the data from before the
     * reset.
     */
    public void reset()
    {
        npoints = 0;
        xpoints = new double[0];
        ypoints = new double[0];
        zpoints = new double[0];
        bounds = new Rectangle3D.Double();
        lines = new ArrayList<Line3D>();
    }

    @Override
    public Object clone()
    {
        Polyline3D pol = new Polyline3D();

        for (int i = 0; i < npoints; i++)
            pol.addPoint(xpoints[i], ypoints[i], zpoints[i]);

        return pol;
    }

    public void calculateLines()
    {
        final List<Line3D> newLines = new ArrayList<Line3D>();
        double xmin, ymin, zmin;
        double xmax, ymax, zmax;

        if (npoints > 0)
        {
            // first point
            Point3D pos = new Point3D.Double(xpoints[0], ypoints[0], zpoints[0]);

            // init bounds
            xmin = xmax = pos.getX();
            ymin = ymax = pos.getY();
            zmin = zmax = pos.getZ();

            // special case
            if (npoints == 1)
                newLines.add(new Line3D(pos, pos));
            else
            {
                for (int i = 1; i < npoints; i++)
                {
                    final double x = xpoints[i];
                    final double y = ypoints[i];
                    final double z = zpoints[i];
                    final Point3D newPos = new Point3D.Double(x, y, z);

                    if (x < xmin)
                        xmin = x;
                    if (y < ymin)
                        ymin = y;
                    if (z < zmin)
                        zmin = z;
                    if (x > xmax)
                        xmax = x;
                    if (y > ymax)
                        ymax = y;
                    if (z > zmax)
                        zmax = z;

                    newLines.add(new Line3D(pos, newPos));
                    pos = newPos;
                }
            }
        }
        else
        {
            xmin = ymin = zmin = 0d;
            xmax = ymax = zmax = 0d;
        }

        bounds = new Rectangle3D.Double(xmin, ymin, zmin, xmax - xmin, ymax - ymin, zmax - zmin);
        lines = newLines;
    }

    protected void updateLines(double x, double y, double z)
    {
        if (lines.isEmpty())
        {
            lines.add(new Line3D(x, y, z, x, y, z));
            bounds = new Rectangle3D.Double(x, y, z, 0d, 0d, 0d);
        }
        else
        {
            final Line3D lastLine = lines.get(lines.size() - 1);
            final Line3D newLine = new Line3D(lastLine.getX2(), lastLine.getY2(), lastLine.getZ2(), x, y, z);
            lines.add(newLine);
            bounds.add(newLine.getBounds());
        }
    }

    /**
     * Appends the specified coordinates to this <code>Polyline3D</code>.
     * <p>
     * If an operation that calculates the bounding box of this <code>Polyline3D</code> has already been performed, such
     * as <code>getBounds</code> or <code>contains</code>, then this method updates the bounding box.
     * 
     * @param p
     *        the point to add
     */
    public void addPoint(Point3D p)
    {
        addPoint(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Appends the specified coordinates to this <code>Polyline3D</code>.
     * <p>
     * If an operation that calculates the bounding box of this <code>Polyline3D</code> has already been performed, such
     * as <code>getBounds</code> or <code>contains</code>, then this method updates the bounding box.
     * 
     * @param x
     *        the specified x coordinate
     * @param y
     *        the specified y coordinate
     * @param z
     *        the specified z coordinate
     */
    public void addPoint(double x, double y, double z)
    {
        if (npoints == xpoints.length)
        {
            double[] tmp;

            tmp = new double[npoints * 2];
            System.arraycopy(xpoints, 0, tmp, 0, npoints);
            xpoints = tmp;

            tmp = new double[npoints * 2];
            System.arraycopy(ypoints, 0, tmp, 0, npoints);
            ypoints = tmp;

            tmp = new double[npoints * 2];
            System.arraycopy(zpoints, 0, tmp, 0, npoints);
            zpoints = tmp;
        }

        xpoints[npoints] = x;
        ypoints[npoints] = y;
        zpoints[npoints] = z;
        npoints++;

        updateLines(x, y, z);
    }

    @Override
    public Rectangle3D getBounds()
    {
        return (Rectangle3D) bounds.clone();
    }

    @Override
    public boolean contains(Point3D p)
    {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double z)
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
        if (lines.isEmpty() || !bounds.intersects(r))
            return false;

        for (Line3D line : lines)
            if (line.intersects(r))
                return true;

        return false;
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return false;
    }

    @Override
    public boolean contains(Rectangle3D r)
    {
        return false;
    }
}
