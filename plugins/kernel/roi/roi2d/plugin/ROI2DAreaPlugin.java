/**
 * 
 */
package plugins.kernel.roi.roi2d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;

import plugins.kernel.roi.roi2d.ROI2DArea;

/**
 * Plugin class for ROI2DArea.
 */
public class ROI2DAreaPlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI2DArea.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROI2DArea(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DArea();
    }
}
