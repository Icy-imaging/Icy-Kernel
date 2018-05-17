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

import icy.gui.component.IcyTextField;
import icy.gui.dialog.MessageDialog;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.system.SystemUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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
    private final JCheckBox saveNewSequence;
    final JCheckBox autoUpdateCheckBox;
    private final JCheckBox alwaysOnTopCheckBox;
    private final JCheckBox usageStatistics;
    private final JSpinner maxMemoryMBSpinner;
    private final JSpinner uiFontSizeSpinner;
    private final JSpinner cacheMemoryPercent;
    final IcyTextField cachePath;
    private final JButton setCachePathButton;
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
        sequencePersistence.setToolTipText(
                "Enable the XML persistence for sequence (file is automatically loaded/saved when sequence is opened/closed)");
        saveNewSequence = new JCheckBox("Ask to save new sequence when closing them");
        autoUpdateCheckBox = new JCheckBox("Enable application update");
        autoUpdateCheckBox.setToolTipText("Enable automatic update for application as soon a new version is available");
        alwaysOnTopCheckBox = new JCheckBox("Application window always on top");
        usageStatistics = new JCheckBox("Usage statistics report");
        usageStatistics.setToolTipText(
                "Report is 100% anonymous, very light on network trafic and help developers so keep it enabled please :)");

        final int maxMemLimit = (int) MathUtil.prevMultiple(ApplicationPreferences.getMaxMemoryMBLimit(), 32);
        maxMemoryMBSpinner = new JSpinner(new SpinnerNumberModel(128, 64, maxMemLimit, 32));
        maxMemoryMBSpinner.setToolTipText("Change the maximum memory available for application");

        cacheMemoryPercent = new JSpinner(new SpinnerNumberModel(40, 10, 80, 5));
        cacheMemoryPercent.setToolTipText(
                "Change the memory portion allocated for image data caching (higher value allow faster image processing but less memory for other taks)");

        cachePath = new IcyTextField();
        cachePath.setToolTipText("Folder used to store cache data (better to use fast SSD storage location)");
        cachePath.setColumns(10);

        setCachePathButton = new JButton("...");
        setCachePathButton.setPreferredSize(new Dimension(32, 20));
        setCachePathButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final JFileChooser fc = new JFileChooser();

                // start at application current directory
                fc.setCurrentDirectory(new File(cachePath.getText()));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fc.showSaveDialog(GeneralPreferencePanel.this) == JFileChooser.APPROVE_OPTION)
                    cachePath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

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

        String maxMemoryMess;
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(GuiUtil.createLineBoxPanel(alwaysOnTopCheckBox, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(exitConfirm, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(autoUpdateCheckBox, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(sequencePersistence, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(saveNewSequence, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(GuiUtil.createLineBoxPanel(usageStatistics, Box.createHorizontalGlue()));
        topPanel.add(Box.createVerticalStrut(18));

        maxMemoryMess = " MB  (max = " + maxMemLimit + " MB";
        if (SystemUtil.is32bits() && ((SystemUtil.getTotalMemory() / (1024 * 1024)) >= 1500))
            maxMemoryMess += " - use 64bit JVM to allow more)";
        else
            maxMemoryMess += ")";

        topPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" Max memory  "), maxMemoryMBSpinner,
                new JLabel(maxMemoryMess), Box.createHorizontalStrut(16), Box.createHorizontalGlue(),
                new JLabel(" Cache reserved  "), cacheMemoryPercent, new JLabel("%"), Box.createHorizontalStrut(4)));
        topPanel.add(Box.createVerticalStrut(2));
        topPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" Cache path     "), cachePath, Box.createHorizontalStrut(4), setCachePathButton,
                Box.createHorizontalStrut(4)));
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(GuiUtil.createLineBoxPanel(new JLabel(" GUI font size  "), uiFontSizeSpinner,
                Box.createHorizontalGlue()));
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
        maxMemoryMBSpinner.setValue(Integer.valueOf(ApplicationPreferences.getMaxMemoryMB()));
        cacheMemoryPercent.setValue(Integer.valueOf(ApplicationPreferences.getCacheMemoryPercent()));
        cachePath.setText(ApplicationPreferences.getCachePath());
        uiFontSizeSpinner.setValue(Integer.valueOf(GeneralPreferences.getGuiFontSize()));
        exitConfirm.setSelected(GeneralPreferences.getExitConfirm());
        sequencePersistence.setSelected(GeneralPreferences.getSequencePersistence());
        saveNewSequence.setSelected(GeneralPreferences.getSaveNewSequence());
        autoUpdateCheckBox.setSelected(GeneralPreferences.getAutomaticUpdate());
        alwaysOnTopCheckBox.setSelected(GeneralPreferences.getAlwaysOnTop());
        usageStatistics.setSelected(GeneralPreferences.getUsageStatisticsReport());
    }

    @Override
    protected void save()
    {
        int intValue;
        boolean booleanValue;
        String stringValue;

        intValue = ((Integer) maxMemoryMBSpinner.getValue()).intValue();
        // launcher setting modified, restart needed
        if (ApplicationPreferences.getMaxMemoryMB() != intValue)
            getPreferenceFrame().setNeedRestart();
        ApplicationPreferences.setMaxMemoryMB(intValue);

        intValue = ((Integer) cacheMemoryPercent.getValue()).intValue();
        // launcher setting modified, restart needed
        if (ApplicationPreferences.getCacheMemoryPercent() != intValue)
            getPreferenceFrame().setNeedRestart();
        ApplicationPreferences.setCacheMemoryPercent(intValue);

        stringValue = cachePath.getText();
        if (ApplicationPreferences.getCachePath() != stringValue)
            getPreferenceFrame().setNeedRestart();
        ApplicationPreferences.setCachePath(stringValue);

        intValue = ((Integer) uiFontSizeSpinner.getValue()).intValue();
        LookAndFeelUtil.setFontSize(intValue);
        GeneralPreferences.setGuiFontSize(intValue);

        GeneralPreferences.setExitConfirm(exitConfirm.isSelected());
        GeneralPreferences.setSequencePersistence(sequencePersistence.isSelected());
        GeneralPreferences.setSaveNewSequence(saveNewSequence.isSelected());
        GeneralPreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        GeneralPreferences.setUsageStatisticsReport(usageStatistics.isSelected());

        booleanValue = alwaysOnTopCheckBox.isSelected();
        Icy.getMainInterface().setAlwaysOnTop(booleanValue);
        GeneralPreferences.setAlwaysOnTop(booleanValue);
    }
}
