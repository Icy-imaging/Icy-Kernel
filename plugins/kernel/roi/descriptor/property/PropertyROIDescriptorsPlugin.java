/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the properties ROI descriptors:<br/>
 * <li>Name</li><br/>
 * <li>Color</li><br/>
 * <li>Opacity</li><br/>
 * <li>Read only</li>
 * 
 * @author Stephane
 */
public class PropertyROIDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_NAME = NameROIDescriptor.ID;
    public static final String ID_COLOR = ColorROIDescriptor.ID;
    public static final String ID_OPACITY = OpacityROIDescriptor.ID;
    public static final String ID_READONLY = ReadOnlyROIDescriptor.ID;

    public static final NameROIDescriptor nameDescriptor = new NameROIDescriptor();
    public static final ColorROIDescriptor colorDescriptor = new ColorROIDescriptor();
    public static final OpacityROIDescriptor opacityDescriptor = new OpacityROIDescriptor();
    public static final ReadOnlyROIDescriptor readOnlyDescriptor = new ReadOnlyROIDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(nameDescriptor);
        result.add(colorDescriptor);
        result.add(opacityDescriptor);
        result.add(readOnlyDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute descriptors
            result.put(nameDescriptor, NameROIDescriptor.getName(roi));
            result.put(colorDescriptor, ColorROIDescriptor.getColor(roi));
            result.put(opacityDescriptor, Float.valueOf(OpacityROIDescriptor.getOpacity(roi)));
            result.put(readOnlyDescriptor, Boolean.valueOf(ReadOnlyROIDescriptor.getReadOnly(roi)));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
