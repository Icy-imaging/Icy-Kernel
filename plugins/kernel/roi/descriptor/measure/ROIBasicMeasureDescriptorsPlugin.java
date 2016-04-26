package plugins.kernel.roi.descriptor.measure;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the following basic measures ROI descriptors:<br/>
 * <li>Contour (in pixel)</li><br/>
 * <li>Interior (in pixel)</li><br/>
 * <li>Perimeter (pixel size unit - 2D ROI only)</li><br/>
 * <li>Surface Area (pixel size unit - 3D ROI only)</li><br/>
 * <li>Area (pixel size unit - 2D ROI only)</li><br/>
 * <li>Volume (pixel size unit - 3D ROI only)</li><br/>
 * 
 * @author Stephane
 */
public class ROIBasicMeasureDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_CONTOUR = ROIContourDescriptor.ID;
    public static final String ID_INTERIOR = ROIInteriorDescriptor.ID;
    public static final String ID_PERIMETER = ROIPerimeterDescriptor.ID;
    public static final String ID_AREA = ROIAreaDescriptor.ID;
    public static final String ID_SURFACE_AREA = ROISurfaceAreaDescriptor.ID;
    public static final String ID_VOLUME = ROIVolumeDescriptor.ID;

    public static final ROIContourDescriptor contourDescriptor = new ROIContourDescriptor();
    public static final ROIInteriorDescriptor interiorDescriptor = new ROIInteriorDescriptor();
    public static final ROIPerimeterDescriptor perimeterDescriptor = new ROIPerimeterDescriptor();
    public static final ROIAreaDescriptor areaDescriptor = new ROIAreaDescriptor();
    public static final ROISurfaceAreaDescriptor surfaceAreaDescriptor = new ROISurfaceAreaDescriptor();
    public static final ROIVolumeDescriptor volumeDescriptor = new ROIVolumeDescriptor();

    /**
     * Calculate the multiplier factor depending the wanted dimension information.
     */
    public static double getMultiplierFactor(Sequence sequence, ROI roi, int dim)
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
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(contourDescriptor);
        result.add(interiorDescriptor);
        result.add(perimeterDescriptor);
        result.add(areaDescriptor);
        result.add(surfaceAreaDescriptor);
        result.add(volumeDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        // use the contour and interior to compute others descriptors
        final double contour = ROIContourDescriptor.computeContour(roi);
        final double interior = ROIInteriorDescriptor.computeInterior(roi);

        result.put(contourDescriptor, Double.valueOf(contour));
        result.put(interiorDescriptor, Double.valueOf(interior));

        int notComputed = 0;

        try
        {
            result.put(perimeterDescriptor, Double.valueOf(ROIPerimeterDescriptor.computePerimeter(roi, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            result.put(perimeterDescriptor, null);
            notComputed++;
        }
        try
        {
            result.put(areaDescriptor, Double.valueOf(ROIAreaDescriptor.computeArea(interior, roi, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            result.put(areaDescriptor, null);
            notComputed++;
        }
        try
        {
            result.put(surfaceAreaDescriptor,
                    Double.valueOf(ROISurfaceAreaDescriptor.computeSurfaceArea(roi, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            result.put(surfaceAreaDescriptor, null);
            notComputed++;
        }
        try
        {
            result.put(volumeDescriptor, Double.valueOf(ROIVolumeDescriptor.computeVolume(interior, roi, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            result.put(volumeDescriptor, null);
            notComputed++;
        }

        if (notComputed == 4)
        {
            throw new UnsupportedOperationException(getClass().getSimpleName()
                    + ": cannot compute any of the descriptors for '" + roi.getName() + "'");
        }

        return result;
    }
}
