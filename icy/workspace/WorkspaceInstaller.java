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
package icy.workspace;

import icy.file.FileUtil;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.frame.progress.SuccessfullAnnounceFrame;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLoader;
import icy.preferences.WorkspaceLocalPreferences;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.workspace.Workspace.TaskDefinition.BandDefinition.ItemDefinition;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

/**
 * @author Stephane
 */
public class WorkspaceInstaller implements Runnable
{
    public static interface WorkspaceInstallerListener extends EventListener
    {
        public void workspaceInstalled(WorkspaceInstallerEvent e);

        public void workspaceRemoved(WorkspaceInstallerEvent e);
    }

    public static class WorkspaceInstallerEvent
    {
        private final Workspace workspace;
        private final boolean successed;

        public WorkspaceInstallerEvent(Workspace workspace, boolean successed)
        {
            super();

            this.workspace = workspace;
            this.successed = successed;
        }

        /**
         * @return the workspace
         */
        public Workspace getWorkspace()
        {
            return workspace;
        }

        /**
         * @return the success
         */
        public boolean getSuccessed()
        {
            return successed;
        }
    }

    private static class WorkspaceInstallInfo
    {
        final Workspace workspace;
        final boolean showConfirm;

        public WorkspaceInstallInfo(Workspace workspace, boolean showConfirm)
        {
            super();

            this.workspace = workspace;
            this.showConfirm = showConfirm;
        }
    }

    /**
     * static class
     */
    private static final WorkspaceInstaller instance = new WorkspaceInstaller();

    /**
     * workspace to install FIFO
     */
    private final List<WorkspaceInstallInfo> installFIFO;
    /**
     * workspace to delete FIFO
     */
    private final List<WorkspaceInstallInfo> removeFIFO;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internals
     */
    private Workspace installingWorkspace;
    private Workspace desinstallingWorkspace;

    private boolean installing;
    private boolean deinstalling;

    /**
     * static class
     */
    private WorkspaceInstaller()
    {
        super();

        installFIFO = new ArrayList<WorkspaceInstallInfo>();
        removeFIFO = new ArrayList<WorkspaceInstallInfo>();

        listeners = new EventListenerList();

        installingWorkspace = null;
        desinstallingWorkspace = null;

        installing = false;
        deinstalling = false;

        // launch installer thread
        new Thread(this, "Workspace installer").start();
    }

    /**
     * install a workspace (asynchronous)
     */
    public static void install(Workspace workspace, boolean showConfirm)
    {
        if (workspace != null)
        {
            if (!NetworkUtil.hasInternetAccess())
            {
                new FailedAnnounceFrame("Cannot install '" + workspace.getName()
                        + "' workspace : you are not connected to Internet.", 10);
                return;
            }

            synchronized (instance.installFIFO)
            {
                instance.installFIFO.add(new WorkspaceInstallInfo(workspace, showConfirm));
            }
        }
    }

    /**
     * return true if WorkspaceInstaller is processing
     */
    public static boolean isProcessing()
    {
        return isInstalling() || isDesinstalling();
    }

    /**
     * return true if WorkspaceInstaller is installing workspace(s)
     */
    public static boolean isInstalling()
    {
        return (!instance.installFIFO.isEmpty()) || instance.installing;
    }

    /**
     * return true if 'workspace' is in the install FIFO
     */
    public static boolean isWaitingForInstall(Workspace workspace)
    {
        final String workspaceName = workspace.getName();

        synchronized (instance.installFIFO)
        {
            for (WorkspaceInstallInfo info : instance.installFIFO)
                if (workspaceName.equals(info.workspace.getName()))
                    return true;
        }

        return false;
    }

    /**
     * return the current installed workspace (null if none)
     */
    public static Workspace getCurrentInstallingWorkspace()
    {
        return instance.installingWorkspace;
    }

    /**
     * return true if specified workspace is currently being installed or will be installed
     */
    public static boolean isInstallingWorkspace(Workspace workspace)
    {
        if (instance.installingWorkspace != null)
        {
            if (instance.installingWorkspace.getName().equals(workspace.getName()))
                return true;
        }

        return isWaitingForInstall(workspace);
    }

    /**
     * uninstall a workspace (asynchronous)
     */
    public static void desinstall(Workspace workspace, boolean showConfirm)
    {
        if (workspace != null)
        {
            synchronized (instance.removeFIFO)
            {
                instance.removeFIFO.add(new WorkspaceInstallInfo(workspace, showConfirm));
            }
        }
    }

    /**
     * return true if WorkspaceInstaller is desinstalling workspace(s)
     */
    public static boolean isDesinstalling()
    {
        return (!instance.removeFIFO.isEmpty()) || instance.deinstalling;
    }

