/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
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
