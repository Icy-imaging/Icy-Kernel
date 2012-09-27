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
package icy.gui.plugin;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginUpdater;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

/**
 * This class create a report from a plugin crash and ask the
 * user if he wants to send it to the dev team of the plugin.
 * 
 * @author Fabrice de Chaumont & Stephane<br>
 */
public class PluginErrorReport
{
    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param title
     *        Error title if any
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String title,
            final String message)
    {
        // cannot be reported...
        if ((plugin == null) && StringUtil.isEmpty(devId))
            return;

        if (reportFrameAlreadyExist(plugin, devId))
            return;

        // always do that in background process
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                if (plugin != null)
                {
                    final CancelableProgressFrame info = new CancelableProgressFrame("Plugin '" + plugin.getName()
                            + "' has crashed, searching for update...");

                    // wait for online basic info loaded
                    PluginRepositoryLoader.waitBasicLoaded();

                    PluginDescriptor onlinePlugin = null;

                    try
                    {
                        // search for update
                        if (!info.isCancelRequested())
                            onlinePlugin = PluginUpdater.getUpdate(plugin);
                    }
                    finally
                    {
                        info.close();
                    }

                    if (!info.isCancelRequested())
                    {
                        // update found
                        if (onlinePlugin != null)
                        {
                            PluginInstaller.install(onlinePlugin, false);
                            new AnnounceFrame(
                                    "The plugin crashed but a new version has been found, try it again when installation is done",
                                    10);
                        }
                        else
                        {
                            // display report as no update were found
                            ThreadUtil.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    new PluginErrorReportFrame(plugin, null, title, message);
                                }
                            });
                        }
                    }
                }
                else
                {
                    // directly display report frame
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            new PluginErrorReportFrame(null, devId, title, message);
                        }
                    });
                }
            }
        });
    }

    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String message)
    {
        report(plugin, devId, null, message);
    }

    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param message
     *        Error message to report
     */
    public static void report(PluginDescriptor plugin, String message)
    {
        report(plugin, null, null, message);
    }

    /**
     * This function test if we already have an active report for the specified plugin
     */
    private static boolean reportFrameAlreadyExist(PluginDescriptor plugin, String devId)
    {
        for (IcyFrame frame : IcyFrame.getAllFrames(PluginErrorReportFrame.class))
        {
            final PluginErrorReportFrame f = (PluginErrorReportFrame) frame;

            if (plugin != null)
            {
                if (StringUtil.equals(f.getPlugin().getName(), plugin.getName()))
                    return true;
            }
            else
            {
                if (StringUtil.equals(f.getDevId(), devId))
                    return true;
            }
        }

        return false;
    }
}
