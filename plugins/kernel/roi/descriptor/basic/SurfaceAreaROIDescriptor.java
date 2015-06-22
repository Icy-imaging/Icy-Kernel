/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Surface area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class SurfaceAreaROIDescriptor extends ROIDescriptor
{
    public static final String ID = BasicROIDescriptorPlugin.ID_SURFACE_AREA;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return "Surface Area";
    }

    @Override
    public String getDescription()
    {
        return "Surface area";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(3, 2).toString();

        return UnitPrefix.MICRO.toString() + "m2";
    }

    @Override
    public Class<?> getType()
    {
        return Double.class;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence, int z, int t, int c) throws UnsupportedOperationException
    {
        return Double.valueOf(computeSurfaceArea(roi, sequence, z, t, c));
    }

    /**
     * Computes and returns the surface area expressed in the unit of the descriptor (see
     * {@link #getUnit(Sequence)}) for the specified ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the surface area
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @param z
     *        the specific Z position (slice) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI Z dimension.
     * @param t
     *        the specific T position (frame) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI T dimension.
     * @param c
     *        the specific C position (channel) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI C dimension.
     * @return the surface area
     * @throws UnsupportedOperationException
     *         if the specified Z, T or C position are not supported for this descriptor or if
     *         surface area calculation is not supported on this ROI.
     */
    public static double computeSurfaceArea(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        return computeSurfaceArea(ContourROIDescriptor.computeContour(roi, z, t, c), roi, sequence);
    }

    /**
     * Computes and returns the surface area from a given number of contour points expressed in the
     * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param contourPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the surface area
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size
     *        information.
     * @throws UnsupportedOperationException
     *         if the surface area calculation for the specified dimension is not supported by the
     *         ROI
     */
    static double computeSurfaceArea(double contourPoints, ROI roi, Sequence sequence)
            throws UnsupportedOperationException
    {
        try
        {
            return ContourROIDescriptor.computeContour(contourPoints, roi, sequence, 3);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation on the ROI: "
                    + roi.getName());
        }
    }
}
