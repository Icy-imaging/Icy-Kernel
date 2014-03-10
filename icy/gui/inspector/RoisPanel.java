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

import icy.action.RoiActions;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.renderer.ImageTableCellRenderer;
import icy.gui.component.renderer.NativeArrayTableCellRenderer;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.util.LookAndFeelUtil;
import icy.image.IntensityInfo;
import icy.main.Icy;
import icy.math.ArrayMath;
import icy.math.MathUtil;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.ThreadUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.ActionMap;
import javax.swing.Icon;
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
import org.pushingpixels.substance.api.skin.SkinChangeListener;

/**
 * @author Stephane
 */
public class RoisPanel extends ExternalizablePanel implements ActiveSequenceListener, TextChangeListener,
        ListSelectionListener, Runnable, PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    static final String[] columnNames = {"", "Name", "Type", "Position X", "Position Y", "Position Z", "Position T",
            "Position C", "Size X", "Size Y", "Size Z", "Size T", "Size C", "Contour", "Interior", "Perimeter", "Area",
            "Surface Area", "Volume", "Min Intensity", "Mean Intensity", "Max Intensity", "Std Deviation"};

    private static final String PREF_ID = "ROIPanel";

    private static final String ID_COLUMN_ICON = "col_icon";
    private static final String ID_COLUMN_NAME = "col_name";
    private static final String ID_COLUMN_TYPE = "col_type";
    private static final String ID_COLUMN_POSITION_X = "position_x";
    private static final String ID_COLUMN_POSITION_Y = "position_y";
    private static final String ID_COLUMN_POSITION_Z = "position_z";
    private static final String ID_COLUMN_POSITION_T = "position_t";
    private static final String ID_COLUMN_POSITION_C = "position_c";
    private static final String ID_COLUMN_SIZE_X = "size_x";
    private static final String ID_COLUMN_SIZE_Y = "size_y";
    private static final String ID_COLUMN_SIZE_Z = "size_z";
    private static final String ID_COLUMN_SIZE_T = "size_t";
    private static final String ID_COLUMN_SIZE_C = "size_c";
    private static final String ID_COLUMN_CONTOUR = "col_contour";
    private static final String ID_COLUMN_POINTS = "col_points";
    private static final String ID_COLUMN_PERIMETER = "col_perimeter";
    private static final String ID_COLUMN_AREA = "col_area";
    private static final String ID_COLUMN_SURFACE_AREA = "col_surface_area";
    private static final String ID_COLUMN_VOLUME = "col_volume";
    private static final String ID_COLUMN_MIN_INT = "col_min_int";
    private static final String ID_COLUMN_MEAN_INT = "col_mean_int";
    private static final String ID_COLUMN_MAX_INT = "col_max_int";
    private static final String ID_COLUMN_STANDARD_DEV = "col_standard_dev";

    // GUI
    AbstractTableModel tableModel;
    ListSelectionModel tableSelectionModel;
    JXTable table;
    IcyTextField nameFilter;
    RoiControlPanel roiControlPanel;

    // ROI info list cache
    List<ROIInfo> rois;
    List<ROIInfo> filteredRois;

    // internals
    final Semaphore modifySelection;
    // complete refresh of the table
    final Runnable tableDataRefresher;
    final Thread roiInfoComputer;
    final LinkedBlockingQueue<ROIInfo> roisToCompute;

    public RoisPanel()
    {
        super("ROI", "roiPanel", new Point(100, 100), new Dimension(400, 600));

        rois = new ArrayList<ROIInfo>();
        filteredRois = new ArrayList<ROIInfo>();
        modifySelection = new Semaphore(1);

        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataInternal();
            }
        };

        initialize();

        LookAndFeelUtil.addSkinChangeListener(new SkinChangeListener()
        {
            @Override
            public void skinChanged()
            {
                // fix the row height which is not preserved on skin change
                table.setRowHeight(24);
            }
        });

        // build table model
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
                return filteredRois.size();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                // substance occasionally do not check size before getting value
                if (row >= filteredRois.size())
                    return null;

                final ROIInfo roiInfo = filteredRois.get(row);
                final ROI roi = roiInfo.getROI();

                switch (column)
                {
                    case 0: // icon
                        return ResourceUtil.getImageIcon(roi.getIcon(), 24);
                    case 1: // name
                        return roi.getName();
                    case 2: // type
                        return roi.getSimpleClassName();
                    case 3: // position X
                        return roiInfo.getPositionXAsString();
                    case 4: // position Y
                        return roiInfo.getPositionYAsString();
                    case 5: // position Z
                        return roiInfo.getPositionZAsString();
                    case 6: // position T
                        return roiInfo.getPositionTAsString();
                    case 7: // position C
                        return roiInfo.getPositionCAsString();
                    case 8: // size X
                        return roiInfo.getSizeXAsString();
                    case 9: // size Y
                        return roiInfo.getSizeYAsString();
                    case 10: // size Z
                        return roiInfo.getSizeZAsString();
                    case 11: // size T
                        return roiInfo.getSizeTAsString();
                    case 12: // size C
                        return roiInfo.getSizeCAsString();
                    case 13: // contour points
                        return Double.valueOf(roiInfo.getNumberOfContourPoints());
                    case 14: // points
                        return Double.valueOf(roiInfo.getNumberOfPoints());
                    case 15: // perimeter
                        return roiInfo.getPerimeter();
                    case 16: // area
                        return roiInfo.getArea();
                    case 17: // surface area
                        return roiInfo.getSurfaceArea();
                    case 18: // volume
                        return roiInfo.getVolume();
                    case 19: // min intensity
                        return roiInfo.getMinIntensities();
                    case 20: // mean intensity
                        return roiInfo.getMeanIntensities();
                    case 21: // max intensity
                        return roiInfo.getMaxIntensities();
                    case 22: // standard deviation
                        return roiInfo.getStandardDeviation();
                }

                return "";
            }

            @Override
            public void setValueAt(Object value, int row, int column)
            {
                // substance occasionally do not check size before getting value
                if (row >= filteredRois.size())
                    return;

                final ROIInfo roiInfo = filteredRois.get(row);
                final ROI roi = roiInfo.getROI();

                switch (column)
                {
                    case 1: // name
                        roi.setName((String) value);
                        break;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return (column == 1);
            }

            @Override
            public Class<?> getColumnClass(int column)
            {
                switch (column)
                {
                    default:
                        return String.class;
                    case 0: // icon
                        return Icon.class;
                    case 13: // contour points
                    case 14: // points
                        return Double.class;
                    case 19: // min intensity
                    case 20: // mean intensity
                    case 21: // max intensity
                    case 22: // standard deviation
                        return double[].class;
                }
            }
        };
        // set table model
        table.setModel(tableModel);
        // alternate highlight
        table.setHighlighters(HighlighterFactory.createSimpleStriping());
        // disable extra actions from column control
        ((ColumnControlButton) table.getColumnControl()).setAdditionalActionsVisible(false);

        // modify column properties
        TableColumnExt col;

        final Comparator<double[]> daComparator = new Comparator<double[]>()
        {
            @Override
            public int compare(double[] o1, double[] o2)
            {
                return Double.compare(ArrayMath.sum(o1), ArrayMath.sum(o2));
            }
        };
        final NativeArrayTableCellRenderer naTableCellRenderer = new NativeArrayTableCellRenderer();

        // icon
        col = table.getColumnExt(0);
        col.setIdentifier(ID_COLUMN_ICON);
        col.setPreferredWidth(26);
        col.setMinWidth(26);
        col.setMaxWidth(26);
        col.setCellRenderer(new ImageTableCellRenderer(24));
        col.setResizable(false);
        col.addPropertyChangeListener(this);
        // name
        col = table.getColumnExt(1);
        col.setIdentifier(ID_COLUMN_NAME);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setToolTipText("ROI name (double click in a cell to edit)");
        col.addPropertyChangeListener(this);
        // type
        col = table.getColumnExt(2);
        col.setIdentifier(ID_COLUMN_TYPE);
        col.setPreferredWidth(80);
        col.setMinWidth(60);
        col.setToolTipText("ROI type");
        col.addPropertyChangeListener(this);
        // position X
        col = table.getColumnExt(3);
        col.setIdentifier(ID_COLUMN_POSITION_X);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("X Position of the ROI");
        col.addPropertyChangeListener(this);
        // position Y
        col = table.getColumnExt(4);
        col.setIdentifier(ID_COLUMN_POSITION_Y);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Y Position of the ROI");
        col.addPropertyChangeListener(this);
        // position Z
        col = table.getColumnExt(5);
        col.setIdentifier(ID_COLUMN_POSITION_Z);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Z Position of the ROI");
        col.addPropertyChangeListener(this);
        // position T
        col = table.getColumnExt(6);
        col.setIdentifier(ID_COLUMN_POSITION_T);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("T Position of the ROI");
        col.addPropertyChangeListener(this);
        // position C
        col = table.getColumnExt(7);
        col.setIdentifier(ID_COLUMN_POSITION_C);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("C Position of the ROI");
        col.addPropertyChangeListener(this);
        // size X
        col = table.getColumnExt(8);
        col.setIdentifier(ID_COLUMN_SIZE_X);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("X dimension size of the ROI (width)");
        col.addPropertyChangeListener(this);
        // size Y
        col = table.getColumnExt(9);
        col.setIdentifier(ID_COLUMN_SIZE_Y);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Y dimension size of the ROI (heigth)");
        col.addPropertyChangeListener(this);
        // size Z
        col = table.getColumnExt(10);
        col.setIdentifier(ID_COLUMN_SIZE_Z);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Z dimension size of the ROI (depth)");
        col.addPropertyChangeListener(this);
        // size T
        col = table.getColumnExt(11);
        col.setIdentifier(ID_COLUMN_SIZE_T);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("T dimension size of the ROI (time)");
        col.addPropertyChangeListener(this);
        // size C
        col = table.getColumnExt(12);
        col.setIdentifier(ID_COLUMN_SIZE_C);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("C dimension size of the ROI (channel)");
        col.addPropertyChangeListener(this);
        // contour points
        col = table.getColumnExt(13);
        col.setIdentifier(ID_COLUMN_CONTOUR);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Number of points for the contour");
        col.addPropertyChangeListener(this);
        // points
        col = table.getColumnExt(14);
        col.setIdentifier(ID_COLUMN_POINTS);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Number of points for the interior");
        col.addPropertyChangeListener(this);
        // perimeter
        col = table.getColumnExt(15);
        col.setIdentifier(ID_COLUMN_PERIMETER);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Perimeter");
        col.addPropertyChangeListener(this);
        // area
        col = table.getColumnExt(16);
        col.setIdentifier(ID_COLUMN_AREA);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Area");
        col.addPropertyChangeListener(this);
        // surface area
        col = table.getColumnExt(17);
        col.setIdentifier(ID_COLUMN_SURFACE_AREA);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Surface Area");
        col.addPropertyChangeListener(this);
        // volume
        col = table.getColumnExt(18);
        col.setIdentifier(ID_COLUMN_VOLUME);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Volume");
        col.addPropertyChangeListener(this);
        // min intensity
        col = table.getColumnExt(19);
        col.setIdentifier(ID_COLUMN_MIN_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Minimum pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // mean intensity
        col = table.getColumnExt(20);
        col.setIdentifier(ID_COLUMN_MEAN_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Mean pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // max intensity
        col = table.getColumnExt(21);
        col.setIdentifier(ID_COLUMN_MAX_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Maximum pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // standard deviation
        col = table.getColumnExt(22);
        col.setIdentifier(ID_COLUMN_STANDARD_DEV);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Standard deviation (per channel)");
        col.addPropertyChangeListener(this);

        // set selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        roisToCompute = new LinkedBlockingQueue<ROIInfo>();
        roiInfoComputer = new Thread(this, "ROI properties calculator");
        roiInfoComputer.setPriority(Thread.MIN_PRIORITY);
        roiInfoComputer.start();

        // load panel preferences
        loadPreferences();

        // set shortcuts
        buildActionMap();

        refreshRois();
    }

    private void initialize()
    {
        // need filter before load
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter ROI on name");
        nameFilter.addTextChangeListener(this);

        // build table
        table = new JXTable();
        table.setAutoStartEditOnKeyStroke(false);
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setColumnControlVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);

        final JPanel middlePanel = new JPanel(new BorderLayout(0, 0));

        middlePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        middlePanel.add(new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // build control panel
        roiControlPanel = new RoiControlPanel(this);

        setLayout(new BorderLayout());
        add(nameFilter, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(roiControlPanel, BorderLayout.SOUTH);

        validate();
    }

    void buildActionMap()
    {
        final InputMap imap = table.getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap amap = table.getActionMap();

        imap.put(RoiActions.unselectAction.getKeyStroke(), RoiActions.unselectAction.getName());
        imap.put(RoiActions.deleteAction.getKeyStroke(), RoiActions.deleteAction.getName());
        // also allow backspace key for delete operation here
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), RoiActions.deleteAction.getName());
        imap.put(RoiActions.copyAction.getKeyStroke(), RoiActions.copyAction.getName());
        imap.put(RoiActions.pasteAction.getKeyStroke(), RoiActions.pasteAction.getName());
        imap.put(RoiActions.copyLinkAction.getKeyStroke(), RoiActions.copyLinkAction.getName());
        imap.put(RoiActions.pasteLinkAction.getKeyStroke(), RoiActions.pasteLinkAction.getName());

        // disable search feature (we have our own filter)
        amap.remove("find");
        amap.put(RoiActions.unselectAction.getName(), RoiActions.unselectAction);
        amap.put(RoiActions.deleteAction.getName(), RoiActions.deleteAction);
        amap.put(RoiActions.copyAction.getName(), RoiActions.copyAction);
        amap.put(RoiActions.pasteAction.getName(), RoiActions.pasteAction);
        amap.put(RoiActions.copyLinkAction.getName(), RoiActions.copyLinkAction);
        amap.put(RoiActions.pasteLinkAction.getName(), RoiActions.pasteLinkAction);
    }

    private XMLPreferences getPreferences()
    {
        return GeneralPreferences.getPreferences().node(PREF_ID);
    }

    Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    private void loadColumnVisibility(XMLPreferences pref, String id, boolean def)
    {
        table.getColumnExt(id).setVisible(pref.getBoolean(id, def));
    }

    private void loadPreferences()
    {
        final XMLPreferences pref = getPreferences();

        loadColumnVisibility(pref, ID_COLUMN_ICON, true);
        loadColumnVisibility(pref, ID_COLUMN_NAME, true);
        loadColumnVisibility(pref, ID_COLUMN_TYPE, false);
        loadColumnVisibility(pref, ID_COLUMN_POSITION_X, false);
        loadColumnVisibility(pref, ID_COLUMN_POSITION_Y, false);
        loadColumnVisibility(pref, ID_COLUMN_POSITION_Z, false);
        loadColumnVisibility(pref, ID_COLUMN_POSITION_T, false);
        loadColumnVisibility(pref, ID_COLUMN_POSITION_C, false);
        loadColumnVisibility(pref, ID_COLUMN_SIZE_X, false);
        loadColumnVisibility(pref, ID_COLUMN_SIZE_Y, false);
        loadColumnVisibility(pref, ID_COLUMN_SIZE_Z, false);
        loadColumnVisibility(pref, ID_COLUMN_SIZE_T, false);
        loadColumnVisibility(pref, ID_COLUMN_SIZE_C, false);
        loadColumnVisibility(pref, ID_COLUMN_CONTOUR, false);
        loadColumnVisibility(pref, ID_COLUMN_POINTS, false);
        loadColumnVisibility(pref, ID_COLUMN_PERIMETER, true);
        loadColumnVisibility(pref, ID_COLUMN_AREA, true);
        loadColumnVisibility(pref, ID_COLUMN_SURFACE_AREA, false);
        loadColumnVisibility(pref, ID_COLUMN_VOLUME, false);
        loadColumnVisibility(pref, ID_COLUMN_MIN_INT, false);
        loadColumnVisibility(pref, ID_COLUMN_MEAN_INT, false);
        loadColumnVisibility(pref, ID_COLUMN_MAX_INT, false);
        loadColumnVisibility(pref, ID_COLUMN_STANDARD_DEV, false);
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    /**
     * refresh ROI list
     */
    protected void refreshRois()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
        {
            final List<ROI> newRois = sequence.getROIs();
            final List<ROI> oldRois = new ArrayList<ROI>();

            // build old ROI list
            for (int i = 0; i < rois.size(); i++)
                oldRois.add(rois.get(i).getROI());

            // remove ROI which are no more in the list (use HashSet for fast contains())
            final Set<ROI> newRoiSet = new HashSet<ROI>(newRois);
            for (int i = oldRois.size() - 1; i >= 0; i--)
                if (!newRoiSet.contains(oldRois.get(i)))
                    rois.remove(i);

            // add ROI which has been added (use HashSet for fast contains())
            final Set<ROI> oldRoiSet = new HashSet<ROI>(oldRois);
            for (ROI roi : newRois)
                if (!oldRoiSet.contains(roi))
                    rois.add(new ROIInfo(roi));
        }
        else
        {
            // no change --> exit
            if (rois.isEmpty())
                return;

            // clear ROI list
            rois.clear();
        }

        // need to refresh the filtered list
        refreshFilteredRois();
    }

    /**
     * refresh filtered ROI list (and refresh table data according)
     */
    protected void refreshFilteredRois()
    {
        final List<ROIInfo> newFilteredRois = getFilteredList(nameFilter.getText());

        final int newSize = newFilteredRois.size();
        final int oldSize = filteredRois.size();

        // easy optimization
        if ((newSize == 0) && (oldSize == 0))
            return;

        // same size
        if (newSize == oldSize)
        {
            // same values, don't need to update it
            if (new HashSet<ROIInfo>(newFilteredRois).containsAll(filteredRois))
                return;
        }

        // update filtered ROI list
        filteredRois = newFilteredRois;
        // refresh whole table
        refreshTableData();
    }

    ROIInfo getRoiInfoToCompute()
    {
        return null;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                roisToCompute.take().compute();
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }

    /**
     * Return index of specified ROI in the ROI list
     */
    private int getRoiIndex(ROI roi)
    {
        for (int i = 0; i < filteredRois.size(); i++)
            if (filteredRois.get(i).getROI() == roi)
                return i;

        return -1;
    }

    /**
     * Return index of specified ROI in the model
     */
    int getRoiModelIndex(ROI roi)
    {
        return getRoiIndex(roi);
    }

    /**
     * Return index of specified ROI in the table
     */
    int getRoiTableIndex(ROI roi)
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
     * Returns the visible ROI in the ROI control panel.
     */
    public List<ROI> getVisibleRois()
    {
        final List<ROIInfo> roisInfo = filteredRois;
        final List<ROI> result = new ArrayList<ROI>(roisInfo.size());

        for (ROIInfo roiInfo : roisInfo)
            result.add(roiInfo.getROI());

        return result;
    }

    /**
     * Returns the ROI informations for the specified ROI.
     */
    public ROIInfo getROIInfo(ROI roi)
    {
        final int index = getRoiIndex(roi);

        if (index != -1)
            return filteredRois.get(index);

        return null;
    }

    /**
     * Returns the selected ROI in the ROI control panel.
     */
    public List<ROIInfo> getSelectedRoisInfo()
    {
        final List<ROIInfo> result = new ArrayList<ROIInfo>(table.getRowCount());

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

            if ((index >= 0) && (index < filteredRois.size()))
                result.add(filteredRois.get(index));
        }

        return result;
    }

    /**
     * Get the selected ROI in the ROI control panel.<br>
     * This actually returns selected ROI from the ROI table in ROI panel (cached).
     */
    public List<ROI> getSelectedRois()
    {
        final List<ROIInfo> roisInfo = getSelectedRoisInfo();
        final List<ROI> result = new ArrayList<ROI>(roisInfo.size());

        for (ROIInfo roiInfo : roisInfo)
            result.add(roiInfo.getROI());

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

    protected List<ROIInfo> getFilteredList(String filter)
    {
        final List<ROIInfo> result = new ArrayList<ROIInfo>();

        synchronized (rois)
        {
            if (StringUtil.isEmpty(filter, true))
                result.addAll(rois);
            else
            {
                final String text = filter.trim().toLowerCase();

                // filter on name
                for (ROIInfo roi : rois)
                    if (roi.getROI().getName().toLowerCase().indexOf(text) != -1)
                        result.add(roi);
            }
        }

        return result;
    }

    public void refreshTableData()
    {
        ThreadUtil.bgRunSingle(tableDataRefresher, false);
    }

    void refreshTableDataInternal()
    {
        // don't eat too much time on data refresh
        ThreadUtil.sleep(10);

        ThreadUtil.invokeNow(new Runnable()
        {

            @Override
            public void run()
            {
                final Sequence sequence = getSequence();

                tableSelectionModel.setValueIsAdjusting(true);
                modifySelection.acquireUninterruptibly();
                try
                {
                    // clear selection
                    tableSelectionModel.clearSelection();
                    // notify table data changed
                    tableModel.fireTableDataChanged();

                    // restore selection from sequence
                    if (sequence != null)
                    {
                        for (ROI roi : sequence.getSelectedROIs())
                        {
                            final int index = getRoiTableIndex(roi);

                            if (index > -1)
                                tableSelectionModel.addSelectionInterval(index, index);
                        }
                    }
                }
                finally
                {
                    modifySelection.release();
                    tableSelectionModel.setValueIsAdjusting(false);
                }
            }
        });

        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    // void roiSelectionChanged(ROIInfo roiInfo)
    // {
    // // refresh informations for this ROI
    // final ROI roi = roiInfo.getROI();
    // final int index = getRoiTableIndex(roi);
    //
    // // check selection change
    // if (index != -1)
    // {
    // // change selection
    // ThreadUtil.invokeLater(new Runnable()
    // {
    // @Override
    // public void run()
    // {
    // modifySelection.acquireUninterruptibly();
    // try
    // {
    // if (roi.isSelected())
    // tableSelectionModel.addSelectionInterval(index, index);
    // else
    // tableSelectionModel.removeSelectionInterval(index, index);
    // }
    // finally
    // {
    // modifySelection.release();
    // }
    //
    // // notify control panel that ROI selection changed
    // roiControlPanel.selectionChanged();
    // }
    // });
    // }
    // }

    // void roiInfoUpdated(ROIInfo roiInfo)
    // {
    // // refresh informations for this ROI
    // final ROI roi = roiInfo.getROI();
    // final int index = getRoiModelIndex(roiInfo.getROI());
    //
    // // notify row changed
    // if (index != -1)
    // {
    // ThreadUtil.invokeLater(new Runnable()
    // {
    // @Override
    // public void run()
    // {
    // tableModel.fireTableRowsUpdated(index, index);
    // }
    // });
    // }
    //
    // // notify control panel that ROI changed
    // if (roi.isSelected())
    // roiControlPanel.roiChanged(new ROIEvent(roi, ROIEventType.ROI_CHANGED));
    // }

    /**
     * Returns selected ROI informations in CSV format (tab separated)
     */
    public String getCSVFormattedInfosOfSelectedRois()
    {
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = columnNames.length;
        final int numrows = table.getSelectedRowCount();

        // table is empty --> returns empty string
        if (numrows == 0)
            return "";

        final StringBuffer sbf = new StringBuffer();
        final int[] rowsselected = table.getSelectedRows();

        // column name
        for (int j = 1; j < numcols; j++)
        {
            sbf.append(table.getModel().getColumnName(j));
            if (j < numcols - 1)
                sbf.append("\t");
        }
        sbf.append("\r\n");

        // then content
        for (int i = 0; i < numrows; i++)
        {
            for (int j = 1; j < numcols; j++)
            {
                final Object value = table.getModel().getValueAt(table.convertRowIndexToModel(rowsselected[i]), j);

                // special case of double array
                if (value instanceof double[])
                {
                    final double[] darray = (double[]) value;

                    for (int l = 0; l < darray.length; l++)
                    {
                        sbf.append(darray[l]);
                        if (l < darray.length - 1)
                            sbf.append(" ");
                    }
                }
                else
                    sbf.append(value);

                if (j < numcols - 1)
                    sbf.append("\t");
            }
            sbf.append("\r\n");
        }

        return sbf.toString();
    }

    /**
     * Returns all ROI informations in CSV format (tab separated)
     */
    public String getCSVFormattedInfos()
    {
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = columnNames.length;
        final int numrows = table.getRowCount();

        // table is empty --> returns empty string
        if (numrows == 0)
            return "";

        final StringBuffer sbf = new StringBuffer();

        // column name
        for (int j = 1; j < numcols; j++)
        {
            sbf.append(table.getModel().getColumnName(j));
            if (j < numcols - 1)
                sbf.append("\t");
        }
        sbf.append("\r\n");

        // then content
        for (int i = 0; i < numrows; i++)
        {
            for (int j = 1; j < numcols; j++)
            {
                final Object value = table.getModel().getValueAt(table.convertRowIndexToModel(i), j);

                // special case of double array
                if (value instanceof double[])
                {
                    final double[] darray = (double[]) value;

                    for (int l = 0; l < darray.length; l++)
                    {
                        sbf.append(darray[l]);
                        if (l < darray.length - 1)
                            sbf.append(" ");
                    }
                }
                else
                    sbf.append(value);

                if (j < numcols - 1)
                    sbf.append("\t");
            }
            sbf.append("\r\n");
        }

        return sbf.toString();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        // column visibility changed ?
        if ("visible".equals(evt.getPropertyName()))
        {
            // store column visibility in preferences
            final TableColumnExt column = (TableColumnExt) evt.getSource();
            getPreferences().putBoolean((String) column.getIdentifier(), column.isVisible());
        }
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
        
        // not in internal selection change ?
        if (modifySelection.tryAcquire())
        {
            try
            {
                final List<ROI> selectedRois = getSelectedRois();
                final Sequence sequence = getSequence();

                // update selected ROI in sequence
                if (sequence != null)
                    sequence.setSelectedROIs(selectedRois);

                // notify the ROI control panel that selection changed
                roiControlPanel.selectionChanged();
            }
            finally
            {
                modifySelection.release();
            }
        }
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

        final SequenceEventSourceType sourceType = event.getSourceType();

        if (sourceType == SequenceEventSourceType.SEQUENCE_ROI)
        {
            final SequenceEventType type = event.getType();

            // changed event already handled by ROIInfo
            if ((type == SequenceEventType.ADDED) || (type == SequenceEventType.REMOVED))
                // refresh the ROI list
                refreshRois();
        }
        // if data or type changed we need to refresh action
        // so we change change ROI position correctly
        else if ((sourceType == SequenceEventSourceType.SEQUENCE_DATA)
                || (sourceType == SequenceEventSourceType.SEQUENCE_TYPE))
            roiControlPanel.refreshROIActions();
    }

    public class ROIInfo implements ROIListener
    {
        private ROI roi;
        private double[] standardDeviation;
        private IntensityInfo[] intensityInfos;

        // cached
        private double numberContourPoints;
        private double numberPoints;
        private boolean sequenceInfInvalid;
        private boolean roiInfInvalid;

        public ROIInfo(ROI roi)
        {
            this.roi = roi;

            numberContourPoints = 0d;
            numberPoints = 0d;
            standardDeviation = new double[0];
            intensityInfos = new IntensityInfo[0];
            sequenceInfInvalid = true;
            roiInfInvalid = true;

            roi.addListener(this);
        }

        public void dispose()
        {
            roi.removeListener(this);
            roi = null;
        }

        /**
         * Recompute ROI informations
         */
        public void compute()
        {
            try
            {
                if (roiInfInvalid)
                {
                    // refresh points number calculation
                    numberContourPoints = MathUtil.roundSignificant(roi.getNumberOfContourPoints(), 5, true);
                    numberPoints = MathUtil.roundSignificant(roi.getNumberOfPoints(), 5, true);

                    roiInfInvalid = false;
                }

                if (sequenceInfInvalid)
                {
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        // calculate intensity infos
                        final Rectangle5D bounds = roi.getBounds5D().createIntersection(sequence.getBounds5D());

                        final int minC = (int) bounds.getC();
                        final int sizeC = (int) bounds.getSizeC();

                        final double[] sd = new double[sizeC];
                        final IntensityInfo[] iis = new IntensityInfo[sizeC];

                        for (int c = 0; c < sizeC; c++)
                        {
                            final IntensityInfo ii = ROIUtil.getIntensityInfo(sequence, roi, -1, -1, minC + c);

                            if (ii != null)
                            {
                                // round values
                                ii.minIntensity = MathUtil.roundSignificant(ii.minIntensity, 5, true);
                                ii.meanIntensity = MathUtil.roundSignificant(ii.meanIntensity, 5, true);
                                ii.maxIntensity = MathUtil.roundSignificant(ii.maxIntensity, 5, true);

                                iis[c] = ii;
                            }
                            else
                                iis[c] = new IntensityInfo();

                            sd[c] = ROIUtil.getStandardDeviation(sequence, roi, -1, -1, minC + c);
                        }

                        intensityInfos = iis;
                        standardDeviation = sd;
                    }
                    else
                    {
                        intensityInfos = new IntensityInfo[0];
                        standardDeviation = new double[0];
                    }

                    sequenceInfInvalid = false;
                }
            }
            catch (Throwable e)
            {
                // we can have some exception here as this is an asynch process (just ignore)
                if (e instanceof OutOfMemoryError)
                    System.err.println("Cannot compute ROI infos: Not enought memory !");
            }

            refreshTableData();
            // roiInfoUpdated(this);
        }

        void requestCompute()
        {
            if (!roisToCompute.contains(this))
            {
                try
                {
                    roisToCompute.put(this);
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
            }
        }

        public ROI getROI()
        {
            return roi;
        }

        public String getName()
        {
            return roi.getName();
        }

        public boolean isRoiInfOutdated()
        {
            return roiInfInvalid;
        }

        public boolean isSequenceInfOutdated()
        {
            return sequenceInfInvalid;
        }

        public double getNumberOfContourPoints()
        {
            // need to recompute
            if (roiInfInvalid)
                requestCompute();

            return numberContourPoints;
        }

        public double getNumberOfPoints()
        {
            // need to recompute
            if (roiInfInvalid)
                requestCompute();

            return numberPoints;
        }

        public String getPerimeter()
        {
            return ROIUtil.getContourSize(Icy.getMainInterface().getActiveSequence(), getNumberOfContourPoints(), roi,
                    2, 5);
        }

        public String getArea()
        {
            return ROIUtil.getInteriorSize(Icy.getMainInterface().getActiveSequence(), getNumberOfPoints(), roi, 2, 5);
        }

        public String getSurfaceArea()
        {
            return ROIUtil.getContourSize(Icy.getMainInterface().getActiveSequence(), getNumberOfContourPoints(), roi,
                    3, 5);
        }

        public String getVolume()
        {
            return ROIUtil.getInteriorSize(Icy.getMainInterface().getActiveSequence(), getNumberOfPoints(), roi, 3, 5);
        }

        public double[] getMinIntensities()
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            final double[] result = new double[infos.length];

            for (int i = 0; i < infos.length; i++)
                result[i] = infos[i].minIntensity;

            return result;
        }

        public double[] getMeanIntensities()
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            final double[] result = new double[infos.length];

            for (int i = 0; i < infos.length; i++)
                result[i] = infos[i].meanIntensity;

            return result;
        }

        public double[] getMaxIntensities()
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            final double[] result = new double[infos.length];

            for (int i = 0; i < infos.length; i++)
                result[i] = infos[i].maxIntensity;

            return result;
        }

        public double[] getStandardDeviation()
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            return standardDeviation.clone();
        }

        public IntensityInfo[] getIntensities()
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            return intensityInfos;
        }

        public double getPositionX()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeX() == Double.POSITIVE_INFINITY)
                return -1d;

            return MathUtil.roundSignificant(bounds.getX(), 5, true);
        }

        public double getPositionY()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeY() == Double.POSITIVE_INFINITY)
                return -1d;

            return MathUtil.roundSignificant(bounds.getY(), 5, true);
        }

        public double getPositionZ()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeZ() == Double.POSITIVE_INFINITY)
                return -1d;

            return MathUtil.roundSignificant(bounds.getZ(), 5, true);
        }

        public double getPositionT()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeT() == Double.POSITIVE_INFINITY)
                return -1d;

            return MathUtil.roundSignificant(bounds.getT(), 5, true);
        }

        public double getPositionC()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeC() == Double.POSITIVE_INFINITY)
                return -1d;

            return MathUtil.roundSignificant(bounds.getC(), 5, true);
        }

        public double getSizeX()
        {
            final double v = roi.getBounds5D().getSizeX();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return v;

            return MathUtil.roundSignificant(v, 5, true);
        }

        public double getSizeY()
        {
            final double v = roi.getBounds5D().getSizeY();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return v;

            return MathUtil.roundSignificant(v, 5, true);
        }

        public double getSizeZ()
        {
            final double v = roi.getBounds5D().getSizeZ();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return v;

            return MathUtil.roundSignificant(v, 5, true);
        }

        public double getSizeT()
        {
            final double v = roi.getBounds5D().getSizeT();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return v;

            return MathUtil.roundSignificant(v, 5, true);
        }

        public double getSizeC()
        {
            final double v = roi.getBounds5D().getSizeC();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return v;

            return MathUtil.roundSignificant(v, 5, true);
        }

        public String getPositionXAsString()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeX() == Double.POSITIVE_INFINITY)
                return "all";

            return StringUtil.toString(MathUtil.roundSignificant(bounds.getX(), 5, true));
        }

        public String getPositionYAsString()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeY() == Double.POSITIVE_INFINITY)
                return "all";

            return StringUtil.toString(MathUtil.roundSignificant(bounds.getY(), 5, true));
        }

        public String getPositionZAsString()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeZ() == Double.POSITIVE_INFINITY)
                return "all";

            return StringUtil.toString(MathUtil.roundSignificant(bounds.getZ(), 5, true));
        }

        public String getPositionTAsString()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeT() == Double.POSITIVE_INFINITY)
                return "all";

            return StringUtil.toString(MathUtil.roundSignificant(bounds.getT(), 5, true));
        }

        public String getPositionCAsString()
        {
            final Rectangle5D bounds = roi.getBounds5D();

            // special case of infinite dimension
            if (bounds.getSizeC() == Double.POSITIVE_INFINITY)
                return "all";

            return StringUtil.toString(MathUtil.roundSignificant(bounds.getC(), 5, true));
        }

        public String getSizeXAsString()
        {
            final double v = roi.getBounds5D().getSizeX();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return MathUtil.INFINITE_STRING;

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeYAsString()
        {
            final double v = roi.getBounds5D().getSizeY();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return MathUtil.INFINITE_STRING;

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeZAsString()
        {
            final double v = roi.getBounds5D().getSizeZ();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return MathUtil.INFINITE_STRING;

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeTAsString()
        {
            final double v = roi.getBounds5D().getSizeT();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return MathUtil.INFINITE_STRING;

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeCAsString()
        {
            final double v = roi.getBounds5D().getSizeC();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return MathUtil.INFINITE_STRING;

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        @Override
        public void roiChanged(ROIEvent event)
        {
            switch (event.getType())
            {
                default:
                    // ROI selected ? --> propagate event control panel
                    if (roi.isSelected())
                        roiControlPanel.roiChanged(event);
                    break;

                case ROI_CHANGED:
                    // notify control panel that ROI changed
                    if (roi.isSelected())
                        roiControlPanel.roiChanged(event);

                    sequenceInfInvalid = true;
                    roiInfInvalid = true;
                    requestCompute();
                    break;

                case SELECTION_CHANGED:
                    // update ROI selection if not in internal selection change
                    if (modifySelection.availablePermits() > 0)
                        refreshTableData();
                    // roiSelectionChanged(this);
                    break;

                case PROPERTY_CHANGED:
                    final String property = event.getPropertyName();

                    if (ROI.PROPERTY_NAME.equals(property) || ROI.PROPERTY_ICON.equals(property))
                        refreshTableData();
                    // roiInfoUpdated(this);
                    break;
            }
        }

        public void sequenceChanged(SequenceEvent event)
        {
            // sequence content changed --> need to recompute intensity infos
            if (event.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            {
                sequenceInfInvalid = true;
                compute();
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            // consider same ROIInfo if the inner ROI is the same
            if (obj instanceof ROIInfo)
                return ((ROIInfo) obj).getROI() == getROI();

            return super.equals(obj);
        }
    }
}