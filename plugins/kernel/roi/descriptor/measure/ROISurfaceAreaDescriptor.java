/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.util.StringUtil;

/**
 * Surface area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISurfaceAreaDescriptor extends ROIDescriptor
{
    public static final String ID = "Surface area";

    public ROISurfaceAreaDescriptor()
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
            return sequence.getBestPixelSizeUnit(3, 2).toString() + "m2";

        return UnitPrefix.MICRO.toString() + "m2";
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        final SequenceEventSourceType sourceType = change.getSourceType();

        if (sourceType == SequenceEventSourceType.SEQUENCE_DATA)
            return true;
        if (sourceType == SequenceEventSourceType.SEQUENCE_META)
        {
            final String metaName = (String) change.getSource();

            return StringUtil.isEmpty(metaName) || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_X)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Y)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Z);
        }

        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeSurfaceArea(roi, sequence));
    }

    /**
     * Computes and returns the surface area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * for the specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the surface area
     * @param sequence
     *        the sequence from which the pixel size can be retrieved
     * @return the surface area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeSurfaceArea(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (!(roi instanceof ROI3D))
            throw new UnsupportedOperationException("Surface area not supported on " + roi.getDimension() + "D ROI !");
        if (sequence == null)
            throw new UnsupportedOperationException("Cannot compute Surface area with null Sequence parameter !");

        final UnitPrefix bestUnit = sequence.getBestPixelSizeUnit(3, 2);
        final double surfaceArea = ((ROI3D) roi).getSurfaceArea(sequence);

        return UnitUtil.getValueInUnit(surfaceArea, UnitPrefix.MICRO, bestUnit, 2);
    }

    // /**
    // * Computes and returns the surface area from a given number of contour points expressed in the
    // * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
    // * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
    // *
    // * @param contourPoints
    // * the number of contour points (override the ROI value)
    // * @param roi
    // * the ROI we want to compute the surface area
    // * @param sequence
    // * the input sequence used to retrieve operation unit by using pixel size
    // * information.
    // * @return the surface area
    // * @throws UnsupportedOperationException
    // * if the operation is not supported for this ROI
    // */
    // static double computeSurfaceArea(double contourPoints, ROI roi, Sequence sequence)
    // throws UnsupportedOperationException
    // {
    // return ROIContourDescriptor.computeContour(contourPoints, roi, sequence, 3);
    // }
}
