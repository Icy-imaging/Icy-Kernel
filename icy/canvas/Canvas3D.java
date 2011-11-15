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
import icy.gui.component.ComponentUtil;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.TNavigationPanel;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMapBand;
import icy.image.lut.LUT;
import icy.image.lut.LUTBand;
import icy.math.Scaler;
import icy.painter.Painter;
import icy.painter.VtkPainter;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.system.thread.SingleInstanceProcessor;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import vtk.vtkActor;
import vtk.vtkActor2D;
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
public class Canvas3D extends IcyCanvas3D implements ActionListener, DocumentListener, ColorChangeListener
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
        }

        @Override
        public void paint(Graphics g)
        {
            // call paint on painters first
            if (getDrawLayers())
            {
                final Sequence seq = getSequence();

                if (seq != null)
                {
                    for (Painter painter : seq.getPainters())
                        painter.paint(null, seq, Canvas3D.this);
                }
            }

            // then refresh
            super.paint(g);
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
    final TNavigationPanel tNav;

    // background color
    private ColorChooserButton backGroundColor = new ColorChooserButton();
    // specular
    final private JTextField specularTextField = new JTextField("0.3");
    final private JTextField specularPowerTextField = new JTextField("30");

    // volume mapper choice
    final private String[] volumeMapperString = {"Raycast", "OpenGL"};
    final private JComboBox volumeMapperCombo = new JComboBox(volumeMapperString);
    // volume component choice
    private JPanel volumeComponentPanel;
    private JComboBox volumeComponentCombo = new JComboBox();
    // volume interpolation choice
    final private String[] volumeInterpolationString = {"Linear", "Nearest"};
    final private JComboBox volumeInterpolationCombo = new JComboBox(volumeInterpolationString);
    // volume resolution
    final private String[] volumeImageSampleDistanceString = {"Auto", "1 (slow)", "2", "3", "4", "5", "6", "7", "8",
            "9", "10 (fast)"};
    final private JComboBox volumeImageSampleDistanceCombo = new JComboBox(volumeImageSampleDistanceString);
    // volume spacing
    final private JTextField volumeZSpacing = new JTextField("1");
    // volume shade
    final private JCheckBox volumeShadeCheckBox = new JCheckBox("Use Shading", false);

    /**
     * internals
     */
    final SingleInstanceProcessor processor;
    final Runnable displayRefresher;
    private final Runnable imageDataBuilder;
    private final Runnable volumeMapperBuilder;
    private final LUT lutSave;
    private boolean initialized;
    private final double[] scaling;

    public Canvas3D(Viewer viewer)
    {
        super(viewer);

        initialized = false;

        // arrange to our dimension format
        if (posT == -1)
            posT = 0;
        posZ = -1;
        posC = -1;

        final Sequence seq = getSequence();

        // X, Y, Z scaling
        scaling = new double[3];

        scaling[0] = seq.getPixelSizeX();
        scaling[1] = seq.getPixelSizeY();
        scaling[2] = seq.getPixelSizeZ();

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

        specularTextField.getDocument().addDocumentListener(this);
        specularPowerTextField.getDocument().addDocumentListener(this);

        panel.add(generalSettingsPanel);

        // volume Settings
        final JPanel volumeSettingsPanel = GuiUtil.generatePanel("Image Volume Rendering Settings");

        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Mapper", 100), Box.createHorizontalStrut(8), volumeMapperCombo,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        volumeComponentPanel = GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Component", 100), Box.createHorizontalStrut(8), volumeComponentCombo,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4));
        volumeSettingsPanel.add(volumeComponentPanel);
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
        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4),
                GuiUtil.createFixedWidthLabel("Z Scaling", 100), Box.createHorizontalStrut(8), volumeZSpacing,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));
        volumeSettingsPanel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(4), volumeShadeCheckBox,
                Box.createHorizontalGlue(), Box.createHorizontalStrut(4)));

        volumeMapperCombo.addActionListener(this);
        volumeComponentCombo.addActionListener(this);
        volumeInterpolationCombo.addActionListener(this);
        volumeImageSampleDistanceCombo.addActionListener(this);
        volumeZSpacing.setText(StringUtil.toString(getZScaling()));
        volumeZSpacing.setColumns(2);
        volumeZSpacing.getDocument().addDocumentListener(this);
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

        tNav = new TNavigationPanel();
        tNav.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set the new T position
                setPositionT(tNav.getValue());
            }
        });

        // main
        setLayout(new BorderLayout());

        add(panel3D, BorderLayout.CENTER);
        add(tNav, BorderLayout.SOUTH);

        // initialize internals
        processor = new SingleInstanceProcessor();
        displayRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO: VTK doesn't accept to run from anywhere except AWT dispatch
                // we keep the method just if VTK change someday
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            // refresh rendering
                            panel3D.repaint();
                            // panel3D.Render();
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
                ThreadUtil.invokeLater(new Runnable()
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
                ThreadUtil.invokeLater(new Runnable()
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

        // update T nav bar
        updateTNav();

        // initialize volume data
        volume = new vtkVolume();
        volumeProperty = new vtkVolumeProperty();
        imageData = null;
        // build volume mapper
        internalBuildVolumeMapper();
        // rebuild component panel
        buildComponentPanel(true);

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
        setupVolumeScaling();

        // reset camera
        resetCamera();

        initialized = true;
    }

    void buildComponentPanel(boolean noEvent)
    {
        final Sequence seq = getSequence();
        if (seq == null)
            return;

        final int sizeC = seq.getSizeC();
        final int posC = getPositionC();

        if (useRaycastVolumeMapper())
        {
            final String[] items = new String[sizeC + 1];

            items[0] = "all";
            for (int i = 1; i <= sizeC; i++)
                items[i] = Integer.toString(i);

            volumeComponentCombo = new JComboBox(items);
            if (!noEvent)
                volumeComponentCombo.addActionListener(this);

            try
            {
                volumeComponentCombo.setSelectedIndex(posC + 1);
            }
            catch (IllegalArgumentException e)
            {
                volumeComponentCombo.setSelectedIndex(0);
            }

            if (noEvent)
                volumeComponentCombo.addActionListener(this);
        }
        else
        {
            final String[] items = new String[sizeC];

            for (int i = 0; i < sizeC; i++)
                items[i] = Integer.toString(i + 1);

            volumeComponentCombo = new JComboBox(items);
            volumeComponentCombo.addActionListener(this);

            try
            {
                if (posC == -1)
                    volumeComponentCombo.setSelectedIndex(0);
                else
                    volumeComponentCombo.setSelectedIndex(posC);
            }
            catch (IllegalArgumentException e)
            {
                volumeComponentCombo.setSelectedIndex(0);
            }
        }

        // rebuild component panel
        volumeComponentPanel.removeAll();
        volumeComponentPanel.add(Box.createHorizontalStrut(4));
        volumeComponentPanel.add(GuiUtil.createFixedWidthLabel("Component", 100));
        volumeComponentPanel.add(Box.createHorizontalStrut(8));
        volumeComponentPanel.add(volumeComponentCombo);
        volumeComponentPanel.add(Box.createHorizontalGlue());
        volumeComponentPanel.add(Box.createHorizontalStrut(4));
        volumeComponentPanel.validate();
    }

    private void buildVolumeMapper()
    {
        processor.addTask(volumeMapperBuilder, false);
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
        volumeMapper.SetInput(imageData);
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
        processor.addTask(imageDataBuilder, false);
    }

    void internalBuildImageData()
    {
        final Sequence sequence = getSequence();
        if (sequence == null)
            return;

        final int sizeX = sequence.getSizeX();
        final int sizeY = sequence.getSizeY();
        final int sizeZ = sequence.getSizeZ();
        final DataType dataType = sequence.getDataType_();
        final int posT = getPositionT();
        final int posC = getPositionC();

        // create a new image data structure
        final vtkImageData newImageData = new vtkImageData();

        newImageData.SetDimensions(sizeX, sizeY, sizeZ);
        // all component ?
        if (posC == -1)
            newImageData.SetNumberOfScalarComponents(sequence.getSizeC());
        else
            newImageData.SetNumberOfScalarComponents(1);
        newImageData.SetWholeExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);

        vtkDataArray array;

        switch (dataType)
        {
            case UBYTE:
                newImageData.SetScalarTypeToUnsignedChar();
                // pre-allocate data
                newImageData.AllocateScalars();
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
                // imageData.SetScalarTypeToChar();
                // // pre-allocate data
                // imageData.AllocateScalars();
                // // get array structure
                // final vtkCharArray array = (vtkCharArray)
                // imageData.GetPointData().GetScalars();
                // // set frame sequence data in the array structure
                // if (posC == -1)
                // array.SetJavaArray(sequence.getDataCopyCXYZAsByte(posT));
                // else
                // array.SetJavaArray(sequence.getDataCopyXYZAsByte(posT, posC));

                newImageData.SetScalarTypeToUnsignedChar();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyCXYZAsByte(posT));
                else
                    ((vtkUnsignedCharArray) array).SetJavaArray(sequence.getDataCopyXYZAsByte(posT, posC));
                break;

            case USHORT:
                newImageData.SetScalarTypeToUnsignedShort();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedShortArray) array).SetJavaArray(sequence.getDataCopyCXYZAsShort(posT));
                else
                    ((vtkUnsignedShortArray) array).SetJavaArray(sequence.getDataCopyXYZAsShort(posT, posC));
                break;

            case SHORT:
                newImageData.SetScalarTypeToShort();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkShortArray) array).SetJavaArray(sequence.getDataCopyCXYZAsShort(posT));
                else
                    ((vtkShortArray) array).SetJavaArray(sequence.getDataCopyXYZAsShort(posT, posC));
                break;

            case UINT:
                newImageData.SetScalarTypeToUnsignedInt();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkUnsignedIntArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
                else
                    ((vtkUnsignedIntArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT, posC));
                break;

            case INT:
                newImageData.SetScalarTypeToInt();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkIntArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
                else
                    ((vtkIntArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT, posC));
                break;

            // not supported because DataBufferLong doesn't exist
            // case ULONG:
            // newImageData.SetScalarTypeToUnsignedInt();
            // // pre-allocate data
            // newImageData.AllocateScalars();
            // // get array structure
            // array = newImageData.GetPointData().GetScalars();
            // // set frame sequence data in the array structure
            // if (posC == -1)
            // ((vtkUnsignedLongArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
            // else
            // ((vtkUnsignedLongArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT,
            // posC));
            // break;

            // not supported because DataBufferLong doesn't exist
            // case LONG:
            // newImageData.SetScalarTypeToInt();
            // // pre-allocate data
            // newImageData.AllocateScalars();
            // // get array structure
            // array = newImageData.GetPointData().GetScalars();
            // // set frame sequence data in the array structure
            // if (posC == -1)
            // ((vtkLongArray) array).SetJavaArray(sequence.getDataCopyCXYZAsInt(posT));
            // else
            // ((vtkLongArray) array).SetJavaArray(sequence.getDataCopyXYZAsInt(posT, posC));
            // break;

            case FLOAT:
                newImageData.SetScalarTypeToFloat();
                // pre-allocate data
                newImageData.AllocateScalars();
                // get array structure
                array = newImageData.GetPointData().GetScalars();
                // set frame sequence data in the array structure
                if (posC == -1)
                    ((vtkFloatArray) array).SetJavaArray(sequence.getDataCopyCXYZAsFloat(posT));
                else
                    ((vtkFloatArray) array).SetJavaArray(sequence.getDataCopyXYZAsFloat(posT, posC));
                break;

            case DOUBLE:
                newImageData.SetScalarTypeToDouble();
                // pre-allocate data
                newImageData.AllocateScalars();
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
                newImageData.SetNumberOfScalarComponents(1);
                newImageData.SetWholeExtent(0, 0, 0, 0, 0, 0);
                newImageData.SetScalarTypeToUnsignedChar();
                // pre-allocate data
                newImageData.AllocateScalars();
                break;
        }

        // set connection
        volumeMapper.SetInput(newImageData);
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
            setupColorProperties(lut.getLutBand(c), 0);
    }

    /**
     * @param volumeProperty
     * @param lut
     */
    private void setupColorProperties(LUT lut)
    {
        for (int comp = 0; comp < lut.getNumComponents(); comp++)
            setupColorProperties(lut.getLutBand(comp), comp);
    }

    private void setDefaultOpacity(LUT lut)
    {
        for (LUTBand lutBand : lut.getLutBands())
            setDefaultOpacity(lutBand);
    }

    private void setDefaultOpacity(LUTBand lutBand)
    {
        final IcyColorMapBand alphaBand = lutBand.getColorMap().alpha;

        alphaBand.beginUpdate();
        try
        {
            alphaBand.removeAllControlPoint();
            alphaBand.setControlPoint(0, 0f);
            alphaBand.setControlPoint(32, 0.02f);
            alphaBand.setControlPoint(255, 0.4f);
        }
        finally
        {
            alphaBand.endUpdate();
        }
    }

    private void restoreOpacity(LUT srcLut, LUT dstLut)
    {
        final int numComp = Math.min(srcLut.getNumComponents(), dstLut.getNumComponents());

        for (int c = 0; c < numComp; c++)
            retoreOpacity(srcLut.getLutBand(c), dstLut.getLutBand(c));
    }

    private void retoreOpacity(LUTBand srcLutBand, LUTBand dstLutBand)
    {
        dstLutBand.getColorMap().alpha.copyFrom(srcLutBand.getColorMap().alpha);
    }

    private void restoreColormap(LUT lut)
    {
        lut.copyScalers(lutSave);
        lut.copyColormaps(lutSave);
    }

    private void saveColormap(LUT lut)
    {
        lutSave.copyScalers(lut);
        lutSave.copyColormaps(lut);
    }

    /**
     * @param volumeProperty
     * @param lutBand
     */
    private void setupColorProperties(LUTBand lutBand, int index)
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

        for (int i = 0; i < IcyColorMap.SIZE; i++)
            pwf.AddPoint(scaler.unscale(i), colorMap.getNormalizedAlpha(i));

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

    private double[] getScaling()
    {
        final double[] result = new double[3];
        final Sequence seq = getSequence();

        if (seq != null)
        {
            result[0] = seq.getPixelSizeX();
            result[1] = seq.getPixelSizeY();
            result[2] = seq.getPixelSizeZ();
        }

        return result;
    }

    private void setupVolumeScaling()
    {
        // update volume scale
        volume.SetScale(scaling);
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
            volumeImageSampleDistanceCombo.setEnabled(true);
        else
            volumeImageSampleDistanceCombo.setEnabled(false);

        // rebuild volume mapper
        buildVolumeMapper();
        // rebuild component panel
        buildComponentPanel(false);
    }

    private void setupVolumeComponent()
    {
        final int newPosC;
        final int posC = getPositionC();

        // get new component value
        if (useRaycastVolumeMapper())
            newPosC = volumeComponentCombo.getSelectedIndex() - 1;
        else
            newPosC = volumeComponentCombo.getSelectedIndex();

        // changed ?
        if (posC != newPosC)
            // set new component value
            setPositionC(newPosC);
    }

    /**
     * update T slider state
     */
    void updateTNav()
    {
        final int maxT = getMaxT();
        final int t = getPositionT();

        tNav.setMaximum(maxT);
        if (t != -1)
            tNav.setValue(t);
        tNav.setVisible(maxT > 0);
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
     * Get X scaling for image volume rendering
     */
    public double getXScaling()
    {
        return scaling[0];
    }

    /**
     * Get Y scaling for image volume rendering
     */
    public double getYScaling()
    {
        return scaling[1];
    }

    /**
     * Get Z scaling for image volume rendering
     */
    public double getZScaling()
    {
        return scaling[2];
    }

    /**
     * Set X scaling for image volume rendering
     */
    public void setXScaling(double value)
    {
        if (scaling[0] != value)
        {
            scaling[0] = value;

            // update scaling
            setupVolumeScaling();
            // refresh rendering
            refresh();
        }
    }

    /**
     * Set Y scaling for image volume rendering
     */
    public void setYScaling(double value)
    {
        if (scaling[1] != value)
        {
            scaling[1] = value;

            // update scaling
            setupVolumeScaling();
            // refresh rendering
            refresh();
        }
    }

    /**
     * Set Z scaling for image volume rendering
     */
    public void setZScaling(double value)
    {
        if (scaling[2] != value)
        {
            scaling[2] = value;

            // update scaling
            setupVolumeScaling();
            // refresh rendering
            refresh();
        }
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
            // wait while processing 3D rendering
            while (processor.isProcessing())
                ThreadUtil.sleep(10);

            final vtkRenderWindow renderWindow = renderer.GetRenderWindow();
            final int[] size = renderWindow.GetSize();
            final vtkUnsignedCharArray array = new vtkUnsignedCharArray();

            // VTK need this to be called in the EDT :-(
            ThreadUtil.invokeNow(new Runnable()
            {
                @Override
                public void run()
                {
                    // NOTE: in vtk the [0,0] pixel is bottom left, so a vertical flip is required
                    renderWindow.GetRGBACharPixelData(0, 0, size[0] - 1, size[1] - 1, 1, array);
                }
            });

            // convert the vtk array into a IcyBufferedImage
            final byte[] data = array.GetJavaArray();
            final IcyBufferedImage image = new IcyBufferedImage(size[0], size[1], 4, DataType.UBYTE);
            final byte[][] c_xy = image.getDataXYCAsByte();

            int offset = 0;
            // loop along y and x, without forgetting the vertical flip
            for (int y = size[1] - 1; y >= 0; y--)
            {
                int xy = y * size[0];

                for (int x = 0; x < size[0]; x++, xy++)
                {
                    c_xy[0][xy] = data[offset++]; // R
                    c_xy[1][xy] = data[offset++]; // G
                    c_xy[2][xy] = data[offset++]; // B
                    c_xy[3][xy] = data[offset++]; // 1
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
            throw new UnsupportedOperationException("getRenderedImage(..) with z != -1 not supported.");

        return getRenderedImage(t, c);
    }

    private void documentChanged(Document doc)
    {
        if ((doc == specularTextField.getDocument()) || (doc == specularPowerTextField.getDocument()))
        {
            setupSpecular();
            refresh();
        }
        else if (doc == volumeZSpacing.getDocument())
        {
            try
            {
                setZScaling(Double.parseDouble(volumeZSpacing.getText()));
            }
            catch (Exception ex)
            {
                // ignore
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
        processor.addTask(displayRefresher, false);
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

        if (e.getSource() == volumeComponentCombo)
            setupVolumeComponent();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        documentChanged(e.getDocument());
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        documentChanged(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        documentChanged(e.getDocument());
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
                    final int curT = getPositionT();

                    // ensure T slider position is correct
                    if (curT != -1)
                        tNav.setValue(curT);
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
            setupColorProperties(lut.getLutBand(component), component);
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

        // check if X,Y or Z resolution changed
        if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_X))
            setXScaling(getSequence().getPixelSizeX());
        if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Y))
            setYScaling(getSequence().getPixelSizeY());
        if (StringUtil.equals(metadataName, Sequence.ID_PIXEL_SIZE_Z))
            volumeZSpacing.setText(StringUtil.toString(getSequence().getPixelSizeZ()));
    }

    @Override
    protected void sequenceTypeChanged()
    {
        super.sequenceTypeChanged();

        if (!initialized)
            return;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // rebuild component panel and data if needed
                buildComponentPanel(false);
            }
        });
    }

    @Override
    protected void sequenceDataChanged(IcyBufferedImage image, SequenceEventType type)
    {
        super.sequenceDataChanged(image, type);

        if (!initialized)
            return;

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // update slider bounds if needed
                updateTNav();
            }
        });

        // rebuild image data and refresh
        buildImageData();
        refresh();
    }

    @Override
    public void layersChanged(LayersEvent event)
    {
        super.layersChanged(event);

        if (!initialized)
            return;

        // refresh
        refresh();
    }

    @Override
    protected void layerRemoved(Layer layer)
    {
        super.layerRemoved(layer);

        if (layer != null)
        {
            // remove painter actor from the vtk render
            final Painter painter = layer.getPainter();

            if (painter instanceof VtkPainter)
            {
                final VtkPainter vp = (VtkPainter) painter;

                for (vtkActor actor : vp.getActors())
                    renderer.RemoveActor(actor);
                for (vtkActor2D actor : vp.getActors2D())
                    renderer.RemoveActor2D(actor);
            }
        }
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
