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
package icy.gui.inspector;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.swimmingPool.SwimmingObject;
import icy.swimmingPool.SwimmingPool;
import icy.swimmingPool.SwimmingPoolEvent;
import icy.swimmingPool.SwimmingPoolListener;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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
public class SwimmingPoolPanel extends JPanel implements TextChangeListener, ListSelectionListener,
        SwimmingPoolListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1565643301342039659L;

    static final String[] columnNames = {"", "Name", "Type"};

    final SwimmingPool swimmingPool;
    ArrayList<SwimmingObject> objects;

    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox objectType;
    final JPanel objectTypePanel;
    final IcyTextField nameFilter;
    final JButton refreshButton;
    final JButton deleteAllButton;
    final JButton deleteButton;

    public SwimmingPoolPanel(boolean showTypeFilter, boolean showNameFilter, boolean showButtons)
    {
        super();

        swimmingPool = Icy.getMainInterface().getSwimmingPool();
        if (swimmingPool != null)
            swimmingPool.addListener(this);

        objects = new ArrayList<SwimmingObject>();

        // GUI

        objectType = new JComboBox(new DefaultComboBoxModel());
        objectType.setToolTipText("Select type to display");
        objectType.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (objectType.getSelectedIndex() != -1)
                {
                    refreshObjects();
                    refreshTableData();
                }
            }
        });

        objectTypePanel = new JPanel();
        objectTypePanel.setLayout(new BoxLayout(objectTypePanel, BoxLayout.PAGE_AXIS));
        objectTypePanel.setVisible(showTypeFilter);

        final JPanel internalRepPanel = new JPanel();
        internalRepPanel.setLayout(new BoxLayout(internalRepPanel, BoxLayout.LINE_AXIS));

        internalRepPanel.add(new JLabel("Object type :"));
        internalRepPanel.add(Box.createHorizontalStrut(8));
        internalRepPanel.add(objectType);
        internalRepPanel.add(Box.createHorizontalGlue());

        objectTypePanel.add(internalRepPanel);
        objectTypePanel.add(Box.createVerticalStrut(8));

        // need filter before load()
        nameFilter = new IcyTextField();
        nameFilter.addTextChangeListener(this);
        nameFilter.setVisible(showNameFilter);

        // build buttons panel

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // refresh list
                refreshObjectTypeList();
                refreshObjects();
            }
        });

        deleteAllButton = new JButton("Delete all");
        deleteAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // delete all objects
                swimmingPool.removeAll();
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // delete selected objects
                for (SwimmingObject so : getSelectedObjects())
                    swimmingPool.remove(so);
            }
        });

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.setVisible(showButtons);

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createHorizontalStrut(64));
        buttonsPanel.add(deleteAllButton);
        buttonsPanel.add(Box.createHorizontalStrut(8));
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createHorizontalGlue());

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
                return objects.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                final SwimmingObject so = objects.get(row);

                switch (column)
                {
                    case 0:
                        return ResourceUtil.scaleIcon(so.getIcon(), 24);

                    case 1:
                        return so.getName();

                    case 2:
                        return so.getObjectSimpleClassName();
                }

                return "";
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                if (columnIndex == 0)
                    return ImageIcon.class;

                return String.class;
            }
        };

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(32);
        col.setMinWidth(32);
        col.setResizable(false);

        col = colModel.getColumn(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);

        col = colModel.getColumn(2);
        col.setPreferredWidth(80);
        col.setMinWidth(40);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowHeight(24);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(Box.createVerticalStrut(2));
        if (showTypeFilter)
            add(objectTypePanel);
        if (showNameFilter)
            add(nameFilter);
        if (showTypeFilter || showNameFilter)
            add(Box.createVerticalStrut(8));
        add(table.getTableHeader());
        add(new JScrollPane(table));
        if (showButtons)
            add(buttonsPanel);

        validate();

        refreshObjectTypeList();
        refreshObjects();
    }

    public void setTypeFilter(String type)
    {
        objectType.setSelectedItem(type);
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    protected void refreshObjects()
    {
        if (swimmingPool != null)
            objects = filterList(swimmingPool.getObjects(), nameFilter.getText());
        else
            objects.clear();
    }

    protected int getObjectIndex(SwimmingObject object)
    {
        return objects.indexOf(object);
    }

    protected int getObjectModelIndex(SwimmingObject object)
    {
        return getObjectIndex(object);
    }

    protected int getObjectTableIndex(SwimmingObject object)
    {
        final int ind = getObjectModelIndex(object);

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

    public ArrayList<SwimmingObject> getSelectedObjects()
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        for (int rowIndex : table.getSelectedRows())
        {
            int index = -1;

            if (rowIndex != -1)
            {
                try
                {
                    index = table.convertRowIndexToModel(rowIndex);
                }
                catch (IndexOutOfBoundsException e)
                {
                    // ignore
                }
            }

            if ((index >= 0) || (index < objects.size()))
                result.add(objects.get(index));
        }

        return result;
    }

    public void setSelectedObjects(ArrayList<SwimmingObject> sos)
    {
        table.clearSelection();

        for (SwimmingObject so : sos)
        {
            final int index = getObjectTableIndex(so);

            if (index > -1)
                table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    protected void refreshObjectTypeList()
    {
        final DefaultComboBoxModel model = (DefaultComboBoxModel) objectType.getModel();
        final Object savedItem = model.getSelectedItem();

        model.removeAllElements();
        model.addElement("ALL");

        if (swimmingPool != null)
        {
            for (String type : SwimmingObject.getObjectTypes(swimmingPool.getObjects()))
                model.addElement(type);
        }

        if (savedItem != null)
            model.setSelectedItem(savedItem);
        else
            objectType.setSelectedIndex(0);
    }

    private ArrayList<SwimmingObject> filterList(ArrayList<SwimmingObject> list, String nameFilterText)
    {
        final ArrayList<SwimmingObject> result = new ArrayList<SwimmingObject>();

        final boolean typeEmpty = objectType.getSelectedIndex() == 0;
        final boolean nameEmpty = StringUtil.isEmpty(nameFilterText, true);
        final String typeFilter;
        final String nameFilterUp;

        if (!typeEmpty)
            typeFilter = objectType.getSelectedItem().toString();
        else
            typeFilter = "";
        if (!nameEmpty)
            nameFilterUp = nameFilterText.toUpperCase();
        else
            nameFilterUp = "";

        for (SwimmingObject so : list)
        {
            // search in name and type
            if ((typeEmpty || so.getObjectSimpleClassName().equals(typeFilter))
                    && (nameEmpty || (so.getName().indexOf(nameFilterUp) != -1)))
                result.add(so);
        }

        return result;
    }

    protected void refreshTableData()
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final ArrayList<SwimmingObject> sos = getSelectedObjects();

                tableModel.fireTableDataChanged();

                // restore previous selected objects if possible
                setSelectedObjects(sos);
            }
        });
    }

    protected void refreshButtonsPanel()
    {
        deleteButton.setEnabled(getSelectedObjects().size() > 0);
        deleteAllButton.setEnabled(objects.size() > 0);
    }

    protected void pluginsChanged()
    {
        refreshObjects();
        refreshTableData();
        refreshButtonsPanel();
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        pluginsChanged();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        refreshButtonsPanel();

        // TODO : send event to notify selection change
    }

    @Override
    public void swimmingPoolChangeEvent(SwimmingPoolEvent swimmingPoolEvent)
    {
        refreshObjectTypeList();
        refreshObjects();
    }
}
