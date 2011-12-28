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

import icy.main.Icy;
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

    private class LoadRunner extends Thread
    {
        public LoadRunner()
        {
            super("Online plugin loader");
        }

        @Override
        public void run()
        {
            final ArrayList<PluginDescriptor> newPlugins = new ArrayList<PluginDescriptor>();

            loadingBasic = true;
            try
            {
                final ArrayList<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

                // load online plugins from all active repositories
                for (RepositoryInfo repoInfo : repositories)
                {
                    // interrupt requested ? stop
                    if (interrupted())
                        return;

                    if (repoInfo.isEnabled())
                    {
                        final ArrayList<PluginDescriptor> pluginsRepos = loadInternal(repoInfo);

                        if (pluginsRepos == null)
                        {
                            failed = true;
                            return;
                        }

                        newPlugins.addAll(pluginsRepos);
                    }
                }

                // sort list on plugin class name
                Collections.sort(newPlugins, PluginNameSorter.instance);

                synchronized (plugins)
                {
                    plugins = newPlugins;
                }
            }
            catch (Exception e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
                failed = true;
                return;
            }
            finally
            {
                loadingBasic = false;
            }

            // notify change for basic infos
            changed();

            loadingDescriptors = true;
            try
            {
                // we load descriptor
                for (PluginDescriptor plugin : plugins)
                {
                    // interrupt requested ? stop
                    if (interrupted())
                        return;

                    plugin.loadDescriptor();
                }

                // sort list on plugin name
                Collections.sort(plugins, PluginNameSorter.instance);
            }
            finally
            {
                loadingDescriptors = false;
            }

            // notify final change for descriptors loading
            changed();

            loadingImages = true;
            try
            {
                // then we load images
                for (PluginDescriptor plugin : plugins)
                {
                    // interrupt requested ? stop
                    if (interrupted())
                        return;

                    plugin.loadImages();
                    // notify change
                    changed();
                }
            }
            finally
            {
                loadingImages = false;
            }
        }
    }

    private static final String ID_ROOT = "plugins";
    private static final String ID_PLUGIN = "plugin";
    // private static final String ID_PATH = "path";

    /**
     * static class
     */
    private static final PluginRepositoryLoader instance = new PluginRepositoryLoader();

    /**
     * Online plugin list
     */
    ArrayList<PluginDescriptor> plugins;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internals
     */
    boolean loadingBasic;
    boolean loadingDescriptors;
    boolean loadingImages;
    boolean failed;

    private LoadRunner loadRunner;

    /**
     * static class
     */
    private PluginRepositoryLoader()
    {
        super();

        plugins = new ArrayList<PluginDescriptor>();
        listeners = new EventListenerList();

        loadingBasic = false;
        loadingDescriptors = false;
        loadingImages = false;
        failed = false;

        // initial loading
        startLoad();
    }

    /**
     * Return the plugins identifier list from a repository URL
     */
    public static ArrayList<PluginOnlineIdent> getPluginIdents(RepositoryInfo repos)
    {
        final Document document = XMLUtil.loadDocument(repos.getLocation(), repos.getAuthenticationInfo(), false);

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

                    // accept only if not empty
                    if (!ident.isEmpty())
                        result.add(ident);
                }
            }

            return result;
        }

        return null;
    }

    /**
     * Start loading process
     */
    private void startLoad()
    {
        loadRunner = new LoadRunner();
        loadRunner.start();
    }

    /**
     * Reload all plugins from all active repositories (old list is cleared)
     */
    public static void reload()
    {
        // request interrupt
        instance.loadRunner.interrupt();

        // wait for end processing
        try
        {
            instance.loadRunner.join();
        }
        catch (InterruptedException e)
        {
            // ignore
        }

        // start it again
        instance.startLoad();
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
     * Load and return the list of online plugins located at specified repository
     */
    ArrayList<PluginDescriptor> loadInternal(RepositoryInfo repos)
    {
        // we start by loading only identifier part
        final ArrayList<PluginOnlineIdent> idents = getPluginIdents(repos);

        // error while retrieving identifiers ?
        if (idents == null)
        {
            System.out.println("Can't connect to repository : " + repos.getName() + " - " + repos.getLocation());
            return null;
        }

        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        // flag for beta version allowed
        final boolean betaAllowed = PluginPreferences.getAllowBeta();

        for (PluginOnlineIdent ident : idents)
        {
            // accept only if required kernel version is ok and beta accepted
            if (ident.getRequiredKernelVersion().isLowerOrEqual(Icy.version)
                    && (betaAllowed || (!ident.getVersion().isBeta())))
            {
                try
                {
                    final PluginDescriptor plugin = new PluginDescriptor(ident, repos);
                    // also set the name
                    plugin.setName(ident.getName());

                    result.add(plugin);
                }
                catch (Exception e)
                {
                    System.out.println("PluginRepositoryLoader.load('" + repos.getLocation() + "') error :");
                    IcyExceptionHandler.showErrorMessage(e, false);
                }
            }
        }

        return result;
    }

    /**
     * @return the pluginList
     */
    public static ArrayList<PluginDescriptor> getPlugins()
    {
        synchronized (instance.plugins)
        {
            return new ArrayList<PluginDescriptor>(instance.plugins);
        }
    }

    public static PluginDescriptor getPlugin(String className)
    {
        synchronized (instance.plugins)
        {
            return PluginDescriptor.getPlugin(instance.plugins, className);
        }
    }

    public static ArrayList<PluginDescriptor> getPlugins(String className)
    {
        synchronized (instance.plugins)
        {
            return PluginDescriptor.getPlugins(instance.plugins, className);
        }
    }

    /**
     * Return the plugins list from the specified repository
     */
    public static ArrayList<PluginDescriptor> getPlugins(RepositoryInfo repos)
    {
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        synchronized (instance.plugins)
        {
            for (PluginDescriptor plugin : instance.plugins)
                if (plugin.getRepository().equals(repos))
                    result.add(plugin);
        }

        return result;
    }

    /**
     * @return true if loader is loading anything (basic, descriptor or images)
     */
    public static boolean isLoading()
    {
        return instance.loadRunner.isAlive();
    }

    /**
     * @return true if loader is currently loading basic informations
     */
    public static boolean isLoadingBasic()
    {
        return instance.loadingBasic;
    }

    /**
     * @return true if loader is currently loading descriptors
     */
    public static boolean isLoadingDescriptors()
    {
        return instance.loadingDescriptors;
    }

    /**
     * @return true if loader is currently loading images
     */
    public static boolean isLoadingImages()
    {
        return instance.loadingImages;
    }

    /**
     * wait until basic loading completed
     */
    public static void waitBasicLoaded()
    {
        while (instance.loadingBasic)
            ThreadUtil.sleep(10);
    }

    /**
     * wait until descriptors loading completed
     */
    public static void waitDescriptorsLoaded()
    {
        while (instance.loadingDescriptors)
            ThreadUtil.sleep(10);
    }

    /**
     * return true if an error occurred during the plugin loading process
     */
    public static boolean failed()
    {
        return instance.failed;
    }

    /**
     * plugin list has changed
     */
    void changed()
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
