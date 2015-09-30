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

import icy.common.listener.weak.WeakListener;
import icy.main.Icy;

/**
 * Weak listener wrapper for MainListener.
 * 
 * @deprecated Use one of these interface instead:<br/>
 *             {@link GlobalViewerListener}<br/>
 *             {@link GlobalSequenceListener}<br/>
 *             {@link GlobalROIListener}<br/>
 *             {@link GlobalOverlayListener}<br/>
 *             {@link GlobalPluginListener}
 * @author Stephane
 */
@Deprecated
public class WeakMainListener extends WeakListener<MainListener> implements MainListener
{
    public WeakMainListener(MainListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        Icy.getMainInterface().removeListener(this);
    }

    @Override
    public void pluginOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.pluginOpened(event);
    }

    @Override
    public void pluginClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.pluginClosed(event);
    }

    @Override
    public void viewerOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerOpened(event);
    }

    @Override
    public void viewerFocused(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerFocused(event);
    }

    @Override
    public void viewerClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.viewerClosed(event);
    }

    @Override
    public void sequenceOpened(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceOpened(event);
    }

    @Override
    public void sequenceFocused(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceFocused(event);
    }

    @Override
    public void sequenceClosed(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.sequenceClosed(event);
    }

    @Override
    public void roiAdded(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.roiAdded(event);
    }

    @Override
    public void roiRemoved(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.roiRemoved(event);
    }

    @Override
    public void painterAdded(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.painterAdded(event);
    }

    @Override
    public void painterRemoved(MainEvent event)
    {
        final MainListener listener = getListener();

        if (listener != null)
            listener.painterRemoved(event);
    }
}
