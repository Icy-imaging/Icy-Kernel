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
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.main.Icy;
import icy.plugin.PluginDescriptor.PluginIdent;
import icy.plugin.PluginDescriptor.PluginNameSorter;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginDaemon;
import icy.preferences.PluginPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.xeustechnologies.jcl.JarClassLoader;

/**
 * Plugin Loader class.<br>
 * This class is used to load plugins from "plugins" package and "plugins" directory
 * 
 * @author Stephane<br>
 */
public class PluginLoader
{
    public static class PluginClassLoader extends JarClassLoader
    {
        public PluginClassLoader()
        {
            super();
        }

        /**
         * Give access to this method
         */
        public Class<?> getLoadedClass(String name)
        {
            return super.findLoadedClass(name);
        }

        /**
         * Give access to this method
         */
        public boolean isLoadedClass(String name)
        {
            return getLoadedClass(name) != null;
        }
    }

    public static interface PluginLoaderListener extends EventListener
    {
        public void pluginLoaderChanged(PluginLoaderEvent e);
    }

    public static class PluginLoaderEvent implements EventHierarchicalChecker
    {
        public PluginLoaderEvent()
        {
            super();
        }

        @Override
        public boolean isEventRedundantWith(EventHierarchicalChecker event)
        {
            return (event instanceof PluginLoaderEvent);
        }
    }

    public final static String PLUGIN_PACKAGE = "plugins";
    public final static String PLUGIN_PATH = "plugins";

    /**
     * static class
     */
    private static ClassLoader loader = new PluginClassLoader();

    /**
     * JAR Class Loader disabled flag
     */
    public static boolean JCLDisabled = false;

    /**
     * Loaded plugin list
     */
    private static ArrayList<PluginDescriptor> plugins = new ArrayList<PluginDescriptor>();

    /**
     * internal updater (no need to dispatch event on the AWT thread)
     */
    private static final UpdateEventHandler updater = new UpdateEventHandler(new ChangeListener()
    {
        @Override
        public void onChanged(EventHierarchicalChecker e)
        {
            final PluginLoaderEvent event = (PluginLoaderEvent) e;

            // start daemon plugins
            startDaemons();

            // notify listener we have changed
            fireEvent(event);
        }
    }, false);

    /**
     * listeners
     */
    private static final EventListenerList listeners = new EventListenerList();

    /**
     * internal
     */
    private static final Runnable reloader = new Runnable()
    {
        @Override
        public void run()
        {
            reloadInternal();
        }
    };;

    private static boolean initialized = false;
    private static boolean needReload = false;
    private static boolean loading = false;
    private static boolean logError = true;

    public static void prepare()
    {
        if (!initialized)
        {
            if (isLoading())
                waitWhileLoading();
            else
                reloadInternal();
        }
    }

    /**
     * Reload the list of installed plugins.<br>
     * Asynchronous version
     */
    public static void reloadAsynch()
    {
        loading = true;
        ThreadUtil.bgRunSingle(reloader);
    }

    /**
     * Reload the list of installed plugins.
     */
    public static void reload(boolean forceReloadNow)
    {
        if ((!forceReloadNow) && isUpdating())
            needReload = true;
        else
        {
            waitWhileLoading();
            reloadInternal();
        }
    }

    /**
     * Stop and restart all daemons plugins.
     */
    public static void resetDaemons()
    {
        // reset will be done later
        if (isLoading())
            return;

        stopDaemons();
        startDaemons();
    }

