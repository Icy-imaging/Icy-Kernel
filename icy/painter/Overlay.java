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
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.main.Icy;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.sequence.Sequence;
import icy.type.point.Point5D;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.event.EventListenerList;

/**
 * Overlay class.<br>
 * <br>
 * This class allow interaction and rich informations display on Sequences.<br>
 * {@link IcyCanvas} subclasses should propagate mouse and key events to overlay.
 * 
 * @author Stephane
 */
@SuppressWarnings("deprecation")
public abstract class Overlay implements Painter, ChangeListener, Comparable<Overlay>
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
    public static final String PROPERTY_CANBEREMOVED = "canBeRemoved";
    public static final String PROPERTY_RECEIVEKEYEVENTONHIDDEN = "receiveKeyEventOnHidden";
    public static final String PROPERTY_RECEIVEMOUSEEVENTONHIDDEN = "receiveMouseEventOnHidden";

    protected static int id_gen = 1;

    /**
     * properties
     */
    protected final int id;
    protected String name;
    protected OverlayPriority priority;
    protected boolean readOnly;
    protected boolean canBeRemoved;
    protected boolean receiveKeyEventOnHidden;
    protected boolean receiveMouseEventOnHidden;

    /**
     * internals
     */
    protected final EventListenerList listeners;
    protected final UpdateEventHandler updater;

    public Overlay(String name, OverlayPriority priority)
    {
        super();

        synchronized (Overlay.class)
        {
            id = id_gen++;
        }

        this.name = name;
        this.priority = priority;
        readOnly = false;
        canBeRemoved = true;
        receiveKeyEventOnHidden = false;
        receiveMouseEventOnHidden = false;

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);
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
     * Returns <code>true</code> if the overlay is attached to the specified {@link Sequence}.
     */
    public boolean isAttached(Sequence sequence)
    {
        if (sequence != null)
            return sequence.contains(this);

        return false;
    }

    /**
     * @deprecated Use {@link #getCanBeRemoved()} instead.
     * @see #setCanBeRemoved(boolean)
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
        setCanBeRemoved(!value);
    }

    /**
     * Returns <code>true</code> if the overlay can be freely removed from the Canvas where it
     * appears and <code>false</code> otherwise.<br/>
     * 
     * @see #setCanBeRemoved(boolean)
     */
    public boolean getCanBeRemoved()
    {
        return canBeRemoved;
    }

    /**
     * Set the <code>canBeRemoved</code> property.<br/>
     * Set it to false if you want to prevent the overlay to be removed from the Canvas where it
     * appears.
     */
    public void setCanBeRemoved(boolean value)
    {
        if (canBeRemoved != value)
        {
            canBeRemoved = value;
            propertyChanged(PROPERTY_CANBEREMOVED);
        }
    }

    /**
     * Return read only property.<br/>
     * When set to <code>true</code> we cannot anymore modify overlay properties from the GUI.
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * Set read only property.<br>
     */
    public void setReadOnly(boolean value)
    {
        if (readOnly != value)
        {
            readOnly = value;
            propertyChanged(PROPERTY_READONLY);
        }
    }

    /**
     * @return <code>true</code> is the overlay should receive {@link KeyEvent} even when it is not
     *         visible.
     */
    public boolean getReceiveKeyEventOnHidden()
    {
        return receiveKeyEventOnHidden;
    }

    /**
     * Set to <code>true</code> if you want to overlay to receive {@link KeyEvent} even when it is
     * not visible.
     */
    public void setReceiveKeyEventOnHidden(boolean value)
    {
        if (receiveKeyEventOnHidden != value)
        {
            receiveKeyEventOnHidden = value;
            propertyChanged(PROPERTY_RECEIVEKEYEVENTONHIDDEN);
        }
    }

    /**
     * @return <code>true</code> is the overlay should receive {@link MouseEvent} even when it is
     *         not visible.
     */
    public boolean getReceiveMouseEventOnHidden()
    {
        return receiveMouseEventOnHidden;
    }

    /**
     * Set to <code>true</code> if you want to overlay to receive {@link KeyEvent} even when it is
     * not visible.
     */
    public void setReceiveMouseEventOnHidden(boolean value)
    {
        if (receiveMouseEventOnHidden != value)
        {
            receiveMouseEventOnHidden = value;
            propertyChanged(PROPERTY_RECEIVEMOUSEEVENTONHIDDEN);
        }
    }

    /**
     * @deprecated Use {@link Sequence#addOverlay(Overlay)} instead.
     */
    @Deprecated
    public void attachTo(Sequence sequence)
    {
        if (sequence != null)
            sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Sequence#removeOverlay(Overlay)} instead.
     */
    @Deprecated
    public void detachFrom(Sequence sequence)
    {
        if (sequence != null)
            sequence.removeOverlay(this);
    }

    /**
     * Remove the Overlay from all sequences where it is currently attached.
     */
    public void remove()
    {
        for (Sequence sequence : getSequences())
            sequence.removeOverlay(this);
    }

    /**
     * Returns all sequences where the overlay is currently attached.
     */
    public List<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequencesContaining(this);
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

    /**
     * @deprecated Use {@link #painterChanged()} instead.
     */
    @Deprecated
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

    /**
     * Paint method called to draw the overlay.
     */
    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        // nothing by default
    }

    /**
     * @deprecated Use {@link #mousePressed(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseReleased(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseClick(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseMove(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseDrag(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseEntered(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    public void mouseEntered(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseExited(MouseEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    public void mouseExited(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #mouseWheelMoved(MouseWheelEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    public void mouseWheelMoved(MouseWheelEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #keyPressed(KeyEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * @deprecated Use {@link #keyReleased(KeyEvent, Point5D.Double, IcyCanvas)} instead
     */
    @Deprecated
    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // no action by default
    }

    /**
     * Mouse press event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mousePressed(e, imagePoint.toPoint2D(), canvas);
        else
            mousePressed(e, (Point2D) null, canvas);
    }

    /**
     * Mouse release event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseReleased(e, imagePoint.toPoint2D(), canvas);
        else
            mouseReleased(e, (Point2D) null, canvas);
    }

    /**
     * Mouse click event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseClick(e, imagePoint.toPoint2D(), canvas);
        else
            mouseClick(e, (Point2D) null, canvas);
    }

    /**
     * Mouse move event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseMove(e, imagePoint.toPoint2D(), canvas);
        else
            mouseMove(e, (Point2D) null, canvas);
    }

    /**
     * Mouse drag event forwarded to the overlay.
     * 
     * @param e
     *        mouse event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseDrag(e, imagePoint.toPoint2D(), canvas);
        else
            mouseDrag(e, (Point2D) null, canvas);
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
    public void mouseEntered(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseEntered(e, imagePoint.toPoint2D(), canvas);
        else
            mouseEntered(e, (Point2D) null, canvas);
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
    public void mouseExited(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseExited(e, imagePoint.toPoint2D(), canvas);
        else
            mouseExited(e, (Point2D) null, canvas);
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
    public void mouseWheelMoved(MouseWheelEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            mouseWheelMoved(e, imagePoint.toPoint2D(), canvas);
        else
            mouseWheelMoved(e, (Point2D) null, canvas);
    }

    /**
     * Key press event forwarded to the overlay.
     * 
     * @param e
     *        key event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            keyPressed(e, imagePoint.toPoint2D(), canvas);
        else
            keyPressed(e, (Point2D) null, canvas);
    }

    /**
     * Key release event forwarded to the overlay.
     * 
     * @param e
     *        key event
     * @param imagePoint
     *        mouse position (image coordinates)
     * @param canvas
     *        icy canvas
     */
    public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // provide backward compatibility
        if (imagePoint != null)
            keyReleased(e, imagePoint.toPoint2D(), canvas);
        else
            keyReleased(e, (Point2D) null, canvas);
    }

    @Override
    public int compareTo(Overlay o)
    {
        // highest priority first
        return o.priority.ordinal() - priority.ordinal();
    }
}
