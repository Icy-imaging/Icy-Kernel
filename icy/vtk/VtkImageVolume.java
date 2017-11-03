/**
 * 
 */
package icy.vtk;

import icy.image.colormap.IcyColorMap;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;
import icy.type.DataType;

import vtk.vtkColorTransferFunction;
import vtk.vtkFixedPointVolumeRayCastMapper;
import vtk.vtkGPUVolumeRayCastMapper;
import vtk.vtkImageData;
import vtk.vtkOpenGLGPUVolumeRayCastMapper;
import vtk.vtkPiecewiseFunction;
import vtk.vtkRenderer;
import vtk.vtkVolume;
import vtk.vtkVolumeMapper;
import vtk.vtkVolumeProperty;
import vtk.vtkVolumeRayCastMapper;

/**
 * Class to represent a 3D image as a 3D VTK volume object.
 * 
 * @author Stephane
 */
public class VtkImageVolume
{
    /**
     * @deprecated
     */
    @Deprecated
    public static enum VtkVolumeMapperType
    {
        RAYCAST_CPU_FIXEDPOINT
        {
            @Override
            public String toString()
            {
                return "Raycaster (CPU)";
            }
        },
        RAYCAST_GPU_OPENGL
        {
            @Override
            public String toString()
            {
                return "Raycaster (OpenGL)";
            }
        },
        TEXTURE2D_OPENGL
        {
            @Override
            public String toString()
            {
                return "Texture 2D (OpenGL)";
            }
        },
        TEXTURE3D_OPENGL
        {
            @Override
            public String toString()
            {
                return "Texture 3D (OpenGL)";
            }
        };
    }

    public static enum VtkVolumeBlendType
    {
        COMPOSITE
        {
            @Override
            public String toString()
            {
                return "Composite";
            }
        },
        MAXIMUM_INTENSITY
        {
            @Override
            public String toString()
            {
                return "Maximum intensity";
            }
        },
        MINIMUM_INTENSITY
        {
            @Override
            public String toString()
            {
                return "Minimum intensity";
            }
        },

        ADDITIVE
        {
            @Override
            public String toString()
            {
                return "Additive";
            }
        };
    }

    /**
     * volume data
     */
    protected vtkVolumeMapper volumeMapper;
    protected vtkVolume volume;
    protected vtkVolumeProperty volumeProperty;
    protected vtkImageData imageData;

    public VtkImageVolume()
    {
        super();

        // build volume property object
        volumeProperty = new vtkVolumeProperty();
        // default volume setup
        volumeProperty.IndependentComponentsOn();
        volumeProperty.DisableGradientOpacityOn();
        setShade(false);
        setAmbient(0.5d);
        setDiffuse(0.4d);
        setSpecular(0.4d);
        setInterpolationMode(VtkUtil.VTK_LINEAR_INTERPOLATION);

        // build default volume mapper
        volumeMapper = new vtkFixedPointVolumeRayCastMapper();
        ((vtkFixedPointVolumeRayCastMapper) volumeMapper).IntermixIntersectingGeometryOn();
        setSampleResolution(0);

        // initialize volume data
        volume = new vtkVolume();
        volume.SetProperty(volumeProperty);
        // setup volume connection
        volume.SetMapper(volumeMapper);
        // volume should not be "pickable" by default
        volume.SetPickable(0);

        imageData = null;
    }

    public void release()
    {
        // delete all VTK objects
        volume.Delete();
        volumeMapper.RemoveAllInputs();
        volumeMapper.Delete();
        volumeProperty.Delete();

        if (imageData != null)
        {
            imageData.GetPointData().GetScalars().Delete();
            imageData.GetPointData().Delete();
            imageData.Delete();
        }

        // after Delete we need to release reference
        volume = null;
        volumeMapper = null;
        volumeProperty = null;
        imageData = null;
    }

    public vtkVolume getVolume()
    {
        return volume;
    }

    /**
     * Return the number of channel contained in image data.
     */
    protected int getChannelCount()
    {
        if (imageData != null)
            return imageData.GetNumberOfScalarComponents();

        // assume 1 by default
        return 1;
    }

