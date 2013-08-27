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
package icy.gui.main;

import icy.gui.frame.IcyInternalFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.math.HungarianAlgorithm;
import icy.resource.ResourceUtil;
import icy.util.GraphicsUtil;
import icy.util.Random;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * Icy {@link JDesktopPane} class.<br>
 * This is the main container of the application.<br>
 * It support overlays so we can use to display message, notification or logo in
 * background. First added overlays is painted first, so take care of that. Call
 * the IcyDesktopPane.repaint() method to update overlays.
 * 
 * @author Fabrice & Stephane
 */
public class IcyDesktopPane extends JDesktopPane implements ContainerListener, MouseListener, MouseMotionListener,
        MouseWheelListener
{
    public static interface DesktopOverlay extends MouseListener, MouseMotionListener, MouseWheelListener
    {
        public void paint(Graphics g, int width, int height);
    }

    public static class AbstractDesktopOverlay extends MouseAdapter implements DesktopOverlay
    {
        @Override
        public void paint(Graphics g, int width, int height)
        {
            // nothing by default
        }
    }

    /**
     * Background overlay.
     */
    public static class BackgroundDesktopOverlay extends AbstractDesktopOverlay
    {
        private final static String BACKGROUND_PATH = "background/";

        private final Image backGround;
        private final Image icyLogo;

        private final Color textColor;
        private final Color bgTextColor;

        public BackgroundDesktopOverlay()
        {
            super();

            // load random background (nor really random as we have only one
            // right now)
            backGround = ResourceUtil.getImage(BACKGROUND_PATH + Integer.toString(Random.nextInt(1)) + ".jpg");
            // load ICY logo
            icyLogo = ResourceUtil.getImage("logoICY.png");

            // default text colors
            textColor = new Color(0, 0, 0, 0.5f);
            bgTextColor = new Color(1, 1, 1, 0.5f);
        }

        @Override
        public void paint(Graphics g, int width, int height)
        {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.scale(2, 2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.2f));
            g2.drawImage(backGround, 0, 0, null);

            g2.dispose();

            final String text = "Version " + Icy.version;
            final int textWidth = (int) GraphicsUtil.getStringBounds(g2, text).getWidth();

            g2 = (Graphics2D) g.create();

            g2.setColor(bgTextColor);
            g2.drawString(text, width - (textWidth + 31), height - 8);
            g2.setColor(textColor);
            g2.drawString(text, width - (textWidth + 30), height - 9);
            g2.drawImage(icyLogo, width - 220, height - 130, null);

            g2.dispose();
        }
    }

    private static final long serialVersionUID = 7914161180763257329L;

    private final ComponentAdapter componentAdapter;
    // private final InternalFrameAdapter internalFrameAdapter;

    private final ArrayList<DesktopOverlay> overlays;

    public IcyDesktopPane()
    {
        super();

        overlays = new ArrayList<DesktopOverlay>();

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

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addContainerListener(this);

        // add the background overlay
        overlays.add(new BackgroundDesktopOverlay());
    }

    // int i = 0;

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();

        // paint overlays
        for (DesktopOverlay overlay : overlays)
            overlay.paint(g, w, h);

        // System.out.println("paint background " + i++);
    }

    private void registerFrame(JInternalFrame frame)
    {
        frame.addComponentListener(componentAdapter);
    }

    void unregisterFrame(JInternalFrame frame)
    {
        frame.removeComponentListener(componentAdapter);
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
     * Returns the list of internal viewers.
     * 
     * @param wantNotVisible
     *        Also return not visible viewers
     * @param wantIconized
     *        Also return iconized viewers
     */
    public Viewer[] getInternalViewers(boolean wantNotVisible, boolean wantIconized)
    {
        final List<Viewer> result = new ArrayList<Viewer>();

        for (Viewer viewer : Icy.getMainInterface().getViewers())
        {
            if (viewer.isInternalized())
            {
                final IcyInternalFrame internalFrame = viewer.getIcyInternalFrame();

                if ((wantNotVisible || internalFrame.isVisible()) && (wantIconized || !internalFrame.isIcon()))
                    result.add(viewer);
            }
        }

        return result.toArray(new Viewer[result.size()]);
    }

    /**
     * Organize all internal viewers in cascade
     */
    public void organizeCascade()
    {
        // get internal viewers
        final Viewer[] viewers = getInternalViewers(false, false);

        // available space (always keep 32 available pixels at south)
        final int w = getWidth();
        final int h = getHeight() - 32;

        final int fw = (int) (w * 0.6f);
        final int fh = (int) (h * 0.6f);

        final int xMax = w - 0;
        final int yMax = h - 0;

        int x = 0 + 32;
        int y = 0 + 32;

        for (Viewer v : viewers)
        {
            final IcyInternalFrame internalFrame = v.getIcyInternalFrame();

            if (internalFrame.isMaximized())
                internalFrame.setMaximized(false);
            internalFrame.setBounds(x, y, fw, fh);
            internalFrame.toFront();

            x += 30;
            y += 20;
            if ((x + fw) > xMax)
                x = 32;
            if ((y + fh) > yMax)
                y = 32;
        }
    }

    /**
     * Organize all internal viewers in tile.
     * 
     * @param type
     *        tile type<br>
     *        MainFrame.TILE_HORIZONTAL, MainFrame.TILE_VERTICAL or
     *        MainFrame.TILE_GRID
     */
    public void organizeTile(int type)
    {
        // get internal viewers
        final Viewer[] viewers = getInternalViewers(false, false);

        final int numFrames = viewers.length;

        // nothing to do
        if (numFrames == 0)
            return;

        // available space (always keep 32 available pixels at south)
        final int w = getWidth();
        final int h = getHeight() - 32;

        int numCol;
        int numLine;

        switch (type)
        {
            case MainFrame.TILE_HORIZONTAL:
                numCol = 1;
                numLine = numFrames;
                break;

            case MainFrame.TILE_VERTICAL:
                numCol = numFrames;
                numLine = 1;
                break;

            default:
                numCol = (int) Math.sqrt(numFrames);
                if (numFrames != (numCol * numCol))
                    numCol++;
                numLine = numFrames / numCol;
                if (numFrames > (numCol * numLine))
                    numLine++;
                break;
        }

        final double[][] framesDistances = new double[numCol * numLine][numFrames];

        final int dx = w / numCol;
        final int dy = h / numLine;
        int k = 0;

        for (int i = 0; i < numLine; i++)
        {
            for (int j = 0; j < numCol; j++, k++)
            {
                final double[] distances = framesDistances[k];
                final double x = (j * dx) + (dx / 2d);
                final double y = (i * dy) + (dy / 2d);

                for (int f = 0; f < numFrames; f++)
                {
                    final Point2D.Double center = ComponentUtil.getCenter(viewers[f].getInternalFrame());
                    distances[f] = Point2D.distanceSq(center.x, center.y, x, y);
                }
            }
        }

        final int[] framePos = new HungarianAlgorithm(framesDistances).resolve();

        k = 0;
        for (int i = 0; i < numLine; i++)
        {
            for (int j = 0; j < numCol; j++, k++)
            {
                final int f = framePos[k];

                if (f < numFrames)
                {
                    final IcyInternalFrame internalFrame = viewers[f].getIcyInternalFrame();

                    if (internalFrame.isMaximized())
                        internalFrame.setMaximized(false);
                    internalFrame.setBounds(j * dx, i * dy, dx, dy);
                    internalFrame.toFront();
                }
            }
        }
    }

    /**
     * @deprecated Use {@link #organizeTile(int)} instead.
     */
    @Deprecated
    public void organizeTile()
    {
        organizeTile(MainFrame.TILE_GRID);
    }

    /**
     * Add the specified overlay to the desktop.
     */
    public void addOverlay(DesktopOverlay overlay)
    {
        if (!overlays.contains(overlay))
            overlays.add(overlay);
    }

    /**
     * remove the specified overlay from the desktop.
     */
    public boolean removeOverlay(DesktopOverlay overlay)
    {
        return overlays.remove(overlay);
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
            unregisterFrame((JInternalFrame) comp);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseWheelMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // send to overlays
        for (DesktopOverlay overlay : overlays)
            overlay.mouseExited(e);
    }
}
