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
package icy.gui.component.renderer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class LabelComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -4914566166205633920L;

    public LabelComboBoxRenderer(JComboBox combo)
    {
        super(combo);
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof JLabel)
        {
            final JLabel label = (JLabel) value;

            setIcon(label.getIcon());
            setText(label.getText());
            setToolTipText(label.getToolTipText());
            setEnabled(label.isEnabled());
            setFont(label.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
