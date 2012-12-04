/**
 * 
 */
package icy.sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 */
public abstract class AbstractSequenceModel implements SequenceModel
{
    private final List<SequenceModelListener> listeners;

    public AbstractSequenceModel()
    {
        super();

        listeners = new ArrayList<SequenceModelListener>();
    }

    /**
     * fire model image changed event
     */
    @Override
    public void fireModelImageChangedEvent()
    {
        for (SequenceModelListener listener : new ArrayList<SequenceModelListener>(listeners))
            listener.imageChanged();
    }

    /**
     * fire model dimension changed event
     */
    @Override
    public void fireModelDimensionChangedEvent()
    {
        for (SequenceModelListener listener : new ArrayList<SequenceModelListener>(listeners))
            listener.dimensionChanged();
    }

    @Override
    public void addSequenceModelListener(SequenceModelListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(listener);
        }
    }

    @Override
    public void removeSequenceModelListener(SequenceModelListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }
}
