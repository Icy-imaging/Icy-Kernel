/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROI.ROIGroupId;
import icy.roi.ROIDescriptor;
import icy.roi.ROIEvent;
import icy.roi.ROIEvent.ROIEventType;
import icy.sequence.Sequence;
import icy.util.StringUtil;

/**
 * Group Id descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIGroupIdDescriptor extends ROIDescriptor
{
    public static final String ID = "GroupId";

    public ROIGroupIdDescriptor()
    {
        super(ID, "Group", String.class);
    }

    @Override
    public String getDescription()
    {
        return "Group id (used for group operation)";
    }

    @Override
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_GROUPID));
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return getGroupId(roi);
    }

    /**
     * Returns ROI group id
     */
    public static ROIGroupId getGroupId(ROI roi)
    {
        if (roi == null)
            return null;

        return roi.getGroupId();
    }
}
