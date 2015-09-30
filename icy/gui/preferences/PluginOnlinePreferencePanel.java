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
package icy.gui.preferences;

import icy.gui.dialog.ConfirmDialog;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginInstaller.PluginInstallerListener;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 */
public class PluginOnlinePreferencePanel extends PluginListPreferencePanel implements PluginRepositoryLoaderListener,
        PluginInstallerListener
{
    private enum PluginOnlineState
    {
        NULL, INSTALLING, REMOVING, HAS_INSTALL, INSTALLED, INSTALLED_FAULTY, OLDER, NEWER
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7737976340704271890L;

    public static final String NODE_NAME = "Online Plugin";

    PluginOnlinePreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        PluginRepositoryLoader.addListener(this);
        PluginInstaller.addListener(this);

        // remove last column not used here
        table.removeColumn(table.getColumn(columnIds[4]));

        repositoryPanel.setVisible(true);
        action1Button.setText("Delete");
        action1Button.setVisible(true);
        action2Button.setText("Install");
        action2Button.setVisible(true);

        updateButtonsState();
        updateRepositories();
    }

    @Override
    protected void closed()
    {
        super.closed();

        PluginRepositoryLoader.removeListener(this);
        PluginInstaller.removeListener(this);
    }

    private PluginOnlineState getPluginOnlineState(PluginDescriptor plugin)
    {
        if (plugin == null)
            return PluginOnlineState.NULL;

        if ((PluginInstaller.isInstallingPlugin(plugin)))
            return PluginOnlineState.INSTALLING;
        if ((PluginInstaller.isDesinstallingPlugin(plugin)))
            return PluginOnlineState.REMOVING;

        // has a local version ?
        if (plugin.isInstalled())
        {
            // get local version
            final PluginDescriptor localPlugin = PluginLoader.getPlugin(plugin.getClassName());

            if (localPlugin != null)
            {
                if (plugin.equals(localPlugin))
                    return PluginOnlineState.INSTALLED;
                if (plugin.isOlder(localPlugin))
                    return PluginOnlineState.OLDER;
                if (plugin.isNewer(localPlugin))
                    return PluginOnlineState.NEWER;
            }

            // local version not loaded --> faulty
            return PluginOnlineState.INSTALLED_FAULTY;
        }

        return PluginOnlineState.HAS_INSTALL;
    }

    @Override
    protected void doAction1()
    {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final List<PluginDescriptor> toRemove = new ArrayList<PluginDescriptor>();

        for (PluginDescriptor plugin : selectedPlugins)
        {
            final PluginOnlineState state = getPluginOnlineState(plugin);

            // remove plugin
            if ((state == PluginOnlineState.INSTALLED) || (state == PluginOnlineState.INSTALLED_FAULTY))
                toRemove.add(plugin);
        }

        // nothing to remove
        if (toRemove.isEmpty())
            return;

        // get dependants plugins
        final List<PluginDescriptor> dependants = PluginInstaller.getLocalDependenciesFrom(toRemove);
        // delete the one we plan to remove
        dependants.removeAll(toRemove);

        String message = "<html>";

        if (!dependants.isEmpty())
        {
            message = message + "The following plugin(s) won't work anymore :<br>";

            for (PluginDescriptor depPlug : dependants)
                message = message + depPlug.getName() + " " + depPlug.getVersion() + "<br>";

            message = message + "<br>";
        }

        message = message + "Are you sure you want to remove selected plugin(s) ?</html>";

        if (ConfirmDialog.confirm(message))
        {
            // remove plugins
            for (PluginDescriptor plugin : toRemove)
                PluginInstaller.desinstall(plugin, false, true);
        }

        // refresh state
        refreshTableData();
    }

    @Override
    protected void doAction2()
    {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

        for (PluginDescriptor plugin : selectedPlugins)
        {
            final PluginOnlineState state = getPluginOnlineState(plugin);

            if ((state == PluginOnlineState.HAS_INSTALL) || (state == PluginOnlineState.NEWER)
                    || (state == PluginOnlineState.OLDER))
            {
                final boolean doInstall;

                if (state == PluginOnlineState.OLDER)
                    doInstall = ConfirmDialog
                            .confirm("You'll replace your plugin by an older version !\nAre you sure you want to continue ?");
                else
                    doInstall = true;

                // install plugin
                if (doInstall)
                    PluginInstaller.install(plugin, true);
            }
        }

        // refresh state
        refreshTableData();
    }

    @Override
    protected void repositoryChanged()
    {
        refreshPlugins();
    }

    @Override
    protected void reloadPlugins()
    {
        PluginRepositoryLoader.reload();
        // so we display the empty list during reload
        pluginsChanged();
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        switch (getPluginOnlineState(plugin))
        {
            case INSTALLING:
                return "installing...";

            case REMOVING:
                return "removing...";

            case NEWER:
                return "update available";

            case OLDER:
                return "outdated";

            case INSTALLED:
                return "installed";

            case INSTALLED_FAULTY:
                return "faulty";
        }

        return "";
    }

    @Override
    protected List<PluginDescriptor> getPlugins()
    {
        // loading...
        if (!PluginRepositoryLoader.isBasicLoaded())
            return new ArrayList<PluginDescriptor>();

        // get selected repository
        final Object selectedItem = repository.getSelectedItem();

        // load plugins from repository
        if (selectedItem != null)
            return PluginRepositoryLoader.getPlugins((RepositoryInfo) selectedItem);

        return PluginRepositoryLoader.getPlugins();
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        super.updateButtonsStateInternal();

        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final boolean selected = (selectedPlugins.size() > 0);

        if (PluginRepositoryLoader.isBasicLoaded())
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
            repository.setEnabled(true);
        }
        else
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
            repository.setEnabled(false);
        }

        if (!selected)
        {
            action1Button.setEnabled(false);
            action2Button.setEnabled(false);
            return;
        }

        PluginOnlineState state;

        state = PluginOnlineState.NULL;
        for (PluginDescriptor plugin : selectedPlugins)
        {
            switch (getPluginOnlineState(plugin))
            {
                case REMOVING:
                    if (state == PluginOnlineState.NULL)
                        state = PluginOnlineState.REMOVING;
                    break;

                case INSTALLED:
                case INSTALLED_FAULTY:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.REMOVING))
                        state = PluginOnlineState.INSTALLED;
                    break;
            }
        }

        // some online plugins are already installed ?
        switch (state)
        {
            case REMOVING:
                // special case where plugins are currently begin removed
                action1Button.setText("Deleting...");
                action1Button.setEnabled(false);
                break;

            case INSTALLED:
                action1Button.setText("Delete");
                action1Button.setEnabled(true);
                break;

            case NULL:
                action1Button.setText("Delete");
                action1Button.setEnabled(false);
                break;
        }

        state = PluginOnlineState.NULL;
        for (PluginDescriptor plugin : selectedPlugins)
        {
            switch (getPluginOnlineState(plugin))
            {
                case INSTALLING:
                    if (state == PluginOnlineState.NULL)
                        state = PluginOnlineState.INSTALLING;
                    break;

                case OLDER:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.INSTALLING))
                        state = PluginOnlineState.OLDER;
                    break;

                case NEWER:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.INSTALLING)
                            || (state == PluginOnlineState.OLDER))
                        state = PluginOnlineState.NEWER;
                    break;

                case HAS_INSTALL:
                    state = PluginOnlineState.HAS_INSTALL;
                    break;
            }
        }

        switch (state)
        {
            case INSTALLING:
                action2Button.setText("Installing...");
                action2Button.setEnabled(false);
                break;

            case HAS_INSTALL:
                action2Button.setText("Install");
                action2Button.setEnabled(true);
                break;

            case OLDER:
                action2Button.setText("Revert");
                action2Button.setEnabled(true);
                break;

            case NEWER:
                action2Button.setText("Update");
                action2Button.setEnabled(true);
                break;

            case NULL:
                action2Button.setText("Install");
                action2Button.setEnabled(false);
                break;
        }
    }

    @Override
    public void pluginRepositeryLoaderChanged(PluginDescriptor plugin)
    {
        if (plugin != null)
        {
            final int ind = getPluginModelIndex(plugin.getClassName());

            if (ind != -1)
            {
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            tableModel.fireTableRowsUpdated(ind, ind);
                        }
                        catch (Exception e)
                        {
                            // ignore possible exception here
                        }
                    }
                });
            }
        }
        else
            pluginsChanged();
    }

    @Override
    public void pluginInstalled(PluginDescriptor plugin, boolean success)
    {
        refreshTableData();
    }

    @Override
    public void pluginRemoved(PluginDescriptor plugin, boolean success)
    {
        refreshTableData();
    }
}
