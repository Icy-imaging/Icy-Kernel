/**
 * 
 */
package icy.vtk;

import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;

import java.awt.Color;

import vtk.vtkColorTransferFunction;
import vtk.vtkPiecewiseFunction;

/**
 * Class to represent a 3D binary image as a 3D VTK volume object.
 * 
 * @author Stephane
 */
public class VtkBinaryVolume extends VtkImageVolume
{
    /**
     * Always single channel here
     */
    @Override
    protected int getChannelCount()
    {
        return 1;
    }

    /**
     * Set the color of the binary volume.
     */
    public void setColor(Color value)
    {
        volumeProperty.SetColor(VtkUtil.getBinaryColorMap(value));
    }

    /**
     * Set the opacity of the binary volume.
     */
    public void setOpacity(double value)
    {
        volumeProperty.SetScalarOpacity(VtkUtil.getBinaryOpacityMap(value));
    }

    /**
     * @deprecated Use {@link #setColor(Color)} instead.
     */
    @Override
    @Deprecated
    public void setColorMap(vtkColorTransferFunction map, int channel)
    {
        // done nothing here
    }

    /**
     * @deprecated Use {@link #setOpacity(double)} instead.
     */
    @Override
    @Deprecated
    public void setOpacityMap(vtkPiecewiseFunction map, int channel)
    {
        // done nothing here
    }

    /**
     * @deprecated Use {@link #setColor(Color)} and {@link #setOpacity(double)} instead.
     */
    @Override
    @Deprecated
    public void setLUT(LUT value)
    {
        // done nothing here
    }

    /**
     * @deprecated Use {@link #setColor(Color)} and {@link #setOpacity(double)} instead.
     */
    @Override
    @Deprecated
    public void setLUT(LUTChannel lutChannel, int channel)
    {
        // done nothing here
    }

    /**
     * Enable / Disable the shading (global)
     */
    @Override
    public void setShade(boolean value)
    {
        volumeProperty.SetShade(value ? 1 : 0);
    }

    /**
     * Sets the ambient lighting coefficient (global)
     */
    @Override
    public void setAmbient(double value)
    {
        volumeProperty.SetAmbient(value);
    }

    /**
     * Sets the diffuse lighting coefficient (global)
     */
    @Override
    public void setDiffuse(double value)
    {
        volumeProperty.SetDiffuse(value);
    }

    /**
     * Sets the specular lighting coefficient (global)
     */
    @Override
    public void setSpecular(double value)
    {
        volumeProperty.SetSpecular(value);
    }

    /**
     * Sets the specular power (global)
     */
    @Override
    public void setSpecularPower(double value)
    {
        volumeProperty.SetSpecularPower(value);
    }
}
