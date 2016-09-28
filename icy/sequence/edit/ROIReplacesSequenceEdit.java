/**
 * 
 */
package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.util.Collection;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI group replace Sequence edit event.
 * 
 * @author Stephane
 */
public class ROIReplacesSequenceEdit extends AbstractROIsSequenceEdit
{
    final Collection<ROI> oldRois;

    public ROIReplacesSequenceEdit(Sequence sequence, Collection<ROI> oldRois, Collection<ROI> newRois, String name)
    {
        super(sequence, newRois, name);

        this.oldRois = oldRois;
    }

    public ROIReplacesSequenceEdit(Sequence sequence, Collection<ROI> oldRois, Collection<ROI> newRois)
    {
        this(sequence, oldRois, newRois, (newRois.size() > 1) ? "ROI group replaced" : "ROI replaced");
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
            for (ROI roi : oldRois)
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
            for (ROI roi : oldRois)
                sequence.removeROI(roi, false);
            for (ROI roi : getROIs())
                sequence.addROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }
}
