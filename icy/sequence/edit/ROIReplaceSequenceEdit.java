package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI replace Sequence edit event.
 * 
 * @author Stephane
 */
public class ROIReplaceSequenceEdit extends AbstractROISequenceEdit
{
    final ROI oldRoi;

    public ROIReplaceSequenceEdit(Sequence sequence, ROI oldRoi, ROI newRoi, String name)
    {
        super(sequence, newRoi, name);

        this.oldRoi = oldRoi;
    }

    public ROIReplaceSequenceEdit(Sequence sequence, ROI oldRoi, ROI newRoi)
    {
        this(sequence, oldRoi, newRoi, "ROI replaced");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Sequence seq = getSequence();

        seq.removeROI(getROI(), false);
        seq.addROI(oldRoi, false);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Sequence seq = getSequence();

        seq.removeROI(oldRoi, false);
        seq.addROI(getROI(), false);
    }
}
