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

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;

/**
 * Weak wrapper for IcyFrameListener.
 * 
 * @author Stephane
 */
public class WeakIcyFrameListener extends WeakListener<IcyFrameListener> implements IcyFrameListener
{
    public WeakIcyFrameListener(IcyFrameListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((IcyFrame) source).removeFrameListener(this);
    }

    @Override
    public void icyFrameOpened(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameOpened(e);
    }

    @Override
    public void icyFrameClosing(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameClosing(e);
    }

    @Override
    public void icyFrameClosed(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameClosed(e);
    }

    @Override
    public void icyFrameIconified(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameIconified(e);
    }

    @Override
    public void icyFrameDeiconified(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameDeiconified(e);
    }

    @Override
    public void icyFrameActivated(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameActivated(e);
    }

    @Override
    public void icyFrameDeactivated(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameDeactivated(e);
    }

    @Override
    public void icyFrameInternalized(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameInternalized(e);
    }

    @Override
    public void icyFrameExternalized(IcyFrameEvent e)
    {
        final IcyFrameListener listener = getListener(e.getFrame());

        if (listener != null)
            listener.icyFrameExternalized(e);
    }
}
