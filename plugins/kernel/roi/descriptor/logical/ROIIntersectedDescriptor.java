/**
 * 
 */
package plugins.kernel.roi.descriptor.logical;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;

/**
 * Number of intersected ROI(s) descriptor (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIIntersectedDescriptor extends ROIDescriptor
{
    public static final String ID = "Intersected";

    public ROIIntersectedDescriptor()
    {
        super(ID, "Intersected", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Number of intersected ROI(s)";
    }

    @Override
    public boolean separateChannel()
    {
        return false;
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        return (change.getSourceType() == SequenceEventSourceType.SEQUENCE_ROI);
    }

    @Override
    public Object compute(ROI roi, Sequence sequence)
    {
        return Double.valueOf(computeIntersectedROIs(roi, sequence));
    }

    /**
     * Returns the number of intersected ROI (the ones attached to the given Sequence) by the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of intersected ROIs
     * @param sequence
     *        the Sequence containing the ROIs we want to test for intersection (test against itself is automatically discarded)
     */
    public static double computeIntersectedROIs(ROI roi, Sequence sequence)
    {
        if ((roi == null) || (sequence == null))
            return 0;

        int result = 0;
        for (ROI r : sequence.getROIs())
            if ((r != roi) && (r != null) && (roi.intersects(r)))
                result++;

        return result;
    }
}
