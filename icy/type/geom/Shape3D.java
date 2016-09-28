package icy.type.geom;

import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;

/**
 * The <code>Shape3D</code> interface provides definitions for objects
 * that represent some form of 3D geometric Shape3D. Each <code>Shape3D</code> object provides callbacks to get the
 * bounding box of the geometry, determine whether points or rectangles lie partly or entirely within the interior
 * of the <code>Shape3D</code>.
 * <p>
 * <b>Definition of insideness:</b> A point is considered to lie inside a <code>Shape3D</code> if and only if:
 * <ul>
 * <li>it lies completely inside the<code>Shape3D</code> boundary <i>or</i>
 * <li>it lies exactly on the <code>Shape3D</code> boundary.
 * </ul>
 * <p>
 * The <code>contains</code> and <code>intersects</code> methods consider the interior of a <code>Shape3D</code> to be
 * the area it encloses as if it were filled.
 */
public interface Shape3D
{
    /**
     * Returns the bounding box of the <code>Shape3D</code>.
     * Note that there is no guarantee that the returned {@link Rectangle3D} is the smallest bounding box that encloses
     * the <code>Shape3D</code>, only that the <code>Shape3D</code> lies entirely within the indicated
     * <code>Rectangle3D</code>.
     * 
     * @return an instance of <code>Rectangle3D</code> that is a
     *         high-precision bounding box of the <code>Shape3D</code>.
     */
    public Rectangle3D getBounds();

    /**
     * Tests if the specified 3D coordinates are inside the boundary of the <code>Shape3D</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @return <code>true</code> if the specified coordinates are inside
     *         the <code>Shape3D</code> boundary; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double z);

    /**
     * Tests if a specified {@link Point3D} is inside the boundary
     * of the <code>Shape3D</code>.
     * 
     * @param p
     *        the specified <code>Point3D</code> to be tested
     * @return <code>true</code> if the specified <code>Point3D</code> is
     *         inside the boundary of the <code>Shape3D</code>; <code>false</code> otherwise.
     */
    public boolean contains(Point3D p);

    /**
     * Tests if the interior of the <code>Shape3D</code> intersects the interior of a specified 3D rectangular area.
     * The rectangular area is considered to intersect the <code>Shape3D</code> if any point is contained in both the
     * interior of the <code>Shape3D</code> and the specified 3D rectangular area.
     * <p>
     * The {@code Shape3D.intersects()} method allows a {@code Shape3D} implementation to conservatively return
     * {@code true} when:
     * <ul>
     * <li>there is a high probability that the 3D rectangular area and the <code>Shape3D</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code Shapes} this method might return {@code true} even though the 3D rectangular area
     * does not intersect the {@code Shape3D}.
     *
     * @param x
     *        the X coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param y
     *        the Y coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param z
     *        the Z coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param sizeX
     *        the width of the specified 3D rectangular area
     * @param sizeY
     *        the height of the specified 3D rectangular area
     * @param sizeZ
     *        the depth of the specified 3D rectangular area
     * @return <code>true</code> if the interior of the <code>Shape3D</code> and the interior of the 3D rectangular area
     *         intersect, or are both highly likely to intersect and intersection calculations would be too expensive to
     *         perform; <code>false</code> otherwise.
     */
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ);

    /**
     * Tests if the interior of the <code>Shape3D</code> intersects the
     * interior of a specified <code>Rectangle3D</code>.
     * The {@code Shape3D.intersects()} method allows a {@code Shape3D} implementation to conservatively return
     * {@code true} when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle3D</code> and the <code>Shape3D</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code Shapes} this method might return {@code true} even though the {@code Rectangle3D}
     * does not intersect the {@code Shape3D}.
     *
     * @param r
     *        the specified <code>Rectangle3D</code>
     * @return <code>true</code> if the interior of the <code>Shape3D</code> and the interior of the specified
     *         <code>Rectangle3D</code> intersect, or are both highly likely to intersect and intersection calculations
     *         would be too expensive to perform; <code>false</code> otherwise.
     * @see #intersects(double, double, double, double, double, double)
     */
    public boolean intersects(Rectangle3D r);

    /**
     * Tests if the interior of the <code>Shape3D</code> entirely contains the specified 3D rectangular area. All
     * coordinates that lie inside the 3D rectangular area must lie within the <code>Shape3D</code> for the entire
     * 3D rectangular area to be considered contained within the <code>Shape3D</code>.
     * <p>
     * The {@code Shape3D.contains()} method allows a {@code Shape3D} implementation to conservatively return
     * {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>Shape3D</code> entirely contains the 3D rectangular
     * area are prohibitively expensive.
     * </ul>
     * This means that for some {@code Shapes} this method might return {@code false} even though the {@code Shape3D}
     * contains the 3D rectangular area.
     *
     * @param x
     *        the X coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param y
     *        the Y coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param z
     *        the Z coordinate of the closest-upper-left corner of the specified 3D rectangular area
     * @param sizeX
     *        the width of the specified 3D rectangular area
     * @param sizeY
     *        the height of the specified 3D rectangular area
     * @param sizeZ
     *        the depth of the specified 3D rectangular area
     * @return <code>true</code> if the interior of the <code>Shape3D</code> entirely contains the specified rectangular
     *         area; <code>false</code> otherwise or, if the <code>Shape3D</code> contains the 3D rectangular area and
     *         the <code>intersects</code> method returns <code>true</code> and the containment calculations would be
     *         too expensive to perform.
     */
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ);

    /**
     * Tests if the interior of the <code>Shape3D</code> entirely contains the specified <code>Rectangle3D</code>.
     * The {@code Shape3D.contains()} method allows a {@code Shape3D} implementation to conservatively return
     * {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>Shape3D</code> entirely contains the
     * <code>Rectangle3D</code> are prohibitively expensive.
     * </ul>
     * This means that for some {@code Shapes} this method might return {@code false} even though the {@code Shape3D}
     * contains the {@code Rectangle3D}.
     *
     * @param r
     *        The specified <code>Rectangle3D</code>
     * @return <code>true</code> if the interior of the <code>Shape3D</code> entirely contains the
     *         <code>Rectangle3D</code>; <code>false</code> otherwise or, if the <code>Shape3D</code> contains the
     *         <code>Rectangle3D</code> and the <code>intersects</code> method returns <code>true</code> and the
     *         containment calculations would be too expensive to perform.
     * @see #contains(double, double, double, double, double, double)
     */
    public boolean contains(Rectangle3D r);
}
