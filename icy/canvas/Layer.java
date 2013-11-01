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

import icy.main.Icy;
import icy.painter.Overlay;
import icy.painter.Overlay.OverlayPriority;
import icy.painter.OverlayEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.OverlayListener;
import icy.painter.OverlayWrapper;
import icy.painter.Painter;
import icy.painter.WeakOverlayListener;
import icy.roi.ROI;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer class.<br>
 * This class encapsulate {@link Overlay} in a canvas to<br>
 * add specific display properties (visibility, transparency...).
 */
public class Layer implements OverlayListener, Comparable<Layer>
{
    public interface LayerListener
    {
        public void layerChanged(Layer source, String propertyName);
    }

    public final static String PROPERTY_NAME = Overlay.PROPERTY_NAME;
    public final static String PROPERTY_PRIORITY = Overlay.PROPERTY_PRIORITY;
    public final static String PROPERTY_READONLY = Overlay.PROPERTY_READONLY;
    public final static String PROPERTY_CANBEREMOVED = Overlay.PROPERTY_CANBEREMOVED;
    public final static String PROPERTY_RECEIVEKEYEVENTONHIDDEN = Overlay.PROPERTY_RECEIVEKEYEVENTONHIDDEN;
    public final static String PROPERTY_RECEIVEMOUSEEVENTONHIDDEN = Overlay.PROPERTY_RECEIVEMOUSEEVENTONHIDDEN;
    public final static String PROPERTY_ALPHA = "alpha";
    public final static String PROPERTY_VISIBLE = "visible";

    public final static String DEFAULT_NAME = "layer";

    /**
     * Returns true if the Layer need to be repainted when the specified property has changed.
     */
    public static boolean isPaintProperty(String propertyName)
    {
        if (propertyName == null)
            return false;

        return propertyName.equals(PROPERTY_ALPHA) || propertyName.equals(PROPERTY_PRIORITY)
                || propertyName.equals(PROPERTY_VISIBLE);
    }

    static Overlay createOverlayWrapper(@SuppressWarnings("deprecation") Painter painter, String name)
    {
        if (painter instanceof Overlay)
            return (Overlay) painter;

        final Overlay result = new OverlayWrapper(painter, name);

        if (name == null)
            result.setName(DEFAULT_NAME);

        // default priority
        result.setPriority(OverlayPriority.SHAPE_NORMAL);

        return result;
    }

    private final Overlay overlay;
    // cache for ROI
    private WeakReference<ROI> roi;

    private boolean visible;
    private float alpha;

    /**
     * listeners
     */
    protected final List<LayerListener> listeners;

    public Layer(Overlay overlay)
    {
        this.overlay = overlay;

        overlay.addOverlayListener(new WeakOverlayListener(this));

        visible = true;
        alpha = 1f;
        roi = null;

        listeners = new ArrayList<LayerListener>();
    }

    /**
     * @deprecated Use {@link #Layer(Overlay)} instead.
     */
    @Deprecated
    public Layer(Painter painter, String name)
    {
        this(createOverlayWrapper(painter, name));
    }

    /**
     * @deprecated Use {@link #Layer(Overlay)} instead.
     */
    @Deprecated
    public Layer(Painter painter)
    {
        this(painter, null);
    }

    /**
     * Returns the attached {@link Overlay}.
     */
    public Overlay getOverlay()
    {
        return overlay;
    }

    /**
     * @deprecated Use {@link #getOverlay()} instead.
     */
    @Deprecated
    public Painter getPainter()
    {
        final Overlay result = getOverlay();

        if (result instanceof OverlayWrapper)
            return ((OverlayWrapper) result).getPainter();

        return result;
    }

    /**
     * Returns layer priority (internally use the overlay priority).
     * 
     * @see Overlay#getPriority()
     */
    public OverlayPriority getPriority()
    {
        return overlay.getPriority();
    }

    /**
     * Set the layer priority (internally set the overlay priority).
     * 
     * @see Overlay#setPriority(OverlayPriority)
     */
    public void setPriority(OverlayPriority priority)
    {
        overlay.setPriority(priority);
    }

