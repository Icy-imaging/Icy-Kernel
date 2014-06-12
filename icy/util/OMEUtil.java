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
package icy.util;

import icy.image.IcyBufferedImage;
import icy.sequence.MetaDataUtil;
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
import ome.xml.model.StructuredAnnotations;
import ome.xml.model.XMLAnnotation;
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
     * Return <code>defaultValue</code> if <code>obj</code> is null or equal to infinite with
     * <code>allowInfinite</code> set to false.
     */
    public static double getValue(PositiveFloat obj, double defaultValue, boolean allowInfinite)
    {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getDouble(obj.getValue(), defaultValue, allowInfinite);
    }

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return defaultValue if specified object is null.
     */
    public static double getValue(PositiveFloat obj, double defaultValue)
    {
        return getValue(obj, defaultValue, true);
    }

    /**
     * Return a PositiveFloat object representing the specified value
     */
    public static PositiveFloat getPositiveFloat(double value)
    {
        return new PositiveFloat(Double.valueOf(value));
    }

    /**
     * Return a PositiveInteger object representing the specified value
     */
    public static PositiveInteger getPositiveInteger(int value)
    {
        return new PositiveInteger(Integer.valueOf(value));
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

        // TODO: remove that when annotations loading will be fixed in Bio-Formats
        if (metadata instanceof OMEXMLMetadataImpl)
        {
            final OME root = (OME) ((OMEXMLMetadataImpl) metadata).getRoot();
            final StructuredAnnotations annotations = root.getStructuredAnnotations();

            // clean up annotation
            if (annotations != null)
            {
                for (int i = annotations.sizeOfXMLAnnotationList() - 1; i >= 0; i--)
                {
                    final XMLAnnotation annotation = annotations.getXMLAnnotation(i);

                    if (StringUtil.isEmpty(annotation.getValue()))
                        annotations.removeXMLAnnotation(annotation);
                }
            }
        }

        OMEService.convertMetadata(metadata, result);

        return result;
    }

    /**
     * Create a new single serie OME Metadata object from the specified Metadata object.
     * 
     * @param serie
     *        Index of the serie we want to keep.
     */
    public static OMEXMLMetadataImpl createOMEMetadata(MetadataRetrieve metadata, int serie)
    {
        final OMEXMLMetadataImpl result = OMEUtil.createOMEMetadata(metadata);

        MetaDataUtil.keepSingleSerie(result, serie);

        // set the default id with correct serie number (for XML metadata)
        result.setImageID(MetadataTools.createLSID("Image", serie), 0);

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
     * @deprecated Use {@link MetaDataUtil#generateMetaData(int, int, int, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, DataType dataType,
            boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sizeX, sizeY, sizeC, dataType, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(IcyBufferedImage, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(IcyBufferedImage image, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(image, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, boolean, boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, useZ, useT, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, int, int, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT, boolean separateChannel)
            throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, sizeZ, sizeT, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sequence, separateChannel);
    }

    /**
     * Report and upload the specified filename to LOCI team.
     */
    public static boolean reportLociError(String fileName, String errorMessage)
    {
        // TODO: implement this when done in LOCI
        // final IssueReporter reporter = new IssueReporter();
        // return reporter.reportBug(fileName, errorMessage);

        return false;
    }
}
