/**
 * 
 */
package plugins.kernel.roi;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.roi.ROI2DArea;

import java.awt.geom.Point2D;

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
    public ROI createROI(Point2D pt)
    {
        return new ROI2DArea(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DArea();
    }
}
