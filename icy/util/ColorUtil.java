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
package icy.util;

import java.awt.Color;

/**
 * Color utilities class
 * 
 * @author stephane
 */
public class ColorUtil
{
    /**
     * Get String representation of the specified color.<br>
     * <br>
     * Default representation is "A:R:G:B" where :<br>
     * A = alpha level in hexadecimal (0x00-0xFF)<br>
     * R = red level in hexadecimal (0x00-0xFF)<br>
     * G = green level in hexadecimal (0x00-0xFF)<br>
     * B = blue level in hexadecimal (0x00-0xFF)<br>
     * 
     * @param color
     */
    public static String toString(Color color)
    {
        return toString(color, true, ":");
    }

    /**
     * Get String representation of the specified rgb value.<br>
     * <br>
     * Default representation is "A:R:G:B" where :<br>
     * A = alpha level in hexadecimal (00-FF)<br>
     * R = red level in hexadecimal (00-FF)<br>
     * G = green level in hexadecimal (00-FF)<br>
     * B = blue level in hexadecimal (00-FF)<br>
     * 
     * @param rgb
     */
    public static String toString(int rgb)
    {
        return toString(rgb, true, ":");
    }

    /**
     * Get String representation of the specified Color value.<br>
     * <br>
     * Default representation is "A:R:G:B" where :<br>
     * A = alpha level<br>
     * R = red level<br>
     * G = green level<br>
     * B = blue level<br>
     * 
     * @param color
     * @param hexa
     *        component level are represented in hexadecimal (2 digits)
     */
    public static String toString(Color color, boolean hexa)
    {
        return toString(color, hexa, ":");
    }

    /**
     * Get String representation of the specified rgb value.<br>
     * <br>
     * Default representation is "A:R:G:B" where :<br>
     * A = alpha level<br>
     * R = red level<br>
     * G = green level<br>
     * B = blue level<br>
     * 
     * @param rgb
     * @param hexa
     *        component level are represented in hexadecimal (2 digits)
     */
    public static String toString(int rgb, boolean hexa)
    {
        return toString(rgb, hexa, ":");
    }

    /**
     * Get String representation of the specified color.<br>
     * <br>
     * Default representation is "AsepRsepGsepB" where :<br>
     * A = alpha level in hexadecimal (0x00-0xFF)<br>
     * R = red level in hexadecimal (0x00-0xFF)<br>
     * G = green level in hexadecimal (0x00-0xFF)<br>
     * B = blue level in hexadecimal (0x00-0xFF)<br>
     * sep = the specified separator<br>
     * <br>
     * Ex : toString(Color.red, true, ":") --> "FF:FF:00:00"
     * 
     * @param color
     * @param hexa
     *        component level are represented in hexadecimal (2 digits)
     */
    public static String toString(Color color, boolean hexa, String sep)
    {
        if (color == null)
            return "-";

        return toString(color.getRGB(), hexa, sep);
    }

    /**
     * Get String representation of the specified rgb value.<br>
     * <br>
     * Default representation is "AsepRsepGsepB" where :<br>
     * A = alpha level in hexadecimal (0x00-0xFF)<br>
     * R = red level in hexadecimal (0x00-0xFF)<br>
     * G = green level in hexadecimal (0x00-0xFF)<br>
     * B = blue level in hexadecimal (0x00-0xFF)<br>
     * sep = the specified separator<br>
     * <br>
     * Ex : toString(0xFF00FF00, true, ":") --> "FF:00:FF:00"
     * 
     * @param rgb
     * @param hexa
     *        component level are represented in hexadecimal (2 digits)
     */
    public static String toString(int rgb, boolean hexa, String sep)
    {
        final int a = (rgb >> 24) & 0xFF;
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = (rgb >> 0) & 0xFF;

        if (hexa)
            return (StringUtil.toHexaString(a, 2) + sep + StringUtil.toHexaString(r, 2) + sep
                    + StringUtil.toHexaString(g, 2) + sep + StringUtil.toHexaString(b, 2)).toUpperCase();

        return StringUtil.toString(a) + sep + StringUtil.toString(r) + sep + StringUtil.toString(g) + sep
                + StringUtil.toString(b);
    }

