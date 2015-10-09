/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * Position X ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPositionXDescriptor extends ROIDescriptor
{
    public static final String ID = "PositionX";

    public ROIPositionXDescriptor()
    {
        super(ID, "Position X", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position X (bounding box)";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionX(roi.getPosition5D()));
    }

    /**
     * Returns position X of specified Point5D object
     */
    public static double getPositionX(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getX();
    }
}
