package plugins.kernel.roi.descriptor.measure;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * MassCenter T coordinate ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class MassCenterTROIDescriptor extends ROIDescriptor
{
    public static final String ID = "MassCenterT";

    public MassCenterTROIDescriptor()
    {
        super(ID, "Mass Center T", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center T";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterT(MassCenterROIDescriptorsPlugin.computeMassCenter(roi)));
    }

    /**
     * Returns position T of specified Point5D object
     */
    public static double getMassCenterT(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getT();
    }
}
