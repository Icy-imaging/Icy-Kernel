/**
 * 
 */
package icy.type.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modified by Stephane Dallongeville
 */

/**
 * This class is a Polygon with double coordinates.
 */
public class Polygon2D implements Shape, Cloneable
{
    private static void addFarthestPoint(List<Point2D> result, List<Point2D> points, int start, int end,
            double maxDeviation)
    {
        final Point2D p1 = points.get(start);
        final Point2D p2 = points.get(end);
        final double x1 = p1.getX();
        final double y1 = p1.getY();
        final double x2 = p2.getX();
        final double y2 = p2.getY();
        int farthest = -1;
        // initialize with maximum allow deviation (squared)
        double maxDist = maxDeviation * maxDeviation;

        for (int i = start + 1; i < end; i++)
        {
            final Point2D p = points.get(i);
            final double dist = Line2D.ptSegDistSq(x1, y1, x2, y2, p.getX(), p.getY());

            if (dist > maxDist)
            {
                farthest = i;
                maxDist = dist;
            }
        }

        // found a point to add ?
        if (farthest != -1)
        {
            // search before point
            addFarthestPoint(result, points, start, farthest, maxDeviation);
            // add point
            result.add(points.get(farthest));
            // search after point
            addFarthestPoint(result, points, farthest, end, maxDeviation);
        }
    }

    /**
     * Returns a polygon2D corresponding to the polygon estimation of the given (closed) contour points with the
     * specified <code>max deviation</code>.
     * 
     * @param points
     *        the list of points representing the input closed contour to transform to polygon.
     * @param maxDeviation
     *        maximum allowed deviation/distance of resulting polygon from the input contour (in pixel).
     * @return the polygon estimation from input contour
     */
    public static Polygon2D getPolygon2D(List<Point2D> points, double maxDeviation)
    {
        // just return
        if (points.size() < 3)
            return new Polygon2D(points);

        final List<Point2D> result = new ArrayList<Point2D>(points.size() / 4);

        int ind = points.size() / 2;

        // close the contour
        points.add(points.get(0));

        // add first point
        result.add(points.get(0));
        // add points between first and medium
        addFarthestPoint(result, points, 0, ind, maxDeviation);
        // add medium point
        result.add(points.get(ind));
        // add points between medium and end
        addFarthestPoint(result, points, ind, points.size() - 1, maxDeviation);

        // restore original contour
        points.remove(points.size() - 1);

        return new Polygon2D(result);
    }

    /**
     * The total number of points. The value of <code>npoints</code> represents the number of valid points in this
     * <code>Polygon</code>.
     */
    public int npoints;

    /**
     * The array of <i>x</i> coordinates. The value of {@link #npoints} is equal to the
     * number of points in this <code>Polygon2D</code>.
     */
    public double[] xpoints;

    /**
     * The array of <i>y</i> coordinates. The value of {@link #npoints} is equal to the
     * number of points in this <code>Polygon2D</code>.
     */
    public double[] ypoints;

    /**
     * Bounds of the Polygon2D.
     * 
     * @see #getBounds()
     */
    protected Rectangle2D bounds;

    protected Path2D.Double path;
    protected Path2D.Double closedPath;

    /**
     * Creates an empty Polygon2D.
     */
    public Polygon2D()
    {
        super();

        reset();
    }

    /**
     * Constructs and initializes a <code>Polygon2D</code> from the specified
     * Rectangle2D.
     * 
     * @param rec
     *        the Rectangle2D
     * @exception NullPointerException
     *            rec is <code>null</code>.
     */
    public Polygon2D(Rectangle2D rec)
    {
        super();

        if (rec == null)
            throw new IllegalArgumentException("null Rectangle");

        npoints = 4;
        xpoints = new double[4];
        ypoints = new double[4];

        xpoints[0] = rec.getMinX();
        ypoints[0] = rec.getMinY();
        xpoints[1] = rec.getMaxX();
        ypoints[1] = rec.getMinY();
        xpoints[2] = rec.getMaxX();
        ypoints[2] = rec.getMaxY();
        xpoints[3] = rec.getMinX();
        ypoints[3] = rec.getMaxY();

        calculatePath();
    }

