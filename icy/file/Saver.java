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

import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.FileFrame;
import icy.gui.menu.ApplicationMenu;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.colormodel.IcyColorModel;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.preferences.GeneralPreferences;
import icy.roi.ROI;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.util.OMEUtil;
import icy.util.StringUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.UnknownFormatException;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.out.APNGWriter;
import loci.formats.out.AVIWriter;
import loci.formats.out.JPEG2000Writer;
import loci.formats.out.JPEGWriter;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.TiffWriter;
import ome.xml.meta.OMEXMLMetadata;

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
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static loci.formats.ome.OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ,
            int sizeT, DataType dataType) throws ServiceException
    {
        return (loci.formats.ome.OMEXMLMetadata) OMEUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT, dataType,
                false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static loci.formats.ome.OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ,
            int sizeT, int dataType, boolean signedDataType) throws ServiceException
    {
        return (loci.formats.ome.OMEXMLMetadata) OMEUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT,
                DataType.getDataType(dataType, signedDataType), false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static loci.formats.ome.OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType)
            throws ServiceException
    {
        return (loci.formats.ome.OMEXMLMetadata) OMEUtil.generateMetaData(sizeX, sizeY, sizeC, 1, 1, dataType, false);
    }

    /**
     * @deprecated use {@link OMEUtil#generateMetaData(int, int, int, DataType, boolean)} instead
     */
    @Deprecated
    public static loci.formats.ome.OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int dataType,
            boolean signedDataType) throws ServiceException
    {
        return (loci.formats.ome.OMEXMLMetadata) OMEUtil.generateMetaData(sizeX, sizeY, sizeC,
                DataType.getDataType(dataType, signedDataType), false);
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
     * Return the closest compatible {@link IcyColorModel} supported by the specified ImageFileFormat
     * from the specified image description.<br>
     * That means this file format is able to save the data described by the returned {@link IcyColorModel} without any
     * loss
     * or conversion.<br>
     * 
     * @param imageFileFormat
     *        Image file format we want to test compatibility
     * @param numChannel
     *        number of channel of the image
     * @param dataType
     *        image data type
     */
    public static IcyColorModel getCompatibleColorModel(ImageFileFormat imageFileFormat, int numChannel,
            DataType dataType)
    {
        final DataType outDataType;
        final int outNumChannel;

        switch (imageFileFormat)
        {
            default:
            case TIFF:
                // TIFF supports all formats
                outDataType = dataType;
                outNumChannel = numChannel;
                break;

            case PNG:
                // PNG only supports byte data type (short is not really valid)
                if (dataType.getSize() > 1)
                    outDataType = DataType.UBYTE;
                else
                    outDataType = dataType;

                // PNG supports a maximum of 4 channels
                outNumChannel = Math.min(numChannel, 4);
                break;

            case AVI:
            case JPG:
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
                break;
        }

        return IcyColorModel.createInstance(outNumChannel, outDataType);
    }

    /**
     * Return the closest compatible {@link IcyColorModel} supported by the specified image file format
     * from the specified {@link IcyColorModel}.<br>
     * That means this image file format supports saving data described by the returned {@link IcyColorModel} without
     * any loss or conversion.
     * 
     * @param imageFileFormat
     *        Image file format we want to test compatibility
     * @param colorModel
     *        the colorModel describing data / image format
     */
    public static IcyColorModel getCompatibleColorModel(ImageFileFormat imageFileFormat, IcyColorModel colorModel)
    {
        return getCompatibleColorModel(imageFileFormat, colorModel.getNumComponents(), colorModel.getDataType_());
    }

    /**
     * Return true if the specified image file format is compatible with the image description.<br>
     * That means this image file format supports saving data without any loss or conversion.
     * 
     * @param imageFileFormat
     *        Image file format we want to test compatibility
     * @param numChannel
     *        number of channel of the image
     * @param alpha
     *        true if the image has an alpha channel
     * @param dataType
     *        image data type
     */
    public static boolean isCompatible(ImageFileFormat imageFileFormat, int numChannel, boolean alpha,
            DataType dataType)
    {
        return isCompatible(imageFileFormat, IcyColorModel.createInstance(numChannel, dataType));
    }

    /**
     * Return true if the specified image file format is compatible with the given {@link IcyColorModel}. <br>
     * That means this image file format supports saving data described by the returned {@link IcyColorModel} without
     * any loss or conversion.<br>
     * The color map data are never preserved, they are always restored to their default.<br>
     */
    public static boolean isCompatible(ImageFileFormat imageFileFormat, IcyColorModel colorModel)
    {
        return colorModel.isCompatible(getCompatibleColorModel(imageFileFormat, colorModel));
    }

    /**
     * Return true if the specified image file format is compatible to save the given Sequence.<br>
     * That means this image file format supports saving all original data (3D/4D/5D) without any loss or conversion.
     */
    public static boolean isCompatible(ImageFileFormat imageFileFormat, Sequence sequence)
    {
        final boolean multiZ = sequence.getSizeZ() > 1;
        final boolean multiT = sequence.getSizeT() > 1;

        switch (imageFileFormat)
        {
            case JPG:
            case PNG:
                // JPG and PNG: no support for time sequence or 3D image
                if ((multiZ) || (multiT))
                    return false;
                break;

            case AVI:
                // AVI: not support for 3D image
                if (multiZ)
                    return false;
                break;
        }

        return isCompatible(imageFileFormat, sequence.getColorModel());
    }

    /**
     * Return the separate channel flag from specified image file format and color space
     */
    private static boolean getSeparateChannelFlag(ImageFileFormat imageFileFormat, int numChannel, DataType dataType)
    {
        // only if we have more than 1 channel
        if (numChannel > 1)
        {
            // only TIFF writer support it: better to not separate channel for RGB images
            if (imageFileFormat.equals(ImageFileFormat.TIFF))
                return (numChannel != 3) || (dataType.getSize() > 1);
        }

        // others writers does not support separated channel
        return false;
    }

    /**
     * Return the separate channel flag from specified image file format and color space
     */
    private static boolean getSeparateChannelFlag(ImageFileFormat imageFileFormat, IcyColorModel colorModel)
    {
        return getSeparateChannelFlag(imageFileFormat, colorModel.getNumComponents(), colorModel.getDataType_());
    }

    /**
     * Return the closest compatible {@link IcyColorModel} supported by writer
     * from the specified image description.<br>
     * That means the writer is able to save the data described by the returned {@link IcyColorModel} without any loss
     * or conversion.<br>
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
        return getCompatibleColorModel(getImageFileFormat(writer, ImageFileFormat.TIFF), numChannel, dataType);
    }

    /**
     * Return the closest compatible {@link IcyColorModel} supported by writer
     * from the specified {@link IcyColorModel}.<br>
     * That means the writer is able to save the data described by the returned {@link IcyColorModel} without any loss
     * or conversion.<br>
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
     * Return true if the specified writer is compatible with the given {@link IcyColorModel}. <br>
     * That means the writer is able to save the data described by the colorModel without any loss
     * or conversion.<br>
     * The color map data are never preserved, they are always restored to their default.<br>
     */
    public static boolean isCompatible(IFormatWriter writer, IcyColorModel colorModel)
    {
        return colorModel.isCompatible(getCompatibleColorModel(writer, colorModel));
    }

    /**
     * Return true if the specified writer is compatible to save the given Sequence.<br>
     * That means the writer is able to save all original data (3D/4D/5D) without any loss or conversion.
     */
    public static boolean isCompatible(IFormatWriter writer, Sequence sequence)
    {
        return isCompatible(getImageFileFormat(writer, ImageFileFormat.TIFF), sequence);
    }

    /**
     * Return the separate channel flag from specified writer and color space
     */
    private static boolean getSeparateChannelFlag(IFormatWriter writer, int numChannel, DataType dataType)
    {
        return getSeparateChannelFlag(getImageFileFormat(writer, ImageFileFormat.TIFF), numChannel, dataType);
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
     * If sequence contains severals images then file is save as a directory to store all single images.
     * 
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence
     */
    public static void save(Sequence sequence, File file)
    {
        save(sequence, file, 15, (sequence.getSizeZ() * sequence.getSizeT()) > 1, true);
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
        save(sequence, file, 15, multipleFiles, showProgress);
    }

    /**
     * @deprecated Use {@link #save(Sequence, File, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static void save(Sequence sequence, File file, int zMin, int zMax, int tMin, int tMax, int fps,
            boolean multipleFiles)
    {
        save(sequence, file, zMin, zMax, tMin, tMax, fps, multipleFiles, true);
    }

    /**
     * @deprecated Use {@link #save(Sequence, File, int, boolean, boolean)} instead.
     */
    @Deprecated
    public static void save(Sequence sequence, File file, int zMin, int zMax, int tMin, int tMax, int fps,
            boolean multipleFile, boolean showProgress)
    {
        save(null, sequence, file, fps, multipleFile, showProgress, true);
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
     * @param fps
     *        frame rate for AVI sequence save
     * @param multipleFile
     *        flag to indicate if images are saved in separate file
     * @param showProgress
     *        show progress bar
     */
    public static void save(Sequence sequence, File file, int fps, boolean multipleFile, boolean showProgress)
    {
        save(null, sequence, file, fps, multipleFile, showProgress, true);
    }

    /**
     * @deprecated Use {@link #save(IFormatWriter, Sequence, File, int, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void save(IFormatWriter formatWriter, Sequence sequence, File file, int zMin, int zMax, int tMin,
            int tMax, int fps, boolean multipleFile, boolean showProgress)
    {
        save(formatWriter, sequence, file, fps, multipleFile, showProgress, true);
    }

    /**
     * @deprecated Use {@link #save(IFormatWriter, Sequence, File, int, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    public static void save(IFormatWriter formatWriter, Sequence sequence, File file, int zMin, int zMax, int tMin,
            int tMax, int fps, boolean multipleFile, boolean showProgress, boolean addToRecent)
    {
        save(formatWriter, sequence, file, fps, multipleFile, showProgress, addToRecent);
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
     *        have to specify a valid Writer to write the image file (see {@link #getWriter(ImageFileFormat)})
     * @param sequence
     *        sequence to save
     * @param file
     *        file where we want to save sequence.<br>
     *        Depending the <code>formatWriter</code> the file extension may be modified.<br>
     *        That is preferred as saving an image with a wrong extension may result in error on
     *        future read (wrong reader detection).<br>
     * @param fps
     *        frame rate for AVI sequence save
     * @param multipleFile
     *        flag to indicate if images are saved in separate file.<br>
     *        When multiple file is enabled the <code>file</code> parameter is considerer as a folder if it doens't have
     *        any extension
     * @param showProgress
     *        show progress bar
     * @param addToRecent
     *        add the saved sequence to recent opened sequence list
     */
    public static void save(IFormatWriter formatWriter, Sequence sequence, File file, int fps, boolean multipleFile,
            boolean showProgress, boolean addToRecent)
    {
        final String filePath = FileUtil.cleanPath(FileUtil.getGenericPath(file.getAbsolutePath()));
        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();
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

            final IFormatWriter writer;
            final Sequence savedSequence;

            // get the writer
            if (formatWriter == null)
                writer = getWriter(file, ImageFileFormat.TIFF);
            else
                writer = formatWriter;

            if (writer == null)
                throw new UnknownFormatException("Can't find a valid image writer for the specified file: " + file);

            // need multiple files ?
            if ((numImages > 1) && multipleFile)
            {
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
                // reset origin informations as now we are saved
                sequence.resetOriginInformation();
                // reset image provider
                sequence.setImageProvider(null);

                // assume that is the saved sequence (used for metadata)
                savedSequence = sequence;

                for (int t = 0; t < sizeT; t++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        String filename = filePathWithoutExt;

                        if (sizeT > 1)
                            filename += "_t" + decimalFormat.format(t);
                        if (sizeZ > 1)
                            filename += "_z" + decimalFormat.format(z);
                        filename += fileExt;

                        // save as single image file
                        save(writer, sequence, filename, t, z, fps, saveFrame);
                    }
                }

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fileBaseDirectory);
            }
            else
            {
                final ImageFileFormat iff = getImageFileFormat(writer, ImageFileFormat.TIFF);
                final String fileExt = FileUtil.getFileExtension(filePath, false);
                // force to set correct file extension
                final String fixedFilePath;

                if (iff.matches(fileExt))
                    fixedFilePath = filePath;
                else
                    fixedFilePath = filePath + "." + iff.getExtensions()[0];

                // default name used --> use filename
                if (sequence.isDefaultName())
                    sequence.setName(FileUtil.getFileName(filePath, false));

                // save whole sequence into a single file
                savedSequence = save(writer, sequence, fixedFilePath, -1, -1, fps, saveFrame);

                // we set filename on actual saved Sequence
                savedSequence.setFilename(filePath);
                // reset origin informations as now we are saved
                sequence.resetOriginInformation();

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fixedFilePath);
            }

            // Sequence persistence enabled --> save XML
            if (GeneralPreferences.getSequencePersistence())
                savedSequence.saveXMLData();
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
            boolean separateChannel, DataType dataType, File file, boolean force) throws FormatException, IOException
    {
        final String filePath = FileUtil.cleanPath(FileUtil.getGenericPath(file.getAbsolutePath()));

        if (FileUtil.exists(filePath))
        {
            // forced ? first delete the file else LOCI won't save it
            if (force)
                FileUtil.delete(filePath, true);
            else
                throw new IOException("File already exists");
        }
        // ensure parent directory exist
        FileUtil.ensureParentDirExist(filePath);

        final IFormatWriter writer;
        final boolean separateCh;

        if (formatWriter == null)
        {
            // get the writer
            writer = getWriter(FileUtil.getFileExtension(filePath, false), ImageFileFormat.TIFF);

            // prepare the metadata
            try
            {
                separateCh = getSeparateChannelFlag(writer, numChannel, dataType);
                writer.setMetadataRetrieve((MetadataRetrieve) MetaDataUtil.generateMetaData(width, height, numChannel,
                        dataType, separateCh));
            }
            catch (ServiceException e)
            {
                System.err.println("Saver.saveImage(...) error :");
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }
        else
        {
            // ready to use writer (metadata already prepared)
            writer = formatWriter;
            separateCh = separateChannel;
        }

        // we never interleaved data even if some image viewer need it to correctly read image (win XP viewer)
        writer.setInterleaved(false);
        writer.setId(filePath);
        writer.setSeries(0);
        // usually give better save performance
        writer.setWriteSequentially(true);

        try
        {
            // separated channel data
            if (separateChannel)
            {
                final int pitch = width * height * dataType.getSize();
                final byte[] dataChannel = new byte[pitch];
                int offset = 0;

                for (int c = 0; c < numChannel; c++)
                {
                    System.arraycopy(data, offset, dataChannel, 0, pitch);
                    writer.saveBytes(c, dataChannel);
                    offset += pitch;
                }
            }
            else
                // save all data at once
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
        saveImage(null, data, width, height, numChannel, false, dataType, file, force);
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

        final boolean separateChannel = getSeparateChannelFlag(writer, image.getIcyColorModel());

        try
        {
            writer.setMetadataRetrieve((MetadataRetrieve) MetaDataUtil.generateMetaData(image, separateChannel));
        }
        catch (ServiceException e)
        {
            System.err.println("Saver.saveImage(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // get byte order
        final boolean littleEndian = !writer.getMetadataRetrieve().getPixelsBinDataBigEndian(0, 0).booleanValue();
        // then save the image
        saveImage(writer, image.getRawData(littleEndian), image.getSizeX(), image.getSizeY(), image.getSizeC(),
                separateChannel, image.getDataType_(), file, force);
    }

    /**
     * Save the specified sequence in the specified file using the given writer.<br>
     * If posT or/and posZ are defined then only a sub part of the original Sequence is saved.
     * 
     * @param writer
     *        writer used to save sequence (define the image format, cannot be <code>null</code> at this point)
     * @param sequence
     *        sequence to save
     * @param filePath
     *        file name where we want to save sequence
     * @param posT
     *        frame index to save (-1 to save all frame from input sequence)
     * @param posZ
     *        slice index to save (-1 to save all slice from input sequence)
     * @param fps
     *        frame rate for AVI writer
     * @param saveFrame
     *        progress frame for save operation (can be null)
     * @return Actual saved Sequence (can be different from input one if conversion was needed)
     * @throws ServiceException
     * @throws IOException
     * @throws FormatException
     */
    private static Sequence save(IFormatWriter writer, Sequence sequence, String filePath, int posT, int posZ, int fps,
            FileFrame saveFrame) throws ServiceException, FormatException, IOException
    {
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

        final File file = new File(filePath);

        // first delete the file else LOCI won't save it correctly
        if (file.exists())
            file.delete();
        // ensure parent directory exist
        FileUtil.ensureParentDirExist(file);

        final ImageFileFormat saveFormat = getImageFileFormat(writer, ImageFileFormat.TIFF);
        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();
        final int adjZ, adjT;
        final int tMin, tMax;
        final int zMin, zMax;

        // adjust posT and posZ depending the writer support
        switch (saveFormat)
        {
            default:
            case TIFF:
                // no restriction for TIFF
                adjZ = posZ;
                adjT = posT;
                break;

            case AVI:
                // AVI: always save single slice
                adjZ = (posZ < 0) ? sizeZ / 2 : posZ;
                adjT = posT;
                break;

            case JPG:
            case PNG:
                // JPG or PNG: always save single image
                adjZ = (posZ < 0) ? sizeZ / 2 : posZ;
                adjT = (posT < 0) ? sizeT / 2 : posT;
                break;
        }

        // convert Sequence in good format for specified writer
        final Sequence compatibleSequence = getCompatibleSequenceForWriter(writer, sequence, adjT, adjZ);
        // get channel separation flag
        final boolean separateChannel = getSeparateChannelFlag(saveFormat, compatibleSequence.getColorModel());
        // prepare metadata
        final OMEXMLMetadata metadata = MetaDataUtil.generateMetaData(compatibleSequence, separateChannel);

        // clean unwanted planes
        MetaDataUtil.keepPlanes(metadata, 0, adjT, adjZ, -1);
        if (adjT < 0)
        {
            // all frame
            tMin = 0;
            tMax = sizeT - 1;
        }
        else
        {
            // single frame
            tMin = tMax = adjT;
            MetaDataUtil.setSizeT(metadata, 0, 1);
        }
        if (adjZ < 0)
        {
            // all slice
            zMin = 0;
            zMax = sizeZ - 1;
        }
        else
        {
            // single slice
            zMin = zMax = adjZ;
            MetaDataUtil.setSizeZ(metadata, 0, 1);
        }

        // specific to TIFF writer
        if (writer instanceof TiffWriter)
        {
            // > 2GB --> use big tiff (important to do it before setId(..) call)
            if (MetaDataUtil.getDataSize(metadata, 0, 0) > 2000000000L)
                ((TiffWriter) writer).setBigTiff(true);
        }

        // set settings
        writer.setFramesPerSecond(fps);
        // generate metadata
        writer.setMetadataRetrieve((MetadataRetrieve) metadata);
        // no interleave (XP default viewer want interleaved channel to correctly read image)
        writer.setInterleaved(false);
        // set id
        writer.setId(filePath);
        // init
        writer.setSeries(0);
        // usually give better save performance
        writer.setWriteSequentially(true);

        final int sizeC = compatibleSequence.getSizeC();
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
                    // interrupt process (partial save)
                    if ((saveFrame != null) && saveFrame.isCancelRequested())
                        return compatibleSequence;

                    final IcyBufferedImage image = compatibleSequence.getImage(t, z);

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
                            data = image.getRawData(data, 0, littleEndian);
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

        return compatibleSequence;
    }

    /**
     * Returns a compatible Sequence representing the input sequence so it can be saved with the specified writer.<br>
     * If the writer support the input sequence then the input sequence is directly returned.
     * 
     * @param writer
     *        writer used to save sequence (define the image format, cannot be <code>null</code>)
     * @param sequence
     *        sequence to save
     * @param posT
     *        frame index to keep (-1 for all frame)
     * @param posZ
     *        slice index to keep (-1 for all slice)
     * @return the compatible sequence for given Writer
     */
    public static Sequence getCompatibleSequenceForWriter(IFormatWriter writer, Sequence sequence, int posT, int posZ)
    {
        final int sizeC = sequence.getSizeC();
        final DataType dataType = sequence.getDataType_();
        final boolean needConvert;
        final ImageFileFormat imageFormat = getImageFileFormat(writer, ImageFileFormat.TIFF);

        // adjust posT and posZ depending the writer support
        switch (imageFormat)
        {
            default:
                // assume TIFF
                needConvert = false;
                break;

            case AVI:
            case JPG:
                // JPG, AVI: only supports byte data type and Gray/RGB images
                needConvert = (dataType.getSize() > 1) || (sizeC == 2) || (sizeC > 3);
                break;

            case PNG:
                // PNG: support byte data type with a maximum of 4 channels
                needConvert = (dataType.getSize() > 1) || (sizeC > 4);
                break;
        }

        // no conversion needed
        if (!needConvert)
            return sequence;

        final int sizeT = sequence.getSizeT();
        final int sizeZ = sequence.getSizeZ();
        final int tMin, tMax;
        final int zMin, zMax;

        if (posT < 0)
        {
            // all frame
            tMin = 0;
            tMax = sizeT - 1;
        }
        else
            // single frame
            tMin = tMax = posT;
        if (posZ < 0)
        {
            // all slice
            zMin = 0;
            zMax = sizeZ - 1;
        }
        else
            // single slice
            zMin = zMax = posZ;

        // wanted image type
        final int imageType = (sizeC > 1) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY;
        // image receiver
        final BufferedImage imgOut = new BufferedImage(sequence.getSizeX(), sequence.getSizeY(), imageType);
        // conversion LUT (use default sequence one)
        final LUT lut = sequence.getDefaultLUT();

        // create compatible sequence
        final Sequence result = new Sequence(OMEUtil.createOMEXMLMetadata(sequence.getOMEXMLMetadata()));

        result.beginUpdate();
        try
        {
            for (int t = tMin; t <= tMax; t++)
                for (int z = zMin; z <= zMax; z++)
                    result.setImage(t, z, IcyBufferedImageUtil.toBufferedImage(sequence.getImage(t, z), imgOut, lut));

            // preserve ROI and overlays (for XML metadata preservation)
            for (ROI roi : sequence.getROIs())
                result.addROI(roi);
            for (Overlay overlay : sequence.getOverlays())
                result.addOverlay(overlay);

            // rename channels and set final name
            switch (imageType)
            {
                default:
                case BufferedImage.TYPE_INT_RGB:
                    result.setChannelName(0, "red");
                    result.setChannelName(1, "green");
                    result.setChannelName(2, "blue");
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                    result.setChannelName(0, "gray");
                    break;
            }
            result.setName(sequence.getName() + " (" + imageFormat + ")");
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }
}
