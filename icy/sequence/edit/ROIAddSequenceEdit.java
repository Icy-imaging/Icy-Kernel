/**
 * 
 */
package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI add Sequence edit event
 * 
 * @author Stephane
 */
public class ROIAddSequenceEdit extends AbstractROISequenceEdit
{
    public ROIAddSequenceEdit(Sequence sequence, ROI source, String name)
    {
        super(sequence, source, name);
    }

    public ROIAddSequenceEdit(Sequence sequence, ROI source)
    {
        this(sequence, source, "ROI added");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        getSequence().removeROI(getROI(), false);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        getSequence().addROI(getROI(), false);
    }
}
