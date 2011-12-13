/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.workspace;

import icy.network.URLUtil;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class WorkspaceRepositoryLoader
{
    public static interface WorkspaceRepositoryLoaderListener extends EventListener
    {
        public void workspaceRepositeryLoaderChanged();
    }

    private class LoadRunner implements Runnable
    {
        @Override
        public void run()
        {
            // wait loading is interrupted
            while (isLoading())
            {
                // request stop
                interruptLoading = true;
                // wait
                ThreadUtil.sleep(10);
            }

            interruptLoading = false;
            loading = true;
            try
            {
                clear();

                final ArrayList<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

                // load online workspace from all active repositories
                for (RepositoryInfo repoInfo : repositories)
                {
                    if (interruptLoading)
                        return;
                    if (repoInfo.isEnabled())
                        loadInternal(repoInfo);
                }

                // sort list
                Collections.sort(workspaces);
            }
            finally
            {
                loading = false;
            }

            changed();
        }
    }

    private static final String ID_ROOT = "workspaces";
    private static final String ID_WORKSPACE = "workspace";
    private static final String ID_PATH = "path";

    /**
     * static class
     */
    private static WorkspaceRepositoryLoader instance = new WorkspaceRepositoryLoader();

    /**
     * Online workspace list
     */
    private final ArrayList<Workspace> workspaces;

    /**
     * internal
     */
    private boolean loading;
    private boolean interruptLoading;

    private final LoadRunner loadRunner;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * static class
     */
    private WorkspaceRepositoryLoader()
    {
        super();

        workspaces = new ArrayList<Workspace>();
        listeners = new EventListenerList();
        loading = false;
        interruptLoading = false;

        loadRunner = new LoadRunner();
    }

    /**
     * Return the workspaces file list from a repository URL
     */
    public static ArrayList<String> getWorkspaceFiles(RepositoryInfo repos)
    {
        final ArrayList<String> result = new ArrayList<String>();
        final Document document = XMLUtil.loadDocument(repos.getLocation(), repos.getAuthenticationInfo(), true);

        if (document != null)
        {
            // get workspaces node
            final Node workspacesNode = XMLUtil.getElement(document.getDocumentElement(), ID_ROOT);

            // workspaces node found
            if (workspacesNode != null)
            {
                final ArrayList<Node> nodes = XMLUtil.getSubNodes(workspacesNode, ID_WORKSPACE);

                for (Node node : nodes)
                {
                    final String path = XMLUtil.getElementValue(node, ID_PATH, "");

                    if (!StringUtil.isEmpty(path))
                        result.add(path);
                }
            }
        }
        else
            System.err.println("Can't connect to repository '" + repos.getName() + "'");

        return result;
    }

    /**
     * clear workspace list
     */
    public static void clear()
    {
        // reset list
        instance.workspaces.clear();
    }

    /**
     * Reload all online workspaces from all active repositories (old list is cleared)
     */
    public static void reload()
    {
        ThreadUtil.bgRunSingle(instance.loadRunner);
    }

    /**
     * Load the list of online workspaces located at specified repository url
     */
    void loadInternal(RepositoryInfo repos)
    {
        final ArrayList<String> paths = getWorkspaceFiles(repos);

        for (String path : paths)
        {
            try
            {
                final Workspace workspace = new Workspace(URLUtil.getURL(path), repos);

                if (!workspace.isEmpty())
                    workspaces.add(workspace);
            }
            catch (Exception e)
            {
                System.err.println("WorkspaceRepositoryLoader.load('" + repos.getLocation() + "') error :");
                IcyExceptionHandler.showErrorMessage(e, false);
            }
        }
    }

    /**
     * @return the workspaceList
     */
    public static ArrayList<Workspace> getWorkspaces()
    {
        return new ArrayList<Workspace>(instance.workspaces);
    }

    public static Workspace getWorkspace(String className)
    {
        return Workspace.getWorkspace(instance.workspaces, className);
    }

    /**
     * Return the workspace list from the specified repository
     */
    public static ArrayList<Workspace> getWorkspaces(RepositoryInfo repos)
    {
        final ArrayList<Workspace> result = new ArrayList<Workspace>();

        synchronized (instance.workspaces)
        {
            for (Workspace workspace : instance.workspaces)
                if (workspace.getRepository().equals(repos))
                    result.add(workspace);
        }

        return result;
    }

    /**
     * @return the loaded
     */
    public static boolean isLoading()
    {
        return instance.loading;
    }

    /**
     * wait until loading completed
     */
    public static void waitWhileLoading()
    {
        while (isLoading())
            ThreadUtil.sleep(10);
    }

    /**
     * workspace list has changed
     */
    private void changed()
    {
        fireEvent();
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public static void addListener(WorkspaceRepositoryLoaderListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.add(WorkspaceRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(WorkspaceRepositoryLoaderListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.remove(WorkspaceRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent()
    {
        for (WorkspaceRepositoryLoaderListener listener : listeners
                .getListeners(WorkspaceRepositoryLoaderListener.class))
            listener.workspaceRepositeryLoaderChanged();
    }

}
