/**
 * 
 */
package icy.sequence.edit;

import icy.sequence.Sequence;

import java.awt.Image;

import javax.swing.undo.CannotUndoException;

/**
 * Default lazy sequence undoable edit (do a complete sequence copy to restore previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 * 
 * @author Stephane
 */
public class DefaultSequenceEdit extends AbstractSequenceEdit
{
    Sequence previous;

    public DefaultSequenceEdit(Sequence previous, Sequence sequence, Image icon)
    {
        super(sequence, icon);

        this.previous = previous;
    }

    public DefaultSequenceEdit(Sequence previous, Sequence sequence)
    {
        this(previous, sequence, null);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getSequence().copyFrom(previous, true);
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
