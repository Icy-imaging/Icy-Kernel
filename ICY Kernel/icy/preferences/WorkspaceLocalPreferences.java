/**
 * 
 */
package icy.preferences;

import icy.workspace.Workspace;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class WorkspaceLocalPreferences
{
    /**
     * pref id
     */
    private static final String PREF_ID = "local";

    /**
     * id
     */
    private static final String ID_ACTIVES = "actives";

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

    public static ArrayList<String> getActivesWorkspace()
    {
        final ArrayList<String> result = new ArrayList<String>();

        if (preferences.nodeExists(ID_ACTIVES))
        {
            final XMLPreferences activesNode = preferences.node(ID_ACTIVES);

            for (String name : activesNode.keys())
                if (activesNode.getBoolean(name, false))
                    result.add(name);
        }
        else
        {
            // default workspaces
            for (String workspaceName : Workspace.DEFAULT_ACTIVE_WORKSPACES)
                result.add(workspaceName);
        }

        return result;
    }

    public static void setActivesWorkspace(ArrayList<String> names)
    {
        final XMLPreferences activesNode = preferences.node(ID_ACTIVES);

        activesNode.clear();
        for (String name : names)
            activesNode.putBoolean(name, true);

        // clean up all non element nodes
        activesNode.clean();
    }

    public static boolean isWorkspaceEnable(String workspaceName)
    {
        return getActivesWorkspace().contains(workspaceName);
    }

    public static void setWorkspaceEnable(String workspaceName, boolean value)
    {
        final ArrayList<String> activesWorkspace = getActivesWorkspace();

        if (value)
        {
            if (!activesWorkspace.contains(workspaceName))
                activesWorkspace.add(workspaceName);
        }
        else
            activesWorkspace.remove(workspaceName);

        setActivesWorkspace(activesWorkspace);
    }

}
