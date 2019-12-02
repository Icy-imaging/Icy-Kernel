/**
 * 
 */
package icy.gui.dialog;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import icy.file.FileUtil;
import icy.file.ImageFileFormat;
import icy.file.Saver;
import icy.file.SequenceFileExporter;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import loci.formats.IFormatWriter;
import loci.formats.gui.ExtensionFileFilter;
import loci.plugins.out.Exporter;

/**
 * Saver dialog used to save resource or image from the {@link Exporter} or {@link SequenceFileExporter}.
 * 
 * @author Stephane
 * @see Saver
 */
public class SaverDialog extends JFileChooser
{
    private static final String PREF_ID = "frame/imageSaver";

    private static final String ID_WIDTH = "width";
    private static final String ID_HEIGHT = "height";
    private static final String ID_PATH = "path";
    private static final String ID_MULTIPLEFILE = "multipleFile";
    private static final String ID_OVERWRITENAME = "overwriteName";
    private static final String ID_FPS = "fps";
    private static final String ID_EXTENSION = "extension";

    // GUI
    private SaverOptionPanel settingPanel;

    // internal
    private final XMLPreferences preferences;

    private final boolean singleZ;
    private final boolean singleT;
    private final boolean singleImage;

    /**
     * <b>Saver Dialog</b><br>
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
    public SaverDialog(Sequence sequence, boolean autoSave)
    {
        super();

        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);

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

        // set last used file filter
        setFileFilter(getFileFilter(preferences.get(ID_EXTENSION, ImageFileFormat.TIFF.getDescription())));

        setMultiSelectionEnabled(false);
        // setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // so the filename information is not lost when changing directory
        setFileSelectionMode(JFileChooser.FILES_ONLY);

        String filename;

        // filename = sequence.getFilename();
        // // single image file ? --> re-use the filename as default filename for saving
        // if (!StringUtil.isEmpty(filename) && FileUtil.exists(filename))
        // filename = FileUtil.getFileName(sequence.getOutputFilename(false), false);
        // else
        // {
        
        // better to use internal metadata name by default as default filename
        filename = sequence.getName();
        // no specific internal name ?
        if (StringUtil.isEmpty(filename) || filename.startsWith(Sequence.DEFAULT_NAME))
            // get filename without extension
            filename = FileUtil.getFileName(sequence.getOutputFilename(false), false);
        // }
        
        // we have a default filename ?
        if (!StringUtil.isEmpty(filename))
        {
            // we should have only filename here (not full path) so we remove unwanted '/' and '\' characters
            filename = FileUtil.cleanPath(filename.replaceAll("/", " ").replaceAll("\\\\", " "));
            // test if filename has already a valid extension
            final String ext = getDialogExtension(filename);
            // remove file extension
            if (ext != null)
                FileUtil.setExtension(filename, "");
            // set dialog filename
            setSelectedFile(new File(filename));
        }

        // create extra setting panel
        settingPanel = new SaverOptionPanel();
        settingPanel.setMultipleFiles(preferences.getBoolean(ID_MULTIPLEFILE, false));
        settingPanel.setOverwriteMetadata(preferences.getBoolean(ID_OVERWRITENAME, true));

        // try to set time interval from metadata
        final double ti = sequence.getTimeInterval() * 1000d;

        if (ti != 0d)
            settingPanel.setTimeInterval(ti);
        // otherwise we just use the last used FPS
        else
            settingPanel.setFramePerSecond(preferences.getInt(ID_FPS, 20));

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

        ImageFileFormat fileFormat = null;
        IFormatWriter writer = null;
        boolean accepted = false;

        while (!accepted)
        {
            // display Saver dialog
            final int value = showSaveDialog(Icy.getMainInterface().getMainFrame());

            // action canceled --> stop here
            if (value != JFileChooser.APPROVE_OPTION)
                break;

            // get selected file format and associated writer
            fileFormat = getSelectedFileFormat();
            writer = Saver.getWriter(fileFormat);

            // selected writer is not compatible ?
            if (!isCompatible(fileFormat, sequence))
            {
                // incompatible saver for this sequence
                // new IncompatibleImageFormatDialog();
                // return;

                // display a confirm dialog about possible loss in save operation
                accepted = ConfirmDialog.confirm("Warning", "Some information will be lost in the " + fileFormat
                        + " saved file(s). Do you want to continue ?");
            }
            else
                accepted = true;
        }

        // only if accepted...
        if (accepted)
        {
            File file = getSelectedFile();
            final String outFilename = file.getAbsolutePath();

            // destination is a folder ?
            if (isFolderRequired())
            {
                // remove extension
                file = new File(FileUtil.setExtension(outFilename, ""));
                // set it so we can get it from getSelectedFile()
                setSelectedFile(file);
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
                    final IFormatWriter w = writer;

                    // do save in background process
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Saver.save(w, s, f, getFps(), isSaveAsMultipleFilesEnabled(), true, true);
                        }
                    });
                }
            }

            // store interface option
            preferences.putInt(ID_WIDTH, getWidth());
            preferences.putInt(ID_HEIGHT, getHeight());
            // save this information only for TIFF format
            if (fileFormat == ImageFileFormat.TIFF)
                preferences.putBoolean(ID_MULTIPLEFILE, isSaveAsMultipleFilesEnabled());
            preferences.putBoolean(ID_OVERWRITENAME, settingPanel.getOverwriteMetadata());
            // save this information only for AVI format
            if (fileFormat == ImageFileFormat.AVI)
                preferences.putInt(ID_FPS, getFps());
            preferences.put(ID_EXTENSION, getFileFilter().getDescription());
        }
    }

    /**
     * <b>Saver Dialog</b><br>
     * <br>
     * Display a dialog to select the destination file then save the specified sequence.
     * 
     * @param sequence
     *        The {@link Sequence} we want to save.
     */
    public SaverDialog(Sequence sequence)
    {
        this(sequence, true);
    }

