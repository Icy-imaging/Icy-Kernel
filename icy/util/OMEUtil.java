/**
 * 
 */
package icy.util;

import icy.image.IcyBufferedImage;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.type.TypeUtil;
import loci.common.services.ServiceException;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;
import loci.formats.services.OMEXMLServiceImpl;
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
     * Safe integer evaluation from PositiveInteger object.<br>
     * Return defaultValue if specified object is null.
     */
    public static int getValue(PositiveInteger obj, int defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getInt(obj.getValue(), defaultValue);
    }

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return defaultValue if specified object is null.
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
     * @deprecated Uses
     *             {@link MetaDataUtil#setMetaData(OMEXMLMetadataImpl, int, int, int, int, int, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadataImpl generateMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC,
            int sizeZ, int sizeT, DataType dataType, boolean separateChannel) throws ServiceException
    {
        final OMEXMLMetadataImpl result;

        if (metadata == null)
            result = MetaDataUtil.createDefaultMetadata("Sample");
        else
            result = metadata;

        MetaDataUtil.setMetaData(result, sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);

        return result;
    }

    /**
     * @deprecated Uses
     *             {@link MetaDataUtil#generateMetaData(int, int, int, int, int, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(int, int, int, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType,
            boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sizeX, sizeY, sizeC, dataType, separateChannel);
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(IcyBufferedImage, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(IcyBufferedImage image, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(image, separateChannel);
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(Sequence, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, useZ, useT, separateChannel);
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(Sequence, int, int, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, sizeZ, sizeT, separateChannel);
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(Sequence, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, separateChannel);
    }
}
