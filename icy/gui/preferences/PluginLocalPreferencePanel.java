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
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import icy.plugin.PluginUpdater;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 */
public class PluginLocalPreferencePanel extends PluginListPreferencePanel implements PluginLoaderListener,
        PluginInstallerListener, PluginRepositoryLoaderListener
{
    private enum PluginLocalState
    {
        NULL, UPDATING, REMOVING, HAS_UPDATE, NO_UPDATE, CHECKING_UPDATE
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6732331255351202922L;

    public static final String NODE_NAME = "Local Plugin";

    PluginLocalPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        PluginRepositoryLoader.addListener(this);
        PluginLoader.addListener(this);
        PluginInstaller.addListener(this);

        // remove last column not used here
        table.removeColumn(table.getColumn(columnIds[4]));

        action1Button.setText("Delete");
        action1Button.setVisible(true);
        action2Button.setText("Check update");
        action2Button.setVisible(true);

        pluginsChanged();
    }

    @Override
    protected void closed()
    {
        super.closed();

        PluginRepositoryLoader.removeListener(this);
        PluginInstaller.removeListener(this);
        PluginLoader.removeListener(this);
    }

    private PluginLocalState getPluginLocalState(PluginDescriptor plugin)
    {
        if (plugin != null)
        {
            if (!PluginRepositoryLoader.isLoaded())
                return PluginLocalState.CHECKING_UPDATE;

            if ((PluginInstaller.isDesinstallingPlugin(plugin)))
                return PluginLocalState.REMOVING;

            // get online version
            final PluginDescriptor onlinePlugin = PluginUpdater.getUpdate(plugin);

            // update available ?
            if (onlinePlugin != null)
            {
                if (PluginInstaller.isInstallingPlugin(onlinePlugin))
                    return PluginLocalState.UPDATING;

                return PluginLocalState.HAS_UPDATE;
            }

            if (plugin.isInstalled())
                return PluginLocalState.NO_UPDATE;

            // here the plugin has just been removed but plugin list is not yet updated
            return PluginLocalState.REMOVING;
        }

        return PluginLocalState.NULL;
    }

    @Override
    protected void doAction1()
    {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final List<PluginDescriptor> toRemove = new ArrayList<PluginDescriptor>();

        for (PluginDescriptor plugin : selectedPlugins)
            if (!PluginInstaller.isDesinstallingPlugin(plugin) && plugin.isInstalled())
                toRemove.add(plugin);

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
            final PluginDescriptor onlinePlugin = PluginUpdater.getUpdate(plugin);
            // install update
            if (onlinePlugin != null)
                PluginInstaller.install(onlinePlugin, true);
        }

        // refresh state
        refreshTableData();
    }

    @Override
    protected void repositoryChanged()
    {
        // do nothing here
    }

    @Override
    protected void reloadPlugins()
    {
        PluginLoader.reloadAsynch();
        // so we display the empty list during reload
        pluginsChanged();
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        if (plugin == null)
            return "";

        switch (getPluginLocalState(plugin))
        {
            case REMOVING:
                return "removing...";

            case CHECKING_UPDATE:
                return "checking...";

            case UPDATING:
                return "updating...";

            case HAS_UPDATE:
                return "update available";

            case NO_UPDATE:
                return "";
        }

        return "";
    }

    @Override
    protected List<PluginDescriptor> getPlugins()
    {
        // loading...
        if (PluginLoader.isLoading())
            return new ArrayList<PluginDescriptor>();

        final List<PluginDescriptor> result = PluginLoader.getPlugins(false);

        // only display installed plugins (this hide inner or dev plugins)
        for (int i = result.size() - 1; i >= 0; i--)
            if (!result.get(i).isInstalled())
                result.remove(i);

        return result;
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        super.updateButtonsStateInternal();

        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final boolean selected = (selectedPlugins.size() > 0);

        if (PluginLoader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
        }

        if (!selected)
        {
            action1Button.setEnabled(false);
            action2Button.setEnabled(false);
            return;
        }

        boolean removing = true;
        for (PluginDescriptor plugin : selectedPlugins)
        {
            if (!PluginInstaller.isDesinstallingPlugin(plugin))
            {
                removing = false;
                break;
            }
        }

        // special case where plugins are currently begin removed
        if (removing)
        {
            action1Button.setText("Removing...");
            action1Button.setEnabled(false);
        }
        else
        {
            action1Button.setText("Remove");
            action1Button.setEnabled(true);
        }

        PluginLocalState state = PluginLocalState.NULL;

        for (PluginDescriptor plugin : selectedPlugins)
        {
            switch (getPluginLocalState(plugin))
            {
                case CHECKING_UPDATE:
                    if ((state == PluginLocalState.NULL) || (state == PluginLocalState.NO_UPDATE))
                        state = PluginLocalState.CHECKING_UPDATE;
                    break;

                case UPDATING:
                    if ((state == PluginLocalState.NULL) || (state == PluginLocalState.NO_UPDATE)
                            || (state == PluginLocalState.CHECKING_UPDATE))
                        state = PluginLocalState.UPDATING;
                    break;

                case HAS_UPDATE:
                    state = PluginLocalState.HAS_UPDATE;
                    break;

                case NO_UPDATE:
                    if (state == PluginLocalState.NULL)
                        state = PluginLocalState.NO_UPDATE;
                    break;
            }
        }

        switch (state)
        {
            case CHECKING_UPDATE:
                action1Button.setEnabled(false);
                action2Button.setText("Checking...");
                action2Button.setEnabled(false);
                break;

            case UPDATING:
                action1Button.setEnabled(false);
                action2Button.setText("Updating...");
                action2Button.setEnabled(false);
                break;

            case HAS_UPDATE:
                action2Button.setText("Update");
                action2Button.setEnabled(true);
                break;

            case NO_UPDATE:
                action2Button.setText("No update");
                action2Button.setEnabled(false);
                break;

            case NULL:
                action1Button.setEnabled(false);
                action2Button.setEnabled(false);
                break;
        }

        // keep delete button enabled only if we can actually delete the plugin
        if (action1Button.isEnabled())
        {
            boolean canDelete = false;
            for (PluginDescriptor plugin : selectedPlugins)
            {
                if (plugin.isInstalled())
                {
                    canDelete = true;
                    break;
                }
            }

            action1Button.setEnabled(canDelete);
        }
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        pluginsChanged();
    }

    @Override
    public void pluginInstalled(PluginDescriptor plugin, boolean success)
    {
        updateButtonsState();
    }

    @Override
    public void pluginRemoved(PluginDescriptor plugin, boolean success)
    {
        updateButtonsState();
    }

    @Override
    public void pluginRepositeryLoaderChanged(PluginDescriptor plugin)
    {
        refreshTableData();
    }
}
