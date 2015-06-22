/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Perimeter ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class PerimeterROIDescriptor extends ROIDescriptor
{
    public static final String ID = BasicROIDescriptorPlugin.ID_PERIMETER;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return "Perimeter";
    }

    @Override
    public String getDescription()
    {
        return "Perimeter";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(2, 1).toString();

        return UnitPrefix.MICRO.toString() + "m";
    }

    @Override
    public Class<?> getType()
    {
        return Double.class;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence, int z, int t, int c) throws UnsupportedOperationException
    {
        return Double.valueOf(computePerimeter(roi, sequence, z, t, c));
    }

    /**
     * Computes and returns the perimeter expressed in the unit of the descriptor (see
     * {@link #getUnit(Sequence)}) for the specified ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the perimeter
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
     * @return the perimeter
     * @throws UnsupportedOperationException
     *         if the specified Z, T or C position are not supported for this descriptor or if
     *         perimeter calculation is not supported on this ROI.
     */
    public static double computePerimeter(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        return computePerimeter(ContourROIDescriptor.computeContour(roi, z, t, c), roi, sequence);
    }

    /**
     * Computes and returns the perimeter from a given number of contour points expressed in the
     * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param contourPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the perimeter
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size
     *        information.
     * @throws UnsupportedOperationException
     *         if the perimeter calculation for the specified dimension is not supported by the ROI
     */
    static double computePerimeter(double contourPoints, ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return ContourROIDescriptor.computeContour(contourPoints, roi, sequence, 2);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation on the ROI: "
                    + roi.getName());
        }
    }
}
