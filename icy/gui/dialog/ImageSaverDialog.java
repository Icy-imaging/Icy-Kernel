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
import icy.file.ImageFileFormat;
import icy.file.Saver;
import icy.gui.component.RangeComponent;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import loci.formats.IFormatWriter;
import loci.formats.gui.ExtensionFileFilter;

/**
 * @author Stephane
 */
public class ImageSaverDialog extends JFileChooser
{
    /**
     * 
     */
    private static final long serialVersionUID = 8771369900303584478L;

    private static final String PREF_ID = "frame/imageSaver";

    private static final String ID_WIDTH = "width";
    private static final String ID_HEIGHT = "height";
    private static final String ID_PATH = "path";
    private static final String ID_MULTIPLEFILE = "multipleFile";
    private static final String ID_OVERWRITENAME = "overwriteName";
    private static final String ID_FILETYPE = "fileType";

    // GUI
    final JCheckBox multipleFileCheck;
    final JPanel multiplesFilePanel;

    final JSpinner fpsSpinner;
    final JPanel fpsPanel;

    final JSpinner zSpinner;
    final JPanel zPanel;
    final JSpinner tSpinner;
    final JPanel tPanel;

    final RangeComponent zRange;
    final JPanel zRangePanel;
    final RangeComponent tRange;
    final JPanel tRangePanel;

    final JCheckBox overwriteNameCheck;
    final JPanel overwriteNamePanel;

    // internal
    final boolean singleZ;
    final boolean singleT;
    final boolean singleImage;

