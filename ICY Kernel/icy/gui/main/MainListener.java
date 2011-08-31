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
package icy.gui.main;

import java.util.EventListener;

public interface MainListener extends EventListener
{
    // Plugin events
    public void pluginOpened(MainEvent event);

    public void pluginClosed(MainEvent event);

    // Viewer events
    public void viewerOpened(MainEvent event);

    public void viewerFocused(MainEvent event);

    public void viewerClosed(MainEvent event);

    // Sequence events
    public void sequenceOpened(MainEvent event);

    public void sequenceFocused(MainEvent event);

    public void sequenceClosed(MainEvent event);

    // ROI events
    public void roiAdded(MainEvent event);

    public void roiRemoved(MainEvent event);

    // Painters events
    public void painterAdded(MainEvent event);

    public void painterRemoved(MainEvent event);

}
