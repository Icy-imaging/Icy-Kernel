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
package icy.main;

import icy.common.Version;
import icy.file.FileUtil;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.GeneralToolTipFrame;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.SplashScreenFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.main.MainInterface;
import icy.gui.main.MainInterfaceBatch;
import icy.gui.main.MainInterfaceGui;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.viewer.Viewer;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLoader;
import icy.plugin.PluginUpdater;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.IcyPreferences;
import icy.preferences.PluginPreferences;
import icy.sequence.Sequence;
import icy.system.AppleUtil;
import icy.system.IcyExceptionHandler;
import icy.system.IcySecurityManager;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.update.IcyUpdater;
import icy.util.StringUtil;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceLoader;
import ij.ImageJ;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * <br>
 * ICY: Image Analysis Software <br>
 * Institut Pasteur <br>
 * Unite d analyse d images quantitative <br>
 * 25,28 Rue du Docteur Roux <br>
 * 75015 Paris - France
 * 
 * @author Fabrice de Chaumont, Stephane Dallongeville
 */

public class Icy
{
    public static final String LIB_PATH = "lib";

    /**
     * ICY Version
     */
    public static Version version = new Version("1.2.1.1");

    /**
     * Main interface
     */
    private static MainInterface mainInterface = null;

    /**
     * Unique instance checker
     */
    static CheckUniqueTool checkUnique = null;

    /**
     * private splash for initial loading
     */
    static SplashScreenFrame splashScreen = null;

    /**
     * VTK loaded flag
     */
    public static boolean vktLibraryLoaded = false;

    /**
     * internals
     */
    static boolean exiting = false;

    /**
     * @param args
     *        Received from the command line.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Initializing...");
            System.out.println();

            // check if ICY is already running.
            checkUnique = new CheckUniqueTool();

            // prepare splashScreen (ok to create it here as we are not yet in substance laf)
            splashScreen = new SplashScreenFrame();

            // It's important to initialize AWT now (with InvokeNow(...) for instance) to avoid
            // the JVM deadlock bug (id: 5104239). It happen when the AWT thread is initialized
            // while others threads load some new library with ClassLoader.loadLibrary

            // display splash NOW
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    // display splash screen
                    splashScreen.setVisible(true);
                }
            });

            // handle arguments
            handleAppArgs(args);

            // initialize network (better to do it at first as a lot of things need networking)
            NetworkUtil.init();

            // preload them here so further process won't wait them too much...
            PluginLoader.reloadAsynch();
            WorkspaceLoader.reload_asynch();

            // FIXME : We use InvokeNow(...) to avoid the JVM deadlock bug (id: 5104239) when the
            // AWT
            // thread is initialized while others threads load some new library with
            // ClassLoader.loadLibrary
            // ThreadUtil.invokeNow(new Runnable()
            // {
            // @Override
            // public void run()
            // {
            // // preload them here so further process won't wait them too much...
            // PluginLoader.reload_asynch();
            // WorkspaceLoader.reload_asynch();
            // }
            // });

            // initialize preferences (need network init)
            IcyPreferences.init();

            // build main interface
            mainInterface = new MainInterfaceGui();
        }
        catch (Throwable t)
        {
            // any error at this point is just fatal
            fatalError(t);
        }

        // do it on AWT thread NOW as this is what we want first
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // init Look And Feel (need mainInterface instance)
                    LookAndFeelUtil.init();
                    // init need "mainInterface" variable to be initialized
                    getMainInterface().init();
                }
                catch (Throwable t)
                {
                    // any error here is fatal
                    fatalError(t);
                }
            }
        });

        // then do less important stuff later
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // we can now hide splash as we have interface
                splashScreen.dispose();
                // show tool tip
                if (GeneralPreferences.getStatupTooltip())
                    new GeneralToolTipFrame();
            }
        });

        // show general informations
        System.out.println(SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " ("
                + SystemUtil.getJavaArchDataModel() + " bit)");
        System.out.println("Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " ("
                + SystemUtil.getOSArch() + ")");
        System.out.println("Number of processors : " + SystemUtil.getAvailableProcessors());
        System.out.println("System total memory : " + UnitUtil.getBytesString(SystemUtil.getTotalMemory()));
        System.out.println("System available memory : " + UnitUtil.getBytesString(SystemUtil.getFreeMemory()));
        System.out.println("Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()));

        // initialize OSX specific stuff
        if (SystemUtil.isMac())
            AppleUtil.init();
        // initialize security
        IcySecurityManager.init();
        // initialize exception handler
        IcyExceptionHandler.init();
        // prepare native library files (need preferences init)
        prepareNativeLibraries();

        // check for core update
        if (GeneralPreferences.getAutomaticUpdate() || GeneralPreferences.getAutomaticCheckUpdate())
            IcyUpdater.checkUpdate(false, GeneralPreferences.getAutomaticUpdate());
        // check for plugin update
        if (PluginPreferences.getAutomaticUpdate() || PluginPreferences.getAutomaticCheckUpdate())
            PluginUpdater.checkUpdate(false, PluginPreferences.getAutomaticUpdate());

        System.out.println();
        System.out.println("ICY Version " + version + " started !");
        System.out.println();

        checkParameters();
    }

    private static void handleAppArgs(String[] args)
    {
        for (String arg : args)
        {
            // special flag to disabled JCL (needed for development)
            if (arg.equalsIgnoreCase("--disableJCL") || arg.equalsIgnoreCase("-dJCL"))
                PluginLoader.JCLDisabled = true;
        }
    }

    static void checkParameters()
    {
        // we verify that some parameters are incorrect
        if ((GeneralPreferences.getMaxMemoryMB() <= 128) && (GeneralPreferences.getMaxMemoryMBLimit() > 128))
            MessageDialog.showDialog(
                    "Your maximum memory setting is low, you should increase it in preferences setting.",
                    MessageDialog.WARNING_MESSAGE);
    }

    static void fatalError(Throwable t)
    {
        // hide splashScreen if needed
        if ((splashScreen != null) && (splashScreen.isVisible()))
            splashScreen.dispose();

        // show error in console
        IcyExceptionHandler.showErrorMessage(t, true);
        // and show error in dialog
        JOptionPane.showMessageDialog(null, IcyExceptionHandler.getErrorMessage(t, true), "Fatal error",
                JOptionPane.ERROR_MESSAGE);

        // exit with error code 1
        System.exit(1);
    }

    /**
     * Restart application with user confirmation
     */
    public static void confirmRestart()
    {
        confirmRestart(null);
    }

