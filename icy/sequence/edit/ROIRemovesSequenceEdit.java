package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI group remove Sequence edit event
 * 
 * @author Stephane
 */
public class ROIRemovesSequenceEdit extends AbstractROIsSequenceEdit
{
    public ROIRemovesSequenceEdit(Sequence sequence, List<ROI> rois)
    {
        super(sequence, rois, (rois.size() > 1) ? "ROI group removed" : "ROI removed");
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
                sequence.addROI(roi, false);
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
                sequence.removeROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

}
