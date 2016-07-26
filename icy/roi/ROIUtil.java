/*
 * Copyright 2010-2015 Institut Pasteur.
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
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.system.IcyExceptionHandler;
import icy.type.DataIteratorUtil;
import icy.type.point.Point3D;
import icy.type.point.Point4D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.StringUtil;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plugins.kernel.roi.descriptor.intensity.ROIIntensityDescriptorsPlugin;
import plugins.kernel.roi.descriptor.intensity.ROIMaxIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMeanIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMinIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIStandardDeviationDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROISumIntensityDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIBasicMeasureDescriptorsPlugin;
import plugins.kernel.roi.descriptor.measure.ROIContourDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIInteriorDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterDescriptorsPlugin;
import plugins.kernel.roi.descriptor.measure.ROIPerimeterDescriptor;
import plugins.kernel.roi.descriptor.measure.ROISurfaceAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIVolumeDescriptor;

/**
 * ROI utilities class.
 * 
 * @author Stephane
 */
public class ROIUtil
{
    /**
     * Returns all available ROI descriptors (see {@link ROIDescriptor}) and their attached plugin
     * (see {@link PluginROIDescriptor}).<br/>
     * This list can be extended by installing new plugin(s) implementing the {@link PluginROIDescriptor} interface.<br/>
     * This method is an alias of {@link ROIDescriptor#getDescriptors()}
     * 
     * @see ROIDescriptor#compute(ROI, Sequence)
     * @see PluginROIDescriptor#compute(ROI, Sequence)
     */
    public static Map<ROIDescriptor, PluginROIDescriptor> getROIDescriptors()
    {
        return ROIDescriptor.getDescriptors();
    }

