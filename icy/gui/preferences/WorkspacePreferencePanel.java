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

import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.main.MainFrame;
import icy.main.Icy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;

/**
 * @author Stephane
 */
public class WorkspacePreferencePanel extends PreferencePanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -4200728138011886705L;

    public static final String NODE_NAME = "Workspace";

    /**
     * gui
     */
    final JCheckBox autoUpdateCheckBox;
    final JCheckBox autoCheckUpdateCheckBox;
    private final JButton cleanButton;

    WorkspacePreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        autoUpdateCheckBox = new JCheckBox("Enable auto update");
        autoUpdateCheckBox.setToolTipText("Enable silent update for workspaces as soon a new version is available");
        autoUpdateCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (autoUpdateCheckBox.isSelected())
                    autoCheckUpdateCheckBox.setSelected(true);
                autoCheckUpdateCheckBox.setEnabled(!autoUpdateCheckBox.isSelected());
            }
        });
        autoCheckUpdateCheckBox = new JCheckBox("Check for update at startup");
        autoCheckUpdateCheckBox.setToolTipText("Check for new workspaces version at startup");

        cleanButton = new JButton("Clean workspaces");
        cleanButton.setToolTipText("Remove missing plugins from workspace description file");
        cleanButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final MainFrame frame = Icy.getMainInterface().getMainFrame();

                // clean workspaces
                if (frame != null)
                    frame.getMainRibbon().cleanWorkspaces();

                new AnnounceFrame("Worspaces cleaned !");
            }
        });

        load();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        // mainPanel.add(autoUpdateCheckBox);
        // mainPanel.add(Box.createVerticalStrut(10));
        // mainPanel.add(autoCheckUpdateCheckBox);
        // mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cleanButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(Box.createVerticalGlue());
    }

    @Override
    protected void load()
    {
        // autoUpdateCheckBox.setSelected(WorkspacePreferences.getAutomaticUpdate());
        // autoCheckUpdateCheckBox.setSelected(WorkspacePreferences.getAutomaticCheckUpdate()
        // || autoUpdateCheckBox.isSelected());
        autoCheckUpdateCheckBox.setEnabled(!autoUpdateCheckBox.isSelected());
    }

    @Override
    protected void save()
    {
        // WorkspacePreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        // WorkspacePreferences.setAutomaticCheckUpdate(autoCheckUpdateCheckBox.isSelected());
    }

}
