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
    private static final String ID_IRC_SERVER = "ircServer";
    private static final String ID_IRC_PORT = "ircPort";
    private static final String ID_IRC_CHANNEL = "ircChannel";

    private final static String DEFAULT_UPDATE_REPOSITORY_BASE = "http://www.bioimageanalysis.org/icy/update/";
    private final static String DEFAULT_UPDATE_REPOSITORY_FILE = "update.php";

    private final static String DEFAULT_IRC_SERVER = "irc.freenode.net";
    private final static int DEFAULT_IRC_PORT = 6666;
    private final static String DEFAULT_IRC_CHANNEL = "icy";

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

    public static String getIrcServer()
    {
        return preferences.get(ID_IRC_SERVER, DEFAULT_IRC_SERVER);
    }

    public static void setIrcServer(String value)
    {
        preferences.put(ID_IRC_SERVER, value);
    }

    public static int getIrcPort()
    {
        return preferences.getInt(ID_IRC_PORT, DEFAULT_IRC_PORT);
    }

    public static void setIrcPort(int value)
    {
        preferences.putInt(ID_IRC_PORT, value);
    }

    public static String getIrcChannel()
    {
        return preferences.get(ID_IRC_CHANNEL, DEFAULT_IRC_CHANNEL);
    }

    public static void setIrcChannel(String value)
    {
        preferences.put(ID_IRC_CHANNEL, value);
    }

}
