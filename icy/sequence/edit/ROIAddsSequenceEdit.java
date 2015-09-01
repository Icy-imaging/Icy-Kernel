package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.util.Collection;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI group add Sequence edit event.
 * 
 * @author Stephane
 */
public class ROIAddsSequenceEdit extends AbstractROIsSequenceEdit
{
    public ROIAddsSequenceEdit(Sequence sequence, Collection<ROI> rois, String name)
    {
        super(sequence, rois, name);
    }

    public ROIAddsSequenceEdit(Sequence sequence, Collection<ROI> rois)
    {
        this(sequence, rois, (rois.size() > 1) ? "ROI group added" : "ROI added");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Sequence sequence = getSequence();

        sequence.beginUpdate();
        try
        {
            for (ROI roi : getROIs())
                sequence.removeROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Sequence sequence = getSequence();

        sequence.beginUpdate();
        try
        {
            for (ROI roi : getROIs())
                sequence.addROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

}
