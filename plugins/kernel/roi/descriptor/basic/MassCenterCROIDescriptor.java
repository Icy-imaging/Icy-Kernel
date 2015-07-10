package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * MassCenter C coordinate ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class MassCenterCROIDescriptor extends ROIDescriptor
{
    public static final String ID = "MassCenterC";

    public MassCenterCROIDescriptor()
    {
        super(ID, "Mass Center C", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center C";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterC(MassCenterROIDescriptorsPlugin.computeMassCenter(roi)));
    }

    /**
     * Returns position C of specified Point5D object
     */
    public static double getMassCenterC(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getC();
    }
}
