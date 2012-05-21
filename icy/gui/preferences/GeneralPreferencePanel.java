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

import icy.gui.dialog.MessageDialog;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.preferences.GeneralPreferences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
     * gui
     */
    final JCheckBox exitConfirm;
    private final JCheckBox sequencePersistence;
    final JCheckBox autoUpdateCheckBox;
    final JCheckBox autoCheckUpdateCheckBox;
    private final JCheckBox alwaysOnTopCheckBox;
    private final JSpinner maxMemoryMBSpinner;
    private final JSpinner uiFontSizeSpinner;
    private final JButton reenableAllToolTipButton;
    private final JButton reenableAllConfirmButton;

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

        final int maxMemLimit = (int) MathUtil.prevMultiple(GeneralPreferences.getMaxMemoryMBLimit(), 32);
        maxMemoryMBSpinner = new JSpinner(new SpinnerNumberModel(128, 64, maxMemLimit, 32));
        maxMemoryMBSpinner.setToolTipText("Change the maximum memory available for application");

        uiFontSizeSpinner = new JSpinner(new SpinnerNumberModel(7, 7, 24, 1));
        uiFontSizeSpinner.setToolTipText("");

        reenableAllToolTipButton = new JButton("Reactivate tooltips");
        reenableAllToolTipButton.setToolTipText("All hidden tooltips will be made visible again");
        reenableAllToolTipButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // clear the saved tool tips preference to re-enable them
                GeneralPreferences.getPreferencesToolTips().removeChildren();
                GeneralPreferences.getPreferencesToolTips().clear();

                MessageDialog.showDialog("All tooltips are now enabled again !");
            }
        });

        reenableAllConfirmButton = new JButton("Reactivate confirmations");
        reenableAllConfirmButton.setToolTipText("All hidden confimation dialogs be made visible again");
        reenableAllConfirmButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // clear the saved tool tips preference to re-enable them
                GeneralPreferences.getPreferencesConfirms().removeChildren();
                GeneralPreferences.getPreferencesConfirms().clear();
                exitConfirm.setSelected(true);

                MessageDialog.showDialog("All confirmation dialogs are now enabled again !");
            }
        });

        load();

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(GuiUtil.createLineBoxPanel(alwaysOnTopCheckBox, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(exitConfirm, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(autoUpdateCheckBox, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(autoCheckUpdateCheckBox, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(sequencePersistence, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(18));
        topPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" GUI font size  "), uiFontSizeSpinner,
                Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" Max memory  "), maxMemoryMBSpinner, new JLabel(" MB  (<= "
                + maxMemLimit + " MB)"), Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));

        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        bottomPanel.add(GuiUtil.createLineBoxPanel(reenableAllToolTipButton, Box.createHorizontalStrut(8),
                reenableAllConfirmButton, Box.createHorizontalGlue()));
        bottomPanel.add(Box.createVerticalStrut(6));

        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createGlue(), BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.validate();
    }

    @Override
    protected void load()
    {
        maxMemoryMBSpinner.setValue(Integer.valueOf(GeneralPreferences.getMaxMemoryMB()));
        uiFontSizeSpinner.setValue(Integer.valueOf(GeneralPreferences.getGuiFontSize()));
        exitConfirm.setSelected(GeneralPreferences.getExitConfirm());
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
        int intValue;
        boolean booleanValue;

        intValue = ((Integer) maxMemoryMBSpinner.getValue()).intValue();
        // launcher setting modified, restart needed
        if (GeneralPreferences.getMaxMemoryMB() != intValue)
            getPreferenceFrame().setNeedRestart();
        GeneralPreferences.setMaxMemoryMB(intValue);

        intValue = ((Integer) uiFontSizeSpinner.getValue()).intValue();
        LookAndFeelUtil.setFontSize(intValue);
        GeneralPreferences.setGuiFontSize(intValue);

        GeneralPreferences.setExitConfirm(exitConfirm.isSelected());
        GeneralPreferences.setSequencePersistence(sequencePersistence.isSelected());
        GeneralPreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        GeneralPreferences.setAutomaticCheckUpdate(autoCheckUpdateCheckBox.isSelected());

        booleanValue = alwaysOnTopCheckBox.isSelected();
        Icy.getMainInterface().setAlwaysOnTop(booleanValue);
        GeneralPreferences.setAlwaysOnTop(booleanValue);
    }

}
