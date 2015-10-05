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
package icy.image.colormap;

import java.awt.Color;

/**
 * @author stephane
 */
public class HSVColorMap extends IcyColorMap
{

    /**
     * Creates a HSV color map with default saturation of 1 and value of 1
     */
    public HSVColorMap()
    {
        this(1f, 1f);
    }

    /**
     * Creates a HSV color map using a periodic variation the hue parameter, i.e. the actual RGB
     * color is the same (red) at both ends of the map.<br>
     * The implementation is actually similar to that of the java.awt.Color class, but this method
     * is optimized to produce floating point RGB values between 0 and 1 for directly use in a
     * ColorSpace.
     * 
     * @param saturation
     *        the color saturation between 0 and 1 (0 = weak, 1 = strong)
     * @param value
     *        the color value between 0 and 1 (0 = dark, 1 = light)
     * @throws IllegalArgumentException
     *         if any of the parameters is out of range
     */

    public HSVColorMap(float saturation, float value) throws IllegalArgumentException
    {
        super("HSV [" + saturation + "," + value + "]");

        if (saturation < 0 || saturation > 1)
            throw new IllegalArgumentException("HSV: Saturation must be in the range [0,1]");
        if (value < 0 || value > 1)
            throw new IllegalArgumentException("HSV: Value must be in the range [0,1]");

        final float min = (1f - saturation) * value;
        int index;

        beginUpdate();
        try
        {
            index = Math.round((MAX_INDEX / 6f) * 0f);
            setRGBControlPoint(index, new Color(value, min, min));

            index = Math.round((MAX_INDEX / 6f) * 1f);
            setRGBControlPoint(index, new Color(value, value, min));

            index = Math.round((MAX_INDEX / 6f) * 2f);
            setRGBControlPoint(index, new Color(min, value, min));

            index = Math.round((MAX_INDEX / 6f) * 3f);
            setRGBControlPoint(index, new Color(min, value, value));

            index = Math.round((MAX_INDEX / 6f) * 4f);
            setRGBControlPoint(index, new Color(min, min, value));

            index = Math.round((MAX_INDEX / 6f) * 5f);
            setRGBControlPoint(index, new Color(value, min, value));

            index = Math.round((MAX_INDEX / 6f) * 6f);
            setRGBControlPoint(index, new Color(value, min, min));
        }
        finally
        {
            endUpdate();
        }
    }
}
