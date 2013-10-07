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
import icy.gui.component.renderer.ImageTableCellRenderer;
import icy.gui.component.renderer.NativeArrayTableCellRenderer;
import icy.gui.main.ActiveSequenceListener;
import icy.image.IntensityInfo;
import icy.main.Icy;
import icy.math.ArrayMath;
import icy.math.MathUtil;
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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Stephane
 */
public class RoisPanel extends JPanel implements ActiveSequenceListener, TextChangeListener, ListSelectionListener,
        Runnable
{

    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    static final String[] columnNames = {"", "Name", "Type", "Edge", "Content", "Min Int.", "Mean Int.", "Max Int."};

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

                switch (column)
                {
                    case 0: // icon
                        return roi.getIcon();
                    case 1: // name
                        return roi.getName();
                    case 2: // type
                        return roi.getSimpleClassName();
                    case 3: // edge points
                        return Double.valueOf(roiInfo.getNumberOfEdgePoints());
                    case 4: // points
                        return Double.valueOf(roiInfo.getNumberOfPoints());
                    case 5: // min intensity
                        return roiInfo.getMinIntensities();
                    case 6: // mean intensity
                        return roiInfo.getMeanIntensities();
                    case 7: // max intensity
                        return roiInfo.getMaxIntensities();
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
                    case 0: // icon
                        return Image.class;
                    default:
                    case 1: // name
                    case 2: // type
                        return String.class;
                    case 3: // edge points
                    case 4: // points
                        return Double.class;
                    case 5: // min intensity
                    case 6: // mean intensity
                    case 7: // max intensity
                        return double[].class;
                }
            }
        };
        // set table model
        table.setModel(tableModel);
        // alternate highlight
        table.addHighlighter(HighlighterFactory.createSimpleStriping());

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
        col.setPreferredWidth(26);
        col.setMinWidth(26);
        col.setCellRenderer(new ImageTableCellRenderer(24));
        // name
        col = table.getColumnExt(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setToolTipText("ROI name (double click to edit)");
        // type
        col = table.getColumnExt(2);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setVisible(false);
        col.setToolTipText("ROI type");
        // edge points
        col = table.getColumnExt(2);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Number of edge points");
        // points
        col = table.getColumnExt(3);
        col.setPreferredWidth(80);
        col.setMinWidth(40);
        col.setToolTipText("Number of content points");
        // min intensity
        col = table.getColumnExt(4);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setVisible(false);
        col.setToolTipText("Minimum pixel intensity (per channel)");
        // mean intensity
        col = table.getColumnExt(4);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setVisible(false);
        col.setToolTipText("Mean pixel intensity (per channel)");
        // max intensity
        col = table.getColumnExt(4);
        col.setPreferredWidth(100);
        col.setMinWidth(60);
        col.setCellRenderer(naTableCellRenderer);
        col.setComparator(daComparator);
        col.setVisible(false);
        col.setToolTipText("Maximum pixel intensity (per channel)");

        // set selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        roisToCompute = new ArrayList<ROIInfo>();
        roiInfoComputer = new Thread(this, "ROI properties calculator");
        roiInfoComputer.setPriority(Thread.MIN_PRIORITY);
        roiInfoComputer.start();

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
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setColumnControlVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

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

    private Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
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
        final List<ROIInfo> newFilteredRois = filterList(rois, nameFilter.getText());

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

    protected List<ROIInfo> filterList(List<ROIInfo> list, String filter)
    {
        final List<ROIInfo> result = new ArrayList<ROIInfo>();

        if (StringUtil.isEmpty(filter, true))
            result.addAll(list);
        else
        {
            final String text = filter.trim().toLowerCase();

            // filter on name
            for (ROIInfo roi : list)
                if (roi.getROI().getName().toLowerCase().indexOf(text) != -1)
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

    void roiInfoUpdated(ROIInfo roiInfo)
    {
        // refresh informations for this ROI
        final ROI roi = roiInfo.getROI();
        final int index = getRoiModelIndex(roi);

        // notify row changed
        if (index != -1)
            tableModel.fireTableRowsUpdated(index, index);
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
            if (event.getType() == SequenceEventType.CHANGED)
                // refresh table data
                refreshTableData();
            else
                // refresh the ROI list
                refreshRois();
        }
    }

    public class ROIInfo implements ROIListener
    {
        private ROI roi;
        private IntensityInfo[] intensityInfos;

        // cached
        private double numberEdgePoints;
        private double numberPoints;
        private boolean intensityInvalid;
        private boolean othersInvalid;

        public ROIInfo(ROI roi)
        {
            this.roi = roi;

            numberEdgePoints = 0d;
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
                    numberEdgePoints = MathUtil.roundSignificant(roi.getNumberOfEdgePoints(), 5);
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

        public boolean isPerimeterOutdated()
        {
            return othersInvalid;
        }

        public boolean isAreaOutdated()
        {
            return othersInvalid;
        }

        public boolean areIntensitiesOutdated()
        {
            return intensityInvalid;
        }

        public double getNumberOfEdgePoints()
        {
            // need to recompute
            if (othersInvalid)
                requestCompute();

            return numberEdgePoints;
        }

        public double getNumberOfPoints()
        {
            // need to recompute
            if (othersInvalid)
                requestCompute();

            return numberPoints;
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
                    // TODO: single ROI selection changed
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
