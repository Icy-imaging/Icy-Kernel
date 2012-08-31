/**
 * 
 */
package icy.preferences;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class PluginPreferences
{
    /**
     * pref id
     */
    private static final String PREF_ID = "plugin";

    /**
     * id
     */
    private static final String ID_ALLOW_BETA = "allowBeta";
    private static final String ID_AUTO_UPDATE = "autoUpdate";
    private static final String ID_INACTIVES_DAEMON = "inactivesdaemon";

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

    public static boolean getAutomaticUpdate()
    {
        return preferences.getBoolean(ID_AUTO_UPDATE, true);
    }

    public static boolean getAllowBeta()
    {
        return preferences.getBoolean(ID_ALLOW_BETA, false);
    }

    public static ArrayList<String> getInactiveDaemons()
    {
        final ArrayList<String> result = new ArrayList<String>();

        if (preferences.nodeExists(ID_INACTIVES_DAEMON))
        {
            final XMLPreferences node = preferences.node(ID_INACTIVES_DAEMON);

            for (String name : node.keys())
                if (node.getBoolean(name, false))
                    result.add(name);
        }

        return result;
    }

    public static void setAutomaticUpdate(boolean value)
    {
        preferences.putBoolean(ID_AUTO_UPDATE, value);
    }

    public static void setAllowBeta(boolean value)
    {
        preferences.putBoolean(ID_ALLOW_BETA, value);
    }

    public static void setInactiveDaemons(ArrayList<String> names)
    {
        final ArrayList<String> inactives = getInactiveDaemons();

        // no modification --> nothing to do
        if ((inactives.size() == names.size()) && inactives.containsAll(names))
            return;

        final XMLPreferences node = preferences.node(ID_INACTIVES_DAEMON);

        node.clear();
        for (String name : names)
            node.putBoolean(name, true);

        // clean up all non element nodes
        node.clean();
    }

}
