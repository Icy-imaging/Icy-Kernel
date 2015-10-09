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
 * @deprecated Use one of these interface instead:<br/>
 *             {@link GlobalViewerListener}<br/>
 *             {@link GlobalSequenceListener}<br/>
 *             {@link GlobalROIListener}<br/>
 *             {@link GlobalOverlayListener}<br/>
 *             {@link GlobalPluginListener}
 */
@Deprecated
public abstract class MainAdapter implements MainListener
{
    @Override
    public void painterAdded(MainEvent event)
    {

    }

    @Override
    public void painterRemoved(MainEvent event)
    {

    }

    @Override
    public void roiAdded(MainEvent event)
    {

    }

    @Override
    public void roiRemoved(MainEvent event)
    {

    }

    @Override
    public void sequenceClosed(MainEvent event)
    {

    }

    @Override
    public void sequenceFocused(MainEvent event)
    {

    }

    @Override
    public void sequenceOpened(MainEvent event)
    {

    }

    @Override
    public void viewerClosed(MainEvent event)
    {

    }

    @Override
    public void viewerFocused(MainEvent event)
    {

    }

    @Override
    public void viewerOpened(MainEvent event)
    {

    }

    @Override
    public void pluginClosed(MainEvent event)
    {

    }

    @Override
    public void pluginOpened(MainEvent event)
    {

    }
}
