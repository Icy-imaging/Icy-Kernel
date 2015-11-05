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
package icy.vtk;

import icy.preferences.CanvasPreferences;
import icy.util.EventUtil;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;

import vtk.vtkActor;
import vtk.vtkActorCollection;
import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkLight;
import vtk.vtkPropPicker;
import vtk.vtkRenderer;

/**
 * @author stephane dallongeville
 */
public class IcyVtkPanel extends VtkJoglPanel implements MouseListener, MouseMotionListener, MouseWheelListener,
        KeyListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -8455671369400627703L;

    protected Timer timer;
    protected vtkPropPicker picker;
    protected vtkAxesActor axis;
    protected vtkRenderer axisRenderer;
    protected vtkCamera axisCam;
    protected int axisOffset[];
    protected double axisScale;
    protected boolean lightFollowCamera;

    public IcyVtkPanel()
    {
        super();

        // used for restore quality rendering after mouse wheel
        timer = new Timer("Timer - vtkPanel");
        // picker
        picker = new vtkPropPicker();
        // set ambient color to white
        lgt.SetAmbientColor(1d, 1d, 1d);
        lightFollowCamera = true;

        // initialize axis
        axisRenderer = new vtkRenderer();
        // BUG: with OpenGL window the global render window viewport is limited to the last layer viewport dimension
        // axisRenderer.SetViewport(0.0, 0.0, 0.2, 0.2);
        axisRenderer.SetLayer(1);
        axisRenderer.InteractiveOff();

        rw.AddRenderer(axisRenderer);
        rw.SetNumberOfLayers(2);

        axisCam = axisRenderer.GetActiveCamera();

        axis = new vtkAxesActor();
        axisRenderer.AddActor(axis);

        // default axis offset and scale
        axisOffset = new int[] {124, 124};
        axisScale = 0.4;

        // reset camera
        axisCam.SetViewUp(0, -1, 0);
        axisCam.Elevation(210);
        axisCam.SetParallelProjection(1);
        axisRenderer.ResetCamera();
        axisRenderer.ResetCameraClippingRange();

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
    }

    @Override
    protected void delete()
    {
        // important to release timer here
        timer.cancel();

        super.delete();

        lock.lock();
        try
        {
            // release VTK objects
            axisCam = null;
            axis = null;
            axisRenderer = null;
            picker = null;

            // call it once in parent as this can take a lot fo time
            // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
        }
        finally
        {
            // removing the renderWindow is let to the superclass
            // because in the very special case of an AWT component
            // under Linux, destroying renderWindow crashes.
            lock.unlock();
        }
    }

    @Override
    public void removeNotify()
    {
        // important to release timer here
        timer.cancel();

        super.removeNotify();
    }

    @Override
    public void sizeChanged()
    {
        super.sizeChanged();

        updateAxisView();
    }

    /**
     * Return picker object.
     */
    public vtkPropPicker getPicker()
    {
        return picker;
    }

    /**
     * Return the actor for axis orientation display.
     */
    public vtkAxesActor getAxesActor()
    {
        return axis;
    }

    public boolean getLightFollowCamera()
    {
        return lightFollowCamera;
    }

    /**
     * Return true if the axis orientation display is enabled
     */
    public boolean isAxisOrientationDisplayEnable()
    {
        return (axis.GetVisibility() == 0) ? false : true;
    }

    /**
     * Returns the offset from border ({X, Y} format) for the axis orientation display
     */
    public int[] getAxisOrientationDisplayOffset()
    {
        return axisOffset;
    }

    /**
     * Returns the scale factor (default = 0.4) for the axis orientation display
     */
    public double getAxisOrientationDisplayScale()
    {
        return axisScale;
    }

    /**
     * Set to <code>true</code> to automatically update light position to camera position when camera move.
     */
    public void setLightFollowCamera(boolean value)
    {
        lightFollowCamera = value;
    }

    /**
     * Return true if the axis orientation display is enabled
     */
    public void setAxisOrientationDisplayEnable(boolean value)
    {
        axis.SetVisibility(value ? 1 : 0);
        updateAxisView();
    }

    /**
     * Sets the offset from border ({X, Y} format) for the axis orientation display (default = {130, 130})
     */
    public void setAxisOrientationDisplayOffset(int[] value)
    {
        axisOffset = value;
        updateAxisView();
    }

    /**
     * Returns the scale factor (default = 0.4) for the axis orientation display
     */
    public void setAxisOrientationDisplayScale(double value)
    {
        axisScale = value;
        updateAxisView();
    }

    public void pickActor(int x, int y)
    {
        pick(x, y);
    }

    /**
     * Pick object at specified position and return it.
     */
    public vtkActor pick(int x, int y)
    {
        lock();
        try
        {
            picker.PickProp(x, rw.GetSize()[1] - y, ren);
        }
        finally
        {
            unlock();
        }

        return picker.GetActor();
    }

    /**
     * Translate specified camera view
     */
    public void translateView(vtkCamera c, vtkRenderer r, double dx, double dy)
    {
        // translation mode
        double FPoint[];
        double PPoint[];
        double APoint[] = new double[3];
        double RPoint[];
        double focalDepth;

        lock();
        try
        {
            // get the current focal point and position
            FPoint = c.GetFocalPoint();
            PPoint = c.GetPosition();

            // calculate the focal depth since we'll be using it a lot
            r.SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
            r.WorldToDisplay();
            focalDepth = r.GetDisplayPoint()[2];

            final int[] size = rw.GetSize();
            APoint[0] = (size[0] / 2.0) + dx;
            APoint[1] = (size[1] / 2.0) + dy;
            APoint[2] = focalDepth;
            r.SetDisplayPoint(APoint);
            r.DisplayToWorld();
            RPoint = r.GetWorldPoint();
            if (RPoint[3] != 0.0)
            {
                RPoint[0] = RPoint[0] / RPoint[3];
                RPoint[1] = RPoint[1] / RPoint[3];
                RPoint[2] = RPoint[2] / RPoint[3];
            }

            /*
             * Compute a translation vector, moving everything 1/2 the distance
             * to the cursor. (Arbitrary scale factor)
             */
            c.SetFocalPoint((FPoint[0] - RPoint[0]) / 2.0 + FPoint[0], (FPoint[1] - RPoint[1]) / 2.0 + FPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + FPoint[2]);
            c.SetPosition((FPoint[0] - RPoint[0]) / 2.0 + PPoint[0], (FPoint[1] - RPoint[1]) / 2.0 + PPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + PPoint[2]);
            r.ResetCameraClippingRange();
        }
        finally
        {
            unlock();
        }
    }

    /**
     * Rotate specified camera view
     */
    public void rotateView(vtkCamera c, vtkRenderer r, int dx, int dy)
    {
        lock();
        try
        {
            // rotation mode
            c.Azimuth(dx);
            c.Elevation(dy);
            c.OrthogonalizeViewUp();
            r.ResetCameraClippingRange();
        }
        finally
        {
            unlock();
        }
    }

    /**
     * Zoom current view by specified factor (negative value means unzoom)
     */
    public void zoomView(vtkCamera c, vtkRenderer r, double factor)
    {
        lock();
        try
        {
            if (c.GetParallelProjection() == 1)
                c.SetParallelScale(cam.GetParallelScale() / factor);
            else
            {
                c.Dolly(factor);
                r.ResetCameraClippingRange();
            }
        }
        finally
        {
            unlock();
        }
    }

    /**
     * Translate current camera view
     */
    public void translateView(double dx, double dy)
    {
        translateView(cam, ren, dx, dy);
        // adjust light position
        if (getLightFollowCamera())
            setLightToCameraPosition(lgt, cam);
    }

    /**
     * Rotate current camera view
     */
    public void rotateView(int dx, int dy)
    {
        // rotate world view
        rotateView(cam, ren, dx, dy);
        // adjust light position
        if (getLightFollowCamera())
            setLightToCameraPosition(lgt, cam);
        // update axis camera
        updateAxisView();
    }

    /**
     * Zoom current view by specified factor (negative value means unzoom)
     */
    public void zoomView(double factor)
    {
        // zoom world
        zoomView(cam, ren, factor);
        // update axis camera
        updateAxisView();
    }

    /**
     * Set the specified light at the same position than the specified camera
     */
    public static void setLightToCameraPosition(vtkLight l, vtkCamera c)
    {
        l.SetPosition(c.GetPosition());
        l.SetFocalPoint(c.GetFocalPoint());
    }

    /**
     * Set coarse and fast rendering mode immediately
     * 
     * @see #setCoarseRendering(long)
     */
    public void setCoarseRendering()
    {
        // set fast rendering
        rw.SetDesiredUpdateRate(10.0);
    }

    /**
     * Set fine (and possibly slow) rendering immediately
     * 
     * @see #setFineRendering(long)
     */
    public void setFineRendering()
    {
        // set quality rendering
        rw.SetDesiredUpdateRate(0.01);
    }

    /**
     * Set coarse and fast rendering for the specified amount of time (in ms, always when set to 0)
     */
    public void setCoarseRendering(long time)
    {
        // cancel pending task
        timer.cancel();
        // want fast update
        rw.SetDesiredUpdateRate(10.0);

        if (time > 0)
            setFineRendering(time);
    }

    /**
     * Set fine (and possibly slow) rendering after specified time delay (in ms)
     */
    public void setFineRendering(long delay)
    {
        // cancel pending task
        timer.cancel();

        if (delay > 0)
        {
            // schedule quality restoration
            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    // no parent --> exit
                    if (getParent() == null)
                        return;

                    // set back quality rendering
                    getRenderWindow().SetDesiredUpdateRate(0.01);
                    // request repaint
                    repaint();
                }
            }, delay);
        }
        else
            // set back quality rendering
            rw.SetDesiredUpdateRate(0.01);
    }

    /**
     * Update axis display depending the current scene camera view.<br>
     * You should call it after having modified camera settings.
     */
    public void updateAxisView()
    {
        if (!isWindowSet())
            return;

        lock();
        try
        {
            double pos[] = cam.GetPosition();
            final double fp[] = cam.GetFocalPoint();
            final double viewup[] = cam.GetViewUp();

            // mimic axis camera position to scene camera position
            axisCam.SetPosition(pos);
            axisCam.SetFocalPoint(fp);
            axisCam.SetViewUp(viewup);
            axisRenderer.ResetCamera();

            pos = axisCam.GetPosition();

            final int[] size = rw.GetSize();
            final double[] bnds = ren.ComputeVisiblePropBounds();
            final double range = (Math.abs(bnds[3] - bnds[2]) + Math.abs(bnds[1] - bnds[0])) / 2;

            // adjust scale
            final double scale = size[1] / 512d;
            // adjust offset
            final int w = (int) (size[0] - (axisOffset[0] * scale));
            final int h = (int) (size[1] - (axisOffset[1] * scale));

            // zoom and translate
            zoomView(axisCam, axisRenderer, axisScale * (range / 2d));
            translateView(axisCam, axisRenderer, -w, -h);
        }
        finally
        {
            unlock();
        }
    }

    @Override
    public void lock()
    {
        // if (!isWindowSet())
        // return;

        super.lock();
    }

    @Override
    public void unlock()
    {
        // if (!isWindowSet())
        // return;

        super.unlock();
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // nothing to do here
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // nothing to do here
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.isConsumed())
            return;

        // nothing to do here
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // just save mouse position
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // camera not yet defined --> exit
        if (cam == null)
            return;

        if (e.isConsumed())
            return;
        if (ren.VisibleActorCount() == 0)
            return;

        // cancel pending task
        timer.cancel();

        // get current mouse position
        final int x = e.getX();
        final int y = e.getY();
        int deltaX = (lastX - x);
        int deltaY = (lastY - y);

        // faster movement with control modifier
        if (EventUtil.isControlDown(e))
        {
            deltaX *= 3;
            deltaY *= 3;
        }

        if (EventUtil.isRightMouseButton(e) || (EventUtil.isLeftMouseButton(e) && EventUtil.isShiftDown(e)))
            // translation mode
            translateView(-deltaX * 2, deltaY * 2);
        else if (EventUtil.isLeftMouseButton(e))
            // rotation mode
            rotateView(deltaX, -deltaY);
        else
            // zoom mode
            zoomView(Math.pow(1.02, -deltaY));

        // save mouse position
        lastX = x;
        lastY = y;

        // request repaint
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.isConsumed())
            return;
        if (ren.VisibleActorCount() == 0)
            return;

        // want fast update
        setCoarseRendering(0);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // restore quality rendering
        setFineRendering(1000);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        // camera not yet defined --> exit
        if (cam == null)
            return;

        // want fast update
        setCoarseRendering(0);

        // get delta
        double delta = e.getWheelRotation() * CanvasPreferences.getMouseWheelSensitivity();
        if (CanvasPreferences.getInvertMouseWheelAxis())
            delta = -delta;

        // faster movement with control modifier
        if (EventUtil.isControlDown(e))
            delta *= 3d;

        zoomView(Math.pow(1.02, delta));

        // request repaint
        repaint();

        // restore quality rendering
        setFineRendering(1000);
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        //
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.isConsumed())
            return;
        if (ren.VisibleActorCount() == 0)
            return;

        vtkActorCollection ac;
        vtkActor anActor;
        int i;

        switch (e.getKeyChar())
        {
            case 'r': // reset camera
                resetCamera();
                repaint();
                break;

            case 'u': // picking
                pickActor(lastX, lastY);
                break;

            case 'w': // wireframe mode
                lock();
                try
                {
                    ac = ren.GetActors();
                    ac.InitTraversal();
                    for (i = 0; i < ac.GetNumberOfItems(); i++)
                    {
                        anActor = ac.GetNextActor();
                        anActor.GetProperty().SetRepresentationToWireframe();
                    }
                }
                finally
                {
                    unlock();
                }
                repaint();
                break;

            case 's':
                lock();
                try
                {
                    ac = ren.GetActors();
                    ac.InitTraversal();
                    for (i = 0; i < ac.GetNumberOfItems(); i++)
                    {
                        anActor = ac.GetNextActor();
                        anActor.GetProperty().SetRepresentationToSurface();
                    }
                }
                finally
                {
                    unlock();
                }
                repaint();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.isConsumed())
            return;
    }
}
