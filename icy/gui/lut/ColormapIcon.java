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
package icy.gui.lut;

import icy.image.colormap.IcyColorMap;
import icy.util.ColorUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * @author Stephane
 */
public class ColormapIcon implements Icon
{
    private final IcyColorMap colormap;
    private int w;
    private int h;

    public ColormapIcon(IcyColorMap colormap, int width, int height)
    {
        super();

        this.colormap = colormap;
        w = (width <= 0) ? 64 : width;
        h = (height <= 0) ? 20 : height;
    }

    public ColormapIcon(IcyColorMap colormap)
    {
        this(colormap, 64, 20);
    }

    public IcyColorMap getColormap()
    {
        return colormap;
    }

    public void setWidth(int value)
    {
        // width >= 8
        w = Math.min(8, value);
    }

    public void setHeight(int value)
    {
        h = value;
    }

    private int pixToIndex(int pixel)
    {
        final float ratio = (float) (IcyColorMap.SIZE - 1) / (float) (w - 1);
        return (int) (pixel * ratio);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        for (int i = 0; i < w; i++)
        {
            // get current color from pixel position
            final Color curColor = colormap.getColor(pixToIndex(i));
            final Color grayMixed = ColorUtil.mixOver(Color.gray, curColor);
            final Color whiteMixed = ColorUtil.mixOver(Color.white, curColor);

            for (int j = 0; j < h; j += 4)
            {
                // set graphics color
                if (((i ^ j) & 4) != 0)
                    g2.setColor(grayMixed);
                else
                    g2.setColor(whiteMixed);

                g2.drawLine(i + x, j + y, i + x, j + y + 3);
            }
        }

        g2.dispose();
    }

    @Override
    public int getIconWidth()
    {
        return w;
    }

    @Override
    public int getIconHeight()
    {
        return h;
    }
}
