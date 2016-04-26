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

import icy.common.CollapsibleEvent;

/**
 * @author Stephane
 */
public class IcyBufferedImageEvent implements CollapsibleEvent
{
    public enum IcyBufferedImageEventType
    {
        DATA_CHANGED, BOUNDS_CHANGED, COLORMAP_CHANGED
    }

    private final IcyBufferedImage image;
    private final IcyBufferedImageEventType type;
    private int param;

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
    public boolean collapse(CollapsibleEvent event)
    {
        if (equals(event))
        {
            final IcyBufferedImageEvent e = (IcyBufferedImageEvent) event;

            // set all component
            if (e.getParam() != param)
                param = -1;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return image.hashCode() ^ type.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IcyBufferedImageEvent)
        {
            final IcyBufferedImageEvent e = (IcyBufferedImageEvent) obj;

            return (image == e.getImage()) && (type == e.getType());
        }

        return super.equals(obj);
    }
}
