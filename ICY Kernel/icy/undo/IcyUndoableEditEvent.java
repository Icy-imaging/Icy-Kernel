/**
 * 
 */
package icy.undo;

import javax.swing.event.UndoableEditEvent;

/**
 * @author Stephane
 */
public class IcyUndoableEditEvent extends UndoableEditEvent
{
    /**
     * 
     */
    private static final long serialVersionUID = 1852166528073863792L;

    public IcyUndoableEditEvent(Object source, IcyUndoableEdit edit)
    {
        super(source, edit);
    }
}
