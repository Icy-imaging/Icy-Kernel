/**
 * 
 */
package icy.roi.edit;

import icy.painter.Anchor3D;
import icy.roi.ROI3D;
import icy.type.point.Point3D;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import plugins.kernel.roi.roi3d.ROI3DPolyLine;

/**
 * 3D control point removed implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point3DRemovedROIEdit extends AbstractPoint3DROIEdit
{
    Point3D position;
    final int index;

    public Point3DRemovedROIEdit(ROI3D roi, List<Anchor3D> previousPoints, Anchor3D point)
    {
        super(roi, point, "ROI point removed");

        position = point.getPosition();

        // we need to save the index in the old point list
        int i = 0;
        for (Anchor3D p : previousPoints)
        {
            if (p.getPosition().equals(position))
                break;

            i++;
        }

        index = i;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        point.setPosition(position);
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.addPoint(point, Math.min(index, roi.getControlPoints().size()));
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.removePoint(point);
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
