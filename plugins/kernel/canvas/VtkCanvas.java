package plugins.kernel.canvas;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JToolBar;

import icy.canvas.Canvas3D;
import icy.canvas.CanvasLayerEvent;
import icy.canvas.CanvasLayerEvent.LayersEventType;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.canvas.Layer;
import icy.common.exception.TooLargeArrayException;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.preferences.CanvasPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.point.Point3D;
import icy.util.ColorUtil;
import icy.util.EventUtil;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkImageVolume;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;
import icy.vtk.VtkImageVolume.VtkVolumeMapperType;
import icy.vtk.VtkUtil;
import plugins.kernel.canvas.VtkSettingPanel.SettingChangeListener;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkColorTransferFunction;
import vtk.vtkCubeAxesActor;
import vtk.vtkImageData;
import vtk.vtkInformation;
import vtk.vtkInformationIntegerKey;
import vtk.vtkLight;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkPicker;
import vtk.vtkPiecewiseFunction;
import vtk.vtkProp;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkTextActor;
import vtk.vtkTextProperty;

/**
 * VTK 3D canvas class.
 * 
 * @author Stephane
 */
@SuppressWarnings("deprecation")
public class VtkCanvas extends Canvas3D implements ActionListener, SettingChangeListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -1274251057822161271L;

    /**
     * icons
     */
    public static final Image ICON_AXES3D = ResourceUtil.getAlphaIconAsImage("axes3d.png");
    public static final Image ICON_BOUNDINGBOX = ResourceUtil.getAlphaIconAsImage("bbox.png");
    public static final Image ICON_GRID = ResourceUtil.getAlphaIconAsImage("3x3_grid.png");
    public static final Image ICON_RULER = ResourceUtil.getAlphaIconAsImage("ruler.png");
    public static final Image ICON_RULERLABEL = ResourceUtil.getAlphaIconAsImage("ruler_label.png");
    public static final Image ICON_TARGET = ResourceUtil.getAlphaIconAsImage("target.png");

    /**
     * properties
     */
    public static final String PROPERTY_AXES = "axis";
    public static final String PROPERTY_BOUNDINGBOX = "boundingBox";
    public static final String PROPERTY_BOUNDINGBOX_GRID = "boundingBoxGrid";
    public static final String PROPERTY_BOUNDINGBOX_RULES = "boundingBoxRules";
    public static final String PROPERTY_BOUNDINGBOX_LABELS = "boundingBoxLabels";
    public static final String PROPERTY_LUT = "lut";
    public static final String PROPERTY_DATA = "data";
    public static final String PROPERTY_SCALE = "scale";
    public static final String PROPERTY_BOUNDS = "bounds";

    /**
     * Used for outline visibility information in vtkActor
     */

    public static final vtkInformationIntegerKey visibilityKey = new vtkInformationIntegerKey().MakeKey("Visibility",
            "Property");
    /**
     * preferences id
     */
    protected static final String PREF_ID = "vtkCanvas";

    /**
     * id
     */
    protected static final String ID_BOUNDINGBOX = PROPERTY_BOUNDINGBOX;
    protected static final String ID_BOUNDINGBOX_GRID = PROPERTY_BOUNDINGBOX_GRID;
    protected static final String ID_BOUNDINGBOX_RULES = PROPERTY_BOUNDINGBOX_RULES;
    protected static final String ID_BOUNDINGBOX_LABELS = PROPERTY_BOUNDINGBOX_LABELS;
    // protected static final String ID_PICKONMOUSEMOVE = "pickOnMouseMove";
    protected static final String ID_AXES = PROPERTY_AXES;
    protected static final String ID_SHADING = VtkSettingPanel.PROPERTY_SHADING;
    protected static final String ID_BGCOLOR = VtkSettingPanel.PROPERTY_BG_COLOR;
    protected static final String ID_MAPPER = VtkSettingPanel.PROPERTY_MAPPER;
    protected static final String ID_SAMPLE = VtkSettingPanel.PROPERTY_SAMPLE;
    protected static final String ID_BLENDING = VtkSettingPanel.PROPERTY_BLENDING;
    protected static final String ID_INTERPOLATION = VtkSettingPanel.PROPERTY_INTERPOLATION;
    protected static final String ID_AMBIENT = VtkSettingPanel.PROPERTY_AMBIENT;
    protected static final String ID_DIFFUSE = VtkSettingPanel.PROPERTY_DIFFUSE;
    protected static final String ID_SPECULAR = VtkSettingPanel.PROPERTY_SPECULAR;

    /**
     * basic vtk objects
     */
    protected vtkRenderer renderer;
    protected vtkRenderWindow renderWindow;
    protected vtkCamera camera;
    // protected vtkAxesActor axes;
    protected vtkCubeAxesActor boundingBox;
    protected vtkCubeAxesActor rulerBox;
    protected vtkTextActor textInfo;
    protected vtkTextProperty textProperty;
    // protected vtkOrientationMarkerWidget widget;
    protected vtkProp pickedObject;

    /**
     * volume data
     */
    protected VtkImageVolume imageVolume;

    /**
     * GUI
     */
    protected VtkSettingPanel settingPanel;
    protected CustomVtkPanel panel3D;
    protected IcyToggleButton axesButton;
    protected IcyToggleButton boundingBoxButton;
    protected IcyToggleButton gridButton;
    protected IcyToggleButton rulerButton;
    protected IcyToggleButton rulerLabelButton;
    // protected IcyToggleButton pickOnMouseMoveButton;

    /**
     * internals
     */
    protected PropertiesUpdater propertiesUpdater;
    protected VtkOverlayUpdater overlayUpdater;
    protected XMLPreferences preferences;
    protected final EDTTask<Object> edtTask;
    protected boolean initialized;

    public VtkCanvas(Viewer viewer)
    {
        super(viewer);

        initialized = false;

        // more than 4 channels ? --> not supported by VTK
        if (getImageSizeC() > 4)
            throw new UnsupportedOperationException(
                    "VTK does not support image with more than 4 channels !\nYou should remove some channels in your image.");

        // multi channel view
        posC = -1;
        // adjust LUT alpha level for 3D view
        lut.setAlphaToLinear3D();

        pickedObject = null;

        // create the properties and the VTK overlay updater processors
        propertiesUpdater = new PropertiesUpdater();
        overlayUpdater = new VtkOverlayUpdater();

        preferences = CanvasPreferences.getPreferences().node(PREF_ID);

        settingPanel = new VtkSettingPanel();
        panel = settingPanel;

        // initialize VTK components & main GUI
        panel3D = new CustomVtkPanel();
        panel3D.addKeyListener(this);
        // set 3D view in center
        add(panel3D, BorderLayout.CENTER);

        // update nav bar & mouse infos
        mouseInfPanel.setVisible(false);
        updateZNav();
        updateTNav();

        // create toolbar buttons
        axesButton = new IcyToggleButton(new IcyIcon(ICON_AXES3D));
        axesButton.setFocusable(false);
        axesButton.setToolTipText("Display 3D axis");
        boundingBoxButton = new IcyToggleButton(new IcyIcon(ICON_BOUNDINGBOX));
        boundingBoxButton.setFocusable(false);
        boundingBoxButton.setToolTipText("Display bounding box");
        gridButton = new IcyToggleButton(new IcyIcon(ICON_GRID));
        gridButton.setFocusable(false);
        gridButton.setToolTipText("Display grid");
        rulerButton = new IcyToggleButton(new IcyIcon(ICON_RULER));
        rulerButton.setFocusable(false);
        rulerButton.setToolTipText("Display rules");
        rulerLabelButton = new IcyToggleButton(new IcyIcon(ICON_RULERLABEL));
        rulerLabelButton.setFocusable(false);
        rulerLabelButton.setToolTipText("Display rules label");
        // pickOnMouseMoveButton = new IcyToggleButton(new IcyIcon(ICON_TARGET));
        // pickOnMouseMoveButton.setFocusable(false);
        // pickOnMouseMoveButton.setToolTipText("Enabled object focus on mouse over (slow)");

        // set fast rendering during initialization
        panel3D.setCoarseRendering(1000);

        renderer = panel3D.getRenderer();
        renderWindow = panel3D.getRenderWindow();
        camera = renderer.GetActiveCamera();

        // initialize text info actor (need to be done before the first getImageData() call !
        textInfo = new vtkTextActor();
        textInfo.SetInput("Not enough memory to display this 3D image !");
        textInfo.SetPosition(10, 10);
        // not visible by default
        textInfo.SetVisibility(0);

        // change text properties
        textProperty = textInfo.GetTextProperty();
        textProperty.SetFontFamilyToArial();

        // rebuild volume image
        updateImageData(getImageData());

        final Sequence seq = getSequence();
        // setup volume scaling
        if (seq != null)
            imageVolume.setScale(seq.getPixelSizeX(), seq.getPixelSizeY(), seq.getPixelSizeZ());
        // setup volume LUT
        imageVolume.setLUT(getLut());

        // initialize axe
        // axes = new vtkAxesActor();
        // widget = new vtkOrientationMarkerWidget();
        // widget.SetOrientationMarker(axes);
        // widget.SetInteractor(interactor);
        // widget.SetViewport(0, 0, 0.3, 0.3);
        // widget.SetEnabled(1);

        // initialize bounding box
        boundingBox = new vtkCubeAxesActor();
        boundingBox.SetBounds(imageVolume.getVolume().GetBounds());
        boundingBox.SetCamera(camera);
        // set bounding box labels properties
        boundingBox.SetFlyModeToStaticEdges();
        boundingBox.SetUseBounds(true);
        boundingBox.XAxisLabelVisibilityOff();
        boundingBox.XAxisMinorTickVisibilityOff();
        boundingBox.XAxisTickVisibilityOff();
        boundingBox.YAxisLabelVisibilityOff();
        boundingBox.YAxisMinorTickVisibilityOff();
        boundingBox.YAxisTickVisibilityOff();
        boundingBox.ZAxisLabelVisibilityOff();
        boundingBox.ZAxisMinorTickVisibilityOff();
        boundingBox.ZAxisTickVisibilityOff();

        // initialize rules and box axis
        rulerBox = new vtkCubeAxesActor();
        rulerBox.SetBounds(imageVolume.getVolume().GetBounds());
        rulerBox.SetCamera(camera);

        // set bounding box labels properties
        rulerBox.SetXUnits("micro meter");
        rulerBox.GetTitleTextProperty(0).SetColor(1.0, 0.0, 0.0);
        rulerBox.GetLabelTextProperty(0).SetColor(1.0, 0.0, 0.0);
        rulerBox.GetXAxesGridlinesProperty().SetColor(1.0, 0.0, 0.0);
        rulerBox.GetXAxesGridpolysProperty().SetColor(1.0, 0.0, 0.0);
        rulerBox.GetXAxesInnerGridlinesProperty().SetColor(1.0, 0.0, 0.0);

        rulerBox.SetYUnits("micro meter");
        rulerBox.GetTitleTextProperty(1).SetColor(0.0, 1.0, 0.0);
        rulerBox.GetLabelTextProperty(1).SetColor(0.0, 1.0, 0.0);
        rulerBox.GetYAxesGridlinesProperty().SetColor(0.0, 1.0, 0.0);
        rulerBox.GetYAxesGridpolysProperty().SetColor(0.0, 1.0, 0.0);
        rulerBox.GetYAxesInnerGridlinesProperty().SetColor(0.0, 1.0, 0.0);

        rulerBox.SetZUnits("micro meter");
        rulerBox.GetTitleTextProperty(2).SetColor(0.0, 0.0, 1.0);
        rulerBox.GetLabelTextProperty(2).SetColor(0.0, 0.0, 1.0);
        rulerBox.GetZAxesGridlinesProperty().SetColor(0.0, 0.0, 1.0);
        rulerBox.GetZAxesGridpolysProperty().SetColor(0.0, 0.0, 1.0);
        rulerBox.GetZAxesInnerGridlinesProperty().SetColor(0.0, 0.0, 1.0);

        rulerBox.XAxisVisibilityOff();
        rulerBox.YAxisVisibilityOff();
        rulerBox.ZAxisVisibilityOff();

        rulerBox.SetGridLineLocation(VtkUtil.VTK_GRID_LINES_FURTHEST);
        rulerBox.SetFlyModeToOuterEdges();
        rulerBox.SetUseBounds(true);

        // restore settings
        settingPanel.setBackgroundColor(new Color(preferences.getInt(ID_BGCOLOR, 0x000000)));
        settingPanel.setVolumeBlendingMode(
                VtkVolumeBlendType.values()[preferences.getInt(ID_BLENDING, VtkVolumeBlendType.COMPOSITE.ordinal())]);

        // volume mapper
        settingPanel.setGPURendering(preferences.getInt(ID_MAPPER, 0) != 0);
        settingPanel.setVolumeInterpolation(preferences.getInt(ID_INTERPOLATION, VtkUtil.VTK_LINEAR_INTERPOLATION));
        settingPanel.setVolumeSample(preferences.getInt(ID_SAMPLE, 0));
        settingPanel.setVolumeAmbient(preferences.getDouble(ID_AMBIENT, 0.2d));
        settingPanel.setVolumeDiffuse(preferences.getDouble(ID_DIFFUSE, 0.4d));
        settingPanel.setVolumeSpecular(preferences.getDouble(ID_SPECULAR, 0.4d));
        settingPanel.setVolumeShading(preferences.getBoolean(ID_SHADING, false));
        axesButton.setSelected(preferences.getBoolean(ID_AXES, true));
        boundingBoxButton.setSelected(preferences.getBoolean(ID_BOUNDINGBOX, true));
        gridButton.setSelected(preferences.getBoolean(ID_BOUNDINGBOX_GRID, true));
        rulerButton.setSelected(preferences.getBoolean(ID_BOUNDINGBOX_RULES, false));
        rulerLabelButton.setSelected(preferences.getBoolean(ID_BOUNDINGBOX_LABELS, false));
        // pickOnMouseMoveButton.setSelected(preferences.getBoolean(ID_PICKONMOUSEMOVE, false));
        // always false by default (preferable)
        // pickOnMouseMoveButton.setSelected(false);

        // apply restored settings
        setBackgroundColorInternal(settingPanel.getBackgroundColor());
        imageVolume.setBlendingMode(settingPanel.getVolumeBlendingMode());
        imageVolume.setGPURendering(settingPanel.getGPURendering());
        // mapper may change blending mode
        settingPanel.setVolumeBlendingMode(imageVolume.getBlendingMode());
        imageVolume.setInterpolationMode(settingPanel.getVolumeInterpolation());
        imageVolume.setSampleResolution(settingPanel.getVolumeSample());
        imageVolume.setAmbient(settingPanel.getVolumeAmbient());
        imageVolume.setDiffuse(settingPanel.getVolumeDiffuse());
        imageVolume.setSpecular(settingPanel.getVolumeSpecular());
        imageVolume.setShade(settingPanel.getVolumeShading());
        // axes.SetVisibility(axesButton.isSelected() ? 1 : 0);
        boundingBox.SetVisibility(boundingBoxButton.isSelected() ? 1 : 0);
        rulerBox.SetDrawXGridlines(gridButton.isSelected() ? 1 : 0);
        rulerBox.SetDrawYGridlines(gridButton.isSelected() ? 1 : 0);
        rulerBox.SetDrawZGridlines(gridButton.isSelected() ? 1 : 0);
        rulerBox.SetXAxisTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetXAxisMinorTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetYAxisTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetYAxisMinorTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetZAxisTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetZAxisMinorTickVisibility(rulerButton.isSelected() ? 1 : 0);
        rulerBox.SetXAxisLabelVisibility(rulerLabelButton.isSelected() ? 1 : 0);
        rulerBox.SetYAxisLabelVisibility(rulerLabelButton.isSelected() ? 1 : 0);
        rulerBox.SetZAxisLabelVisibility(rulerLabelButton.isSelected() ? 1 : 0);
        // setPickOnMouseMove(pickOnMouseMoveButton.isSelected());

        // add volume to renderer
        renderer.AddVolume(imageVolume.getVolume());
        // add bounding box & ruler
        renderer.AddViewProp(boundingBox);
        renderer.AddViewProp(rulerBox);
        renderer.AddViewProp(textInfo);

        // reset camera
        resetCamera();
        // apply lut depending channel configuration
        updateLut();

        // we can now listen for setting changes
        settingPanel.addSettingChangeListener(this);
        axesButton.addActionListener(this);
        boundingBoxButton.addActionListener(this);
        gridButton.addActionListener(this);
        rulerButton.addActionListener(this);
        rulerLabelButton.addActionListener(this);
        // pickOnMouseMoveButton.addActionListener(this);

        // create EDTTask object
        edtTask = new EDTTask<Object>();
        // start the properties and VTK overlay updater processors
        propertiesUpdater.start();
        overlayUpdater.start();

        // initialized !
        initialized = true;

        // add layers actors
        overlayUpdater.addProps(VtkUtil.getLayersProps(getLayers(false)));
    }

    @Override
    public void shutDown()
    {
        final long st = System.currentTimeMillis();
        // wait for initialization to complete before shutdown (max 5s)
        while (((System.currentTimeMillis() - st) < 5000L) && !initialized)
            ThreadUtil.sleep(1);

        propertiesUpdater.interrupt();
        try
        {
            // be sure there is no more processing here
            propertiesUpdater.join();
        }
        catch (InterruptedException e)
        {
            // can ignore safely
        }
        overlayUpdater.interrupt();
        try
        {
            // be sure there is no more processing here
            overlayUpdater.join();
        }
        catch (InterruptedException e)
        {
            // can ignore safely
        }

        // no more initialized (prevent extra useless processing)
        initialized = false;
        propertiesUpdater = null;
        overlayUpdater = null;

        // VTK stuff in EDT
        invokeOnEDTSilent(new Runnable()
        {
            @Override
            public void run()
            {
                renderer.RemoveAllViewProps();
                // renderer.Delete();
                // renderWindow.Delete();
                imageVolume.release();
                // widget.Delete();
                // axes.Delete();
                boundingBox.Delete();
                // camera.Delete();

                // dispose extra panel 3D stuff
                panel3D.disposeInternal();
            }
        });

        // AWTMultiCaster of vtkPanel keep reference of this frame so
        // we have to release as most stuff we can
        removeAll();
        panel.removeAll();

        renderer = null;
        renderWindow = null;
        imageVolume = null;
        // widget = null;
        // axes = null;
        boundingBox = null;
        camera = null;

        panel3D.removeKeyListener(this);
        panel3D = null;
        panel = null;

        // do parent shutdown now
        super.shutDown();

        // call VTK GC: better if we can avoid this !
        // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
    }

    /**
     * Returns initialized state of VtkCanvas
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    @Override
    public void customizeToolbar(JToolBar toolBar)
    {
        toolBar.addSeparator();
        toolBar.add(axesButton);
        toolBar.addSeparator();
        toolBar.add(boundingBoxButton);
        toolBar.add(gridButton);
        toolBar.add(rulerButton);
        toolBar.add(rulerLabelButton);
        // toolBar.addSeparator();
        // toolBar.add(pickOnMouseMoveButton);
    }

    @Override
    protected Overlay createImageOverlay()
    {
        return new VtkCanvasImageOverlay();
    }

    /**
     * Request exclusive access to VTK rendering.<br>
     * 
     * @deprecated Use <code>getVtkPanel().lock()</code> instead.
     */
    @Deprecated
    public void lock()
    {
        if (panel3D != null)
            panel3D.lock();
    }

    /**
     * Release exclusive access from VTK rendering.
     * 
     * @deprecated Use <code>getVtkPanel().unlock()</code> instead.
     */
    @Deprecated
    public void unlock()
    {
        if (panel3D != null)
            panel3D.unlock();
    }

    /**
     * @deprecated Use {@link #getCamera()} instead
     */
    @Deprecated
    public vtkCamera getActiveCam()
    {
        return getCamera();
    }

    /**
     * @return the VTK scene camera object
     */
    public vtkCamera getCamera()
    {
        return camera;
    }

    /**
     * @return the VTK default scene light object.<br>
     *         Can be <code>null</code> if render window is not yet initialized.
     */
    public vtkLight getLight()
    {
        return renderer.GetLights().GetNextItem();
    }

    /**
     * @return the VTK axes object
     */
    public vtkAxesActor getAxes()
    {
        return panel3D.getAxesActor();
    }

    /**
     * @return the VTK bounding box object
     */
    public vtkCubeAxesActor getBoundingBox()
    {
        return boundingBox;
    }

    /**
     * @return the VTK ruler box object
     */
    public vtkCubeAxesActor getRulerBox()
    {
        return rulerBox;
    }

    /**
     * @deprecated there is no more orientation widget because of the jogl bug with multiple viewport.
     */
    @Deprecated
    public vtkOrientationMarkerWidget getWidget()
    {
        return null;
        // return widget;
    }

    /**
     * @return the VTK image volume object
     */
    public VtkImageVolume getImageVolume()
    {
        return imageVolume;
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
        return boundingBoxButton.isSelected();
    }

    /**
     * Enable / disable volume bounding box display.
     */
    public void setBoundingBoxVisible(boolean value)
    {
        if (boundingBoxButton.isSelected() != value)
            boundingBoxButton.doClick();
    }

    /**
     * Returns <code>true</code> if the volume bounding box grid is visible.
     */
    public boolean isBoundingBoxGridVisible()
    {
        return gridButton.isSelected();
    }

    /**
     * Enable / disable volume bounding box grid display.
     */
    public void setBoundingBoxGridVisible(boolean value)
    {
        if (gridButton.isSelected() != value)
            gridButton.doClick();
    }

    /**
     * Returns <code>true</code> if the volume bounding box ruler are visible.
     */
    public boolean isBoundingBoxRulerVisible()
    {
        return rulerButton.isSelected();
    }

    /**
     * Enable / disable volume bounding box ruler display.
     */
    public void setBoundingBoxRulerVisible(boolean value)
    {
        if (rulerButton.isSelected() != value)
            rulerButton.doClick();
    }

    /**
     * Returns <code>true</code> if the volume bounding box ruler labels are visible.
     */
    public boolean isBoundingBoxRulerLabelsVisible()
    {
        return rulerLabelButton.isSelected();
    }

    /**
     * Enable / disable volume bounding box ruler labels display.
     */
    public void setBoundingBoxRulerLabelsVisible(boolean value)
    {
        if (rulerLabelButton.isSelected() != value)
            rulerLabelButton.doClick();
    }

    /**
     * @deprecated USe {@link #setBackgroundColorInternal(Color)}
     */
    @Deprecated
    public void setBoundingBoxColor(Color color)
    {
        setBackgroundColorInternal(color);
    }

    /**
     * Set background color (internal)
     */
    public void setBackgroundColorInternal(Color color)
    {
        renderer.SetBackground(Array1DUtil.floatArrayToDoubleArray(color.getColorComponents(null)));

        final Color oppositeColor;

        // adjust bounding box color
        if (ColorUtil.getLuminance(color) > 128)
            oppositeColor = Color.black;
        else
            oppositeColor = Color.white;

        final float[] comp = oppositeColor.getRGBColorComponents(null);

        final float r = comp[0];
        final float g = comp[0];
        final float b = comp[0];

        boundingBox.GetXAxesLinesProperty().SetColor(r, g, b);
        boundingBox.GetYAxesLinesProperty().SetColor(r, g, b);
        boundingBox.GetZAxesLinesProperty().SetColor(r, g, b);

        rulerBox.GetXAxesGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetXAxesGridpolysProperty().SetColor(r, g, b);
        rulerBox.GetXAxesInnerGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetXAxesLinesProperty().SetColor(r, g, b);

        rulerBox.GetYAxesGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetYAxesGridpolysProperty().SetColor(r, g, b);
        rulerBox.GetYAxesInnerGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetYAxesLinesProperty().SetColor(r, g, b);

        rulerBox.GetZAxesGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetZAxesGridpolysProperty().SetColor(r, g, b);
        rulerBox.GetZAxesInnerGridlinesProperty().SetColor(r, g, b);
        rulerBox.GetZAxesLinesProperty().SetColor(r, g, b);

        textProperty.SetColor(r, g, b);
    }

    /**
     * Returns <code>true</code> if the 3D axis are visible.
     */
    public boolean isAxisVisible()
    {
        return axesButton.isSelected();
    }

    /**
     * Enable / disable 3D axis display.
     */
    public void setAxisVisible(boolean value)
    {
        if (axesButton.isSelected() != value)
            axesButton.doClick();
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
        return settingPanel.getVolumeShading();
    }

    /**
     * @see VtkImageVolume#setShade(boolean)
     */
    public void setVolumeShadingEnable(boolean value)
    {
        settingPanel.setVolumeShading(value);
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
    public double getVolumeSpecular()
    {
        return settingPanel.getVolumeSpecular();
    }

    /**
     * @see VtkImageVolume#setSpecular(double)
     */
    public void setVolumeSpecular(double value)
    {
        settingPanel.setVolumeSpecular(value);
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
     * @see VtkImageVolume#getGPURendering()
     */
    public boolean getGPURendering()
    {
        return settingPanel.getGPURendering();
    }

    /**
     * @see VtkImageVolume#setGPURendering(boolean)
     */
    public void setGPURendering(boolean value)
    {
        settingPanel.setGPURendering(value);
    }

    /**
     * @deprecated Use {@link #getGPURendering()} instead
     */
    @Deprecated
    public VtkVolumeMapperType getVolumeMapperType()
    {
        if (getGPURendering())
            return VtkVolumeMapperType.RAYCAST_GPU_OPENGL;

        return VtkVolumeMapperType.RAYCAST_CPU_FIXEDPOINT;
    }

    /**
     * @deprecated Use {@link #setGPURendering(boolean)} instead
     */
    @Deprecated
    public void setVolumeMapperType(VtkVolumeMapperType value)
    {
        setGPURendering(VtkVolumeMapperType.RAYCAST_GPU_OPENGL.equals(value));
    }

    /**
     * @return visible state of the image volume object
     * @see VtkImageVolume#isVisible()
     */
    public boolean isVolumeVisible()
    {
        return imageVolume.isVisible();
    }

    /**
     * Sets the visible state of the image volume object
     * 
     * @see VtkImageVolume#setVisible(boolean)
     */
    public void setVolumeVisible(boolean value)
    {
        imageVolume.setVisible(value);
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
        if (panel3D != null)
            panel3D.repaint();
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

    protected void resetCamera()
    {
        camera.SetViewUp(0, -1, 0);
        renderer.ResetCamera();
        camera.Elevation(200);
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

    // /**
    // * Returns channel position based on enabled channel in LUT
    // */
    // protected int getChannelPos()
    // {
    // final LUT lut = getLut();
    // int result = -1;
    //
    // for (int c = 0; c < lut.getNumChannel(); c++)
    // {
    // final LUTChannel lutChannel = lut.getLutChannel(c);
    //
    // if (lutChannel.isEnabled())
    // {
    // if (result == -1)
    // result = c;
    // else
    // return -1;
    // }
    // }
    //
    // return result;
    // }

    /**
     * @deprecated Always enabled now (always return <code>true</code>)
     */
    @Deprecated
    public boolean getPickOnMouseMove()
    {
        return true;
        // return pickOnMouseMoveButton.isSelected();
    }

    /**
     * @deprecated Always enable now
     */
    @Deprecated
    public void setPickOnMouseMove(boolean value)
    {
        // if (pickOnMouseMoveButton.isSelected() != value)
        // pickOnMouseMoveButton.doClick();
    }

    /**
     * Returns the picked object on the last mouse move/drag event (can be <code>null</code> if no object was picked).
     * 
     * @see #pickProp(int, int)
     */
    public vtkProp getPickedObject()
    {
        return pickedObject;
    }

    /**
     * @deprecated use {@link #pickProp(int, int)} instead.
     */
    @Deprecated
    public vtkActor pick(int x, int y)
    {
        return (vtkActor) panel3D.pick(x, y);
    }

    /**
     * Pick object at specified position and return it.
     *
     * @see #getPickedObject()
     * @see icy.vtk.IcyVtkPanel#pick(int, int)
     */
    public vtkProp pickProp(int x, int y)
    {
        return panel3D.pick(x, y);
    }

    /**
     * Return reached z world position (normalized) for specified display position
     */
    public double getWorldZ(int x, int y)
    {
        final vtkRenderer r = getRenderer();
        final vtkRenderWindow rw = getRenderWindow();

        if ((r == null) || (rw == null))
            return 0d;

        // need to revert Y axis
        return r.GetZ(x, rw.GetSize()[1] - y);
    }

    public double getWorldZ(Point pt)
    {
        return getWorldZ(pt.x, pt.y);
    }

    /**
     * Convert world coordinates to display coordinates
     */
    public Point3D worldToDisplay(Point3D pt)
    {
        if (pt == null)
            return new Point3D.Double();

        return worldToDisplay(pt.getX(), pt.getY(), pt.getZ());
    }

    /**
     * Convert world coordinates to display coordinates
     */
    public Point3D worldToDisplay(double x, double y, double z)
    {
        final vtkRenderer r = getRenderer();
        final vtkRenderWindow rw = getRenderWindow();

        if ((r == null) || (rw == null))
            return new Point3D.Double();

        r.SetWorldPoint(x, y, z, 1d);
        r.WorldToDisplay();
        final Point3D result = new Point3D.Double(r.GetDisplayPoint());

        // need to revert Y axis
        result.setY(rw.GetSize()[1] - result.getY());

        return result;
    }

    /**
     * Convert display coordinates to world coordinates.
     */
    public Point3D displayToWorld(Point pt)
    {
        if (pt == null)
            return new Point3D.Double();

        return displayToWorld(pt.x, pt.y);
    }

    /**
     * Convert display coordinates to world coordinates.<br>
     */
    public Point3D displayToWorld(int x, int y)
    {
        // get camera focal point
        final double[] fp = camera.GetFocalPoint();
        // transform it to display position (with Z info)
        final Point3D displayFP = worldToDisplay(fp[0], fp[1], fp[2]);
        // keep the Z info from focal point
        return displayToWorld(x, y, displayFP.getZ());
        // return displayToWorld(x, y, getWorldZ(x, y));
    }

    /**
     * Convert display coordinates to world coordinates.<br>
     * Default value for Z should be 0d
     */
    public Point3D displayToWorld(Point pt, double z)
    {
        if (pt == null)
            return new Point3D.Double();

        return displayToWorld(pt.getX(), pt.getY(), z);
    }

    /**
     * Convert display coordinates to world coordinates.<br>
     * Default value for Z is 0d
     */
    public Point3D displayToWorld(double x, double y, double z)
    {
        final vtkRenderer r = getRenderer();
        final vtkRenderWindow rw = getRenderWindow();

        if ((r == null) || (rw == null))
            return new Point3D.Double();

        // need to revert Y axis
        r.SetDisplayPoint(x, rw.GetSize()[1] - y, z);
        r.DisplayToWorld();
        final double[] result = r.GetWorldPoint();

        // final vtkPicker picker = getPicker();
        // pickProp((int)x, (int)y);
        // // picker.Pick(x, rw.GetSize()[1] - y, 0, r);
        // double[] pos = picker.GetPickPosition();
        //
        // System.out.println("displayToWorld(" + x + ", " + y + ", " + z + "):");
        // System.out.println(String.format("%.5g, %.5g, %.5g", result[0], result[1], result[2]));
        // System.out.println(String.format("from Pick: %.5g, %.5g, %.5g", pos[0], pos[1], pos[2]));

        // normalize
        if (result[3] != 0d)
        {
            result[0] /= result[3];
            result[1] /= result[3];
            result[2] /= result[3];
        }
        else
        {
            result[0] = 0d;
            result[1] = 0d;
            result[2] = 0d;
        }

        return new Point3D.Double(result[0], result[1], result[2]);
    }

    @Override
    public Point imageToCanvas(double x, double y, double z)
    {
        final double[] scaling = getVolumeScale();
        final Point3D result = worldToDisplay(x * scaling[0], y * scaling[1], z * scaling[2]);

        // System.out.println("imageToCanvas(" + x + ", " + y + ", " + z + "): " + result);

        // ignore Z coordinate
        return new Point((int) result.getX(), (int) result.getY());
    }

    @Override
    public Point3D.Double canvasToImage(int x, int y)
    {
        final double[] scaling = getVolumeScale();

        // check scaling does not contains any 0
        for (double d : scaling)
        {
            if (d == 0d)
                return new Point3D.Double();
        }

        // get image position in 3D
        Point3D result = displayToWorld(x, y);

        // get the view axis
        final double[] directionOfProjection = getCamera().GetDirectionOfProjection();
        final double dirX = Math.abs(directionOfProjection[0]);
        final double dirY = Math.abs(directionOfProjection[1]);
        final double dirZ = Math.abs(directionOfProjection[2]);

        // we always want to have 2D coordinates cancel position which is not "visible" axis
        if (dirX > dirY)
        {
            if (dirX > dirZ)
                result.setX(Double.NaN);
            else
                result.setZ(Double.NaN);
        }
        else
        {
            if (dirY > dirZ)
                result.setY(Double.NaN);
            else
                result.setZ(Double.NaN);
        }

        result = new Point3D.Double(result.getX() / scaling[0], result.getY() / scaling[1], result.getZ() / scaling[2]);

        // System.out.println("canvasToImage(" + x + ", " + y + "): "
        // + String.format("%.5g, %.5g, %.5g", result.getX(), result.getY(), result.getZ()));

        return (Point3D.Double) result;
    }

    /**
     * @deprecated Use {@link VtkUtil#getLayerProps(Layer)} instead.
     */
    @Deprecated
    protected vtkProp[] getLayerActors(Layer layer)
    {
        return VtkUtil.getLayerProps(layer);
    }

    protected void addLayerActors(Layer layer)
    {
        // not yet (or no more) initialized
        if (overlayUpdater == null)
            return;

        overlayUpdater.addProps(VtkUtil.getLayerProps(layer));
    }

    protected void removeLayerActors(Layer layer)
    {
        // not yet (or no more) initialized
        if (overlayUpdater == null)
            return;

        overlayUpdater.removeProps(VtkUtil.getLayerProps(layer));
    }

    protected void addLayersActors(List<Layer> layers)
    {
        // not yet (or no more) initialized
        if (overlayUpdater == null)
            return;

        overlayUpdater.addProps(VtkUtil.getLayersProps(layers));
    }

    protected void updateBoundingBoxSize()
    {
        final double[] bounds = imageVolume.getVolume().GetBounds();

        boundingBox.SetBounds(bounds);
        rulerBox.SetBounds(bounds);
    }

    /**
     * Build and get image data
     */
    protected vtkImageData getImageData()
    {
        try
        {
            return VtkUtil.getImageData(getSequence(), getPositionT(), getPositionC());
        }
        catch (TooLargeArrayException e)
        {
            // cannot allocate a such large contiguous array
            return null;
        }
        catch (OutOfMemoryError e)
        {
            // just not enough memory
            return null;
        }
    }

    /**
     * update image data
     */
    protected void updateImageData(vtkImageData data)
    {
        if (data != null)
        {
            imageVolume.setVolumeData(data);
            imageVolume.getVolume().SetVisibility(getImageLayer().isVisible() ? 1 : 0);

            if (textInfo != null)
                textInfo.SetVisibility(0);
        }
        else
        {
            // no data --> hide volume
            imageVolume.getVolume().SetVisibility(0);

            if (textInfo != null)
            {
                final Sequence seq = getSequence();

                // we have an image --> not enough memory to display it (show message)
                if ((seq != null) && !seq.isEmpty())
                    textInfo.SetVisibility(1);
            }
        }
    }

    protected void updateLut()
    {
        final LUT lut = getLut();

        // update the whole LUT
        for (int c = 0; c < lut.getNumChannel(); c++)
            updateLut(lut.getLutChannel(c), c);
    }

    protected void updateLut(LUTChannel lutChannel, int channel)
    {
        final Sequence sequence = getSequence();
        if ((sequence == null) || sequence.isEmpty())
            return;

        final int ch = channel;
        final vtkColorTransferFunction colorMap = VtkUtil.getColorMap(lutChannel);
        final vtkPiecewiseFunction opacityMap = VtkUtil.getOpacityMap(lutChannel);

        imageVolume.setColorMap(colorMap, ch);
        imageVolume.setOpacityMap(opacityMap, ch);
    }

    @Override
    public Component getViewComponent()
    {
        return getVtkPanel();
    }

    public IcyVtkPanel getVtkPanel()
    {
        return panel3D;
    }

    /**
     * @deprecated Use {@link #getVtkPanel()}
     */
    @Deprecated
    @Override
    public IcyVtkPanel getPanel3D()
    {
        return getVtkPanel();
    }

    @Override
    public vtkRenderer getRenderer()
    {
        return renderer;
    }

    public vtkRenderWindow getRenderWindow()
    {
        return renderWindow;
    }

    /**
     * @see icy.vtk.IcyVtkPanel#getPicker()
     */
    public vtkPicker getPicker()
    {
        return panel3D.getPicker();
    }

    /**
     * Get scaling for image volume rendering
     */
    @Override
    public double[] getVolumeScale()
    {
        return imageVolume.getScale();
    }

    /**
     * Set scaling for image volume rendering
     */
    @Override
    public void setVolumeScale(double x, double y, double z)
    {
        propertyChange(PROPERTY_SCALE, new double[] {x, y, z});
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // send to overlays
        super.keyPressed(e);

        // forward to view
        panel3D.keyPressed(e);

        if (!e.isConsumed())
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_LEFT:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionT(Math.max(getPositionT() - 5, 0));
                    else
                        setPositionT(Math.max(getPositionT() - 1, 0));
                    e.consume();
                    break;

                case KeyEvent.VK_RIGHT:
                    if (EventUtil.isMenuControlDown(e, true))
                        setPositionT(getPositionT() + 5);
                    else
                        setPositionT(getPositionT() + 1);
                    e.consume();
                    break;

                case KeyEvent.VK_NUMPAD2:
                    if (EventUtil.isMenuControlDown(e, true))
                        panel3D.translateView(0, -50);
                    else
                        panel3D.translateView(0, -10);
                    refresh();
                    e.consume();
                    break;

                case KeyEvent.VK_NUMPAD4:
                    if (EventUtil.isMenuControlDown(e, true))
                        panel3D.translateView(-50, 0);
                    else
                        panel3D.translateView(-10, 0);
                    refresh();
                    e.consume();
                    break;

                case KeyEvent.VK_NUMPAD6:
                    if (EventUtil.isMenuControlDown(e, true))
                        panel3D.translateView(50, 0);
                    else
                        panel3D.translateView(10, 0);
                    refresh();
                    e.consume();
                    break;

                case KeyEvent.VK_NUMPAD8:
                    if (EventUtil.isMenuControlDown(e, true))
                        panel3D.translateView(0, 50);
                    else
                        panel3D.translateView(0, 10);
                    refresh();
                    e.consume();
                    break;
            }
        }
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
    protected void setPositionCInternal(int c)
    {
        // single channel mode is not possible here
        if (c != -1)
            return;

        super.setPositionCInternal(c);
    }

    @Override
    public double getScaleX()
    {
        final double dist = getCamera().GetDistance();
        // cannot compute scaling
        if (dist <= 0d)
            return 1d;

        final double imageSizeX = getImageSizeX();
        // FIXME: from where come that x2 factor
        final double result = (2 * imageSizeX * getVolumeScale()[0]) / dist;
        final double canvasImageRatio = getCanvasSizeX() / ((imageSizeX == 0d) ? 1d : imageSizeX);

        return result * canvasImageRatio;
    }

    @Override
    public double getScaleY()
    {
        final double dist = getCamera().GetDistance();
        // cannot compute scaling
        if (dist <= 0d)
            return 1d;

        final double imageSizeY = getImageSizeY();
        // FIXME: from where come that x2 factor
        final double result = (2 * imageSizeY * getVolumeScale()[1]) / dist;
        final double canvasImageRatio = getCanvasSizeY() / ((imageSizeY == 0d) ? 1d : imageSizeY);

        return result * canvasImageRatio;
    }

    @Override
    public void setMouseImagePosX(double value)
    {
        // just ignore NaN position (canvasToImage(..) can return NaN for specific dimension)
        if (!Double.isNaN(value))
            super.setMouseImagePosX(value);
    }

    @Override
    public void setMouseImagePosY(double value)
    {
        // just ignore NaN position (canvasToImage(..) can return NaN for specific dimension)
        if (!Double.isNaN(value))
            super.setMouseImagePosY(value);
    }

    @Override
    public void setMouseImagePosZ(double value)
    {
        // just ignore NaN position (canvasToImage(..) can return NaN for specific dimension)
        if (!Double.isNaN(value))
            super.setMouseImagePosZ(value);
    }

    @Override
    public BufferedImage getRenderedImage(int t, int c)
    {
        final CustomVtkPanel vp = panel3D;
        if (vp == null)
            return null;

        // save position
        final int prevT = getPositionT();
        final int prevC = getPositionC();

        // set wanted position (needed for correct overlay drawing)
        // we have to fire events else some stuff can miss the change
        setPositionT(t);
        setPositionC(c);
        try
        {
            final vtkImageData imageData = getImageData();

            // VTK need this to be called in the EDT
            invokeOnEDTSilent(new Runnable()
            {
                @Override
                public void run()
                {
                    // set image data
                    updateImageData(imageData);

                    // force fine rendering here
                    vp.setForceFineRendering(true);
                    try
                    {
                        // render now !
                        vp.paint(vp.getGraphics());
                    }
                    finally
                    {
                        vp.setForceFineRendering(false);
                    }
                }
            });

            try
            {
                final Robot robot = new Robot();
                final Rectangle bounds = vp.getBounds();
                // transform in screen coordinates
                bounds.setLocation(ComponentUtil.convertPointToScreen(bounds.getLocation(), vp));
                // do the capture
                return robot.createScreenCapture(bounds);
            }
            catch (AWTException e)
            {
                IcyExceptionHandler.showErrorMessage(e, true);
                return null;
            }

            // final int[] size = renderWindow.GetSize();
            // final int w = size[0];
            // final int h = size[1];
            // final vtkUnsignedCharArray array = new vtkUnsignedCharArray();
            // final vtkImageData imageData = getImageData();
            // final BufferedImage[] result = new BufferedImage[1];
            //
            // // VTK need this to be called in the EDT
            // invokeOnEDTSilent(new Runnable()
            // {
            // @Override
            // public void run()
            // {
            // // set image data
            // updateImageData(imageData);
            //
            // // force fine rendering here
            // panel3D.setForceFineRendering(true);
            // try
            // {
            // // render now !
            // panel3D.paint(panel3D.getGraphics());
            // }
            // finally
            // {
            // panel3D.setForceFineRendering(false);
            // }
            //
            // try
            // {
            // Robot r = new Robot();
            // result[0] = r.createScreenCapture(SwingUtilities.convertRectangle(panel3D, getBounds(), null));
            // }
            // catch (AWTException e)
            // {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            //
            // // NOTE: in vtk the [0,0] pixel is bottom left, so a vertical flip is required
            // // NOTE: GetRGBACharPixelData gives problematic results depending on the platform
            // // (see comment about alpha and platform-dependence in the doc for vtkWindowToImageFilter)
            // // Since the canvas is opaque, simply use GetPixelData.
            // renderWindow.GetPixelData(0, 0, w - 1, h - 1, 1, array);
            // }
            // });
            //
            // // convert the vtk array into a IcyBufferedImage
            // final byte[] inData = array.GetJavaArray();
            // final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            // final int[] outData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            //
            // int inOffset = 0;
            // for (int y = h - 1; y >= 0; y--)
            // {
            // int outOffset = y * w;
            //
            // for (int x = 0; x < w; x++)
            // {
            // final int r = TypeUtil.unsign(inData[inOffset++]);
            // final int g = TypeUtil.unsign(inData[inOffset++]);
            // final int b = TypeUtil.unsign(inData[inOffset++]);
            //
            // outData[outOffset++] = (r << 16) | (g << 8) | (b << 0);
            // }
            // }
            //
            // return image;
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

    protected void invokeOnEDT(Runnable task) throws InterruptedException
    {
        // in initialization --> just execute
        if (edtTask == null)
        {
            task.run();
            return;
        }

        edtTask.setTask(task);

        try
        {
            ThreadUtil.invokeNow(edtTask);
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        catch (Exception t)
        {
            // just ignore as this is async process
            System.out.println("[VTKCanvas] Warning:" + t);
        }
    }

    protected void invokeOnEDTSilent(Runnable task)
    {
        try
        {
            invokeOnEDT(task);
        }
        catch (InterruptedException e)
        {
            // just ignore
        }
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
                    propertyChange(PROPERTY_DATA, null);
                    // layers can change depending position
                    refresh();
                    break;

                case T:
                    propertyChange(PROPERTY_DATA, null);
                    // layers can change depending position
                    refresh();
                    break;

                case Z:
                    // shouldn't happen
                    break;
            }
        }
    }

    @Override
    protected void lutChanged(int channel)
    {
        super.lutChanged(channel);

        // avoid useless process during canvas initialization
        if (!initialized)
            return;

        propertyChange(PROPERTY_LUT, Integer.valueOf(channel));
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
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        super.sequenceDataChanged(image, type);

        // rebuild image data and bounds
        propertyChange(PROPERTY_DATA, null);
        propertyChange(PROPERTY_BOUNDS, null);
    }

    @Override
    protected void sequenceMetaChanged(String metadataName)
    {
        super.sequenceMetaChanged(metadataName);

        final Sequence sequence = getSequence();
        if ((sequence == null) || sequence.isEmpty())
            return;

        // need to set scale ?
        if (StringUtil.isEmpty(metadataName) || (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_X)
                || StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Y)
                || StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Z)))
        {
            setVolumeScale(sequence.getPixelSizeX(), sequence.getPixelSizeY(), sequence.getPixelSizeZ());
        }
    }

    @Override
    protected void layerChanged(CanvasLayerEvent event)
    {
        super.layerChanged(event);

        if (!initialized)
            return;

        if (event.getType() == LayersEventType.CHANGED)
        {
            final String propertyName = event.getProperty();

            // we ignore priority property here as we display in 3D
            if (propertyName.equals(Layer.PROPERTY_OPACITY) || propertyName.equals(Layer.PROPERTY_VISIBLE))
                propertyChange(PROPERTY_LAYERS_VISIBLE, event.getSource());
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
    protected void layersVisibleChanged()
    {
        propertyChange(PROPERTY_LAYERS_VISIBLE, null);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Object source = e.getSource();

        // translate button action to property change event
        if (source == axesButton)
            propertyChange(PROPERTY_AXES, Boolean.valueOf(axesButton.isSelected()));
        else if (source == boundingBoxButton)
            propertyChange(PROPERTY_BOUNDINGBOX, Boolean.valueOf(boundingBoxButton.isSelected()));
        else if (source == gridButton)
            propertyChange(PROPERTY_BOUNDINGBOX_GRID, Boolean.valueOf(gridButton.isSelected()));
        else if (source == rulerButton)
            propertyChange(PROPERTY_BOUNDINGBOX_RULES, Boolean.valueOf(rulerButton.isSelected()));
        else if (source == rulerLabelButton)
            propertyChange(PROPERTY_BOUNDINGBOX_LABELS, Boolean.valueOf(rulerLabelButton.isSelected()));
        // else if (source == pickOnMouseMoveButton)
        // preferences.putBoolean(ID_PICKONMOUSEMOVE, pickOnMouseMoveButton.isSelected());
    }

    protected void propertyChange(String name, Object value)
    {
        // we can ignore it in this case
        if (propertiesUpdater == null)
            return;

        final Property prop = new Property(name, value);

        propertiesUpdater.submit(prop);
    }

    /*
     * Called when one of the value in setting panel has changed
     */
    @Override
    public void settingChange(PropertyChangeEvent evt)
    {
        propertyChange(evt.getPropertyName(), evt.getNewValue());
    }

    protected class EDTTask<T> implements Callable<T>
    {
        protected Runnable task;

        public void setTask(Runnable task)
        {
            this.task = task;
        }

        @Override
        public T call() throws Exception
        {
            task.run();

            return null;
        }
    }

    protected class CustomVtkPanel extends IcyVtkPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7399887230624608711L;

        long lastRefreshTime;
        boolean forceFineRendering;

        public CustomVtkPanel()
        {
            super();

            lastRefreshTime = 0L;
            forceFineRendering = false;
            // key events should be forwarded from the viewer
            removeKeyListener(this);
        }

        public boolean getForceFineRendering()
        {
            return forceFineRendering;
        }

        public void setForceFineRendering(boolean value)
        {
            forceFineRendering = value;
        }

        /**
         * Update mouse cursor
         * 
         * @param b
         */
        protected void updateCursor(boolean consumedByCanvas)
        {
            // don't change custom cursor
            if (getCursor().getType() == Cursor.CUSTOM_CURSOR)
                return;

            // consumed by canvas --> return it to origin
            if (consumedByCanvas)
            {
                GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                return;
            }

            final Sequence seq = getSequence();

            if (seq != null)
            {
                final ROI overlappedRoi = seq.getFocusedROI();

                // overlapping an ROI ?
                if (overlappedRoi != null)
                {
                    final Layer layer = getLayer(overlappedRoi);

                    if ((layer != null) && layer.isVisible())
                    {
                        GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                        return;
                    }
                }

                final List<ROI> selectedRois = seq.getSelectedROIs();

                // search if we are overriding ROI control points
                for (ROI selectedRoi : selectedRois)
                {
                    final Layer layer = getLayer(selectedRoi);

                    if ((layer != null) && layer.isVisible() && selectedRoi.hasSelectedPoint())
                    {
                        GuiUtil.setCursor(this, Cursor.HAND_CURSOR);
                        return;
                    }
                }
            }

            GuiUtil.setCursor(this, Cursor.DEFAULT_CURSOR);
        }

        @Override
        public void paint(Graphics g)
        {
            if (forceFineRendering)
                setFineRendering();
            else
            {
                // several repaint in a short period of time --> set fast rendering for 1 second
                if ((lastRefreshTime != 0) && ((System.currentTimeMillis() - lastRefreshTime) < 250))
                    setCoarseRendering(1000);
            }

            // call paint on overlays first
            paintLayers(getLayers(true), getImageLayer(), getSequence());

            // then do 3D rendering
            super.paint(g);

            lastRefreshTime = System.currentTimeMillis();
        }

        /**
         * Paint layers
         */
        protected void paintLayers(List<Layer> sortedLayers, final Layer imageLayer, Sequence seq)
        {
            final boolean lv = isLayersVisible();

            // call paint in inverse order to have first overlay "at top"
            for (int i = sortedLayers.size() - 1; i >= 0; i--)
            {
                final Layer layer = sortedLayers.get(i);

                if (layer == null)
                    continue;
                
                // update VTK visibility flag
                for (vtkProp prop : VtkUtil.getLayerProps(layer))
                {
                    // image layer is not impacted by global layer visibility
                    if (layer == imageLayer)
                    {
                        // we have a no empty image --> display it if layer is visible
                        if (layer.isVisible() && (seq != null) && !seq.isEmpty())
                            prop.SetVisibility(1);
                        else
                            prop.SetVisibility(0);
                    }
                    else
                    {
                        boolean visible = lv && layer.isVisible();
                        // FIXME: find a better method to know visibility flags should not be impacted here
                        final vtkInformation vtkInfo = prop.GetPropertyKeys();

                        if (vtkInfo != null)
                        {
                            // pick the visibility info
                            if ((vtkInfo.Has(visibilityKey) != 0) && (vtkInfo.Get(visibilityKey) == 0))
                                visible = false;
                        }

                        // finally set the visibility state
                        prop.SetVisibility(visible ? 1 : 0);
                    }

                    // opacity seems to not be correctly handled in VTK ??
                    if (prop instanceof vtkActor)
                        ((vtkActor) prop).GetProperty().SetOpacity(layer.getOpacity());
                    else if (prop instanceof vtkActor2D)
                        ((vtkActor2D) prop).GetProperty().SetOpacity(layer.getOpacity());
                }

                // then do layer painting
                if (lv && layer.isVisible())
                    // important to set Graphics to null for VtkCanvas as some plugins rely on it to detect VtkCanvas
                    // (note that this is not a good way of doing it)
                    layer.getOverlay().paint(null, seq, VtkCanvas.this);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseEntered(e, getMouseImagePos5D());

            super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseExited(e, getMouseImagePos5D());

            super.mouseExited(e);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseClick(e, getMouseImagePos5D());

            super.mouseClicked(e);
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            // update mouse position
            setMousePos(e.getPoint());

            // get picked object (mouse move/drag event)
            pickedObject = pick(e.getX(), e.getY());

            // send mouse event to overlays
            VtkCanvas.this.mouseMove(e, getMouseImagePos5D());

            final boolean consumed = e.isConsumed();

            super.mouseMoved(e);

            // refresh mouse cursor (do it after all process)
            updateCursor(!consumed && e.isConsumed());
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // update mouse position
            setMousePos(e.getPoint());

            // get picked object (mouse move/drag event)
            pickedObject = pick(e.getX(), e.getY());

            // send mouse event to overlays
            VtkCanvas.this.mouseDrag(e, getMouseImagePos5D());

            final boolean consumed = e.isConsumed();

            super.mouseDragged(e);

            // refresh mouse cursor (do it after all process)
            updateCursor(!consumed && e.isConsumed());
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mousePressed(e, getMouseImagePos5D());

            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseReleased(e, getMouseImagePos5D());

            super.mouseReleased(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // send mouse event to overlays
            VtkCanvas.this.mouseWheelMoved(e, getMouseImagePos5D());

            super.mouseWheelMoved(e);
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            if (!e.isConsumed())
            {
                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_R:
                        // reset view
                        resetCamera();

                        // also reset LUT
                        if (EventUtil.isShiftDown(e, true))
                        {
                            final Sequence sequence = getSequence();
                            final Viewer viewer = getViewer();

                            if ((viewer != null) && (sequence != null))
                            {
                                final LUT lut = sequence.createCompatibleLUT();

                                // set default opacity for 3D display
                                lut.setAlphaToLinear3D();
                                viewer.setLut(lut);
                            }
                        }
                        else
                            repaint();

                        e.consume();
                        break;
                }
            }

            super.keyPressed(e);
        }
    }

    /**
     * Image overlay to encapsulate VTK image volume in a canvas layer
     */
    protected class VtkCanvasImageOverlay extends IcyCanvasImageOverlay implements VtkPainter
    {
        public VtkCanvasImageOverlay()
        {
            super();

            // create image volume
            imageVolume = new VtkImageVolume();
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // nothing here
        }

        @Override
        public vtkProp[] getProps()
        {
            // return the image volume as prop
            return new vtkProp[] {imageVolume.getVolume()};
        }
    }

    /**
     * Property to update
     */
    protected static class Property
    {
        String name;
        Object value;

        public Property(String name, Object value)
        {
            super();

            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Property)
                return name.equals(((Property) obj).name);

            return super.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return name.hashCode();
        }
    };

    /**
     * Properties updater helper class
     */
    protected class PropertiesUpdater extends Thread
    {
        final LinkedBlockingQueue<Property> toUpdate;

        public PropertiesUpdater()
        {
            super("VTK canvas properties updater");

            toUpdate = new LinkedBlockingQueue<VtkCanvas.Property>(256);
        }

        public synchronized void submit(Property prop)
        {
            // remove previous property of same name
            if (toUpdate.remove(prop))
            {
                // if we already had a layers visible update then we update all layers
                if (prop.name.equals(PROPERTY_LAYERS_VISIBLE))
                    prop.value = null;
            }

            // add the property
            toUpdate.add(prop);
        }

        protected void updateProperty(Property prop) throws InterruptedException
        {
            final String name = prop.name;
            final Object value = prop.value;

            if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_AMBIENT))
            {
                final double d = ((Double) value).doubleValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setAmbient(d);
                    }
                });

                preferences.putDouble(ID_AMBIENT, d);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_DIFFUSE))
            {
                final double d = ((Double) value).doubleValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setDiffuse(d);
                    }
                });

                preferences.putDouble(ID_DIFFUSE, d);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_SPECULAR))
            {
                final double d = ((Double) value).doubleValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setSpecular(d);
                    }
                });

                preferences.putDouble(ID_SPECULAR, d);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_BG_COLOR))
            {
                final Color color = (Color) value;

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setBackgroundColorInternal(color);
                    }
                });

                preferences.putInt(ID_BGCOLOR, color.getRGB());
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_INTERPOLATION))
            {
                final int i = ((Integer) value).intValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setInterpolationMode(i);
                    }
                });

                preferences.putInt(ID_INTERPOLATION, i);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_MAPPER))
            {
                final boolean gpuRendering = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setGPURendering(gpuRendering);
                    }
                });

                preferences.putInt(ID_MAPPER, gpuRendering ? 1 : 0);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_BLENDING))
            {
                final VtkVolumeBlendType type = (VtkVolumeBlendType) value;

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setBlendingMode(type);
                    }
                });

                preferences.putInt(ID_BLENDING, getVolumeBlendingMode().ordinal());
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_SAMPLE))
            {
                final int i = ((Integer) value).intValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setSampleResolution(i);
                    }
                });

                preferences.putDouble(ID_SAMPLE, i);
            }
            else if (StringUtil.equals(name, PROPERTY_AXES))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        panel3D.setAxisOrientationDisplayEnable(b);
                    }
                });

                preferences.putBoolean(ID_AXES, b);
            }
            else if (StringUtil.equals(name, PROPERTY_BOUNDINGBOX))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        boundingBox.SetVisibility(b ? 1 : 0);
                    }
                });

                preferences.putBoolean(ID_BOUNDINGBOX, b);
            }
            else if (StringUtil.equals(name, PROPERTY_BOUNDINGBOX_GRID))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        rulerBox.SetDrawXGridlines(b ? 1 : 0);
                        rulerBox.SetDrawYGridlines(b ? 1 : 0);
                        rulerBox.SetDrawZGridlines(b ? 1 : 0);
                    }
                });

                preferences.putBoolean(ID_BOUNDINGBOX_GRID, b);
            }
            else if (StringUtil.equals(name, PROPERTY_BOUNDINGBOX_RULES))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        rulerBox.SetXAxisTickVisibility(b ? 1 : 0);
                        rulerBox.SetXAxisMinorTickVisibility(b ? 1 : 0);
                        rulerBox.SetYAxisTickVisibility(b ? 1 : 0);
                        rulerBox.SetYAxisMinorTickVisibility(b ? 1 : 0);
                        rulerBox.SetZAxisTickVisibility(b ? 1 : 0);
                        rulerBox.SetZAxisMinorTickVisibility(b ? 1 : 0);
                    }
                });

                preferences.putBoolean(ID_BOUNDINGBOX_RULES, b);
            }
            else if (StringUtil.equals(name, PROPERTY_BOUNDINGBOX_LABELS))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        rulerBox.SetXAxisLabelVisibility(b ? 1 : 0);
                        rulerBox.SetYAxisLabelVisibility(b ? 1 : 0);
                        rulerBox.SetZAxisLabelVisibility(b ? 1 : 0);
                    }
                });

                preferences.putBoolean(ID_BOUNDINGBOX_LABELS, b);
            }
            else if (StringUtil.equals(name, VtkSettingPanel.PROPERTY_SHADING))
            {
                final boolean b = ((Boolean) value).booleanValue();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imageVolume.setShade(b);
                    }
                });

                preferences.putBoolean(ID_SHADING, b);
            }
            else if (StringUtil.equals(name, PROPERTY_LUT))
            {
                updateLut();
            }
            else if (StringUtil.equals(name, PROPERTY_SCALE))
            {
                final double[] oldScale = getVolumeScale();
                final double[] newScale = (double[]) value;

                if (!Arrays.equals(oldScale, newScale))
                {
                    invokeOnEDT(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            imageVolume.setScale(newScale);
                            // need to update bounding box as well
                            updateBoundingBoxSize();
                        }
                    });
                }
            }
            else if (StringUtil.equals(name, PROPERTY_DATA))
            {
                final vtkImageData data = getImageData();

                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // set image data
                        updateImageData(data);
                    }
                });
            }
            else if (StringUtil.equals(name, PROPERTY_BOUNDS))
            {
                invokeOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateBoundingBoxSize();
                    }
                });
            }
            else if (StringUtil.equals(name, PROPERTY_LAYERS_VISIBLE))
            {
                // force refresh
                refresh();
            }
        }

        @Override
        public void run()
        {
            while (!isInterrupted())
            {
                try
                {
                    updateProperty(toUpdate.take());
                }
                catch (InterruptedException e)
                {
                    // just end process
                    break;
                }

                // need to refresh rendering
                if (toUpdate.isEmpty())
                    refresh();
            }

            // help GC
            toUpdate.clear();
        }
    }

    /**
     * VTK overlay updater helper class
     */
    protected class VtkOverlayUpdater extends Thread
    {
        final LinkedList<vtkProp> propToAdd;
        final LinkedList<vtkProp> propToRemove;

        public VtkOverlayUpdater()
        {
            super("VTK canvas overlay updater");

            propToAdd = new LinkedList<vtkProp>();
            propToRemove = new LinkedList<vtkProp>();
        }

        public void addProp(vtkProp prop)
        {
            synchronized (propToAdd)
            {
                propToAdd.add(prop);
            }
        }

        public void removeProp(vtkProp prop)
        {
            synchronized (propToRemove)
            {
                propToRemove.add(prop);
            }
        }

        public void addProps(List<vtkProp> props)
        {
            synchronized (propToAdd)
            {
                propToAdd.addAll(props);
            }
        }

        public void removeProps(List<vtkProp> props)
        {
            synchronized (propToRemove)
            {
                propToRemove.addAll(props);
            }
        }

        public void addProps(vtkProp[] props)
        {
            synchronized (propToAdd)
            {
                for (vtkProp prop : props)
                    propToAdd.add(prop);
            }
        }

        public void removeProps(vtkProp[] props)
        {
            synchronized (propToAdd)
            {
                for (vtkProp prop : props)
                    propToRemove.add(prop);
            }
        }

        @Override
        public void run()
        {
            while (!isInterrupted())
            {
                while (!isInterrupted() && !propToAdd.isEmpty())
                {
                    invokeOnEDTSilent(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final vtkRenderer r = getRenderer();
                            final vtkCamera cam = getCamera();
                            int done = 0;

                            if ((r != null) && (cam != null))
                            {
                                // add actor by packet of 1000
                                while (!propToAdd.isEmpty() && (done++ < 1000))
                                {
                                    final vtkProp prop = propToAdd.removeFirst();

                                    // actor not yet present in renderer ?
                                    if (r.HasViewProp(prop) == 0)
                                    {
                                        // refresh camera property for this specific kind of actor
                                        if (prop instanceof vtkCubeAxesActor)
                                            ((vtkCubeAxesActor) prop).SetCamera(cam);

                                        // add the actor to the renderer
                                        r.AddViewProp(prop);
                                    }
                                }
                            }
                        }
                    });

                    // sleep a bit to offer a bit of responsiveness
                    ThreadUtil.sleep(10);
                    // and refresh
                    refresh();
                }

                while (!isInterrupted() && !propToRemove.isEmpty())
                {
                    invokeOnEDTSilent(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final vtkRenderer r = getRenderer();
                            final vtkCamera cam = getCamera();
                            int done = 0;

                            if ((r != null) && (cam != null))
                            {
                                // remove actors from renderer by packet of 1000
                                while (!propToRemove.isEmpty() && (done++ < 1000))
                                    r.RemoveViewProp(propToRemove.removeFirst());
                            }
                        }
                    });

                    // sleep a bit to offer a bit of responsiveness
                    ThreadUtil.sleep(10);
                    // and refresh
                    refresh();
                }

                // sleep a bit
                ThreadUtil.sleep(1);
            }

            // help GC
            propToAdd.clear();
            propToRemove.clear();
        }
    }
}
