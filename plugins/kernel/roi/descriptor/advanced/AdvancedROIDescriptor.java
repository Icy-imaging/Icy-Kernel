package plugins.kernel.roi.descriptor.advanced;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} plugin implements the basic ROI descriptors as perimeter,
 * surface, area...
 * 
 * @author Stephane
 */
public class AdvancedROIDescriptor extends Plugin implements PluginROIDescriptor
{
    public static final String ID_STANDARD_DEV = "Standard deviation";

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        Map<String, Class<?>> descriptors = new HashMap<String, Class<?>>();

        descriptors.put(VOLUME, Double.class);
        descriptors.put(SURFACE, Double.class);
        descriptors.put(PERIMETER, Double.class);
        descriptors.put(SURFAREA, Double.class);

        return descriptors;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (roi instanceof ROI2D)
        {
            map.put(SURFACE, Double.valueOf(roi.getNumberOfPoints()));
            map.put(PERIMETER, Double.valueOf(roi.getNumberOfContourPoints()));

            // other descriptors
        }
        else if (roi instanceof ROI3D)
        {
            map.put(VOLUME, Double.valueOf(roi.getNumberOfPoints()));
        }
        else
            throw new UnsupportedOperationException(getClass().getName() + " does not support ROI of type "
                    + roi.getClass().getName());

        return map;
    }
}
