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
package icy.image.colormodel;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class IcyColorModelEvent implements EventHierarchicalChecker
{
    public enum IcyColorModelEventType
    {
        SCALER_CHANGED, COLORMAP_CHANGED
    }

    private final IcyColorModel colorModel;
    private final IcyColorModelEventType type;
    private final int component;

    /**
     * @param colorModel
     * @param type
     */
    public IcyColorModelEvent(IcyColorModel colorModel, IcyColorModelEventType type, int component)
    {
        super();

        this.colorModel = colorModel;
        this.type = type;
        this.component = component;
    }

    /**
     * @return the colorModel
     */
    public IcyColorModel getColorModel()
    {
        return colorModel;
    }

    /**
     * @return the type
     */
    public IcyColorModelEventType getType()
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
        if (event instanceof IcyColorModelEvent)
        {
            final IcyColorModelEvent e = (IcyColorModelEvent) event;

            return (type == e.getType()) && ((component == -1) || (component == e.getComponent()));
        }

        return false;
    }

}
