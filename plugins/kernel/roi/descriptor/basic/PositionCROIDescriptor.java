package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * Position C ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class PositionCROIDescriptor extends ROIDescriptor
{
    public static final String ID = "PositionC";

    public PositionCROIDescriptor()
    {
        super(ID, "Position C", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position C";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionC(roi.getPosition5D()));
    }

    /**
     * Returns position C of specified Point5D object
     */
    public static double getPositionC(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getC();
    }
}
