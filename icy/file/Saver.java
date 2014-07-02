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

import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.IcyBufferedImage;
import icy.image.colormodel.IcyColorModel;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.util.OMEUtil;
import icy.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.UnknownFormatException;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.APNGWriter;
import loci.formats.out.AVIWriter;
import loci.formats.out.JPEG2000Writer;
import loci.formats.out.JPEGWriter;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.TiffWriter;

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
     * Returns the {@link ImageFileFormat} corresponding to specified {@link IFormatWriter}.<br>
     * <code>defaultValue</code> is returned if no matching format is found.
     */
    public static ImageFileFormat getImageFileFormat(IFormatWriter writer, ImageFileFormat defaultValue)
    {
        if (writer instanceof TiffWriter)
            return ImageFileFormat.TIFF;
        if (writer instanceof APNGWriter)
            return ImageFileFormat.PNG;
        if (writer instanceof JPEGWriter)
            return ImageFileFormat.JPG;
        if (writer instanceof JPEG2000Writer)
            return ImageFileFormat.JPG;
        if (writer instanceof AVIWriter)
            return ImageFileFormat.AVI;

        return defaultValue;
    }

    /**
     * @deprecated Use {@link #getImageFileFormat(IFormatWriter, ImageFileFormat)} instead.
     */
    @Deprecated
    public static FileFormat getFileFormat(IFormatWriter writer, FileFormat defaultValue)
    {
        return getImageFileFormat(writer, ImageFileFormat.getFormat(defaultValue)).toFileFormat();
    }

    /**
     * Return the writer to use for the specified ImageFileFormat.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     * 
     * @param format
     *        {@link ImageFileFormat} we want to retrieve the saver.<br>
     *        Accepted values:<br>
     *        {@link ImageFileFormat#TIFF}<br>
     *        {@link ImageFileFormat#PNG}<br>
     *        {@link ImageFileFormat#JPG}<br>
     *        {@link ImageFileFormat#AVI}<br>
     *        null
     */
    public static IFormatWriter getWriter(ImageFileFormat format)
    {
        final IFormatWriter result;

        switch (format)
        {
            case PNG:
                result = new APNGWriter();
                break;

            case JPG:
                result = new JPEGWriter();
                break;

            case AVI:
                result = new AVIWriter();
                break;

            default:
                result = new OMETiffWriter();
                // this way we are sure the TIF saver is always compressing
                try
                {
                    result.setCompression("LZW");
                }
                catch (FormatException e)
                {
                    // no compression
                }
                break;
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getWriter(ImageFileFormat)} instead.
     */
    @Deprecated
    public static IFormatWriter getWriter(FileFormat fileFormat)
    {
        return getWriter(ImageFileFormat.getFormat(fileFormat));
    }

    /**
     * Return the writer to use for the specified filename extension.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     * 
     * @param ext
     *        Extension we want to retrieve the corresponding image writer.
     * @param defaultFormat
     *        default {@link ImageFileFormat} to use if <code>ext</code> is not recognized.<br>
     *        Accepted values:<br>
     *        {@link ImageFileFormat#TIFF}<br>
     *        {@link ImageFileFormat#PNG}<br>
     *        {@link ImageFileFormat#JPG}<br>
     *        {@link ImageFileFormat#AVI}<br>
     *        null
     */
    public static IFormatWriter getWriter(String ext, ImageFileFormat defaultFormat)
    {
        return getWriter(ImageFileFormat.getWriteFormat(ext, defaultFormat));
    }

    /**
     * @deprecated Use {@link #getWriter(String, ImageFileFormat)} instead.
     */
    @Deprecated
    public static IFormatWriter getWriter(String ext, FileFormat defaultFormat)
    {
        return getWriter(ext, ImageFileFormat.getFormat(defaultFormat));
    }

    /**
     * @deprecated Use {@link #getWriter(String, FileFormat)} instead.
     */
    @Deprecated
    public static IFormatWriter getWriter(String ext)
    {
        return getWriter(ext, ImageFileFormat.TIFF);
    }

    /**
     * Return the writer to use for the specified file.<br>
     * <br>
     * The following writer are currently supported :<br>
     * <code>OMETiffWriter</code> : TIFF image file (default)<br>
     * <code>APNGWriter</code> : PNG image file<br>
     * <code>JPEGWriter</code> : JPG image file<br>
     * <code>AVIWriter</code> : AVI video file<br>
     * 
     * @param file
     *        File we want to retrieve the corresponding image writer.
     * @param defaultFormat
     *        default {@link ImageFileFormat} to use if <code>file</code> is not recognized.<br>
     *        Accepted values:<br>
     *        {@link ImageFileFormat#TIFF}<br>
     *        {@link ImageFileFormat#PNG}<br>
     *        {@link ImageFileFormat#JPG}<br>
     *        {@link ImageFileFormat#AVI}<br>
     *        null
     */
    public static IFormatWriter getWriter(File file, ImageFileFormat defaultFormat)
    {
        return getWriter(FileUtil.getFileExtension(file.getName(), false), defaultFormat);
    }

    /**
     * @deprecated Use {@link #getWriter(File, ImageFileFormat)} instead.
     */
    @Deprecated
    public static IFormatWriter getWriter(File file, FileFormat defaultFormat)
    {
        return getWriter(file, ImageFileFormat.getFormat(defaultFormat));
    }

    /**
     * @deprecated Use {@link #getWriter(File, FileFormat)} instead.
     */
    @Deprecated
    public static IFormatWriter getWriter(File file)
    {
        return getWriter(file, ImageFileFormat.TIFF);
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
     * 
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence
     */
    public static void save(Sequence sequence, File file)
    {
        save(sequence, file, 0, sequence.getSizeZ() - 1, 0, sequence.getSizeT() - 1, 15,
                (sequence.getSizeZ() * sequence.getSizeT()) > 1, true);
    }

    /**
     * @deprecated Use {@link #save(Sequence, File, boolean, boolean)} instead.
     */
    @Deprecated
    public static void save(Sequence sequence, File file, boolean multipleFiles)
    {
        save(sequence, file, 0, sequence.getSizeZ() - 1, 0, sequence.getSizeT() - 1, 15, multipleFiles, true);
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multiFile flag is used to indicate<br>
     * if images are saved in severals files (file then specify a directory) or in a single file.
     * 
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence
     * @param multipleFiles
     *        flag to indicate if images are saved in separate file
     * @param showProgress
     *        show progress bar
     */
    public static void save(Sequence sequence, File file, boolean multipleFiles, boolean showProgress)
    {
        save(sequence, file, 0, sequence.getSizeZ() - 1, 0, sequence.getSizeT() - 1, 15, multipleFiles, showProgress);
    }

    /**
     * @deprecated Use {@link #save(Sequence, File, int, int, int, int, int, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static void save(Sequence sequence, File file, int zMin, int zMax, int tMin, int tMax, int fps,
            boolean multipleFiles)
    {
        save(sequence, file, zMin, zMax, tMin, tMax, fps, multipleFiles, true);
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
     * @param showProgress
     *        show progress bar
     */
    public static void save(Sequence sequence, File file, int zMin, int zMax, int tMin, int tMax, int fps,
            boolean multipleFile, boolean showProgress)
    {
        save(null, sequence, file, zMin, zMax, tMin, tMax, fps, multipleFile, showProgress, true);
    }

    /**
     * @deprecated Use
     *             {@link #save(IFormatWriter, Sequence, File, int, int, int, int, int, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static void save(IFormatWriter formatWriter, Sequence sequence, File file, int zMin, int zMax, int tMin,
            int tMax, int fps, boolean multipleFile, boolean showProgress)
    {
        save(formatWriter, sequence, file, zMin, zMax, tMin, tMax, fps, multipleFile, showProgress, true);
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multipleFile flag is used to indicate
     * if images are saved as separate files (file then specify a directory) or not.<br>
     * <code>zMin</code> - <code>zMax</code> and <code>tMin</code> - <code>tMax</code> define the Z
     * and T images range to save.<br>
     * 
     * @param formatWriter
     *        writer used to save sequence (define the image format).<br>
     *        If set to <code>null</code> then writer is determined from the file extension.<br>
     *        If destination file does not have a valid extension (for folder for instance) then you
     *        have to specify a valid Writer to write the image file (see
     *        {@link #getWriter(ImageFileFormat)})
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence.<br>
     *        Depending the <code>formatWriter</code> the file extension may be modified.<br>
     *        That is preferred as saving an image with a wrong extension may result in error on
     *        future read (wrong reader detection).
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
     * @param showProgress
     *        show progress bar
     * @param addToRecent
     *        add the saved sequence to recent opened sequence list
     */
    public static void save(IFormatWriter formatWriter, Sequence sequence, File file, int zMin, int zMax, int tMin,
            int tMax, int fps, boolean multipleFile, boolean showProgress, boolean addToRecent)
    {
        final String filePath = FileUtil.getGenericPath(file.getAbsolutePath());
        final int sizeT = (tMax - tMin) + 1;
        final int sizeZ = (zMax - zMin) + 1;
        final int numImages = sizeT * sizeZ;
        final FileFrame saveFrame;
        final ApplicationMenu mainMenu;

        if (addToRecent)
            mainMenu = Icy.getMainInterface().getApplicationMenu();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            saveFrame = new FileFrame("Saving", filePath);
        else
            saveFrame = null;
        try
        {
            if (saveFrame != null)
            {
                saveFrame.setLength(numImages);
                saveFrame.setPosition(0);
            }

            // need multiple files ?
            if ((numImages > 1) && multipleFile)
            {
                final IFormatWriter writer;

                // so we won't create it for each image
                if (formatWriter == null)
                    writer = getWriter(file, ImageFileFormat.TIFF);
                else
                    writer = formatWriter;

                if (writer == null)
                    throw new UnknownFormatException("Can't find a valid image writer for the specified file: " + file);

                // save as severals images
                final DecimalFormat decimalFormat = new DecimalFormat("0000");
                final String fileName = FileUtil.getFileName(filePath, false);
                String fileExt = FileUtil.getFileExtension(filePath, true);

                String fileBaseDirectory = FileUtil.getDirectory(filePath);
                if (fileBaseDirectory.endsWith("/"))
                    fileBaseDirectory = fileBaseDirectory.substring(0, fileBaseDirectory.length() - 1);

                // no extension (directory) ?
                if (StringUtil.isEmpty(fileExt))
                {
                    // filename is part of directory
                    fileBaseDirectory += FileUtil.separator + fileName;
                    // use the default file extension for the specified writer
                    fileExt = "." + getImageFileFormat(writer, ImageFileFormat.TIFF).getExtensions()[0];
                }

                final String filePathWithoutExt = fileBaseDirectory + FileUtil.separator + fileName;

                // create output directory
                FileUtil.createDir(fileBaseDirectory);

                // default name used --> use filename
                if (sequence.isDefaultName())
                    sequence.setName(fileName);
                sequence.setFilename(fileBaseDirectory);

                for (int t = tMin; t <= tMax; t++)
                {
                    for (int z = zMin; z <= zMax; z++)
                    {
                        String filename = filePathWithoutExt;

                        if ((tMax - tMin) > 0)
                            filename += "_t" + decimalFormat.format(t);
                        if ((zMax - zMin) > 0)
                            filename += "_z" + decimalFormat.format(z);
                        filename += fileExt;

                        // save as single image file
                        save(writer, sequence, filename, z, z, t, t, fps, saveFrame);
                    }
                }

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fileBaseDirectory);
            }
            else
            {
                final String fileExt = FileUtil.getFileExtension(filePath, false);
                final ImageFileFormat iff;

                if (formatWriter != null)
                    iff = getImageFileFormat(formatWriter, ImageFileFormat.TIFF);
                else
                    iff = ImageFileFormat.getWriteFormat(fileExt, ImageFileFormat.TIFF);

                // force to set correct file extension
                final String fixedFilePath;

                if (iff.matches(fileExt))
                    fixedFilePath = filePath;
                else
                    fixedFilePath = filePath + "." + iff.getExtensions()[0];

                // default name used --> use filename
                if (sequence.isDefaultName())
                    sequence.setName(FileUtil.getFileName(fixedFilePath, false));
                sequence.setFilename(fixedFilePath);

                // save into a single file
                save(formatWriter, sequence, fixedFilePath, zMin, zMax, tMin, tMax, fps, saveFrame);

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fixedFilePath);
            }

            // Sequence persistence enabled --> save XML
            if (GeneralPreferences.getSequencePersistence())
                sequence.saveXMLData();
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            if (showProgress && !Icy.getMainInterface().isHeadLess())
                new FailedAnnounceFrame("Failed to save image(s) (see output console for details)", 15);
            return;
        }
        finally
        {
            if (saveFrame != null)
                saveFrame.close();
        }
    }

    /**
     * Save a single image from bytes buffer to the specified file.
     */
    private static void saveImage(IFormatWriter formatWriter, byte[] data, int width, int height, int numChannel,
            DataType dataType, File file, boolean force) throws FormatException, IOException
    {
        if (file.exists())
        {
            // forced ? first delete the file else LOCI won't save it
            if (force)
                file.delete();
            else
                throw new IOException("File already exists");
        }
        // ensure parent directory exist
        FileUtil.ensureParentDirExist(file);

        final IFormatWriter writer;

        if (formatWriter == null)
        {
            // get the writer
            writer = getWriter(file, ImageFileFormat.TIFF);

            // prepare the metadata
            try
            {
                writer.setMetadataRetrieve(MetaDataUtil.generateMetaData(width, height, numChannel, dataType,
                        getSeparateChannelFlag(writer, numChannel, dataType)));
            }
            catch (ServiceException e)
            {
                System.err.println("Saver.saveImage(...) error :");
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
        else
            // ready to use writer (metadata already prepared)
            writer = formatWriter;

        // we always save in interleaved as some image viewer need it to correctly read image
        // (ex: win XP system viewer)
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
     * Save a single image from bytes buffer to the specified file.
     */
    public static void saveImage(byte[] data, int width, int height, int numChannel, DataType dataType, File file,
            boolean force) throws FormatException, IOException
    {
        saveImage(null, data, width, height, numChannel, dataType, file, force);
    }

    /**
     * @deprecated Use {@link #saveImage(byte[], int, int, int, DataType, File, boolean)} instead
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
        final IFormatWriter writer = getWriter(file, ImageFileFormat.TIFF);

        if (writer == null)
            throw new UnknownFormatException("Can't find a valid image writer for the specified file: " + file);

        try
        {
            writer.setMetadataRetrieve(MetaDataUtil.generateMetaData(image,
                    getSeparateChannelFlag(writer, image.getIcyColorModel())));
        }
        catch (ServiceException e)
        {
            System.err.println("Saver.saveImage(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // get byte order
        final boolean littleEndian = !writer.getMetadataRetrieve().getPixelsBinDataBigEndian(0, 0).booleanValue();
        // then save the image (always use interleaved data to save)
        saveImage(writer, image.getRawData(littleEndian, true), image.getSizeX(), image.getSizeY(), image.getSizeC(),
                image.getDataType_(), file, force);
    }

    /**
     * Save the specified sequence in the specified file.<br>
     * When the sequence contains severals image the multipleFile flag is used to indicate<br>
     * if images are saved as separate files (file then specify a directory) or not.<br>
     * zMin - zMax and tMin - tMax define the Z and T images range to save.<br>
     * 
     * @param formatWriter
     *        writer used to save sequence (define the image format)
     * @param sequence
     *        sequence to save
     * @param filePath
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
     * @param saveFrame
     *        progress frame for save operation (can be null)
     * @throws ServiceException
     * @throws IOException
     * @throws FormatException
     */
    private static void save(IFormatWriter formatWriter, Sequence sequence, String filePath, int zMin, int zMax,
            int tMin, int tMax, int fps, FileFrame saveFrame) throws ServiceException, FormatException, IOException
    {
        final File file = new File(filePath);
        final IFormatWriter writer;

        if (formatWriter == null)
            writer = getWriter(file, ImageFileFormat.TIFF);
        else
            writer = formatWriter;

        // TODO: temporary fix for the "incorrect close operation" bug in Bio-Formats
        // with OME TIF writer, remove it when fixed.
        // {
        // try
        // {
        // writer = formatWriter.getClass().newInstance();
        // }
        // catch (Exception e)
        // {
        // throw new ServiceException("Can't create new writer instance: " + e);
        // }
        // }

        if (writer == null)
            throw new UnknownFormatException("Can't find a valid image writer for the specified file: " + filePath);

        // first delete the file else LOCI won't save it correctly
        if (file.exists())
            file.delete();
        // ensure parent directory exist
        FileUtil.ensureParentDirExist(file);

        final int sizeC = sequence.getSizeC();

        // Some image viewer needs interleaved channel data to correctly read image.
        // win XP system viewer for instance
        final boolean interleaved = true;
        final boolean separateChannel = getSeparateChannelFlag(writer, sequence.getColorModel());

        // set settings
        writer.setFramesPerSecond(fps);
        // generate metadata
        writer.setMetadataRetrieve(MetaDataUtil.generateMetaData(sequence, (zMax - zMin) + 1, (tMax - tMin) + 1,
                separateChannel));
        // interleaved flag
        writer.setInterleaved(interleaved);
        // set id
        writer.setId(filePath);
        // init
        writer.setSeries(0);
        // usually give better save performance
        writer.setWriteSequentially(true);

        // get endianess
        final boolean littleEndian = !writer.getMetadataRetrieve().getPixelsBinDataBigEndian(0, 0).booleanValue();
        byte[] data = null;

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
                            {
                                // avoid multiple allocation
                                data = image.getRawData(c, data, 0, littleEndian);
                                writer.saveBytes(imageIndex, data);
                            }

                            imageIndex++;
                        }
                    }
                    else
                    {
                        if (image != null)
                        {
                            // avoid multiple allocation
                            data = image.getRawData(data, 0, littleEndian, interleaved);
                            writer.saveBytes(imageIndex, data);
                        }

                        imageIndex++;
                    }

                    if (saveFrame != null)
                        saveFrame.incPosition();
                }
            }
        }
        finally
        {
            // always close writer after a file has been saved
            writer.close();
        }
    }
}
