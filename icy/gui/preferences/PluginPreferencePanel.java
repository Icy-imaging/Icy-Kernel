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

import icy.preferences.PluginPreferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

public class PluginPreferencePanel extends PreferencePanel
{
    private static final long serialVersionUID = 6383733826888489205L;

    public static final String NODE_NAME = "Plugin";

    /**
     * gui
     */
    final JCheckBox autoUpdateCheckBox;
    private final JCheckBox allowBetaCheckBox;

    PluginPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        autoUpdateCheckBox = new JCheckBox("Enable automatic update");
        autoUpdateCheckBox
                .setToolTipText("Enable (silent) automatic update for plugins as soon a new version is available");
        allowBetaCheckBox = new JCheckBox("Allow beta version");
        allowBetaCheckBox
                .setToolTipText("Show beta version in online plugins, also beta version can be used for update");

        load();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(autoUpdateCheckBox);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(allowBetaCheckBox);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(Box.createVerticalGlue());
    }

    @Override
    protected void load()
    {
        autoUpdateCheckBox.setSelected(PluginPreferences.getAutomaticUpdate());
        allowBetaCheckBox.setSelected(PluginPreferences.getAllowBeta());
    }

    @Override
    protected void save()
    {
        PluginPreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        PluginPreferences.setAllowBeta(allowBetaCheckBox.isSelected());
    }
}
