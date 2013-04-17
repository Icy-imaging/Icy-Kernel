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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.common.EventHierarchicalChecker;
import icy.painter.OverlayEvent.OverlayEventType;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/**
 * Overlay class.<br>
 * <br>
 * This class allow interaction and rich informations display on Sequences.<br>
 * {@link IcyCanvas} subclasses should propagate mouse and key events to overlay.
 * 
 * @author Stephane
 */
@SuppressWarnings("deprecation")
public abstract class Overlay extends AbstractPainter implements Comparable<Overlay>
{
    /**
     * Define the overlay priority:
     * 
     * <pre>
     * Lowest   |   BACKGROUND  (below image)
     *          |   IMAGE       (image level)
     *          |   SHAPE       (just over the image)
     *          |   TEXT        (over image and shape)
     *          |   TOOLTIP     (all over the rest)
     * Highest  |   TOPMOST     (absolute topmost)
     * </pre>
     * 
     * You have 4 levels for each category (except TOPMOST) for finest adjustment:
     * 
     * <pre>
     * Lowest   |   LOW
     *          |   NORMAL
     *          |   HIGH
     * Highest  |   TOP
     * </pre>
     * 
     * TOP level should be used to give <i>focus<i> to a specific Overlay over all other in the same
     * category.
     */
    public static enum OverlayPriority
    {
        BACKGROUND_LOW, BACKGROUND_NORMAL, BACKGROUND_HIGH, BACKGROUND_TOP, IMAGE_LOW, IMAGE_NORMAL, IMAGE_HIGH, IMAGE_TOP, SHAPE_LOW, SHAPE_NORMAL, SHAPE_HIGH, SHAPE_TOP, TEXT_LOW, TEXT_NORMAL, TEXT_HIGH, TEXT_TOP, TOOLTIP_LOW, TOOLTIP_NORMAL, TOOLTIP_HIGH, TOOLTIP_TOP, TOPMOST
    }

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PRIORITY = "priority";
    public static final String PROPERTY_READONLY = "readOnly";
    public static final String PROPERTY_FIXED = "fixed";

    protected static int id_gen = 1;

    /**
     * properties
     */
    protected final int id;
    protected String name;
    protected OverlayPriority priority;
    protected boolean readOnly;
    protected boolean fixed;

    public Overlay(String name, OverlayPriority priority)
    {
        super();

        synchronized (Overlay.class)
        {
            id = id_gen++;
        }

        this.name = name;
        this.priority = priority;
        this.readOnly = false;
        this.fixed = false;
    }

    public Overlay(String name)
    {
        // create overlay with default priority
        this(name, OverlayPriority.SHAPE_NORMAL);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        if (this.name != name)
        {
            this.name = name;
            propertyChanged(PROPERTY_NAME);
        }
    }

    /**
     * @return the priority
     */
    public OverlayPriority getPriority()
    {
        return priority;
    }

    /**
     * @param priority
     *        the priority to set
     */
    public void setPriority(OverlayPriority priority)
    {
        if (this.priority != priority)
        {
            this.priority = priority;
            propertyChanged(PROPERTY_PRIORITY);
        }
    }

    /**
     * Return fixed property.
     */
    public boolean isFixed()
    {
        return fixed;
    }

    /**
     * Set fixed property.<br>
     * Any fixed Overlay cannot be removed from the Canvas where it appears.
     */
    public void setFixed(boolean fixed)
    {
        if (this.fixed != fixed)
        {
            this.fixed = fixed;
            propertyChanged(PROPERTY_FIXED);
        }
    }

    /**
     * Return read only property.
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * Set read only property.<br>
     */
    public void setReadOnly(boolean readOnly)
    {
        if (this.readOnly != readOnly)
        {
            this.readOnly = readOnly;
            propertyChanged(PROPERTY_READONLY);
        }
    }

    /**
     * @deprecated Use {@link #painterChanged()} instead.
     */
    @Deprecated
    @Override
    public void changed()
    {
        painterChanged();
    }

    /**
     * Notify the painter content has changed.<br>
     * All sequence containing the overlay will be repainted to reflect the change.
     */
    public void painterChanged()
    {
        updater.changed(new OverlayEvent(this, OverlayEventType.PAINTER_CHANGED));
    }

    /**
     * Notify the overlay property has changed.
     */
    public void propertyChanged(String propertyName)
    {
        updater.changed(new OverlayEvent(this, OverlayEventType.PROPERTY_CHANGED, propertyName));
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        fireOverlayChangedEvent((OverlayEvent) object);
    }

    protected void fireOverlayChangedEvent(OverlayEvent event)
    {
        for (OverlayListener listener : listeners.getListeners(OverlayListener.class))
            listener.overlayChanged(event);
    }

    /**
     * Add a listener.
     */
    public void addOverlayListener(OverlayListener listener)
    {
        listeners.add(OverlayListener.class, listener);
    }

    /**
     * Remove a listener.
     */
    public void removeOverlayListener(OverlayListener listener)
    {
        listeners.remove(OverlayListener.class, listener);
    }

    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * Mouse enter event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseEntered(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * Mouse exit event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseExited(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * Mouse wheel moved event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseWheelMoved(MouseWheelEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    @Override
    public int compareTo(Overlay o)
    {
        // highest priority first
        return o.priority.ordinal() - priority.ordinal();
    }
}
