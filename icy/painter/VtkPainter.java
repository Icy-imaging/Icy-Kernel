/**
 * 
 */
package icy.painter;

import vtk.vtkActor;
import vtk.vtkActor2D;

/**
 * Basic VTK painter
 * 
 * @author Stephane
 */
public interface VtkPainter
{
    public vtkActor[] getActors();

    public vtkActor2D[] getActors2D();
}
