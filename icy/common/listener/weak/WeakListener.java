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
package icy.common.listener.weak;

import java.lang.ref.WeakReference;

/**
 * Base weak listener class.
 * 
 * @author Stephane
 */
public abstract class WeakListener<T>
{
    private final WeakReference<T> listenerRef;

    public WeakListener(T listener)
    {
        super();

        listenerRef = new WeakReference<T>(listener);
    }

    public T getListener(Object source)
    {
        final T listener = listenerRef.get();

        // no more listener --> remove weak object from list
        if (listener == null)
            removeListener(source);

        return listener;
    }

    public T getListener()
    {
        return getListener(null);
    }

    public abstract void removeListener(Object source);
}
