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
package icy.gui.preferences;

import icy.gui.component.IcyTable;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.plugin.PluginDetailPanel;
import icy.gui.util.ComponentUtil;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.resource.ResourceUtil;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author Stephane
 */
public abstract class PluginListPreferencePanel extends PreferencePanel
        implements TextChangeListener, ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2718763355377652489L;

    static final String[] columnNames = {"", "Name", "Version", "State", "Enabled"};
    static final String[] columnIds = {"Icon", "Name", "Version", "State", "Enabled"};

    List<PluginDescriptor> plugins;

    /**
     * gui
     */
    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox repository;
    final JPanel repositoryPanel;
    final IcyTextField filter;
    final JButton refreshButton;
    final JButton documentationButton;
    final JButton detailButton;
    final JButton action1Button;
    final JButton action2Button;

    private final Runnable buttonsStateUpdater;
    private final Runnable tableDataRefresher;
    private final Runnable pluginsListRefresher;
    private final Runnable repositoriesUpdater;

    final ActionListener repositoryActionListener;

    PluginListPreferencePanel(PreferenceFrame parent, String nodeName, String parentName)
    {
        super(parent, nodeName, parentName);

        plugins = new ArrayList<PluginDescriptor>();

        buttonsStateUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                // need to be done on EDT
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateButtonsStateInternal();
                    }
                });
            }
        };
        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                // need to be done on EDT
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshTableDataInternal();
                    }
                });
            }
        };
        pluginsListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshPluginsInternal();
            }
        };
        repositoriesUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                // need to be done on EDT
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateRepositoriesInternal();
                    }
                });
            }
        };
        repositoryActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                repositoryChanged();
            }
        };

        repository = new JComboBox();
        repository.setToolTipText("Select a repository");
        repository.addActionListener(repositoryActionListener);

        repositoryPanel = new JPanel();
        repositoryPanel.setLayout(new BoxLayout(repositoryPanel, BoxLayout.PAGE_AXIS));
        repositoryPanel.setVisible(false);

        final JPanel internalRepPanel = new JPanel();
        internalRepPanel.setLayout(new BoxLayout(internalRepPanel, BoxLayout.LINE_AXIS));

        internalRepPanel.add(new JLabel("Repository :"));
        internalRepPanel.add(Box.createHorizontalStrut(8));
        internalRepPanel.add(repository);
        internalRepPanel.add(Box.createHorizontalGlue());

        repositoryPanel.add(internalRepPanel);
        repositoryPanel.add(Box.createVerticalStrut(8));

        // need filter before load()
        filter = new IcyTextField();
        filter.addTextChangeListener(this);

        // build buttons panel
        final Dimension buttonsDim = new Dimension(100, 24);

        refreshButton = new JButton("Reload list");
        refreshButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reloadPlugins();
            }
        });
        ComponentUtil.setFixedSize(refreshButton, buttonsDim);

        documentationButton = new JButton("Online doc");
        documentationButton.setToolTipText("Open the online documentation");
        documentationButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

                // open plugin web page
                if (selectedPlugins.size() == 1)
                    NetworkUtil.openBrowser(selectedPlugins.get(0).getWeb());
            }
        });
        ComponentUtil.setFixedSize(documentationButton, buttonsDim);

        detailButton = new JButton("Show detail");
        detailButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

                // open the detail
                if (selectedPlugins.size() == 1)
                    new PluginDetailPanel(selectedPlugins.get(0));
            }
        });
        ComponentUtil.setFixedSize(detailButton, buttonsDim);

        action1Button = new JButton("null");
        action1Button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doAction1();
            }
        });
        action1Button.setVisible(false);
        ComponentUtil.setFixedSize(action1Button, buttonsDim);

        action2Button = new JButton("null");
        action2Button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doAction2();
            }
        });
        action2Button.setVisible(false);
        ComponentUtil.setFixedSize(action2Button, buttonsDim);

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(34));
        buttonsPanel.add(documentationButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(detailButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(action1Button);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(action2Button);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(Box.createVerticalGlue());

        // build table
        tableModel = new AbstractTableModel()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -8573364273165723214L;

            @Override
            public int getColumnCount()
            {
                return columnNames.length;
            }

            @Override
            public String getColumnName(int column)
            {
                return columnNames[column];
            }

            @Override
            public int getRowCount()
            {
                return plugins.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                if (row < plugins.size())
                {
                    final PluginDescriptor plugin = plugins.get(row);

                    switch (column)
                    {
                        case 0:
                            if (plugin.isIconLoaded())
                                return ResourceUtil.scaleIcon(plugin.getIcon(), 32);

                            loadIconAsync(plugin);
                            return ResourceUtil.scaleIcon(PluginDescriptor.DEFAULT_ICON, 32);

                        case 1:
                            return plugin.getName();

                        case 2:
                            return plugin.getVersion().toString();

                        case 3:
                            return getStateValue(plugin);

                        case 4:
                            return Boolean.valueOf(isActive(plugin));
                    }
                }

                return "";
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                if (rowIndex < plugins.size())
                {
                    final PluginDescriptor plugin = plugins.get(rowIndex);

                    if (columnIndex == 4)
                    {
                        if (aValue instanceof Boolean)
                            setActive(plugin, ((Boolean) aValue).booleanValue());
                    }
                }
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return (column == 4);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0:
                        return ImageIcon.class;
                    case 4:
                        return Boolean.class;
                    default:
                        return String.class;
                }
            }
        };

        table = new IcyTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setIdentifier(columnIds[0]);
        col.setMinWidth(32);
        col.setPreferredWidth(32);
        col.setMaxWidth(32);

        col = colModel.getColumn(1);
        col.setIdentifier(columnIds[1]);
        col.setMinWidth(120);
        col.setPreferredWidth(200);
        col.setMaxWidth(500);

        col = colModel.getColumn(2);
        col.setIdentifier(columnIds[2]);
        col.setMinWidth(60);
        col.setPreferredWidth(60);
        col.setMaxWidth(60);

        col = colModel.getColumn(3);
        col.setIdentifier(columnIds[3]);
        col.setMinWidth(70);
        col.setPreferredWidth(90);
        col.setMaxWidth(120);

        col = colModel.getColumn(4);
        col.setIdentifier(columnIds[4]);
        col.setMinWidth(60);
        col.setPreferredWidth(60);
        col.setMaxWidth(60);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowHeight(32);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        // sort on name by default
        table.getRowSorter().toggleSortOrder(1);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                if (!me.isConsumed())
                {
                    if (me.getClickCount() == 2)
                    {
                        // show detail
                        detailButton.doClick();
                        me.consume();
                    }
                }
            }
        });

        final JPanel tableTopPanel = new JPanel();

        tableTopPanel.setLayout(new BoxLayout(tableTopPanel, BoxLayout.PAGE_AXIS));

        tableTopPanel.add(Box.createVerticalStrut(2));
        tableTopPanel.add(repositoryPanel);
        tableTopPanel.add(filter);
        tableTopPanel.add(Box.createVerticalStrut(8));
        tableTopPanel.add(table.getTableHeader());

        final JPanel tablePanel = new JPanel();

        tablePanel.setLayout(new BorderLayout());

        tablePanel.add(tableTopPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        mainPanel.validate();
    }

    protected void loadIconAsync(final PluginDescriptor plugin)
    {
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // icon correctly loaded ?
                if (plugin.loadIcon())
                    refreshTableData();
            }
        });
    }

    @Override
    protected void closed()
    {
        super.closed();

        // do not retains plugins when frame is closed
        plugins.clear();
    }

    private List<PluginDescriptor> filterList(List<PluginDescriptor> list, String filter)
    {
        final List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        final boolean empty = StringUtil.isEmpty(filter, true);
        final String filterUp;

        if (!empty)
            filterUp = filter.toUpperCase();
        else
            filterUp = "";

        for (PluginDescriptor plugin : list)
        {
            final String classname = plugin.getClassName().toUpperCase();
            final String name = plugin.getName().toUpperCase();
            final String desc = plugin.getDescription().toUpperCase();

            // search in name and description
            if (empty || (classname.indexOf(filterUp) != -1) || (name.indexOf(filterUp) != -1)
                    || (desc.indexOf(filterUp) != -1))
                result.add(plugin);
        }

        return result;
    }

    protected boolean isActive(PluginDescriptor plugin)
    {
        return false;
    }

    protected void setActive(PluginDescriptor plugin, boolean value)
    {
    }

    protected abstract void doAction1();

    protected abstract void doAction2();

    protected abstract void repositoryChanged();

    protected abstract void reloadPlugins();

    protected abstract String getStateValue(PluginDescriptor plugin);

    protected abstract List<PluginDescriptor> getPlugins();

    protected int getPluginTableIndex(int rowIndex)
    {
        if (rowIndex == -1)
            return rowIndex;

        try
        {

            return table.convertRowIndexToView(rowIndex);
        }
        catch (IndexOutOfBoundsException e)
        {
            return -1;
        }
    }

    protected int getPluginIndex(PluginDescriptor plugin)
    {
        return plugins.indexOf(plugin);
    }

    protected int getPluginModelIndex(PluginDescriptor plugin)
    {
        return getPluginIndex(plugin);
    }

    protected int getPluginTableIndex(PluginDescriptor plugin)
    {
        return getPluginTableIndex(getPluginModelIndex(plugin));
    }

    protected int getPluginIndex(String pluginClassName)
    {
        for (int i = 0; i < plugins.size(); i++)
        {
            final PluginDescriptor plugin = plugins.get(i);

            if (plugin.getClassName().equals(pluginClassName))
                return i;
        }

        return -1;
    }

    protected int getPluginModelIndex(String pluginClassName)
    {
        return getPluginIndex(pluginClassName);
    }

    protected int getPluginTableIndex(String pluginClassName)
    {
        return getPluginTableIndex(getPluginModelIndex(pluginClassName));
    }

    List<PluginDescriptor> getSelectedPlugins()
    {
        final List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        final int[] rows = table.getSelectedRows();
        if (rows.length == 0)
            return result;

        final List<PluginDescriptor> cachedPlugins = plugins;

        for (int i = 0; i < rows.length; i++)
        {
            try
            {
                final int index = table.convertRowIndexToModel(rows[i]);
                if (index < cachedPlugins.size())
                    result.add(cachedPlugins.get(index));
            }
            catch (IndexOutOfBoundsException e)
            {
                // ignore as async process can cause it
            }
        }

        return result;
    }

    /**
     * Select the specified list of ROI in the ROI Table
     */
    void setSelectedPlugins(HashSet<PluginDescriptor> newSelected)
    {
        final List<PluginDescriptor> modelPlugins = plugins;
        final ListSelectionModel selectionModel = table.getSelectionModel();

        // start selection change
        selectionModel.setValueIsAdjusting(true);
        try
        {
            // start by clearing selection
            selectionModel.clearSelection();

            for (int i = 0; i < modelPlugins.size(); i++)
            {
                final PluginDescriptor plugin = modelPlugins.get(i);

                // HashSet provide fast "contains"
                if (newSelected.contains(plugin))
                {
                    try
                    {
                        // convert model index to view index
                        final int ind = table.convertRowIndexToView(i);
                        if (ind != -1)
                            selectionModel.addSelectionInterval(ind, ind);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        // ignore
                    }
                }
            }
        }
        finally
        {
            // end selection change
            selectionModel.setValueIsAdjusting(false);
        }
    }

    protected void refreshPluginsInternal()
    {
        plugins = filterList(getPlugins(), filter.getText());
        // refresh table data
        refreshTableData();
    }

    protected final void refreshPlugins()
    {
        ThreadUtil.runSingle(pluginsListRefresher);
    }

    protected void updateButtonsStateInternal()
    {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final boolean singleSelection = (selectedPlugins.size() == 1);
        final PluginDescriptor singlePlugin = singleSelection ? selectedPlugins.get(0) : null;

        detailButton.setEnabled(singleSelection);
        documentationButton.setEnabled(singleSelection && !StringUtil.isEmpty(singlePlugin.getWeb()));
    }

    protected final void updateButtonsState()
    {
        ThreadUtil.runSingle(buttonsStateUpdater);
    }

    protected void updateRepositoriesInternal()
    {
        // final RepositoryPreferencePanel panel = (RepositoryPreferencePanel)
        // getPreferencePanel(RepositoryPreferencePanel.class);
        // // refresh repositories list (use list from GUI)
        // final ArrayList<RepositoryInfo> repositeries = panel.repositories;

        // refresh repositories list
        final List<RepositoryInfo> repositeries = RepositoryPreferences.getRepositeries();
        final RepositoryInfo savedRepository = (RepositoryInfo) repository.getSelectedItem();

        // needed to disable events during update time
        repository.removeActionListener(repositoryActionListener);

        repository.removeAllItems();
        for (RepositoryInfo repos : repositeries)
            if (repos.isEnabled())
                repository.addItem(repos);

        repository.addActionListener(repositoryActionListener);

        boolean selected = false;

        // try to set back the old selected repository
        if (savedRepository != null)
        {
            final String repositoryName = savedRepository.getName();

            for (int ind = 0; ind < repository.getItemCount(); ind++)
            {
                final RepositoryInfo repo = (RepositoryInfo) repository.getItemAt(ind);

                if ((repo != null) && (repo.getName().equals(repositoryName)))
                {
                    repository.setSelectedIndex(ind);
                    selected = true;
                    break;
                }
            }
        }

        // manually launch the action
        if (!selected)
            repository.setSelectedIndex((repository.getItemCount() > 0) ? 0 : -1);

        // avoid automatic minimum size here
        repository.setMinimumSize(new Dimension(48, 18));
    }

    protected final void updateRepositories()
    {
        ThreadUtil.runSingle(repositoriesUpdater);
    }

    protected void refreshTableDataInternal()
    {
        final List<PluginDescriptor> plugins = getSelectedPlugins();

        try
        {
            tableModel.fireTableDataChanged();
        }
        catch (Throwable t)
        {
            // sometime sorting can throw exception, ignore them...
        }

        // restore previous selected plugins if possible
        setSelectedPlugins(new HashSet<PluginDescriptor>(plugins));
        // update button state
        buttonsStateUpdater.run();
    }

    protected final void refreshTableData()
    {
        ThreadUtil.runSingle(tableDataRefresher);
    }

    protected void pluginsChanged()
    {
        refreshPlugins();
    }

    @Override
    protected void load()
    {

    }

    @Override
    protected void save()
    {
        // reload repositories as some parameter as beta flag can have changed
        updateRepositories();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        pluginsChanged();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        final int selected = table.getSelectedRow();

        if (!e.getValueIsAdjusting() && (selected != -1))
        {
            final int fi = e.getFirstIndex();
            final int li = e.getLastIndex();

            if ((fi == -1) || ((fi <= selected) && (li >= selected)))
                updateButtonsState();
        }
    }
}
