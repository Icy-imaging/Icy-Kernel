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

import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.workspace.Workspace;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerEvent;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerListener;
import icy.workspace.WorkspaceLoader;
import icy.workspace.WorkspaceRepositoryLoader;
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
        NULL, INSTALLING, DELETING, HAS_INSTALL, INSTALLED
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5061606004171912231L;

    public static final String NODE_NAME = "Online Workspace";

    WorkspaceOnlinePreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME);

        WorkspaceRepositoryLoader.addListener(this);
        WorkspaceInstaller.addListener(this);

        repositoryPanel.setVisible(true);
        action1Button.setText("Install");
        action1Button.setVisible(true);

        updateButtonsState();
        updateRepositories();
    }

    @Override
    protected void closed()
    {
        super.closed();

        WorkspaceRepositoryLoader.removeListener(this);
        WorkspaceInstaller.removeListener(this);
    }

    private WorkspaceOnlineState getWorkspaceOnlineState(Workspace workspace)
    {
        if (workspace == null)
            return WorkspaceOnlineState.NULL;

        if ((WorkspaceInstaller.isInstallingWorkspace(workspace)))
            return WorkspaceOnlineState.INSTALLING;

        if ((WorkspaceInstaller.isDesinstallingWorkspace(workspace)))
            return WorkspaceOnlineState.DELETING;

        if (WorkspaceLoader.isLoaded(workspace))
            return WorkspaceOnlineState.INSTALLED;

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
                WorkspaceInstaller.install(workspace, true);
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
        refreshWorkspaces();
        refreshTableData();
    }

    @Override
    protected void reloadWorkspaces()
    {
        WorkspaceRepositoryLoader.reload();
        updateButtonsState();
    }

    @Override
    protected String getStateValue(Workspace workspace)
    {
        switch (getWorkspaceOnlineState(workspace))
        {
            case INSTALLING:
                return "installing...";

            case DELETING:
                return "deleting...";

            case INSTALLED:
                return "installed";
        }

        return "";
    }

    @Override
    protected ArrayList<Workspace> getWorkspaces()
    {
        // get selected repository
        final Object selectedItem = repository.getSelectedItem();

        // load workspaces from selected repository
        if (selectedItem != null)
            return WorkspaceRepositoryLoader.getWorkspaces((RepositoryInfo) selectedItem);

        return WorkspaceRepositoryLoader.getWorkspaces();
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        if (WorkspaceRepositoryLoader.isLoading())
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

            case DELETING:
                action1Button.setText("Deleting...");
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
    public void workspaceRepositeryLoaderChanged()
    {
        workspacesChanged();
    }

    @Override
    public void workspaceInstalled(WorkspaceInstallerEvent e)
    {
        refreshTableData();
        updateButtonsState();
    }

    @Override
    public void workspaceRemoved(WorkspaceInstallerEvent e)
    {
        refreshTableData();
        updateButtonsState();
    }

}
