package plugins.kernel.roi.descriptor.measure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.roi.ROIIterator;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi3d.ROI3DPoint;

/**
 * This {@link PluginROIDescriptor} implements the mass center ROI descriptors:<br/>
 * <li>Mass center X (in pixel)</li><br/>
 * <li>Mass center Y (in pixel)</li><br/>
 * <li>Mass center C (in pixel)</li><br/>
 * <li>Mass center Z (in pixel)</li><br/>
 * <li>Mass center T (in pixel)</li>
 * 
 * @author Stephane
 */
public class ROIMassCenterDescriptorsPlugin extends Plugin implements PluginROIDescriptor
{
    public static final String ID_MASS_CENTER_X = ROIMassCenterXDescriptor.ID;
    public static final String ID_MASS_CENTER_Y = ROIMassCenterYDescriptor.ID;
    public static final String ID_MASS_CENTER_Z = ROIMassCenterZDescriptor.ID;
    public static final String ID_MASS_CENTER_T = ROIMassCenterTDescriptor.ID;
    public static final String ID_MASS_CENTER_C = ROIMassCenterCDescriptor.ID;

    public static final ROIMassCenterXDescriptor massCenterXDescriptor = new ROIMassCenterXDescriptor();
    public static final ROIMassCenterYDescriptor massCenterYDescriptor = new ROIMassCenterYDescriptor();
    public static final ROIMassCenterZDescriptor massCenterZDescriptor = new ROIMassCenterZDescriptor();
    public static final ROIMassCenterTDescriptor massCenterTDescriptor = new ROIMassCenterTDescriptor();
    public static final ROIMassCenterCDescriptor massCenterCDescriptor = new ROIMassCenterCDescriptor();

    /**
     * Compute and returns the mass center of specified ROI.
     */
    public static Point5D computeMassCenter(ROI roi)
    {
        final Rectangle5D bounds = roi.getBounds5D();

        // special case of empty bounds ? --> return position
        if (bounds.isEmpty())
            return bounds.getPosition();
        // special case of single point ? --> return position
        if ((roi instanceof ROI2DPoint) || (roi instanceof ROI3DPoint))
            return bounds.getPosition();

        final ROIIterator it = new ROIIterator(roi, true);
        double x, y, z, t, c;
        long numPts;

        x = 0d;
        y = 0d;
        z = 0d;
        t = 0d;
        c = 0d;
        numPts = 0;
        while (!it.done())
        {
            x += it.getX();
            y += it.getY();
            z += it.getZ();
            t += it.getT();
            c += it.getC();

            it.next();
            numPts++;
        }

        if (numPts == 0)
            return new Point5D.Double();

        return new Point5D.Double(x / numPts, y / numPts, z / numPts, t / numPts, c / numPts);
    }

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<ROIDescriptor>();

        result.add(massCenterXDescriptor);
        result.add(massCenterYDescriptor);
        result.add(massCenterZDescriptor);
        result.add(massCenterTDescriptor);
        result.add(massCenterCDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        try
        {
            // compute mass center descriptors
            final Point5D massCenter = computeMassCenter(roi);

            result.put(massCenterXDescriptor, Double.valueOf(ROIMassCenterXDescriptor.getMassCenterX(massCenter)));
            result.put(massCenterYDescriptor, Double.valueOf(ROIMassCenterYDescriptor.getMassCenterY(massCenter)));
            result.put(massCenterZDescriptor, Double.valueOf(ROIMassCenterZDescriptor.getMassCenterZ(massCenter)));
            result.put(massCenterTDescriptor, Double.valueOf(ROIMassCenterTDescriptor.getMassCenterT(massCenter)));
            result.put(massCenterCDescriptor, Double.valueOf(ROIMassCenterCDescriptor.getMassCenterC(massCenter)));
        }
        catch (Exception e)
        {
            final String mess = getClass().getSimpleName() + ": cannot compute descriptors for '" + roi.getName() + "'";
            throw new UnsupportedOperationException(mess, e);
        }

        return result;
    }
}
