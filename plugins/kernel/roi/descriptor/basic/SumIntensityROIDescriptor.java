/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Sum intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class SumIntensityROIDescriptor extends ROIDescriptor
{
    public static final String ID = "SumIntensity";

    public SumIntensityROIDescriptor()
    {
        super(ID, "Sum Intensity", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Sum intensity";
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
            return IntensityROIDescriptorsPlugin.computeIntensityDescriptors(roi, sequence).sum;
        }
        catch (Exception e)
        {
            return 0d;
        }
    }
}
