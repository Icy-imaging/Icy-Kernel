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
package icy.plugin;

import icy.file.FileUtil;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.frame.progress.SuccessfullAnnounceFrame;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor.PluginIdent;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.update.Updater;
import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * @author Stephane
 */
public class PluginInstaller implements Runnable
{
    public static interface PluginInstallerListener extends EventListener
    {
        public void pluginInstalled(PluginDescriptor plugin, boolean success);

        public void pluginRemoved(PluginDescriptor plugin, boolean success);
    }

    private static class PluginInstallInfo
    {
        // final PluginRepositoryLoader loader;
        final PluginDescriptor plugin;
        final boolean showConfirm;

        public PluginInstallInfo(PluginDescriptor plugin, boolean showConfirm)
        {
            super();

            this.plugin = plugin;
            this.showConfirm = showConfirm;
        }
    }

    private static final String ERROR_DOWNLOAD = "Error while downloading ";
    private static final String ERROR_SAVE = "Error while saving";
    // private static final String INSTALL_CANCELED = "Plugin installation canceled by user.";

    /**
     * static class
     */
    private static final PluginInstaller instance = new PluginInstaller();

    /**
     * plugin(s) to install FIFO
     */
    private final ArrayList<PluginInstallInfo> installFIFO;
    /**
     * plugin(s) to delete FIFO
     */
    private final ArrayList<PluginInstallInfo> removeFIFO;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internals
     */
    private final ArrayList<PluginDescriptor> installingPlugins;
    private final ArrayList<PluginDescriptor> desinstallingPlugin;

    /**
     * static class
     */
    private PluginInstaller()
    {
        super();

        installFIFO = new ArrayList<PluginInstallInfo>();
        removeFIFO = new ArrayList<PluginInstallInfo>();

        listeners = new EventListenerList();

        installingPlugins = new ArrayList<PluginDescriptor>();
        desinstallingPlugin = new ArrayList<PluginDescriptor>();

        // launch installer thread
        new Thread(this, "Plugin installer").start();
    }

    /**
     * Return true if install or desinstall is possible
     */
    private static boolean isEnabled()
    {
        return !PluginLoader.isJCLDisabled();
    }

    /**
     * Install a plugin (asynchronous)
     */
    public static void install(PluginDescriptor plugin, boolean showConfirm)
    {
        if ((plugin != null) && isEnabled())
        {
            if (!NetworkUtil.hasInternetAccess())
            {
                new FailedAnnounceFrame("Cannot install '" + plugin.getName()
                        + "' plugin : you are not connected to Internet.", 10);
                return;
            }

            synchronized (instance.installFIFO)
            {
                instance.installFIFO.add(new PluginInstallInfo(plugin, showConfirm));
            }
        }
    }

    /**
     * return true if PluginInstaller is processing
     */
    public static boolean isProcessing()
    {
        return isInstalling() || isDesinstalling();
    }

    /**
     * return a copy of the install FIFO
     */
    public static ArrayList<PluginInstallInfo> getInstallFIFO()
    {
        synchronized (instance.installFIFO)
        {
            return new ArrayList<PluginInstaller.PluginInstallInfo>(instance.installFIFO);
        }
    }

    /**
     * Wait while installer is installing plugin.
     */
    public static void waitInstall()
    {
        while (isInstalling())
            ThreadUtil.sleep(100);
    }

    /**
     * return true if PluginInstaller is installing plugin(s)
     */
    public static boolean isInstalling()
    {
        return !instance.installFIFO.isEmpty() || !instance.installingPlugins.isEmpty();
    }

    /**
     * return true if 'plugin' is in the install FIFO
     */
    public static boolean isWaitingForInstall(PluginDescriptor plugin)
    {
        synchronized (instance.installFIFO)
        {
            for (PluginInstallInfo info : instance.installFIFO)
                if (plugin == info.plugin)
                    return true;
        }

        return false;
    }

