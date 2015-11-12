/**
 * 
 */
package icy.roi.edit;

import icy.roi.ROI;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Default lazy implementation for ROI undoable edition (full copy)
 * 
 * @author Stephane
 */
public class DefaultROIEdit extends AbstractROIEdit
{
    ROI previous;
    ROI current;

    public DefaultROIEdit(ROI previous, ROI current)
    {
        super(current);

        this.previous = previous;
        this.current = current.getCopy();
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().copyFrom(previous);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        if (current != null)
            getROI().copyFrom(current);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof DefaultROIEdit)
        {
            final DefaultROIEdit defEdit = (DefaultROIEdit) edit;

            // same ROI ?
            if (defEdit.getROI() == getROI())
            {
                // collapse edits
                current = defEdit.current;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        previous = null;
        current = null;
    }
}
