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
package icy.gui.menu;

import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.Saver;
import icy.gui.component.ComponentUtil;
import icy.gui.dialog.ImageLoaderDialog;
import icy.gui.dialog.ImageSaverDialog;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.preferences.IcyPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.CollectionUtil;
import icy.type.collection.list.RecentFileList;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel.LayoutKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

/**
 * @author Stephane
 */
public class ApplicationMenu extends RibbonApplicationMenu
{
    private static final int RECENTFILE_MAXLEN = 100;

    /**
     * Secondary panel management for "Open Recent File"
     */
    class OpenRecentFilePrimaryRollOverCallBack implements RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback
    {
        private String getToolTipFileName(String path)
        {
            return FileUtil.getFileName(path);
        }

        @Override
        public void menuEntryActivated(JPanel targetPanel)
        {
            final JCommandButtonPanel recentFilesPanel = new JCommandButtonPanel(CommandButtonDisplayState.MEDIUM);

            // set to 1 column maximum
            recentFilesPanel.setMaxButtonColumns(1);
            recentFilesPanel.setLayoutKind(LayoutKind.ROW_FILL);

            // recent files group
            recentFilesPanel.addButtonGroup("Recent Files");

            // remove obsolete entries
            recentFileList.clean();

            final int nbRecentFile = recentFileList.getSize();
            final IcyIcon icon = new IcyIcon("document");

            for (int i = 0; i < nbRecentFile; i++)
            {
                final String entry = recentFileList.getEntryAsName(i, RECENTFILE_MAXLEN, true);

                if (!StringUtil.isEmpty(entry))
                {
                    final JCommandButton button = new JCommandButton(entry, icon);
                    // final JCommandButton button = new JCommandButton(entry, new TestIcon());

                    button.setHorizontalAlignment(SwingConstants.LEFT);

                    final ArrayList<File> files = recentFileList.getEntryAsFiles(i);

                    final RichTooltip toolTip;
                    final int numFile = files.size();

                    if (numFile == 1)
                        toolTip = new RichTooltip("Single file sequence", getToolTipFileName(files.get(0).getPath()));
                    else
                        toolTip = new RichTooltip("Multiple files sequence", getToolTipFileName(files.get(0).getPath()));

                    for (int j = 1; j < Math.min(10, numFile); j++)
                        toolTip.addDescriptionSection(getToolTipFileName(files.get(j).getPath()));
                    if (numFile > 10)
                    {
                        toolTip.addDescriptionSection("...");
                        toolTip.addDescriptionSection(getToolTipFileName(files.get(numFile - 1).getPath()));
                    }

                    button.setActionRichTooltip(toolTip);

                    button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            Loader.load(files);
                        }
                    });

