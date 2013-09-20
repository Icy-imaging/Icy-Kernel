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
import icy.util.ShapeUtil;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.ShapeUtil.ShapeOperation;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DShape} instead.
 */
@Deprecated
public abstract class ROI2DShape extends plugins.kernel.roi.roi2d.ROI2DShape
{
    /**
     * @deprecated Use {@link ROIUtil#merge(List, BooleanOperator)} instead.
     */
    @Deprecated
    public static ROI2DPath merge(ROI2DShape[] rois, ShapeOperation operation)
    {
        return merge(Arrays.asList(rois), operation.getBooleanOperator());
    }

    /**
     * @deprecated Use {@link ROIUtil#merge(List, BooleanOperator)} instead.
     */
    @Deprecated
    public static ROI2DPath merge(List<ROI2DShape> rois, BooleanOperator operator)
    {
        final List<Shape> shapes = new ArrayList<Shape>(rois.size());

        for (ROI2DShape roi : rois)
            shapes.add(roi.getShape());

        final ROI2DPath result = new ROI2DPath(ShapeUtil.merge(shapes, operator));

        switch (operator)
        {
            case OR:
                result.setName("Union");
                break;
            case AND:
                result.setName("Intersection");
                break;
            case XOR:
                result.setName("Exclusive union");
                break;
            default:
                result.setName("Merge");
                break;
        }

        return result;
    }

    /**
     * @deprecated Use {@link ROI#getSubtraction(ROI)} instead.
     */
    @Deprecated
    public static ROI2DPath subtract(ROI2DShape roi1, ROI2DShape roi2)
    {
        final ROI2DPath result = new ROI2DPath(ShapeUtil.subtract(roi1, roi2));

        result.setName("Substraction");

        return result;
    }

    /**
     * @deprecated Use {@link ROI#getSubtraction(ROI)} instead.
     */
    @Deprecated
    public static ROI2DPath substract(ROI2DShape roi1, ROI2DShape roi2)
    {
        return subtract(roi1, roi2);
    }

    /**
     * @deprecated Use {@link plugins.kernel.roi.roi2d.ROI2DShape.ROI2DShapePainter} instead.
     */
    @Deprecated
    protected class ROI2DShapePainter extends plugins.kernel.roi.roi2d.ROI2DShape.ROI2DShapePainter
    {
        public ROI2DShapePainter()
        {
            super();
        }
    }

    public ROI2DShape(Shape shape)
    {
        super(shape);
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROI2DShapePainter();
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new Anchor2D(pos.getX(), pos.getY(), getColor(), getFocusedColor());
    }

    @Override
    protected Anchor2D createAnchor(double x, double y)
    {
        return createAnchor(new Point2D.Double(x, y));
    }
}
