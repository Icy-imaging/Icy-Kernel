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

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Class to define a 2D boolean mask and make basic boolean operation between masks.
 * The bounds property of this object define the area of the mask.
 * The mask contains the boolean mask itself.
 * 
 * @author Stephane
 */
public class BooleanMask2D
{
    /**
     * Build global boolean mask from union of the specified list of ROI2D
     */
    public static BooleanMask2D getUnionBooleanMask(ArrayList<ROI2D> rois)
    {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getAsBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.union(bounds, mask);
        }

        return result;
    }

    public static BooleanMask2D getUnionBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getUnionBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

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
    public static BooleanMask2D getIntersectBooleanMask(ArrayList<ROI2D> rois)
    {
        BooleanMask2D result = null;

        // compute global intersect boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getAsBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.intersect(bounds, mask);
        }

        return result;
    }

    public static BooleanMask2D getIntersectBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getIntersectBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

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
     * Build global boolean mask from union of the specified list of ROI2D
     */
    public static BooleanMask2D getXorBooleanMask(ArrayList<ROI2D> rois)
    {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (ROI2D roi : rois)
        {
            // get roi bounds
            final Rectangle bounds = roi.getBounds();
            // get the boolean mask of roi (optimized from intersection bounds)
            final boolean[] mask = roi.getAsBooleanMask(bounds);

            // update global mask
            if (result == null)
                result = new BooleanMask2D(bounds, mask);
            else
                result.xor(bounds, mask);
        }

        return result;
    }

    public static BooleanMask2D getXorBooleanMask(BooleanMask2D mask1, BooleanMask2D mask2)
    {
        return getXorBooleanMask(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    public static BooleanMask2D getXorBooleanMask(Rectangle bounds1, boolean[] mask1, Rectangle bounds2, boolean[] mask2)
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

    public Rectangle bounds;
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
    public BooleanMask2D getXor(BooleanMask2D booleanMask)
    {
        return getXorBooleanMask(this, booleanMask);
    }

    /**
     * Compute exclusive or operation with specified mask and return result in a new mask
     */
    public BooleanMask2D getXor(Rectangle bounds, boolean[] mask)
    {
        return getXorBooleanMask(this.bounds, this.mask, bounds, mask);
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
    public void xor(BooleanMask2D booleanMask)
    {
        xor(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Compute exclusive or operation with specified mask
     */
    public void xor(Rectangle bounds, boolean[] mask)
    {
        final BooleanMask2D result = getXor(bounds, mask);

        this.bounds = result.bounds;
        this.mask = result.mask;
    }

    /**
     * Optimize mask bounds (set to the minimal size) from content
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
            moveBounds(new Rectangle(0, 0, 0, 0));
        else
            // new calculated bounds
            moveBounds(new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1));
    }

    /**
     * Modify bounds of BooleanMask, keep the mask data
     */
    public void moveBounds(Rectangle value)
    {
        // dimension changed ?
        if ((bounds.width != value.width) || (bounds.height != value.height))
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