    /**
     * Constructs and initializes a <code>Polygon2D</code> from the specified
     * Polygon.
     * 
     * @param pol
     *        the Polygon
     * @exception NullPointerException
     *            pol is <code>null</code>.
     */
    public Polygon2D(Polygon pol)
    {
        super();

        if (pol == null)
            throw new IllegalArgumentException("null Polygon");

        this.npoints = pol.npoints;
        this.xpoints = new double[pol.npoints];
        this.ypoints = new double[pol.npoints];

        for (int i = 0; i < pol.npoints; i++)
        {
            xpoints[i] = pol.xpoints[i];
            ypoints[i] = pol.ypoints[i];
        }

        calculatePath();
    }

    /**
     * Constructs and initializes a <code>Polygon2D</code> from the specified
     * parameters.
     * 
     * @param xpoints
     *        an array of <i>x</i> coordinates
     * @param ypoints
     *        an array of <i>y</i> coordinates
     * @param npoints
     *        the total number of points in the <code>Polygon2D</code>
     * @exception NegativeArraySizeException
     *            if the value of <code>npoints</code> is negative.
     * @exception IndexOutOfBoundsException
     *            if <code>npoints</code> is
     *            greater than the length of <code>xpoints</code> or the length of <code>ypoints</code>.
     * @exception NullPointerException
     *            if <code>xpoints</code> or <code>ypoints</code> is <code>null</code>.
     */
    public Polygon2D(double[] xpoints, double[] ypoints, int npoints)
    {
        super();

        if (npoints > xpoints.length || npoints > ypoints.length)
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");

        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];

        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);

        calculatePath();
    }

    /**
     * Constructs and initializes a <code>Polygon2D</code> from the specified
     * parameters.
     * 
     * @param xpoints
     *        an array of <i>x</i> coordinates
     * @param ypoints
     *        an array of <i>y</i> coordinates
     * @param npoints
     *        the total number of points in the <code>Polygon2D</code>
     * @exception NegativeArraySizeException
     *            if the value of <code>npoints</code> is negative.
     * @exception IndexOutOfBoundsException
     *            if <code>npoints</code> is
     *            greater than the length of <code>xpoints</code> or the length of <code>ypoints</code>.
     * @exception NullPointerException
     *            if <code>xpoints</code> or <code>ypoints</code> is <code>null</code>.
     */
    public Polygon2D(int[] xpoints, int[] ypoints, int npoints)
    {
        super();

        if (npoints > xpoints.length || npoints > ypoints.length)
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");

        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];

        for (int i = 0; i < npoints; i++)
        {
            this.xpoints[i] = xpoints[i];
            this.ypoints[i] = ypoints[i];
        }

        calculatePath();
    }

    public Polygon2D(List<Point2D> points)
    {
        super();

        final int len = points.size();

        this.npoints = len;
        this.xpoints = new double[len];
        this.ypoints = new double[len];

        for (int i = 0; i < len; i++)
        {
            final Point2D pt = points.get(i);

            this.xpoints[i] = pt.getX();
            this.ypoints[i] = pt.getY();
        }

        calculatePath();
    }

    /**
     * Resets this <code>Polygon</code> object to an empty polygon.
     */
    public void reset()
    {
        npoints = 0;
        xpoints = new double[0];
        ypoints = new double[0];
        bounds = new Rectangle2D.Double();
        path = null;
        closedPath = null;
    }

    @Override
    public Object clone()
    {
        Polygon2D pol = new Polygon2D();

        for (int i = 0; i < npoints; i++)
            pol.addPoint(xpoints[i], ypoints[i]);

        return pol;
    }

    public void calculatePath()
    {
        path = new Path2D.Double();

        path.moveTo(xpoints[0], ypoints[0]);
        for (int i = 1; i < npoints; i++)
            path.lineTo(xpoints[i], ypoints[i]);

        bounds = path.getBounds2D();
        closedPath = null;
    }

    protected void updatePath(double x, double y)
    {
        if (path == null)
        {
            path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
            path.moveTo(x, y);
            bounds = new Rectangle2D.Double(x, y, 0, 0);
        }
        else
        {
            path.lineTo(x, y);

            double _xmax = bounds.getMaxX();
            double _ymax = bounds.getMaxY();
            double _xmin = bounds.getMinX();
            double _ymin = bounds.getMinY();

            if (x < _xmin)
                _xmin = x;
            else if (x > _xmax)
                _xmax = x;
            if (y < _ymin)
                _ymin = y;
            else if (y > _ymax)
                _ymax = y;

            bounds = new Rectangle2D.Double(_xmin, _ymin, _xmax - _xmin, _ymax - _ymin);
        }

        closedPath = null;
    }

    /*
     * get the associated {@link Polyline2D}.
     */
    public Polyline2D getPolyline2D()
    {
        Polyline2D pol = new Polyline2D(xpoints, ypoints, npoints);

        pol.addPoint(xpoints[0], ypoints[0]);

        return pol;
    }

    public Polygon getPolygon()
    {
        int[] _xpoints = new int[npoints];
        int[] _ypoints = new int[npoints];

        for (int i = 0; i < npoints; i++)
        {
            _xpoints[i] = (int) xpoints[i]; // todo maybe rounding is better ?
            _ypoints[i] = (int) ypoints[i];
        }

        return new Polygon(_xpoints, _ypoints, npoints);
    }

    public void addPoint(Point2D p)
    {
        addPoint(p.getX(), p.getY());
    }

    /**
     * Appends the specified coordinates to this <code>Polygon2D</code>.
     * 
     * @param x
     *        the specified x coordinate
     * @param y
     *        the specified y coordinate
     */
    public void addPoint(double x, double y)
    {
        if (npoints == xpoints.length)
        {
            double[] tmp;

            tmp = new double[(npoints * 2) + 1];
            System.arraycopy(xpoints, 0, tmp, 0, npoints);
            xpoints = tmp;

            tmp = new double[(npoints * 2) + 1];
            System.arraycopy(ypoints, 0, tmp, 0, npoints);
            ypoints = tmp;
        }

        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;

        updatePath(x, y);
    }

    /**
     * Determines whether the specified {@link Point} is inside this <code>Polygon</code>.
     * 
     * @param p
     *        the specified <code>Point</code> to be tested
     * @return <code>true</code> if the <code>Polygon</code> contains the <code>Point</code>; <code>false</code>
     *         otherwise.
     * @see #contains(double, double)
     */
    public boolean contains(Point p)
    {
        return contains(p.x, p.y);
    }

    /**
     * Determines whether the specified coordinates are inside this <code>Polygon</code>.
     * <p>
     * 
     * @param x
     *        the specified x coordinate to be tested
     * @param y
     *        the specified y coordinate to be tested
     * @return <code>true</code> if this <code>Polygon</code> contains
     *         the specified coordinates, (<i>x</i>,&nbsp;<i>y</i>); <code>false</code> otherwise.
     */
    public boolean contains(int x, int y)
    {
        return contains((double) x, (double) y);
    }

    /**
     * Returns the high precision bounding box of the {@link Shape}.
     * 
     * @return a {@link Rectangle2D} that precisely
     *         bounds the <code>Shape</code>.
     */
    @Override
    public Rectangle2D getBounds2D()
    {
        return (Rectangle2D) bounds.clone();
    }

    @Override
    public Rectangle getBounds()
    {
        return bounds.getBounds();
    }

    /**
     * Determines if the specified coordinates are inside this <code>Polygon</code>. For the definition of
     * <i>insideness</i>, see the class comments of {@link Shape}.
     * 
     * @param x
     *        the specified x coordinate
     * @param y
     *        the specified y coordinate
     * @return <code>true</code> if the <code>Polygon</code> contains the
     *         specified coordinates; <code>false</code> otherwise.
     */
    @Override
    public boolean contains(double x, double y)
    {
        if (npoints <= 2 || !bounds.contains(x, y))
            return false;

        return updateComputingPath().contains(x, y);
    }

    protected Path2D.Double updateComputingPath()
    {
        Path2D.Double result = closedPath;

        // need to recompute it ?
        if (result == null)
        {
            if (path != null)
            {
                result = (Path2D.Double) path.clone();
                result.closePath();
            }
            else
                // empty path
                result = new Path2D.Double();

            closedPath = result;
        }

        return result;
    }

    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this <code>Polygon</code>.
     * 
     * @param p
     *        a specified <code>Point2D</code>
     * @return <code>true</code> if this <code>Polygon</code> contains the
     *         specified <code>Point2D</code>; <code>false</code> otherwise.
     * @see #contains(double, double)
     */
    @Override
    public boolean contains(Point2D p)
    {
        return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the interior of this <code>Polygon</code> intersects the
     * interior of a specified set of rectangular coordinates.
     * 
     * @param x
     *        the x coordinate of the specified rectangular
     *        shape's top-left corner
     * @param y
     *        the y coordinate of the specified rectangular
     *        shape's top-left corner
     * @param w
     *        the width of the specified rectangular shape
     * @param h
     *        the height of the specified rectangular shape
     * @return <code>true</code> if the interior of this <code>Polygon</code> and the interior of the
     *         specified set of rectangular
     *         coordinates intersect each other; <code>false</code> otherwise.
     */
    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        if (npoints <= 0 || !bounds.intersects(x, y, w, h))
            return false;

        return updateComputingPath().intersects(x, y, w, h);
    }

    /**
     * Tests if the interior of this <code>Polygon</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     * 
     * @param r
     *        a specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Polygon</code> and the
     *         interior of the specified <code>Rectangle2D</code> intersect each other; <code>false</code> otherwise.
     */
    @Override
    public boolean intersects(Rectangle2D r)
    {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the interior of this <code>Polygon</code> entirely
     * contains the specified set of rectangular coordinates.
     * 
     * @param x
     *        the x coordinate of the top-left corner of the
     *        specified set of rectangular coordinates
     * @param y
     *        the y coordinate of the top-left corner of the
     *        specified set of rectangular coordinates
     * @param w
     *        the width of the set of rectangular coordinates
     * @param h
     *        the height of the set of rectangular coordinates
     * @return <code>true</code> if this <code>Polygon</code> entirely
     *         contains the specified set of rectangular
     *         coordinates; <code>false</code> otherwise.
     */
    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        if (npoints <= 0 || !bounds.intersects(x, y, w, h))
            return false;

        return updateComputingPath().contains(x, y, w, h);
    }

    /**
     * Tests if the interior of this <code>Polygon</code> entirely
     * contains the specified <code>Rectangle2D</code>.
     * 
     * @param r
     *        the specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Polygon</code> entirely
     *         contains the specified <code>Rectangle2D</code>; <code>false</code> otherwise.
     * @see #contains(double, double, double, double)
     */
    @Override
    public boolean contains(Rectangle2D r)
    {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns an iterator object that iterates along the boundary of this <code>Polygon</code> and provides access to
     * the geometry of the outline of this <code>Polygon</code>. An optional {@link AffineTransform} can be specified so
     * that the coordinates returned in the iteration are transformed accordingly.
     * 
     * @param at
     *        an optional <code>AffineTransform</code> to be applied to the
     *        coordinates as they are returned in the iteration, or <code>null</code> if untransformed coordinates are
     *        desired
     * @return a {@link PathIterator} object that provides access to the
     *         geometry of this <code>Polygon</code>.
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return updateComputingPath().getPathIterator(at);
    }

    /**
     * Returns an iterator object that iterates along the boundary of
     * the <code>Polygon2D</code> and provides access to the geometry of the
     * outline of the <code>Shape</code>. Only SEG_MOVETO, SEG_LINETO, and
     * SEG_CLOSE point types are returned by the iterator.
     * Since polygons are already flat, the <code>flatness</code> parameter
     * is ignored.
     * 
     * @param at
     *        an optional <code>AffineTransform</code> to be applied to the
     *        coordinates as they are returned in the iteration, or <code>null</code> if untransformed coordinates are
     *        desired
     * @param flatness
     *        the maximum amount that the control points
     *        for a given curve can vary from colinear before a subdivided
     *        curve is replaced by a straight line connecting the
     *        endpoints. Since polygons are already flat the <code>flatness</code> parameter is ignored.
     * @return a <code>PathIterator</code> object that provides access to the <code>Shape</code> object's geometry.
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return getPathIterator(at);
    }
}
