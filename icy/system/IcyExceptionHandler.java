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
package icy.system;

import icy.gui.dialog.MessageDialog;
import icy.gui.plugin.PluginErrorReport;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

/**
 * @author Stephane
 */
public class IcyExceptionHandler implements UncaughtExceptionHandler
{
    public static final String ID_KERNELVERSION = "kernelVersion";
    public static final String ID_PLUGINCLASSNAME = "pluginClassName";
    public static final String ID_PLUGINVERSION = "pluginVersion";
    public static final String ID_DEVELOPERID = "developerId";
    public static final String ID_ERRORLOG = "errorLog";

    private static IcyExceptionHandler exceptionHandler = new IcyExceptionHandler();

    public static void init()
    {
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

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

    public static void showErrorMessage(Throwable t, boolean printStackTrace)
    {
        showErrorMessage(t, printStackTrace, true);
    }

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
                if (!(throwable instanceof IcyHandledException))
                    IcyExceptionHandler.showErrorMessage(throwable, true);

                if (throwable instanceof OutOfMemoryError)
                {
                    // handle out of memory error differently
                    MessageDialog
                            .showDialog(
                                    "Out of memory error !",
                                    "You're running out of memory !\nTry to increase the Maximum Memory parameter in Preferences.",
                                    MessageDialog.ERROR_MESSAGE);
                }
                else
                {
                    boolean done = false;

                    // search plugin class (start from the end of stack trace)
                    for (StackTraceElement trace : throwable.getStackTrace())
                    {
                        final String className = trace.getClassName();

                        // plugin class ?
                        if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
                        {
                            final PluginDescriptor plugin = PluginLoader.getPlugin(ClassUtil
                                    .getBaseClassName(className));

                            // plugin found --> show the plugin report frame
                            if (plugin != null)
                            {
                                handleException(plugin, throwable, false);
                                // only send to last plugin raising the exception
                                done = true;
                                break;
                            }
                        }
                    }

                    if (!done)
                    {
                        // we did not find plugin class so we will search for plugin developer id
                        for (StackTraceElement trace : throwable.getStackTrace())
                        {
                            final String className = trace.getClassName();

                            // plugin class ?
                            if (className.startsWith(PluginLoader.PLUGIN_PACKAGE + "."))
                            {
                                // use plugin developer id
                                handleException(className.split("\\.")[1], throwable, false);
                                // only send to last plugin raising the exception
                                done = true;
                                break;
                            }
                        }
                    }

                    if (!done)
                    {
                        // general exception (no plugin information found)
                        handleException(throwable, false);
                    }
                }
            }
        });
    }

    /**
     * Handle the specified exception.<br>
     * It actually display a message or report dialog depending the exception type.
     */
    private static void handleException(PluginDescriptor plugin, String devId, Throwable t, boolean print)
    {
        final boolean handledException = t instanceof IcyHandledException;

        if (t instanceof OutOfMemoryError)
        {
            // handle out of memory error differently
            MessageDialog.showDialog("Out of memory error !",
                    "You're running out of memory !\nTry to increase the Maximum Memory parameter in Preferences.",
                    MessageDialog.ERROR_MESSAGE);
        }
        else if (handledException)
        {
            // handle HandledException differently
            MessageDialog.showDialog(t.getMessage() + ((t.getCause() == null) ? "" : "\n" + t.getCause()),
                    MessageDialog.ERROR_MESSAGE);
        }
        else
        {
            // write message in console if wanted
            if (print)
            {
                if (plugin != null)
                    System.err.println("An error occured while plugin '" + plugin.getName() + "' was running :");
                else if (!StringUtil.isEmpty(devId))
                    System.err.println("An error occured while a plugin was running :");
                IcyExceptionHandler.showErrorMessage(t, true);
            }

            // do report only for plugin error
            if ((plugin != null) || !StringUtil.isEmpty(devId))
            {
                // report error
                PluginErrorReport.report(
                        plugin,
                        devId,
                        "An error occured while the plugin was running.\n\nStack trace :\n"
                                + IcyExceptionHandler.getErrorMessage(t, true));
            }
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
     * It actually display a message or report dialog depending the exception type.
     */
    public static void handleException(Throwable t, boolean print)
    {
        handleException(null, null, t, print);
    }

    /**
     * @deprecated Uses {@link #handleException(PluginDescriptor, Throwable, boolean)} instead.
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
        final String pluginId;
        final HashMap<String, String> values = new HashMap<String, String>();

        values.put(ID_KERNELVERSION, Icy.version.toString());
        icyId = "ICY Version " + Icy.version + "\n";

        if (plugin != null)
        {
            values.put(ID_PLUGINCLASSNAME, plugin.getClassName());
            values.put(ID_PLUGINVERSION, plugin.getVersion().toString());
            pluginId = "Plugin " + plugin.toString() + "\n";
        }
        else
        {
            values.put(ID_PLUGINCLASSNAME, "");
            values.put(ID_PLUGINVERSION, "");
            pluginId = "";
        }

        if (StringUtil.isEmpty(devId))
            values.put(ID_DEVELOPERID, devId);
        else
            values.put(ID_DEVELOPERID, "");

        values.put(ID_ERRORLOG, icyId + pluginId + "\n" + errorLog);

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
