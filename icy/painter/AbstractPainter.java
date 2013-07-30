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

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.main.Icy;
import icy.painter.PainterEvent.PainterEventType;
import icy.sequence.Sequence;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

/**
 * AbstractPainter class.<br>
 * 
 * @deprecated Uses the {@link Overlay} class instead.
 * @author Stephane
 */
@Deprecated
public abstract class AbstractPainter extends PainterAdapter implements ChangeListener
{
    /**
     * listeners
     */
    protected final EventListenerList listeners;
    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;

    /**
     * Create an AbstractPainter and attach it to the specified sequence.
     * 
     * @deprecated Uses the {@link Overlay} class instead.
     */
    @Deprecated
    public AbstractPainter()
    {
        super();

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);
    }

    /**
     * Create an AbstractPainter and attach it to the specified sequence.
     * 
     * @deprecated Uses the {@link Overlay} class instead.
     */
    @Deprecated
    public AbstractPainter(Sequence sequence)
    {
        this();

        if (sequence != null)
            sequence.addPainter(this);
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
     * @deprecated Use {@link Sequence#addPainter(Painter)} instead.
     */
    @Deprecated
    public void attachTo(Sequence sequence)
    {
        if (sequence != null)
            sequence.addPainter(this);
    }

    /**
     * @deprecated Use {@link Sequence#removePainter(Painter)} instead.
     */
    @Deprecated
    public void detachFrom(Sequence sequence)
    {
        if (sequence != null)
            sequence.removePainter(this);
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void detachFromAll()
    {
        remove();
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void delete()
    {
        remove();
    }

    /**
     * Remove the Painter from all sequences where it is currently attached.
     */
    public void remove()
    {
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removePainter(this);
    }

    public void changed()
    {
        updater.changed(new PainterEvent(this, PainterEventType.PAINTER_CHANGED));
    }

    /**
     * Returns all sequences where the painter/overlay is currently attached.
     */
    public ArrayList<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequencesContaining(this);
    }

    /**
     * @deprecated Use {@link Overlay} class instead.
     */
    @Deprecated
    protected void fireChangedEvent(PainterEvent event)
    {
        for (PainterListener listener : listeners.getListeners(PainterListener.class))
            listener.painterChanged(event);
    }

    /**
     * @deprecated Use {@link Overlay#addOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void addPainterListener(PainterListener listener)
    {
        listeners.add(PainterListener.class, listener);
    }

    /**
     * @deprecated Use {@link Overlay#removeOverlayListener(OverlayListener)} instead.
     */
    @Deprecated
    public void removePainterListener(PainterListener listener)
    {
        listeners.remove(PainterListener.class, listener);
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
    public void onChanged(EventHierarchicalChecker object)
    {
        final PainterEvent event = (PainterEvent) object;
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        // notify listeners
        fireChangedEvent(event);

        // notify sequence as they can't listen painter (interface)
        for (Sequence sequence : sequences)
            sequence.painterChanged(this);
    }
}
