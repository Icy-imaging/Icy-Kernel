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
package icy.image.lut;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class LUTEvent implements EventHierarchicalChecker
{
    private final LUT lut;
    private final int component;

    /**
     * @param lut
     * @param component
     */
    public LUTEvent(LUT lut, int component)
    {
        super();

        this.lut = lut;
        this.component = component;
    }

    /**
     * @return the lut
     */
    public LUT getLut()
    {
        return lut;
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
        if (event instanceof LUTEvent)
        {
            final LUTEvent e = (LUTEvent) event;

            return (component == -1) || (component == e.getComponent());
        }

        return false;
    }
}
