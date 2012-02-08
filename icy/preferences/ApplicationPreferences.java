/**
 * 
 */
package icy.preferences;

/**
 * @author Stephane
 */
public class ApplicationPreferences
{
    /**
     * id
     */
    private static final String PREF_ID = "icy";

    private static final String ID_OS = "os";
    private static final String ID_UPDATE_REPOSITORY_BASE = "updateRepositoryBase";
    private static final String ID_UPDATE_REPOSITORY_FILE = "updateRepositoryFile";

    private final static String DEFAULT_UPDATE_REPOSITORY_BASE = "http://www.bioimageanalysis.org/icy/update/";
    private final static String DEFAULT_UPDATE_REPOSITORY_FILE = "update.php";

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

    public static String getOs()
    {
        return preferences.get(ID_OS, "");
    }

    public static void setOs(String value)
    {
        preferences.put(ID_OS, value);
    }

    public static String getUpdateRepositoryBase()
    {
        return preferences.get(ID_UPDATE_REPOSITORY_BASE, DEFAULT_UPDATE_REPOSITORY_BASE);
    }

    public static void setUpdateRepositoryBase(String value)
    {
        preferences.put(ID_UPDATE_REPOSITORY_BASE, value);
    }

    public static String getUpdateRepositoryFile()
    {
        return preferences.get(ID_UPDATE_REPOSITORY_FILE, DEFAULT_UPDATE_REPOSITORY_FILE);
    }

    public static void setUpdateRepositoryFile(String value)
    {
        preferences.put(ID_UPDATE_REPOSITORY_FILE, value);
    }
}
