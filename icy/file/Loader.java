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
package icy.file;

import icy.gui.dialog.SeriesSelectionDialog;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.BandPosition;
import icy.image.IcyBufferedImage;
import icy.image.ImagePosition;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Sequence / Image loader class.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Loader
{
    private static class FilePosition extends BandPosition
    {
        public final File file;
        public boolean tFixed;
        public boolean zFixed;
        public boolean cFixed;

        public FilePosition(File file)
        {
            super();

            this.file = file;
            tFixed = false;
            zFixed = false;
            cFixed = false;
        }

        @Override
        public int compareTo(Object o)
        {
            if (o instanceof FilePosition)
                return alternateCompareTo((FilePosition) o);

            return super.compareTo(o);
        }

        @Override
        public String toString()
        {
            return file.getPath() + "  T:" + t + " Z:" + z + " C:" + c;
        }
    }

    private static class SequenceLoader implements Runnable
    {
        final List<File> files;
        final FileFrame loaderFrame;
        final List<Sequence> sequences;
        final ImageReader mainReader;
        final boolean display;
        final boolean directory;
        IFormatReader lastUsedReader;
        List<Integer> series;
        List<Integer> selectedSeries;

        public SequenceLoader(List<File> files, List<Integer> series, boolean display, boolean directory,
                boolean showProgress)
        {
            super();

            final boolean headless = Icy.isHeadLess();

            this.files = files;

            if (headless)
            {
                loaderFrame = null;
                this.display = false;
            }
            else
            {
                if (showProgress)
                    loaderFrame = new FileFrame("Loading", null);
                else
                    loaderFrame = null;
                this.display = display;
            }

            sequences = new ArrayList<Sequence>();
            mainReader = new ImageReader();
            lastUsedReader = null;
            this.directory = directory;
            this.series = series;
            selectedSeries = null;
        }

        @Override
        public void run()
        {
            if (files.size() == 0)
            {
                if (loaderFrame != null)
                {
                    loaderFrame.close();
                    new AnnounceFrame("No image to load", 10);
                }
                return;
            }

            final ArrayList<FilePosition> filePositions = new ArrayList<FilePosition>();
            String filename = files.get(0).getAbsolutePath();

            try
            {
                // build position list
                for (File file : files)
                    filePositions.add(getPositionFromFilename(file));

                FilePosition pos = filePositions.get(0);
                int t = pos.getT();
                int z = pos.getZ();
                int c = pos.getC();
                boolean sameT = true;
                boolean sameZ = true;
                boolean sameC = true;
                boolean tNotFixed = true;
                boolean zNotFixed = true;
                boolean cNotFixed = true;

                // remove "empty" dimension
                for (FilePosition fp : filePositions)
                {
                    sameT &= (t == fp.getT());
                    sameZ &= (z == fp.getZ());
                    sameC &= (c == fp.getC());
                    tNotFixed &= !fp.tFixed;
                    zNotFixed &= !fp.zFixed;
                    cNotFixed &= !fp.cFixed;
                }

                // same T and not fixed
                if (sameT && tNotFixed)
                {
                    // different C and not fixed
                    if (!sameC && cNotFixed)
                    {
                        // move C to T (C becomes 0)
                        for (FilePosition fp : filePositions)
                            fp.set(fp.getC(), fp.getT(), 0);

                        sameT = false;
                        sameC = true;
                    }
                    // different Z and not fixed
                    else if (!sameZ && zNotFixed)
                    {
                        // move Z to T (Z becomes 0)
                        for (FilePosition fp : filePositions)
                            fp.set(fp.getZ(), 0, fp.getC());

                        sameT = false;
                        sameZ = true;
                    }
                }

                // same Z and not fixed
                if (sameZ && zNotFixed)
                {
                    // different C and not fixed
                    if (!sameC && cNotFixed)
                    {
                        // move C to Z (C becomes 0)
                        for (FilePosition fp : filePositions)
                            fp.set(fp.getT(), fp.getC(), 0);

                        sameZ = false;
                        sameC = true;
                    }
                }

                // sort on position
                Collections.sort(filePositions);

                // then we compact the position
                final int len = filePositions.size();

                final BandPosition firstPos = filePositions.get(0);
                final BandPosition prevPos = new BandPosition();
                final BandPosition newPos = new BandPosition(0, 0, 0);

                // get first old position
                prevPos.copyFrom(firstPos);

                // notify progress to loader frame
                if (loaderFrame != null)
                    loaderFrame.notifyProgress(0, len);

                // load first image (and update position if needed)
                load(filePositions.get(0).file, newPos);

                // fix positions while loading images
                for (int index = 1; index < len; index++)
                {
                    if (loaderFrame != null)
                    {
                        if (loaderFrame.isCancelRequested())
                            return;

                        // notify progress to loader frame
                        loaderFrame.notifyProgress(index, len);
                    }

                    final FilePosition filePosition = filePositions.get(index);
                    final File file = filePosition.file;
                    filename = file.getAbsolutePath();

                    // ordering as follow : C -> T -> Z
                    if (prevPos.getC() != filePosition.getC())
                    {
                        newPos.setC(newPos.getC() + 1);
                        newPos.setT(0);
                        newPos.setZ(0);
                        prevPos.setC(filePosition.getC());

                        // create a new sequence for this channel
                        final Sequence seq = new Sequence(MetaDataUtil.createOMEMetadata(
                                (IMetadata) lastUsedReader.getMetadataStore(), 0));

                        // default name loaded from metadata (if available)
                        String name;
                        if (seq.isDefaultName())
                            name = FileUtil.getFileName(filename, false);
                        else
                            name = seq.getName();
                        // then we add channel name information if available
                        if (seq.isDefaultChannelName(c))
                            name += " (channel " + newPos.getC() + ")";
                        else
                            name += "(" + seq.getChannelName(index) + ")";
                        seq.setName(name);

                        seq.setFilename(FileUtil.setExtension(
                                FileUtil.setExtension(filename, "") + "_C" + newPos.getC(),
                                FileUtil.getFileExtension(filename, true)));
                        sequences.add(seq);
                    }
                    else if (prevPos.getT() != filePosition.getT())
                    {
                        newPos.setT(newPos.getT() + 1);
                        newPos.setZ(0);
                        prevPos.setT(filePosition.getT());
                        prevPos.setZ(0);
                    }
                    else if (prevPos.getZ() != filePosition.getZ())
                    {
                        newPos.setZ(newPos.getZ() + 1);
                        prevPos.setZ(filePosition.getZ());
                    }
                    else
                    {
                        // multi Z image ?
                        if (newPos.getZ() > 0)
                        {
                            // increment Z dim
                            newPos.setZ(newPos.getZ() + 1);
                        }
                        else
                        {
                            // else we increment T dim by default
                            newPos.setT(newPos.getT() + 1);
                            newPos.setZ(0);
                        }
                    }

                    // load image (and update position if needed)
                    load(file, newPos);
                }

                if (loaderFrame != null)
                {
                    if (loaderFrame.isCancelRequested())
                        return;
                }

                // directory load fit in a single sequence ?
                if ((sequences.size() == 1) && directory)
                {
                    final Sequence seq = sequences.get(0);
                    // get directory without last separator
                    final String fileDir = FileUtil.getGenericPath(files.get(0).getParentFile().getAbsolutePath());

                    // set name and filename to use directory instead
                    seq.setName(FileUtil.getFileName(fileDir, false));
                    seq.setFilename(fileDir);
                }

                for (Sequence seq : sequences)
                {
                    // load sequence XML data
                    if (GeneralPreferences.getSequencePersistence())
                        seq.loadXMLData();
                    // then display them
                    if (display)
                        Icy.getMainInterface().addSequence(seq);
                }
            }
            catch (Exception e)
            {
                System.err.println("Error while loading image '" + filename + "' :");
                IcyExceptionHandler.showErrorMessage(e, true);
                new FailedAnnounceFrame("Failed to load image (see output console for detail)", 15);
            }
            finally
            {
                if (loaderFrame != null)
                    loaderFrame.close();
            }
        }

        // do the image load
        private void load(File file, BandPosition position) throws FormatException, IOException
        {
            final String path = file.getAbsolutePath();
            final IFormatReader reader;

            if ((lastUsedReader != null) && lastUsedReader.isThisType(path, true))
                reader = lastUsedReader;
            else
                reader = mainReader.getReader(path);

            // keep trace of last used reader
            lastUsedReader = reader;

            // set current filename
            loaderFrame.setFilename(path);

            // prepare meta data store structure
            reader.setMetadataStore(new OMEXMLMetadataImpl());
            // load file with LOCI library
            reader.setId(path);

            // only if series has not yet be defined
            if (series == null)
            {
                if (reader.getSeriesCount() > 1)
                {
                    // use invokeNow carefully !
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            selectedSeries = new SeriesSelectionDialog(reader).getSelectedSeries();
                        }
                    });
                }
                else
                {
                    selectedSeries = new ArrayList<Integer>();
                    selectedSeries.add(Integer.valueOf(0));
                }
            }
            else
                selectedSeries = series;

            // no selected serie --> exit
            if ((selectedSeries == null) || selectedSeries.isEmpty())
            {
                reader.close();
                return;
            }

            boolean firstSerie = true;

            try
            {
                for (Integer s : selectedSeries)
                {
                    final int serieIndex = s.intValue();
                    Sequence seq;

                    reader.setSeries(serieIndex);

                    // first serie and already have sequence(s) --> concatenate to last one
                    if (firstSerie && (sequences.size() > 0))
                        seq = sequences.get(sequences.size() - 1);
                    else
                    {
                        // create and add the first sequence
                        seq = new Sequence(MetaDataUtil.createOMEMetadata((IMetadata) reader.getMetadataStore(),
                                serieIndex));
                        // default name used --> use better name
                        if (seq.isDefaultName())
                        {
                            // multi series image --> add serie info
                            if (selectedSeries.size() > 1)
                                seq.setName(FileUtil.getFileName(path, false) + " - serie " + s);
                            else
                                seq.setName(FileUtil.getFileName(path, false));
                        }
                        // multi series image --> adjust name to keep file name info
                        else if (selectedSeries.size() > 1)
                            seq.setName(FileUtil.getFileName(path, false) + " - " + seq.getName());
                        // set filename
                        seq.setFilename(path);
                        sequences.add(seq);

                        // re init position
                        position.set(0, 0, 0);
                    }

                    final int frames = reader.getSizeT();
                    final int planes = reader.getSizeZ();

                    // set local length for loader frame
                    final int progressLen = frames * planes;

                    if (loaderFrame != null)
                    {
                        if (progressLen > 10)
                            loaderFrame.setLength(progressLen);
                    }

                    int progress = 0;

                    seq.beginUpdate();
                    try
                    {
                        for (int t = 0; t < frames; t++)
                        {
                            // no single image ?
                            if (t > 0)
                            {
                                // increment T position
                                position.setT(position.getT() + 1);
                                position.setZ(0);
                            }

                            for (int z = 0; z < planes; z++)
                            {
                                if (loaderFrame != null)
                                {
                                    // cancel requested ?
                                    if (loaderFrame.isCancelRequested())
                                        return;

                                    // notify progress to loader frame (only if sufficient image
                                    // loaded)
                                    if (progressLen > 10)
                                        loaderFrame.setPosition(progress++);
                                }

                                // no single image ? increment Z position
                                if (z > 0)
                                    position.setZ(position.getZ() + 1);

                                // get composed image
                                final IcyBufferedImage icyImage = IcyBufferedImage.createFrom(reader, z, t);

                                // image format is not compatible with this sequence ?
                                if (!seq.isCompatible(icyImage))
                                {
                                    // remove empty element on current sequence
                                    seq.packImageList();
                                    seq.endUpdate();

                                    // and add a new sequence
                                    seq = new Sequence(MetaDataUtil.createOMEMetadata(
                                            (IMetadata) reader.getMetadataStore(), serieIndex));
                                    // default name used --> use better name
                                    if (seq.isDefaultName())
                                    {
                                        // multi series image --> add serie info
                                        if (selectedSeries.size() > 1)
                                            seq.setName(FileUtil.getFileName(path, false) + " - serie " + s);
                                        else
                                            seq.setName(FileUtil.getFileName(path, false));
                                    }
                                    // multi series image --> adjust name to keep file name info
                                    else if (selectedSeries.size() > 1)
                                        seq.setName(FileUtil.getFileName(path, false) + " - " + seq.getName());
                                    seq.setFilename(path);
                                    sequences.add(seq);
                                    seq.beginUpdate();

                                    // re init position
                                    position.set(0, 0, 0);
                                }

                                // add image to the sequence
                                seq.setImage(position.getT(), position.getZ(), icyImage);
                            }
                        }
                    }
                    finally
                    {
                        // remove empty element on current sequence
                        seq.packImageList();
                        seq.endUpdate();
                    }

                    firstSerie = false;
                }
            }
            finally
            {
                reader.close();
            }
        }
    }

    /**
     * Returns true if the specified file is not an image file for sure.<br>
     * This method use the well known extension (doc, rtf, txt, exe, xml...) and discard them.
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
     * Returns true if the specified file is a valid and supported image file.
     */
    public static boolean isImageFile(String path)
    {
        // removes well known extensions
        if (canDiscardImageFile(path))
            return false;

        return new ImageReader().isThisType(path);
    }

    /**
     * Returns the reader for the specified image file.<br>
     * Returns null if the specified file is not a valid (or supported) image file.
     * 
     * @throws IOException
     * @throws FormatException
     */
    public static IFormatReader getReader(String path) throws FormatException, IOException
    {
        return new ImageReader().getReader(path);
    }

    /**
     * Returns metadata of the specified image file using the specified Reader.<br>
     * Returns null if the specified file is not a valid (or supported) image file.
     * 
     * @param reader
     *        the reader to use to retrieve the metadata
     * @param path
     *        filename of the image we want to retrieve metadata.
     * @throws IOException
     * @throws FormatException
     */
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
     * Returns metadata of the specified image file.<br>
     * Returns null if the specified file is not a valid (or supported) image file.
     * 
     * @throws IOException
     * @throws FormatException
     */
    public static OMEXMLMetadataImpl getMetaData(String path) throws FormatException, IOException
    {
        return getMetaData(getReader(path), path);
    }

    /**
     * Returns metadata of the specified image file.<br>
     * Returns null if the specified file is not a valid (or supported) image file.
     * 
     * @throws IOException
     * @throws FormatException
     */
    public static OMEXMLMetadataImpl getMetaData(File file) throws FormatException, IOException
    {
        return getMetaData(file.getAbsolutePath());
    }

    /**
     * Load and return the image at given position from the specified reader.
     * 
     * @param reader
     *        initialized image reader (file id already set).
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @return icy image
     * @throws IOException
     * @throws FormatException
     */
    public static IcyBufferedImage loadImage(IFormatReader reader, int z, int t) throws FormatException, IOException
    {
        // return an icy image
        return IcyBufferedImage.createFrom(reader, z, t);
    }

    /**
     * Load and return a single image from the specified reader.<br>
     * If the specified file contains severals image the first image is returned.
     * 
     * @param reader
     *        initialized image reader (file id already set).
     * @return icy image
     * @throws IOException
     * @throws FormatException
     */
    public static IcyBufferedImage loadImage(IFormatReader reader) throws FormatException, IOException
    {
        // return an icy image
        return IcyBufferedImage.createFrom(reader, 0, 0);
    }

    /**
     * Load and return the image at given position from the specified file.
     * 
     * @param file
     *        image file.
     * @param z
     *        Z position of the image to open.
     * @param t
     *        T position of the image to open.
     * @return icy image
     * @throws IOException
     * @throws FormatException
     */
    public static IcyBufferedImage loadImage(File file, int z, int t) throws FormatException, IOException
    {
        final ImageReader reader = new ImageReader();

        // set file id
        reader.setId(file.getAbsolutePath());
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
     * Load and return a single image from the specified file.<br>
     * If the specified file contains severals image the first image is returned.
     * 
     * @param file
     * @return icy image
     * @throws IOException
     * @throws FormatException
     */
    public static IcyBufferedImage loadImage(File file) throws FormatException, IOException
    {
        final ImageReader reader = new ImageReader();

        // set file id
        reader.setId(file.getAbsolutePath());
        try
        {
            // return an icy image
            return IcyBufferedImage.createFrom(reader, 0, 0);
        }
        finally
        {
            // close reader
            reader.close();
        }
    }

    /**
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * 
     * @param files
     *        List of image file to load.
     * @param series
     *        Series to load (for multi serie sequence).
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate,
            boolean showProgress)
    {
        final boolean directory = (files.size() == 1) && files.get(0).isDirectory();

        // explode file list and load internally
        return internalLoadWait(explodeAndClean(files), series, separate, directory, showProgress);
    }

    /**
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * 
     * @param files
     *        List of image file to load.
     * @param series
     *        Series to load (for multi serie sequence).
     * @param separate
     *        Force image to be loaded in separate sequence.
     */
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate)
    {
        return loadSequences(files, series, separate, true);
    }

    /**
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * 
     * @param files
     *        List of image file to load.
     * @param series
     *        Series to load (for multi serie sequence).
     */
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series)
    {
        return loadSequences(files, series, false, true);
    }

    /**
     * @deprecated Uses {@link #loadSequences(List, List, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate,
            boolean display, boolean addToRecent)
    {
        return loadSequences(files, series, separate, true);
    }

    /**
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * 
     * @param files
     *        List of image file to load.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(List<File> files, boolean separate, boolean showProgress)
    {
        return loadSequences(files, null, separate, showProgress);
    }

    /**
     * Load a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * 
     * @param files
     *        List of image file to load.
     * @param separate
     *        Force image to be loaded in separate sequence.
     */
    public static List<Sequence> loadSequences(List<File> files, boolean separate)
    {
        return loadSequences(files, null, separate, true);
    }

    /**
     * @deprecated Uses {@link #loadSequences(List, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, boolean separate, boolean display, boolean addToRecent)
    {
        return loadSequences(files, null, separate, true);
    }

    /**
     * Load a list of sequence from the specified multi serie image file returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param file
     *        Image file to load.
     * @param series
     *        Series to load.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static List<Sequence> loadSequences(File file, List<Integer> series, boolean showProgress)
    {
        return loadSequences(CollectionUtil.createArrayList(file), series, false, showProgress);
    }

    /**
     * Load a list of sequence from the specified multi serie image file returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param file
     *        Image file to load.
     * @param series
     *        Series to load.
     */
    public static List<Sequence> loadSequences(File file, List<Integer> series)
    {
        return loadSequences(CollectionUtil.createArrayList(file), series, false, true);
    }

    /**
     * @deprecated Uses {@link #loadSequences(File, List)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series, boolean display, boolean addToRecent)
    {
        return loadSequences(CollectionUtil.createArrayList(file), series, false, true);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param files
     *        List of image file to load.
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(List<File> files, boolean showProgress)
    {
        final List<Sequence> result = loadSequences(files, null, false, showProgress);

        if (result.size() > 0)
            return result.get(0);

        return null;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param files
     *        List of image file to load.
     */
    public static Sequence loadSequence(List<File> files)
    {
        return loadSequence(files, true);
    }

    /**
     * @deprecated uses {@link #loadSequence(List)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files, boolean display, boolean addToRecent)
    {
        return loadSequence(files);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     */
    public static Sequence loadSequence(File file, boolean showProgress)
    {
        return loadSequence(CollectionUtil.createArrayList(file), showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     */
    public static Sequence loadSequence(File file)
    {
        return loadSequence(CollectionUtil.createArrayList(file), true);
    }

    /**
     * Load the specified image file.<br>
     * The loading process is asynchronous and the resulting sequence is automatically displayed
     * when the process complete.
     * 
     * @param file
     *        image file to load
     */
    public static void load(File file)
    {
        load(CollectionUtil.createArrayList(file), false, true);
    }

    /**
     * Load the specified image file.<br>
     * The loading process is asynchronous and the resulting sequence is automatically displayed
     * when the process complete.
     * 
     * @param file
     *        image file to load
     * @param showProgress
     *        Show progression of loading process.
     */
    public static void load(File file, boolean showProgress)
    {
        load(CollectionUtil.createArrayList(file), false, showProgress);
    }

    /**
     * Load the specified image files.<br>
     * The loading process is asynchronous and by default the loader try to set image in the same
     * sequence if they have the same type.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param files
     *        list of image file to load
     */
    public static void load(List<File> files)
    {
        load(files, false, true);
    }

    /**
     * Load the specified image files.<br>
     * The loading process is asynchronous.<br>
     * If 'separate' is false the loader try to set image in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param files
     *        list of image file to load
     * @param separate
     *        Force image to be loaded in separate sequence
     */
    public static void load(List<File> files, boolean separate)
    {
        load(files, separate, true);
    }

    /**
     * Load the specified image files.<br>
     * The loading process is asynchronous.<br>
     * If 'separate' is false the loader try to set image in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param files
     *        list of image file to load
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(List<File> files, boolean separate, boolean showProgress)
    {
        final boolean directory = (files.size() == 1) && files.get(0).isDirectory();

        // explode file list
        internalLoad(explodeAndClean(files), separate, directory, showProgress);
    }

    /**
     * Load (asynchronously) the specified image files.<br>
     * If 'separate' is false the loader try to set image in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     * 
     * @param files
     *        list of image file to load
     * @param separate
     *        Force image to be loaded in separate sequence
     * @param directory
     *        Specify is the source is a single complete directory
     * @param showProgress
     *        Show progression in loading process
     */
    private static void internalLoad(final List<File> files, final boolean separate, final boolean directory,
            final boolean showProgress)
    {
        // to avoid blocking call
        ThreadUtil.bgRunWait(new Runnable()
        {
            @Override
            public void run()
            {
                final ApplicationMenu mainMenu = Icy.getMainInterface().getApplicationMenu();

                // loading
                if (separate)
                {
                    for (File file : files)
                    {
                        // add as separate item to recent file list
                        if (mainMenu != null)
                            mainMenu.addRecentLoadedFile(file);

                        // create sequence loader
                        final SequenceLoader loadingThread = new SequenceLoader(CollectionUtil.createArrayList(file),
                                null, true, directory, showProgress);
                        // load file using background processor
                        ThreadUtil.bgRunWait(loadingThread);
                    }
                }
                else
                {
                    if (files.size() > 0)
                    {
                        // add as one item to recent file list
                        if (mainMenu != null)
                        {
                            // set only the directory entry
                            if (directory)
                                mainMenu.addRecentLoadedFile(files.get(0).getParentFile());
                            else
                                mainMenu.addRecentLoadedFile(files);
                        }

                        // create and run sequence loader
                        new SequenceLoader(files, null, true, directory, showProgress).run();
                    }
                }
            }
        });
    }

    /**
     * This method load the specified files and return them as Sequence.<br>
     * As this method can take sometime, you should not call it from the EDT.
     * 
     * @param files
     *        list of image file to load
     * @param series
     *        Series to load.
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
    private static List<Sequence> internalLoadWait(final List<File> files, final List<Integer> series,
            final boolean separate, final boolean directory, final boolean showProgress)
    {
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        // loading
        if (separate)
        {
            for (File file : files)
            {
                // create sequence loader
                final SequenceLoader t = new SequenceLoader(CollectionUtil.createArrayList(file), series, false,
                        directory, showProgress);
                // load sequence
                t.run();
                // then add results
                result.addAll(t.sequences);
            }
        }
        else
        {
            if (files.size() > 0)
            {
                // create and run sequence loader
                final SequenceLoader t = new SequenceLoader(files, series, false, directory, showProgress);
                // load sequence
                t.run();
                // then add results
                result.addAll(t.sequences);
            }
        }

        return result;
    }

    private static List<File> explodeAndClean(List<File> files)
    {
        final List<File> result = FileUtil.explode(files, true, false);

        // extensions based exclusion
        for (int i = result.size() - 1; i >= 0; i--)
        {
            final File file = result.get(i);
            final String path = file.getPath();

            // remove well known extensions we can find mixed with image
            if (canDiscardImageFile(path))
                result.remove(i);
        }

        return result;
    }

    private static void setPositionFromNumberString(FilePosition position, String number)
    {
        final int value;

        // try to get position letter from number string
        char pos = Character.toUpperCase(number.charAt(0));
        boolean fixed = false;
        final char firstEmptyPos = position.getFirstEmptyPos();

        // first char is not digit ?
        if (!Character.isDigit(pos))
        {
            // if pos is not a valid position, we use the first available one
            if (!position.isValidIdent(pos))
                pos = firstEmptyPos;
            // else we define the position as fixed
            else
                fixed = true;
            // get number from the rest of the string
            value = StringUtil.parseInt(number.substring(1), -1);
        }
        else
        {
            // use the first available position
            pos = firstEmptyPos;
            // obtain number from full string
            value = StringUtil.parseInt(number, -1);
        }

        switch (pos)
        {
            case ImagePosition.T_ID:
                // doesn't support value >= 100000 for T dimension
                if (value < 100000)
                {
                    position.setT(value);
                    position.tFixed = fixed;
                }
                break;

            case ImagePosition.Z_ID:
                // doesn't support value >= 10000 for Z dimension
                if (value < 10000)
                {
                    position.setZ(value);
                    position.zFixed = fixed;
                }
                break;

            case BandPosition.C_ID:
            case BandPosition.C_ID_ALTERNATE:
                // loaded as separate sequence, no limit
                position.setC(value);
                position.cFixed = fixed;
                break;
        }
    }

    private static int getLastIndexOf(char id, ArrayList<String> numbers)
    {
        int index = numbers.size() - 1;
        while (index >= 0)
        {
            if (Character.toUpperCase(numbers.get(index).charAt(0)) == id)
                return index;

            index--;
        }

        return index;
    }

    private static void removePreviousIdent(char id, ArrayList<String> numbers, int from)
    {
        int index = from;
        while (index >= 0)
        {
            if (Character.toUpperCase(numbers.get(index).charAt(0)) == id)
                numbers.remove(index);

            index--;
        }
    }

    private static boolean removeFirstInvalidIdent(ArrayList<String> numbers)
    {
        final int len = numbers.size();
        int index = 0;

        while (index < len)
        {
            if (!BandPosition.isValidIdentStatic(Character.toUpperCase(numbers.get(index).charAt(0))))
            {
                numbers.remove(index);
                return true;
            }

            index++;
        }

        return false;
    }

    /**
     * Return a FilePosition from the specified filename.
     */
    public static FilePosition getPositionFromFilename(File file)
    {
        final String filename = file.getAbsolutePath();
        final ArrayList<String> numbers = new ArrayList<String>();
        // get filename without extension
        final String value = FileUtil.getFileName(filename, false);

        final int len = value.length();

        int index = 0;
        while (index < len)
        {
            // get starting digit char index
            final int startInd = StringUtil.getNextDigitCharIndex(value, index);

            // we find a digit char ?
            if (startInd >= 0)
            {
                final String number;

                // get ending digit char index
                int endInd = StringUtil.getNextNonDigitCharIndex(value, startInd);
                if (endInd < 0)
                    endInd = len;

                // we want to get number + preceding letter
                number = value.substring(Math.max(0, startInd - 1), endInd);
                // add number only if < X10000 (else it can be a date)
                if (number.length() < 6)
                    // add the found number to the list
                    numbers.add(number);

                // adjust index
                index = endInd;
            }
            else
                index = len;
        }

        // clean up numbers
        if (numbers.size() > 3)
        {
            // find T number
            index = getLastIndexOf(ImagePosition.T_ID, numbers);
            // remove duplicate
            if (index != -1)
                removePreviousIdent(ImagePosition.T_ID, numbers, index - 1);
            // find Z number
            index = getLastIndexOf(ImagePosition.Z_ID, numbers);
            // remove duplicate
            if (index != -1)
                removePreviousIdent(ImagePosition.Z_ID, numbers, index - 1);
            // find C number
            index = getLastIndexOf(BandPosition.C_ID, numbers);
            // remove duplicate
            if (index != -1)
            {
                // remove duplicate C number
                removePreviousIdent(BandPosition.C_ID, numbers, index - 1);
                // remove all C alternate number
                removePreviousIdent(BandPosition.C_ID, numbers, numbers.size() - 1);
            }
            else
            {
                // find C alternate number
                index = getLastIndexOf(BandPosition.C_ID_ALTERNATE, numbers);
                // remove duplicate
                if (index != -1)
                    removePreviousIdent(BandPosition.C_ID_ALTERNATE, numbers, index - 1);
            }
        }

        // keep only the last 3 preferred number
        boolean b = true;
        while ((numbers.size() > 3) && b)
            b = removeFirstInvalidIdent(numbers);

        final FilePosition result = new FilePosition(file);

        // set numbers to to position
        for (String number : numbers)
            setPositionFromNumberString(result, number);

        return result;
    }
}
