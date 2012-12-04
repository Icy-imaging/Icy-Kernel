/**
 * 
 */
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public abstract class ROI5D extends ROI
{
    /**
     * Return ROI5D of ROI list
     */
    public static ArrayList<ROI5D> getROI5DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    public ROI5D()
    {
        super();
    }

    @Override
    final public int getDimension()
    {
        return 5;
    }

    /**
     * Return true if the ROI is active for the specified canvas.<br>
     * It internally uses the visible state of the attached layer.
     */
    public boolean isActiveFor(IcyCanvas canvas)
    {
        if (!canvas.isLayersVisible())
            return false;

        final Layer layer = canvas.getLayer(painter);

        return (layer != null) && layer.isVisible();
    }

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle5D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle5D</code>.
     * 
     * @return an instance of <code>Rectangle5D</code> that is a bounding box of the
     *         <code>ROI</code>.
     */
    public abstract Rectangle5D getBounds5D();

    /**
     * Returns the top left corner of the ROI bounds.<br>
     * 
     * @see #getBounds5D()
     */
    public Point5D getPosition5D()
    {
        return getBounds5D().getPosition();
    }

}
