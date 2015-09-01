/**
 * 
 */
package icy.roi.edit;

import icy.roi.BooleanMask2D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import plugins.kernel.roi.roi2d.ROI2DArea;

/**
 * ROI2DARea change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Area2DChangeROIEdit extends AbstractROIEdit
{
    BooleanMask2D oldMask;
    BooleanMask2D newMask;

    public Area2DChangeROIEdit(ROI2DArea roi, BooleanMask2D oldMask, String name)
    {
        super(roi, name);

        this.oldMask = oldMask;
        // get actual mask
        this.newMask = roi.getBooleanMask(true);
    }

    public Area2DChangeROIEdit(ROI2DArea roi, BooleanMask2D oldMask)
    {
        this(roi, oldMask, "ROI mask changed");
    }

    public ROI2DArea getROI2DArea()
    {
        return (ROI2DArea) source;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI2DArea().setAsBooleanMask(oldMask);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI2DArea().setAsBooleanMask(newMask);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        // if (!isMergeable())
        // return false;
        //
        // if (edit instanceof Area2DChangeROIEdit)
        // {
        // final Area2DChangeROIEdit changeEdit = (Area2DChangeROIEdit) edit;
        //
        // // same ROI ?
        // if (changeEdit.getROI() == getROI())
        // {
        // // collapse edits
        // newMask = changeEdit.newMask;
        // return true;
        // }
        // }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        oldMask = null;
        newMask = null;
    }

}
