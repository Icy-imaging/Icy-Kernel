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
package icy.vtk;

import icy.preferences.CanvasPreferences;
import icy.util.EventUtil;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;

import vtk.vtkPanel;

/**
 * @author stephane
 */
public class IcyVtkPanel extends vtkPanel implements MouseWheelListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -8455671369400627703L;

    protected Timer timer;

    public IcyVtkPanel()
    {
        super();

        // used for restore quality rendering after mouse wheel
        timer = new Timer();

        // we want mouse wheel events
        addMouseWheelListener(this);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        // important to release timer here
        timer.cancel();
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        if (windowset == 1)
        {
            Lock();
            rw.SetSize(width, height);
            UnLock();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setSize(int w, int h)
    {
        // have to use this to by-pass the wrong vtkPanel implementation
        resize(w, h);
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
        {
            // translation mode
            double FPoint[];
            double PPoint[];
            double APoint[] = new double[3];
            double RPoint[];
            double focalDepth;

            // get the current focal point and position
            FPoint = cam.GetFocalPoint();
            PPoint = cam.GetPosition();

            // calculate the focal depth since we'll be using it a lot
            ren.SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
            ren.WorldToDisplay();
            focalDepth = ren.GetDisplayPoint()[2];

            APoint[0] = rw.GetSize()[0] / 2.0 - deltaX;
            APoint[1] = rw.GetSize()[1] / 2.0 + deltaY;
            APoint[2] = focalDepth;
            ren.SetDisplayPoint(APoint);
            ren.DisplayToWorld();
            RPoint = ren.GetWorldPoint();
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
            cam.SetFocalPoint((FPoint[0] - RPoint[0]) / 2.0 + FPoint[0], (FPoint[1] - RPoint[1]) / 2.0 + FPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + FPoint[2]);
            cam.SetPosition((FPoint[0] - RPoint[0]) / 2.0 + PPoint[0], (FPoint[1] - RPoint[1]) / 2.0 + PPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + PPoint[2]);
            resetCameraClippingRange();
        }
        else if (EventUtil.isLeftMouseButton(e))
        {
            // rotation mode
            cam.Azimuth(deltaX);
            cam.Elevation(-deltaY);
            cam.OrthogonalizeViewUp();
            resetCameraClippingRange();

            if (LightFollowCamera == 1)
            {
                lgt.SetPosition(cam.GetPosition());
                lgt.SetFocalPoint(cam.GetFocalPoint());
            }
        }
        else
        {
            // zoom mode
            final double zoomFactor = Math.pow(1.02, -deltaY);

            if (cam.GetParallelProjection() == 1)
                cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
            else
            {
                cam.Dolly(zoomFactor);
                resetCameraClippingRange();
            }
        }

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

        // cancel pending task
        timer.cancel();
        // want fast update
        rw.SetDesiredUpdateRate(10.0);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // cancel pending task
        timer.cancel();

        // schedule quality restoration
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                // set back quality rendering
                GetRenderWindow().SetDesiredUpdateRate(0.01);
                // request repaint
                repaint();
            }
        }, 1000);

        // // set back quality rendering
        // rw.SetDesiredUpdateRate(0.01);
        // // request repaint
        // repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        // camera not yet defined --> exit
        if (cam == null)
            return;

        // cancel pending task
        timer.cancel();
        // want fast update
        rw.SetDesiredUpdateRate(10.0);

        // get delta
        double delta = e.getWheelRotation() * CanvasPreferences.getMouseWheelSensitivity();
        if (CanvasPreferences.getInvertMouseWheelAxis())
            delta = -delta;

        // faster movement with control modifier
        if (EventUtil.isControlDown(e))
            delta *= 3d;

        final double zoomFactor = Math.pow(1.02, delta);

        if (cam.GetParallelProjection() == 1)
            cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
        else
        {
            cam.Dolly(zoomFactor);
            resetCameraClippingRange();
        }

        // request repaint
        repaint();

        // schedule quality restoration
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                // set back quality rendering
                GetRenderWindow().SetDesiredUpdateRate(0.01);
                // request repaint
                repaint();
            }
        }, 1000);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (!e.isConsumed())
            super.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (!e.isConsumed())
            super.keyReleased(e);
    }

    @Override
    public void lock()
    {
        if (windowset == 0)
            return;

        super.lock();
    }

    @Override
    public void unlock()
    {
        if (windowset == 0)
            return;

        super.unlock();
    }

    /**
     * return true if currently rendering
     */
    public boolean isRendering()
    {
        return rendering;
    }
}
