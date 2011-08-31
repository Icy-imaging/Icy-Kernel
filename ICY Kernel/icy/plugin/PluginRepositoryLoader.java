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
package icy.plugin;

import icy.common.EventHierarchicalChecker;
import icy.plugin.PluginDescriptor.PluginClassNameSorter;
import icy.plugin.PluginDescriptor.PluginNameSorter;
import icy.plugin.PluginDescriptor.PluginOnlineIdent;
import icy.preferences.PluginPreferences;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.IcyExceptionHandler;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.XMLUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class PluginRepositoryLoader
{
    public static interface PluginRepositoryLoaderListener extends EventListener
    {
        public void pluginRepositeryLoaderChanged(PluginRepositoryLoaderEvent e);
    }

    public static class PluginRepositoryLoaderEvent implements EventHierarchicalChecker
    {
        private final RepositoryInfo repos;

        public PluginRepositoryLoaderEvent(RepositoryInfo repos)
        {
            super();

            this.repos = repos;
        }

        /**
         * @return the repository
         */
        public RepositoryInfo getRepos()
        {
            return repos;
        }

        @Override
        public boolean isEventRedundantWith(EventHierarchicalChecker event)
        {
            return (event instanceof PluginRepositoryLoaderEvent)
                    && (repos == ((PluginRepositoryLoaderEvent) event).getRepos());
        }
    }

    private static final String ID_ROOT = "plugins";
    private static final String ID_PLUGIN = "plugin";
    // private static final String ID_PATH = "path";

    /**
     * Online plugin list
     */
    private final ArrayList<PluginDescriptor> plugins;

    /**
     * internal
     */
    private final SingleProcessor singleProcessor;
    private boolean loading;
    private boolean loadingDescriptors;
    private boolean interruptLoadDescriptors;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * static class
     */
    public PluginRepositoryLoader()
    {
        super();

        plugins = new ArrayList<PluginDescriptor>();
        listeners = new EventListenerList();
        singleProcessor = new SingleProcessor(true);
        loading = false;
        loadingDescriptors = false;
        interruptLoadDescriptors = false;
    }

    /**
     * Return the plugins ident list from a repository URL
     */
    public static ArrayList<PluginOnlineIdent> getPluginIdents(RepositoryInfo repos)
    {
        final Document document = XMLUtil.loadDocument(repos.getLocation(), repos.getAuthenticationInfo(), true);

        if (document != null)
        {
            final ArrayList<PluginOnlineIdent> result = new ArrayList<PluginOnlineIdent>();
            // get plugins node
            final Node pluginsNode = XMLUtil.getElement(document.getDocumentElement(), ID_ROOT);

            // plugins node found
            if (pluginsNode != null)
            {
                final ArrayList<Node> nodes = XMLUtil.getSubNodes(pluginsNode, ID_PLUGIN);

                for (Node node : nodes)
                {
                    final PluginOnlineIdent ident = new PluginOnlineIdent();

                    ident.loadFromXML(node);

                    if (!ident.isEmpty())
                        result.add(ident);
                }
            }

            return result;
        }

        return null;
    }

    /**
     * Reload all plugins from all active repositories (old list is cleared)
     */
    public void loadAll(boolean loadDescriptor, boolean loadImages)
    {
        clear();

        final ArrayList<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

        // load online plugins from all active repositories
        for (RepositoryInfo repoInfo : repositories)
            if (repoInfo.isEnabled())
                internal_load(repoInfo);

        if (loadDescriptor)
            internal_loadDescriptors(loadImages);
    }

    /**
     * Load the list of online plugins located at specified repository
     */
    public void load(final RepositoryInfo repos, boolean asynch, final boolean loadDescriptor, final boolean loadImages)
    {
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                internal_load(repos);
                if (loadDescriptor)
                    internal_loadDescriptors(loadImages);
            }
        };

        if (asynch)
        {
            // request stop descriptor loading
            interruptLoadDescriptors = true;
            singleProcessor.requestProcess(runnable);
        }
        else
            runnable.run();
    }

    /**
     * clear plugin list
     */
    public void clear()
    {
        // reset list
        plugins.clear();
    }

    /**
     * Load the list of online plugins located at specified repository
     */
    void internal_load(RepositoryInfo repos)
    {
        loading = true;
        try
        {
            // flag for beta version allowed
            final boolean betaAllowed = PluginPreferences.getAllowBeta();

            final ArrayList<PluginOnlineIdent> idents = getPluginIdents(repos);

            // error while retrieving idents ?
            if (idents == null)
            {
                System.err.println("Can't connect to repository '" + repos.getName() + "'");
                return;
            }

            // we start by loading only identifier part
            for (PluginOnlineIdent ident : idents)
            {
                if (betaAllowed || (!ident.getVersion().isBeta()))
                {
                    try
                    {
                        final PluginDescriptor plugin = new PluginDescriptor(ident, repos.getAuthenticationInfo());
                        // also set the name
                        plugin.setName(ident.getName());

                        plugins.add(plugin);
                    }
                    catch (Exception e)
                    {
                        System.err.println("PluginRepositoryLoader.load('" + repos.getLocation() + "') error :");
                        IcyExceptionHandler.showErrorMessage(e, false);
                    }
                }
            }

            // sort list on plugin class name
            Collections.sort(plugins, new PluginClassNameSorter());
        }
        finally
        {
            loading = false;
        }

        // notify quick load done
        changed(repos);

    }

    void internal_loadDescriptors(boolean loadImages)
    {
        // already loading descriptors ?
        while (loadingDescriptors)
        {
            // request stop
            interruptLoadDescriptors = true;
            // wait
            ThreadUtil.sleep(10);
        }

        loadingDescriptors = true;
        interruptLoadDescriptors = false;
        try
        {
            // use copy as we can reload while this process
            final ArrayList<PluginDescriptor> allPlugins = getPlugins();

            // then we load the entire descriptor
            for (PluginDescriptor plugin : allPlugins)
            {
                // interrupt requested ? stop
                if (interruptLoadDescriptors)
                    return;

                plugin.load(loadImages);
                // notify change
                changed(null);
            }

            // sort list on plugin name
            Collections.sort(plugins, new PluginNameSorter());
            // notify final change
            changed(null);
        }
        finally
        {
            loadingDescriptors = false;
        }
    }

    /**
     * @return the pluginList
     */
    public ArrayList<PluginDescriptor> getPlugins()
    {
        return new ArrayList<PluginDescriptor>(plugins);
    }

    public PluginDescriptor getPlugin(String className)
    {
        return PluginDescriptor.getPlugin(plugins, className);
    }

    public ArrayList<PluginDescriptor> getPlugins(String className)
    {
        return PluginDescriptor.getPlugins(plugins, className);
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
     * plugin list has changed
     */
    private void changed(RepositoryInfo repos)
    {
        fireEvent(new PluginRepositoryLoaderEvent(repos));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(PluginRepositoryLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(PluginRepositoryLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent(PluginRepositoryLoaderEvent e)
    {
        for (PluginRepositoryLoaderListener listener : listeners.getListeners(PluginRepositoryLoaderListener.class))
            listener.pluginRepositeryLoaderChanged(e);
    }
}
