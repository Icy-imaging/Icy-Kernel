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
package icy.gui.plugin;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.resource.icon.BasicResizableIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;

/**
 * Class helper to create plugin command button
 * 
 * @author Stephane
 */
public class PluginCommandButton
{
    /**
     * Set a plugin button with specified action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin, boolean doAction)
    {
        final String name = plugin.getName();
        final String className = plugin.getClassName();
        final ImageIcon plugIcon = plugin.getIcon();

        // update text & icon
        button.setText(name);
        button.setIcon(new BasicResizableIcon(plugIcon));
        // save class name here
        button.setName(className);

        button.setActionRichTooltip(new PluginRichToolTip(plugin));

        if (doAction)
        {
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final PluginDescriptor plugin = PluginLoader.getPlugin(((AbstractCommandButton) e.getSource())
                            .getName());

                    if (plugin != null)
                        PluginLauncher.start(plugin);
                }
            });
        }
    }

    /**
     * Set a plugin button with default action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin)
    {
        setButton(button, plugin, true);
    }

    /**
     * Build a plugin button
     */
    public static AbstractCommandButton createButton(PluginDescriptor plugin, boolean toggle, boolean doAction)
    {
        final AbstractCommandButton result;

        // build command button
        if (toggle)
            result = new IcyCommandToggleButton();
        else
            result = new IcyCommandButton();

        setButton(result, plugin, doAction);

        return result;
    }

    /**
     * Build a plugin button with default action (execute plugin)
     */
    public static IcyCommandButton createButton(PluginDescriptor plugin)
    {
        // build with default action listener
        return (IcyCommandButton) createButton(plugin, false, true);
    }

    /**
     * Build a plugin toggle button with default action (execute plugin) if enable.
     */
    public static IcyCommandToggleButton createToggleButton(PluginDescriptor plugin, boolean doAction)
    {
        return (IcyCommandToggleButton) createButton(plugin, true, doAction);
    }
}
