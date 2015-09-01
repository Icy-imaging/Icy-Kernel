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

import icy.gui.util.LookAndFeelUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Icy Icon class.<br>
 * This class is used to display alpha mask or classic image icon.<br>
 * When the image is alpha type (default) default fill color is taken from the current
 * look and feel scheme but it can also be defined manually.
 * 
 * @author Stephane
 */
public class IcyIcon implements ResizableIcon
{
    public static final int DEFAULT_SIZE = 20;

    protected Image image;
    protected String name;
    protected Color color;
    protected boolean alpha;
    protected final Dimension dim;

    private BufferedImage cache;
    private BufferedImage alphaImage;

    private IcyIcon(String name, Image image, int size, boolean alpha)
    {
        super();

        this.image = image;
        this.name = name;
        color = null;
        this.alpha = alpha;

        dim = new Dimension(size, size);

        updateImage();
    }

    public IcyIcon(String name, int size, boolean alpha)
    {
        this(name, null, size, alpha);
    }

    public IcyIcon(String name, int size)
    {
        this(name, null, size, true);
    }

    public IcyIcon(String name, boolean alpha)
    {
        this(name, null, DEFAULT_SIZE, alpha);
    }

    public IcyIcon(String name)
    {
        this(name, null, DEFAULT_SIZE, true);
    }

    public IcyIcon(Image image, int size, boolean alpha)
    {
        this(null, image, size, alpha);
    }

    public IcyIcon(Image image, int size)
    {
        this(null, image, size, true);
    }

    public IcyIcon(Image image, boolean alpha)
    {
        this(null, image, DEFAULT_SIZE, alpha);
    }

    public IcyIcon(Image image)
    {
        this(null, image, DEFAULT_SIZE, true);
    }

    public IcyIcon(IcyIcon icon)
    {
        this(icon.getName(), icon.getImage(), icon.getIconWidth(), icon.getAlpha());
    }

    public int getSize()
    {
        return dim.width;
    }

    public void setSize(int size)
    {
        setDimension(new Dimension(size, size));
    }

    /**
     * @return the iconName
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param value
     *        the name to set
     */
    public void setName(String value)
    {
        if (name != value)
        {
            // can't set image and name at same time
            name = value;
            image = null;

            updateImage();
        }
    }

    /**
     * @return the image
     */
    public Image getImage()
    {
        return image;
    }

    /**
     * @param value
     *        the image to set
     */
    public void setImage(Image value)
    {
        if (image != value)
        {
            // can't set image and name at same time
            image = value;
            name = null;

            updateImage();
        }
    }

    /**
     * @return the fill color (null if we use the default LAF color).
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color
     *        the fill color to set (null = default LAF color)
     */
    public void setColor(Color color)
    {
        if (this.color != color)
        {
            this.color = color;
            update();
        }
    }

    /**
     * @deprecated Use {@link #getAlpha()} instead
     */
    @Deprecated
    public boolean isAlpha()
    {
        return getAlpha();
    }

    /**
     * @return the alpha
     */
    public boolean getAlpha()
    {
        return alpha;
    }

    /**
     * @param alpha
     *        use alpha or color image depending
     */
    public void setAlpha(boolean alpha)
    {
        if (this.alpha != alpha)
        {
            this.alpha = alpha;
            updateImage();
        }
    }

    private void updateImage()
    {
        if (!StringUtil.isEmpty(name))
        {
            if (alpha)
                image = ResourceUtil.getAlphaIconAsImage(name);
            else
                image = ResourceUtil.getColorIconAsImage(name);
        }

        update();
    }

    private void update()
    {
        if (image != null)
        {
            cache = ImageUtil.scaleQuality(image, dim.width, dim.height);
            alphaImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            cache = null;
            alphaImage = null;
        }
    }

    public Dimension getDimension()
    {
        return new Dimension(dim);
    }

    @Override
    public void setDimension(Dimension newDim)
    {
        // dimension changed
        if (!dim.equals(newDim))
        {
            dim.setSize(newDim);
            update();
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        if (alpha)
        {
            // alpha image cache defined ?
            if (alphaImage != null)
            {
                if (color == null)
                    LookAndFeelUtil.paintForegroundImageFromAlphaImage(c, cache, alphaImage);
                else
                    ImageUtil.paintColorImageFromAlphaImage(cache, alphaImage, color);
            }

            g.drawImage(alphaImage, x, y, null);
        }
        else
            g.drawImage(cache, x, y, null);
    }

    @Override
    public int getIconWidth()
    {
        return dim.width;
    }

    @Override
    public int getIconHeight()
    {
        return dim.height;
    }
}
