/**
 * 
 */
package icy.painter;

import vtk.vtkProp;

/**
 * Basic VTK painter.<br>
 * Painter implementing this interface are automatically
 * added / removed from Canvas3D.
 * 
 * @author Stephane
 */
public interface VtkPainter
{
    /**
     * Returns the VTK actors for this painter.
     */
    public vtkProp[] getProps();
}
