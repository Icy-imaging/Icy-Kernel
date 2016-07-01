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
package icy.sequence;

import icy.image.IcyBufferedImage;
import icy.type.DataType;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import loci.common.services.ServiceException;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.units.quantity.Time;
import ome.xml.model.Annotation;
import ome.xml.model.Channel;
import ome.xml.model.Dataset;
import ome.xml.model.Experiment;
import ome.xml.model.Experimenter;
import ome.xml.model.ExperimenterGroup;
import ome.xml.model.Image;
import ome.xml.model.Instrument;
import ome.xml.model.OME;
import ome.xml.model.Pixels;
import ome.xml.model.Plane;
import ome.xml.model.ROI;
import ome.xml.model.StructuredAnnotations;
import ome.xml.model.XMLAnnotation;
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
            ome.addImage(img);
        }

        final Image result = ome.getImage(index);

        if (result.getPixels() == null)
        {
            final Pixels pix = new Pixels();
            // wanted default dimension order
            pix.setDimensionOrder(DimensionOrder.XYCZT);
            // create default pixels object
            result.setPixels(pix);
        }

        return result;
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
     * Return pixels object at specified index for the specified metaData description.
     */
    public static Pixels getPixels(OME ome, int index)
    {
        if (ome != null)
        {
            if (index < ome.sizeOfImageList())
                return ome.getImage(index).getPixels();
        }

        return null;
    }

    /**
     * Return pixels object at specified index for the specified metaData description.
     */
    public static Pixels getPixels(OMEXMLMetadataImpl metaData, int index)
    {
        return getPixels(getOME(metaData), index);
    }

    /**
     * Return plane index for the specified T, Z, C position.
     */
    public static int getPlaneIndex(Pixels pix, int t, int z, int c)
    {
        final int sizeT = OMEUtil.getValue(pix.getSizeT(), 0);
        final int sizeZ = OMEUtil.getValue(pix.getSizeZ(), 0);
        int sizeC = OMEUtil.getValue(pix.getSizeC(), 0);
        int adjC = c;

        final Channel channel = pix.getChannel(0);
        if (channel != null)
        {
            final int spp = OMEUtil.getValue(channel.getSamplesPerPixel(), 0);
            // channel are packed in pixel so consider sizeC = 1
            if ((spp != 0) && (spp == sizeC))
            {
                sizeC = 1;
                adjC = 0;
            }
        }
        DimensionOrder dimOrder = pix.getDimensionOrder();
        // use default dimension order
        if (dimOrder == null)
            dimOrder = DimensionOrder.XYCZT;

        return FormatTools.getIndex(dimOrder.getValue(), sizeZ, sizeC, sizeT, sizeZ * sizeC * sizeT, z, adjC, t);
    }

    public static Plane getPlane(Pixels pix, int index)
    {
        if (pix != null)
        {
            if (index < pix.sizeOfPlaneList())
                return pix.getPlane(index);
        }

        return null;
    }

    /**
     * Return plane object for the specified T, Z, C position.
     */
    public static Plane getPlane(Pixels pix, int t, int z, int c)
    {
        return getPlane(pix, getPlaneIndex(pix, t, z, c));
    }

    /**
     * Ensure the plane at specified index exist for the specified Pixels object.
     */
    public static Plane ensurePlane(Pixels pix, int index)
    {
        // create missing plane
        while (pix.sizeOfPlaneList() <= index)
            pix.addPlane(new Plane());

        return pix.getPlane(index);
    }

    /**
     * Ensure the plane at specified T, Z, C position exist for the specified Pixels object.
     */
    public static Plane ensurePlane(Pixels pix, int t, int z, int c)
    {
        return ensurePlane(pix, getPlaneIndex(pix, t, z, c));
    }

    /**
     * Remove the Plan at specified position
     */
    public static void removePlane(Image img, int t, int z, int c)
    {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int numPlane = pix.sizeOfPlaneList();
        final int index = getPlaneIndex(pix, t, z, c);
        final Plane plane = getPlane(pix, index);

        // remove plane
        pix.removePlane(plane);

        // remove associated annotation
        for (int i = 0; i < plane.sizeOfLinkedAnnotationList(); i++)
            img.unlinkAnnotation(plane.getLinkedAnnotation(i));

        // clean some data
        if (pix.sizeOfBinDataList() == numPlane)
            pix.removeBinData(pix.getBinData(index));
        if (pix.sizeOfTiffDataList() == numPlane)
            pix.removeTiffData(pix.getTiffData(index));
    }

    /**
     * Returns the data type of the specified image serie.
     */
    public static DataType getDataType(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return DataType.getDataTypeFromPixelType(pix.getType());

        // assume byte by default
        return DataType.UBYTE;
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
     * Returns the number of frame (sizeT) of the specified Pixels object.
     */
    private static int getSizeT(Pixels pix)
    {
        return OMEUtil.getValue(pix.getSizeT(), 0);
    }

    /**
     * Returns the number of frame (sizeT) of the specified image serie.
     */
    public static int getSizeT(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return getSizeT(pix);

        return 0;
    }

    /**
     * Sets the data type of the specified image serie.
     */
    public static void setDataType(OMEXMLMetadataImpl metaData, int serie, DataType dataType)
    {
        metaData.setPixelsType(dataType.toPixelType(), serie);
    }

    /**
     * Sets the width (sizeX) of the specified image serie (need to be >= 1).
     */
    public static void setSizeX(OMEXMLMetadataImpl metaData, int serie, int sizeX)
    {
        metaData.setPixelsSizeX(OMEUtil.getPositiveInteger(sizeX), serie);
    }

    /**
     * Sets the height (sizeY) of the specified image serie (need to be >= 1).
     */
    public static void setSizeY(OMEXMLMetadataImpl metaData, int serie, int sizeY)
    {
        metaData.setPixelsSizeY(OMEUtil.getPositiveInteger(sizeY), serie);
    }

    /**
     * Sets the number of channel (sizeC) of the specified image serie (need to be >= 1).
     */
    public static void setSizeC(OMEXMLMetadataImpl metaData, int serie, int sizeC)
    {
        metaData.setPixelsSizeC(OMEUtil.getPositiveInteger(sizeC), serie);
    }

    /**
     * Sets the depth (sizeZ) of the specified image serie (need to be >= 1).
     */
    public static void setSizeZ(OMEXMLMetadataImpl metaData, int serie, int sizeZ)
    {
        metaData.setPixelsSizeZ(OMEUtil.getPositiveInteger(sizeZ), serie);
    }

    /**
     * Sets the number of frame (sizeT) of the specified image serie (need to be >= 1).
     */
    public static void setSizeT(OMEXMLMetadataImpl metaData, int serie, int sizeT)
    {
        metaData.setPixelsSizeT(OMEUtil.getPositiveInteger(sizeT), serie);
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
            return OMEUtil.getValue(pix.getPhysicalSizeX(), defaultValue);

        return defaultValue;
    }

    /**
     * Returns Y pixel size (in µm) of the specified image serie.
     */
    public static double getPixelSizeY(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeY(), defaultValue);

        return defaultValue;
    }

    /**
     * Returns Z pixel size (in µm) of the specified image serie.
     */
    public static double getPixelSizeZ(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeZ(), defaultValue);

        return defaultValue;
    }

    /**
     * Computes and returns the T time interval (in second) from internal time positions.<br>
     * If there is no internal time positions <code>0d</code> is returned.
     */
    public static double getTimeIntervalFromTimePositions(OMEXMLMetadataImpl metaData, int serie)
    {
        final Pixels pix = getPixels(metaData, serie);

        // try to compute time interval from time position
        if (pix != null)
            return computeTimeIntervalFromTimePosition(pix);

        return 0d;
    }

    /**
     * Returns T time interval (in second) for the specified image serie.
     */
    public static double getTimeInterval(OMEXMLMetadataImpl metaData, int serie, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
        {
            final Time timeInc = pix.getTimeIncrement();
            if (timeInc != null)
                return OMEUtil.getValue(timeInc, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the X field position (in µm) for the image at the specified Z, T, C position.
     */
    public static double getPositionX(OMEXMLMetadataImpl metaData, int serie, int t, int z, int c, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
        {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionX(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the Y field position (in µm) for the image at the specified Z, T, C position.
     */
    public static double getPositionY(OMEXMLMetadataImpl metaData, int serie, int t, int z, int c, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
        {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionY(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the Z field position (in µm) for the image at the specified Z, T, C position.
     */
    public static double getPositionZ(OMEXMLMetadataImpl metaData, int serie, int t, int z, int c, double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
        {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionZ(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the time position (in second) for the Pixels object at the specified Z, T, C
     * position.
     */
    private static double getTimePosition(Pixels pix, int t, int z, int c, double defaultValue)
    {
        final Plane plane = getPlane(pix, t, z, c);

        if (plane != null)
            return OMEUtil.getValue(plane.getDeltaT(), defaultValue);

        return defaultValue;
    }

    /**
     * Returns the time position (in second) for the image at the specified Z, T, C position.
     */
    public static double getTimePosition(OMEXMLMetadataImpl metaData, int serie, int t, int z, int c,
            double defaultValue)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return getTimePosition(pix, t, z, c, defaultValue);

        return defaultValue;
    }

    /**
     * Computes time interval from the time position informations.<br>
     * Returns <code>0d</code> if time position informations are missing ot if we have only 1 frame in the image.
     */
    private static double computeTimeIntervalFromTimePosition(Pixels pix)
    {
        final int sizeT = getSizeT(pix);

        if (sizeT <= 1)
            return 0d;

        double result = 0d;
        double last = -1d;
        int lastT = 0;
        int num = 0;

        for (int t = 0; t < sizeT; t++)
        {
            final Plane plane = getPlane(pix, t, 0, 0);

            if (plane != null)
            {
                final double timePos = OMEUtil.getValue(plane.getDeltaT(), Double.NaN);

                if (!Double.isNaN(timePos))
                {
                    if (last != -1d)
                    {
                        // get delta
                        result += (timePos - last) / (t - lastT);
                        num++;
                    }

                    last = timePos;
                    lastT = t;
                }
            }
        }

        // we need at least 1 delta
        if (num == 0)
            return 0d;

        return result / num;
    }

    /**
     * Set X pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeX(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeX(OMEUtil.getLength(value), serie);
    }

    /**
     * Set Y pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeY(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeY(OMEUtil.getLength(value), serie);
    }

    /**
     * Set Z pixel size (in µm) of the specified image serie.
     */
    public static void setPixelSizeZ(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsPhysicalSizeZ(OMEUtil.getLength(value), serie);
    }

    /**
     * Set T time resolution (in second) of the specified image serie.
     */
    public static void setTimeInterval(OMEXMLMetadataImpl metaData, int serie, double value)
    {
        metaData.setPixelsTimeIncrement(OMEUtil.getTime(value), serie);
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
    public static Channel ensureChannel(OMEXMLMetadataImpl metaData, int serie, int index)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            return ensureChannel(pix, index);

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
     * Remove a channel for the specified image serie.
     */
    public static void removeChannel(OMEXMLMetadataImpl metaData, int serie, int index)
    {
        final Pixels pix = getPixels(metaData, serie);

        if (pix != null)
            removeChannel(pix, index);
    }

    /**
     * Remove a channel from the specified Pixels object.
     */
    public static void removeChannel(Pixels pix, int index)
    {
        if (pix.sizeOfChannelList() > index)
            pix.removeChannel(pix.getChannel(index));
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
            removeChannel(pix, pix.sizeOfChannelList() - 1);

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

        final String result = StringUtil.getValue(metaData.getChannelName(serie, channel),
                getDefaultChannelName(channel));
        final String cleaned = XMLUtil.filterString(result);

        // cleaned string != original value --> set it
        if (!cleaned.equals(result))
            setChannelName(metaData, serie, channel, cleaned);

        return cleaned;
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
     * Returns Color of specified channel image serie.
     */
    public static Color getChannelColor(OMEXMLMetadataImpl metaData, int serie, int channel)
    {
        // needed as LOCI does not initialize them on read
        prepareMetaChannelName(metaData, serie, channel);

        return OMEUtil.getJavaColor(metaData.getChannelColor(serie, channel));
    }

    /**
     * Create and return a default OME Metadata object with default image name.
     */
    public static OMEXMLMetadataImpl createDefaultMetadata(String name)
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata();

        final OME ome = getOME(result);

        ensureSerie(ome, 0);

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
     *        width in pixels (need to be >= 1)
     * @param sizeY
     *        height in pixels (need to be >= 1)
     * @param sizeC
     *        number of channel (need to be >= 1)
     * @param sizeZ
     *        number of Z slices (need to be >= 1)
     * @param sizeT
     *        number of T frames (need to be >= 1)
     * @param z
     *        If this value is not <code>-1</code> then only the specified Z plane metadata information are preserved.
     * @param t
     *        If this value is not <code>-1</code> then only the specified T frame metadata information are preserved.
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     * @throws ServiceException
     */
    public static void setMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            int z, int t, DataType dataType, boolean separateChannel) throws ServiceException
    {
        OME ome = (OME) metadata.getRoot();

        if (ome == null)
        {
            metadata.createRoot();
            ome = (OME) metadata.getRoot();
        }

        // keep only one image
        setNumSerie(metadata, 1);
        // we need to remove some plane data
        if ((z != -1) || (t != -1))
            keepPlanes(ome.getImage(0), t, z, -1);

        if (StringUtil.isEmpty(metadata.getImageID(0)))
            metadata.setImageID(MetadataTools.createLSID("Image", 0), 0);
        if (StringUtil.isEmpty(metadata.getImageName(0)))
            metadata.setImageName("Sample", 0);

        if (StringUtil.isEmpty(metadata.getPixelsID(0)))
            metadata.setPixelsID(MetadataTools.createLSID("Pixels", 0), 0);

        // prefer big endian as JVM is big endian
        metadata.setPixelsBigEndian(Boolean.TRUE, 0);
        metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        // force XYCZT dimension order
        metadata.setPixelsDimensionOrder(DimensionOrder.XYCZT, 0);

        // adjust pixel type and dimension size
        metadata.setPixelsType(dataType.toPixelType(), 0);
        metadata.setPixelsSizeX(OMEUtil.getPositiveInteger(sizeX), 0);
        metadata.setPixelsSizeY(OMEUtil.getPositiveInteger(sizeY), 0);
        metadata.setPixelsSizeC(OMEUtil.getPositiveInteger(sizeC), 0);
        metadata.setPixelsSizeZ(OMEUtil.getPositiveInteger(sizeZ), 0);
        metadata.setPixelsSizeT(OMEUtil.getPositiveInteger(sizeT), 0);

        // get time interval information
        double timeInterval = MetaDataUtil.getTimeInterval(metadata, 0, 0d);
        // not defined ?
        if (timeInterval == 0d)
        {
            // try to compute it from time positions
            timeInterval = getTimeIntervalFromTimePositions(metadata, 0);
            // we got something --> set it as the time interval
            if (timeInterval != 0d)
                MetaDataUtil.setTimeInterval(metadata, 0, timeInterval);
        }

        // fix channel number depending separate channel flag
        if (separateChannel)
        {
            // set channel number
            setNumChannel(metadata, 0, sizeC);

            for (int c = 0; c < sizeC; c++)
            {
                if (StringUtil.isEmpty(metadata.getChannelID(0, c)))
                    metadata.setChannelID(MetadataTools.createLSID("Channel", 0, c), 0, c);
                metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(1)), 0, c);
                // metadata.getChannelName(0, c);
            }
        }
        else
        {
            // set channel number
            setNumChannel(metadata, 0, 1);

            if (StringUtil.isEmpty(metadata.getChannelID(0, 0)))
                metadata.setChannelID(MetadataTools.createLSID("Channel", 0, 0), 0, 0);
            metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(sizeC)), 0, 0);
        }
    }

    /**
     * Set metadata object with the given image properties.
     * 
     * @param metadata
     *        metadata object to fill.
     * @param sizeX
     *        width in pixels (need to be >= 1)
     * @param sizeY
     *        height in pixels (need to be >= 1)
     * @param sizeC
     *        number of channel (need to be >= 1)
     * @param sizeZ
     *        number of Z slices (need to be >= 1)
     * @param sizeT
     *        number of T frames (need to be >= 1)
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     * @throws ServiceException
     */
    public static void setMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean separateChannel) throws ServiceException
    {
        setMetaData(metadata, sizeX, sizeY, sizeC, sizeZ, sizeT, -1, -1, dataType, separateChannel);
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
        // do a copy as we mean use several time the same source sequence metadata
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
    public static OMEXMLMetadata generateMetaDataSinglePlane(Sequence sequence, int z, int t, boolean separateChannel)
            throws ServiceException
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata(sequence.getMetadata());

        setMetaData(result, sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(), 1, 1, z, t,
                sequence.getDataType_(), separateChannel);

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

    /**
     * Keep only the specified image serie.
     */
    public static void keepSingleSerie(OMEXMLMetadataImpl metaData, int num)
    {
        final OME ome = getOME(metaData);
        final int numSeries = ome.sizeOfImageList();
        final Image img = getSerie(metaData, num);

        // keep only the desired image
        for (int i = numSeries - 1; i >= 0; i--)
            if (i != num)
                ome.removeImage(ome.getImage(i));

        final Set<Object> toKeep = new HashSet<Object>();

        // try to keep associated dataset only
        toKeep.clear();
        for (int i = 0; i < img.sizeOfLinkedDatasetList(); i++)
            toKeep.add(img.getLinkedDataset(i));
        if (!toKeep.isEmpty())
        {
            for (int i = ome.sizeOfDatasetList() - 1; i >= 0; i--)
            {
                final Dataset obj = ome.getDataset(i);
                if (!toKeep.contains(obj))
                    ome.removeDataset(obj);
            }
        }
        // just assume they are indirectly linked
        else if (ome.sizeOfDatasetList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeDataset(ome.getDataset(i));
        }

        // try to keep associated ROI only
        toKeep.clear();
        for (int i = 0; i < img.sizeOfLinkedROIList(); i++)
            toKeep.add(img.getLinkedROI(i));
        if (!toKeep.isEmpty())
        {
            for (int i = ome.sizeOfROIList() - 1; i >= 0; i--)
            {
                final ROI obj = ome.getROI(i);
                if (!toKeep.contains(obj))
                    ome.removeROI(obj);
            }
        }
        // just assume they are indirectly linked
        else if (ome.sizeOfROIList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeROI(ome.getROI(i));
        }

        // try to keep associated experiment only
        final Experiment exp = img.getLinkedExperiment();
        if (exp != null)
        {
            for (int i = ome.sizeOfExperimentList() - 1; i >= 0; i--)
            {
                final Experiment obj = ome.getExperiment(i);
                if (obj != exp)
                    ome.removeExperiment(obj);
            }
        }
        else if (ome.sizeOfExperimentList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperiment(ome.getExperiment(i));
        }

        // try to keep associated experimenter only
        final Experimenter expr = img.getLinkedExperimenter();
        if (expr != null)
        {
            for (int i = ome.sizeOfExperimenterList() - 1; i >= 0; i--)
            {
                final Experimenter obj = ome.getExperimenter(i);
                if (obj != expr)
                    ome.removeExperimenter(obj);
            }
        }
        else if (ome.sizeOfExperimenterList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperimenter(ome.getExperimenter(i));
        }

        // try to keep associated experimenter group only
        final ExperimenterGroup exprGroup = img.getLinkedExperimenterGroup();
        if (exprGroup != null)
        {
            for (int i = ome.sizeOfExperimenterGroupList() - 1; i >= 0; i--)
            {
                final ExperimenterGroup obj = ome.getExperimenterGroup(i);
                if (obj != exprGroup)
                    ome.removeExperimenterGroup(obj);
            }
        }
        else if (ome.sizeOfExperimenterGroupList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperimenterGroup(ome.getExperimenterGroup(i));
        }

        // try to keep associated instrument only
        final Instrument instr = img.getLinkedInstrument();
        if (instr != null)
        {
            for (int i = ome.sizeOfInstrumentList() - 1; i >= 0; i--)
            {
                final Instrument obj = ome.getInstrument(i);
                if (obj != instr)
                    ome.removeInstrument(obj);
            }
        }
        else if (ome.sizeOfInstrumentList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeInstrument(ome.getInstrument(i));
        }

        // others misc data to clean
        if (ome.sizeOfPlateList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removePlate(ome.getPlate(i));
        }
        if (ome.sizeOfProjectList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeProject(ome.getProject(i));
        }
        if (ome.sizeOfScreenList() == numSeries)
        {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeScreen(ome.getScreen(i));
        }
    }

    /**
     * Keep only the specified plane metadata.
     */
    public static void keepSinglePlane(Image img, int index)
    {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int numPlane = pix.sizeOfPlaneList();
        final Plane plane = getPlane(pix, index);

        // keep only the desired plane
        for (int i = numPlane - 1; i >= 0; i--)
        {
            if (i != index)
                pix.removePlane(pix.getPlane(i));
        }

        final Set<Object> toKeep = new HashSet<Object>();

        // try to keep associated annotation only
        toKeep.clear();
        for (int i = 0; i < plane.sizeOfLinkedAnnotationList(); i++)
            toKeep.add(plane.getLinkedAnnotation(i));
        if (!toKeep.isEmpty())
        {
            for (int i = img.sizeOfLinkedAnnotationList() - 1; i >= 0; i--)
            {
                final Annotation obj = img.getLinkedAnnotation(i);
                if (!toKeep.contains(obj))
                    img.unlinkAnnotation(obj);
            }
        }
        // just assume they are indirectly linked
        else if (img.sizeOfLinkedAnnotationList() == numPlane)
        {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    img.unlinkAnnotation(img.getLinkedAnnotation(i));
        }

        // clean some data
        if (pix.sizeOfBinDataList() == numPlane)
        {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    pix.removeBinData(pix.getBinData(i));
        }
        if (pix.sizeOfTiffDataList() == numPlane)
        {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    pix.removeTiffData(pix.getTiffData(i));
        }
    }

    /**
     * Keep only plane(s) at specified C, Z, T position from the given metadata.
     * 
     * @param posT
     *        keep Plane to given T position (-1 to keep all)
     * @param posZ
     *        keep Plane to given Z position (-1 to keep all)
     * @param posC
     *        keep Plane to given C position (-1 to keep all)
     */
    public static void keepPlanes(Image img, int posT, int posZ, int posC)
    {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int sizeT = OMEUtil.getValue(pix.getSizeT(), 0);
        final int sizeZ = OMEUtil.getValue(pix.getSizeZ(), 0);
        final int sizeC = OMEUtil.getValue(pix.getSizeC(), 0);

        for (int t = 0; t < sizeT; t++)
        {
            boolean remove = (posT != -1) && (posT != t);

            for (int z = 0; t < sizeZ; z++)
            {
                remove |= (posZ != -1) && (posZ != z);

                for (int c = 0; c < sizeC; c++)
                {
                    remove |= (posC != -1) && (posC != c);

                    if (remove)
                        removePlane(img, t, z, c);
                }
            }
        }
    }

    /**
     * Cleanup the meta data (sometime we have empty data structure sitting there)
     */
    public static void clean(OMEXMLMetadataImpl metaData)
    {
        final OME ome = getOME(metaData);
        final StructuredAnnotations annotations = ome.getStructuredAnnotations();

        if (annotations != null)
        {
            for (int i = annotations.sizeOfXMLAnnotationList() - 1; i >= 0; i--)
            {
                final XMLAnnotation xmlAnnotation = annotations.getXMLAnnotation(i);

                if (isEmpty(xmlAnnotation))
                    annotations.removeXMLAnnotation(xmlAnnotation);
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified XML annotation are empty.
     */
    public static boolean isEmpty(XMLAnnotation xmlAnnotation)
    {
        return StringUtil.isEmpty(xmlAnnotation.getDescription()) && StringUtil.isEmpty(xmlAnnotation.getValue());
    }

}
