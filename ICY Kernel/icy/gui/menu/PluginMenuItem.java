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
package icy.gui.menu;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.ResourceUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * This class represent a MenuItem which launch a plugin when pressed.
 * 
 * @author Fabrice de Chaumont
 */
public class PluginMenuItem extends JMenuItem implements ActionListener
{
    private static final long serialVersionUID = 2508924050709990008L;

    private static final int ICON_SIZE = 24;

    private final PluginDescriptor pluginDescriptor;

    public PluginMenuItem(PluginDescriptor pluginDescriptor)
    {
        super(pluginDescriptor.getSimpleClassName());

        this.pluginDescriptor = pluginDescriptor;

        if (pluginDescriptor.getIcon() != null)
            setIcon(ResourceUtil.scaleIcon(pluginDescriptor.getIcon(), ICON_SIZE));

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        PluginLauncher.launch(pluginDescriptor);
    }
}
