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
package icy.imagej.patches;

import icy.imagej.ImageJWrapper;
import icy.main.Icy;
import ij.ImageJ;
import ij.Menus;

/**
 * Overrides {@link Menus} methods.
 * 
 * @author Stephane Dallongeville
 */
@SuppressWarnings("unused")
public class MenusMethods
{
    private MenusMethods()
    {
        // prevent instantiation of utility class
    }

    /** Appends {@link Menus#installUserPlugin(String, boolean)}. */
    public static void installUserPlugin(final Menus obj, String className, boolean force)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#updateMenus()}. */
    public static void updateMenus()
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#updateWindowMenuItem(String, String)}. */
    public static synchronized void updateWindowMenuItem(String oldLabel, String newLabel)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#addOpenRecentItem(String)}. */
    public static synchronized void addOpenRecentItem(String path)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();
    }

    /** Appends {@link Menus#installPlugin(String, char, String, String, ImageJ)}. */
    public static int installPlugin(String plugin, char menuCode, String command, String shortcut, ImageJ ij, int result)
    {
        final ImageJWrapper ijw = Icy.getMainInterface().getImageJ();

        if (ijw != null)
            ijw.menuChanged();

        return result;
    }
}
