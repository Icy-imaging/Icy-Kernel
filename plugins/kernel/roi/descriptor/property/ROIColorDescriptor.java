package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.roi.ROIEvent;
import icy.roi.ROIEvent.ROIEventType;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.Color;

public class ROIColorDescriptor extends ROIDescriptor
{
    public static final String ID = "Color";

    public ROIColorDescriptor()
    {
        super(ID, "Color", Color.class);
    }

    @Override
    public String getDescription()
    {
        return "Color";
    }

    @Override
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_COLOR));
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return getColor(roi);
    }

    /**
     * Returns ROI color
     */
    public static Color getColor(ROI roi)
    {
        if (roi == null)
            return null;

        return roi.getColor();
    }
}
