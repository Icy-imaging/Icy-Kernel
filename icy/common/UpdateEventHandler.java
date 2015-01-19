/*
 * Copyright 2010-2013 Institut Pasteur.
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
import java.util.List;

/**
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
    private final List<EventHierarchicalChecker> pendingChanges;

    /**
     * 
     */
    public UpdateEventHandler(ChangeListener parent, boolean awtDispatch)
    {
        super();

        this.parent = parent;
        this.awtDispatch = awtDispatch;

        updateCnt = 0;
        pendingChanges = new ArrayList<EventHierarchicalChecker>();
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

    public List<EventHierarchicalChecker> getPendingChanges()
    {
        return pendingChanges;
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
            boolean done = false;

            // fire pending events
            while (!done)
            {
                final EventHierarchicalChecker compare;

                synchronized (pendingChanges)
                {
                    // check and remove it from list
                    done = pendingChanges.isEmpty();

                    if (!done)
                        compare = pendingChanges.remove(0);
                    else
                        compare = null;
                }

                // and then process (avoid some dead lock)
                if (compare != null)
                    dispatchOnChanged(compare);
            }
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

    protected void addPendingChange(EventHierarchicalChecker include)
    {
        synchronized (pendingChanges)
        {
            // test if we already have an including object in the list
            for (EventHierarchicalChecker cmp : pendingChanges)
                if (cmp.isEventRedundantWith(include))
                    return;

            // we add it only if it isn't already existing
            pendingChanges.add(include);
        }
    }

    public void changed(EventHierarchicalChecker include)
    {
        if (isUpdating())
            addPendingChange(include);
        else
            dispatchOnChanged(include);
    }

    protected void dispatchOnChanged(EventHierarchicalChecker include)
    {
        final EventHierarchicalChecker event = include;

        if (awtDispatch)
        {
            // dispatch on AWT Dispatch Thread now
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    parent.onChanged(event);
                }
            });
        }
        else
            parent.onChanged(event);
    }
}