    /**
     * Mix 2 colors with priority color
     */
    public static Color mixOver(Color backColor, Color frontColor)
    {
        final int r, g, b, a;

        final float frontAlpha = frontColor.getAlpha() / 255f;
        final float invAlpha = 1f - frontAlpha;

        r = (int) ((backColor.getRed() * invAlpha) + (frontColor.getRed() * frontAlpha));
        g = (int) ((backColor.getGreen() * invAlpha) + (frontColor.getGreen() * frontAlpha));
        b = (int) ((backColor.getBlue() * invAlpha) + (frontColor.getBlue() * frontAlpha));
        a = Math.max(backColor.getAlpha(), frontColor.getAlpha());

        return new Color(r, g, b, a);
    }

    /**
     * Mix 2 colors without "priority" color
     */
    public static Color mix(Color c1, Color c2, boolean useAlpha)
    {
        final int r, g, b, a;

        if (useAlpha)
        {
            final float a1 = c1.getAlpha() / 255f;
            final float a2 = c2.getAlpha() / 255f;
            final float af = a1 + a2;

            r = (int) (((c1.getRed() * a1) + (c2.getRed() * a2)) / af);
            g = (int) (((c1.getGreen() * a1) + (c2.getGreen() * a2)) / af);
            b = (int) (((c1.getBlue() * a1) + (c2.getBlue() * a2)) / af);
            a = Math.max(c1.getAlpha(), c2.getAlpha());
        }
        else
        {
            r = (c1.getRed() + c2.getRed()) / 2;
            g = (c1.getGreen() + c2.getGreen()) / 2;
            b = (c1.getBlue() + c2.getBlue()) / 2;
            a = 255;
        }

        return new Color(r, g, b, a);
    }

    /**
     * Mix 2 colors (no alpha)
     */
    public static Color mix(Color c1, Color c2)
    {
        return mix(c1, c2, false);
    }

    /**
     * Add 2 colors
     */
    public static Color add(Color c1, Color c2, boolean useAlpha)
    {
        final int r, g, b, a;

        r = Math.min(c1.getRed() + c2.getRed(), 255);
        g = Math.min(c1.getGreen() + c2.getGreen(), 255);
        b = Math.min(c1.getBlue() + c2.getBlue(), 255);

        if (useAlpha)
            a = Math.max(c1.getAlpha(), c2.getAlpha());
        else
            a = 255;

        return new Color(r, g, b, a);
    }

    /**
     * Add 2 colors
     */
    public static Color add(Color c1, Color c2)
    {
        return add(c1, c2, false);
    }

    /**
     * Sub 2 colors
     */
    public static Color sub(Color c1, Color c2, boolean useAlpha)
    {
        final int r, g, b, a;

        r = Math.max(c1.getRed() - c2.getRed(), 0);
        g = Math.max(c1.getGreen() - c2.getGreen(), 0);
        b = Math.max(c1.getBlue() - c2.getBlue(), 0);

        if (useAlpha)
            a = Math.max(c1.getAlpha(), c2.getAlpha());
        else
            a = 255;

        return new Color(r, g, b, a);
    }

    /**
     * Sub 2 colors
     */
    public static Color sub(Color c1, Color c2)
    {
        return sub(c1, c2, false);
    }

    /**
     * get to gray level (simple RGB mix)
     */
    public static int getGrayMix(Color c)
    {
        return getGrayMix(c.getRGB());
    }

    /**
     * get to gray level (simple RGB mix)
     */
    public static int getGrayMix(int rgb)
    {
        return (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + ((rgb >> 0) & 0xFF)) / 3;
    }

    /**
     * Convert to gray level color (simple RGB mix)
     */
    public static Color getGrayColorMix(Color c)
    {
        final int gray = getGrayMix(c);
        return new Color(gray, gray, gray);
    }

    /**
     * Convert to gray level color (from luminance calculation)
     */
    public static Color getGrayColorLum(Color c)
    {
        final int gray = getLuminance(c);
        return new Color(gray, gray, gray);
    }

    /**
     * Return luminance (in [0..255] range)
     */
    public static int getLuminance(Color c)
    {
        return (int) ((c.getRed() * 0.299) + (c.getGreen() * 0.587) + (c.getBlue() * 0.114));
    }
}
