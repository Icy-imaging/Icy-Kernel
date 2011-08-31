/**
 * 
 */
package icy.undo;

import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class IcyUndoableEditSupport extends UndoableEditSupport implements Undoable
{
    /**
     * Uses {@link #postEdit(IcyUndoableEdit)} instead
     */
    @Override
    @Deprecated
    public synchronized void postEdit(UndoableEdit e)
    {
        if (e instanceof IcyUndoableEdit)
            super.postEdit(e);
    }

    /**
     * Uses {@link #addUndoableEditListener(IcyUndoableEditListener)} instead
     */
    @Override
    @Deprecated
    public synchronized void addUndoableEditListener(UndoableEditListener l)
    {
        if (l instanceof IcyUndoableEditListener)
            super.addUndoableEditListener(l);
    }

    /**
     * Uses {@link #removeUndoableEditListener(IcyUndoableEditListener)} instead
     */
    @Override
    @Deprecated
    public synchronized void removeUndoableEditListener(UndoableEditListener l)
    {
        if (l instanceof IcyUndoableEditListener)
            super.removeUndoableEditListener(l);
    }

    @Override
    public synchronized IcyUndoableEditListener[] getUndoableEditListeners()
    {
        return (IcyUndoableEditListener[]) super.getUndoableEditListeners();
    }

    @Override
    public void addUndoableEditListener(IcyUndoableEditListener l)
    {
        super.addUndoableEditListener(l);
    }

    @Override
    public void removeUndoableEditListener(IcyUndoableEditListener l)
    {
        super.removeUndoableEditListener(l);
    }

    @Override
    public void postEdit(IcyUndoableEdit e)
    {
        super.postEdit(e);
    }
}
