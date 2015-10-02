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
package icy.gui.inspector;

import icy.action.SequenceOperationActions;
import icy.gui.component.button.IcyButton;
import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import icy.undo.AbstractIcyUndoableEdit;
import icy.undo.IcyUndoManager;
import icy.undo.IcyUndoManagerListener;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
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
public class UndoManagerPanel extends JPanel implements ActiveSequenceListener, ListSelectionListener,
        IcyUndoManagerListener, ChangeListener
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
    IcyButton undoButton;
    IcyButton redoButton;
    JSpinner historySizeField;
    IcyButton clearAllButLastButton;
    IcyButton clearAllButton;
    final Runnable refresher;

    public UndoManagerPanel()
    {
        super();

        undoManager = null;
        isSelectionAdjusting = false;

        initialize();

        historySizeField.setValue(Integer.valueOf(GeneralPreferences.getHistorySize()));
        historySizeField.addChangeListener(this);

        refresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataAndActions();
            }
        };

        refresher.run();
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
                    return undoManager.getEditsCount() + 1;

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

                    return "No opened sequence";
                }

                if (undoManager != null)
                {
                    final AbstractIcyUndoableEdit edit = undoManager.getEdit(row - 1);

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
        table.setToolTipText("Click on an action to undo or redo until that point");

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
        sc.setToolTipText("");
        middlePanel.add(sc);

        final JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        undoButton = new IcyButton(SequenceOperationActions.undoAction);
        undoButton.setFlat(true);
        undoButton.setHideActionText(true);
        bottomPanel.add(undoButton);

        redoButton = new IcyButton(SequenceOperationActions.redoAction);
        redoButton.setFlat(true);
        redoButton.setHideActionText(true);
        bottomPanel.add(redoButton);

        Component horizontalGlue = Box.createHorizontalGlue();
        bottomPanel.add(horizontalGlue);

        JLabel lblNewLabel = new JLabel("History size");
        lblNewLabel.setToolTipText("");
        bottomPanel.add(lblNewLabel);

        Component horizontalStrut = Box.createHorizontalStrut(8);
        bottomPanel.add(horizontalStrut);

        historySizeField = new JSpinner();
        historySizeField.setModel(new SpinnerNumberModel(50, 1, 200, 1));
        historySizeField.setToolTipText("Maximum size of the history (lower value will reduce memory usage)");
        bottomPanel.add(historySizeField);

        Component horizontalStrut_2 = Box.createHorizontalStrut(8);
        bottomPanel.add(horizontalStrut_2);

        clearAllButLastButton = new IcyButton(SequenceOperationActions.undoClearAllButLastAction);
        clearAllButLastButton.setFlat(true);
        clearAllButLastButton.setHideActionText(true);
        bottomPanel.add(clearAllButLastButton);

        clearAllButton = new IcyButton(SequenceOperationActions.undoClearAction);
        clearAllButton.setFlat(true);
        clearAllButton.setHideActionText(true);
        bottomPanel.add(clearAllButton);

        setLayout(new BorderLayout());

        add(middlePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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

            // refresh data and actions
            ThreadUtil.bgRunSingle(refresher);
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

    protected void refreshTableDataAndActions()
    {
        ThreadUtil.invokeNow(new Runnable()
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

                if (undoManager != null)
                {
                    undoButton.setEnabled(undoManager.canUndo());
                    redoButton.setEnabled(undoManager.canRedo());
                    clearAllButLastButton.setEnabled(undoManager.canUndo());
                    clearAllButton.setEnabled(undoManager.canUndo() || undoManager.canRedo());
                }
                else
                {
                    undoButton.setEnabled(false);
                    redoButton.setEnabled(false);
                    clearAllButLastButton.setEnabled(false);
                    clearAllButton.setEnabled(false);
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
    public void stateChanged(ChangeEvent e)
    {
        final int value = ((Integer) historySizeField.getValue()).intValue();

        // change size of all current active undo manager
        for (Sequence sequence : Icy.getMainInterface().getSequences())
        {
            final IcyUndoManager um = sequence.getUndoManager();

            if (um != null)
                um.setLimit(value);
        }

        GeneralPreferences.setHistorySize(value);

        refreshTableDataAndActions();
    }

    @Override
    public void undoManagerChanged(IcyUndoManager source)
    {
        ThreadUtil.bgRunSingle(refresher);
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
