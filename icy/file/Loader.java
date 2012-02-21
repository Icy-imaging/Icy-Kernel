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

import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.BandPosition;
import icy.image.IcyBufferedImage;
import icy.image.ImagePosition;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
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

        public SequenceLoader(List<File> files, boolean display, boolean directory)
        {
            super();

            this.files = files;

            loaderFrame = new FileFrame("Loading", null);
            sequences = new ArrayList<Sequence>();
            mainReader = new ImageReader();
            lastUsedReader = null;
            this.display = display;
            this.directory = directory;
        }

        @Override
        public void run()
        {
            if (files.size() == 0)
            {
                loaderFrame.close();
                new AnnounceFrame("No image to load", 10);
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
                loaderFrame.notifyProgress(0, len);

                // load first image (and update position if needed)
                load(filePositions.get(0).file, newPos);

                // fix positions while loading images
                for (int index = 1; index < len; index++)
                {
                    if (loaderFrame.isCancelRequested())
                        return;

                    // notify progress to loader frame
                    loaderFrame.notifyProgress(index, len);

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

                        // create a new sequence for this component
                        final Sequence seq = new Sequence();
                        seq.setName(FileUtil.getFileName(filename, false) + " Channel " + newPos.getC());
                        seq.setFilename(FileUtil.setExtension(
                                FileUtil.setExtension(filename, "") + "_C" + newPos.getC(),
                                FileUtil.getFileExtension(filename, true)));
                        seq.setMetaData((IMetadata) lastUsedReader.getMetadataStore());
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

                if (loaderFrame.isCancelRequested())
                    return;

                // directory loading resulted in a single sequence
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
                        Icy.addSequence(seq);
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

            final int series = reader.getSeriesCount();
            final int seqSize = sequences.size();
            Sequence seq;

            // already have sequence(s) ? get the last one
            if (seqSize > 0)
                seq = sequences.get(seqSize - 1);
            else
            {
                // create and add the first sequence
                seq = new Sequence();
                seq.setName(FileUtil.getFileName(path, false));
                seq.setFilename(path);
                seq.setMetaData((IMetadata) reader.getMetadataStore());
                sequences.add(seq);
            }

            int progress = 0;
            seq.beginUpdate();
            try
            {
                for (int s = 0; s < series; s++)
                {
                    reader.setSeries(s);

                    final int frames = reader.getSizeT();
                    final int planes = reader.getSizeZ();

                    // set local length for loader frame
                    final int progressLen = series * frames * planes;
                    if (progressLen > 10)
                        loaderFrame.setLength(progressLen);

                    // no single image -> create new sequence 
                    if (s > 0)
                    {
                        // remove empty element on current sequence
                        seq.packImageList();
                        seq.endUpdate();

                        // and add a new sequence
                        seq = new Sequence();
                        seq.setName(FileUtil.getFileName(file.getName(), false));
                        seq.setFilename(path);
                        seq.setMetaData((IMetadata) reader.getMetadataStore());
                        sequences.add(seq);
                        seq.beginUpdate();

                        // re init position
                        position.set(0, 0, 0);                        
//                        position.setT(position.getT() + 1);
//                        position.setZ(0);
                    }

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
                            // cancel requested ?
                            if (loaderFrame.isCancelRequested())
                                return;

                            // notify progress to loader frame (only if sufficient image loaded)
                            if (progressLen > 10)
                                loaderFrame.setPosition(progress++);

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
                                seq = new Sequence();
                                seq.setName(FileUtil.getFileName(file.getName(), false));
                                seq.setFilename(path);
                                seq.setMetaData((IMetadata) reader.getMetadataStore());
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
            }
            finally
            {
                seq.endUpdate();
                reader.close();
            }
        }
    }

    /**
     * Load a single image from the specified file
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
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT.
     * 
     * @param file
     * @return Sequence
     */
    public static Sequence loadSequence(File file)
    {
        final ApplicationMenu mainMenu = Icy.getMainInterface().getApplicationMenu();

        // add as one item to recent file list
        if (mainMenu != null)
            mainMenu.addRecentLoadedFile(file);

        // create sequence loader
        final SequenceLoader seqLoader = new SequenceLoader(CollectionUtil.createArrayList(file), false,
                file.isDirectory());

        // run sequence loader
        seqLoader.run();

        // return first sequence if it exists
        if (seqLoader.sequences.size() > 0)
            return seqLoader.sequences.get(0);

        return null;
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
        load(CollectionUtil.createArrayList(file), false);
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
        load(files, false);
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
        // explode file list
        internalLoad(explodeAndClean(files), separate, (files.size() == 1) && files.get(0).isDirectory());
    }

    private static List<File> explodeAndClean(List<File> files)
    {
        final List<File> result = FileUtil.explode(files, true, false);

        // extensions based exclusion
        for (int i = result.size() - 1; i >= 0; i--)
        {
            final File file = result.get(i);
            final String path = file.getPath();
            final String ext = FileUtil.getFileExtension(path, false).toLowerCase();

            // removes typical extension we can find mixed with image
            if (StringUtil.equals(ext, "xml") || StringUtil.equals(ext, "txt") || StringUtil.equals(ext, "pdf")
                    || StringUtil.equals(ext, "xls") || StringUtil.equals(ext, "doc") || StringUtil.equals(ext, "doc")
                    || StringUtil.equals(ext, "docx"))
                result.remove(i);
        }

        return result;
    }

    private static void internalLoad(final List<File> files, final boolean separate, final boolean directory)
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
                                true, directory);
                        // load file using background processor
                        ThreadUtil.bgRunWait(loadingThread);
                    }
                }
                else
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
                    new SequenceLoader(files, true, directory).run();
                }
            }
        });
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
