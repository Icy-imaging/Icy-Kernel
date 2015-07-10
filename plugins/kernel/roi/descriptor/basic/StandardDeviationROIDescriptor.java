/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Standard Deviation intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class StandardDeviationROIDescriptor extends ROIDescriptor
{
    public static final String ID = "StandardDeviation";

    public StandardDeviationROIDescriptor()
    {
        super(ID, "Standard Deviation", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Standard deviation";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeStandardDeviation(roi, sequence));
    }

    /**
     * Computes and returns the compute standard deviation for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the standard deviation
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeStandardDeviation(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return IntensityROIDescriptorsPlugin.computeIntensityDescriptors(roi, sequence).deviation;
        }
        catch (Exception e)
        {
            return 0d;
        }
    }
}
