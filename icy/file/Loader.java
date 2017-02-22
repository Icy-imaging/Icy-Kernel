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
import icy.sequence.DimensionId;
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
import icy.util.StringUtil.AlphanumComparator;
import icy.util.XMLUtil;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Sequence / Image loader class.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Loader
{
    private static class PositionChunk
    {
        /** Depth (Z) dimension prefixes (taken from Bio-Formats for almost) */
        static final String[] prefixesZ = {"fp", "sec", "z", "zs", "focal", "focalplane"};

        /** Time (T) dimension prefixes (taken from Bio-Formats for almost) */
        static final String[] prefixesT = {"t", "tl", "tp", "time"};

        /** Channel (C) dimension prefixes (taken from Bio-Formats for almost) */
        static final String[] prefixesC = {"c", "ch", "b", "band", "w", "wl", "wave", "wavelength"};

        /** Serie (S)dimension prefixes (taken from Bio-Formats for almost) */
        static final String[] prefixesS = {"s", "series", "sp", "f"};

        public DimensionId dim;
        public int value;

        PositionChunk(String prefix, int value)
        {
            super();

            dim = null;
            if (!StringUtil.isEmpty(prefix))
            {
                final String prefixLC = prefix.toLowerCase();

                dim = getDim(prefixLC, prefixesZ, DimensionId.Z);
                if (dim == null)
                    dim = getDim(prefixLC, prefixesT, DimensionId.T);
                if (dim == null)
                    dim = getDim(prefixLC, prefixesC, DimensionId.C);
                if (dim == null)
                    dim = getDim(prefixLC, prefixesS, DimensionId.NULL);
            }

            this.value = value;
        }

        private static DimensionId getDim(String prefix, String prefixes[], DimensionId d)
        {
            for (String suffix : prefixes)
                if (prefix.endsWith(suffix))
                    return d;

            return null;
        }
    }

    private static class Position
    {
        final List<PositionChunk> chunks;
        final String baseName;

        Position(String baseName)
        {
            super();

            this.baseName = baseName;
            chunks = new ArrayList<Loader.PositionChunk>();
        }

        void addChunk(String prefix, int value)
        {
            final PositionChunk chunk = new PositionChunk(prefix, value);
            // get the previous chunk for this dimension
            final PositionChunk previousChunk = getChunk(chunk.dim, false);

            // already have a chunk for this dimension --> remove it
            if (previousChunk != null)
                removeChunk(previousChunk);

            // add the chunk
            chunks.add(chunk);
        }

        int getValue(DimensionId dim)
        {
            final PositionChunk chunk = getChunk(dim, true);

            if (chunk != null)
                return chunk.value;

            // not found
            return -1;
        }

        public boolean isUnknowDim(DimensionId dim)
        {
            return getChunk(dim, false) == null;
        }

        boolean removeChunk(DimensionId dim)
        {
            return removeChunk(getChunk(dim, true));
        }

        private boolean removeChunk(PositionChunk chunk)
        {
            return chunks.remove(chunk);
        }

        PositionChunk getChunk(DimensionId dim, boolean allowUnknown)
        {
            if (dim != null)
            {
                for (PositionChunk chunk : chunks)
                    if (chunk.dim == dim)
                        return chunk;

                if (allowUnknown)
                    return getChunkFromUnknown(dim);
            }

            return null;
        }

        private PositionChunk getChunkFromUnknown(DimensionId dim)
        {
            final boolean hasCChunk = (getChunk(DimensionId.C, false) != null);
            final boolean hasZChunk = (getChunk(DimensionId.Z, false) != null);
            final boolean hasTChunk = (getChunk(DimensionId.T, false) != null);
            final int unknownCount = getUnknownChunkCount();

            // priority order : T, Z, C
            switch (dim)
            {
                case C:
                    if (hasCChunk)
                        return null;

                    if (hasTChunk)
                    {
                        if (hasZChunk)
                        {
                            // T and Z chunk present --> C = unknown[0]
                            if (unknownCount >= 1)
                                return getUnknownChunk(0);
                        }
                        else
                        {
                            // T chunk present --> Z = unknown[0]; C = unknown[1]
                            if (unknownCount >= 2)
                                return getUnknownChunk(1);
                        }
                    }
                    else if (hasZChunk)
                    {
                        // Z chunk present --> T = unknown[0]; C = unknown[1]
                        if (unknownCount >= 2)
                            return getUnknownChunk(1);
                    }
                    else
                    {
                        // no other chunk present --> T = unknown[0]; Z = unknown[1]; C = unknown[2]
                        if (unknownCount >= 3)
                            return getUnknownChunk(2);
                    }
                    break;

                case Z:
                    if (hasZChunk)
                        return null;

                    if (hasTChunk)
                    {
                        // T chunk present --> Z = unknown[0]
                        if (unknownCount >= 1)
                            return getUnknownChunk(0);
                    }
                    else
                    {
                        // T chunk not present --> T = unknown[0]; Z = unknown[1]
                        if (unknownCount >= 2)
                            return getUnknownChunk(1);
                    }
                    break;

                case T:
                    if (hasTChunk)
                        return null;

                    // T = unknown[0]
                    if (unknownCount >= 1)
                        return getUnknownChunk(0);
                    break;
            }

            return null;
        }

        private PositionChunk getUnknownChunk(int i)
        {
            int ind = 0;

            for (PositionChunk chunk : chunks)
            {
                if (chunk.dim == null)
                {
                    if (ind == i)
                        return chunk;

                    ind++;
                }
            }

            return null;
        }

        int getUnknownChunkCount()
        {
            int result = 0;

            for (PositionChunk chunk : chunks)
                if (chunk.dim == null)
                    result++;

            return result;
        }

        @Override
        public String toString()
        {
            return "Position [S:" + getValue(DimensionId.NULL) + " T:" + getValue(DimensionId.T) + " Z:"
                    + getValue(DimensionId.Z) + " C:" + getValue(DimensionId.C) + "]";
        }
    }

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

    // private final static Set<String> nonImageExtensions = new HashSet<String>(CollectionUtil.asList(new String[] {
    // "xml", "txt", "pdf", "xls", "doc", "docx", "pdf", "rtf", "exe", "wav", "mp3", "app"}));
    /**
     * XML, XLS and TXT file can be image metadata files used to open the whole image, accept it !
     */
    private final static Set<String> nonImageExtensions = new HashSet<String>(CollectionUtil.asList(new String[] {
            "pdf", "doc", "docx", "pdf", "rtf", "exe", "wav", "mp3", "app"}));

    /**
     * Returns all available resource importer.
     */
    public static List<Importer> getImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(Importer.class);
        final List<Importer> result = new ArrayList<Importer>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((Importer) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));
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
        final List<FileImporter> result = new ArrayList<FileImporter>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((FileImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));
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
        final Map<FileImporter, List<String>> result = new HashMap<FileImporter, List<String>>(importers.size());
        final Map<String, FileImporter> extensionImporters = new HashMap<String, FileImporter>(importers.size());

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
                    list = new ArrayList<String>();
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
        final List<FileImporter> result = new ArrayList<FileImporter>(importers.size());

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
        final List<FileImporter> result = new ArrayList<FileImporter>(importers.size());

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
     * Returns all available sequence importer (different from {@link SequenceIdImporter}).
     */
    public static List<SequenceImporter> getSequenceImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceImporter.class);
        final List<SequenceImporter> result = new ArrayList<SequenceImporter>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take id as input.
     */
    public static List<SequenceIdImporter> getSequenceIdImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceIdImporter.class);
        final List<SequenceIdImporter> result = new ArrayList<SequenceIdImporter>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceIdImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take file as input.
     */
    public static List<SequenceFileImporter> getSequenceFileImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceFileImporter.class);
        final List<SequenceFileImporter> result = new ArrayList<SequenceFileImporter>();

        for (PluginDescriptor plugin : plugins)
        {
            try
            {
                // add the importer
                result.add((SequenceFileImporter) PluginLauncher.create(plugin));
            }
            catch (Throwable t)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(t, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified files.<br>
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
    public static Map<SequenceFileImporter, List<String>> getSequenceFileImporters(
            List<SequenceFileImporter> importers, List<String> paths, boolean useFirstFound)
    {
        final Map<SequenceFileImporter, List<String>> result = new HashMap<SequenceFileImporter, List<String>>(
                importers.size());
        final Map<String, SequenceFileImporter> extensionImporters = new HashMap<String, SequenceFileImporter>(
                importers.size());

        for (String path : paths)
        {
            final String ext = FileUtil.getFileExtension(path, false);
            SequenceFileImporter imp;

            // try to get importer from extension first
            imp = extensionImporters.get(ext);

            // do not exist yet
            if (imp == null)
            {
                // find it
                imp = getSequenceFileImporter(importers, path, useFirstFound);
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
                    list = new ArrayList<String>();
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
     * Returns all sequence file importer which can open the specified file.
     */
    public static List<SequenceFileImporter> getSequenceFileImporters(List<SequenceFileImporter> importers, String path)
    {
        final List<SequenceFileImporter> result = new ArrayList<SequenceFileImporter>(importers.size());

        for (SequenceFileImporter importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all sequence file importer which can open the specified file.
     */
    public static List<SequenceFileImporter> getSequenceFileImporters(String path)
    {
        return getSequenceFileImporters(getSequenceFileImporters(), path);
    }

    /**
     * Returns the appropriate sequence file importer for the specified file.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
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
     * @see #getSequenceFileImporters(List, String)
     */
    public static SequenceFileImporter getSequenceFileImporter(List<SequenceFileImporter> importers, String path,
            boolean useFirstFound)
    {
        final List<SequenceFileImporter> result = new ArrayList<SequenceFileImporter>(importers.size());

        for (SequenceFileImporter importer : importers)
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
     * Returns the appropriate sequence file importer for the specified file.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
     * Returns <code>null</code> if no importer can open the file.
     * 
     * @param path
     *        the file we want to retrieve importer for.
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
     * Display a dialog to let the user select the appropriate sequence file importer for the
     * specified file.
     */
    public static SequenceFileImporter selectSequenceFileImporter(final List<SequenceFileImporter> importers,
            final String path)
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
                final ImporterSelectionDialog selectionDialog = new ImporterSelectionDialog(importers, path);

                if (!selectionDialog.isCanceled())
                    result[0] = selectionDialog.getSelectedImporter();
                else
                    result[0] = null;
            }
        });

        return (SequenceFileImporter) result[0];
    }

    /**
     * Returns <code>true</code> if the specified path describes a file type (from extension) which is well known to
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
        return !getSequenceFileImporters(path).isEmpty();
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
        final List<String> result = new ArrayList<String>();

        for (String path : paths)
        {
            if (importer.acceptFile(path))
                result.add(path);
        }

        return result;
    }

    /**
     * Check if we have enough resource to open the image defined by the given size information and wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     * 
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the original image while value
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
            throw new UnsupportedOperationException("Cannot open image with a XY plane size >= 2^31."
                    + ((messageSuffix != null) ? messageSuffix : ""));

        // get free memory
        long freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
        // check that we have enough memory for the whole image and for the ARGB image used for display (sizeXY * 4)
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
     * Check if we have enough resource to open the image defined by the given metadata information, serie index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     * 
     * @param meta
     *        metadata of the image
     * @param serie
     *        serie index
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the original image while value
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
    public static void checkOpening(OMEXMLMetadataImpl meta, int serie, int resolution, int sizeZ, int sizeT,
            String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError
    {
        checkOpening(resolution, MetaDataUtil.getSizeX(meta, serie), MetaDataUtil.getSizeY(meta, serie),
                MetaDataUtil.getSizeC(meta, serie), sizeZ, sizeT, MetaDataUtil.getDataType(meta, serie), messageSuffix);
    }

    /**
     * Check if we have enough resource to open the image defined by the given metadata information, serie index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     * 
     * @param meta
     *        metadata of the image
     * @param serie
     *        serie index
     * @param resolution
     *        wanted image resolution: a value of <code>0</code> means full resolution of the original image while value
     *        <code>1</code> correspond to the resolution / 2.<br>
     *        Formula: <code>resolution / 2^value</code><br>
     * @param messageSuffix
     *        message suffix for the exception if wanted
     * @throws UnsupportedOperationException
     *         if the XY plane size is >= 2^31 pixels
     * @throws OutOfMemoryError
     *         if there is not enough memory to open the image
     */
    public static void checkOpening(OMEXMLMetadataImpl meta, int serie, int resolution, String messageSuffix)
            throws UnsupportedOperationException, OutOfMemoryError
    {
        checkOpening(meta, serie, resolution, MetaDataUtil.getSizeZ(meta, serie), MetaDataUtil.getSizeT(meta, serie),
                messageSuffix);
    }

    // /**
    // * Returns the best resolution to use from the given metadata information and serie index.<br>
    // * If the image is too large to be displayed at full resolution (XY plane size > 2^31) or if we don't have enough
    // * memory to store the whole image (depending wanted constraint) then a sub resolution index is returned.<br>
    // * A return value of <code>0</code> means full resolution of the original image while value <code>1</code>
    // * correspond to the resolution / 2.<br>
    // * Formula: <code>resolution / 2^value</code><br>
    // *
    // * @param meta
    // * metadata of the image
    // * @param serie
    // * serie index
    // * @param showMessage
    // * show announce frame or message in the output log when one size constraint is meet and induce resolution
    // * decrease
    // */
    // public static int getBestResolution(OMEXMLMetadataImpl meta, int serie, boolean showMessage)
    // {
    // // easy trick to disable message display when needed
    // boolean warningDisplayed = !showMessage;
    // // default resolution to open (full resolution)
    // int resolution = 0;
    // // size of XY plane
    // long sizeXY = (long) MetaDataUtil.getSizeX(meta, serie) * (long) MetaDataUtil.getSizeY(meta, serie);
    //
    // // we can't handle that plane size
    // if (sizeXY > Integer.MAX_VALUE)
    // {
    // if (!warningDisplayed)
    // {
    // // notify we can't open that image at full resolution
    // if (!Icy.getMainInterface().isHeadLess())
    // new AnnounceFrame("XY plane size is >= 2^31, try to open sub resolution of the image...", 10);
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
    // // check that we have enough memory for the whole image and the ARGB image used for display (sizeXY * 4)
    // long sizeInByte = MetaDataUtil.getDataSize(meta, serie, resolution) + (sizeXY * 4);
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
    // .println("Not enough memory to open full resolution of the image, try to open sub resolution...");
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
    @Deprecated
    public static IFormatReader getReader(String path) throws FormatException, IOException
    {
        return new ImageReader().getReader(path);
    }

    /**
     * @deprecated Use {@link #getMetaData(File)} instead.
     */
    @Deprecated
    protected static OMEXMLMetadataImpl getMetaData(IFormatReader reader, String path) throws FormatException,
            IOException
    {
        // prepare meta data store structure
        reader.setMetadataStore(new OMEXMLMetadataImpl());
        // load file with LOCI library
        reader.setId(path);

        return (OMEXMLMetadataImpl) reader.getMetadataStore();
    }

    /**
     * Loads and returns metadata of the specified image file with given importer.<br>
     * It can returns <code>null</code> if the specified file is not a valid or supported) image
     * file.
     */
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
     * Loads and returns metadata of the specified image file.
     */
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
     * @deprecated Use {@link #getMetaData(String)} instead.
     */
    @Deprecated
    public static OMEXMLMetadataImpl getMetaData(File file) throws UnsupportedFormatException, IOException
    {
        return getMetaData(file.getAbsolutePath());
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
     * @param serie
     *        Serie index we want to retrieve thumbnail from (for multi serie image).<br>
     *        Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(SequenceFileImporter importer, String path, int serie)
            throws UnsupportedFormatException, IOException
    {
        if (importer.open(path, 0))
        {
            try
            {
                return importer.getThumbnail(serie);
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
     * @param serie
     *        Serie index we want to retrieve thumbnail from (for multi serie image).<br>
     *        Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(String path, int serie) throws UnsupportedFormatException, IOException
    {
        IcyBufferedImage result;
        UnsupportedFormatException lastError = null;

        for (SequenceFileImporter importer : getSequenceFileImporters(path))
        {
            try
            {
                result = loadThumbnail(importer, path, serie);

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
        // set file id
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
     * @param serie
     *        Serie index we want to retrieve image from (for multi serie image).<br>
     *        Set to 0 if unsure (default).
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public static IcyBufferedImage loadImage(SequenceFileImporter importer, String path, int serie, int z, int t)
            throws UnsupportedFormatException, IOException
    {
        if ((importer == null) || !importer.open(path, 0))
            throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");

        try
        {
            return importer.getImage(serie, z, t);
        }
        finally
        {
            importer.close();
        }
    }

    /**
     * Load and return the image at given position from the specified file path.<br>
     * For lower image level access, you can use {@link #getSequenceFileImporter(String, boolean)} method and
     * directly work through the returned {@link ImageProvider} interface.
     * 
     * @param path
     *        image file path.
     * @param serie
     *        Serie index we want to retrieve image from (for multi serie image).<br>
     *        Set to 0 if unsure (default).
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public static IcyBufferedImage loadImage(String path, int serie, int z, int t) throws UnsupportedFormatException,
            IOException
    {
        return loadImage(getSequenceFileImporter(path, true), path, serie, z, t);
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
        final List<Sequence> result = new ArrayList<Sequence>();
        final List<String> paths = FileUtil.toPaths(CollectionUtil.asList(files));

        if (series == null)
            result.addAll(loadSequences(paths, -1, separate, autoOrder, false, showProgress));
        else
        {
            for (int serie : series)
                result.addAll(loadSequences(paths, serie, separate, autoOrder, false, showProgress));
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

        return Arrays.asList(loadSequences(files.toArray(new File[files.size()]), seriesArray, separate, autoOrder,
                showProgress));
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
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files, boolean showProgress)
    {
        return loadSequence(files.toArray(new File[files.size()]), -1, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files)
    {
        return loadSequence(files.toArray(new File[files.size()]), -1, true);
    }

    /**
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead or {@link #load(File, boolean)} if you want
     *             to display the resulting sequence.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files, boolean display, boolean addToRecent)
    {
        return loadSequence(files.toArray(new File[files.size()]), -1, true);
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
     * @deprecated Use {@link #loadSequences(List, int, boolean, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static Sequence[] loadSequences(File[] files, int serie, boolean separate, boolean autoOrder,
            boolean showProgress)
    {
        final List<Sequence> result = loadSequences(FileUtil.toPaths(CollectionUtil.asList(files)), serie, separate,
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
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    @Deprecated
    public static Sequence loadSequence(File[] files, int serie, boolean showProgress)
    {
        final Sequence[] result = loadSequences(files, serie, false, true, showProgress);

        if (result.length > 0)
            return result[0];

        return null;
    }

    /**
     * @deprecated Use {@link #loadSequence(String, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file, int serie, boolean showProgress)
    {
        return loadSequence(new File[] {file}, serie, showProgress);
    }

    /**
     * Load a list of sequence from the specified list of file with the given {@link SequenceFileImporter} and returns
     * them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could be opened (not
     * supported).<br>
     * If the user cancelled the action (serie selection dialog) then it returns <code>null</code>.
     * 
     * @param importer
     *        Importer used to open and load image files.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if
     *        several importers match the user will have to select the appropriate one from a
     *        selection dialog.
     * @param paths
     *        List of image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user<br>
     *        to select the serie to open from a serie selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(SequenceFileImporter importer, List<String> paths, int serie,
            boolean separate, boolean autoOrder, boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<Sequence>();

        // detect if this is a complete folder load
        final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
        // explode path list
        final List<String> singlePaths = cleanNonImageFile(explode(paths));

        // get the sequence importer first
        final Map<SequenceFileImporter, List<String>> sequenceFileImporters;

        // importer not defined --> find the appropriate importers
        if (importer == null)
            sequenceFileImporters = getSequenceFileImporters(singlePaths, false);
        else
        {
            sequenceFileImporters = new HashMap<SequenceFileImporter, List<String>>(1);
            sequenceFileImporters.put(importer, new ArrayList<String>(singlePaths));
        }

        for (Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet())
        {
            final SequenceFileImporter imp = entry.getKey();
            final List<String> currPaths = entry.getValue();
            final boolean dir = directory && (sequenceFileImporters.size() == 1)
                    && (currPaths.size() == singlePaths.size());

            // load sequence
            result.addAll(loadSequences(imp, currPaths, serie, separate, autoOrder, dir, addToRecent, showProgress));

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
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user<br>
     *        to select the serie to open from a serie selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(List<String> paths, int serie, boolean separate, boolean autoOrder,
            boolean addToRecent, boolean showProgress)
    {
        return loadSequences(null, paths, serie, separate, autoOrder, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param importer
     *        Importer used to load the image file (shouldn't be <code>null</code>).
     * @param paths
     *        List of image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        serie selector dialog.
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(SequenceFileImporter importer, List<String> paths, int serie,
            boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = loadSequences(importer, paths, serie, false, true, false, addToRecent,
                showProgress);

        if (result.size() > 0)
            return result.get(0);

        return null;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param importer
     *        Importer used to load the image file (shouldn't be null).
     * @param paths
     *        List of image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(SequenceFileImporter importer, List<String> paths, int serie,
            boolean showProgress)
    {
        return loadSequence(importer, paths, serie, false, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     * 
     * @param paths
     *        List of image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(List<String> paths, int serie, boolean showProgress)
    {
        if (paths.isEmpty())
            return null;

        return loadSequence(getSequenceFileImporter(paths.get(0), false), paths, serie, showProgress);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code> if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        Image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param addToRecent
     *        If set to true the path will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int serie, boolean addToRecent,
            boolean showProgress)
    {
        return loadSequence(importer, path, serie, 0, null, -1, -1, -1, -1, -1, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        Image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int serie, boolean showProgress)
    {
        return loadSequence(importer, path, serie, false, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * If several importers match to open the file the user will have to select the appropriate one
     * from a selection dialog if <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     * compatible importer is automatically used.
     * 
     * @param path
     *        Image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(String path, int serie, boolean showProgress)
    {
        return loadSequence(null, path, serie, showProgress);
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
                        fileImporters = new HashMap<FileImporter, List<String>>(1);
                        fileImporters.put(importer, new ArrayList<String>(singlePaths));
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
     *        If set to <code>null</code> the loader will search for a compatible importer and if several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        image file to load
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image
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
    public static void load(final SequenceFileImporter importer, final String path, final int serie,
            final int resolution, final Rectangle region, final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean addToRecent, final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // load sequence
                final Sequence sequence = loadSequence(importer, path, serie, resolution, region, minZ, maxZ, minT,
                        maxT, channel, addToRecent, showProgress);

                // and display it
                if (sequence != null)
                    Icy.getMainInterface().addSequence(sequence);
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
     * Load the specified files (asynchronous process) by using automatically the appropriate {@link FileImporter} or
     * {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified files are image files:<br>
     * When <i>separate</i> is <code>false</code> the loader try to set image in the same sequence.<br>
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
            @Override
            public void run()
            {
                // detect if this is a complete folder load
                final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
                // explode path list
                final List<String> singlePaths = cleanNonImageFile(explode(paths));

                // get the sequence importer first
                final Map<SequenceFileImporter, List<String>> sequenceFileImporters = getSequenceFileImporters(
                        singlePaths, false);

                for (Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet())
                {
                    final SequenceFileImporter importer = entry.getKey();
                    final List<String> currPaths = entry.getValue();
                    final boolean dir = directory && (sequenceFileImporters.size() == 1)
                            && (currPaths.size() == singlePaths.size());

                    // load sequence
                    final List<Sequence> sequences = loadSequences(importer, currPaths, -1, separate, autoOrder, dir,
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
     * Load the specified file (asynchronous process) by using automatically the appropriate {@link FileImporter} or
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
     * @deprecated Use {@link #loadSequences(List, int, boolean, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    static Sequence[] loadSequences(SequenceFileImporter importer, File[] files, int serie, boolean separate,
            boolean autoOrder, boolean directory, boolean addToRecent, boolean showProgress)
    {
        final List<String> paths = CollectionUtil.asList(FileUtil.toPaths(files));
        final List<Sequence> result = loadSequences(importer, paths, serie, separate, autoOrder, directory,
                addToRecent, showProgress);
        return result.toArray(new Sequence[result.size()]);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code> if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     * 
     * @param importer
     *        Importer used to load the image file.<br>
     *        If set to <code>null</code> the loader will search for a compatible importer and if several importers
     *        match the user will have to select the appropriate one from a selection dialog if
     *        <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *        compatible importer will be automatically used.
     * @param path
     *        image file to load
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image
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
    public static Sequence loadSequence(SequenceFileImporter importer, String path, int serie, int resolution,
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
            final OMEXMLMetadataImpl meta = imp.getMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // serie selection
            int selectedSerie;

            // give the opportunity to select the serie(s) to open ?
            if (serie == -1)
            {
                try
                {
                    // serie selection (create a new importer instance as selectSerie(..) does async processes)
                    selectedSerie = selectSerie(imp.getClass().newInstance(), path, meta, 0);
                }
                catch (Throwable t)
                {
                    IcyExceptionHandler.showErrorMessage(t, true, true);
                    System.err.print("Opening first serie by default...");
                    selectedSerie = 0;
                }

                // user cancelled action in the serie selection ? null = cancel
                if (selectedSerie == -1)
                    return null;
            }
            else
                selectedSerie = serie;

            // load the image
            result = internalLoadSingle(imp, meta, selectedSerie, resolution, region, minZ, maxZ, minT, maxT, channel,
                    loadingFrame);

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
            try
            {
                imp.close();
            }
            catch (Exception e)
            {
                // ignore
            }

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
     *        Importer used to open and load images (cannot be <code>null</code> here).
     * @param paths
     *        list of image file to load
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        serie selector dialog.
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
    static List<Sequence> loadSequences(SequenceFileImporter importer, List<String> paths, int serie, boolean separate,
            boolean autoOrder, boolean directory, boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<Sequence>();

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
            final List<String> remainingFiles = new ArrayList<String>(paths);

            // load each file in a separate sequence
            if (separate)
            {
                if (loadingFrame != null)
                {
                    // each file can contains several image so we use 100 "inter step"
                    loadingFrame.setLength(paths.size() * 100d);
                    loadingFrame.setPosition(0d);
                }

                // load each file in a separate sequence
                for (String path : paths)
                {
                    // load the file
                    final List<Sequence> sequences = internalLoadSingle(importer, path, serie, loadingFrame);

                    // special case where loading was interrupted
                    if (sequences == null)
                        break;

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
                        break;
                }
            }
            else
            {
                final TreeMap<Integer, Sequence> map = new TreeMap<Integer, Sequence>();

                final List<FilePosition> filePositions = getFilePositions(paths, autoOrder, loadingFrame);
                int lastS = 0;

                if (loadingFrame != null)
                {
                    loadingFrame.setAction("Loading");
                    // each file can contains several image so we use 100 "inter step"
                    loadingFrame.setLength(filePositions.size() * 100d);
                    loadingFrame.setPosition(0d);
                }

                for (FilePosition filePos : filePositions)
                {
                    final String path = filePos.path;
                    // load the file
                    final List<Sequence> sequences = internalLoadSingle(importer, path, serie, loadingFrame);

                    // special case where loading was interrupted
                    if (sequences == null)
                        break;

                    final int s = filePos.getS();
                    final int z = filePos.getZ();
                    final int t = filePos.getT();
                    final int c = filePos.getC();
                    boolean concat;

                    // special case of single result --> try to concatenate to last sequence
                    if ((sequences.size() == 1) && !map.isEmpty())
                    {
                        final Sequence seq = sequences.get(0);
                        final int sizeZ = seq.getSizeZ();
                        final int sizeT = seq.getSizeT();
                        final int sizeC = seq.getSizeC();

                        concat = true;
                        // concatenation restriction
                        if (lastS != s)
                            concat = false;
                        if ((sizeZ > 1) && (z > 0))
                            concat = false;
                        if ((sizeT > 1) && (t > 0))
                            concat = false;
                        if ((sizeC > 1) && (c > 0))
                            concat = false;

                        if (concat)
                        {
                            // find last sequence for this channel
                            final Sequence lastSequence = map.get(Integer.valueOf(c));

                            // determine if concatenation is possible
                            if ((lastSequence != null) && !lastSequence.isCompatible(seq.getFirstImage()))
                                concat = false;
                        }

                        // update serie index
                        lastS = s;
                    }
                    else
                        concat = false;

                    // sequence correctly loaded ?
                    if (sequences.size() > 0)
                    {
                        if (concat)
                        {
                            final Sequence seq = sequences.get(0);
                            // find last sequence for this channel
                            Sequence lastSequence = map.get(Integer.valueOf(c));

                            // concatenate
                            lastSequence = concatenateSequence(lastSequence, seq, t > 0, z > 0);
                            // store the merged sequence for this channel
                            map.put(Integer.valueOf(c), lastSequence);
                        }
                        else
                        {
                            // concatenate sequences in map and add it to result list
                            addSequences(result, map);
                            // if on first channel then put the last sequence result in the map
                            if (c == 0)
                                map.put(Integer.valueOf(0), sequences.remove(sequences.size() - 1));
                            // and add the rest to the list
                            if (sequences.size() > 0)
                                result.addAll(sequences);
                        }

                        // remove path from remaining
                        remainingFiles.remove(path);
                    }

                    // interrupt loading
                    if ((loadingFrame != null) && loadingFrame.isCancelRequested())
                        break;
                }

                // concatenate last sequences in map and add it to result list
                addSequences(result, map);

                // add as one item to recent file list
                if (mainMenu != null)
                {
                    // set only the directory entry
                    if (directory)
                        mainMenu.addRecentFile(FileUtil.getDirectory(paths.get(0), false));
                    else
                        mainMenu.addRecentFile(paths);
                }
            }

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

            // directory load fit in a single sequence ?
            if ((result.size() == 1) && directory)
            {
                final Sequence seq = result.get(0);
                // get directory without last separator
                final String fileDir = FileUtil.getDirectory(paths.get(0), false);

                // set sequence name and filename to directory
                seq.setName(FileUtil.getFileName(fileDir, false));
                seq.setFilename(fileDir);
            }

            // TODO: restore colormap --> try to recover colormap

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
     * Internal load a single file and return result as a Sequence.<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     * 
     * @param importer
     *        Importer used to open the image file (cannot be <code>null</code> here)
     * @param metadata
     *        Metadata of the image
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param resolution
     *        Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *        The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *        So for instance level 0 is the default/full image resolution while level 1 is base image
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
     * @param loadingFrame
     *        the loading frame used to display progress of the operation (can be null).<br>
     *        Caller should allocate 100 positions for the internal single load process.
     * @return the Sequence object or <code>null</code>
     */
    static Sequence internalLoadSingle(SequenceFileImporter importer, OMEXMLMetadataImpl metadata, int serie,
            int resolution, Rectangle region, int minZ, int maxZ, int minT, int maxT, int channel,
            FileFrame loadingFrame) throws IOException, UnsupportedFormatException, OutOfMemoryError
    {
        final int sizeX = (region == null) ? MetaDataUtil.getSizeX(metadata, serie) : region.width;
        final int sizeY = (region == null) ? MetaDataUtil.getSizeY(metadata, serie) : region.height;
        final int sizeZ = MetaDataUtil.getSizeZ(metadata, serie);
        final int sizeT = MetaDataUtil.getSizeT(metadata, serie);
        final int sizeC = MetaDataUtil.getSizeC(metadata, serie);

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
        checkOpening(resolution, sizeX, sizeY, sizeC, (adjMaxZ - adjMinZ) + 1, (adjMaxT - adjMinT) + 1,
                MetaDataUtil.getDataType(metadata, serie),
                " Try to open a sub resolution or sub part of the image only.");

        // create result sequence with desired serie metadata
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(metadata, serie));

        // setup sequence properties and metadata from the opening setting
        setupSequence(result, FileUtil.getGenericPath(importer.getOpened()), MetaDataUtil.getNumSerie(metadata) > 1,
                serie, region, resolution, sizeZ, sizeT, sizeC, adjMinZ, adjMaxZ, adjMinT, adjMaxT, channel);

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
                        // cancel requested ? --> stop loading here...
                        if ((loadingFrame != null) && loadingFrame.isCancelRequested())
                            return result;

                        // load image and add it to the sequence
                        if (channel == -1)
                            result.setImage(t - adjMinT, z - adjMinZ,
                                    importer.getImage(serie, resolution, region, z, t));
                        else
                            result.setImage(t - adjMinT, z - adjMinZ,
                                    importer.getImage(serie, resolution, region, z, t, channel));

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
     * Internal load a single file and return result as Sequence list (for multi serie).<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     * 
     * @param importer
     *        Importer to use to open and load images (cannot be <code>null</code> here)
     * @param path
     *        image file to load
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        serie selector dialog.
     * @param loadingFrame
     *        the loading frame used to display progress of the operation (can be null)
     * @throws IOException
     */
    static List<Sequence> internalLoadSingle(SequenceFileImporter importer, String path, int serie,
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

        final List<Sequence> result = new ArrayList<Sequence>();

        try
        {
            // prepare image loading for this file
            if (!importer.open(path, 0))
                throw new UnsupportedFormatException("Image file '" + path + "' is not supported by "
                        + importer.toString() + " importer.");

            // get metadata
            final OMEXMLMetadataImpl meta = importer.getMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // serie selection
            int selectedSeries[];

            // give the opportunity to select the serie(s) to open ?
            if (serie == -1)
            {
                try
                {
                    // serie selection (create a new importer instance as selectSerie(..) does async processes)
                    selectedSeries = selectSeries(importer.getClass().newInstance(), path, meta, 0, false);
                }
                catch (Throwable t)
                {
                    IcyExceptionHandler.showErrorMessage(t, true, true);
                    System.err.print("Opening first serie by default...");
                    selectedSeries = new int[] {0};
                }

                // user cancelled action in the serie selection ? null = cancel
                if (selectedSeries.length == 0)
                    return null;
            }
            else
                selectedSeries = new int[] {serie};

            // add sequence to result
            for (int s : selectedSeries)
                result.add(internalLoadSingle(importer, meta, s, 0, null, -1, -1, -1, -1, -1, loadingFrame));
        }
        catch (UnsupportedFormatException e)
        {
            // the importer should support this file --> re throw the exception
            if (importer.acceptFile(path))
                throw e;
        }
        finally
        {
            // close importer
            importer.close();

            if (loadingFrame != null)
                loadingFrame.setPosition(endStep);
        }

        return result;
    }

    /**
     * Setup the specified sequence object given the different opening informations
     * 
     * @param sequence
     *        sequence to adjust properties
     * @param path
     *        image path
     * @param multiSerie
     *        <code>true</code> if this Sequence comes from a multi serie dataset
     * @param serie
     *        serie index
     * @param region
     *        Rectangle region we want to load from original image
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
    public static void setupSequence(Sequence sequence, String path, boolean multiSerie, int serie, Rectangle region,
            int resolution, int sizeZ, int sizeT, int sizeC, int minZ, int maxZ, int minT, int maxT, int channel)
    {
        // default name
        String name = FileUtil.getFileName(path, false);

        // default name used --> use better name
        if (sequence.isDefaultName())
        {
            // multi series image --> add serie info
            if (multiSerie)
                name += " - serie " + StringUtil.toString(serie);
        }
        else
        {
            // multi series image --> adjust name to keep file name info
            if (multiSerie)
                name += " - " + sequence.getName();
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
        final OMEXMLMetadataImpl metadata = sequence.getMetadata();

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
            name += " - resolution=1/" + StringUtil.toString(divider);
        }

        // adjust Z Range
        if ((minZ > 0) || (maxZ < (sizeZ - 1)))
        {
            sequence.setOriginZRangeMin(minZ);
            sequence.setOriginZRangeMax(maxZ);
        }
        // adjust T Range
        if ((minT > 0) || (maxT < (sizeT - 1)))
        {
            sequence.setOriginTRangeMin(minT);
            sequence.setOriginTRangeMax(maxT);
        }

        // set final name and filename
        sequence.setName(name);
        sequence.setFilename(path);
    }

    /**
     * Display the Serie Selection frame for the given image and returns selected serie(s).<br>
     * Returns a 0 length array if user canceled serie selection.
     */
    public static int[] selectSeries(final SequenceFileImporter importer, final String path,
            final OMEXMLMetadataImpl meta, int defaultSerie, boolean singleSelection)
            throws UnsupportedFormatException, IOException
    {
        final int serieCount = MetaDataUtil.getNumSerie(meta);
        final int[] tmp = new int[serieCount + 1];

        if (serieCount > 0)
        {
            tmp[0] = 1;

            // multi serie, display selection dialog
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
                                final int[] series = new SeriesSelectionDialog(importer, path, meta)
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
                // use the pre selected serie
                else
                    tmp[1] = defaultSerie;
            }
            // only 1 serie so open it
            else
                tmp[1] = 0;
        }

        // copy back result to adjusted array
        final int[] result = new int[tmp[0]];

        System.arraycopy(tmp, 1, result, 0, result.length);

        return result;
    }

    /**
     * Display the Serie Selection frame for the given image and return the selected serie (single selection).<br>
     * Returns <code>-1</code> if user canceled serie selection.
     */
    public static int selectSerie(final SequenceFileImporter importer, final String path,
            final OMEXMLMetadataImpl meta, int defaultSerie) throws UnsupportedFormatException, IOException
    {
        final int selected[] = selectSeries(importer, path, meta, defaultSerie, true);

        if (selected.length > 0)
            return selected[0];

        return -1;
    }

    static List<String> explode(List<String> paths)
    {
        return FileUtil.toPaths(FileUtil.explode(FileUtil.toFiles(paths), null, true, false));
    }

    static List<String> cleanNonImageFile(List<String> paths)
    {
        final List<String> result = new ArrayList<String>();

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
     * Sort the specified image files from their name and return their corresponding Sequence
     * position information.<br>
     * 
     * @param paths
     *        image files we want to sort
     * @param dimOrder
     *        if true we try to determine the Z, T and C image position as well else
     *        only simple T ordering is done.
     * @param loadingFrame
     *        Loading dialog if any to show progress
     */
    public static List<FilePosition> getFilePositions(List<String> paths, boolean dimOrder, FileFrame loadingFrame)
    {
        if (loadingFrame != null)
            loadingFrame.setAction("Extracting position from filename...");

        final List<String> filenames = new ArrayList<String>(paths);
        final List<Position> positions = new ArrayList<Position>(paths.size());
        final List<FilePosition> result = new ArrayList<FilePosition>(paths.size());

        // smart sort on name
        if (paths.size() > 1)
            Collections.sort(filenames, new AlphanumComparator());

        // need to use advanced sort ?
        if (dimOrder && (paths.size() > 1))
        {
            // build position for each file
            for (String filename : filenames)
                positions.add(getPosition(filename));

            final Set<String> baseNames = new HashSet<String>();

            for (Position position : positions)
                baseNames.add(position.baseName);

            for (String baseName : baseNames)
            {
                // remove fixed dimension
                while (cleanPositions(baseName, positions, DimensionId.NULL))
                    ;
                while (cleanPositions(baseName, positions, DimensionId.T))
                    ;
                while (cleanPositions(baseName, positions, DimensionId.Z))
                    ;
                while (cleanPositions(baseName, positions, DimensionId.C))
                    ;
            }

            boolean tSet = false;
            boolean tCanChange = true;
            boolean zSet = false;
            boolean zCanChange = true;

            for (Position position : positions)
            {
                if (position.getValue(DimensionId.T) != -1)
                    tSet = true;
                if (position.getValue(DimensionId.Z) != -1)
                    zSet = true;

                if (!position.isUnknowDim(DimensionId.T))
                    tCanChange = false;
                if (!position.isUnknowDim(DimensionId.Z))
                    zCanChange = false;
            }

            // Z and T position are not fixed, try to open 1 image to get its size information
            if (tCanChange && zCanChange)
            {
                try
                {
                    if (loadingFrame != null)
                    {
                        loadingFrame.setFilename(filenames.get(0));
                        loadingFrame.setAction("Reading metadata");
                    }

                    final OMEXMLMetadataImpl metadata = getMetaData(filenames.get(0));

                    if (loadingFrame != null)
                    {
                        loadingFrame.setFilename(null);
                        loadingFrame.setAction("Extracting position from filename...");
                    }

                    final boolean tMulti = MetaDataUtil.getSizeT(metadata, 0) > 1;
                    final boolean zMulti = MetaDataUtil.getSizeZ(metadata, 0) > 1;
                    boolean swapZT = false;

                    if (tMulti ^ zMulti)
                    {
                        // multi T but single Z
                        if (tMulti)
                        {
                            // T position set but can be swapped with Z
                            if (tSet && tCanChange && !zSet)
                                swapZT = true;
                        }
                        else
                        // multi Z but single T
                        {
                            // Z position set but can be swapped with T
                            if (zSet && zCanChange && !tSet)
                                swapZT = true;
                        }
                    }

                    // swat T and Z dimension
                    if (swapZT)
                    {
                        for (Position position : positions)
                        {
                            final PositionChunk zChunk = position.getChunk(DimensionId.Z, true);
                            final PositionChunk tChunk = position.getChunk(DimensionId.T, true);

                            // swap dim
                            if (zChunk != null)
                                zChunk.dim = DimensionId.T;
                            if (tChunk != null)
                                tChunk.dim = DimensionId.Z;
                        }
                    }
                }
                catch (Throwable t)
                {
                    // ignore...
                }
            }

            // create FilePosition result array
            for (int i = 0; i < positions.size(); i++)
            {
                final Position pos = positions.get(i);
                result.add(new FilePosition(filenames.get(i), pos.baseName, pos.getValue(DimensionId.NULL), pos
                        .getValue(DimensionId.T), pos.getValue(DimensionId.Z), pos.getValue(DimensionId.C)));
            }

            // sort it on basePath, S, T, Z, C position
            Collections.sort(result);
        }
        else
        {
            // create FilePosition result array
            int i = 0;
            for (String filename : filenames)
                result.add(new FilePosition(filename, getBaseName(filename), 0, i++, 0, 0));
        }

        // compact indexes
        if (result.size() > 0)
        {
            FilePosition pos, lastPos;
            int s, t, z, c;

            pos = result.get(0);
            // keep trace of last position
            lastPos = new FilePosition(pos);

            s = 0;
            t = 0;
            z = 0;
            c = 0;
            // set start position
            pos.set(s, t, z, c);

            for (int i = 1; i < result.size(); i++)
            {
                pos = result.get(i);

                // base path changed
                if (!StringUtil.equals(pos.basePath, lastPos.basePath))
                {
                    s++;
                    t = 0;
                    z = 0;
                    c = 0;
                }
                // S position changed
                else if (pos.getS() != lastPos.getS())
                {
                    s++;
                    t = 0;
                    z = 0;
                    c = 0;
                }
                // T position changed
                else if (pos.getT() != lastPos.getT())
                {
                    t++;
                    z = 0;
                    c = 0;
                }
                // Z position changed
                else if (pos.getZ() != lastPos.getZ())
                {
                    z++;
                    c = 0;
                }
                // C position changed
                else if (pos.getC() != lastPos.getC())
                    c++;
                // else assume T changed
                else
                    t++;

                // keep trace of last position
                lastPos = new FilePosition(pos);

                // update current position
                pos.set(s, t, z, c);
            }
        }

        return result;
    }

    /**
     * Sort the specified image files from their name and return their corresponding Sequence
     * position information.<br>
     * 
     * @param paths
     *        image files we want to sort
     * @param dimOrder
     *        if true we try to determine the Z, T and C image position as well else
     *        only simple T ordering is done.
     */
    public static List<FilePosition> getFilePositions(List<String> paths, boolean dimOrder)
    {
        return getFilePositions(paths, dimOrder, null);
    }

    private static String getBaseName(String text)
    {
        String result = new String(text);
        int pos = 0;

        while (pos < result.length())
        {
            final int st = StringUtil.getNextDigitCharIndex(result, pos);

            if (st != -1)
            {
                // get ending digit char index
                int end = StringUtil.getNextNonDigitCharIndex(result, st);
                if (end < 0)
                    end = result.length();
//                final int size = end - st;

                // remove number from name if number size < 6
//                if (size < 6)
                    result = result.substring(0, st) + result.substring(end);
                // pass to next
//                else
//                    pos = end;
            }
            else
                // done
                break;
        }

        return result;
    }

    private static boolean cleanPositions(String baseName, List<Position> positions, DimensionId dim)
    {
        // remove fixed dim
        int value = -1;
        for (Position position : positions)
        {
            if (StringUtil.equals(position.baseName, baseName))
            {
                final int v = position.getValue(dim);

                if (v != -1)
                {
                    if (value == -1)
                        value = v;
                    else if (value != v)
                    {
                        // variable --> stop
                        value = -1;
                        break;
                    }
                }
            }
        }

        // fixed dimension ? --> remove it
        if (value != -1)
        {
            for (Position position : positions)
            {
                if (StringUtil.equals(position.baseName, baseName))
                {
                    if (position.getValue(dim) != -1)
                        position.removeChunk(dim);
                }
            }

            return true;
        }

        return false;
    }

    private static Position getPosition(String filename)
    {
        // get filename without extension
        final String name = FileUtil.getFileName(filename, false);
        final String baseName = getBaseName(name);
        final Position result = new Position(baseName);
        final int len = name.length();

        // int value;
        int index = 0;
        while (index < len)
        {
            // get starting digit char index
            final int startInd = StringUtil.getNextDigitCharIndex(name, index);

            // we find a digit char ?
            if (startInd >= 0)
            {
                // get ending digit char index
                int endInd = StringUtil.getNextNonDigitCharIndex(name, startInd);
                if (endInd < 0)
                    endInd = len;

                // add number only if < 100000 (else it can be a date or id...)
//                if ((endInd - startInd) < 6)
//                {
                    // get prefix
                    final String prefix = getPositionPrefix(name, startInd - 1);
                    // get value
                    final int value = StringUtil.parseInt(name.substring(startInd, endInd), -1);

                    // add the position info
                    result.addChunk(prefix, value);
//                }

                // adjust index
                index = endInd;
            }
            else
                index = len;
        }

        return result;
    }

    private static String getPositionPrefix(String text, int ind)
    {
        if ((ind >= 0) && (ind < text.length()))
        {
            // we have a letter at this position
            if (Character.isLetter(text.charAt(ind)))
                // get complete prefix
                return text.substring(StringUtil.getPreviousNonLetterCharIndex(text, ind) + 1, ind + 1);
        }

        return "";
    }

}
