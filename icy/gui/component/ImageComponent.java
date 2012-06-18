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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

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

    /**
     * @param image
     */
    public ImageComponent(Image image, Dimension d)
    {
        super(true);

        this.image = image;

        final Dimension dim;

        if (d != null)
            dim = d;
        else if (image != null)
            dim = new Dimension(image.getWidth(this), image.getHeight(this));
        else
            dim = new Dimension(320, 200);

        setPreferredSize(dim);

        setVisible(true);
    }

    /**
     * @param image
     */
    public ImageComponent(Image image)
    {
        this(image, null);
    }

    /**
     * @param image
     */
    public ImageComponent(Image image, int width, int height)
    {
        this(image, new Dimension(width, height));
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
            repaint();
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }

}
