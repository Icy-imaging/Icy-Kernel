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
package icy.gui.frame;

import icy.file.FileUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.util.ClassUtil;
import icy.util.Random;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

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

        private static final String SPLASH_FOLDER = ResourceUtil.IMAGE_PATH + "splash";
        private static final int DEFAULT_WIDTH = 960;
        private static final int DEFAULT_HEIGTH = 300;

        private BufferedImage image;

        public SplashPanel()
        {
            image = null;

            String[] files = FileUtil.getFiles(SPLASH_FOLDER, null, false, false, false);

            if (files.length > 0)
                image = ImageUtil.load(files[Random.nextInt(files.length)]);
            else
            {
                try
                {
                    files = ClassUtil.getResourcesInPackage(SPLASH_FOLDER, null, true, false, false, false).toArray(
                            new String[0]);
                    if (files.length > 0)
                    {
                        final URL url = getClass().getResource("/" + files[Random.nextInt(files.length)]);
                        if (url != null)
                            image = ImageUtil.load(url, true);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Warning: cannot load splashscreen image");
                }
            }

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

        add(splash);
        pack();

        setLocationRelativeTo(null);
    }
}
