/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Icy. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package icy.imagej;

import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImage;
import icy.math.ArrayMath;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi2d.ROI2DEllipse;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPath;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi2d.ROI2DShape;

/**
 * ImageJ utilities class.
 * 
 * @author Stephane
 */
public class ImageJUtil
{
    /**
     * Convert the specified native 1D array to supported ImageJ native data array.
     */
    private static Object convertToIJType(Object array, boolean signed)
    {
        // double[] not supported in ImageJ
        if (array instanceof double[])
            return Array1DUtil.arrayToFloatArray(array, signed);
        // long[] not supported in ImageJ
        if (array instanceof long[])
            return Array1DUtil.arrayToShortArray(array, signed);
        // int[] means Color image for ImageJ
        if (array instanceof int[])
            return Array1DUtil.arrayToShortArray(array, signed);

        return Array1DUtil.copyOf(array);
    }

    /**
     * Append the specified {@link IcyBufferedImage} to the given ImageJ {@link ImageStack}.<br>
     * If input {@link ImageStack} is <code>null</code> then a new {@link ImageStack} is returned.
     */
    private static ImageStack appendToStack(IcyBufferedImage img, ImageStack stack)
    {
        final ImageStack result;

        if (stack == null)
            result = new ImageStack(img.getSizeX(), img.getSizeY(), LookUpTable.createGrayscaleColorModel(false));
        else
            result = stack;

        for (int c = 0; c < img.getSizeC(); c++)
            result.addSlice(null, convertToIJType(img.getDataXY(c), img.isSignedDataType()));

        return result;
    }

