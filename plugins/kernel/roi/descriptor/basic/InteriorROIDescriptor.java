/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Interior ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class InteriorROIDescriptor extends ROIDescriptor
{
    public static final String ID = BasicROIDescriptorPlugin.ID_INTERIOR;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return "Interior";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        return "px";
    }

    @Override
    public String getDescription()
    {
        return "Number of points for the interior";
    }

    @Override
    public Class<?> getType()
    {
        return Double.class;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence, int z, int t, int c) throws UnsupportedOperationException
    {
        return Double.valueOf(computeInterior(roi, z, t, c));
    }

    /**
     * Returns the number of point inside the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of contour point
     * @param z
     *        the specific Z position (slice) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI Z dimension.
     * @param t
     *        the specific T position (frame) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI T dimension.
     * @param c
     *        the specific C position (channel) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI C dimension.
     * @return the number of contour point
     * @throws UnsupportedOperationException
     *         if the specified Z, T or C position are not supported for this descriptor.
     */
    public static double computeInterior(ROI roi, int z, int t, int c) throws UnsupportedOperationException
    {
        if ((z != -1) || (t != -1) || (c != -1))
        {
            // use the boolean mask
            final BooleanMask2D mask = roi.getBooleanMask2D(z, t, c, false);

            if (mask == null)
                throw new UnsupportedOperationException("Can't process '" + ID
                        + "' calculation on a specific Z, T, C position.");

            // use the contour length of the mask
            return mask.getContourLength();
        }

        return roi.getNumberOfPoints();
    }

    /**
     * Returns the contour size from a given number of contour points in the best unit (see
     * {@link Sequence#getBestPixelSizeUnit(int, int)}) for the specified sequence and dimension.<br>
     * <ul>
     * Ex:
     * <li>getContourSize(sequence, roi, 2) return the perimeter value</li>
     * <li>getContourSize(sequence, roi, 3) return the surface area value</li>
     * </ul>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param contourPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the contour size
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param dim
     *        the dimension for the contour size operation (2 = perimeter, 3 = surface area, ...)
     * @see Sequence#getBestPixelSizeUnit(int, int)
     * @throws UnsupportedOperationException
     *         if the contour calculation for the specified dimension is not supported by the ROI
     */
    static double computeInterior(double contourPoints, ROI roi, Sequence sequence, int dim)
            throws UnsupportedOperationException
    {
        final double mul = BasicROIDescriptorPlugin.getMultiplierFactor(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul == 0d)
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation for dimension " + dim
                    + " on the ROI: " + roi.getName());

        return sequence.calculateSizeBestUnit(contourPoints * mul, dim, dim - 1);
    }
}
