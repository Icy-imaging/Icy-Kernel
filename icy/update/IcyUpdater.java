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
package icy.update;

import icy.file.FileUtil;
import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.CancelableProgressFrame;
import icy.gui.frame.progress.DownloadFrame;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.network.URLUtil;
import icy.preferences.ApplicationPreferences;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.update.ElementDescriptor.ElementFile;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
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
public class IcyUpdater
{

    private final static int ANNOUNCE_SHOWTIME = 15;

    public final static String PARAM_ARCH = "arch";
    public final static String PARAM_VERSION = "version";

    // internals
    static boolean wantUpdate = false;
    private static boolean silent;
    private static boolean updating = false;
    private static boolean checking = false;
    private static ActionFrame frame = null;
    private static Runnable checker = new Runnable()
    {
        @Override
        public void run()
        {
            processCheckUpdate();
        }
    };

    public static boolean getWantUpdate()
    {
        return wantUpdate;
    }

    /**
     * return true if we are currently checking for update
     */
    public static boolean isCheckingForUpdate()
    {
        return checking;
    }

    /**
     * return true if we are currently processing update
     */
    public static boolean isUpdating()
    {
        return isCheckingForUpdate() || ((frame != null) && frame.isVisible()) || updating;
    }

    /**
     * Do the check update process
     */
    public static void checkUpdate(boolean silent)
    {
        if (!isUpdating())
        {
            IcyUpdater.silent = silent;
            ThreadUtil.bgRunSingle(checker);
        }
    }

    /**
     * @deprecated Use {@link #checkUpdate(boolean)} instead
     */
    @Deprecated
    public static void checkUpdate(boolean showProgress, boolean auto)
    {
        checkUpdate(!showProgress || auto);
    }