    /**
     * return true if specified plugin is currently being installed or will be installed
     */
    public static boolean isInstallingPlugin(PluginDescriptor plugin)
    {
        return (instance.installingPlugins.indexOf(plugin) != -1) || isWaitingForInstall(plugin);
    }

    /**
     * uninstall a plugin (asynchronous)
     */
    public static void desinstall(PluginDescriptor plugin, boolean showConfirm)
    {
        if ((plugin != null) && isEnabled())
        {
            if (showConfirm)
            {
                // get local plugins which depend from the plugin we want to delete
                final ArrayList<PluginDescriptor> dependants = getLocalDependenciesFrom(plugin);

                String message = "<html>";

                if (!dependants.isEmpty())
                {
                    message = message + "The following plugin(s) won't work anymore :<br>";

                    for (PluginDescriptor depPlug : dependants)
                        message = message + depPlug.getName() + " " + depPlug.getVersion() + "<br>";

                    message = message + "<br>";
                }

                message = message + "Are you sure you want to remove '" + plugin.getName() + " " + plugin.getVersion()
                        + "' ?</html>";

                if (ConfirmDialog.confirm(message))
                {
                    synchronized (instance.removeFIFO)
                    {
                        instance.removeFIFO.add(new PluginInstallInfo(plugin, showConfirm));
                    }
                }
            }
            else
            {
                synchronized (instance.removeFIFO)
                {
                    instance.removeFIFO.add(new PluginInstallInfo(plugin, showConfirm));
                }
            }
        }
    }

    /**
     * return a copy of the remove FIFO
     */
    public static ArrayList<PluginInstallInfo> getRemoveFIFO()
    {
        synchronized (instance.removeFIFO)
        {
            return new ArrayList<PluginInstaller.PluginInstallInfo>(instance.removeFIFO);
        }
    }

    /**
     * Wait while installer is removing plugin.
     */
    public static void waitDesinstall()
    {
        while (isDesinstalling())
            ThreadUtil.sleep(100);
    }

    /**
     * return true if PluginInstaller is desinstalling plugin(s)
     */
    public static boolean isDesinstalling()
    {
        return !instance.removeFIFO.isEmpty() || !instance.desinstallingPlugin.isEmpty();
    }

    /**
     * return true if 'plugin' is in the remove FIFO
     */
    public static boolean isWaitingForDesinstall(PluginDescriptor plugin)
    {
        synchronized (instance.removeFIFO)
        {
            for (PluginInstallInfo info : instance.removeFIFO)
                if (plugin == info.plugin)
                    return true;
        }

        return false;
    }

    /**
     * return true if specified plugin is currently being desinstalled or will be desinstalled
     */
    public static boolean isDesinstallingPlugin(PluginDescriptor plugin)
    {
        return (instance.desinstallingPlugin.indexOf(plugin) != -1) || isWaitingForDesinstall(plugin);
    }

    @Override
    public void run()
    {
        while (true)
        {
            // process installations
            while (!installFIFO.isEmpty())
                installInternal();

            // process deletions
            while (!removeFIFO.isEmpty())
                desinstallInternal();

            ThreadUtil.sleep(100);
        }
    }

    /**
     * Backup specified plugin if it already exists.<br>
     * Return an empty string if no error else return error message
     */
    private String backup(PluginDescriptor plugin)
    {
        boolean ok;

        // backup JAR, XML and image files
        ok = Updater.backup(plugin.getJarFilename()) && Updater.backup(plugin.getXMLFilename())
                && Updater.backup(plugin.getIconFilename()) && Updater.backup(plugin.getImageFilename());

        if (!ok)
            return "Can't backup plugin '" + plugin.getName() + "'";

        return "";
    }

