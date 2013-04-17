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

import icy.painter.Anchor2D;
import icy.util.XMLUtil;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Node;

/**
 * ROI 2D Point class.<br>
 * Define a single point ROI<br>
 * 
 * @author Stephane
 */
public class ROI2DPoint extends ROI2DShape
{
    public static final String ID_POSITION = "position";

    private final Anchor2D position;

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPoint(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DPoint(Point2D position)
    {
        super(new Line2D.Double());

        this.position = createAnchor(position);

        // add to the control point list
        controlPoints.add(this.position);

        this.position.addOverlayListener(this);
        this.position.addAnchorListener(this);

        // select the point for "interactive" mode
        this.position.setSelected(true);
        setMousePos(position);

        updateShape();

        setName("Point2D");
    }

    public ROI2DPoint(double x, double y)
    {
        this(new Point2D.Double(x, y));
    }

    public ROI2DPoint()
    {
        this(new Point2D.Double());
    }

    /**
     * @deprecated Use {@link #getLine()} instead.
     */
    @Deprecated
    public Rectangle2D getRectangle()
    {
        final Point2D pt = getPoint();
        return new Rectangle2D.Double(pt.getX(), pt.getY(), 0d, 0d);
    }

    public Line2D getLine()
    {
        return (Line2D) shape;
    }

    public Point2D getPoint()
    {
        return position.getPosition();
    }

    @Override
    protected void updateShape()
    {
        final Point2D pt = getPoint();
        final double x = pt.getX();
        final double y = pt.getY();

        getLine().setLine(x, y, x, y);

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean canAddPoint()
    {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    protected boolean removePoint(Anchor2D pt)
    {
        // remove point on this ROI remove the ROI
        remove();
        return true;
    }

    @Override
    public double getPerimeter()
    {
        return 0d;
    }

    @Override
    public double getVolume()
    {
        return 0d;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            position.loadFromXML(XMLUtil.getElement(node, ID_POSITION));
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

        position.saveToXML(XMLUtil.setElement(node, ID_POSITION));

        return true;
    }
}
