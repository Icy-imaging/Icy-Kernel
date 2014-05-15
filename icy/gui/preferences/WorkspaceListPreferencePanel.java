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
package icy.gui.preferences;

import icy.gui.component.IcyTable;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.util.ComponentUtil;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import icy.workspace.Workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
public abstract class WorkspaceListPreferencePanel extends PreferencePanel implements TextChangeListener,
        ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -264986966623111155L;

    private static final String BOTTOM_MESS = "*Workspace modification need application restart to take effect";

    static final String[] columnNames = {"Name", "Description", "", "Enabled"};

    // protected final Preferences preferences;
    ArrayList<Workspace> workspaces;

    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox repository;
    final JPanel repositoryPanel;
    final IcyTextField filter;
    final JButton refreshButton;
    final JButton action1Button;

    private final Runnable buttonsStateUpdater;
    private final Runnable tableDataRefresher;
    private final Runnable workspaceListRefresher;
    private final Runnable repositoriesUpdater;

    final ActionListener repositoryActionListener;

    WorkspaceListPreferencePanel(PreferenceFrame parent, String nodeName)
    {
        super(parent, nodeName, WorkspacePreferencePanel.NODE_NAME);

        // preferences = Preferences.userRoot().node(preferencesId);
        workspaces = new ArrayList<Workspace>();

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
        workspaceListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshWorkspacesInternal();
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

        // build repository comboBox
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

        load();

        // build buttons panel
        final Dimension buttonsDim = new Dimension(100, 24);

        refreshButton = new JButton("Reload list");
        refreshButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reloadWorkspaces();
            }
        });
        ComponentUtil.setFixedSize(refreshButton, buttonsDim);

        action1Button = new JButton("null");
        action1Button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doAction1(getSelectedWorkspace());
            }
        });
        action1Button.setVisible(false);
        ComponentUtil.setFixedSize(action1Button, buttonsDim);

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(64));
        buttonsPanel.add(action1Button);
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
                return WorkspaceListPreferencePanel.this.getColumnCount();
            }

            @Override
            public String getColumnName(int column)
            {
                return columnNames[column];
            }

            @Override
            public int getRowCount()
            {
                return workspaces.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                final Workspace workspace = workspaces.get(row);

                switch (column)
                {
                    case 0:
                        return workspace.getName();

                    case 1:
                        return workspace.getDescription();

                    case 2:
                        return getStateValue(workspace);

                    case 3:
                        return isWorkspaceEnable(workspace);
                }

                return "";
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return (column == 3);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                if (columnIndex == 3)
                    return Boolean.class;

                return String.class;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                final Workspace workspace = workspaces.get(rowIndex);

                if ((columnIndex == 3) && (aValue instanceof Boolean))
                    setWorkspaceEnable(workspace, (Boolean) aValue);

                // workspace setting changed, restart needed
                getPreferenceFrame().setNeedRestart();
            }
        };

        table = new IcyTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setMinWidth(80);
        col.setPreferredWidth(100);
        col.setMaxWidth(120);

        col = colModel.getColumn(1);
        col.setMinWidth(160);
        col.setPreferredWidth(240);
        col.setMaxWidth(500);

        col = colModel.getColumn(2);
        col.setMinWidth(60);
        col.setPreferredWidth(70);
        col.setMaxWidth(80);

        if (colModel.getColumnCount() > 3)
        {
            col = colModel.getColumn(3);
            col.setMinWidth(60);
            col.setPreferredWidth(60);
            col.setMaxWidth(60);
        }

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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
        mainPanel.add(new JLabel(BOTTOM_MESS), BorderLayout.SOUTH);

        mainPanel.validate();
    }

    @Override
    protected void closed()
    {
        super.closed();

        // do not retains workspaces when frame is closed
        workspaces.clear();
    }

    private ArrayList<Workspace> filterList(ArrayList<Workspace> list, String filter)
    {
        final ArrayList<Workspace> result = new ArrayList<Workspace>();
        final boolean empty = StringUtil.isEmpty(filter, true);
        final String filterUp;

        if (!empty)
            filterUp = filter.toUpperCase();
        else
            filterUp = "";

        for (Workspace workspace : list)
        {
            final String name = workspace.getName().toUpperCase();
            final String desc = workspace.getDescription().toUpperCase();

            // search in name and description
            if (empty || (name.indexOf(filterUp) != -1) || (desc.indexOf(filterUp) != -1))
                result.add(workspace);
        }

        return result;
    }

    protected abstract void doAction1(Workspace workspace);

    protected abstract void repositoryChanged();

    protected abstract void reloadWorkspaces();

    protected abstract String getStateValue(Workspace workspace);

    protected abstract int getColumnCount();

    protected abstract ArrayList<Workspace> getWorkspaces();

    protected abstract void updateButtonsStateInternal();

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
        final ArrayList<RepositoryInfo> repositeries = RepositoryPreferences.getRepositeries();

        final RepositoryInfo savedRepository = (RepositoryInfo) repository.getSelectedItem();

        // needed to disable events during update time
        repository.removeActionListener(repositoryActionListener);

        repository.removeAllItems();
        for (RepositoryInfo repos : repositeries)
            if (repos.isEnabled())
                repository.addItem(repos);
        repository.setEnabled(true);

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

    protected Boolean isWorkspaceEnable(Workspace workspace)
    {
        return Boolean.FALSE;
    }

    protected void setWorkspaceEnable(Workspace workspace, Boolean value)
    {
        // ignore
    }

    protected void refreshWorkspacesInternal()
    {
        workspaces = filterList(getWorkspaces(), filter.getText());
    }

    protected final void refreshWorkspaces()
    {
        ThreadUtil.runSingle(workspaceListRefresher);
    }

    protected int getWorkspaceIndex(Workspace workspace)
    {
        return workspaces.indexOf(workspace);
    }

    protected int getWorkspaceModelIndex(Workspace workspace)
    {
        return getWorkspaceIndex(workspace);
    }

    protected int getWorkspaceTableIndex(Workspace workspace)
    {
        final int ind = getWorkspaceModelIndex(workspace);

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

    Workspace getSelectedWorkspace()
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

        if ((index < 0) || (index >= workspaces.size()))
            return null;

        return workspaces.get(index);
    }

    void setSelectedWorkspace(Workspace workspace)
    {
        final int index = getWorkspaceTableIndex(workspace);

        if (index > -1)
        {
            table.clearSelection();
            table.getSelectionModel().setSelectionInterval(index, index);
        }
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

    protected void refreshTableDataInternal()
    {
        final Workspace workspace = getSelectedWorkspace();

        tableModel.fireTableDataChanged();

        // restore previous selected workspace if possible
        setSelectedWorkspace(workspace);
    }

    protected final void refreshTableData()
    {
        ThreadUtil.runSingle(tableDataRefresher);
    }

    protected void workspacesChanged()
    {
        refreshWorkspaces();
        refreshTableData();
        updateButtonsState();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        workspacesChanged();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        updateButtonsState();
    }
}
