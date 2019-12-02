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
 * Number of contained ROI(s) descriptor (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIContainedDescriptor extends ROIDescriptor
{
    public static final String ID = "Contained";

    public ROIContainedDescriptor()
    {
        super(ID, "Contained", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Number of contained ROI(s)";
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
        return Double.valueOf(computeContainedROIs(roi, sequence));
    }

    /**
     * Returns the number of contained ROI (the ones attached to the given Sequence) by the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of contained ROIs
     * @param sequence
     *        the Sequence containing the ROIs we want to test for <i>contain</i> operation (test against itself is automatically discarded)
     */
    public static double computeContainedROIs(ROI roi, Sequence sequence)
    {
        if ((roi == null) || (sequence == null))
            return 0;

        int result = 0;
        for (ROI r : sequence.getROIs())
            if ((r != roi) && (r != null) && (roi.contains(r)))
                result++;

        return result;
    }
}
