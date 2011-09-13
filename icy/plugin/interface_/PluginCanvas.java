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
package icy.plugin.interface_;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;

/**
 * @author Fab & Stephane
 *         Means the plugin is a canvas plugin
 */
public interface PluginCanvas
{
    /**
     * Return the ROI class name
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