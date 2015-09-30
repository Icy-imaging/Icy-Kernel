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
 * @author Stephane
 */
public class FireColorMap extends IcyColorMap
{
    private static final short[] fire_red = {0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240,
            252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
    private static final short[] fire_green = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133,
            147, 161, 175, 190, 205, 219, 234, 248, 255, 255, 255, 255};
    private static final short[] fire_blue = {31, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255};

    public FireColorMap()
    {
        super("Fire");

        beginUpdate();
        try
        {
            final int nColors = fire_red.length;
            final float scale = (float) IcyColorMap.MAX_INDEX / (nColors - 1);

            for (int i = 0; i < nColors; i++)
            {
                final int index = Math.round(i * scale);
                setRGBControlPoint(index, new Color(fire_red[i], fire_green[i], fire_blue[i]));
            }
        }
        finally
        {
            endUpdate();
        }
    }
}
