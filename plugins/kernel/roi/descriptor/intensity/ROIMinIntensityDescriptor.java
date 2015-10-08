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
 * Minimum intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIMinIntensityDescriptor extends ROIDescriptor
{
    public static final String ID = "MinIntensity";

    public ROIMinIntensityDescriptor()
    {
        super(ID, "Min Intensity", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Minimum intensity";
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
        return Double.valueOf(computeMinIntensity(roi, sequence));
    }

    /**
     * Computes and returns the minimum intensity for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the minimum intensity
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeMinIntensity(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return ROIIntensityDescriptorsPlugin.computeIntensityDescriptors(roi, sequence, false).min;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(ROIMinIntensityDescriptor.class.getSimpleName() + ": cannot compute descriptors for '"
                    + roi.getName() + "'", e);
        }
    }
}
