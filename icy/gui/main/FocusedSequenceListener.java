/**
 * 
 */
package icy.gui.main;

import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.util.EventListener;

/**
 * @author Stephane
 */
public interface FocusedSequenceListener extends EventListener
{
    /**
     * A sequence just got the focus
     */
    public void sequenceFocused(Sequence sequence);

    /**
     * The focused sequence has changed
     */
    public void focusedSequenceChanged(SequenceEvent event);
}
