/**
 * 
 */
package plugins.kernel.roi.roi2d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;

import plugins.kernel.roi.roi2d.ROI2DRectangle;

/**
 * Plugin class for ROI2DRectangle.
 * 
 * @author Stephane
 */
public class ROI2DRectanglePlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI2DRectangle.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROI2DRectangle(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DRectangle();
    }
}
