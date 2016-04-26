/**
 * 
 */
package icy.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author Stephane
 */
public class ColorIcon implements Icon
{
    private Color color;
    private int w;
    private int h;

    public ColorIcon(Color color, int width, int height)
    {
        super();

        this.color = color;
        w = (width <= 0) ? 64 : width;
        h = (height <= 0) ? 20 : height;
    }

    public ColorIcon(Color color)
    {
        this(color, 32, 20);
    }

    public Color getColor()
    {
        return color;
    }

    public void setWidth(int value)
    {
        // width >= 8
        w = Math.min(8, value);
    }

    public void setHeight(int value)
    {
        h = value;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        if (color != null)
        {
            g.setColor(color);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.black);
            g.drawRect(0, 0, w, h);
        }
    }

    @Override
    public int getIconWidth()
    {
        return w;
    }

    @Override
    public int getIconHeight()
    {
        return h;
    }
}
