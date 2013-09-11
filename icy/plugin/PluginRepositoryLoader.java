/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.plugin;

import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor.PluginIdent;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        public void pluginRepositeryLoaderChanged(PluginDescriptor plugin);
    }

    private class Loader implements Runnable
    {
        public Loader()
        {
            super();
        }

        @Override
        public void run()
        {
            // no internet connection ?
            if (!NetworkUtil.hasInternetAccess())
            {
                failed = true;
                return;
            }

            final ArrayList<PluginDescriptor> newPlugins = new ArrayList<PluginDescriptor>();

            try
            {
                final ArrayList<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

                // load online plugins from all active repositories
                for (RepositoryInfo repoInfo : repositories)
                {
                    // reload requested --> stop current loading
                    if (processor.hasWaitingTasks())
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

                plugins = newPlugins;
            }
            catch (Exception e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
                failed = true;
                return;
            }

            // notify change for basic infos
            basicLoaded = true;
            changed(null);

            // we load descriptor
            for (PluginDescriptor plugin : plugins)
            {
                // reload requested --> stop current loading
                if (processor.hasWaitingTasks())
                    return;
                // internet connection lost --> failed
                if (!NetworkUtil.hasInternetAccess())
                {
                    failed = true;
                    return;
                }

                plugin.loadDescriptor();
            }

            // sort list on plugin name
            synchronized (plugins)
            {
                Collections.sort(plugins, PluginNameSorter.instance);
            }

            // notify final change for descriptors loading
            descriptorsLoaded = true;
            changed(null);

            // then we load images
            for (PluginDescriptor plugin : plugins)
            {
                // reload requested --> stop current loading
                if (processor.hasWaitingTasks())
                    return;
                // internet connection lost --> failed
                if (!NetworkUtil.hasInternetAccess())
                {
                    failed = true;
                    return;
                }

                plugin.loadImages();
                // notify change
                changed(plugin);
            }

            // images loaded
            imagesLoaded = true;
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
    boolean basicLoaded;
    boolean descriptorsLoaded;
    boolean imagesLoaded;
    boolean failed;

    private final Loader loader;
    final SingleProcessor processor;

    /**
     * static class
     */
    private PluginRepositoryLoader()
    {
        super();

        plugins = new ArrayList<PluginDescriptor>();
        listeners = new EventListenerList();

        loader = new Loader();
        processor = new SingleProcessor(true, "Online Plugin Loader");
        // we want the processor to stay alive
        processor.setKeepAliveTime(1, TimeUnit.DAYS);

        // initial loading
        load();
    }

    /**
     * Return the plugins identifier list from a repository URL
     */
    public static ArrayList<PluginOnlineIdent> getPluginIdents(RepositoryInfo repos)
    {
        // prepare parameters for plugin list request
        final HashMap<String, String> values = new HashMap<String, String>();

        values.put(NetworkUtil.ID_KERNELVERSION, Icy.version.toString());

        final Document document = XMLUtil.loadDocument(
                repos.getLocation() + "?" + NetworkUtil.getContentString(values), repos.getAuthenticationInfo(), false);

        if (document != null)
        {
            final ArrayList<PluginOnlineIdent> result = new ArrayList<PluginOnlineIdent>();
            // get plugins node
            final Node pluginsNode = XMLUtil.getElement(document.getDocumentElement(), ID_ROOT);

            // plugins node found
            if (pluginsNode != null)
            {
                // flag for beta version allowed
                final boolean betaAllowed = PluginPreferences.getAllowBeta();
                // ident nodes
                final ArrayList<Node> nodes = XMLUtil.getChildren(pluginsNode, ID_PLUGIN);

                for (Node node : nodes)
                {
                    final PluginOnlineIdent ident = new PluginOnlineIdent();

                    ident.loadFromXML(node);

                    // accept only if not empty
                    if (!ident.isEmpty())
                    {
                        // accept only if required kernel version is ok and beta accepted
                        if (ident.getRequiredKernelVersion().isLowerOrEqual(Icy.version)
                                && (betaAllowed || (!ident.getVersion().isBeta())))
                        {

                            // check if we have several version of the same plugin
                            final int ind = PluginIdent.getIndex(result, ident.getClassName());
                            // other version found ?
                            if (ind != -1)
                            {
                                // replace old version if needed
                                if (result.get(ind).isOlderOrEqual(ident))
                                    result.set(ind, ident);
                            }
                            else
                                result.add(ident);
                        }
                    }
                }
            }

            return result;
        }

        return null;
    }

    /**
     * Do loading process.
     */
    private void load()
    {
        basicLoaded = false;
        descriptorsLoaded = false;
        imagesLoaded = false;
        failed = false;

        processor.addTask(loader);
    }

    /**
     * Reload all plugins from all active repositories (old list is cleared).<br>
     * Asynchronous process, use {@link #waitBasicLoaded()} or {@link #waitDescriptorsLoaded()}
     * method to wait for specified data to be loaded.
     */
    public static synchronized void reload()
    {
        instance.load();
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
            if (!NetworkUtil.hasInternetAccess())
                System.out.println("You are not connected to internet.");
            else
                System.out.println("Can't access repository '" + repos.getName() + "'");

            return null;
        }

        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        for (PluginOnlineIdent ident : idents)
        {
            try
            {
                result.add(new PluginDescriptor(ident, repos));
            }
            catch (Exception e)
            {
                System.out.println("PluginRepositoryLoader.load('" + repos.getLocation() + "') error :");
                IcyExceptionHandler.showErrorMessage(e, false);
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

    public static List<PluginDescriptor> getPlugins(String className)
    {
        synchronized (instance.plugins)
        {
            return PluginDescriptor.getPlugins(instance.plugins, className);
        }
    }

    /**
     * Return the plugins list from the specified repository
     */
    public static List<PluginDescriptor> getPlugins(RepositoryInfo repos)
    {
        final List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

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
        return instance.processor.isProcessing();
    }

    /**
     * @return true if basic informations (class names, versions...) are loaded.
     */
    public static boolean isBasicLoaded()
    {
        return instance.failed || instance.basicLoaded;
    }

    /**
     * @return true if complete descriptors are loaded.
     */
    public static boolean isDescriptorsLoaded()
    {
        return instance.failed || instance.descriptorsLoaded;
    }

    /**
     * @return true if images are loaded.
     */
    public static boolean isImagesLoaded()
    {
        return instance.failed || instance.imagesLoaded;
    }

    /**
     * Wait until basic informations are loaded.
     */
    public static void waitBasicLoaded()
    {
        while (!isBasicLoaded())
            ThreadUtil.sleep(10);
    }

    /**
     * Wait until descriptors are loaded.
     */
    public static void waitDescriptorsLoaded()
    {
        while (!isDescriptorsLoaded())
            ThreadUtil.sleep(10);
    }

    /**
     * Returns true if an error occurred during the plugin loading process.
     */
    public static boolean failed()
    {
        return instance.failed;
    }

    /**
     * Plugin list has changed
     */
    void changed(PluginDescriptor plugin)
    {
        fireEvent(plugin);
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
     * 
     * @param plugin
     */
    private void fireEvent(PluginDescriptor plugin)
    {
        for (PluginRepositoryLoaderListener listener : listeners.getListeners(PluginRepositoryLoaderListener.class))
            listener.pluginRepositeryLoaderChanged(plugin);
    }
}
