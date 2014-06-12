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
package icy.sequence;

import icy.image.IcyBufferedImage;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import loci.common.services.ServiceException;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.model.Channel;
import ome.xml.model.Image;
import ome.xml.model.OME;
import ome.xml.model.Pixels;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.primitives.PositiveInteger;

/**
 * Meta data utilities class.<br>
 * Basically provide safe access to metadata.
 * 
 * @author Stephane
 */
public class MetaDataUtil
{
    public static final String DEFAULT_CHANNEL_NAME = "ch ";

    /**
     * Returns OME root element (create it if needed).
     */
    public static OME getOME(OMEXMLMetadataImpl metaData)
    {
        OME result = (OME) metaData.getRoot();

        if (result == null)
        {
            metaData.createRoot();
            result = (OME) metaData.getRoot();
        }

        return result;
    }

    /**
     * Returns the number of image serie of the specified metaData description.
     */
    public static int getNumSerie(OMEXMLMetadataImpl metaData)
    {
        return metaData.getImageCount();
    }

    /**
     * Return image serie object at specified index for the specified metaData description.
     */
    public static Image getSerie(OMEXMLMetadataImpl metaData, int index)
    {
        final OME ome = getOME(metaData);

        if (index < ome.sizeOfImageList())
            return ome.getImage(index);

        return null;
    }

    /**
     * Ensure the image serie at specified index exist for the specified metaData description.
     */
    public static Image ensureSerie(OME ome, int index)
    {
        // create missing image
        while (ome.sizeOfImageList() <= index)
        {
            final Image img = new Image();
            // create default pixels object
            img.setPixels(new Pixels());
            ome.addImage(img);
        }

        return ome.getImage(index);
    }

    /**
     * Set the number of image serie for the specified metaData description.
     */
    public static void setNumSerie(OMEXMLMetadataImpl metaData, int num)
    {
        final OME ome = getOME(metaData);

        // keep only desired number of image
        while (ome.sizeOfImageList() > num)
            ome.removeImage(ome.getImage(ome.sizeOfImageList() - 1));

        // create missing image
        ensureSerie(ome, num - 1);
    }

    /**
     * Keep only the specified image serie.
     */
    public static void keepSingleSerie(OMEXMLMetadataImpl metaData, int num)
    {
        final OME ome = getOME(metaData);

        // keep only the desired image
        for (int i = ome.sizeOfImageList() - 1; i >= 0; i--)
            if (i != num)
                ome.removeImage(ome.getImage(i));
    }

    /**
     * Return pixels object at specified index for the specified metaData description.
     */
    public static Pixels getPixels(OMEXMLMetadataImpl metaData, int index)
    {
        final OME ome = getOME(metaData);

        if (index < ome.sizeOfImageList())
            return ome.getImage(index).getPixels();

        return null;
    }

    /**
     * Ensure the pixels at specified index exist for the specified metaData description.
     */
    public static Pixels ensurePixels(OME ome, int index)
    {
        Image img;
        Pixels result;

        // create missing image
        while (ome.sizeOfImageList() <= index)
        {
            img = new Image();
            ome.addImage(img);
        }

        img = ome.getImage(index);
        result = img.getPixels();

        // create Pixels object
        if (result == null)
        {
            result = new Pixels();
            img.setPixels(result);
        }

        return result;
    }

    /**
     * Returns the data type of the specified image serie.
     */
    public static DataType getDataType(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return DataType.getDataTypeFromPixelType(pix.getType());

        return DataType.UNDEFINED;
    }

    /**
     * Returns the width (sizeX) of the specified image serie.
     */
    public static int getSizeX(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeX(), 0);

