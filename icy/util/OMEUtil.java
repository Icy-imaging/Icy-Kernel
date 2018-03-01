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
package icy.util;

import icy.image.IcyBufferedImage;
import icy.sequence.MetaDataUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.DataType;
import icy.type.TypeUtil;

import java.awt.Color;

import org.w3c.dom.Document;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.MetadataTools;
import loci.formats.ome.OMEXMLMetadataImpl;
import loci.formats.services.OMEXMLService;
import loci.formats.services.OMEXMLServiceImpl;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.OME;
import ome.xml.model.StructuredAnnotations;
import ome.xml.model.XMLAnnotation;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;

/**
 * @author Stephane
 */
public class OMEUtil
{
    private static ServiceFactory factory;
    private static OMEXMLService OMEService;

    static
    {
        try
        {
            factory = new ServiceFactory();
            OMEService = factory.getInstance(OMEXMLService.class);
        }
        catch (DependencyException e)
        {
            System.err.println("Error create OME Service:" + e.getMessage());
            System.err.println("Using default service implementation...");

            factory = null;
            OMEService = new OMEXMLServiceImpl();
        }
    }

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
     * Safe integer evaluation from NonNegativeInteger object.<br>
     * Return defaultValue if specified object is null.
     */
    public static int getValue(NonNegativeInteger obj, int defaultValue)
    {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getInt(obj.getValue(), defaultValue);
    }

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return <code>defaultValue</code> if <code>obj</code> is null or equal to infinite with <code>allowInfinite</code>
     * set to false.
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
     * Convert specified Length to double value in µm (for backward compatibility).<br>
     * Return defaultValue if specified object is <code>null</code>.
     */
    public static double getValue(Length obj, double defaultValue)
    {
        if (obj == null)
            return defaultValue;

        final Number value = obj.value(UNITS.MICROMETER);
        if (value == null)
            return defaultValue;

        return value.doubleValue();
    }

