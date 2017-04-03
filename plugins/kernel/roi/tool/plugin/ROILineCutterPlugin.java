/**
 * 
 */
package plugins.kernel.roi.tool.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import plugins.kernel.roi.tool.ROILineCutter;

/**
 * Plugin class for ROI2DLine.
 * 
 * @author Stephane
 */
public class ROILineCutterPlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROILineCutter.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROILineCutter(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROILineCutter();
    }
}
