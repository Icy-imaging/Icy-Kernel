/**
 * 
 */
package icy.preferences;

/**
 * @author Stephane
 */
public class PluginOnlinePreferences
{
    private static final String PREF_ID = "online";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preference
        preferences = PluginPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

}
