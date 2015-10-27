package icy.gui.inspector;

import icy.gui.component.AbstractRoisPanel.BaseColumnInfo;
import icy.gui.component.button.IcyButton;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROIDescriptor;
import icy.roi.ROIUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class RoiSettingPanel extends JPanel implements ActionListener
{
    // GUI
    private JScrollPane scrollPaneView;
    private JScrollPane scrollPaneExport;
    private JTable tableView;
    private JTable tableExport;
    private JPanel panelExportTop;
    private JLabel lblColumnSelectionFor;
    private JLabel lblNewLabel;
    private IcyButton upViewBtn;
    private IcyButton downViewBtn;
    private IcyButton upExportBtn;
    private IcyButton downExportBtn;

    // internals
    List<BaseColumnInfo> idsView;
    List<BaseColumnInfo> idsExport;

    private AbstractTableModel viewModel;
    private AbstractTableModel exportModel;

    private final XMLPreferences prefView;
    private final XMLPreferences prefExport;

    /**
     * Create the panel.
     * 
     * @param exportPreferences
     */
    public RoiSettingPanel(XMLPreferences viewPreferences, XMLPreferences exportPreferences)
    {
        super();

        prefView = viewPreferences;
        prefExport = exportPreferences;

        final Set<ROIDescriptor> descriptors = ROIUtil.getROIDescriptors().keySet();

        idsView = new ArrayList<BaseColumnInfo>();
        idsExport = new ArrayList<BaseColumnInfo>();

        // build view and export lists
        for (ROIDescriptor descriptor : descriptors)
        {
            idsView.add(new BaseColumnInfo(descriptor, prefView, false));
            idsExport.add(new BaseColumnInfo(descriptor, prefExport, true));
        }

        sortLists();

        initialize();

        viewModel = new AbstractTableModel()
        {
            @Override
            public int getColumnCount()
            {
                return 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0:
                        // name
                        return String.class;

                    case 1:
                        // visibility
                        return Boolean.class;
                }

                return String.class;
            }

            @Override
            public String getColumnName(int column)
            {
                switch (column)
                {
                    case 0:
                        return "Column name";

                    case 1:
                        return "Visible";
                }

                return "";
            }

            @Override
            public int getRowCount()
            {
                return idsView.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0:
                        // name
                        return idsView.get(rowIndex).descriptor.getName();

                    case 1:
                        // visibility
                        return Boolean.valueOf(idsView.get(rowIndex).visible);
                }

                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return (columnIndex == 1);
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                // visibility
                if (columnIndex == 1)
                    idsView.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
            }
        };

        exportModel = new AbstractTableModel()
        {
            @Override
            public int getColumnCount()
            {
                return 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0:
                        // name
                        return String.class;

                    case 1:
                        // visibility
                        return Boolean.class;
                }

                return String.class;
            }

            @Override
            public String getColumnName(int column)
            {
                switch (column)
                {
                    case 0:
                        return "Column name";

                    case 1:
                        return "Visible";
                }

                return "";
            }

            @Override
            public int getRowCount()
            {
                return idsExport.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0:
                        // name
                        return idsExport.get(rowIndex).descriptor.getName();

                    case 1:
                        // visibility
                        return Boolean.valueOf(idsExport.get(rowIndex).visible);
                }

                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return (columnIndex == 1);
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                // visibility
                if (columnIndex == 1)
                    idsExport.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
            }
        };

        TableColumnModel columnModel;
        TableColumn column;

        tableView.setModel(viewModel);
        columnModel = tableView.getColumnModel();

        column = columnModel.getColumn(0);
        column.setPreferredWidth(150);
        column.setMinWidth(80);
        column = columnModel.getColumn(1);
        column.setResizable(false);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        column.setMinWidth(30);

        tableExport.setModel(exportModel);
        columnModel = tableExport.getColumnModel();

        column = columnModel.getColumn(0);
        column.setPreferredWidth(150);
        column.setMinWidth(80);
        column = columnModel.getColumn(1);
        column.setResizable(false);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        column.setMinWidth(30);

        upViewBtn.addActionListener(this);
        downViewBtn.addActionListener(this);
        upExportBtn.addActionListener(this);
        downExportBtn.addActionListener(this);
    }

    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        JPanel panelView = new JPanel();
        splitPane.setLeftComponent(panelView);
        panelView.setLayout(new BorderLayout(0, 0));

        scrollPaneView = new JScrollPane();
        panelView.add(scrollPaneView, BorderLayout.CENTER);

        tableView = new JTable();
        tableView.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableView.setRowSelectionAllowed(true);
        scrollPaneView.setViewportView(tableView);

        JPanel panelViewTop = new JPanel();
        panelView.add(panelViewTop, BorderLayout.NORTH);
        GridBagLayout gbl_panelViewTop = new GridBagLayout();
        gbl_panelViewTop.columnWidths = new int[] {0, 0, 0, 0};
        gbl_panelViewTop.rowHeights = new int[] {14, 0};
        gbl_panelViewTop.columnWeights = new double[] {1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelViewTop.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panelViewTop.setLayout(gbl_panelViewTop);

        lblColumnSelectionFor = new JLabel("Columns to display");
        GridBagConstraints gbc_lblColumnSelectionFor = new GridBagConstraints();
        gbc_lblColumnSelectionFor.insets = new Insets(0, 0, 0, 5);
        gbc_lblColumnSelectionFor.gridx = 0;
        gbc_lblColumnSelectionFor.gridy = 0;
        panelViewTop.add(lblColumnSelectionFor, gbc_lblColumnSelectionFor);

        upViewBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_UP));
        upViewBtn.setToolTipText("Change order of selected column(s)");
        upViewBtn.setFlat(true);
        GridBagConstraints gbc_upViewBtn = new GridBagConstraints();
        gbc_upViewBtn.insets = new Insets(0, 0, 0, 5);
        gbc_upViewBtn.gridx = 1;
        gbc_upViewBtn.gridy = 0;
        panelViewTop.add(upViewBtn, gbc_upViewBtn);

        downViewBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_DOWN));
        downViewBtn.setToolTipText("Change order of selected column(s)");
        downViewBtn.setFlat(true);
        GridBagConstraints gbc_downViewBtn = new GridBagConstraints();
        gbc_downViewBtn.gridx = 2;
        gbc_downViewBtn.gridy = 0;
        panelViewTop.add(downViewBtn, gbc_downViewBtn);

        JPanel panelExport = new JPanel();
        splitPane.setRightComponent(panelExport);
        panelExport.setLayout(new BorderLayout(0, 0));

        scrollPaneExport = new JScrollPane();
        panelExport.add(scrollPaneExport, BorderLayout.CENTER);

        tableExport = new JTable();
        tableExport.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableExport.setRowSelectionAllowed(true);
        scrollPaneExport.setViewportView(tableExport);

        panelExportTop = new JPanel();
        panelExport.add(panelExportTop, BorderLayout.NORTH);
        GridBagLayout gbl_panelExportTop = new GridBagLayout();
        gbl_panelExportTop.columnWidths = new int[] {0, 0, 0, 0};
        gbl_panelExportTop.rowHeights = new int[] {14, 0};
        gbl_panelExportTop.columnWeights = new double[] {1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelExportTop.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panelExportTop.setLayout(gbl_panelExportTop);

        lblNewLabel = new JLabel("Columns to export (XLS or CSV)");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panelExportTop.add(lblNewLabel, gbc_lblNewLabel);

        upExportBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_UP));
        upExportBtn.setToolTipText("Change order of selected column(s)");
        upExportBtn.setFlat(true);
        GridBagConstraints gbc_upExportBtn = new GridBagConstraints();
        gbc_upExportBtn.insets = new Insets(0, 0, 0, 5);
        gbc_upExportBtn.gridx = 1;
        gbc_upExportBtn.gridy = 0;
        panelExportTop.add(upExportBtn, gbc_upExportBtn);

        downExportBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_DOWN));
        downExportBtn.setToolTipText("Change order of selected column(s)");
        downExportBtn.setFlat(true);
        GridBagConstraints gbc_downExportBtn = new GridBagConstraints();
        gbc_downExportBtn.gridx = 2;
        gbc_downExportBtn.gridy = 0;
        panelExportTop.add(downExportBtn, gbc_downExportBtn);
    }

    void fixOrders()
    {
        int order;

        order = 0;
        for (BaseColumnInfo columnInfo : idsView)
            columnInfo.order = order++;
        order = 0;
        for (BaseColumnInfo columnInfo : idsExport)
            columnInfo.order = order++;
    }

    /**
     * Sort lists on their order
     */
    void sortLists()
    {
        // sort tables
        Collections.sort(idsView);
        Collections.sort(idsExport);
        // and fix orders
        fixOrders();
    }

    /**
     * Save columns setting to preferences
     */
    public void save()
    {
        sortLists();

        for (BaseColumnInfo columnInfo : idsView)
            columnInfo.save(prefView);
        for (BaseColumnInfo columnInfo : idsExport)
            columnInfo.save(prefExport);
    }

    List<BaseColumnInfo> getSelected(JTable table, List<BaseColumnInfo> columnInfos)
    {
        final List<BaseColumnInfo> result = new ArrayList<BaseColumnInfo>();
        final int[] selected = table.getSelectedRows();

        for (int index : selected)
            result.add(columnInfos.get(index));

        return result;
    }

    void restoreSelected(JTable table, List<BaseColumnInfo> columnInfos, List<BaseColumnInfo> selected)
    {
        final ListSelectionModel selectionModel = table.getSelectionModel();

        selectionModel.setValueIsAdjusting(true);
        try
        {
            selectionModel.clearSelection();

            for (BaseColumnInfo bci : selected)
            {
                final int index = columnInfos.indexOf(bci);
                if (index >= 0)
                    selectionModel.addSelectionInterval(index, index);
            }
        }
        finally
        {
            selectionModel.setValueIsAdjusting(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();
        final JTable table;
        final List<BaseColumnInfo> columnInfos;
        final int v;

        if ((source == upViewBtn) || (source == downViewBtn))
        {
            table = tableView;
            columnInfos = idsView;
        }
        else if ((source == upExportBtn) || (source == downExportBtn))
        {
            table = tableExport;
            columnInfos = idsExport;
        }
        else
        {
            table = null;
            columnInfos = null;
        }

        if ((source == upViewBtn) || (source == upExportBtn))
            v = -1;
        else if ((source == downViewBtn) || (source == downExportBtn))
            v = 1;
        else
            v = 0;

        if ((table != null) && (columnInfos != null))
        {
            final List<BaseColumnInfo> selected = getSelected(table, columnInfos);

            // update order of selected area
            for (BaseColumnInfo bci : selected)
                bci.order = bci.order + v;

            if (v == -1)
            {
                // change order of previous item
                final int firstSelected = table.getSelectionModel().getMinSelectionIndex();
                if ((firstSelected != -1) && (firstSelected > 0))
                    columnInfos.get(firstSelected - 1).order += table.getSelectedRowCount();
            }
            else
            {
                // change order of next item
                final int lastSelected = table.getSelectionModel().getMaxSelectionIndex();
                if ((lastSelected != -1) && (lastSelected < (columnInfos.size() - 1)))
                    columnInfos.get(lastSelected + 1).order -= table.getSelectedRowCount();
            }

            // sort lists
            sortLists();

            restoreSelected(table, columnInfos, selected);
            // refresh table data
            viewModel.fireTableRowsUpdated(0, idsView.size() - 1);
            exportModel.fireTableRowsUpdated(0, idsExport.size() - 1);
        }
    }
}
