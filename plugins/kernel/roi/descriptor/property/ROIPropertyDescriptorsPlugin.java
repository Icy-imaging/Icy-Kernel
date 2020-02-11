/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

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
 * This {@link PluginROIDescriptor} implements the properties ROI descriptors:<br/>
 * <li>Name</li><br/>
 * <li>Color</li><br/>
 * <li>Opacity</li><br/>
 * <li>Read only</li>
 * 
 * @author Stephane
 */
public class ROIPropertyDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_ID = ROIIdDescriptor.ID;
    public static final String ID_ICON = ROIIconDescriptor.ID;
    public static final String ID_NAME = ROINameDescriptor.ID;
    // public static final String ID_GROUPID = ROIGroupIdDescriptor.ID;
    public static final String ID_COLOR = ROIColorDescriptor.ID;
    public static final String ID_OPACITY = ROIOpacityDescriptor.ID;
    public static final String ID_READONLY = ROIReadOnlyDescriptor.ID;

    public static final ROIIdDescriptor idDescriptor = new ROIIdDescriptor();
    public static final ROIIconDescriptor iconDescriptor = new ROIIconDescriptor();
    public static final ROINameDescriptor nameDescriptor = new ROINameDescriptor();
    // public static final ROIGroupIdDescriptor groupIdDescriptor = new ROIGroupIdDescriptor();
    public static final ROIColorDescriptor colorDescriptor = new ROIColorDescriptor();
    public static final ROIOpacityDescriptor opacityDescriptor = new ROIOpacityDescriptor();
    public static final ROIReadOnlyDescriptor readOnlyDescriptor = new ROIReadOnlyDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(idDescriptor);
        result.add(iconDescriptor);
        result.add(nameDescriptor);
        // result.add(groupIdDescriptor);
        result.add(colorDescriptor);
        result.add(opacityDescriptor);
        result.add(readOnlyDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute descriptors
            result.put(idDescriptor, Integer.valueOf(ROIIdDescriptor.getId(roi)));
            result.put(iconDescriptor, ROIIconDescriptor.getIcon(roi));
            result.put(nameDescriptor, ROINameDescriptor.getName(roi));
            // result.put(groupIdDescriptor, ROIGroupIdDescriptor.getGroupId(roi));
            result.put(colorDescriptor, ROIColorDescriptor.getColor(roi));
            result.put(opacityDescriptor, Float.valueOf(ROIOpacityDescriptor.getOpacity(roi)));
            result.put(readOnlyDescriptor, Boolean.valueOf(ROIReadOnlyDescriptor.getReadOnly(roi)));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
