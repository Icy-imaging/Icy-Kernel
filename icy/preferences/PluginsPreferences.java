/**
 * 
 */
package icy.preferences;

import icy.plugin.PluginLoader;
import icy.plugin.abstract_.Plugin;
import icy.util.ClassUtil;

/**
 * @author Stephane
 */
public class PluginsPreferences
{
    /**
     * pref id
     */
    private static final String PREF_ID = "plugins";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preference
        preferences = IcyPreferences.root().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    /**
     * Return root node for specified Plugin class.
     */
    public static XMLPreferences root(Class<? extends Plugin> pluginClass)
    {
        if (pluginClass != null)
        {
            final String className = pluginClass.getName();

            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className
                        .substring(PluginLoader.PLUGIN_PACKAGE.length() + 1)));
        }

        return null;
    }

    /**
     * Return root node for specified Plugin
     */
    public static XMLPreferences root(Plugin plugin)
    {
        if (plugin != null)
        {
            final String className = plugin.getClass().getName();

            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className
                        .substring(PluginLoader.PLUGIN_PACKAGE.length() + 1)));
        }

        return null;
    }
}
