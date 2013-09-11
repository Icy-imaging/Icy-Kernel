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
package icy.roi.roi2d;

import icy.painter.Anchor2D;
import icy.painter.LineAnchor2D;
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
        // use Path2D shape which allow double coordinates
        super(new Path2D.Double());

        // add points to list
        final Anchor2D anchor = createAnchor(pt);
        // just add the new point at last position
        addPoint(anchor);
        // always select
        anchor.setSelected(true);

        setMousePos(pt);

        updateShape();

        setName("Polygon2D");
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPolygon(Point5D pt)
    {
        this(pt.toPoint2D());
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
     * @deprecated Use {@link #setPoints(List)} instead.
     */
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
    protected void updateShape()
    {
        final Path2D path = getPath();
        Point2D pos;

        path.reset();

        // initial move
        if (controlPoints.size() > 0)
        {
            pos = controlPoints.get(0).getPosition();
            path.moveTo(pos.getX(), pos.getY());

            // special case we have only one point
            if (controlPoints.size() == 1)
            {
                pos = controlPoints.get(0).getPosition();
                path.lineTo(pos.getX(), pos.getY());
            }
            else
            {
                // lines
                for (int i = 1; i < controlPoints.size(); i++)
                {
                    pos = controlPoints.get(i).getPosition();
                    path.lineTo(pos.getX(), pos.getY());
                }

                path.closePath();
            }
        }

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public double getPerimeter()
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
                    final Anchor2D pt = new Anchor2D();
                    pt.loadFromXML(n);
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
            pt.saveToXML(XMLUtil.addElement(dependances, ID_POINT));

        return true;
    }
}
