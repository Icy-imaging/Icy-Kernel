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
import icy.plugin.interface_.PluginOwnThread;
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

        public PluginThread(PluginDescriptor pluginDesc, Plugin plugin)
        {
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
                IcyExceptionHandler.handlePluginException(pluginDesc, t, true);
            }
        }
    }

    /**
     * Start the plugin
     */
    public synchronized static void launch(PluginDescriptor pluginDesc)
    {
        try
        {
            final Plugin plugin = pluginDesc.getPluginClass().newInstance();

            // register plugin
            Icy.getMainInterface().registerPlugin(plugin);

            final Thread thread = new PluginThread(pluginDesc, plugin);

            if (plugin instanceof PluginOwnThread)
                // launch as thread
                thread.start();
            else
                // direct launch in EDT now (no thread creation)
                ThreadUtil.invokeNow(thread);
        }
        catch (Throwable t)
        {
            IcyExceptionHandler.handlePluginException(pluginDesc, t, true);
        }
    }
}
