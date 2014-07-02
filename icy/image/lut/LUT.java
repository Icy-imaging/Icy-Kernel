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

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.file.xml.XMLPersistent;
import icy.image.colormap.IcyColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.image.colorspace.IcyColorSpace;
import icy.image.colorspace.IcyColorSpaceEvent;
import icy.image.colorspace.IcyColorSpaceListener;
import icy.image.lut.LUT.LUTChannelEvent.LUTChannelEventType;
import icy.image.lut.LUTEvent.LUTEventType;
import icy.math.Scaler;
import icy.math.ScalerEvent;
import icy.math.ScalerListener;
import icy.type.DataType;
import icy.util.XMLUtil;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

public class LUT implements IcyColorSpaceListener, ScalerListener, ChangeListener, XMLPersistent
{
    private final static String ID_NUM_CHANNEL = "numChannel";
    private final static String ID_SCALER = "scaler";
    private final static String ID_COLORMAP = "colormap";

    public static interface LUTChannelListener extends EventListener
    {
        public void lutChannelChanged(LUTChannelEvent e);
    }

    public static class LUTChannelEvent
    {
        public enum LUTChannelEventType
        {
            SCALER_CHANGED, COLORMAP_CHANGED
        }

        private final LUTChannel lutChannel;
        private final LUTChannelEventType type;

        public LUTChannelEvent(LUTChannel lutChannel, LUTChannelEventType type)
        {
            super();

            this.lutChannel = lutChannel;
            this.type = type;
        }

        /**
         * @return the lutChannel
         */
        public LUTChannel getLutChannel()
        {
            return lutChannel;
        }

        /**
         * @return the type
         */
        public LUTChannelEventType getType()
        {
            return type;
        }
    }

    public class LUTChannel
    {
        /**
         * band index
         */
        private final int channel;

        /**
         * listeners
         */
        private final EventListenerList listeners;

        public LUTChannel(int channel)
        {
            this.channel = channel;

            listeners = new EventListenerList();
        }

        public LUT getLut()
        {
            return LUT.this;
        }

        public Scaler getScaler()
        {
            return scalers[channel];
        }

        public IcyColorMap getColorMap()
        {
            return colorSpace.getColorMap(channel);
        }

        /**
         * Set the specified colormap (do a copy).
         * 
         * @param colorMap
         *        source colorspace to copy
         * @param setAlpha
         *        also set the alpha information
         */
        public void setColorMap(IcyColorMap colorMap, boolean setAlpha)
        {
            colorSpace.setColorMap(channel, colorMap, setAlpha);
        }

        /**
         * @deprecated Use {@link #setColorMap(IcyColorMap, boolean)} instead.
         */
        @Deprecated
        public void setColorMap(IcyColorMap colorMap)
        {
            setColorMap(colorMap, true);
        }

        /**
         * @deprecated Use {@link #setColorMap(IcyColorMap, boolean)} instead.
         */
        @Deprecated
        public void copyColorMap(IcyColorMap colorMap)
        {
            setColorMap(colorMap, true);
        }

        public double getMin()
        {
            return getScaler().getLeftIn();
        }

        public void setMin(double value)
        {
            getScaler().setLeftIn(value);
        }

        public double getMax()
        {
            return getScaler().getRightIn();
        }

        public void setMax(double value)
        {
            getScaler().setRightIn(value);
        }

        public void setMinMax(double min, double max)
        {
            getScaler().setLeftRightIn(min, max);
        }

        public double getMinBound()
        {
            return getScaler().getAbsLeftIn();
        }

        public double getMaxBound()
        {
            return getScaler().getAbsRightIn();
        }

        public void setMinBound(double value)
        {
            getScaler().setAbsLeftIn(value);
        }

        public void setMaxBound(double value)
        {
            getScaler().setAbsRightIn(value);
        }

        public boolean isEnabled()
        {
            return getColorMap().isEnabled();
        }

        public void setEnabled(boolean value)
        {
            getColorMap().setEnabled(value);
        }

        /**
         * @deprecated Use {@link #getChannel()} instead.
         */
        @Deprecated
        public int getComponent()
        {
            return getChannel();
        }

        /**
         * @return the component
         */
        public int getChannel()
        {
            return channel;
        }

        /**
         * Add a listener.
         */
        public void addListener(LUTChannelListener listener)
        {
            listeners.add(LUTChannelListener.class, listener);
        }

        /**
         * Remove a listener.
         */
        public void removeListener(LUTChannelListener listener)
        {
            listeners.remove(LUTChannelListener.class, listener);
        }

        /**
         * Fire change event.
         */
        public void fireEvent(LUTChannelEvent e)
        {
            for (LUTChannelListener listener : listeners.getListeners(LUTChannelListener.class))
                listener.lutChannelChanged(e);
        }
    }

    private List<LUTChannel> lutChannels = new ArrayList<LUTChannel>();

    final IcyColorSpace colorSpace;
    final Scaler[] scalers;
    final int numChannel;

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
        colorSpace = cm.getIcyColorSpace();
        scalers = cm.getColormapScalers();
        numChannel = colorSpace.getNumComponents();

        if (scalers.length != numChannel)
        {
            throw new IllegalArgumentException("Incorrect size for scalers : " + scalers.length + ".  Expected : "
                    + numChannel);
        }

        final DataType dataType = cm.getDataType_();

