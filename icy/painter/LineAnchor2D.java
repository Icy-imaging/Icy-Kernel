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

import icy.util.EventUtil;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

/**
 * Anchor for line type shape.<br>
 * Support special line drag operation when shift is maintained.
 * 
 * @author Stephane
 */
public abstract class LineAnchor2D extends Anchor2D
{
    public LineAnchor2D(Point2D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), color, selectedColor);
    }

    @Override
    protected boolean updateDrag(InputEvent e, double x, double y)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        final Anchor2D anchor = getPreviousPoint();

        // shift action --> special drag
        if (EventUtil.isShiftDown(e) && (anchor != null))
        {
            final Point2D pos = anchor.getPosition();

            double dx = x - pos.getX();
            double dy = y - pos.getY();

            final double absDx = Math.abs(dx);
            final double absDy = Math.abs(dy);
            final double dist;

            if ((absDx != 0) && (absDy != 0))
                dist = absDx / absDy;
            else
                dist = 0;

            // square drag
            if ((dist > 0.5) && (dist < 1.5))
            {
                // align to DY
                if (absDx > absDy)
                {
                    if (dx >= 0)
                        dx = absDy;
                    else
                        dx = -absDy;
                }
                // align to DX
                else
                {
                    if (dy >= 0)
                        dy = absDx;
                    else
                        dy = -absDx;
                }
            }
            else
            // one direction drag
            {
                // drag X
                if (absDx > absDy)
                    dy = 0;
                // drag Y
                else
                    dx = 0;
            }

            // set new position
            setPosition(pos.getX() + dx, pos.getY() + dy);
        }
        else
        {
            final double dx = x - startDragMousePosition.getX();
            final double dy = y - startDragMousePosition.getY();

            // set new position
            setPosition(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy);
        }

        return true;
    }

    protected abstract Anchor2D getPreviousPoint();
}
