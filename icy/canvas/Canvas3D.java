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

import icy.canvas.CanvasLayerEvent.LayersEventType;
import icy.canvas.IcyCanvasEvent.IcyCanvasEventType;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.colormap.IcyColorMap;
import icy.image.lut.LUT;
import icy.image.lut.LUT.LUTChannel;
import icy.math.Scaler;
import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.InstanceProcessor;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.collection.array.Array1DUtil;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vtk.vtkCamera;
import vtk.vtkColorTransferFunction;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;
import vtk.vtkFixedPointVolumeRayCastMapper;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkIntArray;
import vtk.vtkOpenGLVolumeTextureMapper2D;
import vtk.vtkPanel;
import vtk.vtkPiecewiseFunction;
import vtk.vtkPointData;
import vtk.vtkProp;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkShortArray;
import vtk.vtkUnsignedCharArray;
import vtk.vtkUnsignedIntArray;
import vtk.vtkUnsignedShortArray;
import vtk.vtkVolume;
import vtk.vtkVolumeMapper;
import vtk.vtkVolumeProperty;

/**
 * <br>
 * Class structure: <br>
 * <br>
 * mainPanelVolumeControl contains all the volumeControlPanel contained in the
 * volumeList array.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class Canvas3D extends IcyCanvas3D implements ActionListener, ColorChangeListener, TextChangeListener
{
    private static final long serialVersionUID = -2677870897470280726L;

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
                layer.getOverlay().paint(null, seq, Canvas3D.this);
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseEntered(e, null);

            super.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseExited(e, null);

            super.mouseExited(e);
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseClick(e, null);

            super.mouseClicked(e);
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseMove(e, null);

            super.mouseMoved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseDrag(e, null);

            super.mouseDragged(e);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mousePressed(e, null);

            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseReleased(e, null);

            super.mouseReleased(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // send mouse event to overlays
            Canvas3D.this.mouseWheelMoved(e, null);

            super.mouseWheelMoved(e);
        }
    }

    /**
     * basic vtk objects
     */
    CustomVtkPanel panel3D;
    private vtkRenderer renderer;
    private vtkCamera activeCam;

    /**
     * volume data
     */
    private vtkVolumeMapper volumeMapper;
    private vtkVolume volume;
    private vtkVolumeProperty volumeProperty;
    private vtkImageData imageData;

    /**
     * gui
     */

    // background color
    private ColorChooserButton backGroundColor = new ColorChooserButton();
    // specular
    final private IcyTextField specularTextField = new IcyTextField("0.3");
    final private IcyTextField specularPowerTextField = new IcyTextField("30");

    // volume mapper choice
    final private String[] volumeMapperString = {"Raycast", "OpenGL"};
    final private JComboBox volumeMapperCombo = new JComboBox(volumeMapperString);
    // volume interpolation choice
    final private String[] volumeInterpolationString = {"Linear", "Nearest"};
    final private JComboBox volumeInterpolationCombo = new JComboBox(volumeInterpolationString);
    // volume resolution
    final private String[] volumeImageSampleDistanceString = {"Auto", "1 (slow)", "2", "3", "4", "5", "6", "7", "8",
            "9", "10 (fast)"};
    final private JComboBox volumeImageSampleDistanceCombo = new JComboBox(volumeImageSampleDistanceString);
    // volume shade
    final private JCheckBox volumeShadeCheckBox = new JCheckBox("Use Shading", false);

    /**
     * internals
     */
    final InstanceProcessor processor;
    final Runnable displayRefresher;
    private final Runnable imageDataBuilder;
    private final Runnable volumeMapperBuilder;
    private final LUT lutSave;
    private boolean initialized;
    private final double[] volumeScaling;

    public Canvas3D(Viewer viewer)
    {
        super(viewer);

        initialized = false;

        // all channel visible at once by default
        posC = -1;

        final Sequence seq = getSequence();

        // default X, Y, Z scaling for volume
        volumeScaling = new double[3];

        volumeScaling[0] = seq.getPixelSizeX();
        volumeScaling[1] = seq.getPixelSizeY();
        volumeScaling[2] = seq.getPixelSizeZ();

        panel = GuiUtil.generatePanelWithoutBorder();

        // general Settings
        final JPanel generalSettingsPanel = GuiUtil.generatePanel("General Settings");

        // default is white
        backGroundColor.setColorChooseText("3D Background Color");
        backGroundColor.setColor(Color.white);
        backGroundColor.addColorChangeListener(this);

        generalSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Background color", 100), Box.createHorizontalStrut(8), backGroundColor,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        generalSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Specular", 100), Box.createHorizontalStrut(8), specularTextField,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        generalSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Specular power", 100), Box.createHorizontalStrut(8),
                specularPowerTextField, Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));

        specularTextField.addTextChangeListener(this);
        specularPowerTextField.addTextChangeListener(this);

        panel.add(generalSettingsPanel);

        // volume Settings
        final JPanel volumeSettingsPanel = GuiUtil.generatePanel("Image Volume Rendering Settings");

        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Mapper", 100), Box.createHorizontalStrut(8), volumeMapperCombo,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Interpolation", 100), Box.createHorizontalStrut(8),
                volumeInterpolationCombo, Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        final JLabel maxVolumeSampleLabel = new JLabel("Sample");
        ComponentUtil.setFixedWidth(maxVolumeSampleLabel, 100);
        maxVolumeSampleLabel
                .setToolTipText("Use low value for fine (but slow) render and high value for fast (but draft) render");
        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), maxVolumeSampleLabel,
                Box.createHorizontalStrut(8), volumeImageSampleDistanceCombo, Box.createHorizontalGlue(),
                Box.createHorizontalStrut(4)));
        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), volumeShadeCheckBox,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));

        volumeMapperCombo.addActionListener(this);
        volumeInterpolationCombo.addActionListener(this);
        volumeImageSampleDistanceCombo.addActionListener(this);
        volumeShadeCheckBox.addActionListener(this);

        panel.add(volumeSettingsPanel);

        // volume clipping settings
        // final JPanel volumeClippingSettingsPanel =
        // GuiUtil.generatePanel("Volume clipping settings");
        // volumeClippingSettingsPanel.add(GuiUtil.besidesPanel(new
        // JLabel("settings.")));
        //
        // infoPanel.add(volumeClippingSettingsPanel);

        panel.add(Box.createVerticalGlue());

        // initialize VTK components & main GUI
        panel3D = new CustomVtkPanel();
        panel3D.addKeyListener(this);

        renderer = panel3D.GetRenderer();
        // set renderer properties
        renderer.SetBackground(Array1DUtil.floatArrayToDoubleArray(backGroundColor.getColor().getColorComponents(null)));

        activeCam = renderer.GetActiveCamera();
        // set camera properties
        // activeCam.Azimuth(20.0);
        // activeCam.Dolly(1.60);

        // set 3D view in center
        add(panel3D, BorderLayout.CENTER);

        // initialize internals
        processor = new InstanceProcessor();
        processor.setDefaultThreadName("Canvas3D renderer");
        // we want the processor to stay alive for sometime
        processor.setKeepAliveTime(3, TimeUnit.SECONDS);

        displayRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO: VTK doesn't accept to run from anywhere except AWT dispatch
                // we keep the method just if VTK change someday
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // refresh rendering
                            panel3D.paint(panel3D.getGraphics());
                        }
                        catch (Exception E)
                        {
                            // ignore
                        }
                    }
                });
            }
        };
        imageDataBuilder = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO: VTK doesn't accept to run from anywhere except AWT dispatch
                // we keep the method just if VTK change someday
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            internalBuildImageData();
                        }
                        catch (Exception E)
                        {
                            // ignore
                        }
                    }
                });
            }
        };
        volumeMapperBuilder = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO: VTK doesn't accept to run from anywhere except AWT dispatch
                // we keep the method just if VTK change someday
                ThreadUtil.invokeNow(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            internalBuildVolumeMapper();
                        }
                        catch (Exception E)
                        {
                            // ignore
                        }
                    }
                });
            }
        };

        // save lut and prepare for 3D visualization
        lutSave = seq.createCompatibleLUT();
        final LUT lut = getLut();

        // save colormap
        saveColormap(lut);
        // adjust LUT alpha level for 3D view (this make lutChanged() to be called)
        setDefaultOpacity(lut);

        // update nav bar & mouse infos
        mouseInfPanel.setVisible(false);
        updateZNav();
        updateTNav();

        // initialize volume data
        volume = new vtkVolume();
        volumeProperty = new vtkVolumeProperty();
        imageData = null;
        // build volume mapper
        internalBuildVolumeMapper();

        // default volume setup
        volumeProperty.IndependentComponentsOn();
        volumeProperty.DisableGradientOpacityOn();
        volume.SetProperty(volumeProperty);
        setupSpecular();
        setupVolumeImageSampleDistance();
        setupVolumeInterpolationType();
        // build image data
        internalBuildImageData();
        // update the color property
        setupColorProperties(getPositionC());

        // add volume to renderer
        // TODO : add option to remove volume rendering
        renderer.AddVolume(volume);
        // setup initial scaling after volume has been added to the renderer
        setupVolumeScale();

        // add vtkPainter actors to the renderer
        for (Layer l : getLayers(false))
            addLayerActors(l);

        // reset camera
        resetCamera();

        initialized = true;
    }

    private void buildVolumeMapper()
    {
        processor.submit(volumeMapperBuilder);
    }

    void internalBuildVolumeMapper()
    {
        if (volumeMapper != null)
        {
            // release previous mapper
            volumeMapper.RemoveAllInputs();
            volumeMapper.Delete();
        }

        // DATA
        if (useRaycastVolumeMapper())
        {
            // ACCURATE VISUALIZATION MODE
            volumeMapper = new vtkFixedPointVolumeRayCastMapper();
            ((vtkFixedPointVolumeRayCastMapper) volumeMapper).IntermixIntersectingGeometryOn();
        }
        else
        {
            // FAST VISUALIZATION MODE
            volumeMapper = new vtkOpenGLVolumeTextureMapper2D();
        }

        // setup connections
        volumeMapper.SetInputData(imageData);
        volume.SetMapper(volumeMapper);
    }

    boolean useRaycastVolumeMapper()
    {
        return volumeMapperCombo.getSelectedIndex() == 0;
    }

    boolean useLinearVolumeInterpolation()
    {
        return volumeInterpolationCombo.getSelectedIndex() == 0;
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

    private void buildImageData()
    {
        processor.submit(imageDataBuilder);
    }

    void internalBuildImageData()
    {
        final Sequence sequence = getSequence();
        if (sequence == null)
            return;

        final int sizeX = sequence.getSizeX();
        final int sizeY = sequence.getSizeY();
        final int sizeZ = sequence.getSizeZ();
        final int sizeC;
        final DataType dataType = sequence.getDataType_();
        final int posT = getPositionT();
        final int posC = getPositionC();

        // create a new image data structure
        final vtkImageData newImageData = new vtkImageData();

        newImageData.SetDimensions(sizeX, sizeY, sizeZ);
        // all component ?
        if (posC == -1)
            sizeC = sequence.getSizeC();
        else
            sizeC = 1;
        newImageData.SetExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);

        vtkDataArray array;

        switch (dataType)
        {
            case UBYTE:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_UNSIGNED_CHAR, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyCXYZAsByte(posT));
                else
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyXYZAsByte(posT, posC));
                break;

            case BYTE:
                // FIXME: signed char not supported by VTK java wrapper ??

                // pre-allocate data
                // newImageData.AllocateScalars(VtkUtil.VTK_SIGNED_CHAR, sizeC);
                newImageData.AllocateScalars(VtkUtil.VTK_UNSIGNED_CHAR, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyCXYZAsByte(posT));
                else
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyXYZAsByte(posT, posC));
                break;

            case USHORT:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_UNSIGNED_SHORT, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedShortArray) array).SetJavaArray(sequence.getDataCopyCXYZAsShort(posT));
                else
                    ((vtkUnsignedShortArray) array).SetJavaArray(sequence.getDataCopyXYZAsShort(posT, posC));
                break;

            case SHORT:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_SHORT, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkShortArray) array).SetJavaArray(sequence.getDataCopyCXYZAsShort(posT));
                else
                    ((vtkShortArray) array).SetJavaArray(sequence.getDataCopyXYZAsShort(posT, posC));
                break;

            case UINT:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_UNSIGNED_INT, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedIntArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
                else
                    ((vtkUnsignedIntArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT, posC));
                break;

            case INT:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_INT, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkIntArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
                else
                    ((vtkIntArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT, posC));
                break;

            case FLOAT:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_FLOAT, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkFloatArray) array).SetJavaArray(sequence.getDataCopyCXYZAsFloat(posT));
                else
                    ((vtkFloatArray) array).SetJavaArray(sequence.getDataCopyXYZAsFloat(posT, posC));
                break;

            case DOUBLE:
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_DOUBLE, sizeC);
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkDoubleArray) array).SetJavaArray(sequence.getDataCopyCXYZAsDouble(posT));
                else
                    ((vtkDoubleArray) array).SetJavaArray(sequence.getDataCopyXYZAsDouble(posT, posC));
                break;

            default:
                // we probably have an empty sequence
                newImageData.SetDimensions(1, 1, 1);
                newImageData.SetExtent(0, 0, 0, 0, 0, 0);
                // pre-allocate data
                newImageData.AllocateScalars(VtkUtil.VTK_UNSIGNED_CHAR, sizeC);
                break;
        }

        // set connection
        volumeMapper.SetInputData(newImageData);
        // mark volume as modified
        volume.Modified();

        // release previous volume data memory
        if (imageData != null)
        {
            final vtkPointData pointData = imageData.GetPointData();
            if (pointData != null)
            {
                final vtkDataArray dataArray = pointData.GetScalars();
                if (dataArray != null)
                    dataArray.Delete();
                pointData.Delete();
            }
            imageData.ReleaseData();
            imageData.Delete();
        }

        // set to new image data
        imageData = newImageData;
    }

    /**
     * Adjust the volume image resolution rendering.<br>
     * <ul>
     * <li>0 = auto selection</li>
     * <li>1 = highest resolution (slow)</li>
     * <li>10 = lowest resolution (fast)</li>
     * </ul>
     * 
     * @param value
     */
    public void setVolumeDistanceSample(int value)
    {
        volumeImageSampleDistanceCombo.setSelectedIndex(value);
    }

    void setupColorProperties(int c)
    {
        final LUT lut = getLut();

        if (c == -1)
            setupColorProperties(lut);
        else
            setupColorProperties(lut.getLutChannel(c), 0);
    }

    /**
     * @param volumeProperty
     * @param lut
     */
    private void setupColorProperties(LUT lut)
    {
        for (int comp = 0; comp < lut.getNumChannel(); comp++)
            setupColorProperties(lut.getLutChannel(comp), comp);
    }

    private void setDefaultOpacity(LUT lut)
    {
        for (LUTChannel lutChannel : lut.getLutChannels())
            lutChannel.getColorMap().setDefaultAlphaFor3D();
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

    /**
     * @param volumeProperty
     * @param lutBand
     */
    private void setupColorProperties(LUTChannel lutBand, int index)
    {
        final IcyColorMap colorMap = lutBand.getColorMap();
        final Scaler scaler = lutBand.getScaler();

        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction ctf = new vtkColorTransferFunction();

        ctf.SetRange(scaler.getLeftIn(), scaler.getRightIn());
        for (int i = 0; i < IcyColorMap.SIZE; i++)
            ctf.AddRGBPoint(scaler.unscale(i), colorMap.getNormalizedRed(i), colorMap.getNormalizedGreen(i),
                    colorMap.getNormalizedBlue(i));

        volumeProperty.SetColor(index, ctf);

        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction pwf = new vtkPiecewiseFunction();

        if (colorMap.isEnabled())
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                pwf.AddPoint(scaler.unscale(i), colorMap.getNormalizedAlpha(i));
        }
        else
        {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                pwf.AddPoint(scaler.unscale(i), 0d);
        }

        volumeProperty.SetScalarOpacity(index, pwf);
    }

    private void setupVolumeImageSampleDistance()
    {
        final int distance = volumeImageSampleDistanceCombo.getSelectedIndex();

        if (volumeMapper instanceof vtkFixedPointVolumeRayCastMapper)
        {
            final vtkFixedPointVolumeRayCastMapper mapper = (vtkFixedPointVolumeRayCastMapper) volumeMapper;

            if (distance == 0)
                mapper.AutoAdjustSampleDistancesOn();
            else
            {
                mapper.AutoAdjustSampleDistancesOff();
                mapper.SetImageSampleDistance(distance);
            }
        }
    }

    private void setupSpecular()
    {
        if (volumeShadeCheckBox.isSelected())
            volumeProperty.ShadeOn();
        else
            volumeProperty.ShadeOff();

        try
        {
            volumeProperty.SetSpecular(Double.parseDouble(specularTextField.getText()));
        }
        catch (Exception e)
        {
            // ignore
        }
        try
        {
            volumeProperty.SetSpecularPower(Double.parseDouble(specularPowerTextField.getText()));
        }
        catch (Exception e)
        {
            // ignore
        }
    }

    private void setupVolumeScale()
    {
        // update volume scale
        volume.SetScale(volumeScaling);
    }

    private void setupVolumeInterpolationType()
    {
        if (useLinearVolumeInterpolation())
            volumeProperty.SetInterpolationTypeToLinear();
        else
            volumeProperty.SetInterpolationTypeToNearest();
    }

    private void setupVolumeMapperType()
    {
        // update view type combo state
        if (useRaycastVolumeMapper())
        {
            volumeImageSampleDistanceCombo.setEnabled(true);

            // set C position back to -1
            setPositionC(-1);

            // re-enable all channel
            final int maxC = getMaxPositionC();
            for (int c = 0; c <= maxC; c++)
                getLut().getLutChannel(c).setEnabled(true);
        }
        else
        {
            volumeImageSampleDistanceCombo.setEnabled(false);
            setPositionC(0);
        }

        // rebuild volume mapper
        buildVolumeMapper();
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

    public vtkPanel getPanel3D()
    {
        return panel3D;
    }

    public vtkRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Get scaling for image volume rendering
     */
    public double[] getVolumeScale()
    {
        return Arrays.copyOf(volumeScaling, volumeScaling.length);
    }

    /**
     * Get X scaling for image volume rendering
     */
    public double getVolumeScaleX()
    {
        return volumeScaling[0];
    }

    /**
     * Get Y scaling for image volume rendering
     */
    public double getVolumeScaleY()
    {
        return volumeScaling[1];
    }

    /**
     * Get Z scaling for image volume rendering
     */
    public double getVolumeScaleZ()
    {
        return volumeScaling[2];
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getXScaling()
    {
        return getVolumeScaleX();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getYScaling()
    {
        return getVolumeScaleY();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public double getZScaling()
    {
        return getVolumeScaleZ();
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

    /**
     * Set scaling for image volume rendering
     */
    public void setVolumeScale(double x, double y, double z)
    {
        if ((volumeScaling[0] != x) || (volumeScaling[1] != y) || (volumeScaling[2] != z))
        {
            volumeScaling[0] = x;
            volumeScaling[1] = y;
            volumeScaling[2] = z;

            // update scaling
            setupVolumeScale();
            // refresh rendering
            refresh();
        }
    }

    /**
     * Set X scaling for image volume rendering
     */
    public void setVolumeScaleX(double value)
    {
        if (volumeScaling[0] != value)
        {
            volumeScaling[0] = value;

            // update scaling
            setupVolumeScale();
            // refresh rendering
            refresh();
        }
    }

    /**
     * Set Y scaling for image volume rendering
     */
    public void setVolumeScaleY(double value)
    {
        if (volumeScaling[1] != value)
        {
            volumeScaling[1] = value;

            // update scaling
            setupVolumeScale();
            // refresh rendering
            refresh();
        }
    }

    /**
     * Set Z scaling for image volume rendering
     */
    public void setVolumeScaleZ(double value)
    {
        if (volumeScaling[2] != value)
        {
            volumeScaling[2] = value;

            // update scaling
            setupVolumeScale();
            // refresh rendering
            refresh();
        }
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

    /**
     * @deprecated
     */
    @Deprecated
    public void setXScaling(double value)
    {
        setVolumeScaleX(value);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setYScaling(double value)
    {
        setVolumeScaleY(value);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setZScaling(double value)
    {
        setVolumeScaleZ(value);
    }

    @Override
    protected void setPositionZInternal(int z)
    {
        // not supported, Z should stay at -1
    }

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
                    // rebuild data
                    internalBuildImageData();
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
    public void textChanged(IcyTextField source, boolean validate)
    {
        // only on validation
        if (validate)
        {
            if ((source == specularTextField) || (source == specularPowerTextField))
            {
                setupSpecular();
                refresh();
            }
        }
    }

    /**
     * Force render refresh
     */
    @Override
    public void refresh()
    {
        if (!initialized)
            return;

        // then refresh 3D display in background processing
        processor.submit(displayRefresher);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == volumeShadeCheckBox)
        {
            setupSpecular();
            refresh();
        }

        if (e.getSource() == volumeImageSampleDistanceCombo)
        {
            setupVolumeImageSampleDistance();
            refresh();
        }

        if (e.getSource() == volumeInterpolationCombo)
        {
            setupVolumeInterpolationType();
            refresh();
        }

        if (e.getSource() == volumeMapperCombo)
        {
            setupVolumeMapperType();
            refresh();
        }
    }

    @Override
    public void shutDown()
    {
        super.shutDown();

        // restore colormap
        restoreOpacity(lutSave, getLut());
        // restoreColormap(getLut());

        // processor.shutdownAndWait();

        // AWTMultiCaster of vtkPanel keep reference of this frame so
        // we have to release as most stuff we can
        removeAll();
        panel.removeAll();

        renderer.Delete();
        volume.Delete();
        volumeMapper.RemoveAllInputs();
        volumeMapper.Delete();
        volumeProperty.Delete();
        activeCam.Delete();
        imageData.GetPointData().GetScalars().Delete();
        imageData.GetPointData().Delete();
        imageData.ReleaseData();
        imageData.Delete();

        renderer = null;
        volume = null;
        volumeMapper = null;
        volumeProperty = null;
        activeCam = null;
        imageData = null;

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
                    // we need to rebuild imageData volume
                    buildImageData();
                    // and update the color property
                    setupColorProperties(getPositionC());
                    // refresh
                    refresh();
                    break;

                case T:
                    // rebuild image data and refresh
                    buildImageData();
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
        if (component == -1)
            setupColorProperties(posC);
        else if (posC == -1)
            setupColorProperties(lut.getLutChannel(component), component);
        else if (posC == component)
            setupColorProperties(posC);

        // refresh image
        refresh();
    }

    @Override
    protected void sequenceMetaChanged(String metadataName)
    {
        super.sequenceMetaChanged(metadataName);

        if (!initialized)
            return;

        final Sequence seq = getSequence();

        if (seq == null)
            return;

        if (StringUtil.isEmpty(metadataName))
        {
            // apply all changes related to metadata
            setVolumeScale(seq.getPixelSizeX(), seq.getPixelSizeY(), seq.getPixelSizeZ());
        }
        else
        {
            // check if X,Y or Z resolution changed
            if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_X))
                setVolumeScaleX(seq.getPixelSizeX());
            if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Y))
                setVolumeScaleY(seq.getPixelSizeY());
            if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Z))
                setVolumeScaleZ(seq.getPixelSizeZ());
        }
    }

    @Override
    protected void sequenceTypeChanged()
    {
        super.sequenceTypeChanged();

        if (!initialized)
            return;
    }

    @Override
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        super.sequenceDataChanged(image, type);

        if (!initialized)
            return;

        // rebuild image data and refresh
        buildImageData();
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
    public void colorChanged(ColorChooserButton source)
    {
        if (source == backGroundColor)
        {
            renderer.SetBackground(Array1DUtil.floatArrayToDoubleArray(backGroundColor.getColor().getColorComponents(
                    null)));
            refresh();
        }
    }
}
