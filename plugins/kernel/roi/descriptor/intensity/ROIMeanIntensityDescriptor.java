/**
 * 
 */
package plugins.kernel.roi.descriptor.intensity;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Mean intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIMeanIntensityDescriptor extends ROIDescriptor
{
    public static final String ID = "MeanIntensity";

    public ROIMeanIntensityDescriptor()
    {
        super(ID, "Mean Intensity", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mean intensity";
    }

    @Override
    public boolean useSequenceData()
    {
        return true;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeMeanIntensity(roi, sequence));
    }

    /**
     * Computes and returns the mean intensity for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the mean intensity
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeMeanIntensity(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return ROIIntensityDescriptorsPlugin.computeIntensityDescriptors(roi, sequence, false).mean;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(ROIMeanIntensityDescriptor.class.getSimpleName() + ": cannot compute descriptors for '"
                    + roi.getName() + "'", e);
        }
    }
}
