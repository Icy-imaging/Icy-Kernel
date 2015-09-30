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
package icy.gui.preferences;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.preferences.PluginPreferences;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class PluginStartupPreferencePanel extends PluginListPreferencePanel implements PluginLoaderListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -3485972129754541852L;

    public static final String NODE_NAME = "Startup Plugin";

    final HashSet<String> inactives;

    public PluginStartupPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        inactives = new HashSet<String>();

        PluginLoader.addListener(this);

        // remove columns 3 (not used here)
        table.removeColumn(table.getColumn(columnIds[3]));

        // filter.setVisible(false);
        action1Button.setVisible(false);
        action2Button.setVisible(false);
        refreshButton.setVisible(false);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(new JLabel("Setup plugin which should be loaded at startup (daemon plugin only)."));
        topPanel.add(Box.createVerticalStrut(4));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        load();
        pluginsChanged();
    }

    @Override
    protected void closed()
    {
        super.closed();

        PluginLoader.removeListener(this);
    }

    @Override
    protected boolean isActive(PluginDescriptor plugin)
    {
        return !inactives.contains(plugin.getClassName());
    }

    @Override
    protected void setActive(PluginDescriptor plugin, boolean value)
    {
        final String className = plugin.getClassName();

        if (value)
            inactives.remove(className);
        else
        {
            if (!inactives.contains(className))
                inactives.add(className);
        }
    }

    @Override
    protected void load()
    {
        inactives.clear();
        inactives.addAll(PluginPreferences.getInactiveDaemons());
    }

    @Override
    protected void save()
    {
        // save preferences
        PluginPreferences.setInactiveDaemons(new ArrayList<String>(inactives));
        // restart daemon plugins
        PluginLoader.resetDaemons();
    }

    @Override
    protected void doAction1()
    {
    }

    @Override
    protected void doAction2()
    {
    }

    @Override
    protected void repositoryChanged()
    {
        // do nothing here
    }

    @Override
    protected void reloadPlugins()
    {
        // do nothing here
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        return "";
    }

    @Override
    protected ArrayList<PluginDescriptor> getPlugins()
    {
        return PluginLoader.getDaemonPlugins();
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        super.updateButtonsStateInternal();

        if (PluginLoader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
        }

        action1Button.setEnabled(false);
        action2Button.setEnabled(false);
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        pluginsChanged();
    }
}
