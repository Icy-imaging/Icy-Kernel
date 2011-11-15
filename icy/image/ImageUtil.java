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
package icy.image;

import icy.gui.component.FontUtil;
import icy.gui.util.GuiUtil;
import icy.network.URLUtil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import sun.awt.image.SunWritableRaster;

/**
 * 
 */

/**
 * @author stephane
 */
public class ImageUtil
{
    public static String getImageTypeString(int type)
    {
        switch (type)
        {
            case BufferedImage.TYPE_CUSTOM:
                return "TYPE_CUSTOM";
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "TYPE_USHORT_565_RGB";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "TYPE_USHORT_555_RGB";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "TYPE_BYTE_GRAY";
            case BufferedImage.TYPE_USHORT_GRAY:
                return "TYPE_USHORT_GRAY";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "TYPE_BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_INDEXED:
                return "TYPE_BYTE_INDEXED";
            default:
                return "UNKNOWN TYPE";
        }
    }

    public static String getTransparencyString(int transparency)
    {
        switch (transparency)
        {
            case Transparency.OPAQUE:
                return "OPAQUE";
            case Transparency.BITMASK:
                return "BITMASK";
            case Transparency.TRANSLUCENT:
                return "TRANSLUCENT";
            default:
                return "UNKNOWN TRANSPARENCY";
        }
    }

    /**
     * Create a 8 bits indexed buffered image from specified <code>IndexColorModel</code><br>
     * and byte array data.
     */
    public static BufferedImage createIndexedImage(int w, int h, IndexColorModel cm, byte[] data)
    {
        final SunWritableRaster raster = (SunWritableRaster) Raster.createInterleavedRaster(new DataBufferByte(data, w
                * h, 0), w, h, w, 1, new int[] {0}, null);
        raster.setStolen(false);
        return new BufferedImage(cm, raster, false, null);
    }

    /**
     * Load an image from specified path
     */
    public static BufferedImage loadImage(String path, boolean displayError)
    {
        return loadImage(URLUtil.getURL(path), displayError);
    }

    /**
     * Load an image from specified path
     */
    public static BufferedImage loadImage(String path)
    {
        return loadImage(path, true);
    }

    /**
     * Load an image from specified url
     */
    public static BufferedImage loadImage(URL url, boolean displayError)
    {
        if (url != null)
        {
            try
            {
                return ImageIO.read(url);
            }
            catch (IOException e)
            {
                if (displayError)
                    System.err.println("Can't load image from " + url);
            }
        }

        return null;
    }

    /**
     * Load an image from specified url
     */
    public static Image loadImage(URL url)
    {
        return loadImage(url, true);
    }

    /**
     * Load an image from specified file
     */
    public static BufferedImage loadImage(File file, boolean displayError)
    {
        if (file != null)
        {
            try
            {
                return ImageIO.read(file);
            }
            catch (IOException e)
            {
                if (displayError)
                    System.err.println("Can't load image from " + file);
            }
        }

        return null;
    }

    /**
     * Load an image from specified file
     */
    public static BufferedImage loadImage(File file)
    {
        return loadImage(file, true);
    }

    /**
     * Load an image from specified InputStream
     */
    public static BufferedImage loadImage(InputStream input, boolean displayError)
    {
        if (input != null)
        {
            try
            {
                return ImageIO.read(input);
            }
            catch (IOException e)
            {
                if (displayError)
                    System.err.println("Can't load image from stream " + input);
            }
        }

        return null;
    }

    /**
     * Load an image from specified InputStream
     */
    public static BufferedImage loadImage(InputStream input)
    {
        return loadImage(input, true);
    }

    /**
     * Save an image to specified path in specified format
     */
    public static boolean saveImage(RenderedImage image, String format, String path)
    {
        if (path != null)
        {
            try
            {
                return ImageIO.write(image, format, new FileOutputStream(path));
            }
            catch (IOException e)
            {
                System.err.println("Can't save image to " + path);
            }
        }

        return false;
    }

    /**
     * Save an image to specified file in specified format
     */
    public static boolean saveImage(RenderedImage image, String format, File file)
    {
        if (file != null)
        {
            try
            {
                return ImageIO.write(image, format, file);
            }
            catch (IOException e)
            {
                System.err.println("Can't save image to " + file);
            }
        }

        return false;
    }

    /**
     * Return a RenderedImage from the given Image object.
     */
    public static RenderedImage toRenderedImage(Image image)
    {
        return toBufferedImage(image);
    }

    /**
     * Return a BufferedImage from the given Image object.
     * If the image is already a BufferedImage image then it's directly returned
     */
    public static BufferedImage toBufferedImage(Image image)
    {
        if (image instanceof BufferedImage)
            return (BufferedImage) image;

        final BufferedImage bufImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = bufImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufImage;
    }

