/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

import java.awt.Color;

/**
 * Color descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ColorROIDescriptor extends ROIDescriptor
{
    public static final String ID = "Color";

    public ColorROIDescriptor()
    {
        super(ID, "Color", Color.class);
    }

    @Override
    public String getDescription()
    {
        return "Color";
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
            return Color.black;

        return new Color(roi.getColor().getRGB());
    }
}
