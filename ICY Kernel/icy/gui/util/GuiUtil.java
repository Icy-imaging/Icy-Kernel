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
package icy.gui.util;

import icy.gui.component.ComponentUtil;
import icy.gui.frame.TitledFrame;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * This class is a toolbox with many simple gui routines.
 * 
 * @author Fabrice & Stephane
 */
public class GuiUtil
{
    public static JPanel createLoweredPanel(Component comp)
    {
        final JPanel result = new JPanel();

        result.setBorder(BorderFactory.createLoweredBevelBorder());
        if (comp != null)
        {
            result.setLayout(new BorderLayout());
            result.add(comp, BorderLayout.CENTER);
        }

        return result;
    }

    public static JPanel createRaisedPanel(Component comp)
    {
        final JPanel result = new JPanel();

        result.setBorder(BorderFactory.createRaisedBevelBorder());
        if (comp != null)
        {
            result.setLayout(new BorderLayout());
            result.add(comp, BorderLayout.CENTER);
        }

        return result;
    }

    public static JLabel createBoldLabel(String text)
    {
        final JLabel label = new JLabel(text);

        ComponentUtil.setFontBold(label);

        return label;
    }

    public static JLabel createBigBoldLabel(String text, int incSize)
    {
        final JLabel label = createBoldLabel(text);

        ComponentUtil.increaseFontSize(label, incSize);

        return label;
    }

    public static JPanel createCenteredLabel(String text)
    {
        return createCenteredLabel(new JLabel(text));
    }

    public static JPanel createCenteredLabel(JLabel label)
    {
        return createLineBoxPanel(Box.createHorizontalGlue(), label, Box.createHorizontalGlue());
    }

    public static JPanel createCenteredBoldLabel(String text)
    {
        return createCenteredLabel(createBoldLabel(text));
    }

    public static JLabel createFixedWidthLabel(String text, int w)
    {
        final JLabel result = new JLabel(text);

        ComponentUtil.setFixedWidth(result, w);

        return result;
    }

    public static JLabel createFixedWidthBoldLabel(String text, int w)
    {
        final JLabel result = createBoldLabel(text);

        ComponentUtil.setFixedWidth(result, w);

        return result;
    }

    public static JLabel createFixedWidthRightAlignedLabel(String text, int w)
    {
        final JLabel result = new JLabel(text);

        ComponentUtil.setFixedWidth(result, w);
        result.setHorizontalAlignment(SwingConstants.RIGHT);

        return result;
    }

    public static JPanel createTabLabel(String text, int width)
    {
        return createTabLabel(new JLabel(text), width);
    }

    public static JPanel createTabLabel(JLabel label, int width)
    {
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setHorizontalTextPosition(SwingConstants.LEADING);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(label);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    public static JPanel createTabBoldLabel(String text, int width)
    {
        return createTabLabel(createBoldLabel(text), width);
    }

    public static JPanel createTabArea(String text, int width)
    {
        return createTabArea(new JTextArea(text), width);
    }

    public static JPanel createTabArea(JTextArea area, int width)
    {
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(area);

        // area.setPreferredSize(new Dimension(10, 10));

        return panel;
    }

    public static JPanel createTabArea(String text, int width, int height)
    {
        return createTabArea(new JTextArea(text), width, height);
    }

    public static JPanel createTabArea(JTextArea area, int width, int height)
    {
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        final JScrollPane scrollArea = new JScrollPane(area);
        scrollArea.setPreferredSize(new Dimension(320, height));
        scrollArea.setBorder(null);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(scrollArea);

        return panel;
    }

    public static JPanel createLineBoxPanel(Component... componentArray)
    {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));

        for (Component c : componentArray)
            result.add(c);

