package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * Position T ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPositionTDescriptor extends ROIDescriptor
{
    public static final String ID = "PositionT";

    public ROIPositionTDescriptor()
    {
        super(ID, "Position T", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position T (bounding box)";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionT(roi.getPosition5D()));
    }

    /**
     * Returns position T of specified Point5D object
     */
    public static double getPositionT(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getT();
    }
}
