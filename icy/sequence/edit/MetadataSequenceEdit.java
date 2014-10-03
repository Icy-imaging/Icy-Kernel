/**
 * 
 */
package icy.sequence.edit;

import icy.sequence.Sequence;

import java.awt.Image;

import javax.swing.undo.CannotUndoException;

import loci.formats.ome.OMEXMLMetadataImpl;

/**
 * Default lazy sequence metadata undoable edit (do a complete sequence metadata copy to restore
 * previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 * 
 * @author Stephane
 */
public class MetadataSequenceEdit extends AbstractSequenceEdit
{
    OMEXMLMetadataImpl previous;

    public MetadataSequenceEdit(OMEXMLMetadataImpl previous, Sequence sequence, Image icon)
    {
        super(sequence, "Sequence metadata changed", icon);

        this.previous = previous;
    }

    public MetadataSequenceEdit(OMEXMLMetadataImpl previous, Sequence sequence)
    {
        this(previous, sequence, null);
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
