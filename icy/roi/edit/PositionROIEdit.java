/**
 * 
 */
package icy.roi.edit;

import icy.roi.ROI;
import icy.type.point.Point5D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class PositionROIEdit extends AbstractROIEdit
{
    Point5D prevPos;
    Point5D currentPos;

    public PositionROIEdit(ROI roi, Point5D prevPos, boolean mergeable)
    {
        super(roi, "ROI position changed");

        this.prevPos = prevPos;
        this.currentPos = roi.getPosition5D();

        setMergeable(mergeable);
    }

    public PositionROIEdit(ROI roi, Point5D prevPos)
    {
        this(roi, prevPos, true);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().setPosition5D(prevPos);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI().setPosition5D(currentPos);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof PositionROIEdit)
        {
            final PositionROIEdit posEdit = (PositionROIEdit) edit;

            // same ROI ?
            if (posEdit.getROI() == getROI())
            {
                // collapse edits
                currentPos = posEdit.currentPos;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        prevPos = null;
        currentPos = null;
    }
}
