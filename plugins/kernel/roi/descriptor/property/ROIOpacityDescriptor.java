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
 * Opacity descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIOpacityDescriptor extends ROIDescriptor
{
    public static final String ID = "Opacity";

    public ROIOpacityDescriptor()
    {
        super(ID, "Opacity", Float.class);
    }

    @Override
    public String getDescription()
    {
        return "Opacity factor to display ROI content";
    }

    @Override
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_OPACITY));
    }


    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Float.valueOf(getOpacity(roi));
    }

    /**
     * Returns ROI opacity
     */
    public static float getOpacity(ROI roi)
    {
        if (roi == null)
            return 1f;

        return roi.getOpacity();
    }
}
