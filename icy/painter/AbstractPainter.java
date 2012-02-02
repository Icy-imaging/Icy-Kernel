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
 * @author Stephane
 */
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
     */
    public AbstractPainter(Sequence sequence)
    {
        super();

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        attachTo(sequence);
    }

    public AbstractPainter()
    {
        this(null);
    }

    public void attachTo(Sequence sequence)
    {
        if (sequence != null)
            sequence.addPainter(this);
    }

    public void detachFrom(Sequence sequence)
    {
        if (sequence != null)
            sequence.removePainter(this);
    }

    public void detachFromAll()
    {
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removePainter(this);
    }

    public boolean isAttached(Sequence sequence)
    {
        if (sequence != null)
            return sequence.contains(this);

        return false;
    }

    public void delete()
    {
        detachFromAll();
    }

    public void changed()
    {
        updater.changed(new PainterEvent(this, PainterEventType.PAINTER_CHANGED));
    }

    /**
     * @return sequences where painter is attached
     */
    public ArrayList<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequencesContaining(this);
    }

    protected void fireChangedEvent(PainterEvent event)
    {
        for (PainterListener listener : listeners.getListeners(PainterListener.class))
            listener.painterChanged(event);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addPainterListener(PainterListener listener)
    {
        listeners.add(PainterListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
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
