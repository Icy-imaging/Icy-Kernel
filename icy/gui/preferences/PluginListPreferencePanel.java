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
package icy.gui.preferences;

import icy.gui.component.ComponentUtil;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.plugin.PluginDetailPanel;
import icy.plugin.PluginDescriptor;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.resource.ResourceUtil;
import icy.system.thread.InstanceProcessor;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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
public abstract class PluginListPreferencePanel extends PreferencePanel implements TextChangeListener,
        ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2718763355377652489L;

    static final String[] columnNames = {"", "Name", "Version", "State", "Enabled"};
    static final String[] columnIds = {"Icon", "Name", "Version", "State", "Enabled"};

    ArrayList<PluginDescriptor> plugins;

    /**
     * gui
     */
    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox repository;
    final JPanel repositoryPanel;
    final IcyTextField filter;
    final JButton refreshButton;
    final JButton detailButton;
    final JButton action1Button;
    final JButton action2Button;

    final InstanceProcessor processor;

    private final Runnable buttonsStateUpdater;
    private final Runnable tableDataRefresher;
    private final Runnable pluginsListRefresher;
    private final Runnable repositoriesUpdater;

    final ActionListener repositoryActionListener;

    PluginListPreferencePanel(PreferenceFrame parent, String nodeName, String parentName)
    {
        super(parent, nodeName, parentName);

        plugins = new ArrayList<PluginDescriptor>();

        processor = new InstanceProcessor();
        processor.setDefaultThreadName("Plugin preferences GUI");

        buttonsStateUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                updateButtonsStateInternal();
            }
        };
        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataInternal();
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
                updateRepositoriesInternal();
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

        detailButton = new JButton("Show detail");
        detailButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // open the detail
                new PluginDetailPanel(getSelectedPlugin());
            }
        });
        ComponentUtil.setFixedSize(detailButton, buttonsDim);

        action1Button = new JButton("null");
        action1Button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doAction1(getSelectedPlugin());
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
                doAction2(getSelectedPlugin());
            }
        });
        action2Button.setVisible(false);
        ComponentUtil.setFixedSize(action2Button, buttonsDim);

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(64));
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
                            return ResourceUtil.scaleIcon(plugin.getIcon(), 32);

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

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setIdentifier(columnIds[0]);
        col.setPreferredWidth(32);
        col.setMinWidth(32);
        col.setResizable(false);

        col = colModel.getColumn(1);
        col.setIdentifier(columnIds[1]);
        col.setPreferredWidth(200);
        col.setMinWidth(120);

        col = colModel.getColumn(2);
        col.setIdentifier(columnIds[2]);
        col.setPreferredWidth(70);
        col.setMinWidth(60);

        col = colModel.getColumn(3);
        col.setIdentifier(columnIds[3]);
        col.setPreferredWidth(90);
        col.setMinWidth(70);

        col = colModel.getColumn(4);
        col.setIdentifier(columnIds[4]);
        col.setPreferredWidth(70);
        col.setMinWidth(30);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowHeight(32);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        // sort on name by default
        table.getRowSorter().toggleSortOrder(1);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

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

    private ArrayList<PluginDescriptor> filterList(ArrayList<PluginDescriptor> list, String filter)
    {
        final ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        final boolean empty = StringUtil.isEmpty(filter, true);
        final String filterUp;

        if (!empty)
            filterUp = filter.toUpperCase();
        else
            filterUp = "";

        for (PluginDescriptor plugin : list)
        {
            final String name = plugin.getName().toUpperCase();
            final String desc = plugin.getDescription().toUpperCase();

            // search in name and description
            if (empty || (name.indexOf(filterUp) != -1) || (desc.indexOf(filterUp) != -1))
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

    protected abstract void doAction1(PluginDescriptor plugin);

    protected abstract void doAction2(PluginDescriptor plugin);

    protected abstract void repositoryChanged();

    protected abstract void reloadPlugins();

    protected abstract String getStateValue(PluginDescriptor plugin);

    protected abstract ArrayList<PluginDescriptor> getPlugins();

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
        final int ind = getPluginModelIndex(plugin);

        if (ind == -1)
            return ind;

        try
        {

            return table.convertRowIndexToView(ind);
        }
        catch (IndexOutOfBoundsException e)
        {
            return -1;
        }
    }

    PluginDescriptor getSelectedPlugin()
    {
        int index;

        index = table.getSelectedRow();
        if (index == -1)
            return null;

        try
        {
            index = table.convertRowIndexToModel(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            index = -1;
        }

        if ((index < 0) || (index >= plugins.size()))
            return null;

        return plugins.get(index);
    }

    void setSelectedPlugin(PluginDescriptor plugin)
    {
        final int index = getPluginTableIndex(plugin);

        if (index > -1)
        {
            table.clearSelection();
            table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    protected void refreshPluginsInternal()
    {
        plugins = filterList(getPlugins(), filter.getText());
    }

    protected final void refreshPlugins()
    {
        processor.addTask(pluginsListRefresher, false);
    }

    protected void updateButtonsStateInternal()
    {
        final boolean pluginSelected = (getSelectedPlugin() != null);

        detailButton.setEnabled(pluginSelected);
    }

    protected final void updateButtonsState()
    {
        processor.addTask(buttonsStateUpdater, true);
    }

    protected void updateRepositoriesInternal()
    {
        // final RepositoryPreferencePanel panel = (RepositoryPreferencePanel)
        // getPreferencePanel(RepositoryPreferencePanel.class);
        // // refresh repositories list (use list from GUI)
        // final ArrayList<RepositoryInfo> repositeries = panel.repositories;

        // refresh repositories list
        final ArrayList<RepositoryInfo> repositeries = RepositoryPreferences.getRepositeries();

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
        processor.addTask(repositoriesUpdater, true);
    }

    protected void refreshTableDataInternal()
    {
        final PluginDescriptor plugin = getSelectedPlugin();

        tableModel.fireTableDataChanged();

        // restore previous selected plugin if possible
        setSelectedPlugin(plugin);
    }

    protected final void refreshTableData()
    {
        processor.addTask(tableDataRefresher, true);
    }

    protected void pluginsChanged()
    {
        refreshPlugins();
        refreshTableData();
        updateButtonsState();
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
    public void textChanged(IcyTextField source)
    {
        pluginsChanged();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        updateButtonsState();
    }
}
