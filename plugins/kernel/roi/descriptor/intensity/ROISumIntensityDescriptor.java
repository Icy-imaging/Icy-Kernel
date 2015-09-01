/**
 * 
 */
package plugins.kernel.roi.descriptor.intensity;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Sum intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISumIntensityDescriptor extends ROIDescriptor
{
    public static final String ID = "SumIntensity";

    public ROISumIntensityDescriptor()
    {
        super(ID, "Sum Intensity", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Sum intensity";
    }

    @Override
    public boolean useSequenceData()
    {
        return true;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeSumIntensity(roi, sequence));
    }

    /**
     * Computes and returns the intensity sum for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the intensity sum
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeSumIntensity(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return ROIIntensityDescriptorsPlugin.computeIntensityDescriptors(roi, sequence, false).sum;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(ROISumIntensityDescriptor.class.getSimpleName()
                    + ": cannot compute descriptors for '" + roi.getName() + "'", e);
        }
    }
}
