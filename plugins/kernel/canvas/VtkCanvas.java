package plugins.kernel.canvas;

import icy.canvas.Canvas3D;
import icy.canvas.CanvasLayerEvent;
import icy.canvas.CanvasLayerEvent.LayersEventType;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.canvas.Layer;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.preferences.CanvasPreferences;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.ThreadUtil;
import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkImageVolume;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;
import icy.vtk.VtkImageVolume.VtkVolumeMapperType;
import icy.vtk.VtkSequenceVolume;
import icy.vtk.VtkUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import vtk.vtkCamera;
import vtk.vtkPanel;
import vtk.vtkProp;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkUnsignedCharArray;

/**
 * VTK 3D canvas class.
 * 
 * @author Stephane
 */
@SuppressWarnings("deprecation")
public class VtkCanvas extends Canvas3D implements PropertyChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -1274251057822161271L;

    /**
     * preferences id
     */
    private static final String PREF_ID = "vtkCanvas";

    /**
     * id
     */
    private static final String ID_BGCOLOR = "bgcolor";
    private static final String ID_BOUNDINGBOX = "boundingBox";
    private static final String ID_BOUNDINGBOX_GRID = "boundingBoxGrid";
    private static final String ID_BOUNDINGBOX_RULES = "boundingBoxRules";
    private static final String ID_AXIS = "axis";
    private static final String ID_MAPPER = "mapper";
    private static final String ID_BLENDING = "blending";
    private static final String ID_INTERPOLATION = "interpolation";
    private static final String ID_SHADING = "shading";
    private static final String ID_AMBIENT = "ambient";
    private static final String ID_DIFFUSE = "diffuse";
    private static final String ID_SPECULAR = "specular";
    private static final String ID_SPECULAR_POWER = "specularPower";

    /**
     * basic vtk objects
     */
    protected vtkRenderer renderer;
    protected vtkCamera activeCam;

    /**
     * volume data
     */
    protected VtkSequenceVolume sequenceVolume;

    /**
     * GUI
     */
    protected VtkSettingPanel settingPanel;
    protected CustomVtkPanel panel3D;

    /**
     * internals
     */
    // final InstanceProcessor processor;
    protected XMLPreferences preferences;
    protected final LUT lutSave;
    protected boolean initialized;

    public VtkCanvas(Viewer viewer)
    {
        super(viewer);

        initialized = false;

        // all channel visible at once by default
        posC = -1;

        final Sequence seq = getSequence();

        preferences = CanvasPreferences.getPreferences().node(PREF_ID);

        settingPanel = new VtkSettingPanel();
        panel = settingPanel;

        // default background color set to white
        settingPanel.setBackgroundColor(Color.WHITE);
        settingPanel.addPropertyChangeListener(this);

        // initialize VTK components & main GUI
        panel3D = new CustomVtkPanel();
        panel3D.addKeyListener(this);
        // set 3D view in center
        add(panel3D, BorderLayout.CENTER);

        // update nav bar & mouse infos
        mouseInfPanel.setVisible(false);
        updateZNav();
        updateTNav();

        renderer = panel3D.GetRenderer();
        // set renderer properties
        renderer.SetBackground(Array1DUtil.floatArrayToDoubleArray(getBackgroundColor().getColorComponents(null)));

        activeCam = renderer.GetActiveCamera();
        // set camera properties
        // activeCam.Azimuth(20.0);
        // activeCam.Dolly(1.60);

        // initialize internals
        // processor = new InstanceProcessor();
        // processor.setDefaultThreadName("Canvas3D renderer");
        // // we want the processor to stay alive for sometime
        // processor.setKeepAliveTime(3, TimeUnit.SECONDS);

        // save lut and prepare for 3D visualization
        lutSave = seq.createCompatibleLUT();
        final LUT lut = getLut();

        // save colormap
        saveColormap(lut);
        // adjust LUT alpha level for 3D view (this make lutChanged() to be called)
        setDefaultOpacity(lut);

        // initialize volume data
        sequenceVolume = new VtkSequenceVolume(seq);
        sequenceVolume.setLUT(getLut());
        sequenceVolume.setPosition(getPositionT(), getPositionC());

        // add volume to renderer
        // TODO : add option to remove volume rendering
        renderer.AddVolume(sequenceVolume.getVolume());

        // add vtkPainter actors to the renderer
        for (Layer l : getLayers(false))
            addLayerActors(l);

        // reset camera
        resetCamera();

        // restore settings
        setBackgroundColor(new Color(preferences.getInt(ID_BGCOLOR, 0xFFFFFF)));
        setBoundingBoxVisible(preferences.getBoolean(ID_BOUNDINGBOX, true));
        setBoundingBoxGridVisible(preferences.getBoolean(ID_BOUNDINGBOX_GRID, true));
        setBoundingBoxRulesVisible(preferences.getBoolean(ID_BOUNDINGBOX_RULES, false));
        setAxisVisible(preferences.getBoolean(ID_AXIS, true));
        setVolumeMapperType(VtkVolumeMapperType.values()[preferences.getInt(ID_MAPPER,
                VtkVolumeMapperType.RAYCAST_CPU_FIXEDPOINT.ordinal())]);
        setVolumeInterpolation(preferences.getInt(ID_INTERPOLATION, VtkUtil.VTK_LINEAR_INTERPOLATION));
        setVolumeBlendingMode(VtkVolumeBlendType.values()[preferences.getInt(ID_BLENDING,
                VtkVolumeBlendType.ADDITIVE.ordinal())]);
        setVolumeAmbient(preferences.getDouble(ID_AMBIENT, 0.6d));
        setVolumeDiffuse(preferences.getDouble(ID_DIFFUSE, 0.5d));
        setVolumeSpecularIntensity(preferences.getDouble(ID_SPECULAR, 0.3d));
        setVolumeSpecularPower(preferences.getDouble(ID_SPECULAR_POWER, 30.0d));
        setVolumeShadingEnable(preferences.getBoolean(ID_SHADING, false));

        initialized = true;
    }

    /**
     * Returns rendering background color
     */
    public Color getBackgroundColor()
    {
        return settingPanel.getBackgroundColor();
    }

    /**
     * Sets rendering background color
     */
    public void setBackgroundColor(Color value)
    {
        settingPanel.setBackgroundColor(value);
    }

    /**
     * Returns <code>true</code> if the volume bounding box is visible.
     */
    public boolean isBoundingBoxVisible()
    {
        return settingPanel.isBoundingBoxVisible();
    }

    /**
     * Enable / disable volume bounding box display.
     */
    public void setBoundingBoxVisible(boolean value)
    {
        settingPanel.setBoundingBoxVisible(value);
    }

    /**
     * Returns <code>true</code> if the volume bounding box grid is visible.
     */
    public boolean isBoundingBoxGridVisible()
    {
        return settingPanel.isBoundingBoxGridVisible();
    }

    /**
     * Enable / disable volume bounding box grid display.
     */
    public void setBoundingBoxGridVisible(boolean value)
    {
        settingPanel.setBoundingBoxGridVisible(value);
    }

    /**
     * Returns <code>true</code> if the volume bounding box rules are visible.
     */
    public boolean isBoundingBoxRulesVisible()
    {
        return settingPanel.isBoundingBoxRulesVisible();
    }

    /**
     * Enable / disable volume bounding box rules display.
     */
    public void setBoundingBoxRulesVisible(boolean value)
    {
        settingPanel.setBoundingBoxRulesVisible(value);
    }

    /**
     * Returns <code>true</code> if the 3D axis are visible.
     */
    public boolean isAxisVisible()
    {
        return settingPanel.isAxisVisible();
    }

    /**
     * Enable / disable 3D axis display.
     */
    public void setAxisVisible(boolean value)
    {
        settingPanel.setAxisVisible(value);
    }

    /**
     * @see VtkImageVolume#getBlendingMode()
     */
    public VtkVolumeBlendType getVolumeBlendingMode()
    {
        return settingPanel.getVolumeBlendingMode();
    }

    /**
     * @see VtkImageVolume#setBlendingMode(VtkVolumeBlendType)
     */
    public void setVolumeBlendingMode(VtkVolumeBlendType value)
    {
        settingPanel.setVolumeBlendingMode(value);
    }

    /**
     * @see VtkImageVolume#getSampleResolution()
     */
    public int getVolumeSample()
    {
        return settingPanel.getVolumeSample();
    }

    /**
     * @see VtkImageVolume#setSampleResolution(double)
     */
    public void setVolumeSample(int value)
    {
        settingPanel.setVolumeSample(value);
    }

    /**
     * @see VtkImageVolume#getShade()
     */
    public boolean isVolumeShadingEnable()
    {
        return settingPanel.isVolumeShadingEnable();
    }

    /**
     * @see VtkImageVolume#setShade(boolean)
     */
    public void setVolumeShadingEnable(boolean value)
    {
        settingPanel.setVolumeShadingEnable(value);
    }

    /**
     * @see VtkImageVolume#getAmbient()
     */
    public double getVolumeAmbient()
    {
        return settingPanel.getVolumeAmbient();
    }

    /**
     * @see VtkImageVolume#setAmbient(double)
     */
    public void setVolumeAmbient(double value)
    {
        settingPanel.setVolumeAmbient(value);
    }

    /**
     * @see VtkImageVolume#getDiffuse()
     */
    public double getVolumeDiffuse()
    {
        return settingPanel.getVolumeDiffuse();
    }

    /**
     * @see VtkImageVolume#setDiffuse(double)
     */
    public void setVolumeDiffuse(double value)
    {
        settingPanel.setVolumeDiffuse(value);
    }

    /**
     * @see VtkImageVolume#getSpecular()
     */
    public double getVolumeSpecularIntensity()
    {
        return settingPanel.getVolumeSpecularIntensity();
    }

    /**
     * @see VtkImageVolume#setSpecular(double)
     */
    public void setVolumeSpecularIntensity(double value)
    {
        settingPanel.setVolumeSpecularIntensity(value);
    }

    /**
     * @see VtkImageVolume#getSpecularPower()
     */
    public double getVolumeSpecularPower()
    {
        return settingPanel.getVolumeSpecularPower();
    }

    /**
     * @see VtkImageVolume#setSpecularPower(double)
     */
    public void setVolumeSpecularPower(double value)
    {
        settingPanel.setVolumeSpecularPower(value);
    }

    /**
     * @see VtkImageVolume#getInterpolationMode()
     */
    public int getVolumeInterpolation()
    {
        return settingPanel.getVolumeInterpolation();
    }

    /**
     * @see VtkImageVolume#setInterpolationMode(int)
     */
    public void setVolumeInterpolation(int value)
    {
        settingPanel.setVolumeInterpolation(value);
    }

    /**
     * @see VtkImageVolume#getVolumeMapperType()
     */
    public VtkVolumeMapperType getVolumeMapperType()
    {
        return settingPanel.getVolumeMapperType();
    }

    /**
     * @see VtkImageVolume#setVolumeMapperType(VtkVolumeMapperType)
     */
    public void setVolumeMapperType(VtkVolumeMapperType value)
    {
        settingPanel.setVolumeMapperType(value);
    }

    /**
     * Force render refresh
     */
    @Override
    public void refresh()
    {
        if (!initialized)
            return;

        // refresh rendering
        panel3D.repaint();
        // panel3D.paint(panel3D.getGraphics());
    }

    // private void test()
    // {
    // // exemple de clipping a utiliser par la suite.
    // if ( false )
    // {
    // vtkPlane plane = new vtkPlane();
    // plane.SetOrigin(1000, 1000, 1000);
    // plane.SetNormal( 1, 1, 0);
    // volumeMapper.AddClippingPlane( plane );
    // }
    //
    // vtkOrientationMarkerWidget ow = new vtkOrientationMarkerWidget();
    // }

    private void resetCamera()
    {
        activeCam.SetViewUp(0, -1, 0);
        renderer.ResetCamera();
        activeCam.Elevation(180);
        renderer.ResetCameraClippingRange();
    }

    /**
     * @deprecated Use {@link #setVolumeSample(int)} instead
     */
    @Deprecated
    @Override
    public void setVolumeDistanceSample(int value)
    {
        setVolumeSample(value);
    }

    private void setDefaultOpacity(LUT lut)
    {
        lut.beginUpdate();
        try
        {
            for (LUTChannel lutChannel : lut.getLutChannels())
                lutChannel.getColorMap().setDefaultAlphaFor3D();
        }
        finally
        {
            lut.endUpdate();
        }
    }

    private void restoreOpacity(LUT srcLut, LUT dstLut)
    {
        final int numComp = Math.min(srcLut.getNumChannel(), dstLut.getNumChannel());

        for (int c = 0; c < numComp; c++)
            retoreOpacity(srcLut.getLutChannel(c), dstLut.getLutChannel(c));
    }

    private void retoreOpacity(LUTChannel srcLutBand, LUTChannel dstLutBand)
    {
        dstLutBand.getColorMap().alpha.copyFrom(srcLutBand.getColorMap().alpha);
    }

    private void restoreColormap(LUT lut)
    {
        lut.setScalers(lutSave);
        lut.setColorMaps(lutSave, true);
    }

    private void saveColormap(LUT lut)
    {
        lutSave.setScalers(lut);
        lutSave.setColorMaps(lut, true);
    }

    protected vtkProp[] getLayerActors(Layer layer)
    {
        if (layer != null)
        {
            // add painter actor from the vtk render
            final Overlay overlay = layer.getOverlay();

            if (overlay instanceof VtkPainter)
                return ((VtkPainter) overlay).getProps();
        }

        return new vtkProp[0];
    }

    protected void addLayerActors(Layer layer)
    {
        for (vtkProp actor : getLayerActors(layer))
            VtkUtil.addProp(renderer, actor);
    }

    protected void removeLayerActors(Layer layer)
    {
        for (vtkProp actor : getLayerActors(layer))
            VtkUtil.removeProp(renderer, actor);
    }

    @Override
    public Component getViewComponent()
    {
        return panel3D;
    }

    @Override
    public vtkPanel getPanel3D()
    {
        return panel3D;
    }

    @Override
    public vtkRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Get scaling for image volume rendering
     */
    @Override
    public double[] getVolumeScale()
    {
        return sequenceVolume.getScale();
    }

    /**
     * Set scaling for image volume rendering
     */
    @Override
    public void setVolumeScale(double x, double y, double z)
    {
        final double[] scale = getVolumeScale();

        if ((scale[0] != x) || (scale[1] != y) || (scale[2] != z))
        {
            scale[0] = x;
            scale[1] = y;
            scale[2] = z;

            sequenceVolume.setScale(scale);

            // refresh rendering
            refresh();
        }
    }

    @Override
    public double getMouseImagePosX()
    {
        // not supported
        return 0d;
    }

    @Override
    public double getMouseImagePosY()
    {
        // not supported
        return 0d;
    }

    @Override
    public double getMouseImagePosZ()
    {
        // not supported
        return 0d;
    }

    @Override
    public double getMouseImagePosT()
    {
        // not supported
        return 0d;
    }

    @Override
    public double getMouseImagePosC()
    {
        // not supported
        return 0d;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // send to overlays
        super.keyPressed(e);

        // forward to view
        panel3D.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // send to overlays
        super.keyReleased(e);

        // forward to view
        panel3D.keyReleased(e);
    }

    @Override
    protected void setPositionZInternal(int z)
    {
        // not supported, Z should stay at -1
    }

    @Override
    public BufferedImage getRenderedImage(int t, int c)
    {
        // save position
        final int prevT = getPositionT();
        final int prevC = getPositionC();

        // set wanted position (needed for correct overlay drawing)
        // we have to fire events else some stuff can miss the change
        setPositionT(t);
        setPositionC(c);
        try
        {
            final vtkRenderWindow renderWindow = renderer.GetRenderWindow();
            final int[] size = renderWindow.GetSize();
            final int w = size[0];
            final int h = size[1];
            final vtkUnsignedCharArray array = new vtkUnsignedCharArray();

            // VTK need this to be called in the EDT :-(
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    // force image rebuild
                    sequenceVolume.rebuildImageData();
                    // render
                    panel3D.paint(panel3D.getGraphics());

                    // NOTE: in vtk the [0,0] pixel is bottom left, so a vertical flip is required
                    // NOTE: GetRGBACharPixelData gives problematic results depending on the
                    // platform
                    // (see comment about alpha and platform-dependence in the doc for
                    // vtkWindowToImageFilter)
                    // Since the canvas is opaque, simply use GetPixelData.
                    renderWindow.GetPixelData(0, 0, w - 1, h - 1, 1, array);
                }
            });

            // convert the vtk array into a IcyBufferedImage
            final byte[] inData = array.GetJavaArray();
            final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            final int[] outData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            int inOffset = 0;
            for (int y = h - 1; y >= 0; y--)
            {
                int outOffset = y * w;

                for (int x = 0; x < w; x++)
                {
                    final int r = TypeUtil.unsign(inData[inOffset++]);
                    final int g = TypeUtil.unsign(inData[inOffset++]);
                    final int b = TypeUtil.unsign(inData[inOffset++]);

                    outData[outOffset++] = (r << 16) | (g << 8) | (b << 0);
                }
            }

            return image;
        }
        finally
        {
            // restore position
            setPositionT(prevT);
            setPositionC(prevC);
        }
    }

    @Override
    public BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView)
    {
        if (z != -1)
            throw new UnsupportedOperationException(
                    "Error: getRenderedImage(..) with z != -1 not supported on Canvas3D.");
        if (!canvasView)
            System.out.println("Warning: getRenderedImage(..) with canvasView = false not supported on Canvas3D.");

        return getRenderedImage(t, c);
    }

    @Override
    public void shutDown()
    {
        super.shutDown();

        // save settings
        preferences.putInt(ID_BGCOLOR, getBackgroundColor().getRGB());
        preferences.putBoolean(ID_BOUNDINGBOX, isBoundingBoxVisible());
        preferences.putBoolean(ID_BOUNDINGBOX_RULES, isBoundingBoxRulesVisible());
        preferences.putBoolean(ID_AXIS, isAxisVisible());
        preferences.putInt(ID_MAPPER, getVolumeMapperType().ordinal());
        preferences.putInt(ID_BLENDING, getVolumeBlendingMode().ordinal());
        preferences.putInt(ID_INTERPOLATION, getVolumeInterpolation());
        preferences.putBoolean(ID_SHADING, isVolumeShadingEnable());
        preferences.putDouble(ID_AMBIENT, getVolumeAmbient());
        preferences.putDouble(ID_DIFFUSE, getVolumeDiffuse());
        preferences.putDouble(ID_SPECULAR, getVolumeSpecularIntensity());
        preferences.putDouble(ID_SPECULAR_POWER, getVolumeSpecularPower());

        // restore colormap
        restoreOpacity(lutSave, getLut());
        // restoreColormap(getLut());

        // processor.shutdownAndWait();

        // AWTMultiCaster of vtkPanel keep reference of this frame so
        // we have to release as most stuff we can
        removeAll();
        panel.removeAll();

        renderer.Delete();
        sequenceVolume.release();
        activeCam.Delete();

        renderer = null;
        sequenceVolume = null;
        activeCam = null;

        panel3D = null;
        panel = null;
    }

    @Override
    public void changed(IcyCanvasEvent event)
    {
        super.changed(event);

        // avoid useless process during canvas initialization
        if (!initialized)
            return;

        if (event.getType() == IcyCanvasEventType.POSITION_CHANGED)
        {
            switch (event.getDim())
            {
                case C:
                    sequenceVolume.setPositionC(getPositionC());
                    // refresh
                    refresh();
                    break;

                case T:
                    sequenceVolume.setPositionT(getPositionT());
                    // refresh
                    refresh();
                    break;

                case Z:
                    // shouldn't happen
                    break;
            }
        }
    }

    @Override
    protected void lutChanged(int component)
    {
        super.lutChanged(component);

        // avoid useless process during canvas initialization
        if (!initialized)
            return;

        final LUT lut = getLut();
        final int posC = getPositionC();

        // refresh color properties for specified component
        if (posC == -1)
            sequenceVolume.setLUT(lut);
        else
            sequenceVolume.setLUT(lut.getLutChannel(posC), component);

        // refresh image
        refresh();
    }

    @Override
    protected void sequenceOverlayChanged(Overlay overlay, SequenceEventType type)
    {
        super.sequenceOverlayChanged(overlay, type);

        if (!initialized)
            return;

        // refresh
        refresh();
    }

    @Override
    protected void layerChanged(CanvasLayerEvent event)
    {
        super.layerChanged(event);

        if (!initialized)
            return;

        // layer visibility property modified ?
        if ((event.getType() == LayersEventType.CHANGED) && Layer.isPaintProperty(event.getProperty()))
        {
            // TODO: refresh actor properties from layers properties (alpha and visible)
            // refresh();
        }
    }

    @Override
    protected void layerAdded(Layer layer)
    {
        super.layerAdded(layer);

        addLayerActors(layer);
    }

    @Override
    protected void layerRemoved(Layer layer)
    {
        super.layerRemoved(layer);

        removeLayerActors(layer);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String propertyName = evt.getPropertyName();

        if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_AMBIENT))
            sequenceVolume.setAmbient(getVolumeAmbient());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_AXIS))
            ;
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_BG_COLOR))
            renderer.SetBackground(Array1DUtil.floatArrayToDoubleArray(getBackgroundColor().getColorComponents(null)));
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_BOUNDINGBOX))
            ;
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_BOUNDINGBOXGRID))
            ;
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_BOUNDINGBOXRULER))
            ;
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_DIFFUSE))
            sequenceVolume.setDiffuse(getVolumeAmbient());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_INTERPOLATION))
            sequenceVolume.setInterpolationMode(getVolumeInterpolation());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_MAPPER))
        {
            sequenceVolume.setVolumeMapperType(getVolumeMapperType());
            // FIXME: this line actually make VTK to crash
            // mapper not supported ? --> switch back to default one
//            if (!sequenceVolume.isMapperSupported(renderer))
//                sequenceVolume.setVolumeMapperType(VtkVolumeMapperType.RAYCAST_CPU_FIXEDPOINT);
            // blending mode can change when mapper changed
            setVolumeBlendingMode(sequenceVolume.getBlendingMode());
        }
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_BLENDING))
            sequenceVolume.setBlendingMode(getVolumeBlendingMode());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_SAMPLE))
            sequenceVolume.setSampleResolution(getVolumeSample());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_SHADING))
            sequenceVolume.setShade(isVolumeShadingEnable());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_SPECULAR_INTENSITY))
            sequenceVolume.setSpecular(getVolumeSpecularIntensity());
        else if (StringUtil.equals(propertyName, VtkSettingPanel.PROPERTY_SPECULAR_POWER))
            sequenceVolume.setSpecularPower(getVolumeSpecularPower());
    }

    private class CustomVtkPanel extends IcyVtkPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7399887230624608711L;

        public CustomVtkPanel()
        {
            super();

            // key events should be forwarded from the viewer
            removeKeyListener(this);
        }

        @Override
        public void paint(Graphics g)
        {
            // call paint on overlays first
            if (isLayersVisible())
            {
                final List<Layer> layers = getLayers(true);
                final Layer imageLayer = getImageLayer();
                final Sequence seq = getSequence();

                // call paint in inverse order to have first overlay "at top"
                for (int i = layers.size() - 1; i >= 0; i--)
                {
                    final Layer layer = layers.get(i);

                    // don't call paint on the image layer
                    if (layer != imageLayer)
                        paintLayer(seq, layer);
                }
            }

            // then do 3D rendering
            super.paint(g);
        }

        /**
         * Draw specified image layer and others layers on specified {@link Graphics2D} object.
         */
        void paintLayer(Sequence seq, Layer layer)
        {
            if (layer.isVisible())
                layer.getOverlay().paint(null, seq, VtkCanvas.this);
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseEntered(e, null);

            super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseExited(e, null);

            super.mouseExited(e);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseClick(e, null);

            super.mouseClicked(e);
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseMove(e, null);

            super.mouseMoved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseDrag(e, null);

            super.mouseDragged(e);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mousePressed(e, null);

            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseReleased(e, null);

            super.mouseReleased(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseWheelMoved(e, null);

            super.mouseWheelMoved(e);
        }
    }
}
