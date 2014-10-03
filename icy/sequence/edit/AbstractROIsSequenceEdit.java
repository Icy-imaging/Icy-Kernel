/**
 * 
 */
package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.awt.Image;
import java.util.List;

/**
 * Abstract ROI list sequence undoable edit.
 * 
 * @author Stephane
 */
public class AbstractROIsSequenceEdit extends AbstractSequenceEdit
{
    List<ROI> rois;

    public AbstractROIsSequenceEdit(Sequence sequence, List<ROI> rois, String name, Image icon)
    {
        super(sequence, name, icon);

        this.rois = rois;
    }

    public AbstractROIsSequenceEdit(Sequence sequence, List<ROI> rois, String name)
    {
        this(sequence, rois, name, null);
    }

    public AbstractROIsSequenceEdit(Sequence sequence, List<ROI> rois)
    {
        this(sequence, rois, (rois.size() > 1) ? "ROIs changed" : "ROI changed", null);
    }

    public List<ROI> getROIs()
    {
        return rois;
    }

    @Override
    public void die()
    {
        super.die();

        rois = null;
    }
}
