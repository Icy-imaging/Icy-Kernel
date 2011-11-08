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
package icy.gui.main;

import icy.common.Random;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.progress.TaskFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Could be used to add a message or a logo over the whole Icy.getMainInterface().
 * 
 * @author Fabrice & Stephane
 */
public class IcyDesktopPane extends JDesktopPane implements ContainerListener
{
    private static final long serialVersionUID = 7914161180763257329L;

    private final static String BACKGROUND_PATH = "background/";

    // load random background
    private final static Image backGround = ResourceUtil.getImage(BACKGROUND_PATH + Integer.toString(Random.nextInt(2))
            + ".jpg");
    private final static Image icyLogo = ResourceUtil.getImage("logoICY.png");

    private final Color textColor;
    private final Color bgTextColor;

    private final ComponentAdapter componentAdapter;
    private final InternalFrameAdapter internalFrameAdapter;

    public IcyDesktopPane()
    {
        super();

        textColor = new Color(0, 0, 0, 0.5f);
        bgTextColor = new Color(1, 1, 1, 0.5f);

        // setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        componentAdapter = new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                checkPosition((JInternalFrame) e.getSource());
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                checkPosition((JInternalFrame) e.getSource());
            }
        };

        internalFrameAdapter = new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosing(InternalFrameEvent e)
            {
                unregisterFrame(e.getInternalFrame());
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                unregisterFrame(e.getInternalFrame());
            }
        };

        addContainerListener(this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();
        final Graphics2D g2 = (Graphics2D) g.create();

        final Composite originalComposite = g2.getComposite();
        final AffineTransform originalTransform = g2.getTransform();
        try
        {
            g2.scale(2, 2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.2f));
            g2.drawImage(backGround, 0, 0, null);
        }
        finally
        {
            g2.setComposite(originalComposite);
            g2.setTransform(originalTransform);
        }

        final String text = "Icy Version " + Icy.version;

        g2.setColor(bgTextColor);
        g2.drawString(text, 11, h - 9);
        g2.setColor(textColor);
        g2.drawString(text, 10, h - 10);
        g2.drawImage(icyLogo, w - 220, h - 130, null);

        g2.dispose();
    }

    private void registerFrame(JInternalFrame frame)
    {
        frame.addComponentListener(componentAdapter);
        frame.addInternalFrameListener(internalFrameAdapter);
    }

    void unregisterFrame(JInternalFrame frame)
    {
        frame.removeComponentListener(componentAdapter);
        frame.removeInternalFrameListener(internalFrameAdapter);
    }

    void checkPosition(JInternalFrame frame)
    {
        final Rectangle rect = frame.getBounds();

        if (fixPosition(rect))
            frame.setBounds(rect);
    }

    boolean fixPosition(Rectangle rect)
    {
        final int limit = getY();
        if (rect.y < limit)
        {
            rect.y = limit;
            return true;
        }

        return false;
    }

    /**
     * Organize all internal frames (except progressFrame) in cascade
     */
    public void organizeCascade()
    {
        final JInternalFrame[] frames = getAllFrames();
        final ArrayList<JInternalFrame> openedFrames = new ArrayList<JInternalFrame>();

        for (JInternalFrame f : frames)
        {
            // avoid iconized frame
            if (!f.isIcon())
            {
                final IcyFrame icyFrame = IcyFrame.findIcyFrame(f);

                if (icyFrame != null)
                {
                    // avoid progressFrame
                    if (!(icyFrame instanceof TaskFrame))
                        openedFrames.add(f);
                }
                else
                    openedFrames.add(f);
            }
        }

        final int w = getWidth();
        int h = getHeight();

        // keep space for iconized windows
        if (openedFrames.size() != frames.length)
            h -= 32;

        final int fw = (int) (w * 0.7f);
        final int fh = (int) (h * 0.7f);

        int x = 0;
        int y = 0;
        for (JInternalFrame f : openedFrames)
        {
            // avoid iconized frame
            f.setBounds(x, y, fw, fh);
            x += 30;
            y += 20;
            if ((x + fw) > w)
                x = 0;
            if ((y + fh) > h)
                y = 0;
        }
    }

    /**
     * Organize all internal frames (except progressFrame) in tile
     */
    public void organizeTile()
    {
        final JInternalFrame[] frames = getAllFrames();
        final ArrayList<JInternalFrame> openedFrames = new ArrayList<JInternalFrame>();

        for (JInternalFrame f : frames)
        {
            // avoid iconized frame
            if (!f.isIcon())
            {
                final IcyFrame icyFrame = IcyFrame.findIcyFrame(f);

                if (icyFrame != null)
                {
                    // avoid ProgressFrame
                    if (!(icyFrame instanceof TaskFrame))
                        openedFrames.add(f);
                }
                else
                    openedFrames.add(f);
            }
        }

        final int numOpenedFrames = openedFrames.size();
        if (numOpenedFrames == 0)
            return;

        final int w = getWidth();
        int h = getHeight();

        // keep space for iconized windows
        if (numOpenedFrames != frames.length)
            h -= 32;

        int numCol = (int) Math.sqrt(numOpenedFrames);
        if (numOpenedFrames != (numCol * numCol))
            numCol++;
        int numLine = numOpenedFrames / numCol;
        if (numOpenedFrames > (numCol * numLine))
            numLine++;

        final int dx = w / numCol;
        final int dy = h / numLine;

        int k = 0;
        for (int i = 0; i < numCol; ++i)
            for (int j = 0; j < numCol && k < numOpenedFrames; ++j, ++k)
                openedFrames.get(i * numCol + j).setBounds(j * dx, i * dy, dx, dy);
    }

    @Override
    public void componentAdded(ContainerEvent e)
    {
        final Component comp = e.getChild();

        if (comp instanceof JInternalFrame)
            registerFrame((JInternalFrame) comp);
    }

    @Override
    public void componentRemoved(ContainerEvent e)
    {
        final Component comp = e.getChild();

        if (comp instanceof JInternalFrame)
        {
            unregisterFrame((JInternalFrame) comp);
        }
    }
}