        return 0;
    }

    /**
     * Returns the height (sizeY) of the specified image serie.
     */
    public static int getSizeY(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeY(), 0);

        return 0;
    }

    /**
     * Returns the number of channel (sizeC) of the specified image serie.
     */
    public static int getSizeC(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeC(), 0);

        return 0;
    }

    /**
     * Returns the depth (sizeZ) of the specified image serie.
     */
    public static int getSizeZ(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeZ(), 0);

        return 0;
    }

    /**
     * Returns the number of frame (sizeT) of the specified image serie.
     */
    public static int getSizeT(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeT(), 0);

        return 0;
    }

    /**
     * Returns the id of the specified image serie.
     */
    public static String getImageID(OMEXMLMetadataImpl metaData, int serie)
    {
        final Image img = getSerie(metaData, serie);

        if (img != null)
            return StringUtil.getValue(img.getID(), "");

        return "";
    }

    /**
     * Set the id of the specified image serie.
     */
    public static void setImageID(OMEXMLMetadataImpl metaData, int serie, String value)
    {
        metaData.setImageID(value, serie);
    }

    /**
     * Returns the name of the specified image serie.
     */
    public static String getName(OMEXMLMetadataImpl metaData, int serie)
    {
        final Image img = getSerie(metaData, serie);

        if (img != null)
            return StringUtil.getValue(img.getName(), "");

        return "";
    }

    /**
     * Set the name of the specified image serie.
     */
    public static void setName(OMEXMLMetadataImpl metaData, int serie, String value)
    {
        metaData.setImageName(value, serie);
    }

    /**
     * Returns X pixel size (in µm) of the specified image serie.
     */
    public static double getPixelSizeX(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeX(), defaultValue, false);

        return 1d;
    }

    /**
     * Returns Y pixel size (in µm) of the specified image serie.
     */
    public static double getPixelSizeY(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeY(), defaultValue, false);

        return 1d;
    }

    /**
     * Returns Z pixel size (in µm) of the specified image serie.
     */
    public static double getPixelSizeZ(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeZ(), defaultValue, false);

        return 1d;
    }

    /**
     * Returns T time size (in second) of the specified image serie.
     */
    public static double getTimeInterval(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return TypeUtil.getDouble(pix.getTimeIncrement(), defaultValue, false);

        return 1d;
    }

    /**
     * Set X pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeX(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeX(OMEUtil.getPositiveFloat(value), serie);
    }

    /**
     * Set Y pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeY(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeY(OMEUtil.getPositiveFloat(value), serie);
    }

    /**
     * Set Z pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeZ(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeZ(OMEUtil.getPositiveFloat(value), serie);
    }

    /**
     * Set T time resolution (in second) of the specified image serie.
     */
    public static void setTimeInterval(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsTimeIncrement(Double.valueOf(value), serie);
    }

    /**
     * Get default name for specified channel.
     */
    public static String getDefaultChannelName(int channel)
    {
        return DEFAULT_CHANNEL_NAME + channel;
    }

    /**
     * Returns the number of channel for the specified image serie in metaData description.
     */
    public static int getNumChannel(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return pix.sizeOfChannelList();

        return 0;
    }

    /**
     * Return channel object at specified index for the specified image serie.
     */
    public static Channel getChannel(OMEXMLMetadataImpl metaData, int serie, int index)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return pix.getChannel(index);

        return null;
    }

    /**
     * Ensure the channel at specified index exist for the specified image serie.
     */
    public static Channel ensureChannel(Pixels pix, int index)
    {
        // create missing channel
        while (pix.sizeOfChannelList() <= index)
            pix.addChannel(new Channel());

        return pix.getChannel(index);
    }

    /**
     * Set the number of channel for the specified image serie in metaData description.<br>
     * This is different from {@link #getSizeC(OMEXMLMetadataImpl, int)}.
     */
    public static void setNumChannel(OMEXMLMetadataImpl metaData, int serie, int num)
    {
        final OME ome = getOME(metaData);

        ensureSerie(ome, serie);

        final Image img = ome.getImage(serie);
        Pixels pix = img.getPixels();

        if (pix == null)
        {
            // create pixels object
            pix = new Pixels();
            img.setPixels(pix);
        }

        // keep only desired number of image
        while (pix.sizeOfChannelList() > num)
            pix.removeChannel(pix.getChannel(pix.sizeOfChannelList() - 1));

        // create missing image
        ensureChannel(pix, num - 1);
    }

    /**
     * Initialize default channel name until specified index if they are missing from the meta data
     * description.
     */
    private static void prepareMetaChannelName(OMEXMLMetadataImpl metaData, int serie, int channel)
    {
        int c = getNumChannel(metaData, serie);

        while (channel >= c)
        {
            // set default channel name
            metaData.setChannelName(getDefaultChannelName(c), serie, c);
            c++;
        }
    }

    /**
     * Returns name of specified channel image serie.
     */
    public static String getChannelName(OMEXMLMetadataImpl metaData, int serie, int channel)
    {
        // needed as LOCI does not initialize them on read
        prepareMetaChannelName(metaData, serie, channel);

        return StringUtil.getValue(metaData.getChannelName(serie, channel), getDefaultChannelName(channel));
    }

    /**
     * Set name of specified channel image serie.
     */
    public static void setChannelName(OMEXMLMetadataImpl metaData, int serie, int channel, String value)
    {
        // needed as LOCI only add current channel if it's missing
        prepareMetaChannelName(metaData, serie, channel - 1);

        metaData.setChannelName(value, serie, channel);
    }

    /**
     * Create and return a default OME Metadata object with default image name.
     */
    public static OMEXMLMetadataImpl createDefaultMetadata(String name)
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata();

        result.createRoot();
        result.setImageID(MetadataTools.createLSID("Image", 0), 0);
        result.setImageName(name, 0);

        return result;
    }

    /**
     * @deprecated Use {@link OMEUtil#createOMEMetadata(MetadataRetrieve, int)}
     */
    @Deprecated
    public static OMEXMLMetadataImpl createOMEMetadata(MetadataRetrieve metadata, int serie)
    {
        return OMEUtil.createOMEMetadata(metadata, serie);
    }

    /**
     * Set metadata object with the given image properties.
     * 
     * @param metadata
     *        metadata object to fill.
     * @param sizeX
     *        width in pixels.
     * @param sizeY
     *        height in pixels.
     * @param sizeC
     *        number of channel.
     * @param sizeZ
     *        number of Z slices.
     * @param sizeT
     *        number of T frames.
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     * @throws ServiceException
     */
    public static void setMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean separateChannel) throws ServiceException
    {
        OME ome = (OME) metadata.getRoot();

        if (ome == null)
        {
            metadata.createRoot();
            ome = (OME) metadata.getRoot();
        }

        // keep only one image
        setNumSerie(metadata, 1);

        // save channel name
        final List<String> channelNames = new ArrayList<String>();
        for (int c = 0; c < sizeC; c++)
            channelNames.add(getChannelName(metadata, 0, c));

        // save pixel size and time interval informations
        final double pixelSizeX = MetaDataUtil.getPixelSizeX(metadata, 0, 1d);
        final double pixelSizeY = MetaDataUtil.getPixelSizeY(metadata, 0, 1d);
        final double pixelSizeZ = MetaDataUtil.getPixelSizeZ(metadata, 0, 1d);
        final double timeInterval = MetaDataUtil.getTimeInterval(metadata, 0, 0.1d);

        // init pixels object as we set specific size here
        ome.getImage(0).setPixels(new Pixels());

        if (StringUtil.isEmpty(metadata.getImageID(0)))
            metadata.setImageID(MetadataTools.createLSID("Image", 0), 0);
        if (StringUtil.isEmpty(metadata.getImageName(0)))
            metadata.setImageName("Sample", 0);

        metadata.setPixelsID(MetadataTools.createLSID("Pixels", 0), 0);
        // prefer big endian as JVM is big endian
        metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        metadata.setPixelsDimensionOrder(DimensionOrder.XYCZT, 0);
        metadata.setPixelsType(dataType.toPixelType(), 0);
        metadata.setPixelsSizeX(OMEUtil.getPositiveInteger(sizeX), 0);
        metadata.setPixelsSizeY(OMEUtil.getPositiveInteger(sizeY), 0);
        metadata.setPixelsSizeC(OMEUtil.getPositiveInteger(sizeC), 0);
        metadata.setPixelsSizeZ(OMEUtil.getPositiveInteger(sizeZ), 0);
        metadata.setPixelsSizeT(OMEUtil.getPositiveInteger(sizeT), 0);

        // restore pixel size and time interval informations
        metadata.setPixelsPhysicalSizeX(OMEUtil.getPositiveFloat(pixelSizeX), 0);
        metadata.setPixelsPhysicalSizeY(OMEUtil.getPositiveFloat(pixelSizeY), 0);
        metadata.setPixelsPhysicalSizeZ(OMEUtil.getPositiveFloat(pixelSizeZ), 0);
        metadata.setPixelsTimeIncrement(Double.valueOf(timeInterval), 0);

        if (separateChannel)
        {
            for (int c = 0; c < sizeC; c++)
            {
                metadata.setChannelID(MetadataTools.createLSID("Channel", 0, c), 0, c);
                metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(1)), 0, c);
                metadata.setChannelName(channelNames.get(c), 0, c);
            }
        }
        else
        {
            metadata.setChannelID(MetadataTools.createLSID("Channel", 0, 0), 0, 0);
            metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(sizeC)), 0, 0);
        }
    }

    /**
     * Generates meta data for the given image properties.
     * 
     * @param sizeX
     *        width in pixels.
     * @param sizeY
     *        height in pixels.
     * @param sizeC
     *        number of channel.
     * @param sizeZ
     *        number of Z slices.
     * @param sizeT
     *        number of T frames.
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     * @return OMEXMLMetadata
     * @throws ServiceException
     */
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean separateChannel) throws ServiceException
    {
        final OMEXMLMetadataImpl result = createDefaultMetadata("Sample");

        setMetaData(result, sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);

        return result;
    }

    /**
     * Generates Meta Data for the given arguments.
     * 
     * @see #setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType,
            boolean separateChannel) throws ServiceException
    {
        return generateMetaData(sizeX, sizeY, sizeC, 1, 1, dataType, separateChannel);
    }

    /**
     * Generates Meta Data for the given BufferedImage.
     * 
     * @see #setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(IcyBufferedImage image, boolean separateChannel)
            throws ServiceException
    {
        return generateMetaData(image.getSizeX(), image.getSizeY(), image.getSizeC(), image.getDataType_(),
                separateChannel);
    }

    /**
     * Generates Meta Data for the given Sequence and parameters.
     * 
     * @see #setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT, boolean separateChannel)
            throws ServiceException
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata(sequence.getMetadata());

        setMetaData(result, sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(), useZ ? sequence.getSizeZ()
                : 1, useT ? sequence.getSizeT() : 1, sequence.getDataType_(), separateChannel);

        return result;
    }

    /**
     * Generates Meta Data for the given Sequence and parameters.
     * 
     * @see #setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT, boolean separateChannel)
            throws ServiceException
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata(sequence.getMetadata());

        setMetaData(result, sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(), sizeZ, sizeT,
                sequence.getDataType_(), separateChannel);

        return result;
    }

    /**
     * Generates Meta Data for the given Sequence.
     * 
     * @see #setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean separateChannel) throws ServiceException
    {
        return generateMetaData(sequence, true, true, separateChannel);
    }

}
