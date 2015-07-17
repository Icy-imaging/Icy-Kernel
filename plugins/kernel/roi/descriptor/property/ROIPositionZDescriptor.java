package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * Position Z ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPositionZDescriptor extends ROIDescriptor
{
    public static final String ID = "PositionZ";

    public ROIPositionZDescriptor()
    {
        super(ID, "Position Z", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position Z";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionZ(roi.getPosition5D()));
    }

    /**
     * Returns position Z of specified Point5D object
     */
    public static double getPositionZ(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getZ();
    }
}
