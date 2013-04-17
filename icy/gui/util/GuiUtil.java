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
package icy.gui.util;

import icy.gui.frame.TitledFrame;
import icy.util.GraphicsUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

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
 * This class is a toolbox with many simple GUI routines.
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
        result.validate();

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
        result.validate();

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
        panel.validate();

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
        panel.validate();

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
        panel.validate();

        return panel;
    }

    public static JPanel createLineBoxPanel(Component... componentArray)
    {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
        for (Component c : componentArray)
            result.add(c);
        result.validate();

        return result;
    }

    public static JPanel createPageBoxPanel(Component... componentArray)
    {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
        for (Component c : componentArray)
            result.add(c);
        result.validate();

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

        panel.setLayout(new BorderLayout());
        panel.add(jc1, BorderLayout.CENTER);
        panel.add(jc2, BorderLayout.EAST);
        jc2.setPreferredSize(new Dimension(widthOfSecondComponent, jc2.getPreferredSize().height));
        panel.validate();

        return panel;
    }

    /**
     * Creates a jpanel with a gridlayout of 1,2 with the given arguments.
     */
    public static JPanel besidesPanel(Component... componentArray)
    {
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(1, componentArray.length));
        for (int i = 0; i < componentArray.length; i++)
            panel.add(componentArray[i]);
        panel.validate();

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

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return panel;
    }

    public static JPanel generatePanel(String string)
    {
        JPanel panel = generatePanel();

        panel.setBorder(new TitledBorder(string));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        return panel;
    }

    public static JPanel generatePanelWithoutBorder()
    {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        return panel;
    }

    /**
     * Allow to enable/Disable all the content of a container ( such as a JPanel
     * for instance )
     * 
     * @deprecated what was the goal of this method ???
     */
    @Deprecated
    public static void setEnableContainer(Container container, boolean enable)
    {
        for (Component c : container.getComponents())
        {
            if (c instanceof Container)
                setEnableContainer((Container) c, enable);
            c.setEnabled(enable);
        }
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
            c.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    /**
     * @deprecated Use {@link GraphicsUtil#paintIcyBackGround(int, int, Graphics)} instead
     */
    @Deprecated
    public static void paintBackGround(int width, int height, Graphics g)
    {
        GraphicsUtil.paintIcyBackGround(width, height, g);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#paintIcyBackGround(Component, Graphics)} instead
     */
    @Deprecated
    public static void paintBackGround(Component component, Graphics g)
    {
        GraphicsUtil.paintIcyBackGround(component, g);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#paintIcyBackGround(Component, Graphics)} instead
     */
    @Deprecated
    public static void paintBackGround(Image image)
    {
        GraphicsUtil.paintIcyBackGround(image);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#getStringBounds(Graphics, Font, String)} instead
     */
    @Deprecated
    public static Rectangle2D getStringBounds(Graphics g, Font f, String s)
    {
        return GraphicsUtil.getStringBounds(g, f, s);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#getStringBounds(Component, String)} instead
     */
    @Deprecated
    public static Rectangle2D getStringBounds(Component c, String s)
    {
        return GraphicsUtil.getStringBounds(c, s);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#getStringBounds(Graphics, String)} instead
     */
    @Deprecated
    public static Rectangle2D getStringBounds(Graphics g, String s)
    {
        return GraphicsUtil.getStringBounds(g, s);
    }

    /**
     * @deprecated uses
     *             {@link GraphicsUtil#drawHCenteredString(Graphics, String, int, int, boolean)}
     *             instead
     */
    @Deprecated
    public static void drawHCenteredText(Graphics g, String string, int w, int y)
    {
        GraphicsUtil.drawHCenteredString(g, string, w / 2, y, false);
    }

    /**
     * @deprecated Use {@link GraphicsUtil#drawCenteredString(Graphics, String, int, int, boolean)}
     *             instead
     */
    @Deprecated
    public static void drawCenteredText(Graphics g, String string, int w, int h)
    {
        GraphicsUtil.drawCenteredString(g, string, w / 2, h / 2, false);
    }
}
