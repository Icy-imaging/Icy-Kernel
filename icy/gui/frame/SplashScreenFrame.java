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
package icy.gui.frame;

import icy.resource.ResourceUtil;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Animated ICY Logo.
 * 
 * @author Fab & Stephane
 */
public class SplashScreenFrame extends JFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = -519109094312389176L;

    public class SplashPanel extends JPanel
    {
        private static final long serialVersionUID = -6955085853269659076L;

        private static final String SPLASH_PATH = "splash/";
        private static final int DEFAULT_WIDTH = 960;
        private static final int DEFAULT_HEIGTH = 300;

        private final BufferedImage image;

        public SplashPanel()
        {
            final String fileName = (int) (Math.random() * 11) + ".png";

            image = ResourceUtil.getImage(SPLASH_PATH + fileName);

            if (image != null)
                setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            else
                setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGTH));
        }

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            if (image != null)
                g.drawImage(image, 0, 0, this);
        }
    }

    private final SplashPanel splash;

    /**
	 * 
	 */
    public SplashScreenFrame()
    {
        super("Icy");

        splash = new SplashPanel();

        setUndecorated(true);
//        setAlwaysOnTop(true);

        add(splash);
        pack();

        setLocationRelativeTo(null);
    }
}
