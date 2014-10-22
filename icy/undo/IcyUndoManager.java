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
package icy.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Custom UndoManager for Icy.
 * 
 * @author Stephane
 */
// public class IcyUndoManager extends UndoManager implements IcyUndoableEditListener
public class IcyUndoManager extends AbstractUndoableEdit implements UndoableEditListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 3080107472163005941L;

    private static final int INITIAL_LIMIT = 64;

    /**
     * owner of UndoManager
     */
    protected final Object owner;

    /**
     * The collection of <code>AbstractIcyUndoableEdit</code>s
     * undone/redone "en masse" by this <code>CompoundEdit</code>.
     */
    protected List<AbstractIcyUndoableEdit> edits;

    /**
     * listeners
     */
    protected final EventListenerList listeners;

    /**
     * internals
     */
    protected int indexOfNextAdd;
    protected int limit;

    public IcyUndoManager(Object owner, int limit)
    {
        super();

        edits = new ArrayList<AbstractIcyUndoableEdit>(INITIAL_LIMIT / 2);
        this.owner = owner;
        listeners = new EventListenerList();
        indexOfNextAdd = 0;
        this.limit = limit;
    }

    public IcyUndoManager(Object owner)
    {
        this(owner, INITIAL_LIMIT);
    }

    /**
     * @return the owner
     */
    public Object getOwner()
    {
        return owner;
    }

    /**
     * Returns the maximum number of edits this {@code UndoManager} holds. A value less than 0
     * indicates the number of edits is not limited.
     * 
     * @return the maximum number of edits this {@code UndoManager} holds
     * @see #addEdit
     * @see #setLimit
     */
    public synchronized int getLimit()
    {
        return limit;
    }

    /**
     * Empties the undo manager sending each edit a <code>die</code> message
     * in the process.
     * 
     * @see AbstractUndoableEdit#die
     */
    public void discardAllEdits()
    {
        trimEdits(0, edits.size() - 1);
        fireChangeEvent();
    }

    /**
     * Remove future edits (the ones to redo) from the undo manager sending each edit a
     * <code>die</code> message
     * in the process.
     * 
     * @param keep
     *        number of future edits to keep (1 means we keep only the next edit)
     * @see AbstractUndoableEdit#die
     */
    public synchronized void discardFutureEdits(int keep)
    {
        trimEdits(indexOfNextAdd + keep, edits.size() - 1);
        fireChangeEvent();
    }

    /**
     * Remove future edits (the ones to redo) from the undo manager sending each edit a
     * <code>die</code> message
     * in the process.
     * 
     * @see AbstractUndoableEdit#die
     */
    public synchronized void discardFutureEdits()
    {
        discardFutureEdits(0);
    }

    /**
     * Remove previous edits (the ones to undo) from the undo manager sending each edit a
     * <code>die</code> message
     * in the process.
     * 
     * @param keep
     *        number of previous edits to keep (1 mean we keep only the last edit)
     * @see AbstractUndoableEdit#die
     */
    public synchronized void discardOldEdits(int keep)
    {
        trimEdits(0, indexOfNextAdd - (1 + keep));
        fireChangeEvent();
    }

    /**
     * Empties the undo manager sending each edit a <code>die</code> message
     * in the process.
     * 
     * @see AbstractUndoableEdit#die
     */
    public synchronized void discardOldEdits()
    {
        discardOldEdits(0);
    }

    /**
     * Reduces the number of queued edits to a range of size limit,
     * centered on the index of the next edit.
     */
    protected void trimForLimit()
    {
        if (limit >= 0)
        {
            final int size = edits.size();

            if (size > limit)
            {
                final int halfLimit = limit / 2;
                int keepFrom = indexOfNextAdd - 1 - halfLimit;
                int keepTo = indexOfNextAdd - 1 + halfLimit;

                // These are ints we're playing with, so dividing by two
                // rounds down for odd numbers, so make sure the limit was
                // honored properly. Note that the keep range is
                // inclusive.
                if (keepTo - keepFrom + 1 > limit)
                    keepFrom++;

                // The keep range is centered on indexOfNextAdd,
                // but odds are good that the actual edits Vector
                // isn't. Move the keep range to keep it legal.
                if (keepFrom < 0)
                {
                    keepTo -= keepFrom;
                    keepFrom = 0;
                }
                if (keepTo >= size)
                {
                    int delta = size - keepTo - 1;
                    keepTo += delta;
                    keepFrom += delta;
                }

                trimEdits(keepTo + 1, size - 1);
                trimEdits(0, keepFrom - 1);
            }
        }
    }

    /**
     * Removes edits in the specified range.
     * All edits in the given range (inclusive, and in reverse order)
     * will have <code>die</code> invoked on them and are removed from
     * the list of edits. This has no effect if <code>from</code> &gt; <code>to</code>.
     * 
     * @param from
     *        the minimum index to remove
     * @param to
     *        the maximum index to remove
     */
    protected void trimEdits(int from, int to)
    {
        if (from <= to)
        {
            synchronized (edits)
            {
                for (int i = to; from <= i; i--)
                {
                    edits.get(i).die();
                    edits.remove(i);
                }
            }

            if (indexOfNextAdd > to)
                indexOfNextAdd -= to - from + 1;
            else if (indexOfNextAdd >= from)
                indexOfNextAdd = from;
        }
    }

    /**
     * Sets the maximum number of edits this <code>UndoManager</code> holds. A value less than 0
     * indicates the number of edits is not limited. If edits need to be discarded
     * to shrink the limit, <code>die</code> will be invoked on them in the reverse
     * order they were added. The default is 100.
     * 
     * @param l
     *        the new limit
     * @throws RuntimeException
     *         if this {@code UndoManager} is not in progress
     *         ({@code end} has been invoked)
     * @see #addEdit
     * @see #getLimit
     */
    public synchronized void setLimit(int l)
    {
        limit = l;
        trimForLimit();
    }

    /**
     * Returns the the next significant edit to be undone if <code>undo</code> is invoked. This
     * returns <code>null</code> if there are no edits to be undone.
     * 
     * @return the next significant edit to be undone
     */
    protected AbstractIcyUndoableEdit editToBeUndone()
    {
        int i = indexOfNextAdd;

        while (i > 0)
        {
            final AbstractIcyUndoableEdit edit = edits.get(--i);

            if (edit.isSignificant())
                return edit;
        }

        return null;
    }

    /**
     * Returns the the next significant edit to be redone if <code>redo</code> is invoked. This
     * returns <code>null</code> if there are no edits to be redone.
     * 
     * @return the next significant edit to be redone
     */
    protected AbstractIcyUndoableEdit editToBeRedone()
    {
        final int count = edits.size();
        int i = indexOfNextAdd;

        while (i < count)
        {
            final AbstractIcyUndoableEdit edit = edits.get(i++);

            if (edit.isSignificant())
                return edit;
        }

        return null;
    }

    /**
     * Undoes all changes.
     * 
     * @throws CannotUndoException
     *         if one of the edits throws <code>CannotUndoException</code>
     */
    public void undoAll() throws CannotUndoException
    {
        while (indexOfNextAdd > 0)
        {
            final AbstractIcyUndoableEdit next = edits.get(--indexOfNextAdd);
            next.undo();
        }
    }

    /**
     * Undoes all changes from the index of the next edit to <code>edit</code>, updating the index
     * of the next edit appropriately.
     * 
     * @throws CannotUndoException
     *         if one of the edits throws <code>CannotUndoException</code>
     */
    protected void undoTo(AbstractIcyUndoableEdit edit) throws CannotUndoException
    {
        boolean done = false;

        while (!done)
        {
            final AbstractIcyUndoableEdit next = edits.get(--indexOfNextAdd);
            next.undo();
            done = (next == edit);
        }
    }

    /**
     * Undoes the appropriate edits. This invokes <code>undo</code> on all edits between the
     * index of the next edit and the last significant edit, updating
     * the index of the next edit appropriately.
     * 
     * @throws CannotUndoException
     *         if one of the edits throws <code>CannotUndoException</code> or there are no edits
     *         to be undone
     * @see #canUndo
     * @see #editToBeUndone
     */
    @Override
    public synchronized void undo() throws CannotUndoException
    {
        final AbstractIcyUndoableEdit edit = editToBeUndone();

        if (edit == null)
            throw new CannotUndoException();

        undoTo(edit);

        // notify change
        fireChangeEvent();
    }

    /**
     * Returns true if edits may be undone. This returns true if there are any edits to be undone
     * (<code>editToBeUndone</code> returns non-<code>null</code>).
     * 
     * @return true if there are edits to be undone
     * @see #editToBeUndone
     */
    @Override
    public synchronized boolean canUndo()
    {
        AbstractIcyUndoableEdit edit = editToBeUndone();
        return edit != null && edit.canUndo();
    }

    /**
     * Redoes all changes from the index of the next edit to <code>edit</code>, updating the index
     * of the next edit appropriately.
     * 
     * @throws CannotRedoException
     *         if one of the edits throws <code>CannotRedoException</code>
     */
    protected void redoTo(AbstractIcyUndoableEdit edit) throws CannotRedoException
    {
        boolean done = false;

        while (!done)
        {
            AbstractIcyUndoableEdit next = edits.get(indexOfNextAdd++);
            next.redo();
            done = (next == edit);
        }
    }

    /**
     * Redo the appropriate edits. This invokes <code>redo</code> on
     * all edits between the index of the next edit and the next
     * significant edit, updating the index of the next edit appropriately.
     * 
     * @throws CannotRedoException
     *         if one of the edits throws <code>CannotRedoException</code> or there are no edits
     *         to be redone
     * @see CompoundEdit#end
     * @see #canRedo
     * @see #editToBeRedone
     */
    @Override
    public synchronized void redo() throws CannotRedoException
    {
        AbstractIcyUndoableEdit edit = editToBeRedone();
        if (edit == null)
        {
            throw new CannotRedoException();
        }
        redoTo(edit);
        fireChangeEvent();
    }

    /**
     * Returns true if edits may be redone. If <code>end</code> has
     * been invoked, this returns the value from super. Otherwise,
     * this returns true if there are any edits to be redone
     * (<code>editToBeRedone</code> returns non-<code>null</code>).
     * 
     * @return true if there are edits to be redone
     * @see CompoundEdit#canRedo
     * @see #editToBeRedone
     */
    @Override
    public synchronized boolean canRedo()
    {
        AbstractIcyUndoableEdit edit = editToBeRedone();
        return edit != null && edit.canRedo();
    }

    /**
     * Undo or redo all changes until the specified edit.<br>
     * The specified edit should be in "done" state after the operation.<br>
     * That means redo operation is inclusive while undo is exclusive.<br>
     * To undo all operations just use undoAll().
     */
    public synchronized void undoOrRedoTo(AbstractIcyUndoableEdit edit) throws CannotRedoException, CannotUndoException
    {
        final int index = getIndex(edit);

        // can undo or redo ?
        if (index != -1)
        {
            // we want indexOfNextAdd to change to (index + 1)
            while ((indexOfNextAdd - 1) > index)
            {
                // process undo
                final AbstractIcyUndoableEdit next = edits.get(--indexOfNextAdd);
                next.undo();
            }
            while (indexOfNextAdd <= index)
            {
                // process undo
                AbstractIcyUndoableEdit next = edits.get(indexOfNextAdd++);
                next.redo();
            }

            // notify change
            fireChangeEvent();
        }
    }

    /**
     * Returns the last <code>AbstractIcyUndoableEdit</code> in <code>edits</code>, or
     * <code>null</code> if <code>edits</code> is empty.
     */
    protected AbstractIcyUndoableEdit lastEdit()
    {
        int count = edits.size();

        if (count > 0)
            return edits.get(count - 1);

        return null;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit)
    {
        if (anEdit instanceof AbstractIcyUndoableEdit)
            addEdit((AbstractIcyUndoableEdit) anEdit);

        return super.addEdit(anEdit);
    }

    /**
     * Adds an <code>AbstractIcyUndoableEdit</code> to this <code>UndoManager</code>, if it's
     * possible. This
     * removes all edits from the index of the next edit to the end of the edits
     * list.
     * 
     * @param anEdit
     *        the edit to be added
     * @see CompoundEdit#addEdit
     */
    public synchronized void addEdit(AbstractIcyUndoableEdit anEdit)
    {
        // Trim from the indexOfNextAdd to the end, as we'll
        // never reach these edits once the new one is added.
        trimEdits(indexOfNextAdd, edits.size() - 1);

        final AbstractIcyUndoableEdit last = lastEdit();

        // If this is the first edit received, just add it.
        // Otherwise, give the last one a chance to absorb the new
        // one. If it won't, give the new one a chance to absorb
        // the last one.
        if (last == null)
            edits.add(anEdit);
        else if (!last.addEdit(anEdit))
        {
            // try to replace current edit
            if (anEdit.replaceEdit(last))
                edits.set(edits.size() - 1, anEdit);
            else
                // simply add the new edit
                edits.add(anEdit);
        }

        // make sure the indexOfNextAdd is pointed at the right place
        indexOfNextAdd = edits.size();

        // enforce the limit
        trimForLimit();

        // notify change
        fireChangeEvent();
    }

    /**
     * Prevent the last edit inserted in the UndoManager to be merged with the next inserted edit
     * (even if they are compatible)
     */
    public void noMergeForNextEdit()
    {
        // set last edit to un-mergeable
        if (indexOfNextAdd > 0)
            edits.get(indexOfNextAdd - 1).setMergeable(false);
    }

    /**
     * Returns a description of the undoable form of this edit.
     * If there are edits to be undone, this returns
     * the value from the next significant edit that will be undone.
     * If there are no edits to be undone this returns the value from
     * the <code>UIManager</code> property "AbstractUndoableEdit.undoText".
     * 
     * @return a description of the undoable form of this edit
     * @see #undo
     * @see CompoundEdit#getUndoPresentationName
     */
    @Override
    public synchronized String getUndoPresentationName()
    {
        if (canUndo())
            return editToBeUndone().getUndoPresentationName();

        return UIManager.getString("AbstractUndoableEdit.undoText");
    }

    /**
     * Returns a description of the redoable form of this edit.
     * If there are edits to be redone, this returns
     * the value from the next significant edit that will be redone.
     * If there are no edits to be redone this returns the value from
     * the <code>UIManager</code> property "AbstractUndoableEdit.redoText".
     * 
     * @return a description of the redoable form of this edit
     * @see #redo
     * @see CompoundEdit#getRedoPresentationName
     */
    @Override
    public synchronized String getRedoPresentationName()
    {
        if (canRedo())
            return editToBeRedone().getRedoPresentationName();

        return UIManager.getString("AbstractUndoableEdit.redoText");
    }

    /**
     * Add the specified listener to listeners list
     */
    public void addListener(IcyUndoManagerListener listener)
    {
        listeners.add(IcyUndoManagerListener.class, listener);
    }

    /**
     * Remove the specified listener from listeners list
     */
    public void removeListener(IcyUndoManagerListener listener)
    {
        listeners.remove(IcyUndoManagerListener.class, listener);
    }

    /**
     * Get listeners list
     */
    public IcyUndoManagerListener[] getListeners()
    {
        return listeners.getListeners(IcyUndoManagerListener.class);
    }

    /**
     * fire change event
     */
    private void fireChangeEvent()
    {
        for (IcyUndoManagerListener listener : listeners.getListeners(IcyUndoManagerListener.class))
            listener.undoManagerChanged(this);
    }

    /**
     * Retrieve all edits in the UndoManager
     */
    public List<AbstractIcyUndoableEdit> getAllEdits()
    {
        final List<AbstractIcyUndoableEdit> result = new ArrayList<AbstractIcyUndoableEdit>();

        synchronized (edits)
        {
            for (AbstractIcyUndoableEdit edit : edits)
                result.add(edit);
        }

        return result;
    }

    /**
     * Get number of edit in UndoManager
     */
    public int getEditsCount()
    {
        return edits.size();
    }

    /**
     * Get the index in list of specified edit
     */
    public int getIndex(AbstractIcyUndoableEdit e)
    {
        return edits.indexOf(e);
    }

    /**
     * Get the index in list of specified significant edit
     */
    public int getSignificantIndex(AbstractIcyUndoableEdit e)
    {
        int result = 0;

        synchronized (edits)
        {
            for (AbstractIcyUndoableEdit edit : edits)
            {
                if (edit.isSignificant())
                {
                    if (edit == e)
                        return result;
                    result++;
                }
            }
        }

        return -1;
    }

    /**
     * Get number of significant edit before the specified position in UndoManager
     */
    public int getSignificantEditsCountBefore(int index)
    {
        int result = 0;

        synchronized (edits)
        {
            for (int i = 0; i < index; i++)
                if (edits.get(i).isSignificant())
                    result++;
        }

        return result;
    }

    /**
     * Get number of significant edit in UndoManager
     */
    public int getSignificantEditsCount()
    {
        int result = 0;

        synchronized (edits)
        {
            for (AbstractIcyUndoableEdit edit : edits)
                if (edit.isSignificant())
                    result++;
        }

        return result;
    }

    /**
     * Get edit of specified index
     */
    public AbstractIcyUndoableEdit getEdit(int index)
    {
        synchronized (edits)
        {
            if (index < edits.size())
                return edits.get(index);
        }

        return null;
    }

    /**
     * Get significant edit of specified index
     */
    public AbstractIcyUndoableEdit getSignificantEdit(int index)
    {
        int i = 0;

        synchronized (edits)
        {
            for (AbstractIcyUndoableEdit edit : edits)
            {
                if (edit.isSignificant())
                {
                    if (i == index)
                        return edit;
                    i++;
                }
            }
        }

        return null;
    }

    /**
     * Return the next insert index.
     */
    public int getNextAddIndex()
    {
        return indexOfNextAdd;
    }

    /**
     * Get index of first edit from specified source
     */
    public int getFirstEditIndex(Object source)
    {
        if (source == null)
            return -1;

        synchronized (edits)
        {
            for (int i = 0; i < edits.size(); i++)
                if ((edits.get(i)).getSource() == source)
                    return i;
        }

        return -1;
    }

    /**
     * Get index of last edit from specified source
     */
    public int getLastEditIndex(Object source)
    {
        if (source == null)
            return -1;

        synchronized (edits)
        {
            for (int i = edits.size() - 1; i >= 0; i--)
                if ((edits.get(i)).getSource() == source)
                    return i;
        }

        return -1;
    }

    /**
     * Discard edits from specified source by sending each edit a <code>die</code> message
     * in the process.
     */
    public void discardEdits(Object source)
    {
        synchronized (edits)
        {
            final int lastIndex = getLastEditIndex(source);

            if (lastIndex != -1)
            {
                final List<AbstractIcyUndoableEdit> validEdits = new ArrayList<AbstractIcyUndoableEdit>();

                // keep valid edits
                for (int i = lastIndex + 1; i < edits.size(); i++)
                    validEdits.add(edits.get(i));

                // remove all edits
                edits.clear();
                indexOfNextAdd = 0;

                // add valid edits
                for (AbstractIcyUndoableEdit edit : validEdits)
                    edits.add(edit);

                // make sure the indexOfNextAdd is pointed at the right place
                indexOfNextAdd = edits.size();

                // notify we removed some edits
                fireChangeEvent();
            }
        }
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e)
    {
        addEdit(e.getEdit());
    }

}
