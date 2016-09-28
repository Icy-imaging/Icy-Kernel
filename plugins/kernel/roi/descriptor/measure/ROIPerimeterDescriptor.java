/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.util.StringUtil;

/**
 * Perimeter ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPerimeterDescriptor extends ROIDescriptor
{
    public static final String ID = "Perimeter";

    public ROIPerimeterDescriptor()
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
            return sequence.getBestPixelSizeUnit(2, 1).toString() + "m";

        return UnitPrefix.MICRO.toString() + "m";
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
        return Double.valueOf(computePerimeter(roi, sequence));
    }

    /**
     * Computes and returns the perimeter expressed in the unit of the descriptor (see {@link #getUnit(Sequence)}) for
     * the specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the perimeter
     * @param sequence
     *        the sequence from which the pixel size can be retrieved
     * @return the perimeter expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computePerimeter(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (sequence == null)
            throw new UnsupportedOperationException("Cannot compute Perimeter with null Sequence parameter !");

        final UnitPrefix bestUnit = sequence.getBestPixelSizeUnit(2, 1);
        final double length = roi.getLength(sequence);

        return UnitUtil.getValueInUnit(length, UnitPrefix.MICRO, bestUnit, 1);
    }

    // /**
    // * Computes and returns the perimeter from a given number of contour points expressed in the
    // * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
    // * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
    // *
    // * @param contourPoints
    // * the number of contour points (override the ROI value)
    // * @param roi
    // * the ROI we want to compute the perimeter
    // * @param sequence
    // * the input sequence used to retrieve operation unit by using pixel size
    // * information.
    // * @return the perimeter
    // * @throws UnsupportedOperationException
    // * if the operation is not supported for this ROI
    // */
    // static double computePerimeter(double contourPoints, ROI roi, Sequence sequence)
    // throws UnsupportedOperationException
    // {
    // return ROIContourDescriptor.computeContour(contourPoints, roi, sequence, 2);
    // }
}
