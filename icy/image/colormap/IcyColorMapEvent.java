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
package icy.image.colormap;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class IcyColorMapEvent implements EventHierarchicalChecker
{
    public enum IcyColorMapEventType
    {
        MAP_CHANGED, TYPE_CHANGED
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

    /*
     * (non-Javadoc)
     * 
     * @see icy.common.IcyCompare#isSame(icy.common.IcyCompare)
     */
    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof IcyColorMapEvent)
        {
            final IcyColorMapEvent e = (IcyColorMapEvent) event;

            return (colormap == e.getColormap()) && (type == e.getType());
        }

        return false;
    }
}
