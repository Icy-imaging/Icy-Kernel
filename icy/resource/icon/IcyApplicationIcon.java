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
package icy.resource.icon;

import icy.resource.ResourceUtil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * @author Stephane
 */
public class IcyApplicationIcon implements ResizableIcon
{
    private static final int DEFAULT_SIZE = 32;

    private final Dimension dim;
    private Icon icon;

    public IcyApplicationIcon()
    {
        super();

        dim = new Dimension();
        setDimension(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
    }

    @Override
    public void setDimension(Dimension value)
    {
        if (!dim.equals(value))
        {
            final int w = value.width;

            dim.setSize(w, value.height);

            if (w > 32)
                icon = ResourceUtil.getImageIcon(ResourceUtil.IMAGE_ICY_256, w);
            else if (w > 16)
                icon = ResourceUtil.getImageIcon(ResourceUtil.IMAGE_ICY_32, w);
            else
                icon = ResourceUtil.getImageIcon(ResourceUtil.IMAGE_ICY_16, w);
        }
    }

    @Override
    public int getIconHeight()
    {
        return dim.height;
    }

    @Override
    public int getIconWidth()
    {
        return dim.width;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        icon.paintIcon(c, g, x, y);
    }
}