    /**
     * Sets the color map ({@link vtkColorTransferFunction}) used to render the specified channel of
     * image volume.
     */
    public void setColorMap(vtkColorTransferFunction map, int channel)
    {
        vtkColorTransferFunction oldMap = volumeProperty.GetRGBTransferFunction(channel);
        // global colormap, don't release it
        if (volumeProperty.GetRGBTransferFunction() == oldMap)
            oldMap = null;

        volumeProperty.SetColor(channel, map);
        // delete previous color transfer function if any
        if (oldMap != null)
            oldMap.Delete();
    }

    /**
     * Sets the opacity map ({@link vtkPiecewiseFunction}) used to render the specified channel of
     * image volume.
     */
    public void setOpacityMap(vtkPiecewiseFunction map, int channel)
    {
        vtkPiecewiseFunction oldMap = volumeProperty.GetScalarOpacity(channel);
        // global opacity, don't release it
        if (volumeProperty.GetScalarOpacity() == oldMap)
            oldMap = null;

        volumeProperty.SetScalarOpacity(channel, map);
        // delete previous opacity function if any
        if (oldMap != null)
            oldMap.Delete();
    }

    /**
     * Sets the {@link LUT} used to render the image volume.
     */
    public void setLUT(LUT value)
    {
        for (int channel = 0; channel < Math.min(value.getNumChannel(), getChannelCount()); channel++)
            setLUT(value.getLutChannel(channel), channel);
    }

