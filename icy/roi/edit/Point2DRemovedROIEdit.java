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
 * 2D control point removed implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point2DRemovedROIEdit extends AbstractPoint2DROIEdit
{
    Point2D position;
    final int index;

    public Point2DRemovedROIEdit(ROI2DShape roi, Anchor2D point)
    {
        super(roi, point, "ROI point removed");

        position = point.getPosition();
        index = roi.getControlPoints().indexOf(point);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        point.setPosition(position);
        ((ROI2DShape) getROI()).addPoint(point, Math.min(index, getROI2DShape().getControlPoints().size()));
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        ((ROI2DShape) getROI()).removePoint(point);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        // don't collapse here
        return false;
    }

    @Override
    public void die()
    {
        super.die();

        position = null;
    }

}