    /**
     * Scale an image with specified size
     */
    public static BufferedImage scaleImage(Image image, int width, int height)
    {
        if (image != null)
        {
            // getScaledInstance keep a reference on source image --> memory leak
            // return image.getScaledInstance(width, height, Image.SCALE_FAST);
            final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = result.createGraphics();

            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            return result;
        }

        return null;
    }

    /**
     * Scale an image with specified size
     */
    public static BufferedImage scaleImageQuality(Image image, int width, int height)
    {
        if (image != null)
        {
            final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = result.createGraphics();

            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            return result;
        }

        return null;
    }

    /**
     * Convert an image to a BufferedImage.<br>
     * If <code>out</out> is null, by default a <code>BufferedImage.TYPE_INT_ARGB</code> is created.
     */
    public static BufferedImage convertImage(Image in, BufferedImage out)
    {
        final BufferedImage result;

        // no output type specified ? use ARGB
        if (out == null)
            result = new BufferedImage(in.getWidth(null), in.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        else
            result = out;

        result.getGraphics().drawImage(in, 0, 0, null);

        return result;
    }

    /**
     * Convert an image to grey image.
     */
    public static BufferedImage toGrayImage(Image image)
    {
        if (image != null)
            return convertImage(image, new BufferedImage(image.getWidth(null), image.getHeight(null),
                    BufferedImage.TYPE_BYTE_GRAY));

        return null;
    }

    /**
     * Create a copy of the input image.<br>
     * Result is always a <code>BufferedImage.TYPE_INT_ARGB</code> type image.
     */
    public static BufferedImage getCopy(Image in)
    {
        return convertImage(in, null);
    }

    /**
     * Return true if image has the same size
     */
    public static boolean sameSize(BufferedImage im1, BufferedImage im2)
    {
        return (im1.getWidth() == im2.getWidth()) && (im1.getHeight() == im2.getHeight());
    }

    /**
     * Apply simple color filter with specified alpha factor to the image
     */
    public static void applyColorFilter(Image image, Color color, float alpha)
    {
        if (image != null)
        {
            // should be Graphics2D compatible
            final Graphics2D g = (Graphics2D) image.getGraphics();
            final Rectangle rect = new Rectangle(image.getWidth(null), image.getHeight(null));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(color);
            g.fill(rect);
            g.dispose();
        }
    }

    /**
     * Return an image which contains specified color depending original alpha intensity image
     */
    public static Image getColorImageFromAlphaImage(Image alphaImage, Color color)
    {
        return paintColorImageFromAlphaImage(alphaImage, null, color);
    }

    /**
     * Paint the specified color in 'out' image depending original alpha intensity from 'alphaImage'
     */
    public static Image paintColorImageFromAlphaImage(Image alphaImage, Image out, Color color)
    {
        final int w;
        final int h;
        final Image result;

        if (out == null)
        {
            w = alphaImage.getWidth(null);
            h = alphaImage.getHeight(null);

            if ((w == -1) || (h == -1))
                return null;

            result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            w = out.getWidth(null);
            h = out.getHeight(null);

            if ((w == -1) || (h == -1))
                return null;

            result = out;
        }

        final Graphics2D g = (Graphics2D) result.getGraphics();

        // clear
        g.setBackground(new Color(0x00000000, true));
        g.clearRect(0, 0, w, h);

        // draw icon
        g.drawImage(alphaImage, 0, 0, null);

        // set fill color
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, w, h);

        g.dispose();

        return result;
    }

    /**
     * Draw text in the specified image with specified parameters.<br>
     */
    public static void drawText(Image image, String text, float x, float y, int size, Color color)
    {
        final Graphics2D g = (Graphics2D) image.getGraphics();

        // prepare setting
        g.setColor(color);
        g.setFont(FontUtil.setSize(g.getFont(), size));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw icon
        g.drawString(text, x, y);

        g.dispose();

    }

    /**
     * Draw text at top right in the specified image with specified parameters.<br>
     */
    public static void drawTextTopRight(Image image, String text, int size, boolean bold, Color color)
    {
        final float w = image.getWidth(null);
        final Graphics2D g = (Graphics2D) image.getGraphics();

        // prepare setting
        g.setColor(color);
        g.setFont(FontUtil.setSize(g.getFont(), size));
        if (bold)
            g.setFont(FontUtil.setStyle(g.getFont(), Font.BOLD));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // get string bounds
        final Rectangle2D bounds = GuiUtil.getStringBounds(g, text);

        // draw icon
        g.drawString(text, w - ((float) bounds.getWidth()), 0 - (float) bounds.getY());

        g.dispose();
    }
}
