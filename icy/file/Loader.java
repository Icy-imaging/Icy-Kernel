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
package icy.file;

import icy.common.exception.UnsupportedFormatException;
import icy.file.SequenceFileSticher.SequenceFileGroup;
import icy.file.SequenceFileSticher.SequenceIdent;
import icy.file.SequenceFileSticher.SequencePosition;
import icy.gui.dialog.ImporterSelectionDialog;
import icy.gui.dialog.SeriesSelectionDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.ChannelPosition;
import icy.image.IcyBufferedImage;
import icy.image.ImagePosition;
import icy.image.ImageProvider;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.preferences.GeneralPreferences;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceIdImporter;
import icy.sequence.SequenceImporter;
import icy.sequence.SequencePersistent;
import icy.sequence.SequenceUtil;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.CollectionUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

/**
 * Sequence / Image loader class.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Loader
{
    /**
     * @deprecated Use {@link SequenceFileSticher#groupFiles(SequenceFileImporter, Collection, boolean, FileFrame)} instead
     */
    @Deprecated
    public static class FilePosition extends ChannelPosition
    {
        public final String path;
        public String basePath;
        int s;

        public FilePosition(String path, String basePath, int s, int t, int z, int c)
        {
            super(t, z, c);

            this.s = s;
            this.path = path;
            this.basePath = basePath;
        }

        /**
         * @deprecated Use {@link #FilePosition(String, String, int, int, int, int)} instead.
         */
        @Deprecated
        public FilePosition(String path, int t, int z, int c)
        {
            super(t, z, c);

            this.path = path;
            basePath = "";
        }

        public FilePosition(String path)
        {
            super();

            this.path = path;
        }

        public FilePosition(FilePosition fp)
        {
            this(fp.path, fp.basePath, fp.s, fp.t, fp.z, fp.c);
        }

        public int getS()
        {
            return s;
        }

        public void setS(int s)
        {
            this.s = s;
        }

        public void set(int s, int t, int z, int c)
        {
            super.set(t, z, c);
            this.s = s;
        }

        @Override
        public int compareTo(ImagePosition o)
        {
            if (o instanceof FilePosition)
            {
                int result = basePath.compareTo(((FilePosition) o).basePath);

                if (result != 0)
                    return result;

                final int sp = ((FilePosition) o).s;

                if (s > sp)
                    return 1;
                if (s < sp)
                    return -1;
            }

            return super.compareTo(o);
        }

        @Override
        public String toString()
        {
            return "File=" + path + " Position=[S:" + s + " T:" + t + " Z:" + z + " C:" + c + "]";
        }
    }

    // private final static Set<String> nonImageExtensions = new
    // HashSet<String>(CollectionUtil.asList(new String[] {
    // "xml", "txt", "pdf", "xls", "doc", "docx", "pdf", "rtf", "exe", "wav", "mp3", "app"}));
    /**
     * XML, XLS and TXT file can be image metadata files used to open the whole image, accept it !
     */
    private final static Set<String> nonImageExtensions = new HashSet<>(
            CollectionUtil.asList(new String[] {"pdf", "doc", "docx", "pdf", "rtf", "exe", "wav", "mp3", "app"}));

    // keep trace of reported / warned plugin
    private static Set<String> reportedImporterPlugins = new HashSet<>();
    private static Set<String> warnedImporterPlugins = new HashSet<>();

    private static void handleImporterError(PluginDescriptor plugin, Throwable t)
    {
        final String pluginId = plugin.getName() + " " + plugin.getVersion();

        if (t instanceof UnsupportedClassVersionError)
        {
            if (!warnedImporterPlugins.contains(pluginId))
            {
                // show a specific message in the output console
                System.err.println("Plugin '" + plugin.getName() + "' " + plugin.getVersion()
                        + " is not compatible with java " + ((int) Math.floor(SystemUtil.getJavaVersionAsNumber())));
                System.err.println("You need to install a newer version of java to use it.");

                // add to the list of warned plugins
                warnedImporterPlugins.add(pluginId);
            }
        }
        else
        {
            if (!reportedImporterPlugins.contains(pluginId))
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));

                // add to the list of warned plugins
                reportedImporterPlugins.add(pluginId);
            }
        }
    }

    /**
     * Returns all available resource importer.
     */
    public static List<Importer> getImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(Importer.class);
        final List<Importer> result = new ArrayList<>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((Importer) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available resource (non image) importer which take file as input.
     */
    public static List<FileImporter> getFileImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(FileImporter.class);
        final List<FileImporter> result = new ArrayList<>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((FileImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     * 
     * @param importers
     *        the base list of importer we want to test to open file.
     * @param paths
     *        the list of file we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match for a file.
     */
    public static Map<FileImporter, List<String>> getFileImporters(List<FileImporter> importers, List<String> paths,
            boolean useFirstFound)
    {
        final Map<FileImporter, List<String>> result = new HashMap<>(importers.size());
        final Map<String, FileImporter> extensionImporters = new HashMap<>(importers.size());

        for (String path : paths)
        {
            final String ext = FileUtil.getFileExtension(path, false);
            FileImporter imp;

            // try to get importer from extension first
            imp = extensionImporters.get(ext);

            // do not exist yet
            if (imp == null)
            {
                // find it
                imp = getFileImporter(importers, path, useFirstFound);
                // set the importer for this extension
                if (imp != null)
                    extensionImporters.put(ext, imp);
            }

            // importer found for this path ?
            if (imp != null)
            {
                // retrieve current list of path for this importer
                List<String> list = result.get(imp);

                // do not exist yet --> create it
                if (list == null)
                {
                    list = new ArrayList<>();
                    // set the list for this importer
                    result.put(imp, list);
                }

                // add path to the list
                list.add(path);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     * 
     * @param paths
     *        the list of file we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match for a file.
     */
    public static Map<FileImporter, List<String>> getFileImporters(List<String> paths, boolean useFirstFound)
    {
        return getFileImporters(getFileImporters(), paths, useFirstFound);
    }

    /**
     * Returns all file importer which can open the specified file.
     */
    public static List<FileImporter> getFileImporters(List<FileImporter> importers, String path)
    {
        final List<FileImporter> result = new ArrayList<>(importers.size());

        for (FileImporter importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all file importer which can open the specified file.
     */
    public static List<FileImporter> getFileImporters(String path)
    {
        return getFileImporters(getFileImporters(), path);
    }

    /**
     * Returns the appropriate file importer for the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     * 
     * @param importers
     *        the base list of importer we want to test to open file.
     * @param path
     *        the file we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match.
     * @see #getFileImporters(List, String)
     */
    public static FileImporter getFileImporter(List<FileImporter> importers, String path, boolean useFirstFound)
    {
        final List<FileImporter> result = new ArrayList<>(importers.size());

        for (FileImporter importer : importers)
        {
            if (importer.acceptFile(path))
            {
                if (useFirstFound)
                    return importer;

                result.add(importer);
            }
        }

        // let user select the good importer
        return selectFileImporter(result, path);
    }

    /**
     * Returns the appropriate file importer for the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     * 
     * @param path
     *        the file we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match.
     * @see #getFileImporters(String)
     */
    public static FileImporter getFileImporter(String path, boolean useFirstFound)
    {
        return getFileImporter(getFileImporters(), path, useFirstFound);
    }

    /**
     * Display a dialog to let the user select the appropriate file importer for the specified file.
     */
    public static FileImporter selectFileImporter(final List<FileImporter> importers, final String path)
    {
        if (importers.size() == 0)
            return null;
        if (importers.size() == 1)
            return importers.get(0);

        if (Icy.getMainInterface().isHeadLess())
            return importers.get(0);

        final Object result[] = new Object[1];

        // use invokeNow carefully !
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                // get importer
                result[0] = new ImporterSelectionDialog(importers, path).getSelectedImporter();
            }
        });

        return (FileImporter) result[0];
    }

    /**
     * Returns all available sequence importer (different from {@link SequenceIdImporter} or {@link SequenceFileImporter}).
     */
    public static List<SequenceImporter> getSequenceImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceImporter.class);
        final List<SequenceImporter> result = new ArrayList<>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take path as input.<br>
     * If you want to get specifically importer which use path as input, then you need to use {@link #getSequenceFileImporters()} instead.
     * 
     * @see #getSequenceFileImporters()
     */
    public static List<SequenceIdImporter> getSequenceIdImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceIdImporter.class);
        final List<SequenceIdImporter> result = new ArrayList<>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceIdImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take file as input.
     * 
     * @see #getSequenceIdImporters()
     */
    public static List<SequenceFileImporter> getSequenceFileImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceFileImporter.class);
        final List<SequenceFileImporter> result = new ArrayList<>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceFileImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified list of file path.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     * 
     * @param importers
     *        the base list of importer we want to test to open file.
     * @param paths
     *        the list of path we want to find importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match for a path.
     */
    @SuppressWarnings("resource")
    public static <T extends SequenceFileImporter> Map<T, List<String>> getSequenceFileImporters(List<T> importers,
            List<String> paths, boolean useFirstFound)
    {
        final Map<T, List<String>> result = new HashMap<>(importers.size());
        final Map<String, T> extensionImporters = new HashMap<>(importers.size());
        T imp = null;

        for (String path : paths)
        {
            // get path extension (useful for path path type)
            final String ext = FileUtil.getFileExtension(path, false);

            // have an extension ? --> try to get importer from extension first
            if (!StringUtil.isEmpty(ext))
                imp = extensionImporters.get(ext);

            // have an importer for this path extension ? --> test it
            if ((imp != null) && !imp.acceptFile(path))
                imp = null;

            // do not exist yet
            if (imp == null)
            {
                // find it
                imp = getSequenceFileImporter(importers, path, useFirstFound);
                // set the importer for this extension
                if ((imp != null) && !StringUtil.isEmpty(ext))
                    extensionImporters.put(ext, imp);
            }

            // importer found for this path ?
            if (imp != null)
            {
                // retrieve current list of path for this importer
                List<String> list = result.get(imp);

                // do not exist yet --> create it
                if (list == null)
                {
                    list = new ArrayList<>();
                    // set the list for this importer
                    result.put(imp, list);
                }

                // add path to the list
                list.add(path);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     * 
     * @param paths
     *        the list of file we want to retrieve importer for.
     * @param grouped
     *        if set to <code>true</code> then we want to group the files so we only need to find the first appropriate importer
     *        and don't test for other files (only 1 importer will be returned for all files).
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when severals importers match for a file.
     */
    @SuppressWarnings("resource")
    public static Map<SequenceFileImporter, List<String>> getSequenceFileImporters(SequenceFileImporter defaultImporter,
            List<String> paths, boolean grouped, boolean useFirstFound)
    {
        if (paths.isEmpty())
            return new HashMap<>();

        final Map<SequenceFileImporter, List<String>> result;

        // have a default importer specified ? --> use it for all files
        if (defaultImporter != null)
        {
            result = new HashMap<>();
            result.put(defaultImporter, paths);
            return result;
        }

        // grouped ? --> find the first valid importer
        if (grouped)
        {
            result = new HashMap<>();

            for (String path : paths)
            {
                final SequenceFileImporter imp = getSequenceFileImporter(path, useFirstFound);

                if (imp != null)
                {
                    // use first valid importer for all files
                    result.put(imp, paths);
                    break;
                }
            }

            return result;
        }

        // get importers for each path
        return getSequenceFileImporters(paths, useFirstFound);
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified list of file path.<br>
     * A path can be absent from the returned Map when no importer support it.<br>
     * 
     * @param paths
     *        the list of path we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match for a file.
     */
    public static Map<SequenceFileImporter, List<String>> getSequenceFileImporters(List<String> paths,
            boolean useFirstFound)
    {
        return getSequenceFileImporters(getSequenceFileImporters(), paths, useFirstFound);
    }

    /**
     * Returns all sequence file importer which can open the specified path.
     */
    public static <T extends SequenceFileImporter> List<T> getSequenceFileImporters(List<T> importers, String path)
    {
        final List<T> result = new ArrayList<>(importers.size());

        for (T importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all sequence file importer which can open the specified file path.
     */
    public static List<SequenceFileImporter> getSequenceFileImporters(String path)
    {
        return getSequenceFileImporters(getSequenceFileImporters(), path);
    }

    /**
     * Returns the appropriate sequence file importer for the specified path.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
     * Returns <code>null</code> if no importer can open the specified path.
     * 
     * @param importers
     *        the base list of importer we want to test to open file path.
     * @param path
     *        the path we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match.
     * @see #getSequenceFileImporters(List, String)
     */
    public static <T extends SequenceFileImporter> T getSequenceFileImporter(List<T> importers, String path,
            boolean useFirstFound)
    {
        final List<T> result = new ArrayList<>(importers.size());

        for (T importer : importers)
        {
            if (importer.acceptFile(path))
            {
                if (useFirstFound)
                    return importer;

                result.add(importer);
            }
        }

        // let user select the good importer
        return selectSequenceFileImporter(result, path);
    }

    /**
     * Returns the appropriate sequence file importer for the specified file path.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
     * Returns <code>null</code> if no importer can open the specified file path.
     * 
     * @param path
     *        the file path we want to retrieve importer for.
     * @param useFirstFound
     *        if set to <code>true</code> then the first matching importer is automatically selected
     *        otherwise a dialog appears to let the user to choose the correct importer when
     *        severals importers match.
     * @see #getSequenceFileImporters(String)
     */
    public static SequenceFileImporter getSequenceFileImporter(String path, boolean useFirstFound)
    {
        return getSequenceFileImporter(getSequenceFileImporters(), path, useFirstFound);
    }

    /**
     * Display a dialog to let the user select the appropriate sequence file importer for the given file path.
     */
    @SuppressWarnings("unchecked")
    public static <T extends SequenceFileImporter> T selectSequenceFileImporter(final List<T> importers,
            final String path)
    {
        if (importers.size() == 0)
            return null;
        if (importers.size() == 1)
            return importers.get(0);

        // no choice, take first one
        if (Icy.getMainInterface().isHeadLess())
            return importers.get(0);

        final Object result[] = new Object[1];

        // use invokeNow carefully !
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                // get importer
                final ImporterSelectionDialog selectionDialog = new ImporterSelectionDialog(importers, path);

                if (!selectionDialog.isCanceled())
                    result[0] = selectionDialog.getSelectedImporter();
                else
                    result[0] = null;
            }
        });

        return (T) result[0];
    }

    /**
     * @deprecated Use {@link #getSequenceFileImporter(List, String, boolean)}
     */
    @Deprecated
    public static SequenceFileImporter getSequenceFileImporter(List<SequenceFileImporter> importers, String path)
    {
        return getSequenceFileImporter(importers, path, true);
    }

    /**
     * @deprecated Use {@link #getSequenceFileImporter(String, boolean)}
     */
    @Deprecated
    public static SequenceFileImporter getSequenceFileImporter(String path)
    {
        return getSequenceFileImporter(path, true);
    }

    /**
     * Returns <code>true</code> if the specified path describes a file type (from extension) which
     * is well known to
     * not be an image file.<br>
     * For instance <i>.exe</i>, <i>.wav</i> or <i>.doc</i> file cannot specify an image file so we
     * can quickly discard them (extension based exclusion)
     */
    public static boolean canDiscardImageFile(String path)
    {
        final String ext = FileUtil.getFileExtension(path, false).toLowerCase();

        return nonImageExtensions.contains(ext);
    }

    /**
     * Returns true if the specified file is a supported image file.
     */
    public static boolean isSupportedImageFile(String path)
    {
        return getSequenceFileImporter(path, true) != null;
    }

    /**
     * @deprecated Use {@link #isSupportedImageFile(String)} instead.
     */
    @Deprecated
    public static boolean isImageFile(String path)
    {
        return isSupportedImageFile(path);
    }

    /**
     * Returns path which are supported by the specified imported for the given list of paths.
     */
    public static List<String> getSupportedFiles(SequenceFileImporter importer, List<String> paths)
    {
        final List<String> result = new ArrayList<>();

        for (String path : paths)
        {
            if (importer.acceptFile(path))
                result.add(path);
        }

        return result;
    }

    /**
     * Check if we have enough resource to open the image defined by the given size information and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we
     * don't have enough
     * memory to store the whole image the method throw an exception with an informative error
     * message about the
     * encountered limitation.
     * 
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the
     *        original image while value
     *        <code>1</code> correspond to the resolution / 2.<br>
     *        Formula: <code>resolution / 2^value</code><br>
     * @param sizeX
     *        width of the image region we want to load
     * @param sizeY
     *        height of the image region we want to load
     * @param sizeC
     *        number of channel we want to load
     * @param sizeZ
     *        number of slice we want to load (can be different from original image sizeZ)
     * @param sizeT
     *        number of frame we want to load (can be different from original image sizeT)
     * @param dataType
     *        pixel data type of the image we want to load
     * @param messageSuffix
     *        message suffix for the exception if wanted
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    public static void checkOpening(int resolution, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError
    {
        // size of XY plane
        long sizeXY = (long) sizeX * (long) sizeY;
        // wanted resolution
        sizeXY /= Math.pow(4, resolution);

        // we can't handle that plane size
        if (sizeXY > Integer.MAX_VALUE)
            throw new UnsupportedOperationException(
                    "Cannot open image with a XY plane size >= 2^31." + ((messageSuffix != null) ? messageSuffix : ""));

        // get free memory
        long freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
        // check that we have enough memory for the whole image and for the ARGB image used for
        // display (sizeXY * 4)
        long sizeInByte = (sizeXY * sizeC * sizeZ * sizeT * dataType.getSize()) + (sizeXY * 4);

        // not enough memory to store the whole image ?
        if (sizeInByte > freeInByte)
        {
            // try to release some memory
            System.gc();
            // get updated free memory
            freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
        }

        // still not enough memory ?
        if (sizeInByte > freeInByte)
            throw new OutOfMemoryError("Not enough memory to open the wanted image resolution."
                    + ((messageSuffix != null) ? messageSuffix : ""));
    }

    /**
     * Check if we have enough resource to open the image defined by the given metadata information, series index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we* don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     * 
     * @param meta
     *        metadata of the image
     * @param series
     *        series index
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the
     *        original image while value
     *        <code>1</code> correspond to the resolution / 2.<br>
     *        Formula: <code>resolution / 2^value</code><br>
     * @param sizeZ
     *        number of slice we want to load (can be different from original image sizeZ)
     * @param sizeT
     *        number of frame we want to load (can be different from original image sizeT)
     * @param messageSuffix
     *        message suffix for the exception if wanted
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    public static void checkOpening(OMEXMLMetadata meta, int series, int resolution, int sizeZ, int sizeT,
            String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError
    {
        checkOpening(resolution, MetaDataUtil.getSizeX(meta, series), MetaDataUtil.getSizeY(meta, series),
                MetaDataUtil.getSizeC(meta, series), sizeZ, sizeT, MetaDataUtil.getDataType(meta, series),
                messageSuffix);
    }

    /**
     * Check if we have enough resource to open the image defined by the given metadata information, series index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     * 
     * @param meta
     *        metadata of the image
     * @param series
     *        series index
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the
     *        original image while value
     *        <code>1</code> correspond to the resolution / 2.<br>
     *        Formula: <code>resolution / 2^value</code><br>
     * @param messageSuffix
     *        message suffix for the exception if wanted
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    public static void checkOpening(OMEXMLMetadata meta, int series, int resolution, String messageSuffix)
            throws UnsupportedOperationException, OutOfMemoryError
    {
        checkOpening(meta, series, resolution, MetaDataUtil.getSizeZ(meta, series), MetaDataUtil.getSizeT(meta, series),
                messageSuffix);
    }

    // /**
    // * Returns the best resolution to use from the given metadata information and series index.<br>
    // * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if
    // we don't have enough
    // * memory to store the whole image (depending wanted constraint) then a sub resolution index
    // is returned.<br>
    // * A return value of <code>0</code> means full resolution of the original image while value
    // <code>1</code>
    // * correspond to the resolution / 2.<br>
    // * Formula: <code>resolution / 2^value</code><br>
    // *
    // * @param meta
    // * metadata of the image
    // * @param series
    // * series index
    // * @param showMessage
    // * show announce frame or message in the output log when one size constraint is meet and
    // induce resolution
    // * decrease
    // */
    // public static int getBestResolution(OMEXMLMetadataImpl meta, int series, boolean showMessage)
    // {
    // // easy trick to disable message display when needed
    // boolean warningDisplayed = !showMessage;
    // // default resolution to open (full resolution)
    // int resolution = 0;
    // // size of XY plane
    // long sizeXY = (long) MetaDataUtil.getSizeX(meta, series) * (long) MetaDataUtil.getSizeY(meta,
    // series);
    //
    // // we can't handle that plane size
    // if (sizeXY > Integer.MAX_VALUE)
    // {
    // if (!warningDisplayed)
    // {
    // // notify we can't open that image at full resolution
    // if (!Icy.getMainInterface().isHeadLess())
    // new AnnounceFrame("XY plane size is >= 2^31, try to open sub resolution of the image...",
    // 10);
    // else
    // System.out.println("XY plane size is >= 2^31, try to open sub resolution of the image...");
    //
    // warningDisplayed = true;
    // }
    //
    // // reduce resolution until XY plane size is acceptable
    // do
    // {
    // resolution++;
    // sizeXY /= 4;
    // }
    // while (sizeXY > Integer.MAX_VALUE);
    // }
    //
    // // get free memory
    // long freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
    // // check that we have enough memory for the whole image and the ARGB image used for display
    // (sizeXY * 4)
    // long sizeInByte = MetaDataUtil.getDataSize(meta, series, resolution) + (sizeXY * 4);
    //
    // // not enough memory to store the whole image ?
    // if (sizeInByte > freeInByte)
    // {
    // // try to release some memory
    // System.gc();
    // // get updated free memory
    // freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
    // }
    //
    // // still not enough memory ?
    // if (sizeInByte > freeInByte)
    // {
    // if (!warningDisplayed)
    // {
    // // display an information message that we can only load a sub resolution of the image
    // if (!Icy.getMainInterface().isHeadLess())
    // new AnnounceFrame(
    // "Not enough memory to open full resolution of the image, try to open sub resolution...", 10);
    // else
    // System.out
    // .println("Not enough memory to open full resolution of the image, try to open sub
    // resolution...");
    //
    // warningDisplayed = true;
    // }
    //
    // // reduce image resolution so the whole image fit in about 70% of available memory (safe)
    // freeInByte = (int) (freeInByte * 0.7d);
    // do
    // {
    // resolution++;
    // sizeInByte /= 4;
    // }
    // while (sizeInByte > freeInByte);
    // }
    //
    // return resolution;
    // }

    /**
     * @deprecated Use {@link #getSequenceFileImporters(String)} instead.
     */
    @SuppressWarnings("resource")
    @Deprecated
    public static IFormatReader getReader(String path) throws FormatException, IOException
    {
        return new ImageReader().getReader(path);
    }

    /**
     * Loads and returns metadata of the specified image file with given importer.<br>
     * It can returns <code>null</code> if the specified file is not a valid or supported) image
     * file.
     */
    public static OMEXMLMetadata getOMEXMLMetaData(SequenceFileImporter importer, String path)
            throws UnsupportedFormatException, IOException
    {
        if (importer.open(path, 0))
        {
            try
            {
                return importer.getOMEXMLMetaData();
            }
            finally
            {
                importer.close();
            }
        }

        return null;
    }

    /**
     * Loads and returns metadata of the specified image file.<br>
     * It can returns <code>null</code> if the specified file is not a valid or supported) image
     * file.
     */
    public static OMEXMLMetadata getOMEXMLMetaData(String path) throws UnsupportedFormatException, IOException
    {
        OMEXMLMetadata result;
        UnsupportedFormatException lastError = null;

        for (SequenceFileImporter importer : getSequenceFileImporters(path))
        {
            try
            {
                result = getOMEXMLMetaData(importer, path);

                if (result != null)
                    return result;
            }
            catch (UnsupportedFormatException e)
            {
                lastError = e;
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported :\n", lastError);
    }

    /**
     * @deprecated Use {@link #getOMEXMLMetaData(SequenceFileImporter, String)} instead.
     */
    @Deprecated
    public static OMEXMLMetadataImpl getMetaData(SequenceFileImporter importer, String path)
            throws UnsupportedFormatException, IOException
    {
        if (importer.open(path, 0))
        {
            try
            {
                return importer.getMetaData();
            }
            finally
            {
                importer.close();
            }
        }

        return null;
    }

    /**
     * @deprecated Use {@link #getOMEXMLMetaData(String)} instead
     */
    @Deprecated
    public static OMEXMLMetadataImpl getMetaData(String path) throws UnsupportedFormatException, IOException
    {
        OMEXMLMetadataImpl result;
        UnsupportedFormatException lastError = null;

        for (SequenceFileImporter importer : getSequenceFileImporters(path))
        {
            try
            {
                result = getMetaData(importer, path);

                if (result != null)
                    return result;
            }
            catch (UnsupportedFormatException e)
            {
                lastError = e;
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported :\n", lastError);
    }

    /**
     * @deprecated Use {@link #getOMEXMLMetaData(String)} instead.
     */
    @Deprecated
    public static OMEXMLMetadataImpl getMetaData(File file) throws UnsupportedFormatException, IOException
    {
        return getMetaData(file.getAbsolutePath());
    }

    /**
     * @deprecated Use {@link #getOMEXMLMetaData(String)} instead.
     */
    @Deprecated
    protected static OMEXMLMetadataImpl getMetaData(IFormatReader reader, String path)
            throws FormatException, IOException
    {
        // prepare meta data store structure
        reader.setMetadataStore((MetadataStore) OMEUtil.createOMEXMLMetadata());
        // load file with LOCI library
        reader.setId(path);

        return (OMEXMLMetadataImpl) reader.getMetadataStore();
    }

    // /**
    // * Use the given importer to load and return metadata of the specified image file.
    // *
    // * @throws UnsupportedFormatException
    // * @throws IOException
    // */
    // public static OMEXMLMetadataImpl getMetaData(SequenceFileImporter importer, File file)
    // throws UnsupportedFormatException, IOException
    // {
    // // load current file and add to results
    // return importer.getMetaData(file);
    // }

    /**
     * Returns a thumbnail of the specified image file path.<br>
     * It can return <code>null</code> if the specified file is not a valid or supported image file.
     * 
     * @param importer
     *        Importer used to open and load the thumbnail from the image file.
     * @param path
     *        image file path.
     * @param series
     *        Series index we want to retrieve thumbnail from (for multi series image).<br>
     *        Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(SequenceFileImporter importer, String path, int series)
            throws UnsupportedFormatException, IOException
    {
        if (importer.open(path, 0))
        {
            try
            {
                return importer.getThumbnail(series);
            }
            finally
            {
                importer.close();
            }
        }

        return null;
    }

    /**
     * Returns a thumbnail of the specified image file path.
     * 
     * @param path
     *        image file path.
     * @param series
     *        Series index we want to retrieve thumbnail from (for multi series image).<br>
     *        Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(String path, int series) throws UnsupportedFormatException, IOException
    {
        IcyBufferedImage result;
        UnsupportedFormatException lastError = null;

        for (SequenceFileImporter importer : getSequenceFileImporters(path))
        {
            try
            {
                result = loadThumbnail(importer, path, series);

                if (result != null)
                    return result;
            }
            catch (UnsupportedFormatException e)
            {
                lastError = e;
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported :\n", lastError);
    }

    /**
     * @deprecated Use {@link IcyBufferedImage#createFrom(IFormatReader, int, int)} instead.
     */
    @Deprecated
    public static IcyBufferedImage loadImage(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        // return an icy image
        return IcyBufferedImage.createFrom(reader, z, t);
    }

    /**
     * @deprecated Use {@link IcyBufferedImage#createFrom(IFormatReader, int, int)} with Z and T
     *             parameters set to 0.
     */
    @Deprecated
    public static IcyBufferedImage loadImage(IFormatReader reader) throws FormatException, IOException
    {
        // return an icy image
        return IcyBufferedImage.createFrom(reader, 0, 0);
    }

    /**
     * @deprecated Use {@link #loadImage(String, int, int)} instead.
     */
    @Deprecated
    public static IcyBufferedImage loadImage(File file, int z, int t) throws FormatException, IOException
    {
        return loadImage(file.getAbsolutePath(), z, t);
    }

    /**
     * @deprecated Use {@link #loadImage(String)} instead.
     */
    @Deprecated
    public static IcyBufferedImage loadImage(File file) throws UnsupportedFormatException, IOException
    {
        return loadImage(file.getAbsolutePath());
    }

    /**
     * @deprecated Use {@link #loadImage(String, int, int, int)} instead.
     */
    @Deprecated
    public static IcyBufferedImage loadImage(String path, int z, int t) throws FormatException, IOException
    {
        final IFormatReader reader = getReader(path);

        // disable file grouping
        reader.setGroupFiles(false);
        // set file path
        reader.setId(path);
        try
        {
            // return an icy image
            return IcyBufferedImage.createFrom(reader, z, t);
        }
        finally
        {
            // close reader
            reader.close();
        }
    }

    /**
     * Load and return the image at given position from the specified file path.<br>
     * For lower image level access, you can use importer methods.
     * 
     * @param importer
     *        Importer used to open and load the image file.
     * @param path
     *        image file path.
     * @param series
     *        Series index we want to retrieve image from (for multi series image).<br>
     *        Set to 0 if unsure (default).
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public static IcyBufferedImage loadImage(SequenceFileImporter importer, String path, int series, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        if ((importer == null) || !importer.open(path, 0))
            throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");

        try
        {
            return importer.getImage(series, z, t);
        }
        finally
        {
            importer.close();
        }
    }

    /**
     * Load and return the image at given position from the specified file path.<br>
     * For lower image level access, you can use {@link #getSequenceFileImporter(String, boolean)}
     * method and
     * directly work through the returned {@link ImageProvider} interface.
     * 
     * @param path
     *        image file path.
     * @param series
     *        Series index we want to retrieve image from (for multi series image).<br>
     *        Set to 0 if unsure (default).
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public static IcyBufferedImage loadImage(String path, int series, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        return loadImage(getSequenceFileImporter(path, true), path, series, z, t);
    }

    /**
     * Load and return a single image from the specified file path.<br>
     * If the specified file contains severals image the first image is returned.
     */
    public static IcyBufferedImage loadImage(String path) throws UnsupportedFormatException, IOException
    {
        return loadImage(path, 0, 0, 0);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static Sequence[] loadSequences(File[] files, int[] series, boolean separate, boolean autoOrder,
            boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<>();
        final List<String> paths = FileUtil.toPaths(CollectionUtil.asList(files));

        if (series == null)
            result.addAll(loadSequences(paths, -1, separate, autoOrder, false, showProgress));
        else
        {
            for (int s : series)
                result.addAll(loadSequences(paths, s, separate, autoOrder, false, showProgress));
        }

        return result.toArray(new Sequence[result.size()]);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate,
            boolean autoOrder, boolean showProgress)
    {
        final int[] seriesArray;

        if (series != null)
        {
            seriesArray = new int[series.size()];

            for (int i = 0; i < seriesArray.length; i++)
                seriesArray[i] = series.get(i).intValue();
        }
        else
        {
            seriesArray = new int[1];
            seriesArray[0] = 0;
        }

        return Arrays.asList(
                loadSequences(files.toArray(new File[files.size()]), seriesArray, separate, autoOrder, showProgress));
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate,
            boolean showProgress)
    {
        return loadSequences(files, series, separate, true, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate)
    {
        return loadSequences(files, series, separate, true, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series)
    {
        return loadSequences(files, series, false, true, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, boolean separate, boolean showProgress)
    {
        return loadSequences(files, null, separate, true, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, boolean separate)
    {
        return loadSequences(files, null, separate, true, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, boolean separate, boolean display, boolean addToRecent)
    {
        return loadSequences(files, null, separate, true, true);
    }

    /**
     * @deprecated Use {@link #loadSequence(File, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence[] loadSequences(File file, int[] series, boolean showProgress)
    {
        return loadSequences(new File[] {file}, series, false, true, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequences(File, int[], boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series, boolean showProgress)
    {
        final int[] seriesArray;

        if (series != null)
        {
            seriesArray = new int[series.size()];

            for (int i = 0; i < seriesArray.length; i++)
                seriesArray[i] = series.get(i).intValue();
        }
        else
        {
            seriesArray = new int[1];
            seriesArray[0] = 0;
        }

        return Arrays.asList(loadSequences(new File[] {file}, seriesArray, false, true, showProgress));
    }

    /**
     * @deprecated Use {@link #loadSequences(File, int[], boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series)
    {
        return loadSequences(file, series, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(File, int[], boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series, boolean display, boolean addToRecent)
    {
        return loadSequences(file, series, true);
    }

    /**
     * @deprecated Use {@link #loadSequence(File, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file, boolean showProgress)
    {
        return loadSequence(new File[] {file}, -1, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(File, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file)
    {
        return loadSequence(new File[] {file}, -1, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(List, int, boolean, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static Sequence[] loadSequences(File[] files, int series, boolean separate, boolean autoOrder,
            boolean showProgress)
    {
        final List<Sequence> result = loadSequences(FileUtil.toPaths(CollectionUtil.asList(files)), series, separate,
                autoOrder, false, showProgress);
        return result.toArray(new Sequence[result.size()]);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param files
     *        List of image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    @Deprecated
    public static Sequence loadSequence(File[] files, int series, boolean showProgress)
    {
        final Sequence[] result = loadSequences(files, series, false, true, showProgress);

        if (result.length > 0)
            return result[0];

        return null;
    }

    /**
     * @deprecated Use {@link #loadSequence(String, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file, int series, boolean showProgress)
    {
        return loadSequence(new File[] {file}, series, showProgress);
    }

    /**
     * Load a list of sequence from the specified list of file with the given {@link SequenceFileImporter} and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could be opened (not supported).<br>
     * If the user cancelled the action (series selection dialog) then it returns <code>null</code>.
     * 
     * @param importer
     *        Importer used to open and load image files.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers match the user will have to select the appropriate one from a
     *        selection dialog.
     * @param paths
     *        List of image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user<br>
     *        to select the series to open from a series selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    @SuppressWarnings("resource")
    public static List<Sequence> loadSequences(SequenceFileImporter importer, List<String> paths, int series,
            boolean separate, boolean autoOrder, boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<>();

        // detect if this is a complete folder load
        final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
        // explode path list
        final List<String> singlePaths = cleanNonImageFile(explode(paths));
        final boolean adjSeparate = (singlePaths.size() <= 1) || separate;
        // get the sequence importers first
        final Map<SequenceFileImporter, List<String>> sequenceFileImporters = getSequenceFileImporters(importer,
                singlePaths, !adjSeparate, false);

        for (Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet())
        {
            final SequenceFileImporter imp = entry.getKey();
            final List<String> currPaths = entry.getValue();
            final boolean dir = directory && (sequenceFileImporters.size() == 1)
                    && (currPaths.size() == singlePaths.size());

            // load sequence
            result.addAll(
                    loadSequences(imp, currPaths, series, adjSeparate, autoOrder, dir, addToRecent, showProgress));

            // remove loaded files
            singlePaths.removeAll(currPaths);
        }

        // remove remaining XML persistence files...
        for (int i = singlePaths.size() - 1; i >= 0; i--)
            if (SequencePersistent.isValidXMLPersitence(singlePaths.get(i)))
                singlePaths.remove(i);

        // remaining files ?
        if (singlePaths.size() > 0)
        {
            // get first found importer for remaining files
            final Map<SequenceFileImporter, List<String>> importers = getSequenceFileImporters(singlePaths, true);

            // user canceled action for these paths so we remove them
            for (List<String> values : importers.values())
                singlePaths.removeAll(values);

            if (singlePaths.size() > 0)
            {
                // just log in console
                System.err.println("No compatible importer found for the following files:");
                for (String path : singlePaths)
                    System.err.println(path);
                System.err.println();
            }
        }

        // return sequences
        return result;
    }

    /**
     * Loads a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could not be opened (not
     * supported).<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     * 
     * @param paths
     *        List of image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user<br>
     *        to select the series to open from a series selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(List<String> paths, int series, boolean separate, boolean autoOrder,
            boolean addToRecent, boolean showProgress)
    {
        return loadSequences(null, paths, series, separate, autoOrder, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param importer
     *        Importer used to load the image file (can be null for automatic selection).
     * @param paths
     *        List of image file to load.
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(SequenceFileImporter importer, List<String> paths, boolean addToRecent,
            boolean showProgress)
    {
        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;
        Sequence result = null;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try
        {
            // we want only one group
            final SequenceFileGroup group = SequenceFileSticher.groupFiles(importer, cleanNonImageFile(paths),
                    showProgress, loadingFrame);
            // do group loading
            result = internalLoadGroup(group, false, mainMenu, loadingFrame);
            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (Throwable t)
        {
            // just show the error
            IcyExceptionHandler.showErrorMessage(t, true);

            if (loadingFrame != null)
                new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                        : "Failed to open file(s), see the console output for more details.");
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param importer
     *        Importer used to load the image file (can be null for automatic selection).
     * @param paths
     *        List of image file to load.
     * @param showProgress
     *        Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(SequenceFileImporter importer, List<String> paths, boolean showProgress)
    {
        return loadSequence(importer, paths, false, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(SequenceFileImporter, List, boolean, boolean)} instead
     */
    @Deprecated
    public static Sequence loadSequence(List<?> files, boolean display, boolean addToRecent)
    {
        return loadSequence(files, true);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     * 
     * @param files
     *        List of image file to load (can be String or File object).
     * @param showProgress
     *        Show progression of loading process.
     */
    @SuppressWarnings("unchecked")
    public static Sequence loadSequence(List<?> files, boolean showProgress)
    {
        if (files.size() == 0)
            return null;

        final List<String> paths;

        if (files.get(1) instanceof File)
            paths = FileUtil.toPaths((List<File>) files);
        else
            paths = (List<String>) files;

        return loadSequence(null, paths, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     * 
     * @param files
     *        List of image file to load (can be String or File object).
     */
    public static Sequence loadSequence(List<?> files)
    {
        return loadSequence(files, true);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code>
     * if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        Image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        series selector dialog.
     * @param addToRecent
     *        If set to true the path will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int series, boolean addToRecent,
            boolean showProgress)
    {
        return loadSequence(importer, path, series, 0, null, -1, -1, -1, -1, -1, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        Image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int series, boolean showProgress)
    {
        return loadSequence(importer, path, series, false, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * If several importers match to open the file the user will have to select the appropriate one
     * from a selection dialog if <code>showProgress</code> parameter is set to <code>true</code>
     * otherwise the first
     * compatible importer is automatically used.
     * 
     * @param path
     *        Image file to load.
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(String path, int series, boolean showProgress)
    {
        return loadSequence(null, path, series, showProgress);
    }

    /**
     * @deprecated Use {@link #load(List, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files)
    {
        load(files.toArray(new File[files.size()]), false, true, true);
    }

    /**
     * @deprecated Use {@link #load(List, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate)
    {
        load(files.toArray(new File[files.size()]), separate, true, true);
    }

    /**
     * @deprecated Use {@link #load(List, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate, boolean showProgress)
    {
        load(files.toArray(new File[files.size()]), separate, true, showProgress);
    }

    // /**
    // * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
    // */
    // @Deprecated
    // public static void load(List<File> files, boolean separate, boolean autoOrder, boolean
    // showProgress)
    // {
    // load(files.toArray(new File[files.size()]), separate, autoOrder, showProgress);
    // }

    /**
     * @deprecated Use {@link #load(String, boolean)} instead.
     */
    @Deprecated
    public static void load(File file)
    {
        load(new File[] {file}, false, false, true);
    }

    /**
     * @deprecated Use {@link #load(List, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(final File[] files, final boolean separate, final boolean autoOrder,
            final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // load sequence
                final Sequence[] sequences = loadSequences(files, -1, separate, autoOrder, showProgress);
                // and display them
                for (Sequence seq : sequences)
                    Icy.getMainInterface().addSequence(seq);
            }
        });
    }

    /**
     * @deprecated Use {@link #load(String, boolean)} instead.
     */
    @Deprecated
    public static void load(File file, boolean showProgress)
    {
        load(new File[] {file}, false, false, showProgress);
    }

    /**
     * Load the specified files with the given {@link FileImporter}.<br>
     * The loading process is asynchronous.<br>
     * The FileImporter is responsible to make the loaded files available in the application.<br>
     * This method should be used only for non image file.
     * 
     * @param importer
     *        Importer used to open and load files.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers match the user will have to select the appropriate one from a
     *        selection dialog.
     * @param paths
     *        list of file to load
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final FileImporter importer, final List<String> paths, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // explode path list
                final List<String> singlePaths = explode(paths);

                if (singlePaths.size() > 0)
                {
                    // get the file importer now for remaining file
                    final Map<FileImporter, List<String>> fileImporters;

                    // importer not defined --> find the appropriate importers
                    if (importer == null)
                        fileImporters = getFileImporters(singlePaths, false);
                    else
                    {
                        fileImporters = new HashMap<>(1);
                        fileImporters.put(importer, new ArrayList<>(singlePaths));
                    }

                    for (Entry<FileImporter, List<String>> entry : fileImporters.entrySet())
                    {
                        final FileImporter importer = entry.getKey();
                        final List<String> currPaths = entry.getValue();

                        // load files
                        loadFiles(importer, paths, true, showProgress);

                        // remove loaded files
                        singlePaths.removeAll(currPaths);
                    }
                }

                // remaining files ?
                if (singlePaths.size() > 0)
                {
                    // get first found importer for remaining files
                    final Map<SequenceFileImporter, List<String>> importers = getSequenceFileImporters(singlePaths,
                            true);

                    // user canceled action for these paths so we remove them
                    for (List<String> values : importers.values())
                        singlePaths.removeAll(values);

                    if (singlePaths.size() > 0)
                    {
                        // just log in console
                        System.err.println("No compatible importer found for the following files:");
                        for (String path : singlePaths)
                            System.err.println(path);
                        System.err.println();
                    }
                }
            }
        });
    }

    /**
     * Load the specified files with the given {@link FileImporter}.<br>
     * The FileImporter is responsible to make the loaded files available in the application.<br>
     * This method should be used only for non image file.
     * 
     * @param importer
     *        Importer used to open and load image files.
     * @param paths
     *        list of file to load
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened files.
     * @param showProgress
     *        Show progression in loading process
     */
    static void loadFiles(FileImporter importer, List<String> paths, boolean addToRecent, boolean showProgress)
    {
        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
        {
            loadingFrame = new FileFrame("Loading", null);
            loadingFrame.setLength(paths.size());
            loadingFrame.setPosition(0);
        }
        else
            loadingFrame = null;

        try
        {
            // load each file in a separate sequence
            for (String path : paths)
            {
                if (loadingFrame != null)
                    loadingFrame.incPosition();

                // load current file
                importer.load(path, loadingFrame);

                // add as separate item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));
            }
        }
        catch (Throwable t)
        {
            // just show the error
            IcyExceptionHandler.showErrorMessage(t, true);
            if (loadingFrame != null)
                new FailedAnnounceFrame("Failed to open file(s), see the console output for more details.");
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }
    }

    /**
     * Load the specified image file with the given {@link SequenceFileImporter}.<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        image file to load
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        series selector dialog.
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base
     *        image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region of the image we want to retrieve.<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final SequenceFileImporter importer, final String path, final int series,
            final int resolution, final Rectangle region, final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean addToRecent, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // normal opening operation ?
                if ((resolution == 0) && (region == null) && (minZ <= 0) && (maxZ == -1) && (minT <= 0) && (maxT == -1)
                        && (channel == -1))
                {
                    // load sequence (we use this method to allow multi series opening)
                    final List<Sequence> sequences = loadSequences(
                            (importer == null) ? getSequenceFileImporter(path, !showProgress) : importer,
                            CollectionUtil.asList(path), series, true, false, false, addToRecent, showProgress);

                    // and display them
                    for (Sequence sequence : sequences)
                        if (sequence != null)
                            Icy.getMainInterface().addSequence(sequence);
                }
                else
                {
                    // load sequence
                    final Sequence sequence = loadSequence(importer, path, series, resolution, region, minZ, maxZ, minT,
                            maxT, channel, addToRecent, showProgress);

                    // and display it
                    if (sequence != null)
                        Icy.getMainInterface().addSequence(sequence);
                }
            }
        });
    }

    /**
     * Load the specified image file list (try to group them as much as possible).<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     * 
     * @param paths
     *        list of file we want to group to build a to load
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base
     *        image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region of the image we want to retrieve.<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final List<String> paths, final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT, final int channel, final boolean directory,
            final boolean addToRecent, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                final ApplicationMenu mainMenu;
                final FileFrame loadingFrame;

                if (addToRecent)
                    mainMenu = Icy.getMainInterface().getApplicationMenu();
                else
                    mainMenu = null;
                if (showProgress && !Icy.getMainInterface().isHeadLess())
                    loadingFrame = new FileFrame("Loading", null);
                else
                    loadingFrame = null;

                try
                {
                    // load sequence from group with advanced options
                    for (SequenceFileGroup group : SequenceFileSticher.groupAllFiles(null, cleanNonImageFile(paths),
                            true, loadingFrame))
                    {
                        final Sequence sequence = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT,
                                channel, directory, mainMenu, loadingFrame);

                        if (sequence != null)
                        {
                            // load sequence XML data
                            if (GeneralPreferences.getSequencePersistence())
                                sequence.loadXMLData();
                            // and display it
                            Icy.getMainInterface().addSequence(sequence);
                        }
                    }
                }
                catch (Throwable t)
                {
                    // just show the error
                    IcyExceptionHandler.showErrorMessage(t, true);

                    if (loadingFrame != null)
                        new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                                : "Failed to open file(s), see the console output for more details.");
                }
                finally
                {
                    if (loadingFrame != null)
                        loadingFrame.close();
                }
            }
        });
    }

    /**
     * Load the specified image file group (built using SequenceFileSticher class).<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     * 
     * @param group
     *        Sequence file group built using {@link SequenceFileSticher}.
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base
     *        image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region of the image we want to retrieve.<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final SequenceFileGroup group, final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT, final int channel, final boolean directory,
            final boolean addToRecent, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                final ApplicationMenu mainMenu;
                final FileFrame loadingFrame;

                if (addToRecent)
                    mainMenu = Icy.getMainInterface().getApplicationMenu();
                else
                    mainMenu = null;
                if (showProgress && !Icy.getMainInterface().isHeadLess())
                    loadingFrame = new FileFrame("Loading", null);
                else
                    loadingFrame = null;

                try
                {
                    // load sequence from group with advanced options
                    final Sequence sequence = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT,
                            channel, directory, mainMenu, loadingFrame);

                    if (sequence != null)
                    {
                        // load sequence XML data
                        if (GeneralPreferences.getSequencePersistence())
                            sequence.loadXMLData();
                        // and display it
                        Icy.getMainInterface().addSequence(sequence);
                    }
                }
                catch (Throwable t)
                {
                    // just show the error
                    IcyExceptionHandler.showErrorMessage(t, true);

                    if (loadingFrame != null)
                        new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                                : "Failed to open file(s), see the console output for more details.");
                }
                finally
                {
                    if (loadingFrame != null)
                        loadingFrame.close();
                }
            }
        });
    }

    /**
     * Load the specified image files with the given {@link SequenceFileImporter}.<br>
     * The loading process is asynchronous.<br>
     * If <i>separate</i> is false the loader try to set image in the same sequence.<br>
     * If <i>separate</i> is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param importer
     *        Importer used to open and load image files.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers match the user will have to select the appropriate one from a
     *        selection dialog.
     * @param paths
     *        list of image file to load
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final SequenceFileImporter importer, final List<String> paths, final boolean separate,
            final boolean autoOrder, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // load sequence
                final List<Sequence> sequences = loadSequences(importer, paths, -1, separate, autoOrder, true,
                        showProgress);
                // and display them
                for (Sequence seq : sequences)
                    Icy.getMainInterface().addSequence(seq);
            }
        });
    }

    /**
     * Load the specified files (asynchronous process) by using automatically the appropriate
     * {@link FileImporter} or {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified files are image files:<br>
     * When <i>separate</i> is <code>false</code> the loader try to set image in the same
     * sequence.<br>
     * When <i>separate</i> is <code>true</code> each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param paths
     *        list of file to load
     * @param separate
     *        Force image to be loaded in separate sequence (image files only)
     * @param autoOrder
     *        Try to order image in sequence from their filename (image files only)
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final List<String> paths, final boolean separate, final boolean autoOrder,
            final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @SuppressWarnings("resource")
            @Override
            public void run()
            {
                // detect if this is a complete folder load
                final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
                // explode path list
                final List<String> singlePaths = cleanNonImageFile(explode(paths));
                final boolean adjSeparate = (singlePaths.size() <= 1) || separate;
                final Map<SequenceFileImporter, List<String>> sequenceFileImporters = getSequenceFileImporters(null,
                        singlePaths, !adjSeparate, false);

                for (Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet())
                {
                    final SequenceFileImporter importer = entry.getKey();
                    final List<String> currPaths = entry.getValue();
                    final boolean dir = directory && (sequenceFileImporters.size() == 1)
                            && (currPaths.size() == singlePaths.size());

                    // load sequence
                    final List<Sequence> sequences = loadSequences(importer, currPaths, -1, adjSeparate, autoOrder, dir,
                            true, showProgress);
                    // and display them
                    for (Sequence seq : sequences)
                        Icy.getMainInterface().addSequence(seq);

                    // remove loaded files
                    singlePaths.removeAll(currPaths);
                }

                if (singlePaths.size() > 0)
                {
                    // get the file importer now for remaining file
                    final Map<FileImporter, List<String>> fileImporters = getFileImporters(singlePaths, false);

                    for (Entry<FileImporter, List<String>> entry : fileImporters.entrySet())
                    {
                        final FileImporter importer = entry.getKey();
                        final List<String> currPaths = entry.getValue();

                        // load files
                        loadFiles(importer, paths, true, showProgress);

                        // remove loaded files
                        singlePaths.removeAll(currPaths);
                    }
                }

                // remaining files ?
                if (singlePaths.size() > 0)
                {
                    // get first found importer for remaining files
                    final Map<SequenceFileImporter, List<String>> importers = getSequenceFileImporters(singlePaths,
                            true);

                    // user canceled action for these paths so we remove them
                    for (List<String> values : importers.values())
                        singlePaths.removeAll(values);

                    if (singlePaths.size() > 0)
                    {
                        // just log in console
                        System.err.println("No compatible importer found for the following files:");
                        for (String path : singlePaths)
                            System.err.println(path);
                        System.err.println();
                    }
                }
            }
        });
    }

    /**
     * Load the specified file (asynchronous process) by using automatically the appropriate
     * {@link FileImporter} or
     * {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified file is an image file, the resulting sequence is automatically displayed
     * when process complete.
     * 
     * @param path
     *        file to load
     * @param showProgress
     *        Show progression of loading process.
     */
    public static void load(String path, boolean showProgress)
    {
        load(CollectionUtil.createArrayList(path), false, false, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequences(List, int, boolean, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    static Sequence[] loadSequences(SequenceFileImporter importer, File[] files, int series, boolean separate,
            boolean autoOrder, boolean directory, boolean addToRecent, boolean showProgress)
    {
        final List<String> paths = cleanNonImageFile(CollectionUtil.asList(FileUtil.toPaths(files)));
        final List<Sequence> result = loadSequences(importer, paths, series, separate, autoOrder, directory,
                addToRecent, showProgress);
        return result.toArray(new Sequence[result.size()]);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code>
     * if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        image file to load
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        series selector dialog.
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *        and so on...
     * @param region
     *        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int series, int resolution,
            Rectangle region, int minZ, int maxZ, int minT, int maxT, int channel, boolean addToRecent,
            boolean showProgress)
    {
        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;
        final Sequence result;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", path);
        else
            loadingFrame = null;

        // importer is not specified ? --> get a compatible one.
        final SequenceFileImporter imp = (importer == null) ? getSequenceFileImporter(path, !showProgress) : importer;

        try
        {
            // open image
            imp.open(path, 0);

            // get metadata
            final OMEXMLMetadata meta = imp.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // series selection
            int selectedSerie;

            // give the opportunity to select the series (single one) to open ?
            if (series == -1)
            {
                try
                {
                    // series selection (create a new importer instance as selectSerie(..) does async processes)
                    selectedSerie = selectSerie(imp.getClass().newInstance(), path, meta, 0);
                }
                catch (Throwable t)
                {
                    IcyExceptionHandler.showErrorMessage(t, true, true);
                    System.err.print("Opening first series by default...");
                    selectedSerie = 0;
                }

                // user cancelled action in the series selection ? null = cancel
                if (selectedSerie == -1)
                    return null;
            }
            else
                selectedSerie = series;

            // load the image
            result = internalLoadSingle(imp, meta, selectedSerie, resolution, region, minZ, maxZ, minT, maxT, channel,
                    loadingFrame);

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // imp.close();

            // add as separate item to recent file list
            if (mainMenu != null)
                mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));

            // TODO: restore colormap --> try to recover colormap

            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (Throwable t)
        {
            try
            {
                // close importer when error happen (not stored in Sequence so we need to close it manually)
                imp.close();
            }
            catch (Exception e)
            {
                // ignore
            }

            // just show the error
            IcyExceptionHandler.showErrorMessage(t, true);

            if (loadingFrame != null)
            {
                new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                        : "Failed to open file(s), see the console output for more details.");
            }

            return null;
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     * 
     * @param paths
     *        list of file where to load image from
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to
     *        <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base
     *        image
     *        resolution / 2 and so on...
     * @param region
     *        The 2D region of the image we want to retrieve.<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(List<String> paths, int resolution, Rectangle region, int minZ, int maxZ,
            int minT, int maxT, int channel, boolean directory, boolean addToRecent, boolean showProgress)
    {
        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;
        Sequence result = null;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try
        {
            // we want only one group
            final SequenceFileGroup group = SequenceFileSticher.groupFiles(null, cleanNonImageFile(paths), true,
                    loadingFrame);
            // do group loading
            result = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT, channel, directory, mainMenu,
                    loadingFrame);
            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (Throwable t)
        {
            // just show the error
            IcyExceptionHandler.showErrorMessage(t, true);

            if (loadingFrame != null)
                new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                        : "Failed to open file(s), see the console output for more details.");
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Loads the specified image files and return them as list of sequence.<br>
     * If 'separate' is false the loader try to set images in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     * 
     * @param importer
     *        Importer used to open and load images (cannot be <code>null</code> here) except when separate is false
     * @param paths
     *        list of image file to load
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        series selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param autoOrder
     *        If set to true then images are automatically orderer from their filename.
     * @param directory
     *        Specify is the source is a single complete directory
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    static List<Sequence> loadSequences(SequenceFileImporter importer, List<String> paths, int series, boolean separate,
            boolean autoOrder, boolean directory, boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<>();

        // nothing to load
        if (paths.size() <= 0)
            return result;

        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try
        {
            final List<String> remainingFiles = new ArrayList<>(paths);

            // load each file in a separate sequence
            if (separate || (paths.size() <= 1))
            {
                if (loadingFrame != null)
                {
                    // each file can contains several image so we use 100 "inter-step"
                    loadingFrame.setLength(paths.size() * 100d);
                    loadingFrame.setPosition(0d);
                }

                // load each file in a separate sequence
                for (String path : paths)
                {
                    // load the file
                    final List<Sequence> sequences = internalLoadSingle(importer, path, series, loadingFrame);

                    // special case where loading was interrupted
                    if (sequences == null)
                    {
                        // no error
                        remainingFiles.clear();
                        break;
                    }

                    if (sequences.size() > 0)
                    {
                        // add sequences to result
                        result.addAll(sequences);
                        // remove path from remaining
                        remainingFiles.remove(path);
                        // add as separate item to recent file list
                        if (mainMenu != null)
                            mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));
                    }

                    // interrupt loading
                    if ((loadingFrame != null) && loadingFrame.isCancelRequested())
                    {
                        // no error
                        remainingFiles.clear();
                        break;
                    }
                }
            }
            // grouped loading
            else
            {
                // get file group
                final Collection<SequenceFileGroup> groups = SequenceFileSticher.groupAllFiles(importer, paths,
                        autoOrder, loadingFrame);

                for (SequenceFileGroup group : groups)
                {
                    final Sequence sequence = internalLoadGroup(group, directory, mainMenu, loadingFrame);

                    // loading interrupted ?
                    if ((loadingFrame != null) && loadingFrame.isCancelRequested())
                    {
                        // no error
                        remainingFiles.clear();
                        break;
                    }

                    // remove all paths from the group in remaining list
                    for (SequencePosition pos : group.positions)
                        remainingFiles.remove(pos.getPath());

                    // add to result
                    result.add(sequence);
                }

                // if (loadingFrame != null)
                // {
                // loadingFrame.setAction("Loading");
                // // each file can contains several image so we use 100 "inter step"
                // loadingFrame.setLength(filePositions.size() * 100d);
                // loadingFrame.setPosition(0d);
                // }
                //
                // for (FilePosition filePos : filePositions)
                // {
                // final String path = filePos.path;
                // // load the file
                // final List<Sequence> sequences = internalLoadSingle(importer, path, series, loadingFrame);
                //
                // // special case where loading was interrupted
                // if (sequences == null)
                // {
                // // no error
                // remainingFiles.clear();
                // break;
                // }
                //
                // final int s = filePos.getS();
                // final int z = filePos.getZ();
                // final int t = filePos.getT();
                // final int c = filePos.getC();
                // boolean concat;
                //
                // // special case of single result --> try to concatenate to last sequence
                // if ((sequences.size() == 1) && !map.isEmpty())
                // {
                // final Sequence seq = sequences.get(0);
                // final int sizeZ = seq.getSizeZ();
                // final int sizeT = seq.getSizeT();
                // final int sizeC = seq.getSizeC();
                //
                // concat = true;
                // // concatenation restriction
                // if (lastS != s)
                // concat = false;
                // if ((sizeZ > 1) && (z > 0))
                // concat = false;
                // if ((sizeT > 1) && (t > 0))
                // concat = false;
                // if ((sizeC > 1) && (c > 0))
                // concat = false;
                //
                // if (concat)
                // {
                // // find last sequence for this channel
                // final Sequence lastSequence = map.get(Integer.valueOf(c));
                //
                // // determine if concatenation is possible
                // if ((lastSequence != null) && !lastSequence.isCompatible(seq.getFirstImage()))
                // concat = false;
                // }
                //
                // // update series index
                // lastS = s;
                // }
                // else
                // concat = false;
                //
                // // sequence correctly loaded ?
                // if (sequences.size() > 0)
                // {
                // if (concat)
                // {
                // final Sequence seq = sequences.get(0);
                // // find last sequence for this channel
                // Sequence lastSequence = map.get(Integer.valueOf(c));
                //
                // // concatenate
                // lastSequence = concatenateSequence(lastSequence, seq, t > 0, z > 0);
                // // store the merged sequence for this channel
                // map.put(Integer.valueOf(c), lastSequence);
                // }
                // else
                // {
                // // concatenate sequences in map and add it to result list
                // addSequences(result, map);
                // // if on first channel then put the last sequence result in the map
                // if (c == 0)
                // map.put(Integer.valueOf(0), sequences.remove(sequences.size() - 1));
                // // and add the rest to the list
                // if (sequences.size() > 0)
                // result.addAll(sequences);
                // }
                //
                // // remove path from remaining
                // remainingFiles.remove(path);
                // }
                //
                // // interrupt loading
                // if ((loadingFrame != null) && loadingFrame.isCancelRequested())
                // {
                // // no error
                // remainingFiles.clear();
                // break;
                // }
                // }
                //
                // // concatenate last sequences in map and add it to result list
                // addSequences(result, map);
                //
                // // add as one item to recent file list
                // if (mainMenu != null)
                // {
                // // set only the directory entry
                // if (directory)
                // mainMenu.addRecentFile(FileUtil.getDirectory(paths.get(0), false));
                // else
                // mainMenu.addRecentFile(paths);
                // }
            }

            // TODO: restore colormap --> try to recover colormap

            if (remainingFiles.size() > 0)
            {
                System.err.println("Cannot open the following file(s) (format not supported):");
                for (String path : remainingFiles)
                    System.err.println(path);

                if (loadingFrame != null)
                {
                    new FailedAnnounceFrame(
                            "Some file(s) could not be opened (format not supported). See the console output for more details.");
                }
            }

            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
            {
                for (Sequence seq : result)
                    seq.loadXMLData();
            }
        }
        catch (Throwable t)
        {
            // just show the error
            IcyExceptionHandler.showErrorMessage(t, true);

            if (loadingFrame != null)
                new FailedAnnounceFrame((t instanceof OutOfMemoryError) ? t.getMessage()
                        : "Failed to open file(s), see the console output for more details.");
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Concatenate the <i>src</i> sequence to the <i>dest</i> one.
     */
    static Sequence concatenateSequence(Sequence dest, Sequence src, boolean onT, boolean onZ)
    {
        if (dest == null)
            return src;
        if (src == null)
            return dest;

        final int dst;
        final int dsz;
        final int sst = src.getSizeT();
        final int ssz = src.getSizeZ();

        if (onT)
        {
            if (onZ)
            {
                dst = dest.getSizeT() - 1;
                dsz = dest.getSizeZ(dst);
            }
            else
            {
                dst = dest.getSizeT();
                dsz = 0;
            }
        }
        else
        {
            dst = 0;
            dsz = onZ ? dest.getSizeZ() : 0;
        }

        // put 'dest' in update state to avoid useless recalculations
        if (!dest.isUpdating())
            dest.beginUpdate();

        for (int t = 0; t < sst; t++)
            for (int z = 0; z < ssz; z++)
                dest.setImage(t + dst, z + dsz, src.getImage(t, z));

        return dest;
    }

    static void addSequences(List<Sequence> result, TreeMap<Integer, Sequence> map)
    {
        if (!map.isEmpty())
        {
            // get all sequence from the map orderer by channel
            final Collection<Sequence> sequencesC = map.values();

            // remove update state
            for (Sequence seq : sequencesC)
                if (seq.isUpdating())
                    seq.endUpdate();

            final Sequence sequences[] = sequencesC.toArray(new Sequence[sequencesC.size()]);

            // several sequences ?
            if (sequences.length > 1)
            {
                // concatenate sequences on C dimension
                final Sequence merged = SequenceUtil.concatC(sequences);
                // better to keep name from first image
                merged.setName(sequences[0].getName());
                // then add the result to the list
                result.add(merged);
            }
            else
                result.add(sequences[0]);

            // better to not merge the C channel after all
            // result.addAll(sequencesC);

            // clear the map
            map.clear();
        }
    }

    /**
     * <b>Internal use only !</b><br>
     * Load a single file and return result as a Sequence.<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     * 
     * @param importer
     *        Opened importer used to load the image file (cannot be <code>null</code> here)
     * @param metadata
     *        Metadata of the image
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *        and so on...
     * @param region
     *        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param loadingFrame
     *        the loading frame used to display progress of the operation (can be null).<br>
     *        Caller should allocate 100 positions for the internal single load process.
     * @return the Sequence object or <code>null</code>
     */
    public static Sequence internalLoadSingle(SequenceIdImporter importer, OMEXMLMetadata metadata, int series, int resolution,
            Rectangle region, int minZ, int maxZ, int minT, int maxT, int channel, FileFrame loadingFrame)
            throws IOException, UnsupportedFormatException, OutOfMemoryError
    {
        final int imgSizeX = MetaDataUtil.getSizeX(metadata, series);
        final int imgSizeY = MetaDataUtil.getSizeY(metadata, series);

        final Rectangle adjRegion;

        if (region != null)
            adjRegion = new Rectangle(0, 0, imgSizeX, imgSizeY).intersection(region);
        else
            adjRegion = null;

        final int sizeX = (adjRegion == null) ? imgSizeX : adjRegion.width;
        final int sizeY = (adjRegion == null) ? imgSizeY : adjRegion.height;
        final int sizeZ = MetaDataUtil.getSizeZ(metadata, series);
        final int sizeT = MetaDataUtil.getSizeT(metadata, series);
        final int sizeC = MetaDataUtil.getSizeC(metadata, series);

        final int adjMinZ, adjMaxZ;
        final int adjMinT, adjMaxT;

        if (minZ < 0)
            adjMinZ = 0;
        else
            adjMinZ = Math.min(minZ, sizeZ);
        if (maxZ < 0)
            adjMaxZ = sizeZ - 1;
        else
            adjMaxZ = Math.min(maxZ, sizeZ - 1);
        if (minT < 0)
            adjMinT = 0;
        else
            adjMinT = Math.min(minT, sizeT);
        if (maxT < 0)
            adjMaxT = sizeT - 1;
        else
            adjMaxT = Math.min(maxT, sizeT - 1);

        // check that we can open the image
        checkOpening(resolution, sizeX, sizeY, (channel == -1) ? sizeC : 1, (adjMaxZ - adjMinZ) + 1,
                (adjMaxT - adjMinT) + 1, MetaDataUtil.getDataType(metadata, series),
                " Try to open a sub resolution or sub part of the image only.");

        // create result sequence with desired series metadata
        final Sequence result = new Sequence(OMEUtil.createOMEXMLMetadata(metadata, series));

        // setup sequence properties and metadata from the opening setting
        setupSequence(result, importer, MetaDataUtil.getNumSeries(metadata) > 1, series, adjRegion, resolution, sizeZ,
                sizeT, sizeC, adjMinZ, adjMaxZ, adjMinT, adjMaxT, channel);

        // number of image to process
        final int numImage = ((adjMaxZ - adjMinZ) + 1) * ((adjMaxT - adjMinT) + 1);

        if (numImage > 0)
        {
            // set local length for loader frame
            final double progressStep = 100d / numImage;
            double progress = 0d;

            if (loadingFrame != null)
                progress = loadingFrame.getPosition();

            result.beginUpdate();
            try
            {
                for (int t = adjMinT; t <= adjMaxT; t++)
                {
                    for (int z = adjMinZ; z <= adjMaxZ; z++)
                    {
                        if (loadingFrame != null)
                        {
                            // cancel requested ? --> stop loading here...
                            if (loadingFrame.isCancelRequested())
                                return result;

                            // special group importer ? --> use internal file path
                            if (importer instanceof SequenceFileGroupImporter)
                                loadingFrame.setFilename(((SequenceFileGroupImporter) importer).getPath(z, t,
                                        (channel != -1) ? channel : 0));
                        }

                        // load image and add it to the sequence
                        if (channel == -1)
                            result.setImage(t - adjMinT, z - adjMinZ,
                                    importer.getImage(series, resolution, adjRegion, z, t));
                        else
                            result.setImage(t - adjMinT, z - adjMinZ,
                                    importer.getImage(series, resolution, adjRegion, z, t, channel));

                        progress += progressStep;

                        // notify progress to loader frame
                        if (loadingFrame != null)
                            loadingFrame.setPosition(progress);
                    }
                }
            }
            finally
            {
                result.endUpdate();
            }
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load a single file and return result as Sequence list (for multi series).<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     * 
     * @param importer
     *        Importer to use to open and load images (cannot be <code>null</code> here)
     * @param path
     *        image file to load
     * @param series
     *        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        series selector dialog.
     * @param loadingFrame
     *        the loading frame used to display progress of the operation (can be null)
     * @throws IOException
     */
    static List<Sequence> internalLoadSingle(SequenceFileImporter importer, String path, int series,
            FileFrame loadingFrame) throws IOException, UnsupportedFormatException, OutOfMemoryError
    {
        final double endStep;

        if (loadingFrame != null)
        {
            loadingFrame.setFilename(path);
            // 100 step reserved to load this image
            endStep = loadingFrame.getPosition() + 100d;
        }
        else
            endStep = 0d;

        final List<Sequence> result = new ArrayList<>();

        try
        {
            // prepare image loading for this file
            if (!importer.open(path, 0))
                throw new UnsupportedFormatException(
                        "Image file '" + path + "' is not supported by " + importer.toString() + " importer.");

            // get metadata
            final OMEXMLMetadata meta = importer.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // series selection
            int selectedSeries[];

            // give the opportunity to select the series(s) to open ?
            if (series == -1)
            {
                try
                {
                    // series selection (create a new importer instance as selectSerie(..) does async processes)
                    selectedSeries = selectSeries(importer.getClass().newInstance(), path, meta, 0, false);
                }
                catch (Throwable t)
                {
                    IcyExceptionHandler.showErrorMessage(t, true, true);
                    System.err.print("Opening first series by default...");
                    selectedSeries = new int[] {0};
                }

                // user cancelled action in the series selection ? null = cancel
                if (selectedSeries.length == 0)
                    return null;
            }
            else
                selectedSeries = new int[] {series};

            // add sequence to result
            for (int s : selectedSeries)
                result.add(internalLoadSingle(importer, meta, s, 0, null, -1, -1, -1, -1, -1, loadingFrame));

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // importer.close();
        }
        catch (UnsupportedFormatException e)
        {
            // close importer when error happen (not stored in Sequence so we need to close it manually)
            importer.close();

            // the importer is supposed to support this file --> re throw the exception
            if (importer.acceptFile(path))
                throw e;
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.setPosition(endStep);
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load the specified image file group and return a single Sequence from it.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     * 
     * @param group
     *        image path group we want to load
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *        and so on...
     * @param region
     *        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *        If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ
     *        the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param maxZ
     *        the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole stack.
     * @param minT
     *        the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param maxT
     *        the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *        Set to -1 to retrieve the whole timelaps.
     * @param channel
     *        C position of the image (channel) we want retrieve (-1 means all channel).
     * @param directory
     *        specify is the source is a single complete directory
     * @param mainMenu
     *        menu object used to store recent file (can be null)
     * @param loadingFrame
     *        the loading frame used to cancel / display progress of the operation (can be null)
     * @Return the loaded Sequence (or <code>null<code> if loading was canceled)
     */
    static Sequence internalLoadGroup(SequenceFileGroup group, int resolution, Rectangle region, int minZ, int maxZ,
            int minT, int maxT, int channel, boolean directory, ApplicationMenu mainMenu, FileFrame loadingFrame)
            throws UnsupportedFormatException, IOException
    {
        final double endStep;
        Sequence result;

        if (loadingFrame != null)
        {
            loadingFrame.setFilename(group.ident.base);
            loadingFrame.setAction("Loading image group");
            // 100 step reserved to load this image
            endStep = loadingFrame.getPosition() + 100d;
        }
        else
            endStep = 0d;

        // use the special group importer
        final SequenceFileGroupImporter groupImporter = new SequenceFileGroupImporter();

        try
        {
            // open image group (can't fail)
            groupImporter.open(group, 0);

            // get metadata
            final OMEXMLMetadata meta = groupImporter.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            result = internalLoadSingle(groupImporter, meta, 0, resolution, region, minZ, maxZ, minT, maxT, channel,
                    loadingFrame);

            // directory load ?
            if (directory)
            {
                // get directory without last separator
                final String fileDir = FileUtil.getDirectory(group.ident.base, false);

                // set sequence name and filename to directory
                result.setName(FileUtil.getFileName(fileDir, false));
                result.setFilename(fileDir);

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fileDir);
            }
            // normal file loading
            else
            {
                if (mainMenu != null)
                    mainMenu.addRecentFile(group.getPaths());
            }

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // groupImporter.close();
        }
        catch (UnsupportedFormatException e)
        {
            // close importer when error happen (not stored in Sequence so we need to close it manually)
            groupImporter.close();

            // re throw
            throw e;
        }
        catch (IOException e)
        {
            // close importer when error happen (not stored in Sequence so we need to close it manually)
            groupImporter.close();

            // re throw
            throw e;
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.setPosition(endStep);
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load the specified image file group and return a single Sequence from it.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     * 
     * @param group
     *        image path group we want to load
     * @param directory
     *        specify is the source is a single complete directory
     * @param mainMenu
     *        menu object used to store recent file (can be null)
     * @param loadingFrame
     *        the loading frame used to cancel / display progress of the operation (can be null)
     * @Return the loaded Sequence (or <code>null<code> if loading was canceled)
     */
    static Sequence internalLoadGroup(SequenceFileGroup group, boolean directory, ApplicationMenu mainMenu,
            FileFrame loadingFrame) throws UnsupportedFormatException, IOException
    {
        return internalLoadGroup(group, 0, null, -1, -1, -1, -1, -1, directory, mainMenu, loadingFrame);
    }

    /**
     * Setup the specified sequence object given the different opening informations
     * 
     * @param sequence
     *        sequence to adjust properties
     * @param importer
     *        image path
     * @param multiSerie
     *        <code>true</code> if this Sequence comes from a multi series dataset
     * @param series
     *        series index
     * @param region
     *        Rectangle region we want to load from original image (full resolution)
     * @param resolution
     *        Resolution level to open
     * @param sizeZ
     *        original image sizeZ
     * @param sizeT
     *        original image sizeT
     * @param sizeC
     *        original image sizeC
     * @param minZ
     *        minimum Z slice wanted
     * @param maxZ
     *        maximum Z slice wanted
     * @param minT
     *        minimum T frame wanted
     * @param maxT
     *        maximum T frame wanted
     * @param channel
     *        channel we want to load (-1 for all)
     */
    public static void setupSequence(Sequence sequence, SequenceIdImporter importer, boolean multiSerie, int series,
            Rectangle region, int resolution, int sizeZ, int sizeT, int sizeC, int minZ, int maxZ, int minT, int maxT,
            int channel)
    {
        final String path = FileUtil.getGenericPath(importer.getOpened());
        // default name
        String name = FileUtil.getFileName(path, false);

        // default name used --> use better name
        if (sequence.isDefaultName())
        {
            // multi series image --> add series info
            if (multiSerie)
                name += " - series " + StringUtil.toString(series);
        }
        else
        {
            // multi series image --> adjust name to keep file name info
            if (multiSerie)
                name += " - " + sequence.getName();
            else
                // just use sequence metadata name
                name = sequence.getName();
        }

        // original pixel size
        final double psx = sequence.getPixelSizeX();
        final double psy = sequence.getPixelSizeY();
        final double psz = sequence.getPixelSizeZ();
        // original position
        final double posX = sequence.getPositionX();
        final double posY = sequence.getPositionY();
        final double posZ = sequence.getPositionZ();

        // get sequence metadata
        final OMEXMLMetadata metadata = sequence.getOMEXMLMetadata();

        // cleanup planes
        for (int t = sizeT - 1; t >= 0; t--)
        {
            for (int z = sizeZ - 1; z >= 0; z--)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    if ((t < minT) || (t > maxT) || (z < minZ) || (z > maxZ))
                        MetaDataUtil.removePlane(metadata, 0, maxT, maxZ, c);
                }
            }
        }

        // single channel extraction ?
        if (channel != -1)
        {
            // adjust origin channel
            sequence.setOriginChannel(channel);

            // clean channels and remaining planes
            for (int c = 0; c < sizeC; c++)
            {
                if (c != channel)
                {
                    MetaDataUtil.removePlanes(metadata, 0, -1, -1, c);
                    MetaDataUtil.removeChannel(metadata, 0, c);
                }
            }
        }

        // adjust position X,Y,Z
        if (region != null)
        {
            // set origin region
            sequence.setOriginXYRegion(region);
            // adjust position
            sequence.setPositionX(posX + (region.x * psx));
            sequence.setPositionY(posY + (region.y * psy));
        }
        if (minZ > 0)
            sequence.setPositionZ(posZ + (minZ * psz));

        // using sub resolution ?
        if (resolution > 0)
        {
            final int divider = (int) Math.pow(2, resolution);

            // adjust origin resolution
            sequence.setOriginResolution(resolution);
            // adjust pixel size
            sequence.setPixelSizeX(psx * divider);
            sequence.setPixelSizeY(psy * divider);

            // adjust name
            name += " - binning=" + StringUtil.toString(divider);
        }

        // adjust Z Range
        if ((minZ > 0) || (maxZ < (sizeZ - 1)))
        {
            sequence.setOriginZMin(minZ);
            sequence.setOriginZMax(maxZ);

            // adjust name
            if (minZ == maxZ)
                name += " - Z" + StringUtil.toString(minZ);
            else
                name += " - Z[" + StringUtil.toString(minZ) + "-" + StringUtil.toString(maxZ) + "]";
        }
        // adjust T Range
        if ((minT > 0) || (maxT < (sizeT - 1)))
        {
            sequence.setOriginTMin(minT);
            sequence.setOriginTMax(maxT);

            // adjust name
            if (minT == maxT)
                name += " - T" + StringUtil.toString(minT);
            else
                name += " - T[" + StringUtil.toString(minT) + "-" + StringUtil.toString(maxT) + "]";
        }

        // need to adjust name for channel ?
        if (channel != -1)
            name += " - C" + StringUtil.toString(channel);

        // set final name and filename
        sequence.setName(name);
        sequence.setFilename(path);

        // set importer (for caching / delayed loading...)
        sequence.setImageProvider(importer);
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(final SequenceIdImporter importer, final String path, final OMEXMLMetadata meta,
            int defaultSerie, final boolean singleSelection) throws UnsupportedFormatException, IOException
    {
        final int serieCount = MetaDataUtil.getNumSeries(meta);
        final int[] tmp = new int[serieCount + 1];

        if (serieCount > 0)
        {
            tmp[0] = 1;

            // multi series, display selection dialog
            if (serieCount > 1)
            {
                // allow user to select series to open
                if (!Icy.getMainInterface().isHeadLess())
                {
                    final Exception[] exception = new Exception[1];
                    exception[0] = null;

                    // use invokeNow carefully !
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                final int[] series = new SeriesSelectionDialog(importer, path, meta, singleSelection)
                                        .getSelectedSeries();
                                // get result
                                tmp[0] = series.length;
                                System.arraycopy(series, 0, tmp, 1, series.length);
                            }
                            catch (Exception e)
                            {
                                exception[0] = e;
                            }
                        }
                    });

                    // propagate exception
                    if (exception[0] instanceof UnsupportedFormatException)
                        throw (UnsupportedFormatException) exception[0];
                    else if (exception[0] instanceof IOException)
                        throw (IOException) exception[0];
                }
                // use the pre selected series
                else
                    tmp[1] = defaultSerie;
            }
            // only 1 series so open it
            else
                tmp[1] = 0;
        }

        // copy back result to adjusted array
        final int[] result = new int[tmp[0]];

        System.arraycopy(tmp, 1, result, 0, result.length);

        return result;
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(final SequenceFileImporter importer, final String path, final OMEXMLMetadata meta,
            int defaultSerie, final boolean singleSelection) throws UnsupportedFormatException, IOException
    {
        return selectSeries((SequenceIdImporter) importer, path, meta, defaultSerie, singleSelection);
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(final SequenceFileImporter importer, final String path,
            final OMEXMLMetadataImpl meta, int defaultSerie, boolean singleSelection)
            throws UnsupportedFormatException, IOException
    {
        return selectSeries(importer, path, (OMEXMLMetadata) meta, defaultSerie, singleSelection);
    }

    /**
     * Display the Series Selection frame for the given image and return the selected series (single selection).<br>
     * Returns <code>-1</code> if user canceled series selection.
     */
    public static int selectSerie(final SequenceFileImporter importer, final String path, final OMEXMLMetadata meta,
            int defaultSerie) throws UnsupportedFormatException, IOException
    {
        final int selected[] = selectSeries(importer, path, meta, defaultSerie, true);

        if (selected.length > 0)
            return selected[0];

        return -1;
    }

    /**
     * @deprecated Use {@link #selectSerie(SequenceFileImporter, String, OMEXMLMetadata, int)} instead
     */
    @Deprecated
    public static int selectSerie(final SequenceFileImporter importer, final String path, final OMEXMLMetadataImpl meta,
            int defaultSerie) throws UnsupportedFormatException, IOException
    {
        return selectSerie(importer, path, (OMEXMLMetadata) meta, defaultSerie);
    }

    static List<String> explode(List<String> paths)
    {
        return FileUtil.toPaths(FileUtil.explode(FileUtil.toFiles(paths), null, true, false));
    }

    /**
     * Remove invalid image files from the list of files
     */
    public static List<String> cleanNonImageFile(List<String> paths)
    {
        final List<String> result = new ArrayList<>();

        // extensions based exclusion
        for (String path : paths)
        {
            // no image file or XML persistence --> ignore
            if (canDiscardImageFile(path))
                continue;

            // XML file ?
            if (FileUtil.getFileExtension(path, false).toLowerCase().equals(XMLUtil.FILE_EXTENSION))
            {
                // ignore persistence files
                if (SequencePersistent.isValidXMLPersitence(path))
                    continue;
            }

            result.add(path);
        }

        return result;
    }

    /**
     * @deprecated Use {@link SequenceFileSticher#groupAllFiles(SequenceFileImporter, Collection, boolean, FileFrame)} instead
     */
    @Deprecated
    public static List<FilePosition> getFilePositions(List<String> paths, boolean dimOrder, FileFrame loadingFrame)
    {
        final List<FilePosition> result = new ArrayList<>(paths.size());

        // use new path grouper method
        final Collection<SequenceFileGroup> groups = SequenceFileSticher.groupAllFiles(null, paths, dimOrder,
                loadingFrame);

        // just build FilePosition from contained SequencePosition
        for (SequenceFileGroup group : groups)
        {
            final SequenceIdent ident = group.ident;

            for (SequencePosition pos : group.positions)
                result.add(new FilePosition(pos.getPath(), ident.base, 0, pos.getIndexT(), pos.getIndexZ(),
                        pos.getIndexC()));
        }

        return result;
    }

    /**
     * @deprecated Use {@link SequenceFileSticher#groupAllFiles(SequenceFileImporter, Collection, boolean, FileFrame)} instead
     */
    @Deprecated
    public static List<FilePosition> getFilePositions(List<String> paths, boolean dimOrder)
    {
        return getFilePositions(paths, dimOrder, null);
    }
}
