/**
 * 
 */
package icy.gui.main;

import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.util.EventListener;

/**
 * Listener interface for the current active {@link Sequence}.
 * 
 * @author Stephane
 */
public interface ActiveSequenceListener extends EventListener
{
    /**
     * Sequence just get the active state.<br/>
     * This event is generally preceded by a {@link #sequenceDeactivated(Sequence)} event describing
     * the sequence which actually lose activation.
     */
    public void sequenceActivated(Sequence sequence);

    /**
     * Sequence just lost the active state.<br/>
     * This event is always followed by a {@link #sequenceActivated(Sequence)} event describing the
     * new activated sequence.
     */
    public void sequenceDeactivated(Sequence sequence);

    /**
     * The current active sequence has changed.
     */
    public void activeSequenceChanged(SequenceEvent event);
}
