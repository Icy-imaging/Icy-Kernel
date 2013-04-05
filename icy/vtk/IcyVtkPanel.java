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
package icy.vtk;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import vtk.vtkPanel;

/**
 * @author stephane
 */
public class IcyVtkPanel extends vtkPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -8455671369400627703L;

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        rw.GetSize();
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
        // we don't want the mouse enter to request focus !
        // super.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // always do mouse exited process
        super.mouseExited(e);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (!e.isConsumed())
            super.mouseClicked(e);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // always do mouse moved process
        super.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (!e.isConsumed())
            super.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (!e.isConsumed())
            super.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // always do mouse release process
        super.mouseReleased(e);

        // so we have a fine rendering when action end
        repaint();
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

    /**
     * return true if currently rendering
     */
    public boolean isRendering()
    {
        return rendering;
    }
}
