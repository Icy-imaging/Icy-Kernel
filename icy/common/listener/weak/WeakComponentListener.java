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
package icy.common.listener.weak;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Weak wrapper for ComponentListener.
 * 
 * @author Stephane
 */
public class WeakComponentListener extends WeakListener<ComponentListener> implements ComponentListener
{
    public WeakComponentListener(ComponentListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Component) source).removeComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentResized(e);
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentMoved(e);
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentShown(e);
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
        final ComponentListener listener = getListener(e.getComponent());

        if (listener != null)
            listener.componentHidden(e);
    }
}
