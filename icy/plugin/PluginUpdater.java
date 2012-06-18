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

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.network.NetworkUtil;
import icy.system.thread.SingleProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author stephane
 */
public class PluginUpdater extends ActionFrame
{
    private static class Checker implements Runnable
    {
        private boolean showProgress;
        private boolean auto;

        public Checker(boolean showProgress, boolean auto)
        {
            super();

            this.showProgress = showProgress;
            this.auto = auto;
        }

        @Override
        public void run()
        {
            processCheckUpdate(showProgress, auto);
        }
    }

    private final static int ANNOUNCE_SHOWTIME = 15;

    private static final SingleProcessor processor = new SingleProcessor(false);

    static
    {
        processor.setDefaultThreadName("Plugin updater");
    }

    JList pluginList;
    DefaultListModel listModel;

    /**
     * @param toInstallPlugins
     */
    PluginUpdater(final ArrayList<PluginDescriptor> toInstallPlugins)
    {
        super("Plugin Update", true);

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                setPreferredSize(new Dimension(640, 500));

                final JPanel titlePanel = GuiUtil.createCenteredBoldLabel("Select the plugin(s) to update in the list");
                titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                final JTextArea changeLogArea = new JTextArea();
                changeLogArea.setEditable(false);
                final JLabel changeLogTitleLabel = GuiUtil.createBoldLabel("Change log :");

                listModel = new DefaultListModel();
                pluginList = new JList(listModel);
                for (PluginDescriptor plugin : toInstallPlugins)
                    listModel.addElement(plugin);

                pluginList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                pluginList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (pluginList.getSelectedValue() != null)
                        {
                            final PluginDescriptor plugin = (PluginDescriptor) pluginList.getSelectedValue();

                            final String changeLog = plugin.getChangesLog();

                            if (StringUtil.isEmpty(changeLog))
                                changeLogArea.setText("no change log");
                            else
                                changeLogArea.setText(plugin.getChangesLog());
                            changeLogArea.setCaretPosition(0);
                            changeLogTitleLabel.setText(plugin.getName() + " change log");
                        }
                    }
                });
                pluginList.setSelectionInterval(0, toInstallPlugins.size() - 1);

                getOkBtn().setText("Update");
                getCancelBtn().setText("Close");
                setCloseAfterAction(false);
                setOkAction(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // launch update
                        PluginUpdater.this.doUpdate();
                    }
                });

                final JScrollPane medScrollPane = new JScrollPane(pluginList);
                final JScrollPane changeLogScrollPane = new JScrollPane(GuiUtil.createTabArea(changeLogArea, 4));
                final JPanel bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                        GuiUtil.createCenteredLabel(changeLogTitleLabel), Box.createVerticalStrut(4),
                        changeLogScrollPane);

                final JPanel mainPanel = getMainPanel();

                final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, medScrollPane, bottomPanel);

                mainPanel.add(titlePanel, BorderLayout.NORTH);
                mainPanel.add(splitPane, BorderLayout.CENTER);

                pack();
                addToMainDesktopPane();
                setVisible(true);
                center();
                requestFocus();

                // set splitter to middle
                splitPane.setDividerLocation(0.5d);
            }
        });
    }

    /**
     * update selected plugins
     */
    protected void doUpdate()
    {
        final ArrayList<PluginDescriptor> plugins = new ArrayList<PluginDescriptor>();

        for (Object value : pluginList.getSelectedValues())
            plugins.add((PluginDescriptor) value);

        // process plugins update
        if (!plugins.isEmpty())
            updatePlugins(plugins, true);

        for (PluginDescriptor plugin : plugins)
            listModel.removeElement(plugin);

        // no more plugin to update ? close frame
        if (listModel.isEmpty())
            close();
    }

    static void updatePlugins(ArrayList<PluginDescriptor> plugins, boolean showProgress)
    {
        final boolean b = PluginLoader.getLogError();

        PluginLoader.setLogError(false);
        try
        {
            // update plugins with ordered dependencies
            for (PluginDescriptor plugin : PluginInstaller.getDependenciesOrderedList(plugins))
                PluginInstaller.install(plugin, showProgress);
        }
        finally
        {
            PluginLoader.setLogError(b);
            PluginLoader.reload(false);
        }
    }

    /**
     * Do the check update process
     */
    public static void checkUpdate(boolean showProgress, boolean auto)
    {
        processor.addTask(new Checker(showProgress, auto));
    }

    /**
     * Get update for the specified plugin.
     * 
     * @param plugin
     *        local plugin we are looking update for
     * @return
     *         plugin descriptor of update if any (null if no update)
     */
    public static PluginDescriptor getUpdate(PluginDescriptor plugin)
    {
        // find equivalent online plugins
        final ArrayList<PluginDescriptor> onlinePlugins = PluginRepositoryLoader.getPlugins(plugin.getClassName());
        final PluginDescriptor onlinePlugin;

        // get the last version found
        if (onlinePlugins.size() > 0)
        {
            PluginDescriptor lastVersion = null;

            for (PluginDescriptor currentVersion : onlinePlugins)
                if ((lastVersion == null) || currentVersion.isNewer(lastVersion))
                    lastVersion = currentVersion;

            onlinePlugin = lastVersion;
        }
        else
            // not found in repositories
            onlinePlugin = null;

        // we have an update available ?
        if ((onlinePlugin != null) && onlinePlugin.getVersion().isGreater(plugin.getVersion()))
            return onlinePlugin;

        return null;
    }

    static void processCheckUpdate(boolean showProgress, boolean auto)
    {
        final ArrayList<PluginDescriptor> toInstallPlugins = new ArrayList<PluginDescriptor>();
        final ArrayList<PluginDescriptor> localPlugins = PluginLoader.getPlugins(false);
        final ProgressFrame checkingFrame;

        if (showProgress)
            checkingFrame = new CancelableProgressFrame("checking for plugins update...");
        else
            checkingFrame = null;
        try
        {
            // reload online plugins from all active repositories
            PluginRepositoryLoader.reload();
            // wait for basic infos
            PluginRepositoryLoader.waitBasicLoaded();

            if (PluginRepositoryLoader.failed())
            {
                if (showProgress)
                {
                    if (!NetworkUtil.hasInternetConnection())
                        new AnnounceFrame("You are not connected to internet.", 10);
                    else
                        new AnnounceFrame("Can't access the repositories... You should verify your connection.", 10);
                }

                return;
            }

            for (PluginDescriptor localPlugin : localPlugins)
            {
                // find update
                final PluginDescriptor onlinePlugin = getUpdate(localPlugin);

                // update found, add to the list
                if (onlinePlugin != null)
                    toInstallPlugins.add(onlinePlugin);
            }
        }
        finally
        {
            if (showProgress)
                checkingFrame.close();
        }

        // some updates availables ?
        if (!toInstallPlugins.isEmpty())
        {
            if (auto)
            {
                // automatically install all updates (orderer depending dependencies)
                updatePlugins(toInstallPlugins, showProgress);
            }
            else
            {
                // show announcement for 15 seconds
                new AnnounceFrame(toInstallPlugins.size() + " plugin update are availables", "View", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show pluginInstaller frame
                        new PluginUpdater(toInstallPlugins);
                    }
                }, ANNOUNCE_SHOWTIME);
            }
        }
        else
        {
            // inform that there is no plugin update available
            if (showProgress)
                new AnnounceFrame("No plugin udpate available", 10);
        }
    }
}
