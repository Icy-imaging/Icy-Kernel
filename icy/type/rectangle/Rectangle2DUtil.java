package icy.type.rectangle;

import icy.type.geom.Line2DUtil;
import icy.util.ShapeUtil;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Rectangle2DUtil
{
    /**
     * Returns the shortest line segment which result from the intersection of the given rectangle <b>bounds</b> and
     * line. It returns <code>null</code> if the line segment does not intersects the Rectangle <b>content</b>.
     */
    public static Line2D getIntersectionLine(Rectangle2D rectangle, Line2D line)
    {
        if (rectangle.intersectsLine(line))
        {
            final List<Point2D> result = new ArrayList<Point2D>();

            final Point2D topLeft = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
            final Point2D topRight = new Point2D.Double(rectangle.getMaxX(), rectangle.getMinY());
            final Point2D bottomRight = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());
            final Point2D bottomLeft = new Point2D.Double(rectangle.getMinX(), rectangle.getMaxY());
            Point2D intersection;

            intersection = Line2DUtil.getIntersection(new Line2D.Double(topLeft, topRight), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = Line2DUtil.getIntersection(new Line2D.Double(topRight, bottomRight), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = Line2DUtil.getIntersection(new Line2D.Double(bottomRight, bottomLeft), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = Line2DUtil.getIntersection(new Line2D.Double(bottomLeft, topLeft), line, true, false);
            if (intersection != null)
                result.add(intersection);

            if (result.size() >= 2)
                return new Line2D.Double(result.get(0), result.get(1));
        }

        return null;
    }

    /**
     * Returns a scaled form of the specified {@link Rectangle2D} by specified factor.
     * 
     * @param rect
     *        the {@link Rectangle2D} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (rect location is modified)
     */
    public static Rectangle2D getScaledRectangle(Rectangle2D rect, double factor, boolean centered)
    {
        final Rectangle2D result = new Rectangle2D.Double();

        result.setFrame(rect);
        ShapeUtil.scale(result, factor, centered);

        return result;
    }
}
