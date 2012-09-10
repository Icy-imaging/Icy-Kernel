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
package icy.canvas;

import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.canvas.Layer.LayerListener;
import icy.canvas.LayersEvent.LayersEventType;
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
import icy.image.lut.LUTListener;
import icy.main.Icy;
import icy.painter.Painter;
import icy.plugin.interface_.PluginCanvas;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.sequence.SequenceListener;
import icy.system.IcyExceptionHandler;
import icy.util.ClassUtil;
import icy.util.OMEUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;

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
    public static class EventLayerSorter implements Comparator<Layer>
    {
        public static EventLayerSorter instance = new EventLayerSorter();

        @Override
        public int compare(Layer layer1, Layer layer2)
        {
            final ROI roi1 = layer1.getAttachedROI();
            final ROI roi2 = layer2.getAttachedROI();

            // no attached roi ? --> eliminate
            if (roi1 == null)
            {
                if (roi2 == null)
                    return 0;
                return 1;
            }
            if (roi2 == null)
                return -1;

            // focus priority
            if (roi1.isFocused())
                return -1;
            if (roi2.isFocused())
                return 1;

            // selection priority
            if (roi1.isSelected())
            {
                if (roi2.isSelected())
                    return 0;
                return -1;
            }
            if (roi2.isSelected())
                return 1;

            return 0;
        }
    }

    public static IcyCanvas create(PluginCanvas pluginCanvas, Viewer viewer)
    {
        return pluginCanvas.createCanvas(viewer);
    }

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
     * layers draw flag
     */
    protected boolean drawLayers;
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
     * Layers attached to canvas<br>
     * There are representing sequence's painters with some visualization properties
     */
    protected final ArrayList<Layer> layers;

    /**
     * internal layer id generator
     */
    protected int layerIdGen;
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
     * internals
     */
    protected LUT lut;
    protected boolean synchHeader;

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

        drawLayers = true;
        layers = new ArrayList<Layer>();
        layerIdGen = 1;
        syncId = 0;
        synchHeader = false;
        updater = new UpdateEventHandler(this, false);

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

        final Sequence sequence = getSequence();

        // add sequence's painters to layer list
        if (sequence != null)
        {
            beginUpdate();
            try
            {
                for (Painter painter : sequence.getPainters())
                    addLayer(painter);
            }
            finally
            {
                endUpdate();
            }
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

        // remove all IcyCanvas listeners
        final IcyCanvasListener[] canvasListenters = listenerList.getListeners(IcyCanvasListener.class);
        for (IcyCanvasListener listener : canvasListenters)
            removeCanvasListener(listener);

        // remove all Layers listeners
        final LayersListener[] layersListenters = listenerList.getListeners(LayersListener.class);
        for (LayersListener listener : layersListenters)
            removeLayersListener(listener);
    }

    public abstract void refresh();

    /**
     * @return the drawLayers
     */
    public boolean getDrawLayers()
    {
        return drawLayers;
    }

    /**
     * @param drawLayers
     *        the drawLayers to set
     */
    public void setDrawLayers(boolean drawLayers)
    {
        if (this.drawLayers != drawLayers)
        {
            this.drawLayers = drawLayers;
            repaint();
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
                lutChanged(new LUTEvent(lut, -1));
        }
    }

    /**
     * Called by the parent viewer when building its toolbar<br>
     * so canvas can customize it at some point.
     * 
     * @param toolBar
     */
    public void addViewerToolbarComponents(JToolBar toolBar)
    {

    }

    /**
     * @return the infoPanel
     */
    public JPanel getPanel()
    {
        return panel;
    }

    /**
     * @return the layers
     */
    public ArrayList<Layer> getLayers()
    {
        synchronized (layers)
        {
            return new ArrayList<Layer>(layers);
        }
    }

    /**
     * @return the visible layers
     */
    public ArrayList<Layer> getVisibleLayers()
    {
        final ArrayList<Layer> result = new ArrayList<Layer>();

        synchronized (layers)
        {
            for (Layer l : layers)
                if (l.isVisible())
                    result.add(l);
        }

        return result;
    }

    /**
     * @return the painters of layers
     */
    public ArrayList<Painter> getLayersPainter()
    {
        final ArrayList<Painter> result = new ArrayList<Painter>();

        for (Layer layer : getLayers())
            result.add(layer.getPainter());

        return result;
    }

    /**
     * @return the SyncId
     */
    public int getSyncId()
    {
        return syncId;
    }

    /**
     * Set the synchronization group id (0 means unsynchronized)
     * 
     * @param id
     *        the syncId to set
     */
    public void setSyncId(int id)
    {
        if (this.syncId != id)
        {
            this.syncId = id;

            // notify sync has changed
            updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.SYNC_CHANGED));
        }
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
                    final int posX = getPositionX();
                    final int posY = getPositionY();
                    final int posZ = getPositionZ();
                    final int posT = getPositionT();
                    final int posC = getPositionC();

                    for (IcyCanvas cnv : canvasList)
                    {
                        if (posX != -1)
                            cnv.setPositionX(posX);
                        if (posY != -1)
                            cnv.setPositionY(posY);
                        if (posZ != -1)
                            cnv.setPositionZ(posZ);
                        if (posT != -1)
                            cnv.setPositionT(posT);
                        if (posC != -1)
                            cnv.setPositionC(posC);
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
    public double getMax(DimensionId dim)
    {
        switch (dim)
        {
            case X:
                return getMaxX();
            case Y:
                return getMaxY();
            case Z:
                return getMaxZ();
            case T:
                return getMaxT();
            case C:
                return getMaxC();
        }

        return 0;
    }

    /**
     * Get maximum X value
     */
    public int getMaxX()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int maxX = getImageSizeX() - 1;

        if (maxX < 0)
            return 0;

        return maxX;
    }

    /**
     * Get maximum Y value
     */
    public int getMaxY()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int maxY = getImageSizeY() - 1;

        if (maxY < 0)
            return 0;

        return maxY;
    }

    /**
     * Get maximum Z value
     */
    public int getMaxZ()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int maxZ = getImageSizeZ() - 1;

        if (maxZ < 0)
            return 0;

        return maxZ;
    }

    /**
     * Get maximum T value
     */
    public int getMaxT()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int maxT = getImageSizeT() - 1;

        if (maxT < 0)
            return 0;

        return maxT;
    }

    /**
     * Get maximum C value
     */
    public int getMaxC()
    {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int maxC = getImageSizeC() - 1;

        if (maxC < 0)
            return 0;

        return maxC;
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
     * canvas view size X
     */
    public int getCanvasSizeX()
    {
        // by default we use panel width
        int res = getWidth();
        // preferred width if size not yet set
        if (res == 0)
            res = getPreferredSize().width;

        return res;
    }

    /**
     * canvas view size Y
     */
    public int getCanvasSizeY()
    {
        // by default we use panel height
        int res = getHeight();
        // preferred height if size not yet set
        if (res == 0)
            res = getPreferredSize().height;

        return res;
    }

    /**
     * canvas view size Z
     */
    public int getCanvasSizeZ()
    {
        // by default : no Z dimension
        return 1;
    }

    /**
     * canvas view size T
     */
    public int getCanvasSizeT()
    {
        // by default : no T dimension
        return 1;
    }

    /**
     * canvas view size C
     */
    public int getCanvasSizeC()
    {
        // by default : no C dimension
        return 1;
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
        return 0;
    }

    /**
     * mouse Y image position
     */
    public double getMouseImagePosY()
    {
        return 0;
    }

    /**
     * mouse Z image position
     */
    public double getMouseImagePosZ()
    {
        return getZ();
    }

    /**
     * mouse T image position
     */
    public double getMouseImagePosT()
    {
        return getT();
    }

    /**
     * mouse C image position
     */
    public double getMouseImagePosC()
    {
        return getC();
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
        final int adjX = Math.max(-1, Math.min(x, getMaxX()));

        if (getPositionX() != adjX)
            setPositionXInternal(adjX);
    }

    /**
     * Set Y position
     */
    public void setPositionY(int y)
    {
        final int adjY = Math.max(-1, Math.min(y, getMaxY()));

        if (getPositionY() != adjY)
            setPositionYInternal(adjY);
    }

    /**
     * Set Z position
     */
    public void setPositionZ(int z)
    {
        final int adjZ = Math.max(-1, Math.min(z, getMaxZ()));

        if (getPositionZ() != adjZ)
            setPositionZInternal(adjZ);
    }

    /**
     * Set T position
     */
    public void setPositionT(int t)
    {
        final int adjT = Math.max(-1, Math.min(t, getMaxT()));

        if (getPositionT() != adjT)
            setPositionTInternal(adjT);
    }

    /**
     * Set C position
     */
    public void setPositionC(int c)
    {
        final int adjC = Math.max(-1, Math.min(c, getMaxC()));

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

    // /**
    // * Center on specified image X position
    // */
    // public void centerOnImageX(double value)
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeX = getCanvasSizeX();
    //
    // // X dimension supported ?
    // if (sizeX > 1)
    // setOffsetX(sizeX - ((getImageCanvasSizeX() / 2) + imageToCanvasDeltaX(value)));
    // }
    // }
    //
    // /**
    // * Center on specified image Y position
    // */
    // public void centerOnImageY(double value)
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeY = getCanvasSizeY();
    //
    // // Y dimension supported ?
    // if (sizeY > 1)
    // setOffsetY(sizeY - ((getImageCanvasSizeY() / 2) + imageToCanvasDeltaY(value)));
    // }
    // }
    //
    // /**
    // * Center on specified image Z position
    // */
    // public void centerOnImageZ(double value)
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeZ = getCanvasSizeZ();
    //
    // // Z dimension supported ?
    // if (sizeZ > 1)
    // setOffsetZ(sizeZ - ((getImageCanvasSizeZ() / 2) + imageToCanvasDeltaZ(value)));
    // }
    // }
    //
    // /**
    // * Center on specified image T position
    // */
    // public void centerOnImageT(double value)
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeT = getCanvasSizeT();
    //
    // // T dimension supported ?
    // if (sizeT > 1)
    // setOffsetY(sizeT - ((getImageCanvasSizeT() / 2) + imageToCanvasDeltaT(value)));
    // }
    // }
    //
    // /**
    // * Center on specified image C position
    // */
    // public void centerOnImageC(double value)
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeC = getCanvasSizeC();
    //
    // // C dimension supported ?
    // if (sizeC > 1)
    // setOffsetC(sizeC - ((getImageCanvasSizeC() / 2) + imageToCanvasDeltaC(value)));
    // }
    // }
    //
    // /**
    // * Center image in canvas
    // */
    // public void centerImage()
    // {
    // final Sequence seq = getSequence();
    //
    // if (seq != null)
    // {
    // // get canvas size
    // final int sizeX = getCanvasSizeX();
    // final int sizeY = getCanvasSizeY();
    // final int sizeZ = getCanvasSizeZ();
    // final int sizeT = getCanvasSizeT();
    // final int sizeC = getCanvasSizeC();
    //
    // // X dimension supported ?
    // if (sizeX > 1)
    // setOffsetX((sizeX - getImageCanvasSizeX()) / 2);
    // // Y dimension supported ?
    // if (sizeY > 1)
    // setOffsetY((sizeY - getImageCanvasSizeY()) / 2);
    // // Z dimension supported ?
    // if (sizeZ > 1)
    // setOffsetZ((sizeZ - getImageCanvasSizeZ()) / 2);
    // // R dimension supported ?
    // if (sizeT > 1)
    // setOffsetT((sizeT - getImageCanvasSizeT()) / 2);
    // // C dimension supported ?
    // if (sizeC > 1)
    // setOffsetC((sizeC - getImageCanvasSizeC()) / 2);
    // }
    // }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // by default we only forward event to painters
        for (Layer layer : getVisibleOrderedLayersForEvent())
            layer.getPainter().keyPressed(e, new Point2D.Double(getMouseImagePosX(), getMouseImagePosY()), this);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // by default we only forward event to painters
        for (Layer layer : getVisibleOrderedLayersForEvent())
            layer.getPainter().keyReleased(e, new Point2D.Double(getMouseImagePosX(), getMouseImagePosY()), this);
    }

    /**
     * Get the image at position (t, z, c)
     * 
     * @return image[t, z, c]
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
     * Get the image at position (t, z)
     * 
     * @return image[t, z]
     */
    public IcyBufferedImage getImage(int t, int z)
    {
        if ((t == -1) || (z == -1))
            return null;

        final Sequence sequence = getSequence();

        // have to test this as sequence reference can be release in viewer
        if (sequence != null)
            return sequence.getImage(t, z);

        return null;
    }

    /**
     * Get the current image
     * 
     * @return current image
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
     * Return a RGBA renderer image for image at position (t, z, c)<br>
     * Free feel to the canvas to handle or not a specific dimension.
     * 
     * @param t
     *        T position of wanted image (-1 for complete sequence)
     * @param z
     *        Z position of wanted image (-1 for complete stack)
     * @param c
     *        C position of wanted image (-1 for all components)
     * @param canvasView
     *        render with canvas view if true else use default sequence dimension
     */
    public abstract BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView);

    /**
     * Return a RGBA renderer image representing the canvas view for image at position (t, z) .
     * 
     * @param t
     *        T position of wanted image (-1 for complete sequence)
     * @param z
     *        Z position of wanted image (-1 for complete stack)
     * @param c
     *        C position of wanted image (-1 for all components)
     */
    public BufferedImage getRenderedImage(int t, int z, int c)
    {
        return getRenderedImage(t, z, c, true);
    }

    /**
     * Return a sequence which contains rendered images.<br>
     * <br>
     * Default implementation, override it if needed in your canvas.
     */
    public Sequence getRenderedSequence()
    {
        return getRenderedSequence(true);
    }

    /**
     * Return a sequence which contains rendered images.<br>
     * Default implementation, override it if needed in your canvas.
     * 
     * @param canvasView
     *        render with canvas view if true else use default sequence dimension
     */
    public Sequence getRenderedSequence(boolean canvasView)
    {
        return getRenderedSequence(canvasView, null);
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

            final int posT = getPositionT();
            final int posZ = getPositionZ();
            final int posC = getPositionC();
            final int sizeT = getImageSizeT();
            final int sizeZ = getImageSizeZ();
            final int sizeC = getImageSizeC();

            int pos = 0;
            int len = 1;
            if (posT != -1)
                len *= sizeT;
            if (posZ != -1)
                len *= sizeZ;
            if (posC != -1)
                len *= sizeC;

            result.beginUpdate();
            beginUpdate();
            try
            {
                if (posT != -1)
                {
                    for (int t = 0; t < sizeT; t++)
                    {
                        if (posZ != -1)
                        {
                            for (int z = 0; z < sizeZ; z++)
                            {
                                if (posC != -1)
                                {
                                    final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

                                    for (int c = 0; c < sizeC; c++)
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
                    if (posZ != -1)
                    {
                        for (int z = 0; z < sizeZ; z++)
                        {
                            if (posC != -1)
                            {
                                final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

                                for (int c = 0; c < sizeC; c++)
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
                        if (posC != -1)
                        {
                            final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

                            for (int c = 0; c < sizeC; c++)
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
     * update Z slider state
     */
    protected void updateZNav()
    {
        final int maxZ = getMaxZ();
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
        final int maxT = getMaxT();
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

    public Layer getLayer(Painter painter)
    {
        for (Layer layer : getLayers())
            if (layer.getPainter() == painter)
                return layer;

        return null;
    }

    public Layer getLayer(ROI roi)
    {
        return getLayer(roi.getPainter());
    }

    public boolean hasLayer(Painter painter)
    {
        return getLayer(painter) != null;
    }

    public boolean hasLayer(Layer layer)
    {
        synchronized (layers)
        {
            return layers.contains(layer);
        }
    }

    public void addLayer(Painter painter)
    {
        if (!hasLayer(painter))
        {
            final Layer layer = new Layer(painter);

            layer.setName("layer " + layerIdGen);
            layerIdGen++;

            // listen layer
            layer.addListener(this);

            // add to list
            synchronized (layers)
            {
                layers.add(layer);
            }

            // added
            layerAdded(layer);
        }
    }

    public void removeLayer(Painter painter)
    {
        removeLayer(getLayer(painter));
    }

    public void removeLayer(Layer layer)
    {
        if (hasLayer(layer))
        {
            // stop listening layer
            layer.removeListener(this);

            // remove from list
            synchronized (layers)
            {
                layers.remove(layer);
            }

            // removed
            layerRemoved(layer);
        }
    }

    public ArrayList<Layer> getVisibleOrderedLayersForEvent()
    {
        final ArrayList<Layer> result = getVisibleLayers();

        Collections.sort(result, EventLayerSorter.instance);

        return result;
    }

    /**
     * Add a layer listener
     * 
     * @param listener
     */
    public void addLayersListener(LayersListener listener)
    {
        listenerList.add(LayersListener.class, listener);
    }

    /**
     * Remove a layer listener
     * 
     * @param listener
     */
    public void removeLayersListener(LayersListener listener)
    {
        listenerList.remove(LayersListener.class, listener);
    }

    private void fireLayersChangedEvent(LayersEvent event)
    {
        for (LayersListener listener : getListeners(LayersListener.class))
            listener.layersChanged(event);
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

    private void fireCanvasChangedEvent(IcyCanvasEvent event)
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
        updater.changed(new LayersEvent(layer, LayersEventType.ADDED));
    }

    /**
     * layer removed
     * 
     * @param layer
     */
    protected void layerRemoved(Layer layer)
    {
        // handle with updater
        updater.changed(new LayersEvent(layer, LayersEventType.REMOVED));
    }

    /**
     * layer has changed
     * 
     * @param layer
     */
    @Override
    public void layerChanged(Layer layer)
    {
        // handle with updater
        updater.changed(new LayersEvent(layer, LayersEventType.CHANGED));
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
     * layers has changed (packed event)
     */
    public void layersChanged(LayersEvent event)
    {
        // notify listeners that layers have changed
        fireLayersChangedEvent(event);
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
        final int maxX = getMaxX();
        final int curX = getPositionX();
        if ((curX != -1) && (curX > maxX))
            setPositionX(maxX);

        // adjust Y position if needed
        final int maxY = getMaxY();
        final int curY = getPositionY();
        if ((curY != -1) && (curY > maxY))
            setPositionY(maxY);

        // adjust C position if needed
        final int maxC = getMaxC();
        final int curC = getPositionC();
        if ((curC != -1) && (curC > maxC))
            setPositionC(maxC);

        // adjust Z position if needed
        final int maxZ = getMaxZ();
        final int curZ = getPositionZ();
        if ((curZ != -1) && (curZ > maxZ))
            setPositionZ(maxZ);

        // adjust T position if needed
        final int maxT = getMaxT();
        final int curT = getPositionT();
        if ((curT != -1) && (curT > maxT))
            setPositionT(maxT);

        // refresh mouse panel informations (data values can have changed)
        mouseInfPanel.updateInfos(this);
    }

    /**
     * sequence painter has changed
     * 
     * @param painter
     *        painter which has changed (null if global painter changed)
     * @param type
     *        event type
     */
    protected void sequencePainterChanged(Painter painter, SequenceEventType type)
    {
        final Sequence sequence = getSequence();

        switch (type)
        {
            case ADDED:
                // handle special case of multiple add
                if (painter == null)
                {
                    if (sequence != null)
                    {
                        final ArrayList<Painter> layersPainter = getLayersPainter();

                        beginUpdate();
                        try
                        {
                            // add layers which are present in sequence and not in canvas
                            for (Painter seqPainter : sequence.getPainters())
                                if (!layersPainter.contains(seqPainter))
                                    addLayer(seqPainter);
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                else
                    addLayer(painter);
                break;

            case REMOVED:
                // handle special case of multiple remove
                if (painter == null)
                {
                    if (sequence != null)
                    {
                        final ArrayList<Painter> seqPainters = sequence.getPainters();

                        beginUpdate();
                        try
                        {
                            // remove layers which are not anymore present in sequence
                            for (Layer layer : getLayers())
                                if (!seqPainters.contains(layer.getPainter()))
                                    removeLayer(layer);
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                else
                    removeLayer(painter);
                break;

            case CHANGED:
                // handle special case of multiple remove
                if (painter == null)
                {
                    if (sequence != null)
                    {
                        final ArrayList<Painter> layersPainter = getLayersPainter();
                        final ArrayList<Painter> seqPainters = sequence.getPainters();

                        beginUpdate();
                        try
                        {
                            // add layers which are present in sequence and not in canvas
                            for (Painter seqPainter : sequence.getPainters())
                                if (!layersPainter.contains(seqPainter))
                                    addLayer(seqPainter);
                            // remove layers which are not anymore present in sequence
                            for (Layer layer : getLayers())
                                if (!seqPainters.contains(layer.getPainter()))
                                    removeLayer(layer);
                            // no add or remove but multiple changes
                            layerChanged(getLayer(painter));
                        }
                        finally
                        {
                            endUpdate();
                        }
                    }
                }
                else
                    layerChanged(getLayer(painter));
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
        // FIXME : why this is needed ?

        // if (roi != null)
        // {
        // final Layer layer = getLayer(roi.getPainter());
        //
        // // manually launch changed event on ROI layer
        // if ((layer != null) && (type == SequenceEventType.CHANGED))
        // layer.changed();
        //
        // layerChanged(layer);
        // }
        // else
        // layerChanged(null);
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

            case SEQUENCE_PAINTER:
                sequencePainterChanged((Painter) event.getSource(), event.getType());
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
        if (event instanceof LayersEvent)
            layersChanged((LayersEvent) event);

        if (event instanceof IcyCanvasEvent)
            changed((IcyCanvasEvent) event);
    }
}
