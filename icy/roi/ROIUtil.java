/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.roi;

import icy.image.IntensityInfo;
import icy.math.DataIteratorMath;
import icy.math.MathUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.DataIteratorUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.ShapeUtil.BooleanOperator;

import java.util.List;

/**
 * ROI utilities class.
 * 
 * @author Stephane
 */
public class ROIUtil
{

    /**
     * Computes and returns the standard deviation for the specified sequence region.
     * 
     * @param sequence
     *        The sequence we want to get the intensity informations.
     * @param roi
     *        The ROI define the region where we want to compute the standard deviation.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute the standard
     *        deviation.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute the standard
     *        deviation.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute the standard
     *        deviation.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static double getStandardDeviation(Sequence sequence, ROI roi, int z, int t, int c)
    {
        try
        {
            final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false, z, t, c);

            long numPixels = 0;
            double sum = 0;
            double sum2 = 0;

            // faster to do all calculation in a single iteration run
            while (!it.done())
            {
                final double value = it.get();

                sum += value;
                sum2 += value * value;
                numPixels++;

                it.next();
            }

            if (numPixels > 0)
            {
                double x1 = (sum2 / numPixels);
                double x2 = sum / numPixels;
                x2 *= x2;

                return Math.sqrt(x1 - x2);
            }
        }
        catch (Exception e)
        {
            // we can have exception as the process can be really long
            // and size modified during this period
        }

        return 0d;
    }

    /**
     * Computes and returns the min, max, mean intensity for the specified sequence region.<br>
     * It can returns <code>null</code> if the sequence or the ROI has changed during the operation.
     * 
     * @param sequence
     *        The sequence we want to get the intensity informations.
     * @param roi
     *        The ROI define the region where we want to compute intensity information.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute intensity
     *        information.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute intensity
     *        information.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute intensity
     *        information.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi, int z, int t, int c)
    {
        try
        {
            final IntensityInfo result = new IntensityInfo();
            final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false, z, t, c);

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
     * 
     * @param sequence
     *        The sequence we want to get the number of pixel.
     * @param roi
     *        The ROI define the region where we want to compute the number of pixel.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute the number of
     *        pixel.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute the number of
     *        pixel.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute the number of
     *        pixel.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static long getNumPixel(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorUtil.count(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the minimum pixel intensity contained in the ROI of specified sequence.
     * 
     * @param sequence
     *        The sequence we want to get the min intensity information.
     * @param roi
     *        The ROI define the region where we want to compute min intensity.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute min intensity.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute min intensity.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute min intensity.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static double getMinIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.min(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the maximum pixel intensity contained in the ROI of specified sequence.
     * 
     * @param sequence
     *        The sequence we want to get the max intensity information.
     * @param roi
     *        The ROI define the region where we want to compute max intensity.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute max intensity.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute max intensity.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute max intensity.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static double getMaxIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.max(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the mean pixel intensity contained in the ROI of specified sequence.
     * 
     * @param sequence
     *        The sequence we want to get the mean intensity.
     * @param roi
     *        The ROI define the region where we want to compute mean intensity.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute mean
     *        intensity.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute mean
     *        intensity.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute mean
     *        intensity.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static double getMeanIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.mean(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the sum of all pixel intensity contained in the ROI of specified sequence.
     * 
     * @param sequence
     *        The sequence we want to get the intensity sum.
     * @param roi
     *        The ROI define the region where we want to compute intensity sum.
     * @param z
     *        The specific Z position (contained in the ROI) where we want to compute intensity sum.<br>
     *        Set to -1 to use the whole ROI Z information instead.
     * @param t
     *        The specific T position (contained in the ROI) where we want to compute intensity sum.<br>
     *        Set to -1 to use the whole ROI T information instead.
     * @param c
     *        The specific C position (contained in the ROI) where we want to compute intensity sum.<br>
     *        Set to -1 to use the whole ROI C information instead.
     */
    public static double getSumIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.sum(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the min, max, mean intensity for the specified sequence region.
     * 
     * @param sequence
     *        The sequence we want to get the intensity informations.
     * @param roi
     *        The ROI define the region where we want to compute intensity information.
     */
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi)
    {
        return getIntensityInfo(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the number of pixel contained in the ROI of specified sequence.
     */
    public static long getNumPixel(Sequence sequence, ROI roi)
    {
        return getNumPixel(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the minimum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMinIntensity(Sequence sequence, ROI roi)
    {
        return getMinIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the maximum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMaxIntensity(Sequence sequence, ROI roi)
    {
        return getMaxIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the mean pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMeanIntensity(Sequence sequence, ROI roi)
    {
        return getMeanIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the sum of all pixel intensity contained in the ROI of specified sequence.
     */
    public static double getSumIntensity(Sequence sequence, ROI roi)
    {
        return getSumIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * Merge the specified array of {@link ROI} with the given {@link BooleanOperator}.<br>
     * 
     * @param rois
     *        ROIs we want to merge.
     * @param operator
     *        {@link BooleanOperator} to apply.
     * @return {@link ROI} representing the result of the merge operation.
     */
    public static ROI merge(List<ROI> rois, BooleanOperator operator)
    {
        if (rois.size() == 0)
            return null;

        ROI result = rois.get(0);

        for (int i = 1; i < rois.size(); i++)
        {
            final ROI roi = rois.get(i);

            switch (operator)
            {
                case AND:
                    result = result.getIntersection(roi);
                    break;
                case OR:
                    result = result.getUnion(roi);
                    break;
                case XOR:
                    result = result.getExclusiveUnion(roi);
                    break;
            }
        }

        return result;
    }

    /**
     * Builds and returns a ROI corresponding to the union of the specified ROI list.
     */
    public static ROI getUnion(List<ROI> rois)
    {
        return merge(rois, BooleanOperator.OR);
    }

    /**
     * Builds and returns a ROI corresponding to the exclusive union of the specified ROI list.
     */
    public static ROI getExclusiveUnion(List<ROI> rois)
    {
        return merge(rois, BooleanOperator.XOR);
    }

    /**
     * Builds and returns a ROI corresponding to the intersection of the specified ROI list.
     */
    public static ROI getIntersection(List<ROI> rois)
    {
        return merge(rois, BooleanOperator.AND);
    }

    /**
     * Subtract the content of the roi2 from the roi1 and return the result as a new {@link ROI}.<br>
     * This is equivalent to: <code>roi1.getSubtraction(roi2)</code>
     * 
     * @return {@link ROI} representing the result of subtraction.
     */
    public static ROI subtract(ROI roi1, ROI roi2)
    {
        return roi1.getSubtraction(roi2);
    }

    /**
     * Calculate the multiplier depending the wanted dimension information.
     */
    private static double getMultiplier(Sequence sequence, ROI roi, int dim)
    {
        final int dimRoi = roi.getDimension();

        // cannot give this information for this roi
        if (dimRoi > dim)
            return 0d;

        final Rectangle5D boundsRoi = roi.getBounds5D();
        double mul = 1d;

        switch (dim)
        {
            case 5:
                if (dimRoi == 4)
                {
                    final int sizeC = sequence.getSizeC();

                    if ((boundsRoi.getSizeC() == Double.POSITIVE_INFINITY) && (sizeC > 1))
                        mul *= sizeC;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 4:
                if (dimRoi == 3)
                {
                    final int sizeT = sequence.getSizeT();

                    if ((boundsRoi.getSizeT() == Double.POSITIVE_INFINITY) && (sizeT > 1))
                        mul *= sizeT;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 3:
                if (dimRoi == 2)
                {
                    final int sizeZ = sequence.getSizeZ();

                    if ((boundsRoi.getSizeZ() == Double.POSITIVE_INFINITY) && (sizeZ > 1))
                        mul *= sizeZ;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 2:
                if (dimRoi == 1)
                {
                    final int sizeY = sequence.getSizeY();

                    if ((boundsRoi.getSizeY() == Double.POSITIVE_INFINITY) && (sizeY > 1))
                        mul *= sizeY;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
        }

        return mul;
    }

    /**
     * Returns the contour size for specified sequence and dimension from a given number of contour
     * points.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getContourSize(sequence, roi, 2, 5) --> "0.15 mm" (equivalent to perimeter)</li>
     * <li>getContourSize(sequence, roi, 3, 5) --> "0.028 µm2" (equivalent to surface area)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param contourPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the contour size
     * @param dim
     *        the dimension for the contour size operation (2 = perimeter, 3 = surface area, ...)
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getContourSize(Sequence sequence, double contourPoints, ROI roi, int dim, int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(contourPoints, roundSignificant) * mul, dim - 1, 5);

        return "";
    }

    /**
     * Returns the ROI contour size for specified sequence and dimension.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getContourSize(sequence, roi, 2, 5) --> "0.15 mm" (equivalent to perimeter)</li>
     * <li>getContourSize(sequence, roi, 3, 5) --> "0.028 µm2" (equivalent to surface area)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the contour size
     * @param dim
     *        the dimension for the contour size operation (2 = perimeter, 3 = surface area, ...)
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getContourSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
    {
        return getContourSize(sequence, roi.getNumberOfContourPoints(), roi, dim, roundSignificant);
    }

    /**
     * Returns the ROI contour size for specified sequence and dimension.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getContourSize(sequence, roi, 2) --> "0.15 mm" (equivalent to perimeter)</li>
     * <li>getContourSize(sequence, roi, 3) --> "0.028 µm2" (equivalent to surface area)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the contour size
     * @param dim
     *        the dimension for the contour size operation (2 = perimeter, 3 = surface area, ...)
     */
    public static String getContourSize(Sequence sequence, ROI roi, int dim)
    {
        return getContourSize(sequence, roi, dim, 0);
    }

    /**
     * Returns the ROI interior size for specified sequence and dimension from a given number of
     * interior points.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getInteriorSize(sequence, 1287.36, roi, 2, 5) --> "0.15 mm2" (equivalent to area)</li>
     * <li>getInteriorSize(sequence, 643852.125, roi, 3, 5) --> "0.028 µm3" (equivalent to volume)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param interiorPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the interior size
     * @param dim
     *        the dimension for the interior size operation (2 = area, 3 = volume, ...)
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     * @see #getArea(Sequence, ROI)
     * @see #getVolume(Sequence, ROI)
     */
    public static String getInteriorSize(Sequence sequence, double interiorPoints, ROI roi, int dim,
            int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(interiorPoints, roundSignificant) * mul, dim, 5);

        return "";
    }

    /**
     * Returns the ROI interior size for specified sequence and dimension.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getInteriorSize(sequence, roi, 2, 5) --> "0.15 mm2" (equivalent to area)</li>
     * <li>getInteriorSize(sequence, roi, 3, 5) --> "0.028 µm3" (equivalent to volume)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the interior size
     * @param dim
     *        the dimension for the interior size operation (2 = area, 3 = volume, ...)
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     * @see #getArea(Sequence, ROI)
     * @see #getVolume(Sequence, ROI)
     */
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
    {
        return getInteriorSize(sequence, roi.getNumberOfPoints(), roi, dim, roundSignificant);
    }

    /**
     * Returns the ROI interior size for specified sequence and dimension.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * <ul>
     * Ex:
     * <li>getInteriorSize(sequence, roi, 2) --> "0.15 mm2" (equivalent to area)</li>
     * <li>getInteriorSize(sequence, roi, 3) --> "0.028 µm3" (equivalent to volume)</li>
     * </ul>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the interior size
     * @param dim
     *        the dimension for the interior size operation (2 = area, 3 = volume, ...)
     * @see #getArea(Sequence, ROI)
     * @see #getVolume(Sequence, ROI)
     */
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim)
    {
        return getInteriorSize(sequence, roi, dim, 0);
    }

    /**
     * Return perimeter of the specified ROI with the correct unit.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the perimeter
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getPerimeter(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getContourSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * Return perimeter of the specified ROI with the correct unit.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the perimeter
     */
    public static String getPerimeter(Sequence sequence, ROI roi)
    {
        return getPerimeter(sequence, roi, 0);
    }

    /**
     * Return area of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the area
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getArea(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getInteriorSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * Return area of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the area
     */
    public static String getArea(Sequence sequence, ROI roi)
    {
        return getArea(sequence, roi, 0);
    }

    /**
     * Return surface area of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the surface area
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getSurfaceArea(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getContourSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * Return surface area of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the surface area
     */
    public static String getSurfaceArea(Sequence sequence, ROI roi)
    {
        return getSurfaceArea(sequence, roi, 0);
    }

    /**
     * Return volume of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the volume
     * @param roundSignificant
     *        Round result value to specified number of significant digit (0 to keep all precision).
     */
    public static String getVolume(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getInteriorSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * Return volume of the specified ROI.<br>
     * The unit result is expressed depending the sequence pixel size information.<br>
     * It may returns an empty string if the operation is not supported for that ROI.
     * 
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size information.
     * @param roi
     *        the ROI we want to compute the volume
     */
    public static String getVolume(Sequence sequence, ROI roi)
    {
        return getVolume(sequence, roi, 0);
    }
}