    /**
     * @deprecated Use {@link #SaverDialog(Sequence, boolean)} instead
     */
    @Deprecated
    public SaverDialog(Sequence sequence, int defZ, int defT, boolean autoSave)
    {
        this(sequence, autoSave);
    }

    /**
     * @deprecated Use {@link #SaverDialog(Sequence)} instead
     */
    @Deprecated
    public SaverDialog(Sequence sequence, int defZ, int defT)
    {
        this(sequence, true);
    }

    protected FileFilter getFileFilter(String description)
    {
        final FileFilter[] filters = getChoosableFileFilters();

        for (FileFilter filter : filters)
            if (StringUtil.equals(filter.getDescription(), description))
                return filter;

        // default one
        return ImageFileFormat.TIFF.getExtensionFileFilter();
    }

    private static boolean hasExtension(String name, ExtensionFileFilter extensionFilter)
    {
        return getExtension(name, extensionFilter) != null;
    }

    private static String getExtension(String name, ExtensionFileFilter extensionFilter)
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
        return !singleImage && isSaveAsMultipleFilesEnabled();
    }

    /**
     * Returns <code>true</code> if user chosen to save the sequence as multiple image files.
     */
    public boolean isSaveAsMultipleFilesEnabled()
    {
        return settingPanel.isMultipleFilesVisible() && settingPanel.getMultipleFiles();
    }

    /**
     * Returns <code>true</code> if user chosen to overwrite the sequence internal name by filename.
     */
    public boolean isOverwriteNameEnabled()
    {
        return settingPanel.isOverwriteMetadataVisible() && settingPanel.getOverwriteMetadata();
    }

    /**
     * Returns the desired FPS (Frame Per Second, only for AVI file).
     */
    public int getFps()
    {
        if (settingPanel.isFramePerSecondVisible())
            return settingPanel.getFramePerSecond();

        return 1;
    }

    /**
     * Returns the desired time interval between 2 frames in ms (only for AVI file).
     */
    public double getTimeInterval()
    {
        if (settingPanel.isFramePerSecondVisible())
            return settingPanel.getTimeInterval();

        return 100d;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("static-method")
    public int getZMin()
    {
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("static-method")
    public int getZMax()
    {
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("static-method")
    public int getTMin()
    {
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("static-method")
    public int getTMax()
    {
        return 0;
    }

    void updateSettingPanel()
    {
        final ImageFileFormat fileFormat = getSelectedFileFormat();

        // single image, no need to display selection option
        if (singleImage)
        {
            settingPanel.setMultipleFilesVisible(false);
            settingPanel.setForcedMultipleFilesOff();
        }
        else
        {
            switch (fileFormat)
            {
                case AVI:
                    settingPanel.setMultipleFilesVisible(true);
                    settingPanel.setForcedMultipleFilesOff();
                    break;

                case JPG:
                case PNG:
                    settingPanel.setMultipleFilesVisible(true);
                    settingPanel.setForcedMultipleFilesOn();
                    break;

                case TIFF:
                    settingPanel.setMultipleFilesVisible(true);
                    settingPanel.removeForcedMultipleFiles();
                    settingPanel.setMultipleFiles(preferences.getBoolean(ID_MULTIPLEFILE, false));
                    break;
            }
        }

        settingPanel.setFramePerSecondVisible(fileFormat == ImageFileFormat.AVI);
    }

    private boolean isCompatible(ImageFileFormat fileFormat, Sequence sequence)
    {
        if (fileFormat == ImageFileFormat.AVI)
        {
            // with AVI we force single file saving so we can't save 3D image.
            if (!singleZ)
                return false;
        }

        // just need to test against colormodel now
        return Saver.isCompatible(fileFormat, sequence.getColorModel());
    }
}
