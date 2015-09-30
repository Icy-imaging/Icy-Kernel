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
package icy.swimmingPool;

import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;

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
            synchronized (objects)
            {
                objects.add(object);
            }

            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_ADDED, object));
        }
    }

    public void remove(SwimmingObject object)
    {
        final boolean b;

        synchronized (objects)
        {
            b = objects.remove(object);
        }

        if (b)
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, object));
    }

    public void removeAll()
    {
        final boolean b;

        synchronized (objects)
        {
            b = !objects.isEmpty();

            if (b)
                objects.clear();
        }

        if (b)
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, null));
    }

    /**
     * Remove all objects contained in the collection from the swimming pool.
     */
    public void removeAll(Collection<SwimmingObject> sos)
    {
        final boolean b;

        synchronized (objects)
        {
            b = objects.removeAll(sos);
        }

        if (b)
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, null));
    }

    /**
     * Remove all object with specified name (or name starting with specified name).
     */
    public void removeAll(String name, boolean startWith)
    {
        boolean b = false;

        synchronized (objects)
        {
            for (int i = objects.size() - 1; i >= 0; i--)
            {
                final SwimmingObject so = objects.get(i);

                if (so != null)
                {
                    if (startWith)
                    {
                        if (so.getName().startsWith(name))
                        {
                            objects.remove(i);
                            b = true;
                        }
                    }
                    else if (StringUtil.equals(name, so.getName()))
                    {
                        objects.remove(i);
                        b = true;
                    }
                }
            }
        }

        if (b)
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, null));
    }

    /**
     * Remove all object of specified class type
     */
    public void removeAll(Class<?> objectType)
    {
        boolean b = false;

        synchronized (objects)
        {
            for (int i = objects.size() - 1; i >= 0; i--)
            {
                final SwimmingObject so = objects.get(i);

                if (so != null)
                {
                    final Object obj = so.getObject();

                    if ((obj != null) && objectType.isInstance(obj))
                    {
                        objects.remove(i);
                        b = true;
                    }
                }
            }
        }

        if (b)
            fireSwimmingPoolEvent(new SwimmingPoolEvent(SwimmingPoolEventType.ELEMENT_REMOVED, null));
    }

    /**
     * Return all objects of the swimming pool
     */
    public ArrayList<SwimmingObject> getObjects()
    {
        return new ArrayList<SwimmingObject>(objects);
    }

    /**
     * Return objects with specified name (or name starting with specified name).
     */
    public ArrayList<SwimmingObject> getObjects(String name, boolean startWith)
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    if (startWith)
                    {
                        if (so.getName().startsWith(name))
                            result.add(so);
                    }
                    else if (StringUtil.equals(name, so.getName()))
                        result.add(so);
                }
            }
        }

        return result;
    }

    /**
     * Return objects of specified class type
     */
    public ArrayList<SwimmingObject> getObjects(Class<?> objectType)
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    final Object obj = so.getObject();

                    if ((obj != null) && objectType.isInstance(obj))
                        result.add(so);
                }
            }
        }

        return result;
    }

    /**
     * Return and remove objects with specified name (or name starting with specified name).
     */
    public ArrayList<SwimmingObject> popObjects(String name, boolean startWith)
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        synchronized (objects)
        {
            for (int i = objects.size() - 1; i >= 0; i--)
            {
                final SwimmingObject so = objects.get(i);

                if (so != null)
                {
                    if (startWith)
                    {
                        if (so.getName().startsWith(name))
                        {
                            result.add(so);
                            objects.remove(i);
                        }
                    }
                    else if (StringUtil.equals(name, so.getName()))
                    {
                        result.add(so);
                        objects.remove(i);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Return and remove objects of specified class type
     */
    public ArrayList<SwimmingObject> popObjects(Class<?> objectType)
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        synchronized (objects)
        {
            for (int i = objects.size() - 1; i >= 0; i--)
            {
                final SwimmingObject so = objects.get(i);

                if (so != null)
                {
                    final Object obj = so.getObject();

                    if ((obj != null) && objectType.isInstance(obj))
                    {
                        result.add(so);
                        objects.remove(i);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Return true if the swimming pool contains at least one object with the specified name (or
     * name starting with specified name).
     */
    public boolean hasObjects(String name, boolean startWith)
    {
        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    if (startWith)
                    {
                        if (so.getName().startsWith(name))
                            return true;
                    }
                    else if (StringUtil.equals(name, so.getName()))
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Return true if the swimming pool contains at least one object with the specified class type.
     */
    public boolean hasObjects(Class<?> objectType)
    {
        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    final Object obj = so.getObject();

                    if ((obj != null) && objectType.isInstance(obj))
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Return the number of object with the specified name (or name starting with specified name)
     * contained in the swimming pool.
     */
    public int getCount(String name, boolean startWith)
    {
        int result = 0;

        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    if (startWith)
                    {
                        if (so.getName().startsWith(name))
                            result++;
                    }
                    else if (StringUtil.equals(name, so.getName()))
                        result++;
                }
            }
        }

        return result;
    }

    /**
     * Return the number of object with the specified class type contained in the swimming pool.
     */
    public int getCount(Class<?> objectType)
    {
        int result = 0;

        synchronized (objects)
        {
            for (SwimmingObject so : objects)
            {
                if (so != null)
                {
                    final Object obj = so.getObject();

                    if ((obj != null) && objectType.isInstance(obj))
                        result++;
                }
            }
        }

        return result;
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
