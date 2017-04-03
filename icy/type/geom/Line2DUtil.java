package icy.type.geom;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Line2DUtil
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
}
