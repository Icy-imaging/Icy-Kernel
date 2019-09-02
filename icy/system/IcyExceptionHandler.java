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
package icy.system;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icy.gui.dialog.MessageDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.plugin.PluginErrorReport;
import icy.main.Icy;
import icy.network.WebInterface;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.util.ClassUtil;
import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class IcyExceptionHandler implements UncaughtExceptionHandler
{
    private static final double ERROR_ANTISPAM_TIME = 15 * 1000;
    private static IcyExceptionHandler exceptionHandler = new IcyExceptionHandler();
    private static long lastErrorDialog = 0;
    private static long lastErrorReport = 0;
    private static Set<String> reportedPlugins = new HashSet<String>();

    public static void init()
    {
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    /**
     * Display the specified Throwable message in error output.
     */
    public static void showErrorMessage(Throwable t, boolean printStackTrace)
    {
        showErrorMessage(t, printStackTrace, true);
    }

    /**
     * Display the specified Throwable message in console.<br>
     * If <i>error</i> is true the message is considerer as an error and then written in error
     * output.
     */
    public static void showErrorMessage(Throwable t, boolean printStackTrace, boolean error)
    {
        final String mess = getErrorMessage(t, printStackTrace);

        if (!StringUtil.isEmpty(mess))
        {
            if (error)
                System.err.println(mess);
            else
                System.out.println(mess);
        }
    }

    /**
     * Returns the formatted error message for the specified {@link Throwable}.<br>
     * If <i>printStackTrace</i> is <code>true</code> the stack trace is also returned in the
     * message.
     */
    public static String getErrorMessage(Throwable t, boolean printStackTrace)
    {
        String result = "";
        Throwable throwable = t;

        while (throwable != null)
        {
            result += throwable.toString() + "\n";

            if (printStackTrace)
            {
                try
                {
                    // sometime 'getStackTrace()' throws a weird AbstractMethodError exception
                    for (StackTraceElement element : throwable.getStackTrace())
                        result += "\tat " + element.toString() + "\n";
                }
                catch (Throwable t2)
                {
                    result += "Error while trying to get exception stack trace...\n";
                }
            }

            throwable = throwable.getCause();
            if (throwable != null)
                result += "Caused by :\n";
        }

        return result;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        handleException(t, e, true);
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    private static void handleException(Thread thread, PluginDescriptor plugin, String devId, Throwable t,
            boolean printStackStrace)
    {
        final long current = System.currentTimeMillis();
        final String errMess = (t.getMessage() != null) ? t.getMessage() : "";

        if (t instanceof IcyHandledException)
        {
            final String message = errMess + ((t.getCause() == null) ? "" : "\n" + t.getCause());

            // handle HandledException differently
            MessageDialog.showDialog(message, MessageDialog.ERROR_MESSAGE);
            // update last error dialog time
            lastErrorDialog = System.currentTimeMillis();

            // don't need the antispam for the IcyHandledException
            // if ((current - lastErrorDialog) > ERROR_ANTISPAM_TIME)
            // {
            // // handle HandledException differently
            // MessageDialog.showDialog(message, MessageDialog.ERROR_MESSAGE);
            // // update last error dialog time
            // lastErrorDialog = System.currentTimeMillis();
            // }
            // else
            // // spam --> write it in the console output instead
            // System.err.println(message + " (spam protection)");
        }
        else
        {
            String message = "";

            if (t instanceof OutOfMemoryError)
            {
                if (errMess.contains("Thread"))
                {
                    message = "Out of resource error: cannot create new thread.\n"
                            + "You should report this error as something goes wrong here !";
                }
                else
                {
                    message = "The task could not be completed because there is not enough memory !\n"
                            + "Try to use 'virtual mode' (image caching) or increase increase the 'Maximum Memory' parameter in Preferences.";
                }
            }

            if (!StringUtil.isEmpty(message))
                message += "\n";
            message += getErrorMessage(t, printStackStrace);

            // write message in console if wanted or if spam error message
            if ((t instanceof OutOfMemoryError) || printStackStrace
                    || ((current - lastErrorDialog) < ERROR_ANTISPAM_TIME))
            {
                if (plugin != null)
                    System.err.println("An error occured while plugin '" + plugin.getName() + "' was running :");
                else if (!StringUtil.isEmpty(devId))
                    System.err.println("An error occured while a plugin was running :");

                System.err.println(message);
            }

            // do report (anti spam protected)
            if ((current - lastErrorDialog) > ERROR_ANTISPAM_TIME)
            {
                final String title = t.toString();

                // handle the specific "not enough memory" differently
                if ((t instanceof OutOfMemoryError) && (!errMess.contains("Thread")))
                {
                    if (!Icy.getMainInterface().isHeadLess())
                        new FailedAnnounceFrame(
                                "Not enough memory to complete the process ! Try to use 'Virtual Mode' (image caching) or increase the 'Maximum Memory' parameter in Preferences.",
                                30);
                }
                else
                    // just report the error
                    PluginErrorReport.report(plugin, devId, title, message);

                // update last error dialog time
                lastErrorDialog = System.currentTimeMillis();
            }
        }
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(PluginDescriptor pluginDesc, Throwable t, boolean printStackStrace)
    {
        handleException(null, pluginDesc, null, t, printStackStrace);
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(String devId, Throwable t, boolean printStackStrace)
    {
        handleException(null, null, devId, t, printStackStrace);
    }

    /**
     * Handle the specified exception.<br>
     * Try to find the origin plugin which thrown the exception.
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(Throwable t, boolean printStackStrace)
    {
        handleException((Thread) null, t, printStackStrace);
    }

    /**
     * Handle the specified exception.<br>
     * Try to find the origin plugin which thrown the exception.
     * It actually display a message or report dialog depending the exception type.
     */
    private static void handleException(Thread thread, Throwable t, boolean printStackStrace)
    {
        Throwable throwable = t;
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins();

        while (throwable != null)
        {
            StackTraceElement[] stackTrace;

            try
            {
                // sometime 'getStackTrace()' throws a weird AbstractMethodError exception
                stackTrace = throwable.getStackTrace();
            }
            catch (Throwable t2)
            {
                stackTrace = new StackTraceElement[0];
            }

            // search plugin class (start from the end of stack trace)
            final PluginDescriptor plugin = findPluginFromStackTrace(plugins, stackTrace);

            // plugin found --> show the plugin report frame
            if (plugin != null)
            {
                // only send to last plugin raising the exception
                handleException(thread, plugin, null, t, printStackStrace);
                return;
            }

            // we did not find plugin class so we will search for plugin developer id
            final String devId = findDevIdFromStackTrace(stackTrace);

            if (devId != null)
            {
                handleException(thread, null, devId, t, printStackStrace);
                return;
            }

            throwable = throwable.getCause();
        }

        // general exception (no plugin information found)
        handleException(thread, null, null, t, printStackStrace);
    }

    /**
     * @deprecated Use {@link #handleException(PluginDescriptor, Throwable, boolean)} instead.
     */
    @Deprecated
    public static void handlePluginException(PluginDescriptor pluginDesc, Throwable t, boolean printStackStrace)
    {
        handleException(pluginDesc, t, printStackStrace);
    }

    private static PluginDescriptor findMatchingLocalPlugin(List<PluginDescriptor> plugins, String text)
    {
        String className = ClassUtil.getBaseClassName(text);

        // get the JAR file of this class
        final File file = ClassUtil.getFile(className);

        // found ?
        if (file != null)
        {
            // try to find plugin using the same JAR file (so
            for (PluginDescriptor p : plugins)
            {
                final String jarFileName = p.getJarFilename();

                if (!StringUtil.isEmpty(jarFileName))
                {
                    final File jarFile = new File(jarFileName);

                    // matching jar file --> return plugin
                    if (StringUtil.equals(file.getAbsolutePath(), jarFile.getAbsolutePath()))
                        return p;
                }
            }
        }

        // not found with first method so now we try on the class name
        while (!(StringUtil.equals(className, PluginLoader.PLUGIN_PACKAGE) || StringUtil.isEmpty(className)))
        {
            final PluginDescriptor plugin = findMatchingLocalPluginInternal(plugins, className);

            if (plugin != null)
                return plugin;

            // not found --> we test with parent package
            className = ClassUtil.getPackageName(className);
        }

        return null;
    }

    private static PluginDescriptor findMatchingLocalPluginInternal(List<PluginDescriptor> plugins, String text)
    {
        PluginDescriptor result = null;

        for (PluginDescriptor plugin : plugins)
        {
            if (plugin.getClassName().startsWith(text))
            {
                if (result != null)
                    return null;

                result = plugin;
            }
        }

        return result;
    }

    private static PluginDescriptor findPluginFromStackTrace(List<PluginDescriptor> plugins, StackTraceElement[] st)
    {
        for (StackTraceElement trace : st)
        {
            final String className = trace.getClassName();

            // plugin class ?
            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
            {
                // try to find a matching plugin
                final PluginDescriptor plugin = findMatchingLocalPlugin(plugins, className);

                // plugin found --> show the plugin report frame
                if (plugin != null)
                    return plugin;
            }
        }

        return null;
    }

    private static String findDevIdFromStackTrace(StackTraceElement[] st)
    {
        // we did not find plugin class so we will search for plugin developer id
        for (StackTraceElement trace : st)
        {
            final String className = trace.getClassName();

            // plugin class ?
            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
                // use plugin developer id (only send to last plugin raising the exception)
                return className.split("\\.")[1];
        }

        return null;
    }

    /**
     * Report an error log from a given plugin or developer id to Icy web site.
     * 
     * @param plugin
     *        The plugin responsible of the error or <code>null</code> if the error comes from the
     *        application or if we are not able to get the plugin descriptor.
     * @param devId
     *        The developer id of the plugin responsible of the error when the plugin descriptor was
     *        not found or <code>null</code> if the error comes from the application.
     * @param errorLog
     *        Error log to report.
     */
    public static void report(PluginDescriptor plugin, String devId, String errorLog)
    {
        final long current = System.currentTimeMillis();

        // avoid report spam
        if ((current - lastErrorReport) < ERROR_ANTISPAM_TIME)
            return;
        // we already reported error for this plugin --> avoid spaming
        if ((plugin != null) && reportedPlugins.contains(plugin.getClassName()))
            return;

        // store last send time
        lastErrorReport = current;

        // do error report
        WebInterface.reportError(plugin, devId, errorLog);

        // final String icyId;
        // final String javaId;
        // final String osId;
        // final String memory;
        // String pluginId;
        // String pluginDepsId;
        // final Map<String, String> values = new HashMap<String, String>();
        //
        // values.put(NetworkUtil.ID_KERNELVERSION, Icy.version.toString());
        // values.put(NetworkUtil.ID_JAVANAME, SystemUtil.getJavaName());
        // values.put(NetworkUtil.ID_JAVAVERSION, SystemUtil.getJavaVersion());
        // values.put(NetworkUtil.ID_JAVABITS, Integer.toString(SystemUtil.getJavaArchDataModel()));
        // values.put(NetworkUtil.ID_OSNAME, SystemUtil.getOSName());
        // values.put(NetworkUtil.ID_OSVERSION, SystemUtil.getOSVersion());
        // values.put(NetworkUtil.ID_OSARCH, SystemUtil.getOSArch());
        //
        // icyId = "Icy Version " + Icy.version + "\n";
        // javaId = SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " (" + SystemUtil.getJavaArchDataModel()
        // + " bit)\n";
        // osId = "Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " (" + SystemUtil.getOSArch()
        // + ")\n";
        // memory = "Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()) + "\n";
        //
        // if (plugin != null)
        // {
        // final String className = plugin.getClassName();
        //
        // // we already reported error for this plugin --> avoid spaming
        // if (reportedPlugins.contains(className))
        // return;
        //
        // reportedPlugins.add(className);
        //
        // values.put(NetworkUtil.ID_PLUGINCLASSNAME, className);
        // values.put(NetworkUtil.ID_PLUGINVERSION, plugin.getVersion().toString());
        // pluginId = "Plugin " + plugin.toString();
        //
        // // determine origin plugin
        // PluginDescriptor originPlugin = plugin;
        //
        // // bundled plugin ?
        // if (plugin.isBundled())
        // {
        // try
        // {
        // // get original plugin
        // originPlugin = PluginLoader
        // .getPlugin(((PluginBundled) PluginLauncher.create(plugin)).getMainPluginClassName());
        // // add bundle info
        // pluginId = "Bundled in " + originPlugin.toString();
        // }
        // catch (Throwable t)
        // {
        // // miss bundle info
        // pluginId = "Bundled plugin (could not retrieve origin plugin)";
        // }
        // }
        //
        // pluginId += "\n\n";
        //
        // if (originPlugin.getRequired().size() > 0)
        // {
        // pluginDepsId = "Dependances:\n";
        // for (PluginIdent ident : originPlugin.getRequired())
        // {
        // final PluginDescriptor installed = PluginLoader.getPlugin(ident.getClassName());
        //
        // if (installed == null)
        // pluginDepsId += "Class " + ident.getClassName() + " not found !\n";
        // else
        // pluginDepsId += "Plugin " + installed.toString() + " is correctly installed\n";
        // }
        // pluginDepsId += "\n";
        // }
        // else
        // pluginDepsId = "";
        // }
        // else
        // {
        // values.put(NetworkUtil.ID_PLUGINCLASSNAME, "");
        // values.put(NetworkUtil.ID_PLUGINVERSION, "");
        // pluginId = "";
        // pluginDepsId = "";
        // }
        //
        // if (StringUtil.isEmpty(devId))
        // values.put(NetworkUtil.ID_DEVELOPERID, devId);
        // else
        // values.put(NetworkUtil.ID_DEVELOPERID, "");
        //
        // values.put(NetworkUtil.ID_ERRORLOG, icyId + javaId + osId + memory + "\n" + pluginId + pluginDepsId + errorLog);
        //
        // // send report
        // lastErrorReport = current;
        // NetworkUtil.report(values);
    }

    /**
     * Report an error log from a given plugin to Icy web site.
     * 
     * @param plugin
     *        The plugin responsible of the error or <code>null</code> if the error comes from the
     *        application.
     * @param errorLog
     *        Error log to report.
     */
    public static void report(PluginDescriptor plugin, String errorLog)
    {
        report(plugin, null, errorLog);
    }

    /**
     * Report an error log from the application to Icy web site.
     * 
     * @param errorLog
     *        Error log to report.
     */
    public static void report(String errorLog)
    {
        report(null, null, errorLog);
    }

    /**
     * Report an error log from a given plugin developer id to the Icy web site.
     * 
     * @param devId
     *        The developer id of the plugin responsible of the error or <code>null</code> if the
     *        error comes from the application.
     * @param errorLog
     *        Error log to report.
     */
    public static void report(String devId, String errorLog)
    {
        report(null, devId, errorLog);
    }

}
