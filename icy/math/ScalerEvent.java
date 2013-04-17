/*
 * Copyright 2010-2013 Institut Pasteur.
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

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class ScalerEvent implements EventHierarchicalChecker
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
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        return (event instanceof ScalerEvent) && (scaler == ((ScalerEvent) event).getScaler());
    }

}
