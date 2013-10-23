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
package icy.util;

import icy.system.SystemUtil;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Event related utilities
 * 
 * @author Stephane
 */
public class EventUtil
{
    /**
     * Returns true if Shift key is pressed for the specified event.
     */
    public static boolean isShiftDown(InputEvent e)
    {
        return isShiftDown(e, false);
    }

    /**
     * Returns true if Shift key is pressed for the specified event.
     */
    public static boolean isShiftDown(InputEvent e, boolean exclusive)
    {
        if (exclusive)
            return (e.getModifiers() == InputEvent.SHIFT_MASK);

        return e.isShiftDown();
    }

    /**
     * Returns true if Alt key is pressed for the specified event
     */
    public static boolean isAltDown(InputEvent e)
    {
        return isAltDown(e, false);
    }

    /**
     * Returns true if Alt key is pressed for the specified event.
     */
    public static boolean isAltDown(InputEvent e, boolean exclusive)
    {
        if (exclusive)
            return (e.getModifiers() == InputEvent.ALT_MASK);

        return e.isAltDown();
    }

    /**
     * Returns true if Ctrl key is pressed for the specified event
     */
    public static boolean isControlDown(InputEvent e)
    {
        return isControlDown(e, false);
    }

    /**
     * Returns true if Ctrl key is pressed for the specified event
     */
    public static boolean isControlDown(InputEvent e, boolean exclusive)
    {
        if (exclusive)
            return (e.getModifiers() == InputEvent.CTRL_MASK);

        return e.isControlDown();
    }

    /**
     * Returns true if Ctrl/Cmd menu key is pressed for the specified event.
     */
    public static boolean isMenuControlDown(InputEvent e)
    {
        return isMenuControlDown(e, false);
    }

    /**
     * Returns true if Ctrl/Cmd menu key is pressed for the specified event.
     */
    public static boolean isMenuControlDown(InputEvent e, boolean exclusive)
    {
        if (exclusive)
            return (e.getModifiers() == SystemUtil.getMenuCtrlMask());

        // take care of OSX CMD key here
        return (e.getModifiers() & SystemUtil.getMenuCtrlMask()) != 0;
    }

    /**
     * Returns true if there is no any modifiers in the specified input event.
     */
    public static boolean isNoModifier(InputEvent e)
    {
        return e.getModifiers() == 0;
    }

    /**
     * Returns true if the mouse event specifies the left mouse button.
     * 
     * @param e
     *        a MouseEvent object
     * @return true if the left mouse button was active
     */
    public static boolean isLeftMouseButton(MouseEvent e)
    {
        return ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
    }

    /**
     * Returns true if the mouse event specifies the middle mouse button.
     * 
     * @param e
     *        a MouseEvent object
     * @return true if the middle mouse button was active
     */
    public static boolean isMiddleMouseButton(MouseEvent e)
    {
        return ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
    }

    /**
     * Returns true if the mouse event specifies the right mouse button.
     * 
     * @param e
     *        a MouseEvent object
     * @return true if the right mouse button was active
     */
    public static boolean isRightMouseButton(MouseEvent e)
    {
        return ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
    }

}
