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

import icy.painter.Anchor2D;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DLine} instead.
 */
@Deprecated
public class ROI2DLine extends plugins.kernel.roi.roi2d.ROI2DLine
{
    /**
     * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DLine.ROI2DLineAnchor2D} instead.
     */
    @Deprecated
    protected class ROI2DLineAnchor2D extends plugins.kernel.roi.roi2d.ROI2DLine.ROI2DLineAnchor2D
    {
        public ROI2DLineAnchor2D(Point2D position)
        {
            super(position);
        }
    }

    public ROI2DLine(Point2D pt1, Point2D pt2)
    {
        super(pt1, pt2);
    }

    public ROI2DLine(Line2D line)
    {
        super(line);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DLine(Point2D pt, boolean cm)
    {
        super(pt);
    }

    public ROI2DLine(Point2D pt)
    {
        super(pt);
    }

    public ROI2DLine(double x1, double y1, double x2, double y2)
    {
        super(x1, y1, x2, y2);
    }

    public ROI2DLine()
    {
        super();
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DLineAnchor2D(pos);
    }
}
