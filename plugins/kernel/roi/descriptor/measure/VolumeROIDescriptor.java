/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Volume ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class VolumeROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Volume";

    public VolumeROIDescriptor()
    {
        super(ID, "Volume", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Volume";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(3, 3).toString();

        return UnitPrefix.MICRO.toString() + "m3";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeVolume(roi, sequence));
    }

    /**
     * Computes and returns the volume expressed in the unit of the descriptor (see
     * {@link #getUnit(Sequence)}) for the specified ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the volume
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the area
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeVolume(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        if (!(roi instanceof ROI3D))
            throw new UnsupportedOperationException("Perimeter not supported for ROI" + roi.getDimension() + "D !");

        return computeVolume(InteriorROIDescriptor.computeInterior(roi), roi, sequence);
    }

    /**
     * Computes and returns the volume from a given number of interior points expressed in the
     * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param interiorPoints
     *        the number of interior points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the volume
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size
     *        information.
     * @return the volume
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeVolume(double interiorPoints, ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        try
        {
            return InteriorROIDescriptor.computeInterior(interiorPoints, roi, sequence, 3);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation on the ROI: "
                    + roi.getName());
        }
    }
}
