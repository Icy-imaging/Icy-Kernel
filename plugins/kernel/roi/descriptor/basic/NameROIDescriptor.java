/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Name descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class NameROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Name";

    public NameROIDescriptor()
    {
        super(ID, "Name", String.class);
    }

    @Override
    public String getDescription()
    {
        return "Name";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return getName(roi);
    }

    /**
     * Returns ROI name
     */
    public static String getName(ROI roi)
    {
        if (roi == null)
            return "";

        return roi.getName();
    }
}
