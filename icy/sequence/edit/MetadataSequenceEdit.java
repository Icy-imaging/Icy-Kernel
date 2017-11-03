/**
 * 
 */
package icy.sequence.edit;

import icy.sequence.Sequence;

import java.awt.Image;

import javax.swing.undo.CannotUndoException;

import ome.xml.meta.OMEXMLMetadata;

/**
 * Default lazy sequence metadata undoable edit (do a complete sequence metadata copy to restore
 * previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 * 
 * @author Stephane
 */
public class MetadataSequenceEdit extends AbstractSequenceEdit
{
    OMEXMLMetadata previous;

    public MetadataSequenceEdit(OMEXMLMetadata previous, Sequence sequence, String name, Image icon)
    {
        super(sequence, name, icon);

        this.previous = previous;
    }

    public MetadataSequenceEdit(OMEXMLMetadata previous, Sequence sequence, Image icon)
    {
        this(previous, sequence, "Sequence metadata changed", icon);

        this.previous = previous;
    }

    public MetadataSequenceEdit(OMEXMLMetadata previous, Sequence sequence, String name)
    {
        this(previous, sequence, name, null);

        this.previous = previous;
    }

    public MetadataSequenceEdit(OMEXMLMetadata previous, Sequence sequence)
    {
        this(previous, sequence, "Sequence metadata changed", null);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getSequence().setMetaData(previous);
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
