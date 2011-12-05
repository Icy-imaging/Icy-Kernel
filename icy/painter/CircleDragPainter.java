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
package icy.painter;

import icy.canvas.IcyCanvas;
import icy.sequence.Sequence;
import icy.util.EventUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * FIXME : a nettoyer
 * 
 * @author fab
 * @deprecated Shouldn't be in kernel
 */
public class CircleDragPainter extends AbstractPainter
{
    public double t, z, x, y;
    public int nbUser; // nombre d'objet pointant sur cette 'ancre';

    Ellipse2D activeEllipse;
    Rectangle activeRect = null;
    boolean dragging = false;

    private static CircleDragPainter currently_dragging = null;

    /** list of anchors that will be dragged as this anchor will drag */
    ArrayList<CircleDragPainter> linkedCircle = new ArrayList<CircleDragPainter>();

    public CircleDragPainter(Sequence s, double t, double z, double x, double y)
    {
        super(s);

        this.t = t;
        this.z = z;
        this.x = x;
        this.y = y;
        nbUser = 1;
        activeEllipse = new Ellipse2D.Double(x - 10, y - 10, 20, 20);
    }

    public boolean contains(Point p)
    {
        return activeEllipse.contains(p);
    }

    public boolean contains(Point2D p)
    {
        return activeEllipse.contains(p);
    }

    public void releaseExclusiveDrag()
    {
        if (currently_dragging == this)
        {
            currently_dragging = null;
        }
        dragging = false;
    }

    public void moveTo(Point2D p)
    {
        final double newX = p.getX();
        final double newY = p.getY();

        if ((x != newX) || (y != newY))
        {
            activeEllipse.setFrame(newX - 10, newY - 10, 20, 20);
            x = newX;
            y = newY;

            changed();
        }
    }

    public void addLinkedCircle(CircleDragPainter circleDragPainter)
    {
        linkedCircle.add(circleDragPainter);
    }

    public void removeLinkedCircle(CircleDragPainter circleDragPainter)
    {
        linkedCircle.remove(circleDragPainter);
    }

    @Override
    public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        super.mouseMove(e, imagePoint, canvas);

        if (activeEllipse.contains(imagePoint))
        {
            if (EventUtil.isShiftDown(e))
            {
                if (currently_dragging == null || currently_dragging == this)
                {
                    dragging = true;
                    currently_dragging = this;
                }
            }
            else
            {
                if (currently_dragging == this)
                {
                    currently_dragging = null;
                }
                dragging = false;
            }
        }

        if (!EventUtil.isShiftDown(e))
        {
            if (currently_dragging == this)
            {
                currently_dragging = null;
            }
            dragging = false;
        }

        if (dragging)
        {
            // position offset
            double dx = imagePoint.getX() - x;
            double dy = imagePoint.getY() - y;

            moveTo(imagePoint);
            // drag linked anchors
            for (CircleDragPainter anchor : linkedCircle)
                anchor.moveTo(new Point2D.Double(anchor.x + dx, anchor.y + dy));
        }
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        super.paint(g, sequence, canvas);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // AlphaZ.getAlphaZ( g2 , s , z );

        // if(s.getSelectedT() == t)
        {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(4));
            g.drawOval(activeEllipse.getBounds().x, activeEllipse.getBounds().y, activeEllipse.getBounds().width,
                    activeEllipse.getBounds().height);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(activeEllipse.getBounds().x, activeEllipse.getBounds().y, activeEllipse.getBounds().width,
                    activeEllipse.getBounds().height);
            g.setStroke(new BasicStroke(1));
        }
    }

}
