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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public abstract class ROI2DRectShape extends ROI2DShape
{
    public static final String ID_TOPLEFT = "top_left";
    public static final String ID_BOTTOMRIGHT = "bottom_right";

    protected final Anchor2D topLeft;
    protected final Anchor2D topRight;
    protected final Anchor2D bottomLeft;
    protected final Anchor2D bottomRight;

    /**
     * 
     */
    public ROI2DRectShape(RectangularShape shape, Point2D topLeft, Point2D bottomRight, boolean cm)
    {
        super(shape);

        this.topLeft = createAnchor(topLeft);
        this.topRight = createAnchor(bottomRight.getX(), topLeft.getY());
        this.bottomLeft = createAnchor(topLeft.getX(), bottomRight.getY());
        this.bottomRight = createAnchor(bottomRight);

        // add to the control point list
        controlPoints.add(this.topLeft);
        controlPoints.add(this.topRight);
        controlPoints.add(this.bottomLeft);
        controlPoints.add(this.bottomRight);

        this.topLeft.addListener(this);
        this.topRight.addListener(this);
        this.bottomLeft.addListener(this);
        this.bottomRight.addListener(this);

        // select the bottom right point as we use it to size the ellipse in "creation mode"
        if (cm)
            this.bottomRight.setSelected(true);
        setMousePos(bottomRight);

        updateShape();
    }

    protected RectangularShape getRectangularShape()
    {
        return (RectangularShape) shape;
    }

    public void setRectangle(Rectangle2D bounds)
    {
        beginUpdate();
        try
        {
            // set anchors (only 2 significants anchors need to be adjusted)
            topLeft.setPosition(bounds.getMinX(), bounds.getMinY());
            bottomRight.setPosition(bounds.getMaxX(), bounds.getMaxY());
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    protected void updateShape()
    {
        getRectangularShape().setFrameFromDiagonal(topLeft.getPosition(), bottomRight.getPosition());

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
        delete();
        return true;
    }

    @Override
    public void positionChanged(Anchor2D source)
    {
        // adjust dependents anchors
        if (source == topLeft)
        {
            bottomLeft.setX(topLeft.getX());
            topRight.setY(topLeft.getY());
        }
        else if (source == topRight)
        {
            bottomRight.setX(topRight.getX());
            topLeft.setY(topRight.getY());
        }
        else if (source == bottomLeft)
        {
            topLeft.setX(bottomLeft.getX());
            bottomRight.setY(bottomLeft.getY());
        }
        else if (source == bottomRight)
        {
            topRight.setX(bottomRight.getX());
            bottomLeft.setY(bottomRight.getY());
        }

        super.positionChanged(source);
    }

    @Override
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            // translate (only 2 significants anchors need to be adjusted)
            topLeft.translate(dx, dy);
            bottomRight.translate(dx, dy);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            topLeft.loadFromXML(XMLUtil.getElement(node, ID_TOPLEFT));
            bottomRight.loadFromXML(XMLUtil.getElement(node, ID_BOTTOMRIGHT));
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

        topLeft.saveToXML(XMLUtil.setElement(node, ID_TOPLEFT));
        bottomRight.saveToXML(XMLUtil.setElement(node, ID_BOTTOMRIGHT));

        return true;
    }

}
