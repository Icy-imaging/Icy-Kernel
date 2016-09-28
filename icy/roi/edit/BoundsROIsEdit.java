package icy.roi.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.type.rectangle.Rectangle5D;

import java.util.List;
import java.util.Set;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class BoundsROIsEdit extends AbstractROIsEdit
{
    List<Rectangle5D> previousBounds;
    List<Rectangle5D> newBounds;

    public BoundsROIsEdit(List<? extends ROI> rois, List<Rectangle5D> previousBounds, List<Rectangle5D> newBounds,
            boolean mergeable)
    {
        super(rois, (rois.size() > 1) ? "ROIs bounds changed" : "ROI bounds changed");

        if (rois.size() != previousBounds.size())
            throw new IllegalArgumentException("ROI list and old values list size do not match (" + rois.size()
                    + " != " + previousBounds.size() + ")");
        if (rois.size() != newBounds.size())
            throw new IllegalArgumentException("ROI list and new values list size do not match (" + rois.size()
                    + " != " + newBounds.size() + ")");

        this.previousBounds = previousBounds;
        this.newBounds = newBounds;

        setMergeable(mergeable);
    }

    public BoundsROIsEdit(List<? extends ROI> rois, List<Rectangle5D> previousBounds, List<Rectangle5D> newBounds)
    {
        this(rois, previousBounds, newBounds, true);
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
                roi.setBounds5D(previousBounds.get(ind++));
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
                roi.setBounds5D(newBounds.get(ind++));
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

        if (edit instanceof BoundsROIsEdit)
        {
            final BoundsROIsEdit bndEdit = (BoundsROIsEdit) edit;

            // same ROI list ?
            if (bndEdit.getROIs().equals(getROIs()))
            {
                // collapse edits
                newBounds = bndEdit.newBounds;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        previousBounds = null;
        newBounds = null;
    }
}