package icy.roi.edit;

import icy.roi.ROI;
import icy.undo.AbstractIcyUndoableEdit;

import java.awt.Image;

/**
 * Base ROI undoable edit.
 * 
 * @author Stephane
 */
public abstract class AbstractROIEdit extends AbstractIcyUndoableEdit
{
    public AbstractROIEdit(ROI roi, String name, Image icon)
    {
        super(roi, name, icon);
    }

    public AbstractROIEdit(ROI roi, String name)
    {
        this(roi, name, roi.getIcon());
    }

    public AbstractROIEdit(ROI roi)
    {
        this(roi, "ROI changed", roi.getIcon());
    }

    public ROI getROI()
    {
        return (ROI) getSource();
    }
}