        return result;
    }

    public static JPanel createPageBoxPanel(Component... componentArray)
    {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));

        for (Component c : componentArray)
            result.add(c);

        return result;
    }

    /**
     * Creates a jpanel with a gridlayout of 1,2 with the given arguments, and
     * force the width of the secon columns. Should be use for list of label
     * beside parameters
     */
    public static JPanel besidesPanel(Component jc1, Component jc2, int widthOfSecondComponent)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.add(jc1, BorderLayout.CENTER);
        panel.add(jc2, BorderLayout.EAST);
        // jc2.setMaximumSize( new Dimension( widthOfSecondComponent , 20 ) );
        // jc2.setMinimumSize( new Dimension( widthOfSecondComponent , 20 ) );
        // jc2.setSize( widthOfSecondComponent , 0 );
        jc2.setPreferredSize(new Dimension(widthOfSecondComponent, jc2.getPreferredSize().height));
        return panel;
    }

    /**
     * Creates a jpanel with a gridlayout of 1,2 with the given arguments.
     */
    public static JPanel besidesPanel(Component... componentArray)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(1, componentArray.length));
        for (int i = 0; i < componentArray.length; i++)
        {
            panel.add(componentArray[i]);
        }
        return panel;
    }

    /**
     * This generate a panel with an empty border on the side, so that it is
     * quite pretty. Generated with a boxLayout
     * 
     * @return a JPanel
     */
    public static JPanel generatePanel()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    public static JPanel generatePanel(String string)
    {
        JPanel panel = generatePanel();
        panel.setBorder(new TitledBorder(string));
        return panel;
    }

    public static JPanel generatePanelWithoutBorder()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel;
    }

    /**
     * Allow to enable/Disable all the content of a container ( such as a JPanel
     * for instance )
     */
    public static void setEnableContainer(Container container, boolean enable)
    {

        for (Component c : container.getComponents())
        {
            if (c instanceof Container)
            {
                setEnableContainer((Container) c, enable);
            }
            c.setEnabled(enable);
        }
    }

    public static void paintBackGround(int width, int height, Graphics g)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        final float ray = Math.max(width, height) * 0.05f;
        final RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, width, height, Math.min(ray * 2, 20),
                Math.min(ray * 2, 20));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, Color.white.darker(), 0, height / 1.5f, Color.black));
        g2.fill(roundRect);

        g2.setPaint(Color.black);
        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.fillOval(-width + (width / 2), height / 2, width * 2, height);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setStroke(new BasicStroke(Math.max(1f, Math.min(5f, ray))));
        g2.draw(roundRect);

        g2.dispose();
    }

    public static void paintBackGround(Component component, Graphics g)
    {
        paintBackGround(component.getWidth(), component.getHeight(), g);
    }

    public static void paintBackGround(Image image)
    {
        final Graphics g = image.getGraphics();
        // draw background in image
        paintBackGround(image.getWidth(null), image.getHeight(null), g);
        g.dispose();
    }

    public static Image addBackGround(Image source, int sourceSize, int outSize)
    {
        final BufferedImage result = new BufferedImage(outSize, outSize, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = result.createGraphics();
        // paint background
        paintBackGround(outSize, outSize, g);
        // add origin image
        final int delta = ((outSize - sourceSize) + 1) / 2;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, delta, delta, sourceSize, sourceSize, null);
        g.dispose();

        return result;
    }

    public static Image addBackGround(Image source, int outSize)
    {
        return addBackGround(source, Math.round(outSize * 0.8f), outSize);
    }

    public static Image addBackGround(Image source)
    {
        return addBackGround(source, Math.round(source.getWidth(null) * (1f / 0.8f)));
    }

    /**
     * Return bounds to draw specified string in the specified Graphics context
     * with specified font
     */
    public static Rectangle2D getStringBounds(Graphics g, Font f, String s)
    {
        if (f == null)
            return getStringBounds(g, s);

        return g.getFontMetrics(f).getStringBounds(s, g);
    }

    /**
     * Return bounds to draw specified string in the specified component
     */
    public static Rectangle2D getStringBounds(Component c, String s)
    {
        return c.getFontMetrics(c.getFont()).getStringBounds(s, c.getGraphics());
    }

    /**
     * Return bounds to draw specified string in the specified Graphics context
     * with current font
     */
    public static Rectangle2D getStringBounds(Graphics g, String s)
    {
        return g.getFontMetrics().getStringBounds(s, g);
    }

    /**
     * Draw a text centered on x on a given y.
     * 
     * @param g
     * @param string
     * @param y
     */
    public static void drawHCenteredText(Graphics g, String string, int w, int y)
    {
        final int wt = (int) getStringBounds(g, string).getWidth();
        g.drawString(string, (w - wt) / 2, y);
    }

    public static void drawCenteredText(Graphics g, String string, int w, int h)
    {
        final Rectangle2D rect = getStringBounds(g, string);
        final int wt = (int) rect.getWidth();
        final int ht = (int) rect.getHeight();

        g.drawString(string, (w - wt) / 2, (h - ht) / 2);
    }

    public static TitledFrame generateTitleFrame(String title, JPanel panel, Dimension titleDimension,
            boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
    {
        final Dimension dim;

        if (titleDimension == null)
            dim = new Dimension(400, 70);
        else
            dim = titleDimension;

        final TitledFrame result = new TitledFrame(title, dim, resizable, closable, maximizable, iconifiable);

        result.getMainPanel().add(panel);
        result.setVisible(true);

        return result;
    }

    public static void setCursor(Component c, int cursor)
    {
        if (c == null)
            return;

        if (c.getCursor().getType() != cursor)
            c.setCursor(new Cursor(cursor));
    }
}
