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
package icy.gui.viewer;

import icy.common.EventHierarchicalChecker;
import icy.sequence.DimensionId;

/**
 * @author stephane
 */
public class ViewerEvent implements EventHierarchicalChecker
{
    public enum ViewerEventType
    {
        POSITION_CHANGED, CANVAS_CHANGED, LUT_CHANGED;
    }

    private final Viewer source;
    private final ViewerEventType type;
    private final DimensionId dim;

    public ViewerEvent(Viewer source, ViewerEventType type, DimensionId dim)
    {
        this.source = source;
        this.type = type;
        this.dim = dim;
    }

    public ViewerEvent(Viewer source, ViewerEventType type)
    {
        this(source, type, DimensionId.NULL);
    }

    /**
     * @return the source
     */
    public Viewer getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public ViewerEventType getType()
    {
        return type;
    }

    /**
     * @return the dimension
     */
    public DimensionId getDim()
    {
        return dim;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof ViewerEvent)
        {
            final ViewerEvent ve = (ViewerEvent) event;

            return (ve.getSource() == source) && (ve.getType() == type) && (ve.getDim() == dim);
        }

        return false;
    }

}