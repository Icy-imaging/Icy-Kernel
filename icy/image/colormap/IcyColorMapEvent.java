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
package icy.image.colormap;

import icy.common.CollapsibleEvent;

/**
 * @author stephane
 */
public class IcyColorMapEvent implements CollapsibleEvent
{
    public enum IcyColorMapEventType
    {
        MAP_CHANGED, TYPE_CHANGED, ENABLED_CHANGED
    }

    private final IcyColorMap colormap;
    private final IcyColorMapEventType type;

    public IcyColorMapEvent(IcyColorMap colormap, IcyColorMapEventType type)
    {
        super();

        this.colormap = colormap;
        this.type = type;
    }

    /**
     * @return the colormap
     */
    public IcyColorMap getColormap()
    {
        return colormap;
    }

    /**
     * @return the type
     */
    public IcyColorMapEventType getType()
    {
        return type;
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
        return colormap.hashCode() ^ type.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IcyColorMapEvent)
        {
            final IcyColorMapEvent e = (IcyColorMapEvent) obj;

            return (colormap == e.getColormap()) && (type == e.getType());
        }

        return super.equals(obj);
    }
}
