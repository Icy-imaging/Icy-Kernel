/**
 * 
 */
package icy.roi.edit;

import icy.main.Icy;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.undo.AbstractIcyUndoableEdit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base multiple ROI undoable edit.
 * 
 * @author Stephane
 */
public abstract class AbstractROIsEdit extends AbstractIcyUndoableEdit
{
    public AbstractROIsEdit(List<? extends ROI> rois, String name)
    {
        super(rois, name);
    }

    public AbstractROIsEdit(List<? extends ROI> rois)
    {
        this(rois, (rois.size() > 1) ? "ROIs changed" : "ROI changed");
    }

    @SuppressWarnings("unchecked")
    public List<? extends ROI> getROIs()
    {
        return (List<? extends ROI>) getSource();
    }

    protected Set<Sequence> getSequences()
    {
        final Set<Sequence> result = new HashSet<Sequence>();

        for (ROI roi : getROIs())
            result.addAll(Icy.getMainInterface().getSequencesContaining(roi));

        return result;
    }
}
