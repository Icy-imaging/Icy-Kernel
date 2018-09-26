package icy.math;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

/**
 * Line2D iterator (iterate over Line2D points given a wanted step).
 * 
 * @author Stephane Dallongeville
 */
public class Line2DIterator implements Iterator<Point2D>
{
    final static protected double DEFAULT_STEP = 1d;

    protected boolean done;
    protected int count;
    final protected boolean forceLast;

    final protected Point2D pos;
    final protected Point2D last;

    protected double sx, sy;

    /**
     * Create the Line2D Iterator.
     * 
     * @param line
     *        the lien we want to iterate points
     * @param step
     *        step between each point (default = 1d)
     * @param forceLastPoint
     *        set to <i>true</i> if you want the last point to match the end line position
     */
    public Line2DIterator(Line2D line, double step, boolean forceLastPoint)
    {
        super();

        pos = line.getP1();
        last = line.getP2();
        done = false;
        forceLast = forceLastPoint;

        final double dx = line.getX2() - line.getX1();
        final double dy = line.getY2() - line.getY1();
        final double adx = Math.abs(dx);
        final double ady = Math.abs(dy);

        final double adjStep = (step <= 0d) ? 1d : step;

        // step on X axis
        if (adx > ady)
        {
            count = (int) (adx / adjStep);
            sx = adjStep;
            sy = (ady / adx) * adjStep;
        }
        // step on Y axis
        else
        {
            if (ady == 0d)
            {
                count = 0;
                sx = 0;
            }
            else
            {
                count = (int) (ady / adjStep);
                sx = (adx / ady) * adjStep;
            }
            sy = adjStep;
        }
        // for initial position
        count++;

        // reverse step if needed
        if (dx < 0)
            sx = -sx;
        if (dy < 0)
            sy = -sy;
    }

    /**
     * Create the Line2D Iterator.
     * 
     * @param line
     *        the lien we want to iterate points
     * @param step
     *        step between each point (default = 1d)
     */
    public Line2DIterator(Line2D line, double step)
    {
        this(line, step, true);
    }

    /**
     * Create the Line2D Iterator.
     * 
     * @param line
     *        the lien we want to iterate points
     */
    public Line2DIterator(Line2D line)
    {
        this(line, DEFAULT_STEP, true);
    }

    @Override
    public boolean hasNext()
    {
        return !done;
    }

    @Override
    public Point2D next()
    {
        final Point2D result = (Point2D) pos.clone();

        // done ?
        if (--count <= 0)
        {
            if (forceLast)
            {
                // consider done only if pos is equal to last
                done = pos.equals(last);
                // force equality with last position
                pos.setLocation(last);
            }
            else
                done = true;
        }
        else
            pos.setLocation(pos.getX() + sx, pos.getY() + sy);

        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
