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
 * Internal unique Id descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIIdDescriptor extends ROIDescriptor
{
    public static final String ID = "Id";

    public ROIIdDescriptor()
    {
        super(ID, "Id", String.class);
    }

    @Override
    public String getDescription()
    {
        return "Internal id";
    }

    @Override
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_ID));
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Integer.valueOf(getId(roi));
    }

    /**
     * Returns ROI group id
     */
    public static int getId(ROI roi)
    {
        if (roi == null)
            return 0;

        return roi.getId();
    }
}
