/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.roi.ROIEvent;
import icy.roi.ROIEvent.ROIEventType;
import icy.sequence.Sequence;
import icy.util.StringUtil;

/**
 * Name descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROINameDescriptor extends ROIDescriptor
{
    public static final String ID = "Name";

    public ROINameDescriptor()
    {
        super(ID, "Name", String.class);
    }

    @Override
    public String getDescription()
    {
        return "Name of the ROI";
    }

    @Override
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_NAME));
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
