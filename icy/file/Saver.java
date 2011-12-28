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

import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.IcyBufferedImage;
import icy.image.colormodel.IcyColorModel;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.util.OMEUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.APNGWriter;
import loci.formats.out.AVIWriter;
import loci.formats.out.JPEGWriter;
import loci.formats.out.OMETiffWriter;

/**
 * Sequence / Image saver class.<br>
 * <br>
 * Supported save format are the following : TIFF (preferred), PNG, JPG and AVI.
 * When sequence is saved as multiple file the following naming convention is used :<br>
 * <code>filename-tttt-zzzz</code>
 * 
 * @author Stephane & Fab
 */
public class Saver
{
    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, int, int, DataType, boolean)}
     *             instead
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType) throws ServiceException
    {
        return OMEUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, int, int, DataType, boolean)}
     *             instead
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT, int dataType,
            boolean signedDataType) throws ServiceException
    {
        return OMEUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT,
                DataType.getDataType(dataType, signedDataType), false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType)
            throws ServiceException
    {
        return OMEUtil.generateMetaData(sizeX, sizeY, sizeC, 1, 1, dataType, false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int dataType, boolean signedDataType)
            throws ServiceException
    {
        return OMEUtil.generateMetaData(sizeX, sizeY, sizeC, DataType.getDataType(dataType, signedDataType), false);
    }

    /**
     * Return the writer to use for the specified FileFormat.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     */
    public static IFormatWriter getWriter(FileFormat fileFormat)
    {
        switch (fileFormat)
        {
            case PNG:
                return new APNGWriter();

            case JPG:
                return new JPEGWriter();

            case AVI:
                return new AVIWriter();

            default:
                return new OMETiffWriter();
        }
    }

    /**
     * Return the writer to use for the specified filename extension.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     */
    public static IFormatWriter getWriter(String ext)
    {
        return getWriter(FileFormat.getFileFormat(ext));
    }

    /**
     * Return the writer to use for the specified file.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     */
    public static IFormatWriter getWriter(File file)
    {
        return getWriter(FileUtil.getFileExtension(file.getName(), false));
    }

    /**
     * Return the closest compatible {@link IcyColorModel} supported by writer
     * from the specified image description.<br>
     * That means the writer is able to save the data described by the returned
     * {@link IcyColorModel} without any loss or conversion.<br>
     * 
     * @param writer
     *        IFormatWriter we want to test compatibility
     * @param numChannel
     *        number of channel of the image
     * @param dataType
     *        image data type
     */
    public static IcyColorModel getCompatibleColorModel(IFormatWriter writer, int numChannel, DataType dataType)
    {
        final DataType outDataType;
        final int outNumChannel;

        if (writer instanceof OMETiffWriter)
        {
            // TIFF supports all formats
            outDataType = dataType;
            outNumChannel = numChannel;
        }
        else if (writer instanceof APNGWriter)
        {
            // PNG only supports byte and short data type
            if (dataType.getSize() > 2)
                outDataType = DataType.USHORT;
            else
                outDataType = dataType;

            // PNG supports a maximum of 4 channels
            outNumChannel = Math.min(numChannel, 4);
        }
        else
        {
            // JPG, AVI, default only supports byte data type
            if (dataType.getSize() > 1)
                outDataType = DataType.UBYTE;
            else
                outDataType = dataType;

            // 3 channels at max
            if (numChannel > 3)
                outNumChannel = 3;
            else
            {
                // special case of 2 channels
                if (numChannel == 2)
                    // convert to RGB
                    outNumChannel = 3;
                else
                    outNumChannel = numChannel;
            }
        }

        return IcyColorModel.createInstance(outNumChannel, outDataType);
    }

    /**
     * Return the closest compatible {@link IcyColorModel} supported by writer
     * from the specified {@link IcyColorModel}.<br>
     * That means the writer is able to save the data described by the returned
     * {@link IcyColorModel} without any loss or conversion.<br>
     * 
     * @param writer
     *        IFormatWriter we want to test compatibility
     * @param colorModel
     *        the colorModel describing data / image format
     */
    public static IcyColorModel getCompatibleColorModel(IFormatWriter writer, IcyColorModel colorModel)
    {
        return getCompatibleColorModel(writer, colorModel.getNumComponents(), colorModel.getDataType_());
    }

    /**
     * Return true if the specified writer is compatible with the image description.<br>
     * That means the writer is able to save the data without any loss or conversion.<br>
     * 
     * @param numChannel
     *        number of channel of the image
     * @param alpha
     *        true if the image has an alpha channel
     * @param dataType
     *        image data type
     */
    public static boolean isCompatible(IFormatWriter writer, int numChannel, boolean alpha, DataType dataType)
    {
        return isCompatible(writer, IcyColorModel.createInstance(numChannel, dataType));
    }

    /**
     * Return true if the specified writer is compatible with the specified {@link IcyColorModel}.<br>
     * That means the writer is able to save the data described by the colorModel without any loss
     * or conversion.<br>
     * The color map data are never preserved, they are always restored to their default.<br>
     */
    public static boolean isCompatible(IFormatWriter writer, IcyColorModel colorModel)
    {
        return colorModel.isCompatible(getCompatibleColorModel(writer, colorModel));
    }

    /**
     * Return the separate channel flag from specified writer and color space
     */
    private static boolean getSeparateChannelFlag(IFormatWriter writer, int numChannel, DataType dataType)
    {
        if (writer instanceof OMETiffWriter)
            return (numChannel == 2) || (numChannel > 4) || (dataType.getSize() > 1);

        return false;
    }

    /**
     * Return the separate channel flag from specified writer and color space
     */
    private static boolean getSeparateChannelFlag(IFormatWriter writer, IcyColorModel colorModel)
    {
        return getSeparateChannelFlag(writer, colorModel.getNumComponents(), colorModel.getDataType_());
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * If sequence contains severals images then file is used as a directory<br>
     * to store all single images.
     */
    public static void save(Sequence sequence, File file)
    {
        save(sequence, file, (sequence.getSizeZ() * sequence.getSizeT()) > 1);
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multiFile flag is used to indicate<br>
     * if images are saved in severals files (file then specify a directory) or in a single file.
     */
    public static void save(Sequence sequence, File file, boolean multipleFile)
    {
        save(sequence, file, 0, sequence.getSizeZ() - 1, 0, sequence.getSizeT() - 1, 15, multipleFile);
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multipleFile flag is used to indicate<br>
     * if images are saved as separate files (file then specify a directory) or not.<br>
     * zMin - zMax and tMin - tMax define the Z and T images range to save.<br>
     * 
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence
     * @param zMin
     *        start Z position to save
     * @param zMax
     *        end Z position to save
     * @param tMin
     *        start T position to save
     * @param tMax
     *        end T position to save
     * @param fps
     *        frame rate for AVI sequence save
     * @param multipleFile
     *        flag to indicate if images are saved in separate file
     */
    public static void save(Sequence sequence, File file, int zMin, int zMax, int tMin, int tMax, int fps,
            boolean multipleFile)
    {
        final String filePath = file.getAbsolutePath();
        final int sizeT = (tMax - tMin) + 1;
        final int sizeZ = (zMax - zMin) + 1;

        final FileFrame saveFrame = new FileFrame("Saving", file.getAbsolutePath());
        final ApplicationMenu mainMenu = Icy.getMainInterface().getApplicationMenu();

        try
        {
            saveFrame.setLength(sizeT * sizeZ);
            saveFrame.setPosition(0);

            if (multipleFile)
            {
                // so we won't create it for each image
                final IFormatWriter writer = getWriter(file);

                // save as severals images
                final DecimalFormat decimalFormat = new DecimalFormat("0000");
                final String fileName = FileUtil.getFileName(filePath, false);
                final String fileBaseDirectory = FileUtil.getDirectory(filePath) + fileName;
                final String filePathWithoutExt = fileBaseDirectory + FileUtil.separator + fileName;
                final String fileDotExt = FileUtil.getFileExtension(filePath, true);

                // create output directory
                FileUtil.createDir(fileBaseDirectory);

                for (int t = tMin; t <= tMax; t++)
                {
                    for (int z = zMin; z <= zMax; z++)
                    {
                        final String filename = filePathWithoutExt + "_t" + decimalFormat.format(t) + "_z"
                                + decimalFormat.format(z) + fileDotExt;

                        // save as single image file
                        save(writer, sequence, filename, z, z, t, t, fps, saveFrame);
                    }
                }

                // change sequence name
                sequence.setName(fileName);
                sequence.setFilename(fileBaseDirectory);

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentLoadedFile(new File(fileBaseDirectory));
            }
            else
            {
                // save as multi images file
                save(null, sequence, filePath, zMin, zMax, tMin, tMax, fps, saveFrame);
                
                // change sequence name
                sequence.setName(FileUtil.getFileName(filePath, false));
                sequence.setFilename(filePath);

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentLoadedFile(file);
            }

        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            new FailedAnnounceFrame("Failed to save images (see output console for detail)", 15);
            return;
        }
        finally
        {
            saveFrame.close();
        }
    }

    /**
     * Save a single image from bytes buffer to the specified file.
     */
    public static void saveImage(byte[] data, int width, int height, int numChannel, DataType dataType, File file,
            boolean force) throws FormatException, IOException
    {
        final IFormatWriter writer = getWriter(file);

        if (file.exists())
        {
            // forced ? first delete the file else LOCI won't save it
            if (force)
                file.delete();
            else
                throw new IOException("File already exist");
        }

        try
        {
            writer.setMetadataRetrieve(OMEUtil.generateMetaData(width, height, numChannel, dataType,
                    getSeparateChannelFlag(writer, numChannel, dataType)));
        }
        catch (ServiceException e)
        {
            System.err.println("Saver.saveImage(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // needed so some image viewer can correctly read image (win XP system viewer need it)
        writer.setInterleaved(true);
        writer.setId(file.getAbsolutePath());
        writer.setSeries(0);
        try
        {
            writer.saveBytes(0, data);
        }
        catch (Exception e)
        {
            System.err.println("Saver.saveImage(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }
        writer.close();
    }

    /**
     * @deprecated uses {@link #saveImage(byte[], int, int, int, DataType, File, boolean)} instead
     */
    @Deprecated
    public static void saveImage(byte[] data, int width, int height, int numChannel, int dataType,
            boolean signedDataType, File file, boolean force) throws FormatException, IOException
    {
        saveImage(data, width, height, numChannel, DataType.getDataType(dataType, signedDataType), file, force);
    }

    /**
     * Save a single image to the specified file
     * 
     * @param image
     * @throws IOException
     * @throws FormatException
     */
    public static void saveImage(IcyBufferedImage image, File file, boolean force) throws FormatException, IOException
    {
        final IFormatWriter writer = getWriter(file);

        if (file.exists())
        {
            // forced ? first delete the file else LOCI won't save it
            if (force)
                file.delete();
            else
                throw new IOException("File already exist");
        }

        try
        {
            writer.setMetadataRetrieve(OMEUtil.generateMetaData(image,
                    getSeparateChannelFlag(writer, image.getIcyColorModel())));
        }
        catch (ServiceException e)
        {
            System.err.println("Saver.saveImage(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        final boolean interleaved = true;
        // get endianess
        final boolean littleEndian = !writer.getMetadataRetrieve().getPixelsBinDataBigEndian(0, 0).booleanValue();

        // needed so some image viewer can correctly read image (win XP system viewer need it)
        writer.setInterleaved(interleaved);
        writer.setId(file.getAbsolutePath());
        writer.setSeries(0);
        try
        {
            writer.saveBytes(0, image.getRawData(littleEndian, interleaved));
        }
        catch (Exception e)
        {
            System.err.println("Saver.saveBytes(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }
        writer.close();
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multipleFile flag is used to indicate<br>
     * if images are saved as separate files (file then specify a directory) or not.<br>
     * zMin - zMax and tMin - tMax define the Z and T images range to save.<br>
     * 
     * @param formatWriter
     *        writer used to save sequence
     * @param sequence
     *        sequence to save
     * @param filename
     *        file name where we want to save sequence
     * @param zMin
     *        start Z position to save
     * @param zMax
     *        end Z position to save
     * @param tMin
     *        start T position to save
     * @param tMax
     *        end T position to save
     * @param fps
     *        frame rate for AVI sequence save
     * @param multipleFile
     *        flag to indicate if images are saved in separate file
     */
    private static void save(IFormatWriter formatWriter, Sequence sequence, String filename, int zMin, int zMax,
            int tMin, int tMax, int fps, FileFrame saveFrame)
    {
        final File file = new File(filename);
        final IFormatWriter writer;

        if (formatWriter == null)
            writer = getWriter(file);
        // writer = new BufferedImageWriter();
        else
            writer = formatWriter;

        try
        {
            // forced ? first delete the file else LOCI won't save it
            if (file.exists())
                file.delete();

            final int sizeC = sequence.getSizeC();

            // Some image viewer needs interleaved channel data to correctly read image.
            // win XP system viewer for instance
            final boolean interleaved = true;
            final boolean separateChannel = getSeparateChannelFlag(writer, sequence.getColorModel());

            // set settings
            writer.setFramesPerSecond(fps);
            // generate metadata
            writer.setMetadataRetrieve(OMEUtil.generateMetaData(sequence, (zMax - zMin) + 1, (tMax - tMin) + 1,
                    separateChannel));
            // interleaved flag
            writer.setInterleaved(interleaved);
            // set id
            writer.setId(filename);
            // init
            writer.setSeries(0);
            // usually give better save performance
            writer.setWriteSequentially(true);

            // get endianess
            final boolean littleEndian = !writer.getMetadataRetrieve().getPixelsBinDataBigEndian(0, 0).booleanValue();

            try
            {
                int imageIndex = 0;
                // XYCZT order is important here (see metadata)
                for (int t = tMin; t <= tMax; t++)
                {
                    for (int z = zMin; z <= zMax; z++)
                    {
                        if ((saveFrame != null) && saveFrame.isCancelRequested())
                            return;

                        final IcyBufferedImage image = sequence.getImage(t, z);

                        // separated channel data
                        if (separateChannel)
                        {
                            for (int c = 0; c < sizeC; c++)
                            {
                                if (image != null)
                                    writer.saveBytes(imageIndex, image.getRawData(c, littleEndian));

                                imageIndex++;
                            }
                        }
                        else
                        {
                            if (image != null)
                                writer.saveBytes(imageIndex, image.getRawData(littleEndian, interleaved));
                            // ((BufferedImageWriter) writer).saveImage(imageIndex, image);

                            imageIndex++;
                        }

                        if (saveFrame != null)
                            saveFrame.incPosition();
                    }
                }
            }
            finally
            {
                // always close writer
                writer.close();
            }
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            new FailedAnnounceFrame("Failed to save image (see output console for detail)", 15);
            return;
        }
    }
}
