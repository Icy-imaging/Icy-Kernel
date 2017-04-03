/**
 * 
 */
package icy.roi.edit;

import icy.painter.Anchor3D;
import icy.roi.ROI3D;

import java.awt.Image;

/**
 * Base class of 3D control point change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class AbstractPoint3DROIEdit extends AbstractROIEdit
{
    protected Anchor3D point;

    public AbstractPoint3DROIEdit(ROI3D roi, Anchor3D point, String name, Image icon)
    {
        super(roi, name, icon);

        this.point = point;
    }

    public AbstractPoint3DROIEdit(ROI3D roi, Anchor3D point, String name)
    {
        this(roi, point, name, roi.getIcon());
    }

    public AbstractPoint3DROIEdit(ROI3D roi, Anchor3D point, Image icon)
    {
        this(roi, point, "ROI point changed", icon);
    }

    public AbstractPoint3DROIEdit(ROI3D roi, Anchor3D point)
    {
        this(roi, point, "ROI point changed", roi.getIcon());
    }

    public ROI3D getROI3D()
    {
        return (ROI3D) getSource();
    }

    public Anchor3D getPoint()
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
