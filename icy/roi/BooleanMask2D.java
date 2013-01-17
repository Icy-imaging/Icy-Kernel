/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.roi;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class to define a 2D boolean mask and make basic boolean operation between masks.
 * The bounds property of this object define the area of the mask.
 * The mask contains the boolean mask itself.
 * 
 * @author Stephane
 */
public class BooleanMask2D
{
    private static class Component
    {
        public List<Point> pixels;
        public List<Component> children;
        private Component root;

        public Component()
        {
            pixels = new ArrayList<Point>(256);
            children = new ArrayList<Component>();
            root = this;
        }

        public void add(Point p)
        {
            pixels.add(p);
        }

        public void add(Component c)
        {
            final Component croot = c.root;

            if (croot != root)
            {
                children.add(croot);
                croot.setRoot(root);
            }
        }

        public boolean isRoot()
        {
            return root == this;
        }

        private void setRoot(Component value)
        {
            root = value;

            final int size = children.size();
            for (int c = 0; c < size; c++)
                children.get(c).setRoot(value);
        }

        public int getTotalSize()
        {
            int result = pixels.size();

            final int size = children.size();
            for (int c = 0; c < size; c++)
                result += children.get(c).getTotalSize();

            return result;
        }

        public Point[] getAllPoints(Comparator<Point> comparator)
        {
            final Point[] result = new Point[getTotalSize()];

            // get all point
            getAllPoints(result, 0);

            // sort points
            if (comparator != null)
                Arrays.sort(result, comparator);

            return result;
        }

        private int getAllPoints(Point[] result, int offset)
        {
            int size = pixels.size();
            int off = offset;

            for (int ind = 0; ind < size; ind++)
                result[off++] = pixels.get(ind);

            final int csize = children.size();
            for (int c = 0; c < csize; c++)
                off = children.get(c).getAllPoints(result, off);

            return off;
        }
    }

    /**
     * Build global boolean mask from union of the specified list of ROI2D
     */
    public static BooleanMask2D getUnionBooleanMask(ROI2D[] rois)
    {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.union(bounds, mask);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            return new BooleanMask2D();

        return result;
    }

    /**
     * Build global boolean mask from union of the specified list of ROI2D
     */
    public static BooleanMask2D getUnionBooleanMask(ArrayList<ROI2D> rois)
    {
        return getUnionBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * Build resulting mask from union of the mask1 and mask2.
     */
    public static BooleanMask2D getUnionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getUnionBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from union of the mask1 and mask2.
     */
    public static BooleanMask2D getUnionBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        final Rectangle union = bounds1.union(bounds2);

        if (!union.isEmpty())
        {
            final boolean[] mask = new boolean[union.width * union.height];
            int offDst, offSrc;

            // calculate offset
            offDst = ((bounds1.y - union.y) * union.width) + (bounds1.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds1.height; y++)
            {
                for (int x = 0; x < bounds1.width; x++)
                    mask[offDst + x] |= mask1[offSrc++];

                offDst += union.width;
            }

            // calculate offset
            offDst = ((bounds2.y - union.y) * union.width) + (bounds2.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds2.height; y++)
            {
                for (int x = 0; x < bounds2.width; x++)
                    mask[offDst + x] |= mask2[offSrc++];

                offDst += union.width;
            }

            return new BooleanMask2D(union, mask);
        }

        return new BooleanMask2D();
    }

    /**
     * Build global boolean mask from intersection of the specified list of ROI2D
     */
    public static BooleanMask2D getIntersectBooleanMask(ROI2D[] rois)
    {
        BooleanMask2D result = null;

        // compute global intersect boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.intersect(bounds, mask);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            return new BooleanMask2D();

        return result;
    }

