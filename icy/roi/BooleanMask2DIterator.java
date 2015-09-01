/**
 * 
 */
package icy.roi;

import icy.type.Position2DIterator;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.NoSuchElementException;

/**
 * BooleanMask2D iterator.<br>
 * This class permit to use simple iterator to navigate each point of specified BooleanMask2D in XY
 * <i>([Y[X]])</i> dimension order.<br>
 * <b>If the BooleanMask is modified during iteration the iterator becomes invalid and exception can
 * happen.</b>
 * 
 * @author Stephane
 */
public class BooleanMask2DIterator implements Position2DIterator
{
    protected final BooleanMask2D mask;
    protected final int w, h;
    protected int x, y;
    protected int off;
    protected boolean done;

    /**
     * Create a new BooleanMask2D iterator to iterate each point through the specified
     * <code>BooleanMask2D</code>
     * 
     * @param mask
     *        BooleanMask2D to iterate
     */
    public BooleanMask2DIterator(BooleanMask2D mask)
    {
        super();

        this.mask = mask;
        // cached
        w = mask.bounds.width;
        h = mask.bounds.height;

        // start iterator
        reset();
    }

    public int getMinX()
    {
        return mask.bounds.x;
    }

    public int getMaxX()
    {
        return (mask.bounds.x + w) - 1;
    }

    public int getMinY()
    {
        return mask.bounds.y;
    }

    public int getMaxY()
    {
        return (mask.bounds.y + h) - 1;
    }

    @Override
    public void reset()
    {
        done = mask.bounds.isEmpty();

        if (!done)
        {
            // reset position
            y = 0;
            x = -1;
            off = -1;
            // allow to correctly set initial position in boolean mask
            next();
        }
    }

    @Override
    public void next()
    {
        if (done)
            throw new NoSuchElementException();

        do
            nextPosition();
        // advance while mask do not contains current point
        while (!done && !mask.mask[off]);
    }

    /**
     * Advance one position
     */
    protected void nextPosition()
    {
        off++;
        if (++x >= w)
        {
            x = 0;
            if (++y >= h)
                done = true;
        }
    }

    @Override
    public boolean done()
    {
        return done;
    }

    @Override
    public Point2D get()
    {
        if (done)
            throw new NoSuchElementException();

        return new Point(getX(), getY());
    }

    @Override
    public int getX()
    {
        if (done)
            throw new NoSuchElementException();

        return x + mask.bounds.x;
    }

    @Override
    public int getY()
    {
        if (done)
            throw new NoSuchElementException();

        return y + mask.bounds.y;
    }
}
