/**
 * 
 */
package plugins.kernel.roi.descriptor.property;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the position ROI descriptors:<br/>
 * <li>Position X (in pixel)</li><br/>
 * <li>Position Y (in pixel)</li><br/>
 * <li>Position C (in pixel)</li><br/>
 * <li>Position Z (in pixel)</li><br/>
 * <li>Position T (in pixel)</li>
 * 
 * @author Stephane
 */
public class ROIPositionDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_POSITION_X = ROIPositionXDescriptor.ID;
    public static final String ID_POSITION_Y = ROIPositionYDescriptor.ID;
    public static final String ID_POSITION_Z = ROIPositionZDescriptor.ID;
    public static final String ID_POSITION_T = ROIPositionTDescriptor.ID;
    public static final String ID_POSITION_C = ROIPositionCDescriptor.ID;

    public static final ROIPositionXDescriptor positionXDescriptor = new ROIPositionXDescriptor();
    public static final ROIPositionYDescriptor positionYDescriptor = new ROIPositionYDescriptor();
    public static final ROIPositionZDescriptor positionZDescriptor = new ROIPositionZDescriptor();
    public static final ROIPositionTDescriptor positionTDescriptor = new ROIPositionTDescriptor();
    public static final ROIPositionCDescriptor positionCDescriptor = new ROIPositionCDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(positionXDescriptor);
        result.add(positionYDescriptor);
        result.add(positionZDescriptor);
        result.add(positionTDescriptor);
        result.add(positionCDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute position descriptors
            final Point5D position = roi.getPosition5D();

            result.put(positionXDescriptor, Double.valueOf(ROIPositionXDescriptor.getPositionX(position)));
            result.put(positionYDescriptor, Double.valueOf(ROIPositionYDescriptor.getPositionY(position)));
            result.put(positionZDescriptor, Double.valueOf(ROIPositionZDescriptor.getPositionZ(position)));
            result.put(positionTDescriptor, Double.valueOf(ROIPositionTDescriptor.getPositionT(position)));
            result.put(positionCDescriptor, Double.valueOf(ROIPositionCDescriptor.getPositionC(position)));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
