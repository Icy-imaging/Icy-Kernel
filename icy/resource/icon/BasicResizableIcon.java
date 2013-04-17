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

package icy.resource.icon;

import icy.resource.ResourceUtil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * @author fab & stephane
 */
public class BasicResizableIcon implements ResizableIcon
{
    protected static final int DEFAULT_ICONSIZE = 48;

    protected ImageIcon icon;
    protected final Image image;
    protected Dimension dim;

    public BasicResizableIcon(Image image)
    {
        super();

        this.image = image;

        if (image != null)
            dim = new Dimension(image.getWidth(null), image.getHeight(null));
        else
            dim = new Dimension();

        buildIcon(dim.width);
    }

    public BasicResizableIcon(ImageIcon srcIcon)
    {
        this(srcIcon.getImage());
    }

    public BasicResizableIcon()
    {
        this((Image) null);
    }

    protected void buildIcon(int size)
    {
        if (image != null)
            icon = ResourceUtil.getImageIcon(image, size);
        else
            icon = null;
    }

    @Override
    public void setDimension(Dimension dim)
    {
        this.dim = dim;

        if (icon != null)
        {
            final int size = (int) dim.getWidth();

            if (size != icon.getIconWidth())
                buildIcon(size);
        }
    }

    @Override
    public int getIconHeight()
    {
        if (icon != null)
            return icon.getIconHeight();

        return dim.height;
    }

    @Override
    public int getIconWidth()
    {
        if (icon != null)
            return icon.getIconWidth();

        return dim.width;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        if (icon != null)
            icon.paintIcon(c, g, x, y);
        else
        {
            g.drawLine(x, y, x + dim.width, y + dim.height);
            g.drawLine(x + dim.width, y, x, y + dim.height);
        }
    }
}
