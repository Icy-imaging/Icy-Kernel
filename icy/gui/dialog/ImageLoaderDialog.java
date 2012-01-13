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
package icy.gui.dialog;

import icy.file.FileFormat;
import icy.file.Loader;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import loci.formats.gui.PreviewPane;

/**
 * @author Stephane
 */
public class ImageLoaderDialog extends JFileChooser
{
    /**
     * 
     */
    private static final long serialVersionUID = 347950414244936110L;

    private static final String PREF_ID = "frame/imageLoader";

    // GUI
    final JCheckBox separateSeqCheck;
    final JPanel separateSeqPanel;

    /**
     * 
     */
    public ImageLoaderDialog()
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);

        // can't use WindowsPositionSaver as JFileChooser is a fake JComponent
        // only dimension is stored
        setCurrentDirectory(new File(preferences.get("path", "")));
        setPreferredSize(new Dimension(preferences.getInt("width", 600), preferences.getInt("height", 400)));

        removeChoosableFileFilter(getAcceptAllFileFilter());
        addChoosableFileFilter(FileFormat.TIFF.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.JPG.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.PNG.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.LSM.getExtensionFileFilter());
        addChoosableFileFilter(FileFormat.AVI.getExtensionFileFilter());
        // so we have AllFileFilter selected and in last position
        addChoosableFileFilter(getAcceptAllFileFilter());

        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // setting GUI
        separateSeqCheck = new JCheckBox();
        separateSeqCheck.setSelected(false);
        separateSeqPanel = GuiUtil.createLineBoxPanel(new JLabel("Load in separated sequence"),
                Box.createHorizontalGlue(), separateSeqCheck);

        // preview pane
        final PreviewPane pp = new PreviewPane(this);

        final JPanel settingPanel = new JPanel();
        settingPanel.setBorder(BorderFactory.createTitledBorder((Border) null));
        settingPanel.setLayout(new BorderLayout());

        settingPanel.add(pp, BorderLayout.NORTH);
        settingPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        settingPanel.add(separateSeqPanel, BorderLayout.SOUTH);

        setAccessory(settingPanel);
        updateSettingPanel();

        // listen file filter change
        addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateSettingPanel();
            }
        });

        setDialogTitle("ICY - Load image file");

        // display loader
        final int value = showOpenDialog(Icy.getMainInterface().getMainFrame());

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // store current path
            preferences.put("path", getCurrentDirectory().getAbsolutePath());
            Loader.load(Arrays.asList(getSelectedFiles()), separateSeqCheck.isSelected());
        }

        try
        {
            // close opened files by PreviewPane
            pp.close();
        }
        catch (IOException e)
        {
            // ignore
        }

        // store interface option
        preferences.putInt("width", getWidth());
        preferences.putInt("height", getHeight());
    }

    void updateSettingPanel()
    {
        separateSeqCheck.setEnabled(getSelectedFiles().length > 1);
    }
}
