/**
 * 
 */
package icy.type.point;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Utilities for Point2D class.
 * 
 * @author Stephane
 */
public class Point2DUtil
{
    /**
     * Test if the 2 specified points are <code>connected</code>.<br>
     * Points are considered connected if max(deltaX, deltaY) <= 1
     */
    public static boolean areConnected(Point2D p1, Point2D p2)
    {
        return Math.max(Math.abs(p2.getX() - p1.getX()), Math.abs(p2.getY() - p1.getY())) <= 1;
    }

    /**
     * Returns the L1 distance between 2 points
     */
    public static double getL1Distance(Point2D p1, Point2D p2)
    {
        return Math.abs(p2.getX() - p1.getX()) + Math.abs(p2.getY() - p1.getY());
    }

    /**
     * Returns the square of the distance between 2 points.
     */
    public static double getSquareDistance(Point2D pt1, Point2D pt2)
    {
        double px = pt2.getX() - pt1.getX();
        double py = pt2.getY() - pt1.getY();
        return (px * px) + (py * py);
    }

    /**
     * Returns the distance between 2 points.
     */
    public static double getDistance(Point2D pt1, Point2D pt2)
    {
        return Math.sqrt(getSquareDistance(pt1, pt2));
    }

    /**
     * Returns the distance between 2 points using specified scale factor for x/y dimension.
     */
    public static double getDistance(Point2D pt1, Point2D pt2, double factorX, double factorY)
    {
        double px = (pt2.getX() - pt1.getX()) * factorX;
        double py = (pt2.getY() - pt1.getY()) * factorY;
        return Math.sqrt(px * px + py * py);
    }

    /**
     * Returns the total distance of the specified list of points.
     */
    public static double getTotalDistance(List<Point2D> points, double factorX, double factorY, boolean connectLastPoint)
    {
        final int size = points.size();
        double result = 0d;

        if (size > 1)
        {
            for (int i = 0; i < size - 1; i++)
                result += getDistance(points.get(i), points.get(i + 1), factorX, factorY);

            // add last to first point distance
            if (connectLastPoint)
                result += getDistance(points.get(size - 1), points.get(0), factorX, factorY);
        }

        return result;
    }
}
