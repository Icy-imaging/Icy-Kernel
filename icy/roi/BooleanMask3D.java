package icy.roi;

import icy.type.collection.array.DynamicArray;
import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;

import java.awt.Rectangle;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class to define a 3D boolean mask and make basic boolean operation between masks.<br>
 * The bounds property of this object define the area of the mask where the mask contains the
 * boolean mask itself.
 * 
 * @author Stephane
 */
public class BooleanMask3D implements Cloneable
{
    // Internal use only
    private static BooleanMask2D doUnion2D(BooleanMask2D m1, BooleanMask2D m2)
    {
        if (m1 == null)
        {
            // only use the 2D mask from second mask
            if (m2 != null)
                return (BooleanMask2D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 2D mask from first mask
            return (BooleanMask2D) m1.clone();

        // process union of 2D mask
        return BooleanMask2D.getUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doIntersection2D(BooleanMask2D m1, BooleanMask2D m2)
    {
        if ((m1 == null) || (m2 == null))
            return null;

        // process intersection of 2D mask
        return BooleanMask2D.getIntersection(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doExclusiveUnion2D(BooleanMask2D m1, BooleanMask2D m2)
    {
        if (m1 == null)
        {
            // only use the 2D mask from second mask
            if (m2 != null)
                return (BooleanMask2D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 2D mask from first mask
            return (BooleanMask2D) m1.clone();

        // process exclusive union of 2D mask
        return BooleanMask2D.getExclusiveUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doSubtraction2D(BooleanMask2D m1, BooleanMask2D m2)
    {
        if (m1 == null)
            return null;
        // only use the 2D mask from first mask
        if (m2 == null)
            return (BooleanMask2D) m1.clone();

        // process subtraction of 2D mask
        return BooleanMask2D.getSubtraction(m1, m2);
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
    public static BooleanMask3D getUnion(BooleanMask3D mask1, BooleanMask3D mask2)
    {
        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doUnion2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doUnion2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
    public static BooleanMask3D getIntersection(BooleanMask3D mask1, BooleanMask3D mask2)
    {
        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doIntersection2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doIntersection2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
    public static BooleanMask3D getExclusiveUnion(BooleanMask3D mask1, BooleanMask3D mask2)
    {
        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doExclusiveUnion2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doExclusiveUnion2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
    public static BooleanMask3D getSubtraction(BooleanMask3D mask1, BooleanMask3D mask2)
    {
        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        // need to subtract something ?
        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doSubtraction2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doSubtraction2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
    }

    /**
     * Region represented by the mask.
     */
    public Rectangle3D.Integer bounds;
    /**
     * Boolean mask 2D array.
     */
    public final TreeMap<Integer, BooleanMask2D> mask;

    /**
     * Build a new 3D boolean mask with specified bounds and 2D mask array.<br>
     * The 2D mask array length should be >= to <code>bounds.getSizeZ()</code>.
     */
    public BooleanMask3D(Rectangle3D.Integer bounds, BooleanMask2D[] mask)
    {
        super();

        this.bounds = bounds;
        this.mask = new TreeMap<Integer, BooleanMask2D>();

        // special case of infinite Z dim
        if (bounds.sizeZ == Integer.MAX_VALUE)
            this.mask.put(Integer.valueOf(-1), mask[0]);
        else
        {
            for (int z = 0; z < bounds.sizeZ; z++)
                if (mask[z] != null)
                    this.mask.put(Integer.valueOf(bounds.z + z), mask[z]);
        }
    }

    /**
     * Build a new 3D boolean mask from the specified array of {@link Point3D}.<br>
     */
    public BooleanMask3D(Point3D.Integer[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask2D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle3D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Point3D.Integer pt : points)
            {
                final int x = pt.x;
                final int y = pt.y;
                final int z = pt.z;

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
            }

            // define bounds
            bounds = new Rectangle3D.Integer(minX, minY, minZ, (maxX - minX) + 1, (maxY - minY) + 1, (maxZ - minZ) + 1);

            // set mask
            for (Point3D.Integer pt : points)
            {
                BooleanMask2D m = mask.get(Integer.valueOf(pt.z));

                // allocate boolean mask if needed
                if (m == null)
                {
                    m = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    mask.put(Integer.valueOf(pt.z), m);
                }

                // set mask point
                m.mask[((pt.y - minY) * bounds.sizeX) + (pt.x - minX)] = true;
            }

            // optimize mask 2D bounds
            for (BooleanMask2D m : mask.values())
                m.optimizeBounds();
        }
    }

    /**
     * Build a new boolean mask from the specified array of {@link Point3D}.<br>
     */
    public BooleanMask3D(Point3D[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask2D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle3D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Point3D pt : points)
            {
                final int x = (int) pt.getX();
                final int y = (int) pt.getY();
                final int z = (int) pt.getZ();

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
            }

            // define bounds
            bounds = new Rectangle3D.Integer(minX, minY, minZ, (maxX - minX) + 1, (maxY - minY) + 1, (maxZ - minZ) + 1);

            // set mask
            for (Point3D pt : points)
            {
                BooleanMask2D m = mask.get(Integer.valueOf((int) pt.getZ()));

                // allocate boolean mask if needed
                if (m == null)
                {
                    m = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    mask.put(Integer.valueOf((int) pt.getZ()), m);
                }

                // set mask point
                m.mask[(((int) pt.getY() - minY) * bounds.sizeX) + ((int) pt.getX() - minX)] = true;
            }

            // optimize mask 2D bounds
            for (BooleanMask2D m : mask.values())
                m.optimizeBounds();
        }
    }

    public BooleanMask3D()
    {
        this(new Rectangle3D.Integer(), new BooleanMask2D[0]);
    }

    /**
     * Returns the 2D boolean mask for the specified Z position
     */
    public BooleanMask2D getMask2D(int z)
    {
        // special case of infinite Z dimension
        if (bounds.sizeZ == Integer.MAX_VALUE)
            return mask.firstEntry().getValue();

        return mask.get(Integer.valueOf(z));
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
    public boolean contains(int x, int y, int z)
    {
        if (bounds.contains(x, y, z))
        {
            final BooleanMask2D m2d = getMask2D(z);

            if (m2d != null)
                return m2d.contains(x, y);
        }

        return false;
    }

    /**
     * Return true if mask contains the specified 2D mask at position Z.
     */
    public boolean contains(BooleanMask2D booleanMask, int z)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z);

        if (mask2d != null)
            return mask2d.contains(booleanMask);

        return false;
    }

    /**
     * Return true if mask contains the specified 3D mask.
     */
    public boolean contains(BooleanMask3D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeZ = booleanMask.bounds.sizeZ;

        // check for special MAX_INTEGER case (infinite Z dim)
        if (sizeZ == Integer.MAX_VALUE)
        {
            // we cannot contains it if we are not on infinite Z dim too
            if (bounds.sizeZ != Integer.MAX_VALUE)
                return false;

            return booleanMask.mask.firstEntry().getValue().contains(mask.firstEntry().getValue());
        }

        final int offZ = booleanMask.bounds.z;

        for (int z = offZ; z < offZ + sizeZ; z++)
            if (!contains(booleanMask.getMask2D(z), z))
                return false;

        return true;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask at
     * position Z.
     */
    public boolean intersects(BooleanMask2D booleanMask, int z)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z);

        if (mask2d != null)
            return mask2d.intersects(booleanMask);

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 3D mask region.
     */
    public boolean intersects(BooleanMask3D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeZ = booleanMask.bounds.sizeZ;

        // check for special MAX_INTEGER case (infinite Z dim)
        if (sizeZ == Integer.MAX_VALUE)
        {
            // get the single Z slice
            final BooleanMask2D mask2d = booleanMask.mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask2D m : mask.values())
                if (m.intersects(mask2d))
                    return true;

            return false;
        }

        // check for special MAX_INTEGER case (infinite Z dim)
        if (bounds.sizeZ == Integer.MAX_VALUE)
        {
            // get the single Z slice
            final BooleanMask2D mask2d = mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask2D m : booleanMask.mask.values())
                if (m.intersects(mask2d))
                    return true;

            return false;
        }

        final int offZ = booleanMask.bounds.z;

        for (int z = offZ; z < offZ + sizeZ; z++)
            if (intersects(booleanMask.getMask2D(z), z))
                return true;

        return false;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle3D.Integer getOptimizedBounds(boolean compute2DBounds)
    {
        final Rectangle3D.Integer result = new Rectangle3D.Integer();

        if (mask.isEmpty())
            return result;

        Rectangle bounds2D = null;

        for (BooleanMask2D m2d : mask.values())
        {
            // get optimized 2D bounds for each Z
            final Rectangle optB2d;

            if (compute2DBounds)
                optB2d = m2d.getOptimizedBounds();
            else
                optB2d = m2d.bounds;

            // only add non empty bounds
            if (!optB2d.isEmpty())
            {
                if (bounds2D == null)
                    bounds2D = optB2d;
                else
                    bounds2D.add(optB2d);
            }
        }

        // empty ?
        if ((bounds2D == null) || bounds2D.isEmpty())
            return result;

        int minZ = mask.firstKey().intValue();
        int maxZ = mask.lastKey().intValue();

        // set 2D bounds to start with
        result.setX(bounds2D.x);
        result.setY(bounds2D.y);
        result.setSizeX(bounds2D.width);
        result.setSizeY(bounds2D.height);

        // single Z --> check for special MAX_INTEGER case
        if ((minZ == maxZ) && (bounds.sizeZ == Integer.MAX_VALUE))
        {
            result.setZ(-1);
            result.setSizeZ(Integer.MAX_VALUE);
        }
        else
        {
            bounds.setZ(minZ);
            bounds.setSizeZ((maxZ - minZ) + 1);
        }

        return result;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle3D.Integer getOptimizedBounds()
    {
        return getOptimizedBounds(true);
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public void optimizeBounds()
    {
        // start by optimizing 2D bounds
        for (BooleanMask2D m : mask.values())
            m.optimizeBounds();

        moveBounds(getOptimizedBounds(false));
    }

    /**
     * Change the bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds.
     */
    public void moveBounds(Rectangle3D.Integer value)
    {
        // bounds changed ?
        if (!bounds.equals(value))
        {
            // changed to empty mask
            if (value.isEmpty())
            {
                // clear bounds and mask
                bounds = new Rectangle3D.Integer();
                mask.clear();
                return;
            }

            final Rectangle bounds2D = new Rectangle(value.x, value.y, value.sizeX, value.sizeY);

            // it was infinite Z dim ?
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // get the single 2D mask
                final BooleanMask2D m2d = mask.firstEntry().getValue();

                // adjust 2D bounds if needed to the single 2D mask
                m2d.moveBounds(bounds2D);

                // we passed from infinite Z to defined Z range
                if (value.sizeZ != Integer.MAX_VALUE)
                {
                    // assign the same 2D mask for all Z position
                    mask.clear();
                    for (int z = 0; z <= value.sizeZ; z++)
                        mask.put(Integer.valueOf(z + value.z), (BooleanMask2D) m2d.clone());
                }
            }
            // we pass to infinite Z dim
            else if (value.sizeZ == Integer.MAX_VALUE)
            {
                // try to use the 2D mask at Z position
                BooleanMask2D mask2D = getMask2D(value.z);

                // otherwise we use the first found 2D mask
                if ((mask2D == null) && !mask.isEmpty())
                    mask2D = mask.firstEntry().getValue();

                // set new mask
                mask.clear();
                if (mask2D != null)
                    mask.put(Integer.valueOf(-1), mask2D);
            }
            else
            {
                // create new mask array
                final BooleanMask2D[] newMask = new BooleanMask2D[value.sizeZ];

                for (int z = 0; z <= value.sizeZ; z++)
                {
                    final BooleanMask2D mask2D = getMask2D(value.z + z);

                    if (mask2D != null)
                        // adjust 2D bounds
                        mask2D.moveBounds(bounds2D);

                    newMask[z] = mask2D;
                }

                // set new mask
                mask.clear();
                for (int z = 0; z <= value.sizeZ; z++)
                    mask.put(Integer.valueOf(value.z + z), newMask[z]);
            }

            bounds = value;
        }
    }

    int[] toInt3D(int[] source2D, int z)
    {
        final int[] result = new int[(source2D.length * 3) / 2];

        int pt = 0;
        for (int i = 0; i < source2D.length; i += 2)
        {
            result[pt++] = source2D[i + 0];
            result[pt++] = source2D[i + 1];
            result[pt++] = z;
        }

        return result;
    }

    /**
     * Return an array of {@link icy.type.point.Point3D.Integer} containing the contour/surface
     * points
     * of the 3D mask.<br>
     * Points are returned in ascending XYZ order. <br>
     * <br>
     * WARNING: The basic implementation is not totally accurate.<br>
     * It returns all points from the first and the last Z slices + contour points for intermediate
     * Z
     * slices.
     * 
     * @see #getContourPointsAsIntArray()
     */
    public Point3D.Integer[] getContourPoints()
    {
        return Point3D.Integer.toPoint3D(getContourPointsAsIntArray());
    }

    /**
     * Return an array of integer containing the contour/surface points of the 3D mask.<br>
     * <code>result.length</code> = number of point * 3<br>
     * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZ order.<br>
     * <br>
     * WARNING: The basic implementation is not totally accurate.<br>
     * It returns all points from the first and the last Z slices + contour points for intermediate
     * Z
     * slices.
     * 
     * @see #getContourPoints()
     */
    public int[] getContourPointsAsIntArray()
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        // perimeter = first slice volume + inter slices perimeter + last slice volume
        // TODO: fix this method and use real 3D contour point
        if (mask.size() <= 2)
        {
            for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
                result.add(toInt3D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));
        }
        else
        {
            final Entry<Integer, BooleanMask2D> firstEntry = mask.firstEntry();
            final Entry<Integer, BooleanMask2D> lastEntry = mask.lastEntry();
            final Integer firstKey = firstEntry.getKey();
            final Integer lastKey = lastEntry.getKey();

            result.add(toInt3D(firstEntry.getValue().getPointsAsIntArray(), firstKey.intValue()));

            for (Entry<Integer, BooleanMask2D> entry : mask.subMap(firstKey, false, lastKey, false).entrySet())
                result.add(toInt3D(entry.getValue().getContourPointsAsIntArray(), entry.getKey().intValue()));

            result.add(toInt3D(lastEntry.getValue().getPointsAsIntArray(), lastKey.intValue()));
        }

        return result.asArray();
    }

    /**
     * Return an array of {@link icy.type.point.Point3D.Integer} representing all points of the
     * current 3D mask.<br>
     * Points are returned in ascending XYZ order.
     */
    public Point3D.Integer[] getPoints()
    {
        return Point3D.Integer.toPoint3D(getPointsAsIntArray());
    }

    /**
     * Return an array of integer representing all points of the current 3D mask.<br>
     * <code>result.length</code> = number of point * 3<br>
     * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZ order.
     */
    public int[] getPointsAsIntArray()
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
            result.add(toInt3D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));

        return result.asArray();
    }

    @Override
    public Object clone()
    {
        final BooleanMask3D result = new BooleanMask3D();

        result.bounds = new Rectangle3D.Integer(bounds);
        for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
            result.mask.put(entry.getKey(), (BooleanMask2D) entry.getValue().clone());

        return result;
    }
}
