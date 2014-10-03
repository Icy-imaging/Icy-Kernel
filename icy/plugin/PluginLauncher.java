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
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.plugin.interface_.PluginStartAsThread;
import icy.plugin.interface_.PluginThreaded;
import icy.system.IcyExceptionHandler;
import icy.system.audit.Audit;
import icy.system.thread.ThreadUtil;

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
     * In other case the plugin is executed on the EDT by using
     * {@link ThreadUtil#invokeNow(Callable)} and so method return after completion.
     * 
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting for execution on EDT.
     * @throws Exception
     *         if the computation threw an exception (only when plugin is executed on EDT).
     */
    private static void internalExecute(final Plugin plugin) throws InterruptedException, Exception
    {
        if (plugin instanceof PluginThreaded)
            new Thread((PluginThreaded) plugin, plugin.getName()).start();
        else
        {
            final PluginExecutor executor = new PluginExecutor(plugin);

            // keep backward compatibility
            if (plugin instanceof PluginStartAsThread)
                new Thread(executor, plugin.getName()).start();
            // direct launch in EDT now (no thread creation)
            else
                ThreadUtil.invokeNow((Callable<Boolean>) executor);
        }
    }

    private static Plugin internalCreate(final PluginDescriptor descriptor) throws InterruptedException, Exception
    {
        // create the plugin instance on the EDT
        return ThreadUtil.invokeNow(new Callable<Plugin>()
        {
            @Override
            public Plugin call() throws Exception
            {
                try
                {
                    // try constructor with descriptor argument by default
                    return descriptor.getPluginClass().getConstructor(PluginDescriptor.class).newInstance(descriptor);
                }
                catch (NoSuchMethodException e)
                {
                    // then use default constructor
                    return descriptor.getPluginClass().newInstance();
                }
            }
        });
    }

    /**
     * Start the specified plugin (catched exception version).<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin) or
     * <code>null</code> if an error occured.
     * 
     * @see #startSafe(PluginDescriptor)
     */
    public static Plugin start(PluginDescriptor descriptor)
    {
        final Plugin result;

        try
        {
            try
            {
                // create plugin instance
                result = internalCreate(descriptor);
            }
            catch (IllegalAccessException e)
            {
                System.err.println("Cannot start plugin " + descriptor.getName() + " :");
                System.err.println(e.getMessage());
                return null;
            }
            catch (InstantiationException e)
            {
                System.err.println("Cannot start plugin " + descriptor.getName() + " :");
                System.err.println(e.getMessage());
                return null;
            }

            // register plugin
            Icy.getMainInterface().registerPlugin(result);
            // audit
            Audit.pluginLaunched(result);
            // execute plugin
            internalExecute(result);

            return result;
        }
        catch (InterruptedException e)
        {
            // we just ignore interruption
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handleException(descriptor, t, true);
        }

        return null;
    }

    /**
     * Start the specified plugin.<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin) or
     * <code>null</code> if an error occurred or if the specified class name is not a valid plugin
     * class name.
     */
    public static Plugin start(String pluginClassName)
    {
        final PluginDescriptor plugin = PluginLoader.getPlugin(pluginClassName);

        if (plugin != null)
            return start(plugin);

        return null;
    }

    /**
     * Same as {@link #start(PluginDescriptor)} except it throws {@link Exception} on error so user
     * can handle them.
     * 
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting for execution on EDT.
     * @throws Exception
     *         if the computation threw an exception (only when plugin is executed on EDT).
     */
    public static Plugin startSafe(PluginDescriptor descriptor) throws InterruptedException, Exception
    {
        final Plugin result;

        // create plugin instance
        result = internalCreate(descriptor);

        // register plugin
        Icy.getMainInterface().registerPlugin(result);
        // execute plugin
        internalExecute(result);

        return result;
    }

    /**
     * Same as {@link #start(String)} except it throws {@link Exception} on error so user
     * can handle them.
     * 
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting for execution on EDT.
     * @throws Exception
     *         if the computation threw an exception (only when plugin is executed on EDT).
     */
    public static Plugin startSafe(String pluginClassName) throws InterruptedException, Exception
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
