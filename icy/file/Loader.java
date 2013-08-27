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

import icy.gui.dialog.SeriesSelectionDialog;
import icy.gui.frame.error.ErrorReportFrame;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
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

        public FilePosition(File file, int t, int z, int c)
        {
            super();

            this.file = file;
            this.t = t;
            this.z = z;
            this.c = c;
            tFixed = false;
            zFixed = false;
            cFixed = false;
        }

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

    /**
     * Private class used to load sequence.<br>
     * Create a new instance for each load process.
     */
    private static class SequenceLoader
    {
        final private List<Sequence> sequences;
        final private ImageReader mainReader;
        IFormatReader lastReader;
        int[] selectedSeries;
        private FileFrame frame;

        SequenceLoader()
        {
            super();

            sequences = new ArrayList<Sequence>();
            mainReader = new ImageReader();
            lastReader = null;
        }

        List<Sequence> load(File[] files, int[] series, boolean autoOrder, boolean directory, FileFrame loadingFrame)
        {
            // no file to load --> no need to go further...
            if (files.length == 0)
            {
                if (!Icy.isHeadLess())
                    new AnnounceFrame("No image to load", 10);

                return sequences;
            }

            if (Icy.isHeadLess())
                frame = null;
            else
                frame = loadingFrame;

            final List<FilePosition> filePositions = new ArrayList<FilePosition>();
            String filename = files[0].getAbsolutePath();

            try
            {
                if (autoOrder)
                {
                    // build position list from filename
                    for (File file : files)
                        filePositions.add(getPositionFromFilename(file));
                }
                else
                {
                    for (int i = 0; i < files.length; i++)
                        filePositions.add(new FilePosition(files[i], i, 0, 0));
                }

                // compact positions
                compact(filePositions);

                // then we iterate through them
                final int len = filePositions.size();

                final BandPosition prevPos = new BandPosition();
                final BandPosition currentPos = new BandPosition(0, 0, 0);

                // init previous position with first position
                prevPos.copyFrom(filePositions.get(0));

                // notify progress to loader frame
                if (frame != null)
                    frame.notifyProgress(0, len);

                // load first image (and update position if needed)
                internalLoadSingle(filePositions.get(0).file, currentPos, series, true);

                // fix positions while loading images
                for (int index = 1; index < len; index++)
                {
                    if (frame != null)
                    {
                        if (frame.isCancelRequested())
                            return new ArrayList<Sequence>();

                        // notify progress to loader frame
                        frame.notifyProgress(index, len);
                    }

                    final FilePosition pos = filePositions.get(index);
                    final File file = pos.file;
                    filename = file.getAbsolutePath();
                    boolean forceNew = false;

                    // ordering as follow : C -> T -> Z
                    if (prevPos.getC() != pos.getC())
                    {
                        currentPos.setC(currentPos.getC() + 1);
                        currentPos.setT(0);
                        currentPos.setZ(0);
                        prevPos.setC(pos.getC());

                        // force creation of a new sequence
                        forceNew = true;

                        // create a new sequence for this channel
                        // final Sequence seq = new Sequence(MetaDataUtil.createOMEMetadata(
                        // (IMetadata) lastReader.getMetadataStore(), 0));
                        //
                        // // default name loaded from metadata (if available)
                        // String name;
                        // if (seq.isDefaultName())
                        // name = FileUtil.getFileName(filename, false);
                        // else
                        // name = seq.getName();
                        // // then we add channel name information if available
                        // if (seq.isDefaultChannelName(0))
                        // name += " (channel " + currentPos.getC() + ")";
                        // else
                        // name += "(" + seq.getChannelName(0) + ")";
                        // seq.setName(name);
                        //
                        // seq.setFilename(FileUtil.setExtension(
                        // FileUtil.setExtension(filename, "") + "_C" + currentPos.getC(),
                        // FileUtil.getFileExtension(filename, true)));
                        // sequences.add(seq);
                    }
                    else if (prevPos.getT() != pos.getT())
                    {
                        currentPos.setT(currentPos.getT() + 1);
                        currentPos.setZ(0);
                        prevPos.setT(pos.getT());
                        prevPos.setZ(0);
                    }
                    else if (prevPos.getZ() != pos.getZ())
                    {
                        currentPos.setZ(currentPos.getZ() + 1);
                        prevPos.setZ(pos.getZ());
                    }
                    else
                    {
                        // multi Z image ?
                        if (currentPos.getZ() > 0)
                        {
                            // increment Z dim
                            currentPos.setZ(currentPos.getZ() + 1);
                        }
                        else
                        {
                            // else we increment T dim by default
                            currentPos.setT(currentPos.getT() + 1);
                            currentPos.setZ(0);
                        }
                    }

                    // load image (and update position if needed)
                    internalLoadSingle(file, currentPos, series, forceNew);
                }

                if (frame != null)
                {
                    if (frame.isCancelRequested())
                        return new ArrayList<Sequence>();
                }

                // directory load fit in a single sequence ?
                if ((sequences.size() == 1) && directory)
                {
                    final Sequence seq = sequences.get(0);
                    // get directory without last separator
                    final String fileDir = FileUtil.getGenericPath(files[0].getParentFile().getAbsolutePath());

                    // set name and filename to use directory instead
                    seq.setName(FileUtil.getFileName(fileDir, false));
                    seq.setFilename(fileDir);
                }
            }
            catch (UnknownFormatException e)
            {
                System.err.println(filename + ": unknown or unsupported image format !");
                IcyExceptionHandler.showErrorMessage(e, true);

                if (frame != null)
                {
                    reportLociError("Unknow or unsupported image format",
                            "<html>The image '" + filename
                                    + "' is not recognized or not (yet) supported by BioFormat:<br>"
                                    + IcyExceptionHandler.getErrorMessage(e, false), filename);
                }
            }
            catch (MissingLibraryException e)
            {
                System.err.println("Error while loading image '" + filename + "' :");
                IcyExceptionHandler.showErrorMessage(e, true);
                if (frame != null)
                    new FailedAnnounceFrame("Failed to load image (see output console for detail)", 15);
            }
            catch (Exception e)
            {
                System.err.println("Error while loading image '" + filename + "' :");
                IcyExceptionHandler.showErrorMessage(e, true);
                if (frame != null)
                {
                    reportLociError("Unknow or unsupported image format",
                            "<html>The image '" + filename
                                    + "' is not recognized or not (yet) supported by BioFormat:<br>"
                                    + IcyExceptionHandler.getErrorMessage(e, false), filename);
                }
            }

            if (GeneralPreferences.getSequencePersistence())
            {
                // load sequence XML data
                for (Sequence seq : sequences)
                {
                    try
                    {
                        seq.loadXMLData();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Cannot load persistent data from sequence '" + seq.getName() + "' :");
                        IcyExceptionHandler.showErrorMessage(e, false);
                    }
                }
            }

            return sequences;
        }

        void reportLociError(final String title, final String message, final String filename)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final ErrorReportFrame errorFrame = new ErrorReportFrame(null, title, message);

                    errorFrame.setReportAction(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            IssueReporter reporter = new IssueReporter();
                            reporter.reportBug(filename, errorFrame.getReportMessage());
                        }
                    });
                }
            });
        }

        private Sequence createNewSequence(String path, int serie, boolean multiSerie)
        {
            // create a new sequence
            final Sequence result = new Sequence(MetaDataUtil.createOMEMetadata(
                    (IMetadata) lastReader.getMetadataStore(), serie));

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

        // do the image load
        private void internalLoadSingle(File file, BandPosition position, int[] series, boolean forceNewSequence)
                throws FormatException, IOException
        {
            final String path = file.getAbsolutePath();

            // use the same reader only when we want to concatenate images in same sequence
            if (forceNewSequence || (lastReader == null) || !lastReader.isThisType(path, true))
                lastReader = mainReader.getReader(path);

            // set current filename
            if (frame != null)
                frame.setFilename(path);

            // disable file grouping
            lastReader.setGroupFiles(false);
            // we want all metadata
            lastReader.setOriginalMetadataPopulated(true);
            // prepare meta data store structure
            lastReader.setMetadataStore(new OMEXMLMetadataImpl());
            // load file with LOCI library
            lastReader.setId(path);

            try
            {
                selectedSeries = null;

                // only if series has not yet be defined
                if (series == null)
                {
                    if ((lastReader.getSeriesCount() > 1) && !Icy.isHeadLess())
                    {
                        // use invokeNow carefully !
                        ThreadUtil.invokeNow(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                selectedSeries = new SeriesSelectionDialog(lastReader).getSelectedSeries();
                            }
                        });
                    }
                    else
                        selectedSeries = new int[] {0};
                }
                else
                    selectedSeries = series;

                // no selected serie --> exit
                if ((selectedSeries == null) || (selectedSeries.length == 0))
                    return;

                final boolean multiSerie = selectedSeries.length > 1;
                boolean firstSerie = true;

                for (int serieIndex : selectedSeries)
                {
                    final int numSequence = sequences.size();
                    Sequence seq;

                    lastReader.setSeries(serieIndex);

                    // first serie and already have sequence(s) --> concatenate to last one
                    if (firstSerie && (!forceNewSequence) && (numSequence > 0))
                        seq = sequences.get(numSequence - 1);
                    else
                    {
                        // create and add a new sequence
                        seq = createNewSequence(path, serieIndex, multiSerie);
                        sequences.add(seq);

                        // re init position
                        position.set(0, 0, 0);
                    }

                    final int frames = lastReader.getSizeT();
                    final int planes = lastReader.getSizeZ();

                    // set local length for loader frame
                    final int progressLen = frames * planes;

                    if (frame != null)
                    {
                        if (progressLen > 10)
                            frame.setLength(progressLen);
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
                                if (frame != null)
                                {
                                    // cancel requested ?
                                    if (frame.isCancelRequested())
                                        return;

                                    // notify progress to loader frame (only if sufficient image
                                    // loaded)
                                    if (progressLen > 10)
                                        frame.setPosition(progress++);
                                }

                                // no single image ? increment Z position
                                if (z > 0)
                                    position.setZ(position.getZ() + 1);

                                // get composed image
                                final IcyBufferedImage icyImage = IcyBufferedImage.createFrom(lastReader, z, t);

                                // image format is not compatible with this sequence ?
                                if (!seq.isCompatible(icyImage))
                                {
                                    // remove empty element on current sequence
                                    seq.packImageList();
                                    seq.endUpdate();

                                    // and add a new sequence
                                    seq = createNewSequence(path, serieIndex, multiSerie);
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
                lastReader.close();
            }
        }

        private void compact(List<FilePosition> positions)
        {
            FilePosition pos = positions.get(0);
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
            for (FilePosition fp : positions)
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
                    for (FilePosition fp : positions)
                        fp.set(fp.getC(), fp.getT(), 0);

                    sameT = false;
                    sameC = true;
                }
                // different Z and not fixed
                else if (!sameZ && zNotFixed)
                {
                    // move Z to T (Z becomes 0)
                    for (FilePosition fp : positions)
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
                    for (FilePosition fp : positions)
                        fp.set(fp.getT(), fp.getC(), 0);

                    sameZ = false;
                    sameC = true;
                }
            }

            // sort on position
            Collections.sort(positions);
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
        final String filePath = file.getAbsolutePath();
        final IFormatReader reader = getReader(filePath);

        // disable file grouping
        reader.setGroupFiles(false);
        // set file id
        reader.setId(filePath);
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
        final String filePath = file.getAbsolutePath();
        final IFormatReader reader = getReader(filePath);

        // disable file grouping
        reader.setGroupFiles(false);
        // set file id
        reader.setId(filePath);
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
     *        Series to load (for multi serie sequence), set to <code>null</code> for default.
     * @param separate
     *        Force image to be loaded in separate sequence.
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence[] loadSequences(File[] files, int[] series, boolean separate, boolean autoOrder,
            boolean showProgress)
    {
        // detect if this is a complete folder load
        final boolean directory = (files.length == 1) && files[0].isDirectory();
        // explode file list
        final File[] singleFiles = explodeAndClean(files);

        // load sequences and return them
        return loadSequences(singleFiles, series, separate, autoOrder, directory, false, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequences(File[], int[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(List<File> files, List<Integer> series, boolean separate,
            boolean autoOrder, boolean showProgress)
    {
        final int[] seriesArray = new int[series.size()];

        for (int i = 0; i < seriesArray.length; i++)
            seriesArray[i] = series.get(i).intValue();

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
     * Load a list of sequence from the specified multi serie image file returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     * 
     * @param file
     *        Image file to load.
     * @param series
     *        Series to load (for multi serie sequence), set to <code>null</code> for default.
     * @param showProgress
     *        Show progression of loading process.
     */
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
        final int[] seriesArray = new int[series.size()];

        for (int i = 0; i < seriesArray.length; i++)
            seriesArray[i] = series.get(i).intValue();

        return Arrays.asList(loadSequences(new File[] {file}, seriesArray, false, true, showProgress));
    }

    /**
     * @deprecated Use {@link #loadSequences(File, int[], boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series)
    {
        return loadSequences(CollectionUtil.createArrayList(file), series, false, true, true);
    }

    /**
     * @deprecated Use {@link #loadSequences(File, int[], boolean)} instead.
     */
    @Deprecated
    public static List<Sequence> loadSequences(File file, List<Integer> series, boolean display, boolean addToRecent)
    {
        return loadSequences(CollectionUtil.createArrayList(file), series, false, true, true);
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
    public static Sequence loadSequence(File[] files, int serie, boolean showProgress)
    {
        final Sequence[] result = loadSequences(files, new int[] {serie}, false, true, showProgress);

        if (result.length > 0)
            return result[0];

        return null;
    }

    /**
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files, boolean showProgress)
    {
        return loadSequence(files.toArray(new File[files.size()]), 0, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files)
    {
        return loadSequence(files, true);
    }

    /**
     * @deprecated Use {@link #loadSequence(File[], int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(List<File> files, boolean display, boolean addToRecent)
    {
        return loadSequence(files);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     * 
     * @param file
     *        Image file to load.
     * @param serie
     *        Serie index to load (for multi serie sequence), set to 0 if unsure (default).
     * @param showProgress
     *        Show progression of loading process.
     */
    public static Sequence loadSequence(File file, int serie, boolean showProgress)
    {
        return loadSequence(new File[] {file}, serie, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(File, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file, boolean showProgress)
    {
        return loadSequence(new File[] {file}, 0, showProgress);
    }

    /**
     * @deprecated Use {@link #loadSequence(File, int, boolean)} instead.
     */
    @Deprecated
    public static Sequence loadSequence(File file)
    {
        return loadSequence(new File[] {file}, 0, true);
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
     * @param autoOrder
     *        Try to order image in sequence from their filename
     * @param showProgress
     *        Show progression in loading process
     */
    public static void load(final File[] files, final boolean separate, final boolean autoOrder,
            final boolean showProgress)
    {
        // asynchronous call
        ThreadUtil.bgRunWait(new Runnable()
        {
            @Override
            public void run()
            {
                // detect if this is a complete folder load
                final boolean directory = (files.length == 1) && files[0].isDirectory();
                // explode file list
                final File[] singleFiles = explodeAndClean(files);

                // load sequence
                final Sequence[] sequences = loadSequences(singleFiles, null, separate, autoOrder, directory, true,
                        showProgress);
                // and display them
                for (Sequence seq : sequences)
                    Icy.getMainInterface().addSequence(seq);
            }
        });
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files)
    {
        load(files, false, true, true);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate)
    {
        load(files, separate, true, true);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate, boolean showProgress)
    {
        load(files, separate, true, showProgress);
    }

    /**
     * @deprecated Use {@link #load(File[], boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void load(List<File> files, boolean separate, boolean autoOrder, boolean showProgress)
    {
        load(files.toArray(new File[files.size()]), separate, autoOrder, showProgress);
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
        load(new File[] {file}, false, false, showProgress);
    }

    /**
     * @deprecated Use {@link #load(File, boolean)} instead.
     */
    @Deprecated
    public static void load(File file)
    {
        load(new File[] {file}, false, false, true);
    }

    /**
     * Loads the specified image files and return them as sequences.<br>
     * If 'separate' is false the loader try to set images in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
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
    static Sequence[] loadSequences(File[] files, int[] series, boolean separate, boolean autoOrder, boolean directory,
            boolean addToRecent, boolean showProgress)
    {
        final List<Sequence> result = new ArrayList<Sequence>();
        final ApplicationMenu mainMenu;
        final FileFrame loadingFrame;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress)
            loadingFrame = new FileFrame("Loading...", null);
        else
            loadingFrame = null;

        try
        {
            // loading
            if (separate)
            {
                for (File file : files)
                {
                    // add as separate item to recent file list
                    if (mainMenu != null)
                        mainMenu.addRecentLoadedFile(file);

                    // create sequence loader
                    final SequenceLoader loader = new SequenceLoader();
                    // then load sequence
                    final List<Sequence> sequences = loader.load(new File[] {file}, series, autoOrder, directory,
                            loadingFrame);
                    // and add to results
                    result.addAll(sequences);
                }
            }
            else
            {
                if (files.length > 0)
                {
                    // add as one item to recent file list
                    if (mainMenu != null)
                    {
                        // set only the directory entry
                        if (directory)
                            mainMenu.addRecentLoadedFile(files[0].getParentFile());
                        else
                            mainMenu.addRecentLoadedFile(files);
                    }

                    // create sequence loader
                    final SequenceLoader loader = new SequenceLoader();
                    // then load sequence
                    final List<Sequence> sequences = loader.load(files, series, autoOrder, directory, loadingFrame);
                    // and add to results
                    result.addAll(sequences);
                }
            }
        }
        finally
        {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result.toArray(new Sequence[result.size()]);
    }

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
