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
package icy.util;

import icy.math.MathUtil;

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * Color utilities class.
 * 
 * @author Stephane
 */
public class ColorUtil
{
    /**
     * RGB colorSpace
     */
    public final static ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * Basic rainbow colors
     */
    private final static Color[] colors = generateRainbow(32, true, true, true);

    /**
     * Returns a random color.
     */
    public static Color getRandomColor()
    {
        return colors[Random.nextInt(20)];
    }

    /**
     * Generates a rainbow color table (HSV ramp) of the specified size.
     * 
     * @param saturation
     *        saturation factor (from 0 to 1).
     * @param brightness
     *        brightness factor (from 0 to 1).
     * @param size
     *        the size of rainbow color table.
     * @param black
     *        if true the table will also contains a black color entry.
     * @param white
     *        if true the table will also contains a white color entry.
     * @param gray
     *        if true the table will also contains a gray color entry.
     */
    public static Color[] generateRainbow(float saturation, float brightness, int size, boolean black, boolean white,
            boolean gray)
    {
        final Color[] result = new Color[size];

        int start = 0;
        if (black)
            result[start++] = Color.black;
        if (white)
            result[start++] = Color.white;
        if (gray)
            result[start++] = Color.gray;

        for (int i = start; i < result.length; i++)
            result[i] = Color.getHSBColor((float) (i - start) / (float) (size - start), saturation, brightness);

        return result;
    }

    /**
     * Generates a rainbow color table (HSV ramp) of the specified size.
     * 
     * @param size
     *        the size of the rainbow color table.
     * @param black
     *        if true the table will also contains a black color entry.
     * @param white
     *        if true the table will also contains a white color entry.
     * @param gray
     *        if true the table will also contains a gray color entry.
     */
    public static Color[] generateRainbow(int size, boolean black, boolean white, boolean gray)
    {
        return generateRainbow(1f, 1f, size, black, white, gray);
    }

