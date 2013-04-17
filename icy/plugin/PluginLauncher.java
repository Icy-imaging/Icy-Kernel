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
import icy.system.thread.ThreadUtil;

/**
 * This class launch plugins and register them to the main application.<br>
 * The launch can be in a decicated thread or in the EDT.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class PluginLauncher implements Runnable
{
    protected final PluginDescriptor descriptor;
    protected Plugin plugin;

    protected PluginLauncher(PluginDescriptor descriptor)
    {
        super();

        this.descriptor = descriptor;
        plugin = null;
    }

    protected void create()
    {
        // create the plugin instance on the AWT
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    plugin = descriptor.getPluginClass().newInstance();
                }
                catch (Throwable t)
                {
                    plugin = null;
                    IcyExceptionHandler.handleException(descriptor, t, true);
                }
            }
        });
    }

    @Override
    public void run()
    {
        try
        {
            // keep backward compatibility
            if (plugin instanceof PluginImageAnalysis)
                ((PluginImageAnalysis) plugin).compute();
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handleException(descriptor, t, true);
        }
    }

    /**
     * Execute the plugin (instance should exists).
     */
    protected void execute()
    {
        final Thread thread;

        if (plugin instanceof PluginThreaded)
            thread = new Thread((PluginThreaded) plugin, descriptor.getName());
        // keep backward compatibility
        else if (plugin instanceof PluginStartAsThread)
            thread = new Thread(this, descriptor.getName());
        else
            thread = null;

        // launch as thread
        if (thread != null)
            thread.start();
        else
            // direct launch in EDT now (no thread creation)
            ThreadUtil.invokeNow(this);
    }

    /**
     * Start the specified plugin.<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin)
     */
    public static Plugin start(String pluginClassName)
    {
        final PluginDescriptor plugin = PluginLoader.getPlugin(pluginClassName);

        if (plugin != null)
            return start(plugin);

        return null;
    }

    /**
     * Start the specified plugin.<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin)
     */
    public static Plugin start(PluginDescriptor descriptor)
    {
        try
        {
            final PluginLauncher launcher = new PluginLauncher(descriptor);

            // create plugin instance
            launcher.create();

            final Plugin plugin = launcher.plugin;

            if (plugin != null)
            {
                // register plugin
                Icy.getMainInterface().registerPlugin(launcher.plugin);
                // execute plugin
                launcher.execute();
            }

            return plugin;
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handleException(descriptor, t, true);
        }

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
