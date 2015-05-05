package icy.gui.inspector;

import icy.gui.component.button.IcyButton;
import icy.gui.inspector.RoisPanel.ColumnInfo;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class RoiSettingsPanel extends JPanel
{
    private JScrollPane scrollPaneView;
    private JScrollPane scrollPaneExport;
    private JTable tableView;
    private JTable tableExport;
    private JPanel panelExportTop;
    private JPanel panelExportActions;
    private JLabel lblColumnSelectionFor;
    private JLabel lblNewLabel;
    private IcyButton upViewBtn;
    private IcyButton downViewBtn;
    private IcyButton upExportBtn;
    private IcyButton downExportBtn;

    private List<ColumnInfo> idsView;
    private List<ColumnInfo> idsExport;
    private TableModel viewModel;
    private TableModel exportModel;

    /**
     * Create the panel.
     */
    public RoiSettingsPanel(List<ColumnInfo> idsView, List<ColumnInfo> idsExport)
    {
        super();

        initialize();

        viewModel = new AbstractTableModel()
        {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                switch(columnIndex)
                {
                    case 0:
                        // name
                        return RoiSettingsPanel.this.idsView.get(rowIndex).name;
                        
                    case 1:
                        // visibility
                        return RoiSettingsPanel.this.idsView.get(rowIndex).defVisible;
                }
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getRowCount()
            {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getColumnCount()
            {
                // TODO Auto-generated method stub
                return 0;
            }
        };

        exportModel = new AbstractTableModel()
        {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getRowCount()
            {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getColumnCount()
            {
                // TODO Auto-generated method stub
                return 0;
            }
        };

        tableView.setModel(viewModel);
        tableExport.setModel(exportModel);

        upViewBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                // TODO Auto-generated method stub

            }
        });

        // duplicate list so we can modify them
        this.idsView = new ArrayList<ColumnInfo>(idsView.size());
        for (ColumnInfo inf : idsView)
            this.idsView.add(new ColumnInfo(inf));
        this.idsExport = new ArrayList<ColumnInfo>(idsExport.size());
        for (ColumnInfo inf : idsExport)
            this.idsExport.add(new ColumnInfo(inf));
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
        scrollPaneView.setViewportView(tableView);

        JPanel panelViewTop = new JPanel();
        panelView.add(panelViewTop, BorderLayout.NORTH);
        GridBagLayout gbl_panelViewTop = new GridBagLayout();
        gbl_panelViewTop.columnWidths = new int[] {0, 16, 0, 0, 0};
        gbl_panelViewTop.rowHeights = new int[] {14, 0};
        gbl_panelViewTop.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelViewTop.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panelViewTop.setLayout(gbl_panelViewTop);

        lblColumnSelectionFor = new JLabel("Select columns to display in the ROI table");
        GridBagConstraints gbc_lblColumnSelectionFor = new GridBagConstraints();
        gbc_lblColumnSelectionFor.insets = new Insets(0, 0, 0, 5);
        gbc_lblColumnSelectionFor.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblColumnSelectionFor.anchor = GridBagConstraints.NORTH;
        gbc_lblColumnSelectionFor.gridx = 0;
        gbc_lblColumnSelectionFor.gridy = 0;
        panelViewTop.add(lblColumnSelectionFor, gbc_lblColumnSelectionFor);

        upViewBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_UP));
        upViewBtn.setFlat(true);
        GridBagConstraints gbc_upViewBtn = new GridBagConstraints();
        gbc_upViewBtn.insets = new Insets(0, 0, 0, 5);
        gbc_upViewBtn.gridx = 2;
        gbc_upViewBtn.gridy = 0;
        panelViewTop.add(upViewBtn, gbc_upViewBtn);

        downViewBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_DOWN));
        downViewBtn.setFlat(true);
        GridBagConstraints gbc_downViewBtn = new GridBagConstraints();
        gbc_downViewBtn.gridx = 3;
        gbc_downViewBtn.gridy = 0;
        panelViewTop.add(downViewBtn, gbc_downViewBtn);

        JPanel panelViewActions = new JPanel();
        panelView.add(panelViewActions, BorderLayout.EAST);

        JPanel panelExport = new JPanel();
        splitPane.setRightComponent(panelExport);
        panelExport.setLayout(new BorderLayout(0, 0));

        scrollPaneExport = new JScrollPane();
        panelExport.add(scrollPaneExport, BorderLayout.CENTER);

        tableExport = new JTable();
        scrollPaneExport.setViewportView(tableExport);

        panelExportTop = new JPanel();
        panelExport.add(panelExportTop, BorderLayout.NORTH);
        GridBagLayout gbl_panelExportTop = new GridBagLayout();
        gbl_panelExportTop.columnWidths = new int[] {0, 16, 0, 0, 0};
        gbl_panelExportTop.rowHeights = new int[] {14, 0};
        gbl_panelExportTop.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelExportTop.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        panelExportTop.setLayout(gbl_panelExportTop);

        lblNewLabel = new JLabel("Select the columns for the ROI export (XLS or CSV)");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblNewLabel.anchor = GridBagConstraints.NORTH;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panelExportTop.add(lblNewLabel, gbc_lblNewLabel);

        upExportBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_UP));
        upExportBtn.setFlat(true);
        GridBagConstraints gbc_upExportBtn = new GridBagConstraints();
        gbc_upExportBtn.insets = new Insets(0, 0, 0, 5);
        gbc_upExportBtn.gridx = 2;
        gbc_upExportBtn.gridy = 0;
        panelExportTop.add(upExportBtn, gbc_upExportBtn);

        downExportBtn = new IcyButton(new IcyIcon(ResourceUtil.ICON_ARROW_DOWN));
        downExportBtn.setFlat(true);
        GridBagConstraints gbc_downExportBtn = new GridBagConstraints();
        gbc_downExportBtn.gridx = 3;
        gbc_downExportBtn.gridy = 0;
        panelExportTop.add(downExportBtn, gbc_downExportBtn);

        panelExportActions = new JPanel();
        panelExport.add(panelExportActions, BorderLayout.EAST);
    }

    public List<String> getIdsView()
    {
        return idsView;
    }

    public List<String> getIdsExport()
    {
        return idsExport;
    }
}
