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
package icy.gui.preferences;

import icy.gui.dialog.ConfirmDialog;
import icy.main.Icy;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginInstaller.PluginInstallerListener;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderEvent;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.thread.ThreadUtil;
import icy.update.IcyUpdater;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class PluginOnlinePreferencePanel extends PluginListPreferencePanel implements PluginRepositoryLoaderListener,
        PluginInstallerListener
{
    private enum PluginOnlineState
    {
        NULL, INSTALLING, HAS_INSTALL, INSTALLED, CHECKING_KERNEL, OLDER, NEWER
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7737976340704271890L;

    public static final String NODE_NAME = "Online Plugin";

    /**
     * internal
     */
    private final PluginRepositoryLoader loader;

    PluginOnlinePreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME);

        loader = new PluginRepositoryLoader();
        loader.addListener(this);
        PluginInstaller.addListener(this);

        repositoryPanel.setVisible(true);
        action1Button.setText("Install");
        action1Button.setVisible(true);
        action2Button.setVisible(false);

        reloadPlugins();
        updateButtonsState();
        updateRepositories();
    }

    @Override
    protected void closed()
    {
        super.closed();

        loader.removeListener(this);
        PluginInstaller.removeListener(this);
    }

    private PluginOnlineState getPluginOnlineState(PluginDescriptor plugin)
    {
        if (plugin == null)
            return PluginOnlineState.NULL;

        if ((PluginInstaller.isInstallingPlugin(plugin)))
            return PluginOnlineState.INSTALLING;

        // if (PluginLoader.isLoaded(plugin, false))
        // return PluginOnlineState.INSTALLED;

        // required kernel version > current kernel version
        if (plugin.getKernelVersion().isGreater(Icy.version) && IcyUpdater.isCheckingForUpdate())
            return PluginOnlineState.CHECKING_KERNEL;

        // get local version
        final PluginDescriptor localPlugin = PluginLoader.getPlugin(plugin.getClassName());

        // has a local version ?
        if (localPlugin != null)
        {
            if (plugin.equals(localPlugin))
                return PluginOnlineState.INSTALLED;
            if (plugin.isOlder(localPlugin))
                return PluginOnlineState.OLDER;
            if (plugin.isNewer(localPlugin))
                return PluginOnlineState.NEWER;
        }

        return PluginOnlineState.HAS_INSTALL;
    }

    @Override
    protected void doAction1(PluginDescriptor plugin)
    {
        final PluginOnlineState state = getPluginOnlineState(plugin);

        switch (state)
        {
            case HAS_INSTALL:
            case NEWER:
            case OLDER:
                // required kernel version is > current kernel version
                if (plugin.getKernelVersion().isGreater(Icy.version))
                {
                    if (ConfirmDialog
                            .confirm("Plugin installation",
                                    "This plugin requires a newer version of the application.\nDo you want to check for application update now ?"))
                    {
                        IcyUpdater.checkUpdate(true, false);
                        // refresh state
                        refreshTableData();
                        updateButtonsState();
                    }
                }
                else
                {
                    final boolean doInstall;
                    if (state == PluginOnlineState.OLDER)
                        doInstall = ConfirmDialog
                                .confirm("You'll replace your plugin by an older version !\nAre you sure you want to continue ?");
                    else
                        doInstall = true;

                    if (doInstall)
                    {
                        // install plugin
                        PluginInstaller.install(loader, plugin, true);
                        // refresh state
                        refreshTableData();
                        updateButtonsState();
                    }
                }
                break;

            case INSTALLED:
                // desinstall plugin
                PluginInstaller.desinstall(plugin, true);
                // refresh state
                refreshTableData();
                updateButtonsState();
                break;
        }
    }

    @Override
    protected void doAction2(PluginDescriptor plugin)
    {
        // nothing here
    }

    @Override
    protected void repositoryChanged()
    {
        reloadPlugins();
    }

    @Override
    protected void reloadPlugins()
    {
        // get selected repository
        final Object selectedItem = repository.getSelectedItem();

        // load plugins from repository
        if (selectedItem != null)
        {
            loader.clear();
            loader.load((RepositoryInfo) selectedItem, true, true, true);
        }

        updateButtonsState();
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        switch (getPluginOnlineState(plugin))
        {
            case INSTALLING:
                return "installing...";

            case NEWER:
                return "update available";

            case OLDER:
                return "outdated";

            case INSTALLED:
                return "installed";

            case CHECKING_KERNEL:
                return "checking kernel...";
        }

        return "";
    }

    @Override
    protected ArrayList<PluginDescriptor> getPlugins()
    {
        return loader.getPlugins();
    }

    @Override
    protected void updateButtonsState()
    {
        super.updateButtonsState();

        if (loader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
            repository.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
            repository.setEnabled(true);
        }

        final PluginDescriptor plugin = getSelectedPlugin();

        if (plugin == null)
        {
            action1Button.setEnabled(false);
            return;
        }

        switch (getPluginOnlineState(plugin))
        {
            case INSTALLING:
                action1Button.setText("Installing...");
                action1Button.setEnabled(false);
                break;

            case CHECKING_KERNEL:
                action1Button.setText("Checking...");
                action1Button.setEnabled(false);
                break;

            case HAS_INSTALL:
                action1Button.setText("Install");
                action1Button.setEnabled(true);
                break;

            case INSTALLED:
                // special case where plugin is currently begin removed
                if (PluginInstaller.isDesinstallingPlugin(plugin))
                {
                    action1Button.setText("Deleting...");
                    action1Button.setEnabled(false);
                }
                else
                {
                    action1Button.setText("Delete");
                    action1Button.setEnabled(plugin.isInstalled());
                }
                break;

            case OLDER:
                action1Button.setText("Revert");
                action1Button.setEnabled(true);
                break;

            case NEWER:
                action1Button.setText("Update");
                action1Button.setEnabled(true);
                break;

            case NULL:
                action1Button.setEnabled(false);
                break;
        }
    }

    @Override
    public void pluginRepositeryLoaderChanged(PluginRepositoryLoaderEvent e)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                pluginsChanged();
            }
        });
    }

    @Override
    public void pluginInstalled(boolean success)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableData();
                updateButtonsState();
            }
        });
    }

    @Override
    public void pluginRemoved(boolean success)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableData();
                updateButtonsState();
            }
        });
    }

}
