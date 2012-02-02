/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.image.lut;

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.image.colormodel.IcyColorModel;
import icy.image.colorspace.IcyColorSpace;
import icy.math.Scaler;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

public class LUT implements LUTBandListener, ChangeListener
{
    private ArrayList<LUTBand> lutBands = new ArrayList<LUTBand>();

    private final IcyColorSpace colorSpace;
    private final Scaler[] scalers;
    private final int numComponents;

    private boolean enabled = true;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    public LUT(IcyColorModel cm)
    {
        colorSpace = (IcyColorSpace) cm.getColorSpace();
        scalers = cm.getColormapScalers();
        numComponents = colorSpace.getNumComponents();

        if (scalers.length != numComponents)
        {
            throw new IllegalArgumentException("Incorrect size for scalers : " + scalers.length + ".  Expected : "
                    + numComponents);
        }

        for (int component = 0; component < numComponents; component++)
        {
            final LUTBand band = new LUTBand(this, component);

            band.addListener(this);

            lutBands.add(band);
        }

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);
    }

    public IcyColorSpace getColorSpace()
    {
        return colorSpace;
    }

    public Scaler[] getScalers()
    {
        return scalers;
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
     * @return the lutBand
     */
    public ArrayList<LUTBand> getLutBands()
    {
        return new ArrayList<LUTBand>(lutBands);
    }

    /**
     * @return the numComponents
     */
    public int getNumComponents()
    {
        return numComponents;
    }

    /**
     * Return the LUTBand for specified channel index
     */
    public LUTBand getLutBand(int band)
    {
        return lutBands.get(band);
    }

    /**
     * Copy LUT from the specified source lut
     */
    public void copyFrom(LUT lut)
    {
        copyColormaps(lut);
        copyScalers(lut);
    }

    /**
     * Copy the scalers from the specified source lut
     */
    public void copyScalers(LUT lut)
    {
        final Scaler[] srcScalers = lut.getScalers();
        final int len = Math.min(scalers.length, srcScalers.length);

        beginUpdate();
        try
        {
            for (int i = 0; i < len; i++)
            {
                final Scaler src = srcScalers[i];
                final Scaler dst = scalers[i];

                dst.setAbsLeftRightIn(src.getAbsLeftIn(), src.getAbsRightIn());
                dst.setLeftRightIn(src.getLeftIn(), src.getRightIn());
                dst.setLeftRightOut(src.getLeftOut(), src.getRightOut());
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Copy colormaps from the specified source lut
     */
    public void copyColormaps(LUT lut)
    {
        getColorSpace().copyColormaps(lut.getColorSpace());
    }

    /**
     * Return true if LUT is compatible with specified ColorModel.<br>
     * (Same number of channels with same data type)
     */
    public boolean isCompatible(IcyColorModel colorModel)
    {
        if (numComponents == colorModel.getNumComponents())
        {
            for (int comp = 0; comp < numComponents; comp++)
            {
                final Scaler[] cmScalers = colorModel.getColormapScalers();

                // not compatible data type ?
                if (scalers[comp].isIntegerData() != cmScalers[comp].isIntegerData())
                    return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(LUTListener listener)
    {
        listeners.add(LUTListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(LUTListener listener)
    {
        listeners.remove(LUTListener.class, listener);
    }

    public void fireLUTChanged(LUTEvent e)
    {
        for (LUTListener lutListener : listeners.getListeners(LUTListener.class))
            lutListener.lutChanged(e);
    }

    @Override
    public void onChanged(EventHierarchicalChecker compare)
    {
        final LUTEvent event = (LUTEvent) compare;

        // notify listener we have changed
        fireLUTChanged(event);
    }

    @Override
    public void lutBandChanged(LUTBandEvent e)
    {
        // notify LUT changed via updater object
        updater.changed(new LUTEvent(this, e.getLutband().getComponent()));
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

}
