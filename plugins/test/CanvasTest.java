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
package plugins.test;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import vtk.vtkColorTransferFunction;
import vtk.vtkFixedPointVolumeRayCastMapper;
import vtk.vtkPanel;
import vtk.vtkPiecewiseFunction;
import vtk.vtkRenderer;
import vtk.vtkStructuredPoints;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVolume;
import vtk.vtkVolumeProperty;
import vtk.vtkVolumeRayCastCompositeFunction;

/**
 * @author stephane
 */
public class CanvasTest extends IcyCanvas
{
    /**
     * 
     */
    private static final long serialVersionUID = -8733932806535746145L;

    private static final int shade = 0;
    private static final int interp = 0;

    /**
     * 
     */
    private final vtkPanel renderPanel;
    /**
     * 
     */
    private final vtkRenderer renderer;

    /**
     * 
     */
    private final vtkVolumeProperty volumeProperty;
    /**
     * 
     */
    private final vtkVolumeRayCastCompositeFunction volumeCompositeFunction;
    /**
     * 
     */
    private final vtkFixedPointVolumeRayCastMapper volumeMapper;
    /**
     * 
     */
    private final vtkVolume volume;

    /**
     * @param viewer
     */
    public CanvasTest(Viewer viewer)
    {
        super(viewer);

        renderPanel = new vtkPanel();
        renderPanel.addKeyListener(this);

        renderer = renderPanel.GetRenderer();

        // IcyFrame f = new IcyFrame("test", true, true, true, true);
        // f.addToMainDesktopPane();
        // f.setSize(400, 300);
        // f.setLocation(50, 50);
        // f.setLayout(new BorderLayout());
        // f.add(renderPanel, BorderLayout.CENTER);
        // f.setVisible(true);

        // add renderPanel to the main panel
        setLayout(new BorderLayout());
        add(renderPanel, BorderLayout.CENTER);

        // volume properties
        volumeProperty = new vtkVolumeProperty();

        // volumeProperty.SetColor(getColorTransfertFunction(true));
        // volumeProperty.SetScalarOpacity(getPiecewiseFunction(true));
        volumeProperty.SetColor(0, getColorTransfertFunction(Color.blue));
        volumeProperty.SetColor(1, getColorTransfertFunction(Color.green));
        volumeProperty.SetColor(2, getColorTransfertFunction(Color.red));

        volumeProperty.SetScalarOpacity(0, getPiecewiseFunction(true));
        volumeProperty.SetScalarOpacity(1, getPiecewiseFunction(true));
        volumeProperty.SetScalarOpacity(2, getPiecewiseFunction(true));

        volumeProperty.SetInterpolationType(interp);
        volumeProperty.SetShade(shade);

        // composite function
        volumeCompositeFunction = new vtkVolumeRayCastCompositeFunction();

        // volume mapper
        volumeMapper = new vtkFixedPointVolumeRayCastMapper();

        // volumeMapper.SetVolumeRayCastFunction(volumeCompositeFunction);
        // volumeMapper.IntermixIntersectingGeometryOn();

        updateData();

        // volume
        volume = new vtkVolume();

        volume.SetMapper(volumeMapper);
        volume.SetProperty(volumeProperty);

        renderer.AddVolume(volume);

        renderer.ResetCamera();
        renderer.ResetCameraClippingRange();
    }

    @Override
    public void shutDown()
    {
        // TODO Auto-generated method stub

    }

    public void updateData()
    {
        // final Sequence sequence = getSequence();
        // final int sizeX = sequence.getSizeX();
        // final int sizeY = sequence.getSizeY();
        // final int sizeZ = sequence.getSizeZ();
        // final int sizeC = sequence.getSizeC();
        // final int dataType = sequence.getDataType();
        // final int posT = getT();

        // final vtkImageData imageData = new vtkImageData();
        // imageData.SetDimensions(sizeX, sizeY, sizeZ);
        // imageData.SetNumberOfScalarComponents(3);
        // // ICYvolume.imageData.SetNumberOfScalarComponents(sizeC);
        // imageData.SetSpacing(1, 1, 5);
        // imageData.SetWholeExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);

        final vtkStructuredPoints points = new vtkStructuredPoints();

        points.ReleaseData();

        // Set data dimensions, spacing and origin
        points.SetDimensions(100, 100, 100);
        points.SetSpacing(1, 1, 1);
        points.SetOrigin(50, 50, 50);
        points.SetNumberOfScalarComponents(1);
        points.SetScalarTypeToUnsignedChar();
        points.AllocateScalars();

        final vtkUnsignedCharArray array = (vtkUnsignedCharArray) points.GetPointData().GetScalars();

        // Create some data (a sphere with a 10 unit thick plane through)
        int cnt = 0;
        double dx, dy, dz, r;
        for (int cmp = 0; cmp < 1; cmp++)
        {
            for (int cnt0 = 0; cnt0 < 100; cnt0++)
            {
                for (int cnt1 = 0; cnt1 < 100; cnt1++)
                {
                    for (int cnt2 = 0; cnt2 < 100; cnt2++, cnt++)
                    {
                        dx = (double) cnt0 - 50;
                        dy = (double) cnt1 - 50;
                        dz = (double) cnt2 - 50;
                        r = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (r < 30 || Math.abs(dx) < 5)
                            array.SetTuple1(cnt, 255);
                        // array.SetTuple3(cnt, 255, 200, 172);
                        // array.SetValue(cnt, (char) 128);
                        else
                            array.SetTuple1(cnt, 0);
                        // array.SetTuple3(cnt, 0, 0, 0);
                        // array.SetValue(cnt, (char) 0);
                    }
                }
            }
        }

        // points.Update();
        // points.UpdateData();
        // points.UpdateInformation();

        volumeMapper.SetInput(points);
    }

    private vtkColorTransferFunction getColorTransfertFunction(Color color)
    {
        final vtkColorTransferFunction result = new vtkColorTransferFunction();

        final float[] colors = color.getColorComponents(null);
        final float r = colors[0];
        final float g = colors[1];
        final float b = colors[2];

        result.AddRGBPoint(0.0, 0.0, 0.0, 0.0);
        result.AddRGBPoint(128.0, r, g, b);
        result.AddRGBPoint(255.0, r, g, b);

        return result;
    }

    private vtkPiecewiseFunction getPiecewiseFunction(boolean standard)
    {
        final vtkPiecewiseFunction result = new vtkPiecewiseFunction();

        if (standard)
        {
            // Standard
            result.AddPoint(0, 0.000);
            result.AddPoint(128, 0.02);
            result.AddPoint(255, 0.2);
        }
        else
        {
            // colourful
            result.AddPoint(0, 0.000);
            result.AddPoint(64, 0.0);
            result.AddPoint(255, 0.2);
        }

        return result;
    }

    /**
     * @return the renderer
     */
    public vtkRenderer getRenderer()
    {
        return renderer;
    }

    @Override
    public Component getViewComponent()
    {
        return renderPanel;
    }

    /**
     * @return the renderPanel
     */
    public vtkPanel getRenderPanel()
    {
        return renderPanel;
    }

    @Override
    public void refresh()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
