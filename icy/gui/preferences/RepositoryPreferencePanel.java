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
import icy.gui.dialog.ActionDialog;
import icy.main.Icy;
import icy.preferences.RepositoryPreferences;
import icy.preferences.RepositoryPreferences.RepositoryInfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private class RepositoryDialog extends ActionDialog
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6474402466638414723L;

        // GUI
        final JTextField nameField;
        final JTextField locationField;
        final JCheckBox authCheckBox;
        final JTextField loginField;
        final JPasswordField passwordField;

        final JLabel nameLabel;
        final JLabel locationLabel;
        final JLabel authLabel;
        final JLabel loginLabel;
        final JLabel passwordLabel;

        // internal
        boolean canceled;

        public RepositoryDialog(String title, final RepositoryInfo reposInf)
        {
            super(Icy.getMainInterface().getFrame(), title);

            setMinimumSize(new Dimension(400, 200));
            // setPreferredSize(new Dimension(600, 200));

            canceled = true;

            nameField = new JTextField(reposInf.getName());
            ComponentUtil.setFixedHeight(nameField, 24);
            locationField = new JTextField(reposInf.getLocation());
            ComponentUtil.setFixedHeight(locationField, 24);
            authCheckBox = new JCheckBox("", reposInf.isAuthenticationEnabled());
            ComponentUtil.setFixedHeight(authCheckBox, 24);
            loginField = new JTextField(reposInf.getLogin());
            ComponentUtil.setFixedHeight(loginField, 24);
            passwordField = new JPasswordField(reposInf.getPassword());
            ComponentUtil.setFixedHeight(passwordField, 24);

            authCheckBox.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    updateAuthFields();
                }
            });

            // save changes on validation
            setOkAction(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    reposInf.setName(nameField.getText());
                    reposInf.setLocation(locationField.getText());
                    reposInf.setLogin(loginField.getText());
                    reposInf.setPassword(new String(passwordField.getPassword()));
                    reposInf.setAuthenticationEnabled(authCheckBox.isSelected());
                    canceled = false;
                }
            });

            mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            mainPanel.setLayout(new BorderLayout(8, 8));

            final JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));

            nameLabel = new JLabel("Name");
            ComponentUtil.setFixedHeight(nameLabel, 24);
            locationLabel = new JLabel("Location");
            ComponentUtil.setFixedHeight(locationLabel, 24);
            authLabel = new JLabel("Use authentification");
            ComponentUtil.setFixedHeight(authLabel, 24);
            loginLabel = new JLabel("Login");
            ComponentUtil.setFixedHeight(loginLabel, 24);
            passwordLabel = new JLabel("Password");
            ComponentUtil.setFixedHeight(passwordLabel, 24);

            labelPanel.add(nameLabel);
            labelPanel.add(Box.createVerticalStrut(4));
            labelPanel.add(locationLabel);
            labelPanel.add(Box.createVerticalStrut(4));
            labelPanel.add(authLabel);
            labelPanel.add(Box.createVerticalStrut(4));
            labelPanel.add(loginLabel);
            labelPanel.add(Box.createVerticalStrut(4));
            labelPanel.add(passwordLabel);
            labelPanel.add(Box.createVerticalGlue());

            final JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.PAGE_AXIS));

            fieldPanel.add(nameField);
            fieldPanel.add(Box.createVerticalStrut(4));
            fieldPanel.add(locationField);
            fieldPanel.add(Box.createVerticalStrut(4));
            fieldPanel.add(authCheckBox);
            fieldPanel.add(Box.createVerticalStrut(4));
            fieldPanel.add(loginField);
            fieldPanel.add(Box.createVerticalStrut(4));
            fieldPanel.add(passwordField);
            fieldPanel.add(Box.createVerticalGlue());

            mainPanel.add(labelPanel, BorderLayout.WEST);
            mainPanel.add(fieldPanel, BorderLayout.CENTER);

            updateAuthFields();

            pack();
            ComponentUtil.center(this);
            setVisible(true);
        }

        void updateAuthFields()
        {
            final boolean enabled = authCheckBox.isSelected();

            loginLabel.setEnabled(enabled);
            loginField.setEnabled(enabled);
            passwordLabel.setEnabled(enabled);
            passwordField.setEnabled(enabled);
        }

        /**
         * @return the canceled
         */
        public boolean isCanceled()
        {
            return canceled;
        }
    }

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

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
             */
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

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(120);
        col.setMinWidth(80);

        col = colModel.getColumn(1);
        col.setPreferredWidth(280);
        col.setMinWidth(140);

        col = colModel.getColumn(2);
        col.setPreferredWidth(80);
        col.setMinWidth(60);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

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
        RepositoryPreferences.setRepositeries(repositories);

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

        if (!new RepositoryDialog("Add a new repository", reposInf).isCanceled())
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

        if (!new RepositoryDialog("Edit repository", reposInf).isCanceled())
        {
            // notify data changed
            tableModel.fireTableRowsUpdated(ind, ind);

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
