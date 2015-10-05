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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DRectShape} instead.
 */
@Deprecated
public abstract class ROI2DRectShape extends plugins.kernel.roi.roi2d.ROI2DRectShape
{
    /**
     * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DRectShape.ROI2DRectAnchor2D} instead.
     */
    @Deprecated
    protected class ROI2DRectAnchor2D extends plugins.kernel.roi.roi2d.ROI2DRectShape.ROI2DRectAnchor2D
    {
        public ROI2DRectAnchor2D(Point2D position, Color color, Color selectedColor)
        {
            super(position, color, selectedColor);
        }
    }

    /**
     * 
     */
    public ROI2DRectShape(RectangularShape shape, Point2D topLeft, Point2D bottomRight)
    {
        super(shape, topLeft, bottomRight);
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new ROI2DRectAnchor2D(pos, getColor(), getFocusedColor());
    }
}
