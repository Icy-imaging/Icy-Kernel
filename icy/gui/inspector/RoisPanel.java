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
import icy.gui.component.button.IcyButton;
import icy.gui.component.renderer.ImageTableCellRenderer;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.preferences.GeneralPreferences;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.system.thread.InstanceProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.sort.DefaultSortController;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.pushingpixels.substance.api.skin.SkinChangeListener;

import plugins.kernel.roi.descriptor.intensity.ROIMaxIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMeanIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMinIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROISumIntensityDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIContourDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIInteriorDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterCDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterTDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterXDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterYDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterZDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIPerimeterDescriptor;
import plugins.kernel.roi.descriptor.measure.ROISurfaceAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIVolumeDescriptor;
import plugins.kernel.roi.descriptor.property.ROIIconDescriptor;
import plugins.kernel.roi.descriptor.property.ROINameDescriptor;
import plugins.kernel.roi.descriptor.property.ROIOpacityDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionCDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionTDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionXDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionYDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionZDescriptor;
import plugins.kernel.roi.descriptor.property.ROIReadOnlyDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeCDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeTDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeXDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeYDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeZDescriptor;

/**
 * @author Stephane
 */
public class RoisPanel extends ExternalizablePanel implements ActiveSequenceListener, TextChangeListener,
        ListSelectionListener, Runnable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2870878233087117178L;

    private static final String PREF_ID = "ROIPanel";

    private static final String ID_VIEW = "view";
    private static final String ID_EXPORT = "export";

    private static final String ID_PROPERTY_MINSIZE = "minSize";
    private static final String ID_PROPERTY_MAXSIZE = "maxSize";
    private static final String ID_PROPERTY_DEFAULTSIZE = "defaultSize";
    private static final String ID_PROPERTY_ORDER = "order";
    private static final String ID_PROPERTY_VISIBLE = "visible";

    // default row comparator
    static Comparator<Object> comparator = new Comparator<Object>()
    {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public int compare(Object o1, Object o2)
        {
            if (o1 == null)
            {
                if (o2 == null)
                    return 0;
                return -1;
            }
            if (o2 == null)
                return 1;

            Object obj1 = o1;
            Object obj2 = o2;

            if (o1 instanceof String)
            {
                if (o1.equals("-" + MathUtil.INFINITE_STRING))
                    obj1 = Double.valueOf(Double.NEGATIVE_INFINITY);
                else if (o1.equals(MathUtil.INFINITE_STRING))
                    obj1 = Double.valueOf(Double.POSITIVE_INFINITY);
            }

            if (o2 instanceof String)
            {
                if (o2.equals("-" + MathUtil.INFINITE_STRING))
                    obj2 = Double.valueOf(Double.NEGATIVE_INFINITY);
                else if (o2.equals(MathUtil.INFINITE_STRING))
                    obj2 = Double.valueOf(Double.POSITIVE_INFINITY);
            }

            if ((obj1 instanceof Number) && (obj2 instanceof Number))
            {
                final double d1 = ((Number) obj1).doubleValue();
                final double d2 = ((Number) obj2).doubleValue();

                if (Double.isNaN(d1))
                {
                    if (Double.isNaN(d2))
                        return 0;
                    return -1;
                }
                if (Double.isNaN(d2))
                    return 1;

                if (d1 < d2)
                    return -1;
                if (d1 > d2)
                    return 1;

                return 0;
            }
            else if ((obj1 instanceof Comparable) && (obj1.getClass() == obj2.getClass()))
                return ((Comparable) obj1).compareTo(obj2);

            return o1.toString().compareTo(o2.toString());
        }
    };

    // GUI
    ROITableModel roiTableModel;
    ListSelectionModel roiSelectionModel;
    JXTable roiTable;
    IcyTextField nameFilter;
    JLabel roiNumberLabel;
    JLabel selectedRoiNumberLabel;
    RoiControlPanel roiControlPanel;

    // PluginDescriptors / ROIDescriptor map
    Map<ROIDescriptor, PluginROIDescriptor> descriptorMap;
    // Descriptor / column info (static to the class)
    List<ColumnInfo> columnInfoList;
    // // last visible columns (used to detect change in column configuration)
    // List<String> lastVisibleColumnIds;

    // ROI info list cache
    Set<ROI> roiSet;
    Map<ROI, ROIResults> roiResultsMap;
    List<ROI> filteredRoiList;
    List<ROIResults> filteredRoiResultsList;

    // internals
    final XMLPreferences basePreferences;
    final XMLPreferences viewPreferences;
    final XMLPreferences exportPreferences;
    final Semaphore modifySelection;
    // complete refresh of the roiTable
    final Runnable roiListRefresher;
    final Runnable filteredRoiListRefresher;
    final Runnable tableDataStructureRefresher;
    final Runnable tableDataRefresher;
    final Runnable tableSelectionRefresher;
    final Runnable columnInfoListRefresher;
    final Thread roiInfoComputer;
    final InstanceProcessor processor;

    final LinkedHashSet<ROIResults> descriptorsToCompute;

    public RoisPanel()
    {
        super("ROI", "roiPanel", new Point(100, 100), new Dimension(400, 600));

        basePreferences = GeneralPreferences.getPreferences().node(PREF_ID);
        viewPreferences = basePreferences.node(ID_VIEW);
        exportPreferences = basePreferences.node(ID_EXPORT);

        roiSet = new HashSet<ROI>();
        roiResultsMap = new HashMap<ROI, ROIResults>();
        filteredRoiList = new ArrayList<ROI>();
        filteredRoiResultsList = new ArrayList<ROIResults>();
        modifySelection = new Semaphore(1);
        columnInfoList = new ArrayList<ColumnInfo>();

        initialize();

        roiListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshRoisInternal();
            }
        };
        filteredRoiListRefresher = new Runnable()
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
        columnInfoListRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshColumnInfoListInternal();
            }
        };

        LookAndFeelUtil.addSkinChangeListener(new SkinChangeListener()
        {
            @Override
            public void skinChanged()
            {
                final TableColumnModel columnModel = roiTable.getColumnModel();

                for (int i = 0; i < columnModel.getColumnCount(); i++)
                {
                    final TableColumn column = columnModel.getColumn(i);

                    // need to reset specific renderer as background color can be wrong
                    if (column.getCellRenderer() instanceof ImageTableCellRenderer)
                        column.setCellRenderer(new ImageTableCellRenderer(18));
                }

                // modify highlighter
                roiTable.setHighlighters(HighlighterFactory.createSimpleStriping());
            }
        });

        processor = new InstanceProcessor();
        processor.setThreadName("ROI panel GUI refresher");
        processor.setKeepAliveTime(30, TimeUnit.SECONDS);

        descriptorsToCompute = new LinkedHashSet<ROIResults>(256);
        roiInfoComputer = new Thread(this, "ROI descriptor calculator");
        roiInfoComputer.setPriority(Thread.MIN_PRIORITY);
        roiInfoComputer.start();

        // update descriptors list (this rebuild the column model of the tree table)
        refreshDescriptorList();
        // set shortcuts
        buildActionMap();

        refreshRois();
    }

    private void initialize()
    {
        // need filter before load
        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Filter ROI by name");
        nameFilter.addTextChangeListener(this);

        selectedRoiNumberLabel = new JLabel("0");
        roiNumberLabel = new JLabel("0");

        // build roiTable model
        roiTableModel = new ROITableModel();

        // build roiTable
        roiTable = new JXTable(roiTableModel);
        roiTable.setAutoStartEditOnKeyStroke(false);
        roiTable.setAutoCreateRowSorter(false);
        roiTable.setAutoCreateColumnsFromModel(false);
        roiTable.setShowVerticalLines(false);
        roiTable.setColumnControlVisible(false);
        roiTable.setColumnSelectionAllowed(false);
        roiTable.setRowSelectionAllowed(true);
        roiTable.setSortable(true);
        // set highlight
        roiTable.setHighlighters(HighlighterFactory.createSimpleStriping());

        // set header settings
        final JTableHeader tableHeader = roiTable.getTableHeader();
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(true);

        // set selection model
        roiSelectionModel = roiTable.getSelectionModel();
        roiSelectionModel.addListSelectionListener(this);
        roiSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        roiTable.setRowSorter(new ROITableSortController<ROITableModel>());

        final JPanel middlePanel = new JPanel(new BorderLayout(0, 0));

        middlePanel.add(roiTable.getTableHeader(), BorderLayout.NORTH);
        middlePanel.add(new JScrollPane(roiTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // build control panel
        roiControlPanel = new RoiControlPanel(this);

        final IcyButton settingButton = new IcyButton(RoiActions.settingAction);
        settingButton.setHideActionText(true);
        settingButton.setFlat(true);

        setLayout(new BorderLayout());
        add(GuiUtil.createLineBoxPanel(nameFilter, Box.createHorizontalStrut(8), selectedRoiNumberLabel, new JLabel(
                " / "), roiNumberLabel, Box.createHorizontalStrut(4), settingButton), BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(roiControlPanel, BorderLayout.SOUTH);

        validate();
    }

    void buildActionMap()
    {
        final InputMap imap = roiTable.getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap amap = roiTable.getActionMap();

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
     * Returns roiTable column suffix for the specified channel
     */
    String getChannelNameSuffix(int ch)
    {
        final Sequence sequence = getSequence();

        if ((sequence != null) && (ch < getChannelCount()))
            return " (" + sequence.getChannelName(ch) + ")";

        return "";
    }

    /**
     * Returns ROI descriptor given its id.
     */
    ROIDescriptor getROIDescriptor(String descriptorId)
    {
        synchronized (descriptorMap)
        {
            for (ROIDescriptor descriptor : descriptorMap.keySet())
                if (descriptor.getId().equals(descriptorId))
                    return descriptor;
        }

        return null;
    }

    /**
     * Get column info for specified column index.
     */
    ColumnInfo getColumnInfo(List<ColumnInfo> columns, int column)
    {
        if (column < columns.size())
            return columns.get(column);

        return null;
    }

    /**
     * Get column info for specified column index.
     */
    ColumnInfo getColumnInfo(int column)
    {
        return getColumnInfo(columnInfoList, column);
    }

    ColumnInfo getColumnInfo(List<ColumnInfo> columns, ROIDescriptor descriptor, int channel)
    {
        for (ColumnInfo ci : columns)
            if (ci.descriptor.equals(descriptor) && (ci.channel == channel))
                return ci;

        return null;
    }

    ColumnInfo getColumnInfo(ROIDescriptor descriptor, int channel)
    {
        return getColumnInfo(columnInfoList, descriptor, channel);
    }

    Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                final Object[] roiResultsList;
                final Sequence seq = getSequence();

                synchronized (descriptorsToCompute)
                {
                    while (descriptorsToCompute.isEmpty())
                        descriptorsToCompute.wait();

                    // get results to compute
                    roiResultsList = descriptorsToCompute.toArray();
                    // and remove them
                    descriptorsToCompute.clear();
                }

                for (Object object : roiResultsList)
                {
                    final ROIResults roiResults = (ROIResults) object;
                    final Map<ColumnInfo, DescriptorResult> results = roiResults.descriptorResults;
                    final Object[] keys;

                    synchronized (results)
                    {
                        keys = results.keySet().toArray();
                    }

                    for (Object key : keys)
                    {
                        final ColumnInfo columnInfo = (ColumnInfo) key;
                        final ROIDescriptor descriptor = columnInfo.descriptor;
                        final DescriptorResult result;

                        synchronized (results)
                        {
                            // get result
                            result = results.get(key);
                        }

                        // need to refresh this column result
                        if ((result != null) && result.isOutdated())
                        {
                            // get the corresponding plugin
                            final PluginROIDescriptor plugin;

                            synchronized (descriptorMap)
                            {
                                plugin = descriptorMap.get(descriptor);
                            }

                            if (plugin != null)
                            {
                                final Map<ROIDescriptor, Object> newResults;

                                try
                                {
                                    // need computation per channel ?
                                    if (descriptor.useSequenceData())
                                        newResults = plugin.compute(roiResults.getRoiForChannel(columnInfo.channel),
                                                seq);
                                    else
                                        newResults = plugin.compute(roiResults.roi, seq);

                                    for (Entry<ROIDescriptor, Object> entryNewResult : newResults.entrySet())
                                    {
                                        // get the column for this result
                                        final ColumnInfo resultColumnInfo = getColumnInfo(entryNewResult.getKey(),
                                                columnInfo.channel);
                                        final DescriptorResult oResult;

                                        synchronized (results)
                                        {
                                            // get corresponding result
                                            oResult = results.get(resultColumnInfo);
                                        }

                                        if (oResult != null)
                                        {
                                            // set the result value
                                            oResult.setValue(entryNewResult.getValue());
                                            // result is up to date
                                            oResult.setOutdated(false);
                                        }
                                    }
                                }
                                catch (UnsupportedOperationException e)
                                {
                                    // not supported --> clear associated results and set them as computed
                                    for (ROIDescriptor desc : plugin.getDescriptors())
                                    {
                                        // get the column for this result
                                        final ColumnInfo resultColumnInfo = getColumnInfo(desc, columnInfo.channel);
                                        final DescriptorResult oResult;

                                        synchronized (results)
                                        {
                                            // get corresponding result
                                            oResult = results.get(resultColumnInfo);
                                        }

                                        if (oResult != null)
                                        {
                                            oResult.setValue(null);
                                            oResult.setOutdated(false);
                                        }
                                    }
                                }

                                // refresh table
                                refreshTableData();
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }

    /**
     * Return index of specified ROI in the filtered ROI list
     */
    private int getRoiIndex(ROI roi)
    {
        final int result = Collections.binarySearch(filteredRoiList, roi, ROI.idComparator);

        if (result >= 0)
            return result;

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
     * Return index of specified ROI in the table (view)
     */
    int getRoiViewIndex(ROI roi)
    {
        final int ind = getRoiModelIndex(roi);

        if (ind == -1)
            return ind;

        try
        {
            return roiTable.convertRowIndexToView(ind);
        }
        catch (IndexOutOfBoundsException e)
        {
            return -1;
        }
    }

    ROIResults getRoiResults(int rowModelIndex)
    {
        final List<ROIResults> entries = filteredRoiResultsList;

        if ((rowModelIndex >= 0) && (rowModelIndex < entries.size()))
            return entries.get(rowModelIndex);

        return null;
    }

    /**
     * Returns the visible ROI in the ROI control panel.
     */
    public List<ROI> getVisibleRois()
    {
        return new ArrayList<ROI>(filteredRoiList);
    }

    // /**
    // * Returns the ROI informations for the specified ROI.
    // */
    // public ROIInfo getROIInfo(ROI roi)
    // {
    // final int index = getRoiIndex(roi);
    //
    // if (index != -1)
    // return filteredRois.get(index);
    //
    // return null;
    // }

    /**
     * Returns the number of selected ROI from the table.
     */
    public int getSelectedRoisCount()
    {
        int result = 0;

        synchronized (roiSelectionModel)
        {
            if (!roiSelectionModel.isSelectionEmpty())
            {
                for (int i = roiSelectionModel.getMinSelectionIndex(); i <= roiSelectionModel.getMaxSelectionIndex(); i++)
                    if (roiSelectionModel.isSelectedIndex(i))
                        result++;
            }
        }

        return result;
    }

    /**
     * Returns the selected ROI from the table.
     */
    public List<ROI> getSelectedRois()
    {
        final List<ROIResults> roiResults = filteredRoiResultsList;
        final List<ROI> result = new ArrayList<ROI>(roiResults.size());

        synchronized (roiSelectionModel)
        {
            if (!roiSelectionModel.isSelectionEmpty())
            {
                for (int i = roiSelectionModel.getMinSelectionIndex(); i <= roiSelectionModel.getMaxSelectionIndex(); i++)
                {
                    if (roiSelectionModel.isSelectedIndex(i))
                    {
                        try
                        {
                            final int index = roiTable.convertRowIndexToModel(i);

                            if ((index >= 0) && (index < roiResults.size()))
                                result.add(roiResults.get(index).roi);
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            // ignore
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Select the specified list of ROI in the ROI Table
     */
    protected void setSelectedRoisInternal(Set<ROI> newSelected)
    {
        final List<Integer> selectedIndexes = new ArrayList<Integer>();
        final List<ROI> roiList = filteredRoiList;

        for (int i = 0; i < roiList.size(); i++)
        {
            final ROI roi = roiList.get(i);

            // HashSet provides fast "contains"
            if (newSelected.contains(roi))
            {
                int ind;

                try
                {
                    // convert model index to view index
                    ind = roiTable.convertRowIndexToView(i);
                }
                catch (IndexOutOfBoundsException e)
                {
                    ind = -1;
                }

                if (ind > -1)
                    selectedIndexes.add(Integer.valueOf(ind));
            }
        }

        synchronized (roiSelectionModel)
        {
            // start selection change
            roiSelectionModel.setValueIsAdjusting(true);
            try
            {
                // start by clearing selection
                roiSelectionModel.clearSelection();

                for (Integer index : selectedIndexes)
                    roiSelectionModel.addSelectionInterval(index.intValue(), index.intValue());
            }
            finally
            {
                // end selection change
                roiSelectionModel.setValueIsAdjusting(false);
            }
        }
    }

    protected Set<ROI> getFilteredSet(String filter)
    {
        final Set<ROI> rois = roiSet;
        final Set<ROI> result = new HashSet<ROI>();

        if (StringUtil.isEmpty(filter, true))
            result.addAll(rois);
        else
        {
            final String text = filter.trim().toLowerCase();

            // filter on name
            for (ROI roi : rois)
                if (roi.getName().toLowerCase().indexOf(text) != -1)
                    result.add(roi);
        }

        return result;
    }

    void refreshRoiNumbers()
    {
        final int selectedCount = getSelectedRoisCount();
        final int roisCount = roiTable.getRowCount();

        selectedRoiNumberLabel.setText(Integer.toString(selectedCount));
        roiNumberLabel.setText(Integer.toString(roisCount));

        if (selectedCount == 0)
            selectedRoiNumberLabel.setToolTipText("No selected ROI");
        else if (selectedCount == 1)
            selectedRoiNumberLabel.setToolTipText("1 selected ROI");
        else
            selectedRoiNumberLabel.setToolTipText(selectedCount + " selected ROIs");

        if (roisCount == 0)
            roiNumberLabel.setToolTipText("No ROI");
        else if (roisCount == 1)
            roiNumberLabel.setToolTipText("1 ROI");
        else
            roiNumberLabel.setToolTipText(roisCount + " ROIs");
    }

    /**
     * refresh whole ROI list
     */
    protected void refreshRois()
    {
        processor.submit(true, roiListRefresher);
    }

    /**
     * refresh whole ROI list (internal)
     */
    protected void refreshRoisInternal()
    {
        final Set<ROI> currentRoiSet = roiSet;
        final Set<ROI> newRoiSet;
        final Sequence sequence = getSequence();

        if (sequence != null)
            newRoiSet = sequence.getROISet();
        else
            newRoiSet = new HashSet<ROI>();

        // no change --> exit
        if (newRoiSet.equals(currentRoiSet))
            return;

        final Set<ROI> removedSet = new HashSet<ROI>();

        // build removed set
        for (ROI roi : currentRoiSet)
            if (!newRoiSet.contains(roi))
                removedSet.add(roi);

        // remove from ROI entry map
        for (ROI roi : removedSet)
        {
            final ROIResults roiResults;

            // must be synchronized
            synchronized (roiResultsMap)
            {
                roiResults = roiResultsMap.remove(roi);
            }

            // cancel results computation
            if (roiResults != null)
                cancelDescriptorComputation(roiResults);
        }

        // set new ROI set
        roiSet = newRoiSet;

        // refresh filtered list now
        refreshFilteredRoisInternal();
    }

    /**
     * refresh filtered ROI list
     */
    protected void refreshFilteredRois()
    {
        processor.submit(true, filteredRoiListRefresher);
    }

    /**
     * refresh filtered ROI list (internal)
     */
    protected void refreshFilteredRoisInternal()
    {
        // get new filtered list
        final List<ROI> currentFilteredRoiList = filteredRoiList;
        final Set<ROI> newFilteredRoiSet = getFilteredSet(nameFilter.getText());

        // no change --> exit
        if (newFilteredRoiSet.equals(currentFilteredRoiList))
            return;

        // update filtered lists
        final List<ROI> newFilteredRoiList = new ArrayList<ROI>(newFilteredRoiSet);
        final List<ROIResults> newFilteredResultsList = new ArrayList<ROIResults>(newFilteredRoiList.size());

        // sort on id
        Collections.sort(newFilteredRoiList, ROI.idComparator);
        // then build filtered results list
        for (ROI roi : newFilteredRoiList)
        {
            ROIResults roiResults;

            synchronized (roiResultsMap)
            {
                // try to get the ROI results from the map first
                roiResults = roiResultsMap.get(roi);
                // and create it if needed
                if (roiResults == null)
                {
                    roiResults = new ROIResults(roi);
                    roiResultsMap.put(roi, roiResults);
                }
            }

            newFilteredResultsList.add(roiResults);
        }

        filteredRoiList = newFilteredRoiList;
        filteredRoiResultsList = newFilteredResultsList;

        // update the table model (should always correspond to the filtered roi results list)
        refreshTableDataStructureInternal();
    }

    public void refreshTableDataStructure()
    {
        processor.submit(true, tableDataStructureRefresher);
    }

    protected void refreshTableDataStructureInternal()
    {
        // don't eat too much time on data structure refresh
        ThreadUtil.sleep(1);

        final Set<ROI> newSelectedRois;
        final Sequence sequence = getSequence();

        if (sequence != null)
            newSelectedRois = sequence.getSelectedROISet();
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
                    synchronized (roiTableModel)
                    {
                        // notify table data changed
                        roiTableModel.fireTableDataChanged();
                    }

                    // selection to restore ?
                    if (!newSelectedRois.isEmpty())
                        setSelectedRoisInternal(newSelectedRois);
                }
                finally
                {
                    modifySelection.release();
                }
            }
        });

        refreshRoiNumbers();
        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    public void refreshTableData()
    {
        processor.submit(true, tableDataRefresher);
    }

    void refreshTableDataInternal()
    {
        // don't eat too much time on data structure refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final int rowCount = roiTable.getRowCount();

                    // we use RowsUpdated event to keep selection (DataChanged remove selection)
                    if (rowCount > 0)
                    {
                        synchronized (roiTableModel)
                        {
                            roiTableModel.fireTableRowsUpdated(0, rowCount - 1);
                        }
                    }
                }
                catch (Exception e)
                {
                    // ignore possible exception here
                }
            }
        });

        refreshRoiNumbers();
        // notify the ROI control panel that selection changed (force data refresh)
        roiControlPanel.selectionChanged();
    }

    public void refreshTableSelection()
    {
        processor.submit(true, tableSelectionRefresher);
    }

    protected void refreshTableSelectionInternal()
    {
        // don't eat too much time on selection refresh
        ThreadUtil.sleep(1);

        final Set<ROI> newSelectedRois;
        final Sequence sequence = getSequence();

        if (sequence != null)
            newSelectedRois = sequence.getSelectedROISet();
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
                    // set selection
                    setSelectedRoisInternal(newSelectedRois);
                }
                finally
                {
                    modifySelection.release();
                }
            }
        });

        refreshRoiNumbers();
        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    protected void refreshDescriptorList()
    {
        descriptorMap = ROIUtil.getROIDescriptors();
        refreshColumnInfoList();
    }

    public void refreshColumnInfoList()
    {
        processor.submit(true, columnInfoListRefresher);
    }

    protected void refreshColumnInfoListInternal()
    {
        // rebuild the column property list
        final List<ColumnInfo> newColumnInfos = new ArrayList<ColumnInfo>();
        final int numChannel = getChannelCount();

        for (ROIDescriptor descriptor : descriptorMap.keySet())
        {
            for (int ch = 0; ch < (descriptor.useSequenceData() ? numChannel : 1); ch++)
                newColumnInfos.add(new ColumnInfo(descriptor, ch, viewPreferences, false));
        }

        // sort the list on order
        Collections.sort(newColumnInfos);
        // set new column info
        columnInfoList = newColumnInfos;
        // rebuild table columns
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                // regenerate column model
                roiTable.setColumnModel(new ROITableColumnModel(columnInfoList));
            }
        });
    }

    boolean hasPendingComputation(ROIResults results)
    {
        synchronized (descriptorsToCompute)
        {
            return descriptorsToCompute.contains(results);
        }
    }

    void requestDescriptorComputation(ROIResults results)
    {
        synchronized (descriptorsToCompute)
        {
            descriptorsToCompute.add(results);
            descriptorsToCompute.notifyAll();
        }
    }

    void cancelDescriptorComputation(ROIResults roiResults)
    {
        synchronized (descriptorsToCompute)
        {
            descriptorsToCompute.remove(roiResults);
            descriptorsToCompute.notifyAll();
        }
    }

    void cancelDescriptorComputation(ROI roi)
    {
        synchronized (descriptorsToCompute)
        {
            final Iterator<ROIResults> it = descriptorsToCompute.iterator();

            while (it.hasNext())
            {
                final ROIResults roiResults = it.next();

                // remove all results for this ROI
                if (roiResults.roi == roi)
                    it.remove();
            }

            descriptorsToCompute.notifyAll();
        }
    }

    void cancelAllDescriptorComputation()
    {
        synchronized (descriptorsToCompute)
        {
            descriptorsToCompute.clear();
            descriptorsToCompute.notifyAll();
        }
    }

    protected void sequenceDataChanged()
    {
        final Object[] allRoiResults;

        // get all ROI results
        synchronized (roiResultsMap)
        {
            allRoiResults = roiResultsMap.values().toArray();
        }

        // notify ROI results that sequence data has changed
        for (Object roiResults : allRoiResults)
            ((ROIResults) roiResults).dataChanged(false, true);

        // refresh table data
        refreshTableData();

        // if data changed (more Z or T) we need to refresh action
        // so we can change ROI position correctly
        roiControlPanel.refreshROIActions();
    }

    /**
     * @deprecated Use {@link #getCSVFormattedInfos()} instead.
     */
    @Deprecated
    public String getCSVFormattedInfosOfSelectedRois()
    {
        // Check to ensure we have selected only a contiguous block of cells
        final int numcols = roiTable.getColumnCount();
        final int numrows = roiTable.getSelectedRowCount();

        // roiTable is empty --> returns empty string
        if (numrows == 0)
            return "";

        final StringBuffer sbf = new StringBuffer();
        final int[] rowsselected = roiTable.getSelectedRows();

        // column name
        for (int j = 1; j < numcols; j++)
        {
            sbf.append(roiTable.getModel().getColumnName(j));
            if (j < numcols - 1)
                sbf.append("\t");
        }
        sbf.append("\r\n");

        // then content
        for (int i = 0; i < numrows; i++)
        {
            for (int j = 1; j < numcols; j++)
            {
                final Object value = roiTable.getModel()
                        .getValueAt(roiTable.convertRowIndexToModel(rowsselected[i]), j);

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
        final List<ColumnInfo> exportColumnInfos = new ArrayList<ColumnInfo>();
        final Sequence seq = getSequence();
        final int numChannel = getChannelCount();

        // get export column informations
        for (ROIDescriptor descriptor : descriptorMap.keySet())
        {
            for (int ch = 0; ch < (descriptor.useSequenceData() ? numChannel : 1); ch++)
                exportColumnInfos.add(new ColumnInfo(descriptor, ch, exportPreferences, true));
        }

        // sort the list on order
        Collections.sort(exportColumnInfos);

        final StringBuffer sbf = new StringBuffer();

        // column title
        for (ColumnInfo columnInfo : exportColumnInfos)
        {
            if (columnInfo.visible)
            {
                sbf.append(columnInfo.name);
                sbf.append("\t");
            }
        }
        sbf.append("\r\n");

        final List<ROI> rois = filteredRoiList;

        // content
        for (ROI roi : rois)
        {
            final ROIResults results = new ROIResults(roi);
            final Map<ColumnInfo, DescriptorResult> descriptorResults = results.descriptorResults;

            // compute results
            for (ColumnInfo columnInfo : exportColumnInfos)
            {
                if (columnInfo.visible)
                {
                    // try to retrieve result for this column
                    DescriptorResult result = descriptorResults.get(columnInfo);

                    // not yet computed --> do it now
                    if (result == null)
                    {
                        final ROIDescriptor descriptor = columnInfo.descriptor;
                        final PluginROIDescriptor plugin;

                        // get the corresponding plugin
                        synchronized (descriptorMap)
                        {
                            plugin = descriptorMap.get(descriptor);
                        }

                        if (plugin != null)
                        {
                            final Map<ROIDescriptor, Object> newResults;

                            try
                            {
                                // need computation per channel ?
                                if (descriptor.useSequenceData())
                                    newResults = plugin.compute(results.getRoiForChannel(columnInfo.channel), seq);
                                else
                                    newResults = plugin.compute(results.roi, seq);

                                for (Entry<ROIDescriptor, Object> entryNewResult : newResults.entrySet())
                                {
                                    // get the column for this result
                                    final ColumnInfo resultColumnInfo = getColumnInfo(exportColumnInfos,
                                            entryNewResult.getKey(), columnInfo.channel);
                                    // create result
                                    result = new DescriptorResult(resultColumnInfo);
                                    // and put it in map
                                    descriptorResults.put(resultColumnInfo, result);
                                    // and set result value
                                    result.setValue(entryNewResult.getValue());
                                }
                            }
                            catch (UnsupportedOperationException e)
                            {
                                // ignore
                            }
                        }
                    }
                }
            }

            // display results
            for (ColumnInfo columnInfo : exportColumnInfos)
            {
                if (columnInfo.visible)
                {
                    final DescriptorResult result = descriptorResults.get(columnInfo);
                    final Object value;

                    if (result != null)
                        value = results.formatValue(result.getValue(), columnInfo.descriptor.getId());
                    else
                        value = null;

                    if (value != null)
                        sbf.append(value);

                    sbf.append("\t");
                }
            }
            sbf.append("\r\n");
        }

        return sbf.toString();
    }

    public void showSettingPanel()
    {
        // create and display the setting frame
        new RoiSettingFrame(viewPreferences, exportPreferences, new Runnable()
        {
            @Override
            public void run()
            {
                // refresh table columns
                refreshColumnInfoListInternal();
            }
        });
    }

    @Override
    public void textChanged(IcyTextField source, boolean validate)
    {
        if (source == nameFilter)
            refreshFilteredRois();
    }

    // called when selection changed in the ROI roiTable
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        // currently changing the selection ? --> exit
        if (roiSelectionModel.getValueIsAdjusting())
            return;
        // currently changing the selection ? --> exit
        if (!modifySelection.tryAcquire())
            return;

        // semaphore acquired here
        try
        {
            final List<ROI> selectedRois = getSelectedRois();
            final Sequence sequence = getSequence();

            // update selected ROI in sequence
            if (sequence != null)
                sequence.setSelectedROIs(selectedRois);

        }
        finally
        {
            modifySelection.release();
        }

        refreshRoiNumbers();
        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    @Override
    public void sequenceActivated(Sequence value)
    {
        // refresh ROI list
        refreshRois();
        // refresh table columns
        refreshColumnInfoList();
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
                switch (event.getType())
                {
                    case ADDED:
                    case REMOVED:
                        refreshRois();
                        break;

                    case CHANGED:
                        // already handled by ROIResults directly
                        break;
                }
                break;

            case SEQUENCE_DATA:
                sequenceDataChanged();
                break;

            case SEQUENCE_TYPE:
                // number of channel can have changed
                refreshColumnInfoList();
                // if type changed (number of channel) we need to refresh action
                // so we change change ROI position correctly
                roiControlPanel.refreshROIActions();
                break;
        }
    }

    private class ROITableModel extends AbstractTableModel
    {
        public ROITableModel()
        {
            super();
        }

        @Override
        public int getColumnCount()
        {
            return columnInfoList.size();
        }

        @Override
        public String getColumnName(int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
                return ci.name;

            return "";
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
                return ci.descriptor.getType();

            return String.class;
        }

        @Override
        public int getRowCount()
        {
            return filteredRoiResultsList.size();
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            final ROIResults roiResults = getRoiResults(row);

            if (roiResults != null)
                return roiResults.getValueAt(column);

            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int column)
        {
            final ROIResults roiResults = getRoiResults(row);

            if (roiResults != null)
                roiResults.setValueAt(value, column);
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            final ROIResults roiResults = getRoiResults(row);

            if (roiResults != null)
                return roiResults.isEditable(column);

            return false;
        }
    }

    private class ROIResults implements ROIListener
    {
        public final Map<ColumnInfo, DescriptorResult> descriptorResults;
        public final ROI roi;
        private final Map<Integer, WeakReference<ROI>> channelRois;

        ROIResults(ROI roi)
        {
            super();

            this.roi = roi;
            descriptorResults = new HashMap<ColumnInfo, DescriptorResult>();
            channelRois = new HashMap<Integer, WeakReference<ROI>>();

            // listen for ROI change event
            roi.addListener(this);
        }

        // boolean areResultsUpToDate()
        // {
        // for (DescriptorResult result : descriptorResults.values())
        // if (result.isOutdated())
        // return false;
        //
        // return true;
        // }

        private void clearChannelRois()
        {
            synchronized (channelRois)
            {
                channelRois.clear();
            }
        }

        public ROI getRoiForChannel(int channel)
        {
            final Integer key = Integer.valueOf(channel);
            WeakReference<ROI> reference;
            ROI result;

            synchronized (channelRois)
            {
                reference = channelRois.get(key);
            }

            if (reference != null)
                result = reference.get();
            else
                result = null;

            // channel ROI does not exist ?
            if (result == null)
            {
                // create it
                result = roi.getSubROI(-1, -1, channel);
                // and put it in map
                synchronized (channelRois)
                {
                    // we use WeakReference to not waste memory
                    channelRois.put(key, new WeakReference<ROI>(result));
                }
            }

            return result;
        }

        /**
         * Called when data from ROI or/and sequence changed, in which case we need to invalidate results.
         * 
         * @param roiData
         *        <code>true</code> if ROI data changed
         * @param sequenceData
         *        <code>true</code> if sequence data changed
         */
        public void dataChanged(boolean roiData, boolean sequenceData)
        {
            if (roiData)
            {
                synchronized (descriptorResults)
                {
                    // mark all descriptor results as outdated
                    for (DescriptorResult result : descriptorResults.values())
                        result.setOutdated(true);
                }

                // need to recompute channel rois
                clearChannelRois();
            }
            else if (sequenceData)
            {
                final Object[] keys;

                synchronized (descriptorResults)
                {
                    keys = descriptorResults.keySet().toArray();
                }

                for (Object key : keys)
                {
                    // need to recompute this descriptor ?
                    if (((ROIDescriptor) key).useSequenceData())
                    {
                        final DescriptorResult result;

                        synchronized (descriptorResults)
                        {
                            result = descriptorResults.get(key);
                        }

                        // mark as outdated
                        if (result != null)
                            result.setOutdated(true);
                    }
                }
            }
        }

        String getROIPropertyDescriptorId(String propertyName)
        {
            if (propertyName.equals(ROI.PROPERTY_NAME))
                return ROINameDescriptor.ID;
            if (propertyName.equals(ROI.PROPERTY_ICON))
                return ROIIconDescriptor.ID;

            if (propertyName.equals(ROI.PROPERTY_OPACITY))
                return ROIOpacityDescriptor.ID;
            if (propertyName.equals(ROI.PROPERTY_READONLY))
                return ROIReadOnlyDescriptor.ID;

            return "";
        }

        /**
         * Called when ROI property changed (base ROI only)
         */
        void propertyChanged(String propertyName)
        {
            final String descriptorId = getROIPropertyDescriptorId(propertyName);
            final Object[] keys;

            synchronized (descriptorResults)
            {
                keys = descriptorResults.keySet().toArray();
            }

            for (Object key : keys)
            {
                // found the corresponding descriptor result ?
                if (((ColumnInfo) key).descriptor.getId().equals(descriptorId))
                {
                    final DescriptorResult result;

                    synchronized (descriptorResults)
                    {
                        result = descriptorResults.get(key);
                    }

                    // mark as outdated
                    if (result != null)
                        result.setOutdated(true);

                    // done
                    break;
                }
            }
        }

        public boolean isEditable(int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
            {
                final ROIDescriptor descriptor = ci.descriptor;

                // only name descriptor is editable (a bit hacky)
                return descriptor.getId().equals(ROINameDescriptor.ID);
            }

            return false;
        }

        public Object formatValue(Object value, String id)
        {
            Object result = value;

            // format result if needed
            if (result instanceof Double)
            {
                final double doubleValue = ((Double) result).doubleValue();

                // replace 'infinity' by infinite symbol
                if (doubleValue == Double.POSITIVE_INFINITY)
                    result = MathUtil.INFINITE_STRING;
                else if (doubleValue == Double.NEGATIVE_INFINITY)
                {
                    // position descriptor ? negative infinite means 'ALL' here
                    if (id.equals(ROIPositionXDescriptor.ID) || id.equals(ROIPositionYDescriptor.ID)
                            || id.equals(ROIPositionZDescriptor.ID) || id.equals(ROIPositionTDescriptor.ID)
                            || id.equals(ROIPositionCDescriptor.ID) || id.equals(ROIMassCenterXDescriptor.ID)
                            || id.equals(ROIMassCenterYDescriptor.ID) || id.equals(ROIMassCenterZDescriptor.ID)
                            || id.equals(ROIMassCenterTDescriptor.ID) || id.equals(ROIMassCenterCDescriptor.ID))
                        result = "ALL";
                    else
                        result = "-" + MathUtil.INFINITE_STRING;
                }
                else if (doubleValue == -1d)
                {
                    // position descriptor ? -1 means 'ALL' here
                    if (id.equals(ROIPositionXDescriptor.ID) || id.equals(ROIPositionYDescriptor.ID)
                            || id.equals(ROIPositionZDescriptor.ID) || id.equals(ROIPositionTDescriptor.ID)
                            || id.equals(ROIPositionCDescriptor.ID) || id.equals(ROIMassCenterXDescriptor.ID)
                            || id.equals(ROIMassCenterYDescriptor.ID) || id.equals(ROIMassCenterZDescriptor.ID)
                            || id.equals(ROIMassCenterTDescriptor.ID) || id.equals(ROIMassCenterCDescriptor.ID))
                        result = "ALL";
                }
                else
                {
                    // format double value
                    final double roundedValue = MathUtil.roundSignificant(doubleValue, 5);

                    // simple integer ? -> show it as integer
                    if ((roundedValue == (int) roundedValue) && (Math.abs(roundedValue) < 10000000))
                        result = Integer.valueOf((int) roundedValue);
                    else
                        result = Double.valueOf(roundedValue);
                }
            }

            return result;
        }

        /**
         * Retrieve the value for the specified descriptor
         */
        public Object getValue(ColumnInfo column)
        {
            // get result for this descriptor
            DescriptorResult result;

            synchronized (descriptorResults)
            {
                result = descriptorResults.get(column);

                // no result --> create it and request computation
                if (result == null)
                {
                    // create descriptor result
                    result = new DescriptorResult(column);
                    // and put it in results map
                    descriptorResults.put(column, result);
                }
            }

            // mark it as requested
            result.setRequested();

            // out dated result ? --> request for descriptor computation
            if (result.isOutdated())
                requestDescriptorComputation(this);

            return formatValue(result.getValue(), column.descriptor.getId());
        }

        public Object getValueAt(int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
                return getValue(ci);

            return null;
        }

        public void setValueAt(Object aValue, int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
            {
                final ROIDescriptor descriptor = ci.descriptor;

                // only name descriptor is editable (a bit hacky)
                if (descriptor.getId().equals(ROINameDescriptor.ID))
                    roi.setName((String) aValue);
            }
        }

        @Override
        public void roiChanged(ROIEvent event)
        {
            final ROI roi = event.getSource();

            // ROI selected ? --> propagate event to control panel
            if (roi.isSelected())
                roiControlPanel.roiChanged(event);

            switch (event.getType())
            {
                case ROI_CHANGED:
                    // handle ROI data change
                    dataChanged(true, false);
                    // and refresh table data
                    refreshTableData();
                    break;

                case SELECTION_CHANGED:
                    // not modifying selection from panel ?
                    if (modifySelection.availablePermits() > 0)
                        // update ROI selection
                        refreshTableSelection();
                    break;

                case PROPERTY_CHANGED:
                    // handle ROI property change
                    propertyChanged(event.getPropertyName());
                    // and refresh table data
                    refreshTableData();
                    break;
            }
        }
    }

    class DescriptorResult
    {
        private Object value;
        private boolean outdated;
        private boolean requested;

        public DescriptorResult(ColumnInfo column)
        {
            super();

            value = null;

            // by default we consider it as out dated
            outdated = true;
            requested = false;
        }

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        public boolean isOutdated()
        {
            return outdated;
        }

        public void setOutdated(boolean value)
        {
            outdated = value;
        }

        public boolean isRequested()
        {
            return requested;
        }

        public void setRequested()
        {
            requested = true;
        }
    }

    public static class BaseColumnInfo implements Comparable<BaseColumnInfo>
    {
        final ROIDescriptor descriptor;
        int minSize;
        int maxSize;
        int defaultSize;
        int order;
        boolean visible;

        public BaseColumnInfo(ROIDescriptor descriptor, XMLPreferences preferences, boolean export)
        {
            super();

            this.descriptor = descriptor;

            load(preferences, export);
        }

        public boolean load(XMLPreferences preferences, boolean export)
        {
            final XMLPreferences p = preferences.node(descriptor.getId());

            if (p != null)
            {
                minSize = p.getInt(ID_PROPERTY_MINSIZE, getDefaultMinSize());
                maxSize = p.getInt(ID_PROPERTY_MAXSIZE, getDefaultMaxSize());
                defaultSize = p.getInt(ID_PROPERTY_DEFAULTSIZE, getDefaultDefaultSize());
                order = p.getInt(ID_PROPERTY_ORDER, getDefaultOrder());
                visible = p.getBoolean(ID_PROPERTY_VISIBLE, getDefaultVisible(export));

                return true;
            }

            return false;
        }

        public boolean save(XMLPreferences preferences)
        {
            final XMLPreferences p = preferences.node(descriptor.getId());

            if (p != null)
            {
                // p.putInt(ID_PROPERTY_MINSIZE, minSize);
                // p.putInt(ID_PROPERTY_MAXSIZE, maxSize);
                // p.putInt(ID_PROPERTY_DEFAULTSIZE, defaultSize);
                p.putInt(ID_PROPERTY_ORDER, order);
                p.putBoolean(ID_PROPERTY_VISIBLE, visible);

                return true;
            }

            return false;
        }

        protected boolean getDefaultVisible(boolean export)
        {
            if (descriptor == null)
                return false;

            if (export)
            {
                final Class<?> type = descriptor.getType();
                return ClassUtil.isSubClass(type, String.class) || ClassUtil.isSubClass(type, Number.class);
            }

            final String id = descriptor.getId();

            if (StringUtil.equals(id, ROIIconDescriptor.ID))
                return true;
            if (StringUtil.equals(id, ROINameDescriptor.ID))
                return true;

            if (StringUtil.equals(id, ROIContourDescriptor.ID))
                return true;
            if (StringUtil.equals(id, ROIInteriorDescriptor.ID))
                return true;

            return false;
        }

        protected int getDefaultOrder()
        {
            if (descriptor == null)
                return Integer.MAX_VALUE;

            final String id = descriptor.getId();
            int order = -1;

            order++;
            if (StringUtil.equals(id, ROIIconDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROINameDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROIPositionXDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIPositionYDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIPositionZDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIPositionTDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIPositionCDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROISizeXDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISizeYDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISizeZDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISizeTDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISizeCDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROIMassCenterXDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMassCenterYDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMassCenterZDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMassCenterTDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMassCenterCDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROIContourDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIInteriorDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROIPerimeterDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIAreaDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISurfaceAreaDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIVolumeDescriptor.ID))
                return order;

            order++;
            if (StringUtil.equals(id, ROIMinIntensityDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMeanIntensityDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIMaxIntensityDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROISumIntensityDescriptor.ID))
                return order;

            return Integer.MAX_VALUE;
        }

        protected int getDefaultMinSize()
        {
            if (descriptor == null)
                return Integer.MAX_VALUE;

            final String id = descriptor.getId();

            if (StringUtil.equals(id, ROIIconDescriptor.ID))
                return 22;
            if (StringUtil.equals(id, ROINameDescriptor.ID))
                return 60;

            final Class<?> type = descriptor.getType();

            if (type == Integer.class)
                return 30;
            if (type == Double.class)
                return 40;
            if (type == String.class)
                return 50;

            return 40;
        }

        protected int getDefaultMaxSize()
        {
            if (descriptor == null)
                return Integer.MAX_VALUE;

            final String id = descriptor.getId();

            if (StringUtil.equals(id, ROIIconDescriptor.ID))
                return 22;

            return Integer.MAX_VALUE;
        }

        protected int getDefaultDefaultSize()
        {
            final int maxSize = getDefaultMaxSize();
            final int minSize = getDefaultMinSize();

            if (maxSize == Integer.MAX_VALUE)
                return minSize * 2;

            return (minSize + maxSize) / 2;
        }

        @Override
        public int compareTo(BaseColumnInfo obj)
        {
            return Integer.valueOf(order).compareTo(Integer.valueOf(obj.order));
        }

        @Override
        public int hashCode()
        {
            return descriptor.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof BaseColumnInfo)
                // equality on descriptor
                return ((BaseColumnInfo) obj).descriptor.equals(descriptor);

            return super.equals(obj);
        }
    }

    public class ColumnInfo extends BaseColumnInfo
    {
        final String name;
        final int channel;

        public ColumnInfo(ROIDescriptor descriptor, int channel, XMLPreferences prefs, boolean export)
        {
            super(descriptor, prefs, export);

            this.channel = channel;
            name = getColumnName();
        }

        protected String getSuffix()
        {
            String result = "";

            final String unit = descriptor.getUnit(getSequence());

            if (!StringUtil.isEmpty(unit))
                result += " (" + unit + ")";

            // separate channel
            if (descriptor.useSequenceData())
                result += getChannelNameSuffix(channel);

            return result;
        }

        protected String getColumnName()
        {
            final String id = descriptor.getId();

            // we don't want to display name for these descriptors
            if (id.equals(ROIIconDescriptor.ID))
                return "";

            return descriptor.getName() + getSuffix();
        }

        @Override
        public int hashCode()
        {
            return descriptor.hashCode() ^ channel;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof ColumnInfo)
            {
                final ColumnInfo ci = (ColumnInfo) obj;

                // equality on descriptor and channel number
                return (ci.descriptor.equals(descriptor) && (ci.channel == ci.channel));
            }

            return super.equals(obj);
        }
    }

    class ROITableColumnModel extends DefaultTableColumnModelExt
    {
        public ROITableColumnModel(List<ColumnInfo> columnInfos)
        {
            super();

            // column info are sorted on their order
            int index = 0;
            for (ColumnInfo cp : columnInfos)
            {
                final ROIDescriptor descriptor = cp.descriptor;
                final TableColumnExt column = new TableColumnExt(index++);

                column.setIdentifier(descriptor.getId());
                column.setMinWidth(cp.minSize);
                column.setPreferredWidth(cp.defaultSize);
                if (cp.maxSize != Integer.MAX_VALUE)
                    column.setMaxWidth(cp.maxSize);
                if (cp.minSize == cp.maxSize)
                    column.setResizable(false);
                column.setHeaderValue(cp.name);
                column.setToolTipText(descriptor.getDescription() + cp.getSuffix());
                column.setVisible(cp.visible);
                column.setSortable(true);

                // image class type column --> use a special renderer
                if (descriptor.getType() == Image.class)
                    column.setCellRenderer(new ImageTableCellRenderer(18));

                // and finally add to the model
                addColumn(column);
            }

            setColumnSelectionAllowed(false);
        }
    }

    // class CustomTableCellRenderer extends DefaultTableCellRenderer
    // {
    // @Override
    // public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
    // boolean hasFocus, int row, int column)
    // {
    // super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    //
    // ROIResults roiResults;
    // try
    // {
    // roiResults = getROIResults(roiTable.convertRowIndexToModel(row));
    //
    // }
    // catch (IndexOutOfBoundsException e)
    // {
    // roiResults = null;
    // }
    //
    // final Color defaultColor;
    // final Color computeColor;
    //
    // if (isSelected)
    // defaultColor = UIManager.getColor("Table.selectionBackground");
    // else
    // defaultColor = UIManager.getColor("Table.background");
    // if (roiResults == null)
    // computeColor = Color.green;
    // else if (roiResults.areResultsUpToDate())
    // computeColor = Color.green;
    // else if (hasPendingComputation(roiResults))
    // computeColor = Color.orange;
    // else
    // computeColor = Color.red;
    //
    // // define background color
    // setBackground(ColorUtil.mix(defaultColor, computeColor, 0.15f));
    //
    // return this;
    // }
    // }

    public class ROITableSortController<M extends TableModel> extends DefaultSortController<M>
    {
        public ROITableSortController()
        {
            super();

            cachedModelRowCount = roiTableModel.getRowCount();
            setModelWrapper(new TableRowSorterModelWrapper());
        }

        /**
         * Returns the <code>Comparator</code> for the specified
         * column. If a <code>Comparator</code> has not been specified using
         * the <code>setComparator</code> method a <code>Comparator</code> will be returned based on the column class
         * (<code>TableModel.getColumnClass</code>) of the specified column.
         *
         * @throws IndexOutOfBoundsException
         *         {@inheritDoc}
         */
        @Override
        public Comparator<?> getComparator(int column)
        {
            return comparator;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Note: must implement same logic as the overridden comparator lookup, otherwise will throw ClassCastException
         * because here the comparator is never null.
         * <p>
         * PENDING JW: think about implications to string value lookup!
         * 
         * @throws IndexOutOfBoundsException
         *         {@inheritDoc}
         */
        @Override
        protected boolean useToString(int column)
        {
            return false;
        }

        /**
         * Implementation of DefaultRowSorter.ModelWrapper that delegates to a
         * TableModel.
         */
        private class TableRowSorterModelWrapper extends ModelWrapper<M, Integer>
        {
            public TableRowSorterModelWrapper()
            {
                super();
            }

            @Override
            public M getModel()
            {
                return (M) roiTableModel;
            }

            @Override
            public int getColumnCount()
            {
                return roiTableModel.getColumnCount();
            }

            @Override
            public int getRowCount()
            {
                return roiTableModel.getRowCount();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                return roiTableModel.getValueAt(row, column);
            }

            @Override
            public String getStringValueAt(int row, int column)
            {
                return getStringValueProvider().getStringValue(row, column).getString(getValueAt(row, column));
            }

            @Override
            public Integer getIdentifier(int index)
            {
                return Integer.valueOf(index);
            }
        }
    }
}
