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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.painter.Anchor2D;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class ROI2DPolyLine extends ROI2DShape
{
    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    protected class ROI2DPolyLinePainter extends ROI2DShapePainter
    {
        @Override
        public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            super.mouseClick(e, imagePoint, canvas);

            if (EventUtil.isLeftMouseButton(e))
            {
                if (e.getClickCount() > 1)
                {
                    if (ROI2DPolyLine.this.selected)
                    {
                        ROI2DPolyLine.this.setSelected(false, false);
                        e.consume();
                    }
                }
            }
        }
    }

    /**
     * 
     */
    public ROI2DPolyLine(Point2D pt)
    {
        super(new Path2D.Double());

        // add points to list
        addPointAt(pt, true);

        setMousePos(pt);

        updateShape();

        setName("PolyLine2D");
    }

    /**
     * 
     */
    public ROI2DPolyLine()
    {
        this(new Point2D.Double());
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

            for (int i = 0; i < polygon.npoints; i++)
                addPoint(new Anchor2D(polygon.xpoints[i], polygon.ypoints[i], DEFAULT_SELECTED_COLOR, OVER_COLOR));
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#contains(double, double, double, double)
     */
    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return contains(new Rectangle2D.Double(x, y, w, h));
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#contains(double, double)
     */
    @Override
    public boolean contains(double x, double y)
    {
        return contains(new Point2D.Double(x, y));
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#contains(java.awt.geom.Point2D)
     */
    @Override
    public boolean contains(Point2D p)
    {
        // this ROI doesn't contains area
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#contains(java.awt.geom.Rectangle2D)
     */
    @Override
    public boolean contains(Rectangle2D r)
    {
        // this ROI doesn't contains area
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#intersects(double, double, double, double)
     */
    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2DShape#intersects(java.awt.geom.Rectangle2D)
     */
    @Override
    public boolean intersects(Rectangle2D r)
    {
        // just take care about path
        return ShapeUtil.pathIntersects(getPathIterator(null, 0.1), r);
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI2D#getAsBooleanMask(int, int, int, int)
     */
    @Override
    public boolean[] getAsBooleanMask(int x, int y, int w, int h)
    {
        // this ROI doesn't contains area
        return new boolean[w * h];
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

            final ArrayList<Node> nodesPoint = XMLUtil.getSubNodes(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
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
