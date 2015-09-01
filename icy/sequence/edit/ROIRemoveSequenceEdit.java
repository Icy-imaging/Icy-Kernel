package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI remove Sequence edit event
 * 
 * @author Stephane
 */
public class ROIRemoveSequenceEdit extends AbstractROISequenceEdit
{
    public ROIRemoveSequenceEdit(Sequence sequence, ROI source)
    {
        super(sequence, source, "ROI removed");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        getSequence().addROI(getROI(), false);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        getSequence().removeROI(getROI(), false);
    }
}
