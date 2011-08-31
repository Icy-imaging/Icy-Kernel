/**
 * 
 */
package icy.roi;

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
}
