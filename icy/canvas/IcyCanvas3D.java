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
package icy.canvas;

import icy.gui.viewer.Viewer;

/**
 * @author Stephane
 */
public abstract class IcyCanvas3D extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = 6001100311244609559L;

    public IcyCanvas3D(Viewer viewer)
    {
        super(viewer);

        // default for 3D canvas
        posX = -1;
        posY = -1;
        posZ = -1;
        posT = 0;
    }

    @Override
    public void setPositionT(int t)
    {
        // position -1 not supported for T dimension on this canvas
        if (t != -1)
            super.setPositionT(t);
    }

}