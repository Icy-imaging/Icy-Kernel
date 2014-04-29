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

import icy.util.GraphicsUtil;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * @deprecated Don't use this fancy background panel anymore...
 */
@Deprecated
public class IcyPanel extends JPanel
{
    private static final long serialVersionUID = -7893535181542546173L;

    /**
     * background draw cache flag
     */
    protected boolean bgDrawCached;

    // internal
    private BufferedImage bgImage;

    public IcyPanel()
    {
        this(false);
    }

    public IcyPanel(boolean bgDrawCached)
    {
        super();

        this.bgDrawCached = bgDrawCached;
        bgImage = null;
    }

    /**
     * @return the bgDrawCached
     */
    public boolean isBgDrawCached()
    {
        return bgDrawCached;
    }

    /**
     * @param bgDrawCached
     *        the bgDrawCached to set
     */
    public void setBgDrawCached(boolean bgDrawCached)
    {
        this.bgDrawCached = bgDrawCached;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (bgDrawCached)
        {
            final int w = getWidth();
            final int h = getHeight();

            // cached background
            if ((bgImage == null) || (bgImage.getWidth() != w) || (bgImage.getHeight() != h))
            {
                bgImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                final Graphics2D cachedGraphics = bgImage.createGraphics();
                GraphicsUtil.paintIcyBackGround(this, cachedGraphics);
                cachedGraphics.dispose();
            }

            ((Graphics2D) g).drawImage(bgImage, null, 0, 0);
        }
        else
            GraphicsUtil.paintIcyBackGround(this, g);
    }
}
