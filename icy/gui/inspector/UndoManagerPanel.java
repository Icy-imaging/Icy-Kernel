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
package icy.gui.inspector;

import icy.gui.main.ActiveSequenceListener;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import icy.undo.IcyUndoManager;
import icy.undo.IcyUndoManagerListener;
import icy.undo.AbstractIcyUndoableEdit;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author Stephane
 */
public class UndoManagerPanel extends JPanel implements ActiveSequenceListener, ListSelectionListener,
        IcyUndoManagerListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1464754827529975860L;

    static final String[] columnNames = {"", "Action"};

    protected IcyUndoManager undoManager;

    // GUI
    AbstractTableModel tableModel;
    ListSelectionModel tableSelectionModel;
    JTable table;

    // internals
    boolean isSelectionAdjusting;

    public UndoManagerPanel()
    {
        super();

        undoManager = null;
        isSelectionAdjusting = false;

        initialize();

        refreshTableData();
    }

    private void initialize()
    {
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
                if (undoManager != null)
                    return undoManager.getSignificantEditsCount() + 1;

                return 1;
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                if (row == 0)
                {
                    if (column == 0)
                        return null;

                    if (undoManager != null)
                        return "Initial state";

                    return "No manager";
                }

                if (undoManager != null)
                {
                    final AbstractIcyUndoableEdit edit = undoManager.getSignificantEdit(row - 1);

                    switch (column)
                    {
                        case 0:
                            return edit.getIcon();

                        case 1:
                            return edit.getPresentationName();
                    }
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
                    return Icon.class;

                return String.class;
            }
        };

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(20);
        col.setMinWidth(20);
        col.setMaxWidth(20);

        col = colModel.getColumn(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);

        table.setRowHeight(20);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(true);
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        middlePanel.add(table.getTableHeader());
        final JScrollPane sc = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        middlePanel.add(sc);

        setLayout(new BorderLayout());

        add(middlePanel, BorderLayout.CENTER);
    }

    public void setUndoManager(IcyUndoManager value)
    {
        if (undoManager != value)
        {
            if (undoManager != null)
                undoManager.removeListener(this);

            undoManager = value;

            if (undoManager != null)
                undoManager.addListener(this);

            // refresh data
            refreshTableData();
        }
    }

    // /**
    // * Return index of specified Edit
    // */
    // protected int getEditIndex(AbstractIcyUndoableEdit edit)
    // {
    // if (undoManager != null)
    // return undoManager.getSignificantIndex(edit);
    //
    // return -1;
    // }

    public AbstractIcyUndoableEdit getLastSelectedEdit()
    {
        if (undoManager != null)
        {
            final int index = tableSelectionModel.getMaxSelectionIndex();

            if (index > 0)
                return undoManager.getSignificantEdit(index - 1);
        }

        return null;
    }

    protected void refreshTableData()
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                isSelectionAdjusting = true;
                try
                {
                    tableModel.fireTableDataChanged();

                    if (undoManager != null)
                        tableSelectionModel.setSelectionInterval(0, undoManager.getNextAddIndex());
                    else
                        tableSelectionModel.setSelectionInterval(0, 0);
                }
                finally
                {
                    isSelectionAdjusting = false;
                }
            }
        });
    }

    /**
     * called when selection has changed
     */
    protected void selectionChanged()
    {
        // process undo / redo operation
        if (undoManager != null)
        {
            final AbstractIcyUndoableEdit selectedEdit = getLastSelectedEdit();

            // first entry
            if (selectedEdit == null)
                undoManager.undoAll();
            else
                undoManager.undoOrRedoTo(selectedEdit);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() || isSelectionAdjusting)
            return;

        if (tableSelectionModel.getMinSelectionIndex() != 0)
            tableSelectionModel.setSelectionInterval(0, tableSelectionModel.getMaxSelectionIndex());
        else
            selectionChanged();
    }

    @Override
    public void undoManagerChanged(IcyUndoManager source)
    {
        refreshTableData();
    }

    @Override
    public void sequenceActivated(Sequence sequence)
    {
        if (sequence == null)
            setUndoManager(null);
        else
            setUndoManager(sequence.getUndoManager());
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        // nothing here
    }
}
