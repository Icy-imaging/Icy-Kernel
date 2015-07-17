package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

/**
 * Size X ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISizeXDescriptor extends ROIDescriptor
{
    public static final String ID = "SizeX";

    public ROISizeXDescriptor()
    {
        super(ID, "Size X", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size X";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeX(roi.getBounds5D()));
    }

    /**
     * Returns size X of specified Rectangle5D object
     */
    public static double getSizeX(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeX();
    }
}
