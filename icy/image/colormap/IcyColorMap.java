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
package icy.image.colormap;

import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.file.xml.XMLPersistent;
import icy.image.colormap.IcyColorMapEvent.IcyColorMapEventType;
import icy.util.ColorUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class IcyColorMap implements ChangeListener, XMLPersistent
{
    public enum IcyColorMapType
    {
        RGB, GRAY, ALPHA
    };

    private static final String ID_TYPE = "type";
    private static final String ID_NAME = "name";
    private static final String ID_ENABLED = "enabled";
    private static final String ID_RED = "red";
    private static final String ID_GREEN = "green";
    private static final String ID_BLUE = "blue";
    private static final String ID_GRAY = "gray";
    private static final String ID_ALPHA = "alpha";

    /**
     * define the wanted colormap bits resolution (never change it)
     */
    public static final int COLORMAP_BITS = 8;
    public static final int MAX_LEVEL = (1 << COLORMAP_BITS) - 1;

    /**
     * define colormap size
     */
    public static final int SIZE = 256;
    public static final int MAX_INDEX = SIZE - 1;

    /**
     * colormap name
     */
    private String name;

    /**
     * enabled flag
     */
    private boolean enabled;

    /**
     * RED band
     */
    public final IcyColorMapComponent red;
    /**
     * GREEN band
     */
    public final IcyColorMapComponent green;
    /**
     * BLUE band
     */
    public final IcyColorMapComponent blue;
    /**
     * GRAY band
     */
    public final IcyColorMapComponent gray;
    /**
     * ALPHA band
     */
    public final IcyColorMapComponent alpha;

    /**
     * colormap type
     */
    private IcyColorMapType type;

    /**
     * pre-multiplied RGB caches
     */
    private final int premulRGB[][];
    private final float premulRGBNorm[][];

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    public IcyColorMap(String name, IcyColorMapType type)
    {
        this.name = name;
        enabled = true;

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // colormap band
        red = createColorMapBand((short) 0);
        green = createColorMapBand((short) 0);
        blue = createColorMapBand((short) 0);
        gray = createColorMapBand((short) 0);
        alpha = createColorMapBand((short) 255);

        this.type = type;

        // allocating and init RGB cache
        premulRGB = new int[IcyColorMap.SIZE][3];
        premulRGBNorm = new float[IcyColorMap.SIZE][3];
    }

    public IcyColorMap(String name)
    {
        this(name, IcyColorMapType.RGB);
    }

    public IcyColorMap(String name, Object maps)
    {
        this(name, IcyColorMapType.RGB);

        if (maps instanceof byte[][])
            copyFrom((byte[][]) maps);
        else if (maps instanceof short[][])
            copyFrom((short[][]) maps);

        // try to define color map type from data
        setTypeFromData(false);
    }

    /**
     * Create a copy of specified colormap.
     */
    public IcyColorMap(IcyColorMap colormap)
    {
        this(colormap.name, colormap.type);

        copyFrom(colormap);
    }

    public IcyColorMap()
    {
        this("");
    }

    protected IcyColorMapComponent createColorMapBand(short initValue)
    {
        return new IcyColorMapComponent(IcyColorMap.this, initValue);
    }

    /**
     * Return true if this color map is RGB type
     */
    public boolean isRGB()
    {
        return type == IcyColorMapType.RGB;
    }

    /**
     * Return true if this color map is GRAY type
     */
    public boolean isGray()
    {
        return type == IcyColorMapType.GRAY;
    }

    /**
     * Return true if this color map is ALPHA type
     */
    public boolean isAlpha()
    {
        return type == IcyColorMapType.ALPHA;
    }

    /**
     * @return the type
     */
    public IcyColorMapType getType()
    {
        return type;
    }

    /**
     * @param value
     *        the type to set
     */
    public void setType(IcyColorMapType value)
    {
        if (type != value)
        {
            type = value;

            changed(IcyColorMapEventType.TYPE_CHANGED);
        }
    }

    /**
     * @see IcyColorMap#setTypeFromData()
     */
    public void setTypeFromData(boolean notifyChange)
    {
        boolean grayColor = true;
        boolean noColor = true;
        boolean hasAlpha = false;
        IcyColorMapType cmType;

        for (int i = 0; i < MAX_INDEX; i++)
        {
            final short r = red.map[i];
            final short g = green.map[i];
            final short b = blue.map[i];
            final short a = alpha.map[i];

            grayColor &= (r == g) && (r == b);
            noColor &= (r == 0) && (g == 0) && (b == 0);
            hasAlpha |= (a != MAX_LEVEL);
        }

        if (noColor && hasAlpha)
            cmType = IcyColorMapType.ALPHA;
        else if (grayColor && !noColor)
        {
            // set gray map
            gray.copyFrom(red.map, 0);
            cmType = IcyColorMapType.GRAY;
        }
        else
            cmType = IcyColorMapType.RGB;

        if (notifyChange)
            setType(cmType);
        else
            type = cmType;
    }

    /**
     * Define the type of color map depending its RGBA data.<br>
     * If map contains only alpha information then type = <code>IcyColorMapType.ALPHA</code><br>
     * If map contains only grey level then type = <code>IcyColorMapType.GRAY</code><br>
     * else type = <code>IcyColorMapType.RGB</code>
     */
    public void setTypeFromData()
    {
        setTypeFromData(true);
    }

    /**
     * Set a red control point to specified index and value
     */
    public void setRedControlPoint(int index, int value)
    {
        // set control point
        red.setControlPoint(index, value);
    }

    /**
     * Set a green control point to specified index and value
     */
    public void setGreenControlPoint(int index, int value)
    {
        green.setControlPoint(index, value);
    }

    /**
     * Set a blue control point to specified index and value
     */
    public void setBlueControlPoint(int index, int value)
    {
        blue.setControlPoint(index, value);
    }

    /**
     * Set a gray control point to specified index and value
     */
    public void setGrayControlPoint(int index, int value)
    {
        gray.setControlPoint(index, value);
    }

    /**
     * Set a alpha control point to specified index and value
     */
    public void setAlphaControlPoint(int index, int value)
    {
        alpha.setControlPoint(index, value);
    }

    /**
     * Set RGB control point values to specified index
     */
    public void setRGBControlPoint(int index, Color value)
    {
        red.setControlPoint(index, (short) value.getRed());
        green.setControlPoint(index, (short) value.getGreen());
        blue.setControlPoint(index, (short) value.getBlue());
        gray.setControlPoint(index, (short) ColorUtil.getGrayMix(value));
    }

    /**
     * Set ARGB control point values to specified index
     */
    public void setARGBControlPoint(int index, Color value)
    {
        alpha.setControlPoint(index, (short) value.getAlpha());
        red.setControlPoint(index, (short) value.getRed());
        green.setControlPoint(index, (short) value.getGreen());
        blue.setControlPoint(index, (short) value.getBlue());
        gray.setControlPoint(index, (short) ColorUtil.getGrayMix(value));
    }

    /**
     * Returns the blue component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public short[] getBlueMap()
    {
        if (type == IcyColorMapType.RGB)
            return blue.map;
        if (type == IcyColorMapType.GRAY)
            return gray.map;

        return null;
    }

    /**
     * Returns the green component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public short[] getGreenMap()
    {
        if (type == IcyColorMapType.RGB)
            return green.map;
        if (type == IcyColorMapType.GRAY)
            return gray.map;

        return null;
    }

    /**
     * Returns the red component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public short[] getRedMap()
    {
        if (type == IcyColorMapType.RGB)
            return red.map;
        if (type == IcyColorMapType.GRAY)
            return gray.map;

        return null;
    }

    /**
     * Returns the alpha component map.
     */
    public short[] getAlphaMap()
    {
        return alpha.map;
    }

    /**
     * Returns the normalized blue component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public float[] getNormalizedBlueMap()
    {
        if (type == IcyColorMapType.RGB)
            return blue.mapf;
        if (type == IcyColorMapType.GRAY)
            return gray.mapf;

        return null;
    }

    /**
     * Returns the normalized green component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public float[] getNormalizedGreenMap()
    {
        if (type == IcyColorMapType.RGB)
            return green.mapf;
        if (type == IcyColorMapType.GRAY)
            return gray.mapf;

        return null;
    }

    /**
     * Returns the normalized red component map.<br>
     * If the color map type is {@link IcyColorMapType#GRAY} then it returns the gray map instead.
     * If the color map type is {@link IcyColorMapType#ALPHA} then it returns <code>null</code>.
     */
    public float[] getNormalizedRedMap()
    {
        if (type == IcyColorMapType.RGB)
            return red.mapf;
        if (type == IcyColorMapType.GRAY)
            return gray.mapf;

        return null;
    }

    /**
     * Returns the normalized alpha component map.
     */
    public float[] getNormalizedAlphaMap()
    {
        return alpha.mapf;
    }

    /**
     * Get blue intensity from an input index
     * 
     * @param index
     * @return blue intensity ([0..255] range)
     */
    public short getBlue(int index)
    {
        if (type == IcyColorMapType.RGB)
            return blue.map[index];
        if (type == IcyColorMapType.GRAY)
            return gray.map[index];

        return 0;
    }

    /**
     * Get green intensity from an input index
     * 
     * @param index
     * @return green intensity ([0..255] range)
     */
    public short getGreen(int index)
    {
        if (type == IcyColorMapType.RGB)
            return green.map[index];
        if (type == IcyColorMapType.GRAY)
            return gray.map[index];

        return 0;
    }

    /**
     * Get red intensity from an input index
     * 
     * @param index
     * @return red intensity ([0..255] range)
     */
    public short getRed(int index)
    {
        if (type == IcyColorMapType.RGB)
            return red.map[index];
        if (type == IcyColorMapType.GRAY)
            return gray.map[index];

        return 0;
    }

    /**
     * Get alpha intensity from an input index
     * 
     * @param index
     * @return alpha intensity ([0..255] range)
     */
    public short getAlpha(int index)
    {
        return alpha.map[index];
    }

    /**
     * Get normalized blue intensity from an input index
     * 
     * @param index
     * @return normalized blue intensity
     */
    public float getNormalizedBlue(int index)
    {
        if (type == IcyColorMapType.RGB)
            return blue.mapf[index];
        if (type == IcyColorMapType.GRAY)
            return gray.mapf[index];

        return 0;
    }

    /**
     * Get normalized green intensity from an input index
     * 
     * @param index
     * @return normalized green intensity
     */
    public float getNormalizedGreen(int index)
    {
        if (type == IcyColorMapType.RGB)
            return green.mapf[index];
        if (type == IcyColorMapType.GRAY)
            return gray.mapf[index];

        return 0;
    }

    /**
     * Get normalized red intensity from an input index
     * 
     * @param index
     * @return normalized red intensity
     */
    public float getNormalizedRed(int index)
    {
        if (type == IcyColorMapType.RGB)
            return red.mapf[index];
        if (type == IcyColorMapType.GRAY)
            return gray.mapf[index];

        return 0;
    }

    /**
     * Get alpha normalized intensity from an input index
     * 
     * @param index
     * @return normalized alpha intensity
     */
    public float getNormalizedAlpha(int index)
    {
        return alpha.mapf[index];
    }

    /**
     * Get blue intensity from a normalized input index
     * 
     * @param index
     * @return blue intensity ([0..255] range)
     */
    public short getBlue(float index)
    {
        return getBlue((int) (index * MAX_INDEX));
    }

    /**
     * Get green intensity from a normalized input index
     * 
     * @param index
     * @return green intensity ([0..255] range)
     */
    public short getGreen(float index)
    {
        return getGreen((int) (index * MAX_INDEX));
    }

    /**
     * Get red intensity from a normalized input index
     * 
     * @param index
     * @return red intensity ([0..255] range)
     */
    public short getRed(float index)
    {
        return getRed((int) (index * MAX_INDEX));
    }

    /**
     * Get alpha intensity from a normalized input index
     * 
     * @param index
     * @return alpha intensity ([0..255] range)
     */
    public short getAlpha(float index)
    {
        return getAlpha((int) (index * MAX_INDEX));
    }

    /**
     * Get normalized blue intensity from a normalized input index
     * 
     * @param index
     * @return normalized blue intensity
     */
    public float getNormalizedBlue(float index)
    {
        return getNormalizedBlue((int) (index * MAX_INDEX));
    }

    /**
     * Get normalized green intensity from a normalized input index
     * 
     * @param index
     * @return normalized green intensity
     */
    public float getNormalizedGreen(float index)
    {
        return getNormalizedGreen((int) (index * MAX_INDEX));
    }

    /**
     * Get normalized red intensity from a normalized input index
     * 
     * @param index
     * @return normalized red intensity
     */
    public float getNormalizedRed(float index)
    {
        return getNormalizedRed((int) (index * MAX_INDEX));
    }

    /**
     * Get normalized alpha intensity from a normalized input index
     * 
     * @param index
     * @return normalized alpha intensity
     */
    public float getNormalizedAlpha(float index)
    {
        return getNormalizedAlpha((int) (index * MAX_INDEX));
    }

    /**
     * Set red intensity to specified index
     */
    public void setRed(int index, short value)
    {
        red.setValue(index, value);
    }

    /**
     * Set green intensity to specified index
     */
    public void setGreen(int index, short value)
    {
        green.setValue(index, value);
    }

    /**
     * Set blue intensity to specified index
     */
    public void setBlue(int index, short value)
    {
        blue.setValue(index, value);
    }

    /**
     * Set gray intensity to specified index
     */
    public void setGray(int index, short value)
    {
        gray.setValue(index, value);
    }

    /**
     * Set alpha intensity to specified index
     */
    public void setAlpha(int index, short value)
    {
        alpha.setValue(index, value);
    }

    /**
     * Set red intensity (normalized) to specified index
     */
    public void setNormalizedRed(int index, float value)
    {
        red.setNormalizedValue(index, value);
    }

    /**
     * Set green intensity (normalized) to specified index
     */
    public void setNormalizedGreen(int index, float value)
    {
        green.setNormalizedValue(index, value);
    }

    /**
     * Set blue intensity (normalized) to specified index
     */
    public void setNormalizedBlue(int index, float value)
    {
        blue.setNormalizedValue(index, value);
    }

    /**
     * Set gray intensity (normalized) to specified index
     */
    public void setNormalizedGray(int index, float value)
    {
        gray.setNormalizedValue(index, value);
    }

    /**
     * Set alpha intensity (normalized) to specified index
     */
    public void setNormalizedAlpha(int index, float value)
    {
        alpha.setNormalizedValue(index, value);
    }

    /**
     * Set RGB color to specified index
     */
    public void setRGB(int index, int rgb)
    {
        alpha.setValue(index, MAX_LEVEL);
        red.setValue(index, (rgb >> 16) & 0xFF);
        green.setValue(index, (rgb >> 8) & 0xFF);
        blue.setValue(index, (rgb >> 0) & 0xFF);
        gray.setValue(index, ColorUtil.getGrayMix(rgb));
    }

    /**
     * Set RGB color to specified index
     */
    public void setRGB(int index, Color value)
    {
        setRGB(index, value.getRGB());
    }

    /**
     * Set ARGB color to specified index
     */
    public void setARGB(int index, int argb)
    {
        alpha.setValue(index, (argb >> 24) & 0xFF);
        red.setValue(index, (argb >> 16) & 0xFF);
        green.setValue(index, (argb >> 8) & 0xFF);
        blue.setValue(index, (argb >> 0) & 0xFF);
        gray.setValue(index, ColorUtil.getGrayMix(argb));
    }

    /**
     * Set ARGB color to specified index
     */
    public void setARGB(int index, Color value)
    {
        setARGB(index, value.getRGB());
    }

    /**
     * Set the alpha channel to opaque
     */
    public void setAlphaToOpaque()
    {
        alpha.beginUpdate();
        try
        {
            alpha.removeAllControlPoint();
            alpha.setControlPoint(0, 1f);
            alpha.setControlPoint(255, 1f);
        }
        finally
        {
            alpha.endUpdate();
        }
    }

    /**
     * Set the alpha channel to linear opacity (0 to 1)
     */
    public void setAlphaToLinear()
    {
        alpha.beginUpdate();
        try
        {
            alpha.removeAllControlPoint();
            alpha.setControlPoint(0, 0f);
            alpha.setControlPoint(255, 1f);
        }
        finally
        {
            alpha.endUpdate();
        }
    }

    /**
     * Set the alpha channel to an optimized linear transparency for 3D volume display
     */
    public void setAlphaToLinear3D()
    {
        alpha.beginUpdate();
        try
        {
            alpha.removeAllControlPoint();
            alpha.setControlPoint(0, 0f);
            alpha.setControlPoint(32, 0f);
            alpha.setControlPoint(255, 0.4f);
        }
        finally
        {
            alpha.endUpdate();
        }
    }

    /**
     * @deprecated Use {@link #setAlphaToLinear3D()} instead
     */
    @Deprecated
    public void setDefaultAlphaFor3D()
    {
        setAlphaToLinear3D();
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Set enabled flag.<br>
     * This flag is used to test if the color map is enabled or not.<br>
     * It is up to the developer to implement it or not.
     * 
     * @param enabled
     *        the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        if (this.enabled != enabled)
        {
            this.enabled = enabled;
            changed(IcyColorMapEventType.ENABLED_CHANGED);
        }
    }

    /**
     * Gets a Color object representing the color at the specified index
     * 
     * @param index
     *        the index of the color map to retrieve
     * @return a Color object
     */
    public Color getColor(int index)
    {
        switch (type)
        {
            case RGB:
                return new Color(red.map[index], green.map[index], blue.map[index], alpha.map[index]);
            case GRAY:
                return new Color(gray.map[index], gray.map[index], gray.map[index], alpha.map[index]);
            case ALPHA:
                return new Color(0, 0, 0, alpha.map[index]);
        }

        return Color.black;
    }

    /**
     * Return the pre-multiplied RGB cache
     */
    public int[][] getPremulRGB()
    {
        return premulRGB;
    }

    /**
     * Return the pre-multiplied RGB cache (normalized)
     */
    public float[][] getPremulRGBNorm()
    {
        return premulRGBNorm;
    }

    /**
     * Copy data from specified source colormap.
     * 
     * @param copyAlpha
     *        Also copy the alpha information.
     */
    public void copyFrom(IcyColorMap srcColorMap, boolean copyAlpha)
    {
        beginUpdate();
        try
        {
            // copy colormap band
            red.copyFrom(srcColorMap.red);
            green.copyFrom(srcColorMap.green);
            blue.copyFrom(srcColorMap.blue);
            gray.copyFrom(srcColorMap.gray);
            if (copyAlpha)
                alpha.copyFrom(srcColorMap.alpha);
            // copy type
            setType(srcColorMap.type);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Copy data from specified source colormap
     */
    public void copyFrom(IcyColorMap srcColorMap)
    {
        copyFrom(srcColorMap, true);
    }

    /**
     * Copy data from specified 2D byte array.
     * 
     * @param copyAlpha
     *        Also copy the alpha information.
     */
    public void copyFrom(byte[][] maps, boolean copyAlpha)
    {
        final int len = maps.length;

        beginUpdate();
        try
        {
            // red component
            if (len > 0)
                red.copyFrom(maps[0]);
            if (len > 1)
                green.copyFrom(maps[1]);
            if (len > 2)
                blue.copyFrom(maps[2]);
            if (copyAlpha && (len > 3))
                alpha.copyFrom(maps[3]);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Copy data from specified 2D byte array
     */
    public void copyFrom(byte[][] maps)
    {
        copyFrom(maps, true);
    }

    /**
     * Copy data from specified 2D short array.
     * 
     * @param copyAlpha
     *        Also copy the alpha information.
     */
    public void copyFrom(short[][] maps, boolean copyAlpha)
    {
        final int len = maps.length;

        beginUpdate();
        try
        {
            // red component
            if (len > 0)
                red.copyFrom(maps[0], 8);
            if (len > 1)
                green.copyFrom(maps[1], 8);
            if (len > 2)
                blue.copyFrom(maps[2], 8);
            if (copyAlpha && (len > 3))
                alpha.copyFrom(maps[3], 8);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Copy data from specified 2D short array.
     */
    public void copyFrom(short[][] maps)
    {
        copyFrom(maps, true);
    }

    /**
     * Return true if this is a linear type colormap.<br>
     * Linear colormap are used to display plain gray or color image.<br>
     * A non linear colormap means you usually have an indexed color image or
     * you want to enhance contrast/color in display.
     */
    public boolean isLinear()
    {
        switch (type)
        {
            default:
                return red.isLinear() && green.isLinear() && blue.isLinear();
            case GRAY:
                return gray.isLinear();
            case ALPHA:
                return alpha.isLinear();
        }
    }

    /**
     * Return true if this is a total black colormap.
     */
    public boolean isBlack()
    {
        switch (type)
        {
            case RGB:
                for (int i = 0; i < MAX_INDEX; i++)
                    if ((red.map[i] | green.map[i] | blue.map[i]) != 0)
                        return false;
                return true;

            case GRAY:
                for (int i = 0; i < MAX_INDEX; i++)
                    if (gray.map[i] != 0)
                        return false;
                return true;

            default:
                return false;
        }
    }

    /**
     * Returns the dominant color of this colormap.<br>
     * Warning: this need sometime to compute.
     */
    public Color getDominantColor()
    {
        final Color colors[] = new Color[SIZE];

        for (int i = 0; i < colors.length; i++)
            colors[i] = getColor(i);

        return ColorUtil.getDominantColor(colors);
    }

    /**
     * Update internal RGB cache
     */
    private void updateRGBCache()
    {
        for (int i = 0; i < SIZE; i++)
        {
            final float af = alpha.mapf[i];
            final float rgbn[] = premulRGBNorm[i];

            switch (type)
            {
                case GRAY:
                    final float grayValue = gray.mapf[i] * af;
                    rgbn[0] = grayValue;
                    rgbn[1] = grayValue;
                    rgbn[2] = grayValue;
                    break;

                case RGB:
                    rgbn[0] = blue.mapf[i] * af;
                    rgbn[1] = green.mapf[i] * af;
                    rgbn[2] = red.mapf[i] * af;
                    break;

                default:
                    rgbn[0] = 0f;
                    rgbn[1] = 0f;
                    rgbn[2] = 0f;
                    break;
            }

            final int rgb[] = premulRGB[i];

            rgb[0] = (int) (rgbn[0] * MAX_LEVEL);
            rgb[1] = (int) (rgbn[1] * MAX_LEVEL);
            rgb[2] = (int) (rgbn[2] * MAX_LEVEL);
        }
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(IcyColorMapListener listener)
    {
        listeners.add(IcyColorMapListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(IcyColorMapListener listener)
    {
        listeners.remove(IcyColorMapListener.class, listener);
    }

    /**
     * fire event
     */
    public void fireEvent(IcyColorMapEvent e)
    {
        for (IcyColorMapListener listener : listeners.getListeners(IcyColorMapListener.class))
            listener.colorMapChanged(e);
    }

    /**
     * called when colormap data changed
     */
    public void changed()
    {
        changed(IcyColorMapEventType.MAP_CHANGED);
    }

    /**
     * called when colormap changed
     */
    private void changed(IcyColorMapEventType type)
    {
        // handle changed via updater object
        updater.changed(new IcyColorMapEvent(this, type));
    }

    @Override
    public void onChanged(EventHierarchicalChecker e)
    {
        final IcyColorMapEvent event = (IcyColorMapEvent) e;

        switch (event.getType())
        {
        // refresh RGB cache
            case MAP_CHANGED:
            case TYPE_CHANGED:
                updateRGBCache();
                break;
        }

        // notify listener we have changed
        fireEvent(event);
    }

    /**
     * @see icy.common.UpdateEventHandler#beginUpdate()
     */
    public void beginUpdate()
    {
        updater.beginUpdate();

        red.beginUpdate();
        green.beginUpdate();
        blue.beginUpdate();
        gray.beginUpdate();
        alpha.beginUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#endUpdate()
     */
    public void endUpdate()
    {
        alpha.endUpdate();
        gray.endUpdate();
        blue.endUpdate();
        green.endUpdate();
        red.endUpdate();

        updater.endUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#isUpdating()
     */
    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    @Override
    public String toString()
    {
        return name + " : " + super.toString();
    }

    /**
     * Return true if the colormap has the same type and same color intensities than specified one.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof IcyColorMap)
        {
            final IcyColorMap colormap = (IcyColorMap) obj;

            if (colormap.getType() != type)
                return false;

            if (!Arrays.equals(red.map, colormap.red.map))
                return false;
            if (!Arrays.equals(green.map, colormap.green.map))
                return false;
            if (!Arrays.equals(blue.map, colormap.blue.map))
                return false;
            if (!Arrays.equals(gray.map, colormap.gray.map))
                return false;
            if (!Arrays.equals(alpha.map, colormap.alpha.map))
                return false;

            return true;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return red.map.hashCode() ^ green.map.hashCode() ^ blue.map.hashCode() ^ gray.map.hashCode()
                ^ alpha.map.hashCode() ^ type.ordinal();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setName(XMLUtil.getElementValue(node, ID_NAME, ""));
            setEnabled(XMLUtil.getElementBooleanValue(node, ID_ENABLED, true));
            setType(IcyColorMapType.valueOf(XMLUtil.getElementValue(node, ID_TYPE, IcyColorMapType.RGB.toString())));

            boolean result = true;

            result = result && red.loadFromXML(XMLUtil.getElement(node, ID_RED));
            result = result && green.loadFromXML(XMLUtil.getElement(node, ID_GREEN));
            result = result && blue.loadFromXML(XMLUtil.getElement(node, ID_BLUE));
            result = result && gray.loadFromXML(XMLUtil.getElement(node, ID_GRAY));
            result = result && alpha.loadFromXML(XMLUtil.getElement(node, ID_ALPHA));

            return result;
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementValue(node, ID_NAME, getName());
        XMLUtil.setElementBooleanValue(node, ID_ENABLED, isEnabled());
        XMLUtil.setElementValue(node, ID_TYPE, getType().toString());

        boolean result = true;

        result = result && red.saveToXML(XMLUtil.setElement(node, ID_RED));
        result = result && green.saveToXML(XMLUtil.setElement(node, ID_GREEN));
        result = result && blue.saveToXML(XMLUtil.setElement(node, ID_BLUE));
        result = result && gray.saveToXML(XMLUtil.setElement(node, ID_GRAY));
        result = result && alpha.saveToXML(XMLUtil.setElement(node, ID_ALPHA));

        return result;
    }

}
