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

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class ROI2DLine extends ROI2DShape
{
    public static final String ID_PT1 = "pt1";
    public static final String ID_PT2 = "pt2";

    private final Anchor2D pt1;
    private final Anchor2D pt2;

    /**
     * 
     */
    public ROI2DLine(Point2D pt1, Point2D pt2, boolean cm)
    {
        super(new Line2D.Double());

        this.pt1 = createAnchor(pt1);
        this.pt2 = createAnchor(pt2);

        // add to the control point list
        controlPoints.add(this.pt1);
        controlPoints.add(this.pt2);

        this.pt1.addListener(this);
        this.pt2.addListener(this);

        // select the pt2 to size the line in "creation mode"
        if (cm)
            this.pt2.setSelected(true);
        setMousePos(pt2);

        updateShape();

        setName("Line2D");
    }

    /**
     * 
     */
    public ROI2DLine(Point2D pt1, Point2D pt2)
    {
        this(pt1, pt2, false);
    }

    /**
     * 
     */
    public ROI2DLine(Point2D pt, boolean cm)
    {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt, cm);
    }

    /**
     * 
     */
    public ROI2DLine(Point2D pt)
    {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt, false);
    }

    /**
     * 
     */
    public ROI2DLine()
    {
        this(new Point2D.Double());
    }

    public Line2D getLine()
    {
        return (Line2D) shape;
    }

    @Override
    protected void updateShape()
    {
        getLine().setLine(pt1.getPosition(), pt2.getPosition());
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
        delete();
        return true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            pt1.loadFromXML(XMLUtil.getElement(node, ID_PT1));
            pt2.loadFromXML(XMLUtil.getElement(node, ID_PT2));
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

        pt1.saveToXML(XMLUtil.setElement(node, ID_PT1));
        pt2.saveToXML(XMLUtil.setElement(node, ID_PT2));

        return true;
    }

}
