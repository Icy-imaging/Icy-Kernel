/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.roi;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DEllipse} instead.
 */
@Deprecated
public class ROI2DEllipse extends plugins.kernel.roi.roi2d.ROI2DEllipse
{
    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DEllipse(Point2D topLeft, Point2D bottomRight, boolean cm)
    {
        super(topLeft, bottomRight);
    }

    public ROI2DEllipse(Point2D topLeft, Point2D bottomRight)
    {
        super(topLeft, bottomRight);
    }

    /**
     * Create a ROI ellipse from its rectangular bounds.
     */
    public ROI2DEllipse(double xmin, double ymin, double xmax, double ymax)
    {
        super(xmin, ymin, xmax, ymax);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DEllipse(Rectangle2D rectangle, boolean cm)
    {
        super(rectangle);
    }

    public ROI2DEllipse(Rectangle2D rectangle)
    {
        super(rectangle);
    }

    public ROI2DEllipse(Ellipse2D ellipse)
    {
        super(ellipse);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DEllipse(Point2D pt, boolean cm)
    {
        super(pt);
    }

    public ROI2DEllipse(Point2D pt)
    {
        super(pt);
    }

    public ROI2DEllipse()
    {
        super();
    }
}
