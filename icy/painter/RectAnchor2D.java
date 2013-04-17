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

import icy.util.EventUtil;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

/**
 * Anchor for rectangular shape.<br>
 * Support special rectangular drag operation when shift is maintained.
 * 
 * @author Stephane
 */
public abstract class RectAnchor2D extends Anchor2D
{
    public RectAnchor2D(Point2D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), color, selectedColor);
    }

    @Override
    protected boolean updateDrag(InputEvent e, Point2D imagePoint)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        final Anchor2D anchor = getOppositePoint();

        // shift action --> square drag
        if (EventUtil.isShiftDown(e) && (anchor != null))
        {
            final Point2D pos = anchor.getPosition();

            double dx = imagePoint.getX() - pos.getX();
            double dy = imagePoint.getY() - pos.getY();

            final double absDx = Math.abs(dx);
            final double absDy = Math.abs(dy);

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

            // set new position
            setPosition(new Point2D.Double(pos.getX() + dx, pos.getY() + dy));
        }
        else
        {
            // normal drag
            final double dx = imagePoint.getX() - startDragMousePosition.getX();
            final double dy = imagePoint.getY() - startDragMousePosition.getY();

            // set new position
            setPosition(new Point2D.Double(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy));
        }

        return true;
    }

    protected abstract Anchor2D getOppositePoint();
}
