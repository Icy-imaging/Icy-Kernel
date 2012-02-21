/**
 * 
 */
package icy.preferences;

import icy.util.Random;

/**
 * @author Stephane
 */
public class ChatPreferences
{
    /**
     * preferences id
     */
    private static final String PREF_ID = "chat";

    /**
     * id
     */
    private static final String ID_SERVER = "server";
    private static final String ID_SERVER_PASSWORD = "serverPassword";
    private static final String ID_PORT = "port";
    private static final String ID_EXTRA_CHANNELS = "extraChannels";
    private static final String ID_DESKTOP_CHANNELS = "desktopChannels";

    private static final String ID_NICKNAME = "nickname";
    private static final String ID_REALNAME = "realname";
    private static final String ID_USER_PASSWORD = "userPassword";

    private static final String ID_AUTO_CONNECT = "autoConnect";
    private static final String ID_SHOW_USERS_PANEL = "showUsersPanel";
    private static final String ID_DESKTOP_OVERLAY = "desktopOverlay";
    private static final String ID_USERS_PANEL_WIDTH = "usersPanelWidth";

    /**
     * defaults values
     */
    private final static String DEFAULT_SERVER = "irc.freenode.net";
    private final static String DEFAULT_SERVER_PASSWORD = "";
    private final static int DEFAULT_PORT = 6666;

    private final static String DEFAULT_EXTRA_CHANNELS = "#icy-support";
    private final static String DEFAULT_DESKTOP_CHANNELS = "#icy";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preferences
        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    public static String getServer()
    {
        return preferences.get(ID_SERVER, DEFAULT_SERVER);
    }

    public static void setServer(String value)
    {
        preferences.put(ID_SERVER, value);
    }

    public static String getServerPassword()
    {
        return preferences.get(ID_SERVER_PASSWORD, DEFAULT_SERVER_PASSWORD);
    }

    public static void setServerPassword(String value)
    {
        preferences.put(ID_SERVER_PASSWORD, value);
    }

    public static int getPort()
    {
        return preferences.getInt(ID_PORT, DEFAULT_PORT);
    }

    public static void setPort(int value)
    {
        preferences.putInt(ID_PORT, value);
    }

    public static String getDefaultExtraChannels()
    {
        return DEFAULT_EXTRA_CHANNELS;
    }

    public static String getExtraChannels()
    {
        return preferences.get(ID_EXTRA_CHANNELS, getDefaultExtraChannels());
    }

    public static void setExtraChannels(String value)
    {
        preferences.put(ID_EXTRA_CHANNELS, value);
    }

    public static String getDefaultDesktopChannels()
    {
        return DEFAULT_DESKTOP_CHANNELS;
    }

    public static String getDesktopChannels()
    {
        return preferences.get(ID_DESKTOP_CHANNELS, getDefaultDesktopChannels());
    }

    public static void setDesktopChannels(String value)
    {
        preferences.put(ID_DESKTOP_CHANNELS, value);
    }

    public static String getRandomNickname()
    {
        return "guest" + Random.nextInt(10000);
    }

    public static String getNickname()
    {
        return preferences.get(ID_NICKNAME, getRandomNickname());
    }

    public static String getRealname()
    {
        return preferences.get(ID_REALNAME, getNickname());
    }

    public static String getUserPassword()
    {
        return preferences.get(ID_USER_PASSWORD, "");
    }

    public static boolean getAutoConnect()
    {
        return preferences.getBoolean(ID_AUTO_CONNECT, true);
    }

    public static boolean getShowUsersPanel()
    {
        return preferences.getBoolean(ID_SHOW_USERS_PANEL, false);
    }

    public static boolean getDesktopOverlay()
    {
        return preferences.getBoolean(ID_DESKTOP_OVERLAY, true);
    }

    public static int getUsersPanelWidth()
    {
        return preferences.getInt(ID_USERS_PANEL_WIDTH, 120);
    }

    public static void setNickname(String value)
    {
        preferences.put(ID_NICKNAME, value);
    }

    public static void setRealname(String value)
    {
        preferences.put(ID_REALNAME, value);
    }

    public static void setUserPassword(String value)
    {
        preferences.put(ID_USER_PASSWORD, value);
    }

    public static void setAutoConnect(boolean value)
    {
        preferences.putBoolean(ID_AUTO_CONNECT, value);
    }

    public static void setShowUsersPanel(boolean value)
    {
        preferences.putBoolean(ID_SHOW_USERS_PANEL, value);
    }

    public static void setDesktopOverlay(boolean value)
    {
        preferences.putBoolean(ID_DESKTOP_OVERLAY, value);
    }

    public static void setUsersPanelWidth(int value)
    {
        preferences.putInt(ID_USERS_PANEL_WIDTH, value);
    }
}
