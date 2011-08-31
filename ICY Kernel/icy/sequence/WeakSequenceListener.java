/**
 * 
 */
package icy.sequence;

import java.lang.ref.WeakReference;

/**
 * Weak listener wrapper for SequenceListener interface
 * 
 * @author Stephane
 */
public class WeakSequenceListener implements SequenceListener
{
    private final WeakReference<SequenceListener> listenerRef;

    public WeakSequenceListener(SequenceListener listener)
    {
        super();

        listenerRef = new WeakReference<SequenceListener>(listener);
    }

    private SequenceListener getListener(Sequence sequence)
    {
        final SequenceListener listener = listenerRef.get();

        if ((listener == null) && (sequence != null))
            sequence.removeListener(this);

        return listener;
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
