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
public class ROIMassCenterTDescriptor extends ROIDescriptor
{
    public static final String ID = "MassCenterT";

    public ROIMassCenterTDescriptor()
    {
        super(ID, "Center T", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Mass center T";
    }

    @Override
    public boolean useSequenceData()
    {
        return false;
    }
    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getMassCenterT(ROIMassCenterDescriptorsPlugin.computeMassCenter(roi)));
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
