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
package icy.gui.component;

import icy.gui.util.ComponentUtil;
import icy.util.GraphicsUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Generate a panel with an uniformed logo.
 * 
 * @author Fabrice de Chaumont
 */
public class IcyLogo extends IcyPanel // implements ComponentListener
{
    private static final long serialVersionUID = 3914710344010035775L;

    final String title;
    final Font titleFont;

    public IcyLogo(String title)
    {
        this(title, null);
    }

    public IcyLogo(String title, Dimension dim)
    {
        super();

        this.title = title;

        if (dim != null)
            setPreferredSize(dim);
        else
            ComponentUtil.setPreferredHeight(this, 60);

        titleFont = new Font("Arial", Font.BOLD, 26);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        final int w = getWidth();
        final int h = getHeight();

        g2.setColor(Color.white);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(titleFont);
        GraphicsUtil.drawCenteredString(g2, title, w / 2, h / 2, false);

        g2.dispose();
    }
}
