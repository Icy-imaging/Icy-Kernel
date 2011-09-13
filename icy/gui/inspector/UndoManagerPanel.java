/**
 * 
 */
package icy.gui.inspector;

import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.undo.IcyUndoManager;
import icy.undo.IcyUndoManagerListener;
import icy.undo.IcyUndoableEdit;

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
public class UndoManagerPanel extends JPanel implements ListSelectionListener, IcyUndoManagerListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 1464754827529975860L;

    static final String[] columnNames = {"", "Action"};

    protected IcyUndoManager undoManager;

    // GUI
    final AbstractTableModel tableModel;
    final ListSelectionModel tableSelectionModel;
    final JTable table;

    // internals
    boolean isSelectionAdjusting;

    public UndoManagerPanel()
    {
        super();

        undoManager = null;
        isSelectionAdjusting = false;

        // don't care about releasing the listener here
        Icy.getMainInterface().addListener(new MainAdapter()
        {
            @Override
            public void sequenceFocused(MainEvent event)
            {
                final Sequence seq = (Sequence) event.getSource();

                if (seq == null)
                    setUndoManager(null);
                else
                    setUndoManager(seq.getUndoManager());
            }
        });

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
                    final IcyUndoableEdit edit = undoManager.getSignificantEdit(row - 1);

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
        // if (showControl)
        // add(controlPanel, BorderLayout.SOUTH);

        validate();

        refreshTableData();
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
    // protected int getEditIndex(IcyUndoableEdit edit)
    // {
    // if (undoManager != null)
    // return undoManager.getSignificantIndex(edit);
    //
    // return -1;
    // }

    public IcyUndoableEdit getLastSelectedEdit()
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
            final IcyUndoableEdit selectedEdit = getLastSelectedEdit();

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
}
