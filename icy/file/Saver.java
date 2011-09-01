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
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.TypeUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.services.OMEXMLServiceImpl;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

/**
 * The file format is the following : filename-tttt-zzzz
 * 
 * @author Fab
 */
public class Saver
{
    /**
     * Generates Meta Data for the given arguments
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT, int dataType,
            boolean signedDataType) throws ServiceException
    {
        final OMEXMLService omeService = new OMEXMLServiceImpl();
        final OMEXMLMetadata meta = omeService.createOMEXMLMetadata();
        // define PixelType
        final PixelType pixelType = TypeUtil.dataTypeToPixelType(dataType, signedDataType);

        meta.createRoot();
        meta.setImageID(MetadataTools.createLSID("Image", 0), 0);
        meta.setImageName("Sample", 0);
        meta.setPixelsID(MetadataTools.createLSID("Pixels", 0), 0);
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder(DimensionOrder.XYCZT, 0);
        meta.setPixelsType(pixelType, 0);
        meta.setPixelsSizeX(new PositiveInteger(Integer.valueOf(sizeX)), 0);
        meta.setPixelsSizeY(new PositiveInteger(Integer.valueOf(sizeY)), 0);
        meta.setPixelsSizeC(new PositiveInteger(Integer.valueOf(sizeC)), 0);
        meta.setPixelsSizeZ(new PositiveInteger(Integer.valueOf(sizeZ)), 0);
        meta.setPixelsSizeT(new PositiveInteger(Integer.valueOf(sizeT)), 0);
        meta.setChannelID(MetadataTools.createLSID("Channel", 0, 0), 0, 0);
        meta.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(sizeC)), 0, 0);

        return meta;
    }

    /**
     * Generates Meta Data for the given arguments
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int dataType, boolean signedDataType)
            throws ServiceException
    {
        return generateMetaData(sizeX, sizeY, sizeC, 1, 1, dataType, signedDataType);
    }

    /**
     * Generates Meta Data for the given BufferedImage
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    static OMEXMLMetadata generateMetaData(IcyBufferedImage image) throws ServiceException
    {
        return generateMetaData(image.getSizeX(), image.getSizeY(), image.getSizeC(), image.getDataType(),
                image.isSignedDataType());
    }

    /**
     * Generates Meta Data for the given Sequence
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT) throws ServiceException
    {
        return generateMetaData(sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(),
                useZ ? sequence.getSizeZ() : 1, useT ? sequence.getSizeT() : 1, sequence.getDataType(),
                sequence.isSignedDataType());
    }

    /**
     * Generates Meta Data for the given Sequence
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT) throws ServiceException
    {
        return generateMetaData(sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(), sizeZ, sizeT,
                sequence.getDataType(), sequence.isSignedDataType());
    }

    /**
     * Generates Meta Data for the given Sequence
     * 
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    static OMEXMLMetadata generateMetaData(Sequence sequence) throws ServiceException
    {
        return generateMetaData(sequence, true, true);
    }

    /**
     * Return the OMETiffWrite for any TIFF file else uses the LOCI preferred writer.
     */
    private static IFormatWriter getWriter(File file)
    {
        if (file.getName().toLowerCase().endsWith(".tif"))
            return new OMETiffWriter();

        return new ImageWriter();
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
    public static void save(final Sequence sequence, final File file, final int zMin, final int zMax, final int tMin,
            final int tMax, final int fps, final boolean multipleFile)
    {
        final String filePath = file.getAbsolutePath();
        final int sizeT = (tMax - tMin) + 1;
        final int sizeZ = (zMax - zMin) + 1;

        final FileFrame saveFrame = new FileFrame("Saving", file.getAbsolutePath());

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
            }
            else
            {
                // save as multi images file
                save(null, sequence, filePath, zMin, zMax, tMin, tMax, fps, saveFrame);
            }

            // change sequence name
            sequence.setName(FileUtil.getFileName(filePath, false));
            sequence.setFilename(filePath);
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
     * Save a single image from bytes buffer to the specified file
     */
    public static void saveImage(byte[] data, int width, int height, int numComponent, int dataType,
            boolean signedDataType, File file, boolean force) throws FormatException, IOException
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
            writer.setMetadataRetrieve(generateMetaData(width, height, numComponent, dataType, signedDataType));
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
            writer.setMetadataRetrieve(generateMetaData(image));
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

            final boolean interleaved = true;

            // set settings
            writer.setFramesPerSecond(fps);
            // generate metadata
            writer.setMetadataRetrieve(generateMetaData(sequence, (zMax - zMin) + 1, (tMax - tMin) + 1));
            // needed so some image viewer can correctly read image (win XP system viewer need it)
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
                // ZT order is important here (see metadata)
                for (int t = tMin; t <= tMax; t++)
                {
                    for (int z = zMin; z <= zMax; z++)
                    {
                        if ((saveFrame != null) && saveFrame.isCancelRequested())
                            return;

                        final IcyBufferedImage image = sequence.getImage(t, z);

                        if (image != null)
                            writer.saveBytes(imageIndex, image.getRawData(littleEndian, interleaved));
                        // ((BufferedImageWriter) writer).saveImage(imageIndex, image);

                        imageIndex++;

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
