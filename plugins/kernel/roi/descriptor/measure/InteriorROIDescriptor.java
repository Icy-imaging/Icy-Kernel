/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

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
    public static final String ID = "Interior";

    public InteriorROIDescriptor()
    {
        super(ID, "Interior", Double.class);
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
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeInterior(roi));
    }

    /**
     * Returns the number of point inside the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of contour point
     * @return the number of point inside the ROI
     */
    public static double computeInterior(ROI roi)
    {
        return roi.getNumberOfPoints();
    }

    /**
     * Returns the interior size from a given number of interior points in the best unit (see
     * {@link Sequence#getBestPixelSizeUnit(int, int)}) for the specified sequence and dimension.<br>
     * <ul>
     * Ex:
     * <li>computeInterior(sequence, roi, 2) return the area value</li>
     * <li>computeInterior(sequence, roi, 3) return the volume value</li>
     * </ul>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param interiorPoints
     *        the number of interior points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the interior size
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param dim
     *        the dimension for the interior size operation (2 = area, 3 = volume, ...)
     * @return the number of point inside the ROI
     * @see Sequence#getBestPixelSizeUnit(int, int)
     * @throws UnsupportedOperationException
     *         if the interior calculation for the specified dimension is not supported by the ROI
     */
    static double computeInterior(double interiorPoints, ROI roi, Sequence sequence, int dim)
            throws UnsupportedOperationException
    {
        final double mul = BasicMeasureROIDescriptorsPlugin.getMultiplierFactor(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul == 0d)
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation for dimension " + dim
                    + " on the ROI: " + roi.getName());

        return sequence.calculateSizeBestUnit(interiorPoints * mul, dim, dim);
    }
}
