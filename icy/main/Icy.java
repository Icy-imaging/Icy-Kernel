/*
 * Copyright 2010-2019 Institut Pasteur.
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
package icy.main;

import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import java.io.File;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import icy.action.ActionManager;
import icy.common.Version;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.IdConfirmDialog.Confirmer;
import icy.gui.frame.ExitFrame;
import icy.gui.frame.IcyExternalFrame;
import icy.gui.frame.SplashScreenFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.inspector.InspectorPanel;
import icy.gui.main.MainFrame;
import icy.gui.main.MainInterface;
import icy.gui.main.MainInterfaceBatch;
import icy.gui.main.MainInterfaceGui;
import icy.gui.system.NewVersionFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.image.cache.ImageCache;
import icy.imagej.ImageJPatcher;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginUpdater;
import icy.plugin.abstract_.Plugin;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.IcyPreferences;
import icy.preferences.PluginPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequencePrefetcher;
import icy.system.AppleUtil;
import icy.system.IcyExceptionHandler;
import icy.system.IcySecurityManager;
import icy.system.SingleInstanceCheck;
import icy.system.SystemUtil;
import icy.system.audit.Audit;
import icy.system.thread.ThreadUtil;
import icy.update.IcyUpdater;
import icy.util.StringUtil;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceLoader;
import ij.ImageJ;
import vtk.vtkNativeLibrary;
import vtk.vtkVersion;

/**
 * <h3>Icy - copyright 2019 Institut Pasteur</h3> An open community platform for bio image analysis<br>
 * <i>http://icy.bioimageanalysis.org</i><br>
 * <br>
 * Icy has been created by the Bio Image Analysis team at Institut Pasteur<br>
 * <i>https://research.pasteur.fr/fr/team/bioimage-analysis</i><br>
 * <br>
 * Icy is free and open source, it has been funded both by Institut Pasteur and the FBI consortium<br>
 * <i>https://france-bioimaging.org</i><br>
 * <br>
 * Source code is always provided in the main application package (in the icy.jar archive file) but can be also browsed
 * from the GitHub repository<br>
 * <i>https://github.com/Icy-imaging/Icy-Kernel</i><br>
 * <br>
 *
 * @author Stephane Dallongeville
 * @author Fabrice de Chaumont
 */
public class Icy
{
    public static final String LIB_PATH = "lib";
    public static final int EXIT_FORCE_DELAY = 3000;

    /**
     * Icy Version
     */
    public static Version version = new Version("2.0.1.0");

    /**
     * Main interface
     */
    private static MainInterface mainInterface = null;

    /**
     * Unique instance checker
     */
    static FileLock lock = null;

    /**
     * private splash for initial loading
     */
    static SplashScreenFrame splashScreen = null;

    /**
     * VTK library loaded flag
     */
    static boolean vtkLibraryLoaded = false;

    /**
     * ITK library loaded flag
     */
    static boolean itkLibraryLoaded = false;

    /**
     * No splash screen flag (default = false)
     */
    static boolean noSplash = false;
    /**
     * Exiting flag
     */
    static boolean exiting = false;

    /**
     * Startup parameters
     */
    static String[] args;
    static String[] pluginArgs;
    static String startupPluginName;
    static Plugin startupPlugin;
    static String startupImage;

    /**
     * internals
     */
    static ExitFrame exitFrame = null;
    static Thread terminer = null;