        for (int channel = 0; channel < numChannel; channel++)
        {
            // BYTE data type --> fix bounds to data type bounds
            if (dataType == DataType.UBYTE)
                scalers[channel].setLeftRightIn(dataType.getMinValue(), dataType.getMaxValue());

            lutChannels.add(new LUTChannel(channel));
        }

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // add listener
        for (Scaler scaler : scalers)
            scaler.addListener(this);
        colorSpace.addListener(this);

    }

    protected int indexOf(Scaler scaler)
    {
        for (int i = 0; i < scalers.length; i++)
            if (scalers[i] == scaler)
                return i;

        return -1;
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

    public ArrayList<LUTChannel> getLutChannels()
    {
        return new ArrayList<LUTChannel>(lutChannels);
    }

    /**
     * Return the {@link LUTChannel} for specified channel index.
     */
    public LUTChannel getLutChannel(int channel)
    {
        return lutChannels.get(channel);
    }

    /**
     * @deprecated Use {@link #getLutChannels()} instead.
     */
    @Deprecated
    public ArrayList<LUTBand> getLutBands()
    {
        final ArrayList<LUTBand> result = new ArrayList<LUTBand>();

        for (LUTChannel lutChannel : lutChannels)
            result.add(new LUTBand(this, lutChannel.getChannel()));

        return result;
    }

    /**
     * @deprecated Use {@link #getLutChannel(int)} instead.
     */
    @Deprecated
    public LUTBand getLutBand(int band)
    {
        return getLutBands().get(band);
    }

    /**
     * @return the number of channel.
     */
    public int getNumChannel()
    {
        return numChannel;
    }

    /**
     * @deprecated Use {@link #getNumChannel()} instead.
     */
    @Deprecated
    public int getNumComponents()
    {
        return getNumChannel();
    }

    /**
     * Copy LUT from the specified source lut
     */
    public void copyFrom(LUT lut)
    {
        setColorMaps(lut, true);
        setScalers(lut);
    }

    /**
     * Set the scalers from the specified source lut (do a copy)
     */
    public void setScalers(LUT lut)
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
     * @deprecated Use {@link #setScalers(LUT)} instead.
     */
    @Deprecated
    public void copyScalers(LUT lut)
    {
        setScalers(lut);
    }

    /**
     * Set colormaps from the specified source lut (do a copy).
     * 
     * @param lut
     *        source lut to use
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMaps(LUT lut, boolean setAlpha)
    {
        getColorSpace().setColorMaps(lut.getColorSpace(), setAlpha);
    }

    /**
     * @deprecated USe {@link #setColorMaps(LUT, boolean)} instead.
     */
    @Deprecated
    public void setColormaps(LUT lut)
    {
        setColorMaps(lut, true);
    }

    /**
     * @deprecated Use {@link #setColorMaps(LUT, boolean)} instead.
     */
    @Deprecated
    public void copyColormaps(LUT lut)
    {
        setColorMaps(lut, true);
    }

    /**
     * Return true if LUT is compatible with specified ColorModel.<br>
     * (Same number of channels with same data type)
     */
    public boolean isCompatible(LUT lut)
    {
        if (numChannel != lut.getNumChannel())
            return false;

        final Scaler[] cmScalers = lut.getScalers();

        // check that data type is compatible
        for (int channel = 0; channel < numChannel; channel++)
            if (scalers[channel].isIntegerData() != cmScalers[channel].isIntegerData())
                return false;

        return true;
    }

    /**
     * Return true if LUT is compatible with specified ColorModel.<br>
     * (Same number of channels with same data type)
     */
    public boolean isCompatible(IcyColorModel colorModel)
    {
        if (numChannel != colorModel.getNumComponents())
            return false;

        final Scaler[] cmScalers = colorModel.getColormapScalers();

        // check that data type is compatible
        for (int comp = 0; comp < numChannel; comp++)
            if (scalers[comp].isIntegerData() != cmScalers[comp].isIntegerData())
                return false;

        return true;
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

        // propagate event to LUTChannel
        final int channel = event.getComponent();
        final LUTChannelEventType type = (event.getType() == LUTEventType.COLORMAP_CHANGED) ? LUTChannelEventType.COLORMAP_CHANGED
                : LUTChannelEventType.SCALER_CHANGED;

        if (channel == -1)
        {
            for (LUTChannel lutChannel : lutChannels)
                lutChannel.fireEvent(new LUTChannelEvent(lutChannel, type));
        }
        else
        {
            final LUTChannel lutChannel = getLutChannel(channel);
            lutChannel.fireEvent(new LUTChannelEvent(lutChannel, type));
        }
    }

    @Override
    public void colorSpaceChanged(IcyColorSpaceEvent e)
    {
        // notify LUT colormap changed
        updater.changed(new LUTEvent(this, e.getComponent(), LUTEventType.COLORMAP_CHANGED));
    }

    @Override
    public void scalerChanged(ScalerEvent e)
    {
        // notify LUTBand changed
        updater.changed(new LUTEvent(this, indexOf(e.getScaler()), LUTEventType.SCALER_CHANGED));
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

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        // different channel number --> exit
        if (numChannel != XMLUtil.getElementIntValue(node, ID_NUM_CHANNEL, 1))
            return false;

        beginUpdate();
        try
        {
            for (int ch = 0; ch < numChannel; ch++)
            {
                Node n;

                n = XMLUtil.getElement(node, ID_SCALER + ch);
                if (n != null)
                    scalers[ch].loadFromXML(n);
                n = XMLUtil.getElement(node, ID_COLORMAP + ch);
                if (n != null)
                    colorSpace.getColorMap(ch).loadFromXML(n);
            }
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementIntValue(node, ID_NUM_CHANNEL, numChannel);

        for (int ch = 0; ch < numChannel; ch++)
        {
            Node n;

            n = XMLUtil.setElement(node, ID_SCALER + ch);
            if (n != null)
                scalers[ch].saveToXML(n);
            n = XMLUtil.setElement(node, ID_COLORMAP + ch);
            if (n != null)
                colorSpace.getColorMap(ch).saveToXML(n);
        }

        return true;
    }
}
