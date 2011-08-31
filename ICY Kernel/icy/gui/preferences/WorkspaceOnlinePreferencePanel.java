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

import icy.plugin.PluginRepositoryLoader;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.thread.ThreadUtil;
import icy.workspace.Workspace;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerEvent;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerListener;
import icy.workspace.WorkspaceLoader;
import icy.workspace.WorkspaceRepositoryLoader;
import icy.workspace.WorkspaceRepositoryLoader.WorkspaceRepositoryLoaderEvent;
import icy.workspace.WorkspaceRepositoryLoader.WorkspaceRepositoryLoaderListener;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class WorkspaceOnlinePreferencePanel extends WorkspaceListPreferencePanel implements
        WorkspaceRepositoryLoaderListener, WorkspaceInstallerListener
{
    private enum WorkspaceOnlineState
    {
        NULL, INSTALLING, HAS_INSTALL, INSTALLED
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5061606004171912231L;

    public static final String NODE_NAME = "Online Workspace";

    private final WorkspaceRepositoryLoader workspaceLoader;
    // used for workspace installation
    private final PluginRepositoryLoader pluginLoader;

    WorkspaceOnlinePreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME);

        workspaceLoader = new WorkspaceRepositoryLoader();
        workspaceLoader.addListener(this);
        pluginLoader = new PluginRepositoryLoader();
        WorkspaceInstaller.addListener(this);

        repositoryPanel.setVisible(true);
        action1Button.setText("Install");
        action1Button.setVisible(true);

        reloadWorkspaces();
        updateButtonsState();
        updateRepositories();
    }

    @Override
    protected void closed()
    {
        super.closed();

        workspaceLoader.removeListener(this);
        WorkspaceInstaller.removeListener(this);
    }

    private WorkspaceOnlineState getWorkspaceOnlineState(Workspace workspace)
    {
        if (workspace == null)
            return WorkspaceOnlineState.NULL;

        if (WorkspaceLoader.isLoaded(workspace))
            return WorkspaceOnlineState.INSTALLED;

        if ((WorkspaceInstaller.isInstallingWorkspace(workspace)))
            return WorkspaceOnlineState.INSTALLING;

        return WorkspaceOnlineState.HAS_INSTALL;
    }

    @Override
    protected int getColumnCount()
    {
        return 3;
    }

    @Override
    protected void doAction1(Workspace workspace)
    {
        switch (getWorkspaceOnlineState(workspace))
        {
            case HAS_INSTALL:
                // install workspace
                WorkspaceInstaller.install(workspace, pluginLoader, true);
                // refresh state
                refreshTableData();
                updateButtonsState();
                break;

            case INSTALLED:
                // desinstall workspace
                WorkspaceInstaller.desinstall(workspace, true);
                // refresh state
                refreshTableData();
                updateButtonsState();
                break;
        }
    }

    @Override
    protected void repositoryChanged()
    {
        reloadWorkspaces();
    }

    @Override
    protected void reloadWorkspaces()
    {
        // get selected repository
        final Object selectedItem = repository.getSelectedItem();

        // load workspaces from repository
        if (selectedItem != null)
        {
            RepositoryInfo repinfo = (RepositoryInfo) selectedItem;

            workspaceLoader.clear();
            workspaceLoader.load(repinfo, true);
            pluginLoader.clear();
            pluginLoader.load(repinfo, true, false, false);
        }

        updateButtonsState();
    }

    @Override
    protected String getStateValue(Workspace workspace)
    {
        switch (getWorkspaceOnlineState(workspace))
        {
            case INSTALLING:
                return "installing...";

            case INSTALLED:
                return "installed";
        }

        return "";
    }

    @Override
    protected ArrayList<Workspace> getWorkspaces()
    {
        return workspaceLoader.getWorkspaces();
    }

    @Override
    protected void updateButtonsState()
    {
        if (workspaceLoader.isLoading())
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

        final Workspace workspace = getSelectedWorkspace();

        if (workspace == null)
        {
            action1Button.setEnabled(false);
            return;
        }

        switch (getWorkspaceOnlineState(workspace))
        {
            case INSTALLING:
                action1Button.setText("Installing...");
                action1Button.setEnabled(false);
                break;

            case HAS_INSTALL:
                action1Button.setText("Install");
                action1Button.setEnabled(true);
                break;

            case INSTALLED:
                action1Button.setText("Delete");
                action1Button.setEnabled(true);
                break;

            case NULL:
                action1Button.setEnabled(false);
                break;
        }
    }

    @Override
    public void workspaceRepositeryLoaderChanged(WorkspaceRepositoryLoaderEvent e)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                workspacesChanged();
            }
        });
    }

    @Override
    public void workspaceInstalled(WorkspaceInstallerEvent e)
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
    public void workspaceRemoved(WorkspaceInstallerEvent e)
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
