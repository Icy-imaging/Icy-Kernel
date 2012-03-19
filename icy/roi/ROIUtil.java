/**
 * 
 */
package icy.roi;

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
    public static DataIterator getDataIterator(Sequence sequence, ROI roi)
    {
        return new SequenceDataIterator(sequence, roi);
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
