/**
 * 
 */
package icy.sequence;

import icy.roi.ROI;
import icy.undo.IcyUndoableEdit;

import java.awt.Image;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Base sequence edit container
 * 
 * @author Stephane
 */
public class SequenceEdit extends IcyUndoableEdit
{
    /**
     * @param source
     * @param icon
     */
    SequenceEdit(Object source, Image icon)
    {
        super(source, icon);
    }

    /**
     * @param source
     */
    SequenceEdit(Object source)
    {
        super(source);
    }

    /**
     * ROI add Sequence edit event
     * 
     * @author Stephane
     */
    public static class ROIAdd extends SequenceEdit
    {
        Sequence sequence;

        public ROIAdd(Sequence sequence, ROI source)
        {
            super(source);

            this.sequence = sequence;
        }

        public ROI getROI()
        {
            return (ROI) getSource();
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();

            sequence.removeROI(getROI(), false);
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();

            sequence.addROI(getROI(), false);
        }

        @Override
        public void die()
        {
            super.die();

            sequence = null;
        }

        @Override
        public String getPresentationName()
        {
            return "ROI added";
        }
    }

    /**
     * ROI remove Sequence edit event
     * 
     * @author Stephane
     */
    public static class ROIRemove extends SequenceEdit
    {
        Sequence sequence;

        public ROIRemove(Sequence sequence, ROI source)
        {
            super(source);

            this.sequence = sequence;
        }

        public ROI getROI()
        {
            return (ROI) getSource();
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();

            sequence.addROI(getROI(), false);
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();

            sequence.removeROI(getROI(), false);
        }

        @Override
        public void die()
        {
            super.die();

            sequence = null;
        }

        @Override
        public String getPresentationName()
        {
            return "ROI removed";
        }
    }

    /**
     * ROI remove Sequence edit event
     * 
     * @author Stephane
     */
    public static class ROIRemoveAll extends SequenceEdit
    {
        Sequence sequence;

        public ROIRemoveAll(Sequence sequence, List<ROI> source)
        {
            super(source);

            this.sequence = sequence;
        }

        @SuppressWarnings("unchecked")
        public List<ROI> getROIs()
        {
            return (List<ROI>) getSource();
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();

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
        public void die()
        {
            super.die();

            sequence = null;
        }

        @Override
        public String getPresentationName()
        {
            return "All ROI removed";
        }
    }

}
