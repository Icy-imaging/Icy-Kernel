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
package icy.image.colorspace;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class IcyColorSpaceEvent implements EventHierarchicalChecker
{
    public enum IcyColorSpaceEventType
    {
        CHANGED
    }

    private final IcyColorSpace colorSpace;
    private final IcyColorSpaceEventType type;
    private final int component;

    public IcyColorSpaceEvent(IcyColorSpace colorSpace, IcyColorSpaceEventType type, int component)
    {
        super();

        this.colorSpace = colorSpace;
        this.type = type;
        this.component = component;
    }

    /**
     * @return the colorSpace
     */
    public IcyColorSpace getColorSpace()
    {
        return colorSpace;
    }

    /**
     * @return the type
     */
    public IcyColorSpaceEventType getType()
    {
        return type;
    }

    /**
     * @return the component
     */
    public int getComponent()
    {
        return component;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof IcyColorSpaceEvent)
        {
            final IcyColorSpaceEvent e = (IcyColorSpaceEvent) event;

            return (type == e.getType()) && ((component == -1) || (component == e.getComponent()));
        }

        return false;
    }

}