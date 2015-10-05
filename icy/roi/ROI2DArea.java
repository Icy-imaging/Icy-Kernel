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

import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DArea} instead.
 */
@Deprecated
public class ROI2DArea extends plugins.kernel.roi.roi2d.ROI2DArea
{
    /**
     * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DArea.ROI2DAreaPainter} instead.
     */
    @Deprecated
    public class ROI2DAreaPainter extends plugins.kernel.roi.roi2d.ROI2DArea.ROI2DAreaPainter
    {
    }

    /**
     * Create a ROI2D Area type from the specified {@link BooleanMask2D}.
     */
    public ROI2DArea()
    {
        super();
    }

    /**
     * Create a ROI2D Area type from the specified {@link BooleanMask2D}.
     */
    public ROI2DArea(BooleanMask2D mask)
    {
        super(mask);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DArea(Point2D position, boolean cm)
    {
        super(position);
    }

    /**
     * Create a ROI2D Area type with a single point.
     */
    public ROI2DArea(Point2D position)
    {
        super(position);
    }

    @Override
    protected ROI2DAreaPainter createPainter()
    {
        return new ROI2DAreaPainter();
    }
}
