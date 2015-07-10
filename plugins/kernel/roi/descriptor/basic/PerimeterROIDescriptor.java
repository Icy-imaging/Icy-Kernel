/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Perimeter ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class PerimeterROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Perimeter";

    public PerimeterROIDescriptor()
    {
        super(ID, "Perimeter", Double.class);
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
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computePerimeter(roi, sequence));
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
     * @return the perimeter
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computePerimeter(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (!(roi instanceof ROI2D))
            throw new UnsupportedOperationException("Perimeter not supported for ROI" + roi.getDimension() + "D !");

        return computePerimeter(ContourROIDescriptor.computeContour(roi), roi, sequence);
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
     * @return the perimeter
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computePerimeter(double contourPoints, ROI roi, Sequence sequence)
            throws UnsupportedOperationException
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
