/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

import java.awt.Image;

/**
 * Icon descriptor class (see {@link ROIDescriptor}).<br>
 * Return the ROI icon a 20 pixels side icon
 * 
 * @author Stephane
 */
public class ROIIconDescriptor extends ROIDescriptor
{
    public static final String ID = "Icon";

    public ROIIconDescriptor()
    {
        super(ID, "Icon", Image.class);
    }

    @Override
    public String getDescription()
    {
        return "Icon";
    }

    @Override
    public boolean useSequenceData()
    {
        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return getIcon(roi);
    }

    /**
     * Returns ROI icon
     */
    public static Image getIcon(ROI roi)
    {
        if (roi == null)
            return null;

        return roi.getIcon();
    }
}
