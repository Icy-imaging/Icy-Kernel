/**
 * 
 */
package plugins.kernel.roi.roi2d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

/**
 * Plugin class for ROI2DPolygon.
 * 
 * @author Stephane
 */
public class ROI2DPolygonPlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI2DPolygon.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROI2DPolygon(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DPolygon();
    }
}
