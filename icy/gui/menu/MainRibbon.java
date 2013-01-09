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

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandMenuButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.gui.component.button.IcyCommandToggleMenuButton;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.TaskFrame;
import icy.gui.main.FocusedSequenceListener;
import icy.gui.main.MainFrame;
import icy.gui.menu.action.GeneralActions;
import icy.gui.menu.action.PreferencesActions;
import icy.gui.menu.action.WindowActions;
import icy.gui.menu.search.SearchBar;
import icy.gui.plugin.PluginCommandButton;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.RibbonUtil;
import icy.gui.viewer.Viewer;
import icy.imagej.ImageJWrapper;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginDescriptor.PluginClassNameSorter;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.preferences.GeneralPreferences;
import icy.preferences.WorkspaceLocalPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.system.thread.ThreadUtil;
import icy.workspace.Workspace;
import icy.workspace.Workspace.TaskDefinition;
import icy.workspace.Workspace.TaskDefinition.BandDefinition;
import icy.workspace.Workspace.TaskDefinition.BandDefinition.ItemDefinition;
import icy.workspace.WorkspaceInstaller;
import icy.workspace.WorkspaceLoader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies;

/**
 * This class is used to separate ribbon construction from the ribbon frame
 * 
 * @author Stephane
 */
public class MainRibbon implements PluginLoaderListener, FocusedSequenceListener
{
    /**
     * TASK / BAND NAMES
     */
    public static final String TASK_PLUGINS = "Plugins";
    public static final String BAND_SETUP = "Setup";
    public static final String BAND_NEW = "New";
    public static final String BAND_OTHERS = "Others";

    /**
     * loaded workspaces
     */
    private final ArrayList<Workspace> workspaces;
    private Workspace systemWorkspace;

    /**
     * Search bar object
     */
    SearchBar searchBar;

    /**
     * internals
     */
    BandDefinition setupPluginsBandDef;
    BandDefinition newPluginsBandDef;
    BandDefinition othersPluginsBandDef;

    private final JRibbon ribbon;
    private final ApplicationMenu applicationMenu;
    // private final JRibbonBand othersPluginsBand;
    private final JRibbonBand setupPluginsBand;
    private final JRibbonBand newPluginsBand;
    // private final ImageRibbonTask imageTask;
    private final SequenceOperationTask sequenceOperationTask;
    private final ToolRibbonTask toolRibbonTask;
    private final ImageJTask ijTask;
    final JMenu othersPluginsMenu;

    CommandToggleButtonGroup multiWindowGroup;
    IcyCommandToggleButton multiWindowButton;

    /**
     * @param ribbon
     */
    public MainRibbon(JRibbon ribbon)
    {
        super();

        this.ribbon = ribbon;

        workspaces = new ArrayList<Workspace>();
        othersPluginsMenu = new JMenu("Plugins");

        // APPLICATION MENU & MISC

        applicationMenu = new ApplicationMenu();
        ribbon.setApplicationMenu(applicationMenu);
        final RichTooltip toolTip = new RichTooltip("ICY Application menu", "Load, close and save Sequence from there.");
        ribbon.setApplicationMenuRichTooltip(toolTip);
        // ribbon.configureHelp(new ICYResizableIcon(new IcyIcon("lightbulb"),
        // new
        // Help("Main_Page"));

        // TASKBAR

        buidlTaskBar();

        // FIXED TASKS

        // load image task first as tools task need all plugins loaded...
        // imageTask = new ImageRibbonTask();
        sequenceOperationTask = new SequenceOperationTask();
        toolRibbonTask = new ToolRibbonTask();
        ijTask = new ImageJTask();
        // we want tools task to be the first task
        ribbon.addTask(toolRibbonTask);
        // ribbon.addTask(imageTask);
        ribbon.addTask(sequenceOperationTask);
        ribbon.addTask(ijTask);

        // WORKSPACES

        // load workspace from files
        loadWorkspaces();

        // store system band definition
        setupPluginsBandDef = systemWorkspace.findBand(TASK_PLUGINS, BAND_SETUP);
        newPluginsBandDef = systemWorkspace.findBand(TASK_PLUGINS, BAND_NEW);
        othersPluginsBandDef = systemWorkspace.findBand(TASK_PLUGINS, BAND_OTHERS);

        // build workspaces menu
        buildWorkspaces();

        // store system band
        final RibbonTask pluginTask = RibbonUtil.getTask(ribbon, TASK_PLUGINS);
        setupPluginsBand = RibbonUtil.getBand(pluginTask, BAND_SETUP);
        newPluginsBand = RibbonUtil.getBand(pluginTask, BAND_NEW);
        // othersPluginsBand = RibbonUtil.getBand(pluginTask, BAND_OTHERS);

        // build plugin setup band
        buildSetupPluginBand();

        // save workspaces back so removed stuff are cleaned
        // saveWorkspaces();

        PluginLoader.addListener(this);
        Icy.getMainInterface().addFocusedSequenceListener(this);
    }