    /**
     * Restart application with user confirmation (custom message)
     */
    public static void confirmRestart(String message)
    {
        final String mess;

        if (StringUtil.isEmpty(message))
            mess = "Application need to be restarted so changes can take effet. Do it now ?";
        else
            mess = message;

        if (ConfirmDialog.confirm(mess))
            // restart application now
            exit(true);
    }

    /**
     * Show announcement to restart application
     */
    public static void announceRestart()
    {
        announceRestart(null);
    }

    /**
     * Show announcement to restart application (custom message)
     */
    public static void announceRestart(String message)
    {
        final String mess;

        if (StringUtil.isEmpty(message))
            mess = "Application need to be restarted so changes can take effet.";
        else
            mess = message;

        new AnnounceFrame(mess, "Restart Now", new Runnable()
        {
            @Override
            public void run()
            {
                // restart application now
                exit(true);
            }
        }, 20);
    }

    /**
     * Return true if application can exit
     * This show a confirmation dialog if setting require it or if it's unsafe to exit now.
     */
    public static boolean canExit(boolean showConfirm)
    {
        // we first check if externals listeners allow existing
        if (!getMainInterface().canExitExternal())
            return false;

        // PluginInstaller or WorkspaceInstaller not running
        final boolean safeExit = (!PluginInstaller.isProcessing()) && (!WorkspaceInstaller.isProcessing());

        // not safe, need confirmation
        if (!safeExit)
        {
            if (!ConfirmDialog.confirm("Quit the application",
                    "Some processes are not yet completed, are you sure you want to quit ?",
                    ConfirmDialog.YES_NO_CANCEL_OPTION))
                return false;

            return true;
        }
        else if (showConfirm && GeneralPreferences.getExitConfirm())
        {
            // we need user confirmation
            if (!ConfirmDialog.confirm("Quit the application ?"))
                return false;

            return true;
        }

        return true;
    }