    /**
     * <b>Image Saver Dialog</b><br>
     * <br>
     * Display a dialog to select the destination file then save the specified sequence.<br>
     * <br>
     * To only get selected file from the dialog you must do:<br>
     * <code> ImageSaverDialog dialog = new ImageSaverDialog(sequence, 0, 0, false);</code><br>
     * <code> File selectedFile = dialog.getSelectedFile()</code><br>
     * <br>
     * To directly save specified sequence to the selected file just use:<br>
     * <code>new ImageSaverDialog(sequence, 0, 0, true);</code><br>
     * or<br>
     * <code>new ImageSaverDialog(sequence, 0, 0);</code>
     * 
     * @param sequence
     *        The {@link Sequence} we want to save.
     * @param defZ
     *        default Z slice to save if output format support only single slice image.
     * @param defT
     *        default T frame to save if output format support only single frame image.
     * @param autoSave
     *        If true the sequence is automatically saved to selected file.
     */
    public ImageSaverDialog(Sequence sequence, int defZ, int defT, boolean autoSave)
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);

        singleZ = (sequence.getSizeZ() == 1);
        singleT = (sequence.getSizeT() == 1);
        singleImage = singleZ && singleT;

        // can't use WindowsPositionSaver as JFileChooser is a fake JComponent
        // only dimension is stored
        setCurrentDirectory(new File(preferences.get(ID_PATH, "")));
        setPreferredSize(new Dimension(preferences.getInt(ID_WIDTH, 600), preferences.getInt(ID_HEIGHT, 400)));

        setDialogTitle("Save image file");

        // remove default filter
        removeChoosableFileFilter(getAcceptAllFileFilter());
        // then add our supported save format
        addChoosableFileFilter(ImageFileFormat.TIFF.getExtensionFileFilter());
        addChoosableFileFilter(ImageFileFormat.PNG.getExtensionFileFilter());
        addChoosableFileFilter(ImageFileFormat.JPG.getExtensionFileFilter());
        addChoosableFileFilter(ImageFileFormat.AVI.getExtensionFileFilter());
        setFileFilter(getChoosableFileFilters()[preferences.getInt(ID_FILETYPE, 0)]);

        setMultiSelectionEnabled(false);
        // setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // so the filename information is not lost when changing directory 
        setFileSelectionMode(JFileChooser.FILES_ONLY);

        String filename = FileUtil.getFileName(sequence.getFilename(), true);
        // empty filename --> use sequence name as default filename
        if (StringUtil.isEmpty(filename))
            filename = sequence.getName();
        if (!StringUtil.isEmpty(filename))
        {
            // test if filename has already a valid extension
            final String ext = getDialogExtension(filename);
            // remove file extension
            if (ext != null)
                FileUtil.setExtension(filename, "");
            // set dialog filename
            setSelectedFile(new File(filename));
        }

        multipleFileCheck = new JCheckBox();
        multipleFileCheck.setToolTipText("Save each sequence image in a separate file");
        multipleFileCheck.setSelected(preferences.getBoolean(ID_MULTIPLEFILE, false));
        multipleFileCheck.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent arg0)
            {
                updateSettingPanel();
            }
        });
        multiplesFilePanel = GuiUtil.createLineBoxPanel(new JLabel("Save as multiple files "),
                Box.createHorizontalGlue(), multipleFileCheck);

        fpsSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 99, 1));
        fpsPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("Frame per second ", 100),
                Box.createHorizontalGlue(), fpsSpinner);

        zSpinner = new JSpinner(new SpinnerNumberModel((defZ == -1) ? 0 : defZ, 0, sequence.getSizeZ() - 1, 1));
        zPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("Z slice ", 100), Box.createHorizontalGlue(),
                zSpinner);
        tSpinner = new JSpinner(new SpinnerNumberModel((defT == -1) ? 0 : defT, 0, sequence.getSizeT() - 1, 1));
        tPanel = GuiUtil.createLineBoxPanel(GuiUtil.createFixedWidthLabel("T slice ", 100), Box.createHorizontalGlue(),
                tSpinner);

        zRange = new RangeComponent(0, sequence.getSizeZ() - 1, 1);
        zRange.setSliderVisible(false);
        zRangePanel = GuiUtil.createLineBoxPanel(new JLabel("Z range "), Box.createHorizontalGlue(), zRange);
        tRange = new RangeComponent(0, sequence.getSizeT() - 1, 1);
        tRange.setSliderVisible(false);
        tRangePanel = GuiUtil.createLineBoxPanel(new JLabel("T range "), Box.createHorizontalGlue(), tRange);

        overwriteNameCheck = new JCheckBox();
        overwriteNameCheck.setToolTipText("Overwrite metadata name with filename");
        overwriteNameCheck.setSelected(preferences.getBoolean(ID_OVERWRITENAME, true));
        overwriteNamePanel = GuiUtil.createLineBoxPanel(new JLabel("Overwrite metadata name "),
                Box.createHorizontalGlue(), overwriteNameCheck);

        final JPanel settingPanel = new JPanel();
        settingPanel.setBorder(BorderFactory.createTitledBorder((Border) null));
        settingPanel.setLayout(new BorderLayout());

        settingPanel.add(
                GuiUtil.createPageBoxPanel(multiplesFilePanel, fpsPanel, zPanel, tPanel, zRangePanel, tRangePanel),
                BorderLayout.NORTH);
        settingPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        settingPanel.add(overwriteNamePanel, BorderLayout.SOUTH);

        setAccessory(settingPanel);
        updateSettingPanel();

        // listen file filter change
        addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateSettingPanel();
            }
        });

        // display loader
        final int value = showSaveDialog(Icy.getMainInterface().getMainFrame());

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            final IFormatWriter writer = Saver.getWriter(getSelectedFileFormat());

            // Choose writer should be compatible
            if (Saver.isCompatible(writer, sequence.getColorModel()))
            {
                File file = getSelectedFile();
                final String outFilename = file.getAbsolutePath();

                // destination is a folder ?
                if (isFolderRequired())
                {
                    // test if filename has a image file extension
                    final String ext = getDialogExtension(outFilename);
                    // remove extension
                    if (ext != null)
                    {
                        file = new File(FileUtil.setExtension(outFilename, ""));
                        // set it so we can get it from getSelectedFile()
                        setSelectedFile(file);
                    }
                }
                else
                {
                    // test and add extension if needed
                    final ExtensionFileFilter extensionFilter = (ExtensionFileFilter) getFileFilter();

                    // add file filter extension to filename if not already present
                    if (!hasExtension(outFilename.toLowerCase(), extensionFilter))
                    {
                        file = new File(outFilename + "." + extensionFilter.getExtension());
                        // set it so we can get it from getSelectedFile()
                        setSelectedFile(file);
                    }
                }

                // save requested ?
                if (autoSave)
                {
                    // ask for confirmation as file already exists
                    if (!file.exists() || ConfirmDialog.confirm("Overwrite existing file(s) ?"))
                    {
                        if (file.exists())
                            FileUtil.delete(file, true);

                        // store current path
                        preferences.put(ID_PATH, getCurrentDirectory().getAbsolutePath());

                        // overwrite sequence name with filename
                        if (isOverwriteNameEnabled())
                            sequence.setName(FileUtil.getFileName(file.getAbsolutePath(), false));

                        final Sequence s = sequence;
                        final File f = file;

                        // do save in background process
                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Saver.save(writer, s, f, getZMin(), getZMax(), getTMin(), getTMax(), getFps(),
                                        isSaveAsMultipleFilesEnabled(), true, true);
                            }
                        });
                    }
                }
            }
            else
            {
                // incompatible saver for this sequence
                new IncompatibleImageFormatDialog();
            }

            // store interface option
            preferences.putInt(ID_WIDTH, getWidth());
            preferences.putInt(ID_HEIGHT, getHeight());
            preferences.putBoolean(ID_MULTIPLEFILE, multipleFileCheck.isSelected());
            preferences.putBoolean(ID_OVERWRITENAME, overwriteNameCheck.isSelected());
            preferences.putInt(ID_FILETYPE, CollectionUtil.asList(getChoosableFileFilters()).indexOf(getFileFilter()));
        }
    }

    /**
     * <b>Image Saver Dialog</b><br>
     * <br>
     * Display a dialog to select the destination file then save the specified sequence.<br>
     * 
     * @param sequence
     *        The {@link Sequence} we want to save.
     * @param defZ
     *        default Z slice to save if output format support only single slice image.
     * @param defT
     *        default T frame to save if output format support only single frame image.
     */
    public ImageSaverDialog(Sequence sequence, int defZ, int defT)
    {
        this(sequence, defZ, defT, true);
    }

    /**
     * <b>Image Saver Dialog</b><br>
     * <br>
     * Display a dialog to select the destination file then save the specified sequence.<br>
     * <br>
     * To only get selected file from the dialog you must do:<br>
     * <code> ImageSaverDialog dialog = new ImageSaverDialog(sequence, false);</code><br>
     * <code> File selectedFile = dialog.getSelectedFile()</code><br>
     * <br>
     * To directly save specified sequence to the selected file just use:<br>
     * <code>new ImageSaverDialog(sequence, true);</code><br>
     * or<br>
     * <code>new ImageSaverDialog(sequence);</code>
     * 
     * @param sequence
     *        The {@link Sequence} we want to save.
     * @param autoSave
     *        If true the sequence is automatically saved to selected file.
     */
    public ImageSaverDialog(Sequence sequence, boolean autoSave)
    {
        this(sequence, 0, 0, autoSave);
    }

    /**
     * <b>Image Saver Dialog</b><br>
     * <br>
     * Display a dialog to select the destination file then save the specified sequence.
     * 
     * @param sequence
     *        The {@link Sequence} we want to save.
     */
    public ImageSaverDialog(Sequence sequence)
    {
        this(sequence, 0, 0, true);
    }

    private boolean hasExtension(String name, ExtensionFileFilter extensionFilter)
    {
        return getExtension(name, extensionFilter) != null;
    }

    private String getExtension(String name, ExtensionFileFilter extensionFilter)
    {
        for (String ext : extensionFilter.getExtensions())
            if (name.endsWith(ext.toLowerCase()))
                return ext;

        return null;
    }

    private String getDialogExtension(String name)
    {
        for (FileFilter filter : getChoosableFileFilters())
        {
            final String ext = getExtension(name, (ExtensionFileFilter) filter);

            if (ext != null)
                return ext;
        }

        return null;
    }

    public ImageFileFormat getSelectedFileFormat()
    {
        final FileFilter ff = getFileFilter();

        // default
        if ((ff == null) || !(ff instanceof ExtensionFileFilter))
            return ImageFileFormat.TIFF;

        return ImageFileFormat.getWriteFormat(((ExtensionFileFilter) ff).getExtension(), ImageFileFormat.TIFF);
    }

    /**
     * Returns <code>true</code> if we require a folder to save the sequence with selected options.
     */
    public boolean isFolderRequired()
    {
        final int sizeT = (getTMax() - getTMin()) + 1;
        final int sizeZ = (getZMax() - getZMin()) + 1;
        final int numImages = sizeT * sizeZ;

        return (numImages > 1) && isSaveAsMultipleFilesEnabled();
    }

    /**
     * Returns <code>true</code> if user chosen to save the sequence as multiple image files.
     */
    public boolean isSaveAsMultipleFilesEnabled()
    {
        return multiplesFilePanel.isVisible() && multipleFileCheck.isSelected();
    }

    /**
     * Returns <code>true</code> if user chosen to overwrite the sequence internal name by filename.
     */
    public boolean isOverwriteNameEnabled()
    {
        return overwriteNameCheck.isSelected();
    }

    /**
     * Returns the minimum Z of the selected Z range to save.
     */
    public int getZMin()
    {
        if (zPanel.isVisible())
            return ((Integer) zSpinner.getValue()).intValue();

        if (zRangePanel.isVisible())
            return (int) zRange.getLow();

        return 0;
    }

    /**
     * Returns the maximum Z of the selected Z range to save.
     */
    public int getZMax()
    {
        if (zPanel.isVisible())
            return ((Integer) zSpinner.getValue()).intValue();

        if (zRangePanel.isVisible())
            return (int) zRange.getHigh();

        return 0;
    }

    /**
     * Returns the minimum T of the selected T range to save.
     */
    public int getTMin()
    {
        if (tPanel.isVisible())
            return ((Integer) tSpinner.getValue()).intValue();

        if (tRangePanel.isVisible())
            return (int) tRange.getLow();

        return 0;
    }

    /**
     * Returns the maximum T of the selected T range to save.
     */
    public int getTMax()
    {
        if (tPanel.isVisible())
            return ((Integer) tSpinner.getValue()).intValue();

        if (tRangePanel.isVisible())
            return (int) tRange.getHigh();

        return 0;
    }

    /**
     * Returns the desired FPS (Frame Per Second, only for AVI file).
     */
    public int getFps()
    {
        if (fpsPanel.isVisible())
            return ((Integer) fpsSpinner.getValue()).intValue();

        return 1;
    }

    void updateSettingPanel()
    {
        final ImageFileFormat fileFormat = getSelectedFileFormat();

        final boolean tif = (fileFormat == ImageFileFormat.TIFF);
        final boolean jpg = (fileFormat == ImageFileFormat.JPG);
        final boolean avi = (fileFormat == ImageFileFormat.AVI);

        if (singleImage)
        {
            // single image, no need to display selection option
            multiplesFilePanel.setVisible(false);
            zPanel.setVisible(false);
            zRangePanel.setVisible(false);
            tPanel.setVisible(false);
            tRangePanel.setVisible(false);
        }
        else
        {
            multiplesFilePanel.setVisible(!avi);

            if (multiplesFilePanel.isVisible() && multipleFileCheck.isSelected())
            {
                // save as multiple file so display range option
                zPanel.setVisible(false);
                zRangePanel.setVisible(true && !singleZ);
                tPanel.setVisible(false);
                tRangePanel.setVisible(true && !singleT);
            }
            else
            {
                // save as single file so type give restriction here
                zPanel.setVisible(!tif && !singleZ);
                zRangePanel.setVisible(tif && !singleZ);
                tPanel.setVisible(jpg && !singleT);
                tRangePanel.setVisible(!jpg && !singleT);
            }
        }

        fpsPanel.setVisible(avi);
    }
}
