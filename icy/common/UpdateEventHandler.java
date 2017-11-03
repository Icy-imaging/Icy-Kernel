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
package icy.common;

import icy.common.listener.ChangeListener;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Utility class to handle <code>Update</code> type event.
 * 
 * @author stephane
 */
public class UpdateEventHandler
{
    ChangeListener parent;

    /**
     * dispatch in AWT dispatch thread
     */
    private boolean awtDispatch;
    /**
     * internal update counter
     */
    private int updateCnt;
    /**
     * internal pending change events
     */
    private final LinkedHashMap<CollapsibleEvent, CollapsibleEvent> pendingChanges;

    /**
     * 
     */
    public UpdateEventHandler(ChangeListener parent, boolean awtDispatch)
    {
        super();

        this.parent = parent;
        this.awtDispatch = awtDispatch;

        updateCnt = 0;
        pendingChanges = new LinkedHashMap<CollapsibleEvent, CollapsibleEvent>();
    }

    /**
     * 
     */
    public UpdateEventHandler(ChangeListener parent)
    {
        this(parent, false);
    }

    /**
     * @return the awtDispatch
     */
    public boolean isAwtDispatch()
    {
        return awtDispatch;
    }

    /**
     * @param awtDispatch
     *        the awtDispatch to set
     */
    public void setAwtDispatch(boolean awtDispatch)
    {
        this.awtDispatch = awtDispatch;
    }

    public Collection<CollapsibleEvent> getPendingChanges()
    {
        return pendingChanges.values();
    }

    public void beginUpdate()
    {
        updateCnt++;
    }

    public void endUpdate()
    {
        updateCnt--;
        if (updateCnt <= 0)
        {
            final List<CollapsibleEvent> events;

            synchronized (pendingChanges)
            {
                events = new ArrayList<CollapsibleEvent>(pendingChanges.values());
                pendingChanges.clear();
            }

            // dispatch all contained events (use copy to avoid concurrent changes)
            for (CollapsibleEvent event : events)
                dispatchOnChanged(event);
        }
    }

    public boolean isUpdating()
    {
        return updateCnt > 0;
    }

    public boolean hasPendingChanges()
    {
        return !pendingChanges.isEmpty();
    }

    protected void addPendingChange(CollapsibleEvent change)
    {
        final CollapsibleEvent previousChange;

        // TODO: can take sometime (select all on many ROI)
        // TODO: check how fast is it now...
        synchronized (pendingChanges)
        {
            // search in pending changes if we have an equivalent change
            previousChange = pendingChanges.get(change);

            // not already existing ? --> just add the new change
            if (previousChange == null)
                pendingChanges.put(change, change);
        }

        // found an equivalent previous change ? --> collapse the new change into the old one
        if (previousChange != null)
            previousChange.collapse(change);
    }

    public void changed(CollapsibleEvent event)
    {
        if (isUpdating())
            addPendingChange(event);
        else
            dispatchOnChanged(event);
    }

    protected void dispatchOnChanged(CollapsibleEvent event)
    {
        final CollapsibleEvent e = event;

        if (awtDispatch)
        {
            // dispatch on AWT Dispatch Thread now
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    parent.onChanged(e);
                }
            });
        }
        else
            parent.onChanged(e);
    }
}