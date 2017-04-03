package icy.math;

import icy.type.geom.Line3D;
import icy.type.point.Point3D;

import java.util.Iterator;

/**
 * Line3D iterator (iterate over Line3D points given a wanted step).
 * 
 * @author Stephane Dallongeville
 */
public class Line3DIterator implements Iterator<Point3D>
{
    final static protected double DEFAULT_STEP = 1d;

    protected boolean done;
    protected int count;

    final protected Point3D pos;
    final protected Point3D last;
    protected double sx, sy, sz;

    /**
     * Create the Line3D Iterator.
     * 
     * @param line
     *        the lien we want to iterate points
     * @param step
     *        step between each point (default = 1d)
     */
    public Line3DIterator(Line3D line, double step)
    {
        super();

        pos = line.getP1();
        last = line.getP2();
        done = false;

        final double dx = last.getX() - pos.getX();
        final double dy = last.getY() - pos.getY();
        final double dz = last.getZ() - pos.getZ();
        final double adx = Math.abs(dx);
        final double ady = Math.abs(dy);
        final double adz = Math.abs(dz);

        final double adjStep = (step <= 0d) ? 1d : step;

        // step on X axis
        if ((adx >= ady) && (adx >= adz))
        {
            if (adx == 0d)
            {
                count = 0;
                sy = 0;
                sz = 0;
            }
            else
            {
                count = (int) (adx / adjStep);
                sy = (ady / adx) * adjStep;
                sz = (adz / adx) * adjStep;
            }
            sx = adjStep;
        }
        // adjStep on Y axis
        else if ((ady >= adx) && (ady >= adz))
        {
            count = (int) (ady / adjStep);
            sx = (adx / ady) * adjStep;
            sy = adjStep;
            sz = (adz / ady) * adjStep;
        }
        // adjStep on Z axis
        else
        {
            count = (int) (adz / adjStep);
            sx = (adx / adz) * adjStep;
            sy = (ady / adz) * adjStep;
            sz = adjStep;
        }
        // for initial position
        count++;

        // reverse step if needed
        if (dx < 0)
            sx = -sx;
        if (dy < 0)
            sy = -sy;
        if (dz < 0)
            sz = -sz;
    }

    /**
     * Create the Line3D Iterator.
     * 
     * @param line
     *        the lien we want to iterate points
     */
    public Line3DIterator(Line3D line)
    {
        this(line, DEFAULT_STEP);
    }

    @Override
    public boolean hasNext()
    {
        return !done;
    }

    @Override
    public Point3D next()
    {
        final Point3D result = (Point3D) pos.clone();

        // done ?
        if (--count <= 0)
        {
            // consider done only if pos is equal to last
            done = pos.equals(last);
            // force equality with last position
            pos.setLocation(last);
        }
        else
            pos.setLocation(pos.getX() + sx, pos.getY() + sy, pos.getZ() + sz);

        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
