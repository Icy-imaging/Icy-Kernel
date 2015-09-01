package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

/**
 * Size Z ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISizeZDescriptor extends ROIDescriptor
{
    public static final String ID = "SizeZ";

    public ROISizeZDescriptor()
    {
        super(ID, "Size Z", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size in Z dimension";
    }

    @Override
    public boolean useSequenceData()
    {
        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeZ(roi.getBounds5D()));
    }

    /**
     * Returns size Z of specified Rectangle5D object
     */
    public static double getSizeZ(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeZ();
    }
}