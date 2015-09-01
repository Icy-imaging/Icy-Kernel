/**
 * 
 */
package icy.sequence.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.awt.Image;

/**
 * Abstract ROI sequence undoable edit.
 * 
 * @author Stephane
 */
public class AbstractROISequenceEdit extends AbstractSequenceEdit
{
    ROI roi;

    public AbstractROISequenceEdit(Sequence sequence, ROI roi, String name, Image icon)
    {
        super(sequence, name, icon);

        this.roi = roi;
    }

    public AbstractROISequenceEdit(Sequence sequence, ROI roi, String name)
    {
        this(sequence, roi, name, roi.getIcon());
    }

    public AbstractROISequenceEdit(Sequence sequence, ROI roi)
    {
        this(sequence, roi, "ROI changed", roi.getIcon());
    }

    public ROI getROI()
    {
        return roi;
    }

    @Override
    public void die()
    {
        super.die();

        roi = null;
    }
}
