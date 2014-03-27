/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.canvas;

import icy.gui.viewer.Viewer;

import java.awt.image.BufferedImage;

import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkPanel;
import vtk.vtkRenderer;

/**
 * @deprecated Use {@link VtkCanvas} instead.
 * @author Fabrice de Chaumont & Stephane
 */
@Deprecated
public abstract class Canvas3D extends IcyCanvas3D
{
    private static final long serialVersionUID = -2677870897470280726L;

    public Canvas3D(Viewer viewer)
    {
        super(viewer);
    }

    /**
     * Adjust the volume image resolution rendering.<br>
     * <ul>
     * <li>0 = auto selection</li>
     * <li>1 = highest resolution (slow)</li>
     * <li>10 = lowest resolution (fast)</li>
     * </ul>
     * 
     * @param value
     */
    public abstract void setVolumeDistanceSample(int value);

    public abstract vtkPanel getPanel3D();

    public abstract vtkRenderer getRenderer();

    /**
     * Get scaling for image volume rendering
     */
    public abstract double[] getVolumeScale();

    /**
     * @deprecated Use {@link #getVolumeScale()} instead.
     */
    @Deprecated
    public double getVolumeScaleX()
    {
        return getVolumeScale()[0];
    }

    /**
     * @deprecated Use {@link #getVolumeScale()} instead.
     */
    @Deprecated
    public double getVolumeScaleY()
    {
        return getVolumeScale()[1];
    }

    /**
     * @deprecated Use {@link #getVolumeScale()} instead.
     */
    @Deprecated
    public double getVolumeScaleZ()
    {
        return getVolumeScale()[2];
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getXScaling()
    {
        return getVolumeScaleX();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getYScaling()
    {
        return getVolumeScaleY();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getZScaling()
    {
        return getVolumeScaleZ();
    }

    /**
     * Set scaling for image volume rendering
     */
    public abstract void setVolumeScale(double x, double y, double z);

    /**
     * @deprecated Use {@link #setVolumeScale(double, double, double)} instead
     */
    @Deprecated
    public void setVolumeScaleX(double value)
    {
        final double[] scale = getVolumeScale();

        if (scale[0] != value)
        {
            scale[0] = value;
            setVolumeScale(scale[0], scale[1], scale[2]);
        }
    }

    /**
     * @deprecated Use {@link #setVolumeScale(double, double, double)} instead
     */
    @Deprecated
    public void setVolumeScaleY(double value)
    {
        final double[] scale = getVolumeScale();

        if (scale[1] != value)
        {
            scale[1] = value;
            setVolumeScale(scale[0], scale[1], scale[2]);
        }
    }

    /**
     * @deprecated Use {@link #setVolumeScale(double, double, double)} instead
     */
    @Deprecated
    public void setVolumeScaleZ(double value)
    {
        final double[] scale = getVolumeScale();

        if (scale[2] != value)
        {
            scale[2] = value;
            setVolumeScale(scale[0], scale[1], scale[2]);
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setXScaling(double value)
    {
        setVolumeScaleX(value);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setYScaling(double value)
    {
        setVolumeScaleY(value);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setZScaling(double value)
    {
        setVolumeScaleZ(value);
    }

    public abstract BufferedImage getRenderedImage(int t, int c);
}
