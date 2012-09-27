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
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.inspector.InspectorPanel.InspectorSubPanel;
import icy.gui.util.ComponentUtil;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.ThreadUtil;
import icy.util.ShapeUtil.ShapeOperation;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
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
public class RoisPanel extends InspectorSubPanel implements TextChangeListener, ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    static final String[] columnNames = {"Name", "Type"};

    ArrayList<ROI> rois;
    Sequence sequence;

    // GUI
    final AbstractTableModel tableModel;
    final ListSelectionModel tableSelectionModel;
    final JTable table;

    final JComboBox roiType;
    final IcyTextField nameFilter;
    private final JPanel filtersPanel;

    final IcyTextField nameField;
    final ColorChooserButton colorButton;
    final IcyButton orButton;
    final IcyButton andButton;
    final IcyButton xorButton;
    final IcyButton subButton;
    final IcyButton deleteButton;
    // final IcyButton copyButton;
    final JPanel controlPanel;

    // internals
    boolean isSelectionAdjusting;
    boolean isRoiTypeAdjusting;
    boolean isRoiPropertiesAdjusting;

    final Runnable tableDataRefresher;
    final Runnable roiTypeListRefresher;
    final Runnable controlPanelRefresher;

    public RoisPanel(boolean showFilters, boolean showControl)
    {
        super();

        rois = new ArrayList<ROI>();
        sequence = null;
        isSelectionAdjusting = false;
        isRoiTypeAdjusting = false;
        isRoiPropertiesAdjusting = false;

        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableData();
            }
        };
        roiTypeListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshRoiTypeList();
            }
        };
        controlPanelRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshControlPanel();
            }
        };

        roiType = new JComboBox(new DefaultComboBoxModel());
        roiType.setToolTipText("Select ROI type to display");
        roiType.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (isRoiTypeAdjusting)
                    return;

                if (roiType.getSelectedItem() != null)
                {
                    refreshRois();
                }
            }
        });

        // need filter before load()
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter ROI on name");
        nameFilter.addTextChangeListener(this);

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.LINE_AXIS));
        filtersPanel.setVisible(showFilters);

        filtersPanel.add(roiType);
        filtersPanel.add(Box.createHorizontalStrut(4));
        filtersPanel.add(nameFilter);

        // build control panel
        nameField = new IcyTextField();
        nameField.setToolTipText("Edit name of selected ROI(s)");
        nameField.addTextChangeListener(this);

        colorButton = new ColorChooserButton();
        ComponentUtil.setFixedHeight(colorButton, 22);
        colorButton.setToolTipText("Change color of selected ROI(s)");
        colorButton.addColorChangeListener(new ColorChangeListener()
        {
            @Override
            public void colorChanged(ColorChooserButton source)
            {
                if (isRoiPropertiesAdjusting)
                    return;

                if (colorButton.isEnabled())
                {
                    final Color color = source.getColor();

                    sequence.beginUpdate();
                    try
                    {
                        for (ROI roi : getSelectedRois())
                            if (roi.isEditable())
                                roi.setColor(color);
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }
                }
            }
        });

        // copyButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DOCCOPY));
        // copyButton.setFlat(true);
        // copyButton.setToolTipText("Duplicate selected ROI(s)");
        // copyButton.addActionListener(new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // sequence.beginUpdate();
        // try
        // {
        // // clone selected rois
        // for (ROI roi : getSelectedRois())
        // sequence.addROI(roi.getCopy());
        // }
        // finally
        // {
        // sequence.endUpdate();
        // }
        // }
        // });
        //
        orButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_OR));
        orButton.setFlat(true);
        orButton.setToolTipText("Create a new ROI representing the union of selected ROIs");
        orButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.OR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        andButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_AND));
        andButton.setFlat(true);
        andButton.setToolTipText("Create a new ROI representing the intersection of selected ROIs");
        andButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.AND);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        xorButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_XOR));
        xorButton.setFlat(true);
        xorButton.setToolTipText("Create a new ROI representing the exclusive union of selected ROIs");
        xorButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    final ROI mergeROI = ROI2D.merge(selectedROI2D, ShapeOperation.XOR);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        subButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_ROI_SUB));
        subButton.setFlat(true);
        subButton.setToolTipText("Create a new ROI from the result of first ROI minus the second ROI");
        subButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // OR operation
                sequence.beginUpdate();
                try
                {
                    final ArrayList<ROI> selectedROI = getSelectedRois();
                    // only ROI2D supported now
                    final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new ROI[selectedROI.size()]));

                    // Subtraction work only when 2 ROI are selected
                    if (selectedROI2D.length != 2)
                        return;

                    final ROI mergeROI = ROI2D.subtract(selectedROI2D[0], selectedROI2D[1]);

                    sequence.addROI(mergeROI);
                    sequence.setSelectedROI(mergeROI, true);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        deleteButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DELETE));
        deleteButton.setFlat(true);
        deleteButton.setToolTipText("Delete selected ROI(s)");
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sequence.beginUpdate();
                try
                {
                    // delete selected rois
                    for (ROI roi : getSelectedRois())
                        if (roi.isEditable())
                            sequence.removeROI(roi);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        });

        controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));

        controlPanel.add(nameField);
        controlPanel.add(Box.createHorizontalStrut(8));
        controlPanel.add(colorButton);
        controlPanel.add(Box.createHorizontalStrut(8));
        // controlPanel.add(copyButton);
        // controlPanel.add(Box.createHorizontalStrut(4));
        controlPanel.add(orButton);
        controlPanel.add(andButton);
        controlPanel.add(xorButton);
        controlPanel.add(subButton);
        controlPanel.add(Box.createHorizontalStrut(8));
        controlPanel.add(deleteButton);
        controlPanel.add(Box.createHorizontalGlue());

        controlPanel.setVisible(showControl);

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
            topPanel.add(filtersPanel);
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
            add(controlPanel, BorderLayout.SOUTH);

        validate();

        refreshRoiTypeList();
    }

    // /**
    // * show popup menu
    // */
    // void showROIMergeMenu(Component comp)
    // {
    // final JPopupMenu menu = new JPopupMenu();
    //
    // final JMenuItem orMergeItem = new JMenuItem("union");
    // final JMenuItem andMergeItem = new JMenuItem("intersection");
    // final JMenuItem xorMergeItem = new JMenuItem("exclusive union");
    //
    // menu.add(orMergeItem);
    // menu.add(andMergeItem);
    // menu.add(xorMergeItem);
    //
    // menu.pack();
    // menu.validate();
    //
    // final ActionListener actionListener = new ActionListener()
    // {
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // final Object src = e.getSource();
    // final ShapeOperation op;
    //
    // // OR operation
    // if (src == orMergeItem)
    // op = ShapeOperation.OR;
    // // AND operation
    // else if (src == andMergeItem)
    // op = ShapeOperation.AND;
    // // XOR operation
    // else if (src == xorMergeItem)
    // op = ShapeOperation.XOR;
    // else
    // return;
    //
    // sequence.beginUpdate();
    // try
    // {
    // final ArrayList<ROI> selectedROI = getSelectedRois();
    // // only ROI2D supported now
    // final ROI2D[] selectedROI2D = ROI2D.getROI2DList(selectedROI.toArray(new
    // ROI[selectedROI.size()]));
    //
    // final ROI mergeROI = ROI2D.merge(selectedROI2D, op);
    //
    // sequence.addROI(mergeROI);
    // sequence.setSelectedROI(mergeROI, true);
    // }
    // finally
    // {
    // sequence.endUpdate();
    // }
    // }
    // };
    //
    // orMergeItem.addActionListener(actionListener);
    // andMergeItem.addActionListener(actionListener);
    // xorMergeItem.addActionListener(actionListener);
    //
    // // display menu
    // menu.show(comp, 0, comp.getHeight());
    // }

    public void setTypeFilter(String type)
    {
        roiType.setSelectedItem(type);
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
        if (sequence != null)
            rois = filterList(sequence.getROIs(), nameFilter.getText());
        else
            rois.clear();

        // refresh table data
        ThreadUtil.bgRunSingle(tableDataRefresher, true);
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

    /**
     * Return list of different ROI type (ROI class name) from the specified ROI list
     */
    private ArrayList<String> getRoiTypes(ArrayList<ROI> rois)
    {
        final ArrayList<String> result = new ArrayList<String>();

        for (ROI roi : rois)
        {
            final String type = roi.getSimpleClassName();

            if (!result.contains(type))
                result.add(type);
        }

        return result;
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

    public ArrayList<ROI> getSelectedRois()
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

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

    void setSelectedRoisInternal(ArrayList<ROI> newSelected)
    {
        isSelectionAdjusting = true;
        try
        {
            table.clearSelection();

            if (newSelected != null)
            {
                for (ROI roi : newSelected)
                {
                    final int index = getRoiTableIndex(roi);
                    // final int index = getRoiModelIndex(roi);

                    if (index > -1)
                        tableSelectionModel.addSelectionInterval(index, index);
                }
            }
        }
        finally
        {
            isSelectionAdjusting = false;
        }
    }

    void setSelectedRois(ArrayList<ROI> newSelected, ArrayList<ROI> oldSelected)
    {
        final int newSelectedSize;
        final int oldSelectedSize;

        if (newSelected == null)
            newSelectedSize = 0;
        else
            newSelectedSize = newSelected.size();
        if (oldSelected == null)
            oldSelectedSize = 0;
        else
            oldSelectedSize = oldSelected.size();

        // easy optimisation
        if ((newSelectedSize == 0) && (oldSelectedSize == 0))
            return;

        // same selection, don't need to udpate it
        if ((newSelectedSize == oldSelectedSize) && newSelected.containsAll(oldSelected))
            return;

        // at this point selection has changed !
        setSelectedRoisInternal(newSelected);
        // selection changed
        selectionChanged();
    }

    public void setSelectedRois(ArrayList<ROI> values)
    {
        setSelectedRois(values, getSelectedRois());
    }

    protected void refreshRoiTypeList()
    {
        final DefaultComboBoxModel model = (DefaultComboBoxModel) roiType.getModel();
        final Object savedItem = model.getSelectedItem();

        isRoiTypeAdjusting = true;
        try
        {
            model.removeAllElements();
            model.addElement("ALL");

            if (sequence != null)
            {
                for (String type : getRoiTypes(sequence.getROIs()))
                    model.addElement(type);
            }
        }
        finally
        {
            isRoiTypeAdjusting = false;
        }

        final int index;

        if (savedItem != null)
            index = model.getIndexOf(savedItem);
        else
            index = -1;

        if (index != -1)
            roiType.setSelectedItem(savedItem);
        else
            roiType.setSelectedIndex(0);
    }

    ArrayList<ROI> filterList(ArrayList<ROI> list, String nameFilterText)
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

        final boolean typeEmpty = roiType.getSelectedIndex() == 0;
        final boolean nameEmpty = StringUtil.isEmpty(nameFilterText, true);
        final String typeFilter;
        final String nameFilterUp;

        if (!typeEmpty)
            typeFilter = roiType.getSelectedItem().toString();
        else
            typeFilter = "";
        if (!nameEmpty)
            nameFilterUp = nameFilterText.trim().toLowerCase();
        else
            nameFilterUp = "";

        for (ROI roi : list)
        {
            // search in name and type
            if ((typeEmpty || roi.getSimpleClassName().equals(typeFilter))
                    && (nameEmpty || (roi.getName().toLowerCase().indexOf(nameFilterUp) != -1)))
                result.add(roi);
        }

        return result;
    }

    protected void refreshTableData()
    {
        isSelectionAdjusting = true;
        try
        {
            tableModel.fireTableDataChanged();
        }
        finally
        {
            isSelectionAdjusting = false;
        }

        // restore selected roi
        if (sequence != null)
            setSelectedRoisInternal(sequence.getSelectedROIs());
        else
            setSelectedRoisInternal(null);

        // refresh control panel
        ThreadUtil.bgRunSingle(controlPanelRefresher, true);
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

    protected void refreshControlPanel()
    {
        isRoiPropertiesAdjusting = true;
        try
        {
            if (sequence != null)
            {
                final ArrayList<ROI> selectedRois = getSelectedRois();

                boolean editable = false;
                for (ROI roi : selectedRois)
                    editable |= roi.isEditable();

                // final boolean singleSelect = (selectedRois.size() == 1);
                final boolean hasSelected = (selectedRois.size() > 0);
                final boolean twoSelected = (selectedRois.size() == 2);
                final boolean severalsSelected = (selectedRois.size() > 1);

                nameField.setEnabled(hasSelected && editable);
                colorButton.setEnabled(hasSelected && editable);
                // copyButton.setEnabled(hasSelected);
                orButton.setEnabled(severalsSelected);
                andButton.setEnabled(severalsSelected);
                xorButton.setEnabled(severalsSelected);
                subButton.setEnabled(twoSelected);
                deleteButton.setEnabled(hasSelected && editable);

                if (hasSelected)
                {
                    final ROI roi = selectedRois.get(0);
                    final String name = roi.getName();

                    // handle it manually as setText doesn't check for equality
                    if (!name.equals(nameField.getText()))
                        nameField.setText(name);
                    colorButton.setColor(roi.getColor());
                }
                else
                    nameField.setText("");
            }
            else
            {
                nameField.setEnabled(false);
                colorButton.setEnabled(false);
                // copyButton.setEnabled(false);
                orButton.setEnabled(false);
                andButton.setEnabled(false);
                xorButton.setEnabled(false);
                subButton.setEnabled(false);
                deleteButton.setEnabled(false);
                nameField.setText("");
            }
        }
        finally
        {
            isRoiPropertiesAdjusting = false;
        }
    }

    /**
     * called when selection has changed
     */
    protected void selectionChanged()
    {
        if (sequence != null)
            sequence.setSelectedROIs(getSelectedRois());

        // refresh control panel
        ThreadUtil.bgRunSingle(controlPanelRefresher, true);
    }

    @Override
    public void textChanged(IcyTextField source)
    {
        if (source == nameField)
        {
            if (isRoiPropertiesAdjusting)
                return;

            if (nameField.isEnabled())
            {
                final String name = source.getText();

                sequence.beginUpdate();
                try
                {
                    for (ROI roi : getSelectedRois())
                        if (roi.isEditable())
                            roi.setName(name);
                }
                finally
                {
                    sequence.endUpdate();
                }
            }
        }

        if (source == nameFilter)
            refreshRois();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (isSelectionAdjusting || e.getValueIsAdjusting())
            return;

        selectionChanged();
    }

    @Override
    public void focusChanged(Viewer viewer)
    {

    }

    @Override
    public void focusedViewerChanged(ViewerEvent event)
    {

    }

    @Override
    public void focusChanged(Sequence value)
    {
        if (sequence != value)
        {
            sequence = value;

            // refresh ROI type list
            ThreadUtil.bgRunSingle(roiTypeListRefresher);
        }
    }

    @Override
    public void focusedSequenceChanged(SequenceEvent event)
    {
        if (event.getSourceType() == SequenceEventSourceType.SEQUENCE_ROI)
        {
            if (event.getType() == SequenceEventType.CHANGED)
                ThreadUtil.bgRunSingle(tableDataRefresher, true);
            else
                ThreadUtil.bgRunSingle(roiTypeListRefresher);
        }
    }

}
