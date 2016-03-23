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
package icy.gui.component.button;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Color button used to select a specific color.
 * 
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

        // setBorderPainted(false);
        setFocusPainted(false);

        final Dimension dim = new Dimension(24, 18);
        add(new Box.Filler(dim, dim, dim));

        // save color information in background color
        setBackground(color);
        colorChooseText = "Choose color";

        addActionListener(this);
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return getBackground();
    }

    /**
     * @param color
     *        the color to set
     */
    public void setColor(Color color)
    {
        if (getColor() != color)
        {
            setBackground(color);
            // notify about color change
            fireColorChanged();
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

    // @Override
    // protected void paintComponent(Graphics g)
    // {
    // super.paintComponent(g);
    //
    // g.setColor(color);
    // g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
    // }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final Color c = JColorChooser.showDialog(this, colorChooseText, getColor());

        if (c != null)
            setColor(c);
    }

}
