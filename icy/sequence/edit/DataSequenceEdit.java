/**
 * 
 */
package icy.sequence.edit;

import icy.sequence.Sequence;

import java.awt.Image;

import javax.swing.undo.CannotUndoException;

/**
 * Default lazy sequence data undoable edit (do a complete sequence data copy to restore previous
 * state).<br>
 * Do not handle redo operation to not consume too much memory.
 * 
 * @author Stephane
 */
public class DataSequenceEdit extends AbstractSequenceEdit
{
    Sequence previous;

    public DataSequenceEdit(Sequence previous, Sequence sequence, String name, Image icon)
    {
        super(sequence, name, icon);

        this.previous = previous;
    }

    public DataSequenceEdit(Sequence previous, Sequence sequence, String name)
    {
        this(previous, sequence, name, null);
    }

    public DataSequenceEdit(Sequence previous, Sequence sequence, Image icon)
    {
        this(previous, sequence, "Sequence data changed", icon);
    }

    public DataSequenceEdit(Sequence previous, Sequence sequence)
    {
        this(previous, sequence, "Sequence data changed", null);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getSequence().copyDataFrom(previous);
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
