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
package icy.resource;

import icy.image.ImageUtil;
import icy.util.StringUtil;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class ResourceUtil
{
    public static final Image IMAGE_ICY_16 = ResourceUtil.getIconAsImage("icy16.png");
    public static final Image IMAGE_ICY_32 = ResourceUtil.getIconAsImage("icy32.png");
    public static final Image IMAGE_ICY_256 = ResourceUtil.getImage("logo.png");

    public static final ImageIcon ICON_ICY_16 = ResourceUtil.getImageIcon(IMAGE_ICY_16);
    public static final ImageIcon ICON_ICY_32 = ResourceUtil.getImageIcon(IMAGE_ICY_32);
    public static final ImageIcon ICON_ICY_256 = ResourceUtil.getImageIcon(IMAGE_ICY_256);

    public static final Image ICON_EXPAND = ResourceUtil.getAlphaIconAsImage("top_right_expand.png");
    public static final Image ICON_COLLAPSE = ResourceUtil.getAlphaIconAsImage("top_right_collapse.png");
    public static final Image ICON_PICTURE = ResourceUtil.getAlphaIconAsImage("picture.png");
    public static final Image ICON_WINDOW = ResourceUtil.getAlphaIconAsImage("app_window.png");
    public static final Image ICON_NEWDOC = ResourceUtil.getAlphaIconAsImage("doc_new.png");
    public static final Image ICON_DOC = ResourceUtil.getAlphaIconAsImage("document.png");
    public static final Image ICON_DOCCOPY = ResourceUtil.getAlphaIconAsImage("doc_copy.png");
    public static final Image ICON_DOCIMPORT = ResourceUtil.getAlphaIconAsImage("doc_import.png");
    public static final Image ICON_DOCEXPORT = ResourceUtil.getAlphaIconAsImage("doc_export.png");
    public static final Image ICON_DOCEDIT = ResourceUtil.getAlphaIconAsImage("doc_edit.png");
    public static final Image ICON_OPEN = ResourceUtil.getAlphaIconAsImage("folder_open.png");
    public static final Image ICON_SAVE = ResourceUtil.getAlphaIconAsImage("save.png");
    public static final Image ICON_CLOSE = ResourceUtil.getAlphaIconAsImage("round_delete.png");
    public static final Image ICON_EXIT = ResourceUtil.getAlphaIconAsImage("arrow_bottom_left.png");
    public static final Image ICON_DELETE = ResourceUtil.getAlphaIconAsImage("delete.png");
    public static final Image ICON_COG = ResourceUtil.getAlphaIconAsImage("cog.png");
    public static final Image ICON_UNCHECKED = ResourceUtil.getAlphaIconAsImage("checkbox_unchecked.png");
    public static final Image ICON_CHECKED = ResourceUtil.getAlphaIconAsImage("checkbox_checked.png");
    public static final Image ICON_LOCK_OPEN = ResourceUtil.getAlphaIconAsImage("padlock_open.png");
    public static final Image ICON_LOCK_CLOSE = ResourceUtil.getAlphaIconAsImage("padlock_closed.png");
    public static final Image ICON_LOCKED_BASE = ResourceUtil.getAlphaIconAsImage("locked.png");
    public static final Image ICON_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_open.png");
    public static final Image ICON_NOT_VISIBLE = ResourceUtil.getAlphaIconAsImage("eye_close.png");

    private static final String ICON_PATH = "res/icon/";
    private static final String IMAGE_PATH = "res/image/";

    private static final String ALPHA_ICON_PATH = "alpha/";

    // private static final String BLACK_ICON_PATH = "black/";
    // private static final String WHITE_ICON_PATH = "white/";

    // private static final String COLOR_ICON_PATH = "color/";

    public static ArrayList<Image> getIcyIconImages()
    {
        final ArrayList<Image> result = new ArrayList<Image>();

        result.add(ResourceUtil.IMAGE_ICY_256);
        result.add(ResourceUtil.IMAGE_ICY_32);
        result.add(ResourceUtil.IMAGE_ICY_16);

        return result;
    }

    private static Image scaleImage(Image image, int size)
    {
        // resize if needed
        if ((image != null) && (size != -1))
        {
            // resize only if image has different size
            if ((image.getWidth(null) != size) || (image.getWidth(null) != size))
                return ImageUtil.scaleImage(image, size, size);
        }

        return image;
    }

    /**
     * Return an image with wanted size located in res/image<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static BufferedImage getImage(String name)
    {
        final BufferedImage result;
        final URL url = ResourceUtil.class.getResource("/" + IMAGE_PATH + name);

        if (url != null)
            result = ImageUtil.loadImage(url, true);
        else
            result = ImageUtil.loadImage(new File(IMAGE_PATH + name), true);

        return result;
    }

    /**
     * Return lock image with specified number.
     */
    public static BufferedImage getLockedImage(int number)
    {
        final BufferedImage result = ImageUtil.getCopy(ICON_LOCKED_BASE);

        // nice for 48 pixels image
        ImageUtil.drawTextTopRight(result, StringUtil.toString(number), 26, true, Color.black);

        return result;
    }

    /**
     * Return lock image with specified letter.
     */
    public static BufferedImage getLockedImage(char letter)
    {
        final BufferedImage result = ImageUtil.getCopy(ICON_LOCKED_BASE);

        // nice for 48 pixels image
        ImageUtil.drawTextTopRight(result, String.valueOf(letter), 26, true, Color.black);

        return result;
    }

    /**
     * Return an image with wanted size located in res/icon from its name<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static Image getIconAsImage(String name, int size)
    {
        final Image image;
        final URL url = ResourceUtil.class.getResource("/" + ICON_PATH + name);

        if (url != null)
            image = ImageUtil.loadImage(url, true);
        else
            image = ImageUtil.loadImage(new File(ICON_PATH + name), true);

        if (image == null)
        {
            System.out.println("Resource name can't be found: " + name);
            return null;
        }

        return scaleImage(image, size);
    }

    /**
     * Return an image located in res/icon from its name<br>
     * For any other location use the ImageUtil.loadImage() method
     * 
     * @param name
     */
    public static Image getIconAsImage(String name)
    {
        return getIconAsImage(name, -1);
    }

    /**
     * Create an ImageIcon with specified size from the specified image<br>
     */
    public static ImageIcon getImageIcon(Image image, int size)
    {
        if (image != null)
            return new ImageIcon(scaleImage(image, size));

        return null;
    }

    /**
     * Create an ImageIcon from the specified image (default image size is used as icon size)<br>
     */
    public static ImageIcon getImageIcon(Image image)
    {
        if (image != null)
            return new ImageIcon(image);

        return null;
    }

    /**
     * Return an image located in res/icon with specified square size from its name<br>
     */
    public static ImageIcon getImageIcon(String resourceName, int size)
    {
        return getImageIcon(getIconAsImage(resourceName, size));
    }

    /**
     * Return an image located in res/icon from its name<br>
     */
    public static ImageIcon getImageIcon(String resourceName)
    {
        return getImageIcon(getIconAsImage(resourceName));
    }

    /**
     * Return an image located in res/icon/alpha with specified square size from its name<br>
     */
    public static Image getAlphaIconAsImage(String resourceName, int size)
    {
        return getIconAsImage(ALPHA_ICON_PATH + resourceName, size);
    }

    /**
     * Return an image located in res/icon/alpha from its name<br>
     */
    public static Image getAlphaIconAsImage(String resourceName)
    {
        return getAlphaIconAsImage(resourceName, -1);
    }

    /**
     * Return an ImageIcon located in res/icon/alpha with specified square size<br>
     */
    public static ImageIcon getAlphaIcon(String resourceName, int size)
    {
        return getImageIcon(getIconAsImage(ALPHA_ICON_PATH + resourceName, size));
    }

    /**
     * Return an ImageIcon located in res/icon/alpha<br>
     */
    public static ImageIcon getAlphaIcon(String resourceName)
    {
        return getAlphaIcon(resourceName, -1);
    }

    /**
     * @deprecated use {@link #getAlphaIconAsImage(String, int)} instead
     */
    @Deprecated
    public static Image getBlackIconAsImage(String resourceName, int size)
    {
        return getAlphaIconAsImage(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIconAsImage(String)} instead
     */
    @Deprecated
    public static Image getBlackIconAsImage(String resourceName)
    {
        return getAlphaIconAsImage(resourceName);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getBlackIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getBlackIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getWhiteIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getWhiteIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    // public static ImageIcon getColorIcon(String resourceName, int size)
    // {
    // return getImageIcon(getIconAsImage(COLOR_ICON_PATH + resourceName, size));
    // }
    //
    // public static ImageIcon getColorIcon(String resourceName)
    // {
    // return getColorIcon(resourceName, -1);
    // }

    /**
     * @deprecated use {@link #getAlphaIcon(String, int)} instead
     */
    @Deprecated
    public static ImageIcon getIcyIcon(String resourceName, int size)
    {
        return getAlphaIcon(resourceName, size);
    }

    /**
     * @deprecated use {@link #getAlphaIcon(String)} instead
     */
    @Deprecated
    public static ImageIcon getIcyIcon(String resourceName)
    {
        return getAlphaIcon(resourceName);
    }

    /**
     * Return a new ImageIcon scaled with specified size
     */
    public static ImageIcon scaleIcon(ImageIcon icon, int size)
    {
        if (icon != null)
            return getImageIcon(icon.getImage(), size);

        return null;
    }
}
