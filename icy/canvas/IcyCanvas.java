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
package icy.canvas;

import icy.action.CanvasActions;
import icy.action.GeneralActions;
import icy.action.RoiActions;
import icy.action.WindowActions;
import icy.canvas.CanvasLayerEvent.LayersEventType;
import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.canvas.Layer.LayerListener;
import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.common.listener.ProgressListener;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.MouseImageInfosPanel;
import icy.gui.viewer.TNavigationPanel;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.gui.viewer.ZNavigationPanel;
import icy.image.IcyBufferedImage;
import icy.image.colormodel.IcyColorModel;
import icy.image.lut.LUT;
import icy.image.lut.LUTEvent;
import icy.image.lut.LUTEvent.LUTEventType;
import icy.image.lut.LUTListener;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.painter.OverlayWrapper;
import icy.painter.Painter;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginCanvas;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.sequence.SequenceListener;
import icy.system.IcyExceptionHandler;
import icy.type.collection.CollectionUtil;
import icy.type.point.Point5D;
import icy.util.ClassUtil;
import icy.util.EventUtil;
import icy.util.OMEUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;

import plugins.kernel.canvas.Canvas2DPlugin;
import plugins.kernel.canvas.Canvas3DPlugin;

/**
 * @author Fabrice de Chaumont & Stephane Dallongeville<br>
 * <br>
 *         An IcyCanvas is a basic Canvas used into the viewer. It contains a visual representation
 *         of the sequence and provides some facilities as basic transformation and view
 *         synchronization.<br>
 *         Also IcyCanvas receives key events from Viewer when they are not consumed.<br>
 * <br>
 *         By default transformations are applied in following order :<br>
 *         Rotation, Translation then Scaling.<br>
 *         The rotation transformation is relative to canvas center.<br>
 * <br>
 *         Free feel to implement and override this design or not. <br>
 * <br>
 *         (Canvas2D and Canvas3D derives from IcyCanvas)<br>
 */
