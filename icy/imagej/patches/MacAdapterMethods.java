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


/**
 * @author Stephane
 */
public class MacAdapterMethods
{
    private MacAdapterMethods()
    {
        // prevent instantiation of utility class
    }

    /** Replaces {@link MacAdapter#handleAbout(com.apple.eawt.ApplicationEvent)}. */
    public static void handleAbout(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleOpenApplication(com.apple.eawt.ApplicationEvent)}. */
    public static void handleOpenApplication(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleOpenFile(com.apple.eawt.ApplicationEvent)}. */
    public static void handleOpenFile(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handlePreferences(com.apple.eawt.ApplicationEvent)}. */
    public static void handlePreferences(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handlePrintFile(com.apple.eawt.ApplicationEvent)}. */
    public static void handlePrintFile(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleQuit(com.apple.eawt.ApplicationEvent)}. */
    public static void handleQuit(final Object obj, Object event)
    {
        // do nothing
    }

    /** Replaces {@link MacAdapter#handleReOpenApplication(com.apple.eawt.ApplicationEvent)}. */
    public static void handleReOpenApplication(final Object obj, Object event)
    {
        // do nothing
    }
}
