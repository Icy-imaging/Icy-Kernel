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
package icy.clipboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Clipboard object (used for easy internal Copy/Paste operation).
 * 
 * @author Stephane
 */
public class Clipboard
{
    public interface ClipboardListener
    {
        public void clipboardChanged();
    }

    private final static List<ClipboardListener> listeners = new ArrayList<Clipboard.ClipboardListener>();

    public static final String TYPE_ROILIST = "RoiList";
    public static final String TYPE_ROILINKLIST = "RoiLinkList";
    public static final String TYPE_SEQUENCE = "Sequence";
    // Object should be BufferedImage
    public static final String TYPE_IMAGE = "Image";

    private static String type = "";
    private static Object object = null;

    /**
     * Clears the clipboard.
     */
    public static void clear()
    {
        type = "";
        object = null;
    }

    /**
     * Returns true if the specified type match the current type stored in Clipboard
     */
    public static boolean isType(String t)
    {
        return type.equals(t);
    }

    /**
     * Returns the type of stored object.
     */
    public static String getType()
    {
        return type;
    }

    /**
     * Returns object actually stored in the clipboard.
     */
    public static Object get()
    {
        return object;
    }

    /**
     * Returns object actually stored in the clipboard if it has the specified type.<br>
     * Returns <code>null</code> otherwise.
     */
    public static Object get(String type)
    {
        if (Clipboard.type.equals(type))
            return object;

        return null;
    }

    /**
     * Puts an object in the clipboard.<br>
     * 
     * @param type
     *        object type (should not be null).
     * @param object
     *        object to save in clipboard.
     */
    public static void put(String type, Object object)
    {
        if (type == null)
            throw new IllegalArgumentException("Clipboard.put(type, object): type cannot be null !");

        Clipboard.type = type;
        Clipboard.object = object;

        // notify change
        fireChangedEvent();
    }

    public static void addListener(ClipboardListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public static void removeListener(ClipboardListener listener)
    {
        listeners.remove(listener);
    }

    public static void fireChangedEvent()
    {
        for (ClipboardListener l : listeners)
            l.clipboardChanged();
    }
}
