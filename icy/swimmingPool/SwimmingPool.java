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
package icy.swimmingPool;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

public class SwimmingPool
{
    private final ArrayList<SwimmingObject> objects;
    private final EventListenerList listeners;

    public SwimmingPool()
    {
        objects = new ArrayList<SwimmingObject>();
        listeners = new EventListenerList();
    }

    public void add(SwimmingObject object)
    {
        if (object != null)
        {
            objects.add(object);
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_ADDED, object));
        }
    }

    public void remove(SwimmingObject object)
    {
        if (objects.remove(object))
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, object));
    }

    public void removeAll()
    {
        if (!objects.isEmpty())
        {
            objects.clear();
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, null));
        }
    }

    public ArrayList<SwimmingObject> getObjects()
    {
        return new ArrayList<SwimmingObject>(objects);
    }

    public void addListener(SwimmingPoolListener listener)
    {
        listeners.add(SwimmingPoolListener.class, listener);
    }

    public void removeListener(SwimmingPoolListener listener)
    {
        listeners.remove(SwimmingPoolListener.class, listener);
    }

    private void fireSwimmingPoolEvent(SwimmingPoolEvent swimmingPoolEvent)
    {
        for (SwimmingPoolListener listener : listeners.getListeners(SwimmingPoolListener.class))
            listener.swimmingPoolChangeEvent(swimmingPoolEvent);
    }

}
