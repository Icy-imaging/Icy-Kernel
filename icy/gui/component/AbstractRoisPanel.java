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
package icy.gui.component;

import icy.action.RoiActions;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.IcyButton;
import icy.gui.component.renderer.ImageTableCellRenderer;
import icy.gui.inspector.RoiSettingFrame;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.util.GuiUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.roi.ROIEvent;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIListener;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.system.IcyExceptionHandler;
import icy.system.thread.InstanceProcessor;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.DefaultListSelectionModel;
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
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
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
import plugins.kernel.roi.descriptor.property.ROIColorDescriptor;
import plugins.kernel.roi.descriptor.property.ROIGroupIdDescriptor;
import plugins.kernel.roi.descriptor.property.ROIIconDescriptor;
import plugins.kernel.roi.descriptor.property.ROINameDescriptor;
import plugins.kernel.roi.descriptor.property.ROIOpacityDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionCDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionTDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionXDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionYDescriptor;
import plugins.kernel.roi.descriptor.property.ROIPositionZDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeCDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeTDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeXDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeYDescriptor;
import plugins.kernel.roi.descriptor.property.ROISizeZDescriptor;

/**
 * Abstract ROI panel component
 */
public abstract class AbstractRoisPanel extends ExternalizablePanel implements ActiveSequenceListener,
        TextChangeListener, ListSelectionListener, PluginLoaderListener
{
    /**
     * 
     */
    protected static final long serialVersionUID = -2870878233087117178L;

    protected static final String ID_VIEW = "view";
    protected static final String ID_EXPORT = "export";

    protected static final String ID_PROPERTY_MINSIZE = "minSize";
    protected static final String ID_PROPERTY_MAXSIZE = "maxSize";
    protected static final String ID_PROPERTY_DEFAULTSIZE = "defaultSize";
    protected static final String ID_PROPERTY_ORDER = "order";
    protected static final String ID_PROPERTY_VISIBLE = "visible";

    // default row comparator
    protected static Comparator<Object> comparator = new Comparator<Object>()
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
    protected ROITableModel roiTableModel;
    protected ListSelectionModel roiSelectionModel;
    protected JXTable roiTable;
    protected IcyTextField nameFilter;
    protected JLabel roiNumberLabel;
    protected JLabel selectedRoiNumberLabel;

    // PluginDescriptors / ROIDescriptor map
    protected Map<ROIDescriptor, PluginROIDescriptor> descriptorMap;
    // DescriptorComputer / ROIDescriptor map
    protected Map<ROIDescriptor, DescriptorComputer> descriptorComputerMap;

    // Descriptor / column info (static to the class)
    protected List<ColumnInfo> columnInfoList;
    // // last visible columns (used to detect change in column configuration)
    // List<String> lastVisibleColumnIds;

    // ROI info list cache
    protected Set<ROI> roiSet;
    protected Map<ROI, ROIResults> roiResultsMap;
    protected List<ROI> filteredRoiList;
    protected List<ROIResults> filteredRoiResultsList;

    // internals
    protected final XMLPreferences basePreferences;
    protected final XMLPreferences viewPreferences;
    protected final XMLPreferences exportPreferences;
    protected final Semaphore modifySelection;

    // complete refresh of the roiTable
    protected final Runnable roiListRefresher;
    protected final Runnable filteredRoiListRefresher;
    protected final Runnable tableDataStructureRefresher;
    protected final Runnable tableDataRefresher;
    protected final Runnable tableSelectionRefresher;
    protected final Runnable columnInfoListRefresher;
    protected final InstanceProcessor processor;

    protected final DescriptorComputer primaryDescriptorComputer;
    protected final DescriptorComputer basicDescriptorComputer;
    protected final DescriptorComputer advancedDescriptorComputer;

    protected long lastTableDataRefresh;

    /**
     * Create a new ROI table panel.<br>
     * 
     * @param preferences
     *        XML preferences node which will contains the ROI table settings
     */
    public AbstractRoisPanel(XMLPreferences preferences)
    {
        super("ROI", "roiPanel", new Point(100, 100), new Dimension(400, 600));

        basePreferences = preferences;
        viewPreferences = basePreferences.node(ID_VIEW);
        exportPreferences = basePreferences.node(ID_EXPORT);

        roiSet = new HashSet<ROI>();
        roiResultsMap = new HashMap<ROI, ROIResults>();
        filteredRoiList = new ArrayList<ROI>();
        filteredRoiResultsList = new ArrayList<ROIResults>();
        modifySelection = new Semaphore(1);
        columnInfoList = new ArrayList<ColumnInfo>();

        lastTableDataRefresh = 0L;

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
                refreshFilteredRoisInternal();
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
                // regenerate column model to redefine the Cell Renderer (else colors are wrong)
                roiTable.setColumnModel(new ROITableColumnModel());

                // final TableColumnModel columnModel = roiTable.getColumnModel();
                //
                // for (int i = 0; i < columnModel.getColumnCount(); i++)
                // {
                // final TableColumn column = columnModel.getColumn(i);
                //
                // // need to reset specific renderer as background color can be wrong
                // if (column.getCellRenderer() instanceof ImageTableCellRenderer)
                // column.setCellRenderer(new ImageTableCellRenderer(18));
                // }

                // modify highlighter
                roiTable.setHighlighters(HighlighterFactory.createSimpleStriping());
            }
        });

        processor = new InstanceProcessor();
        processor.setThreadName("ROI panel GUI refresher");
        processor.setKeepAliveTime(30, TimeUnit.SECONDS);

        primaryDescriptorComputer = new DescriptorComputer(DescriptorType.PRIMARY);
        basicDescriptorComputer = new DescriptorComputer(DescriptorType.BASIC);
        advancedDescriptorComputer = new DescriptorComputer(DescriptorType.EXTERNAL);
        primaryDescriptorComputer.start();
        basicDescriptorComputer.start();
        advancedDescriptorComputer.start();

        // update descriptors list (this rebuild the column model of the tree table)
        refreshDescriptorList();
        // set shortcuts
        buildActionMap();

        refreshRois();

        // listen plugin loader changes
        PluginLoader.addListener(this);
    }

    protected void initialize()
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
        roiTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                if (event.getClickCount() == 2)
                {
                    final int c = roiTable.columnAtPoint(event.getPoint());
                    TableColumn col = null;

                    if (c != -1)
                        col = roiTable.getColumn(c);

                    if ((col == null) || !col.getHeaderValue().equals(new ROINameDescriptor().getName()))
                        roiTableDoubleClicked();
                }
            }
        });

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

        final IcyButton settingButton = new IcyButton(RoiActions.settingAction);
        settingButton.setHideActionText(true);
        settingButton.setFlat(true);
        
        final IcyButton xlsExportButton = new IcyButton(RoiActions.xlsExportAction);
        xlsExportButton.setHideActionText(true);
        xlsExportButton.setFlat(true);

        setLayout(new BorderLayout());
        add(GuiUtil.createLineBoxPanel(nameFilter, Box.createHorizontalStrut(8), selectedRoiNumberLabel, new JLabel(
                " / "), roiNumberLabel, Box.createHorizontalStrut(4), settingButton, xlsExportButton), BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);

        validate();
    }

    protected void buildActionMap()
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

    protected ROIResults createNewROIResults(ROI roi)
    {
        return new ROIResults(roi);
    }

    /**
     * Returns number of channel of current sequence
     */
    protected int getChannelCount()
    {
        final Sequence sequence = getSequence();

        if (sequence != null)
            return sequence.getSizeC();

        return 1;
    }

    /**
     * Returns roiTable column suffix for the specified channel
     */
    protected String getChannelNameSuffix(int ch)
    {
        final Sequence sequence = getSequence();

        if ((sequence != null) && (ch < getChannelCount()))
            return " (" + sequence.getChannelName(ch) + ")";

        return "";
    }

    /**
     * Returns ROI descriptor given its id.
     */
    protected ROIDescriptor getROIDescriptor(String descriptorId)
    {
        final ROIDescriptor[] descriptors;

        synchronized (descriptorMap)
        {
            descriptors = descriptorMap.keySet().toArray(new ROIDescriptor[descriptorMap.size()]);
        }

        for (ROIDescriptor descriptor : descriptors)
            if (descriptor.getId().equals(descriptorId))
                return descriptor;

        return null;
    }

    // /**
    // * Get column info for specified visible column index.
    // */
    // protected ColumnInfo getVisibleColumnInfo(List<ColumnInfo> columns, int column)
    // {
    // int ind = 0;
    // for (int c = 0; c < columns.size(); c++)
    // {
    // final ColumnInfo col = columns.get(c);
    //
    // if (col.visible)
    // {
    // if (ind == column)
    // return col;
    // ind++;
    // }
    // }
    //
    // return null;
    // }
    //
    // /**
    // * Get column info for specified visible column index.
    // */
    // protected ColumnInfo getVisibleColumnInfo(int column)
    // {
    // return getVisibleColumnInfo(columnInfoList, column);
    // }

    /**
     * Get column info for specified column index.
     */
    protected ColumnInfo getColumnInfo(List<ColumnInfo> columns, int column)
    {
        if (column < columns.size())
            return columns.get(column);

        return null;
    }

    /**
     * Get column info for specified column index.
     */
    protected ColumnInfo getColumnInfo(int column)
    {
        return getColumnInfo(columnInfoList, column);
    }

    protected ColumnInfo getColumnInfo(List<ColumnInfo> columns, ROIDescriptor descriptor, int channel)
    {
        for (ColumnInfo ci : columns)
            if (ci.descriptor.equals(descriptor) && (ci.channel == channel))
                return ci;

        return null;
    }

    protected ColumnInfo getColumnInfo(ROIDescriptor descriptor, int channel)
    {
        return getColumnInfo(columnInfoList, descriptor, channel);
    }

    protected abstract Sequence getSequence();

    public void setNameFilter(String name)
    {
        nameFilter.setText(name);
    }

    protected boolean computeROIResults(ROIResults roiResults, Sequence seq, ColumnInfo columnInfo)
    {
        final Map<ColumnInfo, DescriptorResult> results = roiResults.descriptorResults;
        final ROIDescriptor descriptor = columnInfo.descriptor;
        final DescriptorResult result;

        synchronized (results)
        {
            // get result
            result = results.get(columnInfo);
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
                    if (descriptor.separateChannel())
                    {
                        // retrieve the ROI for this channel
                        final ROI roi = roiResults.getRoiForChannel(columnInfo.channel);

                        if (roi == null)
                            throw new UnsupportedOperationException("Can't retrieve sub ROI for channel "
                                    + columnInfo.channel);

                        newResults = plugin.compute(roi, seq);
                    }
                    else
                        newResults = plugin.compute(roiResults.roi, seq);

                    for (Entry<ROIDescriptor, Object> entryNewResult : newResults.entrySet())
                    {
                        // get the column for this result
                        final ColumnInfo resultColumnInfo = getColumnInfo(entryNewResult.getKey(), columnInfo.channel);
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
                catch (Throwable t)
                {
                    // not an UnsupportedOperationException --> show the error
                    if (!(t instanceof UnsupportedOperationException))
                        IcyExceptionHandler.handleException(t, true);

                    final List<ROIDescriptor> descriptors = plugin.getDescriptors();

                    if (descriptors != null)
                    {
                        // not supported --> clear associated results and set them as computed
                        for (ROIDescriptor desc : descriptors)
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
                }

                // we updated result
                return true;
            }
        }

        return false;
    }

    /**
     * Return index of specified ROI in the filtered ROI list
     */
    protected int getRoiIndex(ROI roi)
    {
        final int result = Collections.binarySearch(filteredRoiList, roi, ROI.idComparator);

        if (result >= 0)
            return result;

        return -1;
    }

    /**
     * Return index of specified ROI in the model
     */
    protected int getRoiModelIndex(ROI roi)
    {
        return getRoiIndex(roi);
    }

    /**
     * Return index of specified ROI in the table (view)
     */
    protected int getRoiViewIndex(ROI roi)
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

    protected ROIResults getRoiResults(int rowModelIndex)
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

    /**
     * Display the roi in the table (scroll if needed)
     */
    public void scrollTo(ROI roi)
    {
        final int index = getRoiIndex(roi);

        if (index != -1)
            roiTable.scrollRowToVisible(index);
    }

    protected void refreshRoiNumbers()
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
                    roiResults = createNewROIResults(roi);
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
                        try
                        {
                            // notify table data changed
                            roiTableModel.fireTableDataChanged();
                        }
                        catch (Exception e)
                        {
                            // Sorter don't like when we change data while it's sorting...
                        }
                    }

                    // selection to restore ?
                    if (!newSelectedRois.isEmpty())
                        setSelectedRoisInternal(newSelectedRois);

                    // // force loading values on sorted column
                    // final List<? extends SortKey> keys = roiTable.getRowSorter().getSortKeys();
                    // if (!keys.isEmpty())
                    // forceComputationForColumn(keys.get(0).getColumn());
                }
                finally
                {
                    modifySelection.release();
                }
            }
        });

        refreshRoiNumbers();
    }

    public void refreshTableData()
    {
        processor.submit(true, tableDataRefresher);
    }

    protected void refreshTableDataInternal()
    {
        final long time = System.currentTimeMillis();
        final boolean hasPendingTask = primaryDescriptorComputer.hasPendingComputation()
                && basicDescriptorComputer.hasPendingComputation()
                && advancedDescriptorComputer.hasPendingComputation();

        // still pending descriptor task ?
        if (hasPendingTask)
        {
            // avoid too much table data update
            if ((time - lastTableDataRefresh) < 200)
                return;
        }

        lastTableDataRefresh = time;

        // don't eat too much time on data structure refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                final int rowCount = roiTable.getRowCount();

                // we use 'RowsUpdated' event to keep selection (DataChanged remove selection)
                if (rowCount > 0)
                {
                    // save anchor index which is lost with 'RowsUpdated' event
                    final int anchorInd = ((DefaultListSelectionModel) roiSelectionModel).getAnchorSelectionIndex();

                    synchronized (roiTableModel)
                    {
                        try
                        {
                            roiTableModel.fireTableRowsUpdated(0, rowCount - 1);
                        }
                        catch (Exception e)
                        {
                            // Sorter don't like when we change data while it's sorting...
                        }
                    }

                    // restore anchor index
                    if (anchorInd != -1)
                        ((DefaultListSelectionModel) roiSelectionModel).setAnchorSelectionIndex(anchorInd);
                }
            }
        });

        refreshRoiNumbers();
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
            for (int ch = 0; ch < (descriptor.separateChannel() ? numChannel : 1); ch++)
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
                roiTable.setColumnModel(new ROITableColumnModel());
            }
        });
    }

    // protected void forceComputationForColumn(int column)
    // {
    // final int numRow = roiTableModel.getRowCount();
    //
    // try
    // {
    // // force computation of the column values if needed
    // for (int i = 0; i < numRow; i++)
    // roiTableModel.getValueAt(i, column);
    // }
    // catch (Exception e)
    // {
    // // ignore
    // System.out.println(e);
    // }
    // }

    // protected boolean hasPendingComputation(ROIResults results)
    // {
    // return primaryDescriptorComputer.hasPendingComputation(results)
    // || basicDescriptorComputer.hasPendingComputation(results)
    // || advancedDescriptorComputer.hasPendingComputation(results);
    // }

    protected void requestDescriptorComputation(ROIResults results)
    {
        primaryDescriptorComputer.requestDescriptorComputation(results);
        basicDescriptorComputer.requestDescriptorComputation(results);
        advancedDescriptorComputer.requestDescriptorComputation(results);
    }

    protected void cancelDescriptorComputation(ROIResults results)
    {
        primaryDescriptorComputer.cancelDescriptorComputation(results);
        basicDescriptorComputer.cancelDescriptorComputation(results);
        advancedDescriptorComputer.cancelDescriptorComputation(results);
    }

    protected void cancelDescriptorComputation(ROI roi)
    {
        primaryDescriptorComputer.cancelDescriptorComputation(roi);
        basicDescriptorComputer.cancelDescriptorComputation(roi);
        advancedDescriptorComputer.cancelDescriptorComputation(roi);
    }

    protected void cancelAllDescriptorComputation()
    {
        primaryDescriptorComputer.cancelAllDescriptorComputation();
        basicDescriptorComputer.cancelAllDescriptorComputation();
        advancedDescriptorComputer.cancelAllDescriptorComputation();
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
            for (int ch = 0; ch < (descriptor.separateChannel() ? numChannel : 1); ch++)
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

        final List<ROI> rois = new ArrayList<ROI>(filteredRoiList);

        // content
        for (ROI roi : rois)
        {
            final ROIResults results = createNewROIResults(roi);
            final Map<ColumnInfo, DescriptorResult> descriptorResults = results.descriptorResults;

            // compute results
            for (ColumnInfo columnInfo : exportColumnInfos)
            {
                if (columnInfo.visible)
                {
                    // try to retrieve result for this column
                    final DescriptorResult result = descriptorResults.get(columnInfo);

                    // not yet created/computed --> create it and compute it now
                    if (result == null)
                    {
                        descriptorResults.put(columnInfo, new DescriptorResult(columnInfo));
                        computeROIResults(results, seq, columnInfo);
                    }
                }
            }

            // display results
            for (ColumnInfo columnInfo : exportColumnInfos)
            {
                if (columnInfo.visible)
                {
                    final DescriptorResult result = descriptorResults.get(columnInfo);
                    final String id = columnInfo.descriptor.getId();
                    final Object value;

                    if (result != null)
                        value = results.formatValue(result.getValue(), id);
                    else
                        value = null;

                    if (value != null)
                    {
                        // special case of icon --> use the ROI class name
                        if (StringUtil.equals(id, ROIIconDescriptor.ID))
                            sbf.append(roi.getSimpleClassName());
                        // special case of color --> use the color code
                        else if (StringUtil.equals(id, ROIColorDescriptor.ID))
                            sbf.append(String.format("%06X", Integer.valueOf(roi.getColor().getRGB() & 0xFFFFFF)));
                        else
                            sbf.append(value);
                    }

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

    // called when selection changed in the ROI table
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        // currently changing the selection ? --> exit
        if (e.getValueIsAdjusting())
            return;
        // currently changing the selection ? --> exit
        if (roiSelectionModel.getValueIsAdjusting())
            return;

        if (modifySelection.tryAcquire())
        {
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
        }

        refreshRoiNumbers();
    }

    // called when a ROI has been double clicked in the ROI table
    protected void roiTableDoubleClicked()
    {
        final List<ROI> selectedRois = getSelectedRois();

        if (selectedRois.size() > 0)
        {
            final ROI selected = selectedRois.get(0);
            // get active viewer
            final Viewer v = Icy.getMainInterface().getActiveViewer();

            if ((v != null) && (selected != null))
            {
                // get canvas
                final IcyCanvas c = v.getCanvas();

                if (c instanceof IcyCanvas2D)
                {
                    // center view on selected ROI
                    ((IcyCanvas2D) c).centerOn(selected.getBounds5D().toRectangle2D().getBounds());
                }
                else if (c instanceof IcyCanvas3D)
                {
                    // center view on selected ROI
                    ((IcyCanvas3D) c).centerOn(selected.getBounds5D().toRectangle3D().toInteger());
                }
            }
        }
    }

    @Override
    public void sequenceActivated(Sequence value)
    {
        // refresh table columns
        refreshColumnInfoList();
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

            case SEQUENCE_META:
                // refresh column name (unit can change when pixel size changed)
                for (ColumnInfo col : columnInfoList)
                    col.refreshName();

                // refresh column model
                final TableColumnModel model = roiTable.getColumnModel();
                if (model instanceof ROITableColumnModel)
                    ((ROITableColumnModel) model).updateHeaders();

                // don't use break, we also need to send the event to descriptors

            case SEQUENCE_DATA:
                final ROIResults[] allRoiResults;

                // get all ROI results
                synchronized (roiResultsMap)
                {
                    allRoiResults = roiResultsMap.values().toArray(new ROIResults[roiResultsMap.size()]);
                }

                // notify ROI results that sequence has changed
                for (ROIResults roiResults : allRoiResults)
                    roiResults.sequenceChanged(event);

                // refresh table data
                refreshTableData();
                break;

            case SEQUENCE_TYPE:
                // number of channel can have changed
                refreshColumnInfoList();
                break;
        }
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        refreshDescriptorList();
    }

    protected class ROITableModel extends AbstractTableModel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6537163170625368503L;

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

            if ((ci != null) && (ci.showName))
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

    protected class ROIResults implements ROIListener
    {
        public final Map<ColumnInfo, DescriptorResult> descriptorResults;
        public final ROI roi;
        private final Map<Integer, WeakReference<ROI>> channelRois;

        protected ROIResults(ROI roi)
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

                // failed ? try again
                if (result == null)
                    result = roi.getSubROI(-1, -1, channel);

                if (result != null)
                {
                    // and put it in map
                    synchronized (channelRois)
                    {
                        // we use WeakReference to not waste memory
                        channelRois.put(key, new WeakReference<ROI>(result));
                    }
                }
            }

            return result;
        }

        public boolean isEditable(int column)
        {
            final ColumnInfo ci = getColumnInfo(column);

            if (ci != null)
            {
                final ROIDescriptor descriptor = ci.descriptor;
                final String id = descriptor.getId();

                // only name and color descriptor are editable (a bit hacky)
                return id.equals(ROINameDescriptor.ID) || id.equals(ROIColorDescriptor.ID);
            }

            return false;
        }

        public Object formatValue(Object value, String id)
        {
            Object result = value;

            // format result if needed
            if (result instanceof Number)
            {
                final double doubleValue = ((Number) result).doubleValue();

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
                    // value not too large ?
                    if (Math.abs(doubleValue) < 10000000)
                    {
                        // simple integer ? -> show it as integer
                        if (doubleValue == (int) doubleValue)
                            result = Integer.valueOf((int) doubleValue);
                        // small integer value ?
                        else if (Math.abs(doubleValue) < 100)
                            result = Double.valueOf(MathUtil.roundSignificant(doubleValue, 5));
                        // medium integer value ?
                        else if (Math.abs(doubleValue) < 10000)
                            result = Double.valueOf(MathUtil.round(doubleValue, 2));
                        // medium large integer value ?
                        else if (Math.abs(doubleValue) < 1000000)
                            result = Double.valueOf(MathUtil.round(doubleValue, 1));
                        else
                            // large integer value ?
                            result = Integer.valueOf((int) Math.round(doubleValue));
                    }
                    else
                        // format double value
                        result = Double.valueOf(MathUtil.roundSignificant(doubleValue, 5));
                }
            }

            return result;
        }

        /**
         * Retrieve the DescriptorResult for the specified column
         */
        public DescriptorResult getDescriptorResult(ColumnInfo column)
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

            return result;
        }

        /**
         * Retrieve the value for the specified descriptor
         */
        public Object getValue(ColumnInfo column)
        {
            // get result for this descriptor
            final DescriptorResult result = getDescriptorResult(column);

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
                final String id = descriptor.getId();

                // only name descriptor is editable (a bit hacky)
                if (id.equals(ROINameDescriptor.ID))
                    roi.setName((String) aValue);
                else if (id.equals(ROIColorDescriptor.ID))
                    roi.setColor((Color) aValue);
            }
        }

        @Override
        public void roiChanged(ROIEvent event)
        {
            switch (event.getType())
            {
                case ROI_CHANGED:
                case PROPERTY_CHANGED:
                    final Object[] entries;

                    synchronized (descriptorResults)
                    {
                        entries = descriptorResults.entrySet().toArray();
                    }

                    for (Object entryObj : entries)
                    {
                        final Entry<ColumnInfo, DescriptorResult> entry = (Entry<ColumnInfo, DescriptorResult>) entryObj;
                        final ColumnInfo key = entry.getKey();
                        final ROIDescriptor descriptor = key.descriptor;

                        // need to recompute this descriptor ?
                        if (descriptor.needRecompute(event))
                        {
                            final DescriptorResult result = entry.getValue();

                            // mark as outdated
                            if (result != null)
                                result.setOutdated(true);
                        }
                    }

                    // need to recompute channel rois
                    if (event.getType() == ROIEventType.ROI_CHANGED)
                        clearChannelRois();

                    // and refresh table data
                    refreshTableData();
                    break;

                case SELECTION_CHANGED:
                    // not modifying selection from panel ?
                    if (modifySelection.availablePermits() > 0)
                        // update ROI selection
                        refreshTableSelection();
                    break;
            }
        }

        /**
         * Called when the sequence changed, in which case we need to invalidate results.
         * 
         * @param event
         *        Sequence change event
         */
        public void sequenceChanged(SequenceEvent event)
        {
            final Object[] entries;

            synchronized (descriptorResults)
            {
                entries = descriptorResults.entrySet().toArray();
            }

            for (Object entryObj : entries)
            {
                final Entry<ColumnInfo, DescriptorResult> entry = (Entry<ColumnInfo, DescriptorResult>) entryObj;
                final ColumnInfo key = entry.getKey();
                final ROIDescriptor descriptor = key.descriptor;

                // need to recompute this descriptor ?
                if (descriptor.needRecompute(event))
                {
                    final DescriptorResult result = entry.getValue();

                    // mark as outdated
                    if (result != null)
                        result.setOutdated(true);
                }
            }
        }
    }

    protected class DescriptorComputer extends Thread
    {
        protected final LinkedHashSet<ROIResults> resultsToCompute;
        protected final DescriptorType type;

        public DescriptorComputer(DescriptorType type)
        {
            super("ROI " + type.toString() + " descriptor calculator");

            resultsToCompute = new LinkedHashSet<AbstractRoisPanel.ROIResults>(256);
            this.type = type;

            setPriority(Thread.MIN_PRIORITY);
        }

        public boolean hasPendingComputation()
        {
            return resultsToCompute.size() > 0;
        }

        public boolean hasPendingComputation(ROIResults results)
        {
            synchronized (resultsToCompute)
            {
                return resultsToCompute.contains(results);
            }
        }

        public void requestDescriptorComputation(ROIResults results)
        {
            synchronized (resultsToCompute)
            {
                resultsToCompute.add(results);
                resultsToCompute.notifyAll();
            }
        }

        public void cancelDescriptorComputation(ROIResults roiResults)
        {
            synchronized (resultsToCompute)
            {
                resultsToCompute.remove(roiResults);
                resultsToCompute.notifyAll();
            }
        }

        public void cancelDescriptorComputation(ROI roi)
        {
            synchronized (resultsToCompute)
            {
                final Iterator<ROIResults> it = resultsToCompute.iterator();

                while (it.hasNext())
                {
                    final ROIResults roiResults = it.next();

                    // remove all results for this ROI
                    if (roiResults.roi == roi)
                        it.remove();
                }

                resultsToCompute.notifyAll();
            }
        }

        public void cancelAllDescriptorComputation()
        {
            synchronized (resultsToCompute)
            {
                resultsToCompute.clear();
                resultsToCompute.notifyAll();
            }
        }

        @Override
        public void run()
        {
            while (!Thread.interrupted())
            {
                final ROIResults[] roiResultsList;

                synchronized (resultsToCompute)
                {
                    try
                    {
                        while (resultsToCompute.isEmpty())
                            resultsToCompute.wait();
                    }
                    catch (InterruptedException e)
                    {
                        // ignore and just interrupt now
                        Thread.currentThread().interrupt();
                    }

                    // get results to compute
                    roiResultsList = resultsToCompute.toArray(new ROIResults[resultsToCompute.size()]);
                    // and remove them
                    resultsToCompute.clear();
                }

                final Sequence seq = getSequence();

                if (seq != null)
                {
                    // start with primaries descriptors
                    for (ROIResults roiResults : roiResultsList)
                        computeROIResults(roiResults, seq);
                }
            }
        }

        protected void computeROIResults(ROIResults roiResults, Sequence seq)
        {
            final Map<ColumnInfo, DescriptorResult> results = roiResults.descriptorResults;
            final ColumnInfo[] columnInfos;

            synchronized (results)
            {
                columnInfos = results.keySet().toArray(new ColumnInfo[results.size()]);
            }

            boolean needUpdate = false;
            for (ColumnInfo columnInfo : columnInfos)
            {
                // only compute a specific kind of descriptor
                if (columnInfo.getDescriptorType() == type)
                    needUpdate |= AbstractRoisPanel.this.computeROIResults(roiResults, seq, columnInfo);
            }

            // need to refresh data
            if (needUpdate)
                refreshTableData();
        }
    }

    protected class DescriptorResult
    {
        private Object value;
        private boolean outdated;

        public DescriptorResult(ColumnInfo column)
        {
            super();

            value = null;

            // by default we consider it as out dated
            outdated = true;
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
    }

    public static enum DescriptorType
    {
        PRIMARY, BASIC, EXTERNAL
    };

    public static class BaseColumnInfo implements Comparable<BaseColumnInfo>
    {
        public final ROIDescriptor descriptor;
        public int minSize;
        public int maxSize;
        public int defaultSize;
        public int order;
        public boolean visible;

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

            final String id = descriptor.getId();

            if (export)
            {
                if (StringUtil.equals(id, ROIOpacityDescriptor.ID))
                    return false;

                final Class<?> type = descriptor.getType();
                return ClassUtil.isSubClass(type, String.class) || ClassUtil.isSubClass(type, Number.class);
            }

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
            if (StringUtil.equals(id, ROIColorDescriptor.ID))
                return order;
            order++;
            if (StringUtil.equals(id, ROIGroupIdDescriptor.ID))
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
            if (StringUtil.equals(id, ROIColorDescriptor.ID))
                return 18;
            if (StringUtil.equals(id, ROIGroupIdDescriptor.ID))
                return 18;
            if (StringUtil.equals(id, ROINameDescriptor.ID))
                return 60;

            final Class<?> type = descriptor.getType();

            if (type == Integer.class)
                return 30;
            if (type == Float.class)
                return 40;
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
            if (StringUtil.equals(id, ROIColorDescriptor.ID))
                return 18;
            if (StringUtil.equals(id, ROIGroupIdDescriptor.ID))
                return 18;

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

        /**
         * Used to know if this is a primary (name, color...) ROI descriptor.
         * 
         * @see #isBasicDescriptor()
         * @see #isExtendedDescriptor()
         * @see #getDescriptorType()
         */
        protected boolean isPrimaryDescriptor()
        {
            if (descriptor == null)
                return false;

            final String id = descriptor.getId();

            return (StringUtil.equals(id, ROIIconDescriptor.ID)) || (StringUtil.equals(id, ROIColorDescriptor.ID))
                    || (StringUtil.equals(id, ROINameDescriptor.ID))
                    || (StringUtil.equals(id, ROIGroupIdDescriptor.ID))
                    || (StringUtil.equals(id, ROIPositionXDescriptor.ID))
                    || (StringUtil.equals(id, ROIPositionYDescriptor.ID))
                    || (StringUtil.equals(id, ROIPositionZDescriptor.ID))
                    || (StringUtil.equals(id, ROIPositionTDescriptor.ID))
                    || (StringUtil.equals(id, ROIPositionCDescriptor.ID))
                    || (StringUtil.equals(id, ROISizeXDescriptor.ID)) || (StringUtil.equals(id, ROISizeYDescriptor.ID))
                    || (StringUtil.equals(id, ROISizeZDescriptor.ID)) || (StringUtil.equals(id, ROISizeTDescriptor.ID))
                    || (StringUtil.equals(id, ROISizeCDescriptor.ID));
        }

        /**
         * Used to know if this is a primary or basic (interior, contour, intensities..) ROI descriptor.
         * 
         * @see #isPrimaryDescriptor()
         * @see #isExtendedDescriptor()
         * @see #getDescriptorType()
         */
        protected boolean isBasicDescriptor()
        {
            if (descriptor == null)
                return false;

            final String id = descriptor.getId();

            return isPrimaryDescriptor() || (StringUtil.equals(id, ROIMassCenterXDescriptor.ID))
                    || (StringUtil.equals(id, ROIMassCenterYDescriptor.ID))
                    || (StringUtil.equals(id, ROIMassCenterZDescriptor.ID))
                    || (StringUtil.equals(id, ROIMassCenterTDescriptor.ID))
                    || (StringUtil.equals(id, ROIMassCenterCDescriptor.ID))
                    || (StringUtil.equals(id, ROIContourDescriptor.ID))
                    || (StringUtil.equals(id, ROIInteriorDescriptor.ID))
                    || (StringUtil.equals(id, ROIPerimeterDescriptor.ID))
                    || (StringUtil.equals(id, ROIAreaDescriptor.ID))
                    || (StringUtil.equals(id, ROISurfaceAreaDescriptor.ID))
                    || (StringUtil.equals(id, ROIVolumeDescriptor.ID))
                    || (StringUtil.equals(id, ROIMinIntensityDescriptor.ID))
                    || (StringUtil.equals(id, ROIMeanIntensityDescriptor.ID))
                    || (StringUtil.equals(id, ROIMaxIntensityDescriptor.ID));
        }

        /**
         * Used to know if this is a extended (added by an external plugin) ROI descriptor
         * 
         * @see #isPrimaryDescriptor()
         * @see #isBasicDescriptor()
         * @see #getDescriptorType()
         */
        protected boolean isExtendedDescriptor()
        {
            if (descriptor == null)
                return false;

            return !isBasicDescriptor();
        }

        /**
         * Returns the kind of ROI descriptor
         */
        public DescriptorType getDescriptorType()
        {
            if (descriptor == null)
                return null;

            if (isPrimaryDescriptor())
                return DescriptorType.PRIMARY;
            if (isBasicDescriptor())
                return DescriptorType.BASIC;

            return DescriptorType.EXTERNAL;
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

    protected class ColumnInfo extends BaseColumnInfo
    {
        boolean showName;
        String name;
        final int channel;

        public ColumnInfo(ROIDescriptor descriptor, int channel, XMLPreferences prefs, boolean export)
        {
            super(descriptor, prefs, export);

            this.channel = channel;
            refreshName();
        }

        protected String getSuffix()
        {
            String result = "";

            final String unit = descriptor.getUnit(getSequence());

            if (!StringUtil.isEmpty(unit))
                result += " (" + unit + ")";

            // separate channel
            if (descriptor.separateChannel())
                result += getChannelNameSuffix(channel);

            return result;
        }

        protected void refreshName()
        {
            name = descriptor.getName() + getSuffix();

            final String id = descriptor.getId();

            // we don't want to display name for these descriptors
            if (StringUtil.equals(id, ROIIconDescriptor.ID) || StringUtil.equals(id, ROIColorDescriptor.ID)
                    || StringUtil.equals(id, ROIGroupIdDescriptor.ID))
                showName = false;
            else
                showName = true;
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

    protected class ROITableColumnModel extends DefaultTableColumnModelExt
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8024047283485991234L;

        public ROITableColumnModel()
        {
            super();

            final List<ColumnInfo> columnInfos = columnInfoList;

            // column info are sorted on their order
            int index = 0;
            for (ColumnInfo ci : columnInfos)
            {
                final ROIDescriptor descriptor = ci.descriptor;
                final TableColumnExt column = new TableColumnExt(index++);

                column.setIdentifier(descriptor.getId());
                column.setMinWidth(ci.minSize);
                column.setPreferredWidth(ci.defaultSize);
                if (ci.maxSize != Integer.MAX_VALUE)
                    column.setMaxWidth(ci.maxSize);
                if (ci.minSize == ci.maxSize)
                    column.setResizable(false);
                column.setHeaderValue(ci.showName ? ci.name : "");
                column.setToolTipText(descriptor.getDescription() + ci.getSuffix());
                column.setVisible(ci.visible);
                column.setSortable(true);

                final Class<?> type = descriptor.getType();

                // image class type column --> use a special renderer
                if (type == Image.class)
                    column.setCellRenderer(new ImageTableCellRenderer(18));
                else if (type == Color.class)
                    column.setCellRenderer(new ImageTableCellRenderer(16));
                // use the number cell renderer
                else if (ClassUtil.isSubClass(type, Number.class))
                    column.setCellRenderer(new SubstanceDefaultTableCellRenderer.NumberRenderer());
                // column.setCellRenderer(new NumberTableCellRenderer());

                // and finally add to the model
                addColumn(column);
            }

            setColumnSelectionAllowed(false);
        }

        public void updateHeaders()
        {
            final List<ColumnInfo> columnInfos = columnInfoList;
            final List<TableColumn> columns = getColumns(true);
            for (TableColumn column : columns)
            {
                final ColumnInfo ci = getColumnInfo(columnInfos, column.getModelIndex());

                if (ci != null)
                {
                    final ROIDescriptor descriptor = ci.descriptor;

                    // that should be always the case
                    if (StringUtil.equals((String) column.getIdentifier(), descriptor.getId()))
                    {
                        column.setHeaderValue(ci.showName ? ci.name : "");
                        if (column instanceof TableColumnExt)
                            ((TableColumnExt) column).setToolTipText(descriptor.getDescription() + ci.getSuffix());
                    }
                }
            }
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

    protected class ROITableSortController<M extends TableModel> extends DefaultSortController<M>
    {
        public ROITableSortController()
        {
            super();

            cachedModelRowCount = roiTableModel.getRowCount();
            setModelWrapper(new TableRowSorterModelWrapper());
        }

        @Override
        public void sort()
        {
            try
            {
                super.sort();
            }
            catch (Exception e)
            {
                // ignore this...
                // System.err.println("ROI table column sort failed:");
                // System.err.println(e.getMessage());
            }
        }

        // @Override
        // protected void fireSortOrderChanged()
        // {
        // super.fireSortOrderChanged();
        //
        // final List<? extends SortKey> keys = getSortKeys();
        //
        // if (!keys.isEmpty())
        // forceComputationForColumn(keys.get(0).getColumn());
        // }

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

    // protected class NumberTableCellRenderer extends SubstanceDefaultTableCellRenderer.NumberRenderer
    // {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 5033090596184731420L;
    //
    // @Override
    // public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
    // boolean hasFocus, int row, int column)
    // {
    // final Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
    // column);
    //
    // final ROIResults roiResults = getRoiResults(row);
    //
    // if (roiResults != null)
    // {
    // final ColumnInfo ci = getVisibleColumnInfo(column);
    //
    // if (ci != null)
    // {
    // final DescriptorResult descResult = roiResults.getDescriptorResult(ci);
    //
    // if (descResult != null)
    // {
    // if (descResult.isOutdated())
    // result.setBackground(ColorUtil.mix(Color.red, result.getBackground()));
    // }
    // }
    // }
    //
    // return result;
    // }
    // }
}