    /**
     * return true if 'workspace' is in the remove FIFO
     */
    public static boolean isWaitingForDesinstall(Workspace workspace)
    {
        final String workspaceName = workspace.getName();

        synchronized (instance.removeFIFO)
        {
            for (WorkspaceInstallInfo info : instance.removeFIFO)
                if (workspaceName.equals(info.workspace.getName()))
                    return true;
        }

        return false;
    }

    /**
     * return true if specified workspace is currently being desinstalled or will be desinstalled
     */
    public static boolean isDesinstallingWorkspace(Workspace workspace)
    {
        if (instance.desinstallingWorkspace != null)
        {
            if (instance.desinstallingWorkspace.getName().equals(workspace.getName()))
                return true;
        }

        return isWaitingForDesinstall(workspace);
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            boolean empty;
            boolean result;
            WorkspaceInstallInfo installInfo = null;

            // process installations
            empty = installFIFO.isEmpty();

            if (!empty)
            {
                installing = true;
                try
                {
                    while (!empty)
                    {
                        synchronized (installFIFO)
                        {
                            installInfo = installFIFO.remove(0);
                            empty = installFIFO.isEmpty();
                        }

                        // don't install if the workspace is already installed
                        if (!WorkspaceLoader.isLoaded(installInfo.workspace))
                        {
                            result = installInternal(installInfo);
                            // process on workspace installation
                            installed(installInfo.workspace, result);
                        }

                        synchronized (installFIFO)
                        {
                        }
                    }
                }
                finally
                {
                    installing = false;
                }
            }

            // process deletions
            empty = removeFIFO.isEmpty();

            if (!empty)
            {
                deinstalling = true;
                try
                {
                    while (!empty)
                    {
                        synchronized (removeFIFO)
                        {
                            installInfo = removeFIFO.remove(0);
                            empty = removeFIFO.isEmpty();
                        }

                        result = desinstallInternal(installInfo);
                        // process on workspace deletion
                        desinstalled(installInfo.workspace, result);
                    }
                }
                finally
                {
                    deinstalling = false;
                }
            }

            ThreadUtil.sleep(200);
        }
    }

    private boolean deleteWorkspace(Workspace workspace)
    {
        if (!FileUtil.delete(workspace.getLocalFilename(), false))
            System.err.println("deleteWorkspace : Can't delete " + workspace.getLocalFilename());

        // reload workspace list
        WorkspaceLoader.reload();

        return true;
    }

    /**
     * Return local plugins of specified workspace
     */
    private ArrayList<PluginDescriptor> getLocalPlugins(Workspace workspace)
    {
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        for (ItemDefinition item : workspace.getAllItems())
        {
            if (!item.isSeparator())
            {
                final PluginDescriptor plugin = PluginLoader.getPlugin(item.getClassName());

                // plugin found
                if (plugin != null)
                {
                    // add all its dependences
                    PluginInstaller.getLocalDependenciesOf(result, plugin);
                    // the add the plugin itselft
                    PluginDescriptor.addToList(result, plugin);
                }
            }
        }

        return result;
    }

    /**
     * Return independent plugins of workspace (so they can be deleted)
     */
    private ArrayList<PluginDescriptor> getIndependentPlugins(Workspace workspace)
    {
        // get plugins of specified workspace
        final ArrayList<PluginDescriptor> result = getLocalPlugins(workspace);
        final ArrayList<PluginDescriptor> others = new ArrayList<PluginDescriptor>();

        // get plugins of all others workspaces
        for (Workspace ws : WorkspaceLoader.getWorkspaces())
            // we can use name as id here
            if (!StringUtil.equals(ws.getName(), workspace.getName()))
                others.addAll(getLocalPlugins(ws));

        for (PluginDescriptor plugin : others)
        {
            for (int i = result.size() - 1; i >= 0; i--)
            {
                final PluginDescriptor depPlug = result.get(i);

                // have a dependence, remove from list...
                if (plugin.equals(depPlug) || plugin.requires(depPlug))
                    result.remove(i);
            }
        }

        return result;
    }

    private boolean installInternal(WorkspaceInstallInfo installInfo)
    {
        final Workspace workspace = installInfo.workspace;
        final boolean showConfirm = installInfo.showConfirm;

        // installation start
        installingWorkspace = workspace;
        try
        {
            ProgressFrame taskFrame = null;
            int result = 0;
            final String workspaceName = workspace.getName();

            if (showConfirm)
                taskFrame = new ProgressFrame("installing workspace '" + workspaceName + "'...");
            try
            {
                // install workspace (actually install dependent plugins)
                result = workspace.install(taskFrame);

                if (result > 0)
                {
                    if (taskFrame != null)
                        taskFrame.setMessage("saving workspace '" + workspaceName + "'...");

                    // save workspace locally
                    workspace.save();

                    if (taskFrame != null)
                        taskFrame.setMessage("reloading workspaces list...");

                    // reload workspace list
                    WorkspaceLoader.reload();
                }
            }
            finally
            {
                if (taskFrame != null)
                    taskFrame.close();
            }

            final String resMess = "Workspace '" + workspaceName + "' installation";

            if (showConfirm)
            {
                switch (result)
                {
                    default:
                        new FailedAnnounceFrame(resMess + " failed !");
                        break;

                    case 1:
                        new SuccessfullAnnounceFrame(resMess + " succeed !", 10);
                        break;

                    case 2:
                        new SuccessfullAnnounceFrame(resMess + " succeed but some plugins cannot be installed.", 10);
                        break;
                }
            }
            else
            {
                switch (result)
                {
                    default:
                        System.err.println(resMess + " failed !");
                        break;

                    case 1:
                        System.out.println(resMess + " succeed !");
                        break;

                    case 2:
                        System.out.println(resMess + " partially succeed (some plugins cannot be installed) !");
                        break;
                }
            }

            return result > 0;
        }
        finally
        {
            // installation end
            installingWorkspace = null;
        }
    }

    private boolean desinstallInternal(WorkspaceInstallInfo installInfo)
    {
        final Workspace workspace = installInfo.workspace;
        final boolean showConfirm = installInfo.showConfirm;

        // desinstall start
        desinstallingWorkspace = workspace;
        try
        {
            final ArrayList<PluginDescriptor> independentPlugins = new ArrayList<PluginDescriptor>();
            final String workspaceDesc = workspace.getName();

            final boolean deletePlugin;
            final boolean result;
            ProgressFrame taskFrame = null;

            if (showConfirm)
            {
                String message = "<html>Do you want to also remove the associated plugins ?</html>";

                deletePlugin = ConfirmDialog.confirm(message);
            }
            else
                deletePlugin = true;

            if (deletePlugin)
            {
                if (showConfirm)
                    taskFrame = new ProgressFrame("checking plugins dependences...");
                try
                {
                    independentPlugins.addAll(getIndependentPlugins(workspace));
                }
                finally
                {
                    if (taskFrame != null)
                        taskFrame.close();
                }
            }

            if (showConfirm)
                taskFrame = new ProgressFrame("removing workspace '" + workspaceDesc + "'...");
            try
            {
                // remove workspace independent plugins
                for (PluginDescriptor plugin : independentPlugins)
                    if (plugin.isInstalled())
                        PluginInstaller.desinstall(plugin, false);

                // wait for plugins desintallation
                PluginInstaller.waitDesinstall();

                // delete workspace
                result = deleteWorkspace(workspace);
            }
            finally
            {
                if (taskFrame != null)
                    taskFrame.close();
            }

            final String resMess = "Workspace '" + workspaceDesc + "' remove";

            if (showConfirm)
            {
                if (result)
                    new SuccessfullAnnounceFrame(resMess + " succeed !", 10);
                else
                    new FailedAnnounceFrame(resMess + " failed !");
            }
            else
            {
                if (result)
                    System.out.println(resMess + " succeed !");
                else
                    System.err.println(resMess + " failed !");
            }

            return result;
        }
        finally
        {
            // desintall end
            desinstallingWorkspace = null;
        }
    }

    /**
     * process on workspace install
     */
    private void installed(Workspace workspace, boolean result)
    {
        if (result)
        {
            // enable the installed workspace by default
            WorkspaceLocalPreferences.setWorkspaceEnable(workspace.getName(), true);
            // show an announcement for restart
            Icy.announceRestart();
        }

        fireInstalledEvent(new WorkspaceInstallerEvent(workspace, result));
    }

    /**
     * process on workspace remove
     */
    private void desinstalled(Workspace workspace, boolean result)
    {
        if (result)
            // show an announcement for restart
            Icy.announceRestart();

        fireRemovedEvent(new WorkspaceInstallerEvent(workspace, result));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public static void addListener(WorkspaceInstallerListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.add(WorkspaceInstallerListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(WorkspaceInstallerListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.remove(WorkspaceInstallerListener.class, listener);
        }
    }

    /**
     * fire workspace installed event
     */
    private void fireInstalledEvent(WorkspaceInstallerEvent e)
    {
        for (WorkspaceInstallerListener listener : listeners.getListeners(WorkspaceInstallerListener.class))
            listener.workspaceInstalled(e);
    }

    /**
     * fire workspace removed event
     */
    private void fireRemovedEvent(WorkspaceInstallerEvent e)
    {
        for (WorkspaceInstallerListener listener : listeners.getListeners(WorkspaceInstallerListener.class))
            listener.workspaceRemoved(e);
    }

}
