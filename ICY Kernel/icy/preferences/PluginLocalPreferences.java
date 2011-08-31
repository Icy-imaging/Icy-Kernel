/**
 * 
 */
package icy.preferences;

/**
 * @author Stephane
 */
public class PluginLocalPreferences
{
    private static final String PREF_ID = "local";

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
