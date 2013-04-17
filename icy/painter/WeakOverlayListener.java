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
package icy.painter;

import icy.common.listener.weak.WeakListener;

/**
 * Weak wrapper for OverlayListener.
 * 
 * @author Stephane
 */
public class WeakOverlayListener extends WeakListener<OverlayListener> implements OverlayListener
{
    public WeakOverlayListener(OverlayListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Overlay) source).removeOverlayListener(this);
    }

    @Override
    public void overlayChanged(OverlayEvent event)
    {
        final OverlayListener listener = getListener(event.getSource());

        if (listener != null)
            listener.overlayChanged(event);
    }
}