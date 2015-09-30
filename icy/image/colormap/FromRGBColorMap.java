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

/**
 * @author stephane
 */
public class FromRGBColorMap
{

    /**
     * define colormap size
     */
    public static final int COLORMAP_SIZE = 0x100;
    public static final int COLORMAP_MAX = COLORMAP_SIZE - 1;

    /**
     * normalized maps
     */
    public final float[][] maps;

    /**
	 * 
	 */
    public FromRGBColorMap(int numComponents)
    {
        // normalized maps
        maps = new float[numComponents][COLORMAP_SIZE];

        // default [0..1]
        for (int comp = 0; comp < numComponents; comp++)
        {
            for (int index = 0; index < COLORMAP_SIZE; index++)
            {
                maps[comp][index] = (float) index / COLORMAP_MAX;
            }
        }
    }

    /**
     * Get normalized component intensity from a normalized input RGB value
     */
    public float getFromRGBColor(int component, float rgbValue)
    {
        return maps[component][(int) (rgbValue * COLORMAP_MAX)];
    }

    /**
     * Get normalized component intensity from a unnormalized input RGB value
     */
    public float getFromRGBColor(int component, int rgbValue)
    {
        return maps[component][rgbValue];
    }

    /**
     * Set normalized component intensity for a normalized input RGB value
     */
    public void setFromRGBColor(int component, float rgbValue, float value)
    {
        maps[component][(int) (rgbValue * COLORMAP_MAX)] = value;
    }

}
