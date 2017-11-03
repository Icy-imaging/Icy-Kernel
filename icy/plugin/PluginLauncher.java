/*
 * Copyright 2010-2015 Institut Pasteur.
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
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.plugin.interface_.PluginStartAsThread;
import icy.plugin.interface_.PluginThreaded;
import icy.system.IcyExceptionHandler;
import icy.system.audit.Audit;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;

import java.util.concurrent.Callable;

/**
 * This class launch plugins and register them to the main application.<br>
 * The launch can be in a decicated thread or in the EDT.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class PluginLauncher
{
    protected static class PluginExecutor implements Callable<Boolean>, Runnable
    {
        final Plugin plugin;

        public PluginExecutor(Plugin plugin)
        {
            super();

            this.plugin = plugin;
        }

        @Override
        public Boolean call() throws Exception
        {
            // some plugins (as EzPlug) do not respect the PluginActionable convention (run() method
            // contains all the process)
            // so we can't yet use this bloc of code

            // if (plugin instanceof PluginActionable)
            // ((PluginActionable) plugin).run();
            // // keep backward compatibility
            // else if (plugin instanceof PluginImageAnalysis)
            // ((PluginImageAnalysis) plugin).compute();

            // keep backward compatibility
            if (plugin instanceof PluginImageAnalysis)
                ((PluginImageAnalysis) plugin).compute();

            return Boolean.TRUE;
        }

        @Override
        public void run()
        {
            try
            {
                call();
            }
            catch (Throwable t)
            {
                IcyExceptionHandler.handleException(plugin.getDescriptor(), t, true);
            }
        }
    }

    /**
     * Executes the specified plugin.<br>
     * If the specified plugin implements {@link PluginThreaded} then the plugin will be executed in
     * a separate thread and the method will return before completion.<br>
     * In other case the plugin is executed on the EDT by using {@link ThreadUtil#invokeNow(Callable)} and so method
     * return after completion.
     * 
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting for execution on EDT.
     * @throws Exception
     *         if the computation threw an exception (only when plugin is executed on EDT).
     */
    private static void internalExecute(final Plugin plugin) throws Exception
    {
        if (plugin instanceof PluginThreaded)
        {
            // headless mode --> command line direct execution
            if (Icy.getMainInterface().isHeadLess())
                ((PluginThreaded) plugin).run();
            else
                new Thread((PluginThreaded) plugin, plugin.getName()).start();
        }
        else
        {
            final PluginExecutor executor = new PluginExecutor(plugin);

            // headless mode --> command line direct execution
            if (Icy.getMainInterface().isHeadLess())
                executor.call();
            // keep backward compatibility
            else if (plugin instanceof PluginStartAsThread)
                new Thread(executor, plugin.getName()).start();
            // direct launch in EDT now (no thread creation)
            else
                ThreadUtil.invokeNow((Callable<Boolean>) executor);
        }
    }

    /**
     * Creates a new instance of the specified plugin and returns it.<br>
     * 
     * @param plugin
     *        descriptor of the plugin we want to create an instance for
     * @param register
     *        if we want to register the plugin in the active plugin list
     * @see #startSafe(PluginDescriptor)
     */
    public static Plugin create(final PluginDescriptor plugin, boolean register) throws Exception
    {
        final Class<? extends Plugin> clazz = plugin.getPluginClass();
        final Plugin result;

        // use the special PluginNoEDTConstructor interface or headless mode ?
        if (ClassUtil.isSubClass(clazz, PluginNoEDTConstructor.class) || Icy.getMainInterface().isHeadLess())
            result = clazz.newInstance();
        else
        {
            // create the plugin instance on the EDT
            result = ThreadUtil.invokeNow(new Callable<Plugin>()
            {
                @Override
                public Plugin call() throws Exception
                {
                    return clazz.newInstance();
                }
            });
        }

        // register plugin
        if (register)
            Icy.getMainInterface().registerPlugin(result);

        return result;
    }

    /**
     * Creates a new instance of the specified plugin and returns it.<br>
     * The plugin is automatically registered to the list of active plugins.
     * 
     * @param plugin
     *        descriptor of the plugin we want to create an instance for
     * @see #startSafe(PluginDescriptor)
     */
    public static Plugin create(final PluginDescriptor plugin) throws Exception
    {
        return create(plugin, true);
    }

    /**
     * Starts the specified plugin (catched exception version).<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin) or <code>null</code> if an error
     * occurred.
     * 
     * @param plugin
     *        descriptor of the plugin we want to start
     * @see #startSafe(PluginDescriptor)
     */
    public static Plugin start(PluginDescriptor plugin)
    {
        final Plugin result;

        try
        {
            try
            {
                // create plugin instance
                result = create(plugin);
            }
            catch (IllegalAccessException e)
            {
                System.err.println("Cannot start plugin " + plugin.getName() + " :");
                System.err.println(e.getMessage());
                return null;
            }
            catch (InstantiationException e)
            {
                System.err.println("Cannot start plugin " + plugin.getName() + " :");
                System.err.println(e.getMessage());
                return null;
            }

            // audit
            Audit.pluginLaunched(result);
            // execute plugin
            if (result instanceof PluginImageAnalysis)
                internalExecute(result);

            return result;
        }
        catch (InterruptedException e)
        {
            // we just ignore interruption
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handleException(plugin, t, true);
        }

        return null;
    }

    /**
     * @deprecated Use {@link #start(PluginDescriptor)} instead.<br>
     *             You can retrieve a {@link PluginDescriptor} from the class name by using
     *             {@link PluginLoader#getPlugin(String)} method.
     */
    @Deprecated
    public static Plugin start(String pluginClassName)
    {
        final PluginDescriptor plugin = PluginLoader.getPlugin(pluginClassName);

        if (plugin != null)
            return start(plugin);

        return null;
    }

    /**
     * Same as {@link #start(PluginDescriptor)} except it throws {@link Exception} on error
     * so user can handle them.
     * 
     * @param plugin
     *        descriptor of the plugin we want to start
     *        compatibility)
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting for execution on EDT.
     * @throws Exception
     *         if the computation threw an exception (only when plugin is executed on EDT).
     */
    public static Plugin startSafe(PluginDescriptor plugin) throws Exception
    {
        final Plugin result;

        // create plugin instance
        result = create(plugin);

        // audit
        Audit.pluginLaunched(result);
        // execute plugin
        if (result instanceof PluginImageAnalysis)
            internalExecute(result);

        return result;
    }

    /**
     * @deprecated Use {@link #startSafe(PluginDescriptor)} instead.<br>
     *             You can retrieve a {@link PluginDescriptor} from the class name by using
     *             {@link PluginLoader#getPlugin(String)} method.
     */
    @Deprecated
    public static Plugin startSafe(String pluginClassName) throws Exception
    {
        final PluginDescriptor plugin = PluginLoader.getPlugin(pluginClassName);

        if (plugin != null)
            return startSafe(plugin);

        return null;
    }

    /**
     * @deprecated Use {@link #start(PluginDescriptor)} instead.
     */
    @Deprecated
    public synchronized static void launch(PluginDescriptor descriptor)
    {
        start(descriptor);
    }
}
