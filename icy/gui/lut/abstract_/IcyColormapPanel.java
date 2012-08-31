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
package icy.gui.lut.abstract_;

import icy.gui.viewer.Viewer;
import icy.image.lut.LUTBand;

import javax.swing.JPanel;

/**
 * @author Stephane
 */
public abstract class IcyColormapPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 5837250304098381166L;
    
    /**
     * associated Viewer & LUTBand
     */
    public final Viewer viewer;
    public final LUTBand lutBand;

    public IcyColormapPanel(Viewer viewer, LUTBand lutBand)
    {
        super();

        this.viewer = viewer;
        this.lutBand = lutBand;
    }

    /**
     * @return the viewer
     */
    public Viewer getViewer()
    {
        return viewer;
    }

    /**
     * @return the lutBand
     */
    public LUTBand getLutBand()
    {
        return lutBand;
    }

}
