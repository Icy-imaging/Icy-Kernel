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
import icy.util.XMLUtil;

import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class ROI2DPolygon extends ROI2DShape
{
    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    protected class ROI2DPolygonPainter extends ROI2DShapePainter
    {
        @Override
        public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            super.mouseClick(e, imagePoint, canvas);

            if (EventUtil.isLeftMouseButton(e))
            {
                if (e.getClickCount() > 1)
                {
                    if (ROI2DPolygon.this.selected)
                    {
                        ROI2DPolygon.this.setSelected(false, false);
                        e.consume();
                    }
                }
            }
        }
    }

    /**
     * 
     */
    public ROI2DPolygon(Point2D pt)
    {
        super(new Polygon());

        // add point to list
        addPointAt(pt, true);

        setMousePos(pt);

        updateShape();

        setName("Polygon2D");
    }

    /**
     * 
     */
    public ROI2DPolygon()
    {
        this(new Point2D.Double());
    }

    @Override
    protected ROI2DPolygonPainter createPainter()
    {
        return new ROI2DPolygonPainter();
    }

    public Polygon getPolygon()
    {
        return (Polygon) shape;
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
        final Polygon polygon = getPolygon();

        polygon.reset();
        // add points
        for (int i = 0; i < controlPoints.size(); i++)
        {
            final Point2D pos = controlPoints.get(i).getPosition();
            polygon.addPoint((int) Math.round(pos.getX()), (int) Math.round(pos.getY()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#loadFromXML(org.w3c.dom.Node)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#saveToXML(org.w3c.dom.Node)
     */
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