                    // add button to history panel
                    recentFilesPanel.addButtonToLastGroup(button);
                }
            }

            // action group
            recentFilesPanel.addButtonGroup("Action");

            final JCommandButton clearButton = new JCommandButton("Clear recent files");

            clearButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    recentFileList.clear();
                }
            });

            recentFilesPanel.addButtonToLastGroup(clearButton);

            targetPanel.removeAll();
            ComponentUtil.setPreferredWidth(targetPanel, 480);
            targetPanel.setLayout(new BorderLayout());
            targetPanel.add(recentFilesPanel, BorderLayout.CENTER);
            targetPanel.validate();
        }
    }

    /**
     * Default roll callback
     */
    class DefaultRollOverCallBack implements RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback
    {
        @Override
        public void menuEntryActivated(JPanel targetPanel)
        {
            ComponentUtil.setPreferredWidth(targetPanel, 480);

            // ribbon need at least one component in targetPanel
            targetPanel.removeAll();
            targetPanel.add(new JPanel());
            targetPanel.validate();
        }
    }

    final RecentFileList recentFileList;

    private final RibbonApplicationMenuEntryPrimary amepNew;
    private final RibbonApplicationMenuEntrySecondary amesNewGraySequence;
    private final RibbonApplicationMenuEntrySecondary amesNewRGBSequence;
    private final RibbonApplicationMenuEntrySecondary amesNewRGBASequence;
    private final RibbonApplicationMenuEntryPrimary amepOpen;
    private final RibbonApplicationMenuEntryPrimary amepSave;
    private final RibbonApplicationMenuEntryPrimary amepSaveAs;
    private final RibbonApplicationMenuEntryPrimary amepClose;
    private final RibbonApplicationMenuEntrySecondary amesCloseCurrent;
    private final RibbonApplicationMenuEntrySecondary amesCloseOthers;
    private final RibbonApplicationMenuEntrySecondary amesCloseAll;
    private final RibbonApplicationMenuEntryPrimary amepExit;

    final IcyIcon iconNewDoc;
    final IcyIcon iconDoc;
    final IcyIcon iconClose;
    final IcyIcon iconSave;
    final IcyIcon iconExit;

    /**
     * 
     */
    public ApplicationMenu()
    {
        super();

        iconNewDoc = new IcyIcon(ResourceUtil.ICON_NEWDOC);
        iconDoc = new IcyIcon(ResourceUtil.ICON_DOC);
        iconClose = new IcyIcon(ResourceUtil.ICON_CLOSE);
        iconSave = new IcyIcon(ResourceUtil.ICON_SAVE);
        iconExit = new IcyIcon(ResourceUtil.ICON_EXIT);

        recentFileList = new RecentFileList(IcyPreferences.applicationRoot().node("loader"));

        // NEW FILE

        amepNew = new RibbonApplicationMenuEntryPrimary(iconNewDoc, "New", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Icy.addSequence(new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1,
                        DataType.UBYTE)));
            }
        }, CommandButtonKind.ACTION_ONLY);

        amesNewGraySequence = new RibbonApplicationMenuEntrySecondary(iconNewDoc,
                "Create a new 1 gray level channel sequence", new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Icy.addSequence(new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1,
                                DataType.UBYTE)));
                    }
                }, CommandButtonKind.ACTION_ONLY);
        amesNewGraySequence.setDescriptionText("Create a 1 gray level channel sequence.");

        amesNewRGBSequence = new RibbonApplicationMenuEntrySecondary(iconNewDoc, "Create a new RGB color sequence",
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Icy.addSequence(new Sequence("RGB sequence", new IcyBufferedImage(512, 512, 3, DataType.UBYTE)));
                    }
                }, CommandButtonKind.ACTION_ONLY);
        amesNewRGBSequence.setDescriptionText("Create a 3 channels sequence (red, green, blue).");

        amesNewRGBASequence = new RibbonApplicationMenuEntrySecondary(iconNewDoc, "Create a new RGBA color sequence",
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Icy.addSequence(new Sequence("RGBA sequence", new IcyBufferedImage(512, 512, 4, DataType.UBYTE)));
                    }
                }, CommandButtonKind.ACTION_ONLY);
        amesNewRGBASequence.setDescriptionText("Create a 4 channels sequence (red, green, blue, alpha).");

        amepNew.addSecondaryMenuGroup("New Sequence", amesNewGraySequence, amesNewRGBSequence, amesNewRGBASequence);

        // OPEN & IMPORT

        amepOpen = new RibbonApplicationMenuEntryPrimary(iconDoc, "Open", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // new OpenFileDialog();
                new ImageLoaderDialog();
            }
        }, CommandButtonKind.ACTION_ONLY);
        // amepOpen.setRolloverCallback(new OpenFilePrimaryRollOverCallBack());
        amepOpen.setRolloverCallback(new OpenRecentFilePrimaryRollOverCallBack());

        // final RibbonApplicationMenuEntryPrimary amepOpenRecent = new
        // RibbonApplicationMenuEntryPrimary(
        // new ICYResizableIcon.Icy("document.png"), "Open Recent", null,
        // CommandButtonKind.ACTION_ONLY);
        // amepOpenRecent.setRolloverCallback(new OpenRecentFilePrimaryRollOverCallBack());

        // final RibbonApplicationMenuEntryPrimary amEntryImport = new
        // RibbonApplicationMenuEntryPrimary(
        // new ICYResizableIcon.Icy("doc_import.png"), "Import", new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // System.out.println("import action...");
        // }
        // }, CommandButtonKind.ACTION_ONLY);
        // amEntryImport.setRolloverCallback(new DefaultRollOverCallBack());

        // SAVE & EXPORT

        amepSave = new RibbonApplicationMenuEntryPrimary(iconSave, "Save", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                final Sequence seq = viewer.getSequence();

                if (seq != null)
                {
                    final String filename = seq.getFilename();

                    if (StringUtil.isEmpty(filename))
                        new ImageSaverDialog(seq, viewer.getZ(), viewer.getT());
                    else
                        Saver.save(seq, new File(filename));
                }
            }
        }, CommandButtonKind.ACTION_ONLY);
        amepSave.setRolloverCallback(new DefaultRollOverCallBack());

        amepSaveAs = new RibbonApplicationMenuEntryPrimary(iconSave, "Save as...  ", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Viewer viewer = Icy.getMainInterface().getFocusedViewer();
                final Sequence seq = viewer.getSequence();

                new ImageSaverDialog(seq, viewer.getZ(), viewer.getT());
            }
        }, CommandButtonKind.ACTION_ONLY);
        amepSaveAs.setRolloverCallback(new DefaultRollOverCallBack());

        // final RibbonApplicationMenuEntryPrimary amepExport = new
        // RibbonApplicationMenuEntryPrimary(
        // new ICYResizableIcon.Icy("doc_export.png"), "Export", new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // System.out.println("export action...");
        // }
        // }, CommandButtonKind.ACTION_ONLY);
        // amepExport.setRolloverCallback(new DefaultRollOverCallBack());

        // CLOSE

        amepClose = new RibbonApplicationMenuEntryPrimary(iconClose, "Close", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                if (seq != null)
                {
                    for (Viewer viewer : Icy.getMainInterface().getViewers(seq))
                        viewer.close();
                }
            }
        }, CommandButtonKind.ACTION_ONLY);

        amesCloseCurrent = new RibbonApplicationMenuEntrySecondary(iconClose, "Close sequence", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Viewer viewer = Icy.getMainInterface().getFocusedViewer();

                if (viewer != null)
                    viewer.close();
            }
        }, CommandButtonKind.ACTION_ONLY);
        amesCloseCurrent.setDescriptionText("Close last selected sequence.");

        amesCloseOthers = new RibbonApplicationMenuEntrySecondary(iconClose, "Close others", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Viewer focusedViewer = Icy.getMainInterface().getFocusedViewer();

                for (Viewer viewer : Icy.getMainInterface().getViewers())
                    if (viewer != focusedViewer)
                        viewer.close();

            }
        }, CommandButtonKind.ACTION_ONLY);
        amesCloseOthers.setDescriptionText("Close all opened sequences except the last selected one.");

        amesCloseAll = new RibbonApplicationMenuEntrySecondary(iconClose, "Close all sequences", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // closing all viewers will release all sequences
                Icy.getMainInterface().closeAllViewers();
            }
        }, CommandButtonKind.ACTION_ONLY);
        amesCloseAll.setDescriptionText("Close all opened sequences.");

        amepClose.addSecondaryMenuGroup("Close Sequence", amesCloseCurrent, amesCloseOthers, amesCloseAll);

        // EXIT

        amepExit = new RibbonApplicationMenuEntryPrimary(iconExit, "Exit", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Icy.exit(false);
            }
        }, CommandButtonKind.ACTION_ONLY);
        amepExit.setRolloverCallback(new DefaultRollOverCallBack());

        // build menu

        addMenuEntry(amepNew);
        addMenuEntry(amepOpen);
        // addMenuEntry(amepOpenRecent);
        // addMenuEntry(amEntryImport);

        addMenuSeparator();

        addMenuEntry(amepSave);
        addMenuEntry(amepSaveAs);
        // addMenuEntry(amepExport);

        addMenuSeparator();

        addMenuEntry(amepClose);

        addMenuSeparator();

        addMenuEntry(amepExit);

        setDefaultCallback(new OpenRecentFilePrimaryRollOverCallBack());
        // setDefaultCallback(new DefaultRollOverCallBack());

        refreshState();
    }

    private void refreshState()
    {
        final boolean enabled = Icy.getMainInterface().getSequences().size() > 0;

        amepSave.setEnabled(enabled);
        amepSaveAs.setEnabled(enabled);
        amepClose.setEnabled(enabled);
    }

    /**
     * @return the recentFileList
     */
    public RecentFileList getRecentFileList()
    {
        return recentFileList;
    }

    public void addRecentLoadedFile(List<File> files)
    {
        recentFileList.addEntry(files);
    }

    public void addRecentLoadedFile(File file)
    {
        addRecentLoadedFile(CollectionUtil.createArrayList(file));
    }

    public void onSequenceFocusChange()
    {
        refreshState();
    }
}
