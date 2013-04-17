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
package icy.gui.lut;

import icy.gui.component.renderer.CustomComboBoxRenderer;
import icy.image.colormap.IcyColorMap;

import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class ColormapComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = 8439070623266035911L;
    
    private final int width;
    private final int height;

    public ColormapComboBoxRenderer(JComboBox combo, int w, int h)
    {
        super(combo);

        this.width = w;
        this.height = h;
    }

    public ColormapComboBoxRenderer(JComboBox combo)
    {
        this(combo, 64, 16);
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof IcyColorMap)
        {
            final IcyColorMap colormap = (IcyColorMap) value;

            setIcon(new ColormapIcon(colormap, width, height));
            setText(null);
            setToolTipText("Set " + colormap.getName() + " colormap");
            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
