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
package icy.gui.system;

import icy.image.ImageUtil;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.resource.ResourceUtil;
import icy.system.SystemUtil;
import icy.util.ColorUtil;
import icy.util.GraphicsUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Memory monitor.
 * 
 * @author Fab & Stephane
 */
public class MemoryMonitorPanel extends JPanel implements MouseListener, ActionListener
{
    private static final long serialVersionUID = 5629509450385435829L;

    private static int NBVAL = 94;

    /**
     * 0 est la valeur la plus ancienne.
     */
    private final double[][] valeur;
    private final double[] max;
    private final String[] infos;
    private final Timer updateTimer;
    private final double maxMemory;

    boolean displayHelpMessage = false;

    public MemoryMonitorPanel()
    {
        super();

        updateTimer = new Timer(100, this);
        maxMemory = SystemUtil.getJavaMaxMemory();

        // init tables
        valeur = new double[NBVAL][2];
        for (int i = 0; i < NBVAL; i++)
        {
            valeur[i][0] = 0;
            valeur[i][1] = 0;
        }
        max = new double[2];
        setMax(0, maxMemory);
        setMax(1, 100);
        infos = new String[2];
        for (int i = 0; i < 2; i++)
            infos[i] = "";

        setMinimumSize(new Dimension(120, 50));
        setPreferredSize(new Dimension(140, 55));

        addMouseListener(this);

        // start timer
        updateTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        final int w = getWidth();
        final int h = getHeight();

        final Color cpuColor = ColorUtil.mix(Color.blue, Color.white);
        final Color memColor = Color.green;

        GraphicsUtil.paintIcyBackGround(w, h, g);

        final Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // display graph
        if (valeur != null)
        {
            float x;
            double max;
            double ymul;
            final float step = w / 100f;

            // draw used memory
            g2.setStroke(new BasicStroke(3));
            g2.setColor(memColor);

            max = this.max[0];
            if (max != 0)
            {
                ymul = (h - 8) / max;
                x = 6;
                for (int i = 0; i < NBVAL - 1; i++)
                {
                    final double v1 = Math.min(valeur[i][0], max);
                    final double v2 = Math.min(valeur[i + 1][0], max);
                    final int y1 = h - (int) (v1 * ymul);
                    final int y2 = h - (int) (v2 * ymul);
                    g2.drawLine((int) x, y1 - 4, (int) (x + step), y2 - 4);
                    x += step;
                }
            }

            // draw CPU load
            g2.setStroke(new BasicStroke(2));
            g2.setColor(cpuColor);

            max = this.max[1];
            if (max != 0)
            {
                ymul = (h - 8) / max;
                x = 6;
                for (int i = 0; i < NBVAL - 1; i++)
                {
                    final double v1 = Math.min(valeur[i][1], max);
                    final double v2 = Math.min(valeur[i + 1][1], max);
                    final int y1 = h - (int) (v1 * ymul);
                    final int y2 = h - (int) (v2 * ymul);
                    g2.drawLine((int) x, y1 - 4, (int) (x + step), y2 - 4);
                    x += step;
                }
            }
        }

        // display text
        g2.setFont(new Font("Arial", Font.BOLD, 9));

        // display Used & Max Memory
        g2.setColor(Color.black);
        GraphicsUtil.drawHCenteredString(g2, infos[0], (w / 2) + 1, 6 + 1, false);
        g2.setColor(ColorUtil.mix(memColor, Color.white));
        GraphicsUtil.drawHCenteredString(g2, infos[0], w / 2, 6, false);
        // display CPU Load
        g2.setColor(Color.black);
        GraphicsUtil.drawHCenteredString(g2, infos[1], (w / 2) + 1, 18 + 1, false);
        g2.setColor(ColorUtil.mix(cpuColor, Color.white));
        GraphicsUtil.drawHCenteredString(g2, infos[1], w / 2, 18, false);

        String text;

        // display internet connection
        if (!NetworkUtil.hasInternetConnection())
        {
            g2.drawImage(ImageUtil.getColorImageFromAlphaImage(ResourceUtil.ICON_NETWORK, Color.gray), 10, 30, 16, 16,
                    null);
            g2.drawImage(ImageUtil.getColorImageFromAlphaImage(ResourceUtil.ICON_DELETE, Color.red), 13, 35, 10, 10,
                    null);

            if (displayHelpMessage)
            {
                text = "Not connected to internet";

                g2.setColor(Color.black);
                GraphicsUtil.drawHCenteredString(g2, text, (w / 2) + 1, 30 + 1, false);
                g2.setColor(ColorUtil.mix(Color.red, Color.white));
                GraphicsUtil.drawHCenteredString(g2, text, w / 2, 30, false);
            }
        }

        if (displayHelpMessage)
        {
            text = "click to force a garbage collector event";
            g2.setColor(Color.black);
            GraphicsUtil.drawHCenteredString(g2, text, (w / 2) + 1, 44 + 1, false);
            g2.setColor(Color.white);
            GraphicsUtil.drawHCenteredString(g2, text, w / 2, 44, false);
        }

        g2.dispose();
    }

    private void updateMemoryMessageBar()
    {
        final double totalMemory = SystemUtil.getJavaTotalMemory();
        final double usedMemory = totalMemory - SystemUtil.getJavaFreeMemory();
        final int cpuLoad = SystemUtil.getCpuLoad();

        // save used memory
        newValue(0, usedMemory);
        // save CPU load
        newValue(1, cpuLoad);

        setInfo(0, "Memory: " + UnitUtil.getBytesString(usedMemory) + "  (Max: " + UnitUtil.getBytesString(maxMemory)
                + ")");
        setInfo(1, "CPU: " + cpuLoad + "%");

        repaint();
    }

    /**
     * Scroll les valeurs et en ajoute ( un seeker serait plus joli...)
     */
    public void newValue(int curve, double val)
    {
        for (int i = 0; i < NBVAL - 1; i++)
            valeur[i][curve] = valeur[i + 1][curve];

        valeur[NBVAL - 1][curve] = val;
    }

    public void setInfo(int infonb, String info)
    {
        infos[infonb] = info;
    }

    public void setMax(int curve, double max)
    {
        this.max[curve] = max;
    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        final double freeBefore = SystemUtil.getJavaFreeMemory();
        System.gc();
        final double freeAfter = SystemUtil.getJavaFreeMemory();
        final double released = freeAfter - freeBefore;

        System.out.println("Memory free by Garbage Collector : "
                + UnitUtil.getBytesString((released > 0) ? released : 0));
    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        displayHelpMessage = true;
    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        displayHelpMessage = false;
    }

    @Override
    public void mousePressed(MouseEvent arg0)
    {

    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == updateTimer)
            updateMemoryMessageBar();
    }
}
