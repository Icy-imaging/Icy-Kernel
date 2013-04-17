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
package icy.gui.menu;

import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.menu.IcyRibbonApplicationMenuEntryPrimary;
import icy.gui.component.menu.IcyRibbonApplicationMenuEntrySecondary;
import icy.gui.menu.action.FileActions;
import icy.gui.menu.action.GeneralActions;
import icy.gui.menu.action.PreferencesActions;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.preferences.IcyPreferences;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
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
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
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
        @Override
        public void menuEntryActivated(JPanel targetPanel)
        {
            ComponentUtil.setPreferredWidth(targetPanel, 480);

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

                    button.setHorizontalAlignment(SwingConstants.LEFT);

                    final ArrayList<File> files = recentFileList.getEntryAsFiles(i);

                    final RichTooltip toolTip;
                    final int numFile = files.size();

                    if (numFile == 1)
                        toolTip = new RichTooltip("Single file sequence", FileUtil.getFileName(files.get(0).getPath()));
                    else
                        toolTip = new RichTooltip("Multiple files sequence", FileUtil.getFileName(files.get(0)
                                .getPath()));

                    for (int j = 1; j < Math.min(10, numFile); j++)
                        toolTip.addDescriptionSection(FileUtil.getFileName(files.get(j).getPath()));
                    if (numFile > 10)
                    {
                        toolTip.addDescriptionSection("...");
                        toolTip.addDescriptionSection(FileUtil.getFileName(files.get(numFile - 1).getPath()));
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

            final IcyCommandButton clearButton = new IcyCommandButton(FileActions.clearRecentFilesAction);
            recentFilesPanel.addButtonToLastGroup(clearButton);

            targetPanel.removeAll();
            targetPanel.setLayout(new BorderLayout());
            targetPanel.add(new JScrollPane(recentFilesPanel), BorderLayout.CENTER);
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
    private final RibbonApplicationMenuEntryPrimary amepPreferences;
    private final RibbonApplicationMenuEntryPrimary amepExit;

    /**
     * 
     */
    public ApplicationMenu()
    {
        super();

        recentFileList = new RecentFileList(IcyPreferences.applicationRoot().node("loader"));

        // NEW FILE

        amepNew = new IcyRibbonApplicationMenuEntryPrimary(FileActions.newSequenceAction);

        amesNewGraySequence = new IcyRibbonApplicationMenuEntrySecondary(FileActions.newGraySequenceAction);
        amesNewRGBSequence = new IcyRibbonApplicationMenuEntrySecondary(FileActions.newRGBSequenceAction);
        amesNewRGBASequence = new IcyRibbonApplicationMenuEntrySecondary(FileActions.newARGBSequenceAction);

        amepNew.addSecondaryMenuGroup("New sequence", amesNewGraySequence, amesNewRGBSequence, amesNewRGBASequence);

        // OPEN & IMPORT

        amepOpen = new IcyRibbonApplicationMenuEntryPrimary(FileActions.openSequenceAction);
        amepOpen.setRolloverCallback(new OpenRecentFilePrimaryRollOverCallBack());

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

        amepSave = new IcyRibbonApplicationMenuEntryPrimary(FileActions.saveSequenceAction);
        amepSave.setRolloverCallback(new DefaultRollOverCallBack());

        amepSaveAs = new IcyRibbonApplicationMenuEntryPrimary(FileActions.saveAsSequenceAction);
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

        amepClose = new IcyRibbonApplicationMenuEntryPrimary(FileActions.closeSequenceAction);

        amesCloseCurrent = new IcyRibbonApplicationMenuEntrySecondary(FileActions.closeCurrentSequenceAction);
        amesCloseOthers = new IcyRibbonApplicationMenuEntrySecondary(FileActions.closeOthersSequencesAction);
        amesCloseAll = new IcyRibbonApplicationMenuEntrySecondary(FileActions.closeAllSequencesAction);

        amepClose.addSecondaryMenuGroup("Close Sequence", amesCloseCurrent, amesCloseOthers, amesCloseAll);

        // PREFERENCES

        amepPreferences = new IcyRibbonApplicationMenuEntryPrimary(PreferencesActions.preferencesAction);

        // EXIT

        amepExit = new IcyRibbonApplicationMenuEntryPrimary(GeneralActions.exitApplicationAction);
        amepExit.setRolloverCallback(new DefaultRollOverCallBack());

        // build menu

        addMenuEntry(amepNew);
        addMenuEntry(amepOpen);
        addMenuEntry(amepSave);
        addMenuEntry(amepSaveAs);

        // addMenuEntry(amEntryImport);

        // addMenuSeparator();

        // addMenuEntry(amepExport);

        addMenuSeparator();

        addMenuEntry(amepClose);

        addMenuSeparator();

        addMenuEntry(amepPreferences);

        addMenuSeparator();

        addMenuEntry(amepExit);

        setDefaultCallback(new OpenRecentFilePrimaryRollOverCallBack());
        // setDefaultCallback(new DefaultRollOverCallBack());

        refreshState();
    }

    private void refreshState()
    {
        final Sequence focusedSequence = Icy.getMainInterface().getFocusedSequence();

        amepSave.setEnabled((focusedSequence != null) && !StringUtil.isEmpty(focusedSequence.getFilename()));
        amepSaveAs.setEnabled(focusedSequence != null);
        amepClose.setEnabled(Icy.getMainInterface().getSequences().size() > 0);
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
