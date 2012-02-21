/**
 * 
 */
package icy.painter;

import vtk.vtkActor;
import vtk.vtkActor2D;

/**
 * Basic VTK painter.<br>
 * Painter implementing this interface are automatically
 * added / removed from Canvas3D.
 * 
 * @author Stephane
 */
public interface VtkPainter
{
    public vtkActor[] getActors();

    public vtkActor2D[] getActors2D();
}
