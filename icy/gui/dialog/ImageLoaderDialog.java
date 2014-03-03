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
package icy.gui.dialog;

import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Stephane
 */
public class ImageLoaderDialog extends JFileChooser implements PropertyChangeListener
{
    public static class AllImagesFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            if (file.isDirectory()) return true;
            
            return !Loader.canDiscardImageFile(file.getName());
        }

        @Override
        public String getDescription()
        {
            return "All images file";
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static class AllImageFileFilter extends AllImagesFileFilter
    {

    }

    /**
     * 
     */
    private static final long serialVersionUID = 347950414244936110L;

    private static final String PREF_ID = "frame/imageLoader";

    private static final String ID_WIDTH = "width";
    private static final String ID_HEIGTH = "heigth";
    private static final String ID_PATH = "path";
    private static final String ID_SEPARATE = "separate";
    private static final String ID_AUTOORDER = "autoOrder";
    private static final String ID_EXTENSION = "extension";

    // GUI
    private final ImageLoaderOptionPanel optionPanel;

    /**
     * <b>Image Loader Dialog</b><br>
     * <br>
     * Display a dialog to select image file(s) and load them.<br>
     * <br>
     * To only get selected image files from the dialog you must do:<br>
     * <code> ImageLoaderDialog dialog = new ImageLoaderDialog(false);</code><br>
     * <code> File[] selectedFiles = dialog.getSelectedFiles()</code><br>
     * <br>
     * To directly load selected image files just use:<br>
     * <code>new ImageLoaderDialog(true);</code><br>
     * or<br>
     * <code>new ImageLoaderDialog();</code>
     * 
     * @param autoLoad
     *        If true the selected image(s) are automatically loaded.
     */
    public ImageLoaderDialog(boolean autoLoad)
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
        final List<SequenceFileImporter> importers = Loader.getSequenceFileImporters();

        // can't use WindowsPositionSaver as JFileChooser is a fake JComponent
        // only dimension is stored
        setCurrentDirectory(new File(preferences.get(ID_PATH, "")));
        setPreferredSize(new Dimension(preferences.getInt(ID_WIDTH, 600), preferences.getInt(ID_HEIGTH, 400)));

        setAcceptAllFileFilterUsed(false);
        resetChoosableFileFilters();

        // add file filter from importers
        for (SequenceFileImporter importer : importers)
            for (FileFilter filter : importer.getFileFilters())
                addChoosableFileFilter(filter);

        // set last used file filter
        setFileFilter(getFileFilter(preferences.get(ID_EXTENSION, "")));

        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // setting GUI
        optionPanel = new ImageLoaderOptionPanel(preferences.getBoolean(ID_SEPARATE, false), preferences.getBoolean(
                ID_AUTOORDER, true));

        setAccessory(optionPanel);
        updateOptionPanel(false);

        // listen file filter change
        addPropertyChangeListener(this);

        setDialogTitle("Load image file");

        // display loader
        final int value = showOpenDialog(Icy.getMainInterface().getMainFrame());

        // cancel preview refresh (for big file)
        optionPanel.cancelPreview();

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // store current path
            preferences.put(ID_PATH, getCurrentDirectory().getAbsolutePath());
            preferences.putBoolean(ID_SEPARATE, isSeparateSequenceSelected());
            preferences.putBoolean(ID_AUTOORDER, isAutoOrderSelected());
            preferences.put(ID_EXTENSION, getFileFilter().getDescription());

            // load if requested
            if (autoLoad)
            {
                // get the selected imported from file filter
                final SequenceFileImporter importer = getImporter(importers, getFileFilterIndex());
                // load selected file(s)
                Loader.load(importer, CollectionUtil.asList(FileUtil.toPaths(getSelectedFiles())),
                        isSeparateSequenceSelected(), isAutoOrderSelected(), true);
            }
        }

        // store interface option
        preferences.putInt(ID_WIDTH, getWidth());
        preferences.putInt(ID_HEIGTH, getHeight());
    }

    protected FileFilter getFileFilter(String description)
    {
        final FileFilter[] filters = getChoosableFileFilters();

        for (FileFilter filter : filters)
            if (StringUtil.equals(filter.getDescription(), description))
                return filter;

        // take first filter by default
        if (filters.length > 0)
            return filters[0];

        return null;
    }

    protected int getFileFilterIndex()
    {
        final FileFilter[] filters = getChoosableFileFilters();
        final FileFilter filter = getFileFilter();

        for (int i = 0; i < filters.length; i++)
            if (filter == filters[i])
                return i;

        return -1;
    }

    /**
     * <b>Image Loader Dialog</b><br>
     * <br>
     * Display a dialog to select image file(s) and load them.
     */
    public ImageLoaderDialog()
    {
        this(true);
    }

    private SequenceFileImporter getImporter(List<SequenceFileImporter> importers, int filterIndex)
    {
        int i = 0;
        for (SequenceFileImporter importer : importers)
            for (FileFilter filter : importer.getFileFilters())
                if (i++ == filterIndex)
                    return importer;

        return null;
    }

    /**
     * Returns true if user checked the "Separate sequence" option.
     */
    public boolean isSeparateSequenceSelected()
    {
        return optionPanel.isSeparateSequenceSelected();
    }

    /**
     * Returns true if user checked the "Auto Ordering" option.
     */
    public boolean isAutoOrderSelected()
    {
        return optionPanel.isAutoOrderSelected();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String prop = evt.getPropertyName();

        if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        {
            File f = (File) evt.getNewValue();

            if ((f != null) && (f.isDirectory() || !f.exists()))
                f = null;

            // refresh preview
            if (f != null)
                optionPanel.updatePreview(f.getAbsolutePath());

            updateOptionPanel((f != null) && f.isDirectory());
        }
        else
            updateOptionPanel(getSelectedFiles().length > 1);
    }

    protected void updateOptionPanel(boolean multi)
    {
        optionPanel.setSeparateSequenceEnabled(multi);
        optionPanel.setAutoOrderEnabled(multi);
    }
}
