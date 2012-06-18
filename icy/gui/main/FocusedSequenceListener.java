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
     * The focus just changed to another sequence.
     */
    public void focusChanged(Sequence sequence);

    /**
     * The focused sequence has changed.
     */
    public void focusedSequenceChanged(SequenceEvent event);
}
