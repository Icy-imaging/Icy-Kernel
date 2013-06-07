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
package icy.plugin.interface_;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;

/**
 * Plugin Canvas interface.<br>
 * Used to define a plugin representing a specific IcyCanvas.<br>
 * The plugin will appears in the Canvas list.<br>
 * 
 * @author Fab & Stephane
 */
public interface PluginCanvas
{
    /**
     * Return the Canvas class name (CanvasClass.getClassName())
     */
    public String getCanvasClassName();

    /**
     * Create and return a new IcyCanvas
     * 
     * @param viewer
     *        the viewer create and will contain the Canvas
     * @return the new created Canvas
     */
    public IcyCanvas createCanvas(Viewer viewer);
}