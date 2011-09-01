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
package icy.gui.preferences;

import icy.gui.util.GuiUtil;
import icy.math.MathUtil;
import icy.preferences.GeneralPreferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author stephane
 */
public class GeneralPreferencePanel extends PreferencePanel
{
    private static final long serialVersionUID = -5376356916415550024L;

    public static final String NODE_NAME = "General";

    /**
     * id
     */

    /**
     * gui
     */
    private final JCheckBox exitConfirm;
    private final JCheckBox sequencePersistence;
    final JCheckBox autoUpdateCheckBox;
    final JCheckBox autoCheckUpdateCheckBox;
    private final JCheckBox alwaysOnTopCheckBox;
    // private final JCheckBox zoomSmooth;
    private final JSpinner maxMemoryMBSpinner;
    private final JSpinner uiFontSizeSpinner;

    /**
     * @param parent
     */
    GeneralPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        exitConfirm = new JCheckBox("Show confirmation when exiting application");
        sequencePersistence = new JCheckBox("Enable sequence persistence");
        sequencePersistence
                .setToolTipText("Enable the XML persistence for Sequence (file is automatically loaded/saved when sequence is opened/closed)");
        autoUpdateCheckBox = new JCheckBox("Enable auto update");
        autoUpdateCheckBox.setToolTipText("Enable silent update for application as soon a new version is available");
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
        autoCheckUpdateCheckBox = new JCheckBox("Check for application update at startup");
        autoCheckUpdateCheckBox.setToolTipText("Check if a new application version is available at startup");
        alwaysOnTopCheckBox = new JCheckBox("Application window always on top");

        // zoomSmooth = new JCheckBox("Smooth Zoom");

        final int maxMemLimit = (int) MathUtil.prevMultiple(GeneralPreferences.getMaxMemoryMBLimit(), 32);
        maxMemoryMBSpinner = new JSpinner(new SpinnerNumberModel(128, 64, maxMemLimit, 32));
        maxMemoryMBSpinner.setToolTipText("Change the maximum memory available for application");

        uiFontSizeSpinner = new JSpinner(new SpinnerNumberModel(7, 7, 24, 1));
        uiFontSizeSpinner.setToolTipText("");

        load();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(GuiUtil.createLineBoxPanel(alwaysOnTopCheckBox, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(exitConfirm, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(autoUpdateCheckBox, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(autoCheckUpdateCheckBox, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(sequencePersistence, Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        // mainPanel.add(GuiUtil.createLineBoxPanel(zoomSmooth, Box.createHorizontalGlue()));
        // mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" GUI font size  "), uiFontSizeSpinner,
                Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" Max memory  "), maxMemoryMBSpinner, new JLabel(
                " MB (max = " + maxMemLimit + " MB)"), Box.createHorizontalGlue()));
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.validate();
    }

    @Override
    protected void load()
    {
        maxMemoryMBSpinner.setValue(Integer.valueOf(GeneralPreferences.getMaxMemoryMB()));
        uiFontSizeSpinner.setValue(Integer.valueOf(GeneralPreferences.getGuiFontSize()));
        exitConfirm.setSelected(GeneralPreferences.getExitConfirm());
        // zoomSmooth.setSelected(GeneralPreferences.getSmoothZoom());
        sequencePersistence.setSelected(GeneralPreferences.getSequencePersistence());
        autoUpdateCheckBox.setSelected(GeneralPreferences.getAutomaticUpdate());
        autoCheckUpdateCheckBox.setSelected(GeneralPreferences.getAutomaticCheckUpdate()
                || autoUpdateCheckBox.isSelected());
        autoCheckUpdateCheckBox.setEnabled(!autoUpdateCheckBox.isSelected());
        alwaysOnTopCheckBox.setSelected(GeneralPreferences.getAlwaysOnTop());
    }

    @Override
    protected void save()
    {
        final int maxMemory = ((Integer) maxMemoryMBSpinner.getValue()).intValue();

        // launcher setting modified, restart needed
        if (GeneralPreferences.getMaxMemoryMB() != maxMemory)
            getPreferenceFrame().setNeedRestart();

        GeneralPreferences.setMaxMemoryMB(maxMemory);
        GeneralPreferences.setGuiFontSize(((Integer) uiFontSizeSpinner.getValue()).intValue());
        GeneralPreferences.setExitConfirm(exitConfirm.isSelected());
        // GeneralPreferences.setSmoothZoom(zoomSmooth.isSelected());
        GeneralPreferences.setSequencePersistence(sequencePersistence.isSelected());
        GeneralPreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        GeneralPreferences.setAutomaticCheckUpdate(autoCheckUpdateCheckBox.isSelected());
        GeneralPreferences.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
    }

}
