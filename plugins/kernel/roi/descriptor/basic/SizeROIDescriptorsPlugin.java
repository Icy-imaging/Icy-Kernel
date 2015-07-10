package plugins.kernel.roi.descriptor.basic;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This {@link PluginROIDescriptor} implements the size ROI descriptors:<br/>
 * <li>Size X (in pixel)</li><br/>
 * <li>Size Y (in pixel)</li><br/>
 * <li>Size C (in pixel)</li><br/>
 * <li>Size Z (in pixel)</li><br/>
 * <li>Size T (in pixel)</li>
 * 
 * @author Stephane
 */
public class SizeROIDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_SIZE_X = SizeXROIDescriptor.ID;
    public static final String ID_SIZE_Y = SizeYROIDescriptor.ID;
    public static final String ID_SIZE_Z = SizeZROIDescriptor.ID;
    public static final String ID_SIZE_T = SizeTROIDescriptor.ID;
    public static final String ID_SIZE_C = SizeCROIDescriptor.ID;

    public static final SizeXROIDescriptor sizeXDescriptor = new SizeXROIDescriptor();
    public static final SizeYROIDescriptor sizeYDescriptor = new SizeYROIDescriptor();
    public static final SizeZROIDescriptor sizeZDescriptor = new SizeZROIDescriptor();
    public static final SizeTROIDescriptor sizeTDescriptor = new SizeTROIDescriptor();
    public static final SizeCROIDescriptor sizeCDescriptor = new SizeCROIDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(sizeXDescriptor);
        result.add(sizeYDescriptor);
        result.add(sizeZDescriptor);
        result.add(sizeTDescriptor);
        result.add(sizeCDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute size descriptors
            final Rectangle5D size = roi.getBounds5D();

            result.put(sizeXDescriptor, Double.valueOf(SizeXROIDescriptor.getSizeX(size)));
            result.put(sizeYDescriptor, Double.valueOf(SizeYROIDescriptor.getSizeY(size)));
            if (z == -1)
                result.put(sizeZDescriptor, Double.valueOf(SizeZROIDescriptor.getSizeZ(size)));
            else
                result.put(sizeZDescriptor, Double.valueOf(1d));
            if (t == -1)
                result.put(sizeTDescriptor, Double.valueOf(SizeTROIDescriptor.getSizeT(size)));
            else
                result.put(sizeTDescriptor, Double.valueOf(1d));
            if (c == -1)
                result.put(sizeCDescriptor, Double.valueOf(SizeCROIDescriptor.getSizeC(size)));
            else
                result.put(sizeCDescriptor, Double.valueOf(1d));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
