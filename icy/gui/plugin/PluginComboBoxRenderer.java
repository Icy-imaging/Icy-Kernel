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
package icy.gui.plugin;

import icy.gui.component.renderer.CustomComboBoxRenderer;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.resource.ResourceUtil;

import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class PluginComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -8450810538242826922L;

    private final boolean showLabel;

    public PluginComboBoxRenderer(JComboBox combo, boolean showLabel)
    {
        super(combo);

        this.showLabel = showLabel;
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof String)
        {
            final PluginDescriptor plugin = PluginLoader.getPlugin((String) value);

            if (plugin != null)
            {
                setIcon(ResourceUtil.scaleIcon(plugin.getIcon(), 20));
                if (showLabel)
                    setText(plugin.getName());
                else
                    setText("");
                setToolTipText(plugin.getDescription());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
