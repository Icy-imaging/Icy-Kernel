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

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
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
    public static void report(final PluginDescriptor plugin, final String message)
    {
        if (reportFrameAlreadyExist(plugin))
            return;

        final CancelableProgressFrame info = new CancelableProgressFrame("Plugin '" + plugin.getName()
                + "' has crashed, searching for update...");

        // always do that in background process
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
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
                        // confim and install
                        if (IdConfirmDialog.confirm("Plugin update", "An update is available for '" + plugin.getName()
                                + "'.\n"
                                + "It is highly recommended to install it as you meet problem with current version.\n"
                                + "Do you want to install the update ?", "updatePluginAfterError"))
                            PluginInstaller.install(onlinePlugin, false);
                    }
                    else
                    {
                        // display report as no update were found
                        ThreadUtil.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                new PluginErrorReportFrame(plugin, message);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * This function test if we already have an active report for the specified plugin
     */
    private static boolean reportFrameAlreadyExist(PluginDescriptor plugin)
    {
        for (IcyFrame frame : IcyFrame.getAllFrames(PluginErrorReportFrame.class))
            if (StringUtil.equals(((PluginErrorReportFrame) frame).getPlugin().getName(), plugin.getName()))
                return true;

        return false;
    }
}
