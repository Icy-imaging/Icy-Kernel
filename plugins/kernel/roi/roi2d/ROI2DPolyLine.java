/*
 * Copyright 2010-2015 Institut Pasteur.
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

import icy.painter.Anchor2D;
import icy.painter.LineAnchor2D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.type.geom.Polyline2D;
import icy.type.point.Point2DUtil;
import icy.type.point.Point5D;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
        super(new Polyline2D());

        final Anchor2D point = createAnchor(pt);
        point.setSelected(true);
        addPoint(point);

        // set icon (default name is defined by getDefaultName()) 
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

    public ROI2DPolyLine(Polyline2D polyline)
    {
        this(new Point2D.Double());

        setPolyline2D(polyline);
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
    public String getDefaultName()
    {
        return "PolyLine2D";
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DPolyLineAnchor2D(pos, getColor(), getFocusedColor());
    }

    // @Override
    // protected ROI2DPolyLinePainter createPainter()
    // {
    // return new ROI2DPolyLinePainter();
    // }

    /**
     * @deprecated Use {@link #getPolyline2D()} instead
     */
    @Deprecated
    protected Path2D getPath()
    {
        return new Path2D.Double(shape);
    }

    public void setPoints(List<Point2D> pts)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (Point2D pt : pts)
                addNewPoint(pt, false);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #setPoints(List)} instead.
     */
    @Deprecated
    public void setPoints(ArrayList<Point2D> pts)
    {
        setPoints((List<Point2D>) pts);
    }

    public Polyline2D getPolyline2D()
    {
        return (Polyline2D) shape;
    }

    public void setPolyline2D(Polyline2D polyline2D)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (int i = 0; i < polyline2D.npoints; i++)
                addNewPoint(new Point2D.Double(polyline2D.xpoints[i], polyline2D.ypoints[i]), false);
        }
        finally
        {
            endUpdate();
        }
    }

    public Polygon getPolygon()
    {
        return getPolyline2D().getPolygon2D().getPolygon();
    }

    public void setPolygon(Polygon polygon)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (int i = 0; i < polygon.npoints; i++)
                addNewPoint(new Point2D.Double(polygon.xpoints[i], polygon.ypoints[i]), false);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean contains(double x, double y)
    {
        return false;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return false;
    }

    @Override
    public boolean contains(ROI roi)
    {
        return false;
    }

    @Override
    protected double getTotalDistance(List<Point2D> points, double factorX, double factorY)
    {
        // for polyline the total length don't need last point connection
        return Point2DUtil.getTotalDistance(points, factorX, factorY, false);
    }

    @Override
    public double computeNumberOfPoints()
    {
        return 0d;
    }

    @Override
    protected void updateShape()
    {
        final int len;
        final double[] ptsX;
        final double[] ptsY;

        synchronized (controlPoints)
        {
            len = controlPoints.size();
            ptsX = new double[len];
            ptsY = new double[len];

            for (int i = 0; i < len; i++)
            {
                final Anchor2D pt = controlPoints.get(i);

                ptsX[i] = pt.getX();
                ptsY[i] = pt.getY();
            }
        }

        final Polyline2D polyline2d = getPolyline2D();

        // we can have a problem here if we try to redraw while we are modifying the polygon points
        synchronized (polyline2d)
        {
            polyline2d.npoints = len;
            polyline2d.xpoints = ptsX;
            polyline2d.ypoints = ptsY;
            polyline2d.calculatePath();
        }

        // call super method after shape has been updated
        super.updateShape();
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

            final List<Node> pointsNode = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (pointsNode != null)
            {
                for (Node n : pointsNode)
                {
                    final Anchor2D pt = createAnchor(new Point2D.Double());
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

        final Element pointsNode = XMLUtil.setElement(node, ID_POINTS);
        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                pt.savePositionToXML(XMLUtil.addElement(pointsNode, ID_POINT));
        }

        return true;
    }
}
