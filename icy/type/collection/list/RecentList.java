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
package icy.type.collection.list;

import icy.preferences.XMLPreferences;
import icy.type.collection.list.RecentListEvent.RecentListEventType;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public abstract class RecentList
{
    public final static String ID_ENTRY = "entry";

    protected final XMLPreferences preferences;
    protected final int nbMaxEntry;
    protected final ArrayList<Object> list;

    private final EventListenerList listeners;

    public RecentList(XMLPreferences preferences, int nbMaxEntry)
    {
        super();

        this.preferences = preferences;
        this.nbMaxEntry = nbMaxEntry;
        list = new ArrayList<Object>();
        listeners = new EventListenerList();

        // load the list from prefs
        load();
    }

    public void clear()
    {
        list.clear();

        // save to pref
        save();
        // inform about change
        changed();
    }

    public void addEntry(Object entry)
    {
        // remove entry if already present
        list.remove(entry);
        // add entry at top
        list.add(0, entry);

        // remove last entries
        while (list.size() > nbMaxEntry)
            list.remove(list.size() - 1);

        // save to pref
        save();
        // inform about change
        changed();
    }

    public int getSize()
    {
        return list.size();
    }

    public int getMaxSize()
    {
        return nbMaxEntry;
    }

    public Object getEntry(int index)
    {
        return list.get(index);
    }

    protected void load()
    {
        list.clear();

        for (int i = 0; i < nbMaxEntry; i++)
        {
            final Object value = loadEntry(ID_ENTRY + i);
            if (value != null)
                list.add(value);
        }

        changed();
    }

    protected abstract Object loadEntry(final String key);

    protected void save()
    {
    	// clear all
    	preferences.clear();
        preferences.removeChildren();
        preferences.clean();
    	
    	// then save each entry
        for (int i = 0; i < nbMaxEntry; i++)
        {
            if (i < list.size())
                saveEntry(ID_ENTRY + i, list.get(i));
            else
                saveEntry(ID_ENTRY + i, null);
        }
    }

    protected abstract void saveEntry(final String key, final Object value);

    /**
     * process on change
     */
    protected void changed()
    {
        final RecentListEvent event = new RecentListEvent(this, RecentListEventType.CHANGED);
        // notify listeners we have changed
        fireEvent(event);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(RecentListListener listener)
    {
        listeners.add(RecentListListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(RecentListListener listener)
    {
        listeners.remove(RecentListListener.class, listener);
    }

    /**
     * fire event
     * 
     * @param e
     */
    public void fireEvent(RecentListEvent e)
    {
        for (RecentListListener listener : listeners.getListeners(RecentListListener.class))
            listener.RencentFileChanged(e);
    }
}
