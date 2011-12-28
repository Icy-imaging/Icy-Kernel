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

import icy.preferences.WorkspaceLocalPreferences;
import icy.workspace.Workspace;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerEvent;
import icy.workspace.WorkspaceInstaller.WorkspaceInstallerListener;
import icy.workspace.WorkspaceLoader;
import icy.workspace.WorkspaceLoader.WorkspaceLoaderEvent;
import icy.workspace.WorkspaceLoader.WorkspaceLoaderListener;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class WorkspaceLocalPreferencePanel extends WorkspaceListPreferencePanel implements WorkspaceLoaderListener,
        WorkspaceInstallerListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -7178443947421949836L;

    public static final String NODE_NAME = "Local Workspace";

    private ArrayList<String> activesWorkspace;

    WorkspaceLocalPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME);

        action1Button.setText("Delete");
        action1Button.setVisible(true);

        refreshWorkspaces();
        updateButtonsState();

        WorkspaceLoader.addListener(this);
        WorkspaceInstaller.addListener(this);

    }

    @Override
    protected void closed()
    {
        super.closed();

        WorkspaceLoader.removeListener(this);
        WorkspaceInstaller.removeListener(this);
    }

    @Override
    protected int getColumnCount()
    {
        return 4;
    }

    @Override
    protected void doAction1(Workspace workspace)
    {
        // desinstall workspace
        WorkspaceInstaller.desinstall(workspace, true);
        // refresh state
        refreshTableData();
        updateButtonsState();
    }

    @Override
    protected Boolean isWorkspaceEnable(Workspace workspace)
    {
        return Boolean.valueOf(activesWorkspace.contains(workspace.getName()));
    }

    @Override
    protected void setWorkspaceEnable(Workspace workspace, Boolean value)
    {
        final String name = workspace.getName();

        if (value.booleanValue())
        {
            if (!activesWorkspace.contains(name))
                activesWorkspace.add(name);
        }
        else
            activesWorkspace.remove(name);
    }

    private void cleanActivesWorkspace(ArrayList<Workspace> workspaces)
    {
        // clean active workspace list
        for (int i = activesWorkspace.size() - 1; i >= 0; i--)
        {
            final String name = activesWorkspace.get(i);

            if (Workspace.getWorkspace(workspaces, name) == null)
                activesWorkspace.remove(i);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.preferences.WorkspaceListPreferencePanel#load()
     */
    @Override
    protected void load()
    {
        super.load();

        activesWorkspace = WorkspaceLocalPreferences.getActivesWorkspace();
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.gui.preferences.WorkspaceListPreferencePanel#save()
     */
    @Override
    protected void save()
    {
        WorkspaceLocalPreferences.setActivesWorkspace(activesWorkspace);

        super.save();
    }

    @Override
    protected void repositoryChanged()
    {
        // do nothing here
    }

    @Override
    protected void reloadWorkspaces()
    {
        WorkspaceLoader.reload_asynch();
        updateButtonsState();
    }

    @Override
    protected String getStateValue(Workspace workspace)
    {
        if (workspace == null)
            return "";

        if (WorkspaceInstaller.isDesinstallingWorkspace(workspace))
            return "Deleting...";

        return "";
    }

    @Override
    protected ArrayList<Workspace> getWorkspaces()
    {
        final ArrayList<Workspace> result = WorkspaceLoader.getWorkspaces();

        // we don't want to see here WORKSPACE_SYSTEM
        Workspace.removeWorkspace(result, Workspace.WORKSPACE_SYSTEM_NAME);

        cleanActivesWorkspace(result);

        return result;
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        if (WorkspaceLoader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
        }

        final Workspace workspace = getSelectedWorkspace();

        if (workspace == null)
        {
            action1Button.setText("Delete");
            action1Button.setEnabled(false);
            return;
        }

        // special case where workspace is currently begin removed
        if (WorkspaceInstaller.isDesinstallingWorkspace(workspace))
        {
            action1Button.setText("Deleting...");
            action1Button.setEnabled(false);
        }
        else
        {
            action1Button.setText("Delete");
            action1Button.setEnabled(true);
        }
    }

    @Override
    public void workspaceLoaderChanged(WorkspaceLoaderEvent e)
    {
        workspacesChanged();
    }

    @Override
    public void workspaceInstalled(WorkspaceInstallerEvent e)
    {
        final Workspace workspace = e.getWorkspace();

        // enable the installed workspace by default
        setWorkspaceEnable(workspace, Boolean.TRUE);
        // workspace setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
        updateButtonsState();
    }

    @Override
    public void workspaceRemoved(WorkspaceInstallerEvent e)
    {
        // workspace setting changed, restart needed
        getPreferenceFrame().setNeedRestart();
        updateButtonsState();
    }
}
