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
package icy.gui.component.button;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.EventListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * @author Stephane
 */
public class ColorChooserButton extends JButton implements ActionListener
{
    public static interface ColorChangeListener extends EventListener
    {
        void colorChanged(ColorChooserButton source);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5130821224410911737L;

    private Color color;
    private String colorChooseText;

    /**
     * 
     */
    public ColorChooserButton()
    {
        this(Color.black);
    }

    /**
     * @param color
     */
    public ColorChooserButton(Color color)
    {
        super();

        setBorderPainted(false);

        this.color = color;
        colorChooseText = "Choose color";

        // default size
        setPreferredSize(new Dimension(32, 20));
        setSize(new Dimension(32, 20));

        addActionListener(this);

        updateIcon();
    }

    private void updateIcon()
    {
        final Dimension d = getSize();
        final int w = d.width - 2;
        final int h = d.height - 2;

        if ((w <= 0) || (h <= 0))
            return;

        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = img.createGraphics();

        g.setColor(color);
        g.fillRect(1, 1, w - 2, h - 2);
        g.setColor(Color.black);
        g.drawRect(0, 0, w, h);

        setIcon(new ImageIcon(img));
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
        if (this.color != color)
        {
            this.color = color;
            colorChanged();
        }
    }

    /**
     * @return the colorChooseText
     */
    public String getColorChooseText()
    {
        return colorChooseText;
    }

    /**
     * @param colorChooseText
     *        the colorChooseText to set
     */
    public void setColorChooseText(String colorChooseText)
    {
        this.colorChooseText = colorChooseText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        updateIcon();
    }

    private void colorChanged()
    {
        // udpate icon
        updateIcon();
        // and notify about color change
        fireColorChanged();
    }

    protected void fireColorChanged()
    {
        for (ColorChangeListener listener : listenerList.getListeners(ColorChangeListener.class))
            listener.colorChanged(this);
    }

    /**
     * Adds a <code>ColorChangeListener</code> to the button.
     * 
     * @param l
     *        the listener to be added
     */
    public void addColorChangeListener(ColorChangeListener l)
    {
        listenerList.add(ColorChangeListener.class, l);
    }

    /**
     * Removes a ColorChangeListener from the button.
     * 
     * @param l
     *        the listener to be removed
     */
    public void removeColorChangeListener(ColorChangeListener l)
    {
        listenerList.remove(ColorChangeListener.class, l);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Color c = JColorChooser.showDialog(this, colorChooseText, color);

        if (c != null)
            setColor(c);
    }

}
