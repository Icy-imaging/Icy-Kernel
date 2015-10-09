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
package icy.preferences;

/**
 * @author Stephane
 */
public class CanvasPreferences
{
    /**
     * preferences id
     */
    private static final String PREF_ID = "canvas";

    /**
     * id
     */
    private static final String ID_FILTERING = "filtering";
    private static final String ID_INVERT_MOUSEWHEEL_AXIS = "invertMouseWheelAxis";
    private static final String ID_MOUSEWHEEL_SENSIBILITY = "mouseWheelSensibility";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preferences
        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    public static boolean getFiltering()
    {
        return preferences.getBoolean(ID_FILTERING, false);
    }

    public static void setFiltering(boolean value)
    {
        preferences.putBoolean(ID_FILTERING, value);
    }

    public static boolean getInvertMouseWheelAxis()
    {
        return preferences.getBoolean(ID_INVERT_MOUSEWHEEL_AXIS, false);
    }

    public static void setInvertMouseWheelAxis(boolean value)
    {
        preferences.putBoolean(ID_INVERT_MOUSEWHEEL_AXIS, value);
    }

    /**
     * Mouse wheel sensitivity (1-10)
     */
    public static double getMouseWheelSensitivity()
    {
        return preferences.getDouble(ID_MOUSEWHEEL_SENSIBILITY, 5d);
    }

    public static void setMouseWheelSensitivity(double value)
    {
        preferences.putDouble(ID_MOUSEWHEEL_SENSIBILITY, value);
    }

}
