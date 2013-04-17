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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane
 */
public class ROI2DRectangle extends ROI2DRectShape
{
    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DRectangle(Point2D topLeft, Point2D bottomRight, boolean cm)
    {
        this(topLeft, bottomRight);
    }

    public ROI2DRectangle(Point2D topLeft, Point2D bottomRight)
    {
        super(new Rectangle2D.Double(), topLeft, bottomRight);

        setName("Rectangle2D");
    }

    public ROI2DRectangle(double xmin, double ymin, double xmax, double ymax)
    {
        this(new Point2D.Double(xmin, ymin), new Point2D.Double(xmax, ymax));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DRectangle(Rectangle2D rectangle, boolean cm)
    {
        this(rectangle);
    }

    public ROI2DRectangle(Rectangle2D rectangle)
    {
        this(new Point2D.Double(rectangle.getMinX(), rectangle.getMinY()), new Point2D.Double(rectangle.getMaxX(),
                rectangle.getMaxY()));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DRectangle(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DRectangle(Point2D pt)
    {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt);
    }

    public ROI2DRectangle()
    {
        this(new Point2D.Double(), new Point2D.Double());
    }

    public Rectangle2D getRectangle()
    {
        return (Rectangle2D) shape;
    }

    public void setRectangle(Rectangle2D rectangle)
    {
        setBounds2D(rectangle);
    }

    @Override
    public double getPerimeter()
    {
        return getTotalDistance(getPoints());
    }

    @Override
    public double getVolume()
    {
        final Rectangle2D r = getRectangle();

        return r.getWidth() * r.getHeight();
    }
}