    /**
     * @param args
     *        Received from the command line.
     */
    public static void main(String[] args)
    {
        boolean headless = false;

        try
        {
            System.out.println("Initializing...");
            System.out.println();

            // handle arguments (must be the first thing to do)
            headless = handleAppArgs(args);

            // force headless if we have a headless system
            if (!headless && SystemUtil.isHeadLess())
                headless = true;

            // initialize preferences
            IcyPreferences.init();

            // check if Icy is already running
            lock = SingleInstanceCheck.lock("icy");
            if (lock == null)
            {
                // we always accept multi instance in headless mode
                if (!headless)
                {
                    // we need to use our custom ConfirmDialog as
                    // Icy.getMainInterface().isHeadless() will return false here
                    final Confirmer confirmer = new Confirmer("Confirmation",
                            "Icy is already running on this computer. Start anyway ?", JOptionPane.YES_NO_OPTION,
                            ApplicationPreferences.ID_SINGLE_INSTANCE);

                    ThreadUtil.invokeNow(confirmer);

                    if (!confirmer.getResult())
                    {
                        System.out.println("Exiting...");
                        // save preferences
                        IcyPreferences.save();
                        // and quit
                        System.exit(0);
                        return;
                    }
                }
            }

            // fix possible IllegalArgumentException on Swing sorting
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

            if (!headless && !noSplash)
            {
                // prepare splashScreen (ok to create it here as we are not yet in substance laf)
                splashScreen = new SplashScreenFrame();

                // It's important to initialize AWT now (with InvokeNow(...) for instance) to avoid
                // the JVM deadlock bug (id: 5104239). It happen when the AWT thread is initialized
                // while others threads load some new library with ClassLoader.loadLibrary

                // display splash NOW (don't use ThreadUtil as headless is still false here)
                EventQueue.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // display splash screen
                        splashScreen.setVisible(true);
                    }
                });
            }

            // set LOCI debug level (do it immediately as it can quickly show some log messages)
            loci.common.DebugTools.enableLogging("ERROR");

            // fast start
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // force image cache initialization so GUI won't wait after it (need preferences init)
                    ImageCache.isEnabled();
                }
            }, "Initializer 1").start();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // initialize network (need preferences init)
                    NetworkUtil.init();
                }
            }, "Initializer 2").start();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // load plugins classes (need preferences init)
                    PluginLoader.reloadAsynch();
                    WorkspaceLoader.reloadAsynch();
                }
            }, "Initializer 3").start();

            try
            {
                // patches ImageJ classes (need to be done before instancing ImageJ)
                ImageJPatcher.applyPatches();
            }
            catch (Throwable t)
            {
                System.err.println("Error while patching ImageJ classes:");
                IcyExceptionHandler.showErrorMessage(t, false);
            }

            // build main interface
            if (headless)
                mainInterface = new MainInterfaceBatch();
            else
                mainInterface = new MainInterfaceGui();
        }
        catch (Throwable t)
        {
            // any error at this point is fatal
            fatalError(t, headless);
        }

        if (!headless)
        {
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
                        fatalError(t, false);
                    }
                }
            });
        }
        else
        {
            // simple main interface init
            getMainInterface().init();
        }

        // splash screen initialized --> hide it
        if (splashScreen != null)
        {
            // then do less important stuff later
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    // we can now hide splash as we have interface
                    splashScreen.dispose();
                    splashScreen = null;
                }
            });
        }

        // show general informations
        System.out.println(SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " ("
                + SystemUtil.getJavaArchDataModel() + " bit)");
        System.out.println("Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " ("
                + SystemUtil.getOSArch() + ")");
        System.out.println("Number of processors : " + SystemUtil.getNumberOfCPUs());
        System.out.println("System total memory : " + UnitUtil.getBytesString(SystemUtil.getTotalMemory()));
        System.out.println("System available memory : " + UnitUtil.getBytesString(SystemUtil.getFreeMemory()));
        System.out.println("Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()));
        // image cache
        if (ImageCache.isEnabled())
        {
            System.out.println("Image cache initialized (reserved memory = " + ApplicationPreferences.getCacheMemoryMB()
                    + " MB, disk cache location = " + ApplicationPreferences.getCachePath() + ")");
        }
        else
        {
            System.err.println("Couldn't initialize image cache (cache is disabled)");
            // disable virtual mode button from inspector
            final InspectorPanel inspector = getMainInterface().getInspector();
            if (inspector != null)
                inspector.imageCacheDisabled();
        }
        if (headless)
            System.out.println("Headless mode.");
        System.out.println();

        // initialize OSX specific GUI stuff
        if (!headless && SystemUtil.isMac())
            AppleUtil.init();
        // initialize security
        IcySecurityManager.init();
        // initialize exception handler
        IcyExceptionHandler.init();
        // initialize action manager
        ActionManager.init();
        // prepare native library files (need preferences init)
        nativeLibrariesInit();

        // changed version ?
        if (!ApplicationPreferences.getVersion().equals(Icy.version))
        {
            // not headless ?
            if (!headless)
            {
                // display the new version information
                final String changeLog = Icy.getChangeLog();

                // show the new version frame
                if (!StringUtil.isEmpty(changeLog))
                {
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            new NewVersionFrame(Icy.getChangeLog());
                        }
                    });
                }
            }

            // force check update for the new version
            GeneralPreferences.setLastUpdateCheckTime(0);
        }

        // HTTPS not supported ?
        if (!NetworkUtil.isHTTPSSupported())
        {
            // final String text1 = "Your version of java does not support HTTPS connection required by the future new web site.";
            // final String text2 = "You need to upgrade java to 7u111 (Java 7) or 8u101 (Java 8) at least to be able to use online features in future.";
            final String text1 = "Your version of java does not support HTTPS protocol which will be used soon by the new web site.";
            final String text2 = "You need to upgrade your version of java to 7u111 (Java 7) or 8u101 (Java 8) at least to use online features (as search or plugin update) in future.";
            // final String text2 = "You won't be able to use online features (as search or plugin update) in future until you update your version of java (Java
            // 7u111 or Java
            // 8u101 minimum).";

            System.err.println("Warning: " + text1);
            System.err.println(text2);

            // TODO: change message when new web site is here
            if (!Icy.getMainInterface().isHeadLess())
                new ToolTipFrame("<html><b>WARNING:</b> " + text1 + "<br>" + text2 + "</html>", 0,
                        "httpsNotSupportedWarning");
            // new ToolTipFrame("<html><b>" + text1 + "<br>" + text2 + "</b></html>", 0, "httpsNotSupported");
        }

        final long currentTime = System.currentTimeMillis();
        final long halfDayInterval = 1000 * 60 * 60 * 12;

        // check only once per 12 hours slice
        if (currentTime > (GeneralPreferences.getLastUpdateCheckTime() + halfDayInterval))
        {
            // check for core update
            if (GeneralPreferences.getAutomaticUpdate())
                IcyUpdater.checkUpdate(true);
            // check for plugin update
            if (PluginPreferences.getAutomaticUpdate())
                PluginUpdater.checkUpdate(true);

            // update last update check time
            GeneralPreferences.setLastUpdateCheckTime(currentTime);
        }

        // update version info
        ApplicationPreferences.setVersion(Icy.version);
        // set LOCI debug level
        // loci.common.DebugTools.enableLogging("ERROR");
        // set OGL debug level
        // SystemUtil.setProperty("jogl.verbose", "TRUE");
        // SystemUtil.setProperty("jogl.debug", "TRUE");

        System.out.println();
        System.out.println("Icy Version " + version + " started !");
        System.out.println();

        checkParameters();

        // handle startup arguments
        if (startupImage != null)
            Icy.getMainInterface().addSequence(Loader.loadSequence(FileUtil.getGenericPath(startupImage), 0, false));

        // wait while updates are occurring before starting command line plugin...
        while (PluginUpdater.isCheckingForUpdate() || PluginInstaller.isProcessing()
                || WorkspaceInstaller.isProcessing())
            ThreadUtil.sleep(1);

        if (startupPluginName != null)
        {
            PluginLoader.waitWhileLoading();

            final PluginDescriptor plugin = PluginLoader.getPlugin(startupPluginName);

            if (plugin == null)
            {
                System.err.println("Could not launch plugin '" + startupPluginName + "': the plugin was not found.");
                System.err.println("Be sure you correctly wrote the complete class name and respected the case.");
                System.err.println("Ex: plugins.mydevid.analysis.MyPluginClass");
            }
            else
                startupPlugin = PluginLauncher.start(plugin);
        }

        // headless mode ? we can exit now...
        if (headless)
            exit(false);
    }

    private static boolean handleAppArgs(String[] args)
    {
        final List<String> pluginArgsList = new ArrayList<String>();

        startupImage = null;
        startupPluginName = null;
        startupPlugin = null;
        boolean execute = false;
        boolean headless = false;

        // save the base arguments
        Icy.args = args;

        for (String arg : args)
        {
            // store plugin arguments
            if (startupPluginName != null)
                pluginArgsList.add(arg);
            else if (execute)
                startupPluginName = arg;
            // special flag to disabled JCL (needed for development)
            else if (arg.equalsIgnoreCase("--disableJCL") || arg.equalsIgnoreCase("-dJCL"))
                PluginLoader.setJCLDisabled(true);
            // headless mode
            else if (arg.equalsIgnoreCase("--headless") || arg.equalsIgnoreCase("-hl"))
                headless = true;
            // disable splash-screen
            else if (arg.equalsIgnoreCase("--nosplash") || arg.equalsIgnoreCase("-ns"))
                noSplash = true;
            // execute plugin
            else if (arg.equalsIgnoreCase("--execute") || arg.equalsIgnoreCase("-x"))
                execute = true;
            // assume image name ?
            else
                startupImage = arg;
        }

        // save the plugin arguments
        Icy.pluginArgs = pluginArgsList.toArray(new String[pluginArgsList.size()]);

        return headless;
    }

    static void checkParameters()
    {
        // we are using a 32 bits JVM with a 64 bits OS --> warn the user
        if (SystemUtil.isWindows64() && SystemUtil.is32bits())
        {
            final String text = "You're using a 32 bits Java with a 64 bits OS, try to upgrade to 64 bits java for better performance !";

            System.out.println("Warning: " + text);

            if (!Icy.getMainInterface().isHeadLess())
                new ToolTipFrame("<html>" + text + "</html>", 15, "badJavaArchTip");
        }

        // we are using java 6 on OSX --> warn the user
        final double javaVersion = SystemUtil.getJavaVersionAsNumber();

        if ((javaVersion > 0) && (javaVersion < 7d) && SystemUtil.isMac())
        {
            final String text = "It looks like you're using a old version of Java (1.6)<br>"
                    + "It's recommended to use last JDK 8 for OSX for a better user experience.<br>See the <a href=\"http://icy.bioimageanalysis.org/faq#35\">FAQ</a> to get information about how to update java.";

            System.out.println("Warning: " + text);

            if (!Icy.getMainInterface().isHeadLess())
                new ToolTipFrame("<html>" + text + "</html>", 15, "outdatedJavaOSX");
        }

        // detect bad memory setting
        if ((ApplicationPreferences.getMaxMemoryMB() <= 128) && (ApplicationPreferences.getMaxMemoryMBLimit() > 256))
        {
            final String text = "Your maximum memory setting is low, you should increase it in Preferences.";

            System.out.println("Warning: " + text);

            if (!Icy.getMainInterface().isHeadLess())
                new ToolTipFrame("<html>" + text + "</html>", 15, "lowMemoryTip");
        }
        else if (ApplicationPreferences.getMaxMemoryMB() < (ApplicationPreferences.getDefaultMemoryMB() / 2))
        {
            if (!Icy.getMainInterface().isHeadLess())
            {
                new ToolTipFrame(
                        "<html><b>Tip:</b> you can increase your maximum memory in preferences setting.</html>", 15,
                        "maxMemoryTip");
            }
        }

        if (!Icy.getMainInterface().isHeadLess())
        {
            ToolTipFrame tooltip;

            // welcome tip !
            tooltip = new ToolTipFrame(
                    "<html>Access the main menu by clicking on top left icon<br>" + "<img src=\""
                            + Icy.class.getResource("/res/image/help/main_menu.png").toString() + "\" /></html>",
                    30, "mainMenuTip");
            tooltip.setSize(456, 240);

            // new Image Cache !
            tooltip = new ToolTipFrame("<html>" + "<img src=\""
                    + Icy.class.getResource("/res/image/help/virtual_mode.jpg").toString() + "\" /><br>"
                    + "This new button allow to enable/disable Icy <b>virtual mode</b>.<br><br>"
                    + "Virtual mode will force all new created images to be in <i>virtual mode</i> which mean their data can be stored on disk to spare memory.<br>"
                    + "Note that <i>virtual mode</i> is still experimental and <b>some plugins don't support it</b> (processed data can be lost) so use it carefully and only if you're running out of memory.<br>"
                    + "<i>You can change the image caching settings in Icy preferences</i></html>", 30, "virtualMode");
            tooltip.setSize(380, 240);
        }
    }

    static void fatalError(Throwable t, boolean headless)
    {
        // hide splashScreen if needed
        if ((splashScreen != null) && (splashScreen.isVisible()))
            splashScreen.dispose();

        // show error in console
        IcyExceptionHandler.showErrorMessage(t, true);
        // and show error in dialog if not headless
        if (!headless)
        {
            JOptionPane.showMessageDialog(null, IcyExceptionHandler.getErrorMessage(t, true), "Fatal error",
                    JOptionPane.ERROR_MESSAGE);
        }

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
            mess = "Application need to be restarted so changes can take effect. Do it now ?";
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

        if (Icy.getMainInterface().isHeadLess())
        {
            // just display this message
            System.out.println(mess);
        }
        else
        {
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
    }

    /**
     * Returns <code>true</code> if application can exit.<br>
     * Shows a confirmation dialog if setting requires it or if it's unsafe to exit now.
     */
    public static boolean canExit(boolean showConfirm)
    {
        // we first check if externals listeners allow existing
        if (!getMainInterface().canExitExternal())
            return false;

        // headless mode --> allow exit
        if (Icy.getMainInterface().isHeadLess())
            return true;

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
            if (!IdConfirmDialog.confirm("Quit the application ?", GeneralPreferences.ID_CONFIRM_EXIT))
                return false;

            return true;
        }

        return true;
    }

    /**
     * Exit Icy, returns <code>true</code> if the operation should success.<br>
     * Note that the method is asynchronous so you still have a bit of time to execute some stuff before the application
     * actually exit.
     */
    public static boolean exit(final boolean restart)
    {
        // check we can exit application
        if (!canExit(!restart))
            return false;

        // already existing
        if (exiting && terminer.isAlive())
        {
            // set focus on exit frame
            if (exitFrame != null)
                exitFrame.requestFocus();
            // return true;
            return true;
        }

        // we don't want to be in EDT here and avoid BG runner
        // as we test for BG runner completion
        terminer = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // mark the application as exiting
                exiting = true;

                System.out.println("Exiting...");

                final ImageJ ij = Icy.getMainInterface().getImageJ();

                // clean ImageJ exit
                if (ij != null)
                    ij.quit();

                // get main frame
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                // disconnect from chat (not needed but preferred)
                // if (mainFrame != null)
                // mainFrame.getChat().disconnect("Icy closed");

                // close all icyFrames (force wait completion)
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // for (IcyFrame frame : IcyFrame.getAllFrames())
                        // frame.close();
                        // close all JInternalFrames
                        final JDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

                        if (desktopPane != null)
                        {
                            for (JInternalFrame frame : desktopPane.getAllFrames())
                            {
                                try
                                {
                                    try
                                    {
                                        frame.setClosed(true);
                                    }
                                    catch (PropertyVetoException e)
                                    {
                                        // if (frame.getDefaultCloseOperation() !=
                                        // WindowConstants.DISPOSE_ON_CLOSE)
                                        frame.dispose();
                                    }
                                    catch (Throwable t)
                                    {
                                        // error on close ? --> try dispose
                                        frame.dispose();
                                    }
                                }
                                catch (Throwable t)
                                {
                                    // ignore further error here...
                                }
                            }
                        }

                        // then close all external frames except main frame
                        for (JFrame frame : Icy.getMainInterface().getExternalFrames())
                        {
                            if (frame != mainFrame)
                            {
                                if (frame instanceof IcyExternalFrame)
                                {
                                    final IcyExternalFrame iFrame = (IcyExternalFrame) frame;
                                    iFrame.close();
                                    if (iFrame.getDefaultCloseOperation() != WindowConstants.DISPOSE_ON_CLOSE)
                                        iFrame.dispose();
                                }
                                else
                                    frame.dispose();
                            }
                        }
                    }
                });

                // stop daemon plugin
                PluginLoader.stopDaemons();
                // shutdown background processor after frame close
                ThreadUtil.shutdown();
                // shutdown prefetcher
                SequencePrefetcher.shutdown();

                // headless mode
                if (Icy.getMainInterface().isHeadLess())
                {
                    // final long start = System.currentTimeMillis();
                    // // wait 10s max for background processors completed theirs tasks
                    // while (!ThreadUtil.isShutdownAndTerminated() && ((System.currentTimeMillis()
                    // - start) < 10 * 1000))
                    // ThreadUtil.sleep(1);

                    // wait that background processors completed theirs tasks
                    while (!ThreadUtil.isShutdownAndTerminated())
                        ThreadUtil.sleep(1);
                }
                else
                {
                    // need to create the exit frame in EDT
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // create and display the exit frame
                            exitFrame = new ExitFrame(EXIT_FORCE_DELAY);
                        }
                    });

                    // wait that background processors completed theirs tasks
                    while (!ThreadUtil.isShutdownAndTerminated() && !exitFrame.isForced())
                        ThreadUtil.sleep(1);

                    // need to dispose the exit frame in EDT (else we can have deadlock)
                    ThreadUtil.invokeNow(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // can close the exit frame now
                            exitFrame.dispose();
                        }
                    });
                }

                // need to dispose the main frame in EDT (else we can have deadlock)
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // finally close the main frame
                        if (mainFrame != null)
                            mainFrame.dispose();
                    }
                });

                // save preferences
                IcyPreferences.save();
                // save audit data
                Audit.save();
                // cache cleanup
                ImageCache.end();

                // clean up native library files
                // unPrepareNativeLibraries();

                // release lock
                if (lock != null)
                    SingleInstanceCheck.release(lock);

                final boolean doUpdate = IcyUpdater.getWantUpdate();

                // launch updater if needed
                if (doUpdate || restart)
                    IcyUpdater.launchUpdater(doUpdate, restart);

                // good exit
                System.exit(0);
            }
        });

        terminer.setName("Icy Shutdown");
        terminer.start();

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
     * Return true is VTK library loaded.
     */
    public static boolean isVtkLibraryLoaded()
    {
        return vtkLibraryLoaded;
    }

    /**
     * Return true is VTK library loaded.
     */
    public static boolean isItkLibraryLoaded()
    {
        return itkLibraryLoaded;
    }

    /**
     * @deprecated Use {@link MainInterface#isHeadLess()} instead.
     */
    @Deprecated
    public static boolean isHeadLess()
    {
        return getMainInterface().isHeadLess();
    }

    /**
     * Return true is the application is currently exiting.
     */
    public static boolean isExiting()
    {
        return exiting;
    }

    /**
     * Return the main Icy interface.
     */
    public static MainInterface getMainInterface()
    {
        // batch mode
        if (mainInterface == null)
            mainInterface = new MainInterfaceBatch();

        return mainInterface;
    }

    /**
     * Returns the command line arguments
     */
    public static String[] getCommandLineArgs()
    {
        return args;
    }

    /**
     * Returns the plugin command line arguments
     */
    public static String[] getCommandLinePluginArgs()
    {
        return pluginArgs;
    }

    /**
     * Clear the plugin command line arguments.<br>
     * This method should be called after the launching plugin actually 'consumed' the startup
     * arguments.
     */
    public static void clearCommandLinePluginArgs()
    {
        pluginArgs = new String[0];
    }

    /**
     * Returns the startup plugin if any
     */
    public static Plugin getStartupPlugin()
    {
        return startupPlugin;
    }

    /**
     * Return content of the <code>CHANGELOG.txt</code> file
     */
    public static String getChangeLog()
    {
        if (FileUtil.exists("CHANGELOG.txt"))
            return new String(FileUtil.load("CHANGELOG.txt", false));

        return "";
    }

    /**
     * Return content of the <code>COPYING.txt</code> file
     */
    public static String getLicense()
    {
        if (FileUtil.exists("COPYING.txt"))
            return new String(FileUtil.load("COPYING.txt", false));

        return "";
    }

    /**
     * Return content of the <code>README.txt</code> file
     */
    public static String getReadMe()
    {
        if (FileUtil.exists("README.txt"))
            return new String(FileUtil.load("README.txt", false));

        return "";
    }

    /**
     * @deprecated Uses <code>Icy.getMainInterface().addSequence(Sequence)</code> instead.
     */
    @Deprecated
    public static void addSequence(final Sequence sequence)
    {
        Icy.getMainInterface().addSequence(sequence);
    }

    static void nativeLibrariesInit()
    {
        // build the local native library path
        final String libPath = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        final File libPathFile = new File(libPath);

        // get all files in local native library path
        final File[] files = FileUtil.getFiles(libPathFile, null, true, true, false);
        final ArrayList<String> directories = new ArrayList<String>();

        // add base local native library path to user library paths
        directories.add(libPathFile.getAbsolutePath());
        // add base temporary native library path to user library paths
        directories.add(SystemUtil.getTempLibraryDirectory());

        for (File f : files)
        {
            if (f.isDirectory())
            {
                // add all directories to user library paths
                final String filePath = f.getAbsolutePath();
                if (!directories.contains(filePath))
                    directories.add(filePath);
            }
        }

        // add lib folder for unix system
        if (SystemUtil.isUnix())
        {
            directories.add("/lib");
            directories.add("/usr/lib");

            if (SystemUtil.is64bits())
            {
                directories.add("/lib64");
                directories.add("/lib/x86_64");
                directories.add("/lib/x86_64-linux-gnu");
                directories.add("/usr/lib64");
                directories.add("/usr/lib/x86_64");
                directories.add("/usr/lib/x86_64-linux-gnu");
            }
            else
            {
                directories.add("/lib/x86");
                directories.add("/lib/x86-linux-gnu");
                directories.add("/usr/lib/x86");
                directories.add("/usr/lib/x86-linux-gnu");
            }
        }

        if (!SystemUtil.addToJavaLibraryPath(directories.toArray(new String[directories.size()])))
            System.err.println("Native libraries may not load correctly.");

        // load native libraries
        loadVtkLibrary(libPathFile);
        // loadItkLibrary(libPathFile);

        // disable native lib support for JAI as we don't provide them
        SystemUtil.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    private static void loadVtkLibrary(File libPathFile)
    {
        final String vtkLibPath = FileUtil.getGenericPath(new File(libPathFile, "vtk").getAbsolutePath());

        // we load it directly from inner lib path if possible
        System.setProperty("vtk.lib.dir", vtkLibPath);

        vtkLibraryLoaded = false;
        try
        {
            // if (SystemUtil.isUnix())
            // {
            // vtkNativeLibrary.LoadAllNativeLibraries();
            //
            // // assume VTK loaded if at least 1 native library is loaded
            // for (vtkNativeLibrary lib : vtkNativeLibrary.values())
            // if (lib.IsLoaded())
            // vtkLibraryLoaded = true;
            // }
            // else
            {
                loadLibrary(vtkLibPath, "vtkalglib");
                loadLibrary(vtkLibPath, "vtkexpat");
                loadLibrary(vtkLibPath, "vtkDICOMParser");
                loadLibrary(vtkLibPath, "vtkjpeg");
                loadLibrary(vtkLibPath, "vtkjsoncpp");
                loadLibrary(vtkLibPath, "vtkzlib");
                loadLibrary(vtkLibPath, "vtkoggtheora", false);
                loadLibrary(vtkLibPath, "vtkverdict");
                loadLibrary(vtkLibPath, "vtkpng");

                loadLibrary(vtkLibPath, "vtkgl2ps", false); //

                loadLibrary(vtkLibPath, "vtktiff");
                loadLibrary(vtkLibPath, "vtklibxml2");
                loadLibrary(vtkLibPath, "vtkproj4");
                loadLibrary(vtkLibPath, "vtksys");
                loadLibrary(vtkLibPath, "vtkfreetype");
                loadLibrary(vtkLibPath, "vtkmetaio");
                loadLibrary(vtkLibPath, "vtkftgl", false);

                loadLibrary(vtkLibPath, "vtkftgl2", false); //

                loadLibrary(vtkLibPath, "vtkglew", false);
                loadLibrary(vtkLibPath, "vtkCommonCore");
                loadLibrary(vtkLibPath, "vtkWrappingJava");
                loadLibrary(vtkLibPath, "vtkCommonSystem");
                loadLibrary(vtkLibPath, "vtkCommonMath");
                loadLibrary(vtkLibPath, "vtkCommonMisc");
                loadLibrary(vtkLibPath, "vtkCommonTransforms");
                if (SystemUtil.isMac())
                {
                    loadLibrary(vtkLibPath, "vtkhdf5.8.0.2", false);
                    loadLibrary(vtkLibPath, "vtkhdf5_hl.8.0.2", false);
                }
                else
                {
                    loadLibrary(vtkLibPath, "vtkhdf5", false);
                    loadLibrary(vtkLibPath, "vtkhdf5_hl", false);
                }
                loadLibrary(vtkLibPath, "vtkNetCDF");
                loadLibrary(vtkLibPath, "vtkNetCDF_cxx");
                loadLibrary(vtkLibPath, "vtkCommonDataModel");
                loadLibrary(vtkLibPath, "vtkCommonColor");
                loadLibrary(vtkLibPath, "vtkCommonComputationalGeometry");
                loadLibrary(vtkLibPath, "vtkCommonExecutionModel");
                loadLibrary(vtkLibPath, "vtkexoIIc");
                loadLibrary(vtkLibPath, "vtkFiltersVerdict");
                loadLibrary(vtkLibPath, "vtkFiltersProgrammable");
                loadLibrary(vtkLibPath, "vtkImagingMath");
                loadLibrary(vtkLibPath, "vtkIOCore");
                loadLibrary(vtkLibPath, "vtkIOEnSight");
                loadLibrary(vtkLibPath, "vtkIOVideo");
                loadLibrary(vtkLibPath, "vtkIOLegacy");
                loadLibrary(vtkLibPath, "vtkIONetCDF");
                loadLibrary(vtkLibPath, "vtkIOXMLParser");
                loadLibrary(vtkLibPath, "vtkIOXML");
                loadLibrary(vtkLibPath, "vtkIOImage");
                loadLibrary(vtkLibPath, "vtksqlite", false);
                loadLibrary(vtkLibPath, "vtkIOSQL");
                loadLibrary(vtkLibPath, "vtkIOMovie");
                loadLibrary(vtkLibPath, "vtkParallelCore");
                loadLibrary(vtkLibPath, "vtkImagingCore");
                loadLibrary(vtkLibPath, "vtkFiltersCore");
                loadLibrary(vtkLibPath, "vtkImagingColor");
                loadLibrary(vtkLibPath, "vtkImagingFourier");
                loadLibrary(vtkLibPath, "vtkImagingSources");
                loadLibrary(vtkLibPath, "vtkImagingHybrid");
                loadLibrary(vtkLibPath, "vtkImagingStatistics");
                loadLibrary(vtkLibPath, "vtkIOGeometry");
                loadLibrary(vtkLibPath, "vtkIOPLY");
                loadLibrary(vtkLibPath, "vtkFiltersSelection");
                loadLibrary(vtkLibPath, "vtkImagingGeneral");
                loadLibrary(vtkLibPath, "vtkImagingStencil");
                loadLibrary(vtkLibPath, "vtkImagingMorphological");
                loadLibrary(vtkLibPath, "vtkFiltersGeometry");
                loadLibrary(vtkLibPath, "vtkFiltersStatistics");
                loadLibrary(vtkLibPath, "vtkFiltersImaging");
                loadLibrary(vtkLibPath, "vtkIOLSDyna");
                loadLibrary(vtkLibPath, "vtkFiltersGeneral");
                loadLibrary(vtkLibPath, "vtkFiltersHyperTree");
                loadLibrary(vtkLibPath, "vtkFiltersSMP");
                loadLibrary(vtkLibPath, "vtkFiltersTexture");
                loadLibrary(vtkLibPath, "vtkFiltersAMR");
                loadLibrary(vtkLibPath, "vtkFiltersExtraction");
                loadLibrary(vtkLibPath, "vtkFiltersSources");
                loadLibrary(vtkLibPath, "vtkIOExodus");
                loadLibrary(vtkLibPath, "vtkFiltersGeneric");
                loadLibrary(vtkLibPath, "vtkFiltersModeling");
                loadLibrary(vtkLibPath, "vtkIOAMR");
                loadLibrary(vtkLibPath, "vtkFiltersFlowPaths");
                loadLibrary(vtkLibPath, "vtkInfovisCore");
                loadLibrary(vtkLibPath, "vtkIOInfovis");
                loadLibrary(vtkLibPath, "vtkInfovisLayout");
                loadLibrary(vtkLibPath, "vtkRenderingCore");
                loadLibrary(vtkLibPath, "vtkRenderingLOD");
                loadLibrary(vtkLibPath, "vtkRenderingImage");
                loadLibrary(vtkLibPath, "vtkRenderingFreeType");
                loadLibrary(vtkLibPath, "vtkDomainsChemistry");

                loadLibrary(vtkLibPath, "vtkDomainsChemistryOpenGL2", false);

                loadLibrary(vtkLibPath, "vtkInteractionStyle");
                loadLibrary(vtkLibPath, "vtkIOImport");
                loadLibrary(vtkLibPath, "vtkRenderingAnnotation");
                loadLibrary(vtkLibPath, "vtkRenderingLabel");
                loadLibrary(vtkLibPath, "vtkFiltersHybrid");
                loadLibrary(vtkLibPath, "vtkFiltersParallel");
                loadLibrary(vtkLibPath, "vtkFiltersParallelImaging");
                loadLibrary(vtkLibPath, "vtkIOMINC");
                loadLibrary(vtkLibPath, "vtkIOParallel");
                loadLibrary(vtkLibPath, "vtkRenderingVolume");
                loadLibrary(vtkLibPath, "vtkRenderingVolumeAMR", false);
                loadLibrary(vtkLibPath, "vtkInteractionWidgets");
                loadLibrary(vtkLibPath, "vtkInteractionImage");
                loadLibrary(vtkLibPath, "vtkViewsCore");

                loadLibrary(vtkLibPath, "vtkRenderingOpenGL", false); //

                loadLibrary(vtkLibPath, "vtkRenderingOpenGL2", false);

                loadLibrary(vtkLibPath, "vtkRenderingLIC", false); //
                loadLibrary(vtkLibPath, "vtkRenderingVolumeOpenGL", false); //

                loadLibrary(vtkLibPath, "vtkRenderingVolumeOpenGL2", false);

                loadLibrary(vtkLibPath, "vtkRenderingContext2D");
                loadLibrary(vtkLibPath, "vtkRenderingContextOpenGL", false); //

                loadLibrary(vtkLibPath, "vtkRenderingContextOpenGL2", false);

                loadLibrary(vtkLibPath, "vtkRenderingGL2PS", false); //

                loadLibrary(vtkLibPath, "vtkViewsContext2D");
                loadLibrary(vtkLibPath, "vtkGeovisCore");
                loadLibrary(vtkLibPath, "vtkIOExport");
                loadLibrary(vtkLibPath, "vtkChartsCore");
                loadLibrary(vtkLibPath, "vtkViewsInfovis");
                loadLibrary(vtkLibPath, "vtkViewsGeovis", false);

                // JAVA wrapper
                loadLibrary(vtkLibPath, "vtkCommonCoreJava");
                loadLibrary(vtkLibPath, "vtkCommonSystemJava");
                loadLibrary(vtkLibPath, "vtkCommonMathJava");
                loadLibrary(vtkLibPath, "vtkCommonMiscJava");
                loadLibrary(vtkLibPath, "vtkCommonTransformsJava");
                loadLibrary(vtkLibPath, "vtkCommonDataModelJava");
                loadLibrary(vtkLibPath, "vtkCommonColorJava");
                loadLibrary(vtkLibPath, "vtkCommonComputationalGeometryJava");
                loadLibrary(vtkLibPath, "vtkCommonExecutionModelJava");
                loadLibrary(vtkLibPath, "vtkFiltersVerdictJava");
                loadLibrary(vtkLibPath, "vtkFiltersProgrammableJava");
                loadLibrary(vtkLibPath, "vtkImagingMathJava");
                loadLibrary(vtkLibPath, "vtkIOCoreJava");
                loadLibrary(vtkLibPath, "vtkIOEnSightJava");
                loadLibrary(vtkLibPath, "vtkIOVideoJava");
                loadLibrary(vtkLibPath, "vtkIOLegacyJava");
                loadLibrary(vtkLibPath, "vtkIONetCDFJava");
                loadLibrary(vtkLibPath, "vtkIOXMLParserJava");
                loadLibrary(vtkLibPath, "vtkIOXMLJava");
                loadLibrary(vtkLibPath, "vtkIOImageJava");
                loadLibrary(vtkLibPath, "vtkIOSQLJava");
                loadLibrary(vtkLibPath, "vtkIOMovieJava");
                loadLibrary(vtkLibPath, "vtkParallelCoreJava");
                loadLibrary(vtkLibPath, "vtkImagingCoreJava");
                loadLibrary(vtkLibPath, "vtkFiltersCoreJava");
                loadLibrary(vtkLibPath, "vtkImagingColorJava");
                loadLibrary(vtkLibPath, "vtkImagingFourierJava");
                loadLibrary(vtkLibPath, "vtkImagingSourcesJava");
                loadLibrary(vtkLibPath, "vtkImagingHybridJava");
                loadLibrary(vtkLibPath, "vtkImagingStatisticsJava");
                loadLibrary(vtkLibPath, "vtkIOGeometryJava");
                loadLibrary(vtkLibPath, "vtkIOPLYJava");
                loadLibrary(vtkLibPath, "vtkFiltersSelectionJava");
                loadLibrary(vtkLibPath, "vtkImagingGeneralJava");
                loadLibrary(vtkLibPath, "vtkImagingStencilJava");
                loadLibrary(vtkLibPath, "vtkImagingMorphologicalJava");
                loadLibrary(vtkLibPath, "vtkFiltersGeometryJava");
                loadLibrary(vtkLibPath, "vtkFiltersStatisticsJava");
                loadLibrary(vtkLibPath, "vtkFiltersImagingJava");
                loadLibrary(vtkLibPath, "vtkIOLSDynaJava");
                loadLibrary(vtkLibPath, "vtkFiltersGeneralJava");
                loadLibrary(vtkLibPath, "vtkFiltersHyperTreeJava");
                loadLibrary(vtkLibPath, "vtkFiltersSMPJava");
                loadLibrary(vtkLibPath, "vtkFiltersTextureJava");
                loadLibrary(vtkLibPath, "vtkFiltersAMRJava");
                loadLibrary(vtkLibPath, "vtkFiltersExtractionJava");
                loadLibrary(vtkLibPath, "vtkFiltersSourcesJava");
                loadLibrary(vtkLibPath, "vtkIOExodusJava");
                loadLibrary(vtkLibPath, "vtkFiltersGenericJava");
                loadLibrary(vtkLibPath, "vtkFiltersModelingJava");
                loadLibrary(vtkLibPath, "vtkIOAMRJava");
                loadLibrary(vtkLibPath, "vtkFiltersFlowPathsJava");
                loadLibrary(vtkLibPath, "vtkInfovisCoreJava");
                loadLibrary(vtkLibPath, "vtkIOInfovisJava");
                loadLibrary(vtkLibPath, "vtkInfovisLayoutJava");
                loadLibrary(vtkLibPath, "vtkRenderingCoreJava");
                loadLibrary(vtkLibPath, "vtkRenderingLODJava");
                loadLibrary(vtkLibPath, "vtkRenderingImageJava");
                loadLibrary(vtkLibPath, "vtkRenderingFreeTypeJava");
                loadLibrary(vtkLibPath, "vtkDomainsChemistryJava");

                loadLibrary(vtkLibPath, "vtkDomainsChemistryOpenGL2Java", false);

                loadLibrary(vtkLibPath, "vtkInteractionStyleJava");
                loadLibrary(vtkLibPath, "vtkIOImportJava");
                loadLibrary(vtkLibPath, "vtkRenderingAnnotationJava");
                loadLibrary(vtkLibPath, "vtkRenderingLabelJava");
                loadLibrary(vtkLibPath, "vtkFiltersHybridJava");
                loadLibrary(vtkLibPath, "vtkFiltersParallelJava");
                loadLibrary(vtkLibPath, "vtkFiltersParallelImagingJava");
                loadLibrary(vtkLibPath, "vtkIOMINCJava");
                loadLibrary(vtkLibPath, "vtkIOParallelJava");
                loadLibrary(vtkLibPath, "vtkRenderingVolumeJava");
                loadLibrary(vtkLibPath, "vtkRenderingVolumeAMRJava", false);
                loadLibrary(vtkLibPath, "vtkInteractionWidgetsJava");
                loadLibrary(vtkLibPath, "vtkInteractionImageJava");
                loadLibrary(vtkLibPath, "vtkViewsCoreJava");
                loadLibrary(vtkLibPath, "vtkRenderingOpenGLJava", false); //

                loadLibrary(vtkLibPath, "vtkRenderingOpenGL2Java", false);

                loadLibrary(vtkLibPath, "vtkRenderingLICJava", false); //
                loadLibrary(vtkLibPath, "vtkRenderingVolumeOpenGLJava", false); //

                loadLibrary(vtkLibPath, "vtkRenderingVolumeOpenGL2Java", false);

                loadLibrary(vtkLibPath, "vtkRenderingContext2DJava");

                loadLibrary(vtkLibPath, "vtkRenderingContextOpenGLJava", false); //

                loadLibrary(vtkLibPath, "vtkRenderingContextOpenGL2Java", false);

                loadLibrary(vtkLibPath, "vtkRenderingGL2PSJava", false); //

                loadLibrary(vtkLibPath, "vtkViewsContext2DJava");
                loadLibrary(vtkLibPath, "vtkGeovisCoreJava");
                loadLibrary(vtkLibPath, "vtkIOExportJava");
                loadLibrary(vtkLibPath, "vtkChartsCoreJava");
                loadLibrary(vtkLibPath, "vtkViewsInfovisJava");
                loadLibrary(vtkLibPath, "vtkViewsGeovisJava", false);

                // VTK library successfully loaded
                vtkLibraryLoaded = true;
            }

            // redirect vtk error log to file
            vtkNativeLibrary.DisableOutputWindow(new File("vtk.log"));
        }
        catch (Throwable e1)
        {
            IcyExceptionHandler.showErrorMessage(e1, false, false);

            // // try to load the VTK way
            // try
            // {
            // System.out.print("Try to load VTK library using VTK method... ");
            //
            // vtkLibraryLoaded = vtkNativeLibrary.LoadAllNativeLibraries();
            //
            // if (vtkLibraryLoaded)
            // System.out.println("success !");
            // else
            // System.out.println("failed !");
            //
            // // redirect vtk error log to file
            // vtkNativeLibrary.DisableOutputWindow(new File("vtk.log"));
            // }
            // catch (Throwable e2)
            // {
            // IcyExceptionHandler.showErrorMessage(e2, false, false);
            // }
        }

        if (vtkLibraryLoaded)
        {
            final String vv = new vtkVersion().GetVTKVersion();

            System.out.println("VTK " + vv + " library successfully loaded...");

            // final vtkJavaGarbageCollector vtkJavaGarbageCollector =
            // vtkObjectBase.JAVA_OBJECT_MANAGER
            // .getAutoGarbageCollector();
            //
            // set auto garbage collection for VTK (every 20 seconds should be enough)
            // probably not a good idea...
            // vtkJavaGarbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
            // vtkJavaGarbageCollector.SetAutoGarbageCollection(true);
        }
        else
        {
            System.out.println("Cannot load VTK library...");

            if (SystemUtil.isMac())
            {
                final String osVer = SystemUtil.getOSVersion();

                if (osVer.startsWith("10.6") || osVer.startsWith("10.5"))
                    System.out.println(
                            "VTK 6.3 is not supported on OSX " + osVer + ", version 10.7 or above is required.");
            }
        }
    }

    private static void loadItkLibrary(String osDir)
    {
        final String itkLibDir = osDir + FileUtil.separator + "itk";

        try
        {
            loadLibrary(itkLibDir, "SimpleITKJava", true);

            System.out.println("SimpleITK library successfully loaded...");
            itkLibraryLoaded = true;
        }
        catch (Throwable e)
        {
            System.out.println("Cannot load SimpleITK library...");
        }
    }

    private static void loadLibrary(String dir, String name, boolean mandatory, boolean showLog)
    {
        if (mandatory)
            SystemUtil.loadLibrary(dir, name);
        else
        {
            try
            {
                SystemUtil.loadLibrary(dir, name);
            }
            catch (Throwable e)
            {
                if (showLog)
                    System.out.println("cannot load " + name + ", skipping...");
            }
        }
    }

    private static void loadLibrary(String dir, String name, boolean mandatory)
    {
        loadLibrary(dir, name, mandatory, false);
    }

    private static void loadLibrary(String dir, String name)
    {
        loadLibrary(dir, name, true, false);
    }

    static void nativeLibrariesShutdown()
    {
        // build the native local library path
        final String path = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        // get file list (we don't want hidden files if any)
        File[] libraryFiles = FileUtil.getFiles(new File(path), null, true, false, false);

        // remove previous copied files
        for (File libraryFile : libraryFiles)
        {
            // get file in root directory
            final File file = new File(libraryFile.getName());
            // invoke delete on exit if the file exists
            if (file.exists())
                file.deleteOnExit();
        }

        // get file list from temporary native library path
        libraryFiles = FileUtil.getFiles(new File(SystemUtil.getTempLibraryDirectory()), null, true, false, false);

        // remove previous copied files
        for (File libraryFile : libraryFiles)
        {
            // delete file
            if (!FileUtil.delete(libraryFile, false))
                libraryFile.deleteOnExit();
        }
    }
}
