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
package icy.gui.plugin;

import icy.gui.frame.error.ErrorReportFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginUpdater;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.text.BadLocationException;

/**
 * This class create a report from a plugin crash and ask the
 * user if he wants to send it to the dev team of the plugin.
 * 
 * @author Fabrice de Chaumont & Stephane<br>
 */
public class PluginErrorReport
{
    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param title
     *        Error title if any
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String title,
            final String message)
    {
        // directly report in headless mode
        if (Icy.getMainInterface().isHeadLess())
        {
            IcyExceptionHandler.report(plugin, devId, message);
            return;
        }

        // cannot be reported...
        // if ((plugin == null) && StringUtil.isEmpty(devId))
        // return;

        if (ErrorReportFrame.hasErrorFrameOpened())
            return;

        // always do that in background process
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                if (plugin != null)
                {
                    final CancelableProgressFrame info = new CancelableProgressFrame("Plugin '" + plugin.getName()
                            + "' has crashed, searching for update...");

                    // wait for online basic info loaded
                    PluginRepositoryLoader.waitBasicLoaded();

                    PluginDescriptor onlinePlugin = null;

                    try
                    {
                        // search for update
                        if (!info.isCancelRequested())
                            onlinePlugin = PluginUpdater.getUpdate(plugin);
                    }
                    finally
                    {
                        info.close();
                    }

                    // update found and not canceled
                    if (!info.isCancelRequested() && (onlinePlugin != null))
                    {
                        PluginInstaller.install(onlinePlugin, false);
                        new AnnounceFrame(
                                "The plugin crashed but a new version has been found, try it again when installation is done",
                                10);
                        // don't need to report
                        return;
                    }

                    // display report as no update were found
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            doReport(plugin, null, title, message);
                        }
                    });
                }
                else
                {
                    // directly display report frame
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            doReport(null, devId, title, message);
                        }
                    });
                }
            }
        });
    }

    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String message)
    {
        report(plugin, devId, null, message);
    }

    /**
     * Report an error thrown by the specified plugin.
     * 
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param message
     *        Error message to report
     */
    public static void report(PluginDescriptor plugin, String message)
    {
        report(plugin, null, null, message);
    }

    // internal use only
    static void doReport(final PluginDescriptor plugin, final String devId, String title, String message)
    {
        String str;
        Icon icon;

        // build title
        if (plugin != null)
        {
            str = "<html><br>The plugin named <b>" + plugin.getName() + "</b> has encountered a problem";
            icon = plugin.getIcon();
        }
        else if (!StringUtil.isEmpty(devId))
        {
            str = "<html><br>The plugin from the developer <b>" + devId + "</b> has encountered a problem";
            icon = null;
        }
        else
        {
            str = "<html><br>The application has encountered a problem";
            icon = null;
        }

        if (StringUtil.isEmpty(title))
            str += ".<br><br>";
        else
            str += " :<br><i>" + title + "</i><br><br>";

        str += "Reporting this problem is anonymous and will help improving this plugin.<br><br></html>";

        // headless mode --> report directly
        if (Icy.getMainInterface().isHeadLess())
            IcyExceptionHandler.report(plugin, devId, message);
        else
        {
            final ErrorReportFrame frame = new ErrorReportFrame(icon, str, message);

            // set specific report action here
            frame.setReportAction(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

                    try
                    {
                        IcyExceptionHandler.report(plugin, devId, frame.getReportMessage());
                    }
                    catch (BadLocationException ex)
                    {
                        System.err.println("Error while reporting error :");
                        IcyExceptionHandler.showErrorMessage(ex, true);
                    }
                    finally
                    {
                        progressFrame.close();
                    }
                }
            });
        }
    }
}
