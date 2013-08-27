/**
 * 
 */
package plugins.kernel.roi;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.roi.ROI2DRectangle;

import java.awt.geom.Point2D;

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
    public ROI createROI(Point2D pt)
    {
        return new ROI2DRectangle(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DRectangle();
    }
}
