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
public class BasicMeasureROIDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_CONTOUR = ContourROIDescriptor.ID;
    public static final String ID_INTERIOR = InteriorROIDescriptor.ID;
    public static final String ID_PERIMETER = PerimeterROIDescriptor.ID;
    public static final String ID_AREA = AreaROIDescriptor.ID;
    public static final String ID_SURFACE_AREA = SurfaceAreaROIDescriptor.ID;
    public static final String ID_VOLUME = VolumeROIDescriptor.ID;

    public static final ContourROIDescriptor contourDescriptor = new ContourROIDescriptor();
    public static final InteriorROIDescriptor interiorDescriptor = new InteriorROIDescriptor();
    public static final PerimeterROIDescriptor perimeterDescriptor = new PerimeterROIDescriptor();
    public static final AreaROIDescriptor areaDescriptor = new AreaROIDescriptor();
    public static final SurfaceAreaROIDescriptor surfaceAreaDescriptor = new SurfaceAreaROIDescriptor();
    public static final VolumeROIDescriptor volumeDescriptor = new VolumeROIDescriptor();

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
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();
        final ROI r;

        // want a sub part of the ROI ?
        if ((z != -1) || (t != -1) || (c != -1))
            r = roi.getSubROI(z, t, c, false);
        else
            r = roi;

        // use the contour and interior to compute others descriptors
        final double contour = ContourROIDescriptor.computeContour(r);
        final double interior = InteriorROIDescriptor.computeInterior(r);

        result.put(contourDescriptor, Double.valueOf(contour));
        result.put(interiorDescriptor, Double.valueOf(interior));

        try
        {
            result.put(perimeterDescriptor,
                    Double.valueOf(PerimeterROIDescriptor.computePerimeter(contour, r, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            // ignore at this point
        }
        try
        {
            result.put(areaDescriptor, Double.valueOf(AreaROIDescriptor.computeArea(interior, r, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            // ignore at this point
        }
        try
        {
            result.put(surfaceAreaDescriptor,
                    Double.valueOf(SurfaceAreaROIDescriptor.computeSurfaceArea(contour, r, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            // ignore at this point
        }
        try
        {
            result.put(volumeDescriptor, Double.valueOf(VolumeROIDescriptor.computeVolume(interior, r, sequence)));
        }
        catch (UnsupportedOperationException e)
        {
            // ignore at this point
        }

        if (result.isEmpty())
        {
            String mess = getClass().getSimpleName() + ": cannot compute any of the descriptors for '" + roi.getName()
                    + "'";
            // sub part of the ROI ?
            if (r != roi)
                mess += " at position [Z=" + z + ",T=" + t + ",C=" + c + "]";

            throw new UnsupportedOperationException(mess);
        }

        return result;
    }
}
