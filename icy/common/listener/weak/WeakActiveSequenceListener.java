/**
 * 
 */
package icy.common.listener.weak;

import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

/**
 * @author Stephane
 */
public class WeakActiveSequenceListener extends WeakListener<ActiveSequenceListener> implements ActiveSequenceListener
{
    public WeakActiveSequenceListener(ActiveSequenceListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        Icy.getMainInterface().removeActiveSequenceListener(this);
    }

    @Override
    public void sequenceActivated(Sequence sequence)
    {

        final ActiveSequenceListener listener = getListener(null);

        if (listener != null)
            listener.sequenceActivated(sequence);
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        final ActiveSequenceListener listener = getListener(null);

        if (listener != null)
            listener.sequenceDeactivated(sequence);
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        final ActiveSequenceListener listener = getListener(null);

        if (listener != null)
            listener.activeSequenceChanged(event);
    }
}
