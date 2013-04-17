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

import icy.util.ColorUtil;

import java.awt.Color;

/**
 * @author stephane
 */
public class LinearColorMap extends IcyColorMap
{
    /**
     * A built-in 'black-to-black' linear color map
     */
    public static final LinearColorMap black_ = new LinearColorMap("Black", Color.black, IcyColorMapType.GRAY);

    /**
     * A built-in 'black-to-white' linear color map
     */
    public static final LinearColorMap gray_ = new LinearColorMap("Gray", Color.white, IcyColorMapType.GRAY);
    public static final LinearColorMap white_ = gray_;

    /**
     * A built-in 'white-to-black' linear color map
     */
    public static final LinearColorMap gray_inv_ = new LinearColorMap("Gray inverse", Color.white, Color.black,
            IcyColorMapType.GRAY);
    public static final LinearColorMap white_inv_ = gray_inv_;

    /**
     * A built-in 'black-to-red' linear color map
     */
    public static final LinearColorMap red_ = new LinearColorMap("Red", Color.red);

    /**
     * A built-in 'black-to-blue' linear color map
     */
    public static final LinearColorMap blue_ = new LinearColorMap("Blue", Color.blue);

    /**
     * A built-in 'black-to-pink' linear color map
     */
    public static final LinearColorMap pink_ = new LinearColorMap("Pink", Color.pink);

    /**
     * A built-in 'black-to-cyan' linear color map
     */
    public static final LinearColorMap cyan_ = new LinearColorMap("Cyan", Color.cyan);

    /**
     * A built-in 'black-to-orange' linear color map
     */
    public static final LinearColorMap orange_ = new LinearColorMap("Orange", Color.orange);

    /**
     * A built-in 'black-to-yellow' linear color map
     */
    public static final LinearColorMap yellow_ = new LinearColorMap("Yellow", Color.yellow);

    /**
     * A built-in 'black-to-green' linear color map
     */
    public static final LinearColorMap green_ = new LinearColorMap("Green", Color.green);

    /**
     * A built-in 'black-to-magenta' linear color map
     */
    public static final LinearColorMap magenta_ = new LinearColorMap("Magenta", Color.magenta);

    /**
     * A built-in 'transparent-to-opaque' linear color map
     */
    public static final LinearColorMap alpha_ = new LinearColorMap("Alpha", new Color(0f, 0f, 0f, 0f), new Color(0f,
            0f, 0f, 1f), IcyColorMapType.ALPHA);

    /**
     * Creates a simple color map using a linear gradient of the given Color.
     */
    public LinearColorMap(String mapName, Color color)
    {
        this(mapName, Color.black, color, IcyColorMapType.RGB);
    }

    /**
     * Creates a simple color map using a linear gradient of the given Color.
     */
    public LinearColorMap(String mapName, Color color, IcyColorMapType type)
    {
        this(mapName, Color.black, color, type);
    }

    /**
     * Creates a simple color map using a linear gradient from 'colorFrom' to 'colorTo'.
     */
    public LinearColorMap(String mapName, Color colorFrom, Color colorTo)
    {
        this(mapName, colorFrom, colorTo, IcyColorMapType.RGB);
    }

    /**
     * Creates a simple color map using a linear gradient from 'colorFrom' to 'colorTo'.
     */
    public LinearColorMap(String mapName, Color colorFrom, Color colorTo, IcyColorMapType type)
    {
        super(mapName, type);

        beginUpdate();
        try
        {
            red.setControlPoint(0, colorFrom.getRed());
            green.setControlPoint(0, colorFrom.getGreen());
            blue.setControlPoint(0, colorFrom.getBlue());
            gray.setControlPoint(0, ColorUtil.getGrayMix(colorFrom));
            alpha.setControlPoint(0, colorFrom.getAlpha());

            red.setControlPoint(MAX_INDEX, colorTo.getRed());
            green.setControlPoint(MAX_INDEX, colorTo.getGreen());
            blue.setControlPoint(MAX_INDEX, colorTo.getBlue());
            gray.setControlPoint(MAX_INDEX, ColorUtil.getGrayMix(colorTo));
            alpha.setControlPoint(MAX_INDEX, colorTo.getAlpha());
        }
        finally
        {
            endUpdate();
        }
    }

}
