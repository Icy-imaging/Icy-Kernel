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
package icy.roi;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class ROIEvent implements EventHierarchicalChecker
{
    @Deprecated
    public enum ROIPointEventType
    {
        NULL, POINT_ADDED, POINT_REMOVED, POINT_CHANGED;
    }

    public enum ROIEventType
    {
        FOCUS_CHANGED, SELECTION_CHANGED, ROI_CHANGED, PROPERTY_CHANGED, @Deprecated
        PAINTER_CHANGED, @Deprecated
        NAME_CHANGED;
    }

    private final ROI source;
    private final ROIEventType type;
    private String propertyName;

    @Deprecated
    private Object point;
    @Deprecated
    private ROIPointEventType pointEventType;

    /**
     * @deprecated Use {@link #ROIEvent(ROI, ROIEventType)} constructor instead
     */
    @Deprecated
    public ROIEvent(ROI source, ROIEventType type, ROIPointEventType pointEventType, Object point)
    {
        super();

        this.source = source;
        this.type = type;
        propertyName = null;

        this.point = point;
        this.pointEventType = pointEventType;
    }

    public ROIEvent(ROI source, String propertyName)
    {
        super();

        this.source = source;
        type = ROIEventType.PROPERTY_CHANGED;
        this.propertyName = propertyName;
    }

    public ROIEvent(ROI source, ROIEventType type)
    {
        super();

        this.source = source;
        this.type = type;
        propertyName = null;
    }

    /**
     * @return the source
     */
    public ROI getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public ROIEventType getType()
    {
        return type;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    @Deprecated
    public Object getPoint()
    {
        return point;
    }

    @Deprecated
    public ROIPointEventType getPointEventType()
    {
        return pointEventType;
    }

    /**
     * Optimize event
     */
    private boolean optimizeEventWith(ROIEvent e)
    {
        // same type ?
        if (e.getType() == type)
        {
            switch (type)
            {
                case ROI_CHANGED:
                case FOCUS_CHANGED:
                case SELECTION_CHANGED:
                case NAME_CHANGED:
                case PAINTER_CHANGED:
                    return true;

                case PROPERTY_CHANGED:
                    if (e.getPropertyName() == null)
                        propertyName = null;
                    if ((propertyName == null) || propertyName.equals(e.getPropertyName()))
                        return true;
            }
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof ROIEvent)
            return optimizeEventWith((ROIEvent) event);

        return false;
    }
}