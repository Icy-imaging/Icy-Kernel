package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * Position Y ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class PositionYROIDescriptor extends ROIDescriptor
{
    public static final String ID = "PositionY";

    public PositionYROIDescriptor()
    {
        super(ID, "Position Y", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position Y";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionY(roi.getPosition5D()));
    }

    /**
     * Returns position Y of specified Point5D object
     */
    public static double getPositionY(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getY();
    }
}
