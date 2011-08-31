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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

public class TextPainter extends AbstractPainter
{
    public double t, z, x, y;
    public String text;
    private Color backgroundColor;
    private Color color;
    private boolean AllowPaintIn3D = true;
    private boolean visible = true;
    Rectangle activeRect = new Rectangle(10, 10, 20, 20);

    public TextPainter(Sequence s, double t, double z, double x, double y, String text)
    {
        super(s);

        this.t = t;
        this.z = z;
        this.x = x;
        this.y = y;
        this.text = text;
        color = Color.WHITE;
        backgroundColor = Color.BLACK;
    }

    public void moveTo(double x, double y)
    {
        if ((this.x != x) || (this.y != y))
        {
            this.x = x;
            this.y = y;

            changed();
        }
    }

    @Override
    public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas)
    {
        if (e.getKeyChar() == KeyEvent.VK_M && EventUtil.isShiftDown(e))
            setVisible(false);
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

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // AffineTransform transform = g2.getTransform();

        // s.getViewer().getOrthoView().getCanvas().getScale();

        // g2.transform( AffineTransform.getScaleInstance( 0.5, 0.5 ) );

        // g2.translate( x , y );
        // double scale = 1. / s.getViewer().getOrthoView().getCanvas().getScale() ;
        // g2.scale( scale, scale );
        // g2.translate( -x , -y );

        // g2.transform( AffineTransform.getScaleInstance( 0.5, 0.5 ) );

        // g2.translate( x , y );
        // g2.rotate( 0 );
        // g2.translate( -x , -y );

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics(g.getFont());

        activeRect.setBounds((int) x - fm.stringWidth(text) / 2, (int) y - fm.getHeight() / 2, fm.stringWidth(text),
                fm.getHeight());
        activeRect.grow(2, 2);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setColor(backgroundColor);
        g.fillRect(activeRect.x, activeRect.y, activeRect.width, activeRect.height);
        g.setColor(color);
        // g2.drawRect( activeRect.x, activeRect.y, activeRect.width, activeRect.height );

        g.drawRoundRect(activeRect.x, activeRect.y, activeRect.width, activeRect.height, 6, 6);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawString(text, (float) x - (fm.stringWidth(text) / 2f), (float) y + (fm.getHeight() / 2f) - 2);

        // g2.setTransform( transform );

    }

    // public void paint(Graphics3D g) {
    // if(s.getSelectedT() != t) return;
    // if(g == null ) return;
    // if(!AllowPaintIn3D) return;
    //
    // g.setColor(Color.WHITE);
    // g.drawString( text , x, y, z);
    // }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        if (this.backgroundColor != backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            changed();
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

    public boolean isAllowPaintIn3D()
    {
        return AllowPaintIn3D;
    }

    public void setAllowPaintIn3D(boolean allowPaintIn3D)
    {
        AllowPaintIn3D = allowPaintIn3D;
    }

    public boolean isVisible()
    {
        return visible;
    }

}