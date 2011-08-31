/**
 * 
 */
package icy.preferences;

import icy.network.AuthenticationInfo;
import icy.util.StringUtil;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class RepositoryPreferences
{
    public static class RepositoryInfo
    {
        private static final String ID_NAME = "name";
        private static final String ID_LOCATION = "location";
        private static final String ID_ENABLED = "enabled";
        private static final String ID_AUTHENTICATION = "authentication";

        private String name;
        private String location;
        private boolean enabled;
        private final AuthenticationInfo authInf;

        public RepositoryInfo(String name, String location, String login, String password, boolean authEnabled,
                boolean enabled)
        {
            super();

            this.name = name;
            this.location = location;
            this.enabled = enabled;
            authInf = new AuthenticationInfo(login, password, authEnabled);
        }

        public RepositoryInfo(String name, String location, boolean enabled)
        {
            this(name, location, "", "", false, enabled);
        }

        public RepositoryInfo(String name, String location)
        {
            this(name, location, "", "", false, true);
        }

        public RepositoryInfo(XMLPreferences node)
        {
            this("", "", "", "", false, false);

            load(node);
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(location);
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @param name
         *        the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * @return the location
         */
        public String getLocation()
        {
            return location;
        }

        /**
         * @param location
         *        the location to set
         */
        public void setLocation(String location)
        {
            this.location = location;
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

        /**
         * @return the login
         */
        public String getLogin()
        {
            return authInf.getLogin();
        }

        /**
         * @param login
         *        the login to set
         */
        public void setLogin(String login)
        {
            authInf.setLogin(login);
        }

        /**
         * @return the password
         */
        public String getPassword()
        {
            return authInf.getPassword();
        }

        /**
         * @param password
         *        the password to set
         */
        public void setPassword(String password)
        {
            authInf.setPassword(password);
        }

        /**
         * @return the authentication enabled
         */
        public boolean isAuthenticationEnabled()
        {
            return authInf.isEnabled();
        }

        /**
         * @param value
         *        the authentication enabled to set
         */
        public void setAuthenticationEnabled(boolean value)
        {
            authInf.setEnabled(value);
        }

        /**
         * @return the authentication informations
         */
        public AuthenticationInfo getAuthenticationInfo()
        {
            return authInf;
        }

        public void load(XMLPreferences node)
        {
            if (node != null)
            {
                name = node.get(ID_NAME, "");
                location = node.get(ID_LOCATION, "");
                enabled = node.getBoolean(ID_ENABLED, false);
                authInf.load(node.node(ID_AUTHENTICATION));
            }
        }

        public void save(XMLPreferences node)
        {
            if (node != null)
            {
                node.put(ID_NAME, name);
                node.put(ID_LOCATION, location);
                node.putBoolean(ID_ENABLED, enabled);
                authInf.save(node.node(ID_AUTHENTICATION));
            }
        }

        public boolean isDefault()
        {
            return RepositoryPreferences.DEFAULT_REPOSITERY_NAME.equals(name)
                    && RepositoryPreferences.DEFAULT_REPOSITERY_LOCATION.equals(location);
        }

        @Override
        public String toString()
        {
            return name + " - " + location;
        }
    }

    /**
     * pref id
     */
    private static final String PREF_ID = "repositories";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static final String DEFAULT_REPOSITERY_NAME = "default";
    public static final String DEFAULT_REPOSITERY_LOCATION = "http://www.bioimageanalysis.org/icy/repository/";

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

    public static ArrayList<RepositoryInfo> getRepositeries()
    {
        final ArrayList<RepositoryInfo> result = new ArrayList<RepositoryInfo>();
        // TODO : check why i modified to uncached access here
        // final Preferences preferences = Preferences.userRoot().node("icy/repositories");

        final ArrayList<String> childs = preferences.childrenNames();

        for (String child : childs)
        {
            final RepositoryInfo reposInf = new RepositoryInfo(preferences.node(child));

            if (!reposInf.isEmpty())
                result.add(reposInf);
        }

        // add default repository if no entry
        if (result.isEmpty())
            result.add(new RepositoryInfo(DEFAULT_REPOSITERY_NAME, DEFAULT_REPOSITERY_LOCATION, true));

        return result;
    }

    public static void setRepositeries(ArrayList<RepositoryInfo> values)
    {
        // remove all child nodes
        preferences.removeChildren();

        int i = 0;
        for (RepositoryInfo reposInf : values)
        {
            if (!reposInf.isEmpty())
                reposInf.save(preferences.node("repos" + i));

            i++;
        }

        // clean up all non element nodes
        preferences.clean();
    }
}
