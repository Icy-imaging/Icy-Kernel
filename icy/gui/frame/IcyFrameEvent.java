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
package icy.gui.frame;

import java.awt.AWTEvent;
import java.awt.event.WindowEvent;

import javax.swing.event.InternalFrameEvent;

/**
 * @author stephane
 */
public class IcyFrameEvent
{
    private final IcyFrame frame;

    private final InternalFrameEvent internalFrameEvent;
    private final WindowEvent externalFrameEvent;

    /**
     * @param frame
     * @param internalFrameEvent
     * @param externalFrameEvent
     */
    public IcyFrameEvent(IcyFrame frame, InternalFrameEvent internalFrameEvent, WindowEvent externalFrameEvent)
    {
        super();

        this.frame = frame;
        this.internalFrameEvent = internalFrameEvent;
        this.externalFrameEvent = externalFrameEvent;
    }

    /**
     * @return the frame
     */
    public IcyFrame getFrame()
    {
        return frame;
    }

    /**
     * @return the internalFrameEvent
     */
    public InternalFrameEvent getInternalFrameEvent()
    {
        return internalFrameEvent;
    }

    /**
     * @return the externalFrameEvent
     */
    public WindowEvent getExternalFrameEvent()
    {
        return externalFrameEvent;
    }

    /**
     * Return the active event
     */
    public AWTEvent getEvent()
    {
        if (internalFrameEvent != null)
            return internalFrameEvent;

        return externalFrameEvent;
    }

}
