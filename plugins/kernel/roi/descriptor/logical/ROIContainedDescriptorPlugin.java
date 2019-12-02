/**
 * 
 */
package plugins.kernel.roi.descriptor.logical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * The {@link PluginROIDescriptor} implementing the <i>Contained ROIs</i> ROI descriptor
 * 
 * @author Stephane
 */
public class ROIContainedDescriptorPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_CONTAINED_ROIS = ROIContainedDescriptor.ID;

    public static final ROIContainedDescriptor containedDescriptor = new ROIContainedDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(containedDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            result.put(containedDescriptor, containedDescriptor.compute(roi, sequence));
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(
                    getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'", e);
        }

        return result;
    }
}