    /**
     * Return an empty string if no error else return error message
     */
    private String downloadAndSavePlugin(PluginDescriptor plugin, CancelableProgressFrame taskFrame)
    {
        String result;

        if (taskFrame != null)
            taskFrame.setMessage("Downloading " + plugin);

        // ensure descriptor is loaded
        plugin.loadDescriptor();

        final RepositoryInfo repos = plugin.getRepository();
        final String login;
        final String pass;

        // use authentication
        if ((repos != null) && repos.isAuthenticationEnabled())
        {
            login = repos.getLogin();
            pass = repos.getPassword();
        }
        else
        {
            login = null;
            pass = null;
        }

        // download and save JAR file
        result = downloadAndSave(plugin.getJarUrl(), plugin.getJarFilename(), login, pass, true, taskFrame);
        if (!StringUtil.isEmpty(result))
            return result;

        // download and save XML file
        result = downloadAndSave(plugin.getUrl(), plugin.getXMLFilename(), login, pass, true, taskFrame);
        if (!StringUtil.isEmpty(result))
            return result;

        // download and save icon & image files
        downloadAndSave(plugin.getIconUrl(), plugin.getIconFilename(), login, pass, false, taskFrame);
        downloadAndSave(plugin.getImageUrl(), plugin.getImageFilename(), login, pass, false, taskFrame);

        return "";
    }

    /**
     * Return an empty string if no error else return error message
     */
    private String downloadAndSave(String downloadPath, String savePath, String login, String pass,
            boolean displayError, ProgressFrame taskFrame)
    {
        // load data
        final byte[] data = NetworkUtil.download(downloadPath, login, pass, taskFrame, displayError);
        if (data == null)
            return ERROR_DOWNLOAD + downloadPath;

        // save data
        if (!FileUtil.save(savePath, data, displayError))
        {
            System.err.println("Can't write '" + savePath + "' !");
            System.err.println("File may be locked or you don't own the rights to write files here.");
            return ERROR_SAVE + savePath;
        }

        return null;
    }

    private boolean deletePlugin(PluginDescriptor plugin)
    {
        if (!FileUtil.delete(plugin.getJarFilename(), false))
        {
            System.err.println("deletePlugin() : Can't delete " + plugin.getJarFilename());
            // fatal error
            return false;
        }

        if (FileUtil.exists(plugin.getXMLFilename()))
            if (!FileUtil.delete(plugin.getXMLFilename(), false))
                System.err.println("deletePlugin() : Can't delete " + plugin.getXMLFilename());

        FileUtil.delete(plugin.getImageFilename(), false);
        FileUtil.delete(plugin.getIconFilename(), false);

        return true;
    }

    /**
     * Fill list with local dependencies (plugins) of specified plugin
     */
    public static void getLocalDependenciesOf(ArrayList<PluginDescriptor> result, PluginDescriptor plugin)
    {
        // load plugin descriptor informations if not yet done
        plugin.loadDescriptor();

        for (PluginIdent ident : plugin.getRequired())
        {
            // already in our dependences ? --> pass to the next one
            if (PluginDescriptor.getPlugin(result, ident, true) != null)
                continue;

            // find local dependent plugin
            final PluginDescriptor dep = PluginLoader.getPlugin(ident, true);

            // dependence found ?
            if (dep != null)
            {
                // and add it to list
                PluginDescriptor.addToList(result, dep);
                // search its dependencies too
                getLocalDependenciesOf(result, dep);
            }
        }
    }

    /**
     * Return local plugin list which depend from specified plugin.
     */
    public static ArrayList<PluginDescriptor> getLocalDependenciesFrom(PluginDescriptor plugin)
    {
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        for (PluginDescriptor curPlug : PluginLoader.getPlugins(false))
            // require specified plugin ?
            if (curPlug.requires(plugin))
                PluginDescriptor.addToList(result, curPlug);

        return result;
    }

    /**
     * Fill list with 'sources' dependencies of specified plugin
     */
    private static void getLocalDependenciesOf(ArrayList<PluginDescriptor> result, ArrayList<PluginDescriptor> sources,
            PluginDescriptor plugin)
    {
        // load plugin descriptor informations if not yet done
        plugin.loadDescriptor();

        for (PluginIdent ident : plugin.getRequired())
        {
            // already in our dependences ? --> pass to the next one
            if (PluginDescriptor.getPlugin(result, ident, true) != null)
                continue;

            // find sources dependent plugin
            final PluginDescriptor dep = PluginDescriptor.getPlugin(sources, ident, true);

            // dependence found ?
            if (dep != null)
            {
                // and add it to list
                PluginDescriptor.addToList(result, dep);
                // search its dependencies too
                getLocalDependenciesOf(result, sources, dep);
            }
        }
    }

