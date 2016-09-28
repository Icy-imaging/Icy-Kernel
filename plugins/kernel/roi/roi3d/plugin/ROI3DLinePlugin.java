package plugins.kernel.roi.roi3d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import plugins.kernel.roi.roi3d.ROI3DLine;

/**
 * Plugin class for ROI3DLine.
 * 
 * @author Stephane
 */
public class ROI3DLinePlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI3DLine.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROI3DLine(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI3DLine();
    }
}
