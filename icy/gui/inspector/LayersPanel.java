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

import icy.action.CanvasActions;
import icy.canvas.CanvasLayerEvent;
import icy.canvas.CanvasLayerListener;
import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.editor.VisibleCellEditor;
import icy.gui.component.renderer.VisibleCellRenderer;
import icy.gui.main.ActiveViewerListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnControlButton;
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

    static final String[] columnNames = {"Name", ""};

    List<Layer> layers;
    IcyCanvas canvas;

    // GUI
    AbstractTableModel tableModel;
    ListSelectionModel tableSelectionModel;
    JXTable table;
    IcyTextField nameFilter;
    LayerControlPanel controlPanel;

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
                controlPanel.refresh();
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
                // safe
                if (row >= layers.size())
                    return null;

                final Layer layer = layers.get(row);

                switch (column)
                {
                    case 0:
                        // layer name
                        return layer.getName();

                    case 1:
                        // layer visibility
                        return Boolean.valueOf(layer.isVisible());

                    default:
                        return "";
                }
            }

            @Override
            public void setValueAt(Object value, int row, int column)
            {
                // safe
                if (row >= layers.size())
                    return;

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
                // safe
                if (row >= layers.size())
                    return false;

                final boolean editable;

                // name field ?
                if (column == 0)
                {
                    final Layer layer = layers.get(row);
                    editable = (layer != null) ? !layer.isReadOnly() : false;
                }
                else
                    editable = true;

                return editable;
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
                        // layer visibility
                        return Boolean.class;
                }
            }
        };
        // set table model
        table.setModel(tableModel);
        // alternate highlight
        table.setHighlighters(HighlighterFactory.createSimpleStriping());
        // disable extra actions from column control
        ((ColumnControlButton) table.getColumnControl()).setAdditionalActionsVisible(false);
        // remove the internal find command (we have our own filter)
        table.getActionMap().remove("find");

        TableColumnExt col;

        // columns setting - name
        col = table.getColumnExt(0);
        col.setPreferredWidth(140);
        col.setToolTipText("Layer name (double click in a cell to edit)");

        // columns setting - visible
        col = table.getColumnExt(1);
        col.setPreferredWidth(20);
        col.setMinWidth(20);
        col.setMaxWidth(20);
        col.setCellEditor(new VisibleCellEditor(18));
        col.setCellRenderer(new VisibleCellRenderer(18));
        col.setToolTipText("Make the layer visible or not");
        col.setResizable(false);

        // table selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // create shortcuts
        buildActionMap();

        // and refresh layers
        refreshLayers();
    }

    private void initialize()
    {
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter Layer on name");
        nameFilter.addTextChangeListener(this);

        table = new JXTable();
        table.setAutoStartEditOnKeyStroke(false);
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setColumnControlVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);

        controlPanel = new LayerControlPanel(this);

        setLayout(new BorderLayout(0, 0));
        add(nameFilter, BorderLayout.NORTH);
        add(new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        validate();
    }

    void buildActionMap()
    {
        final InputMap imap = table.getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap amap = table.getActionMap();

        imap.put(CanvasActions.unselectAction.getKeyStroke(), CanvasActions.unselectAction.getName());
        imap.put(CanvasActions.deleteLayersAction.getKeyStroke(), CanvasActions.deleteLayersAction.getName());
        // also allow backspace key for delete operation here
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), CanvasActions.deleteLayersAction.getName());

        // disable search feature (we have our own filter)
        amap.remove("find");
        amap.put(CanvasActions.unselectAction.getName(), CanvasActions.unselectAction);
        amap.put(CanvasActions.deleteLayersAction.getName(), CanvasActions.deleteLayersAction);
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
        ThreadUtil.runSingle(layersRefresher);
    }

    /**
     * refresh layer list (internal)
     */
    void refreshLayersInternal()
    {
        if (canvas != null)
            layers = filterList(canvas.getLayers(false), nameFilter.getText());
        else
            layers.clear();

        // refresh table data
        ThreadUtil.runSingle(tableDataRefresher);
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

    public void clearSelected()
    {
        setSelectedLayersInternal(new ArrayList<Layer>());
    }

    void setSelectedLayersInternal(List<Layer> newSelected)
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

                    if (index > -1)
                        tableSelectionModel.addSelectionInterval(index, index);
                }
            }
        }
        finally
        {
            isSelectionAdjusting = false;
        }

        // notify selection changed
        selectionChanged();
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
        final List<Layer> save = getSelectedLayers();

        // need to be done on EDT
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
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

                setSelectedLayersInternal(save);
            }
        });
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

    /**
     * Called when selection changed
     */
    protected void selectionChanged()
    {
        // refresh control panel
        ThreadUtil.runSingle(controlPanelRefresher);
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (source == nameFilter)
            refreshLayers();
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        // internal change --> ignore
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

        ThreadUtil.runSingle(canvasRefresher);
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
            ThreadUtil.runSingle(canvasRefresher);
        }
    }

    @Override
    public void canvasLayerChanged(CanvasLayerEvent event)
    {
        // refresh layer from externals changes
        if (isLayerEditing)
            return;

        switch (event.getType())
        {
            case ADDED:
            case REMOVED:
                refreshLayers();
                break;

            case CHANGED:
                final String property = event.getProperty();

                if (Layer.PROPERTY_NAME.equals(property) || Layer.PROPERTY_OPACITY.equals(property)
                        || Layer.PROPERTY_VISIBLE.equals(property))
                    // refresh table data
                    ThreadUtil.runSingle(tableDataRefresher);
                break;
        }
    }

}
