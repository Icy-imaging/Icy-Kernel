package plugins.kernel.roi.roi3d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;

import plugins.kernel.roi.roi3d.ROI3DPoint;

/**
 * Plugin class for ROI3DPoint.
 * 
 * @author Stephane
 */
public class ROI3DPointPlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI3DPoint.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROI3DPoint(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI3DPoint();
    }
}
