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

import icy.painter.Anchor2D;
import icy.painter.LineAnchor2D;
import icy.resource.ResourceUtil;
import icy.roi.Polygon2D;
import icy.type.point.Point5D;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ROI 2D polygon class.
 * 
 * @author Stephane
 */
public class ROI2DPolygon extends ROI2DShape
{
    protected class ROI2DPolygonAnchor2D extends LineAnchor2D
    {
        public ROI2DPolygonAnchor2D(Point2D position, Color color, Color selectedColor)
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
    public ROI2DPolygon(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DPolygon(Point2D pt)
    {
        super(new Polygon2D());

        // add points to list
        final Anchor2D anchor = createAnchor(pt);
        // just add the new point at last position
        addPoint(anchor);
        // always select
        anchor.setSelected(true);

        // getOverlay().setMousePos(new Point5D.Double(pt.getX(), pt.getY(), -1d, -1d, -1d));

        updateShape();

        // set name and icon
        setName("Polygon2D");
        setIcon(ResourceUtil.ICON_ROI_POLYGON);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPolygon(Point5D pt)
    {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DPolygon(List<Point2D> points)
    {
        this(new Point2D.Double());

        setPoints(points);
    }

    public ROI2DPolygon(Polygon polygon)
    {
        this(new Point2D.Double());

        setPolygon(polygon);
    }

    public ROI2DPolygon()
    {
        this(new Point2D.Double());
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DPolygonAnchor2D(pos, getColor(), getFocusedColor());
    }

    /**
     * @deprecated Use {@link #getPolygon2D()} instead
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

    public Polygon2D getPolygon2D()
    {
        return (Polygon2D) shape;
    }

    public void setPolygon2D(Polygon2D polygon2D)
    {
        beginUpdate();
        try
        {
            removeAllPoint();
            for (int i = 0; i < polygon2D.npoints; i++)
                addNewPoint(new Point2D.Double(polygon2D.xpoints[i], polygon2D.ypoints[i]), false);
        }
        finally
        {
            endUpdate();
        }
    }

    public Polygon getPolygon()
    {
        return getPolygon2D().getPolygon();
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
    protected void updateShape()
    {
        final int len = controlPoints.size();
        final double ptsX[] = new double[len];
        final double ptsY[] = new double[len];

        for (int i = 0; i < len; i++)
        {
            final Anchor2D pt = controlPoints.get(i);

            ptsX[i] = pt.getX();
            ptsY[i] = pt.getY();
        }

        final Polygon2D polygon2d = getPolygon2D();

        // we can have a problem here if we try to redraw while we are modifying the polygon points
        synchronized (polygon2d)
        {
            polygon2d.npoints = len;
            polygon2d.xpoints = ptsX;
            polygon2d.ypoints = ptsY;
            polygon2d.calculatePath();
        }

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        return getTotalDistance(getPoints());
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

        final Element dependances = XMLUtil.setElement(node, ID_POINTS);
        for (Anchor2D pt : controlPoints)
            pt.savePositionToXML(XMLUtil.addElement(dependances, ID_POINT));

        return true;
    }
}
