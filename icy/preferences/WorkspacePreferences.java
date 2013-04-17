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
package icy.preferences;

/**
 * @author Stephane
 */
public class WorkspacePreferences
{
    /**
     * pref id
     */
    private static final String PREF_ID = "workspaces";

    /**
     * id
     */
    // private static final String ID_AUTO_UPDATE = "autoUpdate";
    // private static final String ID_AUTO_CHECK_UPDATE = "autoCheckUpdate";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preference
        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    // public static boolean getAutomaticUpdate()
    // {
    // return preferences.getBoolean(ID_AUTO_UPDATE, true);
    // }
    //
    // public static boolean getAutomaticCheckUpdate()
    // {
    // return preferences.getBoolean(ID_AUTO_CHECK_UPDATE, true);
    // }
    //
    // public static void setAutomaticUpdate(boolean value)
    // {
    // preferences.putBoolean(ID_AUTO_UPDATE, value);
    // }
    //
    // public static void setAutomaticCheckUpdate(boolean value)
    // {
    // preferences.putBoolean(ID_AUTO_CHECK_UPDATE, value);
    // }

}
