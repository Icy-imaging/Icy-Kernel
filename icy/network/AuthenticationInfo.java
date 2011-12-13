/**
 * 
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
    public boolean equals(Object obj)
    {
        if (obj instanceof AuthenticationInfo)
        {
            final AuthenticationInfo auth = (AuthenticationInfo) obj;

            return (StringUtil.equals(auth.login, login) && StringUtil.equals(auth.password, password) && (auth.enabled == enabled));
        }

        return super.equals(obj);
    }
}
