/**
 * 
 */
package icy.util;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.type.TypeUtil;
import loci.common.services.ServiceException;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;
import loci.formats.services.OMEXMLServiceImpl;
import ome.xml.model.OME;
import ome.xml.model.Pixels;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;

import org.w3c.dom.Document;

/**
 * @author Stephane
 */
public class OMEUtil
{
    private static final OMEXMLServiceImpl OMEService = new OMEXMLServiceImpl();

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return 0 if specified object is null.
     */
    public static double getValue(PositiveFloat obj, double defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getDouble(obj.getValue(), defaultValue);
    }

    /**
     * Return a PositiveFloat object representing the specified value
     */
    public static PositiveFloat getPositiveFloat(double value)
    {
        return new PositiveFloat(Double.valueOf(value));
    }

    /**
     * Create a new empty OME Metadata object.
     */
    public static OMEXMLMetadataImpl createOMEMetadata()
    {
        try
        {
            return (OMEXMLMetadataImpl) OMEService.createOMEXMLMetadata();
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            return null;
        }
    }

    /**
     * Create a new OME Metadata object from the specified Metadata object.<br>
     */
    public static OMEXMLMetadataImpl createOMEMetadata(MetadataRetrieve metadata)
    {
        final OMEXMLMetadataImpl result = createOMEMetadata();

        OMEService.convertMetadata(metadata, result);

        return result;
    }

    /**
     * Convert the specified Metadata object to OME Metadata.<br>
     * If the specified Metadata is already OME no conversion is done.
     */
    public static OMEXMLMetadataImpl getOMEMetadata(MetadataRetrieve metadata)
    {
        if (metadata instanceof OMEXMLMetadataImpl)
            return (OMEXMLMetadataImpl) metadata;

        return createOMEMetadata(metadata);
    }

    /**
     * Return a XML document from the specified Metadata object
     */
    public static Document getXMLDocument(MetadataRetrieve metadata)
    {
        try
        {
            return XMLUtil.getDocument(getOMEMetadata(metadata).dumpXML());
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // return empty document
        return XMLUtil.createDocument(false);
    }

    /**
     * Generates meta data for the given image properties.
     * 
     * @param name
     *        image name.
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
     * @return IMetadata
     * @throws ServiceException
     */
    public static OMEXMLMetadataImpl generateMetaData(OMEXMLMetadataImpl metadata, String name, int sizeX, int sizeY,
            int sizeC, int sizeZ, int sizeT, DataType dataType, boolean separateChannel) throws ServiceException
    {
        final OMEXMLMetadataImpl meta;

        if (metadata == null)
        {
            meta = createOMEMetadata();
            meta.createRoot();

            meta.setImageID(MetadataTools.createLSID("Image", 0), 0);
            meta.setImageName(name, 0);
        }
        else
        {
            meta = metadata;
            final OME ome = (OME) meta.getRoot();

            // init pixels object as we set specific size here
            ome.getImage(0).setPixels(new Pixels());
        }

        meta.setPixelsID(MetadataTools.createLSID("Pixels", 0), 0);
        // prefer big endian as JVM is actually big endian
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder(DimensionOrder.XYCZT, 0);
        meta.setPixelsType(dataType.toPixelType(), 0);
        meta.setPixelsSizeX(new PositiveInteger(Integer.valueOf(sizeX)), 0);
        meta.setPixelsSizeY(new PositiveInteger(Integer.valueOf(sizeY)), 0);
        meta.setPixelsSizeC(new PositiveInteger(Integer.valueOf(sizeC)), 0);
        meta.setPixelsSizeZ(new PositiveInteger(Integer.valueOf(sizeZ)), 0);
        meta.setPixelsSizeT(new PositiveInteger(Integer.valueOf(sizeT)), 0);

        if (separateChannel)
        {
            for (int c = 0; c < sizeC; c++)
            {
                meta.setChannelID(MetadataTools.createLSID("Channel", 0, c), 0, c);
                meta.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(1)), 0, c);
            }
        }
        else
        {
            meta.setChannelID(MetadataTools.createLSID("Channel", 0, 0), 0, 0);
            meta.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(sizeC)), 0, 0);
        }

        return meta;
    }

    public static OMEXMLMetadata generateMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC,
            int sizeZ, int sizeT, DataType dataType, boolean separateChannel) throws ServiceException
    {
        return generateMetaData(metadata, "Sample", sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);
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
        return generateMetaData(null, sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);
    }

    /**
     * Generates Meta Data for the given arguments.
     * 
     * @see #generateMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType,
            boolean separateChannel) throws ServiceException
    {
        return generateMetaData(null, sizeX, sizeY, sizeC, 1, 1, dataType, separateChannel);
    }

    /**
     * Generates Meta Data for the given BufferedImage
     * 
     * @see #generateMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(IcyBufferedImage image, boolean separateChannel)
            throws ServiceException
    {
        return generateMetaData(image.getSizeX(), image.getSizeY(), image.getSizeC(), image.getDataType_(),
                separateChannel);
    }

    /**
     * Generates Meta Data for the given Sequence.
     * 
     * @see #generateMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT, boolean separateChannel)
            throws ServiceException
    {
        return generateMetaData(createOMEMetadata(sequence.getMetadata()), sequence.getName(), sequence.getSizeX(),
                sequence.getSizeY(), sequence.getSizeC(), useZ ? sequence.getSizeZ() : 1, useT ? sequence.getSizeT()
                        : 1, sequence.getDataType_(), separateChannel);
    }

    /**
     * Generates Meta Data for the given Sequence.
     * 
     * @see #generateMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT, boolean separateChannel)
            throws ServiceException
    {
        return generateMetaData(createOMEMetadata(sequence.getMetadata()), sequence.getName(), sequence.getSizeX(),
                sequence.getSizeY(), sequence.getSizeC(), sizeZ, sizeT, sequence.getDataType_(), separateChannel);
    }

    /**
     * Generates Meta Data for the given Sequence
     * 
     * @see #generateMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean separateChannel) throws ServiceException
    {
        return generateMetaData(sequence, true, true, separateChannel);
    }
}
