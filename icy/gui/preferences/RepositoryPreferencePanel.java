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
import icy.plugin.PluginRepositoryLoader;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.workspace.WorkspaceRepositoryLoader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
public class RepositoryPreferencePanel extends PreferencePanel implements ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 5676905012950916850L;

    public static final String NODE_NAME = "Repository";

    static final String[] columnNames = {"Name", "Location", "Enabled"};

    /**
     * list of repository
     */
    final ArrayList<RepositoryInfo> repositories;

    /**
     * gui
     */
    final AbstractTableModel tableModel;
    final JTable table;

    final JButton addButton;
    final JButton editButton;
    final JButton removeButton;

    RepositoryPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        repositories = new ArrayList<RepositoryInfo>();

        load();

        // build buttons
        addButton = new JButton("add...");
        addButton.setToolTipText("Add a new repository");
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addRepository();
            }
        });

        editButton = new JButton("edit...");
        editButton.setToolTipText("Edit selected repository");
        editButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editRepository(getSelectedRepository());
            }
        });

        removeButton = new JButton("remove");
        removeButton.setToolTipText("Delete selected repository");
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeRepository(getSelectedRepository());
            }
        });

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        buttonsPanel.add(addButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(editButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(removeButton);
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
                return repositories.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                final RepositoryInfo reposInf = repositories.get(row);

                switch (column)
                {
                    case 0:
                        return reposInf.getName();

                    case 1:
                        return reposInf.getLocation();

                    case 2:
                        return Boolean.valueOf(reposInf.isEnabled());

                }

                return "";
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return (column == 2);
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                final RepositoryInfo reposInf = repositories.get(rowIndex);

                switch (columnIndex)
                {
                    case 0:
                    case 1:
                        // read only
                        break;

                    case 2:
                        if (aValue instanceof Boolean)
                            reposInf.setEnabled(((Boolean) aValue).booleanValue());
                        break;
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                if (columnIndex == 2)
                    return Boolean.class;

                return String.class;
            }
        };

        table = new IcyTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setMinWidth(80);
        col.setPreferredWidth(80);
        col.setMaxWidth(120);

        col = colModel.getColumn(1);
        col.setMinWidth(160);
        col.setPreferredWidth(240);
        col.setMaxWidth(500);

        col = colModel.getColumn(2);
        col.setMinWidth(60);
        col.setPreferredWidth(60);
        col.setMaxWidth(60);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        final JPanel tablePanel = new JPanel();

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));

        tablePanel.add(table.getTableHeader());
        tablePanel.add(new JScrollPane(table));

        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        mainPanel.validate();

        // select first entry in table
        table.getSelectionModel().setSelectionInterval(0, 0);
    }

    @Override
    protected void load()
    {
        repositories.clear();
        repositories.addAll(RepositoryPreferences.getRepositeries());
    }

    @Override
    protected void save()
    {
        // save to rpeferences
        RepositoryPreferences.setRepositeries(repositories);

        // then reload online plugins and workspace as repositories changed
        PluginRepositoryLoader.reload();
        WorkspaceRepositoryLoader.reload();

        // update repositories on Workspace and Plugin panel
        ((PluginLocalPreferencePanel) getPreferencePanel(PluginLocalPreferencePanel.class)).updateRepositories();
        ((PluginOnlinePreferencePanel) getPreferencePanel(PluginOnlinePreferencePanel.class)).updateRepositories();
        ((WorkspaceOnlinePreferencePanel) getPreferencePanel(WorkspaceOnlinePreferencePanel.class))
                .updateRepositories();
        ((WorkspaceLocalPreferencePanel) getPreferencePanel(WorkspaceLocalPreferencePanel.class)).updateRepositories();
    }

    private int getRepositeryIndex(RepositoryInfo reposInf)
    {
        return repositories.indexOf(reposInf);
    }

    private int getRepositeryModelIndex(RepositoryInfo reposInf)
    {
        return getRepositeryIndex(reposInf);
    }

    // private int getRepositeryTableIndex(RepositoryInfo reposInf)
    // {
    // final int ind = getRepositeryModelIndex(reposInf);
    //
    // if (ind != -1)
    // return table.convertRowIndexToView(ind);
    //
    // return ind;
    // }

    RepositoryInfo getSelectedRepository()
    {
        int index;

        index = table.getSelectedRow();
        if (index == -1)
            return null;

        index = table.convertRowIndexToModel(index);
        if (index == -1)
            return null;

        return repositories.get(index);
    }

    boolean addRepository()
    {
        final RepositoryInfo reposInf = new RepositoryInfo("name", "http://");

        if (!new EditRepositoryDialog("Add a new repository", reposInf).isCanceled())
        {
            // add new repository entry
            repositories.add(reposInf);
            // get index
            final int ind = getRepositeryModelIndex(reposInf);
            // notify data changed
            tableModel.fireTableRowsInserted(ind, ind);

            return true;
        }

        return false;
    }

    boolean editRepository(final RepositoryInfo reposInf)
    {
        final int ind = getRepositeryModelIndex(reposInf);

        if (!new EditRepositoryDialog("Edit repository", reposInf).isCanceled())
        {
            try
            {
                // notify data changed
                tableModel.fireTableRowsUpdated(ind, ind);
            }
            catch (Exception e)
            {
                // ignore possible exception here
            }

            return true;
        }

        return false;
    }

    boolean removeRepository(final RepositoryInfo reposInf)
    {
        final int ind = getRepositeryModelIndex(reposInf);

        if (repositories.remove(reposInf))
        {
            // notify data changed
            tableModel.fireTableRowsDeleted(ind, ind);

            return true;
        }

        return false;
    }

    private void udpateButtonsState()
    {
        final RepositoryInfo selectedRepos = getSelectedRepository();
        final boolean enabled = (selectedRepos != null) && !selectedRepos.isDefault();

        editButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        udpateButtonsState();
    }

}
