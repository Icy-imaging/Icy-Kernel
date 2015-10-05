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
package icy.painter;

import icy.common.EventHierarchicalChecker;
import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class OverlayEvent implements EventHierarchicalChecker
{
    public enum OverlayEventType
    {
        PAINTER_CHANGED, PROPERTY_CHANGED;
    }

    private final Overlay source;
    private final OverlayEventType type;
    private String propertyName;

    public OverlayEvent(Overlay source, OverlayEventType type, String propertyName)
    {
        this.source = source;
        this.type = type;
        this.propertyName = propertyName;
    }

    public OverlayEvent(Overlay source, OverlayEventType type)
    {
        this(source, type, null);
    }

    /**
     * @return the source
     */
    public Overlay getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public OverlayEventType getType()
    {
        return type;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    private boolean optimizeEventWith(OverlayEvent e)
    {
        if (e.getType() == type)
        {
//            if (type == OverlayEventType.PROPERTY_CHANGED)
//            {
//                // join properties
//                if (!StringUtil.equals(e.getPropertyName(), propertyName))
//                    propertyName = null;
//            }

            // same property event ?
            if (type == OverlayEventType.PROPERTY_CHANGED)
                return StringUtil.equals(e.getPropertyName(), propertyName);

            return true;
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof OverlayEvent)
            return optimizeEventWith((OverlayEvent) event);

        return false;
    }
}
