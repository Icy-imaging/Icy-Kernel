/**
 * 
 */
package icy.roi;

import icy.image.IntensityInfo;
import icy.math.DataIteratorMath;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.DataIterator;

/**
 * ROI utilities class.
 * 
 * @author Stephane
 */
public class ROIUtil
{

    /**
     * Return a {@link DataIterator} object from the specified {@link ROI} and {@link Sequence}.
     */
    public static SequenceDataIterator getDataIterator(Sequence sequence, ROI roi)
    {
        return new SequenceDataIterator(sequence, roi);
    }

    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi)
    {
        try
        {
            final IntensityInfo result = new IntensityInfo();
            final SequenceDataIterator it = getDataIterator(sequence, roi);

            long numPixels = 0;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            double sum = 0;

            // faster to do all calculation in a single iteration run
            while (!it.done())
            {
                final double value = it.get();

                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
                sum += value;
                numPixels++;

                it.next();
            }

            if (numPixels > 0)
            {
                result.minIntensity = min;
                result.maxIntensity = max;
                result.meanIntensity = sum / numPixels;
            }
            else
            {
                result.minIntensity = 0d;
                result.maxIntensity = 0d;
                result.meanIntensity = 0d;
            }

            return result;
        }
        catch (Exception e)
        {
            // we can have exception as the process can be really long
            // and size modified during this period
            return null;
        }
    }

    /**
     * Returns the number of pixel contained in the ROI of specified sequence.
     */
    public static long getNumPixel(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.count(getDataIterator(sequence, roi));
    }

    /**
     * Returns the minimum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMinIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.min(getDataIterator(sequence, roi));
    }

    /**
     * Returns the maximum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMaxIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.max(getDataIterator(sequence, roi));
    }

    /**
     * Returns the mean pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMeanIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.mean(getDataIterator(sequence, roi));
    }

    /**
     * Returns the sum of all pixel intensity contained in the ROI of specified sequence.
     */
    public static double getSumIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.sum(getDataIterator(sequence, roi));
    }
}
