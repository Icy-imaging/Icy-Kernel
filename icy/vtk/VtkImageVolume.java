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
import vtk.vtkOpenGLVolumeTextureMapper2D;
import vtk.vtkOpenGLVolumeTextureMapper3D;
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
        // RAYCAST_CPU
        // {
        // @Override
        // public String toString()
        // {
        // return "Raycaster";
        // }
        // },
        // RAYCAST_GPU
        // {
        // @Override
        // public String toString()
        // {
        // return "Raycaster (GPU)";
        // }
        // },
        RAYCAST_GPU_OPENGL
        {
            @Override
            public String toString()
            {
                return "Raycaster (OpenGL)";
            }
        },
        // TEXTURE2D
        // {
        // @Override
        // public String toString()
        // {
        // return "Texture 2D";
        // }
        // },
        TEXTURE2D_OPENGL
        {
            @Override
            public String toString()
            {
                return "Texture 2D (OpenGL)";
            }
        },
        // TEXTURE3D
        // {
        // @Override
        // public String toString()
        // {
        // return "Texture 3D";
        // }
        // },
        TEXTURE3D_OPENGL
        {
            @Override
            public String toString()
            {
                return "Texture 3D (OpenGL)";
            }
        };
        // SMART
        // {
        // @Override
        // public String toString()
        // {
        // return "Smart";
        // }
        // };
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
        setSpecular(1.0d);
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

        imageData = null;
    }

    public void release()
    {
        // delete every VTK objects
        volume.FastDelete();
        volumeMapper.RemoveAllInputs();
        volumeMapper.FastDelete();
        volumeProperty.FastDelete();
        imageData.FastDelete();

        // after FastDelete we need to release reference
        volume = null;
        volumeMapper = null;
        volumeProperty = null;
        imageData = null;
    }

    public vtkVolume getVolume()
    {
        return volume;
    }

    // private void test()
    // {
    // // exemple de clipping a utiliser par la suite.
    // if ( false )
    // {
    // vtkPlane plane = new vtkPlane();
    // plane.SetOrigin(1000, 1000, 1000);
    // plane.SetNormal( 1, 1, 0);
    // volumeMapper.AddClippingPlane( plane );
    // }
    //
    // vtkOrientationMarkerWidget ow = new vtkOrientationMarkerWidget();
    // }

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
            oldMap.FastDelete();
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
            oldMap.FastDelete();
    }

    /**
     * Sets the {@link LUT} used to render the image volume.
     */
    public void setLUT(LUT value)
    {
        for (int channel = 0; channel < value.getNumChannel(); channel++)
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
            oldColorMap.FastDelete();

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
            oldOpacity.FastDelete();
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
    public boolean getShade(int index)
    {
        return (volumeProperty.GetShade(index) == 1) ? true : false;
    }

    /**
     * Enable / Disable the shading (global)
     */
    public void setShade(boolean value)
    {
        volumeProperty.SetShade(value ? 1 : 0);
    }

    /**
     * Enable / Disable the shading for the specified component
     */
    public void setShade(int index, boolean value)
    {
        volumeProperty.SetShade(index, value ? 1 : 0);
    }

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
    public double getAmbient(int index)
    {
        return volumeProperty.GetAmbient(index);
    }

    /**
     * Sets the ambient lighting coefficient (global)
     */
    public void setAmbient(double value)
    {
        volumeProperty.SetAmbient(value);
    }

    /**
     * Sets the ambient lighting coefficient for the specified component
     */
    public void setAmbient(int index, double value)
    {
        volumeProperty.SetAmbient(index, value);
    }

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
    public double getDiffuse(int index)
    {
        return volumeProperty.GetDiffuse(index);
    }

    /**
     * Sets the diffuse lighting coefficient (global)
     */
    public void setDiffuse(double value)
    {
        volumeProperty.SetDiffuse(value);
        volume.Update();
    }

    /**
     * Sets the diffuse lighting coefficient for the specified component
     */
    public void setDiffuse(int index, double value)
    {
        volumeProperty.SetDiffuse(index, value);
    }

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
    public double getSpecular(int index)
    {
        return volumeProperty.GetSpecular(index);
    }

    /**
     * Sets the specular lighting coefficient (global)
     */
    public void setSpecular(double value)
    {
        volumeProperty.SetSpecular(value);
    }

    /**
     * Sets the specular lighting coefficient for the specified component
     */
    public void setSpecular(int index, double value)
    {
        volumeProperty.SetSpecular(index, value);
    }

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
    public double getSpecularPower(int index)
    {
        return volumeProperty.GetSpecularPower(index);
    }

    /**
     * Sets the specular power (global)
     */
    public void setSpecularPower(double value)
    {
        volumeProperty.SetSpecularPower(value);
    }

    /**
     * Sets the specular power for the specified component
     */
    public void setSpecularPower(int index, double value)
    {
        volumeProperty.SetSpecularPower(index, value);
    }

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
     * Returns <code>true</code> if the current volume mapper support multi channel rendering.
     */
    public boolean isMultiChannelVolumeMapper()
    {
        return isMultiChannelVolumeMapper(getVolumeMapperType());
    }

    /**
     * Returns <code>true</code> if the specified volume mapper support multi channel rendering.
     */
    public boolean isMultiChannelVolumeMapper(VtkVolumeMapperType mapperType)
    {
        switch (mapperType)
        {
        // case RAYCAST_CPU:
            case RAYCAST_CPU_FIXEDPOINT:
                // case SMART:
                return true;

            default:
                // case RAYCAST_GPU:
            case RAYCAST_GPU_OPENGL:
                // case TEXTURE2D:
                // case TEXTURE3D:
            case TEXTURE2D_OPENGL:
            case TEXTURE3D_OPENGL:
                return false;
        }
    }

    /**
     * Returns the current volume mapper type (see {@link VtkVolumeMapperType}).
     */
    public VtkVolumeMapperType getVolumeMapperType()
    {
        if (volumeMapper instanceof vtkFixedPointVolumeRayCastMapper)
            return VtkVolumeMapperType.RAYCAST_CPU_FIXEDPOINT;
        // else if (volumeMapper instanceof vtkVolumeRayCastMapper)
        // return VtkVolumeMapperType.RAYCAST_CPU;
        // else if (volumeMapper instanceof vtkGPUVolumeRayCastMapper)
        // return VtkVolumeMapperType.RAYCAST_GPU;
        else if (volumeMapper instanceof vtkOpenGLGPUVolumeRayCastMapper)
            return VtkVolumeMapperType.RAYCAST_GPU_OPENGL;
        // else if (volumeMapper instanceof vtkSmartVolumeMapper)
        // return VtkVolumeMapperType.SMART;
        // else if (volumeMapper instanceof vtkVolumeTextureMapper2D)
        // return VtkVolumeMapperType.TEXTURE2D;
        // else if (volumeMapper instanceof vtkVolumeTextureMapper3D)
        // return VtkVolumeMapperType.TEXTURE3D;
        else if (volumeMapper instanceof vtkOpenGLVolumeTextureMapper2D)
            return VtkVolumeMapperType.TEXTURE2D_OPENGL;
        else if (volumeMapper instanceof vtkOpenGLVolumeTextureMapper3D)
            return VtkVolumeMapperType.TEXTURE3D_OPENGL;

        return null;
    }

    /**
     * Sets the current volume mapper type used to render the volume (see
     * {@link VtkVolumeMapperType}).
     */
    public boolean setVolumeMapperType(VtkVolumeMapperType value)
    {
        // volume mapper changed ?
        if (getVolumeMapperType() != value)
        {
            final vtkVolumeMapper newMapper;

            // DATA
            switch (value)
            {
                default:
                case RAYCAST_CPU_FIXEDPOINT:
                    newMapper = new vtkFixedPointVolumeRayCastMapper();
                    ((vtkFixedPointVolumeRayCastMapper) newMapper).IntermixIntersectingGeometryOn();
                    break;
                // case RAYCAST_CPU:
                // newMapper = new vtkVolumeRayCastMapper();
                // ((vtkVolumeRayCastMapper) newMapper).IntermixIntersectingGeometryOn();
                // break;
                // case RAYCAST_GPU:
                // newMapper = new vtkGPUVolumeRayCastMapper();
                // break;
                case RAYCAST_GPU_OPENGL:
                    newMapper = new vtkOpenGLGPUVolumeRayCastMapper();
                    break;
                // case SMART:
                // newMapper = new vtkSmartVolumeMapper();
                // break;
                // case TEXTURE2D:
                // newMapper = new vtkVolumeTextureMapper2D();
                // break;
                // case TEXTURE3D:
                // newMapper = new vtkVolumeTextureMapper3D();
                // break;
                case TEXTURE2D_OPENGL:
                    newMapper = new vtkOpenGLVolumeTextureMapper2D();
                    break;
                case TEXTURE3D_OPENGL:
                    newMapper = new vtkOpenGLVolumeTextureMapper3D();
                    break;
            }

            // setup volume connection
            volume.SetMapper(newMapper);

            // release previous mapper if any
            if (volumeMapper != null)
            {
                volumeMapper.RemoveAllInputs();
                volumeMapper.FastDelete();
            }

            // update volume mapper
            volumeMapper = newMapper;
            // and connect the image data
            if (imageData != null)
                newMapper.SetInputData(imageData);

            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the current selected volume mapper is supported in the specified
     * {@link vtkRenderer}
     */
    public boolean isMapperSupported(vtkRenderer renderer)
    {
        final VtkVolumeMapperType mapperType = getVolumeMapperType();

        if (mapperType == null)
            return false;

        switch (mapperType)
        {
            default:
                return true;

                // case RAYCAST_GPU:
                // return (((vtkGPUVolumeRayCastMapper)
                // volumeMapper).IsRenderSupported(renderer.GetRenderWindow(),
                // volumeProperty) != 0);
            case RAYCAST_GPU_OPENGL:
                return (((vtkOpenGLGPUVolumeRayCastMapper) volumeMapper).IsRenderSupported(renderer.GetRenderWindow(),
                        volumeProperty) != 0);
                // case TEXTURE3D:
                // return (((vtkVolumeTextureMapper3D)
                // volumeMapper).IsRenderSupported(volumeProperty, renderer) != 0);
            case TEXTURE3D_OPENGL:
                return (((vtkOpenGLVolumeTextureMapper3D) volumeMapper).IsRenderSupported(volumeProperty, renderer) != 0);
        }
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
                imageData.FastDelete();

            // set to new image data
            imageData = data;
        }
    }
}