    /**
     * Build global boolean mask from intersection of the specified list of ROI2D
     */
    public static BooleanMask2D getIntersectBooleanMask(ArrayList<ROI2D> rois)
    {
        return getIntersectBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2.
     */
    public static BooleanMask2D getIntersectBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getIntersectBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2.
     */
    public static BooleanMask2D getIntersectBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        final Rectangle intersect = bounds1.intersection(bounds2);

        if (!intersect.isEmpty())
        {
            final boolean[] mask = new boolean[intersect.width * intersect.height];

            // calculate offsets
            int off1 = ((intersect.y - bounds1.y) * bounds1.width) + (intersect.x - bounds1.x);
            int off2 = ((intersect.y - bounds2.y) * bounds2.width) + (intersect.x - bounds2.x);
            int off = 0;

            for (int y = 0; y < intersect.height; y++)
            {
                for (int x = 0; x < intersect.width; x++)
                    mask[off++] = mask1[off1 + x] & mask2[off2 + x];

                off1 += bounds1.width;
                off2 += bounds2.width;
            }

            return new BooleanMask2D(intersect, mask);
        }

        return new BooleanMask2D();
    }

    /**
     * Build global boolean mask from exclusive union of the specified list of ROI2D
     */
    public static BooleanMask2D getExclusiveUnionBooleanMask(ROI2D[] rois)
    {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.exclusiveUnion(bounds, mask);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            return new BooleanMask2D();

        return result;
    }

    /**
     * Build global boolean mask from exclusive union of the specified list of ROI2D
     */
    public static BooleanMask2D getExclusiveUnionBooleanMask(ArrayList<ROI2D> rois)
    {
        return getExclusiveUnionBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2.
     */
    public static BooleanMask2D getExclusiveUnionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getExclusiveUnionBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2.
     */
    public static BooleanMask2D getExclusiveUnionBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        final Rectangle union = bounds1.union(bounds2);

        if (!union.isEmpty())
        {
            final boolean[] mask = new boolean[union.width * union.height];
            int offDst, offSrc;

            // calculate offset
            offDst = ((bounds1.y - union.y) * union.width) + (bounds1.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds1.height; y++)
            {
                for (int x = 0; x < bounds1.width; x++)
                    mask[offDst + x] ^= mask1[offSrc++];

                offDst += union.width;
            }

            // calculate offset
            offDst = ((bounds2.y - union.y) * union.width) + (bounds2.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds2.height; y++)
            {
                for (int x = 0; x < bounds2.width; x++)
                    mask[offDst + x] ^= mask2[offSrc++];

                offDst += union.width;
            }

            final BooleanMask2D result = new BooleanMask2D(union, mask);

            // optimize bounds
            result.optimizeBounds();

            return result;
        }

        return new BooleanMask2D();
    }

    /**
     * @deprecated Uses {@link #getExclusiveUnionBooleanMask(ROI2D[])} instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(ROI2D[] rois)
    {
        return getExclusiveUnionBooleanMask(rois);
    }

    /**
     * @deprecated Uses {@link #getExclusiveUnionBooleanMask(ArrayList)} instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(ArrayList<ROI2D> rois)
    {
        return getExclusiveUnionBooleanMask(rois);
    }

    /**
     * @deprecated Uses {@link #getExclusiveUnionBooleanMask(BooleanMask2D, BooleanMask2D)} instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getExclusiveUnionBooleanMask(mask1, mask2);
    }

    /**
     * @deprecated Uses
     *             {@link #getExclusiveUnionBooleanMask(Rectangle, boolean[], Rectangle, boolean[])}
     *             instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
    {
        return getExclusiveUnionBooleanMask(bounds1, mask1, bounds2, mask2);
    }

    /**
     * Build resulting mask from the subtraction of mask2 from mask1.
     */
    public static BooleanMask2D getSubtractionMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getSubtractionMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from the subtraction of mask2 from mask1.
     */
    public static BooleanMask2D getSubtractionMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        final boolean[] mask = mask1.clone();
        final Rectangle subtract = new Rectangle(bounds1);
        final BooleanMask2D result = new BooleanMask2D(subtract, mask);

        // compute intersection
        final Rectangle intersection = bounds1.intersection(bounds2);

