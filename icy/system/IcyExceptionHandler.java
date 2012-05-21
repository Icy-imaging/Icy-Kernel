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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

/**
 * @author Stephane
 */
public class IcyExceptionHandler implements UncaughtExceptionHandler
{
    public static final String ID_PLUGINCLASSNAME = "pluginClassName";
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
        final Thread thread = t;
        final Throwable throwable = e;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
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
                                handlePluginException(plugin, throwable, false);
                                // only send to last plugin raising the exception
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public static void handlePluginException(PluginDescriptor pluginDesc, Throwable t, boolean print)
    {
        if (print)
        {
            // show message
            System.err.println("An error occured while plugin '" + pluginDesc.getName() + "' was running :");
            IcyExceptionHandler.showErrorMessage(t, true);
        }

        if (t instanceof OutOfMemoryError)
        {
            // handle out of memory error differently
            MessageDialog.showDialog("Out of memory error !",
                    "You're running out of memory !\nTry to increase the Maximum Memory parameter in Preferences.",
                    MessageDialog.ERROR_MESSAGE);
        }
        else
        {
            // report error
            PluginErrorReport.report(pluginDesc, "An error occured while the plugin was running.\n\nStack trace :\n"
                    + IcyExceptionHandler.getErrorMessage(t, true));
        }
    }

    /**
     * Report an error log to the Icy web site.
     * 
     * @param plugin
     *        The plugin responsible of the error or <code>null</code> if the error comes from the application.
     * @param errorLog
     *        Error log to report.
     */
    public static void report(PluginDescriptor plugin, String errorLog)
    {
        final HashMap<String, String> values = new HashMap<String, String>();

        if (plugin != null)
            values.put(ID_PLUGINCLASSNAME, plugin.getClassName());
        else
            values.put(ID_PLUGINCLASSNAME, "");

        final String icyId;
        final String pluginId;

        icyId = "ICY Version " + Icy.version + "\n";
        if (plugin != null)
            pluginId = "Plugin " + plugin.toString() + "\n";
        else
            pluginId = "";

        values.put(ID_ERRORLOG, icyId + pluginId + "\n" + errorLog);

        // send report
        NetworkUtil.report(values);
    }
}
