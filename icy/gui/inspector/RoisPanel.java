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

import icy.clipboard.Clipboard;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.renderer.ImageTableCellRenderer;
import icy.gui.component.renderer.NativeArrayTableCellRenderer;
import icy.gui.main.ActiveSequenceListener;
import icy.image.IntensityInfo;
import icy.main.Icy;
import icy.math.ArrayMath;
import icy.math.MathUtil;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Stephane
 */
public class RoisPanel extends JPanel implements ActionListener, ActiveSequenceListener, TextChangeListener,
        ListSelectionListener, Runnable, PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    static final String[] columnNames = {"", "Name", "Type", "Contour points", "Points", "Perimeter", "Area",
            "Surface Area", "Volume", "Min Intensity", "Mean Intensity", "Max Intensity", "Position X", "Position Y",
            "Position Z", "Position T", "Position C", "Size X", "Size Y", "Size Z", "Size T", "Size C"};

    private static final String PREF_ID = "ROIPanel";

    private static final String ID_COLUMN_ICON = "col_icon";
    private static final String ID_COLUMN_NAME = "col_name";
    private static final String ID_COLUMN_TYPE = "col_type";
    private static final String ID_COLUMN_CONTOUR = "col_contour";
    private static final String ID_COLUMN_POINTS = "col_points";
    private static final String ID_COLUMN_PERIMETER = "col_perimeter";
    private static final String ID_COLUMN_AREA = "col_area";
    private static final String ID_COLUMN_SURFACE_AREA = "col_surface_area";
    private static final String ID_COLUMN_VOLUME = "col_volume";
    private static final String ID_COLUMN_MIN_INT = "col_min_int";
    private static final String ID_COLUMN_MEAN_INT = "col_mean_int";
    private static final String ID_COLUMN_MAX_INT = "col_max_int";
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
    final List<ROIInfo> roisToCompute;

    public RoisPanel()
    {
        super();

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
                    return "";

                final ROIInfo roiInfo = filteredRois.get(row);
                final ROI roi = roiInfo.getROI();
                double d;

                switch (column)
                {
                    case 0: // icon
                        return roi.getIcon();
                    case 1: // name
                        return roi.getName();
                    case 2: // type
                        return roi.getSimpleClassName();
                    case 3: // contour points
                        return Double.valueOf(roiInfo.getNumberOfContourPoints());
                    case 4: // points
                        return Double.valueOf(roiInfo.getNumberOfPoints());
                    case 5: // perimeter
                        return roiInfo.getPerimeter();
                    case 6: // area
                        return roiInfo.getArea();
                    case 7: // surface area
                        return roiInfo.getSurfaceArea();
                    case 8: // volume
                        return roiInfo.getVolume();
                    case 9: // min intensity
                        return roiInfo.getMinIntensities();
                    case 10: // mean intensity
                        return roiInfo.getMeanIntensities();
                    case 11: // max intensity
                        return roiInfo.getMaxIntensities();
                    case 12: // position X
                        return roiInfo.getPositionXAsString();
                    case 13: // position Y
                        return roiInfo.getPositionYAsString();
                    case 14: // position Z
                        return roiInfo.getPositionZAsString();
                    case 15: // position T
                        return roiInfo.getPositionTAsString();
                    case 16: // position C
                        return roiInfo.getPositionCAsString();
                    case 17: // size X
                        return roiInfo.getSizeXAsString();
                    case 18: // size Y
                        return roiInfo.getSizeYAsString();
                    case 19: // size Z
                        return roiInfo.getSizeZAsString();
                    case 20: // size T
                        return roiInfo.getSizeTAsString();
                    case 21: // size C
                        return roiInfo.getSizeCAsString();
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
                        return Image.class;
                    case 3: // contour points
                    case 4: // points
                        return Double.class;
                    case 9: // min intensity
                    case 10: // mean intensity
                    case 11: // max intensity
                        return double[].class;
                }
            }
        };
        // set table model
        table.setModel(tableModel);
        // alternate highlight
        table.addHighlighter(HighlighterFactory.createSimpleStriping());
        // disable extra actions from column control
        ((ColumnControlButton) table.getColumnControl()).setAdditionalActionsVisible(false);
        // // use custom copy command
        table.registerKeyboardAction(this, "Copy",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, SystemUtil.getMenuCtrlMask(), false), JComponent.WHEN_FOCUSED);

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
        final TableCellRenderer naTableCellRenderer = new NativeArrayTableCellRenderer();

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
        col.setToolTipText("ROI name (double click to edit)");
        col.addPropertyChangeListener(this);
        // type
        col = table.getColumnExt(2);
        col.setIdentifier(ID_COLUMN_TYPE);
        col.setPreferredWidth(80);
        col.setMinWidth(60);
        col.setToolTipText("ROI type");
        col.addPropertyChangeListener(this);
        // contour points
        col = table.getColumnExt(3);
        col.setIdentifier(ID_COLUMN_CONTOUR);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Number of contour points");
        col.addPropertyChangeListener(this);
        // points
        col = table.getColumnExt(4);
        col.setIdentifier(ID_COLUMN_POINTS);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Number of points");
        col.addPropertyChangeListener(this);
        // perimeter
        col = table.getColumnExt(5);
        col.setIdentifier(ID_COLUMN_PERIMETER);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Perimeter");
        col.addPropertyChangeListener(this);
        // area
        col = table.getColumnExt(6);
        col.setIdentifier(ID_COLUMN_AREA);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Area");
        col.addPropertyChangeListener(this);
        // surface area
        col = table.getColumnExt(7);
        col.setIdentifier(ID_COLUMN_SURFACE_AREA);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Surface Area");
        col.addPropertyChangeListener(this);
        // volume
        col = table.getColumnExt(8);
        col.setIdentifier(ID_COLUMN_VOLUME);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Volume");
        col.addPropertyChangeListener(this);
        // min intensity
        col = table.getColumnExt(9);
        col.setIdentifier(ID_COLUMN_MIN_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Minimum pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // mean intensity
        col = table.getColumnExt(10);
        col.setIdentifier(ID_COLUMN_MEAN_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Mean pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // max intensity
        col = table.getColumnExt(11);
        col.setIdentifier(ID_COLUMN_MAX_INT);
        col.setPreferredWidth(100);
        col.setMinWidth(40);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setToolTipText("Maximum pixel intensity (per channel)");
        col.addPropertyChangeListener(this);
        // position X
        col = table.getColumnExt(12);
        col.setIdentifier(ID_COLUMN_POSITION_X);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("X Position of the ROI");
        col.addPropertyChangeListener(this);
        // position Y
        col = table.getColumnExt(13);
        col.setIdentifier(ID_COLUMN_POSITION_Y);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Y Position of the ROI");
        col.addPropertyChangeListener(this);
        // position Z
        col = table.getColumnExt(14);
        col.setIdentifier(ID_COLUMN_POSITION_Z);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Z Position of the ROI");
        col.addPropertyChangeListener(this);
        // position T
        col = table.getColumnExt(15);
        col.setIdentifier(ID_COLUMN_POSITION_T);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("T Position of the ROI");
        col.addPropertyChangeListener(this);
        // position C
        col = table.getColumnExt(16);
        col.setIdentifier(ID_COLUMN_POSITION_C);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("C Position of the ROI");
        col.addPropertyChangeListener(this);
        // size X
        col = table.getColumnExt(17);
        col.setIdentifier(ID_COLUMN_SIZE_X);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("X dimension size of the ROI (width)");
        col.addPropertyChangeListener(this);
        // size Y
        col = table.getColumnExt(18);
        col.setIdentifier(ID_COLUMN_SIZE_Y);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Y dimension size of the ROI (heigth)");
        col.addPropertyChangeListener(this);
        // size Z
        col = table.getColumnExt(19);
        col.setIdentifier(ID_COLUMN_SIZE_Z);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("Z dimension size of the ROI (depth)");
        col.addPropertyChangeListener(this);
        // size T
        col = table.getColumnExt(20);
        col.setIdentifier(ID_COLUMN_SIZE_T);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("T dimension size of the ROI (time)");
        col.addPropertyChangeListener(this);
        // size C
        col = table.getColumnExt(21);
        col.setIdentifier(ID_COLUMN_SIZE_C);
        col.setPreferredWidth(60);
        col.setMinWidth(30);
        col.setToolTipText("C dimension size of the ROI (channel)");
        col.addPropertyChangeListener(this);

        // set selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        roisToCompute = new ArrayList<ROIInfo>();
        roiInfoComputer = new Thread(this, "ROI properties calculator");
        roiInfoComputer.setPriority(Thread.MIN_PRIORITY);
        roiInfoComputer.start();

        // load panel preferences
        loadPreferences();

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
        roiControlPanel = new RoiControlPanel();

        setLayout(new BorderLayout());
        add(nameFilter, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(roiControlPanel, BorderLayout.SOUTH);
        validate();
    }

    private XMLPreferences getPreferences()
    {
        return GeneralPreferences.getPreferences().node(PREF_ID);
    }

    private Sequence getSequence()
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
        loadColumnVisibility(pref, ID_COLUMN_CONTOUR, false);
        loadColumnVisibility(pref, ID_COLUMN_POINTS, false);
        loadColumnVisibility(pref, ID_COLUMN_PERIMETER, true);
        loadColumnVisibility(pref, ID_COLUMN_AREA, true);
        loadColumnVisibility(pref, ID_COLUMN_SURFACE_AREA, false);
        loadColumnVisibility(pref, ID_COLUMN_VOLUME, false);
        loadColumnVisibility(pref, ID_COLUMN_MIN_INT, false);
        loadColumnVisibility(pref, ID_COLUMN_MEAN_INT, false);
        loadColumnVisibility(pref, ID_COLUMN_MAX_INT, false);
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
            final ROIInfo roiInfo;

            synchronized (roisToCompute)
            {
                if (!roisToCompute.isEmpty())
                    roiInfo = roisToCompute.get(0);
                else
                    roiInfo = null;
            }

            if (roiInfo != null)
            {
                roiInfo.compute();

                // remove it from the compute list
                synchronized (roisToCompute)
                {
                    roisToCompute.remove(roiInfo);
                }
            }
            else
                ThreadUtil.sleep(10);
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
    private int getRoiTableIndex(ROI roi)
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
        return roiControlPanel.getSelectedRois();
    }

    protected List<ROI> getSelectedRoisInternal()
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

            if ((index >= 0) && (index < filteredRois.size()))
                result.add(filteredRois.get(index).getROI());
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

    protected boolean setSelectedRois(List<ROI> newSelected, List<ROI> oldSelected)
    {
        final int newSelectedSize = newSelected.size();
        final int oldSelectedSize = oldSelected.size();

        // easy optimization
        if ((newSelectedSize == 0) && (oldSelectedSize == 0))
            return false;

        // same selection size ?
        if (newSelectedSize == oldSelectedSize)
        {
            // same selection, don't need to update it
            if (new HashSet<ROI>(newSelected).containsAll(oldSelected))
                return false;
        }

        // at this point selection has changed
        setSelectedRoisInternal(newSelected);
        // selection changed
        selectionChanged(newSelected);

        return true;
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
        ThreadUtil.bgRunSingle(tableDataRefresher, true);
    }

    void refreshTableDataInternal()
    {
        final List<ROI> oldSelected = getSelectedRoisInternal();

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
        {
            // no change in ROI selection ?
            if (!setSelectedRois(sequence.getSelectedROIs(), oldSelected))
                // do internal selection as the TableDataChanged event cleared the selection
                setSelectedRoisInternal(oldSelected);
        }
    }

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

    void roiSelectionChanged(ROIInfo roiInfo)
    {
        // refresh informations for this ROI
        final ROI roi = roiInfo.getROI();
        final int index = getRoiTableIndex(roi);

        // check selection change
        if (index != -1)
        {
            // change selection if needed
            if (roi.isSelected() ^ tableSelectionModel.isSelectedIndex(index))
            {
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (roi.isSelected())
                            tableSelectionModel.addSelectionInterval(index, index);
                        else
                            tableSelectionModel.removeSelectionInterval(index, index);
                    }
                });
            }
        }
    }

    void roiInfoUpdated(ROIInfo roiInfo)
    {
        // refresh informations for this ROI
        final ROI roi = roiInfo.getROI();
        final int index = getRoiModelIndex(roi);

        // notify row changed
        if (index != -1)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    tableModel.fireTableRowsUpdated(index, index);
                }
            });
        }

        // also handle selection change
        roiSelectionChanged(roiInfo);
    }

    private void doCustomeCopy()
    {
        final StringBuffer sbf = new StringBuffer();
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = table.getColumnCount();
        final int numrows = table.getSelectedRowCount();
        final int[] rowsselected = table.getSelectedRows();

        for (int i = 0; i < numrows; i++)
        {
            for (int j = 1; j < numcols; j++)
            {
                final Object value = table.getValueAt(rowsselected[i], j);

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
            sbf.append("\n");
        }

        final StringSelection stsel = new StringSelection(sbf.toString());
        Clipboard.putSystem(stsel, stsel);
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
    public void actionPerformed(ActionEvent e)
    {
        doCustomeCopy();
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

        selectionChanged(getSelectedRoisInternal());
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
            final SequenceEventType type = event.getType();

            // changed event already handled by ROIInfo
            if ((type == SequenceEventType.ADDED) || (type == SequenceEventType.REMOVED))
                // refresh the ROI list
                refreshRois();
        }
    }

    public class ROIInfo implements ROIListener
    {
        private ROI roi;
        private IntensityInfo[] intensityInfos;

        // cached
        private double numberContourPoints;
        private double numberPoints;
        private boolean intensityInvalid;
        private boolean othersInvalid;

        public ROIInfo(ROI roi)
        {
            this.roi = roi;

            numberContourPoints = 0d;
            numberPoints = 0d;
            intensityInfos = new IntensityInfo[0];
            intensityInvalid = true;
            othersInvalid = true;

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
                if (othersInvalid)
                {
                    // refresh points number calculation
                    numberContourPoints = MathUtil.roundSignificant(roi.getNumberOfContourPoints(), 5);
                    numberPoints = MathUtil.roundSignificant(roi.getNumberOfPoints(), 5);

                    othersInvalid = false;
                }

                if (intensityInvalid)
                {
                    final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                    if (sequence != null)
                    {
                        // calculate intensity infos
                        final Rectangle5D bounds = roi.getBounds5D().createIntersection(sequence.getBounds5D());

                        final int minC = (int) bounds.getC();
                        final int sizeC = (int) bounds.getSizeC();

                        intensityInfos = new IntensityInfo[sizeC];

                        for (int c = 0; c < sizeC; c++)
                        {
                            final IntensityInfo ii = ROIUtil.getIntensityInfo(sequence, roi, -1, -1, minC + c);

                            // round values
                            ii.minIntensity = MathUtil.roundSignificant(ii.minIntensity, 5, true);
                            ii.meanIntensity = MathUtil.roundSignificant(ii.meanIntensity, 5, true);
                            ii.maxIntensity = MathUtil.roundSignificant(ii.maxIntensity, 5, true);

                            intensityInfos[c] = ii;
                        }
                    }
                    else
                        intensityInfos = new IntensityInfo[0];

                    intensityInvalid = false;
                }
            }
            catch (Throwable e)
            {
                // we can have some exception here as this is an asynch process (just ignore)
                if (e instanceof OutOfMemoryError)
                    System.err.println("Cannot compute ROI infos: Not enought memory !");
            }

            roiInfoUpdated(this);
        }

        void requestCompute()
        {
            synchronized (roisToCompute)
            {
                if (!roisToCompute.contains(this))
                    roisToCompute.add(this);
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

        public boolean isOthersOutdated()
        {
            return othersInvalid;
        }

        public boolean areIntensitiesOutdated()
        {
            return intensityInvalid;
        }

        public double getNumberOfContourPoints()
        {
            // need to recompute
            if (othersInvalid)
                requestCompute();

            return numberContourPoints;
        }

        public double getNumberOfPoints()
        {
            // need to recompute
            if (othersInvalid)
                requestCompute();

            return numberPoints;
        }

        private String getContourSize()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
                return sequence.calculateSize(getNumberOfContourPoints(), roi.getDimension() - 1, 5);

            return "";
        }

        private String getInteriorSize()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null)
                return sequence.calculateSize(getNumberOfPoints(), roi.getDimension(), 5);

            return "";
        }

        public String getPerimeter()
        {
            if (roi.getDimension() == 2)
                return getContourSize();

            return "";
        }

        public String getArea()
        {
            if (roi.getDimension() == 2)
                return getInteriorSize();

            return "";
        }

        public String getSurfaceArea()
        {
            if (roi.getDimension() == 3)
                return getContourSize();

            return "";
        }

        public String getVolume()
        {
            if (roi.getDimension() == 3)
                return getInteriorSize();

            return "";
        }

        public double[] getMinIntensities()
        {
            // need to recompute
            if (intensityInvalid)
                requestCompute();

            final double[] result = new double[intensityInfos.length];

            for (int i = 0; i < intensityInfos.length; i++)
                result[i] = intensityInfos[i].minIntensity;

            return result;
        }

        public double[] getMeanIntensities()
        {
            // need to recompute
            if (intensityInvalid)
                requestCompute();

            final double[] result = new double[intensityInfos.length];

            for (int i = 0; i < intensityInfos.length; i++)
                result[i] = intensityInfos[i].meanIntensity;

            return result;
        }

        public double[] getMaxIntensities()
        {
            // need to recompute
            if (intensityInvalid)
                requestCompute();

            final double[] result = new double[intensityInfos.length];

            for (int i = 0; i < intensityInfos.length; i++)
                result[i] = intensityInfos[i].maxIntensity;

            return result;
        }

        public IntensityInfo[] getIntensities()
        {
            // need to recompute
            if (intensityInvalid)
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
                return "inf.";

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeYAsString()
        {
            final double v = roi.getBounds5D().getSizeY();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return "inf.";

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeZAsString()
        {
            final double v = roi.getBounds5D().getSizeZ();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return "inf.";

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeTAsString()
        {
            final double v = roi.getBounds5D().getSizeT();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return "inf.";

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        public String getSizeCAsString()
        {
            final double v = roi.getBounds5D().getSizeC();

            // special case of infinite dimension
            if (v == Double.POSITIVE_INFINITY)
                return "inf.";

            return StringUtil.toString(MathUtil.roundSignificant(v, 5, true));
        }

        @Override
        public void roiChanged(ROIEvent event)
        {
            switch (event.getType())
            {
                case ROI_CHANGED:
                    intensityInvalid = true;
                    othersInvalid = true;
                    compute();
                    break;

                case SELECTION_CHANGED:
                    // refresh selection only
                    roiSelectionChanged(this);
                    break;
            }
        }

        public void sequenceChanged(SequenceEvent event)
        {
            // sequence content changed --> need to recompute intensity infos
            if (event.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            {
                intensityInvalid = true;
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