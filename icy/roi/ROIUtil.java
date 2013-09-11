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
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.DataIteratorUtil;
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
     * Returns the min, max, mean intensity for the specified sequence region.
     * 
     * @param sequence
     *        The sequence we want to get the intensity informations.
     * @param roi
     *        The ROI define the region where we want to compute intensity information.
     * @param c
     *        The C position where we want to compute intensity information.<br>
     *        If set to -1 we use the ROI C information instead.
     * @param z
     *        The Z position where we want to compute intensity information.<br>
     *        If set to -1 we use the ROI Z information instead.
     * @param t
     *        The T position where we want to compute intensity information.<br>
     *        If set to -1 we use the ROI T information instead.
     */
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi, int z, int t, int c)
    {
        try
        {
            final IntensityInfo result = new IntensityInfo();
            final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false, -1, -1, c, z, t);

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
     * Returns the min, max, mean intensity for the specified sequence region.
     * 
     * @param sequence
     *        The sequence we want to get the intensity informations.
     * @param roi
     *        The ROI define the region where we want to compute intensity information.
     */
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi)
    {
        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;
            return getIntensityInfo(sequence, roi, roi2d.getZ(), roi2d.getT(), roi2d.getC());
        }
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;
            return getIntensityInfo(sequence, roi, -1, roi3d.getT(), roi3d.getC());
        }
        if (roi instanceof ROI4D)
        {
            final ROI4D roi4d = (ROI4D) roi;
            return getIntensityInfo(sequence, roi, -1, -1, roi4d.getC());
        }

        return getIntensityInfo(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the number of pixel contained in the ROI of specified sequence.
     */
    public static long getNumPixel(Sequence sequence, ROI roi)
    {
        return DataIteratorUtil.count(new SequenceDataIterator(sequence, roi));
    }

    /**
     * Returns the minimum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMinIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.min(new SequenceDataIterator(sequence, roi));
    }

    /**
     * Returns the maximum pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMaxIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.max(new SequenceDataIterator(sequence, roi));
    }

    /**
     * Returns the mean pixel intensity contained in the ROI of specified sequence.
     */
    public static double getMeanIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.mean(new SequenceDataIterator(sequence, roi));
    }

    /**
     * Returns the sum of all pixel intensity contained in the ROI of specified sequence.
     */
    public static double getSumIntensity(Sequence sequence, ROI roi)
    {
        return DataIteratorMath.sum(new SequenceDataIterator(sequence, roi));
    }

    //
    // /**
    // * Merge the specified array of {@link icy.roi.roi2d.ROI2DShape} with the given
    // * {@link BooleanOperator}.<br>
    // *
    // * @param rois
    // * ROIs we want to merge.
    // * @param operator
    // * {@link BooleanOperator} to apply.
    // * @return {@link icy.roi.roi2d.ROI2DPath} representing the result of the ROI merge operation.
    // */
    // public static icy.roi.roi2d.ROI2DPath mergeROI2DShape(List<icy.roi.roi2d.ROI2DShape> rois,
    // BooleanOperator operator)
    // {
    // final List<Shape> shapes = new ArrayList<Shape>(rois.size());
    //
    // for (icy.roi.roi2d.ROI2DShape roi : rois)
    // shapes.add(roi.getShape());
    //
    // final icy.roi.roi2d.ROI2DPath result = new icy.roi.roi2d.ROI2DPath(ShapeUtil.merge(shapes,
    // operator));
    //
    // switch (operator)
    // {
    // case OR:
    // result.setName("Union");
    // break;
    // case AND:
    // result.setName("Intersection");
    // break;
    // case XOR:
    // result.setName("Exclusive union");
    // break;
    // default:
    // result.setName("Merge");
    // break;
    // }
    //
    // return result;
    // }
    //
    // /**
    // * Merge the specified array of {@link icy.roi.roi2d.ROI2D} with the given
    // * {@link BooleanOperator}.<br>
    // *
    // * @param rois
    // * ROIs we want to merge.
    // * @param operator
    // * {@link BooleanOperator} to apply.
    // * @return {@link icy.roi.roi2d.ROI2DArea} representing the result of the ROI merge operation.
    // */
    // public static icy.roi.roi2d.ROI2D mergeROI2D(List<icy.roi.roi2d.ROI2D> rois, BooleanOperator
    // operator)
    // {
    // final List<Shape> shapes = new ArrayList<Shape>(rois.size());
    //
    // for (icy.roi.roi2d.ROI2D roi : rois)
    // shapes.add(roi.getShape());
    //
    // final icy.roi.roi2d.ROI2DPath result = new icy.roi.roi2d.ROI2DPath(ShapeUtil.merge(shapes,
    // operator));
    //
    // switch (operator)
    // {
    // case OR:
    // result.setName("Union");
    // break;
    // case AND:
    // result.setName("Intersection");
    // break;
    // case XOR:
    // result.setName("Exclusive union");
    // break;
    // default:
    // result.setName("Merge");
    // break;
    // }
    //
    // return result;
    // }

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
}
