/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
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
    public static class ROIAddEdit extends SequenceEdit
    {
        Sequence sequence;

        public ROIAddEdit(Sequence sequence, ROI source)
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
     * ROI group add Sequence edit event
     * 
     * @author Stephane
     */
    public static class ROIAddsEdit extends SequenceEdit
    {
        Sequence sequence;

        public ROIAddsEdit(Sequence sequence, List<ROI> source)
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
    public static class ROIRemoveEdit extends SequenceEdit
    {
        Sequence sequence;

        public ROIRemoveEdit(Sequence sequence, ROI source)
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
     * ROI group remove Sequence edit event
     * 
     * @author Stephane
     */
    public static class ROIRemovesEdit extends SequenceEdit
    {
        Sequence sequence;

        public ROIRemovesEdit(Sequence sequence, List<ROI> source)
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
            return "ROI group removed";
        }
    }

}
