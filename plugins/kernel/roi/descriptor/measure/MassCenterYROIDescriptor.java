package plugins.kernel.roi.descriptor.measure;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * MassCenter Y coordinate ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class MassCenterYROIDescriptor extends ROIDescriptor
{
    public static final String ID = "MassCenterY";

    public MassCenterYROIDescriptor()
    {
        super(ID, "Mass Center Y", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center Y";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterY(MassCenterROIDescriptorsPlugin.computeMassCenter(roi)));
    }

    /**
     * Returns position Y of specified Point5D object
     */
    public static double getMassCenterY(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getY();
    }
}
