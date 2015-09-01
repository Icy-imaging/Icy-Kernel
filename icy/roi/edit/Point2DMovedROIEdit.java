/**
 * 
 */
package icy.roi.edit;

import icy.painter.Anchor2D;

import java.awt.geom.Point2D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import plugins.kernel.roi.roi2d.ROI2DShape;

/**
 * Control point position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point2DMovedROIEdit extends AbstractPoint2DROIEdit
{
    protected Point2D prevPos;
    protected Point2D currentPos;

    public Point2DMovedROIEdit(ROI2DShape roi, Anchor2D point, Point2D prevPos)
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

        if (edit instanceof Point2DMovedROIEdit)
        {
            final Point2DMovedROIEdit posEdit = (Point2DMovedROIEdit) edit;

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
