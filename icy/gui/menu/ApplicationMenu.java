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

import icy.action.FileActions;
import icy.action.GeneralActions;
import icy.action.PreferencesActions;
import icy.file.FileUtil;
import icy.file.Importer;
import icy.file.Loader;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.menu.IcyRibbonApplicationMenuEntryPrimary;
import icy.gui.component.menu.IcyRibbonApplicationMenuEntrySecondary;
import icy.gui.plugin.PluginApplicationMenuEntrySecondary;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.preferences.GeneralPreferences;
import icy.preferences.IcyPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceImporter;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
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
public class ApplicationMenu extends RibbonApplicationMenu implements PluginLoaderListener
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

                    final String[] paths = recentFileList.getEntry(i);
                    // final File[] files = recentFileList.getEntryAsFiles(i);

                    final RichTooltip toolTip;
                    final int numFile = paths.length;

                    if (numFile == 1)
                        toolTip = new RichTooltip("Single file sequence", FileUtil.getFileName(paths[0]));
                    else
                        toolTip = new RichTooltip("Multiple files sequence", FileUtil.getFileName(paths[0]));

                    for (int j = 1; j < Math.min(10, numFile); j++)
                        toolTip.addDescriptionSection(FileUtil.getFileName(paths[j]));
                    if (numFile > 10)
                    {
                        toolTip.addDescriptionSection("...");
                        toolTip.addDescriptionSection(FileUtil.getFileName(paths[numFile - 1]));
                    }

                    button.setActionRichTooltip(toolTip);

                    button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            Loader.load(CollectionUtil.asList(paths), false, true, true);
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
    private final RibbonApplicationMenuEntryPrimary amepImport;
    private final RibbonApplicationMenuEntryPrimary amepSaveDefault;
    private final RibbonApplicationMenuEntrySecondary amepSave;
    private final RibbonApplicationMenuEntrySecondary amepSaveAs;
    private final RibbonApplicationMenuEntrySecondary amepSaveMetaData;
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

        amepNew.addSecondaryMenuGroup("New image", amesNewGraySequence, amesNewRGBSequence, amesNewRGBASequence);

        // OPEN & IMPORT

        amepOpen = new IcyRibbonApplicationMenuEntryPrimary(FileActions.openSequenceAction);
        amepOpen.setRolloverCallback(new OpenRecentFilePrimaryRollOverCallBack());

        amepImport = new IcyRibbonApplicationMenuEntryPrimary(new IcyIcon(ResourceUtil.ICON_DOC_IMPORT), "Import",
                null, CommandButtonKind.POPUP_ONLY);
        amepImport.addSecondaryMenuGroup("Import a sequence", getImportEntries());

        // SAVE & EXPORT

        amepSaveDefault = new IcyRibbonApplicationMenuEntryPrimary(FileActions.saveDefaultSequenceAction);

        amepSave = new IcyRibbonApplicationMenuEntrySecondary(FileActions.saveSequenceAction);
        amepSaveAs = new IcyRibbonApplicationMenuEntrySecondary(FileActions.saveAsSequenceAction);
        amepSaveMetaData = new IcyRibbonApplicationMenuEntrySecondary(FileActions.saveMetaDataAction);

        amepSaveDefault.addSecondaryMenuGroup("Save", amepSave, amepSaveAs, amepSaveMetaData);

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
        addMenuEntry(amepSaveDefault);

        addMenuSeparator();
        addMenuEntry(amepImport);

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

        PluginLoader.addListener(this);
    }

    private RibbonApplicationMenuEntrySecondary[] getImportEntries()
    {
        final List<PluginDescriptor> importers = PluginLoader.getPlugins(Importer.class);
        final List<PluginDescriptor> fileImporters = PluginLoader.getPlugins(SequenceImporter.class);
        final List<RibbonApplicationMenuEntrySecondary> result = new ArrayList<RibbonApplicationMenuEntrySecondary>();

        for (int i = 0; i < importers.size(); i++)
        {
            final PluginDescriptor plugin = importers.get(i);

            result.add(new PluginApplicationMenuEntrySecondary(plugin, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // asynchronous loading
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                final Importer importer = (Importer) PluginLauncher.start(plugin);

                                // plugin correctly started ? --> do load operation
                                if (importer != null)
                                    importer.load();
                            }
                            catch (Exception exc)
                            {
                                IcyExceptionHandler.handleException(exc, false);
                            }
                        }
                    });
                }
            }));
        }
        for (int i = 0; i < fileImporters.size(); i++)
        {
            final PluginDescriptor plugin = fileImporters.get(i);

            result.add(new PluginApplicationMenuEntrySecondary(plugin, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // asynchronous loading
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                final SequenceImporter importer = (SequenceImporter) PluginLauncher.start(plugin);

                                // plugin correctly started ? --> do load operation
                                if (importer != null)
                                    importer.load();
                            }
                            catch (Exception exc)
                            {
                                IcyExceptionHandler.handleException(exc, false);
                            }
                        }
                    });
                }
            }));
        }

        // TODO: add sort here from plugin importer preferences

        return result.toArray(new RibbonApplicationMenuEntrySecondary[result.size()]);
    }

    private void refreshState()
    {
        final Sequence focusedSequence = Icy.getMainInterface().getActiveSequence();

        amepImport.setEnabled(getImportEntries().length > 0);
        amepSaveDefault.setEnabled(focusedSequence != null);
        amepSave.setEnabled((focusedSequence != null) && !StringUtil.isEmpty(focusedSequence.getFilename()));
        amepSaveAs.setEnabled(focusedSequence != null);
        amepSaveMetaData.setEnabled((focusedSequence != null) && !StringUtil.isEmpty(focusedSequence.getFilename())
                && GeneralPreferences.getSequencePersistence());
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
        addRecentLoadedFile(files.toArray(new File[files.size()]));
    }

    public void addRecentLoadedFile(File[] files)
    {
        recentFileList.addEntry(files);
    }

    public void addRecentLoadedFile(File file)
    {
        addRecentLoadedFile(new File[] {file});
    }

    /**
     * Add a list of recently opened files (String format)
     */
    public void addRecentFile(List<String> paths)
    {
        addRecentFile(paths.toArray(new String[paths.size()]));
    }

    public void addRecentFile(String[] paths)
    {
        recentFileList.addEntry(paths);
    }

    public void addRecentFile(String path)
    {
        addRecentFile(new String[] {path});
    }

    public void onSequenceActivationChange()
    {
        refreshState();
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        refreshState();
    }
}
