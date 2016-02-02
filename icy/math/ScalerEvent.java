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
package icy.math;

import icy.common.CollapsibleEvent;

/**
 * @author stephane
 */
public class ScalerEvent implements CollapsibleEvent
{
    /**
     * @deprecated
     */
    @Deprecated
    public enum ScalerEventType
    {
        CHANGED
    }

    private final Scaler scaler;

    /**
     * @param scaler
     */
    public ScalerEvent(Scaler scaler)
    {
        super();

        this.scaler = scaler;
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler()
    {
        return scaler;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ScalerEventType getType()
    {
        return ScalerEventType.CHANGED;
    }

    @Override
    public boolean collapse(CollapsibleEvent event)
    {
        if (equals(event))
        {
            // nothing to do here
            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return scaler.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ScalerEvent)
        {
            final ScalerEvent e = (ScalerEvent) obj;

            return (scaler == e.getScaler());
        }

        return super.equals(obj);
    }
}