    // some stuff which need to be initialized after ribbon creation
    public void init()
    {
        ijTask.init();

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        mainFrame.addPropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                multiWindowGroup.setSelected(multiWindowButton, Icy.getMainInterface().isDetachedMode());
            }
        });
    }

    public ToolRibbonTask getToolRibbon()
    {
        return toolRibbonTask;
    }

    public ImageJWrapper getImageJ()
    {
        return ijTask.getImageJ();
    }

    public SearchBar getSearchBar()
    {
        return searchBar;
    }

    private void loadWorkspaces()
    {
        ArrayList<String> workspacesName;

        // load names from preference
        workspacesName = WorkspaceLocalPreferences.getActivesWorkspace();

        // wait for workspace loader is ready
        WorkspaceLoader.waitWhileLoading();

        // load workspaces
        workspaces.clear();
        for (String name : workspacesName)
        {
            // get workspace from loader
            final Workspace ws = WorkspaceLoader.getWorkspace(name);

            // add to active workspace list if not empty
            if (ws == null)
                System.err.println("Workspace " + name + " not found !");
            else if (isInConflict(ws, true))
                System.err.println("Workspace '" + name + "' is discarded (conflict detected)");
            else
                workspaces.add(ws);
        }

        // clean up invalid entries
        workspacesName.clear();
        for (Workspace ws : workspaces)
            workspacesName.add(ws.getName());

        // save back cleaned names to preference
        WorkspaceLocalPreferences.setActivesWorkspace(workspacesName);

        // always add the system workspace
        systemWorkspace = new Workspace(Workspace.WORKSPACE_SYSTEM_NAME);

        // recreate system workspace manually if needed
        systemWorkspace.setDescription("System workspace");
        // this actually add only missing tasks and bands
        systemWorkspace.addBand(MainRibbon.TASK_PLUGINS, MainRibbon.BAND_SETUP);
        systemWorkspace.addBand(MainRibbon.TASK_PLUGINS, MainRibbon.BAND_NEW);
        systemWorkspace.addBand(MainRibbon.TASK_PLUGINS, MainRibbon.BAND_OTHERS);

        workspaces.add(systemWorkspace);
    }

    /**
     * return true if specified workspace is in conflict with current actives workspace
     */
    private boolean isInConflict(Workspace ws, boolean showAsError)
    {
        for (TaskDefinition task : ws.getTasks())
        {
            for (BandDefinition band : task.getBands())
            {
                // conflict : same task and same band in 2 different workspaces
                if (findBand(task.getName(), band.getName()) != null)
                {
                    if (showAsError)
                        System.err.println("Duplicated band : " + task.getName() + "/" + band.getName());

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * get all tasks from actives workspace
     */
    private ArrayList<TaskDefinition> getTasks()
    {
        final ArrayList<TaskDefinition> result = new ArrayList<TaskDefinition>();

        for (Workspace ws : workspaces)
            for (TaskDefinition task : ws.getTasks())
                if (!Workspace.contains(result, task))
                    result.add(task);

        return result;
    }

    /**
     * get all bands for a task from actives workspace
     */
    private ArrayList<BandDefinition> getBands(String taskName)
    {
        final ArrayList<BandDefinition> result = new ArrayList<BandDefinition>();

        for (Workspace ws : workspaces)
            for (TaskDefinition task : ws.getTasks())
                if (task.getName().equals(taskName))
                    for (BandDefinition band : task.getBands())
                        if (!Workspace.contains(result, band))
                            result.add(band);

        return result;
    }

    // private Workspace findWorkspace(String workspaceName)
    // {
    // for (Workspace ws : workspaces)
    // if (ws.getName().equals(workspaceName))
    // return ws;
    //
    // return null;
    // }

    private BandDefinition findBand(String taskName, String bandName)
    {
        for (Workspace ws : workspaces)
        {
            final TaskDefinition task = ws.findTask(taskName);
            if (task != null)
            {
                final BandDefinition band = task.findBand(bandName);
                if (band != null)
                    return band;
            }
        }

        return null;
    }

    /**
     * Get all items from all active workspace
     */
    private ArrayList<ItemDefinition> getAllItems()
    {
        final ArrayList<ItemDefinition> result = new ArrayList<ItemDefinition>();

        // get all items from active workspaces
        for (Workspace workspace : workspaces)
            result.addAll(workspace.getAllItems());

        return result;
    }

    ItemDefinition findItem(String className)
    {
        // search item in active workspaces
        for (Workspace workspace : workspaces)
        {
            final ItemDefinition item = workspace.findItem(className);
            if (item != null)
                return item;
        }

        return null;
    }

    void addItem(String className)
    {
        addItem(PluginLoader.getPlugin(className));
    }

    /**
     * Add an item for the specified plugin.
     */
    void addItem(PluginDescriptor plugin)
    {
        // check that plugin can be displayed in menu
        if ((plugin != null) && plugin.isActionable())
        {
            final IcyCommandButton pluginButton = PluginCommandButton.createButton(plugin);

            // add it to the new installed plugins workspace and save it
            newPluginsBandDef.addItem(plugin.getClassName(), RibbonElementPriority.TOP);
            systemWorkspace.save();
            // add it to the new installed plugins band
            newPluginsBand.addCommandButton(pluginButton, RibbonElementPriority.TOP);
        }
    }

    void updateItem(String className)
    {
        updateItem(PluginLoader.getPlugin(className), findItem(className));
    }

    void updateItem(PluginDescriptor plugin, ItemDefinition item)
    {
        // check that plugin can be displayed in menu
        if ((plugin != null) && plugin.isActionable())
        {
            if (item != null)
            {
                // find the corresponding button
                final AbstractCommandButton button = RibbonUtil.findButton(
                        RibbonUtil.getBand(RibbonUtil.getTask(ribbon, item.getTaskName()), item.getBandName()),
                        item.getClassName());

                // button found --> udpate it
                if (button != null)
                    PluginCommandButton.setButton(button, plugin);
            }
            else
            {
                final Workspace workspace = WorkspaceInstaller.getCurrentInstallingWorkspace();

                // check plugin wasn't installed from a workspace
                if ((workspace == null) || (workspace.findItem(plugin.getClassName()) == null))
                    // add a new item for this plugin
                    addItem(plugin);
            }
        }
    }

    void removeItem(String className)
    {
        removeItem(findItem(className));
    }

    void removeItem(ItemDefinition item)
    {
        if (item != null)
        {
            // FIXME : unsafe (not supposed to work)
            RibbonUtil.removeButton(
                    RibbonUtil.getBand(RibbonUtil.getTask(ribbon, item.getTaskName()), item.getBandName()),
                    item.getClassName());
            // remove item and save workspace
            item.remove();
            // save system workspace only, we want to preserve plugins organization
            systemWorkspace.save();
        }
    }

    private void buildSetupPluginBand()
    {
        setupPluginsBand.addCommandButton(new IcyCommandButton(PreferencesActions.onlinePluginPreferencesAction),
                RibbonElementPriority.TOP);

        RibbonUtil.setFixedResizePolicies(setupPluginsBand);
    }

    // /**
    // * Add a new installed plugin
    // */
    // public void addNewPlugin(String className)
    // {
    // // get the corresponding plugin
    // final PluginDescriptor plugin = PluginLoader.getPlugin(className);
    //
    // // check that menu can be displayed in menu
    // if ((plugin == null) || !plugin.isActionable())
    // return;
    //
    // final IcyCommandButton pluginButton = buildPluginCommandButton(plugin);
    //
    // // add it to the new installed plugins workspace and save it
    // newPluginsBandDef.addItem(plugin.getClassName(), RibbonElementPriority.TOP);
    // systemWorkspace.save();
    // // add it to the new installed plugins band
    // newPluginsBand.addCommandButton(pluginButton, RibbonElementPriority.TOP);
    // }

    private JRibbonBand[] createRibbonBands(TaskDefinition task)
    {
        final ArrayList<BandDefinition> bands = getBands(task.getName());
        final int size = bands.size();
        final JRibbonBand[] result = new JRibbonBand[size];

        for (int i = 0; i < size; i++)
        {
            final BandDefinition band = bands.get(i);
            // TODO : get icon from BandDefinition
            result[i] = new JRibbonBand(band.getName(), new IcyIcon(ResourceUtil.ICON_DOC));
            // use restrictive resize policy by default
            RibbonUtil.setRestrictiveResizePolicies(result[i]);
        }

        return result;
    }

    private RibbonTask createRibbonTask(TaskDefinition task)
    {
        final String name = task.getName();

        final RibbonTask ribbonTask = RibbonUtil.getTask(ribbon, name);
        if (ribbonTask != null)
        {
            System.out.println("Ribbon task " + name + " already exists...");
            return ribbonTask;
        }

        final RibbonTask result = new RibbonTask(name, createRibbonBands(task));

        // use roundRobin collapse policy
        result.setResizeSequencingPolicy(new CoreRibbonResizeSequencingPolicies.RoundRobin(result));

        return result;
    }

    /**
     * build ribbon from workspaces
     */
    private void buildWorkspaces()
    {
        final ArrayList<PluginDescriptor> plugins = PluginLoader.getPlugins(false);

        // get all TaskDefinition from all active workspace
        for (TaskDefinition task : getTasks())
        {
            // create the task with all needed bands
            final RibbonTask ribbonTask = createRibbonTask(task);

            for (BandDefinition band : getBands(task.getName()))
            {
                final JRibbonBand ribbonBand = RibbonUtil.getBand(ribbonTask, band.getName());

                if (ribbonBand != null)
                {
                    // special case of OTHER plugins
                    if (band == othersPluginsBandDef)
                    {
                        final IcyCommandButton btn = new IcyCommandButton("Other Plugins", new IcyIcon(
                                ResourceUtil.ICON_COG));
                        btn.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                        btn.setPopupRichTooltip(new RichTooltip("Other plugins",
                                "You can find here all plugins which are not associated to a workspace"));
                        btn.setPopupCallback(new PopupPanelCallback()
                        {
                            @Override
                            public JPopupPanel getPopupPanel(JCommandButton commandButton)
                            {
                                final JPopupMenu popupMenu = othersPluginsMenu.getPopupMenu();

                                // FIXME : set as heavy weight component for VTK (doesn't work)
                                // popupMenu.setLightWeightPopupEnabled(false);
                                popupMenu.show(btn, 0, btn.getHeight());

                                return null;
                            }
                        });
                        ribbonBand.addCommandButton(btn, RibbonElementPriority.TOP);

                        // refresh unassigned list
                        refreshOthersPluginsList();

                        // adjust restrictive resize policy
                        RibbonUtil.setFixedResizePolicies(ribbonBand);
                    }
                    else
                    {
                        for (ItemDefinition item : band.getItems())
                        {
                            // simple separator
                            if (item.isSeparator())
                                ribbonBand.startGroup();
                            else
                            {
                                final String className = item.getClassName();
                                final PluginDescriptor plugin = PluginDescriptor.getPlugin(plugins, className);

                                // plugin found ?
                                if (plugin != null)
                                {
                                    // check that menu can be displayed in menu
                                    if (plugin.isActionable())
                                        ribbonBand.addCommandButton(PluginCommandButton.createButton(plugin),
                                                item.getPriority());
                                }
                            }
                        }

                        // adjust restrictive resize policy
                        RibbonUtil.setRestrictiveResizePolicies(ribbonBand);
                    }
                }
            }

            // add task to ribbon only if not empty
            if (ribbonTask.getBandCount() > 0)
                ribbon.addTask(ribbonTask);
        }
    }

    /**
     * clean workspace (remove absent plug-in)
     */
    public void cleanWorkspaces()
    {
        final ArrayList<PluginDescriptor> plugins = PluginLoader.getPlugins(false);

        // get all items TaskDefinition from all active workspace
        for (ItemDefinition item : getAllItems())
        {
            // avoid separator
            if (!item.isSeparator())
            {
                final String className = item.getClassName();
                final PluginDescriptor plugin = PluginDescriptor.getPlugin(plugins, className);

                // plugin not found --> remove from workspace
                if (plugin == null)
                    item.remove();
            }
        }

        // save cleaned workspaces
        saveWorkspaces();
    }

    void refreshOthersPluginsList()
    {
        // build others plugin list
        final ArrayList<PluginDescriptor> othersPlugins = new ArrayList<PluginDescriptor>();

        // scan all actionable plugins to find unassigned ones
        for (PluginDescriptor plugin : PluginLoader.getActionablePlugins())
        {
            final String className = plugin.getClassName();
            // search item in actives workspace
            final ItemDefinition item = findItem(className);

            // plugin not defined in active workspaces --> add it to the list
            if ((item == null) || (item.getBandDefinition() == othersPluginsBandDef))
                othersPlugins.add(plugin);
        }

        // refresh unassigned plugins menu
        builOthersPluginsMenu(othersPlugins);

        // rebuild the unassigned workspace
        othersPluginsBandDef.clear();
        for (PluginDescriptor plugin : othersPlugins)
            othersPluginsBandDef.addItem(plugin.getClassName());

        // save the system workspace
        systemWorkspace.save();
    }

    private void builOthersPluginsMenu(ArrayList<PluginDescriptor> plugins)
    {
        othersPluginsMenu.removeAll();

        for (PluginDescriptor pluginDescriptor : plugins)
        {
            String pluginEntry = pluginDescriptor.getSimplePackageName();
            JMenu menuToPutPlugin = othersPluginsMenu;

            while (pluginEntry != null)
            {
                final int index = pluginEntry.indexOf(".");
                final String pluginDir;

                if (index != -1)
                {
                    pluginDir = pluginEntry.substring(0, index);
                    pluginEntry = pluginEntry.substring(index + 1);
                }
                else
                {
                    pluginDir = pluginEntry;
                    pluginEntry = null;
                }

                // look if name is already a menu.
                boolean menuExist = false;
                for (Component component : menuToPutPlugin.getMenuComponents())
                {
                    if (component instanceof JMenu)
                    {
                        final JMenu menu = (JMenu) component;

                        if (menu.getText().equals(pluginDir))
                        {
                            menuToPutPlugin = menu;
                            menuExist = true;
                            break;
                        }
                    }
                }

                if (menuExist == false)
                {
                    // create Menu
                    final JMenu menu = new JMenu(pluginDir);
                    menuToPutPlugin.add(menu);
                    menuToPutPlugin = menu;
                }
            }

            menuToPutPlugin.add(new PluginMenuItem(pluginDescriptor));
        }

        if (plugins.isEmpty())
            othersPluginsMenu.add(new JMenuItem("No plugins"));

        othersPluginsMenu.validate();
    }

    /**
     * save workspaces from menu
     */
    void saveWorkspaces()
    {
        // save all active workspaces
        for (Workspace workspace : workspaces)
            workspace.save();
    }

    /**
     * build task bar (little bar with small icons over all at top)
     */
    private void buidlTaskBar()
    {
        // PREFERENCES
        ribbon.addTaskbarComponent(new IcyCommandButton(PreferencesActions.preferencesAction));

        // PLUGINS
        ribbon.addTaskbarComponent(new IcyCommandButton(PreferencesActions.onlinePluginPreferencesAction));

        // SEPARATOR
        ribbon.addTaskbarComponent(new JSeparator(SwingConstants.VERTICAL));

        // MULTI FRAME MODE
        multiWindowGroup = new CommandToggleButtonGroup();
        multiWindowButton = new IcyCommandToggleButton(GeneralActions.detachedModeAction);
        ribbon.addTaskbarComponent(multiWindowButton);

        multiWindowGroup.add(multiWindowButton);
        multiWindowGroup.setSelected(multiWindowButton, GeneralPreferences.getMultiWindowMode());

        // WINDOWS
        final IcyCommandButton windowsButton = new IcyCommandButton(new IcyIcon("app_window"));

        windowsButton.setPopupRichTooltip(new RichTooltip("Windows",
                "Show specific windows and general windows setting..."));
        windowsButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
        windowsButton.setPopupCallback(new PopupPanelCallback()
        {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton)
            {
                final JCommandPopupMenu result = new JCommandPopupMenu();

                // ALWAYS ON TOP
                final CommandToggleButtonGroup aotGroup = new CommandToggleButtonGroup();
                final IcyCommandToggleMenuButton aotButton = new IcyCommandToggleMenuButton(
                        WindowActions.stayOnTopAction);
                result.addMenuButton(aotButton);

                aotGroup.add(aotButton);
                aotGroup.setSelected(aotButton, GeneralPreferences.getAlwaysOnTop());

                // SEPARATOR
                result.addMenuSeparator();

                // LOOK AND FEEL
                final IcyCommandMenuButton lafButton = new IcyCommandMenuButton("Appearance", new IcyIcon(
                        ResourceUtil.ICON_SMILEY_HAPPY));

                lafButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                lafButton.setPopupRichTooltip(new RichTooltip("Look and feel", "Change appearance of the interface"));
                lafButton.setPopupCallback(new PopupPanelCallback()
                {
                    @Override
                    public JPopupPanel getPopupPanel(JCommandButton commandButton)
                    {
                        // better to build it on request as it takes a bit of time
                        // and we want to speed up the initial loading
                        return LookAndFeelUtil.getLookAndFeelMenu();
                    }
                });
                result.addMenuButton(lafButton);

                // SEPARATOR
                result.addMenuSeparator();

                // SWIMMING POOL
                final IcyCommandMenuButton spButton = new IcyCommandMenuButton(WindowActions.swimmingPoolAction);
                result.addMenuButton(spButton);

                // SCRIPT EDITOR
                // TODO : reactivate when done
                // final IcyCommandMenuButton seButton = new IcyCommandMenuButton("Script Editor",
                // new
                // ICYResizableIcon(new IcyIcon(
                // "lighting"));
                // seButton.addActionListener(new ActionListener()
                // {
                // @Override
                // public void actionPerformed(ActionEvent e)
                // {
                // new ScriptEditor();
                // }
                // });
                // result.addMenuButton(seButton);

                // SEPARATOR
                result.addMenuSeparator();

                // REORGANIZE TILE
                final IcyCommandMenuButton tileButton = new IcyCommandMenuButton("Tile", new IcyIcon("2x2_grid"));
                tileButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                tileButton.setPopupCallback(new PopupPanelCallback()
                {
                    @Override
                    public JPopupPanel getPopupPanel(JCommandButton commandButton)
                    {
                        final JCommandPopupMenu result = new JCommandPopupMenu();

                        // grid
                        result.addMenuButton(new IcyCommandMenuButton(WindowActions.gridTileAction));
                        // horizontal
                        result.addMenuButton(new IcyCommandMenuButton(WindowActions.horizontalTileAction));
                        // vertical
                        result.addMenuButton(new IcyCommandMenuButton(WindowActions.verticalTileAction));

                        return result;
                    }
                });
                result.addMenuButton(tileButton);

                // REORGANIZE CASCADE
                result.addMenuButton(new IcyCommandMenuButton(WindowActions.cascadeAction));

                // SEPARATOR
                result.addMenuSeparator();

                // OPENED SEQUENCES
                final ArrayList<Viewer> allViewers = Icy.getMainInterface().getViewers();
                final IcyCommandMenuButton sequencesButton = new IcyCommandMenuButton("Opened sequences", new IcyIcon(
                        ResourceUtil.ICON_PICTURE));
                sequencesButton.setPopupRichTooltip(new RichTooltip("Opened Sequences", "Show the selected sequence"));
                sequencesButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                sequencesButton.setPopupCallback(new PopupPanelCallback()
                {
                    @Override
                    public JPopupPanel getPopupPanel(JCommandButton commandButton)
                    {
                        final JCommandPopupMenu result = new JCommandPopupMenu();

                        // SEQUENCES
                        for (Viewer viewer : allViewers)
                        {
                            final Viewer v = viewer;

                            final IcyCommandMenuButton seqButton = new IcyCommandMenuButton(viewer.getTitle());
                            seqButton.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    ThreadUtil.invokeLater(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // remove minimized state
                                            if (v.isMinimized())
                                                v.setMinimized(false);
                                            // then grab focus
                                            v.requestFocus();
                                            v.toFront();
                                        }
                                    }, true);
                                }
                            });
                            result.addMenuButton(seqButton);
                        }

                        return result;
                    }
                });
                sequencesButton.setEnabled(allViewers.size() > 0);
                result.addMenuButton(sequencesButton);

                // OPENED FRAMES
                final ArrayList<IcyFrame> allFrames = IcyFrame.getAllFrames();
                final IcyCommandMenuButton framesButton = new IcyCommandMenuButton("Opened frames", new IcyIcon(
                        ResourceUtil.ICON_WINDOW));
                framesButton.setPopupRichTooltip(new RichTooltip("Opened frames", "Show all frames"));
                framesButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                framesButton.setPopupCallback(new PopupPanelCallback()
                {
                    @Override
                    public JPopupPanel getPopupPanel(JCommandButton commandButton)
                    {
                        final JCommandPopupMenu result = new JCommandPopupMenu();

                        // FRAMES
                        for (IcyFrame frame : allFrames)
                        {
                            if ((frame instanceof Viewer) || (frame instanceof TaskFrame) || !frame.isVisible())
                                continue;

                            final IcyFrame f = frame;

                            final IcyCommandMenuButton frameButton = new IcyCommandMenuButton(frame.getTitle());
                            frameButton.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    ThreadUtil.invokeLater(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // remove minimized state
                                            if (f.isMinimized())
                                                f.setMinimized(false);
                                            // then grab focus
                                            f.requestFocus();
                                            f.toFront();
                                        }
                                    }, true);
                                }
                            });
                            result.addMenuButton(frameButton);
                        }

                        return result;
                    }
                });

                // check if we have visible frame
                boolean hasVisibleFrame = false;
                for (IcyFrame frame : allFrames)
                    if (!((frame instanceof Viewer) || (frame instanceof TaskFrame) || !frame.isVisible()))
                        hasVisibleFrame = true;

                framesButton.setEnabled(hasVisibleFrame);
                result.addMenuButton(framesButton);

                return result;
            }
        });
        ribbon.addTaskbarComponent(windowsButton);

        // SEPARATOR
        ribbon.addTaskbarComponent(new JSeparator(SwingConstants.VERTICAL));

        // SEARCH BAR
        searchBar = new SearchBar();
        searchBar.setColumns(14);

        ribbon.addTaskbarComponent(searchBar);

        // HELP / INFOS
        final IcyCommandButton helpAndInfoButton = new IcyCommandButton(new IcyIcon("info"));

        helpAndInfoButton.setPopupRichTooltip(new RichTooltip("General help and information",
                "Help, Updates and Informations about Icy."));
        helpAndInfoButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
        helpAndInfoButton.setPopupCallback(new PopupPanelCallback()
        {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton)
            {
                final JCommandPopupMenu result = new JCommandPopupMenu();

                // HELP
                result.addMenuButton(new IcyCommandMenuButton(GeneralActions.onlineHelpAction));
                // WEB SITE
                result.addMenuButton(new IcyCommandMenuButton(GeneralActions.websiteAction));
                // // FAQ
                // final IcyCommandMenuButton faqButton = new Help("faq")
                // .getIcyCommandMenuButton("Help (online)");
                // result.addMenuButton(faqButton);
                // CHECK FOR UPDATE
                result.addMenuButton(new IcyCommandMenuButton(GeneralActions.checkUpdateAction));
                // ABOUT
                result.addMenuButton(new IcyCommandMenuButton(GeneralActions.aboutAction));

                return result;
            }
        });
        ribbon.addTaskbarComponent(helpAndInfoButton);
    }

    private void checkPluginsMenuCoherence()
    {
        // get plugins we have to display in menu
        final ArrayList<PluginDescriptor> plugins = PluginLoader.getActionablePlugins();
        final ArrayList<ItemDefinition> items = getAllItems();
        final PluginClassNameSorter pluginsSorter = PluginClassNameSorter.instance;

        // sort plugins on classname
        Collections.sort(plugins, pluginsSorter);

        final PluginDescriptor keyPlugin = new PluginDescriptor();

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // find removed plugins
                for (ItemDefinition item : items)
                {
                    if (!item.isSeparator() && (item.getBandDefinition() != othersPluginsBandDef))
                    {
                        // set the className for searched element
                        keyPlugin.getIdent().setClassName(item.getClassName());

                        // search it in plugins list
                        final int index = Collections.binarySearch(plugins, keyPlugin, pluginsSorter);
                        final boolean found;

                        // detect if element was found
                        if ((index >= 0) && (index < plugins.size()))
                            found = pluginsSorter.compare(plugins.get(index), keyPlugin) == 0;
                        else
                            found = false;

                        // not found --> remove item
                        if (!found)
                            removeItem(item);
                    }
                }

                // update or add plugin button
                for (PluginDescriptor plugin : plugins)
                    updateItem(plugin, findItem(plugin.getClassName()));

                refreshOthersPluginsList();
            }
        });
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        // update menu according to plugins change
        checkPluginsMenuCoherence();
    }

    @Override
    public void focusChanged(Sequence sequence)
    {
        sequenceOperationTask.onSequenceChange();
        applicationMenu.onSequenceFocusChange();
    }

    @Override
    public void focusedSequenceChanged(SequenceEvent event)
    {
        final SequenceEventSourceType type = event.getSourceType();

        if ((type == SequenceEventSourceType.SEQUENCE_DATA) || (type == SequenceEventSourceType.SEQUENCE_TYPE))
            sequenceOperationTask.onSequenceChange();
    }
}
