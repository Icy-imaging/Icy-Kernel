/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.viewer;

import icy.canvas.Canvas2D;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasListener;
import icy.common.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.component.renderer.LabelComboBoxRenderer;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.lut.LUTViewer;
import icy.gui.lut.abstract_.IcyLutViewer;
import icy.gui.plugin.PluginComboBoxRenderer;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginCanvas;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.Random;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

import plugins.kernel.canvas.Canvas3DPlugin;

/**
 * Viewer send an event if the IcyCanvas change.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Viewer extends IcyFrame implements KeyListener, SequenceListener, IcyCanvasListener
{
    static final Image ICON_LAYER = ResourceUtil.getAlphaIconAsImage("layers_2.png");
    static final Image ICON_DUPLICATE = ResourceUtil.getAlphaIconAsImage("duplicate.png");
    static final Image ICON_PHOTO = ResourceUtil.getAlphaIconAsImage("photo.png");
    static final Image ICON_PHOTO_SMALL = ResourceUtil.getAlphaIconAsImage("photo_small.png");
    static final Image ICON_UNLOCKED = ResourceUtil.getAlphaIconAsImage("padlock_open.png");

    private class DuplicateAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6394551300296675713L;

        public DuplicateAction()
        {
            super("Duplicate view", new IcyIcon(ICON_DUPLICATE), "Duplicate view", KeyEvent.VK_F2);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final Viewer v = new Viewer(sequence);
                    // copy LUT
                    v.getLut().copyFrom(getLut());
                }
            });
        }
    }

    private class ScreenShotAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4213236521854007422L;

        public ScreenShotAction()
        {
            super("", new IcyIcon(ICON_PHOTO), "Take a screenshot of current view");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            // so it won't change during process
            final IcyCanvas canvas = getCanvas();
            final Sequence seqIn = getSequence();

            if ((seqIn != null) && (canvas != null))
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Rendering...");
                        try
                        {
                            final Sequence seqOut = canvas.getRenderedSequence(true, pf);

                            if (seqOut != null)
                            {
                                // set sequence name
                                seqOut.setName("Screen shot of '" + seqIn.getName() + "' view");
                                // add sequence
                                Icy.addSequence(seqOut);
                            }
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        }
    }

    private class ScreenShotAlternateAction extends IcyAbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4213236521854007422L;

        public ScreenShotAlternateAction()
        {
            super("", new IcyIcon(ICON_PHOTO_SMALL),
                    "Take a screenshot of current view with original sequence dimensions");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            // so it won't change during process
            final IcyCanvas canvas = getCanvas();
            final Sequence seqIn = getSequence();

            if ((seqIn != null) && (canvas != null))
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Rendering...");
                        try
                        {
                            final Sequence seqOut = canvas.getRenderedSequence(false, pf);

                            if (seqOut != null)
                            {
                                // set sequence name
                                seqOut.setName("Rendering shot of '" + seqIn.getName() + "' view");
                                // add sequence
                                Icy.addSequence(seqOut);
                            }
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        }
    }

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
    private IcyLutViewer lutPanel;

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
    boolean sizeAjusted;

    public Viewer(Sequence sequence, boolean visible)
    {
        super("Viewer", true, true, true, true);

        if (sequence == null)
            throw new IllegalArgumentException("Can't open a null sequence.");

        this.sequence = sequence;

        // default
        canvas = null;
        lut = null;
        sizeAjusted = false;

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
        refreshToolBar();

        mainPanel.setLayout(new BorderLayout());

        // set lut (this modify lutPanel)
        setLut(sequence.createCompatibleLUT());
        // set default canvas to Canvas2D (this modify mainPanel)
        setCanvas(new Canvas2D(this));

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
        setZ(((sequence.getSizeZ() + 1) / 2) - 1);

        addFrameListener(new IcyFrameAdapter()
        {
            @Override
            public void icyFrameOpened(IcyFrameEvent e)
            {
                if (!sizeAjusted)
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

                    sizeAjusted = true;
                }
            }

            @Override
            public void icyFrameActivated(IcyFrameEvent e)
            {
                Icy.getMainInterface().setFocusedViewer(Viewer.this);
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

        // do this when viewer is initialized
        icy.main.Icy.getMainInterface().registerViewer(this);
        // automatically add it to the desktop pane
        addToMainDesktopPane();

        if (visible)
        {
            setVisible(true);
            requestFocus();
        }
        else
            setVisible(false);
    }

    public Viewer(Sequence sequence)
    {
        this(sequence, true);
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

        icy.main.Icy.getMainInterface().unRegisterViewer(this);

        // AWT JDesktopPane keep reference on last closed JInternalFrame
        // it's good to free as much reference we can here
        canvas.shutDown();

        lutPanel.removeAll();
        mainPanel.removeAll();
        toolBar.removeAll();

        // remove all listeners for this viewer
        ViewerListener[] vls = listeners.getListeners(ViewerListener.class);
        for (ViewerListener vl : vls)
            listeners.remove(ViewerListener.class, vl);

        lutPanel = null;
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

    private ArrayList<PluginDescriptor> getCanvasPlugins()
    {
        // get all canvas plugins
        final ArrayList<PluginDescriptor> result = PluginLoader.getPlugins(PluginCanvas.class);

        // remove VTK canvas if VTK is not loaded
        if (!Icy.vktLibraryLoaded)
            PluginDescriptor.removeFromList(result, Canvas3DPlugin.class.getName());

        return result;
    }

    /**
     * Rebuild and return viewer menu
     */
    JMenu getMenu()
    {
        final JMenu result = getDefaultSystemMenu();

        final JMenuItem overlayItem = new JMenuItem("Display layers");
        overlayItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        overlayItem.setIcon(new IcyIcon(ICON_LAYER));
        if ((canvas != null) && canvas.getDrawLayers())
            overlayItem.setText("Hide layers");
        else
            overlayItem.setText("Show layers");

        overlayItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (canvas != null)
                {
                    canvas.setDrawLayers(!canvas.getDrawLayers());
                    updateSystemMenu();
                    refreshToolBar();
                }
            }
        });

        final JMenuItem duplicateItem = new JMenuItem(new DuplicateAction());

        // set menu
        result.insert(overlayItem, 0);
        result.insertSeparator(1);
        result.insert(duplicateItem, 2);

        return result;
    }

    private void buildLockCombo()
    {
        final ArrayList<JLabel> labels = new ArrayList<JLabel>();
        JLabel label;

        // no synchro
        label = new JLabel(new IcyIcon(ICON_UNLOCKED));
        label.setToolTipText("Synchronization disabled");
        labels.add(label);

        // complete synchro
        label = new JLabel(new IcyIcon(ResourceUtil.getLockedImage(1)));
        label.setToolTipText("Full synchronization group 1 (view and Z/T position)");
        labels.add(label);
        label = new JLabel(new IcyIcon(ResourceUtil.getLockedImage(2)));
        label.setToolTipText("Full synchronization group 2 (view and Z/T position)");
        labels.add(label);

        // view synchro
        label = new JLabel(new IcyIcon(ResourceUtil.getLockedImage(3)));
        label.setToolTipText("View synchronization group (view synched but not Z/T position)");
        labels.add(label);

        // position synchro
        label = new JLabel(new IcyIcon(ResourceUtil.getLockedImage(4)));
        label.setToolTipText("Slice synchronization group (Z/T position synched but not view)");
        labels.add(label);

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
        canvasComboBox = new JComboBox(getCanvasPlugins().toArray());
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
                final PluginDescriptor plugin = (PluginDescriptor) canvasComboBox.getSelectedItem();

                try
                {
                    final PluginCanvas pluginCanvas = (PluginCanvas) plugin.getPluginClass().newInstance();

                    final String newCanvasClassName = pluginCanvas.getCanvasClassName();
                    final String currentCanvasClassName;

                    if (canvas != null)
                        currentCanvasClassName = canvas.getClass().getName();
                    else
                        currentCanvasClassName = "";

                    // canvas change ?
                    if (!currentCanvasClassName.equals(newCanvasClassName))
                    {
                        final IcyCanvas newCanvas = IcyCanvas.create(pluginCanvas, Viewer.this);

                        if (newCanvas != null)
                            setCanvas(newCanvas);
                    }
                }
                catch (Exception exc)
                {
                    IcyExceptionHandler.showErrorMessage(exc, true);
                }
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
        layersEnabledButton = new IcyToggleButton(ICON_LAYER);
        layersEnabledButton.setToolTipText("Hide layers");
        layersEnabledButton.setFocusable(false);
        layersEnabledButton.setSelected(true);
        layersEnabledButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final IcyCanvas canvas = getCanvas();

                if (canvas != null)
                    canvas.setDrawLayers(layersEnabledButton.isSelected());

                if (layersEnabledButton.isSelected())
                    layersEnabledButton.setToolTipText("Hide layers");
                else
                    layersEnabledButton.setToolTipText("Show layers");

                updateSystemMenu();
            }
        });

        screenShotButton = new IcyButton(new ScreenShotAction());
        screenShotButton.setFocusable(false);
        screenShotButton.setHideActionText(true);
        screenShotAlternateButton = new IcyButton(new ScreenShotAlternateAction());
        screenShotButton.setFocusable(false);
        screenShotButton.setHideActionText(true);
        duplicateButton = new IcyButton(new DuplicateAction());
        duplicateButton.setFocusable(false);
        duplicateButton.setHideActionText(true);
        duplicateButton.setToolTipText("Duplicate view (no data duplication)");
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
            canvas.addViewerToolbarComponents(toolBar);
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

    private void refreshCanvasCombo()
    {
        if (canvas != null)
        {
            // select current active canvas
            final String currentCanvasClassName = canvas.getClass().getName();

            for (PluginDescriptor plugin : getCanvasPlugins())
            {
                try
                {
                    final PluginCanvas pluginCanvas = (PluginCanvas) plugin.getPluginClass().newInstance();
                    final String newCanvasClassName = pluginCanvas.getCanvasClassName();

                    // canvas change ? --> find corresponding plugin in comboBox and select it
                    if (currentCanvasClassName.equals(newCanvasClassName))
                    {
                        canvasComboBox.setSelectedItem(plugin);
                        return;
                    }
                }
                catch (Exception e)
                {
                    IcyExceptionHandler.showErrorMessage(e, true);
                }
            }
        }
    }

    void refreshToolBar()
    {
        // FIXME : switchStateButton stay selected after action

        if (canvas != null)
            layersEnabledButton.setSelected(canvas.getDrawLayers());

        if (layersEnabledButton.isSelected())
            layersEnabledButton.setToolTipText("Hide layers");
        else
            layersEnabledButton.setToolTipText("Show layers");

        // refresh combos
        refreshLockCombo();
        refreshCanvasCombo();

        // switchStateButtonif (!canBeInternalized())
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
        if (!sequence.isLutCompatible(lut))
        {
            // sequence type has changed, we need to recreate a compatible LUT
            final LUT newLut = sequence.createCompatibleLUT();

            // restore the color map of previous LUT
            if (lut != null)
                newLut.getColorSpace().copyColormaps(lut.getColorSpace());

            // set the new lut
            setLut(newLut);
        }

        return lut;
    }

    /**
     * Set the specified canvas for the viewer.
     */
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

        // notify canvas changed to listener
        fireViewerChanged(ViewerEventType.CANVAS_CHANGED);
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
    public IcyLutViewer getLutPanel()
    {
        return lutPanel;
    }

    /**
     * Refresh lut panel
     */
    public void setLutPanel(IcyLutViewer lutViewer)
    {
        lutPanel = lutViewer;
    }

    /**
     * Return the viewer ToolBar object
     */
    public JToolBar getToolBar()
    {
        return toolBar;
    }

    /**
     * @return current T (-1 if all selected)
     */
    public int getT()
    {
        if (canvas != null)
            return canvas.getPositionT();

        return 0;
    }

    /**
     * @param t
     *        T position
     */
    public void setT(int t)
    {
        if (canvas != null)
            canvas.setPositionT(t);
    }

    /**
     * @return current Z (-1 if all selected)
     */
    public int getZ()
    {
        if (canvas != null)
            return canvas.getPositionZ();

        return 0;
    }

    /**
     * @param z
     *        Z position
     */
    public void setZ(int z)
    {
        if (canvas != null)
            canvas.setPositionZ(z);
    }

    /**
     * @return current C (-1 if all selected)
     */
    public int getC()
    {
        if (canvas != null)
            return canvas.getPositionC();

        return 0;
    }

    /**
     * @param c
     *        C position
     */
    public void setC(int c)
    {
        if (canvas != null)
            canvas.setPositionC(c);
    }

    /**
     * Get maximum T value
     */
    public int getMaxT()
    {
        if (canvas != null)
            return canvas.getMaxT();

        return 0;
    }

    /**
     * Get maximum Z value
     */
    public int getMaxZ()
    {
        if (canvas != null)
            return canvas.getMaxZ();

        return 0;
    }

    /**
     * Get maximum C value
     */
    public int getMaxC()
    {
        if (canvas != null)
            return canvas.getMaxC();

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
     * Set the view synchronization group id (-1 means unsynchronized)
     * 
     * @param id
     *        the view synchronization id to set
     */
    public void setViewSyncId(int id)
    {
        if (canvas != null)
            canvas.setSyncId(id);
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
     * Get the image at position (t, z)
     * 
     * @return image[t, z]
     */
    public IcyBufferedImage getImage(int t, int z)
    {
        if (canvas != null)
            return canvas.getImage(t, z);

        return null;
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
     * Return true if only this viewer is currently displaying its attached sequence
     */
    public boolean isUnique()
    {
        return Icy.getMainInterface().isUniqueViewer(this);
    }

    private void lutChanged()
    {
        // final ArrayList<PluginDescriptor> result =
        // PluginLoader.getPlugins(PluginLutViewer.class);
        //
        // if (result.size() > 0)
        // {
        // try
        // {
        // final Plugin plugin = result.get(0).getPluginClass().newInstance();
        // setLutPanel(((PluginLutViewer) plugin).createLutViewer(this, getLut()));
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }
        // }

        // can be called from external thread, replace it in AWT dispatch thread
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // refresh LUT viewer
                setLutPanel(new LUTViewer(Viewer.this, getLut()));

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
                // we update LUT on type change directly on getLut() method

                // // try to keep current LUT if possible
                // if (!sequence.isCompatible(lut))
                // // need to update the lut according to the colormodel change
                // setLut(sequence.createCompatibleLUT());
                break;

            case SEQUENCE_COLORMAP:

                break;

            case SEQUENCE_COMPONENTBOUNDS:
                // refresh lut scalers from sequence lut
                lut.copyScalers(sequence.createCompatibleLUT());
                break;

            case SEQUENCE_PAINTER:

                break;

            case SEQUENCE_ROI:

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
     * called when "detached" mode changed
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        refreshToolBar();
    }
}
