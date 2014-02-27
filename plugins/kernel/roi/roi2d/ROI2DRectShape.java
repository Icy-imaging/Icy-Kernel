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
import icy.painter.Anchor2D;
import icy.painter.RectAnchor2D;
import icy.type.point.Point5D;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.w3c.dom.Node;

/**
 * Base class for rectangular shape ROI.
 * 
 * @author Stephane
 */
public abstract class ROI2DRectShape extends ROI2DShape
{
    protected class ROI2DRectAnchor2D extends RectAnchor2D
    {
        public ROI2DRectAnchor2D(Point2D position, Color color, Color selectedColor)
        {
            super(position, color, selectedColor);
        }

        @Override
        protected Anchor2D getOppositePoint()
        {
            if (this == topLeft)
                return bottomRight;
            if (this == topRight)
                return bottomLeft;
            if (this == bottomLeft)
                return topRight;

            return topLeft;
        };
    }

    public static final String ID_TOPLEFT = "top_left";
    public static final String ID_BOTTOMRIGHT = "bottom_right";

    protected final Anchor2D topLeft;
    protected final Anchor2D topRight;
    protected final Anchor2D bottomLeft;
    protected final Anchor2D bottomRight;

    /**
     * 
     */
    public ROI2DRectShape(RectangularShape shape, Point2D topLeft, Point2D bottomRight)
    {
        super(shape);

        this.topLeft = createAnchor(topLeft);
        this.topRight = createAnchor(bottomRight.getX(), topLeft.getY());
        this.bottomLeft = createAnchor(topLeft.getX(), bottomRight.getY());
        this.bottomRight = createAnchor(bottomRight);

        // add to the control point list (important to add them in clockwise order)
        controlPoints.add(this.topLeft);
        controlPoints.add(this.topRight);
        controlPoints.add(this.bottomRight);
        controlPoints.add(this.bottomLeft);

        this.topLeft.addOverlayListener(this);
        this.topLeft.addPositionListener(this);
        this.topRight.addOverlayListener(this);
        this.topRight.addPositionListener(this);
        this.bottomLeft.addOverlayListener(this);
        this.bottomLeft.addPositionListener(this);
        this.bottomRight.addOverlayListener(this);
        this.bottomRight.addPositionListener(this);

        // select the bottom right point by default for interactive mode
        this.bottomRight.setSelected(true);
        getOverlay().setMousePos(new Point5D.Double(bottomRight.getX(), bottomRight.getY(), -1d, -1d, -1d));

        updateShape();
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DRectAnchor2D(pos, getColor(), getFocusedColor());
    }

    protected RectangularShape getRectangularShape()
    {
        return (RectangularShape) shape;
    }

    @Override
    public boolean canSetBounds()
    {
        return true;
    }

    @Override
    public void setBounds2D(Rectangle2D bounds)
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
    protected boolean removePoint(IcyCanvas canvas, Anchor2D pt)
    {
        // this ROI doesn't support point remove
        return false;
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

            topLeft.loadPositionFromXML(XMLUtil.getElement(node, ID_TOPLEFT));
            bottomRight.loadPositionFromXML(XMLUtil.getElement(node, ID_BOTTOMRIGHT));
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

        topLeft.savePositionToXML(XMLUtil.setElement(node, ID_TOPLEFT));
        bottomRight.savePositionToXML(XMLUtil.setElement(node, ID_BOTTOMRIGHT));

        return true;
    }

}
