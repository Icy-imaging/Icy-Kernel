/**
 * 
 */
package icy.util;

import icy.system.IcyExceptionHandler;
import icy.type.TypeUtil;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.AbstractOMEXMLMetadata;
import loci.formats.services.OMEXMLServiceImpl;
import ome.xml.model.OMEModelObject;
import ome.xml.model.primitives.PositiveFloat;

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
     * Return a XML document from the specified Metadata object
     */
    public static Document getXMLDocument(MetadataRetrieve metadata)
    {
        try
        {
            final AbstractOMEXMLMetadata omexmlMeta = (AbstractOMEXMLMetadata) OMEService.getOMEMetadata(metadata);
            final OMEModelObject root = (OMEModelObject) omexmlMeta.getRoot();
            final Document result = XMLUtil.createDocument(false);

            result.appendChild(root.asXMLElement(result));

            return result;
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        // return empty document
        return XMLUtil.createDocument(false);
    }
}
