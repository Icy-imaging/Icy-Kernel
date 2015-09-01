/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Read-Only descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIReadOnlyDescriptor extends ROIDescriptor
{
    public static final String ID = "ReadOnly";

    public ROIReadOnlyDescriptor()
    {
        super(ID, "Read Only", Boolean.class);
    }

    @Override
    public String getDescription()
    {
        return "Read only state";
    }

    @Override
    public boolean useSequenceData()
    {
        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Boolean.valueOf(getReadOnly(roi));
    }

    /**
     * Returns ROI read only state
     */
    public static boolean getReadOnly(ROI roi)
    {
        if (roi == null)
            return false;

        return roi.isReadOnly();
    }
}