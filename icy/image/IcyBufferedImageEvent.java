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
package icy.image;

import icy.common.EventHierarchicalChecker;

/**
 * @author Stephane
 */
public class IcyBufferedImageEvent implements EventHierarchicalChecker
{
    public enum IcyBufferedImageEventType
    {
        DATA_CHANGED, BOUNDS_CHANGED, COLORMAP_CHANGED
    }

    private final IcyBufferedImage image;
    private final IcyBufferedImageEventType type;
    private final int param;

    /**
     * @param image
     * @param type
     */
    public IcyBufferedImageEvent(IcyBufferedImage image, IcyBufferedImageEventType type)
    {
        this(image, type, -1);
    }

    /**
     * @param image
     * @param type
     * @param param
     */
    public IcyBufferedImageEvent(IcyBufferedImage image, IcyBufferedImageEventType type, int param)
    {
        super();

        this.image = image;
        this.type = type;
        this.param = param;
    }

    /**
     * @return the image
     */
    public IcyBufferedImage getImage()
    {
        return image;
    }

    /**
     * @return the type
     */
    public IcyBufferedImageEventType getType()
    {
        return type;
    }

    /**
     * @return the param
     */
    public int getParam()
    {
        return param;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof IcyBufferedImageEvent)
        {
            final IcyBufferedImageEvent e = (IcyBufferedImageEvent) event;

            return (type == e.getType()) && ((param == -1) || (param == e.getParam()));
        }

        return false;
    }

}