    /**
     * Returns layer name (internally use the overlay name).
     * 
     * @see Overlay#getName()
     */
    public String getName()
    {
        return overlay.getName();
    }

    /**
     * Set the layer name (internally set the overlay name)
     * 
     * @see Overlay#setName(String)
     */
    public void setName(String name)
    {
        overlay.setName(name);
    }

    /**
     * Returns the read only property name (internally use the overlay read only property).
     * 
     * @see Overlay#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return overlay.isReadOnly();
    }

    /**
     * Set read only property (internally set the overlay read only property).
     * 
     * @see Overlay#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly)
    {
        overlay.setReadOnly(readOnly);
    }

    /**
     * Returns fixed property.
     * 
     * @deprecated Use {@link #getCanBeRemoved()} instead.
     */
    @Deprecated
    public boolean isFixed()
    {
        return !getCanBeRemoved();
    }

    /**
     * @deprecated Use {@link #setCanBeRemoved(boolean)} instead.
     */
    @Deprecated
    public void setFixed(boolean value)
    {
        setCanBeRemoved(value);
    }

    /**
     * Returns <code>true</code> if the layer can be freely removed from the Canvas where it
     * appears and <code>false</code> otherwise.<br/>
     * 
     * @see Overlay#getCanBeRemoved()
     */
    public boolean getCanBeRemoved()
    {
        return overlay.getCanBeRemoved();
    }

    /**
     * Set the <code>canBeRemoved</code> property.<br/>
     * Set it to false if you want to prevent the layer to be removed from the Canvas where it
     * appears.
     * 
     * @see Overlay#setCanBeRemoved(boolean)
     */
    public void setCanBeRemoved(boolean value)
    {
        overlay.setCanBeRemoved(value);
    }

    /**
     * @see Overlay#getReceiveKeyEventOnHidden()
     */
    public boolean getReceiveKeyEventOnHidden()
    {
        return overlay.getReceiveKeyEventOnHidden();
    }

    /**
     * @see Overlay#setReceiveKeyEventOnHidden(boolean)
     */
    public void setReceiveKeyEventOnHidden(boolean value)
    {
        overlay.setReceiveKeyEventOnHidden(value);
    }

    /**
     * @see Overlay#getReceiveMouseEventOnHidden()
     */
    public boolean getReceiveMouseEventOnHidden()
    {
        return overlay.getReceiveMouseEventOnHidden();
    }

    /**
     * @see Overlay#setReceiveMouseEventOnHidden(boolean)
     */
    public void setReceiveMouseEventOnHidden(boolean value)
    {
        overlay.setReceiveMouseEventOnHidden(value);
    }

    /**
     * @return the attachedROI
     */
    public ROI getAttachedROI()
    {
        if (roi == null)
            // search for attached ROI
            roi = new WeakReference<ROI>(Icy.getMainInterface().getROI(overlay));

        return roi.get();
    }

    /**
     * @return the visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * @param visible
     *        the visible to set
     */
    public void setVisible(boolean visible)
    {
        if (this.visible != visible)
        {
            this.visible = visible;
            changed(PROPERTY_VISIBLE);
        }
    }

    /**
     * @return the alpha
     */
    public float getAlpha()
    {
        return alpha;
    }

    /**
     * @param value
     *        the alpha to set
     */
    public void setAlpha(float value)
    {
        if (alpha != value)
        {
            alpha = value;
            changed(PROPERTY_ALPHA);
        }
    }

    /**
     * Called on layer property change
     */
    protected void changed(String propertyName)
    {
        // notify listener
        fireChangedEvent(propertyName);
    }

    /**
     * fire event
     */
    private void fireChangedEvent(String propertyName)
    {
        for (LayerListener listener : listeners)
            listener.layerChanged(this, propertyName);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(LayerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(LayerListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void overlayChanged(OverlayEvent event)
    {
        // only interested by property change here
        if (event.getType() == OverlayEventType.PROPERTY_CHANGED)
            changed(event.getPropertyName());
    }

    @Override
    public int compareTo(Layer layer)
    {
        // compare with overlay
        return getOverlay().compareTo(layer.getOverlay());
    }
}
