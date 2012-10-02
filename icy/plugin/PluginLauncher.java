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
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.plugin.interface_.PluginStartAsThread;
import icy.plugin.interface_.PluginThreaded;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;

/**
 * This class launch plugins and provide an Id to them.<br>
 * The launch can be in a thread or in the graphic thread.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class PluginLauncher
{
    private static class PluginThread extends Thread
    {
        final PluginDescriptor pluginDesc;
        final Plugin plugin;

        public PluginThread(PluginDescriptor pluginDesc, Plugin plugin, PluginThreaded runnable)
        {
            super(runnable, pluginDesc.getName());

            this.pluginDesc = pluginDesc;
            this.plugin = plugin;
        }

        @Override
        public void run()
        {
            try
            {
                if (plugin instanceof PluginImageAnalysis)
                {
                    PluginImageAnalysis pluginImageAnalysis = (PluginImageAnalysis) plugin;
                    pluginImageAnalysis.compute();
                }
            }
            catch (Throwable t)
            {
                IcyExceptionHandler.handleException(pluginDesc, t, true);
            }
        }
    }

    /**
     * Start the plugin.<br>
     * Returns the plugin instance (only meaningful for {@link PluginThreaded} plugin)
     */
    public static Plugin launch(PluginDescriptor pluginDesc)
    {
        try
        {
            final Plugin plugin = pluginDesc.getPluginClass().newInstance();

            // register plugin
            Icy.getMainInterface().registerPlugin(plugin);

            final Thread thread;

            if (plugin instanceof PluginThreaded)
                thread = new PluginThread(pluginDesc, plugin, (PluginThreaded) plugin);
            else
                thread = new PluginThread(pluginDesc, plugin, null);

            // keep backward compatibility
            if ((plugin instanceof PluginThreaded) || (plugin instanceof PluginStartAsThread))
                // launch as thread
                thread.start();
            else
                // direct launch in EDT now (no thread creation)
                ThreadUtil.invokeNow(thread);

            return plugin;
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handleException(pluginDesc, t, true);
        }

        return null;
    }
}
