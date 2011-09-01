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
package icy.canvas;

import icy.main.Icy;
import icy.painter.Painter;
import icy.roi.ROI;
import icy.util.StringUtil;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * @author Stephane
 */
public class Layer
{
    public interface LayerListener extends EventListener
    {
        public void layerChanged(Layer layer);
    }

    private final static String DEFAULT_NAME = "layer";

    private final Painter painter;

    private String name;
    private boolean visible;
    private float alpha;

    /**
     * listeners
     */
    protected final EventListenerList listeners;

    /**
     * @param painter
     */
    public Layer(Painter painter, String name)
    {
        super();

        this.painter = painter;
        if (name == null)
            this.name = DEFAULT_NAME;
        else
            this.name = name;

        visible = true;
        alpha = 1f;

        listeners = new EventListenerList();
    }

    /**
     * @param painter
     */
    public Layer(Painter painter)
    {
        this(painter, null);
    }

    /**
     * @return the painter
     */
    public Painter getPainter()
    {
        return painter;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        final ROI layerRoi = getAttachedROI();

        if (layerRoi != null)
            return layerRoi.getName();

        return name;
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        if (!StringUtil.equals(this.name, name))
        {
            this.name = name;
            changed();
        }
    }

    /**
     * @return the attachedToRoi
     */
    public boolean isAttachedToRoi()
    {
        return (getAttachedROI() != null);
    }

    /**
     * @return the attachedROI
     */
    public ROI getAttachedROI()
    {
        return Icy.getMainInterface().getROI(painter);
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
            changed();
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
            changed();
        }
    }

    /**
     * called on change
     */
    void changed()
    {
        // notify listener
        fireChangedEvent();
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(LayerListener listener)
    {
        listeners.add(LayerListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(LayerListener listener)
    {
        listeners.remove(LayerListener.class, listener);
    }

    /**
     * fire event
     */
    private void fireChangedEvent()
    {
        for (LayerListener listener : listeners.getListeners(LayerListener.class))
            listener.layerChanged(this);
    }
}
