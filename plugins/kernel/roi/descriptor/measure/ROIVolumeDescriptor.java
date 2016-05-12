/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.util.StringUtil;

/**
 * Volume ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIVolumeDescriptor extends ROIDescriptor
{
    public static final String ID = "Volume";

    public ROIVolumeDescriptor()
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
            return sequence.getBestPixelSizeUnit(3, 3).toString() + "m3";

        return UnitPrefix.MICRO.toString() + "m3";
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        final SequenceEventSourceType sourceType = change.getSourceType();

        if (sourceType == SequenceEventSourceType.SEQUENCE_DATA)
            return true;
        if (sourceType == SequenceEventSourceType.SEQUENCE_META)
        {
            final String metaName = (String) change.getSource();

            return StringUtil.isEmpty(metaName) || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_X)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Y)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Z);
        }

        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeVolume(roi, sequence));
    }

    /**
     * Computes and returns the volume expressed in the unit of the descriptor (see {@link #getUnit(Sequence)}) for the
     * specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the volume
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the volume expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeVolume(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return computeVolume(ROIInteriorDescriptor.computeInterior(roi), roi, sequence);
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
     * @return the volume expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeVolume(double interiorPoints, ROI roi, Sequence sequence)
            throws UnsupportedOperationException
    {
        try
        {
            // we restrict to ROI3D only
            if (!(roi instanceof ROI3D))
                throw new UnsupportedOperationException();

            return ROIInteriorDescriptor.computeInterior(interiorPoints, roi, sequence, 3);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process " + ID + " calculation for ROI: '" + roi.getName()
                    + "'");
        }
    }
}
