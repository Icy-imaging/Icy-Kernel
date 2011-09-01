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

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class ROI2DPoint extends ROI2DShape
{
    public static final String ID_POSITION = "position";

    private final Anchor2D position;

    /**
     * 
     */
    public ROI2DPoint(Point2D position, boolean cm)
    {
        super(new Rectangle2D.Double());

        this.position = createAnchor(position);

        // add to the control point list
        controlPoints.add(this.position);

        this.position.addListener(this);

        // select the point in "creation mode"
        if (cm)
            this.position.setSelected(true);
        setMousePos(position);

        updateShape();

        setName("Point2D");
    }

    /**
     * 
     */
    public ROI2DPoint(Point2D position)
    {
        this(position, false);
    }

    /**
     * 
     */
    public ROI2DPoint()
    {
        this(new Point2D.Double(), false);
    }

    public Rectangle2D getRectangle()
    {
        return (Rectangle2D) shape;
    }

    @Override
    protected void updateShape()
    {
        getRectangle().setFrameFromDiagonal(Math.floor(position.getX()), Math.floor(position.getY()),
                Math.ceil(position.getX()), Math.ceil(position.getY()));
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

            position.loadFromXML(XMLUtil.getElement(node, ID_POSITION));
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

        position.saveToXML(XMLUtil.setElement(node, ID_POSITION));

        return true;
    }

}
