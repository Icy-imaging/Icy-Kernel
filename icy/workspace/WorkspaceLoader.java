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
import icy.file.FileUtil;
import icy.system.thread.ThreadUtil;
import icy.workspace.WorkspaceLoader.WorkspaceLoaderEvent.WorkspaceLoaderEventType;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * @author Stephane
 */
public class WorkspaceLoader
{
    public static interface WorkspaceLoaderListener extends EventListener
    {
        public void workspaceLoaderChanged(WorkspaceLoaderEvent e);
    }

    public static class WorkspaceLoaderEvent implements EventHierarchicalChecker
    {
        public enum WorkspaceLoaderEventType
        {
            RELOADED
        }

        private final WorkspaceLoaderEventType type;

        /**
         * @param type
         */
        public WorkspaceLoaderEvent(WorkspaceLoaderEventType type)
        {
            super();

            this.type = type;
        }

        /**
         * @return the type
         */
        public WorkspaceLoaderEventType getType()
        {
            return type;
        }

        @Override
        public boolean isEventRedundantWith(EventHierarchicalChecker event)
        {
            return (event instanceof WorkspaceLoaderEvent) && (type == ((WorkspaceLoaderEvent) event).getType());
        }
    }

    public final static String WORKSPACE_PATH = "workspace";
    public final static String EXT = ".xml";

    /**
     * static class
     */
    private static final WorkspaceLoader loader = new WorkspaceLoader();
    private static boolean initialized = false;

    /**
     * Loaded workspace list
     */
    private final ArrayList<Workspace> workspaces;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal
     */
    private final Runnable reloader;
    private boolean loading;

    /**
     * @param path
     */
    private WorkspaceLoader()
    {
        super();

        workspaces = new ArrayList<Workspace>();
        listeners = new EventListenerList();
        reloader = new Runnable()
        {
            @Override
            public void run()
            {
                internal_reload();
            }
        };
        loading = false;
    }

    public static void prepare()
    {
        if (!initialized)
        {
            if (isLoading())
                waitWhileLoading();
            else
                reload();
        }
    }

    /**
     * Reload the list of installed workspaces (workspaces present in the "workspaces" directory)<br>
     * Asynchronous version
     */
    public static void reload_asynch()
    {
        loader.loading = true;

        ThreadUtil.bgRunSingle(loader.reloader);
    }

    /**
     * Reload the list of installed workspaces (workspaces present in the "workspaces" directory)
     */
    public static void reload()
    {
        loader.internal_reload();
    }

    /**
     * Reload the list of installed workspaces (workspaces present in the "workspaces" directory)
     */
    void internal_reload()
    {
        loading = true;

        synchronized (workspaces)
        {
            // reset list
            workspaces.clear();

            final ArrayList<File> files = FileUtil.getFileList(WORKSPACE_PATH, new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    // only accept xml file
                    return FileUtil.getFileExtension(pathname.getPath(), true).toLowerCase().equals(EXT);
                }
            }, true, false);

            for (File file : files)
            {
                final Workspace workspace = new Workspace(file);

                // don't load the specific system workspace
                if (!workspace.getName().equals(Workspace.WORKSPACE_SYSTEM_NAME))
                {
                    // empty workspace ?
                    if (workspace.isEmpty())
                    {
                        // don't show this message for default workspace
                        // if (!workspace.getName().equals(Workspace.WORKSPACE_DEFAULT_NAME))
                        System.err.println("Empty workspace '" + workspace.getName() + "' is not loaded");
                    }
                    else
                        workspaces.add(workspace);
                }
            }

            // sort list
            Collections.sort(workspaces);
        }

        // notify change
        changed();
    }

    /**
     * @return the workspaceList
     */
    public static ArrayList<Workspace> getWorkspaces()
    {
        prepare();

        synchronized (loader.workspaces)
        {
            // better to return a copy as we have async list loading
            return new ArrayList<Workspace>(loader.workspaces);
        }
    }

    /**
     * @return the loading flag
     */
    public static boolean isLoading()
    {
        return loader.loading;
    }

    /**
     * wait until loading completed
     */
    public static void waitWhileLoading()
    {
        while (isLoading())
            ThreadUtil.sleep(10);
    }

    public static boolean isLoaded(Workspace workspace)
    {
        return (getWorkspace(workspace.getName()) != null);
    }

    public static Workspace getWorkspace(String name)
    {
        prepare();

        return Workspace.getWorkspace(getWorkspaces(), name);
    }

    /**
     * 
     */
    private void changed()
    {
        initialized = true;
        loading = false;

        // workspace list has changed
        fireEvent(new WorkspaceLoaderEvent(WorkspaceLoaderEventType.RELOADED));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public static void addListener(WorkspaceLoaderListener listener)
    {
        synchronized (loader.listeners)
        {
            loader.listeners.add(WorkspaceLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(WorkspaceLoaderListener listener)
    {
        synchronized (loader.listeners)
        {
            loader.listeners.remove(WorkspaceLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent(WorkspaceLoaderEvent e)
    {
        for (WorkspaceLoaderListener listener : listeners.getListeners(WorkspaceLoaderListener.class))
            listener.workspaceLoaderChanged(e);
    }
}
