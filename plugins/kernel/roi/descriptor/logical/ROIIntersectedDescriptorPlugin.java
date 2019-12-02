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
 * The {@link PluginROIDescriptor} implementing the <i>Intersected ROIs</i> ROI descriptor
 * 
 * @author Stephane
 */
public class ROIIntersectedDescriptorPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_INTERSECTED_ROIS = ROIIntersectedDescriptor.ID;

    public static final ROIIntersectedDescriptor intersectedDescriptor = new ROIIntersectedDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(intersectedDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            result.put(intersectedDescriptor, intersectedDescriptor.compute(roi, sequence));
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(
                    getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'", e);
        }

        return result;
    }
}
