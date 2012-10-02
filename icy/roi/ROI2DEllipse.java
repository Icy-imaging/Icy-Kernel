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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane
 */
public class ROI2DEllipse extends ROI2DRectShape
{
    /**
     * 
     */
    public ROI2DEllipse(Point2D topLeft, Point2D bottomRight, boolean cm)
    {
        super(new Ellipse2D.Double(), topLeft, bottomRight, cm);

        setName("Ellipse2D");
    }

    /**
     * 
     */
    public ROI2DEllipse(Point2D topLeft, Point2D bottomRight)
    {
        this(topLeft, bottomRight, false);
    }

    public ROI2DEllipse(Rectangle2D rectangle, boolean cm)
    {
        this(new Point2D.Double(rectangle.getMinX(), rectangle.getMinY()), new Point2D.Double(rectangle.getMaxX(),
                rectangle.getMaxY()), cm);
    }

    public ROI2DEllipse(Rectangle2D rectangle)
    {
        this(rectangle, false);
    }

    /**
     * 
     */
    public ROI2DEllipse(Point2D pt, boolean cm)
    {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt, cm);
    }

    /**
     * 
     */
    public ROI2DEllipse(Point2D pt)
    {
        this(pt, false);
    }

    /**
     * 
     */
    public ROI2DEllipse()
    {
        this(new Point2D.Double());
    }

    public Ellipse2D getEllipse()
    {
        return (Ellipse2D) shape;
    }

    public void setEllipse(Ellipse2D ellipse)
    {
        setBounds2D(ellipse.getBounds2D());
    }
}
