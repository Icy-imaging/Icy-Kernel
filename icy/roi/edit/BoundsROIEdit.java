package icy.roi.edit;

import icy.roi.ROI;
import icy.type.rectangle.Rectangle5D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class BoundsROIEdit extends AbstractROIEdit
{
    Rectangle5D prevBounds;
    Rectangle5D currentBounds;

    public BoundsROIEdit(ROI roi, Rectangle5D prevBounds, boolean mergeable)
    {
        super(roi, "ROI bounds changed");

        this.prevBounds = prevBounds;
        this.currentBounds = roi.getBounds5D();

        setMergeable(mergeable);
    }

    public BoundsROIEdit(ROI roi, Rectangle5D prevBounds)
    {
        this(roi, prevBounds, true);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().setBounds5D(prevBounds);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI().setBounds5D(currentBounds);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof BoundsROIEdit)
        {
            final BoundsROIEdit bndEdit = (BoundsROIEdit) edit;

            // same ROI ?
            if (bndEdit.getROI() == getROI())
            {
                // collapse edits
                currentBounds = bndEdit.currentBounds;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        prevBounds = null;
        currentBounds = null;
    }
}
