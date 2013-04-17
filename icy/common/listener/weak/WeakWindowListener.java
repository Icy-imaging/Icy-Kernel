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

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Stephane
 */
public class WeakWindowListener extends WeakListener<WindowListener> implements WindowListener
{
    public WeakWindowListener(WindowListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Window) source).removeWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowOpened(e);
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowClosing(e);
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowClosed(e);
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowIconified(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowDeiconified(e);
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowActivated(e);
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
        final WindowListener listener = getListener(e.getWindow());

        if (listener != null)
            listener.windowDeactivated(e);
    }

}
