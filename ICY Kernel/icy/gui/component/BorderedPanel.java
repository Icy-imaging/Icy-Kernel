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
package icy.gui.component;

import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class BorderedPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -8365817252289450916L;

    private static final int DEFAULT_BORDER_SIZE = 4;

    private int borderWidth;
    private int borderHeight;

    /**
     * @param borderWidth
     * @param borderHeight
     * @param isDoubleBuffered
     */
    public BorderedPanel(int borderWidth, int borderHeight, boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);

        this.borderWidth = borderWidth;
        this.borderHeight = borderHeight;
    }

    /**
     * @param borderWidth
     * @param borderHeight
     */
    public BorderedPanel(int borderWidth, int borderHeight)
    {
        this(borderWidth, borderHeight, true);
    }

    /**
     * @param isDoubleBuffered
     */
    public BorderedPanel(boolean isDoubleBuffered)
    {
        this(DEFAULT_BORDER_SIZE, DEFAULT_BORDER_SIZE, isDoubleBuffered);
    }

    /**
     * 
     */
    public BorderedPanel()
    {
        this(DEFAULT_BORDER_SIZE, DEFAULT_BORDER_SIZE, true);
    }

    public int getClientY()
    {
        return borderHeight;
    }

    public int getClientX()
    {
        return borderWidth;
    }

    public int getClientHeight()
    {
        final int h = getHeight() - (borderHeight * 2);

        if (h > 0)
            return h;

        return 0;
    }

    public int getClientWidth()
    {
        final int w = getWidth() - (borderWidth * 2);

        if (w > 0)
            return w;

        return 0;
    }

}
