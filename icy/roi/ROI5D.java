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

import icy.canvas.IcyCanvas;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;
import java.util.List;

/**
 * 5D ROI base class.
 */
public abstract class ROI5D extends ROI
{
    /**
     * @deprecated Use {@link ROI5D#getROI5DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI5D> getROI5DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    /**
     * Return all 5D ROI from the ROI list
     */
    public static List<ROI5D> getROI5DList(List<ROI> rois)
    {
        final List<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    public ROI5D()
    {
        super();
    }

    @Override
    final public int getDimension()
    {
        return 5;
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return true;
    }

    /**
     * Returns an integer {@link Rectangle5D} that completely encloses the <code>ROI</code>. Note
     * that there is no guarantee that the returned <code>Rectangle5D</code> is the smallest
     * bounding box that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely
     * within the indicated <code>Rectangle5D</code>. The returned <code>Rectangle5D</code> might
     * also fail to completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the
     * limited range of the integer data type. The <code>getBounds5D</code> method generally returns
     * a tighter bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle5D</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle5D.Integer getBounds()
    {
        return getBounds5D().getBoundsInt();
    }

    /**
     * Returns the integer ROI position which normally correspond to the <i>minimum</i> point of the
     * ROI bounds.
     * 
     * @see #getBounds()
     */
    public Point5D.Integer getPosition()
    {
        final Rectangle5D.Integer bounds = getBounds();
        return new Point5D.Integer(bounds.x, bounds.y, bounds.z, bounds.t, bounds.c);
    }
}
