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
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Stephane
 */
public class ImageLoaderDialog extends JFileChooser implements PropertyChangeListener, Runnable
{
    public static class AllImageFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return !Loader.canDiscardImageFile(file.getName());
        }

        @Override
        public String getDescription()
        {
            return "All images file";
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 347950414244936110L;

    private static final String PREF_ID = "frame/imageLoader";

    // GUI
    private final ImageLoaderOptionPanel optionPanel;

    // internal
    private String fileId;

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
        addChoosableFileFilter(new AllImageFileFilter());

        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // setting GUI
        optionPanel = new ImageLoaderOptionPanel(preferences.getBoolean("separate", false), preferences.getBoolean(
                "autoOrder", true));

        setAccessory(optionPanel);
        updateOptionPanel();

        fileId = null;

        // listen file filter change
        addPropertyChangeListener(this);

        setDialogTitle("ICY - Load image file");

        // display loader
        final int value = showOpenDialog(Icy.getMainInterface().getMainFrame());

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // store current path
            preferences.put("path", getCurrentDirectory().getAbsolutePath());
            preferences.putBoolean("separate", optionPanel.isSeparateSequenceSelected());
            preferences.putBoolean("autoOrder", optionPanel.isAutoOrderSelected());
            Loader.load(CollectionUtil.asList(getSelectedFiles()), optionPanel.isSeparateSequenceSelected(), optionPanel.isAutoOrderSelected(), true);
        }

        // store interface option
        preferences.putInt("width", getWidth());
        preferences.putInt("height", getHeight());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String prop = evt.getPropertyName();

        if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        {
            File f = (File) evt.getNewValue();

            if (f != null && (f.isDirectory() || !f.exists()))
                f = null;

            if (f != null)
            {
                fileId = f.getAbsolutePath();
                // refresh preview
                ThreadUtil.bgRunSingle(this);
            }
        }

        // setting state
        updateOptionPanel();
    }

    void updateOptionPanel()
    {
        final boolean multi = getSelectedFiles().length > 1;

        optionPanel.setSeparateSequenceEnabled(multi);
        optionPanel.setAutoOrderEnabled(multi);
    }

    @Override
    public void run()
    {
        optionPanel.updatePreview(fileId);
    }
}
