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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see vtk.vtkPanel#setSize(int, int)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void setSize(int w, int h)
    {
        // have to use this to by-pass the wrong vtkPanel implementation
        resize(w, h);
    }

    /**
     * return true if currently rendering
     */
    public boolean isRendering()
    {
        return rendering;
    }
}
