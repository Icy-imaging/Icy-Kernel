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
package icy.gui.lut.abstract_;

import icy.gui.viewer.Viewer;
import icy.image.lut.LUT;
import icy.sequence.Sequence;

import javax.swing.JPanel;

/**
 * @deprecated
 */
@Deprecated
public class IcyLutViewer extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1121431241903512066L;

    /**
     * associated Viewer & LUT
     */
    protected final Viewer viewer;
    protected final LUT lut;

    public IcyLutViewer(Viewer v, LUT l)
    {
        super();

        viewer = v;
        lut = l;
    }

    /**
     * @return the viewer
     */
    public Viewer getViewer()
    {
        return viewer;
    }

    /**
     * @return the lut
     */
    public LUT getLut()
    {
        return lut;
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence()
    {
        return viewer.getSequence();
    }
}