    /**
     * Check for application update process (synchronized method)
     */
    public static synchronized void processCheckUpdate()
    {
        checking = true;
        try
        {
            wantUpdate = false;

            // delete update directory to avoid partial update
            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);

            final ArrayList<ElementDescriptor> toUpdate;
            final ProgressFrame checkingFrame;

            if (!silent && !Icy.getMainInterface().isHeadLess())
                checkingFrame = new CancelableProgressFrame("checking for application update...");
            else
                checkingFrame = null;

            final String params = PARAM_ARCH + "=" + SystemUtil.getOSArchIdString() + "&" + PARAM_VERSION + "="
                    + Icy.version;

            try
            {
                // error (or cancel) while downloading XML ?
                if (!downloadAndSaveForUpdate(
                        ApplicationPreferences.getUpdateRepositoryBase()
                                + ApplicationPreferences.getUpdateRepositoryFile() + "?" + params, Updater.UPDATE_NAME,
                        checkingFrame, !silent))
                {
                    // remove partially downloaded files
                    FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
                    return;
                }

                // check if some elements need to be updated from network
                toUpdate = Updater.getUpdateElements(Updater.getLocalElements());
            }
            finally
            {
                if (checkingFrame != null)
                    checkingFrame.close();
            }

            final boolean needUpdate;

            // empty ? --> no update
            if (toUpdate.isEmpty())
                needUpdate = false;
            // only the updater require updates ? --> no update
            else if ((toUpdate.size() == 1) && (toUpdate.get(0).getName().equals(Updater.ICYUPDATER_NAME)))
                needUpdate = false;
            // otherwise --> update
            else
                needUpdate = true;

            // some elements need to be updated ?
            if (needUpdate)
            {
                // silent update or headless mode
                if (silent || Icy.getMainInterface().isHeadLess())
                {
                    // automatically install updates
                    if (prepareUpdate(toUpdate, true))
                        // we want update when application will exit
                        wantUpdate = true;
                }
                else
                {
                    final String mess;

                    if (toUpdate.size() > 1)
                        mess = "Some updates are available...";
                    else
                        mess = "An update is available...";

                    // show announcement for 15 seconds
                    new AnnounceFrame(mess, "View", new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // display updates and process them if user accept
                            showUpdateAndProcess(toUpdate);
                        }
                    }, ANNOUNCE_SHOWTIME);
                }
            }
            else
            {
                // cleanup
                FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
                // inform that there is no update available
                if (!silent && !Icy.getMainInterface().isHeadLess())
                    new AnnounceFrame("No application update available", 10);
            }
        }
        finally
        {
            checking = false;
        }
    }

    static void showUpdateAndProcess(final ArrayList<ElementDescriptor> elements)
    {
        if (frame != null)
        {
            synchronized (frame)
            {
                if (frame.isVisible())
                    return;
                frame.getMainPanel().removeAll();
            }
        }
        else
            frame = new ActionFrame("Application update", true);

        frame.setPreferredSize(new Dimension(640, 500));

        frame.getOkBtn().setText("Install");
        frame.setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // download required files
                        if (prepareUpdate(elements, true))
                        {
                            // ask to update and restart application now
                            wantUpdate = true;
                            Icy.confirmRestart();
                        }
                        else
                            new FailedAnnounceFrame(
                                    "An error occured while downloading files (see details in console)", 10000);
                    }
                });
            }
        });

        final JPanel topPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                GuiUtil.createCenteredBoldLabel("The following(s) element(s) will be updated"),
                Box.createVerticalStrut(4));

        final JTextArea changeLogArea = new JTextArea();
        changeLogArea.setEditable(false);
        final JLabel changeLogTitleLabel = GuiUtil.createBoldLabel("Change log :");

        final JList list = new JList(elements.toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (list.getSelectedValue() != null)
                {
                    final ElementDescriptor element = (ElementDescriptor) list.getSelectedValue();

                    final String changeLog = element.getChangelog();

                    if (StringUtil.isEmpty(changeLog))
                        changeLogArea.setText("no change log");
                    else
                        changeLogArea.setText(element.getChangelog());
                    changeLogArea.setCaretPosition(0);
                    changeLogTitleLabel.setText(element.getName() + " change log");
                }
            }
        });
        list.setSelectedIndex(0);

        final JScrollPane medScrollPane = new JScrollPane(list);
        final JScrollPane changeLogScrollPane = new JScrollPane(GuiUtil.createTabArea(changeLogArea, 4));
        final JPanel bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                GuiUtil.createCenteredLabel(changeLogTitleLabel), Box.createVerticalStrut(4), changeLogScrollPane);

        final JPanel mainPanel = frame.getMainPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, medScrollPane, bottomPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        frame.pack();
        frame.addToDesktopPane();
        frame.setVisible(true);
        frame.center();
        frame.requestFocus();

        // set splitter to middle
        splitPane.setDividerLocation(0.5d);
    }

    static boolean prepareUpdate(List<ElementDescriptor> elements, boolean showProgress)
    {
        final DownloadFrame downloadingFrame;

        updating = true;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            downloadingFrame = new DownloadFrame("");
        else
            downloadingFrame = null;
        try
        {
            // get total number of files to process
            int numFile = 0;
            for (ElementDescriptor element : elements)
                numFile += element.getFiles().size();

            if (downloadingFrame != null)
                downloadingFrame.setLength(numFile);

            int curFile = 0;
            for (ElementDescriptor element : elements)
            {
                for (ElementFile elementFile : element.getFiles())
                {
                    curFile++;

                    if (downloadingFrame != null)
                    {
                        // update progress frame message and position
                        downloadingFrame.setMessage("Downloading updates " + curFile + " / " + numFile);

                        final String toolTip = "Downloading " + element.getName() + " : "
                                + FileUtil.getFileName(elementFile.getLocalPath());
                        // update progress frame tooltip
                        downloadingFrame.setToolTipText(toolTip);
                    }

                    // symbolic link file ?
                    if (elementFile.isLink())
                    {
                        // special treatment
                        if (!FileUtil.createLink(
                                Updater.UPDATE_DIRECTORY + FileUtil.separator + elementFile.getLocalPath(),
                                elementFile.getOnlinePath()))
                        {
                            // remove partially downloaded files
                            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
                            return false;
                        }
                    }
                    else
                    {
                        // local file need to be updated --> download new file
                        if (Updater.needUpdate(elementFile.getLocalPath(), elementFile.getDateModif()))
                        {
                            // error (or cancel) while downloading ?
                            if (!downloadAndSaveForUpdate(URLUtil.getNetworkURLString(
                                    ApplicationPreferences.getUpdateRepositoryBase(), elementFile.getOnlinePath()),
                                    elementFile.getLocalPath(), downloadingFrame, showProgress))
                            {
                                // remove partially downloaded files
                                FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            if (downloadingFrame != null)
                downloadingFrame.close();
            updating = false;
        }

        return true;
    }

    public static boolean downloadAndSaveForUpdate(String downloadPath, String savePath, ProgressFrame frame,
            boolean displayError)
    {
        // get data
        final byte[] data;

        data = NetworkUtil.download(downloadPath, frame, displayError);
        if (data == null)
            return false;

        // build save filename
        String saveFilename = Updater.UPDATE_DIRECTORY + FileUtil.separator;

        if (StringUtil.isEmpty(savePath))
            saveFilename += URLUtil.getURLFileName(downloadPath, true);
        else
            saveFilename += savePath;

        if (!FileUtil.save(saveFilename, data, displayError))
            return false;

        return true;
    }

    /**
     * Return true if required files for updates are present
     */
    private static boolean canDoUpdate()
    {
        // check for updater presence
        boolean requiredFilesExist = FileUtil.exists(Updater.UPDATER_NAME);
        // // in update directory ?
        // requiredFilesExist |= FileUtil.exists(Updater.UPDATE_DIRECTORY + FileUtil.separator +
        // Updater.UPDATER_NAME);
        // check for update xml file
        requiredFilesExist &= FileUtil.exists(Updater.UPDATE_DIRECTORY + FileUtil.separator + Updater.UPDATE_NAME);

        // required files present so we can do update
        return requiredFilesExist;
    }

    /**
     * Launch the updater with the specified update and restart parameters
     */
    public static boolean launchUpdater(boolean doUpdate, boolean restart)
    {
        if (doUpdate)
        {
            final String updateName = Updater.UPDATE_DIRECTORY + FileUtil.separator + Updater.UPDATER_NAME;

            // updater need update ? process it first
            if (FileUtil.exists(updateName))
            {
                // replace updater
                if (!FileUtil.rename(updateName, Updater.UPDATER_NAME, true))
                {
                    System.err.println("Can't update 'Upater.jar', Update process can't continue.");
                    return false;
                }
            }

            // this is not really needed...
            if (!canDoUpdate())
            {
                System.err.println("Can't process update : some required files are missing.");
                return false;
            }
        }

        String params = "";

        if (doUpdate)
            params += Updater.ARG_UPDATE + " ";
        if (!restart)
            params += Updater.ARG_NOSTART + " ";

        // launch updater
        SystemUtil.execJAR(Updater.UPDATER_NAME, params);

        // you have to exit application then...
        return true;
    }

}