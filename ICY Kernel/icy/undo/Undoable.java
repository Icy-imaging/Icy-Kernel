/**
 * 
 */
package icy.undo;

/**
 * @author Stephane
 * @deprecated
 */
@Deprecated
public interface Undoable
{
    /**
     * Registers an <code>IcyUndoableEditListener</code>.
     * The listener is notified whenever an edit occurs which can be undone.
     */
    public void addUndoableEditListener(IcyUndoableEditListener l);

    /**
     * Removes an <code>IcyUndoableEditListener</code>.
     */
    public void removeUndoableEditListener(IcyUndoableEditListener l);

    /**
     * Returns an array of all the <code>IcyUndoableEditListener</code>s
     */
    public IcyUndoableEditListener[] getUndoableEditListeners();

    /**
     */
    public void postEdit(IcyUndoableEdit e);
}
