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
package icy.network;

import icy.preferences.XMLPreferences;
import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class AuthenticationInfo
{
    private static final String ID_LOGIN = "login";
    private static final String ID_PASSWORD = "password";
    private static final String ID_ENABLED = "enabled";

    private String login;
    private String password;
    private boolean enabled;

    public AuthenticationInfo(String login, String password, boolean enabled)
    {
        super();

        this.login = login;
        this.password = password;
        this.enabled = enabled;
    }

    public AuthenticationInfo(XMLPreferences node)
    {
        this("", "", false);

        load(node);
    }

    /**
     * @return the login
     */
    public String getLogin()
    {
        return login;
    }

    /**
     * @param login
     *        the login to set
     */
    public void setLogin(String login)
    {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *        the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled
     *        the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void save(XMLPreferences node)
    {
        if (node != null)
        {
            node.put(ID_LOGIN, login);
            node.put(ID_PASSWORD, password);
            node.putBoolean(ID_ENABLED, enabled);
        }
    }

    public void load(XMLPreferences node)
    {
        if (node != null)
        {
            login = node.get(ID_LOGIN, "");
            password = node.get(ID_PASSWORD, "");
            enabled = node.getBoolean(ID_ENABLED, false);
        }
    }

    @Override
    public int hashCode()
    {
        return login.hashCode() ^ password.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof AuthenticationInfo)
        {
            final AuthenticationInfo auth = (AuthenticationInfo) obj;

            return StringUtil.equals(auth.login, login) && StringUtil.equals(auth.password, password);
        }

        return super.equals(obj);
    }
}
