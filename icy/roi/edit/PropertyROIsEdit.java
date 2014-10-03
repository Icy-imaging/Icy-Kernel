/**
 * 
 */
package icy.roi.edit;

import icy.roi.ROI;
import icy.sequence.Sequence;

import java.util.List;
import java.util.Set;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Property change implementation for multiple ROI undoable edition
 * 
 * @author Stephane
 */
public class PropertyROIsEdit extends AbstractROIsEdit
{
    String propertyName;
    List<Object> previousValues;
    Object currentValue;

    public PropertyROIsEdit(List<? extends ROI> rois, String propertyName, List<Object> previousValues,
            Object currentValue)
    {
        super(rois, (rois.size() > 1) ? "ROIs " + propertyName + " changed" : "ROI " + propertyName + " changed");

        if (rois.size() != previousValues.size())
            throw new IllegalArgumentException("ROI list and previous values list size do not match (" + rois.size()
                    + " != " + previousValues.size() + ")");

        this.propertyName = propertyName;
        this.previousValues = previousValues;
        this.currentValue = currentValue;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Set<Sequence> sequences = getSequences();

        // undo
        for (Sequence sequence : sequences)
            sequence.beginUpdate();
        try
        {
            int ind = 0;
            for (ROI roi : getROIs())
                roi.setPropertyValue(propertyName, previousValues.get(ind++));
        }
        finally
        {
            for (Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Set<Sequence> sequences = getSequences();

        // redo
        for (Sequence sequence : sequences)
            sequence.beginUpdate();
        try
        {
            for (ROI roi : getROIs())
                roi.setPropertyValue(propertyName, currentValue);
        }
        finally
        {
            for (Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (edit instanceof PropertyROIsEdit)
        {
            final PropertyROIsEdit propEdit = (PropertyROIsEdit) edit;

            // same property and same ROI list ?
            if (propEdit.propertyName.equals(propertyName) && propEdit.getROIs().equals(getROIs()))
            {
                // collapse edits
                currentValue = propEdit.currentValue;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        previousValues = null;
        currentValue = null;
    }
}
