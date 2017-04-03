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

import plugins.kernel.roi.roi3d.ROI3DPolyLine;

/**
 * 3D control point added implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point3DAddedROIEdit extends AbstractPoint3DROIEdit
{
    Point3D position;
    final int index;

    public Point3DAddedROIEdit(ROI3D roi, Anchor3D point)
    {
        super(roi, point, "ROI point added");

        position = point.getPosition();

        if (roi instanceof ROI3DPolyLine)
            index = ((ROI3DPolyLine) roi).getControlPoints().indexOf(point);
        else
            throw new IllegalArgumentException("Point3DAddedROIEdit: " + roi.getClassName() + " class not supported !");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.removePoint(point);
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        point.setPosition(position);
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.addPoint(point, Math.min(index, roi.getControlPoints().size()));
        }
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
