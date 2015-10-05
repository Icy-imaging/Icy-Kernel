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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link Overlay} class instead.
 */
@Deprecated
public abstract class PainterAdapter implements Painter
{
    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void mouseClick(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {

    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {

    }
}
