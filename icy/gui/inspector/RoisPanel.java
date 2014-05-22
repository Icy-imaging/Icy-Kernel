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
import icy.gui.main.ActiveSequenceListener;
import icy.gui.util.LookAndFeelUtil;
import icy.image.IntensityInfo;
import icy.main.Icy;
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
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.type.rectangle.Rectangle5D;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

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
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.sort.TableSortController;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
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

    // table columns informations
    static final ColumnInfo[] columnInfos = {
            new ColumnInfo("", ID_COLUMN_ICON, "", Image.class, 26, 26, true, false),
            new ColumnInfo("Name", ID_COLUMN_NAME, "ROI name (double click in a cell to edit)", String.class, 60, 100,
                    true, false),
            new ColumnInfo("Type", ID_COLUMN_TYPE, "ROI type", String.class, 60, 80, false, false),
            new ColumnInfo("Position X", ID_COLUMN_POSITION_X, "X Position of the ROI", String.class, 30, 60, false,
                    false),
            new ColumnInfo("Position Y", ID_COLUMN_POSITION_Y, "Y Position of the ROI", String.class, 30, 60, false,
                    false),
            new ColumnInfo("Position Z", ID_COLUMN_POSITION_Z, "Z Position of the ROI", String.class, 30, 60, false,
                    false),
            new ColumnInfo("Position T", ID_COLUMN_POSITION_T, "T Position of the ROI", String.class, 30, 60, false,
                    false),
            new ColumnInfo("Position C", ID_COLUMN_POSITION_C, "C Position of the ROI", String.class, 30, 60, false,
                    false),
            new ColumnInfo("Size X", ID_COLUMN_SIZE_X, "X dimension size of the ROI (width)", String.class, 30, 60,
                    false, false),
            new ColumnInfo("Size Y", ID_COLUMN_SIZE_Y, "Y dimension size of the ROI (heigth)", String.class, 30, 60,
                    false, false),
            new ColumnInfo("Size Z", ID_COLUMN_SIZE_Z, "Z dimension size of the ROI (depth)", String.class, 30, 60,
                    false, false),
            new ColumnInfo("Size T", ID_COLUMN_SIZE_T, "T dimension size of the ROI (time)", String.class, 30, 60,
                    false, false),
            new ColumnInfo("Size C", ID_COLUMN_SIZE_C, "C dimension size of the ROI (channel)", String.class, 30, 60,
                    false, false),
            new ColumnInfo("Contour", ID_COLUMN_CONTOUR, "Number of points for the contour", Double.class, 30, 60,
                    false, false),
            new ColumnInfo("Interior", ID_COLUMN_POINTS, "Number of points for the interior", Double.class, 30, 60,
                    false, false),
            new ColumnInfo("Perimeter", ID_COLUMN_PERIMETER, "Perimeter", String.class, 40, 80, true, false),
            new ColumnInfo("Area", ID_COLUMN_AREA, "Area", String.class, 40, 80, true, false),
            new ColumnInfo("Surface Area", ID_COLUMN_SURFACE_AREA, "Surface Area", String.class, 40, 80, false, false),
            new ColumnInfo("Volume", ID_COLUMN_VOLUME, "Volume", String.class, 40, 80, false, false),
            new ColumnInfo("Min Intensity", ID_COLUMN_MIN_INT, "Minimum pixel intensity", Double.class, 40, 100, false,
                    true),
            new ColumnInfo("Mean Intensity", ID_COLUMN_MEAN_INT, "Mean pixel intensity", Double.class, 40, 100, false,
                    true),
            new ColumnInfo("Max Intensity", ID_COLUMN_MAX_INT, "Maximum pixel intensity", Double.class, 40, 100, false,
                    true),
            new ColumnInfo("Std Deviation", ID_COLUMN_STANDARD_DEV, "Standard deviation", Double.class, 40, 100, false,
                    true)};

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
    final XMLPreferences preferences;
    final Semaphore modifySelection;
    // complete refresh of the table
    final Runnable roiListRefresher;
    final Runnable tableDataStructureRefresher;
    final Runnable tableDataRefresher;
    final Runnable tableSelectionRefresher;
    final Thread roiInfoComputer;
    final LinkedBlockingQueue<ROIInfo> roisToCompute;
    int columnCount;

    public RoisPanel()
    {
        super("ROI", "roiPanel", new Point(100, 100), new Dimension(400, 600));

        preferences = GeneralPreferences.getPreferences().node(PREF_ID);
        rois = new ArrayList<ROIInfo>();
        filteredRois = new ArrayList<ROIInfo>();
        modifySelection = new Semaphore(1);
        columnCount = 0;

        roiListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshRoisInternal();
            }
        };
        tableDataStructureRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataStructureInternal();
            }
        };
        tableDataRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableDataInternal();
            }
        };
        tableSelectionRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshTableSelectionInternal();
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
                return ((TableColumnModelExt) table.getColumnModel()).getColumnCount(true);
            }

            @Override
            public String getColumnName(int column)
            {
                final ColumnInfo ci = getTableColumnInfo(column);

                if (ci != null)
                {
                    String result = ci.name;

                    if (ci.channelInfo)
                        result += getTableChannelName(getTableChannelIndex(column));

                    return result;
                }

                return "";
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
                final int columnInd = getTableColumnInfoIndex(column);
                final int channelInd = getTableChannelIndex(column);

                switch (columnInd)
                {
                    case 0: // icon
                        return roi.getIcon();
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
                        return Double.valueOf(roiInfo.getMinIntensities(channelInd));
                    case 20: // mean intensity
                        return Double.valueOf(roiInfo.getMeanIntensities(channelInd));
                    case 21: // max intensity
                        return Double.valueOf(roiInfo.getMaxIntensities(channelInd));
                    case 22: // standard deviation
                        return Double.valueOf(roiInfo.getStandardDeviation(channelInd));
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
                final ColumnInfo ci = getTableColumnInfo(column);

                if (ci != null)
                    return ci.type;

                return String.class;
            }
        };

        // set table model
        table.setModel(tableModel);
        // modify column properties
        buildTableColumns();
        table.getTableHeader();
        // alternate highlight
        table.setHighlighters(HighlighterFactory.createSimpleStriping());
        // disable extra actions from column control
        ((ColumnControlButton) table.getColumnControl()).setAdditionalActionsVisible(false);

        // set selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        roisToCompute = new LinkedBlockingQueue<ROIInfo>();
        roiInfoComputer = new Thread(this, "ROI properties calculator");
        roiInfoComputer.setPriority(Thread.MIN_PRIORITY);
        roiInfoComputer.start();

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
        table.setAutoCreateRowSorter(false);
        table.setAutoCreateColumnsFromModel(false);

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

    boolean buildTableColumns()
    {
        final int newColCount = getTableColumnCount();

        // nothing to change
        if (columnCount == newColCount)
            return false;
        final TableColumnModelExt colModel = (TableColumnModelExt) table.getColumnModel();
        final List<TableColumn> columns = colModel.getColumns(true);

        // remove row sorter the time we update columns
        table.setRowSorter(null);

        // TODO: try to find a way to disable table refresh while modifying column

        // and regenerate them
        for (int i = 0; i < newColCount; i++)
        {
            final ColumnInfo ci = getTableColumnInfo(i);

            // can't retrieve column informations --> pass to the next
            if (ci == null)
                continue;

            final TableColumnExt col;

            if (i >= columns.size())
                col = new TableColumnExt(i);
            else
                col = (TableColumnExt) columns.get(i);

            // build column name & tool tip
            String name = ci.name;
            String toolTip = ci.toolTip;

            if (ci.channelInfo)
            {
                name += getTableChannelName(getTableChannelIndex(i));
                toolTip += getTableChannelName(getTableChannelIndex(i));
            }

            // column changed ?
            if ((!name.equals(col.getHeaderValue())) || (!ci.id.equals(col.getIdentifier())))
            {
                col.setIdentifier(ci.id);
                col.setMinWidth(ci.minSize);
                col.setPreferredWidth(ci.preferredSize);
                col.setHeaderValue(name);
                col.setToolTipText(toolTip);
                col.setVisible(preferences.getBoolean(ci.id, ci.defVisible));
                col.setModelIndex(i);

                // special icon index
                if (i == 0)
                {
                    col.setMaxWidth(ci.preferredSize);
                    col.setCellRenderer(new ImageTableCellRenderer(ci.preferredSize - 2));
                    col.setResizable(false);
                }

                // need to add this column ?
                if (i >= columns.size())
                {
                    col.addPropertyChangeListener(this);
                    // add the column
                    colModel.addColumn(col);
                }
            }
        }

        // remove old columns no more in use
        for (int i = newColCount; i < columns.size(); i++)
        {
            final TableColumn col = columns.get(i);

            // remove listener
            col.removePropertyChangeListener(this);
            // then remove column
            colModel.removeColumn(col);
        }

        // set row sorter back
        table.setRowSorter(new TableSortController<TableModel>(tableModel));
        // and store new number of column
        columnCount = newColCount;

        return true;
    }

    /**
     * Returns number of channel of current sequence
     */
    int getChannelCount()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
            return sequence.getSizeC();

        return 1;
    }

    /**
     * Returns table column suffix for the specified channel
     */
    String getTableChannelName(int ind)
    {
        final Sequence sequence = getSequence();

        if ((sequence != null) && (ind < sequence.getSizeC()))
            return " (" + sequence.getChannelName(ind) + ")";

        return "";
    }

    /**
     * Get number of column in the table.
     */
    int getTableColumnCount()
    {
        final int channelCnt = getChannelCount();

        int res = 0;
        for (int i = 0; i < columnInfos.length; i++)
        {
            final ColumnInfo ci = columnInfos[i];

            if (ci.channelInfo)
                res += channelCnt;
            else
                res++;
        }

        return res;
    }

    /**
     * Get column info index for specified column index.
     */
    int getTableColumnInfoIndex(int index)
    {
        final int channelCnt = getChannelCount();

        int ind = 0;
        for (int i = 0; i < columnInfos.length; i++)
        {
            final ColumnInfo ci = columnInfos[i];

            if (ind == index)
                return i;

            if (ci.channelInfo)
            {
                ind += channelCnt;
                if (ind > index)
                    return i;
            }
            else
                ind++;
        }

        return -1;
    }

    /**
     * Get column info for specified column index.
     */
    ColumnInfo getTableColumnInfo(int index)
    {
        final int ind = getTableColumnInfoIndex(index);

        if (ind != -1)
            return columnInfos[ind];

        return null;
    }

    /**
     * Get the channel index represented by the specified column index.
     */
    int getTableChannelIndex(int index)
    {
        final int channelCnt = getChannelCount();

        int ind = 0;
        for (int i = 0; i < columnInfos.length; i++)
        {
            final ColumnInfo ci = columnInfos[i];

            if (ind == index)
                return 0;

            if (ci.channelInfo)
            {
                ind += channelCnt;
                if (ind > index)
                    return channelCnt - (ind - index);
            }
            else
                ind++;
        }

        return 0;
    }

    private XMLPreferences getPreferences()
    {
        return GeneralPreferences.getPreferences().node(PREF_ID);
    }

    Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
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

    /**
     * Select the specified list of ROI in the ROI Table
     */
    protected void setSelectedRoisInternal(HashSet<ROI> newSelected)
    {
        final List<ROIInfo> modelRois = filteredRois;

        // start selection change
        tableSelectionModel.setValueIsAdjusting(true);
        try
        {
            // start by clearing selection
            tableSelectionModel.clearSelection();

            for (int i = 0; i < modelRois.size(); i++)
            {
                final ROI roi = modelRois.get(i).getROI();

                // HashSet provide fast "contains"
                if (newSelected.contains(roi))
                {
                    int ind;

                    try
                    {
                        // convert model index to view index
                        ind = table.convertRowIndexToView(i);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        ind = -1;
                    }

                    if (ind > -1)
                        tableSelectionModel.addSelectionInterval(ind, ind);
                }
            }
        }
        finally
        {
            // end selection change
            tableSelectionModel.setValueIsAdjusting(false);
        }
    }

    protected List<ROIInfo> getFilteredList(String filter)
    {
        final List<ROIInfo> result = new ArrayList<ROIInfo>();

        // no need to synchronize on 'rois' as it can be only modified prior to this call
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

        return result;
    }

    /**
     * refresh ROI list
     */
    protected void refreshRois()
    {
        ThreadUtil.runSingle(roiListRefresher);
    }

    /**
     * refresh ROI list
     */
    protected void refreshRoisInternal()
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
                    roisToCompute.remove(rois.remove(i));

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
        refreshTableDataStructure();
    }

    public void refreshTableDataStructure()
    {
        ThreadUtil.runSingle(tableDataStructureRefresher);
    }

    void refreshTableDataStructureInternal()
    {
        // don't eat too much time on data structure refresh
        ThreadUtil.sleep(10);

        final HashSet<ROI> newSelectedRois;
        final Sequence sequence = getSequence();

        if (sequence != null)
            newSelectedRois = new HashSet<ROI>(sequence.getSelectedROIs());
        else
            newSelectedRois = new HashSet<ROI>();

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                modifySelection.acquireUninterruptibly();
                try
                {
                    // rebuild columns
                    if (!buildTableColumns())
                        // notify table data changed
                        tableModel.fireTableDataChanged();

                    // selection to restore ?
                    if (!newSelectedRois.isEmpty())
                        setSelectedRoisInternal(newSelectedRois);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    modifySelection.release();
                }
            }
        });

        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    public void refreshTableData()
    {
        ThreadUtil.runSingle(tableDataRefresher);
    }

    void refreshTableDataInternal()
    {
        // don't eat too much time on data structure refresh
        ThreadUtil.sleep(10);

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                final int rowCount = table.getRowCount();

                if (rowCount > 0)
                    tableModel.fireTableRowsUpdated(0, rowCount - 1);
            }
        });

        // notify the ROI control panel that selection changed (force data refresh)
        roiControlPanel.selectionChanged();
    }

    public void refreshTableSelection()
    {
        ThreadUtil.runSingle(tableSelectionRefresher);
    }

    void refreshTableSelectionInternal()
    {
        // don't eat too much time on selection refresh
        ThreadUtil.sleep(10);

        final HashSet<ROI> newSelectedRois;
        final List<ROI> currentSelectedRois = getSelectedRois();
        final Sequence sequence = getSequence();

        if (sequence != null)
            newSelectedRois = new HashSet<ROI>(sequence.getSelectedROIs());
        else
            newSelectedRois = new HashSet<ROI>();

        // selection changed ?
        if (!CollectionUtil.equals(currentSelectedRois, newSelectedRois))
        {
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    modifySelection.acquireUninterruptibly();
                    tableSelectionModel.setValueIsAdjusting(true);
                    try
                    {
                        // set new selection
                        setSelectedRoisInternal(newSelectedRois);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        tableSelectionModel.setValueIsAdjusting(false);
                        // important to release it after the valueIsAdjusting
                        modifySelection.release();
                    }
                }
            });

            // notify the ROI control panel that selection changed
            roiControlPanel.selectionChanged();
        }
    }

    /**
     * @deprecated Use {@link #getCSVFormattedInfos()} instead.
     */
    @Deprecated
    public String getCSVFormattedInfosOfSelectedRois()
    {
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = table.getColumnCount(true);
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
     * Returns all ROI informations in CSV format (tab separated) immediately.
     */
    public String getCSVFormattedInfos()
    {
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = tableModel.getColumnCount();
        final int numrows = tableModel.getRowCount();

        // table is empty --> returns empty string
        if (numrows == 0)
            return "";

        final StringBuffer sbf = new StringBuffer();

        // column name
        for (int j = 1; j < numcols; j++)
        {
            sbf.append(tableModel.getColumnName(j));
            if (j < numcols - 1)
                sbf.append("\t");
        }
        sbf.append("\r\n");

        // then content
        for (int i = 0; i < numrows; i++)
        {
            for (int j = 1; j < numcols; j++)
            {
                final Object value = tableModel.getValueAt(i, j);

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
        // currently changing the selection ? --> exit
        if (e.getValueIsAdjusting() || !modifySelection.tryAcquire())
            return;

        // semaphore acquired here
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

    @Override
    public void sequenceActivated(Sequence value)
    {
        // force column rebuild
        columnCount = 0;
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
        // if (modifySelection.availablePermits() == 0)
        // return;

        final SequenceEventSourceType sourceType = event.getSourceType();

        switch (sourceType)
        {
            case SEQUENCE_ROI:
                final SequenceEventType type = event.getType();

                // changed event already handled by ROIInfo
                if ((type == SequenceEventType.ADDED) || (type == SequenceEventType.REMOVED))
                    // refresh the ROI list
                    refreshRois();
                break;

            case SEQUENCE_DATA:
                // notify ROI info that sequence data changed
                for (ROIInfo roiInfo : filteredRois)
                    roiInfo.sequenceDataChanged();

                // if data changed (more Z or T) we need to refresh action
                // so we can change ROI position correctly
                roiControlPanel.refreshROIActions();
                break;

            case SEQUENCE_TYPE:
                // if type changed (number of channel) we need to refresh action
                // so we change change ROI position correctly
                roiControlPanel.refreshROIActions();
                break;
        }
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

            requestCompute();

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
                    final Sequence sequence = getSequence();

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
            final Sequence seq = getSequence();
            if (seq == null)
                return "";

            return ROIUtil.getContourSize(seq, getNumberOfContourPoints(), roi, 2, 5);
        }

        public String getArea()
        {
            final Sequence seq = getSequence();
            if (seq == null)
                return "";

            return ROIUtil.getInteriorSize(seq, getNumberOfPoints(), roi, 2, 5);
        }

        public String getSurfaceArea()
        {
            final Sequence seq = getSequence();
            if (seq == null)
                return "";

            return ROIUtil.getContourSize(seq, getNumberOfContourPoints(), roi, 3, 5);
        }

        public String getVolume()
        {
            final Sequence seq = getSequence();
            if (seq == null)
                return "";

            return ROIUtil.getInteriorSize(seq, getNumberOfPoints(), roi, 3, 5);
        }

        public double getMinIntensities(int channel)
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            if (channel < infos.length)
                return infos[channel].minIntensity;

            return 0d;
        }

        public double getMeanIntensities(int channel)
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            if (channel < infos.length)
                return infos[channel].meanIntensity;

            return 0d;
        }

        public double getMaxIntensities(int channel)
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final IntensityInfo[] infos = intensityInfos;
            if (channel < infos.length)
                return infos[channel].maxIntensity;

            return 0d;
        }

        public double getStandardDeviation(int channel)
        {
            // need to recompute
            if (sequenceInfInvalid)
                requestCompute();

            final double[] stdDev = standardDeviation;
            if (channel < stdDev.length)
                return stdDev[channel];

            return 0d;
        }

        // public IntensityInfo[] getIntensities()
        // {
        // // need to recompute
        // if (sequenceInfInvalid)
        // requestCompute();
        //
        // return intensityInfos;
        // }

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
                    // update ROI selection
                    refreshTableSelection();
                    break;

                case PROPERTY_CHANGED:
                    final String property = event.getPropertyName();

                    if (ROI.PROPERTY_NAME.equals(property) || ROI.PROPERTY_ICON.equals(property))
                        refreshTableData();
                    break;
            }
        }

        public void sequenceDataChanged()
        {
            sequenceInfInvalid = true;
            requestCompute();
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

    static class ColumnInfo
    {
        final String id;
        final String name;
        final String toolTip;
        final Class<?> type;
        final int minSize;
        final int preferredSize;
        final boolean defVisible;
        final boolean channelInfo;

        ColumnInfo(String name, String id, String toolTip, Class<?> type, int minSize, int preferredSize,
                boolean defVisible, boolean channelInfo)
        {
            super();

            this.name = name;
            this.id = id;
            this.toolTip = toolTip;
            this.type = type;
            this.minSize = minSize;
            this.preferredSize = preferredSize;
            this.defVisible = defVisible;
            this.channelInfo = channelInfo;
        }
    }
}