        // need to subtract something ?
        if (!intersection.isEmpty())
        {
            // calculate offset
            int offDst = ((intersection.y - subtract.y) * subtract.width) + (intersection.x - subtract.x);
            int offSrc = ((intersection.y - bounds2.y) * bounds2.width) + (intersection.x - bounds2.x);

            for (int y = 0; y < intersection.height; y++)
            {
                for (int x = 0; x < intersection.width; x++)
                    mask[offDst + x] ^= mask2[offSrc + x];

                offDst += subtract.width;
                offSrc += bounds2.width;
            }

            // optimize bounds
            result.optimizeBounds();
        }

        return result;
    }

    /**
     * Return edge points of specified component.<br>
     * A component is basically an isolated object which does not touch any other objects.<br>
     * 
     * @param sortedComponentPoints
     *        Component points sorted in ascending XY order.<br>
     * @return
     *         Edge points of specified component sorted in ascending XY order :
     * 
     *         <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     */
    public static Point[] getComponentEdge(Point[] sortedComponentPoints)
    {
        final Point[] edgePoints = new Point[sortedComponentPoints.length];

        Point last = new Point(sortedComponentPoints[0]);
        int startX = last.x;

        edgePoints[0] = new Point(last);

        int edgePt = 1;
        for (int i = 1; i < sortedComponentPoints.length;)
        {
            final Point pt = sortedComponentPoints[i];
            final int x = pt.x;
            final int y = pt.y;

            // new line
            if (y != last.y)
            {
                // end line point != start line point --> add it
                if (last.x != startX)
                    edgePoints[edgePt++] = new Point(last);

                edgePoints[edgePt++] = new Point(x, y);

                startX = x;
                last.y = y;
            }

            last.x = x;
        }

        final Point[] result = new Point[edgePt];
        System.arraycopy(edgePoints, 0, result, 0, result.length);

        return result;
    }

    /**
     * Region represented by the mask.
     */
    public Rectangle bounds;
    /**
     * Boolean mask array.
     */
    public boolean[] mask;

    /**
     * @param bounds
     * @param mask
     */
    public BooleanMask2D(Rectangle bounds, boolean[] mask)
    {
        super();

        this.bounds = bounds;
        this.mask = mask;
    }

    /**
     * Build a new boolean mask from the specified array of {@link Point}.<br>
     */
    public BooleanMask2D(Point[] points)
    {
        super();

        if ((points == null) || (points.length == 0))
        {
            bounds = new Rectangle();
            mask = new boolean[0];
        }
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (Point pt : points)
            {
                final int x = pt.x;
                final int y = pt.y;

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
            }

            bounds = new Rectangle(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
            mask = new boolean[bounds.width * bounds.height];

            for (Point pt : points)
                mask[((pt.y - minY) * bounds.width) + (pt.x - minX)] = true;
        }
    }

    public BooleanMask2D()
    {
        this(new Rectangle(), new boolean[0]);
    }

    /**
     * Return true if boolean mask is empty<br>
     */
    public boolean isEmpty()
    {
        return bounds.isEmpty();
    }

    /**
     * Return true if mask contains the specified point
     */
    public boolean contains(int x, int y)
    {
        if (bounds.contains(x, y))
            return mask[(x - bounds.x) + ((y - bounds.y) * bounds.width)];

        return false;
    }

    /**
     * Return an array of {@link Point} representing all points of the current mask.<br>
     * Points are returned in ascending XY order :
     * 
     * <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     */
    public Point[] getPoints()
    {
        if (bounds.isEmpty())
            return new Point[0];

        Point[] points = new Point[mask.length];
        final int maxx = bounds.x + bounds.width;
        final int maxy = bounds.y + bounds.height;

        int pt = 0;
        int off = 0;
        for (int y = bounds.y; y < maxy; y++)
        {
            for (int x = bounds.x; x < maxx; x++)
            {
                // we have a component pixel
                if (mask[off++])
                    points[pt++] = new Point(x, y);
            }
        }

        final Point[] result = new Point[pt];

        System.arraycopy(points, 0, result, 0, pt);

        return result;
    }

    /**
     * Return a 2D array of integer representing points of each component of the current mask.<br>
     * A component is basically an isolated object which does not touch any other objects.<br>
     * Internal use only.
     */
    protected List<Component> getComponentsPointsInternal()
    {
        final List<Component> components = new ArrayList<Component>();
        final int w = bounds.width;
        final int minx = bounds.x;
        final int miny = bounds.y;

        // cache
        final Component[] line0 = new Component[w + 2];
        final Component[] line1 = new Component[w + 2];

        Arrays.fill(line0, null);
        Arrays.fill(line1, null);

        Component[] prevLine = line0;
        Component[] currLine = line1;

        Component left;
        Component top;
        Component topleft;

        int offset = 0;
        for (int y = 0; y < bounds.height; y++)
        {
            // prepare previous cache
            topleft = null;
            left = null;

            for (int x = 0; x < w; x++)
            {
                top = prevLine[x + 1];

                // we have a component pixel
                if (mask[offset++])
                {
                    if (topleft != null)
                    {
                        // mix component
                        if ((left != null) && (left != topleft))
                            topleft.add(left);

                        left = topleft;
                    }
                    else if (top != null)
                    {
                        // mix component
                        if ((left != null) && (left != top))
                            top.add(left);

                        left = top;
                    }
                    else if (left == null)
                    {
                        // new component
                        left = new Component();
                        components.add(left);
                    }

                    // add pixel to component
                    left.add(new Point(x + minx, y + miny));
                }
                else
                {
                    // mix component
                    if ((left != null) && (top != null) && (left != top))
                        top.add(left);

                    left = null;
                }

                topleft = top;
                // set current component index line cache
                currLine[x + 1] = left;
            }

            Component[] pl = prevLine;
            prevLine = currLine;
            currLine = pl;
        }

        final ArrayList<Component> result = new ArrayList<Component>();

        // remove partial components
        for (Component component : components)
            if (component.isRoot())
                result.add(component);

        return result;
    }

    /**
     * Compute and return a 2D array of {@link Point} representing points of each component of the
     * current mask.<br>
     * A component is basically an isolated object which do not touch any other objects.<br>
     * <br>
     * The array is returned in the following format :<br>
     * <code>result.lenght</code> = number of component.<br>
     * <code>result[c].length</code> = number of point of component c.<br>
     * <code>result[c][n]</code> = Point n of component c.<br>
     * 
     * @param sorted
     *        When true points are returned in ascending XY order :
     * 
     *        <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     */
    public Point[][] getComponentsPoints(boolean sorted)
    {
        if (bounds.isEmpty())
            return new Point[0][0];

        final Comparator<Point> pointComparator;

        if (sorted)
        {
            pointComparator = new Comparator<Point>()
            {
                @Override
                public int compare(Point p1, Point p2)
                {
                    if (p1.y < p2.y)
                        return -1;
                    if (p1.y > p2.y)
                        return 1;

                    return 0;
                }
            };
        }
        else
            pointComparator = null;

        final List<Component> components = getComponentsPointsInternal();
        final Point[][] result = new Point[components.size()][];

        // convert list of point to Point array
        for (int i = 0; i < result.length; i++)
            result[i] = components.get(i).getAllPoints(pointComparator);

        return result;
    }

    /**
     * Return an array of boolean mask representing each independent component of the current
     * mask.<br>
     * A component is basically an isolated object which does not touch any other objects.
     */
    public BooleanMask2D[] getComponents()
    {
        if (bounds.isEmpty())
            return new BooleanMask2D[0];

        final Point[][] componentsPoints = getComponentsPoints(false);
        final List<BooleanMask2D> result = new ArrayList<BooleanMask2D>();

        // convert array of point to boolean mask
        for (Point[] componentPoints : componentsPoints)
            result.add(new BooleanMask2D(componentPoints));

        return result.toArray(new BooleanMask2D[result.size()]);
    }

    /**
     * Return an array of {@link Point} containing the edge points of the mask.<br>
     * The points are returned from Y min to Y max and X min to X max :
     * 
     * <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     */
    public Point[] getEdgePoints()
    {
        if (isEmpty())
            return new Point[0];

        final List<Point> points = new ArrayList<Point>(1024);
        final int h = bounds.height;
        final int w = bounds.width;
        final int maxx = bounds.x + (w - 1);
        final int maxy = bounds.y + (h - 1);

        // cache
        boolean top = false;
        boolean bottom = false;
        boolean left = false;
        boolean right = false;
        boolean current;

        int offset = 0;

        // special case
        if ((w == 1) && (h == 1))
        {
            if (mask[0])
                points.add(new Point(bounds.x, bounds.y));
        }
        else if (w == 1)
        {
            // first pixel of row
            top = false;
            current = mask[offset];
            bottom = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && bottom))
                points.add(new Point(bounds.x, bounds.y));

            // row
            for (int y = bounds.y + 1; y < maxy; y++)
            {
                // cache
                top = current;
                current = bottom;
                bottom = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && bottom))
                    points.add(new Point(bounds.x, y));
            }

            // cache
            top = current;
            current = bottom;
            bottom = false;

            // current pixel is a border ?
            if (current && !(top && bottom))
                points.add(new Point(bounds.x, maxy));
        }
        // special case
        else if (h == 1)
        {
            // first pixel of line
            left = false;
            current = mask[offset];
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(left && right))
                points.add(new Point(bounds.x, bounds.y));

            // line
            for (int x = bounds.x + 1; x < maxx; x++)
            {
                // cache
                left = current;
                current = right;
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(left && right))
                    points.add(new Point(x, bounds.y));
            }

            // last pixel of first line
            left = current;
            current = right;
            right = false;

            // current pixel is a border ?
            if (current && !(left && right))
                points.add(new Point(maxx, bounds.y));
        }
        else
        {
            // first pixel of first line
            top = false;
            left = false;
            current = mask[offset];
            bottom = mask[offset + w];
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(bounds.x, bounds.y));

            // first line
            for (int x = bounds.x + 1; x < maxx; x++)
            {
                // cache
                left = current;
                current = right;
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(x, bounds.y));
            }

            // last pixel of first line
            left = current;
            current = right;
            bottom = mask[offset + w];
            right = false;
            offset++;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(maxx, bounds.y));

            for (int y = bounds.y + 1; y < maxy; y++)
            {
                // first pixel of line
                left = false;
                current = mask[offset];
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(bounds.x, y));

                for (int x = bounds.x + 1; x < maxx; x++)
                {
                    // cache
                    left = current;
                    current = right;
                    top = mask[offset - w];
                    bottom = mask[offset + w];
                    right = mask[++offset];

                    // current pixel is a border ?
                    if (current && !(top && left && right && bottom))
                        points.add(new Point(x, y));
                }

                // last pixel of line
                left = current;
                current = right;
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = false;
                offset++;

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(maxx, y));
            }

            // first pixel of last line
            left = false;
            current = mask[offset];
            top = mask[offset - w];
            bottom = false;
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(bounds.x, maxy));

            // last line
            for (int x = bounds.x + 1; x < maxx; x++)
            {
                // cache
                left = current;
                current = right;
                top = mask[offset - w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(x, maxy));
            }

            // last pixel of last line
            left = current;
            current = right;
            top = mask[offset - w];
            right = false;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(maxx, maxy));
        }

        return points.toArray(new Point[points.size()]);
    }

    /**
     * Compute intersection with specified mask and return result in a new mask
     */
    public BooleanMask2D getIntersect(BooleanMask2D booleanMask)
    {
        return getIntersectBooleanMask(this, booleanMask);
    }

    /**
     * Compute intersection with specified mask and return result in a new mask
     */
    public BooleanMask2D getIntersect(Rectangle bounds, boolean[] mask)
    {
        return getIntersectBooleanMask(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Compute union with specified mask and return result in a new mask
     */
    public BooleanMask2D getUnion(BooleanMask2D booleanMask)
    {
        return getUnionBooleanMask(this, booleanMask);
    }

    /**
     * Compute union with specified mask and return result in a new mask
     */
    public BooleanMask2D getUnion(Rectangle bounds, boolean[] mask)
    {
        return getUnionBooleanMask(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Compute exclusive or operation with specified mask and return result in a new mask
     */
    public BooleanMask2D getExclusiveUnion(BooleanMask2D booleanMask)
    {
        return getXorBooleanMask(this, booleanMask);
    }

    /**
     * Compute exclusive or operation with specified mask and return result in a new mask
     */
    public BooleanMask2D getExclusiveUnion(Rectangle bounds, boolean[] mask)
    {
        return getXorBooleanMask(this.bounds, this.mask, bounds, mask);
    }

    /**
     * @deprecated Uses {@link #getExclusiveUnion(BooleanMask2D)} instead.
     */
    @Deprecated
    public BooleanMask2D getXor(BooleanMask2D booleanMask)
    {
        return getExclusiveUnion(booleanMask);
    }

    /**
     * @deprecated Uses {@link #getExclusiveUnion(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public BooleanMask2D getXor(Rectangle bounds, boolean[] mask)
    {
        return getExclusiveUnion(bounds, mask);
    }

    /**
     * Compute intersection with specified mask
     */
    public void intersect(BooleanMask2D booleanMask)
    {
        final BooleanMask2D result = getIntersect(booleanMask);

        bounds = result.bounds;
        mask = result.mask;
    }

    /**
     * Compute intersection with specified mask
     */
    public void intersect(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getIntersect(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * Compute union with specified mask
     */
    public void union(BooleanMask2D booleanMask)
    {
        union(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Compute union with specified mask
     */
    public void union(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getUnion(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * Compute exclusive or operation with specified mask
     */
    public void exclusiveUnion(BooleanMask2D booleanMask)
    {
        exclusiveUnion(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Compute exclusive or operation with specified mask
     */
    public void exclusiveUnion(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getExclusiveUnion(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * @deprecated Uses {@link #exclusiveUnion(BooleanMask2D)} instead
     */
    @Deprecated
    public void xor(BooleanMask2D booleanMask)
    {
        exclusiveUnion(booleanMask);
    }

    /**
     * @deprecated Uses {@link #exclusiveUnion(Rectangle, boolean[])} instead
     */
    @Deprecated
    public void xor(Rectangle bounds, boolean[] mask)
    {
        exclusiveUnion(bounds, mask);
    }

    /**
     * Optimize mask bounds so it fit mask content.
     */
    public void optimizeBounds()
    {
        // find best bounds
        final int sizeX = bounds.width;
        final int sizeY = bounds.height;

        int minX = sizeX;
        int minY = sizeY;
        int maxX = -1;
        int maxY = -1;
        int offset = 0;
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (mask[offset++])
                {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }

        // test if empty
        if (minX == sizeX)
            // empty bounds
            setBounds(new Rectangle(bounds.x, bounds.y, 0, 0));
        else
            // new calculated bounds
            setBounds(new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1));
    }

    /**
     * Modify bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds. 
     */
    public void setBounds(Rectangle value)
    {
        // bounds changed ?
        if (!bounds.equals(value))
        {
            // copy bounds as we modify them
            final Rectangle oldBounds = new Rectangle(bounds);
            final Rectangle newBounds = new Rectangle(value);

            // replace to origin (relative to old bounds)
            oldBounds.translate(-bounds.x, -bounds.y);
            newBounds.translate(-bounds.x, -bounds.y);

            final boolean[] newMask = new boolean[newBounds.width * newBounds.height];
            final Rectangle intersect = newBounds.intersection(oldBounds);

            if (!intersect.isEmpty())
            {
                int offSrc = 0;
                int offDst = 0;

                // adjust offset in source mask
                if (intersect.x > 0)
                    offSrc += intersect.x;
                if (intersect.y > 0)
                    offSrc += intersect.y * oldBounds.width;
                // adjust offset in destination mask
                if (newBounds.x < 0)
                    offDst += -newBounds.x;
                if (newBounds.y < 0)
                    offDst += -newBounds.y * newBounds.width;

                // preserve data
                for (int j = 0; j < intersect.height; j++)
                {
                    System.arraycopy(mask, offSrc, newMask, offDst, intersect.width);

                    offSrc += oldBounds.width;
                    offDst += newBounds.width;
                }
            }

            // set new image and maskData
            mask = newMask;
            // set new bounds
            bounds = value;
        }
    }
}
