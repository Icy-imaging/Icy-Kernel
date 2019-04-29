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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import icy.gui.component.IcyTextField;
import icy.gui.dialog.MessageDialog;
import icy.math.MathUtil;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.system.SystemUtil;
import icy.util.StringUtil;

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
    JCheckBox exitConfirm;
    private JCheckBox sequencePersistence;
    private JCheckBox saveNewSequence;
    JCheckBox autoUpdateCheckBox;
    private JCheckBox usageStatistics;
    private JSpinner maxMemoryMBSpinner;
    private JLabel maxMemoryMBLabel;
    private JSpinner cacheMemoryPercent;
    IcyTextField cachePath;
    private JButton setCachePathButton;
    private JButton reenableAllToolTipButton;
    private JButton reenableAllConfirmButton;
    private JSeparator separator;
    private JLabel label;
    private JPanel panel;

    /**
     * @param parent
     */
    GeneralPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        final int maxMemMB = (int) MathUtil.prevMultiple(ApplicationPreferences.getMaxMemoryMBLimit(), 32);

        initializeGUI(maxMemMB);

        String maxMemoryMess = " MB  (max = " + maxMemMB + " MB";
        if (SystemUtil.is32bits() && ((SystemUtil.getTotalMemory() / (1024 * 1024)) >= 1500))
            maxMemoryMess += " - use 64bit JVM to allow more)";
        else
            maxMemoryMess += ")";

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

        load();
    }

    private void initializeGUI(int maxMemMB)
    {
        GridBagLayout gbl_mainPanel = new GridBagLayout();
        gbl_mainPanel.columnWidths = new int[] {200, 0, 80, 0, 4, 0};
        gbl_mainPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 8, 0};
        gbl_mainPanel.columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_mainPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                Double.MIN_VALUE};
        mainPanel.setLayout(gbl_mainPanel);

        exitConfirm = new JCheckBox("Show confirmation when exiting application");
        GridBagConstraints gbc_exitConfirm = new GridBagConstraints();
        gbc_exitConfirm.anchor = GridBagConstraints.WEST;
        gbc_exitConfirm.gridwidth = 4;
        gbc_exitConfirm.insets = new Insets(0, 0, 5, 5);
        gbc_exitConfirm.gridx = 0;
        gbc_exitConfirm.gridy = 0;
        mainPanel.add(exitConfirm, gbc_exitConfirm);
        autoUpdateCheckBox = new JCheckBox("Enable application update");
        GridBagConstraints gbc_autoUpdateCheckBox = new GridBagConstraints();
        gbc_autoUpdateCheckBox.anchor = GridBagConstraints.WEST;
        gbc_autoUpdateCheckBox.gridwidth = 4;
        gbc_autoUpdateCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_autoUpdateCheckBox.gridx = 0;
        gbc_autoUpdateCheckBox.gridy = 1;
        mainPanel.add(autoUpdateCheckBox, gbc_autoUpdateCheckBox);
        autoUpdateCheckBox.setToolTipText("Enable automatic update for application as soon a new version is available");
        sequencePersistence = new JCheckBox("Enable sequence persistence");
        GridBagConstraints gbc_sequencePersistence = new GridBagConstraints();
        gbc_sequencePersistence.anchor = GridBagConstraints.WEST;
        gbc_sequencePersistence.gridwidth = 4;
        gbc_sequencePersistence.insets = new Insets(0, 0, 5, 5);
        gbc_sequencePersistence.gridx = 0;
        gbc_sequencePersistence.gridy = 2;
        mainPanel.add(sequencePersistence, gbc_sequencePersistence);
        sequencePersistence.setToolTipText(
                "Enable the XML persistence for sequence (file is automatically loaded/saved when sequence is opened/closed)");
        saveNewSequence = new JCheckBox("Ask to save new sequence when closing them");
        GridBagConstraints gbc_saveNewSequence = new GridBagConstraints();
        gbc_saveNewSequence.anchor = GridBagConstraints.WEST;
        gbc_saveNewSequence.gridwidth = 4;
        gbc_saveNewSequence.insets = new Insets(0, 0, 5, 5);
        gbc_saveNewSequence.gridx = 0;
        gbc_saveNewSequence.gridy = 3;
        mainPanel.add(saveNewSequence, gbc_saveNewSequence);
        usageStatistics = new JCheckBox("Usage statistics report");
        GridBagConstraints gbc_usageStatistics = new GridBagConstraints();
        gbc_usageStatistics.gridwidth = 4;
        gbc_usageStatistics.anchor = GridBagConstraints.WEST;
        gbc_usageStatistics.insets = new Insets(0, 0, 5, 5);
        gbc_usageStatistics.gridx = 0;
        gbc_usageStatistics.gridy = 4;
        mainPanel.add(usageStatistics, gbc_usageStatistics);
        usageStatistics.setToolTipText(
                "Report is 100% anonymous, very light on network trafic and help developers so keep it enabled please :)");

        separator = new JSeparator();
        GridBagConstraints gbc_separator = new GridBagConstraints();
        gbc_separator.anchor = GridBagConstraints.WEST;
        gbc_separator.fill = GridBagConstraints.VERTICAL;
        gbc_separator.gridwidth = 4;
        gbc_separator.insets = new Insets(0, 0, 5, 5);
        gbc_separator.gridx = 0;
        gbc_separator.gridy = 5;
        mainPanel.add(separator, gbc_separator);

        JLabel label_1 = new JLabel(" Max memory (max = " + maxMemMB + " MB)");
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.gridwidth = 2;
        gbc_label_1.anchor = GridBagConstraints.WEST;
        gbc_label_1.insets = new Insets(0, 0, 5, 5);
        gbc_label_1.gridx = 0;
        gbc_label_1.gridy = 6;
        mainPanel.add(label_1, gbc_label_1);
        maxMemoryMBSpinner = new JSpinner(new SpinnerNumberModel(128, 64, maxMemMB, 32));
        GridBagConstraints gbc_maxMemoryMBSpinner = new GridBagConstraints();
        gbc_maxMemoryMBSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_maxMemoryMBSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_maxMemoryMBSpinner.gridx = 2;
        gbc_maxMemoryMBSpinner.gridy = 6;
        mainPanel.add(maxMemoryMBSpinner, gbc_maxMemoryMBSpinner);
        maxMemoryMBSpinner.setToolTipText("Change the maximum memory available for application");
        maxMemoryMBLabel = new JLabel(" MB");
        GridBagConstraints gbc_lblMbmax = new GridBagConstraints();
        gbc_lblMbmax.anchor = GridBagConstraints.WEST;
        gbc_lblMbmax.insets = new Insets(0, 0, 5, 5);
        gbc_lblMbmax.gridx = 3;
        gbc_lblMbmax.gridy = 6;
        mainPanel.add(maxMemoryMBLabel, gbc_lblMbmax);
        JLabel lblMemoryAllocatedFor = new JLabel(" Memory allocated for data cache ");
        lblMemoryAllocatedFor.setToolTipText(
                "Change the memory portion allocated for image data caching (higher value allow faster image processing but less memory for others taks)");
        GridBagConstraints gbc_lblMemoryAllocatedFor = new GridBagConstraints();
        gbc_lblMemoryAllocatedFor.gridwidth = 2;
        gbc_lblMemoryAllocatedFor.anchor = GridBagConstraints.WEST;
        gbc_lblMemoryAllocatedFor.insets = new Insets(0, 0, 5, 5);
        gbc_lblMemoryAllocatedFor.gridx = 0;
        gbc_lblMemoryAllocatedFor.gridy = 7;
        mainPanel.add(lblMemoryAllocatedFor, gbc_lblMemoryAllocatedFor);

        cacheMemoryPercent = new JSpinner(new SpinnerNumberModel(40, 10, 80, 5));
        GridBagConstraints gbc_cacheMemoryPercent = new GridBagConstraints();
        gbc_cacheMemoryPercent.fill = GridBagConstraints.HORIZONTAL;
        gbc_cacheMemoryPercent.insets = new Insets(0, 0, 5, 5);
        gbc_cacheMemoryPercent.gridx = 2;
        gbc_cacheMemoryPercent.gridy = 7;
        mainPanel.add(cacheMemoryPercent, gbc_cacheMemoryPercent);
        cacheMemoryPercent.setToolTipText(
                "Change the memory portion allocated for image data caching (higher value allow faster image processing but less memory for others taks)");

        label = new JLabel("%");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 3;
        gbc_label.gridy = 7;
        mainPanel.add(label, gbc_label);
        JLabel lblCacheLocation = new JLabel(" Disk data cache location");
        lblCacheLocation.setToolTipText(
                "Folder used to store image data cache (it's recommended to use fast storage location as SSD disk)");
        GridBagConstraints gbc_lblCacheLocation = new GridBagConstraints();
        gbc_lblCacheLocation.anchor = GridBagConstraints.WEST;
        gbc_lblCacheLocation.insets = new Insets(0, 0, 5, 5);
        gbc_lblCacheLocation.gridx = 0;
        gbc_lblCacheLocation.gridy = 8;
        mainPanel.add(lblCacheLocation, gbc_lblCacheLocation);

        cachePath = new IcyTextField();
        GridBagConstraints gbc_cachePath = new GridBagConstraints();
        gbc_cachePath.fill = GridBagConstraints.HORIZONTAL;
        gbc_cachePath.gridwidth = 2;
        gbc_cachePath.insets = new Insets(0, 0, 5, 5);
        gbc_cachePath.gridx = 1;
        gbc_cachePath.gridy = 8;
        mainPanel.add(cachePath, gbc_cachePath);
        cachePath.setToolTipText(
                "Folder used to store image data cache (it's recommended to use fast storage location as SSD disk)");
        cachePath.setColumns(10);

        setCachePathButton = new JButton("...");
        setCachePathButton.setPreferredSize(new Dimension(32, 23));
        GridBagConstraints gbc_setCachePathButton = new GridBagConstraints();
        gbc_setCachePathButton.anchor = GridBagConstraints.WEST;
        gbc_setCachePathButton.insets = new Insets(0, 0, 5, 5);
        gbc_setCachePathButton.gridx = 3;
        gbc_setCachePathButton.gridy = 8;
        mainPanel.add(setCachePathButton, gbc_setCachePathButton);

        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 5, 5);
        gbc_panel.gridwidth = 4;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 10;
        mainPanel.add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {16, 0, 0, 0, 16, 0};
        gbl_panel.rowHeights = new int[] {0, 0};
        gbl_panel.columnWeights = new double[] {0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        reenableAllToolTipButton = new JButton("Reactivate tooltips");
        GridBagConstraints gbc_reenableAllToolTipButton = new GridBagConstraints();
        gbc_reenableAllToolTipButton.insets = new Insets(0, 0, 0, 5);
        gbc_reenableAllToolTipButton.gridx = 1;
        gbc_reenableAllToolTipButton.gridy = 0;
        panel.add(reenableAllToolTipButton, gbc_reenableAllToolTipButton);
        reenableAllToolTipButton.setToolTipText("All hidden tooltips will be made visible again");

        reenableAllConfirmButton = new JButton("Reactivate confirmations");
        GridBagConstraints gbc_reenableAllConfirmButton = new GridBagConstraints();
        gbc_reenableAllConfirmButton.insets = new Insets(0, 0, 0, 5);
        gbc_reenableAllConfirmButton.gridx = 3;
        gbc_reenableAllConfirmButton.gridy = 0;
        panel.add(reenableAllConfirmButton, gbc_reenableAllConfirmButton);
        reenableAllConfirmButton.setToolTipText("All hidden confimation dialogs be made visible again");

        mainPanel.validate();
    }

    @Override
    protected void load()
    {
        maxMemoryMBSpinner.setValue(Integer.valueOf(ApplicationPreferences.getMaxMemoryMB()));
        cacheMemoryPercent.setValue(Integer.valueOf(ApplicationPreferences.getCacheMemoryPercent()));
        cachePath.setText(ApplicationPreferences.getCachePath());
        exitConfirm.setSelected(GeneralPreferences.getExitConfirm());
        sequencePersistence.setSelected(GeneralPreferences.getSequencePersistence());
        saveNewSequence.setSelected(GeneralPreferences.getSaveNewSequence());
        autoUpdateCheckBox.setSelected(GeneralPreferences.getAutomaticUpdate());
        usageStatistics.setSelected(GeneralPreferences.getUsageStatisticsReport());
    }

    @Override
    protected void save()
    {
        int intValue;
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
        if (!StringUtil.equals(ApplicationPreferences.getCachePath(), stringValue))
            getPreferenceFrame().setNeedRestart();
        ApplicationPreferences.setCachePath(stringValue);

        GeneralPreferences.setExitConfirm(exitConfirm.isSelected());
        GeneralPreferences.setSequencePersistence(sequencePersistence.isSelected());
        GeneralPreferences.setSaveNewSequence(saveNewSequence.isSelected());
        GeneralPreferences.setAutomaticUpdate(autoUpdateCheckBox.isSelected());
        GeneralPreferences.setUsageStatisticsReport(usageStatistics.isSelected());
    }
}
