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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Vector;

public class ColorChooserPainter extends AbstractPainter
{
    private Point position;
    private Color color;
    private Vector<Color> colorlist;
    private Rectangle activRect;
    private boolean visible = true;

    ColorChooserPainter(Sequence s, Point p)
    {
        super(s);

        colorlist = new Vector<Color>();

        colorlist.add(Color.RED);
        colorlist.add(Color.ORANGE);
        colorlist.add(Color.YELLOW);
        colorlist.add(Color.GREEN);
        colorlist.add(Color.BLUE);
        colorlist.add(Color.PINK);
        colorlist.add(Color.WHITE);
        colorlist.add(Color.GRAY);
        colorlist.add(Color.BLACK);

        color = Color.RED;
        activRect = new Rectangle(10, 10);
        position = new Point();
        moveTo(p.x, p.y);
    }

    public void moveTo(int x, int y)
    {
        if ((position.x != x) || (position.y != y))
        {
            position.x = x;
            position.y = y;
            activRect.setBounds(x, y, 10, colorlist.size() * 10);

            changed();
        }
    }

    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas q)
    {
        if (e.getKeyChar() == KeyEvent.VK_M && EventUtil.isShiftDown(e))
            setVisible(false);
    }

    @Override
    public void mouseClick(MouseEvent e, Point2D p, IcyCanvas q)
    {
        if (activRect.contains(p))
        {
            if (this.isVisible())
            {
                for (int i = 0; i < colorlist.size(); i++)
                {
                    Rectangle r = new Rectangle();
                    r.setBounds(position.x, position.y + 10 * i, 10, 10);
                    if (r.contains(p))
                    {
                        setColor(colorlist.get(i));
                        return;
                    }
                }
            }
        }

    }

    public void setVisible(boolean b)
    {
        if (visible != b)
        {
            visible = b;

            changed();
        }
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (!visible)
            return;

        for (int i = 0; i < colorlist.size(); i++)
        {
            g.setColor(colorlist.get(i));
            g.fillRect(position.x, position.y + 10 * i, 10, 10);
        }
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        if (this.color != color)
        {
            this.color = color;

            changed();
        }
    }

    public boolean isVisible()
    {
        return visible;
    }

    @Override
    public void keyReleased(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        // TODO Auto-generated method stub

    }
}
