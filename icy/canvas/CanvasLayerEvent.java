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

import icy.common.CollapsibleEvent;
import icy.util.StringUtil;

/**
 * Event for canvas layer.
 * 
 * @author Stephane
 */
public class CanvasLayerEvent implements CollapsibleEvent
{
    public enum LayersEventType
    {
        ADDED, REMOVED, CHANGED;
    }

    private Layer source;
    private final LayersEventType type;
    // only meaningful when type == CHANGED
    private String property;

    public CanvasLayerEvent(Layer source, LayersEventType type, String property)
    {
        super();

        this.source = source;
        this.type = type;
        this.property = property;
    }

    public CanvasLayerEvent(Layer source, LayersEventType type)
    {
        this(source, type, null);
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

    /**
     * Return the property name which has changed.<br>
     * <br>
     * It can be <code>null</code> or one of the following :<br>
     * <code> 
     * Layer.PROPERTY_NAME<br>
     * Layer.PROPERTY_PRIORITY<br>
     * Layer.PROPERTY_ALPHA<br>
     * Layer.PROPERTY_VISIBLE<br>
     * </code>
     */
    public String getProperty()
    {
        return property;
    }

    @Override
    public boolean collapse(CollapsibleEvent event)
    {
        if (equals(event))
        {
            // nothing to change here
            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return source.hashCode() ^ type.hashCode() ^ ((property != null) ? property.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CanvasLayerEvent)
        {
            final CanvasLayerEvent e = (CanvasLayerEvent) obj;

            return (e.getSource() == source) && (e.getType() == type) && StringUtil.equals(e.getProperty(), property);
        }

        return super.equals(obj);
    }
}