    /**
     * Sets the {@link LUTChannel} used to render the specified channel of image volume.
     */
    public void setLUT(LUTChannel lutChannel, int channel)
    {
        final IcyColorMap colorMap = lutChannel.getColorMap();
        final Scaler scaler = lutChannel.getScaler();

        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction newColorMap = new vtkColorTransferFunction();

        newColorMap.SetRange(scaler.getLeftIn(), scaler.getRightIn());
        for (int i = 0; i < IcyColorMap.SIZE; i++)
        {
            newColorMap.AddRGBPoint(scaler.unscale(i), colorMap.getNormalizedRed(i), colorMap.getNormalizedGreen(i),
                    colorMap.getNormalizedBlue(i));
        }

        vtkColorTransferFunction oldColorMap = volumeProperty.GetRGBTransferFunction(channel);
        // global colormap, don't release it
        if (volumeProperty.GetRGBTransferFunction() == oldColorMap)
            oldColorMap = null;

        volumeProperty.SetColor(channel, newColorMap);
        // delete previous color transfer function if any
        if (oldColorMap != null)
            oldColorMap.Delete();

        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction newOpacity = new vtkPiecewiseFunction();

        if (colorMap.isEnabled())
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                newOpacity.AddPoint(scaler.unscale(i), colorMap.getNormalizedAlpha(i));
        }
        else
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                newOpacity.AddPoint(scaler.unscale(i), 0d);
        }

        vtkPiecewiseFunction oldOpacity = volumeProperty.GetScalarOpacity(channel);
        // global opacity, don't release it
        if (volumeProperty.GetScalarOpacity() == oldOpacity)
            oldOpacity = null;

        volumeProperty.SetScalarOpacity(channel, newOpacity);
        // delete previous opacity function if any
        if (oldOpacity != null)
            oldOpacity.Delete();
    }

    /**
     * Get the sample resolution of the raycaster volume rendering.<br>
     * <ul>
     * <li>0 = automatic</li>
     * <li>1 = finest (slow)</li>
     * <li>10 = coarse (fast)</li>
     * </ul>
     */
    public double getSampleResolution()
    {
        if (volumeMapper instanceof vtkFixedPointVolumeRayCastMapper)
        {
            final vtkFixedPointVolumeRayCastMapper mapper = (vtkFixedPointVolumeRayCastMapper) volumeMapper;

            if (mapper.GetAutoAdjustSampleDistances() != 0)
                return 0d;

            return mapper.GetImageSampleDistance();
        }
        else if (volumeMapper instanceof vtkVolumeRayCastMapper)
        {
            final vtkVolumeRayCastMapper mapper = (vtkVolumeRayCastMapper) volumeMapper;

            if (mapper.GetAutoAdjustSampleDistances() != 0)
                return 0d;

            return mapper.GetImageSampleDistance();
        }
        else if (volumeMapper instanceof vtkGPUVolumeRayCastMapper)
        {
            final vtkGPUVolumeRayCastMapper mapper = (vtkGPUVolumeRayCastMapper) volumeMapper;

            if (mapper.GetAutoAdjustSampleDistances() != 0)
                return 0d;

            return mapper.GetImageSampleDistance();
        }
        else if (volumeMapper instanceof vtkOpenGLGPUVolumeRayCastMapper)
        {
            final vtkOpenGLGPUVolumeRayCastMapper mapper = (vtkOpenGLGPUVolumeRayCastMapper) volumeMapper;

            if (mapper.GetAutoAdjustSampleDistances() != 0)
                return 0d;

            return mapper.GetImageSampleDistance();
        }

        return 0d;
    }

    /**
     * Set sample resolution for the raycaster volume rendering.<br>
     * <ul>
     * <li>0 = automatic</li>
     * <li>1 = finest (slow)</li>
     * <li>10 = coarse (fast)</li>
     * </ul>
     */
    public void setSampleResolution(double value)
    {
        if (volumeMapper instanceof vtkFixedPointVolumeRayCastMapper)
        {
            final vtkFixedPointVolumeRayCastMapper mapper = (vtkFixedPointVolumeRayCastMapper) volumeMapper;

            if (value == 0d)
                mapper.AutoAdjustSampleDistancesOn();
            else
            {
                mapper.AutoAdjustSampleDistancesOff();
                mapper.SetImageSampleDistance(value);
            }
        }
        else if (volumeMapper instanceof vtkVolumeRayCastMapper)
        {
            final vtkVolumeRayCastMapper mapper = (vtkVolumeRayCastMapper) volumeMapper;

            if (value == 0d)
                mapper.AutoAdjustSampleDistancesOn();
            else
            {
                mapper.AutoAdjustSampleDistancesOff();
                mapper.SetImageSampleDistance(value);
            }
        }
        else if (volumeMapper instanceof vtkGPUVolumeRayCastMapper)
        {
            final vtkGPUVolumeRayCastMapper mapper = (vtkGPUVolumeRayCastMapper) volumeMapper;

            if (value == 0d)
                mapper.AutoAdjustSampleDistancesOn();
            else
            {
                mapper.AutoAdjustSampleDistancesOff();
                mapper.SetImageSampleDistance(value);
            }
        }
        else if (volumeMapper instanceof vtkOpenGLGPUVolumeRayCastMapper)
        {
            final vtkOpenGLGPUVolumeRayCastMapper mapper = (vtkOpenGLGPUVolumeRayCastMapper) volumeMapper;

            if (value == 0d)
                mapper.AutoAdjustSampleDistancesOn();
            else
            {
                mapper.AutoAdjustSampleDistancesOff();
                mapper.SetImageSampleDistance(value);
            }
        }
    }

    public boolean isPickable()
    {
        return (volume.GetPickable() != 0) ? true : false;
    }

    public void setPickable(boolean value)
    {
        volume.SetPickable(value ? 1 : 0);
    }

    /**
     * Returns the XYZ scaling of the volume image
     */
    public double[] getScale()
    {
        return volume.GetScale();
    }

    /**
     * Sets the XYZ scaling of the volume image
     */
    public void setScale(double x, double y, double z)
    {
        volume.SetScale(x, y, z);
    }

    /**
     * Sets the XYZ scaling of the volume image
     */
    public void setScale(double[] xyz)
    {
        volume.SetScale(xyz);
    }

    /**
     * Returns <code>true</code> if shading is enabled (global)
     */
    public boolean getShade()
    {
        return (volumeProperty.GetShade() == 1) ? true : false;
    }

    /**
     * Returns <code>true</code> if shading is enabled for the specified component
     */
    // public boolean getShade(int index)
    // {
    // return (volumeProperty.GetShade(index) == 1) ? true : false;
    // }

    /**
     * Enable / Disable the shading (global)
     */
    public void setShade(boolean value)
    {
        final int num = getChannelCount();
        for (int ch = 0; ch < num; ch++)
            volumeProperty.SetShade(ch, value ? 1 : 0);

        volumeProperty.SetShade(value ? 1 : 0);
    }

    /**
     * Enable / Disable the shading for the specified component
     */
    // public void setShade(int index, boolean value)
    // {
    // volumeProperty.SetShade(index, value ? 1 : 0);
    // }

    /**
     * Returns the ambient lighting coefficient (global)
     */
    public double getAmbient()
    {
        return volumeProperty.GetAmbient();
    }

    /**
     * Returns the ambient lighting coefficient for the specified component
     */
    // public double getAmbient(int index)
    // {
    // return volumeProperty.GetAmbient(index);
    // }

    /**
     * Sets the ambient lighting coefficient (global)
     */
    public void setAmbient(double value)
    {
        final int num = getChannelCount();
        for (int ch = 0; ch < num; ch++)
            volumeProperty.SetAmbient(ch, value);

        volumeProperty.SetAmbient(value);
    }

    /**
     * Sets the ambient lighting coefficient for the specified component
     */
    // public void setAmbient(int index, double value)
    // {
    // volumeProperty.SetAmbient(index, value);
    // }

    /**
     * Returns the diffuse lighting coefficient (global)
     */
    public double getDiffuse()
    {
        return volumeProperty.GetDiffuse();
    }

    /**
     * Returns the diffuse lighting coefficient for the specified component
     */
    // public double getDiffuse(int index)
    // {
    // return volumeProperty.GetDiffuse(index);
    // }

    /**
     * Sets the diffuse lighting coefficient (global)
     */
    public void setDiffuse(double value)
    {
        final int num = getChannelCount();
        for (int ch = 0; ch < num; ch++)
            volumeProperty.SetDiffuse(ch, value);

        volumeProperty.SetDiffuse(value);
    }

    /**
     * Sets the diffuse lighting coefficient for the specified component
     */
    // public void setDiffuse(int index, double value)
    // {
    // volumeProperty.SetDiffuse(index, value);
    // }

    /**
     * Returns the specular lighting coefficient (global)
     */
    public double getSpecular()
    {
        return volumeProperty.GetSpecular();
    }

    /**
     * Returns the specular lighting coefficient for the specified component
     */
    // public double getSpecular(int index)
    // {
    // return volumeProperty.GetSpecular(index);
    // }

    /**
     * Sets the specular lighting coefficient (global)
     */
    public void setSpecular(double value)
    {
        final int num = getChannelCount();
        for (int ch = 0; ch < num; ch++)
            volumeProperty.SetSpecular(ch, value);

        volumeProperty.SetSpecular(value);
    }

    /**
     * Sets the specular lighting coefficient for the specified component
     */
    // public void setSpecular(int index, double value)
    // {
    // volumeProperty.SetSpecular(index, value);
    // }

    /**
     * Returns the specular power (global)
     */
    public double getSpecularPower()
    {
        return volumeProperty.GetSpecularPower();
    }

    /**
     * Returns the specular power for the specified component
     */
    // public double getSpecularPower(int index)
    // {
    // return volumeProperty.GetSpecularPower(index);
    // }

    /**
     * Sets the specular power (global)
     */
    public void setSpecularPower(double value)
    {
        final int num = getChannelCount();
        for (int ch = 0; ch < num; ch++)
            volumeProperty.SetSpecularPower(ch, value);

        volumeProperty.SetSpecularPower(value);
    }

    /**
     * Sets the specular power for the specified component
     */
    // public void setSpecularPower(int index, double value)
    // {
    // volumeProperty.SetSpecularPower(index, value);
    // }

    /**
     * Returns the interpolation method for rendering.<br>
     * Possible values are:
     * <ul>
     * <li>VTK_NEAREST_INTERPOLATION</li>
     * <li>VTK_LINEAR_INTERPOLATION</li>
     * <li>VTK_CUBIC_INTERPOLATION</li>
     * </ul>
     */
    public int getInterpolationMode()
    {
        return volumeProperty.GetInterpolationType();
    }

    /**
     * Sets the interpolation method for rendering.<br>
     * Possible values are:
     * <ul>
     * <li>VTK_NEAREST_INTERPOLATION</li>
     * <li>VTK_LINEAR_INTERPOLATION</li>
     * <li>VTK_CUBIC_INTERPOLATION</li>
     * </ul>
     */
    public void setInterpolationMode(int value)
    {
        volumeProperty.SetInterpolationType(value);
    }

    /**
     * Returns true if selected volume mapper is the GPU accelerated raycaster.
     */
    public boolean getGPURendering()
    {
        return (volumeMapper instanceof vtkOpenGLGPUVolumeRayCastMapper);
    }

    /**
     * Enable GPU volume rendering.
     * 
     * @param value
     *        if <code>true</code> then the GPU accelerated raycaster will be used otherwise the classical CPU
     *        rayscaster is used.
     */
    public boolean setGPURendering(boolean value)
    {
        // volume mapper changed ?
        if (getGPURendering() != value)
        {
            // save the parameters as they can be modified when mapper change
            final VtkVolumeBlendType blendingMode = getBlendingMode();
            final double sampleResolution = getSampleResolution();
            final vtkVolumeMapper newMapper;

            if (value)
            {
                // GPU raycaster
                newMapper = new vtkOpenGLGPUVolumeRayCastMapper();
            }
            else
            {
                // CPU raycaster
                newMapper = new vtkFixedPointVolumeRayCastMapper();
                ((vtkFixedPointVolumeRayCastMapper) newMapper).IntermixIntersectingGeometryOn();
            }

            // setup volume connection
            volume.SetMapper(newMapper);

            // release previous mapper if any
            if (volumeMapper != null)
            {
                volumeMapper.RemoveAllInputs();
                volumeMapper.Delete();
            }

            // update volume mapper
            volumeMapper = newMapper;
            // and connect the image data
            if (imageData != null)
                newMapper.SetInputData(imageData);

            // restore blending and sample resolution
            setBlendingMode(blendingMode);
            setSampleResolution(sampleResolution);

            return true;
        }

        return false;
    }

    /**
     * @deprecated Should always return true now.
     */
    @Deprecated
    public static boolean isMapperSupported(vtkRenderer renderer)
    {
        return true;
    }

    /**
     * Returns the blending method for rendering.
     */
    public VtkVolumeBlendType getBlendingMode()
    {
        return VtkVolumeBlendType.values()[volumeMapper.GetBlendMode()];
    }

    /**
     * Sets the blending method for rendering.
     */
    public void setBlendingMode(VtkVolumeBlendType value)
    {
        volumeMapper.SetBlendMode(value.ordinal());
    }

    /**
     * Get the current volume image data object.
     * 
     * @see VtkUtil#getImageData(Object, DataType, int, int, int, int)
     */
    public vtkImageData getVolumeData()
    {
        return imageData;
    }

    /**
     * Set the volume image data.
     * 
     * @see VtkUtil#getImageData(Object, DataType, int, int, int, int)
     */
    public void setVolumeData(vtkImageData data)
    {
        if (imageData != data)
        {
            // set connection
            volumeMapper.SetInputData(data);

            // release previous volume data memory
            if (imageData != null)
            {
                imageData.GetPointData().GetScalars().Delete();
                imageData.GetPointData().Delete();
                imageData.Delete();
            }

            // set to new image data
            imageData = data;
        }

        updateChannelProperties();
    }

    /**
     * Refresh channel properties
     */
    protected void updateChannelProperties()
    {
        setShade(getShade());
        setAmbient(getAmbient());
        setDiffuse(getDiffuse());
        setSpecular(getSpecular());
        setSpecularPower(getSpecularPower());
    }

    /**
     * Sets the visible state of the image volume object
     */
    public void setVisible(boolean value)
    {
        volume.SetVisibility(value ? 1 : 0);
    }

    /**
     * @return visible state of the image volume object
     */
    public boolean isVisible()
    {
        return (volume.GetVisibility() != 0) ? true : false;
    }
}