    /**
     * Reload the list of installed plugins (in "plugins" directory)
     */
    static void reloadInternal()
    {
        loading = true;
        needReload = false;

        // stop daemon plugins
        stopDaemons();

        // reset plugins and loader
        final ArrayList<PluginDescriptor> newPlugins = new ArrayList<PluginDescriptor>();
        final ClassLoader newLoader;

        // special case where JCL is disabled
        if (JCLDisabled)
            newLoader = PluginLoader.class.getClassLoader();
        else
        {
            newLoader = new PluginClassLoader();
            // reload plugins directory to search path
            ((PluginClassLoader) newLoader).add(PLUGIN_PATH);
        }

        final HashSet<String> classes = new HashSet<String>();

        try
        {
            // search for plugins in "Plugins" package (needed when working from JAR archive)
            ClassUtil.findClassNamesInPackage(PLUGIN_PACKAGE, true, classes);
            // search for plugins in "Plugins" directory with default plugin package name
            ClassUtil.findClassNamesInPath(PLUGIN_PATH, PLUGIN_PACKAGE, true, classes);
        }
        catch (IOException e)
        {
            if (logError)
            {
                System.err.println("Error loading plugins :");
                IcyExceptionHandler.showErrorMessage(e, true);
            }
        }

        for (String className : classes)
        {
            try
            {
                // don't load class without package name (JCL don't like them)
                if (!ClassUtil.getPackageName(className).isEmpty())
                {
                    // try to load class and check we have a Plugin class at same time
                    final Class<? extends Plugin> pluginClass = newLoader.loadClass(className).asSubclass(Plugin.class);

                    // // ignore interface or abstract classes
                    // if ((!pluginClass.isInterface()) && (!ClassUtil.isAbstract(pluginClass)))
                    // plugins.add(new PluginDescriptor(pluginClass));

                    newPlugins.add(new PluginDescriptor(pluginClass));
                }
            }
            catch (NoClassDefFoundError e)
            {
                if (logError)
                {
                    // fatal error
                    System.err.println("Class '" + className + "' cannot be loaded :");
                    System.err.println("Required class '" + ClassUtil.getQualifiedNameFromPath(e.getMessage())
                            + "' not found.");
                }
            }
            catch (OutOfMemoryError e)
            {
                // fatal error
                IcyExceptionHandler.showErrorMessage(e, false);
                System.err.println("Class '" + className + "' is discarded");
            }
            catch (Error e)
            {
                if (logError)
                {
                    // fatal error
                    IcyExceptionHandler.showErrorMessage(e, false);
                    System.err.println("Class '" + className + "' is discarded");
                }
            }
            catch (ClassCastException e)
            {
                // ignore ClassCastException (for classes which doesn't extend Plugin)
            }
            catch (ClassNotFoundException e)
            {
                // ignore ClassNotFoundException (for no public classes)
            }
            catch (Exception e)
            {
                // fatal error
                if (logError)
                {
                    IcyExceptionHandler.showErrorMessage(e, false);
                    System.err.println("Class '" + className + "' is discarded");
                }
            }
        }

        // sort list
        Collections.sort(newPlugins, PluginNameSorter.instance);

        loader = newLoader;
        plugins = newPlugins;

        // notify change
        changed();
    }

    /**
     * Returns list of daemon plugins.
     */
    public static ArrayList<PluginDescriptor> getDaemonPlugins()
    {
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        synchronized (plugins)
        {
            for (PluginDescriptor pluginDescriptor : plugins)
            {
                final Class<? extends Plugin> classPlug = pluginDescriptor.getPluginClass();

                if ((classPlug != null) && PluginDaemon.class.isAssignableFrom(classPlug))
                {
                    // accept class ?
                    if (!ClassUtil.isAbstract(classPlug) && !classPlug.isInterface())
                        result.add(pluginDescriptor);
                }
            }
        }

        return result;
    }

    /**
     * Start daemons plugins.
     */
    static void startDaemons()
    {
        final ArrayList<String> inactives = PluginPreferences.getInactiveDaemons();

        for (PluginDescriptor pluginDesc : getDaemonPlugins())
        {
            // not found in inactives ?
            if (inactives.indexOf(pluginDesc.getClassName()) == -1)
            {
                try
                {
                    final Plugin plugin = pluginDesc.getPluginClass().newInstance();
                    final Thread thread = new Thread((PluginDaemon) plugin, pluginDesc.getName());

                    // so icy can exit even with running daemon plugin
                    thread.setDaemon(true);

                    // start the daemon
                    thread.start();
                    // register daemon plugin ???
                    Icy.getMainInterface().registerPlugin(plugin);
                }
                catch (Throwable t)
                {
                    IcyExceptionHandler.handlePluginException(pluginDesc, t, true);
                }
            }
        }
    }

