/**
 * 
 */
package icy.roi.edit;

import icy.painter.Anchor3D;
import icy.roi.ROI3D;
import icy.type.point.Point3D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * 3D control point position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point3DMovedROIEdit extends AbstractPoint3DROIEdit
{
    protected Point3D prevPos;
    protected Point3D currentPos;

    public Point3DMovedROIEdit(ROI3D roi, Anchor3D point, Point3D prevPos)
    {
        super(roi, point, "ROI point moved");

        this.prevPos = prevPos;
        this.currentPos = point.getPosition();
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        point.setPosition(prevPos);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        point.setPosition(currentPos);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof Point3DMovedROIEdit)
        {
            final Point3DMovedROIEdit posEdit = (Point3DMovedROIEdit) edit;

            // same ROI and point ?
            if ((posEdit.getROI() == getROI()) && (posEdit.getPoint() == getPoint()))
            {
                // collapse edits
                currentPos = posEdit.currentPos;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        prevPos = null;
        currentPos = null;
    }
}
