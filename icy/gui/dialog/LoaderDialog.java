package icy.gui.dialog;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import icy.file.FileImporter;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.SequenceFileImporter;
import icy.file.SequenceFileSticher;
import icy.file.SequenceFileSticher.SequenceFileGroup;
import icy.gui.dialog.LoaderOptionPanel.LoaderLoadingType;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

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
    private static final String ID_LOADTYPE = "loadtype";
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
     * @param defaultPath
     *        default file path (can be null)
     * @param region
     *        default XY region (can be null)
     * @param series
     *        default series (can be -1)
     * @param autoLoad
     *        If true the selected file(s) are automatically loaded.
     */
    public LoaderDialog(String defaultPath, Rectangle region, int series, boolean autoLoad)
    {
        super();

        final XMLPreferences preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
        sequenceImporters = Loader.getSequenceFileImporters();
        fileImporters = Loader.getFileImporters();

        // create option panel
        optionPanel = new LoaderOptionPanel(
                LoaderLoadingType.values()[preferences.getInt(ID_LOADTYPE, LoaderLoadingType.GROUP.ordinal())]);

        // we can only store dimension for JFileChooser
        setPreferredSize(new Dimension(preferences.getInt(ID_WIDTH, 600), preferences.getInt(ID_HEIGTH, 400)));

        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        setAcceptAllFileFilterUsed(false);
        resetChoosableFileFilters();

        // add file filter from importers
        for (SequenceFileImporter importer : sequenceImporters)
        {
            final List<FileFilter> filters = importer.getFileFilters();

            if (filters != null)
            {
                for (FileFilter filter : filters)
                    addChoosableFileFilter(filter);
            }
        }
        for (FileImporter importer : fileImporters)
        {
            final List<FileFilter> filters = importer.getFileFilters();

            if (filters != null)
            {
                for (FileFilter filter : filters)
                    addChoosableFileFilter(filter);
            }
        }
        // add "all files" filter
        addChoosableFileFilter(allFileFilter);

        // we use a default path ?
        if (defaultPath != null)
        {
            // set all files filter
            setFileFilter(allFileFilter);

            final File file = new File(defaultPath);

            if (file.isDirectory())
                setCurrentDirectory(file);
            else
                setSelectedFile(file);
        }
        else
        {
            // set last used file filter
            setFileFilter(getFileFilter(preferences.get(ID_EXTENSION, allImagesFileFilter.getDescription())));
            // set last used directory
            setCurrentDirectory(new File(GeneralPreferences.getLoaderFolder()));
        }

        // setting GUI
        updateGUI();

        // we use a default path ?
        if (defaultPath != null)
        {
            // refresh preview
            optionPanel.updatePreview(new String[] {defaultPath}, series);
            // updateOptionPanel();
            // have a default XY region ?
            if (region != null)
                optionPanel.setXYRegion(region);
        }

        // listen file filter change
        addPropertyChangeListener(this);

        // display loader
        final int value = showOpenDialog(Icy.getMainInterface().getMainFrame());

        // action confirmed ?
        if (value == JFileChooser.APPROVE_OPTION)
        {
            // store current path
            GeneralPreferences.setLoaderFolder(getCurrentDirectory().getAbsolutePath());
            preferences.putInt(ID_LOADTYPE, optionPanel.getLoadingType().ordinal());
            preferences.put(ID_EXTENSION, getFileFilter().getDescription());

            // load if requested
            if (autoLoad)
            {
                // get selected paths
                final List<String> paths = CollectionUtil.asList(FileUtil.toPaths(getSelectedFiles()));
                // first path
                final String firstPath = paths.get(0);
                // get the selected importer from file filter
                final Object importer = getSelectedImporter();

                // multiple files or folder loading
                if ((paths.size() > 1) || FileUtil.isDirectory(firstPath))
                {
                    if (importer instanceof FileImporter)
                    {
                        // load selected non image file(s)
                        Loader.load((FileImporter) importer, paths, true);
                    }
                    else
                    {
                        if (isSeparateSequenceSelected())
                            // load selected image file(s) separately
                            Loader.load((SequenceFileImporter) importer, paths, true, false, true);
                        else
                        {
                            // build groups
                            final Collection<SequenceFileGroup> groups = SequenceFileSticher
                                    .groupAllFiles((SequenceFileImporter) importer, paths, isAutoOrderSelected(), null);

                            // then load them
                            for (SequenceFileGroup group : groups)
                            {
                                // we don't have series for image grouped loading
                                Loader.load(group, optionPanel.getResolutionLevel(), optionPanel.getXYRegion(),
                                        optionPanel.getFullZRange() ? -1 : optionPanel.getZMin(),
                                        optionPanel.getFullZRange() ? -1 : optionPanel.getZMax(),
                                        optionPanel.getFullTRange() ? -1 : optionPanel.getTMin(),
                                        optionPanel.getFullTRange() ? -1 : optionPanel.getTMax(),
                                        optionPanel.getChannel(), false, true, true);
                            }
                        }
                    }
                }
                else
                {
                    // single file loading
                    if (importer instanceof FileImporter)
                    {
                        // load selected non image file
                        Loader.load((FileImporter) importer, paths, true);
                    }
                    else
                    {
                        // load selected image file with advanced option
                        Loader.load((SequenceFileImporter) importer, firstPath, optionPanel.getSeries(),
                                optionPanel.getResolutionLevel(), optionPanel.getXYRegion(),
                                optionPanel.getFullZRange() ? -1 : optionPanel.getZMin(),
                                optionPanel.getFullZRange() ? -1 : optionPanel.getZMax(),
                                optionPanel.getFullTRange() ? -1 : optionPanel.getTMin(),
                                optionPanel.getFullTRange() ? -1 : optionPanel.getTMax(), optionPanel.getChannel(),
                                isSeparateSequenceSelected(), true, true);
                    }
                }
            }
        }

        // store interface option
        preferences.putInt(ID_WIDTH, getWidth());
        preferences.putInt(ID_HEIGTH, getHeight());
    }

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
     * @param defaultPath
     *        default file path (can be null)
     * @param region
     *        default XY region (can be null)
     * @param autoLoad
     *        If true the selected file(s) are automatically loaded.
     */
    public LoaderDialog(String defaultPath, Rectangle region, boolean autoLoad)
    {
        this(defaultPath, region, -1, autoLoad);
    }

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
        this(null, null, autoLoad);
    }

    /**
     * Display a dialog to select file(s) and load them.
     */
    public LoaderDialog()
    {
        this(null, null, true);
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
        return getSelectedImporter() instanceof SequenceFileImporter;
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
            final List<FileFilter> filters = importer.getFileFilters();
            final int count = (filters != null) ? filters.size() : 0;

            if (filterIndex < (ind + count))
                return importer;

            ind += count;
        }
        for (FileImporter importer : fileImporters)
        {
            final List<FileFilter> filters = importer.getFileFilters();
            final int count = (filters != null) ? filters.size() : 0;

            if (filterIndex < (ind + count))
                return importer;

            ind += count;
        }

        return null;
    }

    public Object getSelectedImporter()
    {
        return getImporter(getFileFilterIndex());
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
        return optionPanel.getLoadingType() == LoaderLoadingType.GROUP;
    }

    /**
     * Get selected resolution level
     */
    public int getResolutionLevel()
    {
        return optionPanel.getResolutionLevel();
    }

    /**
     * Get selected XY region (when region loading is enabled)
     */
    public Rectangle getXYRegion()
    {
        return optionPanel.getXYRegion();
    }

    /**
     * Get minimum Z (Z range selection)
     */
    public int getZMin()
    {
        return optionPanel.getZMin();
    }

    /**
     * Get maximum Z (Z range selection)
     */
    public int getZMax()
    {
        return optionPanel.getZMax();
    }

    /**
     * Get minimum T (T range selection)
     */
    public int getTMin()
    {
        return optionPanel.getTMin();
    }

    /**
     * Get maximum T (T range selection)
     */
    public int getTMax()
    {
        return optionPanel.getTMax();
    }

    /**
     * Get channel selection (-1 for all)
     */
    public int getChannel()
    {
        return optionPanel.getChannel();
    }

    /**
     * Get series selection (-1 for all)
     */
    public int getSeries()
    {
        return optionPanel.getSeries();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String prop = evt.getPropertyName();

        // filter change ?
        if (prop.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
            updateGUI();
        // single selection change ?
        // else if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        // {
        // // multiple selection --> ignore this event
        // if (getSelectedFiles().length > 1)
        // return;
        //
        // final File f = getSelectedFile();
        //
        // // folder or file don't exist ?
        // if ((f == null) || f.isDirectory() || !f.exists())
        // optionPanel.updatePreview(new String[0]);
        // else
        // // refresh preview
        // optionPanel.updatePreview(new String[] {f.getAbsolutePath()});
        //
        // // updateOptionPanel();
        // }
        // multi selection change ?
        else if (prop.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY))
        {
            final File[] files = getSelectedFiles();

            // single selection
            if (files.length < 2)
            {
                final File f = getSelectedFile();

                // folder or file don't exist ?
                if ((f == null) || f.isDirectory() || !f.exists())
                    optionPanel.updatePreview(new String[0]);
                else
                    // refresh preview
                    optionPanel.updatePreview(new String[] {f.getAbsolutePath()});
            }
            else
                optionPanel.updatePreview(FileUtil.toPaths(files));
        }
        // closing ? --> do some final operation on option panel
        else if (prop.equals("JFileChooserDialogIsClosingProperty"))
            optionPanel.closingFromEDT();
        // else
        // updateOptionPanel();
    }

    protected void updateGUI()
    {
        if (isImageFilter() || isAllFileFilter())
        {
            setDialogTitle("Load image file(s)");
            setAccessory(optionPanel);
            // updateOptionPanel();
            revalidate();
        }
        else
        {
            setDialogTitle("Load file(s)");
            setAccessory(null);
            revalidate();
        }
    }

    // protected void updateOptionPanel()
    // {
    // final int numFile = getSelectedFiles().length;
    // final boolean multi;
    //
    // if (numFile > 1)
    // multi = true;
    // else
    // {
    // final File file = getSelectedFile();
    // multi = (file != null) && file.isDirectory();
    // }
    //
    // optionPanel.setMultiFile(multi);
    // }
}
