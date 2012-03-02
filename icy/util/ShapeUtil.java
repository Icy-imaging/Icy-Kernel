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
package icy.util;

import icy.painter.PathAnchor2D;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;

/**
 * @author Stephane
 */
public class ShapeUtil
{
    public interface ShapeConsumer
    {
        public boolean consume(Shape shape);
    }

    /**
     * Scale the specified {@link RectangularShape} by specified factor.
     * 
     * @param shape
     *        the {@link RectangularShape} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (shape location is modified)
     */
    public static void scale(RectangularShape shape, double factor, boolean centered)
    {
        final double w = shape.getWidth();
        final double h = shape.getHeight();
        final double newW = w * factor;
        final double newH = h * factor;

        if (centered)
        {
            final double deltaW = (newW - w) / 2;
            final double deltaH = (newH - h) / 2;
            
            shape.setFrame(shape.getX() - deltaW, shape.getY() - deltaH, newW, newH);
        }
        else
            shape.setFrame(shape.getX(), shape.getY(), newW, newH);
    }

    /**
     * Enlarge the specified {@link RectangularShape} by specified width and height.
     * 
     * @param shape
     *        the {@link RectangularShape} to scale
     * @param width
     *        the width to add
     * @param height
     *        the height to add
     * @param centered
     *        if true then enlargement is centered (shape location is modified)
     */
    public static void enlarge(RectangularShape shape, double width, double height, boolean centered)
    {
        final double w = shape.getWidth();
        final double h = shape.getHeight();
        final double newW = w + width;
        final double newH = h + height;

        if (centered)
        {
            final double deltaW = (newW - w) / 2;
            final double deltaH = (newH - h) / 2;

            shape.setFrame(shape.getX() - deltaW, shape.getY() - deltaH, newW, newH);
        }
        else
            shape.setFrame(shape.getX(), shape.getY(), newW, newH);
    }

    /**
     * Translate a rectangular shape by the specified dx and dy value
     */
    public static void translate(RectangularShape shape, int dx, int dy)
    {
        shape.setFrame(shape.getX() + dx, shape.getY() + dy, shape.getWidth(), shape.getHeight());
    }

    /**
     * Translate a rectangular shape by the specified dx and dy value
     */
    public static void translate(RectangularShape shape, double dx, double dy)
    {
        shape.setFrame(shape.getX() + dx, shape.getY() + dy, shape.getWidth(), shape.getHeight());
    }

    /**
     * Permit to describe any PathIterator in a list of Shape which are returned
     * to the specified ShapeConsumer
     */
    public static boolean consumeShapeFromPath(PathIterator path, ShapeConsumer consumer)
    {
        final Line2D.Double line = new Line2D.Double();
        final QuadCurve2D.Double quadCurve = new QuadCurve2D.Double();
        final CubicCurve2D.Double cubicCurve = new CubicCurve2D.Double();
        double lastX, lastY, curX, curY, movX, movY;
        final double crd[] = new double[6];

        curX = 0;
        curY = 0;
        movX = 0;
        movY = 0;

        while (!path.isDone())
        {
            final int segType = path.currentSegment(crd);

            lastX = curX;
            lastY = curY;

            switch (segType)
            {
                case PathIterator.SEG_MOVETO:
                    curX = crd[0];
                    curY = crd[1];
                    movX = curX;
                    movY = curY;
                    break;

                case PathIterator.SEG_LINETO:
                    curX = crd[0];
                    curY = crd[1];
                    line.setLine(lastX, lastY, curX, curY);
                    if (!consumer.consume(line))
                        return false;
                    break;

                case PathIterator.SEG_QUADTO:
                    curX = crd[2];
                    curY = crd[3];
                    quadCurve.setCurve(lastX, lastY, crd[0], crd[1], curX, curY);
                    if (!consumer.consume(quadCurve))
                        return false;
                    break;

                case PathIterator.SEG_CUBICTO:
                    curX = crd[4];
                    curY = crd[5];
                    cubicCurve.setCurve(lastX, lastY, crd[0], crd[1], crd[2], crd[3], curX, curY);
                    if (!consumer.consume(cubicCurve))
                        return false;
                    break;

                case PathIterator.SEG_CLOSE:
                    line.setLine(lastX, lastY, movX, movY);
                    if (!consumer.consume(line))
                        return false;
                    break;
            }

            path.next();
        }

        return true;
    }

    /**
     * Return all PathAnchor points from the specified shape
     */
    public static ArrayList<PathAnchor2D> getAnchorsFromShape(Shape shape)
    {
        final PathIterator pathIt = shape.getPathIterator(null);
        final ArrayList<PathAnchor2D> result = new ArrayList<PathAnchor2D>();
        final double crd[] = new double[6];
        final double mov[] = new double[2];

        while (!pathIt.isDone())
        {
            final int segType = pathIt.currentSegment(crd);
            PathAnchor2D pt = null;

            switch (segType)
            {
                case PathIterator.SEG_MOVETO:
                    mov[0] = crd[0];
                    mov[1] = crd[1];

                case PathIterator.SEG_LINETO:
                    pt = new PathAnchor2D(crd[0], crd[1]);
                    break;

                case PathIterator.SEG_QUADTO:
                    pt = new PathAnchor2D(crd[0], crd[1], crd[2], crd[3]);
                    break;

                case PathIterator.SEG_CUBICTO:
                    pt = new PathAnchor2D(crd[0], crd[1], crd[2], crd[3], crd[4], crd[5]);
                    break;

                case PathIterator.SEG_CLOSE:
                    pt = new PathAnchor2D(mov[0], mov[1]);
                    // CLOSE points aren't visible
                    pt.setVisible(false);
                    break;
            }

            if (pt != null)
            {
                pt.setType(segType);
                result.add(pt);
            }

            pathIt.next();
        }

        return result;
    }

    /**
     * Update specified path from the specified list of PathAnchor2D
     */
    public static Path2D buildPathFromAnchors(Path2D path, ArrayList<PathAnchor2D> points)
    {
        path.reset();

        for (PathAnchor2D pt : points)
        {
            switch (pt.getType())
            {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_LINETO:
                    path.lineTo(pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_QUADTO:
                    path.quadTo(pt.getPosQExtX(), pt.getPosQExtY(), pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_CUBICTO:
                    path.curveTo(pt.getPosCExtX(), pt.getPosCExtY(), pt.getPosQExtX(), pt.getPosQExtY(), pt.getX(),
                            pt.getY());
                    break;

                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
            }
        }

        if (points.size() > 0)
            path.closePath();

        return path;
    }

    /**
     * Create and return a path from the specified list of PathAnchor2D
     */
    public static Path2D getPathFromAnchors(ArrayList<PathAnchor2D> points)
    {
        return buildPathFromAnchors(new Path2D.Double(), points);
    }

    /**
     * @deprecated uses {@link GraphicsUtil#drawPathIterator(PathIterator, Graphics2D)} instead
     */
    @Deprecated
    public static void drawFromPath(PathIterator path, final Graphics2D g)
    {
        GraphicsUtil.drawPathIterator(path, g);
    }

    /**
     * Return true if the specified PathIterator intersects with the specified Rectangle
     */
    public static boolean pathIntersects(PathIterator path, final Rectangle2D rect)
    {
        return !consumeShapeFromPath(path, new ShapeConsumer()
        {
            @Override
            public boolean consume(Shape shape)
            {
                if (shape.intersects(rect))
                    return false;

                return true;
            }
        });
    }

}
