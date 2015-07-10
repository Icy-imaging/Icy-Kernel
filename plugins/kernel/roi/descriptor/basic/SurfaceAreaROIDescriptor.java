/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Surface area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class SurfaceAreaROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Surface area";

    public SurfaceAreaROIDescriptor()
    {
        super(ID, "Surface Area", Double.class);
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
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeSurfaceArea(roi, sequence));
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
     * @return the surface area
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeSurfaceArea(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (!(roi instanceof ROI3D))
            throw new UnsupportedOperationException("Perimeter not supported for ROI" + roi.getDimension() + "D !");

        return computeSurfaceArea(ContourROIDescriptor.computeContour(roi), roi, sequence);
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
     * @return the surface area
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeSurfaceArea(double contourPoints, ROI roi, Sequence sequence)
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
