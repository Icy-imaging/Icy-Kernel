/**
 * 
 */
package icy.preferences;

/**
 * @author Stephane
 */
public class WorkspaceOnlinePreferences
{
    private static final String PREF_ID = "online";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        preferences = WorkspacePreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }
}