    /**
     * Reorder the list so needed dependencies comes first in list
     */
    public static ArrayList<PluginDescriptor> getDependenciesOrderedList(ArrayList<PluginDescriptor> plugins)
    {
        final ArrayList<PluginDescriptor> sources = new ArrayList<PluginDescriptor>(plugins);
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        while (sources.size() > 0)
        {
            final ArrayList<PluginDescriptor> deps = new ArrayList<PluginDescriptor>();

            getLocalDependenciesOf(result, sources, sources.get(0));

            // add last to first dep
            for (int i = deps.size() - 1; i >= 0; i--)
                PluginDescriptor.addToList(result, deps.get(i));

            // then add plugin
            PluginDescriptor.addToList(result, sources.get(0));

            // remove tested plugin and its dependencies from source
            sources.removeAll(result);
        }

        return result;
    }

    /**
     * resolve dependencies for specified plugin
     * 
     * @param taskFrame
     */
    private boolean checkDependencies(PluginDescriptor plugin, ArrayList<PluginDescriptor> pluginsToInstall,
            CancelableProgressFrame taskFrame)
    {
        // load plugin descriptor informations if not yet done
        plugin.loadDescriptor();

        // check dependencies
        for (PluginIdent ident : plugin.getRequired())
        {
            if ((taskFrame != null) && taskFrame.isCancelRequested())
                return false;

            // already in our dependencies ? --> pass to the next one
            if (PluginDescriptor.getPlugin(pluginsToInstall, ident, true) != null)
                continue;

            final String className = ident.getClassName();

            // get local & online plugin
            final PluginDescriptor localPlugin = PluginLoader.getPlugin(className);
            final PluginDescriptor onlinePlugin = PluginRepositoryLoader.getPlugin(className);

            // plugin not yet installed or out dated ?
            if ((localPlugin == null) || ident.getVersion().isGreater(localPlugin.getVersion()))
            {
                // online plugin not found ?
                if (onlinePlugin == null)
                {
                    // error
                    System.err.println("Can't resolve dependencies for plugin '" + plugin.getName() + "' :");

                    if (localPlugin == null)
                        System.err.println("Plugin class '" + ident.getClassName() + " not found !");
                    else
                    {
                        System.err.println(localPlugin.getName() + " " + localPlugin.getVersion() + " installed");
                        System.err.println("but version " + ident.getVersion() + " or greater needed.");
                    }

                    return false;
                }
                // online plugin version incorrect
                else if (ident.getVersion().isGreater(onlinePlugin.getVersion()))
                {
                    // error
                    System.err.println("Can't resolve dependencies for plugin '" + plugin.getName() + "' :");
                    System.err.println(onlinePlugin.getName() + " " + onlinePlugin.getVersion()
                            + " found in repository");
                    System.err.println("but version " + ident.getVersion() + " or greater needed.");
                    return false;
                }

                // add to the install list
                PluginDescriptor.addToList(pluginsToInstall, onlinePlugin);
                // and check dependencies for this plugin
                if (!checkDependencies(onlinePlugin, pluginsToInstall, taskFrame))
                    return false;
            }
            else
            {
                // just check if we have update for dependency
                if ((onlinePlugin != null) && (localPlugin.getVersion().isLower(onlinePlugin.getVersion())))
                {
                    // as web site doesn't handle version dependency, we force the update

                    // add to the install list
                    PluginDescriptor.addToList(pluginsToInstall, onlinePlugin);
                    // and check dependencies for this plugin
                    if (!checkDependencies(onlinePlugin, pluginsToInstall, taskFrame))
                        return false;
                }
            }
        }

        return true;
    }

