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

import icy.common.EventHierarchicalChecker;
import icy.network.URLUtil;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.workspace.WorkspaceRepositoryLoader.WorkspaceRepositoryLoaderEvent.WorkspaceRepositeryLoaderEventType;

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
        public void workspaceRepositeryLoaderChanged(WorkspaceRepositoryLoaderEvent e);
    }

    public static class WorkspaceRepositoryLoaderEvent implements EventHierarchicalChecker
    {
        public enum WorkspaceRepositeryLoaderEventType
        {
            CHANGED, LOAD_COMPLETED
        }

        private final WorkspaceRepositeryLoaderEventType type;
        private final RepositoryInfo repos;

        /**
         * @param type
         */
        public WorkspaceRepositoryLoaderEvent(WorkspaceRepositeryLoaderEventType type, RepositoryInfo repos)
        {
            super();

            this.type = type;
            this.repos = repos;
        }

        /**
         * @return the type
         */
        public WorkspaceRepositeryLoaderEventType getType()
        {
            return type;
        }

        /**
         * @return the repos
         */
        public RepositoryInfo getRepos()
        {
            return repos;
        }

        @Override
        public boolean isEventRedundantWith(EventHierarchicalChecker event)
        {
            return (event instanceof WorkspaceRepositoryLoaderEvent)
                    && (type == ((WorkspaceRepositoryLoaderEvent) event).getType());
        }
    }

    private static final String ID_ROOT = "workspaces";
    private static final String ID_WORKSPACE = "workspace";
    private static final String ID_PATH = "path";

    /**
     * Online workspace list
     */
    private final ArrayList<Workspace> workspaces;

    /**
     * internal
     */
    private boolean loading;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * static class
     */
    public WorkspaceRepositoryLoader()
    {
        super();

        workspaces = new ArrayList<Workspace>();
        listeners = new EventListenerList();
        loading = false;
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
     * Load the list of online workspaces located at specified repository url
     */
    public void load(final RepositoryInfo repos, boolean asynch)
    {
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                load(repos);
            }
        };

        if (asynch)
            ThreadUtil.bgRunSingle(runnable);
        else
            runnable.run();
    }

    /**
     * clear workspace list
     */
    public void clear()
    {
        // reset list
        workspaces.clear();
    }

    /**
     * Load the list of online workspaces located at specified repository url
     */
    void load(RepositoryInfo repos)
    {
        loading = true;
        try
        {

            final ArrayList<String> paths = getWorkspaceFiles(repos);

            for (String path : paths)
            {
                try
                {
                    final Workspace workspace = new Workspace(URLUtil.getURL(path));

                    if (!workspace.isEmpty())
                    {
                        workspaces.add(workspace);
                        // notify change
                        changed(repos);
                    }

                }
                catch (Exception e)
                {
                    System.err.println("WorkspaceRepositoryLoader.load('" + repos.getLocation() + "') error :");
                    IcyExceptionHandler.showErrorMessage(e, false);
                }
            }

            // sort list
            Collections.sort(workspaces);
        }
        finally
        {
            loading = false;
        }

        // notify load done
        load_completed(repos);
    }

    /**
     * @return the workspaceList
     */
    public ArrayList<Workspace> getWorkspaces()
    {
        return new ArrayList<Workspace>(workspaces);
    }

    public Workspace getWorkspace(String className)
    {
        return Workspace.getWorkspace(workspaces, className);
    }

    /**
     * @return the loaded
     */
    public boolean isLoading()
    {
        return loading;
    }

    /**
     * wait until loading completed
     */
    public void waitWhileLoading()
    {
        while (isLoading())
            ThreadUtil.sleep(10);
    }

    /**
     * workspace list load completed
     */
    private void load_completed(RepositoryInfo repos)
    {
        fireEvent(new WorkspaceRepositoryLoaderEvent(WorkspaceRepositeryLoaderEventType.LOAD_COMPLETED, repos));
    }

    /**
     * workspace list has changed
     */
    private void changed(RepositoryInfo repos)
    {
        fireEvent(new WorkspaceRepositoryLoaderEvent(WorkspaceRepositeryLoaderEventType.CHANGED, repos));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(WorkspaceRepositoryLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(WorkspaceRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(WorkspaceRepositoryLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(WorkspaceRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent(WorkspaceRepositoryLoaderEvent e)
    {
        for (WorkspaceRepositoryLoaderListener listener : listeners
                .getListeners(WorkspaceRepositoryLoaderListener.class))
            listener.workspaceRepositeryLoaderChanged(e);
    }

}
