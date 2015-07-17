package plugins.kernel.roi.descriptor.measure;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * MassCenter X coordinate ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class MassCenterXROIDescriptor extends ROIDescriptor
{
    public static final String ID = "MassCenterX";

    public MassCenterXROIDescriptor()
    {
        super(ID, "Mass Center X", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center X";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterX(MassCenterROIDescriptorsPlugin.computeMassCenter(roi)));
    }

    /**
     * Returns position X of specified Point5D object
     */
    public static double getMassCenterX(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getX();
    }
}
