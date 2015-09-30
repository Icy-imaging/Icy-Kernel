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
 * @author Stephane
 */
@Deprecated
public abstract class MainAdaptater implements MainListener
{
    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#painterAdded(icy.gui.main.MainEvent)
     */
    @Override
    public void painterAdded(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#painterRemoved(icy.gui.main.MainEvent)
     */
    @Override
    public void painterRemoved(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#roiAdded(icy.gui.main.MainEvent)
     */
    @Override
    public void roiAdded(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#roiRemoved(icy.gui.main.MainEvent)
     */
    @Override
    public void roiRemoved(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#sequenceClosed(icy.gui.main.MainEvent)
     */
    @Override
    public void sequenceClosed(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#sequenceFocused(icy.gui.main.MainEvent)
     */
    @Override
    public void sequenceFocused(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#sequenceOpened(icy.gui.main.MainEvent)
     */
    @Override
    public void sequenceOpened(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#viewerClosed(icy.gui.main.MainEvent)
     */
    @Override
    public void viewerClosed(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#viewerFocused(icy.gui.main.MainEvent)
     */
    @Override
    public void viewerFocused(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#viewerOpened(icy.gui.main.MainEvent)
     */
    @Override
    public void viewerOpened(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#pluginClosed(icy.gui.main.MainEvent)
     */
    @Override
    public void pluginClosed(MainEvent event)
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.main.MainListener#pluginOpened(icy.gui.main.MainEvent)
     */
    @Override
    public void pluginOpened(MainEvent event)
    {

    }

}
