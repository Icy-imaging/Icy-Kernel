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
package icy.image.colormap;

import icy.common.EventHierarchicalChecker;
import icy.common.IcyChangedListener;
import icy.common.UpdateEventHandler;
import icy.file.xml.XMLPersistent;
import icy.image.colormap.IcyColorMapEvent.IcyColorMapEventType;
import icy.util.ColorUtil;
import icy.util.XMLUtil;

import java.awt.Color;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class IcyColorMap implements IcyChangedListener, XMLPersistent
{
    public enum IcyColorMapType
    {
        RGB, GRAY, ALPHA
    };

    private static final String ID_TYPE = "type";
    private static final String ID_NAME = "name";
    private static final String ID_RED = "red";
    private static final String ID_GREEN = "green";
    private static final String ID_BLUE = "blue";
    private static final String ID_GRAY = "gray";
    private static final String ID_ALPHA = "alpha";

    /**
     * define the wanted max level (never change it)
     */
    public static final int MAX_LEVEL = 255;

    /**
     * define colormap size
     */
    public static final int SIZE = 0x100;
    public static final int MAX_INDEX = SIZE - 1;

    /**
     * colormap name
     */
    private String name;

    /**
     * RED band
     */
    public final IcyColorMapBand red;
    /**
     * GREEN band
     */
    public final IcyColorMapBand green;
    /**
     * BLUE band
     */
    public final IcyColorMapBand blue;
    /**
     * GRAY band
     */
    public final IcyColorMapBand gray;
    /**
     * ALPHA band
     */
    public final IcyColorMapBand alpha;

    /**
     * colormap type
     */
    private IcyColorMapType type;

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

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // colormap band
        red = createColorMapBand((short) 0);
        green = createColorMapBand((short) 0);
        blue = createColorMapBand((short) 0);
        gray = createColorMapBand((short) 0);
        alpha = createColorMapBand((short) 255);

        this.type = type;
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

    public IcyColorMap()
    {
        this("");
    }

    protected IcyColorMapBand createColorMapBand(short initValue)
    {
        return new IcyColorMapBand(IcyColorMap.this, initValue);
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

            typeChanged();
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
     * Copy data from specified source colormap
     */
    public void copyFrom(IcyColorMap srcColorMap)
    {
        beginUpdate();
        try
        {
            // copy colormap band
            red.copyFrom(srcColorMap.red);
            green.copyFrom(srcColorMap.green);
            blue.copyFrom(srcColorMap.blue);
            gray.copyFrom(srcColorMap.gray);
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
     * Copy data from specified 2D byte array
     */
    public void copyFrom(byte[][] maps)
    {
        final int len = maps.length;

        // red component
        if (len > 0)
            red.copyFrom(maps[0]);
        if (len > 1)
            green.copyFrom(maps[1]);
        if (len > 2)
            blue.copyFrom(maps[2]);
        if (len > 3)
            alpha.copyFrom(maps[3]);
    }

    /**
     * Copy data from specified 2D short array
     */
    public void copyFrom(short[][] maps)
    {
        final int len = maps.length;

        // red component
        if (len > 0)
            red.copyFrom(maps[0], 8);
        if (len > 1)
            green.copyFrom(maps[1], 8);
        if (len > 2)
            blue.copyFrom(maps[2], 8);
        if (len > 3)
            alpha.copyFrom(maps[3], 8);
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
     * called when colormap type changed
     */
    private void typeChanged()
    {
        // handle changed via updater object
        updater.changed(new IcyColorMapEvent(this, IcyColorMapEventType.TYPE_CHANGED));
    }

    /**
     * called when colormap data changed
     */
    public void changed()
    {
        // handle changed via updater object
        updater.changed(new IcyColorMapEvent(this, IcyColorMapEventType.MAP_CHANGED));
    }

    @Override
    public void onChanged(EventHierarchicalChecker compare)
    {
        final IcyColorMapEvent event = (IcyColorMapEvent) compare;

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

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setName(XMLUtil.getElementValue(node, ID_NAME, ""));
            setType(IcyColorMapType.valueOf(XMLUtil.getElementValue(node, ID_TYPE, "")));

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