    /**
     * Generates a rainbow color table (HSV ramp) of the specified size.
     * 
     * @param size
     *        the size of the HSV color table.
     */
    public static Color[] generateRainbow(int size)
    {
        return generateRainbow(size, false, false, false);
    }

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
     * Returns <code>true</code> if the specified color is pure black (alpha is not verified)
     */
    public static boolean isBlack(Color color)
    {
        return (color.getRGB() & 0x00FFFFFF) == 0;
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
     * Mix 2 colors using the following ratio for mixing:<br/>
     * 0f means 100% of color 1 and 0% of color 2<br/>
     * 0.5f means 50% of color 1 and 50% of color 2<br/>
     * 1f means 0% of color 1 and 100% of color 2
     */
    public static Color mix(Color c1, Color c2, float ratio)
    {
        final int r, g, b;
        final float r2 = Math.min(1f, Math.max(0f, ratio));
        final float r1 = 1f - r2;

        r = (int) ((c1.getRed() * r1) + (c2.getRed() * r2));
        g = (int) ((c1.getGreen() * r1) + (c2.getGreen() * r2));
        b = (int) ((c1.getBlue() * r1) + (c2.getBlue() * r2));

        return new Color(r, g, b);
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
     * Subtract 2 colors
     */
    public static Color sub(Color c1, Color c2)
    {
        return sub(c1, c2, false);
    }

    /**
     * Get opposite (XORed) color
     */
    public static Color xor(Color c)
    {
        return new Color(c.getRed() ^ 0xFF, c.getGreen() ^ 0xFF, c.getBlue() ^ 0xFF, c.getAlpha());
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

    /**
     * Convert the specified color to HSV color.
     */
    public static float[] toHSV(Color c)
    {
        return toHSV(c.getRGBColorComponents(null));
    }

    /**
     * Convert the specified RGB color to HSV color.
     */
    public static float[] toHSV(float[] rgb)
    {
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];
        float min, max, delta;
        float h, s, v;

        min = Math.min(r, Math.min(g, b));
        max = Math.max(r, Math.max(g, b));

        // black
        if (max == 0f)
            return new float[] {0, 0, 0};

        v = max;
        delta = max - min;
        s = delta / max;

        // graylevel
        if (delta == 0f)
            return new float[] {0, s, v};

        if (r == max)
            // between yellow & magenta
            h = (g - b) / delta;
        else if (g == max)
            // between cyan & yellow
            h = 2 + (b - r) / delta;
        else
            // between magenta & cyan
            h = 4 + (r - g) / delta;

        // want positif hue
        if (h < 0)
            h += 6f;

        return new float[] {h / 6f, s, v};
    }

    /**
     * Convert the specified HSV color to RGB color.
     */
    public static float[] fromHSV(float[] hsv)
    {
        float h = hsv[0];
        float s = hsv[0];
        float v = hsv[0];
        float f, p, q, t;
        float r, g, b;
        int i;

        // no color
        if (s == 0f)
            return new float[] {v, v, v};

        // sector 0 to 5
        h *= 6f;
        i = (int) Math.floor(h);
        // factorial part of h
        f = h - i;
        p = v * (1f - s);
        q = v * (1f - (s * f));
        t = v * (1f - (s * (1 - f)));

        switch (i)
        {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }

        return new float[] {r, g, b};
    }

    /**
     * Convert the specified XYZ color to RGB color.
     */
    public static float[] fromXYZ(float[] xyz)
    {
        return sRGB.fromCIEXYZ(xyz);
    }

    /**
     * Convert the specified color to XYZ color.
     */
    public static float[] toXYZ(Color c)
    {
        return toXYZ(c.getRGBColorComponents(null));
    }

    /**
     * Convert the specified RGB color to XYZ color.
     */
    public static float[] toXYZ(float[] rgb)
    {
        return sRGB.toCIEXYZ(rgb);
    }

    /**
     * Convert the specified color to LAB color.
     */
    public static float[] toLAB(Color c)
    {
        return toLAB(c.getRGBColorComponents(null));
    }

    /**
     * Convert the specified RGB color to LAB color.
     */
    public static float[] toLAB(float[] rgb)
    {
        return XYZtoLAB(toXYZ(rgb));
    }

    private static float pivotXYZ(float value)
    {
        return (value > 0.008856f) ? (float) MathUtil.cubicRoot(value) : (7.787f * value) + 0.1379f;
    }

    /**
     * Convert the specified XYZ color to LAB color.
     */
    public static float[] XYZtoLAB(float[] xyz)
    {
        float x = pivotXYZ(xyz[0] / 95.047f);
        float y = pivotXYZ(xyz[1] / 100f);
        float z = pivotXYZ(xyz[2] / 108.883f);

        float l = Math.max(0, (116f * y) - 16f);
        float a = 500f * (x - y);
        float b = 200f * (y - z);

        return new float[] {l, a, b};
    }

    /**
     * Compute and returns the distance between the 2 colors.<br>
     * The HSV distance returns a value between 0 and 1 where 1 is maximum distance.<br>
     * The LAB distance returns a positive value where > 2.3 value is considered a
     * significant distance.
     * 
     * @param c1
     *        first color
     * @param c2
     *        second color
     * @param hsv
     *        If set to true we use the HSV color space to compute the color distance otherwise we
     *        use the LAB color space.
     */
    public static double getDistance(Color c1, Color c2, boolean hsv)
    {
        if (hsv)
        {
            // use HSV color space
            final float[] hsv1 = toHSV(c1);
            final float[] hsv2 = toHSV(c2);

            return getDistance(hsv1, hsv2, true);
        }

        // use LAB color space
        final float[] lab1 = toLAB(c1);
        final float[] lab2 = toLAB(c2);

        return getDistance(lab1, lab2, true);
    }

    /**
     * Returns the distance between 2 colors from same color space.
     */
    static double getDistance(float[] c1, float[] c2, boolean compareThirdComponent)
    {
        float result = (float) (Math.pow(c1[0] - c2[0], 2d) + Math.pow(c1[1] - c2[1], 2d));

        if (compareThirdComponent)
            result += Math.pow(c1[2] - c2[2], 2d);

        return result;
    }

    /**
     * Returns the dominant color from the specified color array.<br>
     * The dominant color is calculated by computing the color histogram from a rainbow gradient and
     * returning the highest bin number.
     */
    public static Color getDominantColor(Color colors[])
    {
        return getDominantColor(colors, 33);
    }

    /**
     * Returns the dominant color from the specified color array.<br>
     * The dominant color is calculated by computing the color histogram from a rainbow gradient and
     * returning the color corresponding to the highest bin.
     * 
     * @param colors
     *        Color array we want to retrieve the dominant color from.
     * @param binNumber
     *        the number of bin to construct the rainbow gradient.
     */
    public static Color getDominantColor(Color colors[], int binNumber)
    {
        final Color[] baseColors = generateRainbow(1f, 1f, binNumber, false, false, true);

        final float[][] colorsHSV = new float[colors.length][];
        final float[][] baseColorsHSV = new float[binNumber][];

        // convert colors to HSV float component
        for (int i = 0; i < colors.length; i++)
            colorsHSV[i] = toHSV(colors[i]);
        for (int i = 0; i < baseColors.length; i++)
            baseColorsHSV[i] = toHSV(baseColors[i]);

        final int[] bins = new int[binNumber];

        for (float[] colorHsv : colorsHSV)
        {
            double minDist = getDistance(colorHsv, baseColorsHSV[0], true);
            int minInd = 0;

            for (int ind = 1; ind < baseColorsHSV.length; ind++)
            {
                final double dist = getDistance(colorHsv, baseColorsHSV[ind], true);

                if (dist < minDist)
                {
                    minDist = dist;
                    minInd = ind;
                }
            }

            bins[minInd]++;
        }

        int max = bins[0];
        int maxInd = 0;

        for (int i = 1; i < bins.length; i++)
        {
            final int v = bins[i];

            if (v > max)
            {
                max = v;
                maxInd = i;
            }
        }

        return baseColors[maxInd];
    }

}
