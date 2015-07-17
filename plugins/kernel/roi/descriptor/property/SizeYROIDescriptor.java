package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

/**
 * Size Y ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class SizeYROIDescriptor extends ROIDescriptor
{
    public static final String ID = "SizeY";

    public SizeYROIDescriptor()
    {
        super(ID, "Size Y", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size Y";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeY(roi.getBounds5D()));
    }

    /**
     * Returns size Y of specified Rectangle5D object
     */
    public static double getSizeY(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeY();
    }
}
