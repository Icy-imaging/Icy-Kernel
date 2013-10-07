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

import icy.canvas.CanvasLayerEvent;
import icy.canvas.CanvasLayerListener;
import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.component.editor.SliderCellEditor;
import icy.gui.component.editor.VisibleCellEditor;
import icy.gui.component.renderer.SliderCellRenderer;
import icy.gui.component.renderer.VisibleCellRenderer;
import icy.gui.main.ActiveViewerListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Stephane
 */
public class LayersPanel extends JPanel implements ActiveViewerListener, CanvasLayerListener, TextChangeListener,
        ListSelectionListener
{
    private class CanvasRefresher implements Runnable
    {
        IcyCanvas newCanvas;

        public CanvasRefresher()
        {
            super();
        }

        @Override
        public void run()
        {
            final IcyCanvas c = newCanvas;

            // change canvas
            if (canvas != c)
            {
                if (canvas != null)
                    canvas.removeLayerListener(LayersPanel.this);

                canvas = c;

                if (canvas != null)
                    canvas.addLayerListener(LayersPanel.this);
            }

            refreshLayersInternal();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4550426171735455449L;

    static final String[] columnNames = {"Name", "Opacity", ""};

    List<Layer> layers;
    IcyCanvas canvas;

    // GUI
    AbstractTableModel tableModel;
    ListSelectionModel tableSelectionModel;
    JXTable table;
    IcyTextField nameFilter;
    IcyTextField nameField;
    IcyButton deleteButton;

    // internals
    boolean isSelectionAdjusting;
    boolean isLayerEditing;
    boolean isLayerPropertiesAdjusting;

    final Runnable layersRefresher;
    final Runnable tableDataRefresher;
    final Runnable controlPanelRefresher;
    final CanvasRefresher canvasRefresher;

    public LayersPanel()
    {
        super();

        layers = new ArrayList<Layer>();
        canvas = null;
        isSelectionAdjusting = false;
        isLayerEditing = false;
        isLayerPropertiesAdjusting = false;

        layersRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshLayersInternal();
            }
        };
        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableData();
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
        canvasRefresher = new CanvasRefresher();

        // build GUI
        initialize();

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
                return layers.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                final Layer layer = layers.get(row);

                switch (column)
                {
                    case 0:
                        // layer name
                        return layer.getName();

                    case 1:
                        // layer transparency
                        return Integer.valueOf((int) (layer.getAlpha() * 1000f));

                    case 2:
                        // layer visibility
                        return Boolean.valueOf(layer.isVisible());

                    default:
                        return "";
                }
            }

            @Override
            public void setValueAt(Object value, int row, int column)
            {
                isLayerEditing = true;
                try
                {
                    final Layer layer = layers.get(row);

                    switch (column)
                    {
                        case 0:
                            layer.setName((String) value);
                            break;

                        case 1:
                            // layer transparency
                            layer.setAlpha(((Integer) value).intValue() / 1000f);
                            break;

                        case 2:
                            // layer visibility
                            layer.setVisible(((Boolean) value).booleanValue());
                            break;
                    }
                }
                finally
                {
                    isLayerEditing = false;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    default:
                    case 0:
                        // layer name
                        return String.class;

                    case 1:
                        // layer transparency
                        return Integer.class;

                    case 2:
                        // layer visibility
                        return Boolean.class;
                }
            }
        };
        // set table model
        table.setModel(tableModel);
        // alternate highlight
        table.addHighlighter(HighlighterFactory.createSimpleStriping());

        TableColumnExt col;

        // columns setting - name
        col = table.getColumnExt(0);
        col.setPreferredWidth(140);
        col.setMinWidth(60);
        col.setToolTipText("Layer name (double click to edit)");

        // columns setting - transparency
        col = table.getColumnExt(1);
        // slider doesn't like to be resized when they are used as CellRenderer
        col.setPreferredWidth(100);
        col.setMinWidth(100);
        col.setMaxWidth(100);
        col.setCellEditor(new SliderCellEditor(true));
        col.setCellRenderer(new SliderCellRenderer());
        col.setToolTipText("Change the layer opacity");

        // columns setting - visible
        col = table.getColumnExt(2);
        col.setPreferredWidth(20);
        col.setMinWidth(20);
        col.setMaxWidth(20);
        col.setCellEditor(new VisibleCellEditor(18));
        col.setCellRenderer(new VisibleCellRenderer(18));
        col.setToolTipText("Make the layer visible or not");

        // table selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        refreshLayers();
    }

    private void initialize()
    {
        // need filter before load()
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter Layer on name");
        nameFilter.addTextChangeListener(this);

        table = new JXTable();
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setColumnControlVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        middlePanel.add(table.getTableHeader());
        final JScrollPane sc = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        middlePanel.add(sc);

        JPanel controlPanel = new JPanel();
        GridBagLayout gbl_controlPanel = new GridBagLayout();
        gbl_controlPanel.columnWidths = new int[] {140, 0, 0};
        gbl_controlPanel.rowHeights = new int[] {20, 0};
        gbl_controlPanel.columnWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        gbl_controlPanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        controlPanel.setLayout(gbl_controlPanel);

        // build control panel
        nameField = new IcyTextField();
        nameField.setToolTipText("Edit name of selected Layer(s)");
        nameField.addTextChangeListener(this);

        GridBagConstraints gbc_nameField = new GridBagConstraints();
        gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameField.insets = new Insets(0, 0, 0, 5);
        gbc_nameField.gridx = 0;
        gbc_nameField.gridy = 0;
        controlPanel.add(nameField, gbc_nameField);

        setLayout(new BorderLayout());

        add(nameFilter, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        deleteButton = new IcyButton(new IcyIcon(ResourceUtil.ICON_DELETE));
        deleteButton.setFlat(true);
        deleteButton.setToolTipText("Delete selected Layer(s)");
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence sequence = canvas.getSequence();

                if (sequence != null)
                {
                    sequence.beginUpdate();
                    try
                    {
                        // delete selected layers
                        for (Layer layer : getSelectedLayers())
                            if (layer.getCanBeRemoved())
                                sequence.removeOverlay(layer.getOverlay());
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }
                }
            }
        });
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.fill = GridBagConstraints.BOTH;
        gbc_deleteButton.gridx = 1;
        gbc_deleteButton.gridy = 0;
        controlPanel.add(deleteButton, gbc_deleteButton);

        validate();
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    /**
     * refresh Layer list (and refresh table data according)
     */
    protected void refreshLayers()
    {
        ThreadUtil.bgRunSingle(layersRefresher);
    }

    /**
     * refresh layer list (internal)
     */
    void refreshLayersInternal()
    {
        if (canvas != null)
            layers = filterList(canvas.getLayers(), nameFilter.getText());
        else
            layers.clear();

        // refresh table data
        ThreadUtil.bgRunSingle(tableDataRefresher, true);
    }

    /**
     * Return index of specified Layer in the Layer list
     */
    protected int getLayerIndex(Layer layer)
    {
        return layers.indexOf(layer);
    }

    /**
     * Return index of specified Layer in the model
     */
    protected int getLayerModelIndex(Layer layer)
    {
        return getLayerIndex(layer);
    }

    /**
     * Return index of specified Layer in the table
     */
    protected int getLayerTableIndex(Layer layer)
    {
        final int ind = getLayerModelIndex(layer);

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

    // public Layer getFirstSelectedRoi()
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
    // if ((index >= 0) || (index < layers.size()))
    // return layers.get(index);
    // }
    //
    // return null;
    // }

    public ArrayList<Layer> getSelectedLayers()
    {
        final ArrayList<Layer> result = new ArrayList<Layer>();

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

            if ((index >= 0) && (index < layers.size()))
                result.add(layers.get(index));
        }

        return result;
    }

    void setSelectedLayersInternal(ArrayList<Layer> newSelected)
    {
        isSelectionAdjusting = true;
        try
        {
            table.clearSelection();

            if (newSelected != null)
            {
                for (Layer layer : newSelected)
                {
                    final int index = getLayerTableIndex(layer);
                    // final int index = getLayerModelIndex(layer);

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

    List<Layer> filterList(List<Layer> list, String nameFilterText)
    {
        final List<Layer> result = new ArrayList<Layer>();

        final boolean nameEmpty = StringUtil.isEmpty(nameFilterText, true);
        final String nameFilterUp;

        if (!nameEmpty)
            nameFilterUp = nameFilterText.trim().toLowerCase();
        else
            nameFilterUp = "";

        for (Layer layer : list)
        {
            // search in name and type
            if (nameEmpty || (layer.getName().toLowerCase().indexOf(nameFilterUp) != -1))
                result.add(layer);
        }

        return result;
    }

    protected void refreshTableData()
    {
        final ArrayList<Layer> save = getSelectedLayers();

        isSelectionAdjusting = true;
        try
        {
            tableModel.fireTableDataChanged();
        }
        finally
        {
            isSelectionAdjusting = false;
        }

        setSelectedLayersInternal(save);

        // refresh control panel
        ThreadUtil.bgRunSingle(controlPanelRefresher, true);
    }

    // protected void refreshTableRow(final Layer layer)
    // {
    // isSelectionAdjusting = true;
    // try
    // {
    // final int rowIndex = getLayerModelIndex(layer);
    //
    // tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
    // }
    // finally
    // {
    // isSelectionAdjusting = false;
    // }
    //
    // // restore selected layer
    // if (sequence != null)
    // setSelectedLayersInternal(sequence.getSelectedROIs());
    // else
    // setSelectedLayersInternal(null);
    //
    // // refresh control panel
    // refreshControlPanel();
    // }

    protected void refreshControlPanel()
    {
        while (isLayerPropertiesAdjusting)
            ThreadUtil.sleep(10);

        isLayerPropertiesAdjusting = true;
        try
        {
            if (canvas != null)
            {
                final ArrayList<Layer> selectedLayers = getSelectedLayers();
                // final boolean singleSelect = (selectedRois.size() == 1);
                final boolean hasSelected = (selectedLayers.size() > 0);

                boolean canEdit = false;
                boolean canRemove = false;
                for (Layer layer : selectedLayers)
                {
                    canEdit |= !layer.isReadOnly();
                    canRemove |= layer.getCanBeRemoved();
                }

                nameField.setEnabled(hasSelected && canEdit);
                deleteButton.setEnabled(hasSelected && canRemove);

                if (hasSelected)
                {
                    final Layer layer = selectedLayers.get(0);
                    final String name = layer.getName();

                    // handle it manually as setText doesn't check for equality
                    if (!name.equals(nameField.getText()))
                        nameField.setText(name);
                }
                else
                    nameField.setText("");
            }
            else
            {
                nameField.setEnabled(false);
                deleteButton.setEnabled(false);
                nameField.setText("");
            }
        }
        finally
        {
            isLayerPropertiesAdjusting = false;
        }
    }

    /**
     * Called when selection has changed
     */
    protected void selectionChanged()
    {
        // refresh control panel
        ThreadUtil.bgRunSingle(controlPanelRefresher, true);
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (validate)
        {
            if (source == nameField)
            {
                if (isLayerPropertiesAdjusting)
                    return;

                isLayerPropertiesAdjusting = true;
                try
                {

                    if (nameField.isEnabled())
                    {
                        final String name = source.getText();

                        canvas.beginUpdate();
                        try
                        {
                            for (Layer layer : getSelectedLayers())
                                if (!layer.isReadOnly())
                                    layer.setName(name);
                        }
                        finally
                        {
                            canvas.endUpdate();
                        }
                    }
                }
                finally
                {
                    isLayerPropertiesAdjusting = false;
                }
            }
        }

        if (source == nameFilter)
            refreshLayers();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (isSelectionAdjusting || e.getValueIsAdjusting())
            return;

        selectionChanged();
    }

    @Override
    public void viewerActivated(Viewer viewer)
    {
        if (viewer != null)
            canvasRefresher.newCanvas = viewer.getCanvas();
        else
            canvasRefresher.newCanvas = null;

        ThreadUtil.bgRunSingle(canvasRefresher);
    }

    @Override
    public void viewerDeactivated(Viewer viewer)
    {
        // nothing here
    }

    @Override
    public void activeViewerChanged(ViewerEvent event)
    {
        if (event.getType() == ViewerEventType.CANVAS_CHANGED)
        {
            canvasRefresher.newCanvas = event.getSource().getCanvas();
            ThreadUtil.bgRunSingle(canvasRefresher);
        }
    }

    @Override
    public void canvasLayerChanged(CanvasLayerEvent event)
    {
        // refresh layer from externals changes
        if (!isLayerEditing)
            refreshLayers();
    }

}
