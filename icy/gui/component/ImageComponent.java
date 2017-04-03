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
package icy.gui.component;

import icy.image.ImageUtil;
import icy.resource.ResourceUtil;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * @author stephane
 */
public class ImageComponent extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1448746815524070306L;

    private Image image;
    private BufferedImage cachedImage;
    protected boolean forceUpdateCache;

    /**
     * @deprecated Use {@link #ImageComponent(Image)} instead
     */
    @Deprecated
    public ImageComponent(Image image, Dimension d)
    {
        this(image);
    }

    /**
     * @deprecated Use {@link #ImageComponent(Image)} instead
     */
    @Deprecated
    public ImageComponent(Image image, int width, int height)
    {
        this(image);
    }

    /**
     * @param image
     */
    public ImageComponent(Image image)
    {
        super(true);

        this.image = image;

        if (image != null)
        {
            // be sure image data are ready
            ImageUtil.waitImageReady(image);
            setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
        }

        forceUpdateCache = true;
    }

    public ImageComponent()
    {
        this(null);
    }

    /**
     * @return the image
     */
    public Image getImage()
    {
        return image;
    }

    /**
     * @param image
     *        the image to set
     */
    public void setImage(Image image)
    {
        if (this.image != image)
        {
            this.image = image;
            forceUpdateCache = true;
            repaint();
        }
    }

    protected void updateCache()
    {
        cachedImage = null;

        if (image == null)
            return;

        // be sure image data are ready
        ImageUtil.waitImageReady(image);

        float ix = image.getWidth(null);
        float iy = image.getHeight(null);

        // something wrong here --> use 'fault' image
        if ((ix <= 0f) || (iy <= 0f))
        {
            image = ResourceUtil.ICON_DELETE;
            ix = image.getWidth(null);
            iy = image.getHeight(null);
        }

        // we want a minimal appearance of 100,100
        final float w = Math.max(getWidth(), 100);
        final float h = Math.max(getHeight(), 100);

        if ((w > 0f) && (h > 0f))
        {
            final float sx = w / ix;
            final float sy = h / iy;
            final float s = Math.min(sx, sy);
            final int nix = (int) (ix * s);
            final int niy = (int) (iy * s);

            if ((nix > 0) && (niy > 0))
            {
                // need to rebuild cached image ?
                if (forceUpdateCache || (cachedImage == null) || (nix != cachedImage.getWidth())
                        || (niy != cachedImage.getHeight()))
                {
                    cachedImage = ImageUtil.scaleQuality(image, nix, niy);
                    forceUpdateCache = false;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        updateCache();

        if (cachedImage != null)
            g.drawImage(cachedImage, (getWidth() - cachedImage.getWidth()) / 2,
                    (getHeight() - cachedImage.getHeight()) / 2, null);
    }
}
