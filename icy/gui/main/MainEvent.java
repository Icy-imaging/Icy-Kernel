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
package icy.gui.main;

/**
 * @deprecated Use {@link GlobalOverlayListener}, {@link GlobalPluginListener},
 *             {@link GlobalROIListener}, {@link GlobalSequenceListener} and
 *             {@link GlobalViewerListener} interface instead.
 * @author Stephane
 */
public class MainEvent
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

    // /**
    // * collapse with specified event
    // */
    // public void collapse(MainEvent event)
    // {
    // // just use last source for focused event type
    // if (type == MainEventType.FOCUSED)
    // source = event.getSource();
    // }
    //
    //    @Override
//    public int hashCode()
//    {
//        return sourceType.hashCode() ^ type.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj)
//    {
//        if (obj instanceof MainEvent)
//        {
//            final MainEvent e = (MainEvent) obj;
//
//            // same source type and same type
//            return (e.getSourceType() == sourceType) && (e.getType() == type)
//                    && ((type == MainEventType.FOCUSED) || (e.getSource() == source));
//        }
//
//        return super.equals(obj);
//    }

    @Override
    public String toString()
    {
        return "Source = " + source + "; SourceType = " + sourceType + "; type = " + type;
    }
}
