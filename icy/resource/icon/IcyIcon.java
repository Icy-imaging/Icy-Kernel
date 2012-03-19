/**
 * 
 */
package icy.resource.icon;

import icy.gui.util.LookAndFeelUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Icy Icon class.<br>
 * This class is used to display alpha mask type icon.<br>
 * By default the fill color is taken from the current look and feel scheme but
 * it can also be defined manually.
 * 
 * @author Stephane
 */
public class IcyIcon implements ResizableIcon
{
    public static final int DEFAULT_SIZE = 20;

    protected Image image;
    protected String name;
    protected Color color;
    protected final Dimension dim;

    private BufferedImage cache;
    private BufferedImage tmp;

    private IcyIcon(String name, Image image, int size)
    {
        super();

        this.image = image;
        this.name = name;
        color = null;

        dim = new Dimension(size, size);

        updateImage();
    }

    public IcyIcon(String name, int size)
    {
        this(name, null, size);
    }

    public IcyIcon(String name)
    {
        this(name, null, DEFAULT_SIZE);
    }

    public IcyIcon(Image image, int size)
    {
        this(null, image, size);
    }

    public IcyIcon(Image image)
    {
        this(null, image, DEFAULT_SIZE);
    }

    public int getSize()
    {
        return dim.width;
    }

    public void setSize(int size)
    {
        setDimension(new Dimension(size, size));
    }

    /**
     * @return the iconName
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param value
     *        the name to set
     */
    public void setName(String value)
    {
        if (name != value)
        {
            // can't set image and name at same time
            name = value;
            image = null;

            updateImage();
        }
    }

    /**
     * @return the image
     */
    public Image getImage()
    {
        return image;
    }

    /**
     * @param value
     *        the image to set
     */
    public void setImage(Image value)
    {
        if (image != value)
        {
            // can't set image and name at same time
            image = value;
            name = null;

            updateImage();
        }
    }

    /**
     * @return the fill color (null if we use the default LAF color).
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color
     *        the fill color to set (null = default LAF color)
     */
    public void setColor(Color color)
    {
        if (this.color != color)
        {
            this.color = color;
            update();
        }
    }

    private void updateImage()
    {
        if (!StringUtil.isEmpty(name))
            image = ResourceUtil.getAlphaIconAsImage(name);

        update();
    }

    private void update()
    {
        if (image != null)
        {
            cache = ImageUtil.scaleImageQuality(image, dim.width, dim.height);
            tmp = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            cache = null;
            tmp = null;
        }
    }

    public Dimension getDimension()
    {
        return dim;
    }

    @Override
    public void setDimension(Dimension newDim)
    {
        // dimension changed
        if (!dim.equals(newDim))
        {
            dim.setSize(newDim);
            update();
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        paintImage(c);
        g.drawImage(tmp, x, y, null);
    }

    private void paintImage(Component c)
    {
        if (tmp != null)
        {
            if (color == null)
                LookAndFeelUtil.paintForegroundImageFromAlphaImage(c, cache, tmp);
            else
                ImageUtil.paintColorImageFromAlphaImage(cache, tmp, color);
        }
    }

    @Override
    public int getIconWidth()
    {
        return dim.width;
    }

    @Override
    public int getIconHeight()
    {
        return dim.height;
    }
}
