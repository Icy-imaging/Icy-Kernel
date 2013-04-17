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

import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences.XMLPreferencesRoot;

/**
 * Global class for Preferences
 * 
 * @author Stephane
 */
public class IcyPreferences
{
    private static final String DEFAULT_NAME = "setting.xml";

    // load from default setting file
    private static final XMLPreferencesRoot root = new XMLPreferencesRoot(DEFAULT_NAME);

    public static void init()
    {
        // load preferences
        load();
    }

    public static void load()
    {
        // load root first
        ApplicationPreferences.load();
        // then load others
        GeneralPreferences.load();
        CanvasPreferences.load();
        NetworkPreferences.load();
        ChatPreferences.load();
        RepositoryPreferences.load();
        // load plugin before pluginLocal and pluginOnline
        PluginPreferences.load();
        PluginLocalPreferences.load();
        PluginOnlinePreferences.load();
        // load workspace before workspaceLocal and workspaceOnline
        WorkspacePreferences.load();
        WorkspaceLocalPreferences.load();
        WorkspaceOnlinePreferences.load();
        PluginsPreferences.load();
    }

    public static void save()
    {
        // save to setting file
        root.save();
    }

    public static void clear()
    {
        // removing all from root node is sufficient
        root.getPreferences().clear();
        root.getPreferences().removeChildren();
        root.getPreferences().clean();

        // reload
        load();
    }

    /**
     * Get absolute root
     */
    public static XMLPreferences root()
    {
        return root.getPreferences();
    }

    /**
     * Get application root
     */
    public static XMLPreferences applicationRoot()
    {
        return ApplicationPreferences.getPreferences();
    }

    /**
     * @deprecated Use {@link PluginsPreferences#root(Plugin)} instead.
     */
    @Deprecated
    public static XMLPreferences pluginRoot(Plugin plugin)
    {
        return PluginsPreferences.root(plugin);
    }

    /**
     * @deprecated Use {@link PluginsPreferences#getPreferences()} instead.
     */
    @Deprecated
    public static XMLPreferences pluginsRoot()
    {
        return PluginsPreferences.getPreferences();
    }

}