    /**
     * Computes the specified descriptor from the input {@link ROIDescriptor} set on given ROI
     * and returns the result (or <code>null</code> if the descriptor is not found).<br/>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(Set, String, ROI, Sequence)}
     * 
     * @param roiDescriptors
     *        the input {@link ROIDescriptor} set (see {@link #getROIDescriptors()} method)
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(Set<ROIDescriptor> roiDescriptors, String descriptorId, ROI roi,
            Sequence sequence)
    {
        return ROIDescriptor.computeDescriptor(roiDescriptors, descriptorId, roi, sequence);
    }

    /**
     * Computes the specified descriptor on given ROI and returns the result (or <code>null</code> if the descriptor is
     * not found).<br/>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(String, ROI, Sequence)}
     * 
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(String descriptorId, ROI roi, Sequence sequence)
    {
        return ROIDescriptor.computeDescriptor(descriptorId, roi, sequence);
    }

    /**
     * @deprecated Use {@link ROIStandardDeviationDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
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
     * @deprecated Use {@link ROIIntensityDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
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
     * Returns the number of sequence pixels contained in the specified ROI.
     * 
     * @param sequence
     *        The sequence we want to get the number of pixel.
     * @param roi
     *        The ROI define the region where we want to compute the number of pixel.
     * @param z
     *        The specific Z position (slice) where we want to compute the number of pixel or <code>-1</code> to use the
     *        ROI Z dimension information.
     * @param t
     *        The specific T position (frame) where we want to compute the number of pixel or <code>-1</code> to use the
     *        ROI T dimension information.
     * @param c
     *        The specific C position (channel) where we want to compute the number of pixel or <code>-1</code> to use
     *        the ROI C dimension information.
     */
    public static long getNumPixel(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorUtil.count(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROIMinIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMinIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.min(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROIMaxIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMaxIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.max(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROIMeanIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMeanIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.mean(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROISumIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getSumIntensity(Sequence sequence, ROI roi, int z, int t, int c)
    {
        return DataIteratorMath.sum(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROIStandardDeviationDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static double getStandardDeviation(Sequence sequence, ROI roi)
    {
        return getStandardDeviation(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIIntensityDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi)
    {
        return getIntensityInfo(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the number of sequence pixels contained in the specified ROI.
     */
    public static long getNumPixel(Sequence sequence, ROI roi)
    {
        return getNumPixel(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIMinIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMinIntensity(Sequence sequence, ROI roi)
    {
        return getMinIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIMaxIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMaxIntensity(Sequence sequence, ROI roi)
    {
        return getMaxIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIMeanIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMeanIntensity(Sequence sequence, ROI roi)
    {
        return getMeanIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROISumIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getSumIntensity(Sequence sequence, ROI roi)
    {
        return getSumIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point5D getMassCenter(ROI roi)
    {
        switch (roi.getDimension())
        {
            case 2:
                final ROI2D roi2d = (ROI2D) roi;
                final Point2D pt2d = getMassCenter(roi2d);
                return new Point5D.Double(pt2d.getX(), pt2d.getY(), roi2d.getZ(), roi2d.getT(), roi2d.getC());

            case 3:
                final ROI3D roi3d = (ROI3D) roi;
                final Point3D pt3d = getMassCenter(roi3d);
                return new Point5D.Double(pt3d.getX(), pt3d.getY(), pt3d.getZ(), roi3d.getT(), roi3d.getC());

            case 4:
                final ROI4D roi4d = (ROI4D) roi;
                final Point4D pt4d = getMassCenter(roi4d);
                return new Point5D.Double(pt4d.getX(), pt4d.getY(), pt4d.getZ(), pt4d.getT(), roi4d.getC());

            case 5:
                return getMassCenter((ROI5D) roi);

            default:
                return null;
        }
    }

    /**
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point2D getMassCenter(ROI2D roi)
    {
        double x = 0, y = 0;
        long len = 0;

        final BooleanMask2D mask = roi.getBooleanMask(true);
        final boolean m[] = mask.mask;
        final int h = mask.bounds.height;
        final int w = mask.bounds.width;

        int off = 0;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                if (m[off++])
                {
                    x += i;
                    y += j;
                    len++;
                }
            }
        }

        // get bounds
        final Rectangle2D bounds2D = roi.getBounds2D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point2D.Double(bounds2D.getCenterX(), bounds2D.getCenterY());

        return new Point2D.Double(bounds2D.getX() + (x / len), bounds2D.getY() + (y / len));
    }

    /**
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point3D getMassCenter(ROI3D roi)
    {
        double x = 0, y = 0, z = 0;
        long len = 0;
        final BooleanMask3D mask3d = roi.getBooleanMask(true);

        for (Integer zSlice : mask3d.mask.keySet())
        {
            final int zi = zSlice.intValue();
            final double zd = zi;
            final BooleanMask2D mask = mask3d.getMask2D(zi);
            final boolean m[] = mask.mask;
            final double bx = mask.bounds.x;
            final double by = mask.bounds.y;
            final int h = mask.bounds.height;
            final int w = mask.bounds.width;

            int off = 0;
            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    if (m[off++])
                    {
                        x += bx + i;
                        y += by + j;
                        z += zd;
                        len++;
                    }
                }
            }
        }

        // get bounds
        final Rectangle3D bounds3D = roi.getBounds3D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point3D.Double(bounds3D.getCenterX(), bounds3D.getCenterY(), bounds3D.getCenterZ());

        return new Point3D.Double((x / len), (y / len), (z / len));
    }

    /**
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point4D getMassCenter(ROI4D roi)
    {
        final BooleanMask4D mask4d = roi.getBooleanMask(true);
        double x = 0, y = 0, z = 0, t = 0;
        long len = 0;

        for (Integer tFrame : mask4d.mask.keySet())
        {
            final int ti = tFrame.intValue();
            final double td = ti;
            final BooleanMask3D mask3d = mask4d.getMask3D(ti);

            for (Integer zSlice : mask3d.mask.keySet())
            {
                final int zi = zSlice.intValue();
                final double zd = zi;
                final BooleanMask2D mask = mask3d.getMask2D(zi);
                final boolean m[] = mask.mask;
                final double bx = mask.bounds.x;
                final double by = mask.bounds.y;
                final int h = mask.bounds.height;
                final int w = mask.bounds.width;

                int off = 0;
                for (int j = 0; j < h; j++)
                {
                    for (int i = 0; i < w; i++)
                    {
                        if (m[off++])
                        {
                            x += bx + i;
                            y += by + j;
                            z += zd;
                            t += td;
                            len++;
                        }
                    }
                }
            }
        }

        // get bounds
        final Rectangle4D bounds4D = roi.getBounds4D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point4D.Double(bounds4D.getCenterX(), bounds4D.getCenterY(), bounds4D.getCenterZ(),
                    bounds4D.getCenterT());

        return new Point4D.Double((x / len), (y / len), (z / len), (t / len));

    }

    /**
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point5D getMassCenter(ROI5D roi)
    {
        final BooleanMask5D mask5d = roi.getBooleanMask(true);
        double x = 0, y = 0, z = 0, t = 0, c = 0;
        long len = 0;

        for (Integer cChannel : mask5d.mask.keySet())
        {
            final int ci = cChannel.intValue();
            final double cd = ci;
            final BooleanMask4D mask4d = mask5d.getMask4D(ci);

            for (Integer tFrame : mask4d.mask.keySet())
            {
                final int ti = tFrame.intValue();
                final double td = ti;
                final BooleanMask3D mask3d = mask4d.getMask3D(ti);

                for (Integer zSlice : mask3d.mask.keySet())
                {
                    final int zi = zSlice.intValue();
                    final double zd = zi;
                    final BooleanMask2D mask = mask3d.getMask2D(zi);
                    final boolean m[] = mask.mask;
                    final double bx = mask.bounds.x;
                    final double by = mask.bounds.y;
                    final int h = mask.bounds.height;
                    final int w = mask.bounds.width;

                    int off = 0;
                    for (int j = 0; j < h; j++)
                    {
                        for (int i = 0; i < w; i++)
                        {
                            if (m[off++])
                            {
                                x += bx + i;
                                y += by + j;
                                z += zd;
                                t += td;
                                c += cd;
                                len++;
                            }
                        }
                    }
                }
            }
        }

        // get bounds
        final Rectangle5D bounds5D = roi.getBounds5D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point5D.Double(bounds5D.getCenterX(), bounds5D.getCenterY(), bounds5D.getCenterZ(),
                    bounds5D.getCenterT(), bounds5D.getCenterC());

        return new Point5D.Double((x / len), (y / len), (z / len), (t / len), (c / len));
    }

    /**
     * @deprecated
     */
    @Deprecated
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
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, double contourPoints, ROI roi, int dim, int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(contourPoints, roundSignificant) * mul, dim,
                    dim - 1, 5);

        return "";
    }

    /**
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
    {
        return getContourSize(sequence, roi.getNumberOfContourPoints(), roi, dim, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, ROI roi, int dim)
    {
        return getContourSize(sequence, roi, dim, 0);
    }

    /**
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, double interiorPoints, ROI roi, int dim,
            int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(interiorPoints, roundSignificant) * mul, dim, dim,
                    5);

        return "";
    }

    /**
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
    {
        return getInteriorSize(sequence, roi.getNumberOfPoints(), roi, dim, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim)
    {
        return getInteriorSize(sequence, roi, dim, 0);
    }

    /**
     * @deprecated Use {@link ROIPerimeterDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getPerimeter(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getContourSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROIPerimeterDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getPerimeter(Sequence sequence, ROI roi)
    {
        return getPerimeter(sequence, roi, 0);
    }

    /**
     * @deprecated Use {@link ROIAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getArea(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getInteriorSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROIAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getArea(Sequence sequence, ROI roi)
    {
        return getArea(sequence, roi, 0);
    }

    /**
     * @deprecated Use {@link ROISurfaceAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getSurfaceArea(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getContourSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROISurfaceAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getSurfaceArea(Sequence sequence, ROI roi)
    {
        return getSurfaceArea(sequence, roi, 0);
    }

    /**
     * @deprecated Use {@link ROIVolumeDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getVolume(Sequence sequence, ROI roi, int roundSignificant)
    {
        return getInteriorSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * @deprecated Use {@link ROIVolumeDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getVolume(Sequence sequence, ROI roi)
    {
        return getVolume(sequence, roi, 0);
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
    public static ROI merge(List<? extends ROI> rois, BooleanOperator operator) throws UnsupportedOperationException
    {
        if (rois.size() == 0)
            return null;

        ROI result = rois.get(0).getCopy();

        // copy can fail...
        if (result != null)
        {
            switch (operator)
            {
                case AND:
                    for (int i = 1; i < rois.size(); i++)
                        result = result.intersect(rois.get(i), true);
                    break;
                case OR:
                    for (int i = 1; i < rois.size(); i++)
                        result = result.add(rois.get(i), true);
                    break;
                case XOR:
                    for (int i = 1; i < rois.size(); i++)
                        result = result.exclusiveAdd(rois.get(i), true);
                    break;
            }
        }

        // for (int i = 1; i < rois.size(); i++)
        // {
        // final ROI roi = rois.get(i);
        //
        // switch (operator)
        // {
        // case AND:
        // result = result.getIntersection(roi);
        // break;
        // case OR:
        // result = result.getUnion(roi);
        // break;
        // case XOR:
        // result = result.getExclusiveUnion(roi);
        // break;
        // }
        // }

        return result;
    }

    /**
     * Builds and returns a ROI corresponding to the union of the specified ROI list.
     */
    public static ROI getUnion(List<? extends ROI> rois) throws UnsupportedOperationException
    {
        return merge(rois, BooleanOperator.OR);
    }

    /**
     * Builds and returns a ROI corresponding to the exclusive union of the specified ROI list.
     */
    public static ROI getExclusiveUnion(List<? extends ROI> rois) throws UnsupportedOperationException
    {
        return merge(rois, BooleanOperator.XOR);
    }

    /**
     * Builds and returns a ROI corresponding to the intersection of the specified ROI list.
     */
    public static ROI getIntersection(List<? extends ROI> rois) throws UnsupportedOperationException
    {
        return merge(rois, BooleanOperator.AND);
    }

    /**
     * Subtract the content of the roi2 from the roi1 and return the result as a new {@link ROI}.<br>
     * This is equivalent to: <code>roi1.getSubtraction(roi2)</code>
     * 
     * @return {@link ROI} representing the result of subtraction.
     */
    public static ROI subtract(ROI roi1, ROI roi2) throws UnsupportedOperationException
    {
        return roi1.getSubtraction(roi2);
    }

}
