package plugins.kernel.roi.descriptor.measure;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

/**
 * MassCenter Z coordinate ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIMassCenterZDescriptor extends ROIDescriptor
{
    public static final String ID = "Mass center Z";

    public ROIMassCenterZDescriptor()
    {
        super(ID, "Center Z", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center Z";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterZ(ROIMassCenterDescriptorsPlugin.computeMassCenter(roi)));
    }

    /**
     * Returns position Z of specified Point5D object
     */
    public static double getMassCenterZ(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getZ();
    }
}
