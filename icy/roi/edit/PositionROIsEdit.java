package icy.roi.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

import java.util.List;
import java.util.Set;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Multiple position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class PositionROIsEdit extends AbstractROIsEdit
{
    List<Point5D> previousPositions;
    List<Point5D> newPositions;

    public PositionROIsEdit(List<? extends ROI> rois, List<Point5D> previousPositions, List<Point5D> newPositions,
            boolean mergeable)
    {
        super(rois, (rois.size() > 1) ? "ROIs position changed" : "ROI position changed");

        if (rois.size() != previousPositions.size())
            throw new IllegalArgumentException("ROI list and old values list size do not match (" + rois.size()
                    + " != " + previousPositions.size() + ")");
        if (rois.size() != newPositions.size())
            throw new IllegalArgumentException("ROI list and new values list size do not match (" + rois.size()
                    + " != " + newPositions.size() + ")");

        this.previousPositions = previousPositions;
        this.newPositions = newPositions;

        setMergeable(mergeable);
    }

    public PositionROIsEdit(List<? extends ROI> rois, List<Point5D> previousPositions, List<Point5D> newPositions)
    {
        this(rois, previousPositions, newPositions, true);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Set<Sequence> sequences = getSequences();

        // undo
        for (Sequence sequence : sequences)
            sequence.beginUpdate();
        try
        {
            int ind = 0;
            for (ROI roi : getROIs())
                roi.setPosition5D(previousPositions.get(ind++));
        }
        finally
        {
            for (Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Set<Sequence> sequences = getSequences();

        // redo
        for (Sequence sequence : sequences)
            sequence.beginUpdate();
        try
        {
            int ind = 0;
            for (ROI roi : getROIs())
                roi.setPosition5D(newPositions.get(ind++));
        }
        finally
        {
            for (Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof PositionROIsEdit)
        {
            final PositionROIsEdit posEdit = (PositionROIsEdit) edit;

            // same ROI list ?
            if (posEdit.getROIs().equals(getROIs()))
            {
                // collapse edits
                newPositions = posEdit.newPositions;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        previousPositions = null;
        newPositions = null;
    }
}
