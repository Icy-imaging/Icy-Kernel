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
package icy.system;

import icy.gui.dialog.MessageDialog;
import icy.gui.plugin.PluginErrorReport;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginDescriptor.PluginIdent;
import icy.plugin.PluginLoader;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;

/**
 * @author Stephane
 */
public class IcyExceptionHandler implements UncaughtExceptionHandler
{
    private static final double ERROR_ANTISPAM_TIME = 15 * 1000;
    private static IcyExceptionHandler exceptionHandler = new IcyExceptionHandler();
    private static long lastErrorDialog = 0;

    public static void init()
    {
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    /**
     * Display the specified Throwable message in console.<br>
     * If <i>error</i> is true the message is considerer as an error and then written in error
     * output.
     */
    private static void showSimpleErrorMessage(Throwable t, boolean error)
    {
        if (t != null)
        {
            final String mess = "Caused by : " + t.toString();

            if (error)
                System.err.println(mess);
            else
                System.out.println(mess);

            showSimpleErrorMessage(t.getCause(), error);
        }
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
        if (t != null)
        {
            showSimpleErrorMessage(t.getCause(), error);

            if (printStackTrace)
                t.printStackTrace();

            if (error)
                System.err.println(t.toString());
            else
                System.out.println(t.toString());
        }
    }

    private static String getSimpleErrorMessage(Throwable t)
    {
        if (t != null)
            return "Caused by : " + t.toString() + "\n" + getSimpleErrorMessage(t.getCause());

        return "";
    }

    public static String getErrorMessage(Throwable t, boolean printStackTrace)
    {
        if (t != null)
        {
            String result = getSimpleErrorMessage(t.getCause()) + t.toString() + "\n";

            if (printStackTrace)
                for (StackTraceElement element : t.getStackTrace())
                    result = result + "\tat " + element.toString() + "\n";

            return result;
        }

        return "";
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        // final Thread thread = t;
        final Throwable throwable = e;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                handleException(throwable, true);
            }
        });
    }

    static PluginDescriptor findMatchingLocalPlugin(List<PluginDescriptor> plugins, String text)
    {
        String className = ClassUtil.getBaseClassName(text);

        while (!(StringUtil.equals(className, PluginLoader.PLUGIN_PACKAGE) || StringUtil.isEmpty(className)))
        {
            final PluginDescriptor plugin = findMatchingLocalPluginInternal(plugins, className);

            if (plugin != null)
                return plugin;

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

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    private static void handleException(PluginDescriptor plugin, String devId, Throwable t, boolean print)
    {
        final long current = System.currentTimeMillis();

        if (t instanceof IcyHandledException)
        {
            final String message = t.getMessage() + ((t.getCause() == null) ? "" : "\n" + t.getCause());

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
            if (t instanceof OutOfMemoryError)
            {
                if ((current - lastErrorDialog) > ERROR_ANTISPAM_TIME)
                {
                    // handle out of memory error differently
                    MessageDialog
                            .showDialog(
                                    "Out of memory error !",
                                    "You're running out of memory !\nTry to increase the Maximum Memory parameter in Preferences.",
                                    MessageDialog.ERROR_MESSAGE);
                    // update last error dialog time
                    lastErrorDialog = System.currentTimeMillis();
                }

                // always keep trace in console
                showErrorMessage(t, print);
            }

            String message = getErrorMessage(t, true);

            if (t instanceof OutOfMemoryError)
            {
                message = "Not enough memory or resource !\n"
                        + "Try to increase the Maximum Memory parameter in Preferences.\n"
                        + "You can also report the error if you think it is not due to a memory problem:\n" + message;
            }

            // write message in console if wanted or if spam error message
            if (print || ((current - lastErrorDialog) < ERROR_ANTISPAM_TIME))
            {
                if (plugin != null)
                    System.err.println("An error occured while plugin '" + plugin.getName() + "' was running :");
                else if (!StringUtil.isEmpty(devId))
                    System.err.println("An error occured while a plugin was running :");

                System.err.println(message);
            }

            // NOTE : why only for plugin ??
            // do report only for plugin error
            // if ((plugin != null) || !StringUtil.isEmpty(devId))
            // {
            if ((current - lastErrorDialog) > ERROR_ANTISPAM_TIME)
            {
                String title = t.getMessage();

                if (StringUtil.isEmpty(title))
                    title = t.toString();

                PluginErrorReport.report(plugin, devId, title, message);
                // update last error dialog time
                lastErrorDialog = System.currentTimeMillis();
            }
            // }
        }
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(PluginDescriptor pluginDesc, Throwable t, boolean print)
    {
        handleException(pluginDesc, null, t, print);
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(String devId, Throwable t, boolean print)
    {
        handleException(null, devId, t, print);
    }

    /**
     * Handle the specified exception.<br>
     * Try to find the origin plugin which thrown the exception.
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(Throwable t, boolean print)
    {
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins();

        // search plugin class (start from the end of stack trace)
        for (StackTraceElement trace : t.getStackTrace())
        {
            final String className = trace.getClassName();

            // plugin class ?
            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
            {
                // try to find a matching plugin
                final PluginDescriptor plugin = findMatchingLocalPlugin(plugins, className);

                // plugin found --> show the plugin report frame
                if (plugin != null)
                {
                    // only send to last plugin raising the exception
                    handleException(plugin, t, print);
                    return;
                }
            }
        }

        // we did not find plugin class so we will search for plugin developer id
        for (StackTraceElement trace : t.getStackTrace())
        {
            final String className = trace.getClassName();

            // plugin class ?
            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
            {
                // use plugin developer id (only send to last plugin raising the exception)
                handleException(className.split("\\.")[1], t, print);
                return;
            }
        }

        // general exception (no plugin information found)
        handleException(null, null, t, print);
    }

    /**
     * @deprecated Use {@link #handleException(PluginDescriptor, Throwable, boolean)} instead.
     */
    @Deprecated
    public static void handlePluginException(PluginDescriptor pluginDesc, Throwable t, boolean print)
    {
        handleException(pluginDesc, t, print);
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
        final String icyId;
        final String javaId;
        final String osId;
        final String memory;
        final String pluginId;
        String pluginDepsId;
        final HashMap<String, String> values = new HashMap<String, String>();

        values.put(NetworkUtil.ID_KERNELVERSION, Icy.version.toString());
        values.put(NetworkUtil.ID_JAVANAME, SystemUtil.getJavaName());
        values.put(NetworkUtil.ID_JAVAVERSION, SystemUtil.getJavaVersion());
        values.put(NetworkUtil.ID_JAVABITS, Integer.toString(SystemUtil.getJavaArchDataModel()));
        values.put(NetworkUtil.ID_OSNAME, SystemUtil.getOSName());
        values.put(NetworkUtil.ID_OSVERSION, SystemUtil.getOSVersion());
        values.put(NetworkUtil.ID_OSARCH, SystemUtil.getOSArch());

        icyId = "Icy Version " + Icy.version + "\n";
        javaId = SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " ("
                + SystemUtil.getJavaArchDataModel() + " bit)\n";
        osId = "Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " (" + SystemUtil.getOSArch()
                + ")\n";
        memory = "Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()) + "\n";

        if (plugin != null)
        {
            values.put(NetworkUtil.ID_PLUGINCLASSNAME, plugin.getClassName());
            values.put(NetworkUtil.ID_PLUGINVERSION, plugin.getVersion().toString());
            pluginId = "Plugin " + plugin.toString() + "\n\n";

            if (plugin.getRequired().size() > 0)
            {
                pluginDepsId = "Dependances:\n";
                for (PluginIdent ident : plugin.getRequired())
                {
                    final PluginDescriptor installed = PluginLoader.getPlugin(ident.getClassName());

                    if (installed == null)
                        pluginDepsId += "Plugin " + ident.toString() + " is not installed\n";
                    else
                        pluginDepsId += "Plugin " + installed.toString() + " installed\n";
                }
                pluginDepsId += "\n";
            }
            else
                pluginDepsId = "";
        }
        else
        {
            values.put(NetworkUtil.ID_PLUGINCLASSNAME, "");
            values.put(NetworkUtil.ID_PLUGINVERSION, "");
            pluginId = "";
            pluginDepsId = "";
        }

        if (StringUtil.isEmpty(devId))
            values.put(NetworkUtil.ID_DEVELOPERID, devId);
        else
            values.put(NetworkUtil.ID_DEVELOPERID, "");

        values.put(NetworkUtil.ID_ERRORLOG, icyId + javaId + osId + memory + "\n" + pluginId + pluginDepsId + errorLog);

        // send report
        NetworkUtil.report(values);
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
