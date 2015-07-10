/**
 * 
 */
package plugins.kernel.roi.descriptor.basic;

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
public class PositionROIDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_POSITION_X = PositionXROIDescriptor.ID;
    public static final String ID_POSITION_Y = PositionYROIDescriptor.ID;
    public static final String ID_POSITION_Z = PositionZROIDescriptor.ID;
    public static final String ID_POSITION_T = PositionTROIDescriptor.ID;
    public static final String ID_POSITION_C = PositionCROIDescriptor.ID;

    public static final PositionXROIDescriptor positionXDescriptor = new PositionXROIDescriptor();
    public static final PositionYROIDescriptor positionYDescriptor = new PositionYROIDescriptor();
    public static final PositionZROIDescriptor positionZDescriptor = new PositionZROIDescriptor();
    public static final PositionTROIDescriptor positionTDescriptor = new PositionTROIDescriptor();
    public static final PositionCROIDescriptor positionCDescriptor = new PositionCROIDescriptor();

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
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute position descriptors
            final Point5D position = roi.getPosition5D();

            result.put(positionXDescriptor, Double.valueOf(PositionXROIDescriptor.getPositionX(position)));
            result.put(positionYDescriptor, Double.valueOf(PositionYROIDescriptor.getPositionY(position)));
            if (z == -1)
                result.put(positionZDescriptor, Double.valueOf(PositionZROIDescriptor.getPositionZ(position)));
            else
                result.put(positionZDescriptor, Double.valueOf(z));
            if (t == -1)
                result.put(positionTDescriptor, Double.valueOf(PositionTROIDescriptor.getPositionT(position)));
            else
                result.put(positionTDescriptor, Double.valueOf(t));
            if (c == -1)
                result.put(positionCDescriptor, Double.valueOf(PositionCROIDescriptor.getPositionC(position)));
            else
                result.put(positionCDescriptor, Double.valueOf(c));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
