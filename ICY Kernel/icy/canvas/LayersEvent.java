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
package icy.canvas;

import icy.common.EventHierarchicalChecker;

/**
 * @author Stephane
 */
public class LayersEvent implements EventHierarchicalChecker
{
    public enum LayersEventType
    {
        ADDED, REMOVED, CHANGED;
    }

    private Layer source;
    private final LayersEventType type;

    public LayersEvent(Layer source, LayersEventType type)
    {
        this.source = source;
        this.type = type;
    }

    /**
     * @return the source
     */
    public Layer getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public LayersEventType getType()
    {
        return type;
    }

    private boolean optimizeEventWith(LayersEvent e)
    {
        // TODO: verify this is correct to optimize LayersEvent this way...
        if (e.getType() == type)
        {
            // join events in one global event
            if (e.getSource() != source)
                source = null;

            return true;
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof LayersEvent)
        {
            final LayersEvent e = (LayersEvent) event;

            return optimizeEventWith(e) || ((e.getType() == type) && ((source == null) || (source == e.getSource())));
        }

        return false;
    }
}
