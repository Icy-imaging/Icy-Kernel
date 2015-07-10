package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

/**
 * Size T ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class SizeTROIDescriptor extends ROIDescriptor
{
    public static final String ID = "SizeT";

    public SizeTROIDescriptor()
    {
        super(ID, "Size T", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size T";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeT(roi.getBounds5D()));
    }

    /**
     * Returns size T of specified Rectangle5D object
     */
    public static double getSizeT(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeT();
    }
}
