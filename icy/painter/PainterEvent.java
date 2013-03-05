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
package icy.painter;

import icy.common.EventHierarchicalChecker;

/**
 * @deprecated Uses {@link Overlay} classes instead.
 */
@Deprecated
public class PainterEvent implements EventHierarchicalChecker
{
    public enum PainterEventType
    {
        PAINTER_CHANGED;
    }

    private final Painter source;
    private final PainterEventType type;

    public PainterEvent(Painter source, PainterEventType type)
    {
        this.source = source;
        this.type = type;
    }

    /**
     * @return the source
     */
    public Painter getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public PainterEventType getType()
    {
        return type;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof PainterEvent)
            return ((PainterEvent) event).getType() == type;

        return false;
    }
}
