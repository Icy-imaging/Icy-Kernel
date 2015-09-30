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
package icy.canvas;

import icy.common.EventHierarchicalChecker;
import icy.sequence.DimensionId;

/**
 * @author Stephane
 */
public class IcyCanvasEvent implements EventHierarchicalChecker
{
    public enum IcyCanvasEventType
    {
        POSITION_CHANGED, OFFSET_CHANGED, SCALE_CHANGED, ROTATION_CHANGED, MOUSE_IMAGE_POSITION_CHANGED, SYNC_CHANGED;
    }

    private IcyCanvas source;
    private final IcyCanvasEventType type;
    private final DimensionId dim;

    public IcyCanvasEvent(IcyCanvas source, IcyCanvasEventType type, DimensionId dim)
    {
        this.source = source;
        this.type = type;
        this.dim = dim;
    }

    public IcyCanvasEvent(IcyCanvas source, IcyCanvasEventType type)
    {
        this(source, type, DimensionId.NULL);
    }

    /**
     * @return the source
     */
    public IcyCanvas getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public IcyCanvasEventType getType()
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
        if (event instanceof IcyCanvasEvent)
        {
            final IcyCanvasEvent e = (IcyCanvasEvent) event;

            return (e.getSource() == source) && (e.getType() == type) && (e.getDim() == dim);
        }

        return false;
    }
}
