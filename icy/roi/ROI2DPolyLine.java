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
package icy.roi;

import icy.painter.Anchor2D;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @deprecated Use {@link icy.roi.roi2d.ROI2DPolyLine} instead.
 */
@Deprecated
public class ROI2DPolyLine extends icy.roi.roi2d.ROI2DPolyLine
{
    /**
     * @deprecated Use {@link icy.roi.roi2d.ROI2DPolyLine.ROI2DPolyLineAnchor2D} instead.
     */
    @Deprecated
    protected class ROI2DPolyLineAnchor2D extends icy.roi.roi2d.ROI2DPolyLine.ROI2DPolyLineAnchor2D
    {
        public ROI2DPolyLineAnchor2D(Point2D position, Color color, Color selectedColor)
        {
            super(position, color, selectedColor);
        }
    }

    /**
     * @deprecated Use {@link icy.roi.roi2d.ROI2DPolyLine.ROI2DPolyLinePainter} instead.
     */
    @Deprecated
    protected class ROI2DPolyLinePainter extends icy.roi.roi2d.ROI2DPolyLine.ROI2DPolyLinePainter
    {

    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPolyLine(Point2D pt, boolean cm)
    {
        super(pt);
    }

    /**
     * 
     */
    public ROI2DPolyLine(Point2D pt)
    {
        super(pt);
    }

    public ROI2DPolyLine(Polygon polygon)
    {
        super(new Point2D.Double());

        setPolygon(polygon);
    }

    public ROI2DPolyLine(List<Point2D> points)
    {
        super(points);
    }

    public ROI2DPolyLine()
    {
        super();
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DPolyLineAnchor2D(pos, getColor(), getFocusedColor());
    }

    @Override
    protected ROI2DPolyLinePainter createPainter()
    {
        return new ROI2DPolyLinePainter();
    }
}
