package icy.roi.edit;

import icy.roi.ROI;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Property change implementation for ROI undoable edition
 * 
 * @author Stephane
 */
public class PropertyROIEdit extends AbstractROIEdit
{
    String propertyName;
    Object previousValue;
    Object currentValue;

    public PropertyROIEdit(ROI roi, String propertyName, Object previousValue, Object currentValue)
    {
        super(roi, "ROI " + propertyName + " changed");

        this.propertyName = propertyName;
        this.previousValue = previousValue;
        this.currentValue = currentValue;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().setPropertyValue(propertyName, previousValue);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI().setPropertyValue(propertyName, currentValue);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (edit instanceof PropertyROIEdit)
        {
            final PropertyROIEdit propEdit = (PropertyROIEdit) edit;

            // same ROI and same property ?
            if ((propEdit.getROI() == getROI()) && propEdit.propertyName.equals(propertyName))
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

        previousValue = null;
        currentValue = null;
    }
}