    /**
     * Convert the specified Icy {@link Sequence} object to {@link ImagePlus}.
     */
    private static ImagePlus createImagePlus(Sequence sequence, ProgressListener progressListener)
    {
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();
        final int len = sizeZ * sizeT;

        int position = 0;
        ImageStack stack = null;

        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                if (progressListener != null)
                    progressListener.notifyProgress(position, len);

                stack = appendToStack(sequence.getImage(t, z), stack);

                position++;
            }
        }

        // return the image
        return new ImagePlus(sequence.getName(), stack);
    }

    /**
     * Calibrate the specified Icy {@link Sequence} from the specified ImageJ {@link Calibration} object.
     */
    private static void calibrateIcySequence(Sequence sequence, Calibration cal)
    {
        if (cal != null)
        {
            if (cal.scaled())
            {
                // TODO : apply unit conversion
                sequence.setPixelSizeX(cal.pixelWidth);
                sequence.setPixelSizeY(cal.pixelHeight);
                sequence.setPixelSizeZ(cal.pixelDepth);
            }

            // TODO : apply unit conversion
            sequence.setTimeInterval(cal.frameInterval);
        }
    }

    /**
     * Calibrate the specified ImageJ {@link ImagePlus} from the specified Icy {@link Sequence}.
     */
    private static void calibrateImageJImage(ImagePlus image, Sequence seq)
    {
        final Calibration cal = image.getCalibration();

        final double psx = seq.getPixelSizeX();
        final double psy = seq.getPixelSizeY();
        final double psz = seq.getPixelSizeZ();

        // different from defaults values ?
        if ((psx != 1d) || (psy != 1d) || (psz != 1d))
        {
            cal.pixelWidth = psx;
            cal.pixelHeight = psy;
            cal.pixelDepth = psz;
            // default unit size icy
            cal.setUnit("µm");
        }

        final double ti = seq.getTimeInterval();
        // different from default value
        if (ti != 0.1d)
        {
            cal.frameInterval = ti;
            cal.setTimeUnit("sec");
        }

        image.setDimensions(seq.getSizeC(), seq.getSizeZ(), seq.getSizeT());
        image.setOpenAsHyperStack(image.getNDimensions() > 3);

        // final ImageProcessor ip = image.getProcessor();
        // ip.setMinAndMax(seq.getChannelMin(0) displayMin, displayMax);
    }

    /**
     * Convert the ImageJ {@link ImagePlus} image at position [Z,T] into an Icy image
     */
    public static IcyBufferedImage convertToIcyBufferedImage(ImagePlus image, int z, int t, int sizeX, int sizeY,
            int sizeC, int type, boolean signed16)
    {
        // set position
        image.setPosition(1, z + 1, t + 1);

        // directly use the buffered image to do the conversion...
        if ((sizeC == 1) && ((type == ImagePlus.COLOR_256) || (type == ImagePlus.COLOR_RGB)))
            return IcyBufferedImage.createFrom(image.getBufferedImage());

        final ImageProcessor ip = image.getProcessor();
        final Object data = Array1DUtil.copyOf(ip.getPixels());
        final DataType dataType = ArrayUtil.getDataType(data);
        final Object[] datas = Array2DUtil.createArray(dataType, sizeC);

        // first channel data (get a copy)
        datas[0] = data;
        // special case of 16 bits signed data --> subtract 32768
        if (signed16)
            datas[0] = ArrayMath.subtract(datas[0], Double.valueOf(32768));

        // others channels data
        for (int c = 1; c < sizeC; c++)
        {
            image.setPosition(c + 1, z + 1, t + 1);
            datas[c] = Array1DUtil.copyOf(image.getProcessor().getPixels());
            // special case of 16 bits signed data --> subtract 32768
            if (signed16)
                datas[c] = ArrayMath.subtract(datas, Double.valueOf(32768));
        }

        // create a single image from all channels
        return new IcyBufferedImage(sizeX, sizeY, datas, signed16);
    }

    /**
     * Convert the ImageJ {@link ImagePlus} image at position [Z,T] into an Icy image
     */
    public static IcyBufferedImage convertToIcyBufferedImage(ImagePlus image, int z, int t)
    {
        final int[] dim = image.getDimensions(true);

        return convertToIcyBufferedImage(image, z, t, dim[0], dim[1], dim[2], image.getType(), image
                .getLocalCalibration().isSigned16Bit());
    }

    /**
     * Convert the specified ImageJ {@link ImagePlus} object to Icy {@link Sequence}
     */
    public static Sequence convertToIcySequence(ImagePlus image, ProgressListener progressListener)
    {
        final Sequence result = new Sequence(image.getTitle());
        final int[] dim = image.getDimensions(true);

        final int sizeX = dim[0];
        final int sizeY = dim[1];
        final int sizeC = dim[2];
        final int sizeZ = dim[3];
        final int sizeT = dim[4];
        final int type = image.getType();
        // only integer signed type allowed in ImageJ is 16 bit signed
        final boolean signed16 = image.getLocalCalibration().isSigned16Bit();

        final int len = sizeZ * sizeT;
        int position = 0;

        result.beginUpdate();
        try
        {
            // convert image
            for (int t = 0; t < sizeT; t++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    if (progressListener != null)
                        progressListener.notifyProgress(position, len);

                    result.setImage(t, z, convertToIcyBufferedImage(image, z, t, sizeX, sizeY, sizeC, type, signed16));

                    position++;
                }
            }

            // convert ROI(s)
            final RoiManager roiManager = RoiManager.getInstance();
            final Roi[] rois;

            if (roiManager != null)
                rois = roiManager.getRoisAsArray();
            else
                rois = new Roi[] {};

            if (rois.length > 0)
            {
                for (Roi ijRoi : rois)
                {
                    // can happen
                    if (ijRoi != null)
                        for (ROI icyRoi : convertToIcyRoi(ijRoi))
                            result.addROI(icyRoi);
                }
            }
            else
            {
                final Roi roi = image.getRoi();

                if (roi != null)
                    for (ROI icyRoi : convertToIcyRoi(roi))
                        result.addROI(icyRoi);
            }

            // calibrate
            calibrateIcySequence(result, image.getCalibration());
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Convert the specified Icy {@link Sequence} object to ImageJ {@link ImagePlus}
     */
    public static ImagePlus convertToImageJImage(Sequence sequence, boolean useRoiManager,
            ProgressListener progressListener)
    {
        // create the image
        final ImagePlus result = createImagePlus(sequence, progressListener);
        // calibrate
        calibrateImageJImage(result, sequence);

        // convert ROI
        final List<Roi> ijRois = new ArrayList<Roi>();
        for (ROI2D roi : sequence.getROI2Ds())
            ijRois.add(convertToImageJRoi(roi));

        if (ijRois.size() > 0)
        {
            if ((ijRois.size() > 1) && useRoiManager)
            {
                RoiManager roiManager = RoiManager.getInstance();
                if (roiManager == null)
                {
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // need to do it on EDT
                            new RoiManager();
                        }
                    });
                }

                roiManager = RoiManager.getInstance();
                int n = 0;
                for (Roi roi : ijRois)
                    roiManager.add(result, roi, n++);
            }

            result.setRoi(ijRois.get(0));
        }

        if (result.getNChannels() > 4)
            return new CompositeImage(result, CompositeImage.COLOR);
        else if (result.getNChannels() > 1)
            return new CompositeImage(result, CompositeImage.COMPOSITE);

        return result;
    }

    /**
     * Convert the specified Icy {@link Sequence} object to ImageJ {@link ImagePlus}
     */
    public static ImagePlus convertToImageJImage(Sequence sequence, ProgressListener progressListener)
    {
        return convertToImageJImage(sequence, false, progressListener);
    }

    /**
     * Convert the specified ImageJ {@link Roi} object to Icy {@link ROI}.
     */
    public static List<ROI2D> convertToIcyRoi(Roi roi)
    {
        final List<ROI2D> result = new ArrayList<ROI2D>();
        final List<Point2D> pts = new ArrayList<Point2D>();
        final FloatPolygon fp;

        switch (roi.getType())
        {
            default:
                result.add(new ROI2DRectangle(roi.getFloatBounds()));
                break;

            case Roi.OVAL:
                result.add(new ROI2DEllipse(roi.getFloatBounds()));
                break;

            case Roi.LINE:
                final Rectangle2D rect = roi.getFloatBounds();
                final double x = rect.getX();
                final double y = rect.getY();
                result.add(new ROI2DLine(new Point2D.Double(x, y), new Point2D.Double(x + rect.getWidth(), y
                        + rect.getHeight())));
                break;

            case Roi.TRACED_ROI:
            case Roi.POLYGON:
            case Roi.FREEROI:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                final ROI2DPolygon roiPolygon = new ROI2DPolygon();
                roiPolygon.setPoints(pts);

                // TRACED_ROI should be converted to ROI2DArea
                if (roi.getType() == Roi.TRACED_ROI)
                    result.add(new ROI2DArea(roiPolygon.getBooleanMask(true)));
                else
                    result.add(roiPolygon);
                break;

            case Roi.FREELINE:
            case Roi.POLYLINE:
            case Roi.ANGLE:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                final ROI2DPolyLine roiPolyline = new ROI2DPolyLine();
                roiPolyline.setPoints(pts);

                result.add(roiPolyline);
                break;

            case Roi.COMPOSITE:
                final ROI2DPath roiPath = new ROI2DPath(((ShapeRoi) roi).getShape());
                final Rectangle2D.Double roiBounds = roi.getFloatBounds();
                // we have to adjust position as Shape do not contains it
                if (roiPath.canSetPosition())
                    roiPath.setPosition2D(new Point2D.Double(roiBounds.x, roiBounds.y));
                result.add(roiPath);
                break;

            case Roi.POINT:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                for (Point2D pt : pts)
                    result.add(new ROI2DPoint(pt));
                break;
        }

        int ind = 0;
        for (ROI2D r : result)
        {
            r.setC(roi.getCPosition() - 1);
            r.setZ(roi.getZPosition() - 1);
            r.setT(roi.getTPosition() - 1);
            r.setSelected(false);
            if (result.size() > 1)
                r.setName(roi.getName() + " " + ind);
            else
                r.setName(roi.getName());
            Color c = roi.getStrokeColor();
            if (c == null)
                c = roi.getFillColor();
            if (c != null)
                r.setColor(c);
        }

        return result;
    }

    /**
     * Convert the specified Icy {@link ROI} object to ImageJ {@link Roi}.
     */
    public static Roi convertToImageJRoi(ROI2D roi)
    {
        final Roi result;

        if (roi instanceof ROI2DShape)
        {
            final List<Point2D> pts = ((ROI2DShape) roi).getPoints();

            if (roi instanceof ROI2DPoint)
            {
                final Point2D p = pts.get(0);
                result = new PointRoi(p.getX(), p.getY());
            }
            else if (roi instanceof ROI2DLine)
            {
                final Point2D p1 = pts.get(0);
                final Point2D p2 = pts.get(1);
                result = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
            else if (roi instanceof ROI2DRectangle)
            {
                final Rectangle2D r = roi.getBounds2D();
                result = new Roi(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 0);
            }
            else if (roi instanceof ROI2DEllipse)
            {
                final Rectangle2D r = roi.getBounds2D();
                result = new OvalRoi(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            }
            else if ((roi instanceof ROI2DPolyLine) || (roi instanceof ROI2DPolygon))
            {
                final FloatPolygon fp = new FloatPolygon();
                for (Point2D p : pts)
                    fp.addPoint(p.getX(), p.getY());
                if (roi instanceof ROI2DPolyLine)
                    result = new PolygonRoi(fp, Roi.POLYLINE);
                else
                    result = new PolygonRoi(fp, Roi.POLYGON);
            }
            else
                // create compatible shape ROI
                result = new ShapeRoi(((ROI2DPath) roi).getShape());
        }
        else if (roi instanceof ROI2DArea)
        {
            final ROI2DArea roiArea = (ROI2DArea) roi;
            final Point[] points = roiArea.getBooleanMask(true).getPoints();

            final Area area = new Area();
            for (Point pt : points)
                area.add(new Area(new Rectangle(pt.x, pt.y, 1, 1)));

            result = new ShapeRoi(area);
        }
        else
        {
            // create standard ROI
            final Rectangle2D r = roi.getBounds2D();
            result = new Roi(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        }

        result.setPosition(roi.getC() + 1, roi.getZ() + 1, roi.getT() + 1);
        result.setName(roi.getName());
        result.setStrokeColor(roi.getColor());
        // result.setFillColor(roi.getColor());
        // result.setStrokeWidth(roi.getStroke());

        return result;
    }

    /**
     * @deprecated Use {@link #convertToImageJRoi(ROI2D)} instead.
     */
    @Deprecated
    public static PointRoi convertToImageJRoiPoint(List<ROI2DPoint> points)
    {
        final int size = points.size();
        final float x[] = new float[size];
        final float y[] = new float[size];

        for (int i = 0; i < points.size(); i++)
        {
            final ROI2DPoint point = points.get(i);

            x[i] = (float) point.getPoint().getX();
            y[i] = (float) point.getPoint().getY();
        }

        return new PointRoi(x, y, size);
    }
}
