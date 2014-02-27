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
package icy.file;

import icy.common.exception.UnsupportedFormatException;
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
import icy.plugin.abstract_.Plugin;
import icy.preferences.GeneralPreferences;
import icy.sequence.DimensionId;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import icy.util.StringUtil.AlphanumComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
        static final String[] prefixesS = {"s", "series", "sp"};

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

        boolean removeChunk(DimensionId dim)
        {
            return removeChunk(getChunk(dim, true));
        }

        private boolean removeChunk(PositionChunk chunk)
        {
            return chunks.remove(chunk);
        }

        private PositionChunk getChunk(DimensionId dim, boolean allowUnknown)
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
         * @deprecated Use {@link FilePosition#FilePosition(String, String, int, int, int, int)}
         *             instead.
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
            return "File=" + path + " Position=[T:" + t + " Z:" + z + " C:" + c + "]";
        }
    }

    /**
     * Returns all available sequence file importer.
     */
    public static List<SequenceFileImporter> getSequenceImporters()
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(SequenceFileImporter.class, true, false, true);
        final List<SequenceFileImporter> result = new ArrayList<SequenceFileImporter>();

        for (PluginDescriptor plugin : plugins)
        {
            final Plugin p = PluginLauncher.start(plugin);

            if (p instanceof SequenceFileImporter)
                result.add((SequenceFileImporter) p);
        }

        // TODO: add sort here from plugin importer preferences

        return result;
    }

    /**
     * Returns all importer which can open the specified file.
     */
    public static List<SequenceFileImporter> getSequenceImporters(List<SequenceFileImporter> importers, String path)
    {
        final List<SequenceFileImporter> result = new ArrayList<SequenceFileImporter>();

        for (SequenceFileImporter importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all importer which can open the specified file.
     */
    public static List<SequenceFileImporter> getSequenceImporters(String path)
    {
        return getSequenceImporters(getSequenceImporters(), path);
    }

    /**
     * Returns the first importer which can open the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     * 
     * @see #getSequenceImporters(List, String)
     */
    public static SequenceFileImporter getSequenceImporter(List<SequenceFileImporter> importers, String path)
    {
        for (SequenceFileImporter importer : importers)
            if (importer.acceptFile(path))
                return importer;

        return null;
    }

    /**
     * Returns the first importer which can open the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     * 
     * @see #getSequenceImporters(String)
     */
    public static SequenceFileImporter getSequenceImporter(String path)
    {
        return getSequenceImporter(getSequenceImporters(), path);
    }

    /**
     * Returns <code>true</code> if the specified path describes a file type which is well known to
     * not be an image file.<br>
     * For instance <i>.exe</i>, <i>.wav</i> or <i>.doc</i> file cannot specify an image file do we
     * can quickly discard them.
     */
    public static boolean canDiscardImageFile(String path)
    {
        final String ext = FileUtil.getFileExtension(path, false).toLowerCase();

        // removes typical extension we can find mixed with image
        if (StringUtil.equals(ext, "xml") || StringUtil.equals(ext, "txt") || StringUtil.equals(ext, "pdf")
                || StringUtil.equals(ext, "xls") || StringUtil.equals(ext, "doc") || StringUtil.equals(ext, "doc")
                || StringUtil.equals(ext, "docx") || StringUtil.equals(ext, "pdf") || StringUtil.equals(ext, "rtf")
                || StringUtil.equals(ext, "exe") || StringUtil.equals(ext, "wav") || StringUtil.equals(ext, "mp3"))
            return true;

        return false;
    }

    /**
     * Returns true if the specified file is a supported image file.
     */
    public static boolean isSupportedImageFile(String path)
    {
        return (getSequenceImporter(path) != null);
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
     * @deprecated Use {@link #getSequenceImporters(String)} instead.
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
     * Loads and returns metadata of the specified image file.<br>
     * It returns <code>null</code> if the specified file is not a valid (or supported) image file.
     */
    public static OMEXMLMetadataImpl getMetaData(String path) throws UnsupportedFormatException, IOException
    {
        for (SequenceFileImporter importer : getSequenceImporters(path))
        {
            try
            {
                // load current file and add to results
                return importer.getMetaData(path);
            }
            catch (UnsupportedFormatException e)
            {
                // display it in console
                IcyExceptionHandler.showErrorMessage(e, false);
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");
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
     * Loads and returns metadata of the specified image file.<br>
     * It returns <code>null</code> if the specified file is not a valid (or supported) image file.
     */
    public static OMEXMLMetadataImpl getMetaData(File file) throws UnsupportedFormatException, IOException
    {
        return getMetaData(file.getAbsolutePath());
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
        // get importer for this file
        final SequenceFileImporter importer = getSequenceImporter(path);

        if (importer == null)
            throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");

        return importer.getThumbnail(path, 0);
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
     * For lower image level access, you can use {@link #getSequenceImporter(String)} method and
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
        // get importer for this file
        final SequenceFileImporter importer = getSequenceImporter(path);

        if (importer == null)
            throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");

        return importer.getImage(path, serie, z, t);
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
        // detect if this is a complete folder load
        final boolean directory = (files.length == 1) && files[0].isDirectory();
        // explode file list
        final File[] singleFiles = explodeAndClean(files);

        if (series == null)
            return loadSequences(singleFiles, -1, separate, autoOrder, directory, false, showProgress);
        if (series.length == 1)
            return loadSequences(singleFiles, series[0], separate, autoOrder, directory, false, showProgress);

        final List<Sequence> result = new ArrayList<Sequence>();

        for (int serie : series)
        {
            result.addAll(Arrays.asList(loadSequences(singleFiles, serie, separate, autoOrder, directory, false,
                    showProgress)));
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
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead or
     *             {@link #load(File, boolean)} if you want to display the resulting sequence.
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
     * @deprecated Use {@link #loadSequences(List, int, boolean, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static Sequence[] loadSequences(File[] files, int serie, boolean separate, boolean autoOrder,
            boolean showProgress)
    {
        // detect if this is a complete folder load
        final boolean directory = (files.length == 1) && files[0].isDirectory();
        // explode file list
        final File[] singleFiles = explodeAndClean(files);

        // load sequences and return them
        return loadSequences(singleFiles, serie, separate, autoOrder, directory, false, showProgress);
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
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could not be opened (not
     * supported).
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
        // detect if this is a complete folder load
        final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
        // explode path list
        final List<String> singlePaths = explodeAndClean(paths);

        // load sequences and return them
        return loadSequences(singlePaths, serie, separate, autoOrder, directory, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param paths
     *        List of image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(List<String> paths, int serie, boolean showProgress)
    {
        final List<Sequence> result = loadSequences(paths, serie, false, true, false, showProgress);

        if (result.size() > 0)
            return result.get(0);

        return null;
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
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
        return loadSequence(CollectionUtil.createArrayList(path), serie, showProgress);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files)
    {
        load(files.toArray(new File[files.size()]), false, true, true);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate)
    {
        load(files.toArray(new File[files.size()]), separate, true, true);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
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
     * @deprecated Use {@link #load(File, boolean)} instead.
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
     * Load the specified image files.<br>
     * The loading process is asynchronous.<br>
     * If <i>separate</i> is false the loader try to set image in the same sequence.<br>
     * If <i>separate</i> is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param paths
     *        list of image file to load
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param autoOrder
     *        Try to order image in sequence from their filename
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
                // load sequence
                final List<Sequence> sequences = loadSequences(paths, -1, separate, autoOrder, true, showProgress);
                // and display them
                for (Sequence seq : sequences)
                    Icy.getMainInterface().addSequence(seq);
            }
        });
    }

    /**
     * Load the specified image file.<br>
     * The loading process is asynchronous and the resulting sequence is automatically displayed
     * when the process complete.
     * 
     * @param path
     *        image file to load
     * @param showProgress
     *        Show progression of loading process.
     */
    public static void load(String path, boolean showProgress)
    {
        load(CollectionUtil.createArrayList(path), false, false, showProgress);
    }

    // /**
    // * Loads the specified image files and return them as sequences.<br>
    // * If 'separate' is false the loader try to set images in the same sequence.<br>
    // * If separate is true each image is loaded in a separate sequence.<br>
    // * As this method can take sometime, you should not call it from the EDT.<br>
    // *
    // * @param files
    // * list of image file to load
    // * @param serie
    // * Serie to load.
    // * @param separate
    // * Force image to be loaded in separate sequence
    // * @param directory
    // * Specify is the source is a single complete directory
    // * @param display
    // * If set to true sequences will be automatically displayed after being loaded.
    // * @param addToRecent
    // * If set to true the files list will be traced in recent opened sequence.
    // * @param showProgress
    // * Show progression in loading process
    // */
    // static Sequence[] loadSequences(File[] files, int serie, boolean separate, boolean autoOrder,
    // boolean directory,
    // boolean addToRecent, boolean showProgress)
    // {
    // // nothing to load
    // if (files.length <= 0)
    // return new Sequence[0];
    //
    // final List<Sequence> result = new ArrayList<Sequence>();
    //
    // final ApplicationMenu mainMenu;
    // final FileFrame loadingFrame;
    //
    // if (addToRecent)
    // mainMenu = Icy.getMainInterface().getApplicationMenu();
    // else
    // mainMenu = null;
    // if (showProgress)
    // loadingFrame = new FileFrame("Loading...", null);
    // else
    // loadingFrame = null;
    //
    // try
    // {
    // final SequenceFileImporter[] importers = getSequenceImporters();
    // final List<File> remainingFiles = Arrays.asList(files);
    //
    // if (separate)
    // {
    // // load each file in a separate sequence
    // for (File file : files)
    // {
    // final SequenceFileImporter[] fileImporters = getSequenceImporters(importers, file);
    //
    // for (SequenceFileImporter importer : fileImporters)
    // {
    // try
    // {
    // // load current file and add to results
    // result.addAll(Arrays.asList(importer.load(new FilePosition[] {new FilePosition(file)},
    // serie, loadingFrame)));
    // // remove from remaining
    // remainingFiles.remove(file);
    // // add as separate item to recent file list
    // if (mainMenu != null)
    // mainMenu.addRecentLoadedFile(file);
    // }
    // catch (UnsupportedFormatException e)
    // {
    // // just ignore and pass to next importer
    // }
    // }
    // }
    // }
    // else
    // {
    // // pass through all importers
    // for (SequenceFileImporter importer : importers)
    // {
    // // retrieve supported files for this importer
    // final File[] supportedFiles = getSupportedFiles(importer,
    // remainingFiles.toArray(new File[remainingFiles.size()]));
    //
    // if (supportedFiles.length > 0)
    // {
    // try
    // {
    // // get positions from files
    // final FilePosition[] filePositions = getFilePositions(supportedFiles, autoOrder);
    // // load files and add to results
    // result.addAll(Arrays.asList(importer.load(filePositions, serie, loadingFrame)));
    // // remove from remaining
    // remainingFiles.removeAll(CollectionUtil.asList(supportedFiles));
    // }
    // catch (UnsupportedFormatException e)
    // {
    // // just ignore and pass to next importer
    // }
    // }
    // }
    //
    // // add as one item to recent file list
    // if (mainMenu != null)
    // {
    // // set only the directory entry
    // if (directory)
    // mainMenu.addRecentLoadedFile(files[0].getParentFile());
    // else
    // mainMenu.addRecentLoadedFile(files);
    // }
    // }
    //
    // if (remainingFiles.size() > 0)
    // {
    // System.err.println("Cannot open the following file(s) (format not supported):");
    // for (File file : remainingFiles)
    // System.err.println(file.getAbsolutePath());
    //
    // if (loadingFrame != null)
    // {
    // if (remainingFiles.size() == 1)
    // new FailedAnnounceFrame(
    // "A file could not be opened (format not supported).\nSee the console output for more details.");
    // else
    // new FailedAnnounceFrame(
    // "Some files could not be opened (format not supported).\nSee the console output for more details.");
    //
    // }
    // }
    //
    // // directory load fit in a single sequence ?
    // if ((result.size() == 1) && directory)
    // {
    // final Sequence seq = result.get(0);
    // // get directory without last separator
    // final String fileDir = FileUtil.getGenericPath(files[0].getParentFile().getAbsolutePath());
    //
    // // set name and filename to use directory instead
    // seq.setName(FileUtil.getFileName(fileDir, false));
    // seq.setFilename(fileDir);
    // }
    // }
    // catch (Exception e)
    // {
    // // just show the error
    // IcyExceptionHandler.showErrorMessage(e, true);
    // if (loadingFrame != null)
    // new FailedAnnounceFrame("Failed to open file(s), see the console output for more details.");
    // }
    // finally
    // {
    // if (loadingFrame != null)
    // loadingFrame.close();
    // }
    //
    // return result.toArray(new Sequence[result.size()]);
    // }

    /**
     * @deprecated Use
     *             {@link #loadSequences(List, int, boolean, boolean, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    static Sequence[] loadSequences(File[] files, int serie, boolean separate, boolean autoOrder, boolean directory,
            boolean addToRecent, boolean showProgress)
    {
        final List<String> paths = CollectionUtil.asList(FileUtil.toPaths(files));
        final List<Sequence> result = loadSequences(paths, serie, separate, autoOrder, directory, addToRecent,
                showProgress);
        return (Sequence[]) result.toArray();
    }

    /**
     * Loads the specified image files and return them as list of sequence.<br>
     * If 'separate' is false the loader try to set images in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     * 
     * @param paths
     *        list of image file to load
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).<br>
     *        -1 is a special value so it gives a chance to the user to select series to open from a
     *        serie selector dialog.
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param directory
     *        Specify is the source is a single complete directory
     * @param display
     *        If set to true sequences will be automatically displayed after being loaded.
     * @param addToRecent
     *        If set to true the files list will be traced in recent opened sequence.
     * @param showProgress
     *        Show progression in loading process
     */
    static List<Sequence> loadSequences(List<String> paths, int serie, boolean separate, boolean autoOrder,
            boolean directory, boolean addToRecent, boolean showProgress)
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
        if (showProgress)
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try
        {
            final List<SequenceFileImporter> importers = getSequenceImporters();
            final List<String> remainingFiles = new ArrayList<String>(paths);

            if (separate)
            {
                if (loadingFrame != null)
                {
                    loadingFrame.setLength(paths.size());
                    loadingFrame.setPosition(0);
                }

                // load each file in a separate sequence
                for (String path : paths)
                {
                    if (loadingFrame != null)
                        loadingFrame.incPosition();

                    // load the file
                    final List<Sequence> sequences = internalLoadSingle(importers, path, serie, loadingFrame);

                    // special case where loading was interrupted --> exit
                    if (sequences == null)
                        return result;
                    else if (sequences.size() > 0)
                    {
                        // add sequences to result
                        result.addAll(sequences);
                        // remove path from remaining
                        remainingFiles.remove(path);
                        // add as separate item to recent file list
                        if (mainMenu != null)
                            mainMenu.addRecentLoadedFile(new File(path));
                    }
                }
            }
            else
            {
                final TreeMap<Integer, Sequence> map = new TreeMap<Integer, Sequence>();

                if (loadingFrame != null)
                    loadingFrame.setAction("Extracting position from filename");

                // final List<FilePosition> filePositions = new ArrayList<Loader.FilePosition>();
                // if (remainingFiles.size() > 1)
                // {
                // // use FilePattern to split images in group
                // while (remainingFiles.size() > 0)
                // {
                // final String path = remainingFiles.get(0);
                // final FilePattern pattern = new FilePattern(new Location(path));
                // final String fileArray[] = pattern.getFiles();
                // final List<String> fileList;
                //
                // // FilePattern could not work it out --> single group file
                // if ((fileArray == null) || (fileArray.length == 0))
                // fileList = CollectionUtil.createArrayList(path);
                // else
                // // get files path handled by the pattern in correct format
                // fileList = FileUtil.toPaths(FileUtil.toFiles(CollectionUtil.asList(fileArray)));
                //
                // // get positions from files
                // filePositions.addAll(getFilePositions(fileList, autoOrder));
                //
                // // remove processed files
                // remainingFiles.removeAll(fileList);
                // }
                //
                // // now we will do the loading operation
                // remainingFiles.addAll(paths);
                // }
                // else
                // filePositions.addAll(getFilePositions(paths, autoOrder));

                final List<FilePosition> filePositions = getFilePositions(paths, autoOrder);
                int lastS = 0;

                if (loadingFrame != null)
                {
                    loadingFrame.setAction("Loading");
                    loadingFrame.setLength(filePositions.size());
                    loadingFrame.setPosition(0);
                }

                // load each file in a separate sequence
                for (FilePosition filePos : filePositions)
                {
                    if (loadingFrame != null)
                        loadingFrame.incPosition();

                    final String path = filePos.path;
                    // load the file
                    final List<Sequence> sequences = internalLoadSingle(importers, path, serie, loadingFrame);

                    // special case where loading was interrupted --> exit
                    if (sequences == null)
                        return result;

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
                            //
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
                            "Some file(s) could not be opened (format not supported).\nSee the console output for more details.");
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
                new FailedAnnounceFrame("Failed to open file(s), see the console output for more details.");
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

            // final Sequence sequences[] = sequencesC.toArray(new Sequence[sequencesC.size()]);
            //
            // // several sequences ?
            // if (sequences.length > 1)
            // {
            // // concatenate sequences on C dimension
            // final Sequence merged = SequenceUtil.concatC(sequences);
            // // then add the result to the list
            // result.add(merged);
            // }
            // else
            // result.add(sequences[0]);

            // better to not merge the C channel after all
            result.addAll(sequencesC);

            // clear the map
            map.clear();
        }
    }

    /**
     * Internal load a single file and return result as Sequence list (for multi serie).
     * 
     * @throws IOException
     */
    static List<Sequence> internalLoadSingle(List<SequenceFileImporter> importers, String path, int serie,
            FileFrame loadingFrame) throws IOException
    {
        if (loadingFrame != null)
            loadingFrame.setFilename(path);

        // get importers for this file
        // final List<SequenceFileImporter> fileImporters = getSequenceImporters(importers, path);
        final List<Sequence> result = new ArrayList<Sequence>();

        for (SequenceFileImporter importer : importers)
        {
            try
            {
                // prepare image loading for this file
                importer.open(path);

                // get metadata
                final OMEXMLMetadataImpl meta = importer.getMetaData(path);
                final int serieCount = MetaDataUtil.getNumSerie(meta);
                // do serie selection
                final int selectedSeries[] = selectSerie(importer, path, meta, serie, serieCount);

                if (selectedSeries.length > 0)
                {
                    for (int s : selectedSeries)
                    {
                        final Sequence seq = createNewSequence(path, meta, s, serieCount > 1);
                        final int sizeZ = MetaDataUtil.getSizeZ(meta, s);
                        final int sizeT = MetaDataUtil.getSizeT(meta, s);
                        // set local length for loader frame
                        final int numImage = sizeZ * sizeT;
                        int progress = 0;

                        if (loadingFrame != null)
                        {
                            if (numImage > 5)
                                loadingFrame.setLength(numImage);
                        }

                        seq.beginUpdate();
                        try
                        {
                            for (int t = 0; t < sizeT; t++)
                            {
                                for (int z = 0; z < sizeZ; z++)
                                {
                                    if (loadingFrame != null)
                                    {
                                        // cancel requested ? --> return null to inform about cancel
                                        if (loadingFrame.isCancelRequested())
                                            return null;

                                        // notify progress to loader frame
                                        // (only if sufficient image loaded)
                                        if (numImage > 5)
                                            loadingFrame.setPosition(progress++);
                                    }

                                    // load image and add it to the sequence
                                    seq.setImage(t, z, importer.getImage(path, s, z, t));
                                }
                            }
                        }
                        finally
                        {
                            seq.endUpdate();
                        }

                        // add sequence to result
                        result.add(seq);
                    }
                }

                // no need to test with others importer
                break;
            }
            catch (UnsupportedFormatException e)
            {
                // the importer should support this file ?
                if (importer.acceptFile(path))
                    // display the error in console and pass to next importer
                    IcyExceptionHandler.showErrorMessage(e, false);
            }
            finally
            {
                // close importer
                importer.close();
            }
        }

        return result;
    }

    static Sequence createNewSequence(String path, OMEXMLMetadataImpl meta, int serie, boolean multiSerie)
    {
        // create a new sequence
        final Sequence result = new Sequence(OMEUtil.createOMEMetadata(meta, serie));

        // default name
        final String name = FileUtil.getFileName(path, false);

        // default name used --> use better name
        if (result.isDefaultName())
        {
            // multi series image --> add serie info
            if (multiSerie)
                result.setName(name + " - serie " + StringUtil.toString(serie));
            else
                result.setName(name);
        }
        else
        {
            // multi series image --> adjust name to keep file name info
            if (multiSerie)
                result.setName(name + " - " + result.getName());
        }

        // set final filename
        result.setFilename(path);

        return result;
    }

    static int[] selectSerie(final SequenceFileImporter importer, final String path, final OMEXMLMetadataImpl meta,
            int serie, int serieCount) throws UnsupportedFormatException, IOException
    {
        final int[] result = new int[serieCount];

        if (serieCount > 0)
        {
            // multi serie, display selection dialog
            if (serieCount > 1)
            {
                // default: no selected serie
                Arrays.fill(result, -1);

                // allow user to select series to open
                if ((serie == -1) && !Icy.isHeadLess())
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
                                System.arraycopy(series, 0, result, 0, series.length);
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
                else
                    // use the pre selected serie
                    result[0] = (serie != -1) ? serie : 0;
            }
            else
                // only 1 serie so open it
                result[0] = 0;
        }

        return result;
    }

    /**
     * @deprecated Use {@link #explodeAndClean(List)} instead.
     */
    @Deprecated
    static File[] explodeAndClean(File[] files)
    {
        final File[] allFiles = FileUtil.explode(files, null, true, false);
        final List<File> result = new ArrayList<File>();

        // extensions based exclusion
        for (int i = 0; i < allFiles.length; i++)
        {
            final File file = allFiles[i];

            // keep non discarded images
            if (!canDiscardImageFile(file.getPath()))
                result.add(file);
        }

        return result.toArray(new File[result.size()]);
    }

    static List<String> explodeAndClean(List<String> paths)
    {
        final List<String> allPaths = FileUtil.toPaths(FileUtil.explode(FileUtil.toFiles(paths), null, true, false));
        final List<String> result = new ArrayList<String>();

        // extensions based exclusion
        for (String path : allPaths)
        {
            // keep non discarded images
            if (!canDiscardImageFile(path))
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
     */
    public static List<FilePosition> getFilePositions(List<String> paths, boolean dimOrder)
    {
        final List<String> filenames = new ArrayList<String>(paths);
        final List<Position> positions = new ArrayList<Position>(paths.size());
        final List<FilePosition> result = new ArrayList<FilePosition>(paths.size());

        // smart sort on name
        Collections.sort(filenames, new AlphanumComparator());

        if (dimOrder)
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
                final int size = end - st;

                // remove number from name if number size < 6
                if (size < 6)
                    result = result.substring(0, st) + result.substring(end);
                // pass to next
                else
                    pos = end;
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
                if ((endInd - startInd) < 6)
                {
                    // get prefix
                    final String prefix = getPositionPrefix(name, startInd - 1);
                    // get value
                    final int value = StringUtil.parseInt(name.substring(startInd, endInd), -1);

                    // add the position info
                    result.addChunk(prefix, value);
                }

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