    /**
     * Convert specified Time to double value in second (for backward compatibility).<br>
     * Return defaultValue if specified object is <code>null</code>.
     */
    public static double getValue(Time obj, double defaultValue)
    {
        if (obj == null)
            return defaultValue;

        final Number value = obj.value(UNITS.SECOND);
        if (value == null)
            return defaultValue;

        return value.doubleValue();
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
     * Return a NonNegativeInteger object representing the specified value
     */
    public static NonNegativeInteger getNonNegativeInteger(int value)
    {
        return new NonNegativeInteger(Integer.valueOf(value));
    }

    /**
     * Return a Length object representing the specified value (in µm)
     */
    public static Length getLength(double value)
    {
        return new Length(Double.valueOf(value), UNITS.MICROMETER);
    }

    /**
     * Return a Time object representing the specified value (in second)
     */
    public static Time getTime(double value)
    {
        return new Time(Double.valueOf(value), UNITS.SECOND);
    }

    /**
     * Return a java Color object from a OME Color object
     */
    public static Color getJavaColor(ome.xml.model.primitives.Color value)
    {
        if (value == null)
            return null;

        return new Color(value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha());
    }

    /**
     * Return a OME Color object from a java Color object
     */
    public static ome.xml.model.primitives.Color getOMEColor(Color value)
    {
        return new ome.xml.model.primitives.Color(value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha());
    }

    /**
     * Create a new empty OME Metadata object.
     */
    public synchronized static OMEXMLMetadata createOMEXMLMetadata()
    {
        try
        {
            return OMEService.createOMEXMLMetadata();
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            return null;
        }
    }

    /**
     * @deprecated Use {@link #createOMEXMLMetadata()} instead
     */
    @Deprecated
    public static OMEXMLMetadataImpl createOMEMetadata()
    {
        return (OMEXMLMetadataImpl) createOMEXMLMetadata();
    }

    /**
     * Create a new OME Metadata object from the specified Metadata object.<br>
     */
    public static OMEXMLMetadata createOMEXMLMetadata(MetadataRetrieve metadata)
    {
        final OMEXMLMetadata result = createOMEXMLMetadata();

        // TODO: remove that when annotations loading will be fixed in Bio-Formats
        if (metadata instanceof OMEXMLMetadata)
        {
            final OME root = (OME) ((OMEXMLMetadata) metadata).getRoot();
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

        synchronized (OMEService)
        {
            // need to cast to get rid of this old loci package stuff
            OMEService.convertMetadata((loci.formats.meta.MetadataRetrieve) metadata,
                    (loci.formats.meta.MetadataStore) result);
        }

        return result;
    }

    /**
     * @deprecated Use {@link #createOMEXMLMetadata(MetadataRetrieve)} instead
     */
    @Deprecated
    public synchronized static OMEXMLMetadataImpl createOMEMetadata(loci.formats.meta.MetadataRetrieve metadata)
    {
        return (OMEXMLMetadataImpl) createOMEXMLMetadata(metadata);
    }

    /**
     * Create a new single serie OME Metadata object from the specified Metadata object.
     * 
     * @param serie
     *        Index of the serie we want to keep.
     */
    public static OMEXMLMetadata createOMEXMLMetadata(MetadataRetrieve metadata, int serie)
    {
        final OMEXMLMetadata result = OMEUtil.createOMEXMLMetadata(metadata);

        MetaDataUtil.keepSingleSerie(result, serie);

        // set the default id with correct serie number (for XML metadata)
        result.setImageID(MetadataTools.createLSID("Image", serie), 0);

        return result;
    }

    /**
     * @deprecated Use {@link #createOMEXMLMetadata(MetadataRetrieve,int)} instead
     */
    @Deprecated
    public static OMEXMLMetadataImpl createOMEMetadata(loci.formats.meta.MetadataRetrieve metadata, int serie)
    {
        return (OMEXMLMetadataImpl) createOMEXMLMetadata(metadata, serie);
    }

    /**
     * Convert the specified Metadata object to OME Metadata.<br>
     * If the specified Metadata is already OME no conversion is done.
     */
    public static OMEXMLMetadata getOMEXMLMetadata(MetadataRetrieve metadata)
    {
        if (metadata instanceof OMEXMLMetadata)
            return (OMEXMLMetadata) metadata;

        return createOMEXMLMetadata(metadata);
    }

    /**
     * @deprecated Use {@link #getOMEXMLMetadata(MetadataRetrieve)} instead
     */
    @Deprecated
    public static OMEXMLMetadataImpl getOMEMetadata(loci.formats.meta.MetadataRetrieve metadata)
    {
        return (OMEXMLMetadataImpl) getOMEXMLMetadata(metadata);
    }

    /**
     * Return a XML document from the specified Metadata object
     */
    public static Document getXMLDocument(OMEXMLMetadata metadata)
    {
        try
        {
            return XMLUtil.createDocument(metadata.dumpXML());
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // return empty document
        return XMLUtil.createDocument(false);
    }

    /**
     * @deprecated Use {@link #getXMLDocument(OMEXMLMetadata)} instead
     */
    @Deprecated
    public static Document getXMLDocument(loci.formats.meta.MetadataRetrieve metadata)
    {
        return getXMLDocument(getOMEXMLMetadata(metadata));
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#setMetaData(OMEXMLMetadata, int, int, int, int, int, DataType, boolean)}
     *             instead.
     */
    @Deprecated
    public static OMEXMLMetadataImpl generateMetaData(OMEXMLMetadataImpl metadata, int sizeX, int sizeY, int sizeC,
            int sizeZ, int sizeT, DataType dataType, boolean separateChannel)
    {
        final OMEXMLMetadata result;

        if (metadata == null)
            result = MetaDataUtil.createMetadata("Sample");
        else
            result = metadata;

        MetaDataUtil.setMetaData(result, sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);

        return (OMEXMLMetadataImpl) result;
    }

    /**
     * @deprecated Uses {@link MetaDataUtil#generateMetaData(int, int, int, int, int, DataType, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean separateChannel) throws ServiceException
    {
        return MetaDataUtil.generateMetaData(sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(int, int, int, DataType, boolean)} instead.
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
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean useZ, boolean useT,
            boolean separateChannel)
    {
        return MetaDataUtil.generateMetaData(sequence, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, int sizeZ, int sizeT, boolean separateChannel)
    {
        return MetaDataUtil.generateMetaData(sequence, separateChannel);
    }

    /**
     * @deprecated Use {@link MetaDataUtil#generateMetaData(Sequence, boolean)} instead.
     */
    @Deprecated
    public static OMEXMLMetadata generateMetaData(Sequence sequence, boolean separateChannel)
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
