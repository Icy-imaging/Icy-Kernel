/**
 * 
 */
package icy.resource.icon;

import icy.gui.util.LookAndFeelUtil;
import icy.image.ImageUtil;
import icy.resource.ResourceUtil;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * IcyIcon class.
 * 
 * @author Stephane
 */
public class IcyIcon implements ResizableIcon
{
    public static final int DEFAULT_SIZE = 24;

    protected Image image;
    protected String name;
    protected final Dimension dim;

    private BufferedImage cache;
    private BufferedImage tmp;

    private IcyIcon(String name, Image image, int size)
    {
        super();

        this.image = image;
        this.name = name;

        dim = new Dimension();

        if (size != -1)
        {
            dim.width = size;
            dim.height = size;
        }
        else
        {
            if (image != null)
            {
                dim.width = image.getWidth(null);
                dim.height = image.getHeight(null);
            }
            else
            {
                dim.width = DEFAULT_SIZE;
                dim.height = DEFAULT_SIZE;
            }
        }

        updateImage();
    }

    public IcyIcon(String name, int size)
    {
        this(name, null, size);
    }

    public IcyIcon(String name)
    {
        this(name, null, -1);
    }

    public IcyIcon(Image image, int size)
    {
        this(null, image, size);
    }

    public IcyIcon(Image image)
    {
        this(null, image, -1);
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

    private void updateImage()
    {
        if (!StringUtil.isEmpty(name))
        {
            final String finalName;

            if (name.toLowerCase().endsWith(".png"))
                finalName = name;
            else
                finalName = name + ".png";

            image = ResourceUtil.getAlphaIconAsImage(finalName);
        }

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
            LookAndFeelUtil.paintForegroundImageFromAlphaImage(c, cache, tmp);
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
