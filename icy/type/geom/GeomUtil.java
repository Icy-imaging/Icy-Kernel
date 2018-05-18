package icy.type.geom;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Utilities for geom objects
 * 
 * @author Stephane Dallongeville
 */
public class GeomUtil
{
    /**
     * Get intersection point between 2 lines.
     * 
     * @return the intersection point between 2 lines, it can return <code>null</code> if the 2 lines are //
     */
    public static Point2D getIntersection(Line2D lineA, Line2D lineB)
    {
        final double x1 = lineA.getX1();
        final double y1 = lineA.getY1();
        final double x2 = lineA.getX2();
        final double y2 = lineA.getY2();

        final double x3 = lineB.getX1();
        final double y3 = lineB.getY1();
        final double x4 = lineB.getX2();
        final double y4 = lineB.getY2();

        final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (d != 0)
        {
            final double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            final double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

            return new Point2D.Double(xi, yi);
        }

        return null;
    }

    /**
     * Get intersection point between 2 lines.
     *
     * @param lineA
     *        first line
     * @param lineB
     *        second line
     * @param limitToSegmentA
     *        Limit intersection to segment <i>lineA</i>, if intersection point is outside <i>lineA</i> then
     *        <code>null</code> is returned
     * @param limitToSegmentB
     *        Limit intersection to segment <i>lineB</i>, if intersection point is outside <i>lineB</i> then
     *        <code>null</code> is returned
     */
    public static Point2D getIntersection(Line2D lineA, Line2D lineB, boolean limitToSegmentA, boolean limitToSegmentB)
    {
        final Point2D result = getIntersection(lineA, lineB);

        if (result == null)
            return null;

        final Rectangle2D rectPt = new Rectangle2D.Double(result.getX() - 0.5d, result.getY() - 0.5d, 1d, 1d);

        // constraint to line segment if wanted
        if (limitToSegmentA && !lineA.intersects(rectPt))
            return null;
        if (limitToSegmentB && !lineB.intersects(rectPt))
            return null;

        return result;
    }

    /**
     * Returns rotation information from AffineTransform.<br>
     * <b>WARNING:</b> this method may not be 100% accurate because of possible combined transformation
     */
    public static double getRotation(AffineTransform transform)
    {
        // use the iterative polar decomposition algorithm described by Ken Shoemake:
        // http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf

        // start with the contents of the upper 2x2 portion of the matrix
        double m00 = transform.getScaleX();
        double m01 = transform.getShearX();
        double m10 = transform.getShearY();
        double m11 = transform.getScaleY();

        for (int ii = 0; ii < 10; ii++)
        {
            // store the results of the previous iteration
            final double o00 = m00;
            final double o10 = m10;
            final double o01 = m01;
            final double o11 = m11;

            // compute average of the matrix with its inverse transpose
            final double det = o00 * o11 - o10 * o01;

            // determinant is zero; matrix is not invertible --> use alternative method
            if (Math.abs(det) == 0d)
                return getRotationFast(transform);

            final double hrdet = 0.5f / det;

            m00 = +o11 * hrdet + o00 * 0.5f;
            m10 = -o01 * hrdet + o10 * 0.5f;
            m01 = -o10 * hrdet + o01 * 0.5f;
            m11 = +o00 * hrdet + o11 * 0.5f;

            // compute the difference; if it's small enough, we're done
            final double d00 = m00 - o00;
            final double d10 = m10 - o10;
            final double d01 = m01 - o01;
            final double d11 = m11 - o11;

            if (((d00 * d00) + (d10 * d10) + (d01 * d01) + (d11 * d11)) < 0.0001d)
                break;
        }

        // now that we have a nice orthogonal matrix, we can extract the rotation
        return Math.atan2(m01, m00);
    }

    /**
     * Returns rotation information from AffineTransform (fast method).<br>
     * <b>WARNING:</b> this method may not be 100% accurate because of possible combined transformation
     */
    public static double getRotationFast(AffineTransform transform)
    {
        // m00, m01
        final double a = transform.getScaleX();
        final double b = transform.getShearX();

        if ((Math.abs(a) >= 0.001d) && (Math.abs(b) >= 0.001d))
            return Math.atan(-b / a);

        // m10, m11
        final double c = transform.getShearY();
        final double d = transform.getScaleY();

        if ((Math.abs(c) >= 0.001d) && (Math.abs(d) >= 0.001d))
            return Math.atan(c / d);

        // use alternative method
        final Point2D pp0 = transform.transform(new Point(0, 0), null);
        final Point2D pp1 = transform.transform(new Point(1, 0), null);
        final double dx = pp1.getX() - pp0.getX();
        final double dy = pp1.getY() - pp0.getY();

        return Math.atan2(dy, dx);
    }

    /**
     * Returns uniform scale (X=Y) information from AffineTransform.<br>
     * <b>WARNING:</b> this method may not be 100% accurate because of possible combined transformation
     */
    public static double getScale(AffineTransform transform)
    {
        // m00, m01
        final double a = transform.getScaleX();
        final double b = transform.getShearX();
        // m10, m11
        final double c = transform.getShearY();
        final double d = transform.getScaleY();

        // the square root of the signed area of the parallelogram spanned by the axis vectors
        final double cp = (a * d) - (b * c);

        return (cp < 0f) ? -Math.sqrt(-cp) : Math.sqrt(cp);
    }

    /**
     * Returns scale X information from AffineTransform.<br>
     * <b>WARNING:</b> this method may not be 100% accurate because of possible combined transformation
     */
    public static double getScaleX(AffineTransform transform)
    {
        // m00, m10
        final double a = transform.getScaleX();
        final double c = transform.getShearY();

        return Math.sqrt((a * a) + (c * c));
    }

    /**
     * Returns scale Y information from AffineTransform.<br>
     * <b>WARNING:</b> this method may not be 100% accurate because of possible combined transformation
     */
    public static double getScaleY(AffineTransform transform)
    {
        // m01, m11
        final double b = transform.getShearX();
        final double d = transform.getScaleY();

        return Math.sqrt((b * b) + (d * d));
    }
}
