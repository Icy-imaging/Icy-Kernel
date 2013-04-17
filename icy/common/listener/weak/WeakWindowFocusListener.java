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
import java.awt.event.WindowFocusListener;

/**
 * @author Thomas
 */
public class WeakWindowFocusListener extends WeakListener<WindowFocusListener> implements WindowFocusListener
{
    public WeakWindowFocusListener(WindowFocusListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        if (source != null)
            ((Window) source).removeWindowFocusListener(this);
    }

    @Override
    public void windowGainedFocus(WindowEvent windowevent)
    {
        final WindowFocusListener listener = getListener(windowevent.getWindow());

        if (listener != null)
            listener.windowGainedFocus(windowevent);
    }

    @Override
    public void windowLostFocus(WindowEvent windowevent)
    {
        final WindowFocusListener listener = getListener(windowevent.getWindow());

        if (listener != null)
            listener.windowLostFocus(windowevent);
    }

}
