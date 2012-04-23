/**
 * 
 */
package icy.imagej;

import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImage;
import icy.painter.Painter;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;

import java.util.ArrayList;

/**
 * ImageJ utilities class.
 * 
 * @author Stephane
 */
public class ImageJUtil
{
    /**
     * Calibrate the specified Icy {@link Sequence} from the specified ImageJ {@link Calibration}
     * object.
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
            cal.setUnit("mm");
        }

        final double ti = seq.getTimeInterval();
        // different from default value
        if (ti != 1d)
        {
            cal.frameInterval = ti;
            cal.setTimeUnit("msec");
        }

        image.setDimensions(seq.getSizeC(), seq.getSizeZ(), seq.getSizeT());

        // final ImageProcessor ip = image.getProcessor();
        // ip.setMinAndMax(seq.getChannelMin(0) displayMin, displayMax);
    }

    /**
     * Convert the specified ImageJ {@link ImagePlus} object to Icy {@link Sequence}.
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

                    image.setPosition(1, z + 1, t + 1);

                    // separate RGB channel
                    if ((sizeC == 1) && ((type == ImagePlus.COLOR_256) || (type == ImagePlus.COLOR_RGB)))
                        result.setImage(t, z, IcyBufferedImage.createFrom(image.getBufferedImage()));
                    else
                    {
                        final Object data = image.getProcessor().getPixels();
                        final DataType dataType = ArrayUtil.getDataType(data);
                        final Object[] datas = Array2DUtil.createArray(dataType, sizeC);

                        // first channel data
                        datas[0] = data;

                        // others channels data
                        for (int c = 1; c < sizeC; c++)
                        {
                            image.setPosition(c + 1, z + 1, t + 1);
                            datas[c] = image.getProcessor().getPixels();
                        }

                        // create a single image from all channels
                        result.setImage(t, z, new IcyBufferedImage(sizeX, sizeY, datas));

                        position++;
                    }
                }
            }

            // convert ROI
            final Roi roi = image.getRoi();
            if (roi != null)
                result.addROI(convertToIcyRoi(roi));

            // convert Overlay
            final Overlay overlay = image.getOverlay();
            if (overlay != null)
                for (Painter painter : convertToIcyPainter(overlay))
                    result.addPainter(painter);

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
     * Convert the specified Icy {@link Sequence} object to ImageJ {@link ImagePlus}.
     */
    public static ImagePlus convertToImageJImage(Sequence sequence, ProgressListener progressListener)
    {
        final int sizeX = sequence.getSizeX();
        final int sizeY = sequence.getSizeY();
        final int sizeC = sequence.getSizeC();
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        final int len = sizeZ * sizeT * sizeC;
        int position = 0;

        final ImageStack stack = new ImageStack(sizeX, sizeY, LookUpTable.createGrayscaleColorModel(false));

        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                for (int c = 0; c < sizeZ; c++)
                {
                    if (progressListener != null)
                        progressListener.notifyProgress(position, len);

                    stack.addSlice(null, sequence.getDataXY(t, z, c));

                    position++;
                }
            }
        }

        // create the image
        final ImagePlus result = new ImagePlus(sequence.getName(), stack);

        // convert ROI
        result.setRoi(convertToImageJRoi(sequence.getROIs()));
        // convert Overlay
        result.setOverlay(convertToImageJOverlay(sequence.getPainters()));
        // calibrate
        calibrateImageJImage(result, sequence);

        return result;
    }

    /**
     * Convert the specified ImageJ {@link Roi} object to Icy {@link ROI}.
     */
    public static ROI convertToIcyRoi(Roi roi)
    {
        return null;
    }

    /**
     * Convert the specified Icy {@link ROI} object to ImageJ {@link Roi}.
     */
    public static Roi convertToImageJRoi(ROI roi)
    {
        return null;
    }

    /**
     * Convert the specified Icy {@link Painter} object to ImageJ {@link Roi}.
     */
    public static Roi convertToImageJRoi(Painter painter)
    {
        return null;
    }

    /**
     * Convert the specified list of Icy {@link ROI} object to ImageJ {@link Roi}.
     */
    public static Roi convertToImageJRoi(ArrayList<ROI> roi)
    {
        return null;
    }

    /**
     * Convert the specified ImageJ {@link Roi} object to Icy {@link Painter}.
     */
    public static Painter convertToIcyPainter(Roi roi)
    {
        return null;
    }

    /**
     * Convert the specified ImageJ {@link Overlay} object to an array of Icy {@link Painter}.
     */
    public static Painter[] convertToIcyPainter(Overlay overlay)
    {
        final int size = overlay.size();
        final Painter[] result = new Painter[size];

        for (int i = 0; i < size; i++)
            result[i] = convertToIcyPainter(overlay.get(i));

        return result;
    }

    /**
     * Convert the specified Icy {@link Painter} object to ImageJ {@link Overlay}.
     */
    public static Overlay convertToImageJOverlay(Painter painter)
    {
        return null;
    }

    /**
     * Convert the specified list of Icy {@link Painter} object to ImageJ {@link Overlay}.
     */
    public static Overlay convertToImageJOverlay(ArrayList<Painter> painter)
    {
        return null;
    }
}
