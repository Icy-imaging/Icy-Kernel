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
package icy.swimmingPool;

import icy.common.listener.weak.WeakListener;
import icy.main.Icy;

/**
 * Weak listener wrapper for SwimmingPoolListener.
 * 
 * @author Stephane
 */
public class WeakSwimmingPoolListener extends WeakListener<SwimmingPoolListener> implements SwimmingPoolListener
{
    public WeakSwimmingPoolListener(SwimmingPoolListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        final SwimmingPool swimmingPool = Icy.getMainInterface().getSwimmingPool();

        if (swimmingPool != null)
            swimmingPool.removeListener(this);
    }

    @Override
    public void swimmingPoolChangeEvent(SwimmingPoolEvent event)
    {
        final SwimmingPoolListener listener = getListener();

        if (listener != null)
            listener.swimmingPoolChangeEvent(event);
    }
}
