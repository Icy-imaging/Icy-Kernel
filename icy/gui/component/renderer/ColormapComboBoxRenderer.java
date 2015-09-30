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
package icy.gui.component.renderer;

import icy.gui.lut.ColormapIcon;
import icy.image.colormap.IcyColorMap;
import icy.util.ReflectionUtil;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
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

    /**
     * @deprecated Use {@link #ColormapComboBoxRenderer(JComboBox)} instead
     */
    @Deprecated
    public ColormapComboBoxRenderer(JComboBox combo, int w, int h)
    {
        this(combo);
    }

    public ColormapComboBoxRenderer(JComboBox combo)
    {
        super(combo);
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof IcyColorMap)
        {
            final IcyColorMap colormap = (IcyColorMap) value;
            final JComboBox comboBox = getComboBox();
            final Dimension dim = comboBox.getSize();
            int btnWidth;

            try
            {
                // a bit ugly but we really want to access it
                final JButton popBtn = (JButton) ReflectionUtil.getFieldObject(comboBox.getUI(), "arrowButton", true);
                
                btnWidth = popBtn.getWidth();
                if (btnWidth <= 0)
                    btnWidth = popBtn.getPreferredSize().width;
                if (btnWidth <= 0)
                    btnWidth = 20;
            }
            catch (Exception e)
            {
                btnWidth = 20;
            }

            final Insets insets = getInsets();

            dim.width -= btnWidth + insets.left + insets.right;
            dim.height -= insets.top + insets.bottom + 2;

            setIcon(new ColormapIcon(colormap, dim.width, dim.height));
            setText("");
            setToolTipText("Set " + colormap.getName() + " colormap");
            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
