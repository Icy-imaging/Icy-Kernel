/**
 * 
 */
package icy.undo;

import java.util.EventListener;

/**
 * @author Stephane
 */
public interface IcyUndoManagerListener extends EventListener
{
    public void undoManagerChanged(IcyUndoManager source);
}
