package icy.gui.dialog;

import icy.file.FileImporter;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
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
 * Loader dialog used to load resource or image from the {@link FileImporter} or
 * {@link SequenceFileImporter}.
 * 
 * @author Stephane
 * @see Loader
 */
public class LoaderDialog extends JFileChooser implements PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 5162434537949723956L;

    public static class AllImagesFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            if (file.isDirectory())
                return true;

            return !Loader.canDiscardImageFile(file.getName());
        }

        @Override
        public String getDescription()
        {
            return "All images file";
        }
    }

    public static class AllFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return file.exists();
        }

        @Override
        public String getDescription()
        {
            return "All files";
        }
    }

    public static AllImagesFileFilter allImagesFileFilter = new AllImagesFileFilter();
    public static AllFileFilter allFileFilter = new AllFileFilter();

    private static final String PREF_ID = "frame/imageLoader";

    private static final String ID_WIDTH = "width";
    private static final String ID_HEIGTH = "heigth";
    private static final String ID_SEPARATE = "separate";
    private static final String ID_AUTOORDER = "autoOrder";
    private static final String ID_EXTENSION = "extension";

    // GUI
    protected final LoaderOptionPanel optionPanel;
    protected final List<SequenceFileImporter> sequenceImporters;
    protected final List<FileImporter> fileImporters;

    /**
     * Display a dialog to select image or resource file(s) and load them.<br>
     * <br>
     * To only get selected files from the dialog you must do:<br>
     * <code> LoaderDialog dialog = new LoaderDialog(false);</code><br>
     * <code> File[] selectedFiles = dialog.getSelectedFiles()</code><br>
     * <br>
     * To directly load selected files just use:<br>
     * <code>new LoaderDialog(true);</code><br>
     * or<br>
     * <code>new LoaderDialog();</code>
     * 
     * @param autoLoad
     *        If true the selected file(s) are automatically loaded.
     */
    public LoaderDialog(boolean autoLoad)
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
        sequenceImporters = Loader.getSequenceFileImporters();
        fileImporters = Loader.getFileImporters();

        // create option panel
        optionPanel = new LoaderOptionPanel(preferences.getBoolean(ID_SEPARATE, false), preferences.getBoolean(
                ID_AUTOORDER, true));

        // can't use WindowsPositionSaver as JFileChooser is a fake JComponent
        // only dimension is stored
        setCurrentDirectory(new File(GeneralPreferences.getLoaderFolder()));
        setPreferredSize(new Dimension(preferences.getInt(ID_WIDTH, 600), preferences.getInt(ID_HEIGTH, 400)));

        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        setAcceptAllFileFilterUsed(false);
        resetChoosableFileFilters();

        // add file filter from importers
        for (SequenceFileImporter importer : sequenceImporters)
            for (FileFilter filter : importer.getFileFilters())
                addChoosableFileFilter(filter);
        for (FileImporter importer : fileImporters)
            for (FileFilter filter : importer.getFileFilters())
                addChoosableFileFilter(filter);
        // add "all files" filter
        addChoosableFileFilter(allFileFilter);

        // set last used file filter
        setFileFilter(getFileFilter(preferences.get(ID_EXTENSION, allImagesFileFilter.getDescription())));
        // setting GUI
        updateGUI();

        // listen file filter change
        addPropertyChangeListener(this);

        // display loader
        final int value = showOpenDialog(Icy.getMainInterface().getMainFrame());

        // cancel preview refresh (for big file)
        optionPanel.cancelPreview();

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // store current path
            GeneralPreferences.setLoaderFolder(getCurrentDirectory().getAbsolutePath());
            preferences.putBoolean(ID_SEPARATE, isSeparateSequenceSelected());
            preferences.putBoolean(ID_AUTOORDER, isAutoOrderSelected());
            preferences.put(ID_EXTENSION, getFileFilter().getDescription());

            // load if requested
            if (autoLoad)
            {
                // get the selected importer from file filter
                final Object importer = getImporter(getFileFilterIndex());

                if (importer instanceof FileImporter)
                {
                    // load selected non image file(s)
                    Loader.load((FileImporter) importer, CollectionUtil.asList(FileUtil.toPaths(getSelectedFiles())),
                            true);
                }
                else if (importer instanceof SequenceFileImporter)
                {
                    // load selected image file(s)
                    Loader.load((SequenceFileImporter) importer,
                            CollectionUtil.asList(FileUtil.toPaths(getSelectedFiles())), isSeparateSequenceSelected(),
                            isAutoOrderSelected(), true);
                }
                else
                {
                    // load selected file(s)
                    Loader.load(CollectionUtil.asList(FileUtil.toPaths(getSelectedFiles())),
                            isSeparateSequenceSelected(), isAutoOrderSelected(), true);
                }
            }
        }

        // store interface option
        preferences.putInt(ID_WIDTH, getWidth());
        preferences.putInt(ID_HEIGTH, getHeight());
    }

    /**
     * Display a dialog to select file(s) and load them.
     */
    public LoaderDialog()
    {
        this(true);
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

    protected boolean isImageFilter()
    {
        return getImporter(getFileFilterIndex()) instanceof SequenceFileImporter;
    }

    protected boolean isAllFileFilter()
    {
        return getFileFilter() == allFileFilter;
    }

    protected Object getImporter(int filterIndex)
    {
        int ind = 0;

        for (SequenceFileImporter importer : sequenceImporters)
        {
            final int count = importer.getFileFilters().size();

            if (filterIndex < (ind + count))
                return importer;

            ind += count;
        }
        for (FileImporter importer : fileImporters)
        {
            final int count = importer.getFileFilters().size();

            if (filterIndex < (ind + count))
                return importer;

            ind += count;
        }

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
        final boolean imageFilter = isImageFilter();

        if (prop.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
            updateGUI();
        else if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        {
            File f = (File) evt.getNewValue();

            if ((f != null) && (f.isDirectory() || !f.exists()))
                f = null;

            // refresh preview
            if (f != null)
                optionPanel.updatePreview(f.getAbsolutePath());

            updateOptionPanel();
        }
        else
            updateOptionPanel();
    }

    protected void updateGUI()
    {
        if (isImageFilter() || isAllFileFilter())
        {
            setDialogTitle("Load image file(s)");
            setAccessory(optionPanel);
            updateOptionPanel();
        }
        else
        {
            setDialogTitle("Load file(s)");
            setAccessory(null);
        }
    }

    protected void updateOptionPanel()
    {
        final int numFile = getSelectedFiles().length;
        final boolean multi;

        if (numFile > 1)
            multi = true;
        else if (numFile == 1)
        {
            final File file = getSelectedFile();
            multi = file.isDirectory();
        }
        else
            multi = false;

        optionPanel.setSeparateSequenceEnabled(multi);
        optionPanel.setAutoOrderEnabled(multi);
    }
}
