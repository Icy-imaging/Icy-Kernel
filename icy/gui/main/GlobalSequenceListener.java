package icy.gui.main;

import icy.sequence.Sequence;

import java.util.EventListener;

/**
 * Global {@link Sequence} listener class.
 * Used to listen open, focus and close event for all sequence.
 * 
 * @author Stephane
 */
public interface GlobalSequenceListener extends EventListener
{
    /**
     * Sequence was just opened (first viewer displaying the sequence just opened)
     */
    public void sequenceOpened(Sequence sequence);

    /**
     * Sequence was just closed (last viewer displaying the sequence just closed)
     */
    public void sequenceClosed(Sequence sequence);
}
