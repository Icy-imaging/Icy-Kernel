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

import icy.roi.ROI;
import icy.type.point.Point5D;

/**
 * Plugin ROI interface.<br>
 * Used to define a plugin representing a specific ROI.<br>
 * The plugin will appears in the ROI list.<br>
 * 
 * @author Fab & Stephane
 */
public interface PluginROI
{
    /**
     * Return the ROI class name (ROIClass.getClassName())
     */
    public String getROIClassName();

    // /**
    // * Create and return a new ROI
    // *
    // * @param pt
    // * location of the creation point
    // * @param cm
    // * "create mode" flag, specifying true here means the first point will be<br>
    // * created in "selected" state (so will support direct drag operation)
    // * @return the new created ROI
    // */
    // public ROI createROI(Point2D pt, boolean cm);

    /**
     * Create and return a new ROI for <i>interactive</i> mode.<br>
     * The first point will be created in <i>selected</i> state so will support direct drag
     * operation.
     * 
     * @param pt
     *        location of the creation point
     * @return the new created ROI
     */
    public ROI createROI(Point5D pt);

    /**
     * Create and return a new ROI.<br>
     * Default constructor.
     * 
     * @return the new created ROI
     */
    public ROI createROI();
}
