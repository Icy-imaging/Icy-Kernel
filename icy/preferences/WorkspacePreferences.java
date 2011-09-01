/**
 * 
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
    private static final String ID_AUTO_UPDATE = "autoUpdate";
    private static final String ID_AUTO_CHECK_UPDATE = "autoCheckUpdate";

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

    public static boolean getAutomaticCheckUpdate()
    {
        return preferences.getBoolean(ID_AUTO_CHECK_UPDATE, true);
    }

    public static void setAutomaticUpdate(boolean value)
    {
        preferences.putBoolean(ID_AUTO_UPDATE, value);
    }

    public static void setAutomaticCheckUpdate(boolean value)
    {
        preferences.putBoolean(ID_AUTO_CHECK_UPDATE, value);
    }

}