    private void installInternal()
    {
        CancelableProgressFrame taskFrame = null;

        try
        {
            final ArrayList<PluginInstallInfo> infos;
            boolean showConfirm;

            synchronized (installFIFO)
            {
                infos = new ArrayList<PluginInstaller.PluginInstallInfo>(installFIFO);

                // remove plugin already installed from list and determine if we should
                // display the progress bar
                showConfirm = false;
                for (int i = infos.size() - 1; i >= 0; i--)
                {
                    final PluginInstallInfo info = infos.get(i);
                    final PluginDescriptor plugin = info.plugin;

                    if (!PluginLoader.isLoaded(plugin, false))
                    {
                        installingPlugins.add(plugin);
                        showConfirm |= info.showConfirm;
                    }
                }

                installFIFO.clear();
            }

            if (showConfirm)
                taskFrame = new CancelableProgressFrame("initializing...");

            final ArrayList<PluginDescriptor> dependencies = new ArrayList<PluginDescriptor>();
            final ArrayList<PluginDescriptor> pluginsOk = new ArrayList<PluginDescriptor>();
            final ArrayList<PluginDescriptor> pluginsNOk = new ArrayList<PluginDescriptor>();

            // get dependencies
            for (int i = installingPlugins.size() - 1; i >= 0; i--)
            {
                final PluginDescriptor plugin = installingPlugins.get(i);
                final String plugDesc = plugin.getName() + " " + plugin.getVersion();

                if (taskFrame != null)
                {
                    // cancel requested ?
                    if (taskFrame.isCancelRequested())
                        return;

                    taskFrame.setMessage("Checking dependencies for '" + plugDesc + "' ...");
                }

                // check dependencies
                if (!checkDependencies(plugin, dependencies, taskFrame))
                {
                    // can't resolve dependencies for this plugin
                    pluginsNOk.add(plugin);
                    installingPlugins.remove(i);
                }
            }

            // nothing to install
            if (installingPlugins.isEmpty())
                return;

            // add dependencies to the installing list
            for (PluginDescriptor plugin : dependencies)
                PluginDescriptor.addToList(installingPlugins, plugin);

            String error = "";

            // now we can proceed the installation itself
            for (PluginDescriptor plugin : installingPlugins)
            {
                // already installed --> go to next one
                if (PluginLoader.isLoaded(plugin, false))
                    continue;

                final String plugDesc = plugin.getName() + " " + plugin.getVersion();

                if (taskFrame != null)
                {
                    // cancel requested ? --> interrupt installation
                    if (taskFrame.isCancelRequested())
                        break;

                    taskFrame.setMessage("Installing " + plugDesc + "...");
                }

                try
                {
                    // backup plugin
                    error = backup(plugin);

                    // backup ok --> install plugin
                    if (StringUtil.isEmpty(error))
                    {
                        error = downloadAndSavePlugin(plugin, taskFrame);

                        // an error occurred ? --> restore
                        if (!StringUtil.isEmpty(error))
                            Updater.restore();
                    }
                }
                finally
                {
                    // delete backup
                    FileUtil.delete(Updater.BACKUP_DIRECTORY, true);
                }

                if (StringUtil.isEmpty(error))
                    pluginsOk.add(plugin);
                else
                {
                    pluginsNOk.add(plugin);
                    // print error
                    System.err.println(error);
                }
            }

            // verify installed plugins
            if (taskFrame != null)
                taskFrame.setMessage("Verifying plugins...");

            // reload plugin list
            PluginLoader.reload();

            for (int i = pluginsOk.size() - 1; i >= 0; i--)
            {
                final PluginDescriptor plugin = pluginsOk.get(i);

                error = PluginLoader.verifyPlugin(plugin);

                // send report when we have verification error
                if (!StringUtil.isEmpty(error))
                {
                    IcyExceptionHandler.report(plugin, "An error occured while installing the plugin :\n" + error);
                    // print error
                    System.err.println(error);

                    pluginsOk.remove(i);
                    pluginsNOk.add(plugin);
                }
            }

            if (!pluginsNOk.isEmpty())
            {
                System.err.println();
                System.err.println("Installation of the following plugin(s) failed:");
                for (PluginDescriptor plugin : pluginsNOk)
                {
                    System.err.println(plugin.getName() + " " + plugin.getVersion());
                    // notify about installation fails
                    fireInstalledEvent(plugin, false);
                }
                System.err.println();
            }

            if (!pluginsOk.isEmpty())
            {
                System.out.println();
                System.out.println("The following plugin(s) has been correctly installed:");
                for (PluginDescriptor plugin : pluginsOk)
                {
                    System.out.println(plugin.getName() + " " + plugin.getVersion());
                    // notify about installation successes
                    fireInstalledEvent(plugin, true);
                }
                System.out.println();
            }

            if (showConfirm)
            {
                if (pluginsNOk.isEmpty())
                    new SuccessfullAnnounceFrame("Plugin(s) installation was successful !");
                else if (pluginsOk.isEmpty())
                    new FailedAnnounceFrame("Plugin(s) installation failed !");
                else
                    new FailedAnnounceFrame(
                            "Some plugin(s) installation failed (looks at the output console for detail) !");
            }
        }
        finally
        {
            // installation end
            installingPlugins.clear();
            if (taskFrame != null)
                taskFrame.close();
        }
    }

