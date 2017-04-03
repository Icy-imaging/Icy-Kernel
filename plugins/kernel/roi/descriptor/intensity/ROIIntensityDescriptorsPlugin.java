/**
 * 
 */
package plugins.kernel.roi.descriptor.intensity;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the following "intensity" ROI descriptors:<br/>
 * <li>Minimum intensity</li><br/>
 * <li>Mean intensity</li><br/>
 * <li>Maximum intensity</li><br/>
 * <li>Sum intensity</li><br/>
 * <li>Standard deviation</li><br/>
 * 
 * @author Stephane
 */
public class ROIIntensityDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_MIN_INTENSITY = ROIMinIntensityDescriptor.ID;
    public static final String ID_MEAN_INTENSITY = ROIMeanIntensityDescriptor.ID;
    public static final String ID_MAX_INTENSITY = ROIMaxIntensityDescriptor.ID;
    public static final String ID_SUM_INTENSITY = ROISumIntensityDescriptor.ID;
    public static final String ID_STANDARD_DEVIATION = ROIStandardDeviationDescriptor.ID;

    public static final ROIMinIntensityDescriptor minIntensityDescriptor = new ROIMinIntensityDescriptor();
    public static final ROIMeanIntensityDescriptor meanIntensityDescriptor = new ROIMeanIntensityDescriptor();
    public static final ROIMaxIntensityDescriptor maxIntensityDescriptor = new ROIMaxIntensityDescriptor();
    public static final ROISumIntensityDescriptor sumIntensityDescriptor = new ROISumIntensityDescriptor();
    public static final ROIStandardDeviationDescriptor standardDeviationDescriptor = new ROIStandardDeviationDescriptor();

    public static class IntensityDescriptorInfos
    {
        public double min;
        public double mean;
        public double max;
        public double sum;
        public double deviation;
    };

    /**
     * Returns the pixel intensity information for the specified ROI and Sequence.<br>
     * Be careful: the returned result may be incorrect or exception may be thrown if the ROI change while the
     * descriptor is being computed.
     * 
     * @param roi
     *        the ROI on which we want to compute the intensity descriptors
     * @param sequence
     *        the Sequence used to compute the intensity descriptors
     * @param allowMultiChannel
     *        Allow multi channel intensity computation. If this parameter is set to <code>false</code> and the ROI
     *        number of channel is > 1 then a {@link UnsupportedOperationException} is launch.
     * @throws Exception
     *         If the ROI dimension changed during the descriptor computation.
     * @throws UnsupportedOperationException
     *         If the C dimension of the ROI is > 1 while allowMultiChannel parameter is set to <code>false</code>
     */
    public static IntensityDescriptorInfos computeIntensityDescriptors(ROI roi, Sequence sequence,
            boolean allowMultiChannel) throws Exception, UnsupportedOperationException
    {
        if (!allowMultiChannel && (roi.getBounds5D().getSizeC() > 1d))
            throw new UnsupportedOperationException(
                    "Not allowed to cannot compute intensity descriptor on a multi channel ROI (sizeC > 1).");

        final IntensityDescriptorInfos result = new IntensityDescriptorInfos();

        long numPixels = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0;
        double sum2 = 0;

        // FIXME: we were using interior pixels only, now we also use edge pixels so we can have intensities info
        // for intersection only ROI --> see if that is a good idea...
        final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, true);

        while (!it.done())
        {
            final double value = it.get();

            if (min > value)
                min = value;
            if (max < value)
                max = value;
            sum += value;
            sum2 += value * value;
            numPixels++;

            it.next();
        }

        if (numPixels > 0)
        {
            result.min = min;
            result.max = max;
            result.sum = sum;

            final double mean = sum / numPixels;
            final double x1 = (sum2 / numPixels);
            final double x2 = mean * mean;

            result.mean = mean;
            result.deviation = Math.sqrt(x1 - x2);
        }
        else
        {
            result.min = 0d;
            result.mean = 0d;
            result.max = 0d;
            result.sum = 0d;
            result.deviation = 0d;
        }

        return result;
    }

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(minIntensityDescriptor);
        result.add(meanIntensityDescriptor);
        result.add(maxIntensityDescriptor);
        result.add(sumIntensityDescriptor);
        result.add(standardDeviationDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();
        try
        {
            // compute intensity descriptors
            final IntensityDescriptorInfos intensityInfos = computeIntensityDescriptors(roi, sequence, false);

            result.put(minIntensityDescriptor, Double.valueOf(intensityInfos.min));
            result.put(meanIntensityDescriptor, Double.valueOf(intensityInfos.mean));
            result.put(maxIntensityDescriptor, Double.valueOf(intensityInfos.max));
            result.put(sumIntensityDescriptor, Double.valueOf(intensityInfos.sum));
            result.put(standardDeviationDescriptor, Double.valueOf(intensityInfos.deviation));
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(getClass().getSimpleName() + ": cannot compute descriptors for '"
                    + roi.getName() + "'", e);
        }

        return result;
    }
}
