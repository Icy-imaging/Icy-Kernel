/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Contour ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIContourDescriptor extends ROIDescriptor
{
    public static final String ID = "Contour";

    public ROIContourDescriptor()
    {
        super(ID, "Contour", Double.class);
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        return "px";
    }

    @Override
    public String getDescription()
    {
        return "Number of points for the contour";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeContour(roi));
    }

    /**
     * Returns the number of contour point for the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of contour point
     * @return the number of contour point
     */
    public static double computeContour(ROI roi)
    {
        return roi.getNumberOfContourPoints();
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
     * @return the number of contour point
     * @see Sequence#getBestPixelSizeUnit(int, int)
     * @throws UnsupportedOperationException
     *         if the contour calculation for the specified dimension is not supported by the ROI
     */
    static double computeContour(double contourPoints, ROI roi, Sequence sequence, int dim)
            throws UnsupportedOperationException
    {
        final double mul = ROIBasicMeasureDescriptorsPlugin.getMultiplierFactor(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul == 0d)
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation for dimension " + dim
                    + " on the ROI: " + roi.getName());

        return sequence.calculateSizeBestUnit(contourPoints * mul, dim, dim - 1);
    }
}
