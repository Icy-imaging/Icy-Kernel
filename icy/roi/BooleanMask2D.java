/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.roi;

import icy.type.TypeUtil;
import icy.type.collection.array.DynamicArray;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class to define a 2D boolean mask and make basic boolean operation between masks.<br>
 * The bounds property of this object define the area of the mask where the mask contains the
 * boolean mask itself.
 * 
 * @author Stephane
 */
public class BooleanMask2D implements Cloneable
{
    private static class Component
    {
        public DynamicArray.Int points;
        public List<Component> children;
        private Component root;

        public Component()
        {
            points = new DynamicArray.Int(0);
            children = new ArrayList<Component>();
            root = this;
        }

        public void addPoint(int x, int y)
        {
            points.addSingle(x);
            points.addSingle(y);
        }

        public void addComponent(Component c)
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
            int result = points.getSize();

            final int size = children.size();
            for (int c = 0; c < size; c++)
                result += children.get(c).getTotalSize();

            return result;
        }

        public int[] getAllPoints()
        {
            final int[] result = new int[getTotalSize()];

            // get all point
            getAllPoints(result, 0);

            return result;
        }

        private int getAllPoints(int[] result, int offset)
        {
            final int[] intPoints = points.asArray();

            System.arraycopy(intPoints, 0, result, offset, intPoints.length);

            int off = offset + intPoints.length;
            final int csize = children.size();
            for (int c = 0; c < csize; c++)
                off = children.get(c).getAllPoints(result, off);

            return off;
        }
    }

    /**
     * Build global boolean mask from union of all specified mask
     */
    public static BooleanMask2D getUnion(List<BooleanMask2D> masks)
    {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (BooleanMask2D bm : masks)
        {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getUnion(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from union of the mask1 and mask2.<br>
     * If <code>mask1</code> is <code>null</code> then a copy of <code>mask2</code> is returned.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * <code>null</code> is returned if both <code>mask1</code> and <code>mask2</code> are
     * <code>null</code>.
     */
    public static BooleanMask2D getUnion(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        if (mask1 == null)
            return (BooleanMask2D) mask2.clone();
        if (mask2 == null)
            return (BooleanMask2D) mask1.clone();

        return getUnion(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from union of the mask1 and mask2:
     * 
     * <pre>
     *        mask1          +       mask2        =      result
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public static BooleanMask2D getUnion(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
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
     * Build global boolean mask from intersection of all specified mask
     */
    public static BooleanMask2D getIntersection(List<BooleanMask2D> masks)
    {
        BooleanMask2D result = null;

        // compute global intersect boolean mask of all ROI2D
        for (BooleanMask2D bm : masks)
        {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getIntersection(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2.<br>
     * <code>null</code> is returned if <code>mask1</code> or <code>mask2</code> is
     * <code>null</code>.
     */
    public static BooleanMask2D getIntersection(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        if (mask1 == null)
            return null;
        if (mask2 == null)
            return null;

        return getIntersection(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2:
     * 
     * <pre>
     *        mask1     intersect     mask2      =        result
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     */
    public static BooleanMask2D getIntersection(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
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
     * Build global boolean mask from exclusive union of all specified mask
     */
    public static BooleanMask2D getExclusiveUnion(List<BooleanMask2D> masks)
    {
        BooleanMask2D result = null;

        // compute global exclusive union boolean mask of all ROI2D
        for (BooleanMask2D bm : masks)
        {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getExclusiveUnion(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2.<br>
     * If <code>mask1</code> is <code>null</code> then a copy of <code>mask2</code> is returned.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * <code>null</code> is returned if both <code>mask1</code> and <code>mask2</code> are
     * <code>null</code>.
     */
    public static BooleanMask2D getExclusiveUnion(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        if (mask1 == null)
            return (BooleanMask2D) mask2.clone();
        if (mask2 == null)
            return (BooleanMask2D) mask1.clone();

        return getExclusiveUnion(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2:
     * 
     * <pre>
     *          mask1       xor      mask2        =       result
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public static BooleanMask2D getExclusiveUnion(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
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
     * Build resulting mask from the subtraction of mask2 from mask1.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * If <code>mask1</code> is <code>null</code> then <code>null</code> is returned.
     */
    public static BooleanMask2D getSubtraction(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        if (mask1 == null)
            return null;
        if (mask2 == null)
            return (BooleanMask2D) mask1.clone();

        return getSubtraction(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from the subtraction of mask2 from mask1:
     * 
     * <pre>
     *        mask1          -        mask2       =  result
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     */
    public static BooleanMask2D getSubtraction(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
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
     * @deprecated Use {@link #getUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getUnionBooleanMask(List<BooleanMask2D> masks)
    {
        return getUnion(masks);
    }

    /**
     * @deprecated Use {@link ROIUtil#getUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getUnionBooleanMask(ROI2D[] rois)
    {
        final List<BooleanMask2D> masks = new ArrayList<BooleanMask2D>();

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
            masks.add(roi.getBooleanMask(true));

        return getUnionBooleanMask(masks);
    }

    /**
     * @deprecated Use {@link ROIUtil#getUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getUnionBooleanMask(ArrayList<ROI2D> rois)
    {
        return getUnionBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * @deprecated Use {@link #getUnion(BooleanMask2D, BooleanMask2D)} instead.
     */
    @Deprecated
    public static BooleanMask2D getUnionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getUnion(mask1, mask2);

    }

    /**
     * @deprecated Use {@link #getUnion(Rectangle, boolean[], Rectangle, boolean[])} instead.
     */
    @Deprecated
    public static BooleanMask2D getUnionBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        return getUnion(bounds1, mask1, bounds2, mask2);

    }

    /**
     * @deprecated Use {@link #getIntersection(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectionBooleanMask(List<BooleanMask2D> masks)
    {
        return getIntersection(masks);
    }

    /**
     * @deprecated Use {@link #getIntersection(BooleanMask2D, BooleanMask2D)} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getIntersection(mask1, mask2);
    }

    /**
     * @deprecated Use {@link #getIntersection(Rectangle, boolean[], Rectangle, boolean[])} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectionBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        return getIntersection(bounds1, mask1, bounds2, mask2);
    }

    /**
     * @deprecated Use {@link ROIUtil#getIntersection(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectBooleanMask(ROI2D[] rois)
    {
        final List<BooleanMask2D> masks = new ArrayList<BooleanMask2D>();

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
            masks.add(roi.getBooleanMask(true));

        return getIntersectionBooleanMask(masks);
    }

    /**
     * @deprecated Use {@link ROIUtil#getIntersection(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectBooleanMask(ArrayList<ROI2D> rois)
    {
        return getIntersectBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * @deprecated Use {@link #getIntersectionBooleanMask(BooleanMask2D, BooleanMask2D)} instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getIntersectionBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * @deprecated Use
     *             {@link #getIntersectionBooleanMask(Rectangle, boolean[], Rectangle, boolean[])}
     *             instead.
     */
    @Deprecated
    public static BooleanMask2D getIntersectBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        return getIntersectionBooleanMask(bounds1, mask1, bounds2, mask2);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getExclusiveUnionBooleanMask(List<BooleanMask2D> masks)
    {
        return getExclusiveUnion(masks);
    }

    /**
     * @deprecated Use {@link ROIUtil#getExclusiveUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getExclusiveUnionBooleanMask(ROI2D[] rois)
    {
        final List<BooleanMask2D> masks = new ArrayList<BooleanMask2D>();

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
            masks.add(roi.getBooleanMask(true));

        return getExclusiveUnion(masks);
    }

    /**
     * @deprecated Use {@link ROIUtil#getExclusiveUnion(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getExclusiveUnionBooleanMask(ArrayList<ROI2D> rois)
    {
        return getExclusiveUnionBooleanMask(rois.toArray(new ROI2D[rois.size()]));
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(BooleanMask2D, BooleanMask2D)} instead.
     */
    @Deprecated
    public static BooleanMask2D getExclusiveUnionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getExclusiveUnion(mask1, mask2);

    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(Rectangle, boolean[], Rectangle, boolean[])}
     *             instead.
     */
    @Deprecated
    public static BooleanMask2D getExclusiveUnionBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2,
            boolean[] mask2)
    {
        return getExclusiveUnion(bounds1, mask1, bounds2, mask2);

    }

    /**
     * @deprecated Use {@link #getExclusiveUnionBooleanMask(ROI2D[])} instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(ROI2D[] rois)
    {
        return getExclusiveUnionBooleanMask(rois);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnionBooleanMask(List)} instead.
     */
    @Deprecated
    public static BooleanMask2D getXorBooleanMask(ArrayList<ROI2D> rois)
    {
        return getExclusiveUnionBooleanMask(rois);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnionBooleanMask(BooleanMask2D, BooleanMask2D)} instead.
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
     * Return edge points of specified component.<br>
     * A component is basically an isolated object which does not touch any other objects.<br>
     * 
     * @param sortedComponentPoints
     *        Component points sorted in ascending XY order.<br>
     * @return
     *         Edge points of specified component sorted in ascending XY order :
     * 
     * <pre>
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

    /**
     * Build a new boolean mask from the specified array of integer representing points.<br>
     * <br>
     * The array should contains point coordinates defined as follow:<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     */
    public BooleanMask2D(int[] points)
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

            for (int i = 0; i < points.length; i += 2)
            {
                final int x = points[i + 0];
                final int y = points[i + 1];

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

            for (int i = 0; i < points.length; i += 2)
            {
                final int x = points[i + 0];
                final int y = points[i + 1];

                mask[((y - minY) * bounds.width) + (x - minX)] = true;
            }
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
     * Return true if mask contains the specified 2Dmask.
     */
    public boolean contains(BooleanMask2D booleanMask)
    {
        return contains(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Return true if mask contains the specified 2D mask.
     */
    public boolean contains(Rectangle rect, boolean[] bmask)
    {
        final Rectangle intersect = bounds.union(rect);

        // intersection should be equal to rect
        if (intersect.equals(rect))
        {
            // calculate offsets
            int off1 = ((intersect.y - bounds.y) * bounds.width) + (intersect.x - bounds.x);
            int off2 = ((intersect.y - rect.y) * rect.width) + (intersect.x - rect.x);

            for (int y = 0; y < intersect.height; y++)
            {
                for (int x = 0; x < intersect.width; x++)
                    if (bmask[off2 + x] && !mask[off1 + x])
                        return false;

                off1 += bounds.width;
                off2 += rect.width;
            }

            return true;
        }

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask.
     */
    public boolean intersects(BooleanMask2D booleanMask)
    {
        return intersects(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask region.
     */
    public boolean intersects(Rectangle rect, boolean[] bmask)
    {
        final Rectangle intersect = bounds.intersection(rect);

        if (!intersect.isEmpty())
        {
            // calculate offsets
            int off1 = ((intersect.y - bounds.y) * bounds.width) + (intersect.x - bounds.x);
            int off2 = ((intersect.y - rect.y) * rect.width) + (intersect.x - rect.x);

            for (int y = 0; y < intersect.height; y++)
            {
                for (int x = 0; x < intersect.width; x++)
                    if (mask[off1 + x] && bmask[off2 + x])
                        return true;

                off1 += bounds.width;
                off2 += rect.width;
            }
        }

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
     * 
     * @see #getPointsAsIntArray()
     */
    public Point[] getPoints()
    {
        return TypeUtil.toPoint(getPointsAsIntArray());
    }

    /**
     * Return an array of integer representing all points of the current mask.<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XY order :
     * 
     * <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     */
    public int[] getPointsAsIntArray()
    {
        if (bounds.isEmpty())
            return new int[0];

        int[] points = new int[mask.length * 2];
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
                {
                    points[pt++] = x;
                    points[pt++] = y;
                }
            }
        }

        final int[] result = new int[pt];

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
                            topleft.addComponent(left);

                        left = topleft;
                    }
                    else if (top != null)
                    {
                        // mix component
                        if ((left != null) && (left != top))
                            top.addComponent(left);

                        left = top;
                    }
                    else if (left == null)
                    {
                        // new component
                        left = new Component();
                        components.add(left);
                    }

                    // add pixel to component
                    left.addPoint(x + minx, y + miny);
                }
                else
                {
                    // mix component
                    if ((left != null) && (top != null) && (left != top))
                        top.addComponent(left);

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

        final List<Component> result = new ArrayList<Component>();

        // remove partial components
        for (Component component : components)
            if (component.isRoot())
                result.add(component);

        return result;
    }

    /**
     * Compute and return a 2D array of {@link Point} representing points of each component of the
     * current mask.<br>
     * A component is basically an isolated object which do not touch any other object.<br>
     * <br>
     * The array is returned in the following format :<br>
     * <code>result.lenght</code> = number of component.<br>
     * <code>result[c].length</code> = number of point of component c.<br>
     * <code>result[c][n]</code> = Point n of component c.<br>
     * 
     * @param sorted
     *        When true points are returned in ascending XY order :
     * 
     * <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     * @see #getComponentsPointsAsIntArray()
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

        final int[][] cPoints = getComponentsPointsAsIntArray();
        final Point[][] cResult = new Point[cPoints.length][];

        for (int i = 0; i < cPoints.length; i++)
        {
            final Point[] result = TypeUtil.toPoint(cPoints[i]);

            // sort points
            if (pointComparator != null)
                Arrays.sort(result, pointComparator);

            cResult[i] = result;
        }

        return cResult;
    }

    /**
     * Compute and return a 2D array of integer representing points of each component of the
     * current mask.<br>
     * A component is basically an isolated object which do not touch any other object.<br>
     * <br>
     * The array is returned in the following format :<br>
     * <code>result.lenght</code> = number of component.<br>
     * <code>result[c].length</code> = number of point * 2 for component c.<br>
     * <code>result[c][(pt * 2) + 0]</code> = X coordinate for point <i>pt</i> of component
     * <i>c</i>.<br>
     * <code>result[c][(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i> of component
     * <i>c</i>.<br>
     * 
     * @see #getComponentsPoints(boolean)
     */
    public int[][] getComponentsPointsAsIntArray()
    {
        if (bounds.isEmpty())
            return new int[0][0];

        final List<Component> components = getComponentsPointsInternal();
        final int[][] result = new int[components.size()][];

        // convert list of point to Point array
        for (int i = 0; i < result.length; i++)
            result[i] = components.get(i).getAllPoints();

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

        final int[][] componentsPoints = getComponentsPointsAsIntArray();
        final List<BooleanMask2D> result = new ArrayList<BooleanMask2D>();

        // convert array of point to boolean mask
        for (int[] componentPoints : componentsPoints)
            result.add(new BooleanMask2D(componentPoints));

        return result.toArray(new BooleanMask2D[result.size()]);
    }

    /**
     * Return an array of {@link Point} containing the edge points of the mask.<br>
     * Points are returned in ascending XY order:
     * 
     * <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     * 
     * @see #getEdgePointsAsIntArray()
     */
    public Point[] getEdgePoints()
    {
        return TypeUtil.toPoint(getEdgePointsAsIntArray());
    }

    /**
     * Return an array of integer containing the edge points of the mask.<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XY order:
     * 
     * <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     */
    public int[] getEdgePointsAsIntArray()
    {
        if (isEmpty())
            return new int[0];

        final int[] points = new int[mask.length * 2];
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
        int pt = 0;

        // special case
        if ((w == 1) && (h == 1))
        {
            if (mask[0])
            {
                points[pt++] = bounds.x;
                points[pt++] = bounds.y;
            }
        }
        else if (w == 1)
        {
            // first pixel of row
            top = false;
            current = mask[offset];
            bottom = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && bottom))
            {
                points[pt++] = bounds.x;
                points[pt++] = bounds.y;
            }

            // row
            for (int y = bounds.y + 1; y < maxy; y++)
            {
                // cache
                top = current;
                current = bottom;
                bottom = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && bottom))
                {
                    points[pt++] = bounds.x;
                    points[pt++] = y;
                }
            }

            // cache
            top = current;
            current = bottom;
            bottom = false;

            // current pixel is a border ?
            if (current && !(top && bottom))
            {
                points[pt++] = bounds.x;
                points[pt++] = maxy;
            }
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
            {
                points[pt++] = bounds.x;
                points[pt++] = bounds.y;
            }

            // line
            for (int x = bounds.x + 1; x < maxx; x++)
            {
                // cache
                left = current;
                current = right;
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(left && right))
                {
                    points[pt++] = x;
                    points[pt++] = bounds.y;
                }
            }

            // last pixel of first line
            left = current;
            current = right;
            right = false;

            // current pixel is a border ?
            if (current && !(left && right))
            {
                points[pt++] = maxx;
                points[pt++] = bounds.y;
            }
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
            {
                points[pt++] = bounds.x;
                points[pt++] = bounds.y;
            }

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
                {
                    points[pt++] = x;
                    points[pt++] = bounds.y;
                }
            }

            // last pixel of first line
            left = current;
            current = right;
            bottom = mask[offset + w];
            right = false;
            offset++;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
            {
                points[pt++] = maxx;
                points[pt++] = bounds.y;
            }

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
                {
                    points[pt++] = bounds.x;
                    points[pt++] = y;
                }

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
                    {
                        points[pt++] = x;
                        points[pt++] = y;
                    }
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
                {
                    points[pt++] = maxx;
                    points[pt++] = y;
                }
            }

            // first pixel of last line
            left = false;
            current = mask[offset];
            top = mask[offset - w];
            bottom = false;
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
            {
                points[pt++] = bounds.x;
                points[pt++] = maxy;
            }

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
                {
                    points[pt++] = x;
                    points[pt++] = maxy;
                }
            }

            // last pixel of last line
            left = current;
            current = right;
            top = mask[offset - w];
            right = false;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
            {
                points[pt++] = maxx;
                points[pt++] = maxy;
            }
        }

        final int[] result = new int[pt];

        System.arraycopy(points, 0, result, 0, pt);

        return result;
    }

    /**
     * Compute intersection with specified mask and return result in a new mask
     */
    public BooleanMask2D getIntersection(BooleanMask2D booleanMask)
    {
        return getIntersection(this, booleanMask);
    }

    /**
     * Compute intersection with specified mask and return result in a new mask
     */
    public BooleanMask2D getIntersection(Rectangle bounds, boolean[] mask)
    {
        return getIntersection(this.bounds, this.mask, bounds, mask);
    }

    /**
     * @deprecated Use {@link #getIntersection(BooleanMask2D)} instead.
     */
    @Deprecated
    public BooleanMask2D getIntersect(BooleanMask2D booleanMask)
    {
        return getIntersection(booleanMask);
    }

    /**
     * @deprecated Use {@link #getIntersection(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public BooleanMask2D getIntersect(Rectangle bounds, boolean[] mask)
    {
        return getIntersection(bounds, mask);
    }

    /**
     * Compute union with specified mask and return result in a new mask
     */
    public BooleanMask2D getUnion(BooleanMask2D booleanMask)
    {
        return getUnion(this, booleanMask);
    }

    /**
     * Compute union with specified mask and return result in a new mask
     */
    public BooleanMask2D getUnion(Rectangle bounds, boolean[] mask)
    {
        return getUnion(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Compute exclusive union operation with specified mask and return result in a new mask
     */
    public BooleanMask2D getExclusiveUnion(BooleanMask2D booleanMask)
    {
        return getExclusiveUnion(this, booleanMask);
    }

    /**
     * Compute exclusive union operation with specified mask and return result in a new mask
     */
    public BooleanMask2D getExclusiveUnion(Rectangle bounds, boolean[] mask)
    {
        return getExclusiveUnion(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Subtract the specified mask from current and return result in a new mask.
     */
    public BooleanMask2D getSubtraction(BooleanMask2D mask)
    {
        return getSubtraction(this, mask);
    }

    /**
     * Subtract the specified mask from current and return result in a new mask.
     */
    public BooleanMask2D getSubtraction(Rectangle bounds, boolean[] mask)
    {
        return getSubtraction(this.bounds, this.mask, bounds, mask);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(BooleanMask2D)} instead.
     */
    @Deprecated
    public BooleanMask2D getXor(BooleanMask2D booleanMask)
    {
        return getExclusiveUnion(booleanMask);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public BooleanMask2D getXor(Rectangle bounds, boolean[] mask)
    {
        return getExclusiveUnion(bounds, mask);
    }

    /**
     * @deprecated Use {@link #getIntersection(BooleanMask2D)} instead.
     */
    @Deprecated
    public void intersect(BooleanMask2D booleanMask)
    {
        final BooleanMask2D result = getIntersection(booleanMask);

        bounds = result.bounds;
        mask = result.mask;
    }

    /**
     * @deprecated Use {@link #getIntersection(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public void intersect(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getIntersection(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * @deprecated Use {@link #getUnion(BooleanMask2D)} instead.
     */
    @Deprecated
    public void union(BooleanMask2D booleanMask)
    {
        union(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * @deprecated Use {@link #getUnion(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public void union(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getUnion(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(BooleanMask2D)} instead.
     */
    @Deprecated
    public void exclusiveUnion(BooleanMask2D booleanMask)
    {
        exclusiveUnion(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * @deprecated Use {@link #getExclusiveUnion(Rectangle, boolean[])} instead.
     */
    @Deprecated
    public void exclusiveUnion(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getExclusiveUnion(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * @deprecated Use {@link #exclusiveUnion(BooleanMask2D)} instead
     */
    @Deprecated
    public void xor(BooleanMask2D booleanMask)
    {
        exclusiveUnion(booleanMask);
    }

    /**
     * @deprecated Use {@link #exclusiveUnion(Rectangle, boolean[])} instead
     */
    @Deprecated
    public void xor(Rectangle bounds, boolean[] mask)
    {
        exclusiveUnion(bounds, mask);
    }

    /**
     * Get the smallest bounds which fit mask content.
     */
    public Rectangle getOptimizedBounds()
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
            return new Rectangle(bounds.x, bounds.y, 0, 0);

        // new calculated bounds
        return new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    /**
     * Optimize mask bounds so it fit mask content.
     */
    public void optimizeBounds()
    {
        moveBounds(getOptimizedBounds());
    }

    /**
     * @deprecated Use {@link #moveBounds(Rectangle)} instead.
     */
    @Deprecated
    public void setBounds(Rectangle value)
    {
        moveBounds(value);
    }

    /**
     * Change the bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds.
     */
    public void moveBounds(Rectangle value)
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

    @Override
    public Object clone()
    {
        return new BooleanMask2D((Rectangle) bounds.clone(), mask.clone());
    }

}
