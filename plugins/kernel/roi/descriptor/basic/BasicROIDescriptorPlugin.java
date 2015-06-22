package plugins.kernel.roi.descriptor.basic;

import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the basic ROI descriptors as perimeter, surface,
 * area...
 * 
 * @author Stephane
 */
public class BasicROIDescriptorPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_CONTOUR = "Contour";
    public static final String ID_INTERIOR = "Interior";
    public static final String ID_PERIMETER = "Perimeter";
    public static final String ID_AREA = "Area";
    public static final String ID_SURFACE_AREA = "Surface area";
    public static final String ID_VOLUME = "Volume";
    public static final String ID_MIN_INTENSITY = "Minimum intensity";
    public static final String ID_MEAN_INTENSITY = "Mean intensity";
    public static final String ID_MAX_INTENSITY = "Maximum intensity";
    
    /**
     * Calculate the multiplier factor depending the wanted dimension information.
     */
    static double getMultiplierFactor(Sequence sequence, ROI roi, int dim)
    {
        final int dimRoi = roi.getDimension();

        // cannot give this information for this roi
        if (dimRoi > dim)
            return 0d;

        final Rectangle5D boundsRoi = roi.getBounds5D();
        double mul = 1d;

        switch (dim)
        {
            case 5:
                if (dimRoi == 4)
                {
                    final int sizeC = sequence.getSizeC();

                    if ((boundsRoi.getSizeC() == Double.POSITIVE_INFINITY) && (sizeC > 1))
                        mul *= sizeC;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 4:
                if (dimRoi == 3)
                {
                    final int sizeT = sequence.getSizeT();

                    if ((boundsRoi.getSizeT() == Double.POSITIVE_INFINITY) && (sizeT > 1))
                        mul *= sizeT;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 3:
                if (dimRoi == 2)
                {
                    final int sizeZ = sequence.getSizeZ();

                    if ((boundsRoi.getSizeZ() == Double.POSITIVE_INFINITY) && (sizeZ > 1))
                        mul *= sizeZ;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 2:
                if (dimRoi == 1)
                {
                    final int sizeY = sequence.getSizeY();

                    if ((boundsRoi.getSizeY() == Double.POSITIVE_INFINITY) && (sizeY > 1))
                        mul *= sizeY;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
        }

        return mul;
    }


    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        Map<String, Class<?>> descriptors = new HashMap<String, Class<?>>();

        descriptors.put(ID_CONTOUR, Double.class);
        descriptors.put(ID_CONTOUR, Double.class);
        descriptors.put(ID_CONTOUR, Double.class);
        descriptors.put(ID_CONTOUR, Double.class);

        return descriptors;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        
        final UnitPrefix unit = sequence.getBestPixelSizeUnit(2);
        return UnitUtil.getValueInUnit(value, UnitPrefix.MICRO, unit, 2);


        if (roi instanceof ROI2D)
        {
            map.put(ID_SURFACE, Double.valueOf(roi.getNumberOfPoints()));
            map.put(ID_PERIMETER, Double.valueOf(roi.getNumberOfContourPoints()));

            // other descriptors
        }
        else if (roi instanceof ROI3D)
        {
            map.put(ID_VOLUME, Double.valueOf(roi.getNumberOfPoints()));
        }
        else
            throw new UnsupportedOperationException(getClass().getName() + " does not support ROI of type "
                    + roi.getClass().getName());

        return map;
    }
}