    /**
     * exit
     */
    public static boolean exit(final boolean restart)
    {
        // check we can exit application
        if (!canExit(!restart))
            return false;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // mark the application as exiting
                exiting = true;

                final ImageJ ij = Icy.getMainInterface().getImageJ();

                // clean ImageJ exit
                if (ij != null)
                    ij.quit();

                // close all icyFrames
                for (IcyFrame frame : IcyFrame.getAllFrames())
                    frame.close();
                // close all external frames
                for (JFrame frame : Icy.getMainInterface().getExternalFrames())
                    frame.dispose();
                // then close all JInternalFrames
                final JDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();
                if (desktopPane != null)
                {
                    for (JInternalFrame frame : desktopPane.getAllFrames())
                        frame.dispose();
                }

                // close main frame
                final JFrame mainFrame = Icy.getMainInterface().getMainFrame();
                if (mainFrame != null)
                    mainFrame.dispose();

                // save preferences
                IcyPreferences.save();

                // clean up native library files
                // unPrepareNativeLibraries();

                if (checkUnique != null)
                    checkUnique.releaseUnique();

                final boolean doUpdate = IcyUpdater.getWantUpdate();

                // launch updater if needed
                if (doUpdate || restart)
                    IcyUpdater.launchUpdater(doUpdate, restart);

                // good exit
                System.exit(0);
            }
        });

        return true;
    }

    /**
     * @param force
     * @deprecated use <code>exit(boolean)</code> instead
     */
    @Deprecated
    public static boolean exit(final boolean restart, boolean force)
    {
        return exit(restart);
    }

    /**
     * Return true is the application is currently exiting.
     */
    public static boolean isExiting()
    {
        return exiting;
    }

    public static MainInterface getMainInterface()
    {
        // batch mode
        if (mainInterface == null)
            mainInterface = new MainInterfaceBatch();

        return mainInterface;
    }

    /**
     * Add a viewer for the specified sequence
     * 
     * @param sequence
     *        The sequence to display
     */
    public static void addSequence(final Sequence sequence)
    {
        // thread safe
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new Viewer(sequence);
            }
        });
    }

    static void prepareNativeLibraries()
    {
        final String lastOs = ApplicationPreferences.getOs();
        final String os = SystemUtil.getOSArchIdString();
        final boolean osChanged = !StringUtil.equals(lastOs, os);

        // build the native local library path
        final String path = LIB_PATH + FileUtil.separator + os;
        // get file list (we don't want hidden files if any)
        final ArrayList<File> libraryFiles = FileUtil.getFileList(path, true, false);

        // copy to root directory
        for (File libraryFile : libraryFiles)
        {
            // get destination file (directly copy in root application directory)
            final File dstFile = new File(libraryFile.getName());

            // check if we need to copy the file
            if (osChanged || !dstFile.exists() || (dstFile.lastModified() != libraryFile.lastModified()))
                FileUtil.copy(libraryFile.getPath(), dstFile.getPath(), true, false);
        }

        // save os change
        if (osChanged)
            ApplicationPreferences.setOs(os);

        // load native libraries
        loadVtkLibrary();
    }

    private static void loadVtkLibrary()
    {
        try
        {
            System.loadLibrary("vtkCommonJava");
            System.loadLibrary("vtkFilteringJava");
            System.loadLibrary("vtkIOJava");
            System.loadLibrary("vtkImagingJava");
            System.loadLibrary("vtkGraphicsJava");
            System.loadLibrary("vtkRenderingJava");
            try
            {
                System.loadLibrary("vtkHybridJava");
            }
            catch (Throwable e)
            {
                System.out.println("cannot load vtkHybrid, skipping...");
            }

            System.out.println("VTK library successfully loaded...");
            vktLibraryLoaded = true;
        }
        catch (Throwable e)
        {
            System.out.println("Cannot load VTK library...");
        }
    }

    static void unPrepareNativeLibraries()
    {
        // build the native local library path
        final String path = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        // get file list (we don't want hidden files if any)
        final ArrayList<File> libraryFiles = FileUtil.getFileList(path, true, false);

        // remove previous copied files
        for (File libraryFile : libraryFiles)
        {
            // get file in root directory
            final File file = new File(libraryFile.getName());
            // invoke delete on exit if the file exists
            if (file.exists())
                file.deleteOnExit();
        }
    }
}
