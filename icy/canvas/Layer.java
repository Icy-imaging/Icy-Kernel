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
import icy.painter.Painter;
import icy.painter.WeakOverlayListener;
import icy.roi.ROI;
import icy.util.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer class.<br>
 * This class encapsulate {@link Painter} and {@link Overlay} in a canvas to<br>
 * add specific display properties (visibility, transparency...).
 */
@SuppressWarnings({"deprecation", "javadoc"})
public class Layer implements OverlayListener, Comparable<Layer>
{
    public interface LayerListener
    {
        public void layerChanged(Layer source, String propertyName);
    }

    public final static String PROPERTY_NAME = Overlay.PROPERTY_NAME;
    public final static String PROPERTY_PRIORITY = Overlay.PROPERTY_PRIORITY;
    public final static String PROPERTY_READONLY = Overlay.PROPERTY_READONLY;
    public final static String PROPERTY_FIXED = Overlay.PROPERTY_FIXED;
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

    private final Painter painter;
    // cache for ROI
    private WeakReference<ROI> roi;

    private String name;
    private OverlayPriority priority;
    private boolean readOnly;
    private boolean fixed;
    private boolean visible;
    private float alpha;

    /**
     * listeners
     */
    protected final List<LayerListener> listeners;

    public Layer(Painter painter, String name)
    {
        super();

        this.painter = painter;

        if (painter instanceof Overlay)
            ((Overlay) painter).addOverlayListener(new WeakOverlayListener(this));
        else
        {
            if (name == null)
                this.name = DEFAULT_NAME;
            else
                this.name = name;
            // default priority
            priority = OverlayPriority.SHAPE_NORMAL;
            readOnly = false;
            fixed = false;
        }

        visible = true;
        alpha = 1f;
        roi = null;

        listeners = new ArrayList<LayerListener>();
    }

    public Layer(Painter painter)
    {
        this(painter, null);
    }

    public Layer(Overlay overlay)
    {
        this(overlay, null);
    }

    /**
     * @return the painter
     */
    public Painter getPainter()
    {
        return painter;
    }

    /**
     * Returns layer priority (use the {@link Overlay} priority if present)
     */
    public OverlayPriority getPriority()
    {
        if (painter instanceof Overlay)
            return ((Overlay) painter).getPriority();

        return priority;
    }

    /**
     * Set the layer priority (modify {@link Overlay} priority if present)
     */
    public void setPriority(OverlayPriority priority)
    {
        if (painter instanceof Overlay)
            ((Overlay) painter).setPriority(priority);
        else if (this.priority != priority)
        {
            this.priority = priority;
            changed(PROPERTY_PRIORITY);
        }
    }

    /**
     * Returns layer name (use the {@link Overlay} name if present)
     */
    public String getName()
    {
        if (painter instanceof Overlay)
            return ((Overlay) painter).getName();

        return name;
    }

    /**
     * Set the layer name (modify {@link Overlay} name if present)
     */
    public void setName(String name)
    {
        if (painter instanceof Overlay)
            ((Overlay) painter).setName(name);
        else if (!StringUtil.equals(this.name, name))
        {
            this.name = name;
            changed(PROPERTY_NAME);
        }
    }

    /**
     * @return the read only
     */
    public boolean isReadOnly()
    {
        if (painter instanceof Overlay)
            return ((Overlay) painter).isReadOnly();

        return readOnly;
    }

    /**
     * Set read only property.
     */
    public void setReadOnly(boolean readOnly)
    {
        if (painter instanceof Overlay)
            ((Overlay) painter).setReadOnly(readOnly);
        else if (this.readOnly != readOnly)
        {
            this.readOnly = readOnly;
            changed(PROPERTY_READONLY);
        }
    }

    /**
     * @return the fixed
     */
    public boolean isFixed()
    {
        if (painter instanceof Overlay)
            return ((Overlay) painter).isFixed();

        return fixed;
    }

    /**
     * Set fixed property.<br>
     * Any fixed Layer cannot be removed from the Canvas.
     */
    public void setFixed(boolean fixed)
    {
        if (painter instanceof Overlay)
            ((Overlay) painter).setFixed(fixed);
        else if (this.fixed != fixed)
        {
            this.fixed = fixed;
            changed(PROPERTY_FIXED);
        }
    }

    /**
     * @return the attachedROI
     */
    public ROI getAttachedROI()
    {
        if (roi == null)
            // search for attached ROI
            roi = new WeakReference<ROI>(Icy.getMainInterface().getROI(painter));

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
        // highest priority first
        return layer.getPriority().ordinal() - getPriority().ordinal();
    }

}
