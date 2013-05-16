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

import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.plugin.PluginUpdateFrame;
import icy.network.NetworkUtil;
import icy.system.thread.SingleProcessor;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Plugin updater class.
 * 
 * @author Stephane.D
 */
public class PluginUpdater
{
    private static class Checker implements Runnable
    {
        private boolean showProgress;
        private boolean auto;

        public Checker(boolean showProgress, boolean auto)
        {
            super();

            this.showProgress = showProgress;
            this.auto = auto;
        }

        @Override
        public void run()
        {
            processCheckUpdate(showProgress, auto);
        }
    }

    private static final int ANNOUNCE_SHOWTIME = 15;

    private static final SingleProcessor processor = new SingleProcessor(false, "Plugin updater");

    static
    {
        // we want the processor to stay alive
        processor.setKeepAliveTime(1, TimeUnit.DAYS);
    }

    /**
     * return true if we are currently checking for update
     */
    public static boolean isCheckingForUpdate()
    {
        return processor.isProcessing();
    }

    /**
     * Do the check update process
     */
    public static void checkUpdate(boolean showProgress, boolean auto)
    {
        processor.addTask(new Checker(showProgress, auto));
    }

    /**
     * Get update for the specified plugin.
     * 
     * @param plugin
     *        local plugin we are looking update for
     * @return
     *         plugin descriptor of update if any (null if no update)
     */
    public static PluginDescriptor getUpdate(PluginDescriptor plugin)
    {
        // find equivalent online plugins
        final ArrayList<PluginDescriptor> onlinePlugins = PluginRepositoryLoader.getPlugins(plugin.getClassName());
        final PluginDescriptor onlinePlugin;

        // get the last version found
        if (onlinePlugins.size() > 0)
        {
            PluginDescriptor lastVersion = null;

            for (PluginDescriptor currentVersion : onlinePlugins)
                if ((lastVersion == null) || currentVersion.isNewer(lastVersion))
                    lastVersion = currentVersion;

            onlinePlugin = lastVersion;
        }
        else
            // not found in repositories
            onlinePlugin = null;

        // we have an update available ?
        if ((onlinePlugin != null) && onlinePlugin.getVersion().isGreater(plugin.getVersion()))
            return onlinePlugin;

        return null;
    }

    /**
     * Update the specified list of plugins.
     */
    public static void updatePlugins(ArrayList<PluginDescriptor> plugins, boolean showProgress)
    {
        final boolean b = PluginLoader.getLogError();

        PluginLoader.setLogError(false);
        try
        {
            // update plugins with ordered dependencies
            for (PluginDescriptor plugin : PluginInstaller.getDependenciesOrderedList(plugins))
                PluginInstaller.install(plugin, showProgress);
        }
        finally
        {
            PluginLoader.setLogError(b);
            PluginLoader.reloadAsynch();
        }
    }

    static void processCheckUpdate(boolean showProgress, boolean auto)
    {
        final ArrayList<PluginDescriptor> toInstallPlugins = new ArrayList<PluginDescriptor>();
        final ArrayList<PluginDescriptor> localPlugins = PluginLoader.getPlugins(false);
        final ProgressFrame checkingFrame;

        if (showProgress)
            checkingFrame = new CancelableProgressFrame("checking for plugins update...");
        else
            checkingFrame = null;
        try
        {
            // reload online plugins from all active repositories
            PluginRepositoryLoader.reload();
            // wait for basic infos
            PluginRepositoryLoader.waitBasicLoaded();

            if (PluginRepositoryLoader.failed())
            {
                if (showProgress)
                {
                    if (!NetworkUtil.hasInternetAccess())
                        new AnnounceFrame("You are not connected to internet.", 10);
                    else
                        new AnnounceFrame("Can't access the repositories... You should verify your connection.", 10);
                }

                return;
            }

            for (PluginDescriptor localPlugin : localPlugins)
            {
                // find update
                final PluginDescriptor onlinePlugin = getUpdate(localPlugin);

                // update found, add to the list
                if (onlinePlugin != null)
                {
                    // we load complete descriptor so we will have the changeslog
                    onlinePlugin.loadDescriptor();
                    toInstallPlugins.add(onlinePlugin);
                }
            }
        }
        finally
        {
            if (showProgress)
                checkingFrame.close();
        }

        // some updates availables ?
        if (!toInstallPlugins.isEmpty())
        {
            if (auto)
            {
                // automatically install all updates (orderer depending dependencies)
                updatePlugins(toInstallPlugins, true);
                // updatePlugins(toInstallPlugins, showProgress);
            }
            else
            {
                // show announcement for 15 seconds
                new AnnounceFrame(toInstallPlugins.size() + " plugin update are availables", "View", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show pluginInstaller frame
                        new PluginUpdateFrame(toInstallPlugins);
                    }
                }, ANNOUNCE_SHOWTIME);
            }
        }
        else
        {
            // inform that there is no plugin update available
            if (showProgress)
                new AnnounceFrame("No plugin udpate available", 10);
        }
    }
}
