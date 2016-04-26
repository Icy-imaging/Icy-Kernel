/**
 * 
 */
package plugins.kernel.roi.descriptor.measure;

import icy.math.UnitUtil.UnitPrefix;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.util.StringUtil;

/**
 * Area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIAreaDescriptor extends ROIDescriptor
{
    public static final String ID = "Area";

    public ROIAreaDescriptor()
    {
        super(ID, "Area", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Area";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(2, 2).toString() + "m2";

        return UnitPrefix.MICRO.toString() + "m2";
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

            return StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_X)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Y)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Z);
        }

        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(computeArea(roi, sequence));
    }

    /**
     * Computes and returns the area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)}) for the
     * specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the surface area
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeArea(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return computeArea(ROIInteriorDescriptor.computeInterior(roi), roi, sequence);
    }

    /**
     * Computes and returns the surface area from a given number of contour points expressed in the
     * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param interiorPoints
     *        the number of contour points (override the ROI value)
     * @param roi
     *        the ROI we want to compute the surface area
     * @param sequence
     *        the input sequence used to retrieve operation unit by using pixel size
     *        information.
     * @return the area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     */
    public static double computeArea(double interiorPoints, ROI roi, Sequence sequence)
            throws UnsupportedOperationException
    {
        try
        {
            // we restrict to ROI2D only
            if (!(roi instanceof ROI2D))
                throw new UnsupportedOperationException();

            return ROIInteriorDescriptor.computeInterior(interiorPoints, roi, sequence, 2);
        }
        catch (UnsupportedOperationException e)
        {
            throw new UnsupportedOperationException("Can't process " + ID + " calculation for ROI: '" + roi.getName()
                    + "'");
        }
    }
}