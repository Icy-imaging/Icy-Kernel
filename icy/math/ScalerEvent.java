/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.math;

import icy.common.EventHierarchicalChecker;

/**
 * @author stephane
 */
public class ScalerEvent implements EventHierarchicalChecker
{
    public enum ScalerEventType
    {
        CHANGED
    }

    private final Scaler scaler;
    private final ScalerEventType type;

    /**
     * @param scaler
     * @param type
     */
    public ScalerEvent(Scaler scaler, ScalerEventType type)
    {
        super();

        this.scaler = scaler;
        this.type = type;
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler()
    {
        return scaler;
    }

    /**
     * @return the type
     */
    public ScalerEventType getType()
    {
        return type;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        return (event instanceof ScalerEvent) && (type == ((ScalerEvent) event).getType());
    }

}
