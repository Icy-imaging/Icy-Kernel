/**
 * 
 */
package icy.roi;

import icy.type.collection.array.DynamicArray;
import icy.type.point.Point4D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;

import java.awt.Rectangle;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class to define a 4D boolean mask and make basic boolean operation between masks.<br>
 * The bounds property of this object define the area of the mask where the mask contains the
 * boolean mask itself.
 * 
 * @author Stephane
 */
public class BooleanMask4D
{
    // Internal use only
    private static BooleanMask3D doUnion3D(BooleanMask3D m1, BooleanMask3D m2)
    {
        if (m1 == null)
        {
            // only use the 3D mask from second mask
            if (m2 != null)
                return (BooleanMask3D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 3D mask from first mask
            return (BooleanMask3D) m1.clone();

        // process union of 3D mask
        return BooleanMask3D.getUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask3D doIntersection3D(BooleanMask3D m1, BooleanMask3D m2)
    {
        if ((m1 == null) || (m2 == null))
            return null;

        // process intersection of 3D mask
        return BooleanMask3D.getIntersection(m1, m2);
    }

    // Internal use only
    private static BooleanMask3D doExclusiveUnion3D(BooleanMask3D m1, BooleanMask3D m2)
    {
        if (m1 == null)
        {
            // only use the 3D mask from second mask
            if (m2 != null)
                return (BooleanMask3D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 3D mask from first mask
            return (BooleanMask3D) m1.clone();

        // process exclusive union of 3D mask
        return BooleanMask3D.getExclusiveUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask3D doSubtraction3D(BooleanMask3D m1, BooleanMask3D m2)
    {
        if (m1 == null)
            return null;
        // only use the 3D mask from first mask
        if (m2 == null)
            return (BooleanMask3D) m1.clone();

        // process subtraction of 3D mask
        return BooleanMask3D.getSubtraction(m1, m2);
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
    public static BooleanMask4D getUnion(BooleanMask4D mask1, BooleanMask4D mask2)
    {
        final Rectangle4D.Integer bounds = (Rectangle4D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask3D[] mask;

            // special case of infinite T dimension
            if (bounds.sizeT == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite T dimension
                if ((mask1.bounds.sizeT != Integer.MAX_VALUE) || (mask2.bounds.sizeT != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite T dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask3D[1];

                final BooleanMask3D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask3D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doUnion3D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask3D[bounds.sizeT];

                for (int t = 0; t < bounds.sizeT; t++)
                {
                    final BooleanMask3D m2d1 = mask1.getMask3D(t + bounds.t);
                    final BooleanMask3D m2d2 = mask2.getMask3D(t + bounds.t);

                    mask[t] = doUnion3D(m2d1, m2d2);
                }
            }

            return new BooleanMask4D(bounds, mask);
        }

        return new BooleanMask4D();
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
    public static BooleanMask4D getIntersection(BooleanMask4D mask1, BooleanMask4D mask2)
    {
        final Rectangle4D.Integer bounds = (Rectangle4D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask3D[] mask;

            // special case of infinite T dimension
            if (bounds.sizeT == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite T dimension
                if ((mask1.bounds.sizeT != Integer.MAX_VALUE) || (mask2.bounds.sizeT != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite T dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask3D[1];

                final BooleanMask3D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask3D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doIntersection3D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask3D[bounds.sizeT];

                for (int t = 0; t < bounds.sizeT; t++)
                {
                    final BooleanMask3D m2d1 = mask1.getMask3D(t + bounds.t);
                    final BooleanMask3D m2d2 = mask2.getMask3D(t + bounds.t);

                    mask[t] = doIntersection3D(m2d1, m2d2);
                }
            }

            return new BooleanMask4D(bounds, mask);
        }

        return new BooleanMask4D();
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
    public static BooleanMask4D getExclusiveUnion(BooleanMask4D mask1, BooleanMask4D mask2)
    {
        final Rectangle4D.Integer bounds = (Rectangle4D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask3D[] mask;

            // special case of infinite T dimension
            if (bounds.sizeT == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite T dimension
                if ((mask1.bounds.sizeT != Integer.MAX_VALUE) || (mask2.bounds.sizeT != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite T dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask3D[1];

                final BooleanMask3D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask3D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doExclusiveUnion3D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask3D[bounds.sizeT];

                for (int t = 0; t < bounds.sizeT; t++)
                {
                    final BooleanMask3D m2d1 = mask1.getMask3D(t + bounds.t);
                    final BooleanMask3D m2d2 = mask2.getMask3D(t + bounds.t);

                    mask[t] = doExclusiveUnion3D(m2d1, m2d2);
                }
            }

            return new BooleanMask4D(bounds, mask);
        }

        return new BooleanMask4D();
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
    public static BooleanMask4D getSubtraction(BooleanMask4D mask1, BooleanMask4D mask2)
    {
        final Rectangle4D.Integer bounds = (Rectangle4D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        // need to subtract something ?
        if (!bounds.isEmpty())
        {
            final BooleanMask3D[] mask;

            // special case of infinite T dimension
            if (bounds.sizeT == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite T dimension
                if ((mask1.bounds.sizeT != Integer.MAX_VALUE) || (mask2.bounds.sizeT != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite T dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask3D[1];

                final BooleanMask3D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask3D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doSubtraction3D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask3D[bounds.sizeT];

                for (int t = 0; t < bounds.sizeT; t++)
                {
                    final BooleanMask3D m2d1 = mask1.getMask3D(t + bounds.t);
                    final BooleanMask3D m2d2 = mask2.getMask3D(t + bounds.t);

                    mask[t] = doSubtraction3D(m2d1, m2d2);
                }
            }

            return new BooleanMask4D(bounds, mask);
        }

        return new BooleanMask4D();
    }

    /**
     * Region represented by the mask.
     */
    public Rectangle4D.Integer bounds;
    /**
     * Boolean mask 3D array.
     */
    public final TreeMap<Integer, BooleanMask3D> mask;

    /**
     * Build a new 4D boolean mask with specified bounds and 3D mask array.<br>
     * The 3D mask array length should be >= to <code>bounds.getSizeT()</code>.
     */
    public BooleanMask4D(Rectangle4D.Integer bounds, BooleanMask3D[] mask)
    {
        super();

        this.bounds = bounds;
        this.mask = new TreeMap<Integer, BooleanMask3D>();

        // special case of infinite T dim
        if (bounds.sizeT == Integer.MAX_VALUE)
            this.mask.put(Integer.valueOf(Integer.MIN_VALUE), mask[0]);
        else
        {
            for (int t = 0; t < bounds.sizeT; t++)
                if (mask[t] != null)
                    this.mask.put(Integer.valueOf(bounds.t + t), mask[t]);
        }
    }

    /**
     * Build a new 4D boolean mask from the specified array of {@link Point4D}.<br>
     */
    public BooleanMask4D(Point4D.Integer[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask3D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle4D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int minT = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            int maxT = Integer.MIN_VALUE;

            for (Point4D.Integer pt : points)
            {
                final int x = pt.x;
                final int y = pt.y;
                final int z = pt.z;
                final int t = pt.t;

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
                if (z < minZ)
                    minZ = z;
                if (z > maxZ)
                    maxZ = z;
                if (t < minT)
                    minT = t;
                if (t > maxT)
                    maxT = t;
            }

            // define bounds
            bounds = new Rectangle4D.Integer(minX, minY, minZ, minT, (maxX - minX) + 1, (maxY - minY) + 1,
                    (maxZ - minZ) + 1, (maxT - minT) + 1);

            // set mask
            for (Point4D.Integer pt : points)
            {
                BooleanMask3D m3d = mask.get(Integer.valueOf(pt.t));

                // allocate 3D boolean mask if needed
                if (m3d == null)
                {
                    m3d = new BooleanMask3D(new Rectangle3D.Integer(minX, minY, minZ, bounds.sizeX, bounds.sizeY,
                            bounds.sizeZ), new BooleanMask2D[bounds.sizeZ]);
                    // set 3D mask for position T
                    mask.put(Integer.valueOf(pt.t), m3d);
                }

                BooleanMask2D m2d = m3d.getMask2D(pt.z);

                // allocate 2D boolean mask if needed
                if (m2d == null)
                {
                    m2d = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    m3d.mask.put(Integer.valueOf(pt.z), m2d);
                }

                // set mask point
                m2d.mask[((pt.y - minY) * bounds.sizeX) + (pt.x - minX)] = true;
            }

            // optimize mask 3D bounds
            for (BooleanMask3D m : mask.values())
                m.optimizeBounds();
        }
    }

    /**
     * Build a new boolean mask from the specified array of {@link Point4D}.<br>
     */
    public BooleanMask4D(Point4D[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask3D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle4D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int minT = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            int maxT = Integer.MIN_VALUE;

            for (Point4D pt : points)
            {
                final int x = (int) pt.getX();
                final int y = (int) pt.getY();
                final int z = (int) pt.getZ();
                final int t = (int) pt.getT();

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
                if (z < minZ)
                    minZ = z;
                if (z > maxZ)
                    maxZ = z;
                if (t < minT)
                    minT = t;
                if (t > maxT)
                    maxT = t;
            }

            // define bounds
            bounds = new Rectangle4D.Integer(minX, minY, minZ, minT, (maxX - minX) + 1, (maxY - minY) + 1,
                    (maxZ - minZ) + 1, (maxT - minT) + 1);

            // set mask
            for (Point4D pt : points)
            {
                BooleanMask3D m3d = mask.get(Integer.valueOf((int) pt.getT()));

                // allocate 3D boolean mask if needed
                if (m3d == null)
                {
                    m3d = new BooleanMask3D(new Rectangle3D.Integer(minX, minY, minZ, bounds.sizeX, bounds.sizeY,
                            bounds.sizeZ), new BooleanMask2D[bounds.sizeZ]);
                    // set 3D mask for position T
                    mask.put(Integer.valueOf((int) pt.getT()), m3d);
                }

                BooleanMask2D m2d = m3d.getMask2D((int) pt.getZ());

                // allocate 2D boolean mask if needed
                if (m2d == null)
                {
                    m2d = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    m3d.mask.put(Integer.valueOf((int) pt.getZ()), m2d);
                }

                // set mask point
                m2d.mask[(((int) pt.getY() - minY) * bounds.sizeX) + ((int) pt.getX() - minX)] = true;
            }

            // optimize mask 3D bounds
            for (BooleanMask3D m : mask.values())
                m.optimizeBounds();
        }
    }

    public BooleanMask4D()
    {
        this(new Rectangle4D.Integer(), new BooleanMask3D[0]);
    }

    /**
     * Returns the 3D boolean mask for the specified T position
     */
    public BooleanMask3D getMask3D(int t)
    {
        // special case of infinite T dimension
        if (bounds.sizeT == Integer.MAX_VALUE)
            return mask.firstEntry().getValue();

        return mask.get(Integer.valueOf(t));
    }

    /**
     * Returns the 2D boolean mask for the specified Z, T position
     */
    public BooleanMask2D getMask2D(int z, int t)
    {
        final BooleanMask3D m = getMask3D(t);

        if (m != null)
            return m.getMask2D(z);

        return null;
    }

    /**
     * Return <code>true</code> if boolean mask is empty
     */
    public boolean isEmpty()
    {
        return bounds.isEmpty();
    }

    /**
     * Return true if mask contains the specified point
     */
    public boolean contains(int x, int y, int z, int t)
    {
        if (bounds.contains(x, y, z, t))
        {
            final BooleanMask3D m3d = getMask3D(t);

            if (m3d != null)
                return m3d.contains(x, y, z);
        }

        return false;
    }

    /**
     * Return true if mask contains the specified 2D mask at position Z, T.
     */
    public boolean contains(BooleanMask2D booleanMask, int z, int t)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z, t);

        if (mask2d != null)
            return mask2d.contains(booleanMask);

        return false;
    }

    /**
     * Return true if mask contains the specified 3D mask at position t.
     */
    public boolean contains(BooleanMask3D booleanMask, int t)
    {
        if (isEmpty())
            return false;

        final BooleanMask3D mask3d = getMask3D(t);

        if (mask3d != null)
            return mask3d.contains(booleanMask);

        return false;
    }

    /**
     * Return true if mask contains the specified 4D mask.
     */
    public boolean contains(BooleanMask4D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeT = booleanMask.bounds.sizeT;

        // check for special MAX_INTEGER case (infinite T dim)
        if (sizeT == Integer.MAX_VALUE)
        {
            // we cannot contains it if we are not on infinite T dim too
            if (bounds.sizeT != Integer.MAX_VALUE)
                return false;

            return booleanMask.mask.firstEntry().getValue().contains(mask.firstEntry().getValue());
        }

        final int offT = booleanMask.bounds.t;

        for (int t = offT; t < offT + sizeT; t++)
            if (!contains(booleanMask.getMask3D(t), t))
                return false;

        return true;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask at
     * position Z, T
     */
    public boolean intersects(BooleanMask2D booleanMask, int z, int t)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z, t);

        if (mask2d != null)
            return mask2d.intersects(booleanMask);

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 3D mask at
     * position T
     */
    public boolean intersects(BooleanMask3D booleanMask, int t)
    {
        if (isEmpty())
            return false;

        final BooleanMask3D mask3d = getMask3D(t);

        if (mask3d != null)
            return mask3d.intersects(booleanMask);

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 4D mask region
     */
    public boolean intersects(BooleanMask4D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeT = booleanMask.bounds.sizeT;

        // check for special MAX_INTEGER case (infinite T dim)
        if (sizeT == Integer.MAX_VALUE)
        {
            // get the single T slice
            final BooleanMask3D mask3d = booleanMask.mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask3D m : mask.values())
                if (m.intersects(mask3d))
                    return true;

            return false;
        }

        // check for special MAX_INTEGER case (infinite T dim)
        if (bounds.sizeT == Integer.MAX_VALUE)
        {
            // get the single T slice
            final BooleanMask3D mask3d = mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask3D m : booleanMask.mask.values())
                if (m.intersects(mask3d))
                    return true;

            return false;
        }

        final int offT = booleanMask.bounds.t;

        for (int t = offT; t < offT + sizeT; t++)
            if (intersects(booleanMask.getMask3D(t), t))
                return true;

        return false;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle4D.Integer getOptimizedBounds(boolean compute3DBounds)
    {
        final Rectangle4D.Integer result = new Rectangle4D.Integer();

        if (mask.isEmpty())
            return result;

        Rectangle3D.Integer bounds3D = null;//new Rectangle3D.Integer();

        for (BooleanMask3D m3d : mask.values())
        {
            // get optimized 3D bounds for each T
            final Rectangle3D.Integer optB3d;

            if (compute3DBounds)
                optB3d = m3d.getOptimizedBounds();
            else
                optB3d = new Rectangle3D.Integer(m3d.bounds);

            // only add non empty bounds
            if (!optB3d.isEmpty())
            {
                if (bounds3D == null)
                    bounds3D = optB3d;
                else
                    bounds3D.add(optB3d);
            }
        }

        // empty ?
        if ((bounds3D == null) || bounds3D.isEmpty())
            return result;

        int minT = mask.firstKey().intValue();
        int maxT = mask.lastKey().intValue();

        // set 3D bounds to start with
        result.setX(bounds3D.x);
        result.setY(bounds3D.y);
        result.setZ(bounds3D.z);
        result.setSizeX(bounds3D.sizeX);
        result.setSizeY(bounds3D.sizeY);
        result.setSizeZ(bounds3D.sizeZ);

        // single T --> check for special MAX_INTEGER case
        if ((minT == maxT) && (bounds.sizeT == Integer.MAX_VALUE))
        {
            result.setT(Integer.MIN_VALUE);
            result.setSizeT(Integer.MAX_VALUE);
        }
        else
        {
            result.setT(minT);
            result.setSizeT((maxT - minT) + 1);
        }

        return result;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle4D.Integer getOptimizedBounds()
    {
        return getOptimizedBounds(true);
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public void optimizeBounds()
    {
        // start by optimizing 3D bounds
        for (BooleanMask3D m : mask.values())
            m.optimizeBounds();

        moveBounds(getOptimizedBounds(false));
    }

    /**
     * Change the bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds.
     */
    public void moveBounds(Rectangle4D.Integer value)
    {
        // bounds changed ?
        if (!bounds.equals(value))
        {
            // changed to empty mask
            if (value.isEmpty())
            {
                // clear bounds and mask
                bounds = new Rectangle4D.Integer();
                mask.clear();
                return;
            }

            final Rectangle3D.Integer bounds3D = new Rectangle3D.Integer(value.x, value.y, value.z, value.sizeX,
                    value.sizeY, value.sizeZ);

            // it was infinite T dim ?
            if (bounds.sizeT == Integer.MAX_VALUE)
            {
                // get the single 3D mask
                final BooleanMask3D m3d = mask.firstEntry().getValue();

                // adjust 3D bounds if needed to the single 3D mask
                m3d.moveBounds(bounds3D);

                // we passed from infinite T to defined T range
                if (value.sizeT != Integer.MAX_VALUE)
                {
                    // assign the same 3D mask for all C position
                    mask.clear();
                    for (int t = 0; t <= value.sizeT; t++)
                        mask.put(Integer.valueOf(t + value.t), (BooleanMask3D) m3d.clone());
                }
            }
            // we pass to infinite T dim
            else if (value.sizeT == Integer.MAX_VALUE)
            {
                // try to use the 3D mask at T position
                BooleanMask3D mask3D = getMask3D(value.t);

                // otherwise we use the first found 3D mask
                if ((mask3D == null) && !mask.isEmpty())
                    mask3D = mask.firstEntry().getValue();

                // set new mask
                mask.clear();
                if (mask3D != null)
                    mask.put(Integer.valueOf(Integer.MIN_VALUE), mask3D);
            }
            else
            {
                // create new mask array
                final BooleanMask3D[] newMask = new BooleanMask3D[value.sizeT];

                for (int t = 0; t < value.sizeT; t++)
                {
                    final BooleanMask3D mask3D = getMask3D(value.t + t);

                    if (mask3D != null)
                        // adjust 3D bounds
                        mask3D.moveBounds(bounds3D);

                    newMask[t] = mask3D;
                }

                // set new mask
                mask.clear();
                for (int t = 0; t < value.sizeT; t++)
                    mask.put(Integer.valueOf(value.t + t), newMask[t]);
            }

            bounds = value;
        }
    }

    int[] toInt4D(int[] source3D, int t)
    {
        final int[] result = new int[(source3D.length * 4) / 3];

        int pt = 0;
        for (int i = 0; i < source3D.length; i += 3)
        {
            result[pt++] = source3D[i + 0];
            result[pt++] = source3D[i + 1];
            result[pt++] = source3D[i + 2];
            result[pt++] = t;
        }

        return result;
    }

    /**
     * Return an array of {@link icy.type.point.Point4D.Integer} containing the contour/surface points
     * of the 4D mask.<br>
     * Points are returned in ascending XYZT order. <br>
     * <br>
     * WARNING: The basic implementation is not totally accurate.<br>
     * It returns all points from the first and the last T slices + contour points for intermediate T
     * slices.
     * 
     * @see #getContourPointsAsIntArray()
     */
    public Point4D.Integer[] getContourPoints()
    {
        return Point4D.Integer.toPoint4D(getContourPointsAsIntArray());
    }

    /**
     * Return an array of integer containing the contour/surface points of the 4D mask.<br>
     * <code>result.length</code> = number of point * 4<br>
     * <code>result[(pt * 4) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 3]</code> = T coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZT order.<br>
     * <br>
     * WARNING: The basic implementation is not totally accurate.<br>
     * It returns all points from the first and the last T slices + contour points for intermediate T
     * slices.
     * 
     * @see #getContourPoints()
     */
    public int[] getContourPointsAsIntArray()
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        // perimeter = first slice volume + inter slices perimeter + last slice volume
        // TODO: fix this method and use real 4D contour point
        if (mask.size() <= 2)
        {
            for (Entry<Integer, BooleanMask3D> entry : mask.entrySet())
                result.add(toInt4D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));
        }
        else
        {
            final Entry<Integer, BooleanMask3D> firstEntry = mask.firstEntry();
            final Entry<Integer, BooleanMask3D> lastEntry = mask.lastEntry();
            final Integer firstKey = firstEntry.getKey();
            final Integer lastKey = lastEntry.getKey();

            result.add(toInt4D(firstEntry.getValue().getPointsAsIntArray(), firstKey.intValue()));

            for (Entry<Integer, BooleanMask3D> entry : mask.subMap(firstKey, false, lastKey, false).entrySet())
                result.add(toInt4D(entry.getValue().getContourPointsAsIntArray(), entry.getKey().intValue()));

            result.add(toInt4D(lastEntry.getValue().getPointsAsIntArray(), lastKey.intValue()));
        }

        return result.asArray();
    }

    /**
     * Return an array of {@link icy.type.point.Point4D.Integer} representing all points of the
     * current 4D mask.<br>
     * Points are returned in ascending XYZT order.
     */
    public Point4D.Integer[] getPoints()
    {
        return Point4D.Integer.toPoint4D(getPointsAsIntArray());
    }

    /**
     * Return an array of integer representing all points of the current 4D mask.<br>
     * <code>result.length</code> = number of point * 4<br>
     * <code>result[(pt * 4) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 4) + 3]</code> = T coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZT order.
     */
    public int[] getPointsAsIntArray()
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        for (Entry<Integer, BooleanMask3D> entry : mask.entrySet())
            result.add(toInt4D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));

        return result.asArray();
    }

    @Override
    public Object clone()
    {
        final BooleanMask4D result = new BooleanMask4D();

        result.bounds = new Rectangle4D.Integer(bounds);
        for (Entry<Integer, BooleanMask3D> entry : mask.entrySet())
            result.mask.put(entry.getKey(), (BooleanMask3D) entry.getValue().clone());

        return result;
    }
}