    /**
     * Stop daemons plugins.
     */
    static void stopDaemons()
    {
        final ArrayList<Plugin> activePlugins = Icy.getMainInterface().getActivePlugins();

        for (PluginDescriptor pluginDesc : getDaemonPlugins())
        {
            try
            {
                // search the specified daemon plugin in current active plugins
                final Plugin plugin = Plugin.getPlugin(activePlugins, pluginDesc.getClassName());

                // stop the daemon
                if (plugin != null)
                    ((PluginDaemon) plugin).stop();
            }
            catch (Throwable t)
            {
                IcyExceptionHandler.handlePluginException(pluginDesc, t, true);
            }
        }
    }

    /**
     * Return the loader
     */
    public static ClassLoader getLoader()
    {
        return loader;
    }

    /**
     * Return all loaded resources
     */
    public static Map<String, byte[]> getAllResources()
    {
        prepare();

        if (loader instanceof JarClassLoader)
            ((JarClassLoader) loader).getLoadedResources();

        return new HashMap<String, byte[]>();
    }

    /**
     * Return all loaded classes
     */
    public static Map<String, Class<?>> getAllClasses()
    {
        prepare();

        if (loader instanceof JarClassLoader)
            ((JarClassLoader) loader).getLoadedClasses();

        return new HashMap<String, Class<?>>();
    }

    /**
     * Return a resource as data stream from given resource name
     * 
     * @param name
     *        resource name
     */
    public static InputStream getResourceAsStream(String name)
    {
        prepare();

        return loader.getResourceAsStream(name);
    }

    /**
     * Return the list of loaded plugins
     */
    public static ArrayList<PluginDescriptor> getPlugins()
    {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (plugins)
        {
            return new ArrayList<PluginDescriptor>(plugins);
        }
    }

    /**
     * Return the list of loaded plugins which derive from the specified class
     */
    public static ArrayList<PluginDescriptor> getPlugins(Class<?> clazz)
    {
        return getPlugins(clazz, false, false);
    }

