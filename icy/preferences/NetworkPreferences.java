/*
 * Copyright 2010-2015 Institut Pasteur.
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

import icy.network.NetworkUtil;

/**
 * @author stephane
 */
public class NetworkPreferences
{
    /**
     * preferences id
     */
    private static final String PREF_ID = "network";

    /**
     * id
     */
    private static final String ID_PROXY_SETTING = "proxySetting";
    private static final String ID_PROXY_HTTP_HOST = "proxyHTTPHost";
    private static final String ID_PROXY_HTTP_PORT = "proxyHTTPPort";
    private static final String ID_PROXY_HTTPS_HOST = "proxyHTTPSHost";
    private static final String ID_PROXY_HTTPS_PORT = "proxyHTTPSPort";
    private static final String ID_PROXY_FTP_HOST = "proxyFTPHost";
    private static final String ID_PROXY_FTP_PORT = "proxyFTPPort";
    private static final String ID_PROXY_SOCKS_HOST = "proxySOCKSHost";
    private static final String ID_PROXY_SOCKS_PORT = "proxySOCKSPort";
    private static final String ID_PROXY_AUTHENTICATION = "proxyAuthentication";
    private static final String ID_PROXY_USER = "proxyUser";
    private static final String ID_PROXY_PASSWORD = "proxyPassword";

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

    public static int getProxySetting()
    {
        return preferences.getInt(ID_PROXY_SETTING, NetworkUtil.SYSTEM_PROXY);
    }

    public static void setProxySetting(int value)
    {
        preferences.putInt(ID_PROXY_SETTING, value);
    }

    public static String getProxyHTTPHost()
    {
        return preferences.get(ID_PROXY_HTTP_HOST, "");
    }

    public static void setProxyHTTPHost(String value)
    {
        preferences.put(ID_PROXY_HTTP_HOST, value);
    }

    public static int getProxyHTTPPort()
    {
        return preferences.getInt(ID_PROXY_HTTP_PORT, 80);
    }

    public static void setProxyHTTPPort(int value)
    {
        preferences.putInt(ID_PROXY_HTTP_PORT, value);
    }

    public static String getProxyHTTPSHost()
    {
        return preferences.get(ID_PROXY_HTTPS_HOST, "");
    }

    public static void setProxyHTTPSHost(String value)
    {
        preferences.put(ID_PROXY_HTTPS_HOST, value);
    }

    public static int getProxyHTTPSPort()
    {
        return preferences.getInt(ID_PROXY_HTTPS_PORT, 447);
    }

    public static void setProxyHTTPSPort(int value)
    {
        preferences.putInt(ID_PROXY_HTTPS_PORT, value);
    }

    public static String getProxyFTPHost()
    {
        return preferences.get(ID_PROXY_FTP_HOST, "");
    }

    public static void setProxyFTPHost(String value)
    {
        preferences.put(ID_PROXY_FTP_HOST, value);
    }

    public static int getProxyFTPPort()
    {
        return preferences.getInt(ID_PROXY_FTP_PORT, 21);
    }

    public static void setProxyFTPPort(int value)
    {
        preferences.putInt(ID_PROXY_FTP_PORT, value);
    }

    public static String getProxySOCKSHost()
    {
        return preferences.get(ID_PROXY_SOCKS_HOST, "");
    }

    public static void setProxySOCKSHost(String value)
    {
        preferences.put(ID_PROXY_SOCKS_HOST, value);
    }

    public static int getProxySOCKSPort()
    {
        return preferences.getInt(ID_PROXY_SOCKS_PORT, 1080);
    }

    public static void setProxySOCKSPort(int value)
    {
        preferences.putInt(ID_PROXY_SOCKS_PORT, value);
    }

    public static boolean getProxyAuthentication()
    {
        return preferences.getBoolean(ID_PROXY_AUTHENTICATION, false);
    }

    public static void setProxyAuthentication(boolean value)
    {
        preferences.putBoolean(ID_PROXY_AUTHENTICATION, value);
    }

    public static String getProxyUser()
    {
        return preferences.get(ID_PROXY_USER, "");
    }

    public static void setProxyUser(String value)
    {
        preferences.put(ID_PROXY_USER, value);
    }

    public static String getProxyPassword()
    {
        return preferences.get(ID_PROXY_PASSWORD, "");
    }

    public static void setProxyPassword(String value)
    {
        preferences.put(ID_PROXY_PASSWORD, value);
    }
}
