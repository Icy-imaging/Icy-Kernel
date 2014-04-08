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
package plugins.kernel.roi.roi2d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.painter.Anchor2D;
import icy.painter.LineAnchor2D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class ROI2DPolyLine extends ROI2DShape
{
    protected class ROI2DPolyLineAnchor2D extends LineAnchor2D
    {
        public ROI2DPolyLineAnchor2D(Point2D position, Color color, Color selectedColor)
        {
            super(position, color, selectedColor);
        }

        @Override
        protected Anchor2D getPreviousPoint()
        {
            final int ind = controlPoints.indexOf(this);

            if (ind == 0)
            {
                if (controlPoints.size() > 1)
                    return controlPoints.get(1);

                return null;
            }

            if (ind != -1)
                return controlPoints.get(ind - 1);

            return null;
        }
    }

    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    public class ROI2DPolyLinePainter extends ROI2DShapePainter
    {
        @Override
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                final Graphics2D g2 = (Graphics2D) g.create();
                final Rectangle2D bounds = shape.getBounds2D();
                // trivial paint optimization
                final boolean shapeVisible = GraphicsUtil.isVisible(g2, bounds);
                final boolean small;
                final boolean tiny;

                // disable LOD when creating the ROI
                if (isCreating())
                {
                    small = false;
                    tiny = false;
                }
                else
                {
                    final AffineTransform trans = g.getTransform();
                    final double scale = Math.max(trans.getScaleX(), trans.getScaleY());
                    final double size = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight());
                    small = size < LOD_SMALL;
                    tiny = size < LOD_TINY;
                }

                // simplified draw
                if (small)
                {
                    // trivial paint optimization
                    if (shapeVisible)
                    {
                        // draw shape (simplified version)
                        g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                        g2.setColor(getDisplayColor());
                        g2.draw(shape);

                        if (!tiny)
                        {
                            // draw simplified control points
                            if (isSelected() && !isReadOnly())
                            {
                                final int ray = (int) canvas.canvasToImageDeltaX(2);

                                for (Anchor2D pt : controlPoints)
                                {
                                    if (pt.isVisible())
                                    {
                                        if (pt.isSelected())
                                            g2.setColor(pt.getSelectedColor());
                                        else
                                            g2.setColor(pt.getColor());

                                        g2.fillRect((int) pt.getPositionX() - ray, (int) pt.getPositionY() - ray,
                                                ray * 2, ray * 2);
                                    }
                                }
                            }
                        }
                    }
                }
                // normal draw
                else
                {
                    if (shapeVisible)
                    {
                        if (isSelected())
                        {
                            // just draw plain object shape without border
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                            g2.setColor(getDisplayColor());
                            g2.draw(shape);
                        }
                        else
                        {
                            // draw border
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                            g2.setColor(Color.black);
                            g2.draw(shape);
                            // draw shape
                            g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                            g2.setColor(getDisplayColor());
                            g2.draw(shape);
                        }
                    }

                    if (isSelected() && !isReadOnly())
                    {
                        // draw control point if selected
                        for (Anchor2D pt : controlPoints)
                            pt.paint(g2, sequence, canvas);
                    }
                }

                g2.dispose();
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPolyLine(Point2D pt, boolean cm)
    {
        this(pt);
    }

    /**
     * 
     */
    public ROI2DPolyLine(Point2D pt)
    {
        super(new Path2D.Double());

        // add points to list
        final Anchor2D anchor = createAnchor(pt);
        // just add the new point at last position
        addPoint(anchor);
        // always select
        anchor.setSelected(true);

        // getOverlay().setMousePos(new Point5D.Double(pt.getX(), pt.getY(), -1d, -1d, -1d));

        updateShape();

        // set name and icon
        setName("PolyLine2D");
        setIcon(ResourceUtil.ICON_ROI_POLYLINE);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPolyLine(Point5D pt)
    {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DPolyLine(Polygon polygon)
    {
        this(new Point2D.Double());

        setPolygon(polygon);
    }

    public ROI2DPolyLine(List<Point2D> points)
    {
        this(new Point2D.Double());

        setPoints(points);
    }

    public ROI2DPolyLine()
    {
        this(new Point2D.Double());
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DPolyLineAnchor2D(pos, getColor(), getFocusedColor());
    }

    @Override
    protected ROI2DPolyLinePainter createPainter()
    {
        return new ROI2DPolyLinePainter();
    }

    protected Path2D getPath()
    {
        return (Path2D) shape;
    }

    public void setPoints(List<Point2D> pts)
    {
        beginUpdate();
        try
        {
            removeAllPoint();

            for (Point2D pt : pts)
                addPoint(new Anchor2D(pt.getX(), pt.getY()));
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #setPoints(ArrayList)} instead.
     */
    @Deprecated
    public void setPoints(ArrayList<Point2D> pts)
    {
        setPoints((List<Point2D>) pts);
    }

    public Polygon getPolygon()
    {
        final Polygon result = new Polygon();

        for (Anchor2D point : controlPoints)
            result.addPoint((int) point.getX(), (int) point.getY());

        return result;
    }

    public void setPolygon(Polygon polygon)
    {
        beginUpdate();
        try
        {
            removeAllPoint();

            final Color color = getColor();
            final Color focusedColor = getFocusedColor();

            for (int i = 0; i < polygon.npoints; i++)
                addPoint(new Anchor2D(polygon.xpoints[i], polygon.ypoints[i], color, focusedColor));
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    protected double getTotalDistance(List<Point2D> points)
    {
        // by default the total length don't need last point connection
        return super.getTotalDistance(points, false);
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        return getTotalDistance(getPoints());
    }

    @Override
    public double computeNumberOfPoints()
    {
        return 0d;
    }

    @Override
    protected void updateShape()
    {
        final Path2D path = getPath();

        path.reset();

        // initial move
        if (controlPoints.size() > 0)
        {
            final Point2D pos = controlPoints.get(0).getPosition();
            path.moveTo(pos.getX(), pos.getY());
        }
        // special case we have only one point
        if (controlPoints.size() == 1)
        {
            final Point2D pos = controlPoints.get(0).getPosition();
            path.lineTo(pos.getX(), pos.getY());
        }
        else
        {
            // lines
            for (int i = 1; i < controlPoints.size(); i++)
            {
                final Point2D pos = controlPoints.get(i).getPosition();
                path.lineTo(pos.getX(), pos.getY());
            }
        }

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        // this ROI doesn't contains anything
        return false;
    }

    @Override
    public boolean contains(double x, double y)
    {
        // this ROI doesn't contains anything
        return false;
    }

    @Override
    public boolean contains(Point2D p)
    {
        // this ROI doesn't contains anything
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        // this ROI doesn't contains anything
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        // just take care about path
        return ShapeUtil.pathIntersects(getPathIterator(null, 0.1), r);
    }

    @Override
    public boolean[] getBooleanMask(int x, int y, int w, int h, boolean inclusive)
    {
        if ((w <= 0) || (h <= 0))
            return new boolean[0];

        // this ROI doesn't contains area
        if (!inclusive)
            return new boolean[w * h];

        final BufferedImage maskImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D g = maskImg.createGraphics();

        // draw shape in image
        g.setColor(Color.white);
        g.translate(-x, -y);
        g.draw(shape);
        g.dispose();

        // use the image to define the mask
        final byte[] maskData = ((DataBufferByte) maskImg.getRaster().getDataBuffer()).getData();
        final boolean[] result = new boolean[w * h];

        for (int i = 0; i < result.length; i++)
            result[i] = (maskData[i] != 0);

        return result;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            removeAllPoint();

            final ArrayList<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null)
            {
                for (Node n : nodesPoint)
                {
                    final Anchor2D pt = new Anchor2D();
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        final Element dependances = XMLUtil.setElement(node, ID_POINTS);
        for (Anchor2D pt : controlPoints)
            pt.savePositionToXML(XMLUtil.addElement(dependances, ID_POINT));

        return true;
    }
}
