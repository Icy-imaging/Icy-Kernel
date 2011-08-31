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
    public enum ROIPointEventType
    {
        NULL, POINT_ADDED, POINT_REMOVED, POINT_CHANGED;
    }

    public enum ROIEventType
    {
        ROI_CHANGED, PAINTER_CHANGED, NAME_CHANGED;
    }

    private final ROI source;
    private final ROIEventType type;
    private Object point;
    private ROIPointEventType pointEventType;

    public ROIEvent(ROI source, ROIEventType type, ROIPointEventType pointEventType, Object point)
    {
        this.source = source;
        this.type = type;
        this.point = point;
        this.pointEventType = pointEventType;
    }

    public ROIEvent(ROI source, ROIEventType type)
    {
        this(source, type, ROIPointEventType.NULL, null);
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
     * @return the point
     */
    public Object getPoint()
    {
        return point;
    }

    /**
     * @return the pointEventType
     */
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
                    // same point event type ?
                    if (e.getPointEventType() == pointEventType)
                    {
                        // join events in one global event
                        if (e.getPoint() != point)
                            point = null;
                        return true;
                    }
                    if (e.getPointEventType() == ROIPointEventType.NULL)
                    {
                        pointEventType = ROIPointEventType.NULL;
                        point = null;
                        return true;
                    }
                    break;

                case NAME_CHANGED:
                case PAINTER_CHANGED:
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof ROIEvent)
        {
            final ROIEvent e = (ROIEvent) event;

            return optimizeEventWith(e)
                    || ((type == e.getType()) && (source == e.getSource()) && (pointEventType == ROIPointEventType.NULL));
        }

        return false;
    }
}