    private void desinstallInternal()
    {
        CancelableProgressFrame taskFrame = null;

        try
        {
            final ArrayList<PluginInstallInfo> infos;
            boolean showConfirm;

            synchronized (removeFIFO)
            {
                infos = new ArrayList<PluginInstaller.PluginInstallInfo>(removeFIFO);

                // determine if we should display the progress bar
                showConfirm = false;
                for (int i = infos.size() - 1; i >= 0; i--)
                {
                    final PluginInstallInfo info = infos.get(i);

                    desinstallingPlugin.add(info.plugin);
                    showConfirm |= info.showConfirm;
                }

                removeFIFO.clear();
            }

            if (showConfirm)
                taskFrame = new CancelableProgressFrame("Initializing...");

            // now we can proceed remove
            for (PluginDescriptor plugin : desinstallingPlugin)
            {
                final String plugDesc = plugin.getName() + " " + plugin.getVersion();
                final boolean result;

                if (taskFrame != null)
                {
                    // cancel requested ?
                    if (taskFrame.isCancelRequested())
                        return;

                    taskFrame.setMessage("Removing plugin '" + plugDesc + "'...");
                }

                result = deletePlugin(plugin);

                // notify plugin deletion
                fireRemovedEvent(plugin, result);

                if (showConfirm)
                {
                    if (!result)
                        new FailedAnnounceFrame("Plugin '" + plugDesc + "' delete operation failed !");
                }

                if (result)
                    System.out.println("Plugin '" + plugDesc + "' correctly removed.");
                else
                    System.err.println("Plugin '" + plugDesc + "' delete operation failed !");
            }
        }
        finally
        {
            if (taskFrame != null)
                taskFrame.close();
            // removing end
            desinstallingPlugin.clear();
        }

        // reload plugin list
        PluginLoader.reload();
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public static void addListener(PluginInstallerListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.add(PluginInstallerListener.class, listener);
        }
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public static void removeListener(PluginInstallerListener listener)
    {
        synchronized (instance.listeners)
        {
            instance.listeners.remove(PluginInstallerListener.class, listener);
        }
    }

    /**
     * fire plugin installed event
     */
    private void fireInstalledEvent(PluginDescriptor plugin, boolean success)
    {
        synchronized (listeners)
        {
            for (PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginInstalled(plugin, success);
        }
    }

    /**
     * fire plugin removed event
     */
    private void fireRemovedEvent(PluginDescriptor plugin, boolean success)
    {
        synchronized (listeners)
        {
            for (PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginRemoved(plugin, success);
        }
    }

}
