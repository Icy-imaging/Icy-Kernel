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

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
public class RoisPanel extends JPanel implements ActiveSequenceListener, TextChangeListener, ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    static final String[] columnNames = {"Name", "Type"};

    List<ROI> rois;

    // GUI
    final AbstractTableModel tableModel;
    final ListSelectionModel tableSelectionModel;
    final JTable table;

    final IcyTextField nameFilter;

    final RoiControlPanel roiControlPanel;

    // internals
    final Semaphore modifySelection;
    // complete refresh of the table
    final Runnable tableDataRefresher;

    public RoisPanel(boolean showFilters, boolean showControl)
    {
        super();

        rois = new ArrayList<ROI>();
        modifySelection = new Semaphore(1);

        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataInternal();
            }
        };

        // need filter before load()
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter ROI on name");
        nameFilter.addTextChangeListener(this);

        // build control panel
        roiControlPanel = new RoiControlPanel(this);

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
                return rois.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                // substance occasionally do not check size before getting value
                if (row >= rois.size())
                    return "";

                final ROI roi = rois.get(row);

                switch (column)
                {
                    case 0:
                        return roi.getName();

                    case 1:
                        return roi.getSimpleClassName();
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
                return String.class;
            }
        };

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(140);
        col.setMinWidth(60);

        col = colModel.getColumn(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);

        table.setRowHeight(24);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        if (showFilters)
        {
            topPanel.add(nameFilter);
            topPanel.add(Box.createVerticalStrut(4));
        }

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        middlePanel.add(table.getTableHeader());
        final JScrollPane sc = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        middlePanel.add(sc);

        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        if (showControl)
            add(roiControlPanel, BorderLayout.SOUTH);

        validate();

        refreshRois();
    }

    private Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    /**
     * refresh ROI list (and refresh table data according)
     */
    protected void refreshRois()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
        {
            final List<ROI> newRois = filterList(sequence.getROIs(), nameFilter.getText());

            final int newSize = newRois.size();
            final int oldSize = rois.size();

            // easy optimization
            if ((newSize == 0) && (oldSize == 0))
                return;

            // same size
            if (newSize == oldSize)
            {
                // same values, don't need to update it
                if (new HashSet<ROI>(newRois).containsAll(rois))
                    return;
            }

            // update ROI list
            rois = newRois;
        }
        else
        {
            // no change --> exit
            if (rois.isEmpty())
                return;

            // clear ROI list
            rois.clear();
        }

        // refresh whole table
        refreshTableData();
    }

    /**
     * Return index of specified ROI in the ROI list
     */
    protected int getRoiIndex(ROI roi)
    {
        return rois.indexOf(roi);
    }

    /**
     * Return index of specified ROI in the model
     */
    protected int getRoiModelIndex(ROI roi)
    {
        return getRoiIndex(roi);
    }

    /**
     * Return index of specified ROI in the table
     */
    protected int getRoiTableIndex(ROI roi)
    {
        final int ind = getRoiModelIndex(roi);

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

    // public ROI getFirstSelectedRoi()
    // {
    // int index = table.getSelectedRow();
    //
    // if (index != -1)
    // {
    // try
    // {
    // index = table.convertRowIndexToModel(index);
    // }
    // catch (IndexOutOfBoundsException e)
    // {
    // // ignore
    // }
    //
    // if ((index >= 0) || (index < rois.size()))
    // return rois.get(index);
    // }
    //
    // return null;
    // }

    public List<ROI> getSelectedRois()
    {
        // selected ROI are stored in the control panel
        return roiControlPanel.getSelectedROI();
    }

    protected List<ROI> getInternalSelectedRois()
    {
        final List<ROI> result = new ArrayList<ROI>();

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

            if ((index >= 0) && (index < rois.size()))
                result.add(rois.get(index));
        }

        return result;
    }

    protected void setSelectedRoisInternal(List<ROI> newSelected)
    {
        tableSelectionModel.setValueIsAdjusting(true);
        modifySelection.acquireUninterruptibly();
        try
        {
            tableSelectionModel.clearSelection();

            for (ROI roi : newSelected)
            {
                final int index = getRoiTableIndex(roi);
                // final int index = getRoiModelIndex(roi);

                if (index > -1)
                    tableSelectionModel.addSelectionInterval(index, index);
            }
        }
        finally
        {
            modifySelection.release();
            tableSelectionModel.setValueIsAdjusting(false);
        }
    }

    public void setSelectedRois(List<ROI> newSelected)
    {
        setSelectedRois((newSelected == null) ? new ArrayList<ROI>() : newSelected, getSelectedRois());
    }

    protected void setSelectedRois(List<ROI> newSelected, List<ROI> oldSelected)
    {
        final int newSelectedSize = newSelected.size();
        final int oldSelectedSize = oldSelected.size();

        // easy optimization
        if ((newSelectedSize == 0) && (oldSelectedSize == 0))
            return;

        // same selection size ?
        if (newSelectedSize == oldSelectedSize)
        {
            // same selection, don't need to update it
            if (new HashSet<ROI>(newSelected).containsAll(oldSelected))
                return;
        }

        // at this point selection has changed
        setSelectedRoisInternal(newSelected);
        // selection changed
        selectionChanged(newSelected);
    }

    protected List<ROI> filterList(List<ROI> list, String filter)
    {
        final List<ROI> result = new ArrayList<ROI>();

        if (StringUtil.isEmpty(filter, true))
            result.addAll(list);
        else
        {
            final String text = filter.trim().toLowerCase();

            // filter on name
            for (ROI roi : list)
                if (roi.getName().toLowerCase().indexOf(text) != -1)
                    result.add(roi);
        }

        return result;
    }

    public void refreshTableData()
    {
        ThreadUtil.bgRunSingle(tableDataRefresher, true);
    }

    void refreshTableDataInternal()
    {
        // this actually clear the table selection
        modifySelection.acquireUninterruptibly();
        try
        {
            tableModel.fireTableDataChanged();
        }
        finally
        {
            modifySelection.release();
        }

        Sequence sequence = getSequence();

        // set selection from sequence
        if (sequence != null)
            setSelectedRois(sequence.getSelectedROIs(), new ArrayList<ROI>());
    }

    // protected void refreshTableRow(final ROI roi)
    // {
    // isSelectionAdjusting = true;
    // try
    // {
    // final int rowIndex = getRoiModelIndex(roi);
    //
    // tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
    // }
    // finally
    // {
    // isSelectionAdjusting = false;
    // }
    //
    // // restore selected roi
    // if (sequence != null)
    // setSelectedRoisInternal(sequence.getSelectedROIs());
    // else
    // setSelectedRoisInternal(null);
    //
    // // refresh control panel
    // refreshControlPanel();
    // }

    /**
     * called when selection has changed
     */
    protected void selectionChanged(List<ROI> selectedRois)
    {
        final Sequence sequence = getSequence();

        // update selected ROI in sequence
        if (sequence != null)
        {
            modifySelection.acquireUninterruptibly();
            try
            {
                sequence.setSelectedROIs(selectedRois);
            }
            finally
            {
                modifySelection.release();
            }
        }

        // notify the ROI control panel that selection changed
        roiControlPanel.setSelectedRois(selectedRois);
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (source == nameFilter)
            refreshRois();
    }

    // called when selection changed in the ROI table
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting())
            return;
        // we are modifying elsewhere
        if (modifySelection.availablePermits() == 0)
            return;

        selectionChanged(getInternalSelectedRois());
    }

    @Override
    public void sequenceActivated(Sequence value)
    {
        // refresh ROI list
        refreshRois();
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        // we are modifying externally
        if (modifySelection.availablePermits() == 0)
            return;

        if (event.getSourceType() == SequenceEventSourceType.SEQUENCE_ROI)
        {
            if (event.getType() == SequenceEventType.CHANGED)
                // refresh table data
                refreshTableData();
            else
                // refresh the ROI list
                refreshRois();
        }
    }
}
