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
package icy.gui.viewer;

import icy.action.CanvasActions;
import icy.action.CanvasActions.ToggleLayersAction;
import icy.action.ViewerActions;
import icy.action.WindowActions;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasListener;
import icy.common.MenuCallback;
import icy.common.listener.ProgressListener;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.component.renderer.LabelComboBoxRenderer;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.lut.LUTViewer;
import icy.gui.lut.abstract_.IcyLutViewer;
import icy.gui.plugin.PluginComboBoxRenderer;
import icy.gui.util.ComponentUtil;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.imagej.ImageJWrapper;
import icy.main.Icy;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.interface_.PluginCanvas;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.system.thread.ThreadUtil;
import icy.util.Random;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.EventListenerList;

/**
 * Viewer send an event if the IcyCanvas change.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Viewer extends IcyFrame implements KeyListener, SequenceListener, IcyCanvasListener, PluginLoaderListener
{
    /**
     * associated LUT
     */
    private LUT lut;
    /**
     * associated canvas
     */
    IcyCanvas canvas;
    /**
     * associated sequence
     */
    Sequence sequence;

    /***/
    private final EventListenerList listeners = new EventListenerList();

    /**
     * GUI
     */
    private JToolBar toolBar;
    private JPanel mainPanel;
    private LUTViewer lutViewer;

    JComboBox canvasComboBox;
    JComboBox lockComboBox;
    IcyToggleButton layersEnabledButton;
    IcyButton screenShotButton;
    IcyButton screenShotAlternateButton;
    IcyButton duplicateButton;
    IcyButton switchStateButton;

    /**
     * internals
     */
    boolean initialized;

    public Viewer(Sequence sequence, boolean visible)
    {
        super("Viewer", true, true, true, true);

        if (sequence == null)
            throw new IllegalArgumentException("Can't open a null sequence.");

        this.sequence = sequence;

        // default
        canvas = null;
        lut = null;
        initialized = false;

        mainPanel = new JPanel();

        // set menu directly in system menu so we don't need a extra MenuBar
        setSystemMenuCallback(new MenuCallback()
        {
            @Override
            public JMenu getMenu()
            {
                return Viewer.this.getMenu();
            }
        });

        // build tool bar
        buildToolBar();

        mainPanel.setLayout(new BorderLayout());

        // set lut (this modify lutPanel)
        setLut(sequence.createCompatibleLUT());
        // set default canvas to first available canvas plugin (Canvas2D should be first)
        setCanvas(IcyCanvas.getCanvasPluginNames().get(0));

        setLayout(new BorderLayout());

        add(toolBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // setting frame
        refreshViewerTitle();
        setFocusable(true);
        // set position depending window mode
        setLocationInternal(20 + Random.nextInt(100), 20 + Random.nextInt(60));
        setLocationExternal(100 + Random.nextInt(200), 100 + Random.nextInt(150));
        setSize(640, 480);

        // initial position in sequence
        if (sequence.isEmpty())
            setPositionZ(0);
        else
            setPositionZ(((sequence.getSizeZ() + 1) / 2) - 1);

        addFrameListener(new IcyFrameAdapter()
        {
            @Override
            public void icyFrameOpened(IcyFrameEvent e)
            {
                if (!initialized)
                {
                    if ((Viewer.this.sequence != null) && !Viewer.this.sequence.isEmpty())
                    {
                        adjustViewerToImageSize();
                        initialized = true;
                    }
                }
            }

            @Override
            public void icyFrameActivated(IcyFrameEvent e)
            {
                Icy.getMainInterface().setActiveViewer(Viewer.this);

                // lost focus on ImageJ image
                final ImageJWrapper ij = Icy.getMainInterface().getImageJ();
                if (ij != null)
                    ij.setActiveImage(null);
            }

            @Override
            public void icyFrameExternalized(IcyFrameEvent e)
            {
                refreshToolBar();
            }

            @Override
            public void icyFrameInternalized(IcyFrameEvent e)
            {
                refreshToolBar();
            }
        });

        addKeyListener(this);
        sequence.addListener(this);
        PluginLoader.addListener(this);

        // do this when viewer is initialized
        Icy.getMainInterface().registerViewer(this);
        // automatically add it to the desktop pane
        addToDesktopPane();

        if (visible)
        {
            setVisible(true);
            requestFocus();
        }
        else
            setVisible(false);

        // can be done after setVisible
        buildActionMap();
    }

    public Viewer(Sequence sequence)
    {
        this(sequence, true);
    }

    void buildActionMap()
    {
        // global input map
        buildActionMap(getInputMap(JComponent.WHEN_FOCUSED), getActionMap());
    }

    private void buildActionMap(InputMap imap, ActionMap amap)
    {
        imap.put(WindowActions.gridTileAction.getKeyStroke(), WindowActions.gridTileAction.getName());
        imap.put(WindowActions.horizontalTileAction.getKeyStroke(), WindowActions.horizontalTileAction.getName());
        imap.put(WindowActions.verticalTileAction.getKeyStroke(), WindowActions.verticalTileAction.getName());
        imap.put(CanvasActions.globalDisableSyncAction.getKeyStroke(), CanvasActions.globalDisableSyncAction.getName());
        imap.put(CanvasActions.globalSyncGroup1Action.getKeyStroke(), CanvasActions.globalSyncGroup1Action.getName());
        imap.put(CanvasActions.globalSyncGroup2Action.getKeyStroke(), CanvasActions.globalSyncGroup2Action.getName());
        imap.put(CanvasActions.globalSyncGroup3Action.getKeyStroke(), CanvasActions.globalSyncGroup3Action.getName());
        imap.put(CanvasActions.globalSyncGroup4Action.getKeyStroke(), CanvasActions.globalSyncGroup4Action.getName());

        amap.put(WindowActions.gridTileAction.getName(), WindowActions.gridTileAction);
        amap.put(WindowActions.horizontalTileAction.getName(), WindowActions.horizontalTileAction);
        amap.put(WindowActions.verticalTileAction.getName(), WindowActions.verticalTileAction);
        amap.put(CanvasActions.globalDisableSyncAction.getName(), CanvasActions.globalDisableSyncAction);
        amap.put(CanvasActions.globalSyncGroup1Action.getName(), CanvasActions.globalSyncGroup1Action);
        amap.put(CanvasActions.globalSyncGroup2Action.getName(), CanvasActions.globalSyncGroup2Action);
        amap.put(CanvasActions.globalSyncGroup3Action.getName(), CanvasActions.globalSyncGroup3Action);
        amap.put(CanvasActions.globalSyncGroup4Action.getName(), CanvasActions.globalSyncGroup4Action);
    }

    /**
     * Called when viewer is closed.<br>
     * Free as much references we can here because of the
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4759312">
     * JInternalFrame bug</a>.
     */
    @Override
    public void onClosed()
    {
        // notify close
        fireViewerClosed();

        // remove listeners
        sequence.removeListener(this);
        canvas.removeCanvasListener(this);
        PluginLoader.removeListener(this);

        icy.main.Icy.getMainInterface().unRegisterViewer(this);

        // AWT JDesktopPane keep reference on last closed JInternalFrame
        // it's good to free as much reference we can here
        canvas.shutDown();

        lutViewer.removeAll();
        mainPanel.removeAll();
        toolBar.removeAll();

        // remove all listeners for this viewer
        ViewerListener[] vls = listeners.getListeners(ViewerListener.class);
        for (ViewerListener vl : vls)
            listeners.remove(ViewerListener.class, vl);

        lutViewer = null;
        mainPanel = null;

        canvas = null;
        sequence = null;
        lut = null;
        toolBar = null;
        canvasComboBox = null;
        lockComboBox = null;
        duplicateButton = null;
        layersEnabledButton = null;
        screenShotAlternateButton = null;
        screenShotButton = null;
        switchStateButton = null;

        super.onClosed();
    }

    void adjustViewerToImageSize()
    {
        if (canvas instanceof IcyCanvas2D)
        {
            final IcyCanvas2D cnv = (IcyCanvas2D) canvas;

            final int ix = cnv.getImageSizeX();
            final int iy = cnv.getImageSizeY();

            if ((ix > 0) && (iy > 0))
            {
                // find scale factor to fit image in a 640x540 sized window
                // and limit zoom to 100%
                final double scale = Math.min(Math.min(640d / ix, 540d / iy), 1d);

                cnv.setScaleX(scale);
                cnv.setScaleY(scale);

                // this actually resize viewer as canvas size depend from it
                cnv.fitCanvasToImage();
            }
        }

        // minimum size to start : 400, 240
        final Dimension size = new Dimension(Math.max(getWidth(), 400), Math.max(getHeight(), 240));
        // minimum size global : 200, 140
        final Dimension minSize = new Dimension(200, 140);

        // adjust size of both frames
        setSizeExternal(size);
        setSizeInternal(size);
        setMinimumSizeInternal(minSize);
        setMinimumSizeExternal(minSize);
    }

    /**
     * Rebuild and return viewer menu
     */
    JMenu getMenu()
    {
        final JMenu result = getDefaultSystemMenu();

        final JMenuItem overlayItem = new JMenuItem(CanvasActions.toggleLayersAction);
        if ((canvas != null) && canvas.isLayersVisible())
            overlayItem.setText("Hide layers");
        else
            overlayItem.setText("Show layers");
        final JMenuItem duplicateItem = new JMenuItem(ViewerActions.duplicateAction);

        // set menu
        result.insert(overlayItem, 0);
        result.insertSeparator(1);
        result.insert(duplicateItem, 2);

        return result;
    }

    private void buildLockCombo()
    {
        final ArrayList<JLabel> labels = new ArrayList<JLabel>();

        // get sync action labels
        labels.add(CanvasActions.disableSyncAction.getLabelComponent(true, false));
        labels.add(CanvasActions.syncGroup1Action.getLabelComponent(true, false));
        labels.add(CanvasActions.syncGroup2Action.getLabelComponent(true, false));
        labels.add(CanvasActions.syncGroup3Action.getLabelComponent(true, false));
        labels.add(CanvasActions.syncGroup4Action.getLabelComponent(true, false));

        // build comboBox with lock id
        lockComboBox = new JComboBox(labels.toArray());
        // set specific renderer
        lockComboBox.setRenderer(new LabelComboBoxRenderer(lockComboBox));
        // limit size
        ComponentUtil.setFixedWidth(lockComboBox, 48);
        lockComboBox.setToolTipText("Select synchronisation group");
        // don't want focusable here
        lockComboBox.setFocusable(false);
        // needed because of VTK
        lockComboBox.setLightWeightPopupEnabled(false);

        // action on canvas change
        lockComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // adjust lock id
                setViewSyncId(lockComboBox.getSelectedIndex());
            }
        });
    }

    private void buildCanvasCombo()
    {
        // build comboBox with canvas plugins
        canvasComboBox = new JComboBox(IcyCanvas.getCanvasPluginNames().toArray());
        // specific renderer
        canvasComboBox.setRenderer(new PluginComboBoxRenderer(canvasComboBox, false));
        // limit size
        ComponentUtil.setFixedWidth(canvasComboBox, 48);
        canvasComboBox.setToolTipText("Select canvas type");
        // don't want focusable here
        canvasComboBox.setFocusable(false);
        // needed because of VTK
        canvasComboBox.setLightWeightPopupEnabled(false);

        // action on canvas change
        canvasComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // set selected canvas
                setCanvas((String) canvasComboBox.getSelectedItem());
            }
        });
    }

    /**
     * build the toolBar
     */
    private void buildToolBar()
    {
        // build combo box
        buildLockCombo();
        buildCanvasCombo();

        // build buttons
        layersEnabledButton = new IcyToggleButton(new ToggleLayersAction(true));
        layersEnabledButton.setHideActionText(true);
        layersEnabledButton.setFocusable(false);
        layersEnabledButton.setSelected(true);
        screenShotButton = new IcyButton(CanvasActions.screenShotAction);
        screenShotButton.setFocusable(false);
        screenShotButton.setHideActionText(true);
        screenShotAlternateButton = new IcyButton(CanvasActions.screenShotAlternateAction);
        screenShotAlternateButton.setFocusable(false);
        screenShotAlternateButton.setHideActionText(true);
        duplicateButton = new IcyButton(ViewerActions.duplicateAction);
        duplicateButton.setFocusable(false);
        duplicateButton.setHideActionText(true);
        // duplicateButton.setToolTipText("Duplicate view (no data duplication)");
        switchStateButton = new IcyButton(getSwitchStateAction());
        switchStateButton.setFocusable(false);
        switchStateButton.setHideActionText(true);

        // and build the toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        // so we don't have any border
        toolBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        ComponentUtil.setPreferredHeight(toolBar, 26);

        updateToolbarComponents();
    }

    private void updateToolbarComponents()
    {
        toolBar.removeAll();

        toolBar.add(lockComboBox);
        toolBar.addSeparator();
        toolBar.add(canvasComboBox);
        toolBar.addSeparator();
        toolBar.add(layersEnabledButton);
        if (canvas != null)
            canvas.customizeToolbar(toolBar);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.addSeparator();
        toolBar.add(screenShotButton);
        toolBar.add(screenShotAlternateButton);
        toolBar.addSeparator();
        toolBar.add(duplicateButton);
        toolBar.add(switchStateButton);
    }

    void refreshLockCombo()
    {
        final int syncId = getViewSyncId();

        lockComboBox.setEnabled(isSynchronizedViewSupported());
        lockComboBox.setSelectedIndex(syncId);

        switch (syncId)
        {
            case 0:
                lockComboBox.setBackground(Color.gray);
                lockComboBox.setToolTipText("Synchronization disabled");
                break;

            case 1:
                lockComboBox.setBackground(Color.green);
                lockComboBox.setToolTipText("Full synchronization group 1 (view and Z/T position)");
                break;

            case 2:
                lockComboBox.setBackground(Color.yellow);
                lockComboBox.setToolTipText("Full synchronization group 2 (view and Z/T position)");
                break;

            case 3:
                lockComboBox.setBackground(Color.blue);
                lockComboBox.setToolTipText("View synchronization group (view synched but not Z/T position)");
                break;

            case 4:
                lockComboBox.setBackground(Color.red);
                lockComboBox.setToolTipText("Slice synchronization group (Z/T position synched but not view)");
                break;
        }
    }

    void refreshCanvasCombo()
    {
        if (canvas != null)
        {
            // get plugin class name for this canvas
            final String pluginName = IcyCanvas.getPluginClassName(canvas.getClass().getName());

            if (pluginName != null)
            {
                // align canvas combo to plugin name
                if (!canvasComboBox.getSelectedItem().equals(pluginName))
                    canvasComboBox.setSelectedItem(pluginName);
            }
        }
    }

    void refreshToolBar()
    {
        // FIXME : switchStateButton stay selected after action

        final boolean layersVisible = (canvas != null) ? canvas.isLayersVisible() : false;

        layersEnabledButton.setSelected(layersVisible);
        if (layersVisible)
            layersEnabledButton.setToolTipText("Hide layers");
        else
            layersEnabledButton.setToolTipText("Show layers");

        // refresh combos
        refreshLockCombo();
        refreshCanvasCombo();
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence()
    {
        return sequence;
    }

    /**
     * Set the specified LUT for the viewer.
     */
    public void setLut(LUT value)
    {
        if ((lut != value) && sequence.isLutCompatible(value))
        {
            // set new lut & notify change
            lut = value;
            lutChanged();
        }
    }

    /**
     * Returns the viewer LUT
     */
    public LUT getLut()
    {
        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return lut;

        // sequence can be asynchronously modified so we have to test change on Getter
        if ((lut == null) || !sequence.isLutCompatible(lut))
        {
            // sequence type has changed, we need to recreate a compatible LUT
            final LUT newLut = sequence.createCompatibleLUT();

            // keep the color map of previous LUT if they have the same number of channels
            if ((lut != null) && (lut.getNumChannel() == newLut.getNumChannel()))
                newLut.getColorSpace().setColorMaps(lut.getColorSpace(), true);

            // set the new lut
            setLut(newLut);
        }

        return lut;
    }

    /**
     * Set the specified canvas for the viewer (from the {@link PluginCanvas} class name).
     * 
     * @see IcyCanvas#getCanvasPluginNames()
     */
    public void setCanvas(String pluginClassName)
    {
        // not the same canvas ?
        if ((canvas == null) || !canvas.getClass().getName().equals(IcyCanvas.getCanvasClassName(pluginClassName)))
        {
            final int saveX;
            final int saveY;
            final int saveZ;
            final int saveT;
            final int saveC;

            if (canvas != null)
            {
                // save position
                saveX = canvas.getPositionX();
                saveY = canvas.getPositionY();
                saveZ = canvas.getPositionZ();
                saveT = canvas.getPositionT();
                saveC = canvas.getPositionC();

                canvas.removePropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
                canvas.removeCanvasListener(this);
                canvas.shutDown();
                // remove from mainPanel
                mainPanel.remove(canvas);
            }
            else
                saveX = saveY = saveZ = saveT = saveC = -1;

            // set new canvas
            canvas = IcyCanvas.create(pluginClassName, this);

            if (canvas != null)
            {
                canvas.addCanvasListener(this);
                canvas.addPropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
                // add to mainPanel
                mainPanel.add(canvas, BorderLayout.CENTER);

                // restore position
                if (saveX != -1)
                    canvas.setPositionX(saveX);
                if (saveY != -1)
                    canvas.setPositionY(saveY);
                if (saveZ != -1)
                    canvas.setPositionZ(saveZ);
                if (saveT != -1)
                    canvas.setPositionT(saveT);
                if (saveC != -1)
                    canvas.setPositionC(saveC);
            }
            else
            {
                // new canvas not created ?
                MessageDialog.showDialog("Cannot create Canvas from plugin " + pluginClassName + " !", MessageDialog.ERROR_MESSAGE);
            }

            mainPanel.revalidate();

            // refresh viewer menu (so overlay checkbox is correctly set)
            updateSystemMenu();
            updateToolbarComponents();
            refreshToolBar();

            // fix the OSX lost keyboard focus on canvas change in detached mode.
            KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle(getCanvas());

            // notify canvas changed to listener
            fireViewerChanged(ViewerEventType.CANVAS_CHANGED);
        }
    }

    /**
     * @deprecated Use {@link #setCanvas(String)} instead.
     */
    @Deprecated
    public void setCanvas(IcyCanvas value)
    {
        if (canvas == value)
            return;

        final int saveX;
        final int saveY;
        final int saveZ;
        final int saveT;
        final int saveC;

        if (canvas != null)
        {
            // save position
            saveX = canvas.getPositionX();
            saveY = canvas.getPositionY();
            saveZ = canvas.getPositionZ();
            saveT = canvas.getPositionT();
            saveC = canvas.getPositionC();

            canvas.removePropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
            canvas.removeCanvasListener(this);
            canvas.shutDown();
            // remove from mainPanel
            mainPanel.remove(canvas);
        }
        else
            saveX = saveY = saveZ = saveT = saveC = -1;

        // set new canvas
        canvas = value;

        if (canvas != null)
        {
            canvas.addCanvasListener(this);
            canvas.addPropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
            // add to mainPanel
            mainPanel.add(canvas, BorderLayout.CENTER);

            // restore position
            if (saveX != -1)
                canvas.setPositionX(saveX);
            if (saveY != -1)
                canvas.setPositionY(saveY);
            if (saveZ != -1)
                canvas.setPositionZ(saveZ);
            if (saveT != -1)
                canvas.setPositionT(saveT);
            if (saveC != -1)
                canvas.setPositionC(saveC);
        }

        mainPanel.revalidate();

        // refresh viewer menu (so overlay checkbox is correctly set)
        updateSystemMenu();
        updateToolbarComponents();
        refreshToolBar();

        // fix the OSX lost keyboard focus on canvas change in detached mode.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle(getCanvas());

        // notify canvas changed to listener
        fireViewerChanged(ViewerEventType.CANVAS_CHANGED);
    }

    /**
     * Returns true if the viewer initialization (correct image resizing) is completed.
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Return the viewer Canvas object
     */
    public IcyCanvas getCanvas()
    {
        return canvas;
    }

    /**
     * Return the viewer Canvas panel
     */
    public JPanel getCanvasPanel()
    {
        if (canvas != null)
            return canvas.getPanel();

        return null;
    }

    /**
     * Return the viewer Lut panel
     */
    public LUTViewer getLutViewer()
    {
        return lutViewer;
    }

    /**
     * Set the {@link LUTViewer} for this viewer.
     */
    public void setLutViewer(LUTViewer value)
    {
        lutViewer = value;
    }

    /**
     * @deprecated Use {@link #getLutViewer()} instead
     */
    @Deprecated
    public IcyLutViewer getLutPanel()
    {
        return getLutViewer();
    }

    /**
     * @deprecated Use {@link #setLutViewer(LUTViewer)} instead.
     */
    @Deprecated
    public void setLutPanel(IcyLutViewer lutViewer)
    {
        setLutViewer((LUTViewer) lutViewer);
    }

    /**
     * Return the viewer ToolBar object
     */
    public JToolBar getToolBar()
    {
        return toolBar;
    }

    /**
     * @return current T (-1 if all selected/displayed)
     */
    public int getPositionT()
    {
        if (canvas != null)
            return canvas.getPositionT();

        return 0;
    }

    /**
     * Set the current T position (for multi frame sequence).
     */
    public void setPositionT(int t)
    {
        if (canvas != null)
            canvas.setPositionT(t);
    }

    /**
     * @return current Z (-1 if all selected/displayed)
     */
    public int getPositionZ()
    {
        if (canvas != null)
            return canvas.getPositionZ();

        return 0;
    }

    /**
     * Set the current Z position (for stack sequence).
     */
    public void setPositionZ(int z)
    {
        if (canvas != null)
            canvas.setPositionZ(z);
    }

    /**
     * @return current C (-1 if all selected/displayed)
     */
    public int getPositionC()
    {
        if (canvas != null)
            return canvas.getPositionC();

        return 0;
    }

    /**
     * Set the current C (channel) position (multi channel sequence)
     */
    public void setPositionC(int c)
    {
        if (canvas != null)
            canvas.setPositionC(c);
    }

    /**
     * @deprecated Use {@link #getPositionT()} instead.
     */
    @Deprecated
    public int getT()
    {
        return getPositionT();
    }

    /**
     * @deprecated Use {@link #setPositionT(int)} instead.
     */
    @Deprecated
    public void setT(int t)
    {
        setPositionT(t);
    }

    /**
     * @deprecated Use {@link #getPositionZ()} instead.
     */
    @Deprecated
    public int getZ()
    {
        return getPositionZ();
    }

    /**
     * @deprecated Use {@link #setPositionZ(int)} instead.
     */
    @Deprecated
    public void setZ(int z)
    {
        setPositionZ(z);
    }

    /**
     * @deprecated Use {@link #getPositionZ()} instead.
     */
    @Deprecated
    public int getC()
    {
        return getPositionC();
    }

    /**
     * @deprecated Use {@link #setPositionZ(int)} instead.
     */
    @Deprecated
    public void setC(int c)
    {
        setPositionC(c);
    }

    /**
     * Get maximum T value
     */
    public int getMaxT()
    {
        if (canvas != null)
            return canvas.getMaxPositionT();

        return 0;
    }

    /**
     * Get maximum Z value
     */
    public int getMaxZ()
    {
        if (canvas != null)
            return canvas.getMaxPositionZ();

        return 0;
    }

    /**
     * Get maximum C value
     */
    public int getMaxC()
    {
        if (canvas != null)
            return canvas.getMaxPositionC();

        return 0;
    }

    /**
     * return true if current canvas's viewer does support synchronized view
     */
    public boolean isSynchronizedViewSupported()
    {
        if (canvas != null)
            return canvas.isSynchronizationSupported();

        return false;
    }

    /**
     * @return the viewSyncId
     */
    public int getViewSyncId()
    {
        if (canvas != null)
            return canvas.getSyncId();

        return -1;
    }

    /**
     * Set the view synchronization group id (0 means unsynchronized).
     * 
     * @param id
     *        the view synchronization id to set
     * @see IcyCanvas#setSyncId(int)
     */
    public boolean setViewSyncId(int id)
    {
        if (canvas != null)
            return canvas.setSyncId(id);

        return false;
    }

    /**
     * Return true if this viewer has its view synchronized
     */
    public boolean isViewSynchronized()
    {
        if (canvas != null)
            return canvas.isSynchronized();

        return false;
    }

    /**
     * Delegation for {@link IcyCanvas#getImage(int, int, int)}
     */
    public IcyBufferedImage getImage(int t, int z, int c)
    {
        if (canvas != null)
            return canvas.getImage(t, z, c);

        return null;
    }

    /**
     * @deprecated Use {@link #getImage(int, int, int)} with C = -1 instead.
     */
    @Deprecated
    public IcyBufferedImage getImage(int t, int z)
    {
        return getImage(t, z, -1);
    }

    /**
     * Get the current image
     * 
     * @return current image
     */
    public IcyBufferedImage getCurrentImage()
    {
        if (canvas != null)
            return canvas.getCurrentImage();

        return null;
    }

    /**
     * Return the number of "selected" samples
     */
    public int getNumSelectedSamples()
    {
        if (canvas != null)
            return canvas.getNumSelectedSamples();

        return 0;
    }

    /**
     * @see icy.canvas.IcyCanvas#getRenderedImage(int, int, int, boolean)
     */
    public BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView)
    {
        return canvas.getRenderedImage(t, z, c, canvasView);
    }

    /**
     * @see icy.canvas.IcyCanvas#getRenderedSequence(boolean, icy.common.listener.ProgressListener)
     */
    public Sequence getRenderedSequence(boolean canvasView, ProgressListener progressListener)
    {
        return canvas.getRenderedSequence(canvasView, progressListener);
    }

    /**
     * Returns the T navigation panel.
     */
    protected TNavigationPanel getTNavigationPanel()
    {
        return canvas.getTNavigationPanel();
    }

    /**
     * Returns the frame rate (given in frame per second) for play command.
     */
    public int getFrameRate()
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.getFrameRate();

        return 0;
    }

    /**
     * Sets the frame rate (given in frame per second) for play command.
     */
    public void setFrameRate(int fps)
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.setFrameRate(fps);
    }

    /**
     * Returns true if <code>repeat</code> is enabled for play command.
     */
    public boolean isRepeat()
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.isRepeat();

        return false;
    }

    /**
     * Set <code>repeat</code> mode for play command.
     */
    public void setRepeat(boolean value)
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.setRepeat(value);
    }

    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying()
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.isPlaying();

        return false;
    }

    /**
     * Start sequence play.
     * 
     * @see #stopPlay()
     * @see #setRepeat(boolean)
     */
    public void startPlay()
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.startPlay();
    }

    /**
     * Stop sequence play.
     * 
     * @see #startPlay()
     */
    public void stopPlay()
    {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.stopPlay();
    }

    /**
     * Return true if only this viewer is currently displaying its attached sequence
     */
    public boolean isUnique()
    {
        return Icy.getMainInterface().isUniqueViewer(this);
    }

    private void lutChanged()
    {
        // can be called from external thread, replace it in AWT dispatch thread
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // refresh LUT viewer
                setLutViewer(new LUTViewer(Viewer.this, getLut()));

                fireViewerChanged(ViewerEventType.LUT_CHANGED);
            }
        });
    }

    private void positionChanged(DimensionId dim)
    {
        fireViewerChanged(ViewerEventType.POSITION_CHANGED, dim);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ViewerListener listener)
    {
        listeners.add(ViewerListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ViewerListener listener)
    {
        listeners.remove(ViewerListener.class, listener);
    }

    void fireViewerChanged(ViewerEventType eventType, DimensionId dim)
    {
        final ViewerEvent event = new ViewerEvent(this, eventType, dim);

        for (ViewerListener viewerListener : listeners.getListeners(ViewerListener.class))
            viewerListener.viewerChanged(event);
    }

    void fireViewerChanged(ViewerEventType event)
    {
        fireViewerChanged(event, DimensionId.NULL);
    }

    private void fireViewerClosed()
    {
        for (ViewerListener viewerListener : listeners.getListeners(ViewerListener.class))
            viewerListener.viewerClosed(this);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyTyped(e);
    }

    /**
     * Change the frame's title.
     */
    private void refreshViewerTitle()
    {
        // have to test this as we release sequence reference on closed
        if (sequence != null)
            setTitle(sequence.getName());
    }

    @Override
    public void sequenceChanged(SequenceEvent event)
    {
        switch (event.getSourceType())
        {
            case SEQUENCE_META:
                final String meta = (String) event.getSource();

                if (StringUtil.isEmpty(meta) || StringUtil.equals(meta, Sequence.ID_NAME))
                    refreshViewerTitle();
                break;

            case SEQUENCE_DATA:

                break;

            case SEQUENCE_TYPE:
                // might need initialization
                if (!initialized && (sequence != null) && !sequence.isEmpty())
                {
                    adjustViewerToImageSize();
                    initialized = true;
                }

                // we update LUT on type change directly on getLut() method

                // // try to keep current LUT if possible
                if (!sequence.isLutCompatible(lut))
                    // need to update the lut according to the colormodel change
                    setLut(sequence.createCompatibleLUT());
                break;

            case SEQUENCE_COLORMAP:

                break;

            case SEQUENCE_COMPONENTBOUNDS:
                // refresh lut scalers from sequence lut
                final LUT sequenceLut = sequence.createCompatibleLUT();

                if (!sequenceLut.isCompatible(lut) || (lutViewer == null) || lutViewer.getAutoBounds())
                    lut.setScalers(sequenceLut);
                break;
        }
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {

    }

    @Override
    public void canvasChanged(IcyCanvasEvent event)
    {
        switch (event.getType())
        {
            case POSITION_CHANGED:
                // common process on position change
                positionChanged(event.getDim());
                break;

            case SYNC_CHANGED:
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshLockCombo();
                    }
                });
                break;
        }
    }

    /**
     * called when Canvas property "layer visible" changed
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        refreshToolBar();
        updateSystemMenu();
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // refresh available canvas
                if (canvasComboBox != null)
                {
                    canvasComboBox.setModel(new DefaultComboBoxModel(IcyCanvas.getCanvasPlugins().toArray()));
                    refreshCanvasCombo();
                }
            }
        });
    }
}