    /**
     * Return the list of loaded plugins which derive from the specified class
     * 
     * @param wantAbstract
     *        specified if we also want abstract classes
     * @param wantInterface
     *        specified if we also want interfaces
     */
    public static ArrayList<PluginDescriptor> getPlugins(Class<?> clazz, boolean wantAbstract, boolean wantInterface)
    {
        prepare();

        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        if (clazz != null)
        {
            synchronized (plugins)
            {
                for (PluginDescriptor pluginDescriptor : plugins)
                {
                    final Class<? extends Plugin> classPlug = pluginDescriptor.getPluginClass();

                    if ((classPlug != null) && clazz.isAssignableFrom(classPlug))
                    {
                        // accept class ?
                        if ((wantAbstract || !ClassUtil.isAbstract(classPlug))
                                && (wantInterface || !classPlug.isInterface()))
                            result.add(pluginDescriptor);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Return the list of "actionable" plugins (mean we can launch them from GUI)
     */
    public static ArrayList<PluginDescriptor> getActionablePlugins()
    {
        prepare();

        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        synchronized (plugins)
        {
            for (PluginDescriptor pluginDescriptor : plugins)
                if (pluginDescriptor.isActionable())
                    result.add(pluginDescriptor);
        }

        return result;
    }

    /**
     * @return the loading
     */
    public static boolean isLoading()
    {
        return loading;
    }

    /**
     * wait until loading completed
     */
    public static void waitWhileLoading()
    {
        while (isLoading())
            ThreadUtil.sleep(10);
    }

    public static boolean isLoaded(PluginDescriptor plugin, boolean acceptNewer)
    {
        return (getPlugin(plugin.getIdent(), acceptNewer) != null);
    }

    public static boolean isLoaded(String className)
    {
        return (getPlugin(className) != null);
    }

    public static PluginDescriptor getPlugin(PluginIdent ident, boolean acceptNewer)
    {
        prepare();

        return PluginDescriptor.getPlugin(getPlugins(), ident, acceptNewer);
    }

    public static PluginDescriptor getPlugin(String className)
    {
        prepare();

        return PluginDescriptor.getPlugin(getPlugins(), className);
    }

    public static Class<? extends Plugin> getPluginClass(String className)
    {
        prepare();

        final PluginDescriptor descriptor = getPlugin(className);
        if (descriptor != null)
            return descriptor.getPluginClass();

        return null;
    }

    /**
     * Loads the class with the specified binary name from the Plugin class loader.
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException
    {
        prepare();

        synchronized (loader)
        {
            // try to load class and check we have a Plugin class at same time
            return loader.loadClass(className);
        }
    }

    /**
     * Verify that specified plugins are valid.<br>
     * Return the error string if any (empty string = plugins are valid)
     */
    public static String verifyPluginsAreValid(ArrayList<PluginDescriptor> pluginsToVerify)
    {
        synchronized (loader)
        {
            for (PluginDescriptor plugin : pluginsToVerify)
            {
                try
                {
                    // then try to load the plugin class as Plugin class
                    loader.loadClass(plugin.getClassName()).asSubclass(Plugin.class);
                }
                catch (Error e)
                {
                    return "Fatal error while loading '" + plugin.getClassName() + "' class from "
                            + plugin.getJarFilename() + " :\n" + e.toString();
                }
                catch (ClassCastException e)
                {
                    return "Fatal error while loading '" + plugin.getClassName() + "' class from "
                            + plugin.getJarFilename() + " :\n" + e.toString() + "\n"
                            + "Your plugin class should extends 'icy.plugin.abstract_.Plugin' class !";
                }
                catch (ClassNotFoundException e)
                {
                    return "Fatal error while loading '" + plugin.getClassName() + "' class from "
                            + plugin.getJarFilename() + " :\n" + e.toString() + "\n"
                            + "Verify you correctly set the class name in your plugin description.";
                }
                catch (Exception e)
                {
                    return "Fatal error while loading '" + plugin.getClassName() + "' class from "
                            + plugin.getJarFilename() + " :\n" + e.toString();
                }
            }
        }

        return "";
    }

    /**
     * Load all classes from specified path
     */
    // private static ArrayList<String> loadAllClasses(String path)
    // {
    // // search for class names in that path
    // final HashSet<String> classNames = ClassUtil.findClassNamesInPath(path, true);
    // final ArrayList<String> result = new ArrayList<String>();
    //
    // synchronized (loader)
    // {
    // for (String className : classNames)
    // {
    // try
    // {
    // // try to load class
    // loader.loadClass(className);
    // }
    // catch (Error err)
    // {
    // // fatal error while loading class, store error String
    // result.add("Fatal error while loading " + className + " :\n" + err.toString() + "\n");
    // }
    // catch (ClassNotFoundException cnfe)
    // {
    // // ignore ClassNotFoundException (happen with private class)
    // }
    // catch (Exception exc)
    // {
    // result.add("Fatal error while loading " + className + " :\n" + exc.toString() + "\n");
    // }
    // }
    // }
    //
    // return result;
    // }

    /**
     * 
     */
    static void changed()
    {
        synchronized (updater)
        {
            initialized = true;
            loading = false;

            // plugin list has changed
            updater.changed(new PluginLoaderEvent());
        }
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public static void addListener(PluginLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(PluginLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(PluginLoaderListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(PluginLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    static void fireEvent(PluginLoaderEvent e)
    {
        for (PluginLoaderListener listener : listeners.getListeners(PluginLoaderListener.class))
            listener.pluginLoaderChanged(e);
    }

    public static void beginUpdate()
    {
        synchronized (updater)
        {
            updater.beginUpdate();
        }
    }

    public static void endUpdate()
    {
        synchronized (updater)
        {
            updater.endUpdate();
            if (!updater.isUpdating())
            {
                // proceed pending tasks
                if (needReload)
                    reloadInternal();
            }
        }
    }

    public static boolean isUpdating()
    {
        synchronized (updater)
        {
            return updater.isUpdating();
        }
    }

    /**
     * @return the logError
     */
    public static boolean getLogError()
    {
        return logError;
    }

    public static void setLogError(boolean value)
    {
        logError = value;
    }
}
