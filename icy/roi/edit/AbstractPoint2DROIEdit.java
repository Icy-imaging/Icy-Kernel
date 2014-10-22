/**
 * 
 */
package icy.roi.edit;

import icy.painter.Anchor2D;

import java.awt.Image;

import plugins.kernel.roi.roi2d.ROI2DShape;

/**
 * Base class of control point change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class AbstractPoint2DROIEdit extends AbstractROIEdit
{
    protected Anchor2D point;

    public AbstractPoint2DROIEdit(ROI2DShape roi, Anchor2D point, String name, Image icon)
    {
        super(roi, name, icon);

        this.point = point;
    }

    public AbstractPoint2DROIEdit(ROI2DShape roi, Anchor2D point, String name)
    {
        this(roi, point, name, roi.getIcon());
    }

    public AbstractPoint2DROIEdit(ROI2DShape roi, Anchor2D point, Image icon)
    {
        this(roi, point, "ROI point changed", icon);
    }

    public AbstractPoint2DROIEdit(ROI2DShape roi, Anchor2D point)
    {
        this(roi, point, "ROI point changed", roi.getIcon());
    }

    public ROI2DShape getROI2DShape()
    {
        return (ROI2DShape) getSource();
    }

    public Anchor2D getPoint()
    {
        return point;
    }

    @Override
    public void die()
    {
        super.die();

        point = null;
    }
}
