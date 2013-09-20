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

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DPath} instead.
 */
@Deprecated
public class ROI2DPath extends plugins.kernel.roi.roi2d.ROI2DPath
{
    public ROI2DPath(Path2D path)
    {
        super(path);
    }

    public ROI2DPath(Shape shape)
    {
        super(shape);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPath(Point2D pt, boolean cm)
    {
        super(pt);
    }

    public ROI2DPath(Point2D position)
    {
        super(position);
    }

    public ROI2DPath()
    {
        super();
    }
}
