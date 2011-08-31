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
package icy.gui.main;

import icy.common.EventHierarchicalChecker;

/**
 * @author Stephane
 */
public class MainEvent implements EventHierarchicalChecker
{
    public enum MainEventSourceType
    {
        PLUGIN, VIEWER, SEQUENCE, ROI, PAINTER
    }

    public enum MainEventType
    {
        OPENED, FOCUSED, CLOSED, ADDED, REMOVED
    }

    private final MainEventSourceType sourceType;
    private final MainEventType type;
    private Object source;

    public MainEvent(MainEventSourceType sourceType, MainEventType type, Object source)
    {
        super();

        this.sourceType = sourceType;
        this.type = type;
        this.source = source;
    }

    /**
     * @return the source
     */
    public Object getSource()
    {
        return source;
    }

    /**
     * @return the sourceType
     */
    public MainEventSourceType getSourceType()
    {
        return sourceType;
    }

    /**
     * @return the type
     */
    public MainEventType getType()
    {
        return type;
    }

    /**
     * Optimize event
     */
    private boolean optimizeEventWith(MainEvent e)
    {
        // same source type and same type
        if ((e.getSourceType() == sourceType) && (e.getType() == type))
        {
            // join events in one global event
            if (e.getSource() != source)
                source = null;
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.common.IcyCompare#isSame(icy.common.IcyCompare)
     */
    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof MainEvent)
        {
            final MainEvent e = (MainEvent) event;

            // focus event ?
            if (type == MainEventType.FOCUSED)
                // only need to have the last one for a specific source
                return (type == e.getType()) && (sourceType == e.getSourceType());

            // optimize event
            return optimizeEventWith(e);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Source = " + source + "; SourceType = " + sourceType + "; type = " + type;
    }

}
