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
package icy.workspace;

import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.file.xml.XMLPersistentHelper;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.RibbonUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginLoader;
import icy.plugin.PluginRepositoryLoader;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.workspace.Workspace.TaskDefinition.BandDefinition;
import icy.workspace.Workspace.TaskDefinition.BandDefinition.ItemDefinition;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.internal.ui.ribbon.JBandControlPanel.ControlPanelGroup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class Workspace implements XMLPersistent, Comparable<Workspace>
{
    // public static class ItemDefinitionClassNameSorter implements
    // Comparator<ItemDefinition>
    // {
    // @Override
    // public int compare(ItemDefinition o1, ItemDefinition o2)
    // {
    // return o1.getClassName().compareToIgnoreCase(o2.getClassName());
    // }
    // }

    public static boolean contains(ArrayList<TaskDefinition> tasks, TaskDefinition task)
    {
        for (TaskDefinition t : tasks)
            if (task.getName().equals(t.getName()))
                return true;

        return false;
    }

    public static boolean contains(ArrayList<BandDefinition> bands, BandDefinition band)
    {
        for (BandDefinition b : bands)
            if (band.getName().equals(b.getName()))
                return true;

        return false;
    }

    public static Workspace getWorkspace(ArrayList<Workspace> list, String name)
    {
        for (Workspace workspace : list)
            if (workspace.getName().equals(name))
                return workspace;

        return null;
    }

    public static boolean addWorkspace(ArrayList<Workspace> list, Workspace workspace)
    {
        if (!list.contains(workspace))
        {
            list.add(workspace);
            return true;
        }

        return false;
    }

    public static boolean removeWorkspace(ArrayList<Workspace> list, String name)
    {
        return list.remove(getWorkspace(list, name));
    }

    public class TaskDefinition implements XMLPersistent
    {
        private static final String ID_NAME = "name";

        public class BandDefinition implements XMLPersistent
        {
            public class ItemDefinition implements XMLPersistent
            {
                private static final String ID_CLASSNAME = "classname";
                private static final String ID_PRIORITY = "priority";

                String className;
                RibbonElementPriority priority;

                ItemDefinition()
                {
                    super();

                    className = "";
                    priority = RibbonElementPriority.MEDIUM;
                }

                ItemDefinition(Node node)
                {
                    this();

                    loadFromXML(node);
                }

                ItemDefinition(JRibbonBand band, AbstractCommandButton button)
                {
                    this();

                    // class name is saved here
                    className = button.getName();
                    priority = RibbonUtil.getButtonPriority(band, button);
                }

                ItemDefinition(String className, RibbonElementPriority prio)
                {
                    this();

                    this.className = className;
                    priority = prio;
                }

                ItemDefinition(String className)
                {
                    this(className, RibbonElementPriority.LOW);
                }

                private RibbonElementPriority stringToPrio(String value)
                {
                    if (StringUtil.isEmpty(value))
                        return RibbonElementPriority.MEDIUM;

                    final String prio = value.toLowerCase();

                    if (prio.equals("low"))
                        return RibbonElementPriority.LOW;
                    if (prio.equals("top"))
                        return RibbonElementPriority.TOP;

                    return RibbonElementPriority.MEDIUM;
                }

                private String prioToString(RibbonElementPriority value)
                {
                    switch (value)
                    {
                        case LOW:
                            return "low";
                        case MEDIUM:
                        default:
                            return "med";
                        case TOP:
                            return "top";
                    }
                }

                @Override
                public boolean loadFromXML(Node node)
                {
                    if (node == null)
                        return false;

                    final String nodeName = node.getNodeName();

                    if (nodeName.equals(ID_SEPARATOR))
                        className = ID_SEPARATOR;
                    else if (nodeName.equals(ID_ITEM))
                    {
                        final Element element = (Element) node;

                        className = XMLUtil.getAttributeValue(element, ID_CLASSNAME, "");
                        priority = stringToPrio(XMLUtil.getAttributeValue(element, ID_PRIORITY, ""));
                    }
                    else
                        return false;

                    return true;
                }

                @Override
                public boolean saveToXML(Node node)
                {
                    if (node == null)
                        return false;

                    final String nodeName = node.getNodeName();

                    if (nodeName.equals(ID_ITEM))
                    {
                        final Element element = (Element) node;

                        XMLUtil.setAttributeValue(element, ID_CLASSNAME, className);
                        XMLUtil.setAttributeValue(element, ID_PRIORITY, prioToString(priority));
                    }
                    else if (!nodeName.equals(ID_SEPARATOR))
                        return false;

                    return true;
                }

                /**
                 * @return the className
                 */
                public String getClassName()
                {
                    if (isSeparator())
                        return "";

                    return className;
                }

                /**
                 * @param className
                 *        the className to set
                 */
                public void setClassName(String className)
                {
                    this.className = className;
                }

                /**
                 * return true if this item is a separator
                 */
                public boolean isSeparator()
                {
                    return StringUtil.equals(className, ID_SEPARATOR);
                }

                /**
                 * return true if this item is empty
                 */
                public boolean isEmpty()
                {
                    return StringUtil.isEmpty(className);
                }

                /**
                 * @return the priority
                 */
                public RibbonElementPriority getPriority()
                {
                    return priority;
                }

                /**
                 * set the priority
                 */
                public void setPriority(RibbonElementPriority value)
                {
                    priority = value;
                }

                public BandDefinition getBandDefinition()
                {
                    return BandDefinition.this;
                }

                public String getBandName()
                {
                    return BandDefinition.this.getName();
                }

                public TaskDefinition getTaskDefinition()
                {
                    return TaskDefinition.this;
                }

                public String getTaskName()
                {
                    return TaskDefinition.this.getName();
                }

                public Workspace getWorkspace()
                {
                    return Workspace.this;
                }

                public String getWorkspaceName()
                {
                    return Workspace.this.getName();
                }

                public boolean remove()
                {
                    return BandDefinition.this.removeItem(this);
                }
            }

            private static final String ID_ITEM = "item";
            private static final String ID_SEPARATOR = "separator";

            String name;
            final ArrayList<ItemDefinition> items;

            BandDefinition()
            {
                super();

                name = "";
                items = new ArrayList<ItemDefinition>();
            }

            BandDefinition(Node node)
            {
                this();

                loadFromXML(node);
            }

            BandDefinition(JRibbonBand ribbonBand)
            {
                this();

                loadFrom(ribbonBand);
            }

            /**
             * add ItemDefinition from AbstractCommandButton component
             * 
             * @param band
             * @param button
             */
            public ItemDefinition addItem(JRibbonBand band, AbstractCommandButton button)
            {
                final ItemDefinition result = new ItemDefinition(band, button);
                items.add(result);
                return result;
            }

            public ItemDefinition addItem(String className, RibbonElementPriority prio)
            {
                final ItemDefinition result = new ItemDefinition(className, prio);
                items.add(result);
                return result;
            }

            public ItemDefinition addItem(String className)
            {
                final ItemDefinition result = new ItemDefinition(className);
                items.add(result);
                return result;
            }

            public void addSeparator()
            {
                addItem(ID_SEPARATOR);
            }

            /**
             * remove an item from the band
             * 
             * @param item
             */
            public boolean removeItem(ItemDefinition item)
            {
                if (item != null)
                    return items.remove(item);
                return false;
            }

            /**
             * remove an item from the band
             * 
             * @param className
             */
            public boolean removeItem(String className)
            {
                return removeItem(findItem(className));
            }

            /**
             * remove all items from the band
             */
            public void clear()
            {
                items.clear();
            }

            public TaskDefinition getTaskDefinition()
            {
                return TaskDefinition.this;
            }

            public String getTaskName()
            {
                return TaskDefinition.this.getName();
            }

            public Workspace getWorkspace()
            {
                return Workspace.this;
            }

            public String getWorkspaceName()
            {
                return Workspace.this.getName();
            }

            public boolean loadFrom(JRibbonBand ribbonBand)
            {
                if (ribbonBand == null)
                    return false;

                name = ribbonBand.getTitle();

                // clear before loading
                items.clear();

                for (ControlPanelGroup panelGroup : ribbonBand.getControlPanel().getControlPanelGroups())
                {
                    addSeparator();

                    for (AbstractCommandButton button : panelGroup.getRibbonButtons(RibbonElementPriority.LOW))
                        addItem(ribbonBand, button);

                    for (AbstractCommandButton button : panelGroup.getRibbonButtons(RibbonElementPriority.MEDIUM))
                        addItem(ribbonBand, button);

                    for (AbstractCommandButton button : panelGroup.getRibbonButtons(RibbonElementPriority.TOP))
                        addItem(ribbonBand, button);
                }

                return true;
            }

            @Override
            public boolean loadFromXML(Node node)
            {
                if (node == null)
                    return false;

                name = XMLUtil.getAttributeValue((Element) node, ID_NAME, "");

                // clear before loading
                items.clear();

                final ArrayList<Node> nodesItem = XMLUtil.getSubNodes(node);
                for (Node n : nodesItem)
                {
                    final ItemDefinition item = new ItemDefinition(n);

                    // only add if not empty
                    if (!item.isEmpty())
                        items.add(item);
                }

                return true;
            }

            @Override
            public boolean saveToXML(Node node)
            {
                if (node == null)
                    return false;

                XMLUtil.setAttributeValue((Element) node, ID_NAME, name);

                XMLUtil.removeAllChilds(node);
                for (ItemDefinition item : items)
                {
                    if (item.isSeparator())
                        item.saveToXML(XMLUtil.addElement(node, ID_SEPARATOR));
                    else
                        item.saveToXML(XMLUtil.addElement(node, ID_ITEM));
                }

                return true;
            }

            public ItemDefinition findItem(String className)
            {
                for (ItemDefinition item : items)
                    if (StringUtil.equals(item.getClassName(), className))
                        return item;

                return null;
            }

            /**
             * @return the name
             */
            public String getName()
            {
                return name;
            }

            /**
             * @return the items
             */
            public ArrayList<ItemDefinition> getItems()
            {
                return new ArrayList<ItemDefinition>(items);
            }
        }

        private static final String ID_BAND = "band";

        String name;
        final ArrayList<BandDefinition> bands;

        TaskDefinition()
        {
            super();

            name = "";
            bands = new ArrayList<BandDefinition>();
        }

        TaskDefinition(Node node)
        {
            this();

            loadFromXML(node);
        }

        TaskDefinition(RibbonTask ribbonTask)
        {
            this();

            if (ribbonTask != null)
            {
                name = ribbonTask.getTitle();

                for (AbstractRibbonBand<?> ribbonBand : ribbonTask.getBands())
                    if (ribbonBand instanceof JRibbonBand)
                        addBand((JRibbonBand) ribbonBand);
            }
        }

        public Workspace getWorkspace()
        {
            return Workspace.this;
        }

        public String getWorkspaceName()
        {
            return Workspace.this.getName();
        }

        /**
         * add a BandDefinition from JRibbonBand component
         */
        public BandDefinition addBand(JRibbonBand ribbonBand)
        {
            final BandDefinition result = new BandDefinition(ribbonBand);
            bands.add(result);
            return result;
        }

        /**
         * add a BandDefinition
         */
        public BandDefinition addBand(String bandName)
        {
            BandDefinition band = findBand(bandName);
            if (band == null)
            {
                band = new BandDefinition();
                band.name = bandName;
                bands.add(band);
            }

            return band;
        }

        /**
         * Remove a BandDefinition by name
         * 
         * @param bandName
         *        : name of the band to remove
         */
        public boolean removeBand(String bandName)
        {
            BandDefinition band = findBand(bandName);
            if (band == null)
                return false;
            return removeBand(band);
        }

        /**
         * Remove a band definition
         */
        public boolean removeBand(BandDefinition ribbonBand)
        {
            for (ItemDefinition itd : ribbonBand.getItems())
                ribbonBand.removeItem(itd);
            bands.remove(ribbonBand);
            return true;
        }

        public ItemDefinition addItem(String bandName, String className)
        {
            BandDefinition band = findBand(bandName);
            if (band == null)
            {
                band = new BandDefinition();
                band.name = bandName;
                bands.add(band);
            }

            return band.addItem(className);
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            name = XMLUtil.getAttributeValue((Element) node, ID_NAME, "");

            // clear before loading
            bands.clear();

            final ArrayList<Node> nodesBand = XMLUtil.getSubNodes(node, ID_BAND);
            for (Node n : nodesBand)
                bands.add(new BandDefinition(n));

            return true;
        }

        @Override
        public boolean saveToXML(Node node)
        {
            if (node == null)
                return false;

            XMLUtil.setAttributeValue((Element) node, ID_NAME, name);

            XMLUtil.removeAllChilds(node);
            for (BandDefinition band : bands)
                band.saveToXML(XMLUtil.addElement(node, ID_BAND));

            return true;
        }

        public BandDefinition findBand(String name)
        {
            for (BandDefinition band : bands)
                if (band.name.equals(name))
                    return band;

            return null;
        }

        public ItemDefinition findItem(String className)
        {
            for (BandDefinition band : bands)
            {
                final ItemDefinition item = band.findItem(className);

                if (item != null)
                    return item;
            }

            return null;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return the items
         */
        public ArrayList<BandDefinition> getBands()
        {
            return new ArrayList<BandDefinition>(bands);
        }

    }

    public static final String[] DEFAULT_ACTIVE_WORKSPACES = {"Initial Beta ToolSet", "Tutorial Basics"};

    // public static final String WORKSPACE_DEFAULT_NAME = "default";
    public static final String WORKSPACE_SYSTEM_NAME = "sys";

    private static final String ID_NAME = "name";
    private static final String ID_DESCRIPTION = "description";
    private static final String ID_TASK = "task";

    private String name;
    private String description;
    private final ArrayList<TaskDefinition> tasks;

    private boolean installing;

    /**
     * empty workspace
     */
    public Workspace()
    {
        super();

        name = "";
        description = "";
        tasks = new ArrayList<TaskDefinition>();
        installing = false;
    }

    /**
     * workspace loaded from file
     */
    public Workspace(File file) throws IllegalArgumentException
    {
        this();

        load(file);
    }

    /**
     * workspace loaded from url
     */
    public Workspace(URL url) throws IllegalArgumentException
    {
        this();

        load(url);
    }

    /**
     * workspace loaded from his name
     */
    public Workspace(String name) throws IllegalArgumentException
    {
        this();

        this.name = name;

        load();
    }

    public String getLocalFilename()
    {
        return WorkspaceLoader.WORKSPACE_PATH + FileUtil.separator + name + WorkspaceLoader.EXT;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    public boolean isEmpty()
    {
        return tasks.isEmpty();
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param description
     *        the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the installing
     */
    public boolean isInstalling()
    {
        return installing;
    }

    public void clear()
    {
        tasks.clear();
    }

    public TaskDefinition findTask(String taskName)
    {
        for (TaskDefinition task : tasks)
            if (task.name.equals(taskName))
                return task;

        return null;
    }

    public BandDefinition findBand(String taskName, String bandName)
    {
        final TaskDefinition task = findTask(taskName);
        if (task != null)
            return task.findBand(bandName);

        return null;
    }

    public ItemDefinition findItem(String className)
    {
        for (TaskDefinition task : tasks)
        {
            final ItemDefinition item = task.findItem(className);

            if (item != null)
                return item;
        }

        return null;
    }

    /**
     * @return the definitions
     */
    public ArrayList<TaskDefinition> getTasks()
    {
        return new ArrayList<TaskDefinition>(tasks);
    }

    /**
     * @return all items contained in this workspace
     */
    public ArrayList<ItemDefinition> getAllItems()
    {
        final ArrayList<ItemDefinition> result = new ArrayList<ItemDefinition>();

        for (TaskDefinition task : tasks)
            for (BandDefinition band : task.bands)
                result.addAll(band.items);

        return result;
    }

    /**
     * add a TaskDefinition from RibbonTask component
     */
    public TaskDefinition addTask(RibbonTask ribbonTask)
    {
        final TaskDefinition result = new TaskDefinition(ribbonTask);
        tasks.add(result);
        return result;
    }

    /**
     * add a TaskDefinition
     */
    public TaskDefinition addTask(String taskName)
    {
        TaskDefinition task = findTask(taskName);
        if (task == null)
        {
            task = new TaskDefinition();
            task.name = taskName;
            tasks.add(task);
        }

        return task;
    }

    /**
     * add a BandDefinition
     */
    public BandDefinition addBand(String taskName, String bandName)
    {
        TaskDefinition task = findTask(taskName);
        if (task == null)
        {
            task = new TaskDefinition();
            task.name = taskName;
            tasks.add(task);
        }

        return task.addBand(bandName);
    }

    /**
     * add a ItemDefinition
     */
    public ItemDefinition addItem(String taskName, String bandName, String className)
    {
        TaskDefinition task = findTask(taskName);
        if (task == null)
        {
            task = new TaskDefinition();
            task.name = taskName;
            tasks.add(task);
        }

        return task.addItem(bandName, className);
    }

    public boolean removeTask(String taskName)
    {
        return tasks.remove(findTask(taskName));
    }

    /**
     * load from local
     */
    public boolean load()
    {
        return XMLPersistentHelper.loadFromXML(this, getLocalFilename());
    }

    /**
     * load from file
     */
    public boolean load(File file)
    {
        return XMLPersistentHelper.loadFromXML(this, file);
    }

    /**
     * load from file
     */
    public boolean load(URL url)
    {
        return XMLPersistentHelper.loadFromXML(this, url);
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        name = XMLUtil.getAttributeValue((Element) node, ID_NAME, "");
        description = XMLUtil.getAttributeValue((Element) node, ID_DESCRIPTION, "");
        // don't load the "enabled" property here

        // clear before loading
        tasks.clear();

        final ArrayList<Node> nodesTask = XMLUtil.getSubNodes(node, ID_TASK);
        for (Node n : nodesTask)
            tasks.add(new TaskDefinition(n));

        return true;
    }

    /**
     * save
     */
    public boolean save()
    {
        return XMLPersistentHelper.saveToXML(this, getLocalFilename());
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setAttributeValue((Element) node, ID_NAME, name);
        XMLUtil.setAttributeValue((Element) node, ID_DESCRIPTION, description);
        // don't save the "enabled" property here

        XMLUtil.removeAllChilds(node);
        for (TaskDefinition task : tasks)
            task.saveToXML(XMLUtil.addElement(node, ID_TASK));

        return true;
    }

    /**
     * Install the workspace.<br>
     * Return 0 if workspace cannot be installed<br>
     * Return 1 if workspace correctly installed<br>
     * Return 2 if workspace partially installed<br>
     */
    public int install(PluginRepositoryLoader loader, final ProgressFrame progressFrame)
    {
        if (installing)
            return 0;

        installing = true;

        try
        {
            // get all items
            final ArrayList<ItemDefinition> items = getAllItems();
            final ArrayList<PluginDescriptor> pluginsToInstall = new ArrayList<PluginDescriptor>();

            if (progressFrame != null)
            {
                progressFrame.setLength(items.size() + 2);
                progressFrame.setPosition(0);
                progressFrame.setMessage("waiting for plugin loader to find plugins...");
            }

            // wait while online loader is ready
            loader.waitWhileLoading();

            if (progressFrame != null)
            {
                progressFrame.incPosition();
                progressFrame.setMessage("installing workspace '" + name + "' : searching for installed plugins...");
            }

            for (ItemDefinition item : items)
            {
                // avoid separator item
                if (!item.isSeparator())
                {
                    final String className = item.getClassName();

                    // try first from loaded plugins
                    PluginDescriptor plugin = PluginLoader.getPlugin(className);

                    // not found ?
                    if (plugin == null)
                    {
                        // then try from repositery plugins
                        plugin = loader.getPlugin(className);

                        if (plugin == null)
                        {
                            System.err.println("Can't install plugin '" + className + "' : not found in repositery");
                            return 0;
                        }

                        // add to installation list
                        PluginDescriptor.addToList(pluginsToInstall, plugin);
                    }
                }
            }

            PluginLoader.beginUpdate();
            try
            {
                if (progressFrame != null)
                {
                    progressFrame.setLength(pluginsToInstall.size() + 2);
                    progressFrame.incPosition();
                    progressFrame.setMessage("installing workspace '" + name + "' : downloading plugins...");
                }

                // install missing plugins (no confirmation needed)
                for (PluginDescriptor plugin : pluginsToInstall)
                    PluginInstaller.install(loader, plugin, false);

                // wait installation completion
                while (PluginInstaller.isInstalling())
                {
                    ThreadUtil.sleep(10);
                    // update progress frame
                    progressFrame.setPosition(1 + (pluginsToInstall.size() - PluginInstaller.getInstallFIFO().size()));
                }
            }
            finally
            {
                PluginLoader.endUpdate();
            }

            final ArrayList<PluginDescriptor> installedPlugins = PluginLoader.getPlugins();

            // get number of correctly installed plugins
            int numberOfInstalledPlugin = 0;
            for (PluginDescriptor plugin : pluginsToInstall)
                if (PluginDescriptor.existInList(installedPlugins, plugin))
                    numberOfInstalledPlugin++;

            // return 1 if complete installation
            if (numberOfInstalledPlugin == pluginsToInstall.size())
                return 1;
            // return 2 if partial installation
            else if (numberOfInstalledPlugin > 0)
                return 2;

            // return 0 if no installation
            return 0;
        }
        finally
        {
            installing = false;
        }
    }

    @Override
    public int compareTo(Workspace o)
    {
        return name.compareTo(o.getName());
    }
}
