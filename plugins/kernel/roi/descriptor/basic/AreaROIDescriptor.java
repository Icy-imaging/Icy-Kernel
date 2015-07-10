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
 * Area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class AreaROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Area";

    public AreaROIDescriptor()
    {
        super(ID, "Area", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Area";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(2, 2).toString();

        return UnitPrefix.MICRO.toString() + "m2";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeArea(roi, sequence));
    }

    /**
     * Computes and returns the area expressed in the unit of the descriptor (see
     * {@link #getUnit(Sequence)}) for the specified ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the surface area
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the area
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeArea(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (!(roi instanceof ROI2D))
            throw new UnsupportedOperationException("Perimeter not supported for ROI" + roi.getDimension() + "D !");

        return computeArea(InteriorROIDescriptor.computeInterior(roi), roi, sequence);
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
     * @return the area
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeArea(double interiorPoints, ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return InteriorROIDescriptor.computeInterior(interiorPoints, roi, sequence, 2);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation on the ROI: "
                    + roi.getName());
        }
    }
}