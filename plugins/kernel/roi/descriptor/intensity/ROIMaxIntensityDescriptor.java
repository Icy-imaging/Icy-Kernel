/**
 * 
 */
package plugins.kernel.roi.descriptor.intensity;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;

/**
 * Maximum intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIMaxIntensityDescriptor extends ROIDescriptor
{
    public static final String ID = "MaxIntensity";

    public ROIMaxIntensityDescriptor()
    {
        super(ID, "Max Intensity", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Maximum intensity";
    }

    @Override
    public boolean separateChannel()
    {
        return true;
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        return (change.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA);
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeMaxIntensity(roi, sequence));
    }

    /**
     * Computes and returns the maximum intensity for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the maximum intensity
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeMaxIntensity(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return ROIIntensityDescriptorsPlugin.computeIntensityDescriptors(roi, sequence, false).max;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(ROIMaxIntensityDescriptor.class.getSimpleName()
                    + ": cannot compute descriptors for '" + roi.getName() + "'", e);
        }
    }
}
