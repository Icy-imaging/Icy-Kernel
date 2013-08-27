/**
 * 
 */
package plugins.kernel.roi;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.roi.ROI2DEllipse;

import java.awt.geom.Point2D;

/**
 * Plugin class for ROI2DEllipse.
 * 
 * @author Stephane
 */
public class ROI2DEllipsePlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROI2DEllipse.class.getName();
    }

    @Override
    public ROI createROI(Point2D pt)
    {
        return new ROI2DEllipse(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROI2DEllipse();
    }
}
