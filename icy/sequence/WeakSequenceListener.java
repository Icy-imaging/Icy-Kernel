/**
 * 
 */
package icy.sequence;

import icy.common.listener.weak.WeakListener;

/**
 * Weak listener wrapper for SequenceListener interface
 * 
 * @author Stephane
 */
public class WeakSequenceListener extends WeakListener<SequenceListener> implements SequenceListener
{
    public WeakSequenceListener(SequenceListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Sequence) source).removeListener(this);
    }

    @Override
    public void sequenceChanged(SequenceEvent event)
    {
        final SequenceListener listener = getListener(event.getSequence());

        if (listener != null)
            listener.sequenceChanged(event);
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        final SequenceListener listener = getListener(sequence);

        if (listener != null)
            listener.sequenceClosed(sequence);
    }
}