public abstract class IcyCanvas extends JPanel implements KeyListener, ViewerListener, SequenceListener, LUTListener,
        ChangeListener, LayerListener
{
    protected class IcyCanvasImageOverlay extends Overlay
    {
        public IcyCanvasImageOverlay()
        {
            super((getSequence() == null) ? "Image" : getSequence().getName(), OverlayPriority.IMAGE_NORMAL);

            // we fix the image overlay
            canBeRemoved = false;
            readOnly = false;
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // default lazy implementation (very slow)
            if (g != null)
                g.drawImage(getCurrentImage(), null, 0, 0);
        }
    }

    /**
     * Return the class name of all {@link PluginCanvas}.
     */
    public static List<String> getCanvasPlugins()
    {
        // get all canvas plugins
        final List<PluginDescriptor> plugins = PluginLoader.getPlugins(PluginCanvas.class);

        // remove VTK canvas if VTK is not loaded
        if (!Icy.isVtkLibraryLoaded())
            PluginDescriptor.removeFromList(plugins, Canvas3DPlugin.class.getName());

        final List<String> result = new ArrayList<String>();

        // we want the kernel canvas to be first
        result.add(Canvas2DPlugin.class.getName());
        if (Icy.isVtkLibraryLoaded())
            result.add(Canvas3DPlugin.class.getName());

        for (PluginDescriptor plugin : plugins)
        {
            final String className = plugin.getClassName();

            // ignore kernel as they have been already added
            if (Canvas2DPlugin.class.getName().equals(className))
                continue;
            if (Canvas3DPlugin.class.getName().equals(className))
                continue;

            CollectionUtil.addUniq(result, plugin.getClassName());
        }

        return result;
    }

    /**
     * Create a {@link IcyCanvas} object from its class name or {@link PluginCanvas} class name.<br>
     * 
     * @param viewer
     *        {@link Viewer} to which to canvas is attached.
     */
    public static IcyCanvas create(String className, Viewer viewer)
    {
        IcyCanvas result = null;

        try
        {
            // search for the specified className
            final Class<?> clazz = ClassUtil.findClass(className);

            // class found
            if (clazz != null)
            {
                try
                {
                    // we first check if we have a IcyCanvas Plugin class here
                    final Class<? extends PluginCanvas> canvasClazz = clazz.asSubclass(PluginCanvas.class);
                    // create canvas
                    result = canvasClazz.newInstance().createCanvas(viewer);
                }
                catch (ClassCastException e0)
                {
                    // check if this is a IcyCanvas class
                    final Class<? extends IcyCanvas> canvasClazz = clazz.asSubclass(IcyCanvas.class);

                    // get constructor (Viewer)
                    final Constructor<? extends IcyCanvas> constructor = canvasClazz
                            .getConstructor(new Class[] {Viewer.class});
                    // build canvas
                    result = constructor.newInstance(new Object[] {viewer});
                }
            }
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            result = null;
        }

        return result;
    }

    public static void addVisibleLayerToList(final Layer layer, ArrayList<Layer> list)
    {
        if ((layer != null) && (layer.isVisible()))
            list.add(layer);
    }

    private static final long serialVersionUID = -8461229450296203011L;

    public static final String PROPERTY_LAYERS_VISIBLE = "layersVisible";

    /**
     * Navigations bar
     */
    final protected ZNavigationPanel zNav;
    final protected TNavigationPanel tNav;

    /**
     * The panel where mouse informations are displayed
     */
    protected final MouseImageInfosPanel mouseInfPanel;

    /**
     * The panel contains all settings and informations data such as<br>
     * scale factor, rendering mode...
     * Will be retrieved by the inspector to get information on the current canvas.
     */
    protected JPanel panel;

    /**
     * attached viewer
     */
    protected final Viewer viewer;
    /**
     * layers visible flag
     */
    protected boolean layersVisible;
    /**
     * synchronization group :<br>
     * 0 = unsynchronized
     * 1 = full synchronization group 1
     * 2 = full synchronization group 2
     * 3 = view synchronization group (T and Z navigation are not synchronized)
     * 4 = slice synchronization group (only T and Z navigation are synchronized)
     */
    protected int syncId;

    /**
     * Overlay/Layer used to display sequence image
     */
    protected final Overlay imageOverlay;
    protected final Layer imageLayer;

    /**
     * Layers attached to canvas<br>
     * There are representing sequence overlays with some visualization properties
     */
    protected final Map<Overlay, Layer> layers;
    /**
     * Priority ordered layers.
     */
    protected List<Layer> orderedLayers;

    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;

    /**
     * Current X position (should be -1 when canvas handle multi X dimension view).
     */
    protected int posX;
    /**
     * Current Y position (should be -1 when canvas handle multi Y dimension view).
     */
    protected int posY;
    /**
     * Current Z position (should be -1 when canvas handle multi Z dimension view).
     */
    protected int posZ;
    /**
     * Current T position (should be -1 when canvas handle multi T dimension view).
     */
    protected int posT;
    /**
     * Current C position (should be -1 when canvas handle multi C dimension view).
     */
    protected int posC;

    /**
     * Current mouse position (canvas coordinate space)
     */
    protected Point mousePos;

    /**
     * internals
     */
    protected LUT lut;
    protected boolean synchHeader;
    protected boolean orderedLayersOutdated;

    /**
     * Constructor
     * 
     * @param viewer
     */
    public IcyCanvas(Viewer viewer)
    {
        super();

        // default
        this.viewer = viewer;

        layersVisible = true;
        layers = new HashMap<Overlay, Layer>();
        orderedLayers = new ArrayList<Layer>();
        syncId = 0;
        synchHeader = false;
        orderedLayersOutdated = false;
        updater = new UpdateEventHandler(this, false);

        // default position
        mousePos = new Point(0, 0);
        posX = -1;
        posY = -1;
        posZ = -1;
        posT = -1;
        posC = -1;

        // GUI stuff
        panel = new JPanel();

        // Z navigation
        zNav = new ZNavigationPanel();
        zNav.addChangeListener(new javax.swing.event.ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set the new Z position
                setPositionZ(zNav.getValue());
            }
        });

        // T navigation
        tNav = new TNavigationPanel();
        tNav.addChangeListener(new javax.swing.event.ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set the new T position
                setPositionT(tNav.getValue());
            }
        });

        // mouse info panel
        mouseInfPanel = new MouseImageInfosPanel();

        // default canvas layout
        setLayout(new BorderLayout());

        add(zNav, BorderLayout.WEST);
        add(GuiUtil.createPageBoxPanel(tNav, mouseInfPanel), BorderLayout.SOUTH);

        // create image overlay
        imageOverlay = createImageOverlay();

        beginUpdate();
        try
        {
            // first add image layer
            imageLayer = addLayer(getImageOverlay());

            final Sequence sequence = getSequence();

            if (sequence != null)
            {
                // then add sequence overlays to layer list
                for (Overlay overlay : sequence.getOverlays())
                    addLayer(overlay);
            }
            else
                System.err.println("Sequence null when canvas created");
        }
        finally
        {
            endUpdate();
        }

        // add listeners
        viewer.addListener(this);
        final Sequence seq = getSequence();
        if (seq != null)
            seq.addListener(this);

        // set lut (no event wanted here)
        lut = null;
        setLut(viewer.getLut(), false);
    }

    /**
     * Called by the viewer when canvas is closed.
     */
    public void shutDown()
    {
        // remove navigation panel listener
        zNav.removeAllChangeListener();
        tNav.removeAllChangeListener();

        // remove listeners
        if (lut != null)
            lut.removeListener(this);
        final Sequence seq = getSequence();
        if (seq != null)
            seq.removeListener(this);
        viewer.removeListener(this);

        // remove all layers
        beginUpdate();
        try
        {
            for (Layer layer : getLayers())
                removeLayer(layer);
        }
        finally
        {
            endUpdate();
        }

        // release layers
        orderedLayers = null;

        // remove all IcyCanvas listeners
        final IcyCanvasListener[] canvasListenters = listenerList.getListeners(IcyCanvasListener.class);
        for (IcyCanvasListener listener : canvasListenters)
            removeCanvasListener(listener);

        // remove all Layers listeners
        final CanvasLayerListener[] layersListenters = listenerList.getListeners(CanvasLayerListener.class);
        for (CanvasLayerListener listener : layersListenters)
            removeLayerListener(listener);
    }

    /**
     * Force canvas refresh
     */
    public abstract void refresh();

    protected Overlay createImageOverlay()
    {
        // default image overlay
        return new IcyCanvasImageOverlay();
    }

    /**
     * Returns the {@link Overlay} used to display the current sequence image
     */
    public Overlay getImageOverlay()
    {
        return imageOverlay;
    }

    /**
     * Returns the {@link Layer} object used to display the current sequence image
     */
    public Layer getImageLayer()
    {
        return imageLayer;
    }

    /**
     * @deprecated Use {@link #isLayersVisible()} instead.
     */
    @Deprecated
    public boolean getDrawLayers()
    {
        return isLayersVisible();
    }

    /**
     * @deprecated Use {@link #setLayersVisible(boolean)} instead.
     */
    @Deprecated
    public void setDrawLayers(boolean value)
    {
        setLayersVisible(value);
    }

    /**
     * Return true if layers are visible on the canvas.
     */
    public boolean isLayersVisible()
    {
        return layersVisible;
    }

    /**
     * Make layers visible on this canvas (default = true).
     */
    public void setLayersVisible(boolean value)
    {
        if (layersVisible != value)
        {
            layersVisible = value;
            firePropertyChange(PROPERTY_LAYERS_VISIBLE, !value, value);

            final Component comp = getViewComponent();

            if (comp != null)
                comp.repaint();
        }
    }

    /**
     * @return the viewer
     */
    public Viewer getViewer()
    {
        return viewer;
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence()
    {
        return viewer.getSequence();
    }

    /**
     * @return the main view component
     */
    public abstract Component getViewComponent();

    /**
     * @return the Z navigation bar panel
     */
    public ZNavigationPanel getZNavigationPanel()
    {
        return zNav;
    }

    /**
     * @return the T navigation bar panel
     */
    public TNavigationPanel getTNavigationPanel()
    {
        return tNav;
    }

    /**
     * @return the mouse image informations panel
     */
    public MouseImageInfosPanel getMouseImageInfosPanel()
    {
        return mouseInfPanel;
    }

    /**
     * @return the LUT
     */
    public LUT getLut()
    {
        // ensure we have the good lut
        setLut(viewer.getLut(), true);

        return lut;
    }

    /**
     * set canvas LUT
     */
    private void setLut(LUT lut, boolean event)
    {
        if (this.lut != lut)
        {
            if (this.lut != null)
                this.lut.removeListener(this);

            this.lut = lut;

            // add listener to the new lut
            if (lut != null)
                lut.addListener(this);

            // launch a lutChanged event if wanted
            if (event)
                lutChanged(new LUTEvent(lut, -1, LUTEventType.COLORMAP_CHANGED));
        }
    }

    /**
     * @deprecated Use {@link #customizeToolbar(JToolBar)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void addViewerToolbarComponents(JToolBar toolBar)
    {

    }

    /**
     * Called by the parent viewer when building the toolbar.<br>
     * This way the canvas can customize it by adding specific command for instance.<br>
     * 
     * @param toolBar
     *        the parent toolbar to customize
     */
    public void customizeToolbar(JToolBar toolBar)
    {
        addViewerToolbarComponents(toolBar);
    }

    /**
     * Returns the setting panel of this canvas.<br>
     * The setting panel is displayed in the inspector so user can change canvas parameters.
     */
    public JPanel getPanel()
    {
        return panel;
    }

    /**
     * Returns all layers attached to this canvas.<br/>
     * 
     * @param sorted
     *        If <code>true</code> the returned list is sorted on the layer priority.<br>
     *        Sort operation is cached so the method could take sometime when sort cache need to be
     *        rebuild.
     */
    public List<Layer> getLayers(boolean sorted)
    {
        if (sorted)
        {
            // need to rebuild sorted layer list ?
            if (orderedLayersOutdated)
            {
                // build and sort the list
                synchronized (layers)
                {
                    orderedLayers = new ArrayList<Layer>(layers.values());
                }
                Collections.sort(orderedLayers);

                orderedLayersOutdated = false;
            }

            return new ArrayList<Layer>(orderedLayers);
        }

        synchronized (layers)
        {
            return new ArrayList<Layer>(layers.values());
        }
    }

    /**
     * Returns all layers attached to this canvas.<br/>
     * The returned list is sorted on the layer priority.<br>
     * Sort operation is cached so the method could take sometime when cache need to be rebuild.
     */
    public List<Layer> getLayers()
    {
        return getLayers(true);
    }

    /**
     * Returns all visible layers (visible property set to <code>true</code>) attached to this
     * canvas.
     * 
     * @param sorted
     *        If <code>true</code> the returned list is sorted on the layer priority.<br>
     *        Sort operation is cached so the method could take sometime when sort cache need to be
     *        rebuild.
     */
    public List<Layer> getVisibleLayers(boolean sorted)
    {
        final List<Layer> olayers = getLayers(sorted);
        final List<Layer> result = new ArrayList<Layer>(olayers.size());

        for (Layer l : olayers)
            if (l.isVisible())
                result.add(l);

        return result;
    }

    /**
     * Returns all visible layers (visible property set to <code>true</code>) attached to this
     * canvas.<br/>
     * The list is sorted on the layer priority.
     */
    public ArrayList<Layer> getVisibleLayers()
    {
        return (ArrayList<Layer>) getVisibleLayers(true);
    }

    /**
     * @deprecated Use {@link #getLayers()} instead (sorted on Layer priority).
     */
    @Deprecated
    public List<Layer> getOrderedLayersForEvent()
    {
        return getLayers();
    }

    /**
     * @deprecated Use {@link #getVisibleLayers()} instead (sorted on Layer priority).
     */
    @Deprecated
    public List<Layer> getVisibleOrderedLayersForEvent()
    {
        return getVisibleLayers();
    }

    /**
     * @deprecated Use {@link #getOverlays()} instead.
     */
    @Deprecated
    public List<Painter> getLayersPainter()
    {
        final ArrayList<Painter> result = new ArrayList<Painter>();

        for (Overlay overlay : getOverlays())
        {
            if (overlay instanceof OverlayWrapper)
                result.add(((OverlayWrapper) overlay).getPainter());
            else
                result.add(overlay);
        }

        return result;
    }

    /**
     * Directly returns a {@link Set} of all Overlay displayed by this canvas.
     */
    public Set<Overlay> getOverlays()
    {
        synchronized (layers)
        {
            return new HashSet<Overlay>(layers.keySet());
        }
    }

    /**
     * @return the SyncId
     */
    public int getSyncId()
    {
        return syncId;
    }

    /**
     * Set the synchronization group id (0 means unsynchronized).<br>
     * 
     * @return <code>false</code> if the canvas do not support synchronization group.
     * @param id
     *        the syncId to set
     */
    public boolean setSyncId(int id)
    {
        if (!isSynchronizationSupported())
            return false;

        if (this.syncId != id)
        {
            this.syncId = id;

            // notify sync has changed
            updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.SYNC_CHANGED));
        }

        return true;
    }

    /**
     * Return true if this canvas support synchronization
     */
    public boolean isSynchronizationSupported()
    {
        // default (override it when supported)
        return false;
    }

    /**
     * Return true if this canvas is synchronized
     */
    public boolean isSynchronized()
    {
        return syncId > 0;
    }

    /**
     * Return true if current canvas is synchronized and is currently the synchronize leader.
     */
    public boolean isSynchHeader()
    {
        return synchHeader;
    }

    /**
     * Return true if current canvas is synchronized and it's not the synchronize header
     */
    public boolean isSynchSlave()
    {
        if (isSynchronized())
        {
            if (isSynchHeader())
                return false;

            // search for a header in synchronized canvas
            for (IcyCanvas cnv : getSynchronizedCanvas())
                if (cnv.isSynchHeader())
                    return true;
        }

        return false;
    }

    /**
     * Return true if this canvas is synchronized on view (offset, zoom and rotation).
     */
    public boolean isSynchOnView()
    {
        return (syncId == 1) || (syncId == 2) || (syncId == 3);
    }

    /**
     * Return true if this canvas is synchronized on slice (T and Z position)
     */
    public boolean isSynchOnSlice()
    {
        return (syncId == 1) || (syncId == 2) || (syncId == 4);
    }

    /**
     * Return true if this canvas is synchronized on cursor (mouse cursor)
     */
    public boolean isSynchOnCursor()
    {
        return (syncId > 0);
    }

    /**
     * Return true if we get the synchronizer header from synchronized canvas
     */
    protected boolean getSynchHeader()
    {
        return getSynchHeader(getSynchronizedCanvas());
    }

    /**
     * Return true if we get the synchronizer header from specified canvas list.
     */
    protected boolean getSynchHeader(List<IcyCanvas> canvasList)
    {
        for (IcyCanvas canvas : canvasList)
            if (canvas.isSynchHeader())
                return canvas == this;

        // no header found so we are header
        synchHeader = true;

        return true;
    }

    /**
     * Release synchronizer header
     */
    protected void releaseSynchHeader()
    {
        synchHeader = false;
    }

    /**
     * Return the list of canvas which are synchronized with the current one
     */
    private List<IcyCanvas> getSynchronizedCanvas()
    {
        final ArrayList<IcyCanvas> result = new ArrayList<IcyCanvas>();

        if (isSynchronized())
        {
            final ArrayList<Viewer> viewers = Icy.getMainInterface().getViewers();

            for (int i = viewers.size() - 1; i >= 0; i--)
            {
                final IcyCanvas cnv = viewers.get(i).getCanvas();

                if ((cnv == this) || (cnv.getSyncId() != syncId))
                    viewers.remove(i);
            }

            for (Viewer v : viewers)
            {
                final IcyCanvas cnv = v.getCanvas();

                // only permit same class
                if (cnv.getClass().isInstance(this))
                    result.add(cnv);
            }
        }

        return result;
    }

    /**
     * Synchronize views of specified list of canvas
     */
    protected void synchronizeCanvas(List<IcyCanvas> canvasList, IcyCanvasEvent event, boolean processAll)
    {
        final IcyCanvasEventType type = event.getType();
        final DimensionId dim = event.getDim();

        // position synchronization
        if (isSynchOnSlice())
        {
            if (processAll || (type == IcyCanvasEventType.POSITION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final int x = getPositionX();
                    final int y = getPositionY();
                    final int z = getPositionZ();
                    final int t = getPositionT();
                    final int c = getPositionC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        if (x != -1)
                            cnv.setPositionX(x);
                        if (y != -1)
                            cnv.setPositionY(y);
                        if (z != -1)
                            cnv.setPositionZ(z);
                        if (t != -1)
                            cnv.setPositionT(t);
                        if (c != -1)
                            cnv.setPositionC(c);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                    {
                        final int pos = getPosition(dim);
                        if (pos != -1)
                            cnv.setPosition(dim, pos);
                    }
                }
            }
        }

        // view synchronization
        if (isSynchOnView())
        {
            if (processAll || (type == IcyCanvasEventType.SCALE_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double sX = getScaleX();
                    final double sY = getScaleY();
                    final double sZ = getScaleZ();
                    final double sT = getScaleT();
                    final double sC = getScaleC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        cnv.setScaleX(sX);
                        cnv.setScaleY(sY);
                        cnv.setScaleZ(sZ);
                        cnv.setScaleT(sT);
                        cnv.setScaleC(sC);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setScale(dim, getScale(dim));
                }
            }

            if (processAll || (type == IcyCanvasEventType.ROTATION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double rotX = getRotationX();
                    final double rotY = getRotationY();
                    final double rotZ = getRotationZ();
                    final double rotT = getRotationT();
                    final double rotC = getRotationC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        cnv.setRotationX(rotX);
                        cnv.setRotationY(rotY);
                        cnv.setRotationZ(rotZ);
                        cnv.setRotationT(rotT);
                        cnv.setRotationC(rotC);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setRotation(dim, getRotation(dim));
                }
            }

            // process offset in last as it can be limited depending destination scale value
            if (processAll || (type == IcyCanvasEventType.OFFSET_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final int offX = getOffsetX();
                    final int offY = getOffsetY();
                    final int offZ = getOffsetZ();
                    final int offT = getOffsetT();
                    final int offC = getOffsetC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        cnv.setOffsetX(offX);
                        cnv.setOffsetY(offY);
                        cnv.setOffsetZ(offZ);
                        cnv.setOffsetT(offT);
                        cnv.setOffsetC(offC);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setOffset(dim, getOffset(dim));
                }
            }
        }

        // cursor synchronization
        if (isSynchOnCursor())
        {
            // mouse synchronization
            if (processAll || (type == IcyCanvasEventType.MOUSE_IMAGE_POSITION_CHANGED))
            {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL))
                {
                    final double mipX = getMouseImagePosX();
                    final double mipY = getMouseImagePosY();
                    final double mipZ = getMouseImagePosZ();
                    final double mipT = getMouseImagePosT();
                    final double mipC = getMouseImagePosC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        cnv.setMouseImagePosX(mipX);
                        cnv.setMouseImagePosY(mipY);
                        cnv.setMouseImagePosZ(mipZ);
                        cnv.setMouseImagePosT(mipT);
                        cnv.setMouseImagePosC(mipC);
                    }
                }
                else
                {
                    for (IcyCanvas cnv : canvasList)
                        cnv.setMouseImagePos(dim, getMouseImagePos(dim));
                }
            }
        }
    }

    /**
     * Get position for specified dimension
     */
    public int getPosition(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getPositionX();
            case Y:
                return getPositionY();
            case Z:
                return getPositionZ();
            case T:
                return getPositionT();
            case C:
                return getPositionC();
        }

        return 0;
    }

    /**
     * @return current X (-1 if all selected)
     */
    public int getPositionX()
    {
        return -1;
    }

    /**
     * @return current Y (-1 if all selected)
     */
    public int getPositionY()
    {
        return -1;
    }

    /**
     * @return current Z (-1 if all selected)
     */
    public int getPositionZ()
    {
        return posZ;
    }

    /**
     * @return current T (-1 if all selected)
     */
    public int getPositionT()
    {
        return posT;
    }

    /**
     * @return current C (-1 if all selected)
     */
    public int getPositionC()
    {
        return posC;
    }

    /**
     * Returns the 5D canvas position (-1 mean that the complete dimension is selected)
     */
    public Point5D.Integer getPosition5D()
    {
        return new Point5D.Integer(getPositionX(), getPositionY(), getPositionZ(), getPositionT(), getPositionC());
    }

    /**
     * @return current Z (-1 if all selected)
     * @deprecated uses getPositionZ() instead
     */
    @Deprecated
    public int getZ()
    {
        return getPositionZ();
    }

    /**
     * @return current T (-1 if all selected)
     * @deprecated uses getPositionT() instead
     */
    @Deprecated
    public int getT()
    {
        return getPositionT();
    }

    /**
     * @return current C (-1 if all selected)
     * @deprecated uses getPositionC() instead
     */
    @Deprecated
    public int getC()
    {
        return getPositionC();
    }

    /**
     * Get maximum position for specified dimension
     */
    public double getMaxPosition(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getMaxPositionX();
            case Y:
                return getMaxPositionY();
            case Z:
                return getMaxPositionZ();
            case T:
                return getMaxPositionT();
            case C:
                return getMaxPositionC();
        }

        return 0;
    }

    /**
     * Get maximum X value
     */
    public int getMaxPositionX()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeX() - 1);
    }

    /**
     * Get maximum Y value
     */
    public int getMaxPositionY()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeY() - 1);
    }

    /**
     * Get maximum Z value
     */
    public int getMaxPositionZ()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeZ() - 1);
    }

    /**
     * Get maximum T value
     */
    public int getMaxPositionT()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeT() - 1);
    }

    /**
     * Get maximum C value
     */
    public int getMaxPositionC()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeC() - 1);
    }

    /**
     * Get the maximum 5D position for the canvas.
     * 
     * @see #getPosition5D()
     */
    public Point5D.Integer getMaxPosition5D()
    {
        return new Point5D.Integer(getMaxPositionX(), getMaxPositionY(), getMaxPositionZ(), getMaxPositionT(),
                getMaxPositionC());
    }

    /**
     * @deprecated Use {@link #getMaxPosition(DimensionId)} instead
     */
    @Deprecated
    public double getMax(DimensionId dim)
    {
        return getMaxPosition(dim);
    }

    /**
     * @deprecated Use {@link #getMaxPositionX()} instead
     */
    @Deprecated
    public int getMaxX()
    {
        return getMaxPositionX();
    }

    /**
     * @deprecated Use {@link #getMaxPositionY()} instead
     */
    @Deprecated
    public int getMaxY()
    {
        return getMaxPositionY();
    }

    /**
     * @deprecated Use {@link #getMaxPositionZ()} instead
     */
    @Deprecated
    public int getMaxZ()
    {
        return getMaxPositionZ();
    }

    /**
     * @deprecated Use {@link #getMaxPositionT()} instead
     */
    @Deprecated
    public int getMaxT()
    {
        return getMaxPositionT();
    }

    /**
     * @deprecated Use {@link #getMaxPositionC()} instead
     */
    @Deprecated
    public int getMaxC()
    {
        return getMaxPositionC();
    }

    /**
     * Get canvas view size for specified Dimension
     */
    public int getCanvasSize(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getCanvasSizeX();
            case Y:
                return getCanvasSizeY();
            case Z:
                return getCanvasSizeZ();
            case T:
                return getCanvasSizeT();
            case C:
                return getCanvasSizeC();
        }

        // size not supported
        return -1;
    }

    /**
     * Returns the canvas view size X.
     */
    public int getCanvasSizeX()
    {
        final Component comp = getViewComponent();
        int res = 0;

        if (comp != null)
        {
            // by default we use view component width
            res = comp.getWidth();
            // preferred width if size not yet set
            if (res == 0)
                res = comp.getPreferredSize().width;
        }

        return res;
    }

    /**
     * Returns the canvas view size Y.
     */
    public int getCanvasSizeY()
    {
        final Component comp = getViewComponent();
        int res = 0;

        if (comp != null)
        {
            // by default we use view component width
            res = comp.getHeight();
            // preferred width if size not yet set
            if (res == 0)
                res = comp.getPreferredSize().height;
        }

        return res;
    }

    /**
     * Returns the canvas view size Z.
     */
    public int getCanvasSizeZ()
    {
        // by default : no Z dimension
        return 1;
    }

    /**
     * Returns the canvas view size T.
     */
    public int getCanvasSizeT()
    {
        // by default : no T dimension
        return 1;
    }

    /**
     * Returns the canvas view size C.
     */
    public int getCanvasSizeC()
    {
        // by default : no C dimension
        return 1;
    }

    /**
     * Returns the mouse position (in canvas coordinate space).
     */
    public Point getMousePos()
    {
        return (Point) mousePos.clone();
    }

    /**
     * Get mouse image position for specified Dimension
     */
    public double getMouseImagePos(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getMouseImagePosX();
            case Y:
                return getMouseImagePosY();
            case Z:
                return getMouseImagePosZ();
            case T:
                return getMouseImagePosT();
            case C:
                return getMouseImagePosC();
        }

        return 0;
    }

    /**
     * mouse X image position
     */
    public double getMouseImagePosX()
    {
        // default implementation
        return getPositionX();
    }

    /**
     * mouse Y image position
     */
    public double getMouseImagePosY()
    {
        // default implementation
        return getPositionY();
    }

    /**
     * mouse Z image position
     */
    public double getMouseImagePosZ()
    {
        // default implementation
        return getPositionZ();
    }

    /**
     * mouse T image position
     */
    public double getMouseImagePosT()
    {
        // default implementation
        return getPositionT();
    }

    /**
     * mouse C image position
     */
    public double getMouseImagePosC()
    {
        // default implementation
        return getPositionC();
    }

    /**
     * Returns the 5D mouse image position
     */
    public Point5D.Double getMouseImagePos5D()
    {
        return new Point5D.Double(getMouseImagePosX(), getMouseImagePosY(), getMouseImagePosZ(), getMouseImagePosT(),
                getMouseImagePosC());
    }

    /**
     * Get offset for specified Dimension
     */
    public int getOffset(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getOffsetX();
            case Y:
                return getOffsetY();
            case Z:
                return getOffsetZ();
            case T:
                return getOffsetT();
            case C:
                return getOffsetC();
        }

        return 0;
    }

    /**
     * X offset
     */
    public int getOffsetX()
    {
        return 0;
    }

    /**
     * Y offset
     */
    public int getOffsetY()
    {
        return 0;
    }

    /**
     * Z offset
     */
    public int getOffsetZ()
    {
        return 0;
    }

    /**
     * T offset
     */
    public int getOffsetT()
    {
        return 0;
    }

    /**
     * C offset
     */
    public int getOffsetC()
    {
        return 0;
    }

    /**
     * Returns the 5D offset.
     */
    public Point5D.Integer getOffset5D()
    {
        return new Point5D.Integer(getOffsetX(), getOffsetY(), getOffsetZ(), getOffsetT(), getOffsetC());
    }

    /**
     * X image offset
     * 
     * @deprecated use getOffsetX() instead
     */
    @Deprecated
    public int getImageOffsetX()
    {
        return 0;
    }

    /**
     * Y image offset
     * 
     * @deprecated use getOffsetY() instead
     */
    @Deprecated
    public int getImageOffsetY()
    {
        return 0;
    }

    /**
     * Z image offset
     * 
     * @deprecated use getOffsetZ() instead
     */
    @Deprecated
    public int getImageOffsetZ()
    {
        return 0;
    }

    /**
     * T image offset
     * 
     * @deprecated use getOffsetT() instead
     */
    @Deprecated
    public int getImageOffsetT()
    {
        return 0;
    }

    /**
     * C image offset
     * 
     * @deprecated use getOffsetC() instead
     */
    @Deprecated
    public int getImageOffsetC()
    {
        return 0;
    }

    /**
     * X canvas offset
     * 
     * @deprecated use getOffsetX() instead
     */
    @Deprecated
    public int getCanvasOffsetX()
    {
        return 0;
    }

    /**
     * Y canvas offset
     * 
     * @deprecated use getOffsetY() instead
     */
    @Deprecated
    public int getCanvasOffsetY()
    {
        return 0;
    }

    /**
     * Z canvas offset
     * 
     * @deprecated use getOffsetZ() instead
     */
    @Deprecated
    public int getCanvasOffsetZ()
    {
        return 0;
    }

    /**
     * T canvas offset
     * 
     * @deprecated use getOffsetT() instead
     */
    @Deprecated
    public int getCanvasOffsetT()
    {
        return 0;
    }

    /**
     * C canvas offset
     * 
     * @deprecated use getOffsetC() instead
     */
    @Deprecated
    public int getCanvasOffsetC()
    {
        return 0;
    }

    /**
     * X scale factor
     * 
     * @deprecated use getScaleX() instead
     */
    @Deprecated
    public double getScaleFactorX()
    {
        return getScaleX();
    }

    /**
     * Y scale factor
     * 
     * @deprecated use getScaleY() instead
     */
    @Deprecated
    public double getScaleFactorY()
    {
        return getScaleY();
    }

    /**
     * Z scale factor
     * 
     * @deprecated use getScaleZ() instead
     */
    @Deprecated
    public double getScaleFactorZ()
    {
        return getScaleZ();
    }

    /**
     * T scale factor
     * 
     * @deprecated use getScaleT() instead
     */
    @Deprecated
    public double getScaleFactorT()
    {
        return getScaleT();
    }

    /**
     * C scale factor
     * 
     * @deprecated use getScaleC() instead
     */
    @Deprecated
    public double getScaleFactorC()
    {
        return getScaleC();
    }

    /**
     * Get scale factor for specified Dimension
     */
    public double getScale(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getScaleX();
            case Y:
                return getScaleY();
            case Z:
                return getScaleZ();
            case T:
                return getScaleT();
            case C:
                return getScaleC();
        }

        return 1d;
    }

    /**
     * X scale factor
     */
    public double getScaleX()
    {
        return 1d;
    }

    /**
     * Y scale factor
     */
    public double getScaleY()
    {
        return 1d;
    }

    /**
     * Z scale factor
     */
    public double getScaleZ()
    {
        return 1d;
    }

    /**
     * T scale factor
     */
    public double getScaleT()
    {
        return 1d;
    }

    /**
     * C scale factor
     */
    public double getScaleC()
    {
        return 1d;
    }

    /**
     * Get rotation angle (radian) for specified Dimension
     */
    public double getRotation(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getRotationX();
            case Y:
                return getRotationY();
            case Z:
                return getRotationZ();
            case T:
                return getRotationT();
            case C:
                return getRotationC();
        }

        return 1d;
    }

    /**
     * X rotation angle (radian)
     */
    public double getRotationX()
    {
        return 0d;
    }

    /**
     * Y rotation angle (radian)
     */
    public double getRotationY()
    {
        return 0d;
    }

    /**
     * Z rotation angle (radian)
     */
    public double getRotationZ()
    {
        return 0d;
    }

    /**
     * T rotation angle (radian)
     */
    public double getRotationT()
    {
        return 0d;
    }

    /**
     * C rotation angle (radian)
     */
    public double getRotationC()
    {
        return 0d;
    }

    /**
     * Get image size for specified Dimension
     */
    public int getImageSize(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getImageSizeX();
            case Y:
                return getImageSizeY();
            case Z:
                return getImageSizeZ();
            case T:
                return getImageSizeT();
            case C:
                return getImageSizeC();
        }

        return 0;
    }

    /**
     * Get image size X
     */
    public int getImageSizeX()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeX();

        return 0;
    }

    /**
     * Get image size Y
     */
    public int getImageSizeY()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeY();

        return 0;
    }

    /**
     * Get image size Z
     */
    public int getImageSizeZ()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeZ();

        return 0;
    }

    /**
     * Get image size T
     */
    public int getImageSizeT()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeT();

        return 0;
    }

    /**
     * Get image size C
     */
    public int getImageSizeC()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeC();

        return 0;
    }

    /**
     * Get image size in canvas pixel coordinate for specified Dimension
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSize(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getImageCanvasSizeX();
            case Y:
                return getImageCanvasSizeY();
            case Z:
                return getImageCanvasSizeZ();
            case T:
                return getImageCanvasSizeT();
            case C:
                return getImageCanvasSizeC();
        }

        return 0;
    }

    /**
     * Get image size X in canvas pixel coordinate
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSizeX()
    {
        return imageToCanvasDeltaX(getImageSizeX());
    }

    /**
     * Get image size Y in canvas pixel coordinate
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSizeY()
    {
        return imageToCanvasDeltaY(getImageSizeY());
    }

    /**
     * Get image size Z in canvas pixel coordinate
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSizeZ()
    {
        return imageToCanvasDeltaZ(getImageSizeZ());
    }

    /**
     * Get image size T in canvas pixel coordinate
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSizeT()
    {
        return imageToCanvasDeltaT(getImageSizeT());
    }

    /**
     * Get image size C in canvas pixel coordinate
     * 
     * @deprecated doesn't take rotation transformation in account.<br>
     *             Use IcyCanvasXD.getImageCanvasSize(..) instead
     */
    @Deprecated
    public int getImageCanvasSizeC()
    {
        return imageToCanvasDeltaC(getImageSizeC());
    }

    /**
     * Set position for specified dimension
     */
    public void setPosition(DimensionId dim, int value)
    {
        switch (dim)
        {
            case X:
                setPositionX(value);
                break;
            case Y:
                setPositionY(value);
                break;
            case Z:
                setPositionZ(value);
                break;
            case T:
                setPositionT(value);
                break;
            case C:
                setPositionC(value);
                break;
        }
    }

    /**
     * Set Z position
     * 
     * @deprecated uses setPositionZ(int) instead
     */
    @Deprecated
    public void setZ(int z)
    {
        setPositionZ(z);
    }

    /**
     * Set T position
     * 
     * @deprecated uses setPositionT(int) instead
     */
    @Deprecated
    public void setT(int t)
    {
        setPositionT(t);
    }

    /**
     * Set C position
     * 
     * @deprecated uses setPositionC(int) instead
     */
    @Deprecated
    public void setC(int c)
    {
        setPositionC(c);
    }

    /**
     * Set X position
     */
    public void setPositionX(int x)
    {
        final int adjX = Math.max(-1, Math.min(x, getMaxPositionX()));

        if (getPositionX() != adjX)
            setPositionXInternal(adjX);
    }

    /**
     * Set Y position
     */
    public void setPositionY(int y)
    {
        final int adjY = Math.max(-1, Math.min(y, getMaxPositionY()));

        if (getPositionY() != adjY)
            setPositionYInternal(adjY);
    }

    /**
     * Set Z position
     */
    public void setPositionZ(int z)
    {
        final int adjZ = Math.max(-1, Math.min(z, getMaxPositionZ()));

        if (getPositionZ() != adjZ)
            setPositionZInternal(adjZ);
    }

    /**
     * Set T position
     */
    public void setPositionT(int t)
    {
        final int adjT = Math.max(-1, Math.min(t, getMaxPositionT()));

        if (getPositionT() != adjT)
            setPositionTInternal(adjT);
    }

    /**
     * Set C position
     */
    public void setPositionC(int c)
    {
        final int adjC = Math.max(-1, Math.min(c, getMaxPositionC()));

        if (getPositionC() != adjC)
            setPositionCInternal(adjC);
    }

    /**
     * Set X position internal
     */
    protected void setPositionXInternal(int x)
    {
        posX = x;
        // common process on position change
        positionChanged(DimensionId.X);
    }

    /**
     * Set Y position internal
     */
    protected void setPositionYInternal(int y)
    {
        posY = y;
        // common process on position change
        positionChanged(DimensionId.Y);
    }

    /**
     * Set Z position internal
     */
    protected void setPositionZInternal(int z)
    {
        posZ = z;
        // common process on position change
        positionChanged(DimensionId.Z);
    }

    /**
     * Set T position internal
     */
    protected void setPositionTInternal(int t)
    {
        posT = t;
        // common process on position change
        positionChanged(DimensionId.T);
    }

    /**
     * Set C position internal
     */
    protected void setPositionCInternal(int c)
    {
        posC = c;
        // common process on position change
        positionChanged(DimensionId.C);
    }

    /**
     * Set mouse position (in canvas coordinate space).<br>
     * The method returns <code>true</code> if the mouse position actually changed.
     */
    public boolean setMousePos(int x, int y)
    {
        if ((mousePos.x != x) || (mousePos.y != y))
        {
            mousePos.x = x;
            mousePos.y = y;

            // mouse image position as probably changed so this method should be overridden
            // to implement the correct calculation for the mouse iamge position change

            return true;
        }

        return false;
    }

    /**
     * Set mouse position (in canvas coordinate space)
     */
    public void setMousePos(Point point)
    {
        setMousePos(point.x, point.y);
    }

    /**
     * Set mouse image position for specified dimension (required for synchronization)
     */
    public void setMouseImagePos(DimensionId dim, double value)
    {
        switch (dim)
        {
            case X:
                setMouseImagePosX(value);
                break;
            case Y:
                setMouseImagePosY(value);
                break;
            case Z:
                setMouseImagePosZ(value);
                break;
            case T:
                setMouseImagePosT(value);
                break;
            case C:
                setMouseImagePosC(value);
                break;
        }
    }

    /**
     * Set mouse X image position
     */
    public void setMouseImagePosX(double value)
    {
        if (getMouseImagePosX() != value)
            // internal set
            setMouseImagePosXInternal(value);
    }

    /**
     * Set mouse Y image position
     */
    public void setMouseImagePosY(double value)
    {
        if (getMouseImagePosY() != value)
            // internal set
            setMouseImagePosYInternal(value);
    }

    /**
     * Set mouse Z image position
     */
    public void setMouseImagePosZ(double value)
    {
        if (getMouseImagePosZ() != value)
            // internal set
            setMouseImagePosZInternal(value);
    }

    /**
     * Set mouse T image position
     */
    public void setMouseImagePosT(double value)
    {
        if (getMouseImagePosT() != value)
            // internal set
            setMouseImagePosTInternal(value);
    }

    /**
     * Set mouse C image position
     */
    public void setMouseImagePosC(double value)
    {
        if (getMouseImagePosC() != value)
            // internal set
            setMouseImagePosCInternal(value);
    }

    /**
     * Set offset X internal
     */
    protected void setMouseImagePosXInternal(double value)
    {
        // notify change
        mouseImagePositionChanged(DimensionId.X);
    }

    /**
     * Set offset Y internal
     */
    protected void setMouseImagePosYInternal(double value)
    {
        // notify change
        mouseImagePositionChanged(DimensionId.Y);
    }

    /**
     * Set offset Z internal
     */
    protected void setMouseImagePosZInternal(double value)
    {
        // notify change
        mouseImagePositionChanged(DimensionId.Z);
    }

    /**
     * Set offset T internal
     */
    protected void setMouseImagePosTInternal(double value)
    {
        // notify change
        mouseImagePositionChanged(DimensionId.T);
    }

    /**
     * Set offset C internal
     */
    protected void setMouseImagePosCInternal(double value)
    {
        // notify change
        mouseImagePositionChanged(DimensionId.C);
    }

    /**
     * Set offset for specified dimension
     */
    public void setOffset(DimensionId dim, int value)
    {
        switch (dim)
        {
            case X:
                setOffsetX(value);
                break;
            case Y:
                setOffsetY(value);
                break;
            case Z:
                setOffsetZ(value);
                break;
            case T:
                setOffsetT(value);
                break;
            case C:
                setOffsetC(value);
                break;
        }
    }

    /**
     * Set offset X
     */
    public void setOffsetX(int value)
    {
        if (getOffsetX() != value)
            // internal set
            setOffsetXInternal(value);
    }

    /**
     * Set offset Y
     */
    public void setOffsetY(int value)
    {
        if (getOffsetY() != value)
            // internal set
            setOffsetYInternal(value);
    }

    /**
     * Set offset Z
     */
    public void setOffsetZ(int value)
    {
        if (getOffsetZ() != value)
            // internal set
            setOffsetZInternal(value);
    }

    /**
     * Set offset T
     */
    public void setOffsetT(int value)
    {
        if (getOffsetT() != value)
            // internal set
            setOffsetTInternal(value);
    }

    /**
     * Set offset C
     */
    public void setOffsetC(int value)
    {
        if (getOffsetC() != value)
            // internal set
            setOffsetCInternal(value);
    }

    /**
     * Set offset X internal
     */
    protected void setOffsetXInternal(int value)
    {
        // notify change
        offsetChanged(DimensionId.X);
    }

    /**
     * Set offset Y internal
     */
    protected void setOffsetYInternal(int value)
    {
        // notify change
        offsetChanged(DimensionId.Y);
    }

    /**
     * Set offset Z internal
     */
    protected void setOffsetZInternal(int value)
    {
        // notify change
        offsetChanged(DimensionId.Z);
    }

    /**
     * Set offset T internal
     */
    protected void setOffsetTInternal(int value)
    {
        // notify change
        offsetChanged(DimensionId.T);
    }

    /**
     * Set offset C internal
     */
    protected void setOffsetCInternal(int value)
    {
        // notify change
        offsetChanged(DimensionId.C);
    }

    /**
     * Set scale factor for specified dimension
     */
    public void setScale(DimensionId dim, double value)
    {
        switch (dim)
        {
            case X:
                setScaleX(value);
                break;
            case Y:
                setScaleY(value);
                break;
            case Z:
                setScaleZ(value);
                break;
            case T:
                setScaleT(value);
                break;
            case C:
                setScaleC(value);
                break;
        }
    }

    /**
     * Set scale factor X
     */
    public void setScaleX(double value)
    {
        if (getScaleX() != value)
            // internal set
            setScaleXInternal(value);
    }

    /**
     * Set scale factor Y
     */
    public void setScaleY(double value)
    {
        if (getScaleY() != value)
            // internal set
            setScaleYInternal(value);
    }

    /**
     * Set scale factor Z
     */
    public void setScaleZ(double value)
    {
        if (getScaleZ() != value)
            // internal set
            setScaleZInternal(value);
    }

    /**
     * Set scale factor T
     */
    public void setScaleT(double value)
    {
        if (getScaleT() != value)
            // internal set
            setScaleTInternal(value);
    }

    /**
     * Set scale factor C
     */
    public void setScaleC(double value)
    {
        if (getScaleC() != value)
            // internal set
            setScaleCInternal(value);
    }

    /**
     * Set scale factor X internal
     */
    protected void setScaleXInternal(double value)
    {
        // notify change
        scaleChanged(DimensionId.X);
    }

    /**
     * Set scale factor Y internal
     */
    protected void setScaleYInternal(double value)
    {
        // notify change
        scaleChanged(DimensionId.Y);
    }

    /**
     * Set scale factor Z internal
     */
    protected void setScaleZInternal(double value)
    {
        // notify change
        scaleChanged(DimensionId.Z);
    }

    /**
     * Set scale factor T internal
     */
    protected void setScaleTInternal(double value)
    {
        // notify change
        scaleChanged(DimensionId.T);
    }

    /**
     * Set scale factor C internal
     */
    protected void setScaleCInternal(double value)
    {
        // notify change
        scaleChanged(DimensionId.C);
    }

    /**
     * Set rotation angle (radian) for specified dimension
     */
    public void setRotation(DimensionId dim, double value)
    {
        switch (dim)
        {
            case X:
                setRotationX(value);
                break;
            case Y:
                setRotationY(value);
                break;
            case Z:
                setRotationZ(value);
                break;
            case T:
                setRotationT(value);
                break;
            case C:
                setRotationC(value);
                break;
        }
    }

    /**
     * Set X rotation angle (radian)
     */
    public void setRotationX(double value)
    {
        if (getRotationX() != value)
            // internal set
            setRotationXInternal(value);
    }

    /**
     * Set Y rotation angle (radian)
     */
    public void setRotationY(double value)
    {
        if (getRotationY() != value)
            // internal set
            setRotationYInternal(value);
    }

    /**
     * Set Z rotation angle (radian)
     */
    public void setRotationZ(double value)
    {
        if (getRotationZ() != value)
            // internal set
            setRotationZInternal(value);
    }

    /**
     * Set T rotation angle (radian)
     */
    public void setRotationT(double value)
    {
        if (getRotationT() != value)
            // internal set
            setRotationTInternal(value);
    }

    /**
     * Set C rotation angle (radian)
     */
    public void setRotationC(double value)
    {
        if (getRotationC() != value)
            // internal set
            setRotationCInternal(value);
    }

    /**
     * Set X rotation angle internal
     */
    protected void setRotationXInternal(double value)
    {
        // notify change
        rotationChanged(DimensionId.X);
    }

    /**
     * Set Y rotation angle internal
     */
    protected void setRotationYInternal(double value)
    {
        // notify change
        rotationChanged(DimensionId.Y);
    }

    /**
     * Set Z rotation angle internal
     */
    protected void setRotationZInternal(double value)
    {
        // notify change
        rotationChanged(DimensionId.Z);
    }

    /**
     * Set T rotation angle internal
     */
    protected void setRotationTInternal(double value)
    {
        // notify change
        rotationChanged(DimensionId.T);
    }

    /**
     * Set C rotation angle internal
     */
    protected void setRotationCInternal(double value)
    {
        // notify change
        rotationChanged(DimensionId.C);
    }

    /**
     * Called when mouse image position changed
     */
    public void mouseImagePositionChanged(DimensionId dim)
    {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.MOUSE_IMAGE_POSITION_CHANGED, dim));
    }

    /**
     * Called when canvas offset changed
     */
    public void offsetChanged(DimensionId dim)
    {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.OFFSET_CHANGED, dim));
    }

    /**
     * Called when scale factor changed
     */
    public void scaleChanged(DimensionId dim)
    {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.SCALE_CHANGED, dim));
    }

    /**
     * Called when rotation angle changed
     */
    public void rotationChanged(DimensionId dim)
    {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.ROTATION_CHANGED, dim));
    }

    /**
     * Convert specified canvas delta X to image delta X.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaX(int value)
    {
        return value / getScaleX();
    }

    /**
     * Convert specified canvas delta Y to image delta Y.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaY(int value)
    {
        return value / getScaleY();
    }

    /**
     * Convert specified canvas delta Z to image delta Z.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaZ(int value)
    {
        return value / getScaleZ();
    }

    /**
     * Convert specified canvas delta T to image delta T.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaT(int value)
    {
        return value / getScaleT();
    }

    /**
     * Convert specified canvas delta C to image delta C.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaC(int value)
    {
        return value / getScaleC();
    }

    /**
     * Convert specified canvas delta X to log image delta X.<br>
     * The conversion is still affected by zoom ratio but with specified logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaX(int value, double logFactor)
    {
        final double scaleFactor = getScaleX();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert specified canvas delta X to log image delta X.<br>
     * The conversion is still affected by zoom ratio but with logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaX(int value)
    {
        return canvasToImageLogDeltaX(value, 5d);
    }

    /**
     * Convert specified canvas delta Y to log image delta Y.<br>
     * The conversion is still affected by zoom ratio but with specified logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaY(int value, double logFactor)
    {
        final double scaleFactor = getScaleY();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert specified canvas delta Y to log image delta Y.<br>
     * The conversion is still affected by zoom ratio but with logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaY(int value)
    {
        return canvasToImageLogDeltaY(value, 5d);
    }

    /**
     * Convert specified canvas X coordinate to image X coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.canvasToImage(...) instead
     */
    @Deprecated
    public double canvasToImageX(int value)
    {
        return canvasToImageDeltaX(value - getOffsetX());
    }

    /**
     * Convert specified canvas Y coordinate to image Y coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.canvasToImage(...) instead
     */
    @Deprecated
    public double canvasToImageY(int value)
    {
        return canvasToImageDeltaY(value - getOffsetY());
    }

    /**
     * Convert specified canvas Z coordinate to image Z coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.canvasToImage(...) instead
     */
    @Deprecated
    public double canvasToImageZ(int value)
    {
        return canvasToImageDeltaZ(value - getOffsetZ());
    }

    /**
     * Convert specified canvas T coordinate to image T coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.canvasToImage(...) instead
     */
    @Deprecated
    public double canvasToImageT(int value)
    {
        return canvasToImageDeltaT(value - getOffsetT());
    }

    /**
     * Convert specified canvas C coordinate to image C coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.canvasToImage(...) instead
     */
    @Deprecated
    public double canvasToImageC(int value)
    {
        return canvasToImageDeltaC(value - getOffsetC());
    }

    /**
     * Convert specified image delta X to canvas delta X.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaX(double value)
    {
        return (int) (value * getScaleX());
    }

    /**
     * Convert specified image delta Y to canvas delta Y.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaY(double value)
    {
        return (int) (value * getScaleY());
    }

    /**
     * Convert specified image delta Z to canvas delta Z.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaZ(double value)
    {
        return (int) (value * getScaleZ());
    }

    /**
     * Convert specified image delta T to canvas delta T.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaT(double value)
    {
        return (int) (value * getScaleT());
    }

    /**
     * Convert specified image delta C to canvas delta C.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaC(double value)
    {
        return (int) (value * getScaleC());
    }

    /**
     * Convert specified image X coordinate to canvas X coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.imageToCanvas(...) instead
     */
    @Deprecated
    public int imageToCanvasX(double value)
    {
        return imageToCanvasDeltaX(value) + getOffsetX();
    }

    /**
     * Convert specified image Y coordinate to canvas Y coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.imageToCanvas(...) instead
     */
    @Deprecated
    public int imageToCanvasY(double value)
    {
        return imageToCanvasDeltaY(value) + getOffsetY();
    }

    /**
     * Convert specified image Z coordinate to canvas Z coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.imageToCanvas(...) instead
     */
    @Deprecated
    public int imageToCanvasZ(double value)
    {
        return imageToCanvasDeltaZ(value) + getOffsetZ();
    }

    /**
     * Convert specified image T coordinate to canvas T coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.imageToCanvas(...) instead
     */
    @Deprecated
    public int imageToCanvasT(double value)
    {
        return imageToCanvasDeltaT(value) + getOffsetT();
    }

    /**
     * Convert specified image C coordinate to canvas C coordinate
     * 
     * @deprecated Cannot give correct result if rotation is applied so use
     *             IcyCanvasXD.imageToCanvas(...) instead
     */
    @Deprecated
    public int imageToCanvasC(double value)
    {
        return imageToCanvasDeltaC(value) + getOffsetC();
    }

    /**
     * Helper to forward mouse press event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mousePressed(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mousePressed(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse press event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mousePressed(MouseEvent event)
    {
        mousePressed(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse release event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseReleased(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseReleased(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse release event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseReleased(MouseEvent event)
    {
        mouseReleased(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse click event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseClick(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseClick(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse click event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseClick(MouseEvent event)
    {
        mouseClick(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse move event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseMove(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseMove(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse mouse event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseMove(MouseEvent event)
    {
        mouseMove(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse drag event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseDrag(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseDrag(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse drag event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseDrag(MouseEvent event)
    {
        mouseDrag(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse enter event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseEntered(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseEntered(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse entered event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseEntered(MouseEvent event)
    {
        mouseEntered(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse exit event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseExited(MouseEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseExited(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse exited event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseExited(MouseEvent event)
    {
        mouseExited(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse wheel event to the overlays.
     * 
     * @param event
     *        original mouse event
     * @param pt
     *        mouse image position
     */
    public void mouseWheelMoved(MouseWheelEvent event, Point5D.Double pt)
    {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseWheelMoved(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse wheel event to the overlays.
     * 
     * @param event
     *        original mouse event
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
        mouseWheelMoved(event, getMouseImagePos5D());
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        final boolean globalVisible = isLayersVisible();
        final Point5D.Double pt = getMouseImagePos5D();

        // forward event to overlays
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveKeyEventOnHidden())
                layer.getOverlay().keyPressed(e, pt, this);
        }

        if (!e.isConsumed())
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_0:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (CanvasActions.globalDisableSyncAction.isEnabled())
                        {
                            CanvasActions.globalDisableSyncAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e))
                    {
                        if (CanvasActions.disableSyncAction.isEnabled())
                        {
                            CanvasActions.disableSyncAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_1:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (CanvasActions.globalSyncGroup1Action.isEnabled())
                        {
                            CanvasActions.globalSyncGroup1Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e))
                    {
                        if (CanvasActions.syncGroup1Action.isEnabled())
                        {
                            CanvasActions.syncGroup1Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_2:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (CanvasActions.globalSyncGroup2Action.isEnabled())
                        {
                            CanvasActions.globalSyncGroup2Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e))
                    {
                        if (CanvasActions.syncGroup2Action.isEnabled())
                        {
                            CanvasActions.syncGroup2Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_3:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (CanvasActions.globalSyncGroup3Action.isEnabled())
                        {
                            CanvasActions.globalSyncGroup3Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e))
                    {
                        if (CanvasActions.syncGroup3Action.isEnabled())
                        {
                            CanvasActions.syncGroup3Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_4:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (CanvasActions.globalSyncGroup4Action.isEnabled())
                        {
                            CanvasActions.globalSyncGroup4Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e))
                    {
                        if (CanvasActions.syncGroup4Action.isEnabled())
                        {
                            CanvasActions.syncGroup4Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_G:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (WindowActions.gridTileAction.isEnabled())
                        {
                            WindowActions.gridTileAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_H:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (WindowActions.horizontalTileAction.isEnabled())
                        {
                            WindowActions.horizontalTileAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_A:
                    if (EventUtil.isMenuControlDown(e, true))
                    {
                        if (RoiActions.selectAllAction.isEnabled())
                        {
                            RoiActions.selectAllAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_V:
                    if (EventUtil.isShiftDown(e, true))
                    {
                        if (WindowActions.verticalTileAction.isEnabled())
                        {
                            WindowActions.verticalTileAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isMenuControlDown(e, true))
                    {
                        if (GeneralActions.pasteImageAction.isEnabled())
                        {
                            GeneralActions.pasteImageAction.execute();
                            e.consume();
                        }
                        else if (RoiActions.pasteAction.isEnabled())
                        {
                            RoiActions.pasteAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isAltDown(e, true))
                    {
                        if (RoiActions.pasteLinkAction.isEnabled())
                        {
                            RoiActions.pasteLinkAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_C:
                    if (EventUtil.isMenuControlDown(e, true))
                    {
                        // do this one first else copyImage hide it
                        if (RoiActions.copyAction.isEnabled())
                        {
                            // copy them to icy clipboard
                            RoiActions.copyAction.execute();
                            e.consume();
                        }
                        else if (GeneralActions.copyImageAction.isEnabled())
                        {
                            // copy image to system clipboard
                            GeneralActions.copyImageAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isAltDown(e, true))
                    {
                        if (RoiActions.copyLinkAction.isEnabled())
                        {
                            // copy link of selected ROI to clipboard
                            RoiActions.copyLinkAction.execute();
                            e.consume();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        final boolean globalVisible = isLayersVisible();
        final Point5D.Double pt = getMouseImagePos5D();

        // forward event to overlays
        for (Layer layer : getLayers(true))
        {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveKeyEventOnHidden())
                layer.getOverlay().keyReleased(e, pt, this);
        }
    }

    /**
     * Gets the image at position (t, z, c).
     */
    public IcyBufferedImage getImage(int t, int z, int c)
    {
        if ((t == -1) || (z == -1))
            return null;

        final Sequence sequence = getSequence();

        // have to test this as sequence reference can be release in viewer
        if (sequence != null)
            return sequence.getImage(t, z, c);

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
     * Get the current image.
     */
    public IcyBufferedImage getCurrentImage()
    {
        return getImage(getPositionT(), getPositionZ(), getPositionC());
    }

    /**
     * @deprecated use {@link #getRenderedImage(int, int, int, boolean)} instead
     */
    @Deprecated
    public final BufferedImage getRenderedImage(int t, int z, int c, int imageType, boolean canvasView)
    {
        return getRenderedImage(t, z, c, canvasView);
    }

    /**
     * @deprecated use {@link #getRenderedSequence(boolean)} instead
     */
    @Deprecated
    public final Sequence getRenderedSequence(int imageType, boolean canvasView)
    {
        return getRenderedSequence(canvasView);
    }

    /**
     * Returns a RGB or ARGB (depending support) BufferedImage representing the canvas view for
     * image at position (t, z, c).
     * Free feel to the canvas to handle or not a specific dimension.
     * 
     * @param t
     *        T position of wanted image (-1 for complete sequence)
     * @param z
     *        Z position of wanted image (-1 for complete stack)
     * @param c
     *        C position of wanted image (-1 for all channels)
     * @param canvasView
     *        render with canvas view if true else use default sequence dimension
     */
    public abstract BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView);

    /**
     * @deprecated Use {@link #getRenderedImage(int, int, int, boolean)} instead.
     */
    @Deprecated
    public BufferedImage getRenderedImage(int t, int z, int c)
    {
        return getRenderedImage(t, z, c, true);
    }

    /**
     * Return a sequence which contains rendered images.<br>
     * Default implementation, override it if needed in your canvas.
     * 
     * @param canvasView
     *        render with canvas view if true else use default sequence dimension
     * @param progressListener
     *        progress listener which receive notifications about progression
     */
    public Sequence getRenderedSequence(boolean canvasView, ProgressListener progressListener)
    {
        final Sequence seqIn = getSequence();
        // create output sequence
        final Sequence result = new Sequence();

        if (seqIn != null)
        {
            // derive original metadata
            result.setMetaData(OMEUtil.createOMEMetadata(seqIn.getMetadata()));

            int t = getPositionT();
            int z = getPositionZ();
            int c = getPositionC();
            final int sizeT = getImageSizeT();
            final int sizeZ = getImageSizeZ();
            final int sizeC = getImageSizeC();

            int pos = 0;
            int len = 1;
            if (t != -1)
                len *= sizeT;
            if (z != -1)
                len *= sizeZ;
            if (c != -1)
                len *= sizeC;

            result.beginUpdate();
            // This cause position changed event to not be sent during rendering.
            // Painters have to take care of that, they should check the canvas position
            // in the paint() method
            beginUpdate();
            try
            {
                if (t != -1)
                {
                    for (t = 0; t < sizeT; t++)
                    {
                        if (z != -1)
                        {
                            for (z = 0; z < sizeZ; z++)
                            {
                                if (c != -1)
                                {
                                    final List<BufferedImage> images = new ArrayList<BufferedImage>();

                                    for (c = 0; c < sizeC; c++)
                                    {
                                        images.add(getRenderedImage(t, z, c, canvasView));
                                        pos++;
                                        if (progressListener != null)
                                            progressListener.notifyProgress(pos, len);
                                    }

                                    result.setImage(t, z, IcyBufferedImage.createFrom(images));
                                }
                                else
                                {
                                    result.setImage(t, z, getRenderedImage(t, z, -1, canvasView));
                                    pos++;
                                    if (progressListener != null)
                                        progressListener.notifyProgress(pos, len);
                                }
                            }
                        }
                        else
                        {
                            result.setImage(t, 0, getRenderedImage(t, -1, -1, canvasView));
                            pos++;
                            if (progressListener != null)
                                progressListener.notifyProgress(pos, len);
                        }
                    }
                }
                else
                {
                    if (z != -1)
                    {
                        for (z = 0; z < sizeZ; z++)
                        {
                            if (c != -1)
                            {
                                final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

                                for (c = 0; c < sizeC; c++)
                                {
                                    images.add(getRenderedImage(-1, z, c, canvasView));
                                    pos++;
                                    if (progressListener != null)
                                        progressListener.notifyProgress(pos, len);
                                }

                                result.setImage(0, z, IcyBufferedImage.createFrom(images));
                            }
                            else
                            {
                                result.setImage(0, z, getRenderedImage(-1, z, -1, canvasView));
                                pos++;
                                if (progressListener != null)
                                    progressListener.notifyProgress(pos, len);
                            }
                        }
                    }
                    else
                    {
                        if (c != -1)
                        {
                            final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

                            for (c = 0; c < sizeC; c++)
                            {
                                images.add(getRenderedImage(-1, -1, c, canvasView));
                                pos++;
                                if (progressListener != null)
                                    progressListener.notifyProgress(pos, len);
                            }

                            result.setImage(0, 0, IcyBufferedImage.createFrom(images));
                        }
                        else
                        {
                            result.setImage(0, 0, getRenderedImage(-1, -1, -1, canvasView));
                            pos++;
                            if (progressListener != null)
                                progressListener.notifyProgress(pos, len);
                        }
                    }
                }
            }
            finally
            {
                endUpdate();
                result.endUpdate();
            }
        }

        return result;
    }

    /**
     * @deprecated Use {@link #getRenderedSequence(boolean, ProgressListener)} instead.
     */
    @Deprecated
    public Sequence getRenderedSequence(boolean canvasView)
    {
        return getRenderedSequence(canvasView, null);
    }

    /**
     * @deprecated Use {@link #getRenderedSequence(boolean, ProgressListener)} instead.
     */
    @Deprecated
    public Sequence getRenderedSequence()
    {
        return getRenderedSequence(true, null);
    }

    /**
     * Return the number of "selected" samples
     */
    public int getNumSelectedSamples()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int base_len = getImageSizeX() * getImageSizeY() * getImageSizeC();

        if (getPositionT() == -1)
        {
            if (getPositionZ() == -1)
                return base_len * getImageSizeZ() * getImageSizeT();

            return base_len * getImageSizeT();
        }

        if (getPositionZ() == -1)
            return base_len * getImageSizeZ();

        return base_len;
    }

    /**
     * Returns the frame rate (given in frame per second) for play command (T navigation panel).
     */
    public int getFrameRate()
    {
        return tNav.getFrameRate();
    }

    /**
     * Sets the frame rate (given in frame per second) for play command (T navigation panel).
     */
    public void setFrameRate(int fps)
    {
        tNav.setFrameRate(fps);
    }

    /**
     * update Z slider state
     */
    protected void updateZNav()
    {
        final int maxZ = getMaxPositionZ();
        final int z = getPositionZ();

        zNav.setMaximum(maxZ);
        if (z != -1)
        {
            zNav.setValue(z);
            zNav.setVisible(maxZ > 0);
        }
        else
            zNav.setVisible(false);
    }

    /**
     * update T slider state
     */
    protected void updateTNav()
    {
        final int maxT = getMaxPositionT();
        final int t = getPositionT();

        tNav.setMaximum(maxT);
        if (t != -1)
        {
            tNav.setValue(t);
            tNav.setVisible(maxT > 0);
        }
        else
            tNav.setVisible(false);
    }

    /**
     * @deprecated Use {@link #getLayer(Overlay)} instead.
     */
    @Deprecated
    public Layer getLayer(Painter painter)
    {
        for (Layer layer : getLayers(false))
            if (layer.getPainter() == painter)
                return layer;

        return null;
    }

    /**
     * Find the layer corresponding to the specified Overlay
     */
    public Layer getLayer(Overlay overlay)
    {
        return layers.get(overlay);
    }

    /**
     * Find the layer corresponding to the specified ROI (use the ROI overlay internally).
     */
    public Layer getLayer(ROI roi)
    {
        return getLayer(roi.getOverlay());
    }

    /**
     * @deprecated Use {@link #hasLayer(Overlay)} instead.
     */
    @Deprecated
    public boolean hasLayer(Painter painter)
    {
        return getLayer(painter) != null;
    }

    /**
     * Returns true if the canvas contains a layer for the specified {@link Overlay}.
     */
    public boolean hasLayer(Overlay overlay)
    {
        synchronized (layers)
        {
            return layers.containsKey(overlay);
        }
    }

    public boolean hasLayer(Layer layer)
    {
        synchronized (layers)
        {
            return layers.containsValue(layer);
        }
    }

    /**
     * @deprecated Use {@link #addLayer(Overlay)} instead.
     */
    @Deprecated
    public void addLayer(Painter painter)
    {
        if (!hasLayer(painter))
            addLayer(new Layer(painter));
    }

    public Layer addLayer(Overlay overlay)
    {
        if (!hasLayer(overlay))
            return addLayer(new Layer(overlay));

        return null;
    }

    protected Layer addLayer(Layer layer)
    {
        if (layer != null)
        {
            // listen layer
            layer.addListener(this);

            // add to list
            synchronized (layers)
            {
                layers.put(layer.getOverlay(), layer);
                if (Layer.DEFAULT_NAME.equals(layer))
                    layer.setName("layer " + layers.size());
            }

            // added
            layerAdded(layer);
        }

        return layer;
    }

    /**
     * @deprecated Use {@link #removeLayer(Overlay)} instead.
     */
    @Deprecated
    public void removeLayer(Painter painter)
    {
        removeLayer(getLayer(painter));
    }

    /**
     * Remove the layer for the specified {@link Overlay} from the canvas.<br/>
     * Returns <code>true</code> if the method succeed.
     */
    public boolean removeLayer(Overlay overlay)
    {
        final Layer layer;

        // remove from list
        synchronized (layers)
        {
            layer = layers.remove(overlay);
        }

        if (layer != null)
        {
            // stop listening layer
            layer.removeListener(this);
            // notify remove
            layerRemoved(layer);

            return true;
        }

        return false;
    }

    /**
     * Remove the specified layer from the canvas.
     */
    public void removeLayer(Layer layer)
    {
        removeLayer(layer.getOverlay());
    }

    /**
     * @deprecated Use {@link #addLayerListener(CanvasLayerListener)} instead.
     */
    @Deprecated
    public void addLayersListener(CanvasLayerListener listener)
    {
        addLayerListener(listener);
    }

    /**
     * @deprecated Use {@link #removeLayerListener(CanvasLayerListener)} instead.
     */
    @Deprecated
    public void removeLayersListener(CanvasLayerListener listener)
    {
        removeLayerListener(listener);
    }

    /**
     * Add a layer listener
     * 
     * @param listener
     */
    public void addLayerListener(CanvasLayerListener listener)
    {
        listenerList.add(CanvasLayerListener.class, listener);
    }

    /**
     * Remove a layer listener
     * 
     * @param listener
     */
    public void removeLayerListener(CanvasLayerListener listener)
    {
        listenerList.remove(CanvasLayerListener.class, listener);
    }

    protected void fireLayerChangedEvent(CanvasLayerEvent event)
    {
        for (CanvasLayerListener listener : getListeners(CanvasLayerListener.class))
            listener.canvasLayerChanged(event);
    }

    /**
     * Add a IcyCanvas listener
     * 
     * @param listener
     */
    public void addCanvasListener(IcyCanvasListener listener)
    {
        listenerList.add(IcyCanvasListener.class, listener);
    }

    /**
     * Remove a IcyCanvas listener
     * 
     * @param listener
     */
    public void removeCanvasListener(IcyCanvasListener listener)
    {
        listenerList.remove(IcyCanvasListener.class, listener);
    }

    protected void fireCanvasChangedEvent(IcyCanvasEvent event)
    {
        for (IcyCanvasListener listener : getListeners(IcyCanvasListener.class))
            listener.canvasChanged(event);
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    public void endUpdate()
    {
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    /**
     * layer added
     * 
     * @param layer
     */
    protected void layerAdded(Layer layer)
    {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.ADDED));
    }

    /**
     * layer removed
     * 
     * @param layer
     */
    protected void layerRemoved(Layer layer)
    {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.REMOVED));
    }

    /**
     * layer has changed
     */
    @Override
    public void layerChanged(Layer layer, String propertyName)
    {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.CHANGED, propertyName));
    }

    /**
     * canvas changed (packed event).<br>
     * do global changes processing here
     */
    public void changed(IcyCanvasEvent event)
    {
        final IcyCanvasEventType eventType = event.getType();

        // handle synchronized canvas
        if (isSynchronized())
        {
            final List<IcyCanvas> synchCanvasList = getSynchronizedCanvas();

            // this is the synchronizer header so dispatch view changes to others canvas
            if (getSynchHeader(synchCanvasList))
            {
                try
                {
                    // synchronize all events when the view has just been synchronized
                    final boolean synchAll = (eventType == IcyCanvasEventType.SYNC_CHANGED);
                    synchronizeCanvas(synchCanvasList, event, synchAll);
                }
                finally
                {
                    releaseSynchHeader();
                }
            }
        }

        switch (eventType)
        {
            case POSITION_CHANGED:
                final int curZ = getPositionZ();
                final int curT = getPositionT();
                final int curC = getPositionC();

                switch (event.getDim())
                {
                    case Z:
                        // ensure Z slider position
                        if (curZ != -1)
                            zNav.setValue(curZ);
                        break;

                    case T:
                        // ensure T slider position
                        if (curT != -1)
                            tNav.setValue(curT);
                        break;

                    case C:
                        // single channel mode
                        if (curC != -1)
                        {
                            final int maxC = getMaxPositionC();

                            // disabled others channels
                            for (int c = 0; c <= maxC; c++)
                                getLut().getLutChannel(c).setEnabled(curC == c);
                        }
                        break;

                    case NULL:
                        // ensure Z slider position
                        if (curZ != -1)
                            zNav.setValue(curZ);
                        // ensure T slider position
                        if (curT != -1)
                            tNav.setValue(curT);
                        break;
                }
                // refresh mouse panel informations
                mouseInfPanel.updateInfos(this);
                break;

            case MOUSE_IMAGE_POSITION_CHANGED:
                // refresh mouse panel informations
                mouseInfPanel.updateInfos(this);
                break;
        }

        // notify listeners that canvas have changed
        fireCanvasChangedEvent(event);
    }

    /**
     * layer property has changed (packed event)
     */
    protected void layerChanged(CanvasLayerEvent event)
    {
        final String property = event.getProperty();

        // we need to rebuild sorted layer list
        if ((event.getType() != LayersEventType.CHANGED) || (property == null) || (property == Layer.PROPERTY_PRIORITY))
            orderedLayersOutdated = true;

        // notify listeners that layers have changed
        fireLayerChangedEvent(event);
    }

    /**
     * position has changed<br>
     * 
     * @param dim
     *        define the position which has changed
     */
    protected void positionChanged(DimensionId dim)
    {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.POSITION_CHANGED, dim));
    }

    @Override
    public void lutChanged(LUTEvent event)
    {
        final int curC = getPositionC();

        // single channel mode ?
        if (curC != -1)
        {
            final int channel = event.getComponent();

            // channel is enabled --> change C position
            if ((channel != -1) && getLut().getLutChannel(channel).isEnabled())
                setPositionC(channel);
            else
                // ensure we have 1 channel enable
                getLut().getLutChannel(curC).setEnabled(true);
        }

        lutChanged(event.getComponent());
    }

    /**
     * lut changed
     * 
     * @param component
     */
    protected void lutChanged(int component)
    {

    }

    /**
     * sequence meta data has changed
     */
    protected void sequenceMetaChanged(String metadataName)
    {

    }

    /**
     * sequence type has changed
     */
    protected void sequenceTypeChanged()
    {

    }

    /**
     * sequence component bounds has changed
     * 
     * @param colorModel
     * @param component
     */
    protected void sequenceComponentBoundsChanged(IcyColorModel colorModel, int component)
    {

    }

    /**
     * sequence component bounds has changed
     * 
     * @param colorModel
     * @param component
     */
    protected void sequenceColorMapChanged(IcyColorModel colorModel, int component)
    {

    }

    /**
     * sequence data has changed
     * 
     * @param image
     *        image which has changed (null if global data changed)
     * @param type
     *        event type
     */
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        // update sliders bounds if needed
        updateZNav();
        updateTNav();

        // adjust X position if needed
        final int maxX = getMaxPositionX();
        final int curX = getPositionX();
        if ((curX != -1) && (curX > maxX))
            setPositionX(maxX);

        // adjust Y position if needed
        final int maxY = getMaxPositionY();
        final int curY = getPositionY();
        if ((curY != -1) && (curY > maxY))
            setPositionY(maxY);

        // adjust C position if needed
        final int maxC = getMaxPositionC();
        final int curC = getPositionC();
        if ((curC != -1) && (curC > maxC))
            setPositionC(maxC);

        // adjust Z position if needed
        final int maxZ = getMaxPositionZ();
        final int curZ = getPositionZ();
        if ((curZ != -1) && (curZ > maxZ))
            setPositionZ(maxZ);

        // adjust T position if needed
        final int maxT = getMaxPositionT();
        final int curT = getPositionT();
        if ((curT != -1) && (curT > maxT))
            setPositionT(maxT);

        // refresh mouse panel informations (data values can have changed)
        mouseInfPanel.updateInfos(this);
    }

    /**
     * @deprecated Use {@link #sequenceOverlayChanged(Overlay, SequenceEventType)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    protected void sequencePainterChanged(Painter painter, SequenceEventType type)
    {
        // no more stuff here
    }

    /**
     * Sequence overlay has changed
     * 
     * @param overlay
     *        overlay which has changed (null if global overlay changed)
     * @param type
     *        event type
     */
    protected void sequenceOverlayChanged(Overlay overlay, SequenceEventType type)
    {
        final Sequence sequence = getSequence();

        switch (type)
        {
            case ADDED:
                // handle special case of multiple adds
                if (overlay == null)
                {
                    if (sequence != null)
                    {
                        final Set<Overlay> overlays = getOverlays();

                        beginUpdate();
                        try
                        {
                            // add layers which are present in sequence and not in canvas
                            for (Overlay seqOverlay : sequence.getOverlaySet())
                                if (!overlays.contains(seqOverlay))
                                    addLayer(seqOverlay);
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                else
                    addLayer(overlay);
                break;

            case REMOVED:
                // handle special case of multiple removes
                if (overlay == null)
                {
                    if (sequence != null)
                    {
                        final Set<Overlay> seqOverlays = sequence.getOverlaySet();

                        beginUpdate();
                        try
                        {
                            // remove layers which are not anymore present in sequence
                            for (Overlay o : getOverlays())
                                if ((o != imageOverlay) && !seqOverlays.contains(o))
                                    removeLayer(o);
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                else
                    removeLayer(overlay);
                break;

            case CHANGED:
                // handle special case of multiple removes or/and adds
                if (overlay == null)
                {
                    if (sequence != null)
                    {
                        final Set<Overlay> overlays = getOverlays();
                        final Set<Overlay> seqOverlays = sequence.getOverlaySet();

                        beginUpdate();
                        try
                        {
                            // remove layers which are not anymore present in sequence
                            for (Overlay o : getOverlays())
                                if ((o != imageOverlay) && !seqOverlays.contains(o))
                                    removeLayer(o);
                            // add layers which are present in sequence and not in canvas
                            for (Overlay seqOverlay : seqOverlays)
                                if (!overlays.contains(seqOverlay))
                                    addLayer(seqOverlay);
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                break;
        }
    }

    /**
     * sequence roi has changed
     * 
     * @param roi
     *        roi which has changed (null if global roi changed)
     * @param type
     *        event type
     */
    protected void sequenceROIChanged(ROI roi, SequenceEventType type)
    {
        // nothing here

    }

    @Override
    public void viewerChanged(ViewerEvent event)
    {
        switch (event.getType())
        {
            case POSITION_CHANGED:
                // ignore this event as we are launching it
                break;

            case LUT_CHANGED:
                // set new lut
                setLut(viewer.getLut(), true);
                break;

            case CANVAS_CHANGED:
                // nothing to do
                break;
        }
    }

    @Override
    public void viewerClosed(Viewer viewer)
    {
        // nothing to do here
    }

    @Override
    public final void sequenceChanged(SequenceEvent event)
    {
        switch (event.getSourceType())
        {
            case SEQUENCE_META:
                sequenceMetaChanged((String) event.getSource());
                break;

            case SEQUENCE_TYPE:
                sequenceTypeChanged();
                break;

            case SEQUENCE_COMPONENTBOUNDS:
                sequenceComponentBoundsChanged((IcyColorModel) event.getSource(), event.getParam());
                break;

            case SEQUENCE_COLORMAP:
                sequenceColorMapChanged((IcyColorModel) event.getSource(), event.getParam());
                break;

            case SEQUENCE_DATA:
                sequenceDataChanged((IcyBufferedImage) event.getSource(), event.getType());
                break;

            case SEQUENCE_OVERLAY:
                final Overlay overlay = (Overlay) event.getSource();

                sequenceOverlayChanged(overlay, event.getType());

                // backward compatibility
                @SuppressWarnings("deprecation")
                final Painter painter;

                if (overlay instanceof OverlayWrapper)
                    painter = ((OverlayWrapper) overlay).getPainter();
                else
                    painter = overlay;

                sequencePainterChanged(painter, event.getType());
                break;

            case SEQUENCE_ROI:
                sequenceROIChanged((ROI) event.getSource(), event.getType());
                break;
        }
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        // nothing to do here
    }

    @Override
    public void onChanged(EventHierarchicalChecker event)
    {
        if (event instanceof CanvasLayerEvent)
            layerChanged((CanvasLayerEvent) event);

        if (event instanceof IcyCanvasEvent)
            changed((IcyCanvasEvent) event);
    }
}
