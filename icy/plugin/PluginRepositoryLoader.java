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

import icy.plugin.PluginDescriptor.PluginNameSorter;
import icy.plugin.PluginDescriptor.PluginOnlineIdent;
import icy.preferences.PluginPreferences;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.IcyExceptionHandler;
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
        public void pluginRepositeryLoaderChanged();
    }

    private class LoadAllRunner implements Runnable
    {
        boolean loadDescriptor;
        boolean loadImages;

        public LoadAllRunner()
        {
            super();
        }

        void setParameters(boolean loadDescriptor, boolean loadImages)
        {
            this.loadDescriptor = loadDescriptor;
            this.loadImages = loadImages;
        }

        @Override
        public void run()
        {
            // cache
            final boolean ld = loadDescriptor;
            final boolean li = loadImages;

            // request interrupt of loading if any
            interruptLoadDescriptors = true;

            startLoading();
            try
            {
                clear();

                final ArrayList<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

                // load online plugins from all active repositories
                for (RepositoryInfo repoInfo : repositories)
                    if (repoInfo.isEnabled())
                        loadInternal(repoInfo);

                if (ld)
                    loadDescriptorsInternal(li);
            }
            finally
            {
                endLoading();
            }
        }
    }

    // private class LoadSingleRunner implements Runnable
    // {
    // RepositoryInfo repos;
    // boolean loadDescriptor;
    // boolean loadImages;
    //
    // public LoadSingleRunner()
    // {
    // super();
    // }
    //
    // void setParameters(RepositoryInfo repos, boolean loadDescriptor, boolean loadImages)
    // {
    // this.repos = repos;
    // this.loadDescriptor = loadDescriptor;
    // this.loadImages = loadImages;
    // }
    //
    // @Override
    // public void run()
    // {
    // // cache
    // final RepositoryInfo r = repos;
    // final boolean ld = loadDescriptor;
    // final boolean li = loadImages;
    //
    // startLoading();
    // try
    // {
    // loadInternal(r);
    // if (ld)
    // loadDescriptorsInternal(li);
    // }
    // finally
    // {
    // endLoading();
    // }
    // }
    // }

    private static final String ID_ROOT = "plugins";
    private static final String ID_PLUGIN = "plugin";
    // private static final String ID_PATH = "path";

    /**
     * static class
     */
    private static PluginRepositoryLoader instance = new PluginRepositoryLoader();

    /**
     * Online plugin list
     */
    private ArrayList<PluginDescriptor> plugins;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internals
     */
    private boolean loading;
    private boolean loadingDescriptors;
    boolean interruptLoadDescriptors;

    private final LoadAllRunner loadAllRunner;

    // private final LoadSingleRunner loadSingleRunner;

    /**
     * static class
     */
    private PluginRepositoryLoader()
    {
        super();

        plugins = new ArrayList<PluginDescriptor>();
        listeners = new EventListenerList();

        loading = false;
        loadingDescriptors = false;
        interruptLoadDescriptors = false;

        loadAllRunner = new LoadAllRunner();
        // loadSingleRunner = new LoadSingleRunner();
    }

    /**
     * Return the plugins identifier list from a repository URL
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
     * clear plugin list
     */
    public static void clear()
    {
        // reset list
        instance.plugins.clear();
    }

    /**
     * Reload all plugins from all active repositories (old list is cleared)
     */
    public static void reload(boolean asynch, boolean loadDescriptor, boolean loadImages)
    {
        instance.loadAllRunner.setParameters(loadDescriptor, loadImages);

        if (asynch)
            ThreadUtil.bgRunSingle(instance.loadAllRunner);
        else
            instance.loadAllRunner.run();
    }

    /**
     * Load the list of online plugins located at specified repository
     */
    // public static void load(final RepositoryInfo repos, boolean asynch, final boolean
    // loadDescriptor,
    // final boolean loadImages)
    // {
    // instance.loadSingleRunner.setParameters(repos, loadDescriptor, loadImages);
    //
    // if (asynch)
    // ThreadUtil.bgRunSingle(instance.loadAllRunner);
    // else
    // instance.loadAllRunner.run();
    // }

    /**
     * Load the list of online plugins located at specified repository
     */
    void loadInternal(RepositoryInfo repos)
    {
        // we start by loading only identifier part
        final ArrayList<PluginOnlineIdent> idents = getPluginIdents(repos);

        // error while retrieving identifiers ?
        if (idents == null)
        {
            System.err.println("Can't connect to repository '" + repos.getName() + "'");
            return;
        }

        // flag for beta version allowed
        final boolean betaAllowed = PluginPreferences.getAllowBeta();

        for (PluginOnlineIdent ident : idents)
        {
            if (betaAllowed || (!ident.getVersion().isBeta()))
            {
                try
                {
                    final PluginDescriptor plugin = new PluginDescriptor(ident, repos);
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
        Collections.sort(plugins, PluginNameSorter.instance);
        // notify quick load done
        changed();
    }

    void loadDescriptorsInternal(boolean loadImages)
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
                changed();
            }

            // sort list on plugin name
            Collections.sort(plugins, PluginNameSorter.instance);
            // notify final change
            changed();
        }
        finally
        {
            loadingDescriptors = false;
        }
    }

    /**
     * @return the pluginList
     */
    public static ArrayList<PluginDescriptor> getPlugins()
    {
        return new ArrayList<PluginDescriptor>(instance.plugins);
    }

    public static PluginDescriptor getPlugin(String className)
    {
        return PluginDescriptor.getPlugin(instance.plugins, className);
    }

    public static ArrayList<PluginDescriptor> getPlugins(String className)
    {
        return PluginDescriptor.getPlugins(instance.plugins, className);
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
        while (instance.loading)
            ThreadUtil.sleep(10);
    }

    /**
     * Start loading
     */
    void startLoading()
    {
        synchronized (instance)
        {
            waitWhileLoading();
            loading = true;
        }
    }

    /**
     * End loading
     */
    void endLoading()
    {
        loading = false;
    }

    /**
     * plugin list has changed
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
    public static void addListener(PluginRepositoryLoaderListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.add(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(PluginRepositoryLoaderListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.remove(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent()
    {
        for (PluginRepositoryLoaderListener listener : listeners.getListeners(PluginRepositoryLoaderListener.class))
            listener.pluginRepositeryLoaderChanged();
    }
}
