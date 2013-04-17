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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Simple image overlay class.
 */
public class ImageOverlay extends Overlay
{
    public Image image;

    public ImageOverlay(String name, Image image)
    {
        super(name, OverlayPriority.IMAGE_NORMAL);

        this.image = image;
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (g != null)
            g.drawImage(image, 0, 0, null);
    }
}
