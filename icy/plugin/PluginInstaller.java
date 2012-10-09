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
import icy.gui.dialog.IdConfirmDialog;
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
        public void pluginInstalled(boolean success);

        public void pluginRemoved(boolean success);
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
    private static final String INSTALL_INTERRUPT = "Plugin installation canceled by user.";

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
    private PluginDescriptor desinstallingPlugin;

    private boolean installing;
    private boolean deinstalling;

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
        desinstallingPlugin = null;

        installing = false;
        deinstalling = false;

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
     * return true if PluginInstaller is installing plugin(s)
     */
    public static boolean isInstalling()
    {
        return (!instance.installFIFO.isEmpty()) || instance.installing;
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
     * return true if PluginInstaller is desinstalling plugin(s)
     */
    public static boolean isDesinstalling()
    {
        return (!instance.removeFIFO.isEmpty()) || instance.deinstalling;
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
        return (plugin.equals(instance.desinstallingPlugin)) || isWaitingForDesinstall(plugin);
    }

    @Override
    public void run()
    {
        while (true)
        {
            boolean empty;
            boolean result;
            PluginInstallInfo installInfo = null;

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

                        // don't install already installed plugins
                        if (!PluginLoader.isLoaded(installInfo.plugin, false))
                        {
                            result = internal_install(installInfo);
                            // notify plugin installation
                            fireInstalledEvent(result);
                        }
                        else
                            System.out.println("Info: plugin " + installInfo.plugin.toString() + " already installed.");
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

                        result = internal_desinstall(installInfo);
                        // notify plugin deletion
                        fireRemovedEvent(result);
                    }
                }
                finally
                {
                    deinstalling = false;
                }
            }

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

    private boolean internal_install(PluginInstallInfo installInfo)
    {
        final boolean showConfirm = installInfo.showConfirm;
        final PluginDescriptor basePlugin = installInfo.plugin;
        final String plugDesc = basePlugin.getName() + " " + basePlugin.getVersion();
        final CancelableProgressFrame taskFrame;

        if (showConfirm)
            taskFrame = new CancelableProgressFrame("initializing...");
        else
            taskFrame = null;

        // installation start
        installingPlugins.add(basePlugin);

        try
        {
            final ArrayList<PluginDescriptor> pluginsToInstall = new ArrayList<PluginDescriptor>();
            boolean result = true;
            boolean verifyError = false;
            String error = "";

            if (taskFrame != null)
                taskFrame.setMessage("checking dependencies for '" + plugDesc + "' ...");

            // check dependencies
            result = checkDependencies(basePlugin, pluginsToInstall, taskFrame);

            // cancel requested ?
            if ((taskFrame != null) && taskFrame.isCancelRequested())
            {
                System.out.println(INSTALL_INTERRUPT);
                return false;
            }

            if (result)
            {
                // remove base plugin if present in dependencies (shouldn't be the case)
                final PluginDescriptor doublon = PluginDescriptor
                        .getPlugin(pluginsToInstall, basePlugin.getClassName());
                if (doublon != null)
                    pluginsToInstall.remove(doublon);

                // confirmation wanted
                if (showConfirm)
                {
                    if (!pluginsToInstall.isEmpty())
                    {
                        if (taskFrame != null)
                            taskFrame.setMessage("waiting user confirmation...");

                        String message = "The following plugin(s) need to be installed :<br>";

                        for (PluginDescriptor plug : pluginsToInstall)
                            message = message + plug.getName() + " " + plug.getVersion() + "<br>";

                        message = message + "<br>";

                        // message not empty ? display confirmation
                        if (!IdConfirmDialog.confirm("<html>" + message + "Do you want to continue ?</html>",
                                "pluginInstall1"))
                        {
                            System.out.println(INSTALL_INTERRUPT);
                            return false;
                        }
                    }
                }

                // add dependencies to the installing list
                for (PluginDescriptor plugin : pluginsToInstall)
                    PluginDescriptor.addToList(installingPlugins, plugin);

                // cancel requested ?
                if ((taskFrame != null) && taskFrame.isCancelRequested())
                {
                    System.out.println(INSTALL_INTERRUPT);
                    return false;
                }

                if (taskFrame != null)
                    taskFrame.setMessage("backup files...");

                // backup plugins
                for (PluginDescriptor plugin : installingPlugins)
                {
                    error = backup(plugin);
                    // stop if error
                    if (!StringUtil.isEmpty(error))
                        break;

                    // cancel requested
                    if ((taskFrame != null) && taskFrame.isCancelRequested())
                    {
                        error = INSTALL_INTERRUPT;
                        break;
                    }
                }

                // update and install plugins (index keep trace where installation ended)
                if (StringUtil.isEmpty(error))
                {
                    if (taskFrame != null)
                        taskFrame.setMessage("installing " + plugDesc + "...");

                    for (PluginDescriptor plugin : installingPlugins)
                    {
                        error = downloadAndSavePlugin(plugin, taskFrame);
                        // stop if error
                        if (!StringUtil.isEmpty(error))
                            break;

                        // cancel requested
                        if ((taskFrame != null) && taskFrame.isCancelRequested())
                        {
                            error = INSTALL_INTERRUPT;
                            break;
                        }
                    }
                }

                // verify
                if (StringUtil.isEmpty(error))
                {
                    if (taskFrame != null)
                        taskFrame.setMessage("verifying plugin(s)...");

                    final boolean wasLogging = PluginLoader.getLogError();

                    PluginLoader.setLogError(false);
                    try
                    {
                        // force reload plugins
                        PluginLoader.reload();
                    }
                    finally
                    {
                        PluginLoader.setLogError(wasLogging);
                    }

                    // verify plugins
                    error = PluginLoader.verifyPluginsAreValid(installingPlugins);
                    // trace verify error
                    verifyError = !StringUtil.isEmpty(error);
                }
            }

            // an error occurred during installation ?
            if (!StringUtil.isEmpty(error))
            {
                if (taskFrame != null)
                    taskFrame.setMessage("restoring plugins...");

                // restore previous plugins
                Updater.restore();

                // send report only for verify error
                if (verifyError)
                {
                    // send report to all plugins we tried to install
                    // as we do not know where the problem is exactly
                    for (PluginDescriptor plugin : installingPlugins)
                        IcyExceptionHandler.report(plugin, "An error occured while installing the plugin :\n" + error);
                }

                if (taskFrame != null)
                    taskFrame.setMessage("reloading plugin list...");

                // reload plugins
                PluginLoader.reloadAsynch();

                // print error
                if (error.equals(INSTALL_INTERRUPT))
                    System.out.println(error);
                else
                    System.err.println(error);

                result = false;
            }

            if (taskFrame != null)
            {
                if (result)
                    taskFrame.setMessage("done...");
                else
                    taskFrame.setMessage("failed...");
            }

            if (showConfirm)
            {
                if (result)
                    new SuccessfullAnnounceFrame("Plugin " + plugDesc + " installation succeed !", 10);
                else
                    new FailedAnnounceFrame("Plugin " + plugDesc + " installation failed !");
            }
            else
            {
                if (result)
                    System.out.println("Plugin " + plugDesc + " installation succeed !");
                else
                    System.err.println("Plugin " + plugDesc + " installation failed !");
            }

            return result;
        }
        finally
        {
            // delete backup
            FileUtil.delete(Updater.BACKUP_DIRECTORY, true);
            // installation end
            installingPlugins.clear();
            if (taskFrame != null)
                taskFrame.close();
        }
    }

    private boolean internal_desinstall(PluginInstallInfo installInfo)
    {
        final PluginDescriptor plugin = installInfo.plugin;
        final boolean showConfirm = installInfo.showConfirm;

        // desinstall start
        desinstallingPlugin = plugin;
        try
        {
            final String plugDesc = plugin.getName() + " " + plugin.getVersion();
            final boolean result;
            ProgressFrame taskFrame = null;

            if (showConfirm)
                taskFrame = new ProgressFrame("removing plugin '" + plugDesc + "'...");
            try
            {
                result = deletePlugin(plugin);
            }
            finally
            {
                if (taskFrame != null)
                    taskFrame.close();
            }

            // reload plugin list
            PluginLoader.reloadAsynch();

            final String resMess = "Plugin '" + plugDesc + "' remove";

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
            // desinstall end
            desinstallingPlugin = null;
        }
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
    private void fireInstalledEvent(boolean success)
    {
        synchronized (listeners)
        {
            for (PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginInstalled(success);
        }
    }

    /**
     * fire plugin removed event
     */
    private void fireRemovedEvent(boolean success)
    {
        synchronized (listeners)
        {
            for (PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginRemoved(success);
        }
    }

}
