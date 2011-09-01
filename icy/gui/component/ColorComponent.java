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

import icy.util.ColorUtil;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * @author Stephane
 */
public class ColorComponent extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 2883762420253112984L;

    private Color color;

    public ColorComponent(Color color)
    {
        super(true);

        this.color = color;

        setOpaque(true);
        setVisible(true);
    }

    public ColorComponent()
    {
        this(null);
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color
     *        the color to set
     */
    public void setColor(Color color)
    {
        this.color = color;

        if (color != null)
            setToolTipText(ColorUtil.toString(color.getRGB(), false, " : "));
        else
            setToolTipText("");

        repaint();
    }

    /**
     * @param rgb
     *        the color to set
     */
    public void setColor(int rgb)
    {
        setColor(new Color(rgb));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        if (color == null)
            super.paintComponent(g);
        else
        {
            final int w = getWidth();
            final int h = getHeight();

            g.setColor(color);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.black);
            g.drawRect(0, 0, w, h);
        }
    }
}
