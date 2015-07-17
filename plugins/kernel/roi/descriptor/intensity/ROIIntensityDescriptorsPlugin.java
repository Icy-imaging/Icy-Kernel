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
        double min;
        double mean;
        double max;
        double sum;
        double deviation;
    };

    /**
     * Returns the pixel intensity information for the specified ROI and Sequence
     * 
     * @param roi
     *        the ROI on which we want to compute the intensity descriptors
     * @param sequence
     *        the Sequence used to compute the intensity descriptors
     * @throws Exception
     *         If the ROI changed during the descriptor computation
     */
    public static IntensityDescriptorInfos computeIntensityDescriptors(ROI roi, Sequence sequence) throws Exception
    {
        final IntensityDescriptorInfos result = new IntensityDescriptorInfos();
        final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false);

        long numPixels = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0;
        double sum2 = 0;

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
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();
        final ROI r;

        // want a sub part of the ROI ?
        if ((z != -1) || (t != -1) || (c != -1))
            r = roi.getSubROI(z, t, c, false);
        else
            r = roi;

        try
        {
            // compute intensity descriptors
            final IntensityDescriptorInfos intensityInfos = computeIntensityDescriptors(r, sequence);

            result.put(minIntensityDescriptor, Double.valueOf(intensityInfos.min));
            result.put(meanIntensityDescriptor, Double.valueOf(intensityInfos.mean));
            result.put(maxIntensityDescriptor, Double.valueOf(intensityInfos.max));
            result.put(sumIntensityDescriptor, Double.valueOf(intensityInfos.sum));
            result.put(standardDeviationDescriptor, Double.valueOf(intensityInfos.deviation));
        }
        catch (Exception e)
        {
            String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            // sub part of the ROI ?
            if (r != roi)
                mess += " at position [Z=" + z + ",T=" + t + ",C=" + c + "]";

            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
