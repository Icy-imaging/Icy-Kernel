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
package icy.image.lut;

import icy.image.colormap.IcyColorMap;
import icy.image.colorspace.IcyColorSpace;
import icy.image.colorspace.IcyColorSpaceEvent;
import icy.image.colorspace.IcyColorSpaceEvent.IcyColorSpaceEventType;
import icy.image.colorspace.IcyColorSpaceListener;
import icy.image.lut.LUT.LUTChannel;
import icy.image.lut.LUTBandEvent.LUTBandEventType;
import icy.math.Scaler;
import icy.math.ScalerEvent;
import icy.math.ScalerEvent.ScalerEventType;
import icy.math.ScalerListener;

import javax.swing.event.EventListenerList;

/**
 * @deprecated Use {@link LUTChannel} instead.
 */
@Deprecated
public class LUTBand implements ScalerListener, IcyColorSpaceListener
{
    /**
     * parent LUT
     */
    private final LUT lut;

    /**
     * band index
     */
    private final int component;

    private boolean enabled;

    /**
     * cached
     */
    private final Scaler scaler;

    /**
     * listeners
     */
    private EventListenerList listeners;

    public LUTBand(LUT lut, int component)
    {
        this.lut = lut;
        this.component = component;

        // add listener
        getColorSpace().addListener(this);

        // cached
        scaler = lut.getScalers()[component];
        scaler.addListener(this);

        // default
        enabled = true;

        listeners = new EventListenerList();
    }

    @Override
    protected void finalize() throws Throwable
    {
        // remove listener
        getColorSpace().removeListener(this);
        scaler.removeListener(this);

        super.finalize();
    }

    public LUT getLut()
    {
        return lut;
    }

    public IcyColorSpace getColorSpace()
    {
        return lut.getColorSpace();
    }

    public IcyColorMap getColorMap()
    {
        return getColorSpace().getColorMap(component);
    }

    public void copyColorMap(IcyColorMap colorMap, boolean copyName, boolean copyAlpha)
    {
        getColorSpace().copyColormap(component, colorMap, copyName, copyAlpha);
    }

    public void copyColorMap(IcyColorMap colorMap)
    {
        copyColorMap(colorMap, true, true);
    }

    // private void adJustBounds()
    // {
    // // return default bounds ([0..255] / [-128..127]) for BYTE data type
    // if ((!adjustByteToo) && (dataType.getJavaType() == DataType.BYTE))
    // return dataType.getDefaultBounds();
    // }

    public Scaler getScaler()
    {
        return scaler;
    }

    public double getMin()
    {
        return scaler.getLeftIn();
    }

    public void setMin(double value)
    {
        scaler.setLeftIn(value);
    }

    public double getMax()
    {
        return scaler.getRightIn();
    }

    public void setMax(double value)
    {
        scaler.setRightIn(value);
    }

    public void setMinMax(double min, double max)
    {
        scaler.setLeftRightIn(min, max);
    }

    public double getMinBound()
    {
        return scaler.getAbsLeftIn();
    }

    public double getMaxBound()
    {
        return scaler.getAbsRightIn();
    }

    public void setMinBound(double value)
    {
        scaler.setAbsLeftIn(value);
    }

    public void setMaxBound(double value)
    {
        scaler.setAbsRightIn(value);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @return the component
     */
    public int getComponent()
    {
        return component;
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(LUTBandListener listener)
    {
        listeners.add(LUTBandListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(LUTBandListener listener)
    {
        listeners.remove(LUTBandListener.class, listener);
    }

    /**
     * fire event
     */
    public void fireEvent(LUTBandEvent e)
    {
        for (LUTBandListener listener : listeners.getListeners(LUTBandListener.class))
            listener.lutBandChanged(e);
    }

    @Override
    public void scalerChanged(ScalerEvent e)
    {
        if (e.getType() == ScalerEventType.CHANGED)
            // notify LUTBand changed
            fireEvent(new LUTBandEvent(this, LUTBandEventType.SCALER_CHANGED));
    }

    @Override
    public void colorSpaceChanged(IcyColorSpaceEvent e)
    {
        if ((e.getType() == IcyColorSpaceEventType.CHANGED) && (e.getComponent() == component))
            // notify LUTBand changed
            fireEvent(new LUTBandEvent(this, LUTBandEventType.COLORMAP_CHANGED));
    }

